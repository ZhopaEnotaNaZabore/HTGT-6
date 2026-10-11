package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.world.*;

/** Persistent start descriptors and per-target completion ledger for Trail Ruins. */
public final class TrailRuinsData120 extends WorldSavedData{
    private static final String NAME="HTGT6TrailRuins120";private final Map<String,Start> starts=new HashMap<String,Start>();private final Set<String> slices=new HashSet<String>();
    public TrailRuinsData120(){super(NAME);}public TrailRuinsData120(String name){super(name);}
    public static TrailRuinsData120 get(World world){TrailRuinsData120 d=(TrailRuinsData120)world.loadItemData(TrailRuinsData120.class,NAME);if(d==null){d=new TrailRuinsData120();world.setItemData(NAME,d);}return d;}
    public Start start(int x,int z){return starts.get(key(x,z));}
    public Start record(int x,int z,int surface,long seed){Start s=start(x,z);if(s!=null)return s;s=new Start(x,z,surface,seed);starts.put(key(x,z),s);markDirty();return s;}
    public List<Start> near(int x,int z,int radius){List<Start> out=new ArrayList<Start>();for(Start s:starts.values())if(Math.abs(s.chunkX-x)<=radius&&Math.abs(s.chunkZ-z)<=radius)out.add(s);Collections.sort(out);return out;}
    public boolean complete(Start s,int x,int z){return slices.contains(slice(s,x,z));}public void markComplete(Start s,int x,int z){if(slices.add(slice(s,x,z)))markDirty();}
    @Override public void readFromNBT(NBTTagCompound root){starts.clear();slices.clear();NBTTagList list=root.getTagList("Starts",10);for(int i=0;i<list.tagCount();i++){NBTTagCompound n=list.getCompoundTagAt(i);Start s=new Start(n.getInteger("X"),n.getInteger("Z"),n.getInteger("Surface"),n.getLong("Seed"));starts.put(key(s.chunkX,s.chunkZ),s);}NBTTagList done=root.getTagList("Slices",8);for(int i=0;i<done.tagCount();i++)slices.add(done.getStringTagAt(i));}
    @Override public void writeToNBT(NBTTagCompound root){NBTTagList list=new NBTTagList();List<Start> ordered=new ArrayList<Start>(starts.values());Collections.sort(ordered);for(Start s:ordered){NBTTagCompound n=new NBTTagCompound();n.setInteger("X",s.chunkX);n.setInteger("Z",s.chunkZ);n.setInteger("Surface",s.surfaceY);n.setLong("Seed",s.layoutSeed);list.appendTag(n);}root.setTag("Starts",list);NBTTagList done=new NBTTagList();List<String> d=new ArrayList<String>(slices);Collections.sort(d);for(String s:d)done.appendTag(new NBTTagString(s));root.setTag("Slices",done);}
    private static String key(int x,int z){return x+","+z;}private static String slice(Start s,int x,int z){return key(s.chunkX,s.chunkZ)+">"+key(x,z);}
    public static final class Start implements Comparable<Start>{public final int chunkX,chunkZ,surfaceY;public final long layoutSeed;Start(int x,int z,int y,long seed){chunkX=x;chunkZ=z;surfaceY=y;layoutSeed=seed;}public int compareTo(Start s){int c=Integer.compare(chunkX,s.chunkX);return c!=0?c:Integer.compare(chunkZ,s.chunkZ);}}
}
