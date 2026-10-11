package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

final class SwimmingPoseASM {
    static final String HOOK="com/mod/htgt6/minecraft1202/Module/Gameplay/Aquatic/SwimmingPose120";
    static byte[] transform(byte[] bytes,int target){
        ClassNode n=new ClassNode();new ClassReader(bytes).accept(n,0);int count=0;
        for(MethodNode m:n.methods){
            if(target==0&&(m.name.equals("handleWaterMovement")||m.name.equals("func_70072_I"))&&m.desc.equals("()Z"))
                for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).desc.equals("(DDD)Lnet/minecraft/util/AxisAlignedBB;")&&(((MethodInsnNode)i).name.equals("contract")||((MethodInsnNode)i).name.equals("func_72331_e"))){
                    InsnList h=new InsnList();h.add(new VarInsnNode(Opcodes.ALOAD,0));h.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"waterBounds","(Lnet/minecraft/util/AxisAlignedBB;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/AxisAlignedBB;",false));m.instructions.insert(i,h);count++;
                }
            if(target==1){
                boolean eye=(m.name.equals("getEyeHeight")||m.name.equals("func_70047_e"))&&m.desc.equals("()F");
                boolean ray=(m.name.equals("getPosition")||m.name.equals("func_70666_h"))&&m.desc.equals("(F)Lnet/minecraft/util/Vec3;");
                if(eye||ray)for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==(eye?Opcodes.FRETURN:Opcodes.ARETURN)){
                    InsnList h=new InsnList();h.add(new VarInsnNode(Opcodes.ALOAD,0));if(ray)h.add(new VarInsnNode(Opcodes.FLOAD,1));
                    h.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,eye?"eye":"rayOrigin",eye?"(FLnet/minecraft/entity/player/EntityPlayer;)F":"(Lnet/minecraft/util/Vec3;Lnet/minecraft/entity/player/EntityPlayer;F)Lnet/minecraft/util/Vec3;",false));m.instructions.insertBefore(i,h);count++;
                }
            }
            if(target==2&&(m.name.equals("orientCamera")||m.name.equals("func_78467_g")))
                for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof LdcInsnNode&&Float.valueOf(1.62F).equals(((LdcInsnNode)i).cst)){
                    InsnList h=new InsnList();h.add(new VarInsnNode(Opcodes.ALOAD,2));h.add(new VarInsnNode(Opcodes.FLOAD,1));h.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"cameraHeight","(FLnet/minecraft/entity/EntityLivingBase;F)F",false));m.instructions.insert(i,h);count++;
                }
        }
        if(count==0)throw new IllegalStateException("Swimming pose hook missing: "+target);
        ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);n.accept(w);return w.toByteArray();
    }
}
