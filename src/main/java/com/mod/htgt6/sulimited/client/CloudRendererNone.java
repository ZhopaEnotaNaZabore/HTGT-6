package com.mod.htgt6.sulimited.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.client.IRenderHandler;

/** Explicit no-op renderer; null would make RenderGlobal draw vanilla clouds. */
public final class CloudRendererNone extends IRenderHandler {
    public static final CloudRendererNone INSTANCE = new CloudRendererNone();
    private CloudRendererNone() { }
    @Override public void render(float partialTicks, WorldClient world, Minecraft minecraft) { }
}
