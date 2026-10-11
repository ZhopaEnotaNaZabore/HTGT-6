package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

/** Persistent monument starts and one-shot elder-guardian slots. */
public final class OceanMonumentData120 extends WorldSavedData {
    private static final String NAME="HTGT6OceanMonuments";
    private static final int HALF_WIDTH=29;
    private final Set<String> starts=new HashSet<String>(),claimedMobs=new HashSet<String>();
    public OceanMonumentData120(){super(NAME);}public OceanMonumentData120(String name){super(name);}
    public static OceanMonumentData120 get(World world){OceanMonumentData120 data=
            (OceanMonumentData120)world.loadItemData(OceanMonumentData120.class,NAME);
        if(data==null){data=new OceanMonumentData120();world.setItemData(NAME,data);}return data;}
    public void recordStart(int chunkX,int chunkZ){if(starts.add(chunkX+","+chunkZ))markDirty();}
    public boolean claimMob(int chunkX,int chunkZ,int slot){boolean result=claimedMobs.add(
            chunkX+","+chunkZ+","+slot);if(result)markDirty();return result;}
    public boolean hasStart(int chunkX,int chunkZ){return starts.contains(chunkX+","+chunkZ);}
    /**
     * Structure-aware spawn predicate used by ordinary guardians.  A monument start is stored as
     * its centre chunk; the generated building occupies centre-29 through centre+28, matching the
     * 58 block bounding box used by the generator.
     */
    public boolean isInsideMonument(int blockX,int blockZ){
        for(String key:starts){
            int comma=key.indexOf(',');if(comma<1)continue;
            try{
                int chunkX=Integer.parseInt(key.substring(0,comma));
                int chunkZ=Integer.parseInt(key.substring(comma+1));
                int centreX=(chunkX<<4)+8,centreZ=(chunkZ<<4)+8;
                if(blockX>=centreX-HALF_WIDTH&&blockX<centreX+HALF_WIDTH
                        &&blockZ>=centreZ-HALF_WIDTH&&blockZ<centreZ+HALF_WIDTH)return true;
            }catch(NumberFormatException ignored){}
        }
        return false;
    }
    @Override public void readFromNBT(NBTTagCompound tag){starts.clear();claimedMobs.clear();
        read(tag.getTagList("Starts",8),starts);read(tag.getTagList("ClaimedMobs",8),claimedMobs);}
    @Override public void writeToNBT(NBTTagCompound tag){tag.setTag("Starts",write(starts));
        tag.setTag("ClaimedMobs",write(claimedMobs));}
    private static void read(NBTTagList list,Set<String> out){for(int i=0;i<list.tagCount();i++)
        out.add(list.getStringTagAt(i));}
    private static NBTTagList write(Set<String> values){NBTTagList list=new NBTTagList();
        for(String value:values)list.appendTag(new NBTTagString(value));return list;}
}
