package com.mod.htgt6.minecraft1202.Module.World.Modern;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import com.mod.htgt6.common.util.WorldSafety;
/** Generate once into an overlay; replay each recorded cell only in its owning chunk. */
public final class VillageFeaturePlan120 {
    public static void place(World world,JigsawPlacementRuntime120.Piece piece,int cx,int cz){
        String key=piece.element.feature+":"+piece.x+","+piece.y+","+piece.z;
        JigsawPlansData120 data=JigsawPlansData120.get(world);NBTTagCompound plan=data.feature(key);
        if(plan==null){
            // Never save a truncated canopy because a neighbouring chunk is temporarily absent.
            for(int x=(piece.x-16)>>4;x<=(piece.x+16)>>4;x++)
                for(int z=(piece.z-16)>>4;z<=(piece.z+16)>>4;z++)
                    if(!WorldSafety.isChunkLoaded(world,x,z))return;
            Buffer buffer=new Buffer(world,piece.x,piece.z);
            FuturaVillageGenerator120.placeFeature(buffer,piece);plan=buffer.save();data.feature(key,plan);
        }
        replay(world,plan,cx,cz);
    }
    public static void replay(World world,NBTTagCompound plan,int cx,int cz){
        NBTTagList blocks=plan.getTagList("Cells",10);for(int i=0;i<blocks.tagCount();i++){
            NBTTagCompound n=blocks.getCompoundTagAt(i);int[] p=n.getIntArray("Pos");
            if((p[0]>>4)!=cx||(p[2]>>4)!=cz||p[1]<1||p[1]>=world.getActualHeight())continue;
            Block block=(Block)Block.blockRegistry.getObject(n.getString("Block"));
            if(block!=null)world.setBlock(p[0],p[1],p[2],block,n.getInteger("Meta"),2);
        }
    }
    private static final class Cell {final int x,y,z,meta;final Block block;Cell(int x,int y,int z,Block b,int m){this.x=x;this.y=y;this.z=z;block=b;meta=m;}}
    private static final class Buffer extends World {
        final World source;final int ox,oz;final Map<String,Cell> cells=new LinkedHashMap<String,Cell>();
        Buffer(World source,int x,int z){super(null,"Village feature plan",new WorldProviderSurface(),new WorldSettings(source.getWorldInfo()),new net.minecraft.profiler.Profiler());this.source=source;ox=x;oz=z;worldInfo=source.getWorldInfo();isRemote=true;rand=new Random(source.getSeed());}
        String key(int x,int y,int z){return x+","+y+","+z;}
        boolean bounded(int x,int z){return Math.abs(x-ox)<=16&&Math.abs(z-oz)<=16;}
        public Block getBlock(int x,int y,int z){Cell c=cells.get(key(x,y,z));return c!=null?c.block:bounded(x,z)?WorldSafety.getSafeBlock(source,x,y,z):Blocks.air;}
        public int getBlockMetadata(int x,int y,int z){Cell c=cells.get(key(x,y,z));if(c!=null)return c.meta;net.minecraft.world.chunk.Chunk chunk=bounded(x,z)?WorldSafety.getLoadedChunk(source,x>>4,z>>4):null;return chunk==null||y<0||y>=getActualHeight()?0:chunk.getBlockMetadata(x&15,y,z&15);}
        public boolean isAirBlock(int x,int y,int z){return getBlock(x,y,z).isAir(this,x,y,z);}
        public boolean blockExists(int x,int y,int z){return y>=0&&y<getActualHeight()&&bounded(x,z)&&WorldSafety.isChunkLoaded(source,x>>4,z>>4);}
        public boolean setBlock(int x,int y,int z,Block b,int meta,int flags){if(!blockExists(x,y,z))return false;cells.put(key(x,y,z),new Cell(x,y,z,b,meta));return true;}
        public boolean setBlockMetadataWithNotify(int x,int y,int z,int m,int flags){return setBlock(x,y,z,getBlock(x,y,z),m,flags);}
        public int getActualHeight(){return source==null?384:source.getActualHeight();}public int getHeight(){return getActualHeight();}public long getSeed(){return source==null?0:source.getSeed();}
        public TileEntity getTileEntity(int x,int y,int z){return null;}
        protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}protected int func_152379_p(){return 0;}public net.minecraft.entity.Entity getEntityByID(int id){return null;}
        NBTTagCompound save(){NBTTagCompound tag=new NBTTagCompound();NBTTagList list=new NBTTagList();for(Cell c:cells.values()){NBTTagCompound n=new NBTTagCompound();n.setIntArray("Pos",new int[]{c.x,c.y,c.z});n.setString("Block",String.valueOf(Block.blockRegistry.getNameForObject(c.block)));n.setInteger("Meta",c.meta);list.appendTag(n);}tag.setTag("Cells",list);return tag;}
    }
    private VillageFeaturePlan120(){}
}
