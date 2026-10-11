package com.mod.htgt6.minecraft1202.Module.World.Modern;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
/** Accepted plans survive code changes; legacy worlds conservatively keep their old village algorithm. */
public final class JigsawPlansData120 extends WorldSavedData {
    private static final String NAME="HTGT6JigsawPlans120";
    private final Map<String,JigsawPlacementRuntime120.Result> plans=new TreeMap<String,JigsawPlacementRuntime120.Result>();
    private final Map<String,NBTTagCompound> features=new TreeMap<String,NBTTagCompound>();
    public NBTTagCompound feature(String key){return features.get(key);}
    public void feature(String key,NBTTagCompound value){features.put(key,value);markDirty();}
    public JigsawPlansData120(){super(NAME);}public JigsawPlansData120(String name){super(name);}
    public static JigsawPlansData120 get(World w){JigsawPlansData120 d=(JigsawPlansData120)w.loadItemData(JigsawPlansData120.class,NAME);if(d==null){d=new JigsawPlansData120();w.setItemData(NAME,d);}return d;}
    public JigsawPlacementRuntime120.Result get(String key){return plans.get(key);}
    public void put(String key,JigsawPlacementRuntime120.Result plan){plans.put(key,plan);markDirty();}
    public void readFromNBT(NBTTagCompound tag){plans.clear();features.clear();NBTTagList list=tag.getTagList("Plans",10);for(int i=0;i<list.tagCount();i++){NBTTagCompound p=list.getCompoundTagAt(i);plans.put(p.getString("Key"),JigsawPlacementRuntime120.load(p.getCompoundTag("Plan")));}list=tag.getTagList("Features",10);for(int i=0;i<list.tagCount();i++){NBTTagCompound p=list.getCompoundTagAt(i);features.put(p.getString("Key"),p.getCompoundTag("Blocks"));}}
    public void writeToNBT(NBTTagCompound tag){NBTTagList list=new NBTTagList();for(Map.Entry<String,JigsawPlacementRuntime120.Result> e:plans.entrySet()){NBTTagCompound p=new NBTTagCompound();p.setString("Key",e.getKey());p.setTag("Plan",JigsawPlacementRuntime120.save(e.getValue()));list.appendTag(p);}tag.setTag("Plans",list);list=new NBTTagList();for(Map.Entry<String,NBTTagCompound> e:features.entrySet()){NBTTagCompound p=new NBTTagCompound();p.setString("Key",e.getKey());p.setTag("Blocks",e.getValue());list.appendTag(p);}tag.setTag("Features",list);}
    public static boolean touchesOldTerrain(World w,int cx,int cz){
        net.minecraft.world.chunk.storage.IChunkLoader loader=w.getSaveHandler().getChunkLoader(w.provider);
        java.io.File folder=w.getSaveHandler().getWorldDirectory();
        if(w.provider.getSaveFolder()!=null)folder=new java.io.File(folder,w.provider.getSaveFolder());
        for(int x=cx-6;x<=cx+6;x++)for(int z=cz-6;z<=cz+6;z++){
            net.minecraft.world.chunk.Chunk loaded=com.mod.htgt6.common.util.WorldSafety.getLoadedChunk(w,x,z);
            if(loaded!=null&&loaded.isTerrainPopulated)return true;
            // chunkExists opens (and otherwise creates) a region file: only inspect existing regions.
            java.io.File region=new java.io.File(new java.io.File(folder,"region"),"r."+(x>>5)+"."+(z>>5)+".mca");
            if(region.isFile()&&loader instanceof net.minecraft.world.chunk.storage.AnvilChunkLoader
                    &&((net.minecraft.world.chunk.storage.AnvilChunkLoader)loader).chunkExists(w,x,z))return true;
        }
        return false;
    }
}
