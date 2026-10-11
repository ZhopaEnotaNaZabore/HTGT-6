package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.asm.HTGT6Config;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockFrostedIce;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/** Modern additions around Forge's existing soil result, shared by worldgen and block updates. */
public final class FuturaColumnPlantSurvival120 {
    private static final int[][] SIDES={{1,0},{-1,0},{0,1},{0,-1}};
    private FuturaColumnPlantSurvival120() {}
    public static boolean cactus(boolean legacy,World w,int x,int y,int z) {
        if(!HTGT6Config.RUN_MINECRAFT_1_7_9999)return legacy;
        if(y<0||y>=FuturaHeight120.interactionHeight(w)||w.getBlock(x,y+1,z).getMaterial().isLiquid())return false;
        for(int[] side:SIDES){Material m=w.getBlock(x+side[0],y,z+side[1]).getMaterial();if(m.isSolid()||m==Material.lava)return false;}
        Block below=w.getBlock(x,y-1,z);
        return legacy||below==Backport120Content.suspiciousSand||below==Blocks.cactus;
    }
    public static boolean cane(boolean legacy,World w,int x,int y,int z) {
        if(!HTGT6Config.RUN_MINECRAFT_1_7_9999)return legacy;
        if(y<0||y>=FuturaHeight120.interactionHeight(w))return false;
        if(legacy)return true; // Do not discard a mod's canSustainPlant contract.
        Block below=w.getBlock(x,y-1,z);
        if(below==Blocks.reeds)return true;
        if(!(below==Blocks.dirt||below==Blocks.grass||below==Blocks.mycelium||below==Blocks.sand
                ||below==Backport120Content.suspiciousSand||below==Backport120Content.rootedDirt
                ||below==Backport120Content.mossBlock||below==Backport120Content.mud
                ||below==Backport120Content.muddyMangroveRoots))return false;
        for(int[] side:SIDES){Block b=w.getBlock(x+side[0],y-1,z+side[1]);if(b.getMaterial()==Material.water||b instanceof BlockFrostedIce)return true;}
        return false;
    }
}
