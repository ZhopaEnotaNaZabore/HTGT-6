package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

/** Pulsing full-screen Darkness effect, independent of legacy blindness fog. */
public final class SculkDarknessOverlay120 {
    @SubscribeEvent public void render(RenderGameOverlayEvent.Post event){
        if(event.type!=RenderGameOverlayEvent.ElementType.ALL)return;
        Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null)return;
        PotionEffect effect=mc.thePlayer.getActivePotionEffect(Backport120Content.darkness);if(effect==null)return;
        float pulse=.5F+.5F*(float)Math.sin((effect.getDuration()+event.partialTicks)*.08F);
        int alpha=115+(int)(pulse*85F);Gui.drawRect(0,0,event.resolution.getScaledWidth(),event.resolution.getScaledHeight(),alpha<<24);
    }
}
