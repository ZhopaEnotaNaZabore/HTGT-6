package com.mod.htgt6.minecraft1202.Module.Gameplay.Frostburn;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.TileEntityStructure120;

public final class ContainerStructure120 extends Container {
    public final TileEntityStructure120 tile;
    public ContainerStructure120(TileEntityStructure120 tile) { this.tile=tile; }
    @Override public boolean canInteractWith(EntityPlayer player) {
        return tile!=null && player.capabilities.isCreativeMode && player.canCommandSenderUseCommand(2,"")
                && player.worldObj==tile.getWorldObj() && player.getDistanceSq(tile.xCoord+.5,tile.yCoord+.5,tile.zCoord+.5)<=64
                && tile.getWorldObj().getTileEntity(tile.xCoord,tile.yCoord,tile.zCoord)==tile;
    }
    @Override public ItemStack transferStackInSlot(EntityPlayer player,int slot) { return null; }
}
