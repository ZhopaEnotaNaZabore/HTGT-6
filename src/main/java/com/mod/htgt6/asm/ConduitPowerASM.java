package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Adds conduit checks at potion-specific anchors; leaves vanilla/Crucible air handling intact. */
final class ConduitPowerASM {
    static final String HOOK="com/mod/htgt6/minecraft1202/Module/Gameplay/Aquatic/ConduitPowerHooks120";
    static byte[] transform(byte[] bytes, boolean client) {
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
        for(MethodNode m:node.methods)for(AbstractInsnNode i:m.instructions.toArray())
            if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(HOOK))return bytes;
        int checks=0,brightness=0;
        for(MethodNode m:node.methods){
            boolean breathing=!client&&m.desc.equals("()V")&&(m.name.equals("onEntityUpdate")||m.name.equals("func_70030_z"));
            if(breathing||client)for(AbstractInsnNode i:m.instructions.toArray()){
                if(!(i instanceof MethodInsnNode))continue;
                MethodInsnNode call=(MethodInsnNode)i;
                if(call.getOpcode()!=Opcodes.INVOKEVIRTUAL||!(call.name.equals("isPotionActive")||call.name.equals("func_70644_a")||call.name.equals("func_82165_m")))continue;
                // The int and Potion overloads have different production SRG names.
                if(!call.desc.equals(breathing?"(I)Z":"(Lnet/minecraft/potion/Potion;)Z"))continue;
                AbstractInsnNode anchor=previous(i);
                if(breathing&&anchor instanceof FieldInsnNode&&((FieldInsnNode)anchor).desc.equals("I"))anchor=previous(anchor);
                if(!(anchor instanceof FieldInsnNode))continue;
                FieldInsnNode field=(FieldInsnNode)anchor;
                if(!field.owner.equals("net/minecraft/potion/Potion"))continue;
                boolean match=breathing?(field.name.equals("waterBreathing")||field.name.equals("field_76427_o")):(field.name.equals("nightVision")||field.name.equals("field_76439_r"));
                if(!match)continue;
                call.setOpcode(Opcodes.INVOKESTATIC);call.owner=HOOK;call.name=breathing?"breathing":"vision";
                call.desc=breathing?"(Lnet/minecraft/entity/EntityLivingBase;I)Z":"(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/potion/Potion;)Z";call.itf=false;checks++;
            }
            if(client&&(m.name.equals("getNightVisionBrightness")||m.name.equals("func_82830_a"))&&m.desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;F)F")){
                InsnList code=new InsnList();LabelNode vanilla=new LabelNode();
                code.add(new VarInsnNode(Opcodes.ALOAD,1));code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"conduitVisionOnly","(Lnet/minecraft/entity/EntityLivingBase;)Z",false));
                code.add(new JumpInsnNode(Opcodes.IFEQ,vanilla));code.add(new InsnNode(Opcodes.FCONST_1));code.add(new InsnNode(Opcodes.FRETURN));code.add(vanilla);
                m.instructions.insert(code);brightness++;
            }
        }
        if(checks!=(client?2:1)||brightness!=(client?1:0))throw new IllegalStateException("Conduit power anchors drift: client="+client+", checks="+checks+", brightness="+brightness);
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
    private static AbstractInsnNode previous(AbstractInsnNode i){do{i=i.getPrevious();}while(i!=null&&i.getOpcode()<0);return i;}
}
