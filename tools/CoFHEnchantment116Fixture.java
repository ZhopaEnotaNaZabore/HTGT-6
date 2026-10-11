import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import com.mod.htgt6.asm.NetheriteHoeEnchantmentASM116;
/** Exercises the installed CoFH method's actual bytecode, without changing its jar or bootstrapping CoFH. */
public final class CoFHEnchantment116Fixture {
 static final String SHIM="fixture/CoFHEnchantmentShim";
 static int checks;
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static byte[] read(InputStream in)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);in.close();return out.toByteArray();}
 static ClassNode node(byte[] b){ClassNode n=new ClassNode();new ClassReader(b).accept(n,0);return n;}
 static int frames(ClassNode n,String name){for(MethodNode m:n.methods)if(m.name.equals(name)){int count=0;for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof FrameNode)count++;return count;}throw new AssertionError(name);}
 static class Loader extends ClassLoader {Class<?> define(byte[] b){return defineClass(SHIM.replace('/','.'),b,0,b.length);}}
 public static void main(String[] args)throws Exception {
  byte[] cofh;try(ZipFile z=new ZipFile(args[0])){cofh=read(z.getInputStream(z.getEntry("cofh/asm/ASMCore.class")));}
  ClassNode shim=new ClassNode();shim.version=Opcodes.V1_8;shim.access=Opcodes.ACC_PUBLIC;shim.name=SHIM;shim.superName="java/lang/Object";
  shim.fields.add(new FieldNode(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC,"runtimeDeobfEnabled","Z",null,null));
  for(MethodNode m:node(cofh).methods)if(m.name.equals("alterEnchantment")) {
   m.access=Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC;
   for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof FieldInsnNode){FieldInsnNode f=(FieldInsnNode)i;if(f.owner.equals("cofh/asm/LoadingPlugin")&&f.name.equals("runtimeDeobfEnabled"))f.owner=SHIM;}
   shim.methods.add(m);
  }
  check(shim.methods.size()==1,"exact installed CoFH method");ClassWriter sw=new ClassWriter(0);shim.accept(sw);Class<?> type=new Loader().define(sw.toByteArray());
  java.lang.reflect.Method transform=type.getMethod("alterEnchantment",String.class,byte[].class,ClassReader.class);
  byte[] original=read(CoFHEnchantment116Fixture.class.getClassLoader().getResourceAsStream("net/minecraft/enchantment/Enchantment.class"));
  for(boolean srg:new boolean[]{false,true}) {
   ClassNode base=node(original);String method=srg?"func_92089_a":"canApply";
   if(srg)for(MethodNode m:base.methods)if(m.name.equals("canApply"))m.name=method;
   ClassWriter bw=new ClassWriter(0);base.accept(bw);byte[] input=bw.toByteArray();
   byte[] patched=NetheriteHoeEnchantmentASM116.transform(input);
   check(frames(node(patched),method)==frames(base,method),"hoe hook adds no control-flow frames");
   check(Arrays.equals(patched,NetheriteHoeEnchantmentASM116.transform(patched)),"hook idempotence");
   type.getField("runtimeDeobfEnabled").setBoolean(null,srg);
   byte[] combined=(byte[])transform.invoke(null,"net.minecraft.enchantment.Enchantment",patched,new ClassReader(patched));
   ClassNode result=node(combined);new ClassReader(combined).accept(new ClassNode(),ClassReader.EXPAND_FRAMES);checks+=2;
   boolean cofhHook=false,hoeHook=false;
   for(MethodNode m:result.methods)if(m.name.equals(method)) {
    new Analyzer(new BasicVerifier()).analyze(result.name,m);checks++;
    for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode){String n=((MethodInsnNode)i).name;cofhHook|=n.equals("cofh_canEnchantApply");hoeHook|=n.equals("allowsEnchantment");}
   }
   check(cofhHook&&hoeHook,"both CoFH override and hoe support retained");
  }
  System.out.println("CoFH + Backport actual enchantment chain: "+checks+" checks passed");
 }
}
