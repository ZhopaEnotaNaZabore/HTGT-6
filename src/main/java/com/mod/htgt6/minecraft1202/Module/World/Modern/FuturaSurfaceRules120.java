package com.mod.htgt6.minecraft1202.Module.World.Modern;

import net.minecraft.block.Block;

/** Packed block state used by the ported 1.20.2 surface-rule evaluator. */
public final class FuturaSurfaceRules120 {
    public static final class State {
        public final Block block;
        public final int metadata;
        State(Block block, int metadata) { this.block = block; this.metadata = metadata; }
    }
    private FuturaSurfaceRules120() { }
}
