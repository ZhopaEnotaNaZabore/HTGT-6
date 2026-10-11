package com.mod.htgt6.client.inventory112;

import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandSystem;
import com.mod.htgt6.asm.HTGT6Config;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Items;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.ShieldItemRenderer;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.ShieldRenderTransforms120;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.ShieldRenderTransforms120.Transform;
import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;

/** Native off-hand rendering for the HUD, first person and other players. */
public final class OffhandRenderer112 extends Gui {
    private static final ResourceLocation WIDGETS = new ResourceLocation("htgt6", "textures/gui/widgets_1_12.png");
    private static final Minecraft minecraft = Minecraft.getMinecraft();
    private static ItemStack offhandToRender;
    private static float equippedProgress;
    private static float previousEquippedProgress;

    /** ItemRenderer.updateEquippedItem offhand branch from 1.12.2. */
    public static void updateEquippedItem() {
        previousEquippedProgress = equippedProgress;
        if (minecraft.thePlayer == null) {
            offhandToRender = null;
            equippedProgress = 0.0F;
            previousEquippedProgress = 0.0F;
            return;
        }
        ItemStack current = OffhandHooks.getOffhand(minecraft.thePlayer);
        boolean same = sameRenderedItem(offhandToRender, current);
        float change = (same ? 1.0F : 0.0F) - equippedProgress;
        if (change < -0.4F) change = -0.4F;
        if (change > 0.4F) change = 0.4F;
        equippedProgress += change;
        if (equippedProgress < 0.1F) offhandToRender = current == null ? null : current.copy();
    }

    /** Commits an already predicted F-swap without replaying a deep unequip/re-equip dip. */
    public static void acceptPredictedSwap(ItemStack current) {
        offhandToRender = current == null ? null : current.copy();
        equippedProgress = 1.0F;
        previousEquippedProgress = 1.0F;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onHud(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.HOTBAR || minecraft.thePlayer == null) return;
        ItemStack stack = OffhandHooks.getOffhand(minecraft.thePlayer);
        if (stack == null) return;
        ScaledResolution resolution = event.resolution;
        int center = resolution.getScaledWidth() / 2;
        int y = resolution.getScaledHeight() - 23;
        boolean right = "RIGHT".equalsIgnoreCase(HTGT6Config.INVENTORY_112_HUD_OFFHAND_SIDE);
        int frameX = right ? center + 91 : center - 121;
        int itemX = right ? center + 97 : center - 117;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT
                | GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPushMatrix();
        minecraft.getTextureManager().bindTexture(WIDGETS);
        drawTexturedModalRect(frameX, y, right ? 53 : 24, 22, 29, 24);
        RenderHelper.enableGUIStandardItemLighting();
        RenderItem itemRenderer = RenderItem.getInstance();
        float previousZ = itemRenderer.zLevel;
        itemRenderer.zLevel = 100.0F;
        itemRenderer.renderItemAndEffectIntoGUI(minecraft.fontRenderer, minecraft.getTextureManager(),
                stack, itemX, y + 3);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.0F, 200.0F);
        itemRenderer.renderItemOverlayIntoGUI(minecraft.fontRenderer, minecraft.getTextureManager(),
                stack, itemX, y + 3);
        if (stack.getItem() == CombatContent.shield) {
            int cooldownHeight = cooldownOverlayHeight(
                    OffhandSystem.getShieldCooldownTicks(minecraft.thePlayer),
                    com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.ShieldMechanics120.DISABLE_COOLDOWN);
            if (cooldownHeight > 0) {
                GL11.glTranslatef(0.0F, 0.0F, 25.0F);
                drawRect(itemX, y + 19 - cooldownHeight, itemX + 16, y + 19, 0x7FFFFFFF);
            }
        }
        GL11.glPopMatrix();
        itemRenderer.zLevel = previousZ;
        RenderHelper.disableStandardItemLighting();
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    /** Called from the transformed ItemRenderer after vanilla has configured the hand projection. */
    public static void renderFirstPersonBackHand(float partialTicks) {
        if (minecraft.thePlayer == null) return;
        ItemStack stack = offhandToRender;
        if (stack == null) return;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT
                | GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glPushMatrix();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableStandardItemLighting();
        boolean blockingShield = stack.getItem() == CombatContent.shield
                && Inventory112ClientHandler.isLocalShieldRaised();
        float progress = previousEquippedProgress
                + (equippedProgress - previousEquippedProgress) * partialTicks;
        float equipOffset = 1.0F - progress;
        // ItemRenderer.transformSideFirstPerson(LEFT, equipOffset), 1.12.2.
        GL11.glTranslatef(-0.56F, -0.52F - equipOffset * 0.6F, -0.72F);
        if (stack.getItem() == CombatContent.shield) {
            // Modern builtin/entity model units: no legacy 0.4 item scale.
            if (blockingShield) {
                applyShieldTransform(ShieldRenderTransforms120.FIRST_PERSON_LEFT_BLOCKING, true);
            } else {
                applyShieldTransform(ShieldRenderTransforms120.FIRST_PERSON_LEFT, true);
            }
            ShieldItemRenderer.renderRawModel(stack);
        } else if(stack.getItem() instanceof com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow){
            com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.CrossbowItemRenderer139.renderOffhand(stack,minecraft.thePlayer,true);
        } else {
            // ItemRenderer 1.7.10 expects the caller to apply the hand's 0.4 model scale.
            GL11.glScalef(0.4F, 0.4F, 0.4F);
            renderPasses(minecraft.thePlayer, stack, IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON);
        }
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerPre(RenderPlayerEvent.Pre event) {
        ItemStack stack = OffhandHooks.getOffhand(event.entityPlayer);
        if (stack == null) return;
        boolean raisedShield = stack.getItem() == CombatContent.shield
                && (event.entityPlayer == minecraft.thePlayer
                ? Inventory112ClientHandler.isLocalShieldRaised()
                : com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandSystem.isClientShieldBlocking(event.entityPlayer));
        int pose = raisedShield ? 3 : 1;
        event.renderer.modelBipedMain.heldItemLeft = pose;
        event.renderer.modelArmor.heldItemLeft = pose;
        event.renderer.modelArmorChestplate.heldItemLeft = pose;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onThirdPerson(RenderPlayerEvent.Specials.Pre event) {
        ItemStack stack = OffhandHooks.getOffhand(event.entityPlayer);
        ItemStack main = event.entityPlayer.inventory.getCurrentItem();
        boolean mainShield=main!=null&&main.getItem()==CombatContent.shield;
        if(mainShield)event.renderItem=false;
        if(stack == null&&!mainShield) return;
        try (RenderStateGuard112 ignored=RenderStateGuard112.capture()) {
            if(mainShield){GL11.glPushMatrix();try{
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);GL11.glColor4f(1,1,1,1);
                if(event.entityPlayer.isSneaking())GL11.glTranslatef(0.0F,0.2F,0.0F);
                event.renderer.modelBipedMain.bipedRightArm.postRender(0.0625F);
                renderThirdPersonShield112(event.entityPlayer,main,false);
            }finally{GL11.glPopMatrix();}}
            if(stack!=null){GL11.glPushMatrix();try {
                    GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                    GL11.glColor4f(1, 1, 1, 1);
                    if (event.entityPlayer.isSneaking()) GL11.glTranslatef(0.0F, 0.2F, 0.0F);
                    event.renderer.modelBipedMain.bipedLeftArm.postRender(0.0625F);
                    if (stack.getItem() == CombatContent.shield) renderThirdPersonShield112(event.entityPlayer, stack,true);
                    else if(stack.getItem() instanceof com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow)
                        com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.CrossbowItemRenderer139.renderOffhand(stack,event.entityPlayer,false);
                    else {
                        GL11.glTranslatef(0.0625F, 0.4375F, 0.0625F);
                        applyMirroredThirdPersonTransform(event.entityPlayer, stack);
                        renderPasses(event.entityPlayer, stack, IItemRenderer.ItemRenderType.EQUIPPED);
                    }
                } finally {
                    GL11.glColor4f(1, 1, 1, 1);
                    GL11.glPopMatrix();
                }}
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerPost(RenderPlayerEvent.Post event) {
        if (OffhandHooks.getOffhand(event.entityPlayer) == null) return;
        event.renderer.modelBipedMain.heldItemLeft = 0;
        event.renderer.modelArmor.heldItemLeft = 0;
        event.renderer.modelArmorChestplate.heldItemLeft = 0;
    }

    private void applyMirroredThirdPersonTransform(net.minecraft.entity.player.EntityPlayer player, ItemStack stack) {
        IItemRenderer custom = MinecraftForgeClient.getItemRenderer(stack, IItemRenderer.ItemRenderType.EQUIPPED);
        boolean customBlock = custom != null && custom.shouldUseRenderHelper(
                IItemRenderer.ItemRenderType.EQUIPPED, stack, IItemRenderer.ItemRendererHelper.BLOCK_3D);
        if (customBlock || stack.getItem() instanceof ItemBlock
                && RenderBlocks.renderItemIn3d(Block.getBlockFromItem(stack.getItem()).getRenderType())) {
            float scale = 0.375F;
            GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
            GL11.glRotatef(20.0F, 1, 0, 0);
            GL11.glRotatef(-45.0F, 0, 1, 0);
            GL11.glScalef(scale, -scale, scale);
        } else if (stack.getItem() == Items.bow) {
            float scale = 0.625F;
            GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
            GL11.glRotatef(20.0F, 0, 1, 0);
            GL11.glScalef(scale, -scale, scale);
            GL11.glRotatef(-100.0F, 1, 0, 0);
            GL11.glRotatef(-45.0F, 0, 1, 0);
        } else if (stack.getItem().isFull3D()) {
            float scale = 0.625F;
            if (stack.getItem().shouldRotateAroundWhenRendering()) {
                GL11.glRotatef(180.0F, 0, 0, 1);
                GL11.glTranslatef(0.0F, -0.125F, 0.0F);
            }
            GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
            GL11.glScalef(scale, -scale, scale);
            GL11.glRotatef(-100.0F, 1, 0, 0);
            GL11.glRotatef(-45.0F, 0, 1, 0);
        } else {
            float scale = 0.375F;
            GL11.glTranslatef(-0.25F, 0.1875F, -0.1875F);
            GL11.glScalef(scale, scale, scale);
            GL11.glRotatef(-60.0F, 0, 0, 1);
            GL11.glRotatef(-90.0F, 1, 0, 0);
            GL11.glRotatef(-20.0F, 0, 0, 1);
        }
    }

    /** Exact third-person transforms from Minecraft 1.20.2 shield JSON. */
    private static void renderThirdPersonShield112(net.minecraft.entity.player.EntityPlayer player, ItemStack stack,
                                                    boolean left) {
        // LayerHeldItem.renderHeldItem, adapted to the 1.7 arm model.
        GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(left?-0.0625F:0.0625F, 0.125F, -0.625F);
        boolean raised = left ? (player == minecraft.thePlayer ? Inventory112ClientHandler.isLocalShieldRaised()
                : com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandSystem.isClientShieldBlocking(player))
                : ShieldItemRenderer.isMainHandBlocking(player);
        if(raised)applyShieldTransform(left?ShieldRenderTransforms120.THIRD_PERSON_LEFT_BLOCKING
                :ShieldRenderTransforms120.THIRD_PERSON_RIGHT_BLOCKING, left);
        else applyShieldTransform(left?ShieldRenderTransforms120.THIRD_PERSON_LEFT
                :ShieldRenderTransforms120.THIRD_PERSON_RIGHT, left);
        ShieldItemRenderer.renderRawModel(stack);
    }

    public static void applyItemTransform(Transform transform) {
        applyItemTransform(transform.x / 16.0F, transform.y / 16.0F, transform.z / 16.0F,
                transform.rotationX, transform.rotationY, transform.rotationZ, transform.scale);
    }

    /** ItemTransform.apply(leftHand), followed by ItemRenderer's model origin translation. */
    public static void applyShieldTransform(Transform transform, boolean left) {
        float[] values = ShieldRenderTransforms120.components(transform, left);
        applyItemTransform(values[0], values[1], values[2], values[3], values[4], values[5], values[6]);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
    }

    /** Forge 1.12 ItemCameraTransforms quaternion path, preserved on LWJGL 2. */
    private static void applyItemTransform(float tx, float ty, float tz,
            float rx, float ry, float rz, float scale) {
        GL11.glTranslatef(tx, ty, tz);
        float x = (float) Math.toRadians(rx) * 0.5F;
        float y = (float) Math.toRadians(ry) * 0.5F;
        float z = (float) Math.toRadians(rz) * 0.5F;
        float sx = (float) Math.sin(x), cx = (float) Math.cos(x);
        float sy = (float) Math.sin(y), cy = (float) Math.cos(y);
        float sz = (float) Math.sin(z), cz = (float) Math.cos(z);
        float qx = sx * cy * cz + cx * sy * sz;
        float qy = cx * sy * cz - sx * cy * sz;
        float qz = sx * sy * cz + cx * cy * sz;
        float qw = cx * cy * cz - sx * sy * sz;
        FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
        matrix.put(1 - 2 * qy * qy - 2 * qz * qz).put(2 * qx * qy + 2 * qw * qz)
                .put(2 * qx * qz - 2 * qw * qy).put(0);
        matrix.put(2 * qx * qy - 2 * qw * qz).put(1 - 2 * qx * qx - 2 * qz * qz)
                .put(2 * qy * qz + 2 * qw * qx).put(0);
        matrix.put(2 * qx * qz + 2 * qw * qy).put(2 * qy * qz - 2 * qw * qx)
                .put(1 - 2 * qx * qx - 2 * qy * qy).put(0);
        matrix.put(0).put(0).put(0).put(1).flip();
        GL11.glMultMatrix(matrix);
        GL11.glScalef(scale, scale, scale);
    }

    private static void renderPasses(net.minecraft.entity.player.EntityPlayer player, ItemStack stack,
            IItemRenderer.ItemRenderType type) {
        // A Forge custom renderer owns all of its layers. Replaying it for every
        // legacy icon pass made the complete banner render twice in the offhand.
        IItemRenderer custom = MinecraftForgeClient.getItemRenderer(stack, type);
        int iconPasses = stack.getItem().requiresMultipleRenderPasses()
                ? stack.getItem().getRenderPasses(stack.getItemDamage()) : 1;
        int passes = offhandRenderPassCount(custom != null, iconPasses);
        for (int pass = 0; pass < passes; ++pass) {
            int color = stack.getItem().getColorFromItemStack(stack, pass);
            GL11.glColor4f((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F,
                    (color & 255) / 255.0F, 1.0F);
            minecraft.entityRenderer.itemRenderer.renderItem(player, stack, pass, type);
        }
    }

    public static int offhandRenderPassCount(boolean customRenderer, int iconPasses) {
        return customRenderer ? 1 : Math.max(1, iconPasses);
    }

    /** Modern item-cooldown overlay height in a 16-pixel hotbar icon. */
    public static int cooldownOverlayHeight(int remainingTicks, int totalTicks) {
        if (remainingTicks <= 0 || totalTicks <= 0) return 0;
        return Math.min(16, (int) Math.ceil(16.0D * remainingTicks / totalTicks));
    }

    private static boolean sameRenderedItem(ItemStack first, ItemStack second) {
        if (first == second) return true;
        if (first == null || second == null) return false;
        return first.getItem() == second.getItem()
                && first.getItemDamage() == second.getItemDamage()
                && ItemStack.areItemStackTagsEqual(first, second);
    }
}
