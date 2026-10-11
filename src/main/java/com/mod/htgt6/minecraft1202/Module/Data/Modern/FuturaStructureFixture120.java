package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.*;
import java.util.*;
import java.lang.reflect.Constructor;
import net.minecraft.nbt.*;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.gen.structure.*;

final class FuturaStructureFixture120 {
    static void validate() throws Exception {
        new FuturaStructureGenerators120.Mineshaft(); // register the new codecs
        int[] rings={3,6,10,15,21,28,36,9};
        for(long seed:new long[]{0,1,-1,0x1202L}){
            List<ChunkCoordIntPair> positions=FuturaStructurePlacement120.strongholds(seed,null);
            require(positions.size()==128,"stronghold count");int index=0;
            for(int ring=0;ring<rings.length;ring++)for(int i=0;i<rings[ring];i++){
                ChunkCoordIntPair p=positions.get(index++);double radius=Math.hypot(p.chunkXPos,p.chunkZPos);
                require(radius>=128+192*ring-41&&radius<=128+192*ring+41,"stronghold ring radius");
            }
            List<ChunkCoordIntPair> forced=FuturaStructurePlacement120.strongholds(seed,(x,z,r)->new ChunkCoordIntPair(7,-11));
            for(ChunkCoordIntPair p:forced)require(p.chunkXPos==7&&p.chunkZPos==-11,"preferred biome result ignored");
        }
        boolean nearOrigin=false;
        for(long seed=0;seed<10000&&!nearOrigin;seed++)nearOrigin=FuturaStructurePlacement120.mineshaft(seed,0,0);
        require(nearOrigin,"obsolete mineshaft suppression at origin");
        for(String set:new String[]{"desert_pyramids","jungle_temples","swamp_huts"}){
            int candidates=0;
            for(int z=-32;z<0;z++)for(int x=-32;x<0;x++)if(FuturaStructurePlacement120.scattered(73,x,z,set))candidates++;
            require(candidates==1,"negative random-spread region: "+set);
        }
        for(Class<?> type:new Class<?>[]{FuturaStructureGenerators120.MineshaftStart.class,FuturaStructureGenerators120.StrongholdStart.class}){
            Constructor<?> constructor=type.getDeclaredConstructor(Random.class,int.class,int.class);constructor.setAccessible(true);
            boolean aboveLegacySea=false;
            for(int i=0;i<32;i++){
                StructureStart start=(StructureStart)constructor.newInstance(new Random(i),-5,9);
                require(start.getBoundingBox().minY>=0&&start.getBoundingBox().maxY<127,"translated underground range");
                aboveLegacySea|=start.getBoundingBox().maxY>63;
                NBTTagCompound tag=start.func_143021_a(-5,9);
                require(tag.getString("id").startsWith("Futura"),"unregistered structure start");
                StructureStart restored=MapGenStructureIO.func_143035_a(tag,null);
                require(restored!=null&&restored.func_143019_e()==-5&&restored.func_143018_f()==9,"structure chunk persistence");
                require(restored.getComponents().size()==start.getComponents().size(),"structure pieces lost after reload");
                for(Object value:restored.getComponents())if(value instanceof StructureMineshaftPieces.Room){
                    StructureComponent room=(StructureComponent)value;
                    Random beforeRandom=new Random(i);
                    StructureMineshaftPieces.Room before=new StructureMineshaftPieces.Room(0,beforeRandom,(-5<<4)+2,(9<<4)+2);
                    List<StructureComponent> beforePieces=new ArrayList<StructureComponent>();beforePieces.add(before);
                    before.buildComponent(before,beforePieces,beforeRandom);
                    NBTTagList entrances=room.func_143010_b().getTagList("Entrances",11),original=before.func_143010_b().getTagList("Entrances",11);
                    require(entrances.tagCount()==original.tagCount(),"mineshaft entrance count changed");
                    int dy=room.getBoundingBox().minY-before.getBoundingBox().minY;
                    for(int j=0;j<entrances.tagCount();j++){
                        int[] box=entrances.func_150306_c(j),oldBox=original.func_150306_c(j);
                        for(int axis=0;axis<6;axis++)require(box[axis]==oldBox[axis]+(axis==1||axis==4?dy:0),"mineshaft entrance not translated");
                    }
                }
            }
            require(aboveLegacySea,"structure still uses sea level 63");
        }
        NBTTagCompound old=new NBTTagCompound();old.setString("id","Mineshaft");old.setIntArray("BB",new int[]{0,10,0,15,20,15});
        require(MapGenStructureIO.func_143035_a(old,null) instanceof StructureMineshaftStart,"legacy mineshaft codec lost");
        System.out.println("Futura structures OK: 128 rings, negative spread regions, origin mineshafts, 64 translated starts and NBT reload");
    }
    private static void require(boolean condition,String why){if(!condition)throw new AssertionError(why);}
}
