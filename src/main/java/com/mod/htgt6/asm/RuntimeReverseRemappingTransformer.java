package com.mod.htgt6.asm;

import net.minecraft.launchwrapper.IClassTransformer;

/** Restores notch names immediately after HTGT6Transformer for later coremods. */
public final class RuntimeReverseRemappingTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || isHTGT6Asm(name) || isHTGT6Asm(transformedName)
                || !RuntimeBytecodeRemapper.isProductionBridgeEnabled()
                || !RuntimeBytecodeRemapper.isObfuscatedClass(name, transformedName)) {
            return basicClass;
        }
        return RuntimeBytecodeRemapper.toNotch(basicClass);
    }

    private static boolean isHTGT6Asm(String name) {
        return name != null && name.replace('/', '.').startsWith("com.mod.htgt6.asm.");
    }
}
