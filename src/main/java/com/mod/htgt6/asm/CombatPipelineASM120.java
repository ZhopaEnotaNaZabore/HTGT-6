package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
/** Hooks run before damage enters vanilla/Forge processing, never in LivingHurt. */
public final class CombatPipelineASM120 {
 private static final String ATTACK="com/mod/htgt6/minecraft1202/Module/Gameplay/Combat/CombatAttack120",DAMAGE="com/mod/htgt6/minecraft1202/Module/Gameplay/Combat/CombatDamage120";
 public static byte[] transform(byte[] bytes,int kind){
  ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int hooks=0;
  for(MethodNode m:node.methods){
   if(kind==0&&(m.name.equals("attackTargetEntityWithCurrentItem")||m.name.equals("func_71059_n"))&&m.desc.equals("(Lnet/minecraft/entity/Entity;)V")){
    clear(m);m.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));m.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));m.instructions.add(call(ATTACK,"attack","(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V"));m.instructions.add(new InsnNode(Opcodes.RETURN));hooks++;
   }
   if(kind==0||kind==1)for(AbstractInsnNode raw:m.instructions.toArray())if(raw instanceof MethodInsnNode){MethodInsnNode c=(MethodInsnNode)raw;
    if(c.getOpcode()==Opcodes.INVOKESTATIC&&c.owner.equals("net/minecraftforge/common/ISpecialArmor$ArmorProperties")&&c.name.equals("ApplyArmor")
      &&(c.desc.equals("(Lnet/minecraft/entity/EntityLivingBase;[Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/DamageSource;D)F")
       ||c.desc.equals("(Lnet/minecraft/entity/EntityLivingBase;[Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/DamageSource;DZ)F"))){c.owner=DAMAGE;hooks++;}
   }
   if(kind==1){
    if((m.name.equals("knockBack")||m.name.equals("func_70653_a"))&&m.desc.equals("(Lnet/minecraft/entity/Entity;FDD)V")){
     clear(m);m.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));m.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));m.instructions.add(new VarInsnNode(Opcodes.FLOAD,2));m.instructions.add(new VarInsnNode(Opcodes.DLOAD,3));m.instructions.add(new VarInsnNode(Opcodes.DLOAD,5));m.instructions.add(call(ATTACK,"hurtKnockback","(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/Entity;FDD)V"));m.instructions.add(new InsnNode(Opcodes.RETURN));hooks++;
    }
    if(m.name.equals("applyArmorCalculations")||m.name.equals("func_70655_b")){
     // Preserve original incoming damage in parameter 2; vanilla's final legacy product is discarded.
     for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof VarInsnNode&&i.getOpcode()==Opcodes.FSTORE&&((VarInsnNode)i).var==2)m.instructions.set(i,new InsnNode(Opcodes.POP));
    }
    if(m.name.equals("applyArmorCalculations")||m.name.equals("func_70655_b"))for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==Opcodes.FRETURN){
     InsnList h=new InsnList();h.add(new VarInsnNode(Opcodes.ALOAD,0));h.add(new VarInsnNode(Opcodes.ALOAD,1));h.add(new VarInsnNode(Opcodes.FLOAD,2));h.add(call(DAMAGE,"armorResult","(FLnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/util/DamageSource;F)F"));m.instructions.insertBefore(i,h);hooks++;}
    if(m.name.equals("applyEntityAttributes")||m.name.equals("func_110147_ax"))for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==Opcodes.RETURN){InsnList h=new InsnList();h.add(new VarInsnNode(Opcodes.ALOAD,0));h.add(call(ATTACK,"register","(Lnet/minecraft/entity/EntityLivingBase;)V"));m.instructions.insertBefore(i,h);hooks++;}
    if(m.name.equals("swingItem")||m.name.equals("func_71038_i")){InsnList h=new InsnList();h.add(new VarInsnNode(Opcodes.ALOAD,0));h.add(call(ATTACK,"swing","(Lnet/minecraft/entity/EntityLivingBase;)V"));m.instructions.insert(h);hooks++;}
   }
   if(kind==2&&(m.name.equals("getEnchantmentModifierDamage")||m.name.equals("func_77508_a"))){clear(m);m.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));m.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));m.instructions.add(call(DAMAGE,"protection",m.desc));m.instructions.add(new InsnNode(Opcodes.IRETURN));hooks++;}
   if(kind==4&&(m.name.equals("onItemRightClick")||m.name.equals("func_77659_a"))){clear(m);m.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));m.instructions.add(new InsnNode(Opcodes.ARETURN));hooks++;}
   if(kind==3&&m.name.equals("func_152376_a")){
    String field=null;for(FieldNode f:node.fields)if(f.desc.equals("I")&&(f.access&Opcodes.ACC_STATIC)==0){field=f.name;break;}
    if(field==null)throw new IllegalStateException("Enchantment damage type field missing");
    clear(m);m.instructions.add(new VarInsnNode(Opcodes.ILOAD,1));m.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));m.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,node.name,field,"I"));m.instructions.add(new VarInsnNode(Opcodes.ALOAD,2));m.instructions.add(call(DAMAGE,"sharpness","(IILnet/minecraft/entity/EnumCreatureAttribute;)F"));m.instructions.add(new InsnNode(Opcodes.FRETURN));hooks++;
   }
  }
  if(hooks==0)throw new IllegalStateException("Combat pipeline hook missing: "+node.name);
  ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(w);return w.toByteArray();
 }
 private static MethodInsnNode call(String owner,String name,String desc){return new MethodInsnNode(Opcodes.INVOKESTATIC,owner,name,desc,false);}
 private static void clear(MethodNode m){m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();}
 private CombatPipelineASM120(){}
}
