package com.mod.htgt6.sulimited.worldgen.constructor;

/** Runs once after the primary terrain sampler has built a chunk. */
public interface SUChunkConstructor {
    void generate(SUChunkContext context);
}
