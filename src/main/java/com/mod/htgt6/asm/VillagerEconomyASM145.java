package com.mod.htgt6.asm;
import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
/** Managed economy and customer-price hooks; foreign professions retain vanilla ownership. */
public final class VillagerEconomyASM145 {
    private static final String HOOK="com/mod/htgt6/minecraft1202/Module/Gameplay/VillageAndPillage/VillagerEconomy145";
    public static byte[] transform(byte[] bytes){
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int trade=0,ai=0,customer=0;boolean changed=false;
        for(MethodNode m:node.methods){
            if(m.desc.equals("(Lnet/minecraft/village/MerchantRecipe;)V")&&(m.name.equals("useRecipe")||m.name.equals("func_70933_a"))){
                trade++;if(!has(m,"onTrade")){atReturns(m,"onTrade","Lnet/minecraft/village/MerchantRecipe;");changed=true;}
            }else if(m.desc.equals("()V")&&(m.name.equals("updateAITick")||m.name.equals("func_70629_bd"))){
                ai++;if(!has(m,"beforeAi")){InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"beforeAi","(Lnet/minecraft/entity/passive/EntityVillager;)V",false));m.instructions.insert(code);changed=true;}
            }else if(m.desc.equals("(Lnet/minecraft/entity/player/EntityPlayer;)V")&&(m.name.equals("setCustomer")||m.name.equals("func_70932_a_"))){
                customer++;if(!has(m,"onCustomer")){atReturns(m,"onCustomer","Lnet/minecraft/entity/player/EntityPlayer;");changed=true;}
            }
        }
        if(trade!=1||ai!=1||customer!=1)throw new IllegalStateException("Villager economy hook drift: trade="+trade+", ai="+ai+", customer="+customer);
        if(!changed)return bytes;ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
    }
    private static boolean has(MethodNode m,String name){for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(HOOK)&&((MethodInsnNode)i).name.equals(name))return true;return false;}
    private static void atReturns(MethodNode m,String name,String argument){for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==Opcodes.RETURN){InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));code.add(new VarInsnNode(Opcodes.ALOAD,1));code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,name,"(Lnet/minecraft/entity/passive/EntityVillager;"+argument+")V",false));m.instructions.insertBefore(i,code);}}
    private VillagerEconomyASM145(){}
}
