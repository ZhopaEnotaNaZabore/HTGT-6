package com.mod.htgt6.minecraft1202.Module.Registry.Exploration;

import com.mod.htgt6.minecraft1202.Module.Client.Exploration.ExplorationUpdate112Client;

import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;
import com.mod.htgt6.minecraft1202.Module.Events.Exploration.ExplorationEventHandler112;
import com.mod.htgt6.minecraft1202.Minecraft1202;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityEvoker112;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityEvokerFangs112;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityVex112;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityVindicator112;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlamaSpit112;
import com.mod.htgt6.minecraft1202.Module.World.Exploration.WoodlandMansionGenerator112;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/** Minecraft 1.11 content using its final Minecraft 1.12 registries and data contracts. */
public final class ExplorationUpdate112 {
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("MC1202_Explore");
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-Exploration112");
    private static boolean initialized;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        NETWORK.registerMessage(com.mod.htgt6.minecraft1202.Module.Network.Exploration.PacketTotemActivation112.Handler.class,
                com.mod.htgt6.minecraft1202.Module.Network.Exploration.PacketTotemActivation112.class, 0, Side.CLIENT);
        ExplorationContent112.register();
        NETWORK.registerMessage(com.mod.htgt6.minecraft1202.Module.Network.Exploration.PacketOpenLlamaInventory120.Handler.class,
                com.mod.htgt6.minecraft1202.Module.Network.Exploration.PacketOpenLlamaInventory120.class,1,Side.SERVER);
        FMLCommonHandler.instance().bus().register(new com.mod.htgt6.minecraft1202.Module.Network.Exploration.PacketOpenLlamaInventory120());
        registerEntities();
        GameRegistry.registerWorldGenerator(new WoodlandMansionGenerator112(), 19);
        MinecraftForge.EVENT_BUS.register(new ExplorationEventHandler112());
        registerClient();
        LOGGER.info("Minecraft Java 1.11 Exploration Update initialized on the 1.12 contract");
    }

    private static void registerEntities() {
        int vindicator = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(EntityVindicator112.class, "HTGT6.VindicationIllager",
                vindicator, 0x959B9B, 0x275E61);
        EntityRegistry.registerModEntity(EntityVindicator112.class, "vindication_illager", 22,
                Minecraft1202.instance, 80, 3, true);
        int evoker = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(EntityEvoker112.class, "HTGT6.EvocationIllager",
                evoker, 0x959B9B, 0x1E1C1A);
        EntityRegistry.registerModEntity(EntityEvoker112.class, "evocation_illager", 23,
                Minecraft1202.instance, 80, 3, true);
        int vex = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(EntityVex112.class, "HTGT6.Vex", vex,
                0x7A90A4, 0xE8E8E8);
        EntityRegistry.registerModEntity(EntityVex112.class, "vex", 24,
                Minecraft1202.instance, 80, 1, true);
        EntityRegistry.registerModEntity(EntityEvokerFangs112.class, "evocation_fangs", 25,
                Minecraft1202.instance, 64, 1, true);
        int llama = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(EntityLlama112.class, "HTGT6.Llama", llama,
                0xC09E7D, 0x995A35);
        EntityRegistry.registerModEntity(EntityLlama112.class, "llama", 26,
                Minecraft1202.instance, 80, 3, true);
        EntityRegistry.registerModEntity(EntityLlamaSpit112.class, "llama_spit", 27,
                Minecraft1202.instance, 64, 1, true);
        EntityRegistry.addSpawn(EntityLlama112.class, 5, 4, 6, EnumCreatureType.creature,
                BiomeGenBase.extremeHills, BiomeGenBase.extremeHillsEdge, BiomeGenBase.savanna,
                BiomeGenBase.savannaPlateau);
    }

    private static void registerClient() {
        if (!FMLCommonHandler.instance().getSide().isClient()) return;
        try {
            Class.forName("com.mod.htgt6.minecraft1202.Module.Client.Exploration.ExplorationUpdate112Client").getMethod("init").invoke(null);
        } catch (Exception error) {
            throw new IllegalStateException("Unable to initialize Exploration Update 1.12 client", error);
        }
    }

    private ExplorationUpdate112() { }
}
