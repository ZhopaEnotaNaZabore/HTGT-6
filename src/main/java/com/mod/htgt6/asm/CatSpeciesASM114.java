package com.mod.htgt6.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Keep native ocelot mate selection from accepting the modern cat subclass. */
public final class CatSpeciesASM114 {
    public static final String HOOK="com/mod/htgt6/minecraft1202/Module/Entity/Modern/EntityCat114";
    public static byte[] transform(byte[] bytes) {
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
        int found=0;boolean changed=false;
        for(MethodNode method:node.methods) {
            if(!method.desc.equals("(Lnet/minecraft/entity/passive/EntityAnimal;)Z") ||
                    !(method.name.equals("canMateWith")||method.name.equals("func_70878_b"))) continue;
            found++;
            boolean installed=false;
            for(AbstractInsnNode insn:method.instructions.toArray()) if(insn instanceof MethodInsnNode &&
                    ((MethodInsnNode)insn).owner.equals(HOOK)&&((MethodInsnNode)insn).name.equals("restrictLegacyMating")) installed=true;
            if(installed)continue;
            for(AbstractInsnNode insn:method.instructions.toArray()) if(insn.getOpcode()==Opcodes.IRETURN) {
                InsnList guard=new InsnList();guard.add(new VarInsnNode(Opcodes.ALOAD,1));
                guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"restrictLegacyMating",
                        "(ZLnet/minecraft/entity/passive/EntityAnimal;)Z",false));
                method.instructions.insertBefore(insn,guard);
            }
            changed=true;
        }
        if(found!=1)throw new IllegalStateException("Ocelot species hook drift: "+found);
        if(!changed)return bytes;
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
    private CatSpeciesASM114() {}
}
