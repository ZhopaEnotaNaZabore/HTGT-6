package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;

/** Wooden trapdoor retaining the complete legacy facing/open/top metadata layout. */
public final class BlockBackport120TrapDoor extends BlockTrapDoor {
    public BlockBackport120TrapDoor(String name) {
        super(com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherWoodMaterial116.forName(name));
        setBlockName("backport_1_20." + name);
        setBlockTextureName("htgt6:1_20_backport/"+name);
        setHardness(3.0F);
        setResistance(Backport120Content.legacyResistance(3.0F));
        setStepSound(Block.soundTypeWood);
    }

    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
}
