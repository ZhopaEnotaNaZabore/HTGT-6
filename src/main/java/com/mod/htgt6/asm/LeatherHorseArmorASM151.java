package com.mod.htgt6.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Typed hooks leave foreign/vanilla armor indices and their texture arrays untouched. */
public final class LeatherHorseArmorASM151 {
    private static final String HOOK="com/mod/htgt6/minecraft1202/Module/Gameplay/VillageAndPillage/LeatherHorseHooks151";
    private static final String HORSE="Lnet/minecraft/entity/passive/EntityHorse;";
    private LeatherHorseArmorASM151(){}
    public static byte[] transform(byte[] bytes){
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
        for(MethodNode m:node.methods)for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(HOOK))return bytes;
        int init=0,equip=0,accept=0,armor=0,interact=0;
        for(MethodNode m:node.methods){
            if((m.name.equals("entityInit")||m.name.equals("func_70088_a"))&&m.desc.equals("()V"))for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==Opcodes.RETURN){InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"initialize","("+HORSE+")V",false));m.instructions.insertBefore(i,code);init++;}
            if(m.name.equals("func_146086_d")&&m.desc.equals("(Lnet/minecraft/item/ItemStack;)V")){InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));code.add(new VarInsnNode(Opcodes.ALOAD,1));code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"equipped","("+HORSE+"Lnet/minecraft/item/ItemStack;)V",false));m.instructions.insert(code);equip++;}
            if(m.name.equals("func_146085_a")&&m.desc.equals("(Lnet/minecraft/item/Item;)Z")){
                // OR the typed extension with the existing result; no branch/frame changes.
                for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==Opcodes.IRETURN){InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"accepts","(Lnet/minecraft/item/Item;)Z",false));code.add(new InsnNode(Opcodes.IOR));m.instructions.insertBefore(i,code);accept++;}
            }
            if((m.name.equals("getTotalArmorValue")||m.name.equals("func_70658_aO"))&&m.desc.equals("()I"))for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==Opcodes.IRETURN){InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"protection","(I"+HORSE+")I",false));m.instructions.insertBefore(i,code);armor++;}
            if((m.name.equals("interact")||m.name.equals("func_70085_c"))&&m.desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;)Z")){
                // Extend the existing iron-armor comparison at its exact branch: all tame/adult/rider guards remain vanilla.
                for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof FieldInsnNode){FieldInsnNode field=(FieldInsnNode)i;if(field.owner.equals("net/minecraft/init/Items")&&(field.name.equals("iron_horse_armor")||field.name.equals("field_151138_bX"))){
                    AbstractInsnNode branch=i.getNext();if(branch.getOpcode()!=Opcodes.IF_ACMPNE)throw new IllegalStateException("Horse interact armor comparison drift");
                    m.instructions.remove(i);m.instructions.insertBefore(branch,new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"ironOrLeather","(Lnet/minecraft/item/Item;)Z",false));m.instructions.set(branch,new JumpInsnNode(Opcodes.IFEQ,((JumpInsnNode)branch).label));interact++;
                }}
            }
        }
        if(init!=1||equip!=1||accept!=1||armor!=1||interact!=1)throw new IllegalStateException("Leather horse hooks drift: "+init+"/"+equip+"/"+accept+"/"+armor+"/"+interact);
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
}
