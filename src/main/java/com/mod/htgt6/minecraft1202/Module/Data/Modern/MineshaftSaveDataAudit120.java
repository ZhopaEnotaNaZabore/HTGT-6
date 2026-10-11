package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Read-only audit of vanilla and NovaCraft MapGenStructure saved-data ledgers. */
public final class MineshaftSaveDataAudit120 {
    public static void main(String[] args)throws Exception{
        if(args.length!=2)throw new IllegalArgumentException("Expected Mineshaft.dat and SculkMineshaft.dat");
        audit(args[0],"Mineshaft");
        audit(args[1],"SculkMineshaft");
    }

    private static void audit(String path,String expectedId)throws Exception{
        InputStream input=new FileInputStream(path);NBTTagCompound root;
        try{root=CompressedStreamTools.readCompressed(input);}finally{input.close();}
        NBTTagCompound data=root.hasKey("data",10)?root.getCompoundTag("data"):root;
        NBTTagCompound features=data.getCompoundTag("Features");
        Set keys=features.func_150296_c();Set<String> chunks=new HashSet<String>();
        int valid=0,children=0;
        for(Object rawKey:keys){String key=String.valueOf(rawKey);
            NBTTagCompound start=features.getCompoundTag(key);
            String id=start.getString("id");int[] box=start.getIntArray("BB");
            int chunkX=start.getInteger("ChunkX"),chunkZ=start.getInteger("ChunkZ");
            NBTTagList pieces=start.getTagList("Children",10);
            require(expectedId.equals(id),path+" contains unexpected structure "+id);
            require(box.length==6&&box[0]<=box[3]&&box[1]<=box[4]&&box[2]<=box[5],
                    path+" contains invalid bounding box at "+key);
            require(chunks.add(chunkX+","+chunkZ),path+" contains duplicate start chunk "+chunkX+","+chunkZ);
            require(pieces.tagCount()>0,path+" contains an empty structure start at "+key);
            valid++;children+=pieces.tagCount();
        }
        System.out.println(expectedId+" save audit: starts="+valid+", pieces="+children);
    }
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private MineshaftSaveDataAudit120(){ }
}
