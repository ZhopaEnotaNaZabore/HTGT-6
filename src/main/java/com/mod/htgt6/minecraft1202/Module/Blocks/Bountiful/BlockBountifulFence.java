package com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;

/** 1.8 wooden fence: connects to every wooden-fence family and fence gate. */
public final class BlockBountifulFence extends BlockFence {
    public BlockBountifulFence(String wood,String publicName){
        super("minecraft:planks_"+wood,Material.wood);
        setBlockName(publicName+"_fence");setHardness(2.0F);setResistance(5.0F);
        setStepSound(Block.soundTypeWood);setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);
    }
    @Override public boolean canConnectFenceTo(IBlockAccess world,int x,int y,int z){
        Block block=world.getBlock(x,y,z);
        if(block instanceof BlockFence)return block.getMaterial()==Material.wood;
        if(block instanceof net.minecraft.block.BlockFenceGate)return true;
        return block.getMaterial().isOpaque()&&block.renderAsNormalBlock()&&block.getMaterial()!=Material.gourd;
    }
}
