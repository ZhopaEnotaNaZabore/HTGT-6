package com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b.blocks;

import com.mod.htgt6.sulimited.block.GlieseTerrainBlock;
import com.mod.htgt6.sulimited.block.IndexedTerrainBlock;
import com.mod.htgt6.sulimited.block.TerrainTextureStyle;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;


public class B_database  {
    public static Block Gliese792bSurface ;
    public static Block Gliese792bBasalt;
    public static Block Gliese792bAsh;
    public static Block scoria, dryChannel;

    public static void initGLiese792Bblocks ()

    {

        addStat();
        BlockRegistration();

    }

    public static void BlockRegistration () {
//NAME
        Gliese792bSurface.setBlockName("Gliese792Surface");
        Gliese792bBasalt.setBlockName("Gliese792bBasalt");
        Gliese792bAsh.setBlockName("Gliese792bAsh");


        //TEXTURE
        Gliese792bBasalt.setBlockTextureName("htgt6:gliese/basalt");
        Gliese792bAsh.setBlockTextureName("htgt6:gliese/ash");

        //REGISTRY

        GameRegistry.registerBlock(Gliese792bSurface, "Gliese792bSurface");
        GameRegistry.registerBlock(Gliese792bBasalt, "Gliese792bBasalt");
        GameRegistry.registerBlock(Gliese792bAsh, "Gliese792bAsh");
        GameRegistry.registerBlock(scoria, "GlieseScoria");
        GameRegistry.registerBlock(dryChannel, "GlieseDryChannel");




    }

    public static void addStat () {
        Gliese792bSurface = new IndexedTerrainBlock(Material.rock, TerrainTextureStyle.MAGMATIC, 0x623B2E);
        Gliese792bSurface.setHarvestLevel("pickaxe", 3);
        Gliese792bSurface.setHardness(20.0F);
        Gliese792bBasalt = new GlieseTerrainBlock(Material.rock, "basalt", 3.0F);
        Gliese792bAsh = new GlieseTerrainBlock(Material.ground, "ash", 0.6F);
        scoria = new GlieseTerrainBlock(Material.rock, "scoria", 1.8F).setBlockName("GlieseScoria");
        dryChannel = new GlieseTerrainBlock(Material.rock, "channel", 2.2F).setBlockName("GlieseDryChannel");

    }

}
