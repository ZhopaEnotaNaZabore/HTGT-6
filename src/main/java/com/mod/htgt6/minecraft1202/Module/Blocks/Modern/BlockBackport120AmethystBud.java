package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.ForgeHooks;

import java.util.Random;

/** Direction occupies metadata 0..5; waterlogging is omitted because 1.7 cells cannot coexist. */
public final class BlockBackport120AmethystBud extends Block {
    private final int height,offset;
    private final boolean cluster;
    public BlockBackport120AmethystBud(String name,int height,int offset,boolean cluster){super(Material.glass);
        this.height=height;this.offset=offset;this.cluster=cluster;setBlockName("backport_1_20."+name);
        setBlockTextureName("htgt6:1_20_backport/"+name);setHardness(1.5F);
        setResistance(Backport120Content.legacyResistance(1.5F));setStepSound(soundTypeGlass);setHarvestLevel("pickaxe",1);
        setCreativeTab(CreativeTabs.tabDecorations);if(cluster)setLightLevel(5F/15F);setBounds(ForgeDirection.UP);}
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
    @Override public boolean isOpaqueCube(){return false;}@Override public boolean renderAsNormalBlock(){return false;}
    @Override protected boolean canSilkHarvest(){return true;}
    @Override public Item getItemDropped(int m,Random r,int f){return cluster?Backport120Content.amethystShard:null;}
    @Override public int quantityDropped(Random r){return cluster?2:0;}
    @Override public int quantityDroppedWithBonus(int fortune,Random r){if(!cluster)return 0;
        EntityPlayer player=harvesters.get();ItemStack tool=player==null?null:player.getCurrentEquippedItem();
        if(player==null||!ForgeHooks.canToolHarvestBlock(this,0,tool))return 2;
        return 4*Math.max(1,r.nextInt(fortune+2));}
    @Override public boolean canHarvestBlock(EntityPlayer player,int metadata){return true;}
    @Override public int onBlockPlaced(World w,int x,int y,int z,int side,float hx,float hy,float hz,int metadata){return side>=0&&side<6?side:1;}
    @Override public boolean canPlaceBlockAt(World w,int x,int y,int z){for(ForgeDirection d:ForgeDirection.VALID_DIRECTIONS)if(supports(w,x,y,z,d))return true;return false;}
    private static boolean supports(IBlockAccess w,int x,int y,int z,ForgeDirection facing){int sx=x-facing.offsetX,sy=y-facing.offsetY,sz=z-facing.offsetZ;
        return w.getBlock(sx,sy,sz).isSideSolid(w,sx,sy,sz,facing);}
    @Override public void onNeighborBlockChange(World w,int x,int y,int z,Block n){ForgeDirection f=ForgeDirection.getOrientation(w.getBlockMetadata(x,y,z)&7);
        if(f==ForgeDirection.UNKNOWN||!supports(w,x,y,z,f)){dropBlockAsItem(w,x,y,z,w.getBlockMetadata(x,y,z),0);w.setBlockToAir(x,y,z);}}
    @Override public void setBlockBoundsBasedOnState(IBlockAccess w,int x,int y,int z){setBounds(ForgeDirection.getOrientation(w.getBlockMetadata(x,y,z)&7));}
    @Override public void setBlockBoundsForItemRender(){setBounds(ForgeDirection.UP);}
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World w,int x,int y,int z){setBlockBoundsBasedOnState(w,x,y,z);return super.getCollisionBoundingBoxFromPool(w,x,y,z);}
    private void setBounds(ForgeDirection f){float min=offset/16F,max=(16-offset)/16F,h=height/16F;
        if(f==ForgeDirection.DOWN)setBlockBounds(min,1-h,min,max,1,max);else if(f==ForgeDirection.NORTH)setBlockBounds(min,min,1-h,max,max,1);
        else if(f==ForgeDirection.SOUTH)setBlockBounds(min,min,0,max,max,h);else if(f==ForgeDirection.WEST)setBlockBounds(1-h,min,min,1,max,max);
        else if(f==ForgeDirection.EAST)setBlockBounds(0,min,min,h,max,max);else setBlockBounds(min,0,min,max,h,max);}
}
