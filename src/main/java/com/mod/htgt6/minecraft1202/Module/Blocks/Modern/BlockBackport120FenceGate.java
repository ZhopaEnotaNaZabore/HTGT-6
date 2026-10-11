package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.client.renderer.texture.IIconRegister;

/** Native 1.7.10 gate interaction/collision with modern JSON state rendering. */
public final class BlockBackport120FenceGate extends BlockFenceGate {
    private boolean netherMaterial;
    @Override public net.minecraft.block.material.Material getMaterial(){return netherMaterial?com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherWoodMaterial116.MATERIAL:super.getMaterial();}

    private final String texture;

    public BlockBackport120FenceGate(String name, String texture) {
        this.texture = texture;
        netherMaterial=com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherWoodMaterial116.matches(name);
        setBlockName("backport_1_20." + name);
        setHardness(2.0F);
        setResistance(Backport120Content.legacyResistance(3.0F));
        setStepSound(Block.soundTypeWood);
    }

    @Override public void registerBlockIcons(IIconRegister register) {
        blockIcon = register.registerIcon("htgt6:1_20_backport/" + texture);
    }

    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
}
