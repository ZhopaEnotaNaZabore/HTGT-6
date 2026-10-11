package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import java.util.Random;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityRavager;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityEvoker112;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityVindicator112;

/** Minecraft 1.20.2 Raid.RaiderType tables and getPotentialBonusSpawns, using captured base wave count. */
public final class RaidWavePlan145 {
    public enum Type {
        VINDICATOR(new int[]{0,0,2,0,1,4,2,5}),
        EVOKER(new int[]{0,0,0,0,0,1,1,2}),
        PILLAGER(new int[]{0,4,3,3,4,4,4,2}),
        WITCH(new int[]{0,0,0,0,3,0,0,1}),
        RAVAGER(new int[]{0,0,0,1,0,1,0,2});
        private final int[] waves;
        Type(int[] waves){this.waves=waves;}
        public EntityLiving create(World world){
            switch(this){
                case VINDICATOR:return new EntityVindicator112(world);
                case EVOKER:return new EntityEvoker112(world);
                case PILLAGER:return new EntityPillager(world);
                case WITCH:return new com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityRaidWitch151(world);
                default:return new EntityRavager(world);
            }
        }
    }
    public static int baseCount(Type type,int wave,int baseWaves){
        if(wave<1||baseWaves<1||baseWaves>7||wave>baseWaves+1)return 0;
        return type.waves[Math.min(wave,baseWaves)];
    }
    public static int bonusCount(Type type,int wave,boolean bonus,EnumDifficulty difficulty,Random random){
        if(difficulty==EnumDifficulty.PEACEFUL)return 0;
        boolean easy=difficulty==EnumDifficulty.EASY;
        int max;
        switch(type){
            case WITCH:if(easy||wave<=2||wave==4)return 0;max=1;break;
            case PILLAGER:case VINDICATOR:max=easy?random.nextInt(2):difficulty==EnumDifficulty.NORMAL?1:2;break;
            case RAVAGER:max=!easy&&bonus?1:0;break;
            default:return 0;
        }
        return max>0?random.nextInt(max+1):0;
    }
    public static Type rider(int wave,int ravagerIndex){
        if(wave==5)return Type.PILLAGER;
        if(wave>=7)return ravagerIndex==0?Type.EVOKER:Type.VINDICATOR;
        return null;
    }
    private RaidWavePlan145(){}
}
