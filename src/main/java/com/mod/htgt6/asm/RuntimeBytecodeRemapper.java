package com.mod.htgt6.asm;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.common.asm.transformers.deobf.FMLRemappingAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/** Converts only the class currently patched by HTGT6 between notch and SRG. */
final class RuntimeBytecodeRemapper {

    private static final FMLDeobfuscatingRemapper FML = FMLDeobfuscatingRemapper.INSTANCE;
    private static volatile ReverseMappings reverseMappings;
    private static volatile boolean productionBridgeEnabled;

    private RuntimeBytecodeRemapper() {}

    static void enableProductionBridge() {
        productionBridgeEnabled = true;
    }

    static boolean isProductionBridgeEnabled() {
        return productionBridgeEnabled;
    }

    static boolean isObfuscatedClass(String name, String transformedName) {
        if (name == null || transformedName == null) return false;
        return !name.replace('/', '.').equals(transformedName.replace('/', '.'));
    }

    static byte[] toSrg(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        reader.accept(new FMLRemappingAdapter(writer), ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    static byte[] toNotch(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        reader.accept(new RemappingClassAdapter(writer, reverse()), ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    private static Remapper reverse() {
        ReverseMappings current = reverseMappings;
        if (current != null) return current;
        synchronized (RuntimeBytecodeRemapper.class) {
            current = reverseMappings;
            if (current == null) {
                current = new ReverseMappings(readRawMap("rawMethodMaps"), readRawMap("rawFieldMaps"));
                reverseMappings = current;
            }
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Map<String, String>> readRawMap(String fieldName) {
        try {
            Field field = FMLDeobfuscatingRemapper.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (Map<String, Map<String, String>>) field.get(FML);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to read Forge runtime mappings: " + fieldName, e);
        }
    }

    private static final class ReverseMappings extends Remapper {
        private final Map<String, String> methods = new HashMap<String, String>();
        private final Map<String, String> globalMethods = new HashMap<String, String>();
        private final Map<String, String> fields = new HashMap<String, String>();
        private final Map<String, String> globalFields = new HashMap<String, String>();

        ReverseMappings(Map<String, Map<String, String>> rawMethods,
                        Map<String, Map<String, String>> rawFields) {
            if (rawMethods != null) {
                for (Map.Entry<String, Map<String, String>> ownerEntry : rawMethods.entrySet()) {
                    String ownerSrg = FML.map(ownerEntry.getKey());
                    for (Map.Entry<String, String> entry : ownerEntry.getValue().entrySet()) {
                        int descriptorStart = entry.getKey().indexOf('(');
                        if (descriptorStart < 0) continue;
                        String notchName = entry.getKey().substring(0, descriptorStart);
                        String srgDesc = FML.mapMethodDesc(entry.getKey().substring(descriptorStart));
                        put(methods, memberKey(ownerSrg, entry.getValue(), srgDesc), notchName);
                        putUnique(globalMethods, entry.getValue() + '\u0000' + srgDesc, notchName);
                    }
                }
            }
            if (rawFields != null) {
                for (Map.Entry<String, Map<String, String>> ownerEntry : rawFields.entrySet()) {
                    String ownerSrg = FML.map(ownerEntry.getKey());
                    for (Map.Entry<String, String> entry : ownerEntry.getValue().entrySet()) {
                        int descriptorStart = entry.getKey().lastIndexOf(':');
                        if (descriptorStart < 0) continue;
                        String notchName = entry.getKey().substring(0, descriptorStart);
                        String srgDesc = FML.mapDesc(entry.getKey().substring(descriptorStart + 1));
                        put(fields, memberKey(ownerSrg, entry.getValue(), srgDesc), notchName);
                        putUnique(globalFields, entry.getValue() + '\u0000' + srgDesc, notchName);
                    }
                }
            }
        }

        @Override
        public String map(String internalName) {
            return FML.unmap(internalName);
        }

        @Override
        public String mapMethodName(String owner, String name, String desc) {
            String mapped = methods.get(memberKey(owner, name, desc));
            if (mapped == null) mapped = globalMethods.get(name + '\u0000' + desc);
            return mapped == null || mapped.isEmpty() ? name : mapped;
        }

        @Override
        public String mapFieldName(String owner, String name, String desc) {
            String mapped = fields.get(memberKey(owner, name, desc));
            if (mapped == null) mapped = globalFields.get(name + '\u0000' + desc);
            return mapped == null || mapped.isEmpty() ? name : mapped;
        }

        private static String memberKey(String owner, String name, String desc) {
            return owner + '\u0000' + name + '\u0000' + desc;
        }

        private static void put(Map<String, String> target, String key, String value) {
            target.put(key, value);
        }

        private static void putUnique(Map<String, String> target, String key, String value) {
            if (!target.containsKey(key)) target.put(key, value);
            else if (!value.equals(target.get(key))) target.put(key, "");
        }
    }
}
