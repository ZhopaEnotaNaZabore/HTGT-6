package com.mod.htgt6.client;






import com.mod.htgt6.client.handlers.FluidTextureHandler;
import com.mod.htgt6.client.hud.ThermometerHUDRenderer;
import com.mod.htgt6.client.menu.HTGT6MainMenuHandler;
import com.mod.htgt6.client.menu.HTGT6OptionsMenuHandler;
import com.mod.htgt6.client.menu.HTGT6PauseMenuHandler;
import com.mod.htgt6.client.nei.NEIHTGT6Config;
import com.mod.htgt6.client.GUI.MachineUiThemeSettingsHandler;
import com.mod.htgt6.client.vanillatweaks.CHAT.*;
import com.mod.htgt6.common.ui.MachineUiTheme;
//


import com.mod.htgt6.client.vanillatweaks.VanillaTweaksHandler;
import com.mod.htgt6.client.vanillatweaks.VanillaTweaksMultiplayerHandler;
import com.mod.htgt6.client.vanillatweaks.memory.CommandMemory;
import com.mod.htgt6.client.vanillatweaks.memory.MemoryCleaner;
import com.mod.htgt6.client.render.MiningUnitDrillRenderer;
import com.mod.htgt6.client.render.ChemicalPlantCoreRenderer;
import com.mod.htgt6.client.render.ElectronicFactoryRenderer;
import com.mod.htgt6.client.render.MultiblockGatewayRenderHandler;
import com.mod.htgt6.client.render.MultiblockHologramRenderer;
import com.mod.htgt6.client.render.IndustrialMixerRenderer;
import com.mod.htgt6.client.render.SuperMassiveCompressorRenderer;
import com.mod.htgt6.client.render.FluidSolidifierRenderer;
import com.mod.htgt6.common.TE.TESuperMasicCompressor;
import com.mod.htgt6.common.TE.FluidSolidifierTE;
import com.mod.htgt6.common.chemical.MultiTileChemicalPlant;
import com.mod.htgt6.common.electronics.TEElectronicFactory;
import com.mod.htgt6.common.mining.MultiTEMiningUnit;
import com.mod.htgt6.common.mixer.MultiTileIndustrialMixer;
import com.mod.htgt6.common.multiblock.gateway.BlockMultiblockGateway;

import com.mod.htgt6.common.CommonProxy;



import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

import net.minecraftforge.common.MinecraftForge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;






public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(new com.mod.htgt6.client.render.ItemAtlasSprites());
        MinecraftForge.EVENT_BUS.register(new com.mod.htgt6.client.render.MachineAtlasSprites());
        MachineUiTheme.load(event.getModConfigurationDirectory());
        com.mod.htgt6.client.sounds.OfficialSoundsClient.start();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientRegistry.bindTileEntitySpecialRenderer(MultiTEMiningUnit.class, new MiningUnitDrillRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(com.mod.htgt6.common.turbine.MultiTileSteamTurbine.class,
                new com.mod.htgt6.client.render.SteamTurbineRenderer());
        net.minecraftforge.client.MinecraftForgeClient.registerItemRenderer(
                net.minecraft.item.Item.getItemFromBlock(com.mod.htgt6.common.turbine.SteamTurbineContent.controller),
                new com.mod.htgt6.client.render.SteamTurbineItemRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(MultiTileChemicalPlant.class, new ChemicalPlantCoreRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TESuperMasicCompressor.class,
                new SuperMassiveCompressorRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TEElectronicFactory.class, new ElectronicFactoryRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(MultiTileIndustrialMixer.class,
                new IndustrialMixerRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(com.mod.htgt6.common.flotation.MultiTileFlotationChamber.class,
                new com.mod.htgt6.client.render.FlotationChamberRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(
                com.mod.htgt6.common.centrifuge.MultiTileIndustrialCentrifuge.class,
                new com.mod.htgt6.client.render.IndustrialCentrifugeRenderer());
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
                com.mod.htgt6.common.crusher.MultiTileIndustrialCrusher.class,
                new com.mod.htgt6.client.render.IndustrialCrusherRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(FluidSolidifierTE.class,
                new FluidSolidifierRenderer());
        int gatewayRenderId = RenderingRegistry.getNextAvailableRenderId();
        BlockMultiblockGateway.setRenderId(gatewayRenderId);
        RenderingRegistry.registerBlockHandler(new MultiblockGatewayRenderHandler(gatewayRenderId));
        int hullRenderId = RenderingRegistry.getNextAvailableRenderId();
        com.mod.htgt6.common.block.greg.technological.hulls.BlockTieredHull.setRenderId(hullRenderId);
        RenderingRegistry.registerBlockHandler(new com.mod.htgt6.client.render.TieredHullRenderHandler(hullRenderId));
        VanillaTweaksHandler handler = new VanillaTweaksHandler();
        MinecraftForge.EVENT_BUS.register(handler);

        FMLCommonHandler.instance()
                .bus()
                .register(handler);
        MinecraftForge.EVENT_BUS.register(new VanillaTweaksMultiplayerHandler());
        MinecraftForge.EVENT_BUS.register(new HTGT6MainMenuHandler());
        MinecraftForge.EVENT_BUS.register(new com.mod.htgt6.client.menu.ConnectionScreenThemeHandler());
        MinecraftForge.EVENT_BUS.register(new HTGT6OptionsMenuHandler());
        MinecraftForge.EVENT_BUS.register(new HTGT6PauseMenuHandler());
        //  new TAB();
        registerVanillaTweaks();
        MinecraftForge.EVENT_BUS.register(new FluidTextureHandler());
        MinecraftForge.EVENT_BUS.register(new ThermometerHUDRenderer());
        MinecraftForge.EVENT_BUS.register(new MachineUiThemeSettingsHandler());
        if (cpw.mods.fml.common.Loader.isModLoaded("gtnhlib")) {
            MinecraftForge.EVENT_BUS.register(new com.mod.htgt6.client.tooltip.ThemedItemTooltip());
        }
        if (cpw.mods.fml.common.Loader.isModLoaded("betterquesting")) {
            MinecraftForge.EVENT_BUS.register(new com.mod.htgt6.client.questing.KannaQuestBookHandler());
        }
        MinecraftForge.EVENT_BUS.register(MultiblockHologramRenderer.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new com.mod.htgt6.client.render.CrusherMobRenderer());
    }


    public void registerVanillaTweaks() {
        //Chat BAR
        MinecraftForge.EVENT_BUS.register(new ChatGuiInterceptor());
        FMLCommonHandler.instance().bus().register(new ChatAutocompleteBridge());
        //HiTech6 Stickers
        // Sticker encoding and drawing are owned by the custom chat pipeline.
        //ChatNEW
        MinecraftForge.EVENT_BUS.register(new ChatRender());
        //NEI


        //memory
        new MemoryCleaner();
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new CommandMemory());
//test



    }


    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }


    public void registerNEI() {
        NEIHTGT6Config.registerManual();
    }

    @Override public void toggleMultiblockHologram(World world, int x, int y, int z, EntityPlayer player) {
        MultiblockHologramRenderer.INSTANCE.toggle(world, x, y, z, player);
    }

}







