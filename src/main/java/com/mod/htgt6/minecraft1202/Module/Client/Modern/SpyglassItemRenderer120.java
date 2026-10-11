package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/** Exact two-element spyglass_in_hand.json geometry and UVs on the 1.7 renderer. */
public final class SpyglassItemRenderer120 implements IItemRenderer {
    public static final ResourceLocation TEXTURE = new ResourceLocation(
            "htgt6", "textures/items/1_20_backport/spyglass_model.png");
    static final float[][] ELEMENTS = {
            {7, 8.5F, 7, 9, 13.5F, 9, 2, 7},
            {6.9F, 2.4F, 6.9F, 9.1F, 8.6F, 9.1F, 7, 13}
    };
    /** Only explicit item-model transform in spyglass_in_hand.json. */
    public static final float THIRD_PERSON_TRANSLATE_Y=-2F/16F;

    @Override public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        // The inventory icon is the official flat spyglass.json layer.
        return type != ItemRenderType.INVENTORY;
    }

    @Override public boolean shouldUseRenderHelper(ItemRenderType type,ItemStack item,
                                                    ItemRendererHelper helper) {
        return type == ItemRenderType.ENTITY && (helper == ItemRendererHelper.BLOCK_3D
                || helper == ItemRendererHelper.ENTITY_BOBBING
                || helper == ItemRendererHelper.ENTITY_ROTATION);
    }

    @Override public void renderItem(ItemRenderType type,ItemStack item,Object... data) {
        try (RenderStateGuard112 ignored = RenderStateGuard112.capture()) {
            GL11.glPushMatrix();
            try {
                Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
                GL11.glColor4f(1,1,1,1);
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                if(type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
                    // spyglass_in_hand.json intentionally has no first-person
                    // display override. Forge has already established the held
                    // item frame, so the model transform here is identity.
                } else if(type == ItemRenderType.EQUIPPED) {
                    GL11.glTranslatef(0,THIRD_PERSON_TRANSLATE_Y,0);
                } else {
                    // Exact ground display transform: rotation [90,0,0].
                    GL11.glRotatef(90F,1,0,0);
                }
                renderModel();
            } finally { GL11.glPopMatrix(); }
        }
    }

    static void renderModel() {
        for(float[] e:ELEMENTS)box(e[0]/16F,e[1]/16F,e[2]/16F,e[3]/16F,e[4]/16F,e[5]/16F,
                0,e[6]/16F,2F/16F,e[7]/16F);
    }

    private static void box(float x0,float y0,float z0,float x1,float y1,float z1,
                            float u0,float v0,float u1,float v1) {
        Tessellator t=Tessellator.instance;t.startDrawingQuads();
        t.setNormal(0,0,-1);quad(t,x1,y1,z0,x0,y1,z0,x0,y0,z0,x1,y0,z0,u0,v0,u1,v1);
        t.setNormal(0,0,1);quad(t,x0,y1,z1,x1,y1,z1,x1,y0,z1,x0,y0,z1,u0,v0,u1,v1);
        t.setNormal(-1,0,0);quad(t,x0,y1,z0,x0,y1,z1,x0,y0,z1,x0,y0,z0,u0,v0,u1,v1);
        t.setNormal(1,0,0);quad(t,x1,y1,z1,x1,y1,z0,x1,y0,z0,x1,y0,z1,u0,v0,u1,v1);
        // Official model uses the 0..2 square at the top of both tubes.
        t.setNormal(0,1,0);quad(t,x0,y1,z1,x1,y1,z1,x1,y1,z0,x0,y1,z0,0,0,2F/16F,2F/16F);
        if(y0 < .5F){t.setNormal(0,-1,0);quad(t,x0,y0,z0,x1,y0,z0,x1,y0,z1,x0,y0,z1,
                0,13F/16F,2F/16F,15F/16F);}
        t.draw();
    }

    private static void quad(Tessellator t,float x0,float y0,float z0,float x1,float y1,float z1,
                             float x2,float y2,float z2,float x3,float y3,float z3,
                             float u0,float v0,float u1,float v1){
        t.addVertexWithUV(x0,y0,z0,u0,v0);t.addVertexWithUV(x1,y1,z1,u1,v0);
        t.addVertexWithUV(x2,y2,z2,u1,v1);t.addVertexWithUV(x3,y3,z3,u0,v1);
    }
}
