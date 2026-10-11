package com.mod.htgt6.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Bytecode contract for Angelica/Sodium rendering of Futura's 24-section chunks. */
public final class AngelicaHeightASMFixture120 {
    private static final String COLUMN =
            "me/jellysquid/mods/sodium/client/render/chunk/ChunkRenderColumn";
    private static final String MANAGER =
            "me/jellysquid/mods/sodium/client/render/chunk/ChunkRenderManager";
    private static final String CULLER =
            "me/jellysquid/mods/sodium/client/render/chunk/cull/graph/ChunkGraphCuller";
    private static final String CLONED =
            "me/jellysquid/mods/sodium/client/world/cloned/ClonedChunkSection";
    private static final String SLICE =
            "me/jellysquid/mods/sodium/client/world/WorldSlice";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Expected Angelica jar");
        ZipFile jar = new ZipFile(args[0]);
        try {
            HTGT6Transformer transformer = new HTGT6Transformer();
            ClassNode column = transform(transformer, jar, COLUMN);
            require(constants(column, "<init>", 24) == 1, "ChunkRenderColumn is not 24 sections");

            ClassNode manager = transform(transformer, jar, MANAGER);
            require(constants(manager, "loadSections", 24) == 1, "loadSections is not 24 sections");
            require(constants(manager, "unloadSections", 24) == 1, "unloadSections is not 24 sections");
            require(constants(manager, "getTotalSections", 24) == 1, "section total is not 24 per column");

            ClassNode culler = transform(transformer, jar, CULLER);
            require(constants(culler, "initSearch", 23) == 1, "visibility graph still clamps at section 15");

            ClassNode cloned = transform(transformer, jar, CLONED);
            require(constants(cloned, "isOutOfBuildLimitVertically", 384) == 1,
                    "cloned section still rejects Y >= 256");
            ClassNode slice = transform(transformer, jar, SLICE);
            require(constants(slice, "getLightBrightnessForSkyBlocks", 384) == 1,
                    "world slice still rejects light above Y 255");
            require(constants(slice, "getLightLevel", 383) == 1
                            && constants(slice, "getLightLevel", 255) == 0,
                    "world slice still clamps upper-section light to Y 255");
        } finally {
            jar.close();
        }
        System.out.println("Angelica Futura height ASM fixtures OK; sections=24, height=384");
    }

    private static ClassNode transform(HTGT6Transformer transformer, ZipFile jar, String name)
            throws IOException {
        byte[] output = transformer.transform(name, name, read(jar, name + ".class"));
        ClassNode node = new ClassNode();
        new ClassReader(output).accept(node, 0);
        return node;
    }

    private static int constants(ClassNode owner, String methodName, int value) {
        int count = 0;
        for (MethodNode method : owner.methods) {
            if (!methodName.equals(method.name)) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof IntInsnNode
                        && (instruction.getOpcode() == Opcodes.BIPUSH
                        || instruction.getOpcode() == Opcodes.SIPUSH)
                        && ((IntInsnNode) instruction).operand == value) count++;
            }
        }
        return count;
    }

    private static byte[] read(ZipFile jar, String name) throws IOException {
        ZipEntry entry = jar.getEntry(name);
        if (entry == null) throw new IOException("Missing " + name);
        InputStream input = jar.getInputStream(entry);
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            for (int length; (length = input.read(buffer)) >= 0;) output.write(buffer, 0, length);
            return output.toByteArray();
        } finally {
            input.close();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private AngelicaHeightASMFixture120() { }
}
