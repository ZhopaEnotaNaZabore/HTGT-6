import java.net.*;import java.io.*;
public final class Piercing142Launcher {
 public static void main(String[] args)throws Exception {
  String[] paths=System.getProperty("java.class.path").split(java.util.regex.Pattern.quote(File.pathSeparator));URL[] urls=new URL[paths.length];for(int i=0;i<paths.length;i++)urls[i]=new File(paths[i]).toURI().toURL();
  URLClassLoader loader=new URLClassLoader(urls,null){protected Class<?> findClass(String name)throws ClassNotFoundException{
   if(!name.equals("net.minecraft.entity.projectile.EntityArrow")&&!name.equals("net.minecraftforge.event.entity.living.LivingAttackEvent"))return super.findClass(name);
   try{InputStream in=getResourceAsStream(name.replace('.','/')+".class");ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=in.read(buf))>=0)out.write(buf,0,n);in.close();
    if(name.equals("net.minecraftforge.event.entity.living.LivingAttackEvent")){
     // Standalone Forge sources lack the EventSubscriptionTransformer's @Cancelable method.
     org.objectweb.asm.tree.ClassNode node=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(out.toByteArray()).accept(node,0);org.objectweb.asm.tree.MethodNode cancel=new org.objectweb.asm.tree.MethodNode(org.objectweb.asm.Opcodes.ACC_PUBLIC,"isCancelable","()Z",null,null);cancel.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.ICONST_1));cancel.instructions.add(new org.objectweb.asm.tree.InsnNode(org.objectweb.asm.Opcodes.IRETURN));node.methods.add(cancel);org.objectweb.asm.ClassWriter writer=new org.objectweb.asm.ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS);node.accept(writer);byte[] event=writer.toByteArray();return defineClass(name,event,0,event.length);
    }
    Class<?> transformer=Class.forName("com.mod.htgt6.asm.HTGT6Transformer",true,Piercing142Launcher.class.getClassLoader());java.lang.reflect.Method method=transformer.getDeclaredMethod("transformFrostburnArrow120",byte[].class);method.setAccessible(true);byte[] bytes=(byte[])method.invoke(null,(Object)out.toByteArray());return defineClass(name,bytes,0,bytes.length);
   }catch(Exception e){throw new ClassNotFoundException(name,e);}
  }};
  Thread.currentThread().setContextClassLoader(loader);
  try{loader.loadClass("com.mod.htgt6.minecraft1202.Module.Data.Bountiful.Piercing142Fixture").getMethod("main",String[].class).invoke(null,(Object)args);}catch(java.lang.reflect.InvocationTargetException e){throw new RuntimeException(e.getCause());}
 }
}

