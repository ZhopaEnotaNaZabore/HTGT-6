package com.mod.htgt6.common.handler;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.TE.FurnTE;
import com.mod.htgt6.common.block.CookingPot;
import com.mod.htgt6.common.block.EternalBlock;
import com.mod.htgt6.common.block.Furn;
import cpw.mods.fml.common.registry.GameRegistry;

public class ModBlocks {
    public static final EternalBlock ETERNAL_BLOCK = new EternalBlock();
    public static final CookingPot COOKING_POT = new CookingPot();
    public static final Furn FURN = new Furn();


    public static void register() {

        //регистр TileEntity
        GameRegistry.registerTileEntity(FurnTE.class, HTGT6.MOD_ID + ":Furn");
        GameRegistry.registerBlock(FURN, "Furn");


        //Регистр просто блоков
        GameRegistry.registerBlock(ETERNAL_BLOCK, "EternalBlock");
        GameRegistry.registerBlock(COOKING_POT, "CookingPot");
    }
}
