package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityAxolotl120;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderAxolotl120 extends RenderLiving {
    private static final String[] NAMES={"lucy","wild","gold","cyan","blue"};
    private static final ResourceLocation[] TEXTURES=new ResourceLocation[NAMES.length];
    static{for(int i=0;i<NAMES.length;i++)TEXTURES[i]=new ResourceLocation("htgt6","textures/entity/axolotl/axolotl_"+NAMES[i]+".png");}
    public RenderAxolotl120(){super(new ModelAxolotl120(),0.35F);}
    @Override protected ResourceLocation getEntityTexture(Entity raw){return TEXTURES[((EntityAxolotl120)raw).getVariant()];}
}
