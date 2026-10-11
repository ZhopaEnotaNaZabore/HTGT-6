package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.TileEntityDragonHead;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class RenderDragonHead120 extends TileEntitySpecialRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft",
            "textures/entity/enderdragon/dragon.png");
    private final ModelDragonHead120 model = new ModelDragonHead120();
    @Override public void renderTileEntityAt(TileEntity raw, double x, double y, double z, float partialTick) {
        TileEntityDragonHead tile = (TileEntityDragonHead) raw;
        int facing = tile.getBlockMetadata();
        float yaw = facing == 2 ? 180.0F : facing == 3 ? 0.0F : facing == 4 ? 90.0F
                : facing == 5 ? -90.0F : tile.getRotation() * 22.5F;
        double px = x + 0.5D, py = y + (facing == 1 ? 0.0D : 0.25D), pz = z + 0.5D;
        if (facing == 2) pz += 0.25D; else if (facing == 3) pz -= 0.25D;
        else if (facing == 4) px += 0.25D; else if (facing == 5) px -= 0.25D;
        renderModel(px, py, pz, yaw, jawAngle(tile.getAnimation(partialTick)));
    }
    public static float jawAngle(float animation) {
        return (float) (Math.sin(animation * Math.PI * 0.2D) + 1.0D) * 0.2F;
    }
    public void renderModel(double x, double y, double z, float yaw, float jaw) {
        // Item renderers do not receive TileEntityRendererDispatcher from Forge.
        try(com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112 ignored=com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112.capture()){
            net.minecraft.client.Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
            GL11.glPushMatrix();
            try{
                GL11.glDisable(GL11.GL_CULL_FACE);GL11.glEnable(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL);GL11.glColor4f(1,1,1,1);
                GL11.glTranslatef((float)x,(float)y,(float)z);GL11.glScalef(-1,-1,1);
                GL11.glTranslatef(0,-.374375F,0);GL11.glScalef(.75F,.75F,.75F);
                model.render(yaw,jaw);
            }finally{GL11.glPopMatrix();}
        }
    }
}
