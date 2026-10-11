package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelHorse;
import net.minecraft.client.renderer.entity.RenderHorse;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.LeatherHorseHooks151;
import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;
/** Modern geometry and official textures only for normal horses. Other horse types keep vanilla rendering. */
public final class RenderHorse151 extends RenderHorse {
 private final ModelBase legacy;
 private final ModelHorse151 modern=new ModelHorse151(0),armor=new ModelHorse151(.1F);
 private static final String[] COATS={"white","creamy","chestnut","brown","black","gray","darkbrown"};
 private static final String[] MARKINGS={"white","whitefield","whitedots","blackdots"};
 private static final ResourceLocation[] COAT_TEXTURES=new ResourceLocation[7],MARK_TEXTURES=new ResourceLocation[4];
 private static final ResourceLocation[] ARMOR_TEXTURES={texture("armor/horse_armor_leather.png"),texture("armor/horse_armor_iron.png"),texture("armor/horse_armor_gold.png"),texture("armor/horse_armor_diamond.png")};
 static{for(int i=0;i<7;i++)COAT_TEXTURES[i]=texture("horse_"+COATS[i]+".png");for(int i=0;i<4;i++)MARK_TEXTURES[i]=texture("horse_markings_"+MARKINGS[i]+".png");}
 public RenderHorse151(){super(new ModelHorse(),.75F);legacy=mainModel;}
 private static ResourceLocation texture(String path){return new ResourceLocation("htgt6","textures/entity/horse151/"+path);}
 public static ResourceLocation coat(int variant){int index=variant&255;return COAT_TEXTURES[index<7?index:0];}
 public static ResourceLocation markings(int variant){int index=(variant>>>8)&255;return index>=1&&index<=4?MARK_TEXTURES[index-1]:null;}
 public static ResourceLocation armorTexture(int vanillaIndex,int leatherColor){return leatherColor>=0?ARMOR_TEXTURES[0]:vanillaIndex>=1&&vanillaIndex<=3?ARMOR_TEXTURES[vanillaIndex]:null;}
 @Override public void doRender(EntityLiving living,double x,double y,double z,float yaw,float partial){
  EntityHorse horse=(EntityHorse)living;mainModel=horse.getHorseType()==0?modern:legacy;
  try(RenderStateGuard112 guard=RenderStateGuard112.capture()){super.doRender(living,x,y,z,yaw,partial);}
  finally{mainModel=legacy;setRenderPassModel(null);}
 }
 @Override protected void preRenderCallback(EntityHorse horse,float partial){if(horse.getHorseType()==0)GL11.glScalef(1.1F,1.1F,1.1F);super.preRenderCallback(horse,partial);}
 @Override protected ResourceLocation getEntityTexture(EntityHorse horse){return horse.getHorseType()==0?coat(horse.getHorseVariant()):super.getEntityTexture(horse);}
 void preparePass(ModelHorse151 model){model.onGround=mainModel.onGround;model.isRiding=mainModel.isRiding;model.isChild=mainModel.isChild;setRenderPassModel(model);}
 @Override protected int shouldRenderPass(EntityLivingBase living,int pass,float partial){
  EntityHorse horse=(EntityHorse)living;if(horse.getHorseType()!=0)return -1;
  GL11.glColor4f(1,1,1,1);ResourceLocation path=null;ModelHorse151 model=modern;
  if(pass==0&&!horse.isInvisible())path=markings(horse.getHorseVariant());
  else if(pass==1){int color=LeatherHorseHooks151.color(horse);path=armorTexture(horse.func_110241_cb(),color);model=armor;if(path!=null&&color>=0)GL11.glColor4f((color>>16&255)/255F,(color>>8&255)/255F,(color&255)/255F,1);}
  if(path==null)return -1;
  bindTexture(path);preparePass(model);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);return 1;
 }
}
