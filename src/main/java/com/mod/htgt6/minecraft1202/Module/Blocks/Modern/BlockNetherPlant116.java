package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherVegetation116;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.world.*;
import java.util.*;

/** Nylium vegetation does not inherit the old mushroom light restriction. */
public final class BlockNetherPlant116 extends BlockBush implements IGrowable,net.minecraftforge.common.IShearable {
    public final boolean warped,fungus,sprouts;
    public BlockNetherPlant116(String name,boolean warped,boolean fungus,boolean sprouts){
        this.warped=warped;this.fungus=fungus;this.sprouts=sprouts;
        setBlockName("backport_1_20."+name);setBlockTextureName("htgt6:1_20_backport/"+name);
        setHardness(0);setStepSound(sprouts?NetherVegetationContent116.SPROUT_SOUND:fungus?NetherVegetationContent116.FUNGUS_SOUND:NetherVegetationContent116.ROOT_SOUND);
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);setTickRandomly(false);
        float inset=fungus?.25F:.125F;setBlockBounds(inset,0,inset,1-inset,(sprouts?3F:fungus?9F:13F)/16,1-inset);
    }
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
    @Override protected boolean canPlaceBlockOn(Block b){return b==Blocks.grass||b==Blocks.dirt||b==Blocks.farmland||b==Blocks.mycelium||b==EtFVanillaGapContent120.soulSoil||b==NetherVegetationContent116.crimsonNylium||b==NetherVegetationContent116.warpedNylium;}
    @Override public boolean canBlockStay(World w,int x,int y,int z){return canPlaceBlockOn(w.getBlock(x,y-1,z));}
    @Override public Item getItemDropped(int m,Random r,int fortune){return sprouts?null:Item.getItemFromBlock(this);}
    @Override public boolean isShearable(ItemStack tool,IBlockAccess w,int x,int y,int z){return sprouts;}
    @Override public ArrayList<ItemStack> onSheared(ItemStack tool,IBlockAccess w,int x,int y,int z,int fortune){return new ArrayList<ItemStack>(Collections.singletonList(new ItemStack(this)));}
    @Override public boolean func_149851_a(World w,int x,int y,int z,boolean client){return fungus&&w.getBlock(x,y-1,z)==(warped?NetherVegetationContent116.warpedNylium:NetherVegetationContent116.crimsonNylium);}
    @Override public boolean func_149852_a(World w,Random r,int x,int y,int z){return r.nextFloat()<.4F;}
    @Override public void func_149853_b(World w,Random r,int x,int y,int z){if(!w.isRemote&&func_149851_a(w,x,y,z,false))NetherVegetation116.growFungus(w,r,x,y,z,warped);}
}
