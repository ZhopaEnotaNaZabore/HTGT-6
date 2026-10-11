package com.mod.htgt6.sulimited.worldgen.constructor;

/** Runs once for each local X/Z column after chunk constructors. */
public interface SUColumnConstructor {
    void generate(SUChunkContext context, int localX, int localZ);
}
