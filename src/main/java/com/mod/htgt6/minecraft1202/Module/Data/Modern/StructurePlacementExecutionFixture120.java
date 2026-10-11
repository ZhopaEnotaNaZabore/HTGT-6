package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Data.Exploration.WoodlandMansionTemplateRuntime112;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.WoodlandMansionLayout112;
import com.mod.htgt6.minecraft1202.Module.World.Exploration.WoodlandMansionGenerator112;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaAncientCityGenerator120;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.*;

/** Executes production NBT placement against a recording World, not just template fingerprints. */
public final class StructurePlacementExecutionFixture120 {
    public static void assertContract() {
        try {
            require(!WoodlandMansionGenerator112.validSurfaceHeight(123,true)
                    &&WoodlandMansionGenerator112.validSurfaceHeight(124,true)
                    &&!WoodlandMansionGenerator112.validSurfaceHeight(59,false)
                    &&WoodlandMansionGenerator112.validSurfaceHeight(60,false),"mansion translated height gate");
            Constructor<WoodlandMansionLayout112.Piece> ctor=WoodlandMansionLayout112.Piece.class.getDeclaredConstructor(
                    String.class,int.class,int.class,int.class,WoodlandMansionLayout112.Rotation.class,WoodlandMansionLayout112.Mirror.class);
            ctor.setAccessible(true);
            int mansionWrites=0,cityWrites=0;
            for(WoodlandMansionLayout112.Rotation rotation:WoodlandMansionLayout112.Rotation.values())
                for(WoodlandMansionLayout112.Mirror mirror:WoodlandMansionLayout112.Mirror.values()) {
                    Grid w=world();
                    WoodlandMansionLayout112.Piece piece=ctor.newInstance("corridor_floor",0,0,0,rotation,mirror);
                    WoodlandMansionTemplateRuntime112.Template t=WoodlandMansionTemplateRuntime112.load(piece.name);
                    List<int[]> chunks=new ArrayList<int[]>();
                    for(int cx=-2;cx<=0;cx++)for(int cz=-2;cz<=0;cz++)chunks.add(new int[]{cx,cz});
                    Collections.shuffle(chunks,new Random(47));
                    for(int[] chunk:chunks) {
                        w.ownerX=chunk[0];w.ownerZ=chunk[1];
                        WoodlandMansionTemplateRuntime112.placeInChunk(w,t,piece,-16,150,-16,chunk[0],chunk[1],new Random(47));
                    }
                    require(w.writes==t.blocks.size(),"mansion NBT cells must each be written once");
                    mansionWrites+=w.writes;
                    for(int[] chunk:chunks) {
                        w.ownerX=chunk[0];w.ownerZ=chunk[1];
                        WoodlandMansionGenerator112.fillFoundations(w,Collections.singletonList(piece),-16,150,-16,chunk[0],chunk[1]);
                    }
                    require(w.getBlock(-16,149,-16)==Blocks.cobblestone,"mansion foundation missing");
                    require(w.getBlock(-16,140,-16)==Blocks.stone,"foundation overwrote solid ground");
                    require(w.getBlock(-16,141,-16)==Blocks.cobblestone,"foundation stopped in water");
                    int before=w.writes;
                    for(int[] chunk:chunks){w.ownerX=chunk[0];w.ownerZ=chunk[1];WoodlandMansionGenerator112.fillFoundations(w,Collections.singletonList(piece),-16,150,-16,chunk[0],chunk[1]);}
                    require(w.writes==before,"foundation replay must be idempotent");
                }
            for(int rotation=0;rotation<4;rotation++) {
                Grid w=world();
                String resource="/assets/htgt6/structures/ancient_city/city_center/walls/bottom_1.nbt";
                for(int cx=-4;cx<=3;cx++)for(int cz=-4;cz<=3;cz++) {
                    w.ownerX=cx;w.ownerZ=cz;
                    FuturaAncientCityGenerator120.placeTemplateAtOrigin(w,resource,-16,200,-16,rotation,cx,cz,Collections.<String>emptyList(),false);
                }
                require(w.writes>100,"Ancient City production NBT placer wrote no wall");
                cityWrites+=w.writes;
            }
            System.out.println("Structure execution 47 OK: mansion NBT writes="+mansionWrites+", city wall writes="+cityWrites+", rotations/mirrors, shuffled negative chunks, foundations and height gate");
        } catch(Exception e){throw new IllegalStateException("Structure execution fixture",e);}
    }
    private static Grid world() throws Exception {
        Class<?> u=Class.forName("sun.misc.Unsafe");Field f=u.getDeclaredField("theUnsafe");f.setAccessible(true);
        Grid w=(Grid)u.getMethod("allocateInstance",Class.class).invoke(f.get(null),Grid.class);
        w.cells=new HashMap<String,Block>();w.rand=new Random(47);w.isRemote=true;return w;
    }
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    public static final class Grid extends World {
        Map<String,Block> cells;int ownerX,ownerZ,writes;
        private Grid(){super(null,"fixture",(WorldProvider)null,(WorldSettings)null,null);}
        public int getActualHeight(){return 384;}public int getHeight(){return 384;}public long getSeed(){return 47;}
        public Block getBlock(int x,int y,int z){Block b=cells.get(x+","+y+","+z);return b!=null?b:y<=140?Blocks.stone:y==141?Blocks.water:Blocks.air;}
        public int getBlockMetadata(int x,int y,int z){return 0;}
        public boolean isAirBlock(int x,int y,int z){return getBlock(x,y,z)==Blocks.air;}
        public TileEntity getTileEntity(int x,int y,int z){return null;}
        public boolean setBlock(int x,int y,int z,Block b,int meta,int flags){
            require((x>>4)==ownerX&&(z>>4)==ownerZ,"cross-chunk structure write");
            require(y>=0&&y<384,"out-of-height structure write");
            cells.put(x+","+y+","+z,b);writes++;return true;
        }
        protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
        protected int func_152379_p(){return 0;}
        public net.minecraft.entity.Entity getEntityByID(int id){return null;}
    }
    private StructurePlacementExecutionFixture120(){}
}
