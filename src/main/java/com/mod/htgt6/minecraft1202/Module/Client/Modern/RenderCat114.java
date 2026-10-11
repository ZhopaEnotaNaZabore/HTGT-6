package com.mod.htgt6.minecraft1202.Module.Client.Modern;
import net.minecraft.client.renderer.entity.RenderLiving;import net.minecraft.entity.*;import net.minecraft.util.ResourceLocation;import org.lwjgl.opengl.GL11;import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityCat114;
public final class RenderCat114 extends RenderLiving {
 public static final String[] VARIANTS={"tabby","black","red","siamese","british_shorthair","calico","persian","ragdoll","white","jellie","all_black"};
 private static final ResourceLocation[] TEXTURES=new ResourceLocation[11];static{for(int i=0;i<11;i++)TEXTURES[i]=new ResourceLocation("htgt6","textures/entity/village114/cat/"+VARIANTS[i]+".png");}
 public RenderCat114(){super(new ModelCat114(),.4F);setRenderPassModel(new ModelCat114());}
 protected ResourceLocation getEntityTexture(Entity e){return TEXTURES[((EntityCat114)e).variant()];}
 protected void preRenderCallback(EntityLivingBase e,float partial){GL11.glScalef(.8F,.8F,.8F);if(((EntityCat114)e).lying())GL11.glTranslatef(.4F,.15F,.1F);}
 protected int shouldRenderPass(EntityLivingBase e,int pass,float partial){if(pass==0&&((EntityCat114)e).isTamed()){bindTexture(new ResourceLocation("htgt6","textures/entity/village114/cat/cat_collar.png"));int c=com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.SignDye114.collarColor(((EntityCat114)e).collar());GL11.glColor3f(((c>>16)&255)/255F,((c>>8)&255)/255F,(c&255)/255F);return 1;}return -1;}
}
