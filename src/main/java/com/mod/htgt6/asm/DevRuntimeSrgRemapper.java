package com.mod.htgt6.asm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

/**
 * Makes normal SRG-named release mods usable in ForgeGradle's MCP runtime.
 *
 * <p>The transformer is inert in an assembled/production client. It exists so
 * the standard {@code runClient} task can use the unmodified final-modpack jars
 * in {@code run/mods}, instead of maintaining a second set of dev jars.</p>
 */
public final class DevRuntimeSrgRemapper implements IClassTransformer {

    private static final Logger LOGGER = LogManager.getLogger("HTGT6-DevRemapper");
    private static final String MAPPINGS_PROPERTY = "htgt6.devMcpMappings";

    private final MappingRemapper remapper;

    public DevRuntimeSrgRemapper() {
        Object deobfuscated = Launch.blackboard.get("fml.deobfuscatedEnvironment");
        String mappingsPath = System.getProperty(MAPPINGS_PROPERTY);
        if (!Boolean.TRUE.equals(deobfuscated) || mappingsPath == null || mappingsPath.isEmpty()) {
            remapper = null;
            return;
        }

        File mappings = new File(mappingsPath);
        if (!mappings.isFile()) {
            throw new IllegalStateException("Missing ForgeGradle SRG-to-MCP mappings: " + mappings);
        }
        try {
            remapper = MappingRemapper.read(mappings);
            LOGGER.info("Enabled standard runClient SRG-to-MCP compatibility ({} fields, {} methods)",
                    remapper.fieldCount(), remapper.methodCount());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read ForgeGradle SRG-to-MCP mappings: " + mappings, e);
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || name == null || name.startsWith("com.mod.htgt6.asm.")) {
            return basicClass;
        }
        if (remapper == null) {
            return RuntimeBytecodeRemapper.isProductionBridgeEnabled()
                    && RuntimeBytecodeRemapper.isObfuscatedClass(name, transformedName)
                    ? RuntimeBytecodeRemapper.toSrg(basicClass) : basicClass;
        }
        ClassReader reader = new ClassReader(basicClass);
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new RemappingClassAdapter(writer, remapper), ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    private static final class MappingRemapper extends Remapper {
        private final Map<String, String> classes = new HashMap<String, String>();
        private final Map<String, String> fields = new HashMap<String, String>();
        private final Map<String, String> methods = new HashMap<String, String>();
        private final Map<String, String> inheritedFields = new HashMap<String, String>();
        private final Map<String, String> inheritedMethods = new HashMap<String, String>();

        static MappingRemapper read(File file) throws IOException {
            MappingRemapper result = new MappingRemapper();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] part = line.trim().split("\\s+");
                    if (part.length == 3 && "CL:".equals(part[0])) {
                        result.classes.put(part[1], part[2]);
                    } else if (part.length == 3 && "FD:".equals(part[0])) {
                        String sourceName = simpleName(part[1]);
                        String mappedName = simpleName(part[2]);
                        result.fields.put(memberKey(owner(part[1]), sourceName), mappedName);
                        // SRG field identifiers are globally stable. Keep an
                        // owner-independent fallback for bytecode which emits
                        // an inherited field against the concrete subclass.
                        result.inheritedFields.put(sourceName, mappedName);
                    } else if (part.length == 5 && "MD:".equals(part[0])) {
                        String sourceName = simpleName(part[1]);
                        String mappedName = simpleName(part[3]);
                        result.methods.put(methodKey(owner(part[1]), sourceName, part[2]), mappedName);
                        // invokevirtual commonly names the actual subclass as
                        // owner even when the SRG method is declared by Item,
                        // Block, Material, etc. ForgeGradle's dev runtime needs
                        // the same inheritance fallback as SpecialSource.
                        result.inheritedMethods.put(methodSignature(sourceName, part[2]), mappedName);
                    }
                }
            } finally {
                reader.close();
            }
            return result;
        }

        @Override
        public String map(String typeName) {
            String mapped = classes.get(typeName);
            return mapped == null ? typeName : mapped;
        }

        @Override
        public String mapFieldName(String owner, String name, String desc) {
            String mapped = fields.get(memberKey(owner, name));
            if (mapped == null) mapped = inheritedFields.get(name);
            return mapped == null ? name : mapped;
        }

        @Override
        public String mapMethodName(String owner, String name, String desc) {
            String mapped = methods.get(methodKey(owner, name, desc));
            if (mapped == null) mapped = inheritedMethods.get(methodSignature(name, desc));
            return mapped == null ? name : mapped;
        }

        int fieldCount() {
            return fields.size();
        }

        int methodCount() {
            return methods.size();
        }

        private static String owner(String member) {
            return member.substring(0, member.lastIndexOf('/'));
        }

        private static String simpleName(String member) {
            return member.substring(member.lastIndexOf('/') + 1);
        }

        private static String memberKey(String owner, String name) {
            return owner + '\n' + name;
        }

        private static String methodKey(String owner, String name, String desc) {
            return owner + '\n' + name + '\n' + desc;
        }

        private static String methodSignature(String name, String desc) {
            return name + '\n' + desc;
        }
    }
}
