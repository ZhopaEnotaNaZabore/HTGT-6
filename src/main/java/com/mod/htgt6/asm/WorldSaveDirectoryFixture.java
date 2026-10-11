package com.mod.htgt6.asm;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Arrays;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.SaveFormatOld;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

/** Regression for the Windows mapped-session.lock startup failure, without touching real saves. */
public final class WorldSaveDirectoryFixture {
    public static void main(String[] args) throws Exception {
        File base = Files.createTempDirectory(new File("tmp").toPath(), "save-directory-").toFile();
        AnvilSaveConverter format = new AnvilSaveConverter(base);
        File existing = new File(base, "existing");
        require(existing.mkdir(), "test directory");
        File lock = new File(existing, "session.lock");
        byte[] sentinel = {1,2,3,4,5,6,7,8};
        Files.write(lock.toPath(), sentinel);
        try (RandomAccessFile file = new RandomAccessFile(lock, "rw")) {
            MappedByteBuffer mapped = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 8);
            for (int i = 0; i < 4; i++) require(WorldSaveDirectoryHooks.resolve(format, "existing").equals(existing), "existing path");
            require(Arrays.equals(Files.readAllBytes(lock.toPath()), sentinel), "directory lookup changed session owner");
            require(mapped.get(0) == 1, "mapped lock changed");
            if (System.getProperty("os.name").startsWith("Windows")) {
                boolean blocked = false;
                try (FileOutputStream ignored = new FileOutputStream(lock)) { }
                catch (IOException expected) { blocked = true; }
                require(blocked, "fixture did not reproduce Windows mapped-file truncation failure");
            }
        }
        File fresh = WorldSaveDirectoryHooks.resolve(format, "new-world");
        require(fresh.isDirectory() && !new File(fresh, "session.lock").exists(), "new-world lookup opened a save session");
        require(new File(fresh, "AE2").mkdir(), "early mod directory creation");
        File legacy = WorldSaveDirectoryHooks.resolve(new SaveFormatOld(base), "legacy-world");
        require(legacy.isDirectory() && !new File(legacy, "session.lock").exists(), "legacy format lookup");
        // Actual world opening still writes a session lock through the unmodified SaveHandler.
        format.getSaveLoader("new-world", false);
        require(new File(fresh, "session.lock").length() == 8, "real world opening lost session protection");
        verifyTransform();
        System.out.println("World save-directory regression OK: mapped lock preserved, existing/new/legacy worlds, AE2 child, real session creation and ASM");
    }

    private static void verifyTransform() throws Exception {
        String target = "net/minecraftforge/common/DimensionManager";
        byte[] original;
        try (InputStream in = WorldSaveDirectoryFixture.class.getResourceAsStream("/" + target + ".class");
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192]; int size;
            while ((size = in.read(buffer)) != -1) out.write(buffer, 0, size);
            original = out.toByteArray();
        }
        HTGT6Transformer transformer = new HTGT6Transformer();
        byte[] patched = transformer.transform(target, target, original);
        patched = transformer.transform(target, target, patched);
        ClassNode node = new ClassNode(); new ClassReader(patched).accept(node, 0);
        int hooks = 0;
        for (MethodNode method : node.methods) {
            if (!method.name.equals("getCurrentSaveRootDirectory")) continue;
            new Analyzer(new BasicInterpreter()).analyze(node.name, method);
            for (AbstractInsnNode instruction : method.instructions.toArray()) if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                require(call.owner.equals("com/mod/htgt6/asm/WorldSaveDirectoryHooks") && call.name.equals("current"), "unexpected lookup call");
                hooks++;
            }
        }
        require(hooks == 1, "missing or duplicate directory hook");
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
