package com.mod.htgt6.asm;

import net.minecraft.launchwrapper.IClassTransformer;

import java.lang.reflect.Method;

public class HTGT6Transformer implements IClassTransformer {

    @Override
    public byte[] transform(
            String name,
            String transformedName,
            byte[] basicClass
    ) {

        if (basicClass == null)
            return null;

        if (!TransformRouter.hasTransformer(transformedName))
            return basicClass;

        try {

            Class<?> transformer =
                    TransformRouter.getTransformer(transformedName);

            Method transform =
                    transformer.getMethod(
                            "transform",
                            byte[].class
                    );

            System.out.println(
                    "[HTGT6 ASM] Transforming -> "
                            + transformedName
            );

            return (byte[]) transform.invoke(
                    null,
                    basicClass
            );

        } catch (Throwable t) {

            System.out.println(
                    "[HTGT6 ASM] FAILED -> "
                            + transformedName
            );

            t.printStackTrace();

            return basicClass;
        }
    }
}
