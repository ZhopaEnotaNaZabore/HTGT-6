package com.mod.htgt6.asm;

import com.mod.htgt6.client.inventory112.OffhandRefillGuard;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import net.minecraft.inventory.*;
import net.minecraft.entity.player.EntityPlayer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

/** Executes the patched slot map from the installed Inventory Tweaks jar. */
public final class OffhandInventoryTweaksFixture {
    public static void main(String[] args) throws Exception {
        final File file = new File(args[0]);
        final Map<String, byte[]> patched = new HashMap<String, byte[]>();
        try (ZipFile jar = new ZipFile(file)) {
            for (String name : new String[]{"invtweaks.containers.VanillaSlotMaps", "invtweaks.InvTweaks"}) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                try (InputStream in = jar.getInputStream(jar.getEntry(name.replace('.', '/') + ".class"))) {
                    byte[] buffer = new byte[8192]; int count;
                    while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
                }
                byte[] result = new HTGT6Transformer().transform(name, name, out.toByteArray());
                ClassNode node = new ClassNode(); new ClassReader(result).accept(node, 0);
                int guardCalls = 0;
                for (MethodNode method : node.methods) {
                    if ("handleAutoRefill".equals(method.name)) {
                        new Analyzer(new BasicVerifier()).analyze(node.name, method);
                        for (AbstractInsnNode insn : method.instructions.toArray())
                            if (insn instanceof MethodInsnNode && "suppress".equals(((MethodInsnNode) insn).name)) guardCalls++;
                    }
                    // Fixture runs on Forge's MCP classpath; the installed mod uses SRG fields.
                    for (AbstractInsnNode insn : method.instructions.toArray())
                        if (insn instanceof FieldInsnNode && "field_75151_b".equals(((FieldInsnNode) insn).name))
                            ((FieldInsnNode) insn).name = "inventorySlots";
                }
                if (name.endsWith("InvTweaks")) require(guardCalls == 1, "refill guard inserted");
                ClassWriter writer = new ClassWriter(0); node.accept(writer);
                patched.put(name, writer.toByteArray());
            }
        }
        try (URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()}, OffhandInventoryTweaksFixture.class.getClassLoader()) {
            @Override protected Class<?> findClass(String name) throws ClassNotFoundException {
                byte[] bytes = patched.get(name);
                return bytes == null ? super.findClass(name) : defineClass(name, bytes, 0, bytes.length);
            }
        }) {
            Class<?> maps = loader.loadClass("invtweaks.containers.VanillaSlotMaps");
            for (int size : new int[]{45, 46}) {
                Container container = new Container() {
                    @Override public boolean canInteractWith(EntityPlayer player) { return true; }
                };
                InventoryBasic inventory = new InventoryBasic("fixture", true, size);
                for (int i = 0; i < size; i++) container.inventorySlots.add(new Slot(inventory, i, 0, 0));
                Map<?, ?> sections = (Map<?, ?>) maps.getMethod("containerPlayerSlots", Container.class).invoke(null, container);
                Map<String, List> byName = new HashMap<String, List>();
                for (Map.Entry<?, ?> entry : sections.entrySet()) byName.put(entry.getKey().toString(), (List) entry.getValue());
                require(byName.get("INVENTORY").size() == 36, "inventory count");
                require(byName.get("INVENTORY_NOT_HOTBAR").size() == 27, "storage count");
                for (int slot = 0; slot < 9; slot++) {
                    require(byName.get("INVENTORY_HOTBAR").get(slot) == container.inventorySlots.get(36 + slot), "hotbar " + slot);
                    require(byName.get("INVENTORY").get(27 + slot) == container.inventorySlots.get(36 + slot), "refill " + slot);
                }
            }
        }
        for (int slot = 0; slot < 9; slot++) {
            OffhandRefillGuard.reset();
            require(!OffhandRefillGuard.suppress(slot), "ordinary refill enabled");
            OffhandRefillGuard.begin(slot); OffhandRefillGuard.begin(slot);
            require(OffhandRefillGuard.suppress(slot), "pending swap");
            OffhandRefillGuard.acknowledge(slot);
            require(OffhandRefillGuard.suppress(slot), "second swap pending");
            require(OffhandRefillGuard.suppress(slot), "pending persists across ticks");
            OffhandRefillGuard.acknowledge(slot);
            require(OffhandRefillGuard.suppress(slot), "acknowledgement baseline refresh");
            require(!OffhandRefillGuard.suppress(slot), "ordinary refill restored");
        }
        OffhandRefillGuard.reset();
        System.out.println("Offhand/Inventory Tweaks OK: actual patched slot map, all nine slots, refill bytecode and transaction guard");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
