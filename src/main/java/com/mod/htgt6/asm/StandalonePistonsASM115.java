package com.mod.htgt6.asm;
import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
/** Leaves the piston renderer and persistence format vanilla; replaces only movement decisions. */
public final class StandalonePistonsASM115 {
 public static final String HOOK="com/mod/htgt6/minecraft1202/Module/Gameplay/Modern/StandalonePistons115";
 public static byte[] transform(byte[] bytes,boolean tile){ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int found=0;boolean changed=false;
  for(MethodNode m:node.methods){
   boolean update=!tile&&(m.name.equals("updatePistonState")||m.name.equals("func_150078_e"))&&m.desc.equals("(Lnet/minecraft/world/World;III)V");
   boolean event=!tile&&(m.name.equals("onBlockEventReceived")||m.name.equals("func_149696_a"))&&m.desc.equals("(Lnet/minecraft/world/World;IIIII)Z");
   boolean tick=tile&&(m.name.equals("updateEntity")||m.name.equals("func_145845_h"))&&m.desc.equals("()V");if(!update&&!event&&!tick)continue;found++;
   boolean installed=false;for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(HOOK))installed=true;if(installed)continue;
   InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));
   if(tick){code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,"tick","(Lnet/minecraft/tileentity/TileEntityPiston;)V",false));m.instructions.insert(code);}
   else {code.add(new VarInsnNode(Opcodes.ALOAD,1));for(int local=2;local<=(event?6:4);local++)code.add(new VarInsnNode(Opcodes.ILOAD,local));code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,HOOK,event?"event":"update",event?"(Lnet/minecraft/block/BlockPistonBase;Lnet/minecraft/world/World;IIIII)Z":"(Lnet/minecraft/block/BlockPistonBase;Lnet/minecraft/world/World;III)V",false));code.add(new InsnNode(event?Opcodes.IRETURN:Opcodes.RETURN));m.instructions.clear();m.instructions.add(code);m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();}
   changed=true;
  }
  if(found!=(tile?1:2))throw new IllegalStateException("Standalone piston hook drift: "+found);if(!changed)return bytes;ClassWriter out=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(out);return out.toByteArray();
 }
}
