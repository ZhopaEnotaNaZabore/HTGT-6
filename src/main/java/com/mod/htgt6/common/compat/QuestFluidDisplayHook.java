package com.mod.htgt6.common.compat;

import gregapi.data.CS;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.apache.logging.log4j.LogManager;

/** BQ bypasses GT6's inventory item updater. Resolve display metadata in the receiving registry. */
public final class QuestFluidDisplayHook {
    private static final String DISPLAY = "gregapi:gt.display.fluid";
    private static final Set<String> WARNED = Collections.synchronizedSet(new HashSet<String>());

    public static NBTTagCompound normalizeNBT(NBTTagCompound input) {
        if (input == null || !DISPLAY.equals(input.getString("id"))) return input;
        NBTTagCompound result = (NBTTagCompound) input.copy();
        Integer id = resolve(result.getCompoundTag("tag"));
        if (id != null) result.setShort("Damage", id.shortValue());
        return result;
    }

    /** Also resolves again after a client receives the server's fluid registry. */
    public static ItemStack normalizeStack(ItemStack stack) {
        if (stack == null || !DISPLAY.equals(Item.itemRegistry.getNameForObject(stack.getItem()))) return stack;
        Integer id = resolve(stack.getTagCompound());
        if (id != null && stack.getItemDamage() != id) stack.setItemDamage(id);
        return stack;
    }

    private static Integer resolve(NBTTagCompound tag) {
        String name = tag == null ? "" : tag.getString("f");
        if (name.isEmpty()) {
            warn("<unnamed>", "GT6 quest fluid display has no saved fluid name; old numeric ID cannot be safely migrated");
            return null;
        }
        String alias = CS.FluidsGT.FLUID_RENAMINGS.get(name);
        Fluid fluid = FluidRegistry.getFluid(alias == null || alias.isEmpty() ? name : alias);
        if (fluid != null) return fluid.getID();
        warn(name, "Missing named quest fluid '" + name + "'; preserved its name and disabled stale display ID");
        // GT6's own missing-display sentinel. Never silently show another fluid at the old ID.
        return 32767;
    }

    private static void warn(String key, String message) {
        if (WARNED.add(key)) LogManager.getLogger("HTGT6-QuestFluids").warn(message);
    }

    private QuestFluidDisplayHook() { }
}
