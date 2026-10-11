package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.*;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.event.world.WorldEvent;
/** Existing-Nether fallback uses the ordinary monster cap, without terrain hooks. */
public final class HoglinNether116 {
    @SubscribeEvent public void potentialSpawns(WorldEvent.PotentialSpawns e){
        if(e.world.provider.dimensionId!=-1||e.type!=EnumCreatureType.monster)return;
        boolean hoglin=false;
        for(int i=0;i<e.list.size();i++){
            BiomeGenBase.SpawnListEntry entry=(BiomeGenBase.SpawnListEntry)e.list.get(i);
            if(entry.entityClass==EntityHoglin116.class)hoglin=true;
            if(entry.entityClass==net.minecraft.entity.monster.EntityPigZombie.class)e.list.set(i,new BiomeGenBase.SpawnListEntry(EntityZombifiedPiglin116.class,entry.itemWeight,entry.minGroupCount,entry.maxGroupCount));
        }
        if(!hoglin)e.list.add(new BiomeGenBase.SpawnListEntry(EntityHoglin116.class,5,3,4));
    }
}
