package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;

import com.mod.htgt6.HTGT6;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderPillager extends RenderBiped {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            HTGT6.MOD_ID.toLowerCase(), "textures/entity/village_pillage/pillager.png");

    private final com.mod.htgt6.minecraft1202.Module.Client.Shared.TileEntityBountifulBannerRenderer bannerRenderer=new com.mod.htgt6.minecraft1202.Module.Client.Shared.TileEntityBountifulBannerRenderer();
    public RenderPillager() { super(new ModelPillager(), 0.5F);bannerRenderer.func_147497_a(net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher.instance); }
    @Override protected void renderEquippedItems(net.minecraft.entity.EntityLiving entity,float partial){
        super.renderEquippedItems(entity,partial);
        net.minecraft.item.ItemStack head=entity.getEquipmentInSlot(4);
        if(head==null||head.getItem()!=com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.banner)return;
        org.lwjgl.opengl.GL11.glPushMatrix();
        try{((net.minecraft.client.model.ModelBiped)mainModel).bipedHead.postRender(.0625F);bannerRenderer.renderOnHead(head);}
        finally{org.lwjgl.opengl.GL11.glPopMatrix();}
    }
    @Override protected void preRenderCallback(net.minecraft.entity.EntityLivingBase entity, float partialTicks) {
        org.lwjgl.opengl.GL11.glScalef(0.9375F, 0.9375F, 0.9375F);
    }
    @Override protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
}
