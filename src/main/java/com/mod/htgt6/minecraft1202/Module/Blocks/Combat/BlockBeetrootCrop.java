package com.mod.htgt6.minecraft1202.Module.Blocks.Combat;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;

import net.minecraft.block.BlockCrops;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;

public final class BlockBeetrootCrop extends BlockCrops {
    private final IIcon[] stages = new IIcon[4];
    public BlockBeetrootCrop() { setBlockName("beetroots"); }
    @Override public void registerBlockIcons(IIconRegister register) {
        for (int i = 0; i < 4; ++i) stages[i] = register.registerIcon("htgt6:combat_update/beetroots_stage_" + i);
        blockIcon = stages[0];
    }
    /** Retain the existing NBT mapper's 0,2,4,7 encoding while exposing four growth ages. */
    public static int age(int metadata){return metadata>=7?3:Math.max(0,Math.min(2,metadata/2));}
    public static int metadata(int age){return Math.max(0,Math.min(3,age))*7/3;}
    @Override public IIcon getIcon(int side, int metadata) { return stages[age(metadata)]; }
    @Override public void setBlockBoundsBasedOnState(net.minecraft.world.IBlockAccess world,int x,int y,int z){setBlockBounds(0,0,0,1,(age(world.getBlockMetadata(x,y,z))+1)*.125F,1);}
    @Override public void updateTick(net.minecraft.world.World world,int x,int y,int z,java.util.Random random){
        if(world.isRemote||random.nextInt(3)==0)return;
        int previous=world.getBlockMetadata(x,y,z);
        super.updateTick(world,x,y,z,random);
        if(world.getBlock(x,y,z)==this&&world.getBlockMetadata(x,y,z)>previous)
            world.setBlockMetadataWithNotify(x,y,z,metadata(age(previous)+1),2);
    }
    @Override public void func_149863_m(net.minecraft.world.World world,int x,int y,int z){
        if(!world.isRemote)world.setBlockMetadataWithNotify(x,y,z,metadata(age(world.getBlockMetadata(x,y,z))+(2+world.rand.nextInt(4))/3),2);
    }
    @Override public java.util.ArrayList<net.minecraft.item.ItemStack> getDrops(net.minecraft.world.World world,int x,int y,int z,int metadata,int fortune){
        java.util.ArrayList<net.minecraft.item.ItemStack> drops=new java.util.ArrayList<net.minecraft.item.ItemStack>();
        if(age(metadata)<3){drops.add(new net.minecraft.item.ItemStack(func_149866_i()));return drops;}
        drops.add(new net.minecraft.item.ItemStack(func_149865_P()));int seeds=1;
        for(int i=0;i<3+Math.max(0,fortune);i++)if(world.rand.nextFloat()<4F/7F)seeds++;
        if(seeds>0)drops.add(new net.minecraft.item.ItemStack(func_149866_i(),seeds));return drops;
    }
    @Override protected Item func_149866_i() { return CombatContent.beetrootSeeds; }
    @Override protected Item func_149865_P() { return CombatContent.beetroot; }
}
