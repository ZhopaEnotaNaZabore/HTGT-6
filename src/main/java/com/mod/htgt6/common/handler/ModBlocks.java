package com.mod.htgt6.common.handler;


import com.mod.htgt6.common.TE.*;
import com.mod.htgt6.common.block.glass.BoronGlass1;
import com.mod.htgt6.common.block.hitech6.*;
import com.mod.htgt6.common.block.info.ItemBlockAssembler;
import com.mod.htgt6.common.block.technological.PCcube;
import cpw.mods.fml.common.registry.GameRegistry;
import com.mod.htgt6.common.block.greg.technological.hulls.hullHV;
import com.mod.htgt6.common.block.greg.technological.hulls.hullMV;
import com.mod.htgt6.common.block.greg.technological.hulls.hullLV;
import net.minecraft.block.Block;

public class ModBlocks {

    private static final hullLV HULL_LV = new hullLV();
    private static final hullMV HULL_MV = new hullMV();
    private static final hullHV HULL_HV = new hullHV();
    private static final BoronGlass1 BORON_GLASS_1 = new BoronGlass1();
    private static final PCcube P_CCUBE = new PCcube();
    private static final BlockAssembler BLOCK_ASSEMBLER = new BlockAssembler();
    private static final BlockSuperMasicCompressor BLOCK_SUPER_MASIC_COMPRESSOR = new BlockSuperMasicCompressor();

    public static void register() {

        //регистр TileEntity
       // GameRegistry.registerTileEntity(FurnTE.class, HTGT6.MOD_ID + ":Furn");
        //GameRegistry.registerBlock(HULL_ULV, "ULVhull");
        GameRegistry.registerTileEntity(TileEntityAssembler.class, "htgt6.Assembler");
       // GameRegistry.registerBlock(BLOCK_ASSEMBLER, "assembler");
       GameRegistry.registerBlock(BLOCK_ASSEMBLER, ItemBlockAssembler.class, "assembler");


        //Регистр просто блоков

        GameRegistry.registerBlock(HULL_LV, "HullLV");
        GameRegistry.registerBlock(HULL_MV, "hullMV");
        GameRegistry.registerBlock(HULL_HV, "hullHV");
        GameRegistry.registerBlock(BORON_GLASS_1, "BoronGlass1");
        GameRegistry.registerBlock(P_CCUBE, "PCcube");
        GameRegistry.registerBlock(BLOCK_SUPER_MASIC_COMPRESSOR, "blockSMCompressor");
    }
}
