package com.mod.htgt6.common.handler;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.TE.FurnTE;
import com.mod.htgt6.common.block.CookingPot;
import com.mod.htgt6.common.block.EternalBlock;
import com.mod.htgt6.common.block.Furn;
import com.mod.htgt6.common.block.glass.BoronGlass1;
import cpw.mods.fml.common.registry.GameRegistry;
import com.mod.htgt6.common.block.greg.technological.hulls.*;
import net.minecraft.block.Block;

public class ModBlocks {
    public static final EternalBlock ETERNAL_BLOCK = new EternalBlock();
    public static final CookingPot COOKING_POT = new CookingPot();
    public static final Furn FURN = new Furn();
    private static final hullULV HULL_ULV = new hullULV();
    private static final hullLV HULL_LV = new hullLV();
    private static final hullMV HULL_MV = new hullMV();
    private static final hullHV HULL_HV = new hullHV();
private static final BoronGlass1 BORON_GLASS_1 = new BoronGlass1();

    public static void register() {

        //регистр TileEntity
        GameRegistry.registerTileEntity(FurnTE.class, HTGT6.MOD_ID + ":Furn");
        GameRegistry.registerBlock(FURN, "Furn");


        //Регистр просто блоков
        GameRegistry.registerBlock(ETERNAL_BLOCK, "EternalBlock");
        GameRegistry.registerBlock(COOKING_POT, "CookingPot");
        GameRegistry.registerBlock(HULL_ULV, "ULVhull");
        GameRegistry.registerBlock(HULL_LV, "HullLV");
        GameRegistry.registerBlock(HULL_MV, "hullMV");
        GameRegistry.registerBlock(HULL_HV, "hullHV");
        GameRegistry.registerBlock(BORON_GLASS_1, "BoronGlass1");
    }
}
