package com.mod.htgt6.common.block.greg.technological.hulls;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

public class blockHullDatabase {
    public static Block hullULV;
    public static Block HullEV;
    public static Block HullIV;
    public static Block HullLUV;
    public static Block HullZPM;
    public static Block HullUV;
    public static Block HullPUV;
    public static Block HullMAX;
    public static Block hullHV;
    public static Block hullLV;
    public static Block hullMV;

public static Block MiningWellFrameBoxTi;

    public static void InitiHulls() {
        addStat();
        BlockRegistration();
        GameRegistry.registerTileEntity(TileEntityTieredHull.class, "htgt6:tiered_hull");
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new TieredHullChunkMigration());

    }

    public static void BlockRegistration () {
        //NAME
        hullMV.setBlockName("hullMV");
        hullLV.setBlockName("hullLV");
        hullHV.setBlockName("hullHV");
        HullEV.setBlockName("HullEV");
        HullIV.setBlockName("HullIV");
        HullLUV.setBlockName("HullLUV");
        HullZPM.setBlockName("HullZPM");
        HullUV.setBlockName("HullUV");
        HullPUV.setBlockName("HullPUV");
        HullMAX.setBlockName("HullMAX");
        hullULV.setBlockName("hullULV");
        MiningWellFrameBoxTi.setBlockName("MiningWellFrameBoxTi");








        //TEXTURE NAME
        HullLUV.setBlockTextureName(HTGT6.MOD_ID+":hullUlv");
        HullEV.setBlockTextureName(HTGT6.MOD_ID+":hullUlv");
        HullIV.setBlockTextureName(HTGT6.MOD_ID+":hullUlv");
        HullZPM.setBlockTextureName(HTGT6.MOD_ID+":hullUlv");
        HullUV.setBlockTextureName(HTGT6.MOD_ID+":hullUlv");
        HullPUV.setBlockTextureName(HTGT6.MOD_ID+":hullUlv");
        HullMAX.setBlockTextureName(HTGT6.MOD_ID+":hullUlv");
        hullULV.setBlockTextureName(HTGT6.MOD_ID+":hullUlv");
        hullHV.setBlockTextureName(HTGT6.MOD_ID+":hullUlv");
        hullLV.setBlockTextureName(HTGT6.MOD_ID+":hullUlv");
        hullMV.setBlockTextureName(HTGT6.MOD_ID+":hullUlv");
        MiningWellFrameBoxTi.setBlockTextureName(HTGT6.MOD_ID+":MiningWellFrameBoxTi");
        //REGISTER
        // Never insert a registration above these legacy entries. Worlds on
        // 1.7.10 save numeric IDs, therefore this historical order is part of
        // the save compatibility contract.
        GameRegistry.registerBlock(hullULV, ItemBlockTieredHull.class, "ULVhull");
        GameRegistry.registerBlock(HullEV, ItemBlockTieredHull.class, "HullEV");
        GameRegistry.registerBlock(HullIV, ItemBlockTieredHull.class, "HullIV");
        GameRegistry.registerBlock(HullLUV, ItemBlockTieredHull.class, "HullLUV");
        GameRegistry.registerBlock(HullZPM, ItemBlockTieredHull.class, "HullZPM");
        GameRegistry.registerBlock(HullUV, ItemBlockTieredHull.class, "HullUV");
        GameRegistry.registerBlock(HullPUV, ItemBlockTieredHull.class, "HullPUV");
        GameRegistry.registerBlock(HullMAX, ItemBlockTieredHull.class, "HullMAX");

    }

    /**
     * Register additions after every legacy block from this mod. Keeping them
     * out of {@link #BlockRegistration()} prevents an ID shift in old worlds.
     */
    public static void registerAdditionalHulls() {
        GameRegistry.registerBlock(hullMV, ItemBlockTieredHull.class, "hullMV");
        GameRegistry.registerBlock(hullLV, ItemBlockTieredHull.class, "hullLV");
        GameRegistry.registerBlock(hullHV, ItemBlockTieredHull.class, "hullHV");
        GameRegistry.registerBlock(MiningWellFrameBoxTi, "MiningWellFrameBoxTi");
    }
    public static void addStat () {
        hullMV =  new BlockTieredHull(0xFFD34E, 2);
        hullLV =  new BlockTieredHull(0xD5E2E8, 1);
        hullHV =  new BlockTieredHull(0xFF9648, 3);
        HullEV = new BlockTieredHull(0xC47AFF, 4);
        HullIV = new BlockTieredHull(0x648FFF, 5);
        HullLUV = new BlockTieredHull(0xFF91CF, 6);
        HullZPM = new BlockTieredHull(0xFF5964, 7);
        HullUV = new BlockTieredHull(0x50E5ED, 8);
        HullPUV = new BlockTieredHull(0x7DEC70, 9);
        HullMAX = new BlockTieredHull(0xFFF1B0, 16);
        hullULV =  new BlockTieredHull(0x8898A6, 0);
        MiningWellFrameBoxTi = new blockHullStatbase();


    }


}




