package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120ConcretePowder;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

public final class Concrete96Fixture {
    public static void main(String[] args)throws Exception {
        BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
        BlockBackport120ConcretePowder powder=new BlockBackport120ConcretePowder();powder.setHardened(Blocks.stained_hardened_clay);
        for(int color=0;color<16;color++)for(net.minecraft.block.Block water:new net.minecraft.block.Block[]{Blocks.water,Blocks.flowing_water}) {
            w.cells.clear();w.setBlock(0,10,0,water,0,3);
            EntityFallingBlock e=new EntityFallingBlock(w,.5,10.5,.5,powder,color);e.field_145812_b=2;
            check(BlockBackport120ConcretePowder.hardenFalling(e)&&e.isDead,"wet falling entity consumed");
            check(w.getBlock(0,10,0)==Blocks.stained_hardened_clay&&w.getBlockMetadata(0,10,0)==color,"color retained");
            check(!BlockBackport120ConcretePowder.hardenFalling(e),"dead entity cannot place twice");
        }
        w.cells.clear();w.setBlock(0,7,0,Blocks.water,0,3);
        EntityFallingBlock fast=new EntityFallingBlock(w,.5,5.5,.5,powder,9);fast.prevPosY=10.5;
        w.rayHit=new MovingObjectPosition(0,7,0,1,Vec3.createVectorHelper(.5,8,.5));
        check(BlockBackport120ConcretePowder.hardenFalling(fast)&&w.getBlockMetadata(0,7,0)==9,"fast fall stops at traced water");
        w.rayHit=null;
        for(net.minecraft.block.Block dry:new net.minecraft.block.Block[]{Blocks.air,Blocks.lava,Blocks.stone}) {
            w.setBlock(0,10,0,dry,0,3);EntityFallingBlock e=new EntityFallingBlock(w,.5,10.5,.5,powder,3);
            check(!BlockBackport120ConcretePowder.hardenFalling(e)&&!e.isDead,"dry/lava left to vanilla");
        }
        w.setBlock(0,10,0,Blocks.water,0,3);
        EntityFallingBlock rejected=new EntityFallingBlock(w,.5,10.5,.5,powder,3);w.rejectWrites=true;
        check(!BlockBackport120ConcretePowder.hardenFalling(rejected)&&!rejected.isDead,"rejected write preserves entity");w.rejectWrites=false;
        w.isRemote=true;check(!BlockBackport120ConcretePowder.hardenFalling(rejected)&&w.getBlock(0,10,0)==Blocks.water,"client cannot mutate");w.isRemote=false;
        EntityFallingBlock sand=new EntityFallingBlock(w,.5,10.5,.5,Blocks.sand,0);
        check(!BlockBackport120ConcretePowder.hardenFalling(sand)&&!sand.isDead,"ordinary sand unaffected");
        java.io.InputStream stream=EntityFallingBlock.class.getResourceAsStream("EntityFallingBlock.class");
        ClassReader reader=new ClassReader(stream);stream.close();
        ClassNode original=new ClassNode();reader.accept(original,0);ClassWriter originalWriter=new ClassWriter(0);original.accept(originalWriter);
        Class<?> transformer=Class.forName("com.mod.htgt6.asm.ConcreteFallingASM");
        java.lang.reflect.Method transform=transformer.getDeclaredMethod("transform",byte[].class);transform.setAccessible(true);
        byte[] bytes=(byte[])transform.invoke(null,(Object)originalWriter.toByteArray());
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
        int hooks=0;
        for(MethodNode method:node.methods) {
            new Analyzer(new BasicVerifier()).analyze(node.name,method);
            boolean sourceRemoval=false;
            for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction instanceof MethodInsnNode) {
                MethodInsnNode call=(MethodInsnNode)instruction;
                if(call.name.equals("setBlockToAir"))sourceRemoval=true;
                if(call.name.equals("hardenFalling")){check(sourceRemoval,"hook follows source removal");hooks++;}
            }
        }
        check(hooks==1,"single verified ASM hook");
        System.out.println("PASS: 16 colors/source+flowing water, fast trace, no double placement, dry/lava/client/sand guards, rejected placement, Forge ASM verifier and source-removal order");
    }
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
