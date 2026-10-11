package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public final class NativeWallASM116 {
    public static byte[] transform(byte[] bytes){ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);boolean srg=false;for(MethodNode m:node.methods)if(m.name.equals("func_149668_a"))srg=true;
        String hook="com/mod/htgt6/minecraft1202/Module/Gameplay/Modern/NativeWall116";
        for(MethodNode m:node.methods)for(AbstractInsnNode insn:m.instructions.toArray())if(insn instanceof MethodInsnNode&&((MethodInsnNode)insn).owner.equals(hook))return bytes;
        if(!node.interfaces.contains("com/mod/htgt6/minecraft1202/Module/Gameplay/Shared/IWaterloggedBlock150"))node.interfaces.add("com/mod/htgt6/minecraft1202/Module/Gameplay/Shared/IWaterloggedBlock150");
        for(MethodNode m:node.methods)if((m.name.equals("getIcon")||m.name.equals("func_149691_a"))&&m.desc.equals("(II)Lnet/minecraft/util/IIcon;")){InsnList mask=new InsnList();mask.add(new VarInsnNode(Opcodes.ILOAD,2));mask.add(new VarInsnNode(Opcodes.ALOAD,0));mask.add(new MethodInsnNode(Opcodes.INVOKESTATIC,hook,"iconMetadata","(ILnet/minecraft/block/BlockWall;)I",false));mask.add(new VarInsnNode(Opcodes.ISTORE,2));m.instructions.insert(mask);}
        add(node,"isWaterlogged","(Lnet/minecraft/world/IBlockAccess;III)Z","wet");
        add(node,"setWaterlogged","(Lnet/minecraft/world/World;IIIZ)Z","water");
        add(node,srg?"func_149660_a":"onBlockPlaced","(Lnet/minecraft/world/World;IIIIFFFI)I","placed");
        add(node,srg?"func_149692_a":"damageDropped","(I)I","dropped");
        add(node,srg?"func_149643_k":"getDamageValue","(Lnet/minecraft/world/World;III)I","picked");
        add(node,"getLightOpacity","(Lnet/minecraft/world/IBlockAccess;III)I","opacity");
        add(node,srg?"func_149727_a":"onBlockActivated","(Lnet/minecraft/world/World;IIILnet/minecraft/entity/player/EntityPlayer;IFFF)Z","activate");
        add(node,"removedByPlayer","(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;IIIZ)Z","remove");
        add(node,"onBlockExploded","(Lnet/minecraft/world/World;IIILnet/minecraft/world/Explosion;)V","explode");
        add(node,"getExplosionResistance","(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;IIIDDD)F","resistance");
        boolean client=false;for(MethodNode m:node.methods)if(m.name.equals("getIcon")||m.name.equals("func_149691_a"))client=true;
        if(client){add(node,srg?"func_149701_w":"getRenderBlockPass","()I","renderPass");add(node,"canRenderInPass","(I)Z","clientPass");}
        add(node,srg?"func_149743_a":"addCollisionBoxesToList","(Lnet/minecraft/world/World;IIILnet/minecraft/util/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;)V","boxes");
        add(node,srg?"func_149668_a":"getCollisionBoundingBoxFromPool","(Lnet/minecraft/world/World;III)Lnet/minecraft/util/AxisAlignedBB;","bounds");
        add(node,srg?"func_149719_a":"setBlockBoundsBasedOnState","(Lnet/minecraft/world/IBlockAccess;III)V","selection");
        add(node,srg?"func_149731_a":"collisionRayTrace","(Lnet/minecraft/world/World;IIILnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;)Lnet/minecraft/util/MovingObjectPosition;","ray");
        add(node,srg?"func_149645_b":"getRenderType","()I","render");
        add(node,srg?"func_149695_a":"onNeighborBlockChange","(Lnet/minecraft/world/World;IIILnet/minecraft/block/Block;)V","neighbor");
        ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);return out.toByteArray();
    }
    private static void add(ClassNode node,String name,String desc,String hook){
        MethodNode method=null;for(MethodNode m:node.methods)if(m.name.equals(name)&&m.desc.equals(desc)){method=m;break;}
        boolean ownOnly=!hook.equals("wet")&&!hook.equals("water");String fallbackOwner=node.superName,fallbackName=name;
        if(method==null){method=new MethodNode(Opcodes.ACC_PUBLIC,name,desc,null,null);node.methods.add(method);}
        else{
            if(ownOnly){fallbackOwner=node.name;fallbackName="htgt6$legacy$"+name;MethodNode saved=new MethodNode(Opcodes.ACC_PRIVATE,fallbackName,desc,null,null);method.accept(saved);saved.name=fallbackName;saved.access=Opcodes.ACC_PRIVATE;node.methods.add(saved);}
            method.instructions.clear();method.tryCatchBlocks.clear();if(method.localVariables!=null)method.localVariables.clear();
        }
        LabelNode fallback=new LabelNode();
        if(ownOnly){method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/mod/htgt6/minecraft1202/Module/Gameplay/Modern/NativeWall116","isNative","(Lnet/minecraft/block/BlockWall;)Z",false));method.instructions.add(new JumpInsnNode(Opcodes.IFEQ,fallback));}
        args(method,desc);
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/mod/htgt6/minecraft1202/Module/Gameplay/Modern/NativeWall116",hook,"(Lnet/minecraft/block/BlockWall;"+desc.substring(1),false));method.instructions.add(new InsnNode(Type.getReturnType(desc).getOpcode(Opcodes.IRETURN)));
        if(ownOnly){method.instructions.add(fallback);method.instructions.add(new FrameNode(Opcodes.F_SAME,0,null,0,null));args(method,desc);method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,fallbackOwner,fallbackName,desc,false));method.instructions.add(new InsnNode(Type.getReturnType(desc).getOpcode(Opcodes.IRETURN)));}
    }
    private static void args(MethodNode method,String desc){method.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));int slot=1;for(Type t:Type.getArgumentTypes(desc)){method.instructions.add(new VarInsnNode(t.getOpcode(Opcodes.ILOAD),slot));slot+=t.getSize();}}
    private NativeWallASM116(){}
}
