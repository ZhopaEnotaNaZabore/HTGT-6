package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** One pending berry slowdown at Entity.moveEntity entry; MCP and SRG names. */
public final class BerryMovementASM {
    private static final String HOOK="com/mod/htgt6/minecraft1202/Module/Gameplay/VillageAndPillage/BerryMovement147";
    public static byte[] transform(byte[] bytes){
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
        String web=null;for(FieldNode field:node.fields)if(field.desc.equals("Z")&&(field.name.equals("isInWeb")||field.name.equals("field_70134_J")))web=field.name;
        int count=0;
        for(MethodNode method:node.methods)if((method.name.equals("moveEntity")||method.name.equals("func_70091_d"))&&method.desc.equals("(DDD)V")){
            for(AbstractInsnNode insn:method.instructions.toArray())if(insn instanceof MethodInsnNode&&((MethodInsnNode)insn).owner.equals(HOOK))return bytes;
            if(web==null)throw new IllegalStateException("Berry movement missing Entity.isInWeb");
            InsnList code=new InsnList();LabelNode end=new LabelNode();
            code.add(new VarInsnNode(Opcodes.ALOAD,0));code.add(new VarInsnNode(Opcodes.ALOAD,0));
            code.add(new FieldInsnNode(Opcodes.GETFIELD,node.name,web,"Z"));
            code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"consume","(Lnet/minecraft/entity/Entity;Z)Z",false));
            code.add(new JumpInsnNode(Opcodes.IFEQ,end));
            for(int axis=0;axis<3;axis++){
                int local=1+2*axis;code.add(new VarInsnNode(Opcodes.DLOAD,local));code.add(new LdcInsnNode(axis==1?.75D:(double).8F));code.add(new InsnNode(Opcodes.DMUL));code.add(new VarInsnNode(Opcodes.DSTORE,local));
            }
            code.add(end);code.add(new FrameNode(Opcodes.F_SAME,0,null,0,null));
            method.instructions.insert(code);count++;
        }
        if(count!=1)throw new IllegalStateException("Berry movement hook drift: "+count);
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
    private BerryMovementASM(){}
}
