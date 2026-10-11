package com.mod.htgt6.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/** Repeats only legacy collision resolution, never movement, gravity or the entity tick. */
final class PiercingArrowASM {
    static final String HOOK="com/mod/htgt6/minecraft1202/Module/Entity/Combat/EntityCombatArrow";
    static void transform(ClassNode node) {
        int starts=0,predicates=0,deaths=0,ends=0,counts=0;
        for(MethodNode m:node.methods)if(m.desc.equals("()V")&&(m.name.equals("onUpdate")||m.name.equals("func_70071_h_"))) {
            LabelNode repeat=new LabelNode();int deadIndex=0,criticalIndex=0;
            for(AbstractInsnNode i:m.instructions.toArray()) {
                if(i instanceof FieldInsnNode) {
                    FieldInsnNode f=(FieldInsnNode)i;
                    if(f.getOpcode()==Opcodes.PUTFIELD&&(f.name.equals("ticksInAir")||f.name.equals("field_70257_an"))) {
                        // The increment, unlike resets, ends with IADD before PUTFIELD.
                        AbstractInsnNode previous=i.getPrevious();while(previous!=null&&previous.getOpcode()<0)previous=previous.getPrevious();
                        if(previous!=null&&previous.getOpcode()==Opcodes.IADD){m.instructions.insert(i,repeat);starts++;}
                    }
                }
                if(!(i instanceof MethodInsnNode))continue;
                MethodInsnNode call=(MethodInsnNode)i;
                if((call.name.equals("canBeCollidedWith")||call.name.equals("func_70067_L"))&&call.desc.equals("()Z")) {
                    m.instructions.insertBefore(i,new VarInsnNode(Opcodes.ALOAD,0));
                    m.instructions.set(i,new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"canHit","(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/projectile/EntityArrow;)Z",false));predicates++;
                } else if((call.name.equals("setArrowCountInEntity")||call.name.equals("func_85034_r"))&&call.desc.equals("(I)V")) {
                    m.instructions.insertBefore(i,new VarInsnNode(Opcodes.ALOAD,0));
                    m.instructions.set(i,new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"updateEmbeddedArrowCount","(Lnet/minecraft/entity/EntityLivingBase;ILnet/minecraft/entity/projectile/EntityArrow;)V",false));counts++;
                } else if((call.name.equals("setDead")||call.name.equals("func_70106_y"))&&call.desc.equals("()V")&&++deadIndex==2) {
                    m.instructions.set(i,new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"finishEntityHit","(Lnet/minecraft/entity/projectile/EntityArrow;)V",false));deaths++;
                } else if((call.name.equals("getIsCritical")||call.name.equals("func_70241_g"))&&call.desc.equals("()Z")&&++criticalIndex==2) {
                    AbstractInsnNode receiver=i.getPrevious();while(receiver.getOpcode()<0)receiver=receiver.getPrevious();
                    if(!(receiver instanceof VarInsnNode)||receiver.getOpcode()!=Opcodes.ALOAD||((VarInsnNode)receiver).var!=0)throw new IllegalStateException("Arrow critical receiver drift");
                    InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));
                    code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"repeatCollision","(Lnet/minecraft/entity/projectile/EntityArrow;)Z",false));
                    code.add(new JumpInsnNode(Opcodes.IFNE,repeat));m.instructions.insertBefore(receiver,code);ends++;
                }
            }
        }
        if(starts!=1||predicates!=1||deaths!=1||ends!=1||counts!=1)throw new IllegalStateException("Piercing arrow anchors drift: "+starts+","+predicates+","+deaths+","+ends+","+counts);
    }
}
