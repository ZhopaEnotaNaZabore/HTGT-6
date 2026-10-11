package com.mod.htgt6.common.handler.recipes;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Bytecode contract check; does not initialize SRG GT6 classes outside Forge. */
public final class GT6CrusherDynamicRecipeCheck {
    public static void main(String[] args) throws Exception {
        String special = "gregapi/recipes/maps/RecipeMapSpecialSingleInput";
        Set<String> importer = references("com/mod/htgt6/common/handler/recipes/GT6CrusherRecipeCollector",
                "recipesForInput");
        require(importer.contains("getNEIUsages") && !importer.contains("findRecipe"),
                "Importer must use pre-server enumeration, not gated runtime search");
        Set<String> usages = references(special, "getNEIUsages");
        require(usages.contains("getRecipeFor") && !usages.contains("mFinishedServerStarted"),
                "GT6 usage enumeration no longer supports pre-server generation");
        require(references(special, "findRecipe").contains("mFinishedServerStarted"),
                "Recheck workaround: GT6 runtime startup gate has changed");
        Set<String> stages = references("gregapi/recipes/maps/RecipeMapShredder", "getRecipeFor");
        for (String stage : new String[] {"crushed", "crushedPurified", "crushedCentrifuged",
                "dustImpure", "dustPure", "dust"}) {
            require(stages.contains(stage), "Missing bundled GT6 stage: " + stage);
        }
        System.out.println("GT6CrusherDynamicRecipeCheck: OK (bytecode contract; no game simulation)");
    }

    private static Set<String> references(String type, final String method) throws Exception {
        final Set<String> names = new HashSet<String>();
        try (InputStream stream = GT6CrusherDynamicRecipeCheck.class.getClassLoader()
                .getResourceAsStream(type + ".class")) {
            require(stream != null, "Missing class " + type);
            new ClassReader(stream).accept(new ClassVisitor(Opcodes.ASM5) {
                @Override public MethodVisitor visitMethod(int access, String name, String descriptor,
                        String signature, String[] exceptions) {
                    if (!method.equals(name)) return null;
                    return new MethodVisitor(Opcodes.ASM5) {
                        @Override public void visitMethodInsn(int opcode, String owner, String name,
                                String descriptor, boolean isInterface) { names.add(name); }
                        @Override public void visitFieldInsn(int opcode, String owner, String name,
                                String descriptor) { names.add(name); }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        return names;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
