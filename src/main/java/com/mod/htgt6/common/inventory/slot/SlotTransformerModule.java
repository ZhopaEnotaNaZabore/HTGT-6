package com.mod.htgt6.common.inventory.slot;

import com.mod.htgt6.common.TE.TESuperMasicCompressor; //[cite: 12]
import com.mod.htgt6.common.TE.TileEntityAssembler; //[cite: 12]
import com.mod.htgt6.common.TE.UniversalGasTurbineTE; // Added
import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules; //[cite: 12]
import net.minecraft.inventory.IInventory; //[cite: 12]
import net.minecraft.inventory.Slot; //[cite: 12]
import net.minecraft.item.Item; //[cite: 12]
import net.minecraft.item.ItemStack; //[cite: 12]

public class SlotTransformerModule extends Slot { //[cite: 12]

    private final IInventory tile; //[cite: 12]

    public SlotTransformerModule( //[cite: 12]
                                  IInventory inventory, //[cite: 12]
                                  IInventory tile, //[cite: 12]
                                  int slot, //[cite: 12]
                                  int x, //[cite: 12]
                                  int y) { //[cite: 12]

        super(inventory, slot, x, y); //[cite: 12]
        this.tile = tile; //[cite: 12]
    } //[cite: 12]

    @Override
    public boolean isItemValid(ItemStack stack) { //[cite: 12]

        if (stack == null) //[cite: 12]
            return false; //[cite: 12]

        Item item = stack.getItem(); //[cite: 12]

        // Added instance tracking check for the Universal Gas Turbine
        if (tile instanceof TESuperMasicCompressor //[cite: 12]
                || tile instanceof TileEntityAssembler //[cite: 12]
                || tile instanceof UniversalGasTurbineTE) {

            return item == Tmodules.LowLevelTransformer //[cite: 12]
                    || item == Tmodules.MediumLevelTransformer //[cite: 12]
                    || item == Tmodules.HighLevelTransformer //[cite: 12]
                    || item == Tmodules.UltimateLevelTransformer //[cite: 12]
                    || item == Tmodules.OmegaTransformer; //[cite: 12]
        } //[cite: 12]

        return false; //[cite: 12]
    }
}
