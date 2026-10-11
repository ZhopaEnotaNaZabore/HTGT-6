package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.ChunkProviderFutura120;

import com.mod.htgt6.asm.HTGT6Transformer;
import com.mod.htgt6.minecraft1202.Module.World.Modern.AquiferSampler120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.CaveDensityRuntime120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.NoiseChunk120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Dormant/ready generator-height and non-power-of-two raw-array contracts. */
public final class FuturaGeneratorHeightFixtures120 {
    private static final String CHUNK="net/minecraft/world/chunk/Chunk";
    public static void main(String[] args)throws Exception{
        if(args.length!=1)throw new IllegalArgumentException("Expected Forge deobfuscated jar");
        require(FuturaHeight120.generatorHeight(false,true)==256,"Vanilla generator height changed");
        require(FuturaHeight120.generatorHeight(true,false)==256,"Incomplete pipeline activated Futura generator");
        require(FuturaHeight120.generatorSectionCount(true,false)==16,"Dormant generator section count changed");
        require(FuturaHeight120.generatorHeight(true,true)==384,"Ready Futura generator is not 384");
        require(FuturaHeight120.generatorSectionCount(true,true)==24,"Ready Futura generator is not 24 sections");
        require(ChunkProviderFutura120.rawArrayIndex(384,15,383,15)==98303,"384 raw-array upper index changed");
        require(ChunkProviderFutura120.rawArrayIndex(384,0,256,0)
                !=ChunkProviderFutura120.rawArrayIndex(384,0,0,1),"384 raw-array columns overlap");
        verifyMapgenStaging();
        verifyVanillaMineshaftLifecycle();
        verifyDensityAquiferAndPreliminary();
        verifyActualChunkTransform(args[0]);
        require(FuturaHeight120.ActivationGate.GENERATOR_24_SECTIONS.isReady(),
                "Proven generator gate must remain ready");
        System.out.println("Futura generator height fixtures OK; dormant=256, ready=384, raw stride=384");
    }

    private static void verifyMapgenStaging(){
        Block[] extended=new Block[256*384],legacy=new Block[256*256];
        extended[ChunkProviderFutura120.rawArrayIndex(384,0,255,0)]=Blocks.stone;
        extended[ChunkProviderFutura120.rawArrayIndex(384,0,256,0)]=Blocks.dirt;
        extended[ChunkProviderFutura120.rawArrayIndex(384,0,0,1)]=Blocks.grass;
        ChunkProviderFutura120.copyLowerLegacyVolume(extended,384,legacy,false);
        require(legacy[255]==Blocks.stone&&legacy[256]==Blocks.grass,"Mapgen staging changed column stride");
        legacy[255]=Blocks.cobblestone;legacy[256]=Blocks.sand;
        ChunkProviderFutura120.copyLowerLegacyVolume(extended,384,legacy,true);
        require(extended[255]==Blocks.cobblestone&&extended[384]==Blocks.sand,
                "Mapgen staging merge changed lower columns");
        require(extended[256]==Blocks.dirt,"Mapgen staging overwrote upper Futura sections");
    }

    private static void verifyVanillaMineshaftLifecycle()throws IOException{
        String resource="/com/mod/htgt6/minecraft1202/Module/World/Modern/ChunkProviderFutura120.class";
        InputStream input=FuturaGeneratorHeightFixtures120.class.getResourceAsStream(resource);
        require(input!=null,"Compiled Futura chunk provider missing");
        byte[] bytes;try{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];
            for(int count;(count=input.read(buffer))>=0;)out.write(buffer,0,count);bytes=out.toByteArray();
        }finally{input.close();}
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
        require(callCount(node,"<init>","getModdedMapGen")==3,
                "Futura map generators are not retained per provider");
        require(callCount(node,"runRawMapGenerators","func_151539_a")==3,
                "Raw map generators must each prepare exactly once per chunk");
        require(callCount(node,"populate","generateStructuresInChunk")==3,
                "Map structures must each populate exactly once per chunk");
        require(callCount(node,"recreateStructures","func_151539_a")==3,
                "Structure recreation lifecycle is incomplete");
    }

    private static int callCount(ClassNode node,String methodName,String callName){
        int count=0;for(MethodNode method:node.methods)if(methodName.equals(method.name))
            for(AbstractInsnNode instruction:method.instructions.toArray())
                if(instruction instanceof MethodInsnNode
                        &&callName.equals(((MethodInsnNode)instruction).name))count++;
        return count;
    }

    private static void verifyDensityAquiferAndPreliminary(){
        long seed=0x1202F00DL;FuturaMountainDensityRouter120 router=new FuturaMountainDensityRouter120(seed);
        NoiseChunk120 noise=router.createNoiseChunk(0,0,384);double[] column=new double[384];
        noise.fillColumn(0,0,0,column);
        require(Double.isFinite(column[0])&&Double.isFinite(column[383]),"NoiseChunk lost 384 endpoints");
        int preliminary=router.preliminarySurfaceLevel(7,-11,383);
        require(preliminary==Integer.MAX_VALUE||(preliminary>=0&&preliminary<=383),
                "Preliminary surface escaped 0..383: "+preliminary);
        AquiferSampler120 aquifer=new AquiferSampler120(seed,FuturaMountainDensityRouter120.SEA_LEVEL,
                CaveDensityRuntime120.modernToFuturaY(-54),router,383);
        require(aquifer.sampleDensity(7,CaveDensityRuntime120.futuraToModernY(383),-11,-1.0D)!=null,
                "Aquifer rejected Futura Y=383");
    }

    private static void verifyActualChunkTransform(String jarPath)throws IOException{
        ZipFile jar=new ZipFile(jarPath);try{
            byte[] transformed=new HTGT6Transformer().transform(CHUNK,CHUNK,read(jar,CHUNK+".class"));
            ClassNode node=new ClassNode();new ClassReader(transformed).accept(node,0);
            for(MethodNode method:node.methods)if("<init>".equals(method.name)
                    &&"(Lnet/minecraft/world/World;[Lnet/minecraft/block/Block;[BII)V".equals(method.desc)){
                int adds=0,ors=0;for(AbstractInsnNode instruction:method.instructions.toArray()){
                    if(instruction.getOpcode()==Opcodes.IADD)adds++;
                    if(instruction.getOpcode()==Opcodes.IOR)ors++;
                }
                require(ors==0&&adds>=2,"Chunk raw-array constructor is not non-power-of-two safe");return;
            }
            throw new IllegalStateException("Chunk raw-array constructor missing");
        }finally{jar.close();}
    }

    private static byte[] read(ZipFile jar,String name)throws IOException{ZipEntry entry=jar.getEntry(name);
        if(entry==null)throw new IOException("Missing "+name);InputStream in=jar.getInputStream(entry);
        try{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];
            for(int count;(count=in.read(buffer))>=0;)out.write(buffer,0,count);return out.toByteArray();}finally{in.close();}}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private FuturaGeneratorHeightFixtures120(){ }
}
