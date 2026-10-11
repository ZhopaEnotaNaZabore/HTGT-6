package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Leaves;
import com.mod.htgt6.minecraft1202.Module.World.Modern.ChunkProviderFutura120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeDecorator120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaCarverRuntime120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.AquiferSampler120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaDripstoneCavesRuntime120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaUndergroundFeatures120;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/** Headless wiring and official-constant contract for the complete cave backport pass. */
public final class FuturaCaveBackportFixtures120 {
    private static final Block FIXTURE_ROCK=new FixtureRock();
    public static void main(String[] args)throws Exception{
        require(FuturaCarverRuntime120.RANGE==8,"carver neighbour range");
        require(FuturaCarverRuntime120.CAVE_PROBABILITY==.15D,"cave probability");
        require(FuturaCarverRuntime120.EXTRA_CAVE_PROBABILITY==.07D,"extra cave probability");
        require(FuturaCarverRuntime120.CANYON_PROBABILITY==.01D,"canyon probability");
        require(FuturaDripstoneCavesRuntime120.LARGE_MIN==10&&FuturaDripstoneCavesRuntime120.LARGE_MAX==48,
                "large_dripstone count");
        require(FuturaDripstoneCavesRuntime120.CLUSTER_MIN==48&&FuturaDripstoneCavesRuntime120.CLUSTER_MAX==96,
                "dripstone_cluster count");
        require(FuturaDripstoneCavesRuntime120.POINTED_MIN==192&&FuturaDripstoneCavesRuntime120.POINTED_MAX==256,
                "pointed_dripstone count");
        require(FuturaMountainDensityRouter120.modernToFuturaY(FuturaDripstoneCavesRuntime120.MAX_ABSOLUTE_Y)==320,
                "dripstone max Y translation");
        require(dripstoneDoesNotProvideChunks(),"dripstone population may recursively provide neighbour chunks");
        require(FuturaUndergroundFeatures120.GEODE_RARITY==24,"amethyst geode rarity");
        carverDeterminism();
        field(ChunkProviderFutura120.class,"carvers",FuturaCarverRuntime120.class);
        field(ChunkProviderFutura120.class,"biomeDecorator",FuturaBiomeDecorator120.class);
        field(FuturaBiomeDecorator120.class,"dripstone",FuturaDripstoneCavesRuntime120.class);
        field(FuturaBiomeDecorator120.class,"underground",FuturaUndergroundFeatures120.class);
        require(call(ChunkProviderFutura120.class,"provideChunk","FuturaCarverRuntime120","carve"),"raw chunk does not invoke carvers");
        require(call(FuturaBiomeDecorator120.class,"decorate","FuturaBiomeDecorator120","dispatch"),"decorator does not invoke feature dispatch");
        require(call(FuturaBiomeDecorator120.class,"dispatch","FuturaDripstoneCavesRuntime120","populateLarge")
                &&call(FuturaBiomeDecorator120.class,"dispatch","FuturaDripstoneCavesRuntime120","populateFeature"),"dispatcher does not invoke both dripstone paths");
        require(call(FuturaBiomeDecorator120.class,"dispatch","FuturaUndergroundFeatures120","populateGeodes"),"dispatcher does not invoke geodes");
        require(call(FuturaBiomeDecorator120.class,"dispatch","FuturaUndergroundFeatures120","populateOre"),"dispatcher does not invoke named ores");
        Method blockColor=BlockBackport120Leaves.class.getMethod("getBlockColor");
        Method worldColor=BlockBackport120Leaves.class.getMethod("colorMultiplier",
                net.minecraft.world.IBlockAccess.class,Integer.TYPE,Integer.TYPE,Integer.TYPE);
        require(blockColor.getDeclaringClass()==BlockBackport120Leaves.class
                &&worldColor.getDeclaringClass()==BlockBackport120Leaves.class,"cherry tint override missing");
        require(resourceContains("/assets/htgt6/lang/ru_RU.lang",
                "tile.backport_1_20.aquifer_water.name=Грунтовые воды"),"Russian groundwater localization");
        System.out.println("Futura cave backport fixtures OK; carvers=3, dripstone=3, geodes+ores wired, cave spawn+tint localized");
    }
    private static void carverDeterminism(){
        final int height=384;final long seed=0x120CA7E5L;
        FuturaMountainDensityRouter120 terrain=new FuturaMountainDensityRouter120(seed);
        AquiferSampler120 aquifer=new AquiferSampler120(seed,FuturaMountainDensityRouter120.SEA_LEVEL,
                FuturaMountainDensityRouter120.modernToFuturaY(-54),terrain,height-1);
        FuturaCarverRuntime120 carver=new FuturaCarverRuntime120(seed,aquifer);
        Block[] first=null;int selectedX=0,carved=0;
        for(int chunkX=-12;chunkX<=12&&carved==0;chunkX++){
            first=stoneChunk(height);byte[] metadata=new byte[first.length];
            carver.carve(first,metadata,height,chunkX,0);carved=countCarved(first);
            if(carved>0)selectedX=chunkX;
        }
        require(first!=null&&carved>0,"configured carvers produced no cells in 25 chunks");
        Block[] second=stoneChunk(height);byte[] firstMeta=new byte[first.length],secondMeta=new byte[first.length];
        // Recreate the selected result with retained metadata, then repeat it exactly.
        first=stoneChunk(height);carver.carve(first,firstMeta,height,selectedX,0);
        carver.carve(second,secondMeta,height,selectedX,0);
        for(int i=0;i<first.length;i++){
            require(first[i]==second[i]&&firstMeta[i]==secondMeta[i],"carver is not deterministic at "+i);
        }
        for(int column=0;column<256;column++)for(int y=0;y<5;y++)
            require(first[column*height+y]==null,"carver replaced protected bottom cells");
    }
    private static Block[] stoneChunk(int height){
        Block[] blocks=new Block[256*height];Arrays.fill(blocks,FIXTURE_ROCK);
        for(int column=0;column<256;column++)for(int y=0;y<5;y++)blocks[column*height+y]=null;
        return blocks;
    }
    private static int countCarved(Block[] blocks){
        int carved=0;
        for(int column=0;column<256;column++)for(int y=5;y<384;y++)
            if(blocks[column*384+y]!=FIXTURE_ROCK)carved++;
        return carved;
    }
    private static final class FixtureRock extends Block {private FixtureRock(){super(Material.rock);}}
    private static void field(Class<?> owner,String name,Class<?> type)throws Exception{
        Field field=owner.getDeclaredField(name);
        require(field.getType()==type&&Modifier.isFinal(field.getModifiers()),name+" production field");
    }
    private static boolean call(Class<?> inspected,final String method,final String ownerSuffix,final String called)throws Exception{
        InputStream in=inspected.getResourceAsStream(inspected.getSimpleName()+".class");
        if(in==null)return false;final boolean[] found={false};
        try{new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM5){
            @Override public MethodVisitor visitMethod(int access,String name,String desc,String signature,String[] exceptions){
                if(!method.equals(name))return null;
                return new MethodVisitor(Opcodes.ASM5){
                    @Override public void visitMethodInsn(int opcode,String owner,String name,String desc,boolean itf){
                        if(owner.endsWith("/"+ownerSuffix)&&called.equals(name))found[0]=true;
                    }
                };}
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);}finally{in.close();}
        return found[0];
    }
    private static boolean dripstoneDoesNotProvideChunks()throws Exception{
        InputStream in=FuturaDripstoneCavesRuntime120.class.getResourceAsStream("FuturaDripstoneCavesRuntime120.class");if(in==null)return false;
        final boolean[] guarded={false,false};final boolean[] unsafe={false};try{new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM5){
            @Override public MethodVisitor visitMethod(int access,final String method,String desc,String signature,String[] exceptions){return new MethodVisitor(Opcodes.ASM5){
                @Override public void visitMethodInsn(int opcode,String owner,String name,String desc,boolean itf){
                    if("loadedBlock".equals(method)&&"chunkExists".equals(name))guarded[0]=true;
                    if("setLoaded".equals(method)&&"chunkExists".equals(name))guarded[1]=true;
                    if(("getBlock".equals(name)||"setBlock".equals(name))&&!"loadedBlock".equals(method)&&!"setLoaded".equals(method))unsafe[0]=true;
                    if("getChunkFromChunkCoords".equals(name)||"provideChunk".equals(name)||"loadChunk".equals(name))unsafe[0]=true;
                }};}
        },ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);}finally{in.close();}return guarded[0]&&guarded[1]&&!unsafe[0];
    }
    private static boolean resourceContains(String path,String expected)throws Exception{
        InputStream in=FuturaCaveBackportFixtures120.class.getResourceAsStream(path);if(in==null)return false;
        BufferedReader reader=new BufferedReader(new InputStreamReader(in,"UTF-8"));
        try{String line;while((line=reader.readLine())!=null)if(expected.equals(line))return true;return false;}
        finally{reader.close();}
    }
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private FuturaCaveBackportFixtures120(){ }
}
