package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeDecorator120;

import java.util.HashSet;
import java.util.Set;

/** Headless contract for the native 1.7 implementation of the 1.20 feature pipeline. */
public final class FuturaBiomeDecoratorFixture120 {
    public static void main(String[] ignored) throws Exception {
        verifyPreparedPopulationWindow();
        verifyStepOrder();verifySeedIsolation();FuturaSelectorFixture120.validate();
        System.out.println("Futura 1.20 biome decorator pipeline fixture OK");
    }

    private static void verifyStepOrder(){
        String expected="RAW_GENERATION,LAKES,LOCAL_MODIFICATIONS,UNDERGROUND_STRUCTURES,"
                +"SURFACE_STRUCTURES,STRONGHOLDS,UNDERGROUND_ORES,UNDERGROUND_DECORATION,"
                +"FLUID_SPRINGS,VEGETAL_DECORATION,TOP_LAYER_MODIFICATION";
        StringBuilder actual=new StringBuilder();
        for(FuturaBiomeDecorator120.DecorationStep step:FuturaBiomeDecorator120.DecorationStep.values()){
            if(actual.length()>0)actual.append(',');actual.append(step.name());
        }
        require(expected.equals(actual.toString()),"1.20 decoration step order changed: "+actual);
    }

    private static void verifySeedIsolation(){
        Set<Long> values=new HashSet<Long>();
        for(FuturaBiomeDecorator120.DecorationStep step:FuturaBiomeDecorator120.DecorationStep.values())
            for(int feature=0;feature<4;feature++){
                long seed=FuturaBiomeDecorator120.featureSeed(0x1202DEC0L,7,-11,step,feature);
                require(values.add(seed),"feature seed collision at "+step+"/"+feature);
            }
        require(FuturaBiomeDecorator120.featureSeed(1L,0,0,FuturaBiomeDecorator120.DecorationStep.UNDERGROUND_ORES,0)
                        !=FuturaBiomeDecorator120.featureSeed(1L,1,0,FuturaBiomeDecorator120.DecorationStep.UNDERGROUND_ORES,0),
                "neighbour chunks share a decorator seed");
    }

    private static void verifyPreparedPopulationWindow() throws Exception {
        Class<?> unsafe=Class.forName("sun.misc.Unsafe");
        java.lang.reflect.Field singleton=unsafe.getDeclaredField("theUnsafe");singleton.setAccessible(true);
        Object allocator=singleton.get(null);
        java.lang.reflect.Method allocate=unsafe.getMethod("allocateInstance",Class.class);
        java.lang.reflect.Field loader=cpw.mods.fml.common.Loader.class.getDeclaredField("instance");loader.setAccessible(true);
        loader.set(null,allocate.invoke(allocator,cpw.mods.fml.common.Loader.class));
        net.minecraft.init.Bootstrap.func_151354_b();
        PopulationWindow w=(PopulationWindow)allocate.invoke(allocator,PopulationWindow.class);
        net.minecraft.world.gen.feature.WorldGenDungeons dungeon=new net.minecraft.world.gen.feature.WorldGenDungeons();
        int attempts=0;
        for(int chunk:new int[]{-19,0,23}) {
            w.origin=chunk<<4;
            for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++) {
                int x=(chunk<<4)+dx,z=(chunk<<4)+dz;
                dungeon.generate(w,new java.util.Random(attempts++),x,120,z);
            }
        }
        System.out.println("Prepared dungeon window OK: modern origins, "+attempts+" actual generator probes bounded");
    }
    private static final class OutsideWindow extends RuntimeException {}
    public static final class PopulationWindow extends net.minecraft.world.World {
        int origin;
        private PopulationWindow(){super(null,"fixture",(net.minecraft.world.WorldProvider)null,(net.minecraft.world.WorldSettings)null,null);}
        public net.minecraft.block.Block getBlock(int x,int y,int z){
            if(x<origin-32||x>=origin+48||z<origin-32||z>=origin+48)throw new OutsideWindow();
            return net.minecraft.init.Blocks.stone;
        }
        protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
        protected int func_152379_p(){return 0;}
        public net.minecraft.entity.Entity getEntityByID(int id){return null;}
    }
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private FuturaBiomeDecoratorFixture120(){ }
}
