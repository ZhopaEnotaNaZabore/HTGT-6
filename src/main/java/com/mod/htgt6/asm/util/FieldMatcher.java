package com.mod.htgt6.asm.util;


import org.objectweb.asm.tree.FieldInsnNode;

public final class FieldMatcher {

    private FieldMatcher(){}

    public static boolean isBiomeArray(FieldInsnNode field) {

        return field.owner.equals("net/minecraft/world/chunk/Chunk")
                && (
                field.name.equals("blockBiomeArray")
                        || field.name.equals("field_76638_b")
        );

    }

}