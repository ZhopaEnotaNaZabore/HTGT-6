package com.mod.htgt6.minecraft1202.Module.Data.Frostburn;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.TileEntityStructure120;
import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;
import com.mod.htgt6.minecraft1202.Module.Network.Frostburn.StructureNetwork120;

public final class StructureBlockFixture120 {
    public static void main(String[] args) throws Exception {
        com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.initialize();
        TileEntity.addMapping(TileEntityStructure120.class,"fixture_structure");
        java.nio.file.Path fixtures=java.nio.file.Files.createDirectories(new File("build/structure-fixtures").toPath());
        File root=java.nio.file.Files.createTempDirectory(fixtures,"structure-fixture-").toFile();
        Grid world=new Grid(root);
        TileEntityStructure120 editor=new TileEntityStructure120();world.setTileEntity(0,20,0,editor);
        editor.name="fixture:round_trip";editor.offsetX=1;editor.offsetY=0;editor.sizeX=3;editor.sizeY=1;editor.sizeZ=1;
        world.setBlock(1,20,0,Blocks.stone,3,2);
        world.setBlock(2,20,0,FrostburnContent.structureVoid,0,2);
        world.setBlock(3,20,0,Blocks.chest,0,2);
        net.minecraft.tileentity.TileEntityChest chest=new net.minecraft.tileentity.TileEntityChest();world.setTileEntity(3,20,0,chest);
        chest.setInventorySlotContents(0,new net.minecraft.item.ItemStack(net.minecraft.init.Items.diamond,7));
        editor.save();require(new File(root,"generated/fixture/structures/round_trip.nbt").isFile(),"structure file created");
        editor.offsetX=5;world.setBlock(6,20,0,Blocks.gold_block,0,2);editor.load();
        require(world.getBlock(5,20,0)==Blocks.stone&&world.getBlockMetadata(5,20,0)==3,"named block and metadata round trip");
        require(world.getBlock(6,20,0)==Blocks.gold_block,"structure void leaves destination unchanged");
        net.minecraft.tileentity.TileEntityChest restored=(net.minecraft.tileentity.TileEntityChest)world.getTileEntity(7,20,0);
        require(restored!=null&&restored.getStackInSlot(0).stackSize==7&&restored.xCoord==7,"tile NBT and relocated position");
        editor.offsetX=9;editor.sizeX=0;
        require(!editor.load(true)&&editor.sizeX==3&&world.getBlock(9,20,0)==Blocks.air,"GUI load prepares size without placing");
        require(editor.load(true)&&world.getBlock(9,20,0)==Blocks.stone,"second GUI load places structure");
        editor.offsetX=5;
        NBTTagCompound blankTile=editor.capture();
        for(int i=0;i<blankTile.getTagList("blocks",10).tagCount();i++)blankTile.getTagList("blocks",10).getCompoundTagAt(i).removeTag("nbt");
        editor.place(blankTile);
        require(world.getTileEntity(7,20,0) instanceof net.minecraft.tileentity.TileEntityChest
                &&((net.minecraft.tileentity.TileEntityChest)world.getTileEntity(7,20,0)).getStackInSlot(0)==null,"same-state placement creates fresh tile when template omits NBT");
        NBTTagCompound invalid=editor.capture();invalid.getTagList("palette",10).getCompoundTagAt(0).setString("Name","missing:block");
        Map<String,Object[]> before=new HashMap<String,Object[]>(world.cells);
        try {editor.place(invalid);throw new AssertionError("missing block accepted");}catch(IOException expected) { }
        require(world.cells.equals(before),"invalid template rejected before writes");
        require(!TileEntityStructure120.validName("../escape")&&!TileEntityStructure120.validName("x:/escape")&&TileEntityStructure120.validName("test:path/name"),"path validation");
        require(!editor.shouldRefresh(FrostburnContent.structureBlock,FrostburnContent.structureBlock,0,1,world,0,20,0),"mode change preserves tile");
        TileEntityStructure120 corner=new TileEntityStructure120();corner.name=editor.name;world.setTileEntity(4,24,4,corner);world.setBlock(4,24,4,FrostburnContent.structureBlock,2,2);
        require(editor.detectCorners()&&editor.sizeX==3&&editor.sizeY==3&&editor.sizeZ==3,"corner detection");
        NBTTagCompound saved=new NBTTagCompound();editor.writeToNBT(saved);TileEntityStructure120 copy=new TileEntityStructure120();copy.readFromNBT(saved);
        require(copy.name.equals(editor.name)&&copy.offsetX==1&&copy.sizeY==3,"editor NBT persistence");
        StructureNetwork120.Edit edit=new StructureNetwork120.Edit();edit.name="fixture:round_trip";edit.sx=48;edit.sy=1;edit.sz=1;
        io.netty.buffer.ByteBuf buffer=io.netty.buffer.Unpooled.buffer();edit.toBytes(buffer);StructureNetwork120.Edit decoded=new StructureNetwork120.Edit();decoded.fromBytes(buffer);buffer.release();
        require(decoded.valid()&&decoded.sx==48&&decoded.name.equals(edit.name),"packet round trip");decoded.sx=49;require(!decoded.valid(),"packet size boundary");
        FixturePlayer player=new FixturePlayer(world);player.setPosition(.5,20.5,.5);player.capabilities.isCreativeMode=true;
        com.mod.htgt6.minecraft1202.Module.Gameplay.Frostburn.ContainerStructure120 container=new com.mod.htgt6.minecraft1202.Module.Gameplay.Frostburn.ContainerStructure120(editor);
        require(container.canInteractWith(player),"nearby creative operator allowed");
        player.operator=false;require(!container.canInteractWith(player),"non-operator rejected");player.operator=true;
        player.capabilities.isCreativeMode=false;require(!container.canInteractWith(player),"survival rejected");player.capabilities.isCreativeMode=true;
        player.setPosition(100,20,0);require(!container.canInteractWith(player),"distant operator rejected");
        editor.name="fixture:pulse";editor.redstone(true);
        File pulse=new File(root,"generated/fixture/structures/pulse.nbt");byte[] first=java.nio.file.Files.readAllBytes(pulse.toPath());
        world.setBlock(1,21,1,Blocks.diamond_block,0,2);editor.redstone(true);
        require(Arrays.equals(first,java.nio.file.Files.readAllBytes(pulse.toPath())),"steady power does not repeat save");
        editor.redstone(false);editor.redstone(true);
        require(!Arrays.equals(first,java.nio.file.Files.readAllBytes(pulse.toPath())),"next rising edge saves again");
        world.loaded=false;try {editor.capture();throw new AssertionError("unloaded region accepted");}catch(IOException expected) { }
        integrity(root);
        transforms(root);
        System.out.println("Structure editor OK: file save/load, metadata, chest NBT, void, preflight, paths, modes, corners, persistence, packet bounds, unloaded chunks");
    }
    private static void require(boolean ok,String message) {if(!ok)throw new AssertionError(message);}
    private static void integrity(File root) throws Exception {
        Grid world=new Grid(root);TileEntityStructure120 editor=new TileEntityStructure120();world.setTileEntity(0,20,0,editor);
        editor.sizeX=8;editor.sizeY=4;editor.sizeZ=4;editor.offsetX=16;editor.offsetY=0;
        for(int x=16;x<24;x++)for(int y=20;y<24;y++)for(int z=0;z<4;z++)world.setBlock(x,y,z,Blocks.stone,0,2);
        NBTTagCompound template=editor.capture();world.cells.clear();editor.offsetX=32;editor.integrity=.5F;editor.seed=1234;
        editor.place(template);Set<String> first=new HashSet<String>(world.cells.keySet());
        require(!first.isEmpty()&&first.size()<128,"partial integrity actually selects a subset");
        world.cells.clear();editor.place(template);require(first.equals(world.cells.keySet()),"seed repeats identical selection");
        world.cells.clear();editor.seed=4321;editor.place(template);require(!first.equals(world.cells.keySet()),"different seed changes selection");
        world.cells.clear();editor.integrity=1F;editor.place(template);require(world.cells.size()==128,"full integrity includes every block");
        NBTTagCompound state=new NBTTagCompound();editor.integrity=.25F;editor.seed=Long.MIN_VALUE;editor.metadata="loot_marker";editor.writeToNBT(state);
        TileEntityStructure120 copy=new TileEntityStructure120();copy.readFromNBT(state);require(copy.integrity==.25F&&copy.seed==Long.MIN_VALUE,"options survive NBT");
        require(copy.metadata.equals("loot_marker"),"DATA marker survives NBT");
        state.setFloat("integrity",Float.NaN);copy.readFromNBT(state);require(copy.integrity==1F,"malformed saved integrity normalized");
        StructureNetwork120.Edit edit=new StructureNetwork120.Edit();edit.integrity=Float.NaN;require(!edit.valid(),"NaN packet rejected");
        edit.integrity=Float.POSITIVE_INFINITY;require(!edit.valid(),"infinite packet rejected");
        edit.integrity=.25F;edit.seed=Long.MIN_VALUE;edit.metadata="loot_marker";
        io.netty.buffer.ByteBuf buffer=io.netty.buffer.Unpooled.buffer();edit.toBytes(buffer);
        StructureNetwork120.Edit decoded=new StructureNetwork120.Edit();decoded.fromBytes(buffer);buffer.release();
        require(decoded.integrity==.25F&&decoded.seed==Long.MIN_VALUE&&decoded.metadata.equals("loot_marker"),"options and DATA survive network packet");
    }
    private static void transforms(File root) throws Exception {
        Grid world=new Grid(root);TileEntityStructure120 editor=new TileEntityStructure120();world.setTileEntity(0,20,0,editor);
        editor.offsetY=0;editor.offsetX=16;editor.sizeX=3;editor.sizeY=1;editor.sizeZ=2;
        world.setBlock(16,20,0,Blocks.oak_stairs,4,2);
        world.setBlock(17,20,0,Blocks.furnace,4,2);
        world.setBlock(18,20,1,Blocks.chest,2,2);
        net.minecraft.tileentity.TileEntityChest chest=new net.minecraft.tileentity.TileEntityChest();world.setTileEntity(18,20,1,chest);
        chest.setInventorySlotContents(0,new net.minecraft.item.ItemStack(net.minecraft.init.Items.diamond,7));
        NBTTagCompound template=editor.capture();editor.offsetX=0;editor.rotation=1;world.placementCallbacks=true;
        editor.place(template);
        require(world.getBlockMetadata(0,20,1)==2,"furnace orientation restored after real legacy onBlockAdded");
        require(world.getBlockMetadata(0,20,0)==6,"east upside-down stair rotates south and preserves half");
        require(world.getBlock(-1,20,2)==Blocks.chest&&world.getBlockMetadata(-1,20,2)==5,"north chest rotates east at negative X");
        require(((net.minecraft.tileentity.TileEntityChest)world.getTileEntity(-1,20,2)).getStackInSlot(0).stackSize==7,"rotated chest retains inventory");
        world.setBlock(0,20,0,FrostburnContent.structureBlock,1,2);editor.updateContainingBlockInfo();
        editor.showBoundingBox=false;require(editor.getRenderBoundingBox().minX==0&&editor.getRenderBoundingBox().maxZ==1,"hidden LOAD box reduces render bounds");editor.showBoundingBox=true;
        require(Arrays.equals(editor.previewBounds(),new int[]{-1,0,1,3}),"LOAD preview uses transformed cells");
        require(editor.getRenderBoundingBox().minX==-1&&editor.getRenderBoundingBox().maxZ==3,"render culling covers transformed region");
        world.setBlock(0,20,0,FrostburnContent.structureBlock,0,2);editor.updateContainingBlockInfo();
        require(Arrays.equals(editor.previewBounds(),new int[]{0,0,3,2}),"SAVE preview remains untransformed");
        editor.rotation=0;editor.mirror=2;editor.offsetZ=10;editor.place(template);
        require(world.getBlockMetadata(0,20,10)==5&&world.getBlock(-2,20,11)==Blocks.chest,"mirror reflects stair and asymmetric position");
        NBTTagCompound saved=new NBTTagCompound();editor.rotation=3;editor.showBoundingBox=false;editor.writeToNBT(saved);
        TileEntityStructure120 copy=new TileEntityStructure120();copy.readFromNBT(saved);
        require(!copy.showBoundingBox&&copy.rotation==3&&copy.mirror==2,"transforms persist in NBT");
        StructureNetwork120.Edit edit=new StructureNetwork120.Edit();edit.rotation=3;edit.mirror=2;edit.showBoundingBox=false;
        io.netty.buffer.ByteBuf buffer=io.netty.buffer.Unpooled.buffer();edit.toBytes(buffer);
        StructureNetwork120.Edit decoded=new StructureNetwork120.Edit();decoded.fromBytes(buffer);buffer.release();
        require(decoded.valid()&&!decoded.showBoundingBox&&decoded.rotation==3&&decoded.mirror==2,"transforms survive packet");
        decoded.rotation=4;require(!decoded.valid(),"invalid rotation rejected");decoded.rotation=0;decoded.mirror=3;require(!decoded.valid(),"invalid mirror rejected");
        require(com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.StructureTransform120.metadata(Blocks.wooden_door,9,0,1)==8,"mirror swaps upper door hinge");
        require(com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.StructureTransform120.metadata(Blocks.wooden_door,4,1,0)==5,"rotation preserves open lower door bit");
        require(com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.StructureTransform120.metadata(FrostburnContent.boneBlock,4,1,0)==8,"bone axis rotates X to Z");
        require(com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.StructureTransform120.metadata(Blocks.trapdoor,12,1,0)==15,"trapdoor rotation preserves open/top bits");
        require(com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.StructureTransform120.metadata(Blocks.standing_sign,0,1,0)==4,"south sign rotates west");
        for(int r=0;r<4;r++)for(int mirror=0;mirror<3;mirror++) {
            int[] bounds=com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.StructureTransform120.bounds(3,2,r,mirror);
            Set<String> cells=new HashSet<String>();
            for(int x=0;x<3;x++)for(int z=0;z<2;z++) {
                int[] point=com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.StructureTransform120.position(x,z,r,mirror);
                require(point[0]>=bounds[0]&&point[0]<bounds[2]&&point[1]>=bounds[1]&&point[1]<bounds[3],"all transformed cells inside preview");
                cells.add(point[0]+","+point[1]);
            }
            require(cells.size()==6&&(bounds[2]-bounds[0])*(bounds[3]-bounds[1])==6,"all 12 transforms are bijections");
        }
        Block[] directional={Blocks.oak_stairs,Blocks.wooden_door,Blocks.trapdoor,Blocks.log,Blocks.hay_block,Blocks.standing_sign,Blocks.bed,Blocks.fence_gate};
        for(Block block:directional)for(int m=0;m<(block==Blocks.oak_stairs?8:16);m++) {
            int value=m;
            for(int r=0;r<4;r++)value=com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.StructureTransform120.metadata(block,value,1,0);
            require(value==m,"four rotations restore metadata for "+block+"/"+m);
            for(int mirror=1;mirror<=2;mirror++) {
                value=com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.StructureTransform120.metadata(block,m,0,mirror);
                value=com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.StructureTransform120.metadata(block,value,0,mirror);
                require(value==m,"double reflection restores metadata");
            }
        }
        NBTTagCompound unsupported=(NBTTagCompound)template.copy();unsupported.getTagList("palette",10).getCompoundTagAt(0).setString("Name","minecraft:rail");
        Map<String,Object[]> before=new HashMap<String,Object[]>(world.cells);
        try{editor.place(unsupported);throw new AssertionError("unsupported orientation accepted");}catch(IOException expected){}
        require(world.cells.equals(before),"unsupported block fails before world writes");
        unsupported=(NBTTagCompound)template.copy();
        for(int i=0;i<unsupported.getTagList("blocks",10).tagCount();i++) {
            NBTTagCompound e=unsupported.getTagList("blocks",10).getCompoundTagAt(i);
            if(e.hasKey("nbt",10))e.getCompoundTag("nbt").setString("id","MobSpawner");
        }
        try{editor.place(unsupported);throw new AssertionError("unsupported NBT accepted");}catch(IOException expected){}
        require(world.cells.equals(before),"unsupported tile fails before world writes");
        editor.rotation=1;editor.mirror=0;editor.offsetX=0;editor.offsetZ=0;world.unloadedX=-1;
        try{editor.place(template);throw new AssertionError("negative unloaded chunk accepted");}catch(IOException expected){}
        require(world.cells.equals(before),"transformed chunk preflight uses negative bounds");
        System.out.println("Structure transforms OK: placement, metadata, inventory, preview, NBT, packets, unsupported states and negative chunk preflight");
    }
    private static final class FixturePlayer extends net.minecraft.entity.player.EntityPlayer {
        boolean operator=true;
        FixturePlayer(World world){super(world,new com.mojang.authlib.GameProfile(new UUID(55,66),"StructureFixture"));}
        @Override public void addChatMessage(net.minecraft.util.IChatComponent message) { }
        @Override public boolean canCommandSenderUseCommand(int level,String command){return operator;}
        @Override public net.minecraft.util.ChunkCoordinates getPlayerCoordinates(){return new net.minecraft.util.ChunkCoordinates(0,20,0);}
    }
    private static ISaveHandler handler(final File root) {
        return (ISaveHandler)Proxy.newProxyInstance(StructureBlockFixture120.class.getClassLoader(),new Class<?>[]{ISaveHandler.class},
            (proxy,method,args)->method.getName().equals("getMapFileFromName")?new File(root,"data/"+args[0]+".dat"):null);
    }
    private static final class Grid extends World {
        final Map<String,Object[]> cells=new HashMap<String,Object[]>();final Map<String,TileEntity> tiles=new HashMap<String,TileEntity>();boolean loaded=true,placementCallbacks;int unloadedX=Integer.MIN_VALUE;
        Grid(File root) {super(handler(root),"structure fixture",new WorldProviderSurface(),new WorldSettings(1,WorldSettings.GameType.CREATIVE,false,false,WorldType.DEFAULT),new net.minecraft.profiler.Profiler());provider.registerWorld(this);this.chunkProvider=createChunkProvider();}
        private String key(int x,int y,int z){return x+","+y+","+z;}
        @Override public Block getBlock(int x,int y,int z){Object[] entry=cells.get(key(x,y,z));return entry==null?Blocks.air:(Block)entry[0];}
        @Override public int getBlockMetadata(int x,int y,int z){Object[] entry=cells.get(key(x,y,z));return entry==null?0:(Integer)entry[1];}
        @Override public boolean setBlock(int x,int y,int z,Block b,int meta,int flags){cells.put(key(x,y,z),new Object[]{b,meta});
            if(placementCallbacks&&(b instanceof net.minecraft.block.BlockChest||b instanceof net.minecraft.block.BlockFurnace))b.onBlockAdded(this,x,y,z);
            return true;}
        @Override public boolean setBlockMetadataWithNotify(int x,int y,int z,int meta,int flags){Object[] entry=cells.get(key(x,y,z));if(entry==null)return false;entry[1]=meta;return true;}
        @Override public TileEntity getTileEntity(int x,int y,int z){return tiles.get(key(x,y,z));}
        @Override public void setTileEntity(int x,int y,int z,TileEntity tile){tile.xCoord=x;tile.yCoord=y;tile.zCoord=z;tile.setWorldObj(this);tiles.put(key(x,y,z),tile);loadedTileEntityList.add(tile);}
        @Override public void removeTileEntity(int x,int y,int z){loadedTileEntityList.remove(tiles.remove(key(x,y,z)));}
        @Override public void markBlockForUpdate(int x,int y,int z) { }
        @Override public void notifyBlocksOfNeighborChange(int x,int y,int z,Block block) { }
        @Override public void markTileEntityChunkModified(int x,int y,int z,TileEntity tile) { }
        @Override protected IChunkProvider createChunkProvider(){return (IChunkProvider)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{IChunkProvider.class},(proxy,method,args)->method.getName().equals("chunkExists")?loaded&&((Integer)args[0])!=unloadedX:null);}
        @Override protected int func_152379_p(){return 0;}
        @Override public net.minecraft.entity.Entity getEntityByID(int id){return null;}
    }
}
