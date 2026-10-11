package com.mod.htgt6.sulimited.block;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
/** Authored albedos without a second index-texture tint. */
public final class GlieseTerrainBlock extends Block {
    public GlieseTerrainBlock(Material material,String texture,float hardness) {
        super(material); setBlockTextureName("htgt6:gliese/"+texture); setHardness(hardness); setResistance(hardness*3);
        boolean soft=material==Material.ground || material==Material.sand;
        setStepSound(soft?soundTypeGravel:soundTypeStone); setHarvestLevel(soft?"shovel":"pickaxe",0);
    }
}
