package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

public final class DragonHeadItemRenderer120 implements IItemRenderer {
    private final RenderDragonHead120 renderer = new RenderDragonHead120();
    @Override public boolean handleRenderType(ItemStack item, ItemRenderType type) { return true; }
    @Override public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) { return true; }
    @Override public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushMatrix();
        try{
            if (type == ItemRenderType.INVENTORY) GL11.glTranslatef(0.0F, -0.1F, 0.0F);
            renderer.renderModel(0.0D, 0.0D, 0.0D, 180.0F, RenderDragonHead120.jawAngle(0));
        }finally{GL11.glPopMatrix();}
    }
}
