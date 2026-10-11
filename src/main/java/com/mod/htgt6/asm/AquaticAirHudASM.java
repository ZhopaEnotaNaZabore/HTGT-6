package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

/** Keep Forge's air HUD visible while the modern surface refill is incomplete. */
final class AquaticAirHudASM {
    static byte[] transform(byte[] bytes){
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0;
        String owner="com/mod/htgt6/minecraft1202/Module/Gameplay/Aquatic/PlayerBreathing120";
        for(MethodNode method:node.methods)if(method.name.equals("renderAir")&&method.desc.equals("(II)V")){
            for(AbstractInsnNode insn:method.instructions.toArray())if(insn instanceof MethodInsnNode){
                MethodInsnNode call=(MethodInsnNode)insn;
                if(call.owner.equals(owner)&&call.name.equals("showAir"))return bytes;
                String descriptor="(Lnet/minecraft/block/material/Material;)Z";
                // Forge's own class name stays readable even when its Minecraft calls are Notch-named.
                // The ordinary class-name bridge therefore does not normalize this class.
                boolean raw=!call.desc.equals(descriptor)&&FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(call.desc).equals(descriptor);
                String methodName=raw?FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(call.owner,call.name,call.desc):call.name;
                if(call.getOpcode()==Opcodes.INVOKEVIRTUAL&&(methodName.equals("isInsideOfMaterial")||methodName.equals("func_70055_a"))
                        &&(raw||call.desc.equals(descriptor))){
                    String player=raw?FMLDeobfuscatingRemapper.INSTANCE.unmap("net/minecraft/entity/player/EntityPlayer"):"net/minecraft/entity/player/EntityPlayer";
                    String material=raw?FMLDeobfuscatingRemapper.INSTANCE.unmap("net/minecraft/block/material/Material"):"net/minecraft/block/material/Material";
                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,owner,"showAir",
                            "(L"+player+";L"+material+";)Z",false));count++;
                }
            }
        }
        if(count!=1)throw new IllegalStateException("Aquatic air HUD hook drift: "+count);
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
}
