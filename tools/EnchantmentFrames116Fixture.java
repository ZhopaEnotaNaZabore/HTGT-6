import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import com.mod.htgt6.asm.NetheriteHoeEnchantmentASM116;
public class EnchantmentFrames116Fixture {
 public static void main(String[] args) {
  int checks=0;
  for(int version:new int[]{Opcodes.V1_5,Opcodes.V1_6,Opcodes.V1_8})for(String name:new String[]{"canApply","func_92089_a"})for(boolean branch:new boolean[]{false,true}) {
   ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_FRAMES|ClassWriter.COMPUTE_MAXS);
   writer.visit(version,Opcodes.ACC_PUBLIC,"net/minecraft/enchantment/Enchantment",null,"java/lang/Object",null);
   MethodVisitor m=writer.visitMethod(Opcodes.ACC_PUBLIC,name,"(Lnet/minecraft/item/ItemStack;)Z",null,null);m.visitCode();
   if(branch){Label other=new Label();m.visitVarInsn(Opcodes.ALOAD,1);m.visitJumpInsn(Opcodes.IFNULL,other);m.visitInsn(Opcodes.ICONST_1);m.visitInsn(Opcodes.IRETURN);m.visitLabel(other);}
   m.visitInsn(Opcodes.ICONST_0);m.visitInsn(Opcodes.IRETURN);m.visitMaxs(0,0);m.visitEnd();writer.visitEnd();
   byte[] input=writer.toByteArray();new ClassReader(input).accept(new ClassNode(),ClassReader.EXPAND_FRAMES);
   byte[] output=NetheriteHoeEnchantmentASM116.transform(input);
   try {for(int flags:new int[]{0,ClassReader.EXPAND_FRAMES})new ClassReader(output).accept(new ClassNode(),flags);}
   catch(Throwable t){throw new AssertionError("Frame corruption: version="+version+" method="+name+" branch="+branch,t);}
   if(!java.util.Arrays.equals(output,NetheriteHoeEnchantmentASM116.transform(output)))throw new AssertionError("Not idempotent");
   checks++;
  }
  System.out.println("Enchantment frame compatibility: "+checks+" cases passed");
 }
}
