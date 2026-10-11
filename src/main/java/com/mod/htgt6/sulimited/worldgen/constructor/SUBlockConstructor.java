package com.mod.htgt6.sulimited.worldgen.constructor;

/** Optional expensive hook, invoked only for existing blocks in the chunk. */
public interface SUBlockConstructor {
    void generate(SUChunkContext context, int localX, int y, int localZ);
}
