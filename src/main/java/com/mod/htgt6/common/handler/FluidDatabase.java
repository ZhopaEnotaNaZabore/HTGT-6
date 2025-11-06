package com.mod.htgt6.common.handler;


import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.item.htgt6.fluids.HelemaLiquid;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class FluidDatabase {
    public static final Fluid helemaLiquid = new Fluid(HTGT6.MOD_ID +":HelemaLiquid");
    public static HelemaLiquid HELEMA_LIQUID;

    public static void register() {
        // Жидкость должна быть зарегистрирована раньше, чем блок для которой она будет прикреплена!
        if (FluidRegistry.registerFluid(helemaLiquid)) {
            // Если конфликтов никаких нет, то регистрируем блок для жидкости, иначе ничего не делаем.
            HELEMA_LIQUID = new HelemaLiquid(helemaLiquid);
            GameRegistry.registerBlock(HELEMA_LIQUID, "HelemaLiquid");
        }
    }
}
