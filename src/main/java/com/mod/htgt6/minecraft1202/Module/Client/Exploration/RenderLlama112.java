package com.mod.htgt6.minecraft1202.Module.Client.Exploration;

import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderLlama112 extends RenderLiving {
    private static final ResourceLocation[] TEXTURES = {
        texture("creamy"), texture("white"), texture("brown"), texture("gray")
    };
    private static final ResourceLocation TRADER_DECOR=new ResourceLocation("htgt6",
            "textures/entity/exploration_update/llama/decor/trader_llama.png");
    private static final ResourceLocation[] DECOR=decorTextures();
    private static ResourceLocation[] decorTextures(){
        String[] names={"white","orange","magenta","light_blue","yellow","lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black"};
        ResourceLocation[] result=new ResourceLocation[16];
        for(int i=0;i<16;i++)result[i]=new ResourceLocation("htgt6","textures/entity/exploration_update/llama/decor/"+names[i]+".png");
        return result;
    }
    public RenderLlama112() { super(new ModelLlama112(), 0.7F); }
    private static ResourceLocation texture(String name) {
        return new ResourceLocation("htgt6", "textures/entity/exploration_update/llama/" + name + ".png");
    }
    @Override protected ResourceLocation getEntityTexture(Entity entity) {
        return TEXTURES[((EntityLlama112)entity).getVariant()];
    }
    @Override protected int shouldRenderPass(net.minecraft.entity.EntityLivingBase entity,int pass,float partial){
        if(pass==0&&entity instanceof EntityLlama112){
            EntityLlama112 llama=(EntityLlama112)entity;int color=llama.getCarpetColor();
            if(color>=0&&color<16){bindTexture(DECOR[color]);setRenderPassModel(mainModel);return 1;}
            if(llama.isTraderLlama()){bindTexture(TRADER_DECOR);setRenderPassModel(mainModel);return 1;}}
        return -1;
    }
}
