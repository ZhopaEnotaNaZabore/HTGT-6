package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
/** Copy/replace the spawn list before Forge's event, without mutating biome-owned lists. */
public final class OutpostSpawnASM120 implements Opcodes {
    public static byte[] transform(byte[] bytes){
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
        for(MethodNode m:node.methods)if(m.name.equals("getPotentialSpawns")&&Type.getArgumentTypes(m.desc).length==6){
            for(AbstractInsnNode n=m.instructions.getFirst();n!=null;n=n.getNext())if(n instanceof MethodInsnNode&&((MethodInsnNode)n).owner.endsWith("OutpostSpawnRuntime120"))return bytes;
            InsnList hook=new InsnList();hook.add(new VarInsnNode(ALOAD,0));hook.add(new VarInsnNode(ALOAD,1));
            for(int i=2;i<=4;i++)hook.add(new VarInsnNode(ILOAD,i));hook.add(new VarInsnNode(ALOAD,5));
            hook.add(new MethodInsnNode(INVOKESTATIC,"com/mod/htgt6/minecraft1202/Module/World/VillageAndPillage/OutpostSpawnRuntime120","list","(Ljava/lang/Object;Ljava/lang/Object;IIILjava/util/List;)Ljava/util/List;",false));
            hook.add(new VarInsnNode(ASTORE,5));m.instructions.insert(hook);
        }
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
    private OutpostSpawnASM120(){}
}
