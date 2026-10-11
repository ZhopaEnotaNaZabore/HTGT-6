import com.mod.htgt6.minecraft1202.Module.Client.Combat.*;
import net.minecraft.client.model.*;
import java.lang.reflect.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public final class DragonHead125Fixture {
 public static void main(String[] args)throws Exception{
  ModelDragonHead120 model=new ModelDragonHead120();Field h=ModelDragonHead120.class.getDeclaredField("head"),j=ModelDragonHead120.class.getDeclaredField("jaw");h.setAccessible(true);j.setAccessible(true);
  ModelRenderer head=(ModelRenderer)h.get(model),jaw=(ModelRenderer)j.get(model);
  check(model.textureWidth==256&&model.textureHeight==256,"atlas");
  check(head.cubeList.size()==6&&jaw.cubeList.size()==1,"seven cubes");
  check(head.childModels.contains(jaw),"jaw inherits head rotation");check(jaw.rotationPointY==4&&jaw.rotationPointZ==-8,"jaw pivot");
  ModelBox lip=(ModelBox)head.cubeList.get(0);check(lip.posZ1==-24&&lip.posZ2==-8,"snout forward extent");
  ModelBox leftNostril=(ModelBox)head.cubeList.get(3),rightNostril=(ModelBox)head.cubeList.get(5);
  check(leftNostril.posZ1==-22&&rightNostril.posZ1==-22,"nostrils above muzzle");
  ClassNode node=new ClassNode();new ClassReader(RenderDragonHead120.class.getName()).accept(node,0);int manager=0,unsafe=0,finallyBlocks=0;
  for(MethodNode m:node.methods)if(m.name.equals("renderModel")){
   finallyBlocks=m.tryCatchBlocks.size();
   for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode){MethodInsnNode c=(MethodInsnNode)i;
    if(c.name.equals("bindTexture")){if(c.owner.equals("net/minecraft/client/renderer/texture/TextureManager"))manager++;else unsafe++;}
   }
  }
  check(manager==1&&unsafe==0,"texture binding independent of TESR dispatcher");check(finallyBlocks>=2,"matrix and render state protected on failure");
  System.out.println("PASS dragon125: geometry, hierarchy, dispatcher-independent texture binding and cleanup");
 }
 static void check(boolean value,String name){if(!value)throw new AssertionError(name);}
}
