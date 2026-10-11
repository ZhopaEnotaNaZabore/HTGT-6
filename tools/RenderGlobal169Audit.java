import java.io.*;import java.nio.file.*;import java.util.*;import java.util.zip.*;
import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import org.objectweb.asm.util.*;import org.objectweb.asm.tree.analysis.*;
import com.mod.htgt6.asm.HTGT6Transformer;
public final class RenderGlobal169Audit {
 static ClassNode read(byte[] b){ClassNode n=new ClassNode();new ClassReader(b).accept(n,ClassReader.SKIP_FRAMES);return n;}
 static String trace(MethodNode m){Textifier text=new Textifier();m.accept(new TraceMethodVisitor(text));StringWriter s=new StringWriter();text.print(new PrintWriter(s));return s.toString().replaceAll("(?m)^.*MAX(?:STACK|LOCALS).*(?:\\r?\\n)?","");}
 public static void main(String[] args)throws Exception{
  String name="net/minecraft/client/renderer/RenderGlobal";byte[] input;
  try(ZipFile zip=new ZipFile(args[0]);InputStream in=zip.getInputStream(zip.getEntry(name+".class"));ByteArrayOutputStream out=new ByteArrayOutputStream()){
   byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);input=out.toByteArray();
  }
  byte[] output=new HTGT6Transformer().transform(name,name,input);
  ClassNode before=read(input),after=read(output);Map<String,String> baseline=new HashMap<>();
  for(MethodNode m:before.methods)baseline.put(m.name+m.desc,trace(m));
  int changed=0;
  for(MethodNode m:after.methods)if(!trace(m).equals(baseline.get(m.name+m.desc))){
   System.out.println("CHANGED "+m.name+m.desc);new Analyzer(new BasicVerifier()).analyze(after.name,m);changed++;
   if(!m.name.equals("<init>")&&!m.name.equals("loadRenderers")&&!m.name.equals("playRecord"))throw new AssertionError("Unexpected method changed");
  }
  if(changed!=3||before.methods.size()!=after.methods.size())throw new AssertionError("Unexpected transform scope");
  System.out.println("PASS: exactly 3 changed methods; all other "+(before.methods.size()-changed)+" methods unchanged; BasicVerifier passed");
  if(!Arrays.equals(output,new HTGT6Transformer().transform(name,name,output)))throw new AssertionError("Repeated transform changed bytes");
  for(boolean srg:new boolean[]{false,true}){
   ClassNode decorated=read(input);
   for(MethodNode m:decorated.methods){
    if(srg){if(m.name.equals("loadRenderers"))m.name="func_72712_a";if(m.name.equals("playRecord"))m.name="func_72702_a";}
    for(AbstractInsnNode ins:m.instructions.toArray()){
     if(ins instanceof IntInsnNode&&((IntInsnNode)ins).operand==16){LabelNode label=new LabelNode();InsnList marker=new InsnList();marker.add(label);marker.add(new LineNumberNode(12345,label));m.instructions.insert(ins,marker);}
     if(srg&&ins instanceof FieldInsnNode&&((FieldInsnNode)ins).name.equals("renderChunksTall"))((FieldInsnNode)ins).name="field_72763_n";
    }
   }
   if(srg)for(FieldNode f:decorated.fields)if(f.name.equals("renderChunksTall"))f.name="field_72763_n";
   ClassWriter writer=new ClassWriter(0);decorated.accept(writer);
   byte[] first=new HTGT6Transformer().transform(name,name,writer.toByteArray());
   byte[] second=new HTGT6Transformer().transform(name,name,first);
   if(!Arrays.equals(first,second))throw new AssertionError("Not idempotent: "+srg);
   ClassNode result=read(first);
   for(MethodNode m:result.methods)if(m.name.equals("<init>")||m.name.equals("loadRenderers")||m.name.equals("func_72712_a")||m.name.equals("playRecord")||m.name.equals("func_72702_a"))new Analyzer(new BasicVerifier()).analyze(name,m);
   System.out.println("PASS repeated transform + labels/lines + verifier, SRG="+srg);
  }
 }
}
