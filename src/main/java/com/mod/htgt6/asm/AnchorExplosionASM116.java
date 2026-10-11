package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
/** Keep vanilla explosion mechanics; substitute modern damage only for our scoped anchor explosion. */
public final class AnchorExplosionASM116 {
    private static final String H="com/mod/htgt6/minecraft1202/Module/Gameplay/Modern/AnchorExplosion116";
    public static byte[] transform(byte[] bytes){
        ClassNode c=new ClassNode();new ClassReader(bytes).accept(c,0);int sites=0;
        for(MethodNode m:c.methods){
            if(!(m.name.equals("doExplosionA")||m.name.equals("func_77278_a"))||!m.desc.equals("()V"))continue;
            for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(H))return bytes;
            for(AbstractInsnNode i:m.instructions.toArray()){
                if(!(i instanceof MethodInsnNode))continue;MethodInsnNode call=(MethodInsnNode)i;
                if(call.owner.equals("net/minecraft/entity/Entity")&&(call.name.equals("attackEntityFrom")||call.name.equals("func_70097_a"))&&call.desc.equals("(Lnet/minecraft/util/DamageSource;F)Z")){
                    m.instructions.insertBefore(call,new VarInsnNode(Opcodes.ALOAD,0));
                    m.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,H,"damage","(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/DamageSource;FLnet/minecraft/world/Explosion;)Z",false));sites++;
                }
            }
        }
        if(sites!=1)throw new IllegalStateException("Anchor explosion damage sites="+sites);
        ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(out);return out.toByteArray();
    }
    private AnchorExplosionASM116(){}
}
