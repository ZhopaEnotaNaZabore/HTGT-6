package com.mod.htgt6.asm;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

/** Bytecode contract for the 65536-slot Infinity potion-ID adapter. */
public final class PotionIdASMFixture120 {
    private static final String POTION = "net/minecraft/potion/Potion";
    private static final String EFFECT = "net/minecraft/potion/PotionEffect";
    private static final String ADD = "net/minecraft/network/play/server/S1DPacketEntityEffect";
    private static final String REMOVE = "net/minecraft/network/play/server/S1EPacketRemoveEntityEffect";
    private static final String HANDLER = "net/minecraft/client/network/NetHandlerPlayClient";

    public static void main(String[] args) throws Exception {
        require(args.length == 1, "expected Forge/Minecraft jar");
        HTGT6Config.RUN_MINECRAFT_1_7_9999 = true;
        HTGT6Config.ENABLE_EXTENDED_BIOME_ASM = false;
        ZipFile jar = new ZipFile(args[0]);
        try {
            HTGT6Transformer transformer = new HTGT6Transformer();
            verifyPotion(transform(transformer, jar, POTION));
            verifyEffect(transform(transformer, jar, EFFECT));
            verifyAddPacket(transform(transformer, jar, ADD));
            verifyRemovePacket(transform(transformer, jar, REMOVE));
            verifyHandler(transform(transformer, jar, HANDLER));
        } finally { jar.close(); }
        System.out.println("Infinity potion IDs ASM contract OK: registry, NBT, add/remove packets and client");
    }

    private static ClassNode transform(HTGT6Transformer transformer, ZipFile jar, String name)
            throws Exception {
        ZipEntry entry = jar.getEntry(name + ".class");
        require(entry != null, "class " + name);
        byte[] input = read(jar.getInputStream(entry));
        byte[] output = transformer.transform(name.replace('/', '.'), name.replace('/', '.'), input);
        ClassNode node = new ClassNode(); new ClassReader(output).accept(node, 0); return node;
    }

    private static void verifyPotion(ClassNode node) {
        int expanded = 0;
        for (MethodNode method : node.methods) if ("<clinit>".equals(method.name))
            for (AbstractInsnNode insn : method.instructions.toArray())
                if (insn instanceof TypeInsnNode && insn.getOpcode() == Opcodes.ANEWARRAY
                        && POTION.equals(((TypeInsnNode) insn).desc)
                        && previous(insn) instanceof LdcInsnNode
                        && Integer.valueOf(65536).equals(((LdcInsnNode) previous(insn)).cst)) expanded++;
        require(expanded == 1, "65536-slot potionTypes allocation");
    }

    private static void verifyEffect(ClassNode node) {
        require(calls(node, "setInteger", "(Ljava/lang/String;I)V") == 2,
                "PotionEffect integer Id/Duration writes");
        require(calls(node, "getInteger", "(Ljava/lang/String;)I") >= 2,
                "PotionEffect integer Id/Duration reads");
    }

    private static void verifyAddPacket(ClassNode node) {
        boolean field = false;
        for (FieldNode value : node.fields) if ("htgt6PotionId".equals(value.name) && "I".equals(value.desc)) field = true;
        require(field, "S1D full-width field");
        require(calls(node, "readVarIntFromBuffer", "()I") == 1, "S1D VarInt read");
        require(calls(node, "writeVarIntToBuffer", "(I)V") == 1, "S1D VarInt write");
        require(hasMethod(node, "htgt6$getPotionId120", "()I"), "S1D int getter");
    }

    private static void verifyRemovePacket(ClassNode node) {
        require(calls(node, "readVarIntFromBuffer", "()I") == 1, "S1E VarInt read");
        require(calls(node, "writeVarIntToBuffer", "(I)V") == 1, "S1E VarInt write");
    }

    private static void verifyHandler(ClassNode node) {
        require(calls(node, "htgt6$getPotionId120", "()I") == 1,
                "client constructs PotionEffect with full ID");
    }

    private static int calls(ClassNode node, String name, String desc) {
        int count = 0;
        for (MethodNode method : node.methods) for (AbstractInsnNode insn : method.instructions.toArray())
            if (insn instanceof MethodInsnNode && name.equals(((MethodInsnNode) insn).name)
                    && desc.equals(((MethodInsnNode) insn).desc)) count++;
        return count;
    }
    private static boolean hasMethod(ClassNode node, String name, String desc) {
        for (MethodNode method : node.methods) if (name.equals(method.name) && desc.equals(method.desc)) return true;
        return false;
    }
    private static AbstractInsnNode previous(AbstractInsnNode node) {
        AbstractInsnNode cursor = node.getPrevious();
        while (cursor != null && cursor.getOpcode() < 0) cursor = cursor.getPrevious();
        return cursor;
    }
    private static byte[] read(InputStream input) throws Exception {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream(); byte[] buffer = new byte[4096]; int count;
            while ((count = input.read(buffer)) >= 0) output.write(buffer, 0, count);
            return output.toByteArray();
        } finally { input.close(); }
    }
    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError("Infinity potion IDs mismatch: " + label);
    }
    private PotionIdASMFixture120() { }
}
