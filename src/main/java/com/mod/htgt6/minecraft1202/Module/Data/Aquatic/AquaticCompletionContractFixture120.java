package com.mod.htgt6.minecraft1202.Module.Data.Aquatic;

import com.google.gson.JsonArray;import com.google.gson.JsonElement;import com.google.gson.JsonObject;import com.google.gson.JsonParser;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.TileEntityConduit120;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDolphin120;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityPhantom120;
import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.ModelAquaticFish120;
import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.ModelDolphin120;
import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.ModelPhantom120;
import com.mod.htgt6.minecraft1202.Module.Registry.Aquatic.AquaticUpdate;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import java.io.InputStream;import java.io.InputStreamReader;import java.util.Map;

/** Completion gate for the Update Aquatic asset/content groups absent before the full patch. */
public final class AquaticCompletionContractFixture120 {
 public static void main(String[] ignored)throws Exception{
  require(OceanContent.deadCoral!=null&&OceanContent.deadCoralFan!=null&&OceanContent.deadWallCoralFans.length==5,"dead coral variants");
  require(OceanContent.driedKelp!=null&&OceanContent.driedKelpBlock!=null,"dried kelp");
  require(OceanContent.fishBucket!=null&&OceanContent.nautilusShell!=null&&OceanContent.phantomMembrane!=null,"aquatic items");
  require(OceanContent.conduit!=null&&OceanContent.conduit.hasTileEntity(0)&&new TileEntityConduit120()!=null,"functional conduit tile");
  require(TileEntityConduit120.effectRangeForFrame(16)==32&&TileEntityConduit120.effectRangeForFrame(42)==96,"vanilla conduit range steps");
  require(TileEntityConduit120.isFrameOffset(2,0,0)&&TileEntityConduit120.isFrameOffset(0,2,2)&&!TileEntityConduit120.isFrameOffset(1,1,1),"conduit frame shell");
  int[] networkIds={AquaticUpdate.DROWNED_MOD_ID,AquaticUpdate.TRIDENT_MOD_ID,AquaticUpdate.TURTLE_MOD_ID,
          AquaticUpdate.COD_MOD_ID,AquaticUpdate.SALMON_MOD_ID,AquaticUpdate.PUFFERFISH_MOD_ID,
          AquaticUpdate.TROPICAL_FISH_MOD_ID,AquaticUpdate.DOLPHIN_MOD_ID,AquaticUpdate.PHANTOM_MOD_ID};
  java.util.Set<Integer> uniqueNetworkIds=new java.util.HashSet<Integer>();for(int id:networkIds){require(id>29,"aquatic network ID overlaps another backport module: "+id);uniqueNetworkIds.add(id);}require(uniqueNetworkIds.size()==networkIds.length,"duplicate aquatic network entity ID");
  Class<?>[] entities={EntityAquaticFish120.Cod.class,EntityAquaticFish120.Salmon.class,EntityAquaticFish120.Pufferfish.class,EntityAquaticFish120.Tropical.class,EntityDolphin120.class,EntityPhantom120.class};
  require(new java.util.HashSet<Class<?>>(java.util.Arrays.asList(entities)).size()==6,"six distinct aquatic entity identities");
  String[] blocks={"dead_tube_coral","dead_brain_coral","dead_bubble_coral","dead_fire_coral","dead_horn_coral","dead_tube_coral_fan","dead_brain_coral_fan","dead_bubble_coral_fan","dead_fire_coral_fan","dead_horn_coral_fan","dried_kelp_top","dried_kelp_bottom","dried_kelp_side","conduit"};for(String name:blocks)resource("textures/blocks/"+name+".png");
  String[] items={"dried_kelp","nautilus_shell","phantom_membrane","cod_bucket","salmon_bucket","pufferfish_bucket","tropical_fish_bucket"};for(String name:items)resource("textures/items/"+name+".png");
  String[] entitiesTextures={"fish/cod.png","fish/salmon.png","fish/pufferfish.png","fish/tropical_a.png","fish/tropical_b.png","dolphin.png","phantom.png","phantom_eyes.png","conduit/base.png","conduit/cage.png","conduit/open_eye.png","conduit/closed_eye.png"};for(String name:entitiesTextures)resource("textures/entity/"+name);
  for(String shape:new String[]{"a","b"})for(int i=1;i<=6;i++)resource("textures/entity/fish/tropical_"+shape+"_pattern_"+i+".png");
  verifyModels();
  verifySounds();System.out.println("Complete Update Aquatic contract OK: missing blocks/items restored, 6 entities, conduit, official sound bank");
 }
 private static void verifyModels()throws Exception{
  int[] fishChildren={6,5,13,6};for(int kind=0;kind<4;kind++)require(root(new ModelAquaticFish120(kind)).childModels.size()==fishChildren[kind],"complete fish model "+kind);
  verifyDolphinModel();
  net.minecraft.client.model.ModelRenderer phantom=root(new ModelPhantom120());require(phantom.childModels.size()==4,"complete phantom model");
 }
 private static void verifyDolphinModel()throws Exception{
  ModelDolphin120 model=new ModelDolphin120();net.minecraft.client.model.ModelRenderer body=part(model,"body");
  net.minecraft.client.model.ModelRenderer back=part(model,"backFin"),left=part(model,"leftFin"),right=part(model,"rightFin"),tail=part(model,"tail"),fin=part(model,"tailFin"),head=part(model,"head"),nose=part(model,"nose");
  require(body.childModels.size()==5&&tail.childModels.size()==1&&head.childModels.size()==1,"complete dolphin hierarchy");
  box(body,-4,-7,0,4,0,13,"dolphin body");box(back,-.5F,0,8,.5F,4,13,"dolphin dorsal fin");
  box(left,-.5F,-4,0,.5F,0,7,"dolphin left fin");box(right,-.5F,-4,0,.5F,0,7,"dolphin right fin");
  box(tail,-2,-2.5F,0,2,2.5F,11,"dolphin tail");box(fin,-5,-.5F,0,5,.5F,6,"dolphin tail fin");
  box(head,-4,-3,-3,4,4,3,"dolphin head");box(nose,-1,2,-7,1,4,-3,"dolphin nose");
  require(close(body.rotationPointY,22)&&close(body.rotationPointZ,-5),"dolphin body pivot");
  require(close(left.rotationPointX,2)&&close(right.rotationPointX,-2)&&close(left.rotationPointZ,4)&&close(right.rotationPointZ,4),"dolphin pectoral pivots");
  require(close(left.rotateAngleX,1.0471976F)&&close(left.rotateAngleZ,2.0943952F)&&close(right.rotateAngleX,1.0471976F)&&close(right.rotateAngleZ,-2.0943952F),"dolphin pectoral rotations");
  require(close(tail.rotationPointY,-2.5F)&&close(tail.rotationPointZ,11)&&close(tail.rotateAngleX,-.10471976F)&&close(fin.rotationPointZ,9),"dolphin tail pivots");
  require(close(head.rotationPointY,-4)&&close(head.rotationPointZ,-3),"dolphin head pivot");
  require(left.mirror&&!right.mirror,"dolphin fin UV mirroring");
  net.minecraft.entity.Entity probe=new net.minecraft.entity.Entity(null){protected void entityInit(){}protected void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound tag){}protected void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound tag){}};
  model.setRotationAngles(0,0,0,20,10,.0625F,probe);
  require(close(body.rotateAngleX,10*.017453292F)&&close(body.rotateAngleY,20*.017453292F)&&close(tail.rotateAngleX,-.10471976F)&&close(fin.rotateAngleX,0),"dolphin resting pose");
  probe.motionX=1;model.setRotationAngles(0,0,0,20,10,.0625F,probe);
  require(close(body.rotateAngleX,10*.017453292F-.1F)&&close(tail.rotateAngleX,-.1F)&&close(fin.rotateAngleX,-.2F),"dolphin moving wave");
 }
 private static net.minecraft.client.model.ModelRenderer part(Object model,String name)throws Exception{java.lang.reflect.Field field=model.getClass().getDeclaredField(name);field.setAccessible(true);return (net.minecraft.client.model.ModelRenderer)field.get(model);}
 private static void box(net.minecraft.client.model.ModelRenderer part,float x1,float y1,float z1,float x2,float y2,float z2,String what){require(part.cubeList.size()==1,what+" cube count");net.minecraft.client.model.ModelBox box=(net.minecraft.client.model.ModelBox)part.cubeList.get(0);require(close(box.posX1,x1)&&close(box.posY1,y1)&&close(box.posZ1,z1)&&close(box.posX2,x2)&&close(box.posY2,y2)&&close(box.posZ2,z2),what+" geometry");}
 private static boolean close(float a,float b){return Math.abs(a-b)<.00001F;}
 private static net.minecraft.client.model.ModelRenderer root(Object model)throws Exception{java.lang.reflect.Field field=model.getClass().getDeclaredField(model instanceof ModelDolphin120?"body":"root");field.setAccessible(true);return (net.minecraft.client.model.ModelRenderer)field.get(model);}
 private static void verifySounds()throws Exception{InputStream stream=AquaticCompletionContractFixture120.class.getResourceAsStream("/assets/htgt6/sounds.json");require(stream!=null,"sounds.json");JsonObject root=new JsonParser().parse(new InputStreamReader(stream,"UTF-8")).getAsJsonObject();int events=0,files=0;for(Map.Entry<String,JsonElement> e:root.entrySet()){if(!e.getKey().matches("^(ambient\\.underwater|block\\.(bubble_column|conduit)|entity\\.(cod|dolphin|drowned|phantom|puffer_fish|salmon|tropical_fish|turtle)|item\\.trident).*"))continue;events++;JsonArray sounds=e.getValue().getAsJsonObject().getAsJsonArray("sounds");for(JsonElement sound:sounds){String name=sound.isJsonPrimitive()?sound.getAsString():sound.getAsJsonObject().get("name").getAsString();if(name.startsWith("minecraft:"))continue;resource("sounds/"+name+".ogg");files++;}}require(events>=80&&files>=150,"complete aquatic sound graph");}
 private static void resource(String path){try{InputStream stream=path.startsWith("sounds/")?com.mod.htgt6.minecraft1202.Module.Data.Modern.ExternalSoundFixture179.open("assets/htgt6/"+path.replace("sounds/htgt6:","sounds/")):AquaticCompletionContractFixture120.class.getResourceAsStream("/assets/htgt6/"+path);require(stream!=null,"missing "+path);stream.close();}catch(java.io.IOException failure){throw new AssertionError("Resource check failed: "+path,failure);}}
 private static void require(boolean ok,String what){if(!ok)throw new IllegalStateException(what);}
 private AquaticCompletionContractFixture120(){}
}
