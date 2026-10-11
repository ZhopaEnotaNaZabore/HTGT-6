package com.mod.htgt6.minecraft1202.Module.Blocks.Combat;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import java.util.Random;
import net.minecraft.block.BlockIce;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public final class BlockFrostedIce extends BlockIce {
    private final IIcon[] stages = new IIcon[4];
    public BlockFrostedIce() { setBlockName("frosted_ice"); setHardness(0.5F); setResistance(Backport120Content.legacyResistance(0.5F)); setTickRandomly(true); setCreativeTab(null); }
    @Override public void registerBlockIcons(IIconRegister register) {
        for (int i = 0; i < stages.length; ++i) stages[i] = register.registerIcon("htgt6:combat_update/frosted_ice_" + i);
        blockIcon = stages[0];
    }
    @Override public IIcon getIcon(int side, int metadata) { return stages[Math.min(3, metadata & 3)]; }
    @Override public boolean canSilkHarvest(World world,net.minecraft.entity.player.EntityPlayer player,int x,int y,int z,int metadata){return false;}
    @Override public net.minecraft.item.ItemStack getPickBlock(net.minecraft.util.MovingObjectPosition hit,World world,int x,int y,int z){return null;}
    @Override public void updateTick(World world, int x, int y, int z, Random random) {
        if(world.isRemote)return;
        int age = world.getBlockMetadata(x, y, z) & 3;
        if ((random.nextInt(3) == 0 || fewerNeighboursThan(world, x, y, z, 4))
                && world.getBlockLightValue(x, y, z) > 11 - age - getLightOpacity(world,x,y,z)) {
            if (slightlyMelt(world,x,y,z)) {
                ageNeighbours(world, x, y, z,random);
                return;
            }
        }
        world.scheduleBlockUpdate(x, y, z, this, 20 + random.nextInt(21));
    }
    private boolean slightlyMelt(World world,int x,int y,int z){
        int age=world.getBlockMetadata(x,y,z)&3;
        if(age<3){world.setBlockMetadataWithNotify(x,y,z,age+1,2);return false;}
        melt(world,x,y,z);return true;
    }
    private void melt(World world,int x,int y,int z){
        world.setBlock(x,y,z,world.provider.isHellWorld?net.minecraft.init.Blocks.air:net.minecraft.init.Blocks.water,0,3);
    }
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,net.minecraft.block.Block neighbor){
        if(!world.isRemote&&neighbor==this&&fewerNeighboursThan(world,x,y,z,2))melt(world,x,y,z);
    }
    private boolean fewerNeighboursThan(World world, int x, int y, int z, int limit) {
        int count = 0;
        for (int side = 0; side < 6; ++side) {
            if (world.getBlock(x + net.minecraft.util.Facing.offsetsXForSide[side],
                    y + net.minecraft.util.Facing.offsetsYForSide[side],
                    z + net.minecraft.util.Facing.offsetsZForSide[side]) == this && ++count >= limit) return false;
        }
        return true;
    }
    private void ageNeighbours(World world, int x, int y, int z,Random random) {
        for (int side = 0; side < 6; ++side) {
            int nx = x + net.minecraft.util.Facing.offsetsXForSide[side];
            int ny = y + net.minecraft.util.Facing.offsetsYForSide[side];
            int nz = z + net.minecraft.util.Facing.offsetsZForSide[side];
            if (world.getBlock(nx, ny, nz) == this&&!slightlyMelt(world,nx,ny,nz))
                world.scheduleBlockUpdate(nx, ny, nz, this, 20+random.nextInt(21));
        }
    }
}
