import com.mod.htgt6.asm.HTGT6Transformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import java.util.*;
import java.util.zip.*;
import java.lang.reflect.*;
public class GTWater130Fixture {
 static int checks;
 static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);checks++;}
 static byte[] bytes(ZipFile z,String path)throws Exception {java.io.InputStream in=z.getInputStream(z.getEntry(path));java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);in.close();return out.toByteArray();}
 static ClassNode read(byte[] b){ClassNode c=new ClassNode();new ClassReader(b).accept(c,0);return c;}
 static byte[] write(ClassNode c){ClassWriter w=new ClassWriter(0);c.accept(w);return w.toByteArray();}
 static final Map<String,String> N=new HashMap<String,String>();
 static {N.put("func_149695_a","onNeighborBlockChange");N.put("func_149726_b","onBlockAdded");N.put("func_149674_a","updateTick");N.put("func_149741_i","getRenderColor");N.put("func_149720_d","colorMultiplier");}
 public static void main(String[] args)throws Exception {
  Method patch=HTGT6Transformer.class.getDeclaredMethod("transformGTWaterBlock",byte[].class,String.class);patch.setAccessible(true);
  ZipFile z=new ZipFile("libs/gregtech_1.7.10-6.15.08-HiTech6_Edition.jar");
  for(boolean mcp:new boolean[]{false,true})for(String name:new String[]{"BlockWaterlike","BlockOcean","BlockRiver","BlockSwamp"}) {
   String owner="gregtech/blocks/fluids/"+name;ClassNode original=read(bytes(z,owner+".class"));
   if(mcp) for(MethodNode m:original.methods) {if(N.containsKey(m.name))m.name=N.get(m.name);else if(m.name.startsWith("func_"))m.name="mcp_"+m.name;}
   byte[] result=(byte[])patch.invoke(null,write(original),owner);ClassNode c=read(result);
   Set<String> signatures=new HashSet<String>();int noops=0;
   for(MethodNode m:c.methods){check(signatures.add(m.name+m.desc),"no duplicate method");
    boolean callback=N.containsKey(m.name)||N.containsValue(m.name);
    if((callback&&m.desc.endsWith(")V"))||m.name.equals("updateFlow")){
     int count=0;for(AbstractInsnNode n:m.instructions.toArray())if(n.getOpcode()>=0){count++;check(n.getOpcode()==Opcodes.RETURN,"no callback side effects "+owner+"."+m.name);}
     check(count==1,"single return");new Analyzer(new BasicVerifier()).analyze(c.name,m);noops++;
    }
   }
   check(noops>=(name.equals("BlockWaterlike")?4:name.equals("BlockSwamp")?2:3),"all declared/inherited callbacks sealed");
   ClassNode twice=read((byte[])patch.invoke(null,result,owner));check(twice.methods.size()==c.methods.size(),"idempotent method set");
   for(String keep:new String[]{"drain","canDrain","getFlowVector","onHeadInside"}) {
    MethodNode before=null,after=null;for(MethodNode m:original.methods)if(m.name.equals(keep))before=m;for(MethodNode m:c.methods)if(m.name.equals(keep))after=m;
    if(before!=null)check(after!=null&&before.instructions.size()==after.instructions.size(),"preserved "+keep);
   }
  }
  z.close();
  ClassNode transformer=read(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("build/gtwater130/classes/com/mod/htgt6/asm/HTGT6Transformer.class")));
  for(MethodNode m:transformer.methods)if(m.name.equals("generateGTWaterInBiomeArray"))for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)n;check(!call.owner.equals("net/minecraft/world/World"),"raw composition never calls World");}
  System.out.println("PASS GTWater130: "+checks);
 }
}
