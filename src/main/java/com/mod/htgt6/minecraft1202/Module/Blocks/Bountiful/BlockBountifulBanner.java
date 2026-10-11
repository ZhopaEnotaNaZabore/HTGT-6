package com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import java.util.ArrayList;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Shared standing/wall banner block; appearance and NBT live in its tile entity. */
public final class BlockBountifulBanner extends BlockContainer {
    private final boolean wall;
    public BlockBountifulBanner(boolean wall) { super(Material.wood); this.wall=wall; setBlockName(wall?"bountiful_wall_banner":"bountiful_standing_banner"); setBlockTextureName("minecraft:wool_colored_white"); setHardness(1.0F); setResistance(Backport120Content.legacyResistance(1.0F)); setStepSound(soundTypeWood); setBlockBounds(0.25F,0,0.25F,0.75F,1,0.75F); }
    public boolean isWall(){return wall;}
    @Override public TileEntity createNewTileEntity(World world,int metadata){return new TileEntityBountifulBanner();}
    @Override public boolean isOpaqueCube(){return false;} @Override public boolean renderAsNormalBlock(){return false;} @Override public int getRenderType(){return -1;}
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world,int x,int y,int z){return null;}
    @Override public void setBlockBoundsBasedOnState(IBlockAccess world,int x,int y,int z){if(!wall){setBlockBounds(0.25F,0,0.25F,0.75F,1,0.75F);return;}switch(world.getBlockMetadata(x,y,z)){case 2:setBlockBounds(0,0,0.875F,1,1,1);break;case 3:setBlockBounds(0,0,0,1,1,0.125F);break;case 4:setBlockBounds(0.875F,0,0,1,1,1);break;default:setBlockBounds(0,0,0,0.125F,1,1);}}
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,net.minecraft.block.Block neighbor) {
        if(world.isRemote)return;
        int side=world.getBlockMetadata(x,y,z);
        boolean supported;
        if(wall) {
            supported=side>=2&&side<=5&&world.getBlock(x-net.minecraft.util.Facing.offsetsXForSide[side],y,
                    z-net.minecraft.util.Facing.offsetsZForSide[side]).getMaterial().isSolid();
        } else supported=world.getBlock(x,y-1,z).getMaterial().isSolid();
        if(!supported){dropBlockAsItem(world,x,y,z,side,0);world.setBlockToAir(x,y,z);}
    }
    @Override public ArrayList<ItemStack> getDrops(World world,int x,int y,int z,int meta,int fortune){ArrayList<ItemStack> drops=new ArrayList<ItemStack>();TileEntity tile=world.getTileEntity(x,y,z);drops.add(tile instanceof TileEntityBountifulBanner?((TileEntityBountifulBanner)tile).asItem():new ItemStack(BountifulContent.banner));return drops;}
    @Override public ItemStack getPickBlock(MovingObjectPosition target,World world,int x,int y,int z,EntityPlayer player){TileEntity tile=world.getTileEntity(x,y,z);return tile instanceof TileEntityBountifulBanner?((TileEntityBountifulBanner)tile).asItem():new ItemStack(BountifulContent.banner);}
}
