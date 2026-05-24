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


    public static void InitiHulls() {
        addStat();
        BlockRegistration();

    }

    public static void BlockRegistration () {
        //NAME
        HullEV.setBlockName("HullEV");
        HullIV.setBlockName("HullIV");
        HullLUV.setBlockName("HullLUV");
        HullZPM.setBlockName("HullZPM");
        HullUV.setBlockName("HullUV");
        HullPUV.setBlockName("HullPUV");
        HullMAX.setBlockName("HullMAX");
        hullULV.setBlockName("hullULV");







        //TEXTURE NAME
        HullLUV.setBlockTextureName(HTGT6.MOD_ID+":HullLUV");
        HullEV.setBlockTextureName(HTGT6.MOD_ID+":HullEV");
        HullIV.setBlockTextureName(HTGT6.MOD_ID+":HullIV");
        HullZPM.setBlockTextureName(HTGT6.MOD_ID+":HullZPM");
        HullUV.setBlockTextureName(HTGT6.MOD_ID+":HullUV");
        HullPUV.setBlockTextureName(HTGT6.MOD_ID+":HullPUV");
        HullMAX.setBlockTextureName(HTGT6.MOD_ID+":HullMAX");
        hullULV.setBlockTextureName(HTGT6.MOD_ID+":hullUlv");

        //REGISTER
        GameRegistry.registerBlock(hullULV, "ULVhull");
        GameRegistry.registerBlock(HullEV, "HullEV");
        GameRegistry.registerBlock(HullIV, "HullIV");
        GameRegistry.registerBlock(HullLUV, "HullLUV");
        GameRegistry.registerBlock(HullZPM, "HullZPM");
        GameRegistry.registerBlock(HullUV, "HullUV");
        GameRegistry.registerBlock(HullPUV, "HullPUV");
        GameRegistry.registerBlock(HullMAX, "HullMAX");


    }
    public static void addStat () {
        HullEV = new blockHullStatbase();
        HullIV = new blockHullStatbase();
        HullLUV = new blockHullStatbase();
        HullZPM = new blockHullStatbase();
        HullUV = new blockHullStatbase();
        HullPUV = new blockHullStatbase();
        HullMAX = new blockHullStatbase();
        hullULV =  new blockHullStatbase();


    }


}




