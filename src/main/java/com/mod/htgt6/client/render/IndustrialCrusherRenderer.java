package com.mod.htgt6.client.render;

import com.mod.htgt6.common.crusher.MultiTileIndustrialCrusher;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import java.util.Map;
import java.util.WeakHashMap;

/** Dedicated 7x6x16 crusher mechanism; fixed draw batches, no spawned items/particles. */
public final class IndustrialCrusherRenderer extends TileEntitySpecialRenderer {
    private static final ResourceLocation METAL = new ResourceLocation("htgt6", "textures/blocks/industrial_crusher_inner.png");
    private final Map<TileEntity, Motion> motions = new WeakHashMap<TileEntity, Motion>();
    private final RenderItem cargoRenderer = new RenderItem() {
        @Override public boolean shouldBob() { return false; }
        @Override public boolean shouldSpreadItems() { return false; }
        @Override public byte getMiniBlockCount(ItemStack stack, byte count) { return 1; }
        @Override public byte getMiniItemCount(ItemStack stack, byte count) { return 1; }
    };
    private EntityItem displayCargo;

    public IndustrialCrusherRenderer() {
        cargoRenderer.setRenderManager(RenderManager.instance);
    }

    private static final class Motion { double last = -1, angle; }

    @Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (!(tile instanceof MultiTileIndustrialCrusher)) return;
        MultiTileIndustrialCrusher machine = (MultiTileIndustrialCrusher) tile;
        if (machine.getWorldObj() == null || !machine.isStructureFormed()) return;
        boolean active = machine.isConveyorRunning() || machine.isMachineActive() && machine.getStructureDiagnostic().startsWith("PROCESSING:");
        float progress = machine.getProcessProgressFraction(partialTicks);
        Motion motion = motions.get(tile);
        if (motion == null) { motion = new Motion(); motions.put(tile, motion); }
        double now = MultiblockAnimationClock.frameTime(machine.getWorldObj().getTotalWorldTime(), partialTicks);
        if (motion.last >= 0 && active) motion.angle = (motion.angle + Math.max(0, Math.min(2, now - motion.last)) * 12) % 360;
        motion.last = now;
        int oldTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        float lightX = OpenGlHelper.lastBrightnessX, lightY = OpenGlHelper.lastBrightnessY;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(x + .5, y, z + .5);
            int facing = machine.getBlockMetadata();
            if (facing == 3) GL11.glRotatef(180, 0, 1, 0);
            else if (facing == 4) GL11.glRotatef(90, 0, 1, 0);
            else if (facing == 5) GL11.glRotatef(-90, 0, 1, 0);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 220, 220);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_CULL_FACE);
            bindTexture(METAL);
            Tessellator t = Tessellator.instance;
            t.startDrawingQuads();
            // Input deck and lower product conveyor, separate levels with a gravity chute.
            belt(t, .52, 6.25, 3.10, motion.angle / 360D * .40, true);
            belt(t, 8.55, 14.98, 1.28, motion.angle / 360D * .40, false);
            for (int side = -1; side <= 1; side += 2) {
                double a = side < 0 ? -2.35 : 1.93;
                box(t, a, 1.02, .6, a + .42, 2.98, 5.95, .55F);
                for (int vent = 0; vent < 5; vent++)
                    box(t, a - .008, 1.28 + vent * .24, 1.1, a + .435, 1.35 + vent * .24, 5.4, .22F);
                for (int support = 0; support < 3; support++)
                    box(t, a, 1.02, 9 + support * 2.3, a + .4, 1.18, 9.3 + support * 2.3, .6F);
                // Bearings hold the transverse shafts on both sides of the feed path.
                for (double rz : new double[] {7.1, 8.7}) {
                    box(t, a, 2.50, rz - .40, a + .42, 4.45, rz + .40, .48F);
                    box(t, a - .06, 3.38, rz - .28, a + .48, 3.96, rz + .28, .90F);
                }
            }
            // Fixed rear motor and gearbox over the output belt.
            box(t, -1.8, 2.48, 10.25, 1.8, 4.60, 11.50, .49F);
            box(t, -1.30, 2.72, 11.50, 1.30, 4.35, 14.50, .63F);
            for (int fin = 0; fin < 12; fin++)
                box(t, -1.46, 2.60, 11.7 + fin * .21, 1.46, 4.46, 11.78 + fin * .21, .39F);
            box(t, -2.42, 4.76, 6.60, 2.42, 4.94, 9.55, .57F);
            box(t, -2.0, 1.55, 8.65, -1.75, 2.52, 9.7, .76F);
            box(t, 1.75, 1.55, 8.65, 2.0, 2.52, 9.7, .76F);
            // Sloping chute from the roller nip to the lower belt.
            shade(t, .82F);
            quad(t, -1.75, 2.65, 7.80, 1.75, 2.65, 7.80, 1.75, 1.43, 9.65, -1.75, 1.43, 9.65);
            t.draw();
            roller(7.1, motion.angle);
            roller(8.7, -motion.angle + 18);
            boolean loaded = machine.getCurrentRecipeItemInputCount() > 0;
            if (loaded && progress < .45F) {
                int count = machine.getCurrentRecipeItemInputCount();
                drawStack(machine, machine.getCurrentRecipeItemInputForRender(IndustrialCrusherCycle.component(progress, count)),
                        0, 3.20, IndustrialCrusherCycle.inputZ(progress, count), .40);
            } else if (loaded && progress < .65F) {
                ItemStack input = machine.getCurrentRecipeItemInputForRender(0);
                for (int i = 0; i < 4; i++) {
                    double p = (progress - .45) / .20;
                    drawStack(machine, input, (i - 1.5) * .40, 3.42 - p * 1.65, 7.9 + p * 1.1, .13);
                }
            } else if (loaded) {
                drawStack(machine, machine.getCurrentRecipePrimaryOutputForRender(), 0, 1.43,
                        IndustrialCrusherCycle.outputZ(progress), .28);
            }
            ItemStack mobDrop=machine.getMobOutputForRender();
            if(mobDrop!=null){
                float transit=machine.getMobOutputTicksForRender(partialTicks);
                drawStack(machine,mobDrop,-.65,
                        com.mod.htgt6.common.crusher.CrusherConveyorRules.outputY(transit),
                        com.mod.htgt6.common.crusher.CrusherConveyorRules.outputZ(transit),.28);
            }
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            t.startDrawingQuads();
            t.setColorOpaque_F(active ? .1F : .12F, active ? .95F : .32F, active ? 1F : .35F);
            quad(t, -2.44, 4.0, 10.4, -2.44, 4.5, 10.4, -2.44, 4.5, 10.6, -2.44, 4.0, 10.6);
            t.draw();
        } finally {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightX, lightY);
            GL11.glPopMatrix();
            GL11.glPopAttrib();
            OpenGlHelper.setActiveTexture(oldTexture);
        }
    }

    private static void belt(Tessellator t, double start, double end, double y, double phase, boolean input) {
        double width = input ? 1.55 : 1.30;
        box(t, -width - .18, y - .27, start, width + .18, y - .09, end, .50F);
        box(t, -width, y - .09, start, width, y, end, .18F);
        box(t, -width - .20, y, start, -width, y + .17, end, .67F);
        box(t, width, y, start, width + .20, y + .17, end, .67F);
        double offset = phase % .40;
        for (double rz = start + offset; rz < end - .05; rz += .40)
            box(t, -width + .04, y, rz, width - .04, y + .025, Math.min(end, rz + .045), .40F);
    }

    private void roller(double z, double angle) {
        bindTexture(METAL);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(0, 3.65, z);
            GL11.glRotated(angle, 1, 0, 0);
            Tessellator t = Tessellator.instance;
            t.startDrawingQuads();
            box(t, -2.45, -.14, -.14, 2.45, .14, .14, .86F);
            for (int segment = 0; segment < 16; segment++) {
                double a = segment * Math.PI / 8, b = (segment + 1) * Math.PI / 8;
                double ya = Math.cos(a) * .67, za = Math.sin(a) * .67;
                double yb = Math.cos(b) * .67, zb = Math.sin(b) * .67;
                shade(t, (float) (.65 + .22 * Math.cos(a)));
                quad(t, -1.92, ya, za, 1.92, ya, za, 1.92, yb, zb, -1.92, yb, zb);
                quad(t, -1.92, 0, 0, -1.92, ya, za, -1.92, yb, zb, -1.92, 0, 0);
                quad(t, 1.92, 0, 0, 1.92, ya, za, 1.92, yb, zb, 1.92, 0, 0);
            }
            for (int row = 0; row < 8; row++) for (int tooth = 0; tooth < 8; tooth++) {
                double a = tooth * Math.PI / 4 + (row % 2) * Math.PI / 8;
                double cy = Math.cos(a) * .73, cz = Math.sin(a) * .73;
                double xx = -1.85 + row * .47;
                box(t, xx, cy - .14, cz - .14, xx + .26, cy + .14, cz + .14, .94F);
            }
            t.draw();
        } finally { GL11.glPopMatrix(); }
    }

    private void drawStack(MultiTileIndustrialCrusher machine, ItemStack stack,
                           double x, double y, double z, double size) {
        if (stack == null || stack.getItem() == null) return;
        if (displayCargo == null) displayCargo = new EntityItem(machine.getWorldObj());
        // Render-only reusable object: never spawned, ticked, picked up or added to the world.
        displayCargo.worldObj = machine.getWorldObj();
        ItemStack displayed = stack.copy();
        displayed.stackSize = 1;
        displayCargo.setEntityItemStack(displayed);
        displayCargo.age = 0;
        displayCargo.hoverStart = 0F;

        Block block = stack.getItem() instanceof ItemBlock ? Block.getBlockFromItem(stack.getItem()) : null;
        IItemRenderer custom = MinecraftForgeClient.getItemRenderer(stack, IItemRenderer.ItemRenderType.ENTITY);
        boolean blockModel = block != null && RenderBlocks.renderItemIn3d(block.getRenderType())
                || custom != null && custom.shouldUseRenderHelper(IItemRenderer.ItemRenderType.ENTITY,
                        stack, IItemRenderer.ItemRendererHelper.BLOCK_3D);
        int renderType = block == null ? 1 : block.getRenderType();
        boolean largeNativeScale = renderType == 1 || renderType == 19 || renderType == 12 || renderType == 2;
        double scale = size * (blockModel && !largeNativeScale ? 8D : 4D);
        int oldTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        float lightX = OpenGlHelper.lastBrightnessX, lightY = OpenGlHelper.lastBrightnessY;
        boolean inFrame = RenderItem.renderInFrame;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(x, y + (blockModel ? size : size * .5), z);
            GL11.glScaled(scale, scale, scale);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 220F, 220F);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, .1F);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            GL11.glColor4f(1F, 1F, 1F, 1F);
            RenderItem.renderInFrame = false;
            // Forge dispatches GT6's custom block renderer, all item passes and material tints.
            // Reading getIconIndex alone loses the stone base and shows only the ore overlay.
            cargoRenderer.doRender(displayCargo, 0, 0, 0, 0F, 0F);
        } finally {
            RenderItem.renderInFrame = inFrame;
            displayCargo.worldObj = null;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightX, lightY);
            GL11.glPopMatrix();
            GL11.glPopAttrib();
            OpenGlHelper.setActiveTexture(oldTexture);
        }
    }

    private static void shade(Tessellator t, float c) { t.setColorOpaque_F(c, c, c); }
    private static void box(Tessellator t, double a,double b,double c,double d,double e,double f,float shade) {
        shade(t, shade);
        quad(t,a,b,c,d,b,c,d,e,c,a,e,c); quad(t,a,b,f,d,b,f,d,e,f,a,e,f);
        quad(t,a,b,c,a,b,f,a,e,f,a,e,c); quad(t,d,b,c,d,b,f,d,e,f,d,e,c);
        quad(t,a,e,c,d,e,c,d,e,f,a,e,f); quad(t,a,b,c,d,b,c,d,b,f,a,b,f);
    }
    private static void quad(Tessellator t,double a,double b,double c,double d,double e,double f,double g,double h,double i,double j,double k,double l) {
        t.addVertexWithUV(a,b,c,0,0); t.addVertexWithUV(d,e,f,1,0); t.addVertexWithUV(g,h,i,1,1); t.addVertexWithUV(j,k,l,0,1);
    }
}
