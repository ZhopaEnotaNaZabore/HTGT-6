package com.mod.htgt6.common.handler;

import com.mod.htgt6.client.GUI.*;

import com.mod.htgt6.common.TE.TESuperMasicCompressor;
import com.mod.htgt6.common.TE.TileEntityAssembler;
import com.mod.htgt6.common.TE.UniversalGasTurbineTE;
import com.mod.htgt6.common.inventory.ContainerAssembler;


import com.mod.htgt6.common.inventory.ContainerScanner;
import com.mod.htgt6.common.inventory.ContainerSuperMasicCompressor;
import com.mod.htgt6.common.inventory.ContainerUniversalGasTurbine;
import cpw.mods.fml.common.network.IGuiHandler;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
;

public class GuiHandler implements IGuiHandler {

    // ID for the Assembler GUI
    public static final int ASSEMBLER_ID = 0;
    public static final int SUPER_MASIC_COMPRESSOR_ID = 1;
    public static final int SCANNER_GUI_ID = 2;
    public static final int BULK_SCANNER_GUI_ID = 3;
    public static final int UNIVERSAL_GAS_TURBINE_ID = 4;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);

        if (ID == ASSEMBLER_ID) {
            if (te instanceof TileEntityAssembler) {
                return new ContainerAssembler(player.inventory, (TileEntityAssembler) te);
            }
        }
        if (ID == SUPER_MASIC_COMPRESSOR_ID) {
            if (te instanceof TESuperMasicCompressor) {
                return new ContainerSuperMasicCompressor(player.inventory, (TESuperMasicCompressor) te);
            }
        }
        if (ID == SCANNER_GUI_ID || ID == BULK_SCANNER_GUI_ID) {
            return new ContainerScanner(player.inventory);
        }
        if (ID == UNIVERSAL_GAS_TURBINE_ID) {
            if (te instanceof UniversalGasTurbineTE) {
                return new ContainerUniversalGasTurbine(player.inventory, (UniversalGasTurbineTE) te);
            }
        }


        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);

        if (ID == ASSEMBLER_ID) {
            if (te instanceof TileEntityAssembler) {
                // Client gets both the Container and the TE for rendering
                return new GuiAssembler(new ContainerAssembler(player.inventory, (TileEntityAssembler) te), (TileEntityAssembler) te);
            }
        }
        if (ID == SUPER_MASIC_COMPRESSOR_ID) {
            if (te instanceof TESuperMasicCompressor) {
                return new GuiSuperMasicCompressor(new ContainerSuperMasicCompressor(player.inventory, (TESuperMasicCompressor) te), (TESuperMasicCompressor) te);
            }
        }
        if (ID == SCANNER_GUI_ID) {
            return new GuiScanner(player);
        }
        if (ID == BULK_SCANNER_GUI_ID) {
            return new GuiBulkScanner(player);
        }
        if (ID == UNIVERSAL_GAS_TURBINE_ID) {
            if (te instanceof UniversalGasTurbineTE) {
                return new GuiUniversalGasTurbine(new ContainerUniversalGasTurbine(player.inventory, (UniversalGasTurbineTE) te), (UniversalGasTurbineTE) te);
            }
        }

            return null;
        }
    }


