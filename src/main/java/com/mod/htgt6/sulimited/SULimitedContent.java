package com.mod.htgt6.sulimited;

import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b.blocks.B_database;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.blocks.GlieseCwater;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.blocks.blocksGliese792C;

/**
 * Compatibility bridge for legacy block registration order.
 *
 * 1.7.10 stores numeric block IDs in old worlds.  This method is called by
 * HTGT6 at the historical pre-init position; moving it into the child mod's
 * later lifecycle would shift IDs and corrupt existing saves.
 */
public final class SULimitedContent {
    private SULimitedContent() {}

    public static void registerLegacyWorldBlocks() {
        B_database.initGLiese792Bblocks();
        blocksGliese792C.InitGlieseCblocks();
        GlieseCwater.init();
    }
}
