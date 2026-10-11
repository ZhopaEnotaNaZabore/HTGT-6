package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import com.mod.htgt6.minecraft1202.Module.World.Modern.WorldGenBackport120MangroveTree;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

/** Lower three bits are hanging age 0..4; bit 3 marks a hanging propagule. */
public final class BlockBackport120MangrovePropagule extends BlockBush implements IGrowable {
    static final int HANGING = 8;
    public BlockBackport120MangrovePropagule(){
        setBlockName("backport_1_20.mangrove_propagule");setHardness(0.0F);
        setStepSound(Block.soundTypeGrass);setCreativeTab(CreativeTabs.tabDecorations);setTickRandomly(true);
        setBlockBounds(0.25F,0,0.25F,0.75F,1,0.75F);
    }
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    @Override public void registerBlockIcons(IIconRegister register) {
        blockIcon = register.registerIcon("htgt6:1_20_backport/mangrove_propagule");
    }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world,int x,int y,int z){return null;}
    static boolean hanging(int metadata){return(metadata&HANGING)!=0;}
    static int age(int metadata){return metadata&7;}
    @Override public boolean canBlockStay(World world,int x,int y,int z){
        int state=world.getBlockMetadata(x,y,z);
        return hanging(state)?world.getBlock(x,y+1,z)==Backport120Content.mangroveLeaves
                :super.canBlockStay(world,x,y,z);
    }
    @Override protected boolean canPlaceBlockOn(Block block){
        return block==Blocks.grass||block==Blocks.dirt||block==Backport120Content.mud
                ||block==Backport120Content.muddyMangroveRoots;
    }
    @Override public void updateTick(World world,int x,int y,int z,Random random){
        super.updateTick(world,x,y,z,random);
        if(world.getBlock(x,y,z)!=this)return;
        if(world.isRemote)return;int state=world.getBlockMetadata(x,y,z);
        if(hanging(state)){if(age(state)<4&&random.nextInt(3)==0)world.setBlockMetadataWithNotify(x,y,z,HANGING+age(state)+1,3);}
        else if(world.getBlockLightValue(x,y+1,z)>=9&&canGrowHere(world,x,y,z)&&random.nextInt(7)==0)grow(world,random,x,y,z);
    }
    @Override public boolean func_149851_a(World world,int x,int y,int z,boolean client){return canGrowHere(world,x,y,z);}
    @Override public boolean func_149852_a(World world,Random random,int x,int y,int z){return true;}
    @Override public void func_149853_b(World world,Random random,int x,int y,int z){grow(world,random,x,y,z);}
    private void grow(World world,Random random,int x,int y,int z){
        if(!canGrowHere(world,x,y,z))return;
        if(!net.minecraftforge.event.terraingen.TerrainGen.saplingGrowTree(world,random,x,y,z))return;
        new WorldGenBackport120MangroveTree().generate(world,random,x,y,z);
    }
    private boolean canGrowHere(World world,int x,int y,int z){
        return world.getBlock(x,y,z)==this&&!hanging(world.getBlockMetadata(x,y,z))
                &&canBlockStay(world,x,y,z)&&y+WorldGenBackport120MangroveTree.MAX_TREE_ABOVE<world.getHeight();
    }
}
