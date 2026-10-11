package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;

import java.lang.reflect.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.entity.Entity;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import com.mod.htgt6.minecraft1202.Module.World.Bountiful.OceanMonumentGenerator;

/** Runs the actual builder, recording writes across every intersecting chunk. */
public final class MonumentWater184Fixture {
    public static void main(String[] args) throws Exception {
        BountifulMechanicsFixture120.initialize();
        Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);
        sun.misc.Unsafe unsafe=(sun.misc.Unsafe)uf.get(null);
        Class<?> builder=Class.forName(OceanMonumentGenerator.class.getName()+"$Builder");
        Constructor<?> ctor=builder.getDeclaredConstructor(World.class,int.class,int.class,int.class,int.class,int.class);
        ctor.setAccessible(true);
        Method generate=builder.getDeclaredMethod("generate");generate.setAccessible(true);
        int bad=0, chunks=0;
        for(boolean futura:new boolean[]{false,true})for(int origin:new int[]{-29,7}) {
            int sea=futura?127:63;
            int surfaceWrites=0;
            for(int cx=origin>>4;cx<=(origin+57)>>4;cx++)for(int cz=origin>>4;cz<=(origin+57)>>4;cz++) {
                RecordingWorld w=(RecordingWorld)unsafe.allocateInstance(RecordingWorld.class);
                w.cx=cx;w.cz=cz;w.sea=sea;
                generate.invoke(ctor.newInstance(w,origin,origin,cx,cz,OceanMonumentGenerator.baseY(futura)));
                bad+=w.above;surfaceWrites+=w.surface;chunks++;
                if(w.outside!=0)throw new AssertionError("Writes escaped chunk");
                if(w.solid==0)throw new AssertionError("Structure lost its solid blocks");
            }
            if(surfaceWrites!=58*58)throw new AssertionError("Missing ocean surface coverage: "+surfaceWrites);
        }
        if(bad!=0)throw new AssertionError("Water writes at/above sea plane: "+bad);
        System.out.println("PASS monument water: "+chunks+" chunks, legacy/Futura, positive/negative origins; no elevated water; complete surface preserved");
    }
    public static final class RecordingWorld extends World {
        int cx,cz,sea,above,outside,surface,solid;
        private RecordingWorld(){super(null,"fixture",(WorldProvider)null,(WorldSettings)null,null);}
        @Override protected IChunkProvider createChunkProvider(){return null;}
        @Override protected int func_152379_p(){return 0;}
        @Override public long getSeed(){return 42;}
        @Override public Block getBlock(int x,int y,int z){return Blocks.stone;}
        @Override public boolean setBlock(int x,int y,int z,Block b,int meta,int flags){
            if((x>>4)!=cx||(z>>4)!=cz)outside++;
            if(b==Blocks.water||b==Blocks.flowing_water){if(y>=sea)above++;if(y==sea-1)surface++;}
            else if(b!=Blocks.air)solid++;
            return true;
        }
        @Override public Entity getEntityByID(int id){return null;}
    }
}
