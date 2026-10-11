package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

/** Gravity-affected concrete powder that hardens when it touches water. */
public final class BlockBackport120ConcretePowder extends BlockFalling {
    private Block hardened;
    @SideOnly(Side.CLIENT) private IIcon[] icons;

    public BlockBackport120ConcretePowder() {
        super(Material.sand);
        setBlockName("backport_1_20.concrete_powder");
        setHardness(0.5F);
        setStepSound(soundTypeSand);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    public void setHardened(Block value) { hardened = value; }

    /** Called after EntityFallingBlock has removed its source and moved this tick. */
    public static boolean hardenFalling(net.minecraft.entity.item.EntityFallingBlock entity) {
        World world=entity.worldObj;
        if(world.isRemote||entity.isDead||!(entity.func_145805_f() instanceof BlockBackport120ConcretePowder))return false;
        BlockBackport120ConcretePowder powder=(BlockBackport120ConcretePowder)entity.func_145805_f();
        if(powder.hardened==null)return false;
        int x=net.minecraft.util.MathHelper.floor_double(entity.posX);
        int y=net.minecraft.util.MathHelper.floor_double(entity.posY);
        int z=net.minecraft.util.MathHelper.floor_double(entity.posZ);
        // Modern FallingBlockEntity clips source fluids when moving over one block per tick.
        double dx=entity.posX-entity.prevPosX,dy=entity.posY-entity.prevPosY,dz=entity.posZ-entity.prevPosZ;
        if(dx*dx+dy*dy+dz*dz>1) {
            net.minecraft.util.MovingObjectPosition hit=world.func_147447_a(
                    net.minecraft.util.Vec3.createVectorHelper(entity.prevPosX,entity.prevPosY,entity.prevPosZ),
                    net.minecraft.util.Vec3.createVectorHelper(entity.posX,entity.posY,entity.posZ),true,false,false);
            if(hit!=null&&isWater(world.getBlock(hit.blockX,hit.blockY,hit.blockZ))) {
                x=hit.blockX;y=hit.blockY;z=hit.blockZ;
            }
        }
        if(!isWater(world.getBlock(x,y,z)))return false;
        if(!world.setBlock(x,y,z,powder.hardened,entity.field_145814_a&15,3))return false;
        entity.setDead();
        return true;
    }

    private static boolean isWater(Block block) { return block==Blocks.water||block==Blocks.flowing_water; }

    @Override public void onBlockAdded(World world, int x, int y, int z) {
        if (!harden(world, x, y, z)) super.onBlockAdded(world, x, y, z);
    }
    @Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (!harden(world, x, y, z)) super.onNeighborBlockChange(world, x, y, z, neighbor);
    }
    private boolean harden(World world, int x, int y, int z) {
        if (world.isRemote || hardened == null || world.getBlock(x,y,z)!=this) return false;
        // A dry powder block does not solidify merely from water underneath it.
        // It must fall into that water; DOWN is skipped by modern touchesLiquid.
        for (int side = 1; side < 6; side++) {
            int nx=x+net.minecraftforge.common.util.ForgeDirection.getOrientation(side).offsetX;
            int ny=y+net.minecraftforge.common.util.ForgeDirection.getOrientation(side).offsetY;
            int nz=z+net.minecraftforge.common.util.ForgeDirection.getOrientation(side).offsetZ;
            Block block=world.getBlock(nx,ny,nz);
            if (block == Blocks.water || block == Blocks.flowing_water) {
                return world.setBlock(x,y,z,hardened,world.getBlockMetadata(x,y,z)&15,3);
            }
        }
        return false;
    }

    @Override public void updateTick(World world,int x,int y,int z,java.util.Random random) {
        if(world.isRemote || world.getBlock(x,y,z)!=this || harden(world,x,y,z))return;
        if(!fallInstantly && world.checkChunksExist(x-32,y-32,z-32,x+32,y+32,z+32)) {
            super.updateTick(world,x,y,z,random);
            return;
        }
        if(y<0 || !BlockFalling.func_149831_e(world,x,y-1,z))return;
        int color=world.getBlockMetadata(x,y,z)&15, landing=y;
        // The legacy instant-fall branch uses setBlock(block) and silently loses metadata.
        while(landing>0 && BlockFalling.func_149831_e(world,x,landing-1,z)) {
            --landing;
            Block destination=world.getBlock(x,landing,z);
            if(hardened!=null && (destination==Blocks.water || destination==Blocks.flowing_water))break;
        }
        if(landing<=0) { world.setBlockToAir(x,y,z);return; }
        Block destination=world.getBlock(x,landing,z);
        Block result=hardened!=null && (destination==Blocks.water || destination==Blocks.flowing_water)?hardened:this;
        if(!world.setBlockToAir(x,y,z))return;
        if(!world.setBlock(x,landing,z,result,color,3)) {
            // Preserve the source when another mod refuses the destination change.
            world.setBlock(x,y,z,this,color,3);
        }
    }

    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister register) {
        icons = new IIcon[Backport120Content.TERRACOTTA_COLORS.length];
        for (int i=0;i<icons.length;i++) icons[i]=register.registerIcon(
                "htgt6:1_20_backport/"+Backport120Content.TERRACOTTA_COLORS[i]+"_concrete_powder");
        blockIcon=icons[0];
    }
    @SideOnly(Side.CLIENT) @Override public IIcon getIcon(int side,int metadata){return icons[metadata&15];}
    @Override public int damageDropped(int metadata){return metadata&15;}
    @SuppressWarnings({"rawtypes","unchecked"}) @SideOnly(Side.CLIENT)
    @Override public void getSubBlocks(Item item,CreativeTabs tab,List list){
        for(int i=0;i<16;i++)list.add(new ItemStack(item,1,i));
    }
}
