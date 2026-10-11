package com.mod.htgt6.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** MCP/SRG input; the existing DevRuntimeSrgRemapper supplies the production bridge. */
public final class NetheriteItemASM120 {
    private static final String HOOK="com/mod/htgt6/minecraft1202/Module/Gameplay/VillageAndPillage/NetheriteEquipment120";
    private static final String IMMUNE="(Lnet/minecraft/entity/item/EntityItem;Lnet/minecraft/util/DamageSource;)Z";
    private static final String TICK="(Lnet/minecraft/entity/item/EntityItem;)V";

    public static byte[] transform(byte[] bytes){
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
        int damageMethods=0,tickMethods=0;boolean changed=false;
        for(MethodNode method:node.methods){
            if(("attackEntityFrom".equals(method.name)||"func_70097_a".equals(method.name))
                    &&method.desc.equals("(Lnet/minecraft/util/DamageSource;F)Z")){
                damageMethods++;
                int existing=calls(method,"immune",IMMUNE);
                if(existing>1)throw new IllegalStateException("Duplicate netherite damage guards");
                if(existing==1)continue;
                InsnList hook=new InsnList();LabelNode proceed=new LabelNode();
                hook.add(new VarInsnNode(Opcodes.ALOAD,0));hook.add(new VarInsnNode(Opcodes.ALOAD,1));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"immune",IMMUNE,false));
                hook.add(new JumpInsnNode(Opcodes.IFEQ,proceed));hook.add(new InsnNode(Opcodes.ICONST_0));
                hook.add(new InsnNode(Opcodes.IRETURN));hook.add(proceed);
                hook.add(new FrameNode(Opcodes.F_SAME,0,null,0,null));
                method.instructions.insert(hook);changed=true;
            }
            if(("onUpdate".equals(method.name)||"func_70071_h_".equals(method.name))&&method.desc.equals("()V")){
                tickMethods++;int returns=0;
                for(AbstractInsnNode ins:method.instructions.toArray())if(ins.getOpcode()==Opcodes.RETURN)returns++;
                int existing=calls(method,"afterItemTick",TICK);
                if(existing!=0&&existing!=returns)throw new IllegalStateException("Partial netherite tick instrumentation: "+existing+"/"+returns);
                if(existing==returns&&returns>0)continue;
                if(returns==0)throw new IllegalStateException("EntityItem tick has no return");
                for(AbstractInsnNode ins:method.instructions.toArray())if(ins.getOpcode()==Opcodes.RETURN){
                    InsnList hook=new InsnList();hook.add(new VarInsnNode(Opcodes.ALOAD,0));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"afterItemTick",TICK,false));
                    method.instructions.insertBefore(ins,hook);
                }
                changed=true;
            }
        }
        if(damageMethods!=1||tickMethods!=1)throw new IllegalStateException("Netherite item methods="+damageMethods+"/"+tickMethods);
        if(!changed)return bytes; // Byte-for-byte stable on repeated application.
        ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);return out.toByteArray();
    }
    private static int calls(MethodNode method,String name,String descriptor){
        int count=0;for(AbstractInsnNode ins:method.instructions.toArray())if(ins instanceof MethodInsnNode){
            MethodInsnNode call=(MethodInsnNode)ins;
            if(call.getOpcode()==Opcodes.INVOKESTATIC&&HOOK.equals(call.owner)&&name.equals(call.name)&&descriptor.equals(call.desc))count++;
        }return count;
    }
    private NetheriteItemASM120(){}
}
