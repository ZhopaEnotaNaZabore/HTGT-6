package com.mod.htgt6.api.hitech6.Slots;

import com.mod.htgt6.common.TE.*;
import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules; //[cite: 12]
import net.minecraft.inventory.IInventory; //[cite: 12]
import net.minecraft.inventory.Slot; //[cite: 12]
import net.minecraft.item.Item; //[cite: 12]
import net.minecraft.item.ItemStack; //[cite: 12]

public class SlotTransformerModule extends Slot {

    private final IInventory tile;

    public SlotTransformerModule(
                                  IInventory inventory,
                                  IInventory tile,
                                  int slot,
                                  int x,
                                  int y) {

        super(inventory, slot, x, y);
        this.tile = tile;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {

        if (stack == null)
            return false;

        Item item = stack.getItem();

        // Added instance tracking check for the Universal Gas Turbine
        if (tile instanceof TESuperMasicCompressor
                || tile instanceof TileEntityAssembler
                || tile instanceof UniversalGasTurbineTE
                || tile instanceof MagicEnergyGeneratorTE
                || tile instanceof UniversalEngineTE
                || tile instanceof FluidSolidifierTE
                || tile instanceof RadioisotopeGeneratorTE
                || tile instanceof TEElectromagneticProcessor
                || tile instanceof MetalFormerTE
                || tile instanceof LineAssemblerTE)



        {

            return item == Tmodules.LowLevelTransformer
                    || item == Tmodules.MediumLevelTransformer
                    || item == Tmodules.HighLevelTransformer
                    || item == Tmodules.UltimateLevelTransformer
                    || item == Tmodules.OmegaTransformer;
        }

        return false;
    }
}
