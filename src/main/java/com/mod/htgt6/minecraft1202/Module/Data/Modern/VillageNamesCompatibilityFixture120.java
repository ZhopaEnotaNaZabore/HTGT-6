package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.asm.HTGT6Transformer;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/** Verifies the narrow Village Names 4.4.13 compatibility patches against the shipped jar. */
public final class VillageNamesCompatibilityFixture120 {
    private static final String FUNCTIONS="astrotibs/villagenames/utility/FunctionsVN";
    private static final String MONITOR="astrotibs/villagenames/handler/EntityMonitorHandler";

    public static void main(String[] args)throws Exception{
        if(args.length!=1)throw new IllegalArgumentException("Expected Village Names jar");
        ZipFile zip=new ZipFile(args[0]);
        try{
            HTGT6Transformer transformer=new HTGT6Transformer();
            ClassNode functions=read(transformer.transform(FUNCTIONS.replace('/','.'),
                    FUNCTIONS.replace('/','.'),entry(zip,FUNCTIONS+".class")));
            MethodNode trade=find(functions,"monitorVillagerTrades",
                    "(Lnet/minecraft/entity/passive/EntityVillager;)V");
            require(nextOpcode(trade.instructions.getFirst())==Opcodes.RETURN,
                    "VN trade monitor was not disabled");

            ClassNode monitor=read(transformer.transform(MONITOR.replace('/','.'),MONITOR.replace('/','.'),
                    entry(zip,MONITOR+".class")));
            MethodNode living=find(monitor,"onLivingUpdateEvent",
                    "(Lnet/minecraftforge/event/entity/living/LivingEvent$LivingUpdateEvent;)V");
            int[] expected={Opcodes.ALOAD,Opcodes.GETFIELD,Opcodes.INVOKEVIRTUAL,
                    Opcodes.LDC,Opcodes.IF_ACMPNE,Opcodes.RETURN};
            AbstractInsnNode instruction=living.instructions.getFirst();
            for(int opcode:expected){instruction=nextInstruction(instruction);require(instruction!=null
                    &&instruction.getOpcode()==opcode,"VN living monitor guard opcode mismatch");
                instruction=instruction.getNext();}
            System.out.println("Village Names compatibility fixtures OK");
        }finally{zip.close();}
    }

    private static byte[] entry(ZipFile zip,String name)throws Exception{
        ZipEntry entry=zip.getEntry(name);if(entry==null)throw new IllegalStateException("Missing "+name);
        InputStream input=zip.getInputStream(entry);ByteArrayOutputStream output=new ByteArrayOutputStream();
        try{byte[] buffer=new byte[8192];for(int read;(read=input.read(buffer))>=0;)output.write(buffer,0,read);}
        finally{input.close();}return output.toByteArray();
    }
    private static ClassNode read(byte[] bytes){ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);return node;}
    private static MethodNode find(ClassNode node,String name,String desc){for(MethodNode method:node.methods)
        if(name.equals(method.name)&&desc.equals(method.desc))return method;
        throw new IllegalStateException("Missing method "+name+desc);}
    private static int nextOpcode(AbstractInsnNode node){node=nextInstruction(node);return node==null?-1:node.getOpcode();}
    private static AbstractInsnNode nextInstruction(AbstractInsnNode node){while(node!=null&&node.getOpcode()<0)node=node.getNext();return node;}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private VillageNamesCompatibilityFixture120(){}
}
