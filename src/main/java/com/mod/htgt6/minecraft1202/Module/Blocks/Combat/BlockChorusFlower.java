package com.mod.htgt6.minecraft1202.Module.Blocks.Combat;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;

public final class BlockChorusFlower extends Block {
    private IIcon alive;
    private IIcon dead;

    public BlockChorusFlower() {
        super(Material.plants);
        setBlockName("chorus_flower");
        setHardness(0.4F);
        setResistance(Backport120Content.legacyResistance(0.4F));
        setStepSound(soundTypeWood);
        setTickRandomly(true);
        setCreativeTab(CreativeTabs.tabDecorations);
    }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return CombatContent.getChorusRenderId(); }
    @Override public int getRenderBlockPass() { return 0; }
    @Override public void setBlockBoundsBasedOnState(IBlockAccess world,int x,int y,int z){setBlockBounds(0,0,0,1,1,1);}
    @Override public void registerBlockIcons(IIconRegister register) {
        alive = register.registerIcon("htgt6:combat_update/chorus_flower");
        dead = register.registerIcon("htgt6:combat_update/chorus_flower_dead");
        blockIcon = alive;
    }
    @Override public IIcon getIcon(int side, int metadata) { return metadata >= 5 ? dead : alive; }
    @Override public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return super.canPlaceBlockAt(world,x,y,z)&&canSurvive(world,x,y,z);
    }
    @Override public void updateTick(World world, int x, int y, int z, Random random) {
        if (world.isRemote) return;
        int age = world.getBlockMetadata(x, y, z);
        if(!canSurvive(world,x,y,z)){world.func_147480_a(x,y,z,false);return;}
        if(age>=5||y>=world.getHeight()-1||!world.isAirBlock(x,y+1,z))return;
        Block below=world.getBlock(x,y-1,z);boolean grow=false,rooted=false;
        if(below==Blocks.end_stone){grow=true;}
        else if(below!=CombatContent.chorusPlant){grow=world.isAirBlock(x,y-1,z);}
        else {int stem=1;for(int d=2;d<=5;d++){Block b=world.getBlock(x,y-d,z);if(b!=CombatContent.chorusPlant){rooted=b==Blocks.end_stone;break;}stem++;}grow=stem<2||random.nextInt(rooted?5:4)>=stem;}
        if(grow&&horizontalClear(world,x,y+1,z,-1)&&world.isAirBlock(x,y+2,z)){
            world.setBlock(x,y,z,CombatContent.chorusPlant,0,2);world.setBlock(x,y+1,z,this,age,2);return;
        }
        if(age>=4){world.setBlockMetadataWithNotify(x,y,z,5,2);return;}
        int attempts=random.nextInt(4)+(rooted?1:0);boolean branched=false;
        for(int i=0;i<attempts;i++){int side=2+random.nextInt(4),nx=x+net.minecraft.util.Facing.offsetsXForSide[side],nz=z+net.minecraft.util.Facing.offsetsZForSide[side];if(world.isAirBlock(nx,y,nz)&&world.isAirBlock(nx,y-1,nz)&&horizontalClear(world,nx,y,nz,net.minecraft.util.Facing.oppositeSide[side])){world.setBlock(nx,y,nz,this,age+1,2);branched=true;}}
        if(branched)world.setBlock(x,y,z,CombatContent.chorusPlant,0,2);else world.setBlockMetadataWithNotify(x,y,z,5,2);
    }
    private boolean canSurvive(World world,int x,int y,int z){Block below=world.getBlock(x,y-1,z);if(below==Blocks.end_stone||below==CombatContent.chorusPlant)return true;if(!world.isAirBlock(x,y-1,z))return false;int plants=0;for(int s=2;s<6;s++){Block b=world.getBlock(x+net.minecraft.util.Facing.offsetsXForSide[s],y,z+net.minecraft.util.Facing.offsetsZForSide[s]);if(b==CombatContent.chorusPlant)plants++;else if(!world.isAirBlock(x+net.minecraft.util.Facing.offsetsXForSide[s],y,z+net.minecraft.util.Facing.offsetsZForSide[s]))return false;}return plants==1;}
    private boolean horizontalClear(World world,int x,int y,int z,int except){for(int s=2;s<6;s++)if(s!=except&&!world.isAirBlock(x+net.minecraft.util.Facing.offsetsXForSide[s],y,z+net.minecraft.util.Facing.offsetsZForSide[s]))return false;return true;}
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block block){if(!canSurvive(world,x,y,z))world.scheduleBlockUpdate(x,y,z,this,1);}
    @Override public Item getItemDropped(int metadata, Random random, int fortune) { return Item.getItemFromBlock(this); }
}
