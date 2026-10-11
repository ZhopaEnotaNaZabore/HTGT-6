package com.mod.htgt6.asm;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Headless contract for the vanilla ItemMap locked-cartography ASM guard. */
public final class LockedMapASMFixture120 {
    private static final String CLASS_NAME = "net.minecraft.item.ItemMap";
    private static final String METHOD_DESC = "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;"
            + "Lnet/minecraft/entity/Entity;IZ)V";
    private static final String SELF = "com/mod/htgt6/asm/HTGT6Transformer";

    public static void main(String[] ignored) throws Exception {
        verifyNbtSemantics();
        byte[] original = readResource("/net/minecraft/item/ItemMap.class");
        byte[] transformed = new HTGT6Transformer().transform(CLASS_NAME, CLASS_NAME, original);
        verifyGuard(new ClassReader(original), new ClassReader(transformed));
        System.out.println("Locked map ASM contract OK: terrain freezes while marker updates remain vanilla");
    }

    private static void verifyNbtSemantics() {
        require(!HTGT6Transformer.isLockedMap120(null), "null stack changed");
        ItemStack ordinary = new ItemStack(new Item());
        require(!HTGT6Transformer.isLockedMap120(ordinary), "ordinary map changed");
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("map_is_scaling", true);
        ordinary.setTagCompound(tag);
        require(!HTGT6Transformer.isLockedMap120(ordinary), "unrelated cartography tag changed");
        tag.setBoolean("map_is_locked", true);
        require(HTGT6Transformer.isLockedMap120(ordinary), "locked marker ignored");
        tag.setBoolean("map_is_locked", false);
        require(!HTGT6Transformer.isLockedMap120(ordinary), "false locked marker treated as locked");

        CountingMap map = new CountingMap();
        HTGT6Transformer.updateMapDataUnlessLocked120(map, null, null, null, ordinary);
        require(map.updates == 1, "ordinary map terrain update suppressed");
        tag.setBoolean("map_is_locked", true);
        HTGT6Transformer.updateMapDataUnlessLocked120(map, null, null, null, ordinary);
        require(map.updates == 1, "locked map terrain update executed");
    }

    private static void verifyGuard(ClassReader originalReader, ClassReader transformedReader) {
        MethodNode original = updateMethod(originalReader);
        MethodNode transformed = updateMethod(transformedReader);
        require(firstCode(original.instructions.getFirst()).getOpcode()
                        == firstCode(transformed.instructions.getFirst()).getOpcode(),
                "onUpdate received an early method guard");
        require(opcodeCount(original, Opcodes.RETURN) == opcodeCount(transformed, Opcodes.RETURN),
                "onUpdate received an early return");
        require(callCount(original, "updateVisiblePlayers") == 1
                        && callCount(transformed, "updateVisiblePlayers") == 1,
                "vanilla player/marker tracking changed");
        require(callCount(transformed, "isLockedMap120") == 0,
                "locking was applied to all of onUpdate instead of terrain update");

        MethodInsnNode wrapper = null;
        for (AbstractInsnNode instruction : transformed.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode)instruction;
            if (SELF.equals(call.owner) && "updateMapDataUnlessLocked120".equals(call.name)) {
                require(wrapper == null, "terrain update wrapped more than once");
                wrapper = call;
            }
        }
        require(wrapper != null && wrapper.getOpcode() == Opcodes.INVOKESTATIC,
                "terrain-only locked-map wrapper absent");
        AbstractInsnNode stackLoad = firstCode(wrapper.getPrevious());
        require(stackLoad instanceof VarInsnNode && stackLoad.getOpcode() == Opcodes.ALOAD
                && ((VarInsnNode)stackLoad).var == 1, "wrapper does not receive current ItemStack");
    }

    private static MethodNode updateMethod(ClassReader reader) {
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        MethodNode result = null;
        for (MethodNode method : node.methods) if (METHOD_DESC.equals(method.desc)
                && ("onUpdate".equals(method.name) || "func_77663_a".equals(method.name)
                || "a".equals(method.name))) {
            require(result == null, "ambiguous ItemMap update method");
            result = method;
        }
        require(result != null, "ItemMap update method absent");
        return result;
    }

    private static int opcodeCount(MethodNode method, int opcode) {
        int count = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray())
            if (instruction.getOpcode() == opcode) count++;
        return count;
    }

    private static int callCount(MethodNode method, String name) {
        int count = 0;
        for (AbstractInsnNode instruction : method.instructions.toArray())
            if (instruction instanceof MethodInsnNode && name.equals(((MethodInsnNode)instruction).name)) count++;
        return count;
    }

    private static AbstractInsnNode firstCode(AbstractInsnNode node) {
        while (node != null && node.getOpcode() < 0) node = node.getNext();
        return node;
    }

    private static byte[] readResource(String path) throws Exception {
        InputStream input = LockedMapASMFixture120.class.getResourceAsStream(path);
        require(input != null, "missing runtime ItemMap bytecode");
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            for (int read; (read = input.read(buffer)) >= 0;) output.write(buffer, 0, read);
            return output.toByteArray();
        } finally { input.close(); }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private static final class CountingMap extends ItemMap {
        int updates;
        @Override public void updateMapData(World world, Entity entity, MapData data) { updates++; }
    }

    private LockedMapASMFixture120() { }
}
