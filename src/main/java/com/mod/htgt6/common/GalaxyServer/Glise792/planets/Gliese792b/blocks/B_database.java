package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.blocks;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;


public class B_database  {
    public static Block Gliese792bSurface ;

    public static void initGLiese792Bblocks ()

    {

        addStat();
        BlockRegistration();

    }

    public static void BlockRegistration () {
//NAME
        Gliese792bSurface.setBlockName("Gliese792Surface");


        //TEXTURE
        Gliese792bSurface.setBlockTextureName(HTGT6.MOD_ID+":planet_gliese_b_surf");

        //REGISTRY

        GameRegistry.registerBlock(Gliese792bSurface, "Gliese792bSurface");




    }

    public static void addStat () {
        Gliese792bSurface = new B_statbase();

    }

}
