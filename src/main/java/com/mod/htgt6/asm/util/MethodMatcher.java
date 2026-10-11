package com.mod.htgt6.asm.util;


import org.objectweb.asm.tree.MethodInsnNode;

public final class MethodMatcher {

    private MethodMatcher(){}

    public static boolean isGetBiomeArray(MethodInsnNode m) {

        return (
                m.name.equals("getBiomeArray")
                        || m.name.equals("func_76605_m")
        );

    }

    public static boolean isSetBiomeArray(MethodInsnNode m) {

        return (
                m.name.equals("setBiomeArray")
                        || m.name.equals("func_76633_a")
        );

    }

}