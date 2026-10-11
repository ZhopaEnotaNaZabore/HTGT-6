package com.mod.htgt6.minecraft1202.Module.World.Modern;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import java.util.Collections;
import java.util.List;

/** Fixed, fully prepared feature region. It never provides a server chunk during feature execution. */
public final class FuturaGenerationRegion120 extends World {
    public static final int READ_RADIUS=2,WRITE_RADIUS=1;
    private final World source;
    private final int centerX,centerZ,height;
    private final Chunk[][] chunks=new Chunk[5][5];

    public FuturaGenerationRegion120(World source,int chunkX,int chunkZ){
        super(null,"Futura feature region",new RegionProvider(source),new WorldSettings(source.getWorldInfo()),new Profiler());
        this.source=source;this.centerX=chunkX;this.centerZ=chunkZ;this.height=source.getActualHeight();
        this.worldInfo=source.getWorldInfo();this.isRemote=source.isRemote;
        this.provider.registerWorld(this);
        this.difficultySetting=source.difficultySetting;
        for(int dx=-READ_RADIUS;dx<=READ_RADIUS;dx++)for(int dz=-READ_RADIUS;dz<=READ_RADIUS;dz++){
            Chunk chunk=com.mod.htgt6.common.util.WorldSafety.getLoadedChunk(source,chunkX+dx,chunkZ+dz);
            if(chunk==null)throw new IllegalStateException("Unprepared Futura region at "+(chunkX+dx)+","+(chunkZ+dz));
            chunks[dx+READ_RADIUS][dz+READ_RADIUS]=chunk;
        }
        this.chunkProvider=new RegionChunks();
    }
    public static World unwrap(World world){return world instanceof FuturaGenerationRegion120?((FuturaGenerationRegion120)world).source:world;}
    public boolean writable(int x,int z){return Math.abs((x>>4)-centerX)<=WRITE_RADIUS&&Math.abs((z>>4)-centerZ)<=WRITE_RADIUS;}
    private boolean readable(int cx,int cz){return Math.abs(cx-centerX)<=READ_RADIUS&&Math.abs(cz-centerZ)<=READ_RADIUS;}
    @Override public Chunk getChunkFromChunkCoords(int cx,int cz){
        if(!readable(cx,cz))throw new IllegalStateException("Feature read outside Futura region: "+cx+","+cz+" from "+centerX+","+centerZ);
        return chunks[cx-centerX+READ_RADIUS][cz-centerZ+READ_RADIUS];
    }
    @Override public Block getBlock(int x,int y,int z){
        return y<0||y>=height?Blocks.air:getChunkFromChunkCoords(x>>4,z>>4).getBlock(x&15,y,z&15);
    }
    @Override public int getBlockMetadata(int x,int y,int z){return y<0||y>=height?0:getChunkFromChunkCoords(x>>4,z>>4).getBlockMetadata(x&15,y,z&15);}
    @Override public boolean setBlock(int x,int y,int z,Block block,int meta,int flags){
        if(y<0||y>=height||!writable(x,z))return false;
        return source.setBlock(x,y,z,block,meta,flags);
    }
    @Override public boolean setBlockMetadataWithNotify(int x,int y,int z,int meta,int flags){
        return y>=0&&y<height&&writable(x,z)&&source.setBlockMetadataWithNotify(x,y,z,meta,flags);
    }
    @Override public TileEntity getTileEntity(int x,int y,int z){
        if(y<0||y>=height)return null;
        getChunkFromChunkCoords(x>>4,z>>4);return source.getTileEntity(x,y,z);
    }
    @Override public void setTileEntity(int x,int y,int z,TileEntity tile){if(writable(x,z)&&y>=0&&y<height)source.setTileEntity(x,y,z,tile);}
    @Override public void scheduleBlockUpdate(int x,int y,int z,Block b,int delay){if(writable(x,z))source.scheduleBlockUpdate(x,y,z,b,delay);}
    @Override public boolean spawnEntityInWorld(Entity entity){entity.worldObj=source;return source.spawnEntityInWorld(entity);}
    @Override public int getHeight(){return height;}
    @Override public int getActualHeight(){return height;}
    @Override public long getSeed(){return source==null?super.getSeed():source.getSeed();}
    @Override public BiomeGenBase getBiomeGenForCoords(int x,int z){
        getChunkFromChunkCoords(x>>4,z>>4);
        return source.getBiomeGenForCoords(x,z);
    }
    @Override public int getHeightValue(int x,int z){
        return placementHeight("WORLD_SURFACE_WG",x,z);
    }
    /** Resolve the resident chunk once per column, without caching mutable block state. */
    public int placementHeight(String type,int x,int z){
        Chunk chunk=getChunkFromChunkCoords(x>>4,z>>4);
        int localX=x&15,localZ=z&15;
        boolean floor=type.startsWith("OCEAN_FLOOR"),motion=type.startsWith("MOTION_BLOCKING");
        for(int y=height-1;y>=0;y--){
            Block b=chunk.getBlock(localX,y,localZ);
            if(floor?b.getMaterial().blocksMovement()
                    :motion?b.getMaterial().blocksMovement()||b.getMaterial().isLiquid():b!=Blocks.air)return y+1;
        }
        return 0;
    }
    @Override public int getTopSolidOrLiquidBlock(int x,int z){
        Chunk chunk=getChunkFromChunkCoords(x>>4,z>>4);
        for(int y=height-1;y>=0;y--){Block b=chunk.getBlock(x&15,y,z&15);
            if((b.getMaterial().blocksMovement()||b.getMaterial().isLiquid())&&!b.isLeaves(this,x,y,z))return y+1;}
        return 0;
    }
    @Override public int getPrecipitationHeight(int x,int z){return FuturaPlacementExecutor120.height(this,"MOTION_BLOCKING",x,z);}
    @Override protected IChunkProvider createChunkProvider(){return null;}
    @Override protected int func_152379_p(){return 0;}
    @Override public Entity getEntityByID(int id){return source.getEntityByID(id);}

    private static final class RegionProvider extends WorldProviderSurface {
        private final World source;
        RegionProvider(World source){this.source=source;this.dimensionId=source.provider.dimensionId;}
        @Override protected void registerWorldChunkManager(){this.worldChunkMgr=source.getWorldChunkManager();}
        @Override public int getHeight(){return source.getActualHeight();}
        @Override public int getActualHeight(){return source.getActualHeight();}
    }
    private final class RegionChunks implements IChunkProvider {
        public boolean chunkExists(int x,int z){return readable(x,z);}
        public Chunk provideChunk(int x,int z){return getChunkFromChunkCoords(x,z);}
        public Chunk loadChunk(int x,int z){return provideChunk(x,z);}
        public void populate(IChunkProvider p,int x,int z){throw new IllegalStateException("Nested region population");}
        public boolean saveChunks(boolean all,IProgressUpdate p){return false;}
        public void saveExtraData(){}
        public boolean unloadQueuedChunks(){return false;}
        public boolean canSave(){return false;}
        public String makeString(){return "Futura fixed feature region";}
        public int getLoadedChunkCount(){return 25;}
        public List getPossibleCreatures(EnumCreatureType type,int x,int y,int z){return Collections.emptyList();}
        public ChunkPosition func_147416_a(World w,String name,int x,int y,int z){return null;}
        public void recreateStructures(int x,int z){}
    }
}
