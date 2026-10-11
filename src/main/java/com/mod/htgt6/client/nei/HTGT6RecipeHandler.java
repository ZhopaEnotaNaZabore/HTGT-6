package com.mod.htgt6.client.nei;

import codechicken.nei.api.API;
import codechicken.nei.recipe.DefaultOverlayHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.mod.htgt6.client.GUI.GuiElectromagneticProcessor;
import com.mod.htgt6.client.GUI.GuiFluidSolidifier;
import com.mod.htgt6.client.GUI.GuiLineAssembler;
import com.mod.htgt6.client.GUI.GuiMetalFormer;
import net.minecraft.client.gui.inventory.GuiContainer;
import com.mod.htgt6.client.nei.structure.*;

/**
 * Single registration point for every HiTech6 NEI recipe category.  Keeping the
 * registration table here prevents GUI classes and the loader from registering
 * handlers a second time.
 */
public final class HTGT6RecipeHandler {
    private HTGT6RecipeHandler() {}

    public static void registerAll() {
        StructurePreviewNEIHooks.register();
        register(new GasTurbineRecipeHandler());
        register(new MagicGeneratorRecipeHandler());
        register(new UniversalEngineRecipeHandler());
        register(new StructureLibNEIHandler());
        register(new ChemicalPlantNEIHandler());
        register(new ElectronicFactoryNEIHandler());
        register(new IndustrialMixerNEIHandler());
        register(new FlotationChamberNEIHandler());
        register(new IndustrialCentrifugeNEIHandler());
        register(new IndustrialCrusherNEIHandler());

        // ModularUI has no legacy GuiContainer overlay target. The handler is
        // still available through the machine's NEI button and item lookup.
        register(new ASrecipeHandler());
        register(new CSrecipeHandler());
        register(new RadioisotopeGeneratorRecipeHandler());
        register(new SolidifierRecipeHandlerNEI(), GuiFluidSolidifier.class, "fluid_solidifier");
        register(new LineAssemblerNEIHandler(), GuiLineAssembler.class, "line_assembler");
        register(new EMNEIHandler(), GuiElectromagneticProcessor.class, "em_processor");
        register(new MetalFormerNEIHandler(), GuiMetalFormer.class, "metal_former");
        registerMultiblockCatalysts();
    }

    /** U on a controller opens its processing category, not the structure viewer. */
    private static void registerMultiblockCatalysts() {
        for (HTGT6MultiblockStructure structure : HTGT6MultiblockRegistry.all()) {
            TemplateRecipeHandler handler = null;
            if (structure instanceof ChemicalPlantNEIStructure) handler = new ChemicalPlantNEIHandler();
            else if (structure instanceof ElectronicFactoryNEIStructure) handler = new ElectronicFactoryNEIHandler();
            else if (structure instanceof IndustrialMixerNEIStructure) handler = new IndustrialMixerNEIHandler();
            else if (structure instanceof FlotationChamberNEIStructure) handler = new FlotationChamberNEIHandler();
            else if (structure instanceof IndustrialCentrifugeNEIStructure) handler = new IndustrialCentrifugeNEIHandler();
            else if (structure instanceof IndustrialCrusherNEIStructure) handler = new IndustrialCrusherNEIHandler();
            else if (structure instanceof SuperMassiveCompressorNEIStructure) handler = new CSrecipeHandler();
            else if (structure instanceof FluidSolidifierNEIStructure) handler = new SolidifierRecipeHandlerNEI();
            if (handler != null) API.addRecipeCatalyst(structure.getController(), handler);
        }
    }

    private static void register(TemplateRecipeHandler handler) {
        API.registerRecipeHandler(handler);
        API.registerUsageHandler(handler);
    }

    private static void register(TemplateRecipeHandler handler, Class<? extends GuiContainer> gui, String overlayId) {
        register(handler);
        API.registerGuiOverlay(gui, overlayId);
        API.registerGuiOverlayHandler(gui, new DefaultOverlayHandler(), overlayId);
    }
}
