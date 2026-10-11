package com.mod.htgt6.asm;

import com.mod.htgt6.common.compat.QuestFluidDisplayHook;
import com.mod.htgt6.common.crusher.CrusherOreImportFixture;
import com.mod.htgt6.common.item.htgt6.fluids.FluidsHTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipFile;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

/** Tests actual BQ bytecode and named-fluid migration without launching a client. */
public final class QuestFluidDisplayFixture {
    private static final String TARGET = "betterquesting.api.utils.BigItemStack";
    public static void main(String[] args) throws Exception {
        CrusherOreImportFixture.initializeGT6();
        Item display = new Item();
        cpw.mods.fml.common.Loader.instance().activeModContainer().getMetadata().modId = "gregapi";
        GameRegistry.registerItem(display, "gt.display.fluid", "gregapi");
        require(Item.itemRegistry.getObject("gregapi:gt.display.fluid") == display, "fixture display registered under GT6 name");
        Fluid wrong = new Fluid("quest_fixture_wrong"); FluidRegistry.registerFluid(wrong);
        Fluid wanted = new Fluid("quest_fixture_wanted"); FluidRegistry.registerFluid(wanted);
        NBTTagCompound original = new NBTTagCompound();
        original.setString("id", "gregapi:gt.display.fluid"); original.setInteger("Count", 7);
        original.setShort("Damage", (short)wrong.getID()); original.setString("OreDict", "");
        NBTTagCompound tag = new NBTTagCompound(); tag.setString("f", wanted.getName());
        tag.setLong("a", 1000); tag.setShort("h", (short)450); original.setTag("tag", tag);
        NBTTagCompound normalized = QuestFluidDisplayHook.normalizeNBT(original);
        require(normalized.getShort("Damage") == wanted.getID(), "name must win over wrong valid ID");
        require(original.getShort("Damage") == wrong.getID(), "input NBT must not be mutated");
        require(normalized.getCompoundTag("tag").equals(tag) && normalized.getInteger("Count") == 7, "preserve payload/count");
        NBTTagCompound ordinary = (NBTTagCompound)original.copy(); ordinary.setString("id", "gregtech:gt.meta.dust");
        require(QuestFluidDisplayHook.normalizeNBT(ordinary) == ordinary, "ordinary material metadata untouched");
        NBTTagCompound missing = (NBTTagCompound)original.copy(); missing.getCompoundTag("tag").setString("f", "missing_quest_fixture");
        require(QuestFluidDisplayHook.normalizeNBT(missing).getShort("Damage") == 32767, "unknown name cannot show stale fluid");

        byte[] bytes;
        try (ZipFile jar = new ZipFile("run/mods/BetterQuesting-3.7.15-GTNH.jar")) {
            try (InputStream in = jar.getInputStream(jar.getEntry(TARGET.replace('.', '/') + ".class"))) {
                ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] buf = new byte[8192];
                for (int n; (n = in.read(buf)) != -1;) out.write(buf,0,n); bytes = out.toByteArray();
            }
        }
        final byte[] patched = verify(bytes);
        Map<String,String> names = new HashMap<String,String>();
        names.put("net/minecraft/item/ItemStack", "add"); names.put("net/minecraft/nbt/NBTTagCompound", "dh");
        ClassWriter obf = new ClassWriter(0);
        new ClassReader(bytes).accept(new RemappingClassAdapter(obf,new SimpleRemapper(names)),ClassReader.EXPAND_FRAMES);
        verify(obf.toByteArray());
        ClassLoader loader = new ClassLoader(QuestFluidDisplayFixture.class.getClassLoader()) {
            @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (!TARGET.equals(name)) return super.loadClass(name,resolve);
                Class<?> type = findLoadedClass(name);
                if (type == null) type = defineClass(name,patched,0,patched.length);
                if (resolve) resolveClass(type); return type;
            }
        };
        Class<?> big = loader.loadClass(TARGET);
        Object stack = big.getMethod("loadItemStackFromNBT",NBTTagCompound.class).invoke(null,original);
        require(stack != null, "BQ must load registered display item");
        Method getter = big.getMethod("getBaseStack");
        ItemStack base = (ItemStack)getter.invoke(stack);
        require(base.getItemDamage() == wanted.getID(), "real BQ load must normalize");
        base.setItemDamage(wrong.getID());
        require(((ItemStack)getter.invoke(stack)).getItemDamage() == wanted.getID(), "rebind after client registry sync");
        NBTTagCompound saved = (NBTTagCompound)big.getMethod("writeToNBT",NBTTagCompound.class).invoke(stack,new NBTTagCompound());
        require(saved.getString("id").equals("gregapi:gt.display.fluid") && saved.getCompoundTag("tag").getString("f").equals(wanted.getName()), "portable BQ roundtrip");

        FluidsHTGT6.initialize();
        for (java.lang.reflect.Field field : FluidsHTGT6.class.getFields()) {
            if (field.getType() != Fluid.class) continue;
            Fluid value = (Fluid)field.get(null);
            require(value != null && FluidRegistry.getFluid(value.getName()) == value, "chemistry field must reference registered fluid: " + field.getName());
        }
        Fluid platinum = FluidsHTGT6.liqPlatinumSol;
        FluidsHTGT6.initialize();
        require(FluidsHTGT6.liqPlatinumSol == platinum, "repeat registration must reuse fluid");
        System.out.println("Quest fluid display PASS: real BQ load/get/save, stale IDs, payload preservation, missing-fluid guard, obfuscated ASM, chemistry registration");
    }
    private static byte[] verify(byte[] bytes) throws Exception {
        HTGT6Transformer transformer = new HTGT6Transformer();
        byte[] patched = transformer.transform(TARGET,TARGET,bytes);
        require(!Arrays.equals(bytes,patched),"dispatch must patch BQ");
        require(Arrays.equals(patched,transformer.transform(TARGET,TARGET,patched)),"idempotent patch");
        ClassNode node = new ClassNode(); new ClassReader(patched).accept(node,0);
        int calls = 0;
        for (MethodNode method : node.methods) {
            boolean hooked = false;
            for (AbstractInsnNode insn : method.instructions.toArray())
                if (insn instanceof MethodInsnNode && QuestFluidDisplayASM.HOOK.equals(((MethodInsnNode)insn).owner)) { calls++; hooked=true; }
            if (hooked) new Analyzer(new BasicVerifier()).analyze(node.name,method);
        }
        require(calls == 2,"expected load and getter hooks"); return patched;
    }
    private static void require(boolean ok,String message) { if (!ok) throw new AssertionError(message); }
}
