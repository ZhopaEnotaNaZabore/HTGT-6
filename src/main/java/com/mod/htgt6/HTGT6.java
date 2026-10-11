package com.mod.htgt6;


import com.mod.htgt6.api.hitech6.BaseMachines.BlockMachine;
import com.mod.htgt6.common.block.info.MachineItemBlock;
import com.mod.htgt6.asm.HTGT6Config;
import com.mod.htgt6.client.vanillatweaks.CHAT.PacketRequestChangeTitle;
import com.mod.htgt6.client.vanillatweaks.CHAT.PacketSyncActiveTitle;
import com.mod.htgt6.client.vanillatweaks.CHAT.PacketAchievementChatCard;
import com.mod.htgt6.client.vanillatweaks.TAB.PacketSyncTabStats;
import com.mod.htgt6.client.vanillatweaks.TAB.TABClientRenderer;
import com.mod.htgt6.client.vanillatweaks.TAB.TABServerHandler;
import com.mod.htgt6.common.CommonProxy;
import com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator;
import com.mod.htgt6.common.Migaration.MigrationDispatcher;
import com.mod.htgt6.common.Migaration.MigrationEnvironmentGuard;
import com.mod.htgt6.common.logging.LifecycleAudit;



import com.mod.htgt6.common.TE.*;
import com.mod.htgt6.common.handler.GuiHandler;


import com.mod.htgt6.common.handler.recipe.RM.ElectromagneticProcessorRecipeMap;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.ModpackGeneratedRecipes;
import com.mod.htgt6.common.handler.recipes.BurningBoxRecipes;
import com.mod.htgt6.common.handler.recipes.EnderIOSagMillGT6Recipes;
import com.mod.htgt6.common.handler.recipes.ImmersiveEngineeringCrusherGT6Recipes;
import com.mod.htgt6.common.handler.recipes.GT6CrusherRecipeMaterializer;
import com.mod.htgt6.common.network.PacketShowHUD;
import com.mod.htgt6.common.network.PacketTurbineFluidTransfer;
import com.mod.htgt6.common.network.PacketEngineFluidTransfer;
import com.mod.htgt6.common.network.PacketFluidMachineTransfer;
import com.mod.htgt6.common.network.PacketSyncFluidMachineTank;
import com.mod.htgt6.common.network.PacketGatewayFluidTransfer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import minetweaker.MineTweakerAPI;
import net.minecraft.block.Block;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = HTGT6.MOD_ID, version = "1.0.4", dependencies = "required-after:gregapi; required-after:gregtech")
public class HTGT6 {
    public static final String MOD_ID = "HTGT6";
    @Mod.Instance(MOD_ID) //
    public static HTGT6 instance;

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        com.mod.htgt6.client.vanillatweaks.CHAT.PacketChatImage.Handler.clear();
        event.registerServerCommand(new com.mod.htgt6.client.vanillatweaks.CHAT.CommandTitleAdmin());
        event.registerServerCommand(new com.mod.htgt6.common.agent.CommandHTGT6Agent());
    }

    @Mod.EventHandler
    public void serverStopped(cpw.mods.fml.common.event.FMLServerStoppedEvent event) {
        com.mod.htgt6.client.vanillatweaks.CHAT.PacketChatImage.Handler.clear();
    }

    @Mod.EventHandler
    public void missingMappings(FMLMissingMappingsEvent event) {
        MigrationDispatcher.interceptMissingMappings(event);
    }
  //  public static final String ASSET_PREFIX = MOD_ID.toLowerCase();
  public static SimpleNetworkWrapper network;
     @SidedProxy(
            clientSide = "com.mod.htgt6.client.ClientProxy",
            serverSide = "com.mod.htgt6.common.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        long lifecycleStarted = LifecycleAudit.begin(MOD_ID, "PRE_INIT");
        GT6MaterialAutoGenerator.registerAll();
        com.mod.htgt6.common.GTHooks.MaterialHooks.MaterialFrameBoxes.register();
        proxy.preInit(event);
        network = NetworkRegistry.INSTANCE.newSimpleChannel("HiTech6_Networking");
        FMLCommonHandler.instance().bus().register(com.mod.htgt6.common.network.MachineUiTransfers.INSTANCE);
        network.registerMessage(PacketSyncTabStats.Handler.class, PacketSyncTabStats.class, 0, Side.CLIENT);



        int packetId = 1;
        HTGT6.network.registerMessage(PacketRequestChangeTitle.Handler.class, PacketRequestChangeTitle.class, packetId++, cpw.mods.fml.relauncher.Side.SERVER
        );
        HTGT6.network.registerMessage(PacketSyncActiveTitle.Handler.class, PacketSyncActiveTitle.class, packetId++, cpw.mods.fml.relauncher.Side.CLIENT
        );
        HTGT6.network.registerMessage(PacketShowHUD.Handler.class, PacketShowHUD.class, packetId++, Side.CLIENT);
        HTGT6.network.registerMessage(PacketTurbineFluidTransfer.Handler.class, PacketTurbineFluidTransfer.class,
                packetId++, Side.SERVER);
        HTGT6.network.registerMessage(PacketEngineFluidTransfer.Handler.class, PacketEngineFluidTransfer.class,
                packetId++, Side.SERVER);
        HTGT6.network.registerMessage(PacketFluidMachineTransfer.Handler.class, PacketFluidMachineTransfer.class,
                packetId++, Side.SERVER);
        HTGT6.network.registerMessage(PacketSyncFluidMachineTank.Handler.class, PacketSyncFluidMachineTank.class,
                packetId++, Side.CLIENT);
        HTGT6.network.registerMessage(PacketAchievementChatCard.Handler.class, PacketAchievementChatCard.class,
                packetId++, Side.CLIENT);
        HTGT6.network.registerMessage(PacketGatewayFluidTransfer.Handler.class, PacketGatewayFluidTransfer.class,
                packetId++, Side.SERVER);
        HTGT6.network.registerMessage(com.mod.htgt6.client.vanillatweaks.CHAT.PacketSyncPurchasedTitles.Handler.class,
                com.mod.htgt6.client.vanillatweaks.CHAT.PacketSyncPurchasedTitles.class, packetId++, Side.CLIENT);

        network.registerMessage(com.mod.htgt6.client.vanillatweaks.CHAT.PacketChatImage.Handler.class,
                com.mod.htgt6.client.vanillatweaks.CHAT.PacketChatImage.class, packetId++, Side.SERVER);
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(
                new com.mod.htgt6.client.vanillatweaks.CHAT.PacketChatImage.Handler());

        LifecycleAudit.end(MOD_ID, "PRE_INIT", lifecycleStarted);
    }


    public static Block blockMachine;
public void APIclasses() {

    blockMachine = new BlockMachine().setBlockName("blockMachine");
    GameRegistry.registerBlock(blockMachine, MachineItemBlock.class, "blockMachine");
}


    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        long lifecycleStarted = LifecycleAudit.begin(MOD_ID, "INIT");
        NetworkRegistry.INSTANCE.registerGuiHandler(this,new GuiHandler());

        GameRegistry.registerTileEntity(TileEntityAssembler.class, "TileEntityAssembler");
        GameRegistry.registerTileEntity(TESuperMasicCompressor.class, "TileEntitySMCompressor");
        GameRegistry.registerTileEntity(UniversalGasTurbineTE.class, "UniversalGasTurbineTE");
        GameRegistry.registerTileEntity(MagicEnergyGeneratorTE.class, "MagicGeneratorTE");
        GameRegistry.registerTileEntity(RadioisotopeGeneratorTE.class, "RadioIsotopeGenTE");
        GameRegistry.registerTileEntity(UniversalEngineTE.class, "UniversalEngineTE");
        GameRegistry.registerTileEntity(FluidSolidifierTE.class, "FluidSolidifierTE");
        GameRegistry.registerTileEntity(LineAssemblerTE.class, "htgt6:line_assembler");
        GameRegistry.registerTileEntity(TEElectromagneticProcessor.class, "TEEM_Processor");
        GameRegistry.registerTileEntity(MetalFormerTE.class, "MetalFormer");
        proxy.init(event);


        TABServerHandler serverHandler = new TABServerHandler();
        FMLCommonHandler.instance().bus().register(serverHandler);
        if (FMLCommonHandler.instance().getSide().isClient()) {
            TABClientRenderer clientRenderer = new TABClientRenderer();
            MinecraftForge.EVENT_BUS.register(clientRenderer);
            FMLCommonHandler.instance().bus().register(clientRenderer);
        }


        APIclasses();
        LifecycleAudit.end(MOD_ID, "INIT", lifecycleStarted);
    }


   @Mod.EventHandler
   public void postInit(FMLPostInitializationEvent event) {
       long lifecycleStarted = LifecycleAudit.begin(MOD_ID, "POST_INIT");
       MigrationEnvironmentGuard.verifyEmbeddedAmunRa();
       MineTweakerAPI.registerClass( com.mod.htgt6.common.MT.CTAssembler.class);
       MineTweakerAPI.registerClass(com.mod.htgt6.common.MT.CTSMCompressor.class);


        proxy.postInit(event);
        proxy.registerNEI();
        LifecycleAudit.end(MOD_ID, "POST_INIT", lifecycleStarted);
   }

   @Mod.EventHandler
   public void loadComplete(FMLLoadCompleteEvent event) {
       long lifecycleStarted = LifecycleAudit.begin(MOD_ID, "LOAD_COMPLETE");
       // GT6 keeps part of its shaped/shapeless recipes in CR.BUFFER until the
       // end of postInit.  Removing replacements during our postInit therefore
       // only touched CraftingManager while the buffered GT6 originals were
       // inserted afterwards.  Register the migrated script operations after
       // GT6 has flushed that buffer, preserving the original script order:
       // remove old outputs first, then add the HiTech6 replacements.
       ModpackGeneratedRecipes.register();
       com.mod.htgt6.common.handler.recipe.RM.FlotationChamberRecipeHandler.INSTANCE.registerDefaults();
       com.mod.htgt6.common.handler.recipe.RM.MultiblockAssemblerRecipes.register();
       com.mod.htgt6.common.harvest.MachineWrenchHarvest.install();
       com.mod.htgt6.common.handler.recipe.RM.AssemblerRecipeHandler.registerTierModules();
       com.mod.htgt6.common.handler.recipes.PlatinumGroupChemistryRecipes.register();
         com.mod.htgt6.common.handler.recipes.PlatinumGroupSludgeRecipes.install();
         com.mod.htgt6.common.handler.recipes.DeepOreProcessingRecipes.register();
       com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.INSTANCE.importGT6MixerRecipes();
       // The migrated scripts above add a sizeable electronics chain directly
       // to GT6's Assembler/Press maps. The Electronics Factory imported the
       // base GT6 map in postInit, so run its incremental import once more now
       // and include HTGT6 SMD components from the Press as line recipes.
       com.mod.htgt6.common.handler.recipe.RM.ElectronicFactoryRecipeHandler.INSTANCE
               .importGT6CircuitAssemblerRecipes();
       com.mod.htgt6.common.handler.recipe.RM.ElectronicFactoryRecipeHandler.INSTANCE
               .importGT6PressSMDRecipes();
       // Collect component Mixer recipes inserted late by scripts into the
       // independent Industrial Mixer recipe database.
       com.mod.htgt6.common.handler.recipe.RM.IndustrialMixerRecipeHandler.INSTANCE
               .importGT6MixerRecipes();
       // Include compressor/implosion recipes inserted late by GT6 or scripts.
       com.mod.htgt6.common.handler.recipe.RM.NativeIndustrialCentrifugeRecipes.register();
       com.mod.htgt6.common.handler.recipe.RM.IndustrialCentrifugeRecipeHandler.INSTANCE
               .importGT6CentrifugeRecipes();
       com.mod.htgt6.common.handler.recipe.RM.CompressorRecipeHandler.INSTANCE.importGT6Recipes();
       // GT6 raw-ore crushing is input-driven and is otherwise absent from a
       // recipe-map snapshot. Materialize it without modifying GT6 recipes,
       // then copy the original maps into the two external crushers.
       GT6CrusherRecipeMaterializer.materialize();
       com.mod.htgt6.common.handler.recipe.RM.IndustrialCrusherRecipeHandler.INSTANCE.importGT6CrusherRecipes();
       BurningBoxRecipes.register();
       EnderIOSagMillGT6Recipes.register();
       ImmersiveEngineeringCrusherGT6Recipes.register();
       LifecycleAudit.end(MOD_ID, "LOAD_COMPLETE", lifecycleStarted);
       com.mod.htgt6.asm.ASMStartupBanner.printOnce();
   }




}
