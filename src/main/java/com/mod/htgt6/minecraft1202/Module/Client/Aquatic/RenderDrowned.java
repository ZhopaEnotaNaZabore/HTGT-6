package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import com.mod.htgt6.HTGT6;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDrowned;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

public final class RenderDrowned extends RenderBiped {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(HTGT6.MOD_ID.toLowerCase(), "textures/entity/zombie/drowned.png");
    private static final ResourceLocation OUTER_TEXTURE =
            new ResourceLocation(HTGT6.MOD_ID.toLowerCase(),
                    "textures/entity/zombie/drowned_outer_layer.png");

    public RenderDrowned() {
        super(new ModelDrowned(), 0.5F);
        setRenderPassModel(new ModelDrowned(0.25F));
    }

    private final ItemStack shell = new ItemStack(OceanContent.nautilusShell);

    @Override
    protected void renderEquippedItems(EntityLiving entity, float partialTick) {
        super.renderEquippedItems(entity, partialTick);
        if (!(entity instanceof EntityDrowned) || !((EntityDrowned)entity).carriesNautilusShell()) return;
        try (RenderStateGuard112 ignored = RenderStateGuard112.capture()) {
            GL11.glPushMatrix();
            try {
                if (mainModel.isChild) {
                    GL11.glTranslatef(0F, .625F, 0F);
                    GL11.glRotatef(-20F, -1F, 0F, 0F);
                    GL11.glScalef(.5F, .5F, .5F);
                }
                modelBipedMain.bipedLeftArm.postRender(.0625F);
                // Mirror the legacy RenderBiped flat-item transform at the left
                // wrist. The right-hand item remains owned by the base renderer.
                GL11.glScalef(-1F, 1F, 1F);
                GL11.glDisable(GL11.GL_CULL_FACE);
                GL11.glTranslatef(-.0625F, .4375F, .0625F);
                GL11.glTranslatef(.25F, .1875F, -.1875F);
                GL11.glScalef(.375F, .375F, .375F);
                GL11.glRotatef(60F, 0F, 0F, 1F);
                GL11.glRotatef(-90F, 1F, 0F, 0F);
                GL11.glRotatef(20F, 0F, 0F, 1F);
                GL11.glColor4f(1F, 1F, 1F, 1F);
                renderManager.itemRenderer.renderItem(entity, shell, 0);
            } finally {
                GL11.glPopMatrix();
            }
        }
    }

    @Override
    protected int shouldRenderPass(EntityLivingBase entity, int pass, float partialTick) {
        if (pass != 0) return -1;
        bindTexture(OUTER_TEXTURE);
        return 1;
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
}
