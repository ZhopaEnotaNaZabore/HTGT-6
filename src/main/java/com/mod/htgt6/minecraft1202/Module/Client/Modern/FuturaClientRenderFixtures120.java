package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120;

import com.mod.htgt6.asm.HTGT6Transformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Actual-transform contracts for the Futura 24-section client render grid. */
public final class FuturaClientRenderFixtures120 {
    private static final String GLOBAL="net/minecraft/client/renderer/RenderGlobal";
    private static final String RENDERER="net/minecraft/client/renderer/WorldRenderer";
    private static final String CLIENT="net/minecraft/client/multiplayer/WorldClient";

    public static void main(String[] args)throws Exception{
        if(args.length!=1)throw new IllegalArgumentException("Expected Forge deobfuscated jar");
        require(HTGT6Transformer.renderSectionCount120(false)==16,"Vanilla render grid changed");
        require(HTGT6Transformer.renderHeight120(false)==256,"Vanilla render height changed");
        require(HTGT6Transformer.renderSectionCount120(true)==24,"Futura render grid is not 24 sections");
        require(HTGT6Transformer.renderHeight120(true)==384,"Futura render height is not 384");
        ZipFile jar=new ZipFile(args[0]);
        try{
            HTGT6Transformer transformer=new HTGT6Transformer();
            byte[] global=transformer.transform(GLOBAL,GLOBAL,read(jar,GLOBAL+".class"));
            byte[] renderer=transformer.transform(RENDERER,RENDERER,read(jar,RENDERER+".class"));
            byte[] client=transformer.transform(CLIENT,CLIENT,read(jar,CLIENT+".class"));
            require(countCalls(global,"renderSectionCount120")==1,
                    "RenderGlobal conditional section matcher changed");
            require(constructorVerticalCapacity(global)==24,
                    "RenderGlobal display-list/occlusion capacity is not 24 sections");
            require(countCalls(client,"renderHeight120")==1,
                    "WorldClient conditional invalidation matcher changed");
            require(countConstant(renderer,256)==0,
                    "WorldRenderer contains a total-height 256 cap");
            require(worldRendererLocalLoops(renderer)>=3,
                    "WorldRenderer no longer renders a complete 16-block section");
        }finally{jar.close();}
        require(FuturaHeight120.ActivationGate.CLIENT_RENDER_24_SECTIONS.isReady(),
                "Proven client-render gate must remain ready");
        require(FuturaHeight120.isPipelineComplete(),"All proven height gates must complete the pipeline");
        System.out.println("Futura client render fixtures OK; vanilla=16/256, Futura=24/384, pipeline=true");
    }

    private static int constructorVerticalCapacity(byte[] bytes){
        ClassNode node=read(bytes);
        for(MethodNode method:node.methods)if("<init>".equals(method.name)){
            for(AbstractInsnNode instruction:method.instructions.toArray()){
                if(!(instruction instanceof IntInsnNode)||instruction.getOpcode()!=Opcodes.BIPUSH
                        ||((IntInsnNode)instruction).operand!=24)continue;
                AbstractInsnNode next=instruction.getNext();
                if(next instanceof VarInsnNode&&next.getOpcode()==Opcodes.ISTORE
                        &&((VarInsnNode)next).var==3)return 24;
            }
        }
        return 0;
    }

    private static int worldRendererLocalLoops(byte[] bytes){
        ClassNode node=read(bytes);int count=0;
        for(MethodNode method:node.methods)if("updateRenderer".equals(method.name))
            for(AbstractInsnNode instruction:method.instructions.toArray())
                if(instruction instanceof IntInsnNode&&instruction.getOpcode()==Opcodes.BIPUSH
                        &&((IntInsnNode)instruction).operand==16)count++;
        return count;
    }

    private static int countConstant(byte[] bytes,int value){
        ClassNode node=read(bytes);int count=0;
        for(MethodNode method:node.methods)for(AbstractInsnNode instruction:method.instructions.toArray())
            if(instruction instanceof IntInsnNode&&((IntInsnNode)instruction).operand==value)count++;
        return count;
    }

    private static int countCalls(byte[] bytes,String name){
        ClassNode node=read(bytes);int count=0;
        for(MethodNode method:node.methods)for(AbstractInsnNode instruction:method.instructions.toArray())
            if(instruction instanceof MethodInsnNode&&name.equals(((MethodInsnNode)instruction).name))count++;
        return count;
    }

    private static ClassNode read(byte[] bytes){ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);return node;}
    private static byte[] read(ZipFile jar,String name)throws IOException{
        ZipEntry entry=jar.getEntry(name);if(entry==null)throw new IOException("Missing "+name);
        InputStream in=jar.getInputStream(entry);try{ByteArrayOutputStream out=new ByteArrayOutputStream();
            byte[] buffer=new byte[8192];for(int read;(read=in.read(buffer))>=0;)out.write(buffer,0,read);return out.toByteArray();}
        finally{in.close();}
    }
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private FuturaClientRenderFixtures120(){ }
}
