package com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.nbt.*;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;

/** Operator structure editor using named blocks and legacy metadata, never numeric IDs. */
public final class TileEntityStructure120 extends TileEntity {
    public String name = "";
    public String metadata = "";
    public int offsetX, offsetY = 1, offsetZ, sizeX, sizeY, sizeZ;
    private boolean powered;
    public int clientRevision;
    public float integrity=1F;
    public long seed;
    public int rotation, mirror;
    public boolean showBoundingBox=true;
    public static final int MAX_SIZE = 48;

    @Override public boolean canUpdate() { return false; }
    @Override public net.minecraft.util.AxisAlignedBB getRenderBoundingBox() {
        if(getBlockMetadata()==1&&!showBoundingBox)return net.minecraft.util.AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord,xCoord+1,yCoord+1,zCoord+1);
        return net.minecraft.util.AxisAlignedBB.getBoundingBox(
                Math.min(xCoord,xCoord+offsetX+previewBounds()[0]),Math.min(yCoord,yCoord+offsetY),Math.min(zCoord,zCoord+offsetZ+previewBounds()[1]),
                Math.max(xCoord+1,xCoord+offsetX+previewBounds()[2]),Math.max(yCoord+1,yCoord+offsetY+sizeY),Math.max(zCoord+1,zCoord+offsetZ+previewBounds()[3]));
    }
    public int[] previewBounds() {
        return StructureTransform120.bounds(Math.max(1,sizeX),Math.max(1,sizeZ),getBlockMetadata()==1?rotation:0,getBlockMetadata()==1?mirror:0);
    }
    @Override public boolean shouldRefresh(Block oldBlock,Block newBlock,int oldMeta,int newMeta,net.minecraft.world.World world,int x,int y,int z) {
        return oldBlock!=newBlock;
    }
    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("name", name);
        tag.setString("metadata",metadata);
        tag.setInteger("posX", offsetX);tag.setInteger("posY", offsetY);tag.setInteger("posZ", offsetZ);
        tag.setInteger("sizeX", sizeX);tag.setInteger("sizeY", sizeY);tag.setInteger("sizeZ", sizeZ);
        tag.setBoolean("powered", powered);
        tag.setBoolean("showboundingbox",showBoundingBox);
        tag.setInteger("rotation",rotation);tag.setInteger("mirror",mirror);
        tag.setFloat("integrity",integrity);tag.setLong("seed",seed);
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        name = tag.getString("name");
        metadata=tag.getString("metadata");
        if(metadata.length()>128)metadata=metadata.substring(0,128);
        offsetX=clamp(tag.getInteger("posX"),-48,48);offsetY=clamp(tag.getInteger("posY"),-48,48);offsetZ=clamp(tag.getInteger("posZ"),-48,48);
        sizeX=clamp(tag.getInteger("sizeX"),0,48);sizeY=clamp(tag.getInteger("sizeY"),0,48);sizeZ=clamp(tag.getInteger("sizeZ"),0,48);
        powered=tag.getBoolean("powered");
        showBoundingBox=!tag.hasKey("showboundingbox")||tag.getBoolean("showboundingbox");
        rotation=clamp(tag.getInteger("rotation"),0,3);mirror=clamp(tag.getInteger("mirror"),0,2);
        integrity=tag.hasKey("integrity",99)?tag.getFloat("integrity"):1F;
        if(Float.isNaN(integrity))integrity=1F;
        integrity=Math.max(0F,Math.min(1F,integrity));seed=tag.getLong("seed");
    }
    public static int clamp(int n,int min,int max) { return Math.max(min,Math.min(max,n)); }
    public void changed() { markDirty();worldObj.markBlockForUpdate(xCoord,yCoord,zCoord); }
    @Override public Packet getDescriptionPacket() {
        NBTTagCompound tag=new NBTTagCompound();writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,7,tag);
    }
    @Override public void onDataPacket(NetworkManager manager,S35PacketUpdateTileEntity packet) { readFromNBT(packet.func_148857_g());clientRevision++; }

    public static boolean validName(String value) {
        return value!=null && value.length()<=128 && value.matches("(?:[a-z0-9_.-]+:)?[a-z0-9_./-]+")
                && !value.contains("..") && !value.startsWith("/") && !value.contains(":/");
    }
    private static String canonicalName(String value) { return value.indexOf(':')<0?"minecraft:"+value:value; }
    private File file() throws IOException {
        if(!validName(name))throw new IOException("Invalid structure name");
        String[] parts=canonicalName(name).split(":",2);
        File root=worldObj.getSaveHandler().getMapFileFromName("structure_location").getParentFile().getParentFile();
        File directory=new File(root,"generated/"+parts[0]+"/structures").getCanonicalFile();
        File result=new File(directory,parts[1]+".nbt").getCanonicalFile();
        if(!result.toPath().startsWith(directory.toPath()))throw new IOException("Invalid structure path");
        return result;
    }
    private void checkRegion(int sx,int sy,int sz) throws IOException { checkRegion(sx,sy,sz,false); }
    private void checkRegion(int sx,int sy,int sz,boolean transformed) throws IOException {
        if(sx<1||sy<1||sz<1||sx>48||sy>48||sz>48)throw new IOException("Structure size must be 1..48");
        int x=xCoord+offsetX,y=yCoord+offsetY,z=zCoord+offsetZ;
        if(y<0||y+sy>worldObj.getActualHeight())throw new IOException("Structure outside build height");
        int[] bounds=StructureTransform120.bounds(sx,sz,transformed?rotation:0,transformed?mirror:0);
        for(int cx=(x+bounds[0])>>4;cx<=(x+bounds[2]-1)>>4;cx++)for(int cz=(z+bounds[1])>>4;cz<=(z+bounds[3]-1)>>4;cz++)
            if(!worldObj.getChunkProvider().chunkExists(cx,cz))throw new IOException("Structure includes unloaded chunks");
    }
    public NBTTagCompound capture() throws IOException {
        checkRegion(sizeX,sizeY,sizeZ);
        NBTTagCompound result=new NBTTagCompound();
        result.setInteger("HTGT6StructureVersion",1);
        result.setTag("size",ints(sizeX,sizeY,sizeZ));
        NBTTagList palette=new NBTTagList(),blocks=new NBTTagList();
        Map<String,Integer> states=new LinkedHashMap<String,Integer>();
        for(int y=0;y<sizeY;y++)for(int x=0;x<sizeX;x++)for(int z=0;z<sizeZ;z++) {
            int wx=xCoord+offsetX+x,wy=yCoord+offsetY+y,wz=zCoord+offsetZ+z;
            Block block=worldObj.getBlock(wx,wy,wz);
            if(block==FrostburnContent.structureVoid)continue;
            String id=Block.blockRegistry.getNameForObject(block);
            if(id==null)throw new IOException("Unregistered block in structure");
            int meta=worldObj.getBlockMetadata(wx,wy,wz);
            String key=id+"/"+meta;Integer state=states.get(key);
            if(state==null) {
                state=states.size();states.put(key,state);
                NBTTagCompound entry=new NBTTagCompound();entry.setString("Name",id);entry.setInteger("HTGT6Meta",meta);palette.appendTag(entry);
            }
            NBTTagCompound entry=new NBTTagCompound();entry.setTag("pos",ints(x,y,z));entry.setInteger("state",state);
            TileEntity tile=worldObj.getTileEntity(wx,wy,wz);
            if(tile!=null) { NBTTagCompound data=new NBTTagCompound();tile.writeToNBT(data);data.removeTag("x");data.removeTag("y");data.removeTag("z");entry.setTag("nbt",data); }
            blocks.appendTag(entry);
        }
        result.setTag("palette",palette);result.setTag("blocks",blocks);result.setTag("entities",new NBTTagList());
        return result;
    }
    public void save() throws IOException {
        NBTTagCompound template=capture();File target=file();
        Files.createDirectories(target.toPath().getParent());
        Path temporary=Files.createTempFile(target.toPath().getParent(),"structure-",".tmp");
        try {
            try(OutputStream out=Files.newOutputStream(temporary)) { CompressedStreamTools.writeCompressed(template,out); }
            try { Files.move(temporary,target.toPath(),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
            catch(AtomicMoveNotSupportedException e) { Files.move(temporary,target.toPath(),StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
    public void load() throws IOException {
        load(false);
    }
    /** GUI first prepares changed dimensions; redstone performs an immediate load. */
    public boolean load(boolean prepareSize) throws IOException {
        File source=file();
        if(!source.isFile()||source.length()>16*1024*1024)throw new IOException("Structure missing or too large");
        NBTTagCompound template;
        try(DataInputStream in=new DataInputStream(new java.util.zip.GZIPInputStream(new FileInputStream(source)))) {
            template=CompressedStreamTools.func_152456_a(in,new NBTSizeTracker(32L*1024L*1024L));
        } catch(RuntimeException error) { throw new IOException("Invalid or oversized structure NBT",error); }
        if(template.getInteger("HTGT6StructureVersion")!=1)throw new IOException("Unsupported structure palette version");
        NBTTagList dimensions=template.getTagList("size",3);
        if(dimensions.tagCount()!=3)throw new IOException("Invalid structure size");
        int sx=intAt(dimensions,0),sy=intAt(dimensions,1),sz=intAt(dimensions,2);
        if(sx<1||sy<1||sz<1||sx>48||sy>48||sz>48)throw new IOException("Invalid structure size");
        if(prepareSize&&(sx!=sizeX||sy!=sizeY||sz!=sizeZ)) {
            sizeX=sx;sizeY=sy;sizeZ=sz;changed();return false;
        }
        place(template);return true;
    }
    public void place(NBTTagCompound template) throws IOException {
        // A legacy palette is explicitly versioned. Modern properties need a
        // state translator; never silently discard them or replace with air.
        if(template.getInteger("HTGT6StructureVersion")!=1)throw new IOException("Unsupported structure palette version");
        NBTTagList dimensions=template.getTagList("size",3);
        if(dimensions.tagCount()!=3)throw new IOException("Invalid structure size");
        int sx=intAt(dimensions,0),sy=intAt(dimensions,1),sz=intAt(dimensions,2);
        if(rotation<0||rotation>3||mirror<0||mirror>2)throw new IOException("Invalid structure transform");
        checkRegion(sx,sy,sz,true);
        NBTTagList palette=template.getTagList("palette",10),entries=template.getTagList("blocks",10);
        if(palette.tagCount()>sx*sy*sz||entries.tagCount()>sx*sy*sz)throw new IOException("Invalid structure counts");
        Block[] blocks=new Block[palette.tagCount()];int[] meta=new int[blocks.length];
        for(int i=0;i<blocks.length;i++) {
            NBTTagCompound p=palette.getCompoundTagAt(i);String id=p.getString("Name");
            if(!Block.blockRegistry.containsKey(id))throw new IOException("Missing block: "+id);
            blocks[i]=(Block)Block.blockRegistry.getObject(id);meta[i]=p.getInteger("HTGT6Meta");
            if(meta[i]<0||meta[i]>15)throw new IOException("Invalid block metadata");
            meta[i]=StructureTransform120.metadata(blocks[i],meta[i],rotation,mirror);
        }
        Set<Integer> positions=new HashSet<Integer>();
        // Validate every entry before changing the world.
        for(int i=0;i<entries.tagCount();i++) {
            NBTTagCompound e=entries.getCompoundTagAt(i);NBTTagList p=e.getTagList("pos",3);int state=e.getInteger("state");
            if(p.tagCount()!=3||state<0||state>=blocks.length)throw new IOException("Invalid block entry");
            int x=intAt(p,0),y=intAt(p,1),z=intAt(p,2);
            if(x<0||x>=sx||y<0||y>=sy||z<0||z>=sz||!positions.add(x+sx*(z+sz*y)))throw new IOException("Invalid or repeated block position");
            if(e.hasKey("nbt",10))StructureTransform120.validateTile(blocks[state],e.getCompoundTag("nbt"),rotation,mirror);
            if(e.hasKey("nbt",10) && TileEntity.createAndLoadEntity(e.getCompoundTag("nbt"))==null)
                throw new IOException("Missing tile entity type");
        }
        boolean[] selected=new boolean[entries.tagCount()];
        Random random=seed==0?new Random():new Random(seed);
        for(int i=0;i<entries.tagCount();i++) {
            NBTTagCompound e=entries.getCompoundTagAt(i);NBTTagList p=e.getTagList("pos",3);int state=e.getInteger("state");
            if(blocks[state]==FrostburnContent.structureVoid)continue;
            if(integrity<1F&&random.nextFloat()>integrity)continue;
            selected[i]=true;
            int[] transformed=StructureTransform120.position(intAt(p,0),intAt(p,2),rotation,mirror);
            int x=xCoord+offsetX+transformed[0],y=yCoord+offsetY+intAt(p,1),z=zCoord+offsetZ+transformed[1];
            worldObj.removeTileEntity(x,y,z);
            worldObj.setBlock(x,y,z,blocks[state],meta[state],2);
            if(worldObj.getBlock(x,y,z)!=blocks[state])
                throw new IOException("Block placement rejected at "+x+","+y+","+z);
            if(e.hasKey("nbt",10)) {
                NBTTagCompound data=(NBTTagCompound)e.getCompoundTag("nbt").copy();data.setInteger("x",x);data.setInteger("y",y);data.setInteger("z",z);
                TileEntity tile=TileEntity.createAndLoadEntity(data);
                if(tile!=null)worldObj.setTileEntity(x,y,z,tile);
            } else if(blocks[state].hasTileEntity(meta[state])) {
                TileEntity fresh=blocks[state].createTileEntity(worldObj,meta[state]);
                if(fresh!=null)worldObj.setTileEntity(x,y,z,fresh);
            }
        }
        // Legacy onBlockAdded may auto-face this block and adjacent chests.
        // Restore every stored state after placement, before any neighbor notifications.
        for(int i=0;i<entries.tagCount();i++) {
            if(!selected[i])continue;
            NBTTagCompound e=entries.getCompoundTagAt(i);NBTTagList p=e.getTagList("pos",3);int state=e.getInteger("state");
            int[] transformed=StructureTransform120.position(intAt(p,0),intAt(p,2),rotation,mirror);
            int x=xCoord+offsetX+transformed[0],y=yCoord+offsetY+intAt(p,1),z=zCoord+offsetZ+transformed[1];
            if(worldObj.getBlock(x,y,z)!=blocks[state])throw new IOException("Block changed during structure placement");
            if(worldObj.getBlockMetadata(x,y,z)!=meta[state])worldObj.setBlockMetadataWithNotify(x,y,z,meta[state],2);
            if(worldObj.getBlockMetadata(x,y,z)!=meta[state])throw new IOException("Block metadata placement rejected");
        }
        for(int i=0;i<entries.tagCount();i++) {
            NBTTagCompound e=entries.getCompoundTagAt(i);NBTTagList p=e.getTagList("pos",3);
            if(!selected[i])continue;
            int[] transformed=StructureTransform120.position(intAt(p,0),intAt(p,2),rotation,mirror);
            int x=xCoord+offsetX+transformed[0],y=yCoord+offsetY+intAt(p,1),z=zCoord+offsetZ+transformed[1];
            worldObj.notifyBlocksOfNeighborChange(x,y,z,worldObj.getBlock(x,y,z));
        }
        sizeX=sx;sizeY=sy;sizeZ=sz;changed();
    }
    public boolean detectCorners() {
        int minX=Integer.MAX_VALUE,minY=minX,minZ=minX,maxX=Integer.MIN_VALUE,maxY=maxX,maxZ=maxX,count=0;
        for(Object value:worldObj.loadedTileEntityList)if(value instanceof TileEntityStructure120) {
            TileEntityStructure120 other=(TileEntityStructure120)value;
            if(other==this||!canonicalName(name).equals(canonicalName(other.name))||other.getBlockMetadata()!=2
                    ||Math.abs(other.xCoord-xCoord)>80||Math.abs(other.zCoord-zCoord)>80)continue;
            minX=Math.min(minX,other.xCoord);minY=Math.min(minY,other.yCoord);minZ=Math.min(minZ,other.zCoord);
            maxX=Math.max(maxX,other.xCoord);maxY=Math.max(maxY,other.yCoord);maxZ=Math.max(maxZ,other.zCoord);count++;
        }
        if(count==0)return false;
        if(count==1) { minX=Math.min(minX,xCoord);minY=Math.min(minY,yCoord);minZ=Math.min(minZ,zCoord);maxX=Math.max(maxX,xCoord);maxY=Math.max(maxY,yCoord);maxZ=Math.max(maxZ,zCoord); }
        int sx=maxX-minX-1,sy=maxY-minY-1,sz=maxZ-minZ-1;
        if(sx<1||sy<1||sz<1||sx>48||sy>48||sz>48)return false;
        if(Math.abs(minX+1-xCoord)>48||Math.abs(minY+1-yCoord)>48||Math.abs(minZ+1-zCoord)>48)return false;
        offsetX=minX+1-xCoord;offsetY=minY+1-yCoord;offsetZ=minZ+1-zCoord;sizeX=sx;sizeY=sy;sizeZ=sz;changed();return true;
    }
    public void redstone(boolean active) {
        if(worldObj.isRemote||active==powered)return;
        powered=active;markDirty();if(!active)return;
        try { if(getBlockMetadata()==0)save();else if(getBlockMetadata()==1)load(); }
        catch(IOException error) { org.apache.logging.log4j.LogManager.getLogger("HTGT6-Structure").warn("Structure {}: {}",name,error.getMessage()); }
    }
    private static final java.lang.reflect.Field LIST = cpw.mods.fml.relauncher.ReflectionHelper.findField(NBTTagList.class,"tagList","field_74747_a");
    private static int intAt(NBTTagList list,int index) {
        try { return ((NBTBase.NBTPrimitive)((List<?>)LIST.get(list)).get(index)).func_150287_d(); }
        catch(IllegalAccessException error) { throw new IllegalStateException(error); }
    }
    private static NBTTagList ints(int... values) { NBTTagList list=new NBTTagList();for(int value:values)list.appendTag(new NBTTagInt(value));return list; }
}
