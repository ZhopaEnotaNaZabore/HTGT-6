package com.mod.htgt6.common.item.htgt6.fluids;

import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class FluidsHTGT6 {
//Chemistry
    public static Fluid liqPlatinumSol;
    public static Fluid liqPalladConc;
    public static Fluid liqKSo4;
    public static Fluid liqRuCl;
    public static Fluid liqAcidicOsmium;
    public static Fluid liqKeF;
    public static Fluid BauxideSollution;
    public static Fluid HeatedBauxideSollution;

    ///Advanced fuels
    public static Fluid EnrichedNitroFuel;
    public static Fluid AdminFuel;

    public static void initialize() {
        FluidRegistration();
    }

    public static void FluidRegistration() {
        AdminFuel =  registerColoredFluid( "ADMfluid", 400, 2500, 300, false, 0xFFFF00);
        EnrichedNitroFuel =  registerColoredFluid( "EnrichedNitroFuel", 400, 2500, 300, false, 0xFFFF00);
        liqPlatinumSol = registerColoredFluid("Platiunum_sollution", 400, 2500, 300, false, 0x00FFFF);
        liqPalladConc = registerColoredFluid("Palladium_sollution", 400, 2500, 300, false, 0xE6E6FA);
        liqKSo4 = registerColoredFluid("Potassium_Disuplhate", 400, 2500, 300, false, 0xFFFDD0);
        liqRuCl = registerColoredFluid("Ruthenium_Salt", 400, 2500, 300, false, 0x8B0000);
        liqAcidicOsmium = registerColoredFluid("Acidic_Osmium_sollution", 400, 2500, 300, false, 0xB0C4DE);
        liqKeF = registerColoredFluid("Trinium_Fluiride", 400, 2500, 300, false, 0x7FFFD4);

        BauxideSollution = registerColoredFluid("BauxideSollution", 400, 2500, 300, false, 0xCD853F);
        HeatedBauxideSollution = registerColoredFluid("HeatedBauxideSollution", 400, 2500, 450, false, 0xD2691E);
    }

    private static Fluid registerColoredFluid(String fluidName, int density, int viscosity, int temperature, boolean gaseous, final int rgbColor) {
        // Fluid lowercases registry names; the lookup itself is case-sensitive.
        // Keep legacy names (including spelling) so saved tanks and quests remain compatible.
        String registryName = fluidName.toLowerCase(java.util.Locale.ENGLISH);
        Fluid returnFluid = FluidRegistry.getFluid(registryName);

        if (returnFluid == null) {
            Fluid newFluid = new Fluid(fluidName) {
                @Override
                public int getColor() {
                    return rgbColor;
                }

                @Override
                @SideOnly(Side.CLIENT)
                public IIcon getStillIcon() {
                    return Blocks.water.getIcon(0, 0);
                }

                @Override
                @SideOnly(Side.CLIENT)
                public IIcon getFlowingIcon() {
                    return Blocks.water.getIcon(1, 0);
                }
            };

            newFluid.setDensity(density)
                    .setViscosity(viscosity)
                    .setTemperature(temperature)
                    .setGaseous(gaseous)
                    .setUnlocalizedName(fluidName); // <-- ADD THIS LINE

            FluidRegistry.registerFluid(newFluid);
            returnFluid = FluidRegistry.getFluid(registryName);
        }

        return returnFluid;
    }
}

