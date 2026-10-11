package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;
import com.mod.htgt6.minecraft1202.Module.Blocks.Shared.BlockLantern120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
/** Soul lantern shares attachment and geometry while retaining its light level and identity. */
public final class BlockBackport120SoulLantern extends BlockLantern120 {
    public BlockBackport120SoulLantern(){super("backport_1_20.soul_lantern","htgt6:1_20_backport/soul_lantern",10F/15F);}
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
}
