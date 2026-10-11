package com.mod.htgt6.minecraft1202.Module.Client.Exploration;

import com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.BlockShulkerBox112;
import com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.TileEntityShulkerBox112;
import com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.ShulkerBoxShape112;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.ModelCombatShulker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/** 1.20.2 shulker-box orientation and lid animation through the 1.7 TESR API. */
public final class RenderShulkerBox112 extends TileEntitySpecialRenderer {
    private final ModelCombatShulker model = new ModelCombatShulker();
    @Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (!(tile instanceof TileEntityShulkerBox112) || !(tile.getBlockType() instanceof BlockShulkerBox112)) return;
        renderBox(((BlockShulkerBox112)tile.getBlockType()).getColorName(),
                ((TileEntityShulkerBox112)tile).getProgress(partialTicks), x, y, z,tile.getBlockMetadata());
    }
    public static void applyPose(ModelCombatShulker model, float progress) {
        model.lid.rotationPointY = 24.0F - 8.0F * progress;
        model.lid.rotateAngleY = 270.0F * progress * (float)Math.PI / 180.0F;
    }
    public void renderBox(String color, float progress, double x, double y, double z) {
        renderBox(color,progress,x,y,z,0);
    }
    public static float[] orientation(int metadata) {
        int side=ShulkerBoxShape112.direction(metadata).ordinal();
        return new float[]{side==0?180:side==1?0:90,side==2?180:side==4?90:side==5?-90:0};
    }
    public void renderBox(String color, float progress, double x, double y, double z,int metadata) {
        try (RenderStateGuard112 ignored = RenderStateGuard112.capture()) {
            GL11.glPushMatrix();
            try {
                Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("htgt6",
                        "textures/entity/exploration_update/shulker_box/" + color + ".png"));
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glColor4f(1, 1, 1, 1);
                GL11.glTranslated(x + 0.5D, y + 0.5D, z + 0.5D);
                float[] rotation=orientation(metadata);
                GL11.glRotatef(rotation[0],1,0,0);GL11.glRotatef(rotation[1],0,0,1);
                GL11.glScalef(0.9995F, -0.9995F, -0.9995F);
                GL11.glTranslatef(0, -1, 0);
                applyPose(model, progress);
                model.base.render(0.0625F);
                model.lid.render(0.0625F);
            } finally { GL11.glPopMatrix(); }
        }
    }
}
