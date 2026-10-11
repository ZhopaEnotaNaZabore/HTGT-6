package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageBell120;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/** Renders the official 1.20 bell-body geometry over the JSON attachment model. */
public final class RenderVillageBell120 extends TileEntitySpecialRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation("htgt6",
            "textures/entity/village_pillage/bell/bell_body.png");
    private final ModelRenderer body;
    private final ModelRenderer lip;

    public RenderVillageBell120() {
        ModelBase model = new ModelBase() { };
        model.textureWidth = 32;
        model.textureHeight = 32;
        body = new ModelRenderer(model, 0, 0);
        body.addBox(-3.0F, -6.0F, -3.0F, 6, 7, 6);
        lip = new ModelRenderer(model, 0, 13);
        lip.addBox(-4.0F, 1.0F, -4.0F, 8, 2, 8);
    }

    @Override public void renderTileEntityAt(TileEntity raw, double x, double y, double z, float partialTicks) {
        if (!(raw instanceof TileEntityVillageBell120)) return;
        TileEntityVillageBell120 bell = (TileEntityVillageBell120)raw;
        float swing = 0.0F;
        if (bell.isRinging()) {
            float ticks = bell.getRingTicks(partialTicks);
            swing = MathHelper.sin(ticks / (float)Math.PI) / (4.0F + ticks / 3.0F);
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glTranslated(x + .5D, y + .75D, z + .5D);
        float degrees = swing * 57.295776F;
        switch (bell.getRingDirection()) {
            case 0: GL11.glRotatef(-degrees, 1F, 0F, 0F); break;
            case 1: GL11.glRotatef(-degrees, 0F, 0F, 1F); break;
            case 2: GL11.glRotatef(degrees, 1F, 0F, 0F); break;
            case 3: GL11.glRotatef(degrees, 0F, 0F, 1F); break;
            default: break;
        }
        bindTexture(TEXTURE);
        body.render(.0625F);
        lip.render(.0625F);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }
}
