package com.mod.htgt6.common.item.htgt6.fluids;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class FluidsHTGT6 {

    public static Fluid liqPlatinumSol;
    public static Fluid liqPalladConc;

    public static Fluid liqKSo4;
    public static Fluid liqRuCl;
    public static Fluid liqAcidicOsmium;
    public static Fluid liqKeF;
///////////////////////////////////////
    public static Block blockPlatinumSol;
     public static Block blockPalladConc;
     public static Block blockKSo4;
     public static Block blockRuCl;
     public static Block blockAcidicOsmium;
     public static Block blockKeF;




    public static void initialize() {
        FluidRegistration();
        BlockFluidRegistration();
        ItemRegistration();
    }

    public static void FluidRegistration(){
        liqPlatinumSol = new Fluid("platinumsol").setGaseous(false).setDensity(400).setViscosity(2500);
        liqPalladConc = new Fluid("palladconc").setGaseous(false).setDensity(400).setViscosity(2500);
        liqKSo4 = new Fluid("KalliumSulphate").setGaseous(false).setDensity(400).setViscosity(2500);
        liqRuCl = new Fluid("RutheniumSalt").setGaseous(false).setDensity(400).setViscosity(2500);
        liqAcidicOsmium = new Fluid("AcidicOsmium").setGaseous(false).setDensity(400).setViscosity(2500);
        liqKeF = new Fluid("TriniumFluoirite").setGaseous(false).setDensity(400).setViscosity(2500);


        FluidRegistry.registerFluid(liqKSo4);
        FluidRegistry.registerFluid(liqAcidicOsmium);
        FluidRegistry.registerFluid(liqKeF);
        FluidRegistry.registerFluid(liqRuCl);
        FluidRegistry.registerFluid(liqPlatinumSol);
        FluidRegistry.registerFluid(liqPalladConc);
    }
    private static Fluid registerFluid(String fluidName, int density, int viscosity, int temperature, boolean gaseous) {
        Fluid returnFluid = FluidRegistry.getFluid(fluidName);
        if (returnFluid == null)
        {
            FluidRegistry.registerFluid(new Fluid(fluidName).setDensity(density).setViscosity(viscosity).setTemperature(temperature).setGaseous(gaseous));
            returnFluid = FluidRegistry.getFluid(fluidName);
        }
        return returnFluid;

    }
    public static void BlockFluidRegistration() {
        blockPlatinumSol = new blockFluidHTGT6(liqPlatinumSol, Material.water, false).setBlockName("PlatinumSollution");
        blockPlatinumSol.setBlockTextureName(HTGT6.MOD_ID+":fluid2");

        blockPalladConc = new blockFluidHTGT6(liqPalladConc, Material.water, false).setBlockName("PalladiumSollution");
        blockPalladConc.setBlockTextureName(HTGT6.MOD_ID+":fluid3");

        blockKSo4 = new blockFluidHTGT6(liqKSo4, Material.water, false).setBlockName("KalliumSuplhate");
        blockKSo4.setBlockTextureName(HTGT6.MOD_ID+":fluid4");

        blockRuCl = new blockFluidHTGT6(liqRuCl, Material.water, false).setBlockName("RutheniumSalt");
        blockRuCl.setBlockTextureName(HTGT6.MOD_ID+":fluid5");

        blockAcidicOsmium = new blockFluidHTGT6(liqAcidicOsmium, Material.water, false).setBlockName("AcidicOsmium");
        blockAcidicOsmium.setBlockTextureName(HTGT6.MOD_ID+":fluid6");

        blockKeF = new blockFluidHTGT6(liqKeF, Material.water, false).setBlockName("TriniumFluoirite");
        blockKeF.setBlockTextureName(HTGT6.MOD_ID+":fluid7");




        GameRegistry.registerBlock(blockKeF, "TriniumFluoirite");
        GameRegistry.registerBlock(blockAcidicOsmium, "AcidicOsmium");
        GameRegistry.registerBlock(blockRuCl, "RutheniumSalt");
        GameRegistry.registerBlock(blockPalladConc, "PalladiumSollution");
        GameRegistry.registerBlock(blockPlatinumSol, "PlatinumSollution");
        GameRegistry.registerBlock(blockKSo4, "KalliumSuplhate");
    }

    public static void  ItemRegistration () {

    }

}

