package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMap120;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

/** Official 1.20.2 filled_map.json two-layer inventory/entity appearance. */
public final class TreasureMapItemRenderer120 implements IItemRenderer {
    public static final ResourceLocation PAPER = new ResourceLocation(
            "htgt6", "textures/items/treasure_map/filled_map.png");
    public static final ResourceLocation MARKINGS = new ResourceLocation(
            "htgt6", "textures/items/treasure_map/filled_map_markings.png");

    @Override public boolean handleRenderType(ItemStack stack,ItemRenderType type) {
        // First-person maps deliberately stay on vanilla ItemRenderer/MapItemRenderer
        // so the actual MapData and RED_X are shown. Third-person EQUIPPED is only a
        // generated item-model pass in 1.7 and therefore needs our two-layer icon;
        // falling back there leaves the treasure map visually blank in the hand.
        return VanillaTreasureMap120.isTreasureMap(stack)
                && (type == ItemRenderType.INVENTORY || type == ItemRenderType.ENTITY
                || type == ItemRenderType.EQUIPPED);
    }
    @Override public boolean shouldUseRenderHelper(ItemRenderType type,ItemStack stack,
                                                    ItemRendererHelper helper) {
        return type == ItemRenderType.INVENTORY || (type == ItemRenderType.ENTITY
                && (helper == ItemRendererHelper.ENTITY_BOBBING
                || helper == ItemRendererHelper.ENTITY_ROTATION));
    }
    @Override public void renderItem(ItemRenderType type,ItemStack stack,Object... data) {
        try(RenderStateGuard112 ignored=RenderStateGuard112.capture()){
            GL11.glPushMatrix();
            try{
                GL11.glColor4f(1,1,1,1);
                GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
                if(type==ItemRenderType.INVENTORY){GL11.glScalef(16,16,1);layer(PAPER,0);layer(MARKINGS,.002F);}
                else {GL11.glTranslatef(-.5F,-.5F,0);extruded(PAPER,0);extruded(MARKINGS,.002F);}
            }finally{GL11.glPopMatrix();}
        }
    }
    private static void extruded(ResourceLocation texture,float z){
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);GL11.glTranslatef(0,0,z);
        ItemRenderer.renderItemIn2D(Tessellator.instance,1,0,0,1,16,16,1F/16F);
        GL11.glTranslatef(0,0,-z);
    }
    private static void layer(ResourceLocation texture,float z){
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setNormal(0,0,1);
        t.addVertexWithUV(0,1,z,0,1);t.addVertexWithUV(1,1,z,1,1);
        t.addVertexWithUV(1,0,z,1,0);t.addVertexWithUV(0,0,z,0,0);t.draw();
    }
}
