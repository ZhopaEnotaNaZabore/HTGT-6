package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c.blocks;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.block.statbase.BlockStoneStatbase;
import com.mod.htgt6.common.block.statbase.blockDirtStatbase;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

public class blocksGliese792C {

    public static Block GlieseCstone;
    public static Block GliseCDirt;

    public static void InitGlieseCblocks() {
        addStat();
        BlockRegistration();

    }


    public static void BlockRegistration () {

        GlieseCstone.setBlockName("GlieseCStone");
        GliseCDirt.setBlockName("GliseCDirt");


        GlieseCstone.setBlockTextureName(HTGT6.MOD_ID+":GlieseCStone");
        GliseCDirt.setBlockTextureName(HTGT6.MOD_ID+":GliseCDirt");

        GameRegistry.registerBlock(GlieseCstone, "GlieseCStone");
        GameRegistry.registerBlock(GliseCDirt, "GliseCDirt");
    }

    public static void addStat () {
        GlieseCstone = new BlockStoneStatbase();
        GliseCDirt =  new blockDirtStatbase();

    }


}
