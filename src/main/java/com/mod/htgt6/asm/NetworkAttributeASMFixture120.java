package com.mod.htgt6.asm;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Regression for the unknown-mob-attribute crash in NetHandlerPlayClient. */
public final class NetworkAttributeASMFixture120 {
    private static final String NAME="net/minecraft/client/network/NetHandlerPlayClient";
    public static void main(String[] args)throws Exception{
        if(args.length!=1)throw new IllegalArgumentException("Expected Forge binary jar");
        ZipFile jar=new ZipFile(args[0]);byte[] input;
        try{input=read(jar,NAME+".class");}finally{jar.close();}
        byte[] output=new HTGT6Transformer().transform(NAME,NAME,input);
        ClassNode node=new ClassNode();new ClassReader(output).accept(node,0);
        int widened=0,broken=0;
        for(MethodNode method:node.methods){
            if(!"(Lnet/minecraft/network/play/server/S20PacketEntityProperties;)V".equals(method.desc))continue;
            for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction instanceof LdcInsnNode){
                Object value=((LdcInsnNode)instruction).cst;
                if(Double.valueOf(-Double.MAX_VALUE).equals(value))widened++;
                if(Double.valueOf(Double.MIN_NORMAL).equals(value))broken++;
            }
        }
        if(widened!=1||broken!=0)throw new IllegalStateException(
                "synthetic attribute range widened="+widened+", stale positive minimum="+broken);
        System.out.println("NetHandlerPlayClient modded-attribute range fixture OK");
    }
    private static byte[] read(ZipFile jar,String name)throws Exception{
        ZipEntry entry=jar.getEntry(name);if(entry==null)throw new IllegalStateException("Missing "+name);
        InputStream input=jar.getInputStream(entry);try{ByteArrayOutputStream output=new ByteArrayOutputStream();
            byte[] buffer=new byte[8192];for(int count;(count=input.read(buffer))>=0;)output.write(buffer,0,count);
            return output.toByteArray();}finally{input.close();}
    }
    private NetworkAttributeASMFixture120(){}
}
