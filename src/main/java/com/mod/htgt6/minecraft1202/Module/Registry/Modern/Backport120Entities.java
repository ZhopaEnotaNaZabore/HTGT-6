package com.mod.htgt6.minecraft1202.Module.Registry.Modern;

import com.mod.htgt6.minecraft1202.Minecraft1202;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityAxolotl120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityGlowSquid120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityGoat120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityAllay120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityCamel120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityCamelSeat120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityParrot120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPanda120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWarden120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWanderingTrader120;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityTraderLlama120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeRegistry120;
import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;

/** Entity tracking plus natural-spawn registration for the modern backport. */
public final class Backport120Entities {
    public static final int AXOLOTL_MOD_ID=28;
    public static final int GLOW_SQUID_MOD_ID=29;
    // 30..38 belong to Update Aquatic. All update modules share the same
    // Minecraft1202 mod container, so Forge's mod-entity IDs must be unique
    // across the complete backport rather than merely inside this registry.
    public static final int GOAT_MOD_ID=39;
    public static final int ALLAY_MOD_ID=40;
    public static final int CAMEL_MOD_ID=41;
    public static final int PARROT_MOD_ID=42;
    // 43..45 belong to Combat (boat, dragon fireball and second seat).
    public static final int WARDEN_MOD_ID=46;
    public static final int PANDA_MOD_ID=47;
    public static final int WANDERING_TRADER_MOD_ID=48;
    public static final int TRADER_LLAMA_MOD_ID=49;
    public static final int CAMEL_SEAT_MOD_ID=50;
    public static final int BEE_MOD_ID=1003;
    public static final int PIGLIN_MOD_ID=1004,ZOMBIFIED_PIGLIN_MOD_ID=1005;
    public static final int PIGLIN_BRUTE_MOD_ID=1006,HOGLIN_MOD_ID=1007,ZOGLIN_MOD_ID=1008,STRIDER_MOD_ID=1009;
    public static final int CAT_MOD_ID=1000,FOX_MOD_ID=1001,MOOSHROOM_MOD_ID=1002;
    public static final int CAMEL_TRACKING_RANGE=80;
    public static final int CAMEL_UPDATE_FREQUENCY=3;
    public static final boolean CAMEL_SENDS_VELOCITY=true;
    public static final int ALLAY_TRACKING_RANGE=64;
    public static final int ALLAY_UPDATE_FREQUENCY=3;
    public static final int GOAT_TRACKING_RANGE=80;
    public static final int GOAT_UPDATE_FREQUENCY=3;
    public static final BiomeGenBase[] GOAT_SPAWN_BIOMES={};
    public static final BiomeGenBase[] CAMEL_SPAWN_BIOMES={};
    public static final BiomeGenBase[] PARROT_SPAWN_BIOMES={BiomeGenBase.jungle,BiomeGenBase.jungleHills};
    private static boolean registered;
    private static boolean futuraSpawnsRegistered;
    public static synchronized void register(){
        if(registered)return;
        registerEgg(EntityAxolotl120.class,"HTGT6.Axolotl",0xFBC1E3,0xA62D74);
        EntityRegistry.registerModEntity(EntityAxolotl120.class,"Axolotl",AXOLOTL_MOD_ID,Minecraft1202.instance,80,3,true);
        registerEgg(EntityGlowSquid120.class,"HTGT6.GlowSquid",0x095B68,0x85F1E9);
        EntityRegistry.registerModEntity(EntityGlowSquid120.class,"GlowSquid",GLOW_SQUID_MOD_ID,Minecraft1202.instance,80,3,true);
        EntityRegistry.registerModEntity(EntityGoat120.class,"Goat",GOAT_MOD_ID,Minecraft1202.instance,
                GOAT_TRACKING_RANGE,GOAT_UPDATE_FREQUENCY,true);
        registerEgg(EntityAllay120.class,"HTGT6.Allay",0xDAFDFF,0x567D8D);
        EntityRegistry.registerModEntity(EntityAllay120.class,"Allay",ALLAY_MOD_ID,Minecraft1202.instance,
                ALLAY_TRACKING_RANGE,ALLAY_UPDATE_FREQUENCY,true);
        EntityRegistry.registerModEntity(EntityCamel120.class,"Camel",CAMEL_MOD_ID,Minecraft1202.instance,
                CAMEL_TRACKING_RANGE,CAMEL_UPDATE_FREQUENCY,CAMEL_SENDS_VELOCITY);
        EntityRegistry.registerModEntity(EntityCamelSeat120.class,"CamelSecondSeat",CAMEL_SEAT_MOD_ID,
                Minecraft1202.instance,CAMEL_TRACKING_RANGE,CAMEL_UPDATE_FREQUENCY,false);
        EntityRegistry.registerModEntity(EntityParrot120.class,"Parrot",PARROT_MOD_ID,Minecraft1202.instance,
                80,3,true);
        EntityRegistry.addSpawn(EntityParrot120.class,40,1,2,EnumCreatureType.creature,PARROT_SPAWN_BIOMES);
        EntityRegistry.registerModEntity(EntityWarden120.class,"Warden",WARDEN_MOD_ID,Minecraft1202.instance,
                96,3,true);
        registerEgg(EntityPanda120.class,"HTGT6.Panda",0xE7E7E7,0x1B1B22);
        EntityRegistry.registerModEntity(EntityPanda120.class,"Panda",PANDA_MOD_ID,Minecraft1202.instance,
                80,3,true);
        EntityRegistry.registerModEntity(EntityWanderingTrader120.class,"WanderingTrader",WANDERING_TRADER_MOD_ID,
                Minecraft1202.instance,80,3,true);
        EntityRegistry.registerModEntity(EntityTraderLlama120.class,"TraderLlama",TRADER_LLAMA_MOD_ID,
                Minecraft1202.instance,80,3,true);
        EntityRegistry.registerModEntity(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityCat114.class,"Cat",CAT_MOD_ID,Minecraft1202.instance,80,3,true);
        EntityRegistry.registerModEntity(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityFox114.class,"Fox",FOX_MOD_ID,Minecraft1202.instance,80,3,true);
        EntityRegistry.registerModEntity(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityMooshroom114.class,"Mooshroom",MOOSHROOM_MOD_ID,Minecraft1202.instance,80,3,true);
        EntityRegistry.addSpawn(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityFox114.class,8,2,4,EnumCreatureType.creature,BiomeGenBase.taiga,BiomeGenBase.taigaHills,BiomeGenBase.coldTaiga,BiomeGenBase.coldTaigaHills,BiomeGenBase.megaTaiga,BiomeGenBase.megaTaigaHills);
        EntityRegistry.registerModEntity(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityBee115.class,"Bee",BEE_MOD_ID,Minecraft1202.instance,80,3,true);
        EntityRegistry.registerModEntity(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglin116.class,"Piglin",PIGLIN_MOD_ID,Minecraft1202.instance,80,3,true);
        EntityRegistry.registerModEntity(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglinBrute116.class,"PiglinBrute",PIGLIN_BRUTE_MOD_ID,Minecraft1202.instance,80,3,true);
        EntityRegistry.registerModEntity(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityZombifiedPiglin116.class,"ZombifiedPiglin",ZOMBIFIED_PIGLIN_MOD_ID,Minecraft1202.instance,80,3,true);
        EntityRegistry.registerModEntity(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityHoglin116.class,"Hoglin",HOGLIN_MOD_ID,Minecraft1202.instance,80,3,true);
        EntityRegistry.registerModEntity(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityZoglin116.class,"Zoglin",ZOGLIN_MOD_ID,Minecraft1202.instance,80,3,true);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.HoglinNether116());
        EntityRegistry.registerModEntity(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityStrider116.class,"Strider",STRIDER_MOD_ID,Minecraft1202.instance,80,3,true);
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(new com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.StriderNether116());
        com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.PiglinNether116 piglinEvents=new com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.PiglinNether116();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(piglinEvents);
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(piglinEvents);
        BackportEntityIds114.validate(EntityRegistry.instance(),cpw.mods.fml.common.FMLCommonHandler.instance().findContainerFor(Minecraft1202.instance));
        registered=true;
    }
    /** Called after Futura owns and registers its extended biome IDs. */
    public static synchronized void registerFuturaBiomeSpawns(){
        if(futuraSpawnsRegistered)return;
        EntityRegistry.addSpawn(EntityGoat120.class,5,1,3,EnumCreatureType.creature,
                FuturaBiomeRegistry120.get(FuturaBiomeIds120.SNOWY_SLOPES),
                FuturaBiomeRegistry120.get(FuturaBiomeIds120.JAGGED_PEAKS),
                FuturaBiomeRegistry120.get(FuturaBiomeIds120.FROZEN_PEAKS));
        EntityRegistry.addSpawn(EntityParrot120.class,40,1,2,EnumCreatureType.creature,
                FuturaBiomeRegistry120.get(FuturaBiomeIds120.BAMBOO_JUNGLE));
        EntityRegistry.addSpawn(EntityPanda120.class,80,1,2,EnumCreatureType.creature,
                FuturaBiomeRegistry120.get(FuturaBiomeIds120.BAMBOO_JUNGLE));
        futuraSpawnsRegistered=true;
        EntityRegistry.addSpawn(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityFox114.class,8,2,4,EnumCreatureType.creature,FuturaBiomeRegistry120.get(FuturaBiomeIds120.GROVE));
    }
    private static void registerEgg(Class entity,String name,int primary,int secondary){
        EntityRegistry.registerGlobalEntityID(entity,name,EntityRegistry.findGlobalUniqueEntityId(),primary,secondary);
    }
    private Backport120Entities(){}
}
