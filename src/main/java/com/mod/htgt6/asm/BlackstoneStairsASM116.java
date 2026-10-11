package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
/** Preserve legacy stair behavior, except that our water bit cannot change corner topology. */
public final class BlackstoneStairsASM116 {
    private static final String HOOK="com/mod/htgt6/minecraft1202/Module/Blocks/Modern/BlockBlackstoneStairs116";
    public static byte[] transform(byte[] bytes) {
        ClassNode c=new ClassNode();new ClassReader(bytes).accept(c,0);int count=0;
        for(MethodNode m:c.methods) {
            if(!m.name.equals("func_150146_f")||!m.desc.equals("(Lnet/minecraft/world/IBlockAccess;IIII)Z"))continue;
            count++;
            for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(HOOK))return bytes;
            m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();
            m.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));m.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
            for(int slot=2;slot<=5;slot++)m.instructions.add(new VarInsnNode(Opcodes.ILOAD,slot));
            m.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"sameOrientation","(Lnet/minecraft/block/BlockStairs;Lnet/minecraft/world/IBlockAccess;IIII)Z",false));m.instructions.add(new InsnNode(Opcodes.IRETURN));
        }
        if(count!=1)throw new IllegalStateException("Stair corner methods="+count);
        ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(out);return out.toByteArray();
    }
    private BlackstoneStairsASM116(){}
}
