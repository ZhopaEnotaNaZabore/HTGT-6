package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.world.*;
import java.util.*;
/** Stored plant identity is the block ID; no per-pot ticking tile entity. */
public final class BlockPottedNether116 extends Block {
    public final Block plant;
    public BlockPottedNether116(String name,Block plant){super(Material.circuits);this.plant=plant;setBlockName("backport_1_20.potted_"+name);setBlockTextureName("flower_pot");setHardness(0);setStepSound(soundTypeStone);setBlockBounds(5F/16,0,5F/16,11F/16,6F/16,11F/16);}
    @Override public boolean isOpaqueCube(){return false;}
    @Override public boolean renderAsNormalBlock(){return false;}
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
    @Override public ArrayList<ItemStack> getDrops(World w,int x,int y,int z,int m,int fortune){return new ArrayList<ItemStack>(Arrays.asList(new ItemStack(Items.flower_pot),new ItemStack(plant)));}
    @Override protected boolean canSilkHarvest(){return false;}
    @Override public Item getItem(World w,int x,int y,int z){return Item.getItemFromBlock(plant);}
    @Override public ItemStack getPickBlock(net.minecraft.util.MovingObjectPosition hit,World w,int x,int y,int z,EntityPlayer p){return new ItemStack(plant);}
    @Override public boolean canPlaceBlockAt(World w,int x,int y,int z){return super.canPlaceBlockAt(w,x,y,z)&&World.doesBlockHaveSolidTopSurface(w,x,y-1,z);}
    @Override public void onNeighborBlockChange(World w,int x,int y,int z,Block neighbor){if(!w.isRemote&&!World.doesBlockHaveSolidTopSurface(w,x,y-1,z)){dropBlockAsItem(w,x,y,z,w.getBlockMetadata(x,y,z),0);w.setBlockToAir(x,y,z);}}
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float hx,float hy,float hz){
        if(!p.canPlayerEdit(x,y,z,side,p.getHeldItem()))return false;
        if(!w.isRemote&&w.setBlock(x,y,z,Blocks.flower_pot,0,3)){ItemStack result=new ItemStack(plant);if(!p.inventory.addItemStackToInventory(result))p.dropPlayerItemWithRandomChoice(result,false);}
        return true;
    }
}
