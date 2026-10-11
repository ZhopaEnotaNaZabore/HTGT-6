package com.mod.htgt6.asm;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Headless contract for the format-neutral ExtendedBlockStorage load fast path. */
public final class ChunkSectionLoadASMFixture120 {
    private static final String CLASS_NAME =
            "net.minecraft.world.chunk.storage.ExtendedBlockStorage";
    private static final String SELF = "com/mod/htgt6/asm/HTGT6Transformer";

    public static void main(String[] ignored) throws Exception {
        primeBlockStates();
        verifyVanillaCounts();
        verifyVanillaInvalidIdFails();
        verifyNotEnoughIdsCounts();
        verifyAsmShape();
        System.out.println("Chunk section load ASM contract OK: packed recount preserves block/tick counts");
    }

    private static void verifyVanillaCounts() {
        ExtendedBlockStorage storage = new ExtendedBlockStorage(0, true);
        byte[] ids = new byte[4096];
        ids[0] = 1;
        ids[1] = 2;
        ids[4095] = 2;
        storage.setBlockLSBArray(ids);
        long counts = HTGT6Transformer.recountChunkSection120(storage);
        require((int) counts == 3, "vanilla non-air count changed");
        require((int) (counts >>> 32) == 2, "vanilla random-tick count changed");
    }

    private static void verifyNotEnoughIdsCounts() {
        short[] ids = {1, 2, 2, 0, (short) 65535};
        long counts = HTGT6Transformer.recountShortBlockIds120(ids, false);
        require((int) counts == 3, "NEID non-air count changed");
        require((int) (counts >>> 32) == 2, "NEID random-tick count changed");
        require((ids[4] & 65535) == 65535, "invalid NEID ID removed while policy disabled");
        HTGT6Transformer.recountShortBlockIds120(ids, true);
        require(ids[4] == 0, "invalid NEID ID retained while policy enabled");
    }

    private static void verifyVanillaInvalidIdFails() {
        ExtendedBlockStorage storage = new ExtendedBlockStorage(0, false);
        byte[] ids = new byte[4096];
        ids[0] = (byte) 255;
        storage.setBlockLSBArray(ids);
        boolean failed = false;
        try {
            HTGT6Transformer.recountChunkSection120(storage);
        } catch (IllegalStateException expected) {
            failed = true;
        }
        require(failed, "unmapped vanilla ID was silently discarded");
    }

    private static void primeBlockStates() throws Exception {
        java.lang.reflect.Field field = HTGT6Transformer.class
                .getDeclaredField("CHUNK_SECTION_BLOCK_STATES");
        field.setAccessible(true);
        byte[] states = (byte[]) field.get(null);
        states[1] = 3;
        states[2] = 4;
        states[255] = 1;
        states[65535] = 1;
    }

    private static void verifyAsmShape() throws Exception {
        byte[] original = readResource("/net/minecraft/world/chunk/storage/ExtendedBlockStorage.class");
        byte[] transformed = new HTGT6Transformer().transform(CLASS_NAME, CLASS_NAME, original);
        ClassNode node = new ClassNode();
        new ClassReader(transformed).accept(node, 0);
        MethodNode recount = null;
        for (MethodNode method : node.methods)
            if ("htgt6$recountBlocksForLoad".equals(method.name)
                    && "()V".equals(method.desc)) recount = method;
        require(recount != null, "section recount bridge absent");
        int helperCalls = 0, countWrites = 0;
        for (AbstractInsnNode instruction : recount.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (SELF.equals(call.owner) && "recountChunkSection120".equals(call.name)) helperCalls++;
            } else if (instruction instanceof FieldInsnNode
                    && instruction.getOpcode() == Opcodes.PUTFIELD
                    && "I".equals(((FieldInsnNode) instruction).desc)) countWrites++;
        }
        require(helperCalls == 1, "packed recount helper was not installed exactly once");
        require(countWrites == 2, "block/tick counters were not both restored");

        byte[] anvilOriginal = readResource(
                "/net/minecraft/world/chunk/storage/AnvilChunkLoader.class");
        byte[] anvilTransformed = new HTGT6Transformer().transform(
                "net.minecraft.world.chunk.storage.AnvilChunkLoader",
                "net.minecraft.world.chunk.storage.AnvilChunkLoader", anvilOriginal);
        ClassNode anvil = new ClassNode();
        new ClassReader(anvilTransformed).accept(anvil, 0);
        int redirected = 0, legacyCalls = 0;
        for (MethodNode method : anvil.methods) for (AbstractInsnNode instruction
                : method.instructions.toArray()) if (instruction instanceof MethodInsnNode) {
            MethodInsnNode call = (MethodInsnNode) instruction;
            if ("htgt6$recountBlocksForLoad".equals(call.name)) redirected++;
            if ("removeInvalidBlocks".equals(call.name) || "func_76672_e".equals(call.name))
                legacyCalls++;
        }
        require(redirected == 1, "Anvil section recount was not redirected exactly once");
        require(legacyCalls == 0, "Anvil still invokes the per-block recount");
    }

    private static byte[] readResource(String path) throws Exception {
        InputStream input = ChunkSectionLoadASMFixture120.class.getResourceAsStream(path);
        require(input != null, "missing runtime ExtendedBlockStorage bytecode");
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

    private ChunkSectionLoadASMFixture120() { }
}
