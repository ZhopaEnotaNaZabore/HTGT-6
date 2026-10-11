package com.mod.htgt6.minecraft1202.Module.Blocks.Combat;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public final class BlockEndRod extends Block {
    public BlockEndRod() {
        super(Material.circuits);
        setBlockName("end_rod");
        setBlockTextureName("htgt6:combat_update/end_rod");
        setHardness(0.0F);
        setLightLevel(0.9375F);
        setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(0.375F, 0.0F, 0.375F, 0.625F, 1.0F, 0.625F);
    }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return CombatContent.getEndRodRenderId(); }
    @Override public int damageDropped(int metadata) { return 0; }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side,
                             float hitX, float hitY, float hitZ, int metadata) {
        if(side<0||side>5)return 1;
        int bx=x-net.minecraft.util.Facing.offsetsXForSide[side],by=y-net.minecraft.util.Facing.offsetsYForSide[side],bz=z-net.minecraft.util.Facing.offsetsZForSide[side];
        return world.getBlock(bx,by,bz)==this&&world.getBlockMetadata(bx,by,bz)==side?net.minecraft.util.Facing.oppositeSide[side]:side;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        switch (world.getBlockMetadata(x, y, z)) {
            case 2:
            case 3:
                setBlockBounds(0.375F, 0.375F, 0.0F, 0.625F, 0.625F, 1.0F);
                break;
            case 4:
            case 5:
                setBlockBounds(0.0F, 0.375F, 0.375F, 1.0F, 0.625F, 0.625F);
                break;
            default:
                setBlockBounds(0.375F, 0.0F, 0.375F, 0.625F, 1.0F, 0.625F);
        }
    }
}
