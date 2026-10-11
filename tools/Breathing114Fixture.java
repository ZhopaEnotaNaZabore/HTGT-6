package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.PlayerBreathing120;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDolphin120;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.Enchantment;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public final class Breathing114Fixture {
    static int checks;
    static final String HOOK="com/mod/htgt6/minecraft1202/Module/Gameplay/Aquatic/PlayerBreathing120";
    public static void main(String[] args)throws Exception {
        BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
        Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
        world.provider.worldObj=world;
        Constructor<?> ctor=Class.forName("LlamaInteraction77Fixture$Player").getDeclaredConstructor(World.class);
        ctor.setAccessible(true);EntityPlayer player=(EntityPlayer)ctor.newInstance(world);
        int[] starts={-20,-19,-1,0,1,295,296,297,299,300,301,4800};
        for(int air:starts){player.setAir(air);check(PlayerBreathing120.surfaceAir(300,player)==(air>=300?air:Math.min(air+4,300)),"surface boundary "+air);}
        player.setAir(0);for(int i=0;i<74;i++)player.setAir(PlayerBreathing120.surfaceAir(300,player));
        check(player.getAir()==296,"74 ticks not full");player.setAir(PlayerBreathing120.surfaceAir(300,player));check(player.getAir()==300,"75 ticks full");
        player.setAir(20);player.setHealth(0);check(PlayerBreathing120.surfaceAir(300,player)==20,"dead player unchanged");player.setHealth(20);
        check(PlayerBreathing120.surfaceAir(600,player)==600,"Crucible custom maximum preserved");
        EntityDolphin120 dolphin=new EntityDolphin120(world);dolphin.setAir(4800);
        check(PlayerBreathing120.surfaceAir(300,dolphin)==300,"nonplayer retains original call contract");
        Method decrease=EntityLivingBase.class.getDeclaredMethod("decreaseAirSupply",int.class);decrease.setAccessible(true);
        for(int level=0;level<=3;level++) {
            ItemStack helmet=new ItemStack(Items.diamond_helmet);if(level>0)helmet.addEnchantment(Enchantment.respiration,level);
            player.inventory.armorInventory[3]=helmet;player.getRNG().setSeed(114);Random expected=new Random(114);int air=300;
            for(int i=0;i<100;i++){int next=level>0&&expected.nextInt(level+1)>0?air:air-1;int actual=(Integer)decrease.invoke(player,air);check(actual==next,"respiration level "+level+" tick "+i);air=actual;}
        }
        byte[] original=read(EntityLivingBase.class.getResourceAsStream("EntityLivingBase.class"));
        Method transform=Class.forName("com.mod.htgt6.asm.AquaticBreathingASM").getDeclaredMethod("transform",byte[].class);transform.setAccessible(true);
        byte[] result=(byte[])transform.invoke(null,(Object)original);verify(result,"MCP");
        check(Arrays.equals(result,(byte[])transform.invoke(null,(Object)result)),"idempotent hook");
        ClassNode srg=node(original);
        for(MethodNode m:srg.methods){if(m.name.equals("onEntityUpdate"))m.name="func_70030_z";
            for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof MethodInsnNode){MethodInsnNode c=(MethodInsnNode)n;if(c.name.equals("setAir"))c.name="func_70050_g";}}
        ClassWriter writer=new ClassWriter(0);srg.accept(writer);verify((byte[])transform.invoke(null,(Object)writer.toByteArray()),"SRG");
        Method bubble=Class.forName("com.mod.htgt6.asm.HTGT6Transformer").getDeclaredMethod("transformBubbleBreathing120",byte[].class);bubble.setAccessible(true);
        byte[] combined=(byte[])transform.invoke(null,bubble.invoke(null,(Object)original));verify(combined,"bubble composition");
        int bubbleCalls=0;for(MethodNode m:node(combined).methods)for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof MethodInsnNode&&((MethodInsnNode)n).name.equals("airSupply"))bubbleCalls++;
        check(bubbleCalls==1,"existing column breathing retained");
        ClassNode broken=node(original);for(Iterator<MethodNode> it=broken.methods.iterator();it.hasNext();)if(it.next().name.equals("onEntityUpdate"))it.remove();
        writer=new ClassWriter(0);broken.accept(writer);boolean rejected=false;try{transform.invoke(null,(Object)writer.toByteArray());}catch(InvocationTargetException e){rejected=e.getCause() instanceof IllegalStateException;}
        check(rejected,"missing target rejected");
        System.out.println("PASS breathing114: "+checks+" checks; surface recovery, Respiration 0..3, MCP/SRG bytecode and bubble composition");
    }
    static void verify(byte[] bytes,String label)throws Exception {
        ClassNode cn=node(bytes);int calls=0,constant=0;
        for(MethodNode m:cn.methods)if(m.name.equals("onEntityUpdate")||m.name.equals("func_70030_z")) {
            new Analyzer(new BasicVerifier()).analyze(cn.name,m);
            for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof MethodInsnNode&&((MethodInsnNode)n).owner.equals(HOOK))calls++;
            for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof IntInsnNode&&((IntInsnNode)n).operand==-20)constant++;
        }
        check(calls==1,label+" exactly one refill hook");check(constant==1,label+" drowning threshold retained");
    }
    static ClassNode node(byte[] bytes){ClassNode n=new ClassNode();new ClassReader(bytes).accept(n,0);return n;}
    static byte[] read(InputStream in)throws IOException{try{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return out.toByteArray();}finally{in.close();}}
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
}

