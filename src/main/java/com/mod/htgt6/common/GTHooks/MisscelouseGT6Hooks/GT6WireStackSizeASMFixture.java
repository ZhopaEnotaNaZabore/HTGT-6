package com.mod.htgt6.common.GTHooks.MisscelouseGT6Hooks;

import com.mod.htgt6.asm.HTGT6Transformer;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Bytecode and configuration contract for HTGT6's fixed GT6 wire stacks. */
public final class GT6WireStackSizeASMFixture {
    private static final String CLASS_NAME = "gregapi.GT_API";
    private static final String ENTRY = "gregapi/GT_API.class";
    private static final String ITEM_CLASS =
            "gregapi.block.multitileentity.MultiTileEntityItemInternal";
    private static final String ITEM_ENTRY =
            "gregapi/block/multitileentity/MultiTileEntityItemInternal.class";
    private static final String SELF = "com/mod/htgt6/asm/HTGT6Transformer";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Expected the GT6 jar path");
        try (ZipFile jar = new ZipFile(args[0])) {
            int redirects = countHook(new HTGT6Transformer().transform(CLASS_NAME, CLASS_NAME,
                    read(jar, ENTRY)), "getGT6StackSizeConfigProperty");
            if (redirects == 0) throw new AssertionError("GT_API stack-size lookup was not redirected");
            int itemHooks = countHook(new HTGT6Transformer().transform(ITEM_CLASS, ITEM_CLASS,
                    read(jar, ITEM_ENTRY)), "getGT6WireStackLimit");
            if (itemHooks != 1) throw new AssertionError("MultiTileEntity item hooks=" + itemHooks);
            System.out.println("GT6 wire stack-size ASM fixture passed; config redirects="
                    + redirects + ", item hooks=" + itemHooks);
        }

        // The no-arg form is deliberately used: Forge's file constructor
        // requires FMLInjectionData and cannot run in this standalone fixture.
        Configuration config = new Configuration();
        config.get("stacksizes", "wireGt16_8", 8);
        config.get("stacksizes", "cableGt12_10", 10);
        Property fixed = HTGT6Transformer.getGT6StackSizeConfigProperty(
                config, "stacksizes", "wireGt16_8", 8);
        if (fixed.getInt() != 64) throw new AssertionError("Wire stack limit is " + fixed.getInt());
        ConfigCategory category = config.getCategory("stacksizes");
        for (String key : category.keySet()) {
            String lower = key.toLowerCase(java.util.Locale.ROOT);
            if (lower.startsWith("wire") || lower.startsWith("cable"))
                throw new AssertionError("Wire key survived cleanup: " + key);
        }
    }

    private static byte[] read(ZipFile jar, String path) throws Exception {
        ZipEntry entry = jar.getEntry(path);
        if (entry == null) throw new AssertionError("Missing " + path);
        try (InputStream input = jar.getInputStream(entry)) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            for (int count; (count = input.read(buffer)) >= 0;) output.write(buffer, 0, count);
            return output.toByteArray();
        }
    }

    private static int countHook(byte[] transformed, String hookName) {
        ClassNode node = new ClassNode();
        new ClassReader(transformed).accept(node, 0);
        int count = 0;
        for (MethodNode method : node.methods) for (AbstractInsnNode instruction
                : method.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (call.getOpcode() == Opcodes.INVOKESTATIC && SELF.equals(call.owner)
                    && hookName.equals(call.name)) count++;
        }
        return count;
    }

    private GT6WireStackSizeASMFixture() { }
}
