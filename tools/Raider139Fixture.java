import com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.*;
import net.minecraft.client.model.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Headless mesh contract against the independently inspected 1.20.2 layer definitions. */
public final class Raider139Fixture {
 static int checks;
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static void eq(float a,float b,String s){check(Math.abs(a-b)<0.00001F,s+": "+a+" != "+b);}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static ModelRenderer part(Object m,String n)throws Exception{return (ModelRenderer)field(m.getClass(),n).get(m);}
 static ModelBox cube(ModelRenderer p,int i){return (ModelBox)p.cubeList.get(i);}
 static void box(ModelRenderer p,int i,float x,float y,float z,int w,int h,int d){
  ModelBox b=cube(p,i);eq(b.posX1,x,"box x");eq(b.posY1,y,"box y");eq(b.posZ1,z,"box z");
  eq(b.posX2-b.posX1,w,"box width");eq(b.posY2-b.posY1,h,"box height");eq(b.posZ2-b.posZ1,d,"box depth");
 }
 static void pivot(ModelRenderer p,float x,float y,float z){eq(p.rotationPointX,x,"pivot x");eq(p.rotationPointY,y,"pivot y");eq(p.rotationPointZ,z,"pivot z");}
 static void uv(ModelRenderer p,int index,int u,int v,int w,int h,int d)throws Exception {
  TexturedQuad[] q=(TexturedQuad[])field(ModelBox.class,"quadList").get(cube(p,index));
  float minU=1,maxU=0,minV=1,maxV=0;
  for(TexturedQuad face:q)for(PositionTextureVertex vert:face.vertexPositions){
   minU=Math.min(minU,vert.texturePositionX);maxU=Math.max(maxU,vert.texturePositionX);
   minV=Math.min(minV,vert.texturePositionY);maxV=Math.max(maxV,vert.texturePositionY);
  }
  eq(minU,u/p.textureWidth,"UV min u");eq(maxU,(u+2*w+2*d)/p.textureWidth,"UV max u");
  eq(minV,v/p.textureHeight,"UV min v");eq(maxV,(v+d+h)/p.textureHeight,"UV max v");
 }
 public static void main(String[] args)throws Exception {
  ModelRavager r=new ModelRavager();check(r.textureWidth==128&&r.textureHeight==128,"ravager atlas");
  ModelRenderer neck=part(r,"neck"),head=part(r,"head"),mouth=part(r,"mouth"),body=part(r,"body");
  check(neck.childModels.contains(head)&&head.childModels.contains(mouth),"neck head jaw tree");
  box(neck,0,-5,-1,-18,10,10,18);pivot(neck,0,-7,5.5F);uv(neck,0,68,73,10,10,18);
  box(head,0,-8,-20,-14,16,20,16);box(head,1,-2,-6,-18,4,8,4);pivot(head,0,16,-17);uv(head,0,0,0,16,20,16);uv(head,1,0,0,4,8,4);
  check(head.childModels.size()==3,"two horns and jaw");
  for(int i=0;i<2;i++){
   ModelRenderer horn=(ModelRenderer)head.childModels.get(i);box(horn,0,0,-14,-2,2,14,4);
   pivot(horn,i==0?-10:8,-14,-8);eq(horn.rotateAngleX,1.0995574F,"horn tilt");check(horn.mirror==(i==1),"horn mirror");uv(horn,0,74,55,2,14,4);
  }
  box(mouth,0,-8,0,-16,16,3,16);pivot(mouth,0,-2,2);uv(mouth,0,0,36,16,3,16);
  box(body,0,-7,-10,-7,14,16,20);box(body,1,-6,6,-7,12,13,18);pivot(body,0,1,2);eq(body.rotateAngleX,(float)Math.PI/2,"body tilt");uv(body,0,0,55,14,16,20);uv(body,1,0,91,12,13,18);
  ModelRenderer[] legs=(ModelRenderer[])field(r.getClass(),"legs").get(r);
  for(int i=0;i<4;i++){
   box(legs[i],0,-4,0,-4,8,37,8);pivot(legs[i],i%2==0?-8:8,-13,i<2?18:-5);
   check(legs[i].mirror==(i%2==1),"leg mirror");uv(legs[i],0,i<2?96:64,0,8,37,8);
   eq(legs[i].rotationPointY+cube(legs[i],0).posY2,24,"feet at floor");
  }
  r.setRotationAngles(0,1,0,30,-15,.0625F,null);
  eq(legs[0].rotateAngleX,.4F,"hind stride");eq(legs[1].rotateAngleX,-.4F,"opposite hind stride");eq(legs[2].rotateAngleX,-.4F,"diagonal front stride");eq(legs[3].rotateAngleX,.4F,"diagonal pair");
  eq(head.rotateAngleY,(float)Math.PI/6,"head yaw");eq(head.rotateAngleX,-(float)Math.PI/12,"head pitch");
  ModelPillager p=new ModelPillager();check(p.textureWidth==64&&p.textureHeight==64,"pillager atlas");
  ModelRenderer nose=part(p,"nose"),folded=part(p,"foldedArms");
  check(p.bipedHead.childModels.contains(nose),"nose inherits head pose");box(nose,0,-1,-1,-6,2,4,2);pivot(nose,0,-2,0);uv(nose,0,24,0,2,4,2);
  check(!p.bipedHeadwear.showModel,"pillager hat hidden");box(p.bipedHeadwear,0,-4,-10,-4,8,12,8);
  box(p.bipedBody,0,-4,0,-3,8,12,6);box(p.bipedBody,1,-4,0,-3,8,20,6);uv(p.bipedBody,0,16,20,8,12,6);uv(p.bipedBody,1,0,38,8,20,6);
  uv(p.bipedRightArm,0,40,46,4,12,4);uv(p.bipedLeftArm,0,40,46,4,12,4);check(p.bipedLeftArm.mirror&&p.bipedLeftLeg.mirror,"left limb UV mirrors");
  check(folded.childModels.size()==1,"folded shoulder child");pivot(folded,0,3,-1);eq(folded.rotateAngleX,-.75F,"folded tilt");
  p.setRotationAngles(0,1,0,0,0,.0625F,null);eq(p.bipedRightLeg.rotateAngleX,.7F,"illager stride");check(!folded.showModel&&p.bipedLeftArm.showModel&&p.bipedRightArm.showModel,"empty hand neutral visibility");
  Method pose=ModelPillager.class.getDeclaredMethod("applyCrossbowPose",int.class,boolean.class,int.class);pose.setAccessible(true);
  check(!(Boolean)pose.invoke(p,0,false,25),"no crossbow is not aiming");
  check((Boolean)pose.invoke(p,25,false,25),"full charge arm pose");
  eq(p.bipedRightArm.rotateAngleY,-.8F,"charge right yaw");eq(p.bipedRightArm.rotateAngleX,-.97079635F,"charge right pitch");
  eq(p.bipedLeftArm.rotateAngleY,.85F,"charge left yaw endpoint");eq(p.bipedLeftArm.rotateAngleX,-1.5707964F,"charge left pitch endpoint");
  pose.invoke(p,100,false,25);eq(p.bipedLeftArm.rotateAngleY,.85F,"charge progress clamped");
  pose.invoke(p,10,false,25);eq(p.bipedLeftArm.rotateAngleY,.58F,"charge partial progress");
  p.bipedHead.rotateAngleX=.2F;p.bipedHead.rotateAngleY=.3F;pose.invoke(p,0,true,25);
  eq(p.bipedRightArm.rotateAngleY,0,"hold follows head yaw");eq(p.bipedLeftArm.rotateAngleY,.9F,"support arm follows head yaw");
  eq(p.bipedRightArm.rotateAngleX,-(float)Math.PI/2+.3F,"hold follows head pitch");eq(p.bipedLeftArm.rotateAngleX,-1.3F,"support arm pitch");
  int scales=0;ClassNode node=new ClassNode();new ClassReader(RenderPillager.class.getName()).accept(node,0);
  for(MethodNode m:node.methods)if(m.name.equals("preRenderCallback"))for(AbstractInsnNode ins:m.instructions.toArray())if(ins instanceof LdcInsnNode&&Float.valueOf(.9375F).equals(((LdcInsnNode)ins).cst))scales++;
  check(scales==3,"modern illager renderer scale");
  try(ZipFile z=new ZipFile("tools/decompile1202/reference/client.jar")){
   for(String name:new String[]{"pillager","ravager"}){
    BufferedImage local=ImageIO.read(new File("src/main/resources/assets/htgt6/textures/entity/village_pillage/"+name+".png"));
    BufferedImage modern=ImageIO.read(z.getInputStream(z.getEntry("assets/minecraft/textures/entity/illager/"+name+".png")));
    check(local.getWidth()==modern.getWidth()&&local.getHeight()==modern.getHeight(),name+" texture size");
    int differences=0;for(int y=0;y<local.getHeight();y++)for(int x=0;x<local.getWidth();x++)if(local.getRGB(x,y)!=modern.getRGB(x,y))differences++;
    check(differences==0,name+" actual texture pixels match reference (different PNG compression allowed)");
   }
  }
  net.minecraft.world.World world=com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.initialize();
  com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager mob=
    new com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager(world);
  net.minecraft.item.ItemStack crossbow=new net.minecraft.item.ItemStack(
    new com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow());
  mob.setCurrentItemOrArmor(0,crossbow);p.setRotationAngles(0,0,0,0,0,.0625F,mob);
  check(!folded.showModel&&p.bipedRightArm.showModel,"idle crossbow uses separate holding arms");
  eq(p.bipedRightArm.rotateAngleY,-.3F,"uncharged crossbow still holds reference pose");
  mob.setCrossbowChargeTicks(12);p.setRotationAngles(0,0,0,0,0,.0625F,mob);
  check(!folded.showModel&&p.bipedLeftArm.showModel&&p.bipedRightArm.showModel,"synced charge watcher selects separate arms");
  eq(p.bipedLeftArm.rotateAngleY,.4F+12F/25F*.45F,"watcher progress drives charge pose");
  mob.setCrossbowChargeTicks(0);crossbow.setTagCompound(new net.minecraft.nbt.NBTTagCompound());crossbow.getTagCompound().setBoolean("Charged",true);
  p.setRotationAngles(0,0,0,0,0,.0625F,mob);check(!folded.showModel,"charged item selects hold pose");eq(p.bipedRightArm.rotateAngleY,-.3F,"charged item hold angle");
  mob.setCurrentItemOrArmor(0,null);p.setRotationAngles(0,0,0,0,0,.0625F,mob);
  check(!folded.showModel&&p.bipedRightArm.showModel,"switching to empty hands keeps neutral arms visible");
  eq(p.bipedRightArm.rotateAngleY,0,"empty hands reset hold yaw");
  System.out.println("PASS raider139: "+checks);
 }
}
