package com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.blocks;

import com.mod.htgt6.sulimited.block.GlieseTerrainBlock;
import com.mod.htgt6.sulimited.block.IndexedTerrainBlock;
import com.mod.htgt6.sulimited.block.TerrainTextureStyle;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class blocksGliese792C {

    public static Block GlieseCstone;
    public static Block beachSand, reefRock, deepReefRock, ventRock, deepCoral;
    public static Block GliseCDirt;
    public static Block GlieseCAlgae;
    public static Block GlieseCCoral;
    public static Block GlieseCMossyPetrifiedEarth;

    public static void InitGlieseCblocks() {
        addStat();
        BlockRegistration();

    }


    public static void BlockRegistration () {

        GlieseCstone.setBlockName("GlieseCStone");
        GliseCDirt.setBlockName("GliseCDirt");


        GlieseCAlgae.setBlockTextureName("htgt6:gliese/algae");
        GlieseCCoral.setBlockTextureName("htgt6:gliese/coral");

        GameRegistry.registerBlock(beachSand, "GlieseBeachSand");
        GameRegistry.registerBlock(reefRock, "GlieseReefRock");
        GameRegistry.registerBlock(deepReefRock, "GlieseDeepReefRock");
        GameRegistry.registerBlock(ventRock, "GlieseVentRock");
        GameRegistry.registerBlock(deepCoral, "GlieseDeepCoral");
        GameRegistry.registerBlock(GlieseCstone, "GlieseCStone");
        GameRegistry.registerBlock(GliseCDirt, "GliseCDirt");
        GameRegistry.registerBlock(GlieseCAlgae, "GlieseCAlgae");
        GameRegistry.registerBlock(GlieseCCoral, "GlieseCCoral");
        GameRegistry.registerBlock(GlieseCMossyPetrifiedEarth, "GlieseCMossyPetrifiedEarth");
    }

    public static void addStat () {
        beachSand = new GlieseTerrainBlock(Material.sand, "beach", 0.5F).setBlockName("GlieseBeachSand");
        reefRock = new GlieseTerrainBlock(Material.rock, "reef", 1.2F).setBlockName("GlieseReefRock");
        deepReefRock = new GlieseTerrainBlock(Material.rock, "deep_reef", 2.0F).setBlockName("GlieseDeepReefRock");
        ventRock = new GlieseTerrainBlock(Material.rock, "vent", 3.0F).setBlockName("GlieseVentRock").setLightLevel(0.2F);
        deepCoral = new BlockGlieseCPlant(true).setBlockName("GlieseDeepCoral").setBlockTextureName("htgt6:gliese/deep_coral").setLightLevel(0.35F);
        GlieseCstone = new IndexedTerrainBlock(Material.rock, TerrainTextureStyle.SUEDE_STONE, 0x494847);
        GlieseCstone.setHardness(1.5F);
        GlieseCstone.setHarvestLevel("pickaxe", 0);
        GliseCDirt = new IndexedTerrainBlock(Material.ground, TerrainTextureStyle.SMOOTH_SANDY, 0x524940);
        GliseCDirt.setHardness(0.5F);
        GlieseCAlgae = new BlockGlieseCPlant(false).setBlockName("GlieseCAlgae");
        GlieseCCoral = new BlockGlieseCPlant(true).setBlockName("GlieseCCoral");
        GlieseCMossyPetrifiedEarth = new GlieseTerrainBlock(Material.rock, "moss", 1.5F)
                .setHardness(1.5F).setBlockName("GlieseCMossyPetrifiedEarth");

    }


}
