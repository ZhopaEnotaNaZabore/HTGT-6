package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.RiptideAttackHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.lwjgl.opengl.GL11;

/** Direct 1.20.2 SpinAttackEffectLayer adaptation for third-person players. */
public final class RiptideEffectRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HTGT6.MOD_ID.toLowerCase(),
            "textures/entity/trident_riptide.png");
    private final ModelRiptideEffect model = new ModelRiptideEffect();

    @SubscribeEvent
    public void render(RenderPlayerEvent.Specials.Post event) {
        if (event.entityPlayer == null || !RiptideAttackHandler.isActive(event.entityPlayer)) return;
        try (RenderStateGuard112 ignored = RenderStateGuard112.capture()) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1, 1, 1, 1);
            float age = event.entityPlayer.ticksExisted + event.partialRenderTick;
            for (int pass = 0; pass < 3; pass++) {
                GL11.glPushMatrix();
                float rotation = -age * (45.0F + pass * 5.0F);
                GL11.glRotatef(rotation, 0, 1, 0);
                float scale = 0.75F * pass;
                GL11.glScalef(scale, scale, scale);
                GL11.glTranslatef(0.0F, -0.2F + 0.6F * pass, 0.0F);
                model.render();
                GL11.glPopMatrix();
            }
        }
    }
}
