package com.mod.htgt6.common.handler;

import com.mod.htgt6.client.GUI.GuiBulkScanner;
import com.mod.htgt6.client.GUI.GuiElectromagneticProcessor;
import com.mod.htgt6.client.GUI.GuiFluidSolidifier;
import com.mod.htgt6.client.GUI.GuiLineAssembler;
import com.mod.htgt6.client.GUI.GuiMetalFormer;
import com.mod.htgt6.client.GUI.GuiScanner;
import com.mod.htgt6.common.TE.FluidSolidifierTE;
import com.mod.htgt6.common.TE.LineAssemblerTE;
import com.mod.htgt6.common.TE.MetalFormerTE;
import com.mod.htgt6.common.TE.TEElectromagneticProcessor;
import com.mod.htgt6.common.inventory.ContainerElectromagneticProcessor;
import com.mod.htgt6.common.inventory.ContainerFluidSolidifier;
import com.mod.htgt6.common.inventory.ContainerLineAssembler;
import com.mod.htgt6.common.inventory.ContainerMetalFormer;
import com.mod.htgt6.common.inventory.ContainerScanner;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/** Legacy GUI routing for machines which have not moved to ModularUI yet. */
public class GuiHandler implements IGuiHandler {
    public static final int SCANNER_GUI_ID = 2;
    public static final int BULK_SCANNER_GUI_ID = 3;
    public static final int Solidifier = 8;
    public static final int LINE_ASSEMBLER_ID = 9;
    public static final int MINING_UNIT_ID = 10;
    public static final int ELECTROMAGNETIC_PROCESSOR_ID = 11;
    public static final int MF_ID = 12;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (id == Solidifier && tile instanceof FluidSolidifierTE) {
            return new ContainerFluidSolidifier(player.inventory, (FluidSolidifierTE) tile);
        }
        if (id == SCANNER_GUI_ID || id == BULK_SCANNER_GUI_ID) {
            return new ContainerScanner(player.inventory);
        }
        if (id == LINE_ASSEMBLER_ID && tile instanceof LineAssemblerTE) {
            return new ContainerLineAssembler(player.inventory, (LineAssemblerTE) tile);
        }
        if ((id == ELECTROMAGNETIC_PROCESSOR_ID || id == 1)
                && tile instanceof TEElectromagneticProcessor) {
            return new ContainerElectromagneticProcessor(player.inventory, (TEElectromagneticProcessor) tile);
        }
        if ((id == MF_ID || id == 2) && tile instanceof MetalFormerTE) {
            return new ContainerMetalFormer(player.inventory, (MetalFormerTE) tile);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (id == SCANNER_GUI_ID) return new GuiScanner(player);
        if (id == BULK_SCANNER_GUI_ID) return new GuiBulkScanner(player);
        if (id == Solidifier && tile instanceof FluidSolidifierTE) {
            FluidSolidifierTE solidifier = (FluidSolidifierTE) tile;
            return new GuiFluidSolidifier(new ContainerFluidSolidifier(player.inventory, solidifier), solidifier);
        }
        if (id == LINE_ASSEMBLER_ID && tile instanceof LineAssemblerTE) {
            LineAssemblerTE assembler = (LineAssemblerTE) tile;
            return new GuiLineAssembler(new ContainerLineAssembler(player.inventory, assembler), assembler);
        }
        if ((id == ELECTROMAGNETIC_PROCESSOR_ID || id == 1)
                && tile instanceof TEElectromagneticProcessor) {
            return new GuiElectromagneticProcessor(player.inventory, (TEElectromagneticProcessor) tile);
        }
        if ((id == MF_ID || id == 2) && tile instanceof MetalFormerTE) {
            return new GuiMetalFormer(player.inventory, (MetalFormerTE) tile);
        }
        return null;
    }
}
