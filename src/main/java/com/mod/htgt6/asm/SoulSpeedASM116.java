package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
/** Remove legacy soul sand drag only for living entities wearing Soul Speed boots. */
public final class SoulSpeedASM116 {
    private static final String H="com/mod/htgt6/minecraft1202/Module/Gameplay/Modern/SoulSpeed116";
    public static byte[] transform(byte[] bytes){
        ClassNode c=new ClassNode();new ClassReader(bytes).accept(c,0);int found=0;
        for(MethodNode m:c.methods){
            if(!(m.name.equals("onEntityCollidedWithBlock")||m.name.equals("func_149670_a"))||!m.desc.equals("(Lnet/minecraft/world/World;IIILnet/minecraft/entity/Entity;)V"))continue;
            found++;for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(H))return bytes;
            InsnList hook=new InsnList();LabelNode vanilla=new LabelNode();hook.add(new VarInsnNode(Opcodes.ALOAD,5));hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,H,"bypassSlowdown","(Lnet/minecraft/entity/Entity;)Z",false));hook.add(new JumpInsnNode(Opcodes.IFEQ,vanilla));hook.add(new InsnNode(Opcodes.RETURN));hook.add(vanilla);hook.add(new FrameNode(Opcodes.F_SAME,0,null,0,null));m.instructions.insert(hook);
        }if(found!=1)throw new IllegalStateException("Soul sand collision methods="+found);
        ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(out);return out.toByteArray();
    }
    private SoulSpeedASM116(){}
}
