package com.mod.htgt6.asm;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

/** Verifies the actual patched server classes, not a synthetic Forge-shaped method. */
public final class CrucibleAnchor116Fixture {
    private static final String H="com/mod/htgt6/minecraft1202/Module/Gameplay/Modern/RespawnAnchor116";
    public static void main(String[] args)throws Exception {
        File file=new File(args[0]);
        FMLDeobfuscatingRemapper.INSTANCE.setup(new File("."),new LaunchClassLoader(new URL[]{file.toURI().toURL()}),"/deobfuscation_data-1.7.10.lzma");
        RuntimeBytecodeRemapper.enableProductionBridge();
        try(ZipFile zip=new ZipFile(file)) {
            for(String target:new String[]{"net/minecraft/server/management/ServerConfigurationManager","net/minecraft/entity/player/EntityPlayer"}) {
                boolean player=target.endsWith("EntityPlayer");
                String notch=FMLDeobfuscatingRemapper.INSTANCE.unmap(target);
                ByteArrayOutputStream buffer=new ByteArrayOutputStream();
                try(InputStream in=zip.getInputStream(zip.getEntry(notch+".class"))){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)buffer.write(b,0,n);}
                byte[] input=RuntimeBytecodeRemapper.toSrg(buffer.toByteArray());
                byte[] patched=RespawnAnchorASM116.transform(input,player);
                require(Arrays.equals(patched,RespawnAnchorASM116.transform(patched,player)),"Not idempotent");
                // Exercise dispatch and production remapping, too.
                byte[] full=new HTGT6Transformer().transform(notch,target,input);
                ClassNode node=new ClassNode();new ClassReader(RuntimeBytecodeRemapper.toSrg(RuntimeBytecodeRemapper.toNotch(full))).accept(node,0);
                Map<String,Integer> counts=new HashMap<String,Integer>();
                for(MethodNode method:node.methods){
                    if((method.access&(Opcodes.ACC_ABSTRACT|Opcodes.ACC_NATIVE))==0)new Analyzer(new BasicVerifier()).analyze(node.name,method);
                    int coordinates=0;boolean event=false;
                    for(AbstractInsnNode i:method.instructions.toArray()){
                        if(i instanceof TypeInsnNode&&i.getOpcode()==Opcodes.NEW&&((TypeInsnNode)i).desc.equals("org/bukkit/event/player/PlayerRespawnEvent")){
                            require(coordinates==3,"Unsafe location reaches Bukkit event");event=true;
                        }
                        if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(H)){
                            String hook=((MethodInsnNode)i).name;counts.put(hook,counts.containsKey(hook)?counts.get(hook)+1:1);
                            if(hook.equals("hybridCoordinate")){require(!event,"Overriding plugin destination");coordinates++;}
                        }
                    }
                }
                if(player)require(counts.size()==1&&Integer.valueOf(1).equals(counts.get("spawnChanged")),"Player hook");
                else{
                    for(String hook:new String[]{"prepareHybrid","canRespawn","spawnPosition","resolve","place"})require(Integer.valueOf(1).equals(counts.get(hook)),"Missing/duplicate "+hook);
                    require(Integer.valueOf(3).equals(counts.get("hybridCoordinate")),"Location coordinates");
                    require(Integer.valueOf(3).equals(counts.get("finishHybrid")),"Cancellation returns not covered");
                    require(counts.size()==7,"Unexpected hooks");
                    // An unknown method shape must fail explicitly instead of silently disabling anchors.
                    ClassNode broken=new ClassNode();new ClassReader(input).accept(broken,0);
                    for(MethodNode method:broken.methods)if(method.name.equals("respawnPlayer")&&method.desc.contains("org/bukkit/Location"))method.name="unknownRespawn";
                    ClassWriter writer=new ClassWriter(0);broken.accept(writer);
                    boolean rejected=false;try{RespawnAnchorASM116.transform(writer.toByteArray(),false);}catch(IllegalStateException expected){rejected=true;}
                    require(rejected,"Unknown hybrid shape silently accepted");
                }
                System.out.println("PASS Crucible anchor: "+target+" "+counts);
            }
        }
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
