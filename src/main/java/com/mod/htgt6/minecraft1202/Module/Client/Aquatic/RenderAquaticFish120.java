package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class RenderAquaticFish120 extends RenderLiving {
    private static final ResourceLocation COD=tex("cod"),SALMON=tex("salmon"),PUFFER=tex("pufferfish"),TA=new ResourceLocation("htgt6","textures/entity/fish/tropical_a.png"),TB=new ResourceLocation("htgt6","textures/entity/fish/tropical_b.png");
    private final int kind;
    public RenderAquaticFish120(int kind){super(new ModelAquaticFish120(kind),.25F);this.kind=kind;setRenderPassModel(new ModelAquaticFish120(kind));}
    private static ResourceLocation tex(String name){return new ResourceLocation("htgt6","textures/entity/fish/"+name+".png");}
    @Override protected ResourceLocation getEntityTexture(Entity entity){return kind==0?COD:kind==1?SALMON:kind==2?PUFFER:((EntityAquaticFish120.Tropical)entity).getVariant()<6?TA:TB;}
    @Override protected void renderModel(EntityLivingBase living,float limb,float amount,float age,float yaw,float pitch,float scale){
        if(kind==3){float[][] base={{.96F,.38F,.18F},{.95F,.72F,.12F},{.25F,.68F,.92F},{.75F,.28F,.72F},{.25F,.82F,.42F},{.88F,.86F,.72F}};float[] c=base[((EntityAquaticFish120.Tropical)living).getVariant()%6];GL11.glColor3f(c[0],c[1],c[2]);}
        super.renderModel(living,limb,amount,age,yaw,pitch,scale);GL11.glColor4f(1,1,1,1);
    }
    @Override protected int shouldRenderPass(EntityLivingBase living,int pass,float partial){if(kind!=3||pass!=0)return -1;int v=((EntityAquaticFish120.Tropical)living).getVariant();bindTexture(new ResourceLocation("htgt6","textures/entity/fish/tropical_"+(v<6?"a":"b")+"_pattern_"+(v%6+1)+".png"));float[][] colors={{1,.45F,.2F},{1,.8F,.15F},{.3F,.8F,1},{.9F,.3F,.8F},{.3F,1,.5F},{1,1,.9F}};float[] c=colors[v%6];GL11.glColor3f(c[0],c[1],c[2]);return 1;}
    @Override protected void func_82408_c(EntityLivingBase living,int pass,float partial){GL11.glColor4f(1,1,1,1);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);}
    @Override protected void preRenderCallback(EntityLivingBase living,float partial){if(kind==2){float scale=EntityAquaticFish120.Pufferfish.puffScale(((EntityAquaticFish120.Pufferfish)living).getPuffState());GL11.glScalef(scale,scale,scale);}}
}
