package com.mod.htgt6.minecraft1202.Module.Registry.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Client.Bountiful.BountifulUpdateClient;

import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulEndermite;
import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulGuardian;
import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulArmorStand;
import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulRabbit;
import com.mod.htgt6.minecraft1202.Module.World.Bountiful.OceanMonumentGenerator;
import com.mod.htgt6.minecraft1202.Module.World.Bountiful.BountifulRockGenerator120;
import com.mod.htgt6.minecraft1202.Minecraft1202;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.MinecraftForge;
import com.mod.htgt6.minecraft1202.Module.Events.Bountiful.BountifulEventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Lifecycle entry point for the staged Minecraft Java 1.8 Bountiful Update backport. */
public final class BountifulUpdate {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-BountifulUpdate");
    private static boolean initialized;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        BountifulContent.register();
        registerEntities();
        GameRegistry.registerWorldGenerator(new OceanMonumentGenerator(), 15);
        GameRegistry.registerWorldGenerator(new BountifulRockGenerator120(), 14);
        BountifulEventHandler handler=new BountifulEventHandler();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance().bus().register(handler);
        registerClientContent();
        LOGGER.info("Minecraft Java 1.8 - Bountiful Update module initialized");
    }

    private static void registerEntities(){
        int rabbit=EntityRegistry.findGlobalUniqueEntityId();EntityRegistry.registerGlobalEntityID(EntityBountifulRabbit.class,"HTGT6.Rabbit",rabbit,0x995F40,0x734831);EntityRegistry.registerModEntity(EntityBountifulRabbit.class,"Rabbit",10,Minecraft1202.instance,64,3,true);
        int mite=EntityRegistry.findGlobalUniqueEntityId();EntityRegistry.registerGlobalEntityID(EntityBountifulEndermite.class,"HTGT6.Endermite",mite,0x161616,0x6E6E6E);EntityRegistry.registerModEntity(EntityBountifulEndermite.class,"Endermite",11,Minecraft1202.instance,64,3,true);
        int guardian=EntityRegistry.findGlobalUniqueEntityId();EntityRegistry.registerGlobalEntityID(EntityBountifulGuardian.class,"HTGT6.Guardian",guardian,0x5A8272,0xF17D30);EntityRegistry.registerModEntity(EntityBountifulGuardian.class,"Guardian",12,Minecraft1202.instance,80,3,true);
        int stand=EntityRegistry.findGlobalUniqueEntityId();EntityRegistry.registerGlobalEntityID(EntityBountifulArmorStand.class,"HTGT6.ArmorStand",stand);EntityRegistry.registerModEntity(EntityBountifulArmorStand.class,"ArmorStand",13,Minecraft1202.instance,64,3,true);
        EntityRegistry.addSpawn(EntityBountifulRabbit.class,4,2,3,EnumCreatureType.creature,
                BiomeGenBase.desert,BiomeGenBase.desertHills);
        EntityRegistry.addSpawn(EntityBountifulRabbit.class,10,2,3,EnumCreatureType.creature,
                BiomeGenBase.icePlains,
                BiomeGenBase.coldTaiga,BiomeGenBase.coldTaigaHills,BiomeGenBase.taiga,
                BiomeGenBase.taigaHills,BiomeGenBase.megaTaiga,BiomeGenBase.megaTaigaHills);
        // The biome entry supplies attempts; EntityBountifulGuardian confines successful spawns to
        // persisted monument bounds, like StructureSpawnOverride in modern Java Edition.
        EntityRegistry.addSpawn(EntityBountifulGuardian.class,1,2,4,EnumCreatureType.monster,
                BiomeGenBase.deepOcean);
    }

    private static void registerClientContent(){if(!FMLCommonHandler.instance().getSide().isClient())return;try{Class.forName("com.mod.htgt6.minecraft1202.Module.Client.Bountiful.BountifulUpdateClient").getMethod("init").invoke(null);}catch(Exception e){throw new IllegalStateException("Unable to initialize Minecraft Java 1.8 client content",e);}}

    private BountifulUpdate() { }
}
