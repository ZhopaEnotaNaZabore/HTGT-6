import java.util.*;import java.util.zip.*;import java.io.*;import java.lang.reflect.*;
import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import org.objectweb.asm.tree.analysis.*;
public class Conduit138ASMFixture {
 static int checks;
 public static void main(String[] a)throws Exception{
  Method transform=Class.forName("com.mod.htgt6.asm.ConduitPowerASM").getDeclaredMethod("transform",byte[].class,boolean.class);transform.setAccessible(true);
  try(ZipFile zip=new ZipFile(a[0])){for(boolean client:new boolean[]{false,true})for(boolean srg:new boolean[]{false,true}){
   String name=client?"net/minecraft/client/renderer/EntityRenderer":"net/minecraft/entity/EntityLivingBase";
   ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=zip.getInputStream(zip.getEntry(name+".class"))){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}
   ClassNode source=new ClassNode();new ClassReader(out.toByteArray()).accept(source,0);
   Map<String,String> map=new HashMap<String,String>();map.put("onEntityUpdate","func_70030_z");map.put("getNightVisionBrightness","func_82830_a");map.put("isPotionActive","func_70644_a");map.put("nightVision","field_76439_r");map.put("waterBreathing","field_76427_o");
   if(srg)for(MethodNode m:source.methods){if(m.name.equals("isPotionActive"))m.name=m.desc.equals("(I)Z")?"func_82165_m":"func_70644_a";else if(map.containsKey(m.name))m.name=map.get(m.name);for(AbstractInsnNode i:m.instructions.toArray()){if(i instanceof MethodInsnNode){MethodInsnNode c=(MethodInsnNode)i;if(c.name.equals("isPotionActive"))c.name=c.desc.equals("(I)Z")?"func_82165_m":"func_70644_a";else if(map.containsKey(c.name))c.name=map.get(c.name);}if(i instanceof FieldInsnNode){FieldInsnNode f=(FieldInsnNode)i;if(map.containsKey(f.name))f.name=map.get(f.name);}}}
   ClassWriter w=new ClassWriter(0);source.accept(w);byte[] patched=(byte[])transform.invoke(null,w.toByteArray(),client);
   check(Arrays.equals(patched,(byte[])transform.invoke(null,patched,client)),"idempotent");
   ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);int hooks=0;
   for(MethodNode m:node.methods){boolean modified=false;for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.endsWith("/ConduitPowerHooks120")){hooks++;modified=true;}
    if(modified){new Analyzer(new BasicVerifier()).analyze(node.name,m);checks++;}}
   check(hooks==(client?3:1),"specific anchors only");
  }}System.out.println("PASS Conduit138 ASM: "+checks+" checks");
 }
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
}
