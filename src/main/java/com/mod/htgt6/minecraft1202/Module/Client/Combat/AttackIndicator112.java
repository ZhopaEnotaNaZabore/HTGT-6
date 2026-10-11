package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.AttackCooldown112;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import org.lwjgl.opengl.GL11;

/** Minecraft 1.12 cooled-attack indicator, rendered immediately below the crosshair. */
public final class AttackIndicator112 extends Gui {
    private static final ResourceLocation ICONS =
            new ResourceLocation("htgt6", "textures/gui/icons_1_12.png");
    private final Minecraft minecraft = Minecraft.getMinecraft();
    @SubscribeEvent
    public void onCrosshair(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS
                || minecraft.thePlayer == null || minecraft.theWorld == null
                || minecraft.gameSettings.hideGUI) return;

        float strength = com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatAttack120.strength(minecraft.thePlayer,event.partialTicks);
        int x = event.resolution.getScaledWidth() / 2 - 8;
        int y = event.resolution.getScaledHeight() / 2 + 9;
        boolean readyTarget = isReadyTarget(strength, minecraft.objectMouseOver == null
                ? null : minecraft.objectMouseOver.entityHit);

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);
        minecraft.getTextureManager().bindTexture(ICONS);
        if (readyTarget) {
            drawTexturedModalRect(x, y, 68, 94, 16, 16);
        } else if (strength < 1.0F) {
            int filled = (int) (strength * 17.0F);
            drawTexturedModalRect(x, y, 36, 94, 16, 4);
            drawTexturedModalRect(x, y, 52, 94, filled, 4);
        }
        GL11.glPopAttrib();
    }

    public static boolean isReadyTarget(float strength, Entity target) {
        // The crosshair already selected the target, including custom mod ray-trace results.
        return strength >= 1.0F && target != null && target.isEntityAlive();
    }
}
