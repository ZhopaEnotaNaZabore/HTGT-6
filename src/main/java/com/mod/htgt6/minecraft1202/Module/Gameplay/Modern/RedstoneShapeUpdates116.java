package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.world.*;
/** Only live wire connection masks are cached; chunk unload frees them in constant time. */
public final class RedstoneShapeUpdates116 {
    private static final Map<World,Map<Long,Map<Integer,Integer>>> WORLDS=new WeakHashMap<World,Map<Long,Map<Integer,Integer>>>();
    private static long chunk(int x,int z){return ((long)(x>>4)<<32)^((z>>4)&0xffffffffL);}
    private static int cell(int x,int y,int z){return (y<<8)|((x&15)<<4)|(z&15);}
    public static void changed(BlockRedstoneWire wire,World w,int x,int y,int z){
        if(w.isRemote||wire!=Blocks.redstone_wire&&wire!=RedstoneWire116.dot)return;
        if(w.getBlock(x,y,z)!=wire){remove(w,x,y,z);return;}
        int mask=0;int[] sides=RedstoneWire116.sides(w,x,y,z);for(int i=0;i<4;i++)if(sides[i]!=0)mask|=1<<i;
        Integer previous;synchronized(WORLDS){Map<Long,Map<Integer,Integer>> chunks=WORLDS.get(w);if(chunks==null){chunks=new HashMap<Long,Map<Integer,Integer>>();WORLDS.put(w,chunks);}long key=chunk(x,z);Map<Integer,Integer> cells=chunks.get(key);if(cells==null){cells=new HashMap<Integer,Integer>();chunks.put(key,cells);}previous=cells.put(cell(x,y,z),mask);}
        if(previous!=null&&previous==mask)return;
        w.markBlockForUpdate(x,y,z);int[] dx={0,1,0,-1},dz={-1,0,1,0};
        for(int i=0;i<4;i++)if(previous==null||((previous^mask)&(1<<i))!=0){int nx=x+dx[i],nz=z+dz[i];if(!w.blockExists(nx,y,nz))continue;Block receiver=w.getBlock(nx,y,nz);if(receiver instanceof BlockRedstoneWire)continue;
            w.notifyBlockOfNeighborChange(nx,y,nz,wire);if(receiver.isNormalCube())w.notifyBlocksOfNeighborChange(nx,y,nz,wire);
        }
    }
    public static void remove(World w,int x,int y,int z){synchronized(WORLDS){Map<Long,Map<Integer,Integer>> chunks=WORLDS.get(w);if(chunks!=null){Map<Integer,Integer> cells=chunks.get(chunk(x,z));if(cells!=null){cells.remove(cell(x,y,z));if(cells.isEmpty())chunks.remove(chunk(x,z));}}}}
    @SubscribeEvent public void unload(ChunkEvent.Unload event){synchronized(WORLDS){Map<Long,Map<Integer,Integer>> chunks=WORLDS.get(event.world);if(chunks!=null)chunks.remove(chunk(event.getChunk().xPosition<<4,event.getChunk().zPosition<<4));}}
    @SubscribeEvent public void unload(WorldEvent.Unload event){synchronized(WORLDS){WORLDS.remove(event.world);}}
}
