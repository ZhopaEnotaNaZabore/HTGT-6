package com.mod.htgt6.minecraft1202.Module.Data.Shared;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.BuriedTreasureHandler;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanAquaticHandler;
import com.mod.htgt6.minecraft1202.Module.Data.Aquatic.TreasureMapContractFixture120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.WorldGenOceanRuin;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.WorldGenVanillaShipwreck;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanStructureHandler;
import com.mod.htgt6.common.util.DeferredStructurePlanFixture120;
import com.mod.htgt6.common.util.StructurePlacement120;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.world.ChunkEvent;

import java.lang.reflect.Method;

/** Registration and deterministic replay regression for Aquatic structures in Futura. */
public final class AquaticFuturaStructureFixture120 {
    public static void assertContract(){
        StructurePlacement120.assertContract();
        requirePlacement(StructurePlacement120.SHIPWRECK,24,4,165745295,"1.20.2 shipwreck");
        requirePlacement(StructurePlacement120.OCEAN_RUIN,20,8,14357621,"1.20.2 ocean ruin");
        DeferredStructurePlanFixture120.assertContract();
        TreasureMapContractFixture120.assertContract();
        WorldGenVanillaShipwreck.validateTemplates();
        WorldGenOceanRuin.validateTemplates();
        eventPair(OceanStructureHandler.class);
        eventPair(OceanAquaticHandler.class);
        eventPair(BuriedTreasureHandler.class);
    }
    private static void eventPair(Class<?> type){
        requireSubscribed(type,"onDecorate",DecorateBiomeEvent.Post.class);
        requireSubscribed(type,"onChunkLoad",ChunkEvent.Load.class);
    }
    private static void requireSubscribed(Class<?> type,String name,Class<?> parameter){
        try{
            Method method=type.getMethod(name,parameter);
            if(method.getAnnotation(SubscribeEvent.class)==null)
                throw new IllegalStateException(type.getName()+'.'+name+" is not subscribed");
        }catch(NoSuchMethodException error){throw new IllegalStateException(
                "Missing Aquatic replay event "+type.getName()+'.'+name,error);}
    }
    private static void requirePlacement(StructurePlacement120.Placement placement,int spacing,
                                         int separation,int salt,String name){
        if(placement.spacing!=spacing||placement.separation!=separation||placement.salt!=salt)
            throw new IllegalStateException(name+" placement differs from the official release");
    }
    public static void main(String[] ignored){assertContract();System.out.println("Aquatic Futura structures OK");}
    private AquaticFuturaStructureFixture120(){ }
}
