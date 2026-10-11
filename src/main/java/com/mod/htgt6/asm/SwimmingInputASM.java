package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
final class SwimmingInputASM {
    static byte[] transform(byte[] bytes){ClassNode n=new ClassNode();new ClassReader(bytes).accept(n,0);int input=0,ground=0;
        String helper="com/mod/htgt6/minecraft1202/Module/Client/Aquatic/SwimmingInput120";
        for(MethodNode m:n.methods)if((m.name.equals("onLivingUpdate")||m.name.equals("func_70636_d"))&&m.desc.equals("()V"))for(AbstractInsnNode i:m.instructions.toArray()){
            if(i instanceof MethodInsnNode){MethodInsnNode c=(MethodInsnNode)i;if(c.desc.equals("()V")&&(c.name.equals("updatePlayerMoveState")||c.name.equals("func_78898_a"))){InsnList h=new InsnList();h.add(new VarInsnNode(Opcodes.ALOAD,0));h.add(new MethodInsnNode(Opcodes.INVOKESTATIC,helper,"update","(Lnet/minecraft/client/entity/EntityPlayerSP;)V",false));m.instructions.insert(i,h);input++;}}
            if(input==1&&ground==0&&i instanceof FieldInsnNode&&i.getOpcode()==Opcodes.GETFIELD){FieldInsnNode f=(FieldInsnNode)i;if(f.name.equals("onGround")||f.name.equals("field_70122_E")){InsnList h=new InsnList();h.add(new VarInsnNode(Opcodes.ALOAD,0));h.add(new MethodInsnNode(Opcodes.INVOKESTATIC,helper,"sprintSurface","(ZLnet/minecraft/entity/player/EntityPlayer;)Z",false));m.instructions.insert(i,h);ground++;}}
        }
        if(input!=1||ground!=1)throw new IllegalStateException("Swimming input hook drift: "+input+"/"+ground);
        ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);return w.toByteArray();
    }
}
