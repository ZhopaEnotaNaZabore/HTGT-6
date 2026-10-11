package com.mod.htgt6.client.handlers;


import com.mod.htgt6.common.item.htgt6.fluids.FluidsHTGT6;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fluids.Fluid;

public class FluidTextureHandler {

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        // Ensure we are working with the Block/Fluid texture sheet
        if (event.map.getTextureType() == 0) {

            // Explicitly register vanilla water texture strings to generate correct IIcon instances
            IIcon waterStill = event.map.registerIcon("minecraft:water_still");
            IIcon waterFlowing = event.map.registerIcon("minecraft:water_flow");

            // Assign the stitched pointers safely to your fluids
            setupFluidTexture(FluidsHTGT6.liqPlatinumSol, waterStill, waterFlowing);
            setupFluidTexture(FluidsHTGT6.liqPalladConc, waterStill, waterFlowing);
            setupFluidTexture(FluidsHTGT6.liqKSo4, waterStill, waterFlowing);
            setupFluidTexture(FluidsHTGT6.liqRuCl, waterStill, waterFlowing);
            setupFluidTexture(FluidsHTGT6.liqAcidicOsmium, waterStill, waterFlowing);
            setupFluidTexture(FluidsHTGT6.liqKeF, waterStill, waterFlowing);
            setupFluidTexture(FluidsHTGT6.BauxideSollution, waterStill, waterFlowing);
            setupFluidTexture(FluidsHTGT6.HeatedBauxideSollution, waterStill, waterFlowing);
        }
    }

    private void setupFluidTexture(Fluid fluid, IIcon still, IIcon flowing) {
        if (fluid != null) {
            fluid.setIcons(still, flowing);
        }
    }
}