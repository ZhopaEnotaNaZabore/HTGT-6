package com.mod.htgt6.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Hooks the final resolved collision, never the initial (possibly occluded) block ray. */
public final class TargetImpactASM116 {
    private static final String HOOK="com/mod/htgt6/minecraft1202/Module/Gameplay/Modern/TargetImpact116";
    private static final String HIT="net/minecraft/util/MovingObjectPosition";
    private static final String DESC="(Lnet/minecraft/entity/Entity;L"+HIT+";)V";
    public static byte[] transform(byte[] bytes){
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int methods=0;boolean changed=false;
        boolean arrowOrHook=node.name.equals("net/minecraft/entity/projectile/EntityArrow")||node.name.equals("net/minecraft/entity/projectile/EntityFishHook");
        for(MethodNode method:node.methods){
            if(!(method.name.equals("onUpdate")||method.name.equals("func_70071_h_"))||!method.desc.equals("()V"))continue;
            methods++;int existing=0;
            for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof MethodInsnNode){MethodInsnNode m=(MethodInsnNode)i;if(m.owner.equals(HOOK)&&m.name.equals("onImpact")&&m.desc.equals(DESC))existing++;}
            if(existing==1){
                int embedding=0;for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof MethodInsnNode){MethodInsnNode m=(MethodInsnNode)i;if(m.owner.equals(HOOK)&&m.name.equals("embeddingMetadata"))embedding++;}
                if(embedding!=(node.name.endsWith("/EntityArrow")?1:0))throw new IllegalStateException("Incomplete target embedding hooks: "+node.name);
                continue;
            }
            if(existing!=0)throw new IllegalStateException("Duplicate target impact hooks: "+node.name);
            boolean found=false;
            for(AbstractInsnNode i:method.instructions.toArray()){
                if(arrowOrHook&&i instanceof FieldInsnNode){
                    FieldInsnNode field=(FieldInsnNode)i;
                    if(field.getOpcode()==Opcodes.GETFIELD&&field.owner.equals(HIT)
                            &&(field.name.equals("entityHit")||field.name.equals("field_72308_g"))){
                        // At the first entityHit read, entity-vs-block collision selection is final.
                        // Keep the original hit receiver on the operand stack.
                        InsnList hook=new InsnList();hook.add(new InsnNode(Opcodes.DUP));hook.add(new VarInsnNode(Opcodes.ALOAD,0));hook.add(new InsnNode(Opcodes.SWAP));
                        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"onImpact",DESC,false));method.instructions.insertBefore(i,hook);found=true;break;
                    }
                }else if(!arrowOrHook&&i instanceof MethodInsnNode){
                    MethodInsnNode call=(MethodInsnNode)i;
                    if((call.name.equals("onImpact")||call.name.equals("func_70184_a")||call.name.equals("func_70227_a"))
                            &&call.desc.equals("(L"+HIT+";)V")&&call.getOpcode()!=Opcodes.INVOKESTATIC){
                        InsnList hook=new InsnList();hook.add(new InsnNode(Opcodes.DUP2));hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"onImpact",DESC,false));
                        method.instructions.insertBefore(i,hook);found=true;break;
                    }
                }
            }
            if(!found)throw new IllegalStateException("Target resolved-impact anchor absent: "+node.name);
            if(node.name.equals("net/minecraft/entity/projectile/EntityArrow"))retainArrowEmbedding(node,method);
            changed=true;
        }
        if(methods!=1)throw new IllegalStateException("Target onUpdate methods="+methods+" for "+node.name);
        if(!changed)return bytes;
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }

    private static void retainArrowEmbedding(ClassNode node,MethodNode update){
        String saved=null,block=null;
        for(FieldNode field:node.fields)if(field.desc.equals("Lnet/minecraft/block/Block;")){
            if(block!=null)throw new IllegalStateException("Ambiguous arrow block field");block=field.name;
        }
        // The persisted NBT key is namespace independent; discover its actual field,
        // rather than guessing a private MCP/SRG/production name.
        for(MethodNode method:node.methods)if((method.name.equals("writeEntityToNBT")||method.name.equals("func_70014_b"))
                &&method.desc.equals("(Lnet/minecraft/nbt/NBTTagCompound;)V")){
            for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof LdcInsnNode&&"inData".equals(((LdcInsnNode)i).cst)){
                for(AbstractInsnNode next=i.getNext();next!=null;next=next.getNext()){
                    if(next instanceof FieldInsnNode){FieldInsnNode field=(FieldInsnNode)next;if(field.owner.equals(node.name)&&field.desc.equals("I")){saved=field.name;break;}}
                    if(next instanceof MethodInsnNode)break;
                }
            }
        }
        if(saved==null||block==null)throw new IllegalStateException("Arrow NBT embedding contract not found");
        for(AbstractInsnNode i:update.instructions.toArray())if(i instanceof MethodInsnNode){
            MethodInsnNode call=(MethodInsnNode)i;
            if(call.owner.equals("net/minecraft/world/World")&&(call.name.equals("getBlockMetadata")||call.name.equals("func_72805_g"))&&call.desc.equals("(III)I")){
                InsnList hook=new InsnList();hook.add(new VarInsnNode(Opcodes.ALOAD,0));hook.add(new FieldInsnNode(Opcodes.GETFIELD,node.name,saved,"I"));
                hook.add(new VarInsnNode(Opcodes.ALOAD,0));hook.add(new FieldInsnNode(Opcodes.GETFIELD,node.name,block,"Lnet/minecraft/block/Block;"));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"embeddingMetadata","(IILnet/minecraft/block/Block;)I",false));
                update.instructions.insert(i,hook);return;
            }
        }
        throw new IllegalStateException("Arrow ground metadata anchor not found");
    }
    private TargetImpactASM116(){}
}
