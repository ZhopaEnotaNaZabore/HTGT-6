package com.mod.htgt6.minecraft1202.Module.Client.Exploration;

import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderIllager112 extends RenderBiped {
    private final ResourceLocation texture;
    private final com.mod.htgt6.minecraft1202.Module.Client.Shared.TileEntityBountifulBannerRenderer bannerRenderer=new com.mod.htgt6.minecraft1202.Module.Client.Shared.TileEntityBountifulBannerRenderer();
    public RenderIllager112(String name) {
        super(new ModelIllager112(), 0.5F);
        bannerRenderer.func_147497_a(net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher.instance);
        texture = new ResourceLocation("htgt6", "textures/entity/exploration_update/illager/" + name + ".png");
    }
    @Override protected void renderEquippedItems(net.minecraft.entity.EntityLiving entity,float partial){
        super.renderEquippedItems(entity,partial);
        net.minecraft.item.ItemStack head=entity.getEquipmentInSlot(4);
        if(head==null||head.getItem()!=com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.banner)return;
        org.lwjgl.opengl.GL11.glPushMatrix();
        try{((net.minecraft.client.model.ModelBiped)mainModel).bipedHead.postRender(.0625F);bannerRenderer.renderOnHead(head);}
        finally{org.lwjgl.opengl.GL11.glPopMatrix();}
    }
    @Override protected ResourceLocation getEntityTexture(Entity entity) { return texture; }
}
