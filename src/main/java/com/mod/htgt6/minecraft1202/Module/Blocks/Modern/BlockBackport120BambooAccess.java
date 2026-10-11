package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

/** Narrow bridge allowing the worldgen runtime to use the block's canonical metadata layout. */
public final class BlockBackport120BambooAccess {
    public static int state(int age, int leaves, int stage) {
        return BlockBackport120Bamboo.state(age, leaves, stage);
    }
    private BlockBackport120BambooAccess() { }
}
