package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherVegetation116;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import java.util.Random;

public final class BlockNylium116 extends BlockBackport120Simple implements IGrowable {
    public final boolean warped;
    public BlockNylium116(boolean warped){
        super(Material.rock,warped?"warped_nylium":"crimson_nylium",.4F,.4F,NetherVegetationContent116.NYLIUM_SOUND,"pickaxe",0);
        this.warped=warped;setTickRandomly(true);
    }
    @Override public Item getItemDropped(int m,Random r,int fortune){return Item.getItemFromBlock(Blocks.netherrack);}
    @Override protected boolean canSilkHarvest(){return true;}
    @Override public void updateTick(World w,int x,int y,int z,Random r){if(!w.isRemote&&w.blockExists(x,y+1,z)&&w.getBlock(x,y+1,z).getLightOpacity(w,x,y+1,z)>=15)w.setBlock(x,y,z,Blocks.netherrack,0,3);}
    @Override public boolean func_149851_a(World w,int x,int y,int z,boolean client){return w.isAirBlock(x,y+1,z);}
    @Override public boolean func_149852_a(World w,Random r,int x,int y,int z){return true;}
    @Override public void func_149853_b(World w,Random r,int x,int y,int z){if(!w.isRemote&&func_149851_a(w,x,y,z,false))NetherVegetation116.spread(w,r,x,y+1,z,warped);}
}
