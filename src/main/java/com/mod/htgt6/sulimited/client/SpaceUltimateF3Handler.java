package com.mod.htgt6.sulimited.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class SpaceUltimateF3Handler {

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onDebugTextRender(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();

        if (event == null || event.left == null || mc.gameSettings == null
                || !mc.gameSettings.showDebugInfo || mc.thePlayer == null) {
            return;
        }

        for (int i = 0; i < event.left.size(); i++) {
                String line = event.left.get(i);

                // FIX: Look for the new exact string
                if (line != null && line.startsWith("Biome: Space Ultimate")) {

                    int dimensionID = mc.thePlayer.dimension;

                    // Default fallback name for minimaps and unknown dimensions
                    String newBiomeName = "Space Ultimate";

                    // Append the planet names for F3
                    if (dimensionID == -81) {
                        newBiomeName = "Space Ultimate - Gliese 792b";
                    } else if (dimensionID == -888) {
                        newBiomeName = "Space Ultimate - Gliese 792c";
                    }

                    event.left.set(i, "Biome: " + newBiomeName);
                    break;
                }
        }
    }
}
