package com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

/** Persistent descriptors and per-chunk replay claims for pillager outposts. */
public final class PillagerOutpostData120 extends WorldSavedData {
    private static final String NAME="HTGT6PillagerOutposts";
    private final Map<String,Start> starts=new HashMap<String,Start>();
    private final Set<String> slices=new HashSet<String>();
    private final Set<String> guards=new HashSet<String>();
    private final Set<String> spawnedGuards=new HashSet<String>();
    public PillagerOutpostData120(){super(NAME);}
    public PillagerOutpostData120(String name){super(name);}
    public static PillagerOutpostData120 get(World world){
        PillagerOutpostData120 data=(PillagerOutpostData120)world.loadItemData(PillagerOutpostData120.class,NAME);
        if(data==null){data=new PillagerOutpostData120();world.setItemData(NAME,data);}return data;
    }

    public Start start(int chunkX,int chunkZ){return starts.get(chunkKey(chunkX,chunkZ));}
    public Start recordStart(int chunkX,int chunkZ,int surfaceY,long layoutSeed){
        String key=chunkKey(chunkX,chunkZ);Start old=starts.get(key);if(old!=null)return old;
        Start value=new Start(chunkX,chunkZ,surfaceY,layoutSeed);starts.put(key,value);markDirty();return value;
    }
    public List<Start> startsNear(int chunkX,int chunkZ,int radius){
        List<Start> result=new ArrayList<Start>();
        for(Start start:starts.values())if(Math.abs(start.chunkX-chunkX)<=radius&&Math.abs(start.chunkZ-chunkZ)<=radius)
            result.add(start);
        Collections.sort(result);return result;
    }
    public Start recordNewStart(int x,int z,int y,long seed){
        Start old=start(x,z);if(old!=null)return old;
        Start value=recordStart(x,z,y,seed);value.version=2;markDirty();return value;
    }
    public boolean hasSlice(Start start,int targetChunkX,int targetChunkZ){
        return slices.contains(sliceKey(start.chunkX,start.chunkZ,targetChunkX,targetChunkZ));
    }
    public void recordSlice(Start start,int targetChunkX,int targetChunkZ){
        if(slices.add(sliceKey(start.chunkX,start.chunkZ,targetChunkX,targetChunkZ)))markDirty();
    }
    public boolean claimGuards(int chunkX,int chunkZ){boolean claimed=guards.add(chunkKey(chunkX,chunkZ));if(claimed)markDirty();return claimed;}
    public boolean hasGuard(int x,int z,int index){return guards.contains(chunkKey(x,z))||spawnedGuards.contains(chunkKey(x,z)+":"+index);}
    public void recordGuard(int x,int z,int index){if(spawnedGuards.add(chunkKey(x,z)+":"+index))markDirty();}

    @Override public void readFromNBT(NBTTagCompound tag){
        starts.clear();slices.clear();guards.clear();spawnedGuards.clear();
        NBTTagList startTags=tag.getTagList("Starts",10);
        for(int i=0;i<startTags.tagCount();i++){
            NBTTagCompound value=startTags.getCompoundTagAt(i);
            Start start=new Start(value.getInteger("ChunkX"),value.getInteger("ChunkZ"),
                    value.getInteger("SurfaceY"),value.getLong("LayoutSeed"));
            start.version=value.getInteger("PlanVersion");start.rejected=value.getBoolean("Rejected");
            if(value.hasKey("Plan",10))start.plan=com.mod.htgt6.minecraft1202.Module.World.Modern.JigsawPlacementRuntime120.load(value.getCompoundTag("Plan"));
            int[] samples=value.getIntArray("Terrain");for(int j=0;j+2<samples.length;j+=3)start.heights.put(((long)samples[j]<<32)^(samples[j+1]&0xffffffffL),samples[j+2]);
            starts.put(chunkKey(start.chunkX,start.chunkZ),start);
        }
        readStrings(tag.getTagList("Slices",8),slices);readStrings(tag.getTagList("Guards",8),guards);
        readStrings(tag.getTagList("SpawnedGuards",8),spawnedGuards);
    }
    @Override public void writeToNBT(NBTTagCompound tag){
        NBTTagList startTags=new NBTTagList();List<Start> ordered=new ArrayList<Start>(starts.values());Collections.sort(ordered);
        for(Start start:ordered){NBTTagCompound value=new NBTTagCompound();value.setInteger("ChunkX",start.chunkX);
            value.setInteger("ChunkZ",start.chunkZ);value.setInteger("SurfaceY",start.surfaceY);
            value.setLong("LayoutSeed",start.layoutSeed);value.setInteger("PlanVersion",start.version);value.setBoolean("Rejected",start.rejected);
            if(start.plan!=null)value.setTag("Plan",com.mod.htgt6.minecraft1202.Module.World.Modern.JigsawPlacementRuntime120.save(start.plan));
            int[] samples=new int[start.heights.size()*3];int i=0;for(Map.Entry<Long,Integer> e:start.heights.entrySet()){samples[i++]=(int)(e.getKey()>>32);samples[i++]=(int)(long)e.getKey();samples[i++]=e.getValue();}value.setIntArray("Terrain",samples);
            startTags.appendTag(value);}
        tag.setTag("Starts",startTags);tag.setTag("Slices",writeStrings(slices));tag.setTag("Guards",writeStrings(guards));
        tag.setTag("SpawnedGuards",writeStrings(spawnedGuards));
    }
    private static String chunkKey(int x,int z){return x+","+z;}
    private static String sliceKey(int sx,int sz,int tx,int tz){return sx+","+sz+">"+tx+","+tz;}
    private static void readStrings(NBTTagList list,Set<String> out){for(int i=0;i<list.tagCount();i++)out.add(list.getStringTagAt(i));}
    private static NBTTagList writeStrings(Set<String> values){NBTTagList list=new NBTTagList();List<String> ordered=new ArrayList<String>(values);
        Collections.sort(ordered);for(String value:ordered)list.appendTag(new NBTTagString(value));return list;}

    public static final class Start implements Comparable<Start>{
        public final int chunkX,chunkZ,surfaceY;public final long layoutSeed;
        public int version;public boolean rejected;
        public com.mod.htgt6.minecraft1202.Module.World.Modern.JigsawPlacementRuntime120.Result plan;
        public final Map<Long,Integer> heights=new java.util.TreeMap<Long,Integer>();
        Start(int chunkX,int chunkZ,int surfaceY,long layoutSeed){this.chunkX=chunkX;this.chunkZ=chunkZ;
            this.surfaceY=surfaceY;this.layoutSeed=layoutSeed;}
        @Override public int compareTo(Start other){int x=Integer.compare(chunkX,other.chunkX);return x!=0?x:Integer.compare(chunkZ,other.chunkZ);}
    }
}
