package xonin.backhand.api.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/** Binary ABI exposing HTGT6's native off-hand compartment to legacy clients. */
public interface IOffhandInventory {
    ItemStack backhand$getOffhandItem();
    void backhand$setOffhandItem(ItemStack stack);
    int backhand$getOffhandSlot();

    static boolean isValidSwitch(int slot, EntityPlayer player) {
        return player != null && (slot >= 0 && slot < 9 || slot == backhandSlot(player));
    }

    static int backhandSlot(EntityPlayer player) {
        return player.inventory instanceof IOffhandInventory
                ? ((IOffhandInventory) player.inventory).backhand$getOffhandSlot() : 40;
    }
}
