package com.mod.htgt6.minecraft1202.Module.World.Modern;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;

/** New 1.18-1.20 biome identity; final decoration is owned by the Futura provider. */
public final class BiomeGenFuturaClimate120 extends BiomeGenBase {
    @SuppressWarnings("unchecked")
    public BiomeGenFuturaClimate120(int id,String name,BiomeGenBase source,float temperature,float rainfall,
                             boolean snow,boolean dry,int waterColor){
        super(id);setBiomeName(name);setColor(source.color);field_150609_ah=source.field_150609_ah;
        topBlock=source.topBlock;fillerBlock=source.fillerBlock;field_150604_aj=source.field_150604_aj;
        field_76754_C=source.field_76754_C;rootHeight=source.rootHeight;heightVariation=source.heightVariation;
        this.temperature=temperature;this.rainfall=rainfall;waterColorMultiplier=waterColor;
        spawnableMonsterList.clear();spawnableCreatureList.clear();spawnableWaterCreatureList.clear();spawnableCaveCreatureList.clear();
        if(id!=FuturaBiomeIds120.DEEP_DARK){
        spawnableMonsterList.addAll(source.getSpawnableList(EnumCreatureType.monster));
        spawnableCreatureList.addAll(source.getSpawnableList(EnumCreatureType.creature));
        spawnableWaterCreatureList.addAll(source.getSpawnableList(EnumCreatureType.waterCreature));
        spawnableCaveCreatureList.addAll(source.getSpawnableList(EnumCreatureType.ambient));
        }
        if(snow)setEnableSnow();if(dry)setDisableRain();
    }
}
