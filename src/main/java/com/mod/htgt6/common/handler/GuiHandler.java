package com.mod.htgt6.common.handler;

import com.mod.htgt6.client.GUI.GuiAssembler;
import com.mod.htgt6.common.TE.TileEntityAssembler;
import com.mod.htgt6.common.inventory.ContainerAssembler;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

    // ID for the Assembler GUI
    public static final int ASSEMBLER_ID = 0;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);

        if (ID == ASSEMBLER_ID) {
            if (te instanceof TileEntityAssembler) {
                return new ContainerAssembler(player.inventory, (TileEntityAssembler) te);
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
        return null;
    }
}