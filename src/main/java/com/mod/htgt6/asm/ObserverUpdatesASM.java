package com.mod.htgt6.asm;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
/** Preserve update coordinates, which the 1.7 Block neighbor callback discards. */
final class ObserverUpdatesASM {
 static byte[] transform(byte[] bytes){
  ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
  boolean forge=false;for(MethodNode m:node.methods)if(m.name.equals("markAndNotifyBlock"))forge=true;
  int count=0;
  for(MethodNode m:node.methods){
   boolean notify=m.name.equals("markAndNotifyBlock")&&m.desc.equals("(IIILnet/minecraft/world/chunk/Chunk;Lnet/minecraft/block/Block;Lnet/minecraft/block/Block;I)V");
   boolean metadata=(m.name.equals("setBlockMetadataWithNotify")||m.name.equals("func_72921_c"))&&m.desc.equals("(IIIII)Z");
   boolean block=!forge&&(m.name.equals("setBlock")||m.name.equals("func_147465_d"))&&m.desc.equals("(IIILnet/minecraft/block/Block;II)Z");
   if(!notify&&!metadata&&!block)continue;
   count++;
   for(AbstractInsnNode n:m.instructions.toArray())if(n.getOpcode()==(notify?Opcodes.RETURN:Opcodes.IRETURN)){
    InsnList hook=new InsnList();hook.add(new VarInsnNode(Opcodes.ALOAD,0));
    for(int i=1;i<=3;i++)hook.add(new VarInsnNode(Opcodes.ILOAD,i));
    hook.add(new VarInsnNode(Opcodes.ILOAD,notify?7:metadata?5:6));
    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/mod/htgt6/minecraft1202/Module/Gameplay/Exploration/ObserverUpdates120",
      notify?"notifyChange":"afterChange",notify?"(Lnet/minecraft/world/World;IIII)V":"(ZLnet/minecraft/world/World;IIII)Z",false));
    m.instructions.insertBefore(n,hook);
   }
  }
  if(count!=2)throw new IllegalStateException("Observer update anchors: "+count);
  ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);node.accept(writer);return writer.toByteArray();
 }
}
