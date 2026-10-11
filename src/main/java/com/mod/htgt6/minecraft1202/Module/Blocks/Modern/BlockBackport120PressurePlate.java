package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.material.Material;

/** All-entity wooden pressure plate with modern powered/unpowered JSON models. */
public final class BlockBackport120PressurePlate extends BlockPressurePlate {
    public BlockBackport120PressurePlate(String name, String texture) {
        super("htgt6:1_20_backport/" + texture, com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherWoodMaterial116.forName(name), Sensitivity.everything);
        setBlockName("backport_1_20." + name);
        setHardness(0.5F);
        setResistance(Backport120Content.legacyResistance(0.5F));
        setStepSound(Block.soundTypeWood);
    }

    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
}
