import java.net.*;import java.io.*;
public final class Horse151Launcher {
 public static void main(String[] args)throws Exception {
  String[] paths=System.getProperty("java.class.path").split(java.util.regex.Pattern.quote(File.pathSeparator));URL[] urls=new URL[paths.length];for(int i=0;i<paths.length;i++)urls[i]=new File(paths[i]).toURI().toURL();
  URLClassLoader loader=new URLClassLoader(urls,null){protected Class<?> findClass(String name)throws ClassNotFoundException{
   if(!name.equals("net.minecraft.entity.passive.EntityHorse"))return super.findClass(name);
   try{InputStream in=getResourceAsStream(name.replace('.','/')+".class");ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=in.read(buf))>=0)out.write(buf,0,n);in.close();
    byte[] bytes=com.mod.htgt6.asm.LeatherHorseArmorASM151.transform(out.toByteArray());return defineClass(name,bytes,0,bytes.length);
   }catch(Exception e){throw new ClassNotFoundException(name,e);}
  }};
  Thread.currentThread().setContextClassLoader(loader);
  try{loader.loadClass("com.mod.htgt6.minecraft1202.Module.Data.Bountiful.Horse151Fixture").getMethod("main",String[].class).invoke(null,(Object)args);}catch(java.lang.reflect.InvocationTargetException e){throw new RuntimeException(e.getCause());}
 }
}

