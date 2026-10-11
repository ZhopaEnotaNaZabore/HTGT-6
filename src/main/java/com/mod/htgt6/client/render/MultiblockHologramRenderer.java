package com.mod.htgt6.client.render;

import com.mod.htgt6.client.nei.structure.HTGT6MultiblockRegistry;
import com.mod.htgt6.client.nei.structure.HTGT6MultiblockStructure;
import com.mod.htgt6.api.hitech6.multiblock.FluidGatewayVariant;
import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.common.multiblock.gateway.BlockMultiblockGateway;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/** Lightweight missing-block hologram driven by the same structures used by NEI. */
@SideOnly(Side.CLIENT)
public final class MultiblockHologramRenderer {
    public static final MultiblockHologramRenderer INSTANCE = new MultiblockHologramRenderer();
    private static final double MAX_RENDER_DISTANCE_SQ = 128.0D * 128.0D;
    private Projection projection;

    private MultiblockHologramRenderer() { }

    public void toggle(World world, int x, int y, int z, EntityPlayer player) {
        if (projection != null && projection.matches(world, x, y, z)) {
            projection = null;
            player.addChatMessage(new ChatComponentTranslation("message.htgt6.hologram.disabled"));
            return;
        }

        Block controllerBlock = world.getBlock(x, y, z);
        HTGT6MultiblockStructure structure = HTGT6MultiblockRegistry.findByController(controllerBlock);
        if (structure == null) {
            player.addChatMessage(new ChatComponentTranslation("message.htgt6.hologram.unsupported"));
            return;
        }
        int facing = world.getBlockMetadata(x, y, z);
        projection = new Projection(world.provider.dimensionId, x, y, z, facing, controllerBlock, structure);
        player.addChatMessage(new ChatComponentTranslation("message.htgt6.hologram.enabled", structure.getName()));
    }

    @SubscribeEvent public void onWorldUnload(WorldEvent.Unload event) {
        if (event.world != null && event.world.isRemote && projection != null
                && event.world.provider.dimensionId == projection.dimension) projection = null;
    }

    @SubscribeEvent public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (projection == null || minecraft.theWorld == null || minecraft.thePlayer == null) return;
        World world = minecraft.theWorld;
        if (world.provider.dimensionId != projection.dimension
                || world.getBlock(projection.x, projection.y, projection.z) != projection.controllerBlock) {
            projection = null;
            return;
        }
        if (minecraft.thePlayer.getDistanceSq(projection.x + 0.5D, projection.y + 0.5D,
                projection.z + 0.5D) > MAX_RENDER_DISTANCE_SQ) return;

        float partialTicks = event.partialTicks;
        double cameraX = minecraft.thePlayer.lastTickPosX
                + (minecraft.thePlayer.posX - minecraft.thePlayer.lastTickPosX) * partialTicks;
        double cameraY = minecraft.thePlayer.lastTickPosY
                + (minecraft.thePlayer.posY - minecraft.thePlayer.lastTickPosY) * partialTicks;
        double cameraZ = minecraft.thePlayer.lastTickPosZ
                + (minecraft.thePlayer.posZ - minecraft.thePlayer.lastTickPosZ) * partialTicks;
        renderProjection(world, cameraX, cameraY, cameraZ);
    }

    private void renderProjection(World world, double cameraX, double cameraY, double cameraZ) {
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT
                | GL11.GL_LINE_BIT | GL11.GL_TEXTURE_BIT | GL11.GL_CURRENT_BIT);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        forEachMissing(world, cameraX, cameraY, cameraZ, new BoxConsumer() {
            @Override public void accept(AxisAlignedBB box, boolean obstructed,
                                         HTGT6MultiblockStructure.StructureBlock expected) {
                drawTexturedBlock(box, expected, obstructed);
            }
        });

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(1.25F);
        forEachMissing(world, cameraX, cameraY, cameraZ, new BoxConsumer() {
            @Override public void accept(AxisAlignedBB box, boolean obstructed,
                                         HTGT6MultiblockStructure.StructureBlock expected) {
                RenderGlobal.drawOutlinedBoundingBox(box, obstructed ? 0xFF382D : 0x32E6FF);
            }
        });
        GL11.glDepthMask(true);
        GL11.glPopAttrib();
    }

    private void forEachMissing(World world, double cameraX, double cameraY, double cameraZ,
                                BoxConsumer consumer) {
        HTGT6MultiblockStructure structure = projection.structure;
        for (int localY = 0; localY < structure.getHeight(); localY++) {
            for (int modelX = 0; modelX < structure.getWidth(); modelX++) {
                for (int modelZ = 0; modelZ < structure.getDepth(); modelZ++) {
                    HTGT6MultiblockStructure.StructureBlock expected = structure.getBlock(modelX, localY, modelZ);
                    if (expected == null) continue;
                    int[] position = localToWorld(modelX - structure.getControllerX(),
                            localY - structure.getControllerY(), modelZ - structure.getControllerZ());
                    if (position[0] == projection.x && position[1] == projection.y && position[2] == projection.z) continue;
                    Block actual = world.getBlock(position[0], position[1], position[2]);
                    int metadata = world.getBlockMetadata(position[0], position[1], position[2]);
                    if (expected.matches(actual, metadata)) continue;
                    boolean obstructed = actual != null && !actual.isAir(world, position[0], position[1], position[2]);
                    AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                            position[0] - cameraX + 0.025D, position[1] - cameraY + 0.025D,
                            position[2] - cameraZ + 0.025D,
                            position[0] - cameraX + 0.975D, position[1] - cameraY + 0.975D,
                            position[2] - cameraZ + 0.975D);
                    consumer.accept(box, obstructed, expected);
                }
            }
        }
    }

    private int[] localToWorld(int u, int y, int v) {
        int facing = projection.facing;
        if (facing == 3) return new int[] { projection.x - u, projection.y + y, projection.z - v };
        if (facing == 4) return new int[] { projection.x + v, projection.y + y, projection.z - u };
        if (facing == 5) return new int[] { projection.x - v, projection.y + y, projection.z + u };
        return new int[] { projection.x + u, projection.y + y, projection.z + v };
    }

    private static void drawTexturedBlock(AxisAlignedBB b,
            HTGT6MultiblockStructure.StructureBlock expected, boolean obstructed) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setBrightness(0x00F000F0);
        if (obstructed) t.setColorRGBA_F(1F, 0.22F, 0.18F, 0.38F);
        else t.setColorRGBA_F(1F, 1F, 1F, 0.34F);
        IIcon down = expected.block.getIcon(0, expected.metadata);
        IIcon up = expected.block.getIcon(1, expected.metadata);
        IIcon north = expected.block.getIcon(2, expected.metadata);
        IIcon south = expected.block.getIcon(3, expected.metadata);
        IIcon west = expected.block.getIcon(4, expected.metadata);
        IIcon east = expected.block.getIcon(5, expected.metadata);
        faceY(t, b, b.minY, down, false); faceY(t, b, b.maxY, up, true);
        faceZ(t, b, b.minZ, north, false); faceZ(t, b, b.maxZ, south, true);
        faceX(t, b, b.minX, west, false); faceX(t, b, b.maxX, east, true);
        t.draw();
        if (expected.block instanceof BlockMultiblockGateway) {
            IIcon overlay = ((BlockMultiblockGateway) expected.block).getOverlayIcon();
            if (overlay != null) {
                GatewayType type = FluidGatewayVariant.type(expected.metadata);
                float[] color = gatewayColor(type);
                AxisAlignedBB outer = b.expand(0.002D, 0.002D, 0.002D);
                t.startDrawingQuads();
                t.setBrightness(0x00F000F0);
                t.setColorRGBA_F(color[0], color[1], color[2], obstructed ? 0.42F : 0.72F);
                faceY(t, outer, outer.minY, overlay, false); faceY(t, outer, outer.maxY, overlay, true);
                faceZ(t, outer, outer.minZ, overlay, false); faceZ(t, outer, outer.maxZ, overlay, true);
                faceX(t, outer, outer.minX, overlay, false); faceX(t, outer, outer.maxX, overlay, true);
                t.draw();
            }
        }
    }

    private static float[] gatewayColor(GatewayType type) {
        switch (type) {
            case FLUID_OUTPUT: return new float[] { 1.0F, 0.55F, 0.15F };
            case ITEM_INPUT: return new float[] { 0.30F, 1.0F, 0.45F };
            case ITEM_OUTPUT: return new float[] { 0.85F, 0.35F, 1.0F };
            case ENERGY_INPUT: return new float[] { 1.0F, 0.90F, 0.20F };
            case ENERGY_OUTPUT: return new float[] { 0.25F, 0.70F, 1.0F };
            default: return new float[] { 0.25F, 0.90F, 1.0F };
        }
    }

    private static void faceY(Tessellator t, AxisAlignedBB b, double y, IIcon i, boolean reverse) {
        if (i == null) return;
        if (!reverse) {
            t.addVertexWithUV(b.minX,y,b.minZ,i.getMinU(),i.getMinV()); t.addVertexWithUV(b.maxX,y,b.minZ,i.getMaxU(),i.getMinV());
            t.addVertexWithUV(b.maxX,y,b.maxZ,i.getMaxU(),i.getMaxV()); t.addVertexWithUV(b.minX,y,b.maxZ,i.getMinU(),i.getMaxV());
        } else {
            t.addVertexWithUV(b.minX,y,b.maxZ,i.getMinU(),i.getMaxV()); t.addVertexWithUV(b.maxX,y,b.maxZ,i.getMaxU(),i.getMaxV());
            t.addVertexWithUV(b.maxX,y,b.minZ,i.getMaxU(),i.getMinV()); t.addVertexWithUV(b.minX,y,b.minZ,i.getMinU(),i.getMinV());
        }
    }
    private static void faceZ(Tessellator t, AxisAlignedBB b, double z, IIcon i, boolean reverse) {
        if (i == null) return;
        if (!reverse) {
            t.addVertexWithUV(b.maxX,b.minY,z,i.getMaxU(),i.getMaxV()); t.addVertexWithUV(b.minX,b.minY,z,i.getMinU(),i.getMaxV());
            t.addVertexWithUV(b.minX,b.maxY,z,i.getMinU(),i.getMinV()); t.addVertexWithUV(b.maxX,b.maxY,z,i.getMaxU(),i.getMinV());
        } else {
            t.addVertexWithUV(b.minX,b.minY,z,i.getMinU(),i.getMaxV()); t.addVertexWithUV(b.maxX,b.minY,z,i.getMaxU(),i.getMaxV());
            t.addVertexWithUV(b.maxX,b.maxY,z,i.getMaxU(),i.getMinV()); t.addVertexWithUV(b.minX,b.maxY,z,i.getMinU(),i.getMinV());
        }
    }
    private static void faceX(Tessellator t, AxisAlignedBB b, double x, IIcon i, boolean reverse) {
        if (i == null) return;
        if (!reverse) {
            t.addVertexWithUV(x,b.minY,b.minZ,i.getMinU(),i.getMaxV()); t.addVertexWithUV(x,b.minY,b.maxZ,i.getMaxU(),i.getMaxV());
            t.addVertexWithUV(x,b.maxY,b.maxZ,i.getMaxU(),i.getMinV()); t.addVertexWithUV(x,b.maxY,b.minZ,i.getMinU(),i.getMinV());
        } else {
            t.addVertexWithUV(x,b.minY,b.maxZ,i.getMinU(),i.getMaxV()); t.addVertexWithUV(x,b.minY,b.minZ,i.getMaxU(),i.getMaxV());
            t.addVertexWithUV(x,b.maxY,b.minZ,i.getMaxU(),i.getMinV()); t.addVertexWithUV(x,b.maxY,b.maxZ,i.getMinU(),i.getMinV());
        }
    }

    private interface BoxConsumer {
        void accept(AxisAlignedBB box, boolean obstructed,
                    HTGT6MultiblockStructure.StructureBlock expected);
    }

    private static final class Projection {
        final int dimension, x, y, z, facing;
        final Block controllerBlock;
        final HTGT6MultiblockStructure structure;

        Projection(int dimension, int x, int y, int z, int facing, Block controllerBlock,
                   HTGT6MultiblockStructure structure) {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.facing = facing;
            this.controllerBlock = controllerBlock;
            this.structure = structure;
        }

        boolean matches(World world, int x, int y, int z) {
            return world.provider.dimensionId == dimension && this.x == x && this.y == y && this.z == z;
        }
    }
}
