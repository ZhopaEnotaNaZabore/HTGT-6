package com.mod.htgt6.minecraft1202.Module.World.VillageAndPillage;
import java.util.*;
import net.minecraft.world.World;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.PillagerOutpostData120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.JigsawPlacementRuntime120;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager;
public final class OutpostSpawnRuntime120 {
    public static List list(Object worldObject,Object type,int x,int y,int z,List original){
        if(!(worldObject instanceof World)||type!=EnumCreatureType.monster)return original;
        World world=(World)worldObject;if(!contains(world,x,y,z))return original;
        List<BiomeGenBase.SpawnListEntry> out=new ArrayList<BiomeGenBase.SpawnListEntry>();
        if(world.difficultySetting!=EnumDifficulty.PEACEFUL&&world.getGameRules().getGameRuleBooleanValue("doMobSpawning"))out.add(new BiomeGenBase.SpawnListEntry(EntityPillager.class,1,1,1));
        return out;
    }
    public static boolean contains(World world,int x,int y,int z){
        if(world==null||world.isRemote||world.provider.dimensionId!=0)return false;
        // Read-only query: querying a spawn list must not create saved data.
        PillagerOutpostData120 data=(PillagerOutpostData120)world.loadItemData(PillagerOutpostData120.class,"HTGT6PillagerOutposts");
        if(data==null)return false;
        for(PillagerOutpostData120.Start start:data.startsNear(x>>4,z>>4,7)){
            if(start.rejected||!data.hasSlice(start,start.chunkX,start.chunkZ))continue;
            JigsawPlacementRuntime120.Result plan=PillagerOutpostGenerator.layout(start);if(plan!=null&&contains(plan,x,y,z))return true;
        }
        return false;
    }
    public static boolean contains(JigsawPlacementRuntime120.Result plan,int x,int y,int z){
        int minX=Integer.MAX_VALUE,minY=minX,minZ=minX,maxX=Integer.MIN_VALUE,maxY=maxX,maxZ=maxX;
        for(JigsawPlacementRuntime120.Piece p:plan.pieces){JigsawPlacementRuntime120.Box b=p.box();minX=Math.min(minX,b.minX);minY=Math.min(minY,b.minY);minZ=Math.min(minZ,b.minZ);maxX=Math.max(maxX,b.maxX);maxY=Math.max(maxY,b.maxY);maxZ=Math.max(maxZ,b.maxZ);}
        return x>=minX&&x<=maxX&&y>=minY&&y<=maxY&&z>=minZ&&z<=maxZ;
    }
    private OutpostSpawnRuntime120(){}
}
