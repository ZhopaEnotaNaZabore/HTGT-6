package com.mod.htgt6.common.ui;

import com.mod.htgt6.common.TE.RadioisotopeGeneratorTE;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/** Exercises the real transactional output method without starting a world. */
public final class RadioisotopeOutputCheck {
    public static void main(String[] args) throws Exception {
        RadioisotopeGeneratorTE tile = new RadioisotopeGeneratorTE();
        Item product = new Item(), other = new Item();
        Field pending = RadioisotopeGeneratorTE.class.getDeclaredField("pendingDecayOutputs");
        pending.setAccessible(true);
        Method flush = RadioisotopeGeneratorTE.class.getDeclaredMethod("flushDecayOutputs");
        flush.setAccessible(true);
        tile.inventory[1] = new ItemStack(product, 63);
        for (int i = 2; i <= 6; i++) tile.inventory[i] = new ItemStack(other, 64);
        pending.set(tile, Arrays.asList(new ItemStack(product, 1), new ItemStack(other, 1)));
        flush.invoke(tile);
        require(tile.inventory[1].stackSize == 63 && pending.get(tile) != null, "Blocked batch partially committed");
        tile.inventory[6] = null;
        flush.invoke(tile);
        require(tile.inventory[1].stackSize == 64 && tile.inventory[6].stackSize == 1
                && pending.get(tile) == null, "Batch not committed exactly once");

        for (int i = 1; i <= 6; i++) tile.inventory[i] = new ItemStack(other, 64);
        tile.inventory[1] = new ItemStack(product, 63);
        NBTTagCompound tag = new NBTTagCompound(); tag.setString("variant", "different");
        tile.inventory[1].setTagCompound(tag);
        pending.set(tile, Arrays.asList(new ItemStack(product, 1)));
        flush.invoke(tile);
        require(pending.get(tile) != null && tile.inventory[1].stackSize == 63, "Merged incompatible NBT");

        for (int i = 1; i <= 6; i++) tile.inventory[i] = null;
        pending.set(tile, Arrays.asList(new ItemStack(product, 70)));
        flush.invoke(tile);
        require(tile.inventory[1].stackSize == 64 && tile.inventory[2].stackSize == 6, "Output exceeds stack limit");
        System.out.println("RadioisotopeOutputCheck: OK");
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
