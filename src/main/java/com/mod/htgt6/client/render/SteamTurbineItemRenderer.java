package com.mod.htgt6.client.render;

import com.mod.htgt6.common.turbine.SteamTurbineContent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

/** The item has no world/rotor inventory: show the same empty socket as a placed controller. */
public final class SteamTurbineItemRenderer implements IItemRenderer {
    private final RenderBlocks renderer = new RenderBlocks();

    @Override public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type != ItemRenderType.FIRST_PERSON_MAP;
    }
    @Override public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }
    @Override public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON)
                GL11.glTranslatef(.5F, .5F, .5F);
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            GL11.glColor4f(1, 1, 1, 1);
            Block block = SteamTurbineContent.controller;
            renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
            for (int side = 0; side < 6; side++) {
                IIcon icon = block.getIcon(side, 0);
                ForgeDirection d = ForgeDirection.getOrientation(side);
                Tessellator t = Tessellator.instance;
                t.startDrawingQuads();
                t.setNormal(d.offsetX, d.offsetY, d.offsetZ);
                t.setColorOpaque_F(1, 1, 1);
                switch (side) {
                    case 0: renderer.renderFaceYNeg(block,-.5,-.5,-.5,icon); break;
                    case 1: renderer.renderFaceYPos(block,-.5,-.5,-.5,icon); break;
                    case 2: renderer.renderFaceZNeg(block,-.5,-.5,-.5,icon); break;
                    case 3: renderer.renderFaceZPos(block,-.5,-.5,-.5,icon); break;
                    case 4: renderer.renderFaceXNeg(block,-.5,-.5,-.5,icon); break;
                    case 5: renderer.renderFaceXPos(block,-.5,-.5,-.5,icon); break;
                }
                t.draw();
            }
            MultiblockRenderState state = new MultiblockRenderState("mixer");
            try {
                // Positive Z is the visible front in vanilla's inventory block projection.
                GL11.glRotatef(180, 0, 1, 0);
                SteamTurbineRenderer.bindMetal();
                SteamTurbineRenderer.front(.48, false, 0, .5F, .5F, .5F, false);
            } finally { state.close(); }
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }
}
