package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.TileEntityBountifulBannerRenderer;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Bountiful.BountifulBannerColors;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.ShieldBannerData120;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/** Layered Java Edition shield renderer for inventory, main hand and transformed offhand paths. */
public final class ShieldItemRenderer implements IItemRenderer {
    private static final ResourceLocation PLAIN_TEXTURE = new ResourceLocation(
            "htgt6", "textures/entity/combat_update/shield_base_nopattern.png");
    private static final ResourceLocation DECORATED_TEXTURE = new ResourceLocation(
            "htgt6", "textures/entity/combat_update/shield_base.png");
    private static final ResourceLocation DECORATION_BASE = new ResourceLocation(
            "htgt6", "textures/entity/combat_update/shield/base.png");
    private static final ModelShield SHIELD = new ModelShield();

    @Override public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type == ItemRenderType.ENTITY || type == ItemRenderType.INVENTORY || type == ItemRenderType.EQUIPPED
                || type == ItemRenderType.EQUIPPED_FIRST_PERSON;
    }

    @Override public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
            ItemRendererHelper helper) {
        if (type == ItemRenderType.INVENTORY) return helper == ItemRendererHelper.INVENTORY_BLOCK;
        if (type == ItemRenderType.ENTITY) {
            return helper == ItemRendererHelper.BLOCK_3D
                    || helper == ItemRendererHelper.ENTITY_BOBBING
                    || helper == ItemRendererHelper.ENTITY_ROTATION;
        }
        return (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON)
                && (helper == ItemRendererHelper.BLOCK_3D || helper == ItemRendererHelper.EQUIPPED_BLOCK);
    }

    @Override public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        // Draw first person only at the ItemRenderer tail, outside its legacy sword matrix.
        if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) return;
        try (RenderStateGuard112 ignored = RenderStateGuard112.capture()) {
            GL11.glPushMatrix();
            try {
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                // The model is authored around its centre while Forge expects custom
                // inventory/block renderers in the local 0..1 cube.
                if (type != ItemRenderType.ENTITY) GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                if (type == ItemRenderType.INVENTORY) {
                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                } else {
                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                }
                renderModel(item);
            } finally {
                GL11.glPopMatrix();
            }
        }
    }

    private static final class EquippedFields {
        static final java.lang.reflect.Field STACK = cpw.mods.fml.relauncher.ReflectionHelper.findField(
                net.minecraft.client.renderer.ItemRenderer.class, "itemToRender", "field_78453_b");
        static final java.lang.reflect.Field CURRENT = cpw.mods.fml.relauncher.ReflectionHelper.findField(
                net.minecraft.client.renderer.ItemRenderer.class, "equippedProgress", "field_78454_c");
        static final java.lang.reflect.Field PREVIOUS = cpw.mods.fml.relauncher.ReflectionHelper.findField(
                net.minecraft.client.renderer.ItemRenderer.class, "prevEquippedProgress", "field_78451_d");
    }

    /** The same camera-space entry as offhand, after the legacy main-hand matrix is popped. */
    public static void renderFirstPersonMainHand(Object renderer, float partialTicks) {
        net.minecraft.entity.player.EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null || player.isInvisible()) return;
        try {
            ItemStack stack = (ItemStack) EquippedFields.STACK.get(renderer);
            if (stack == null || stack.getItem() != com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.shield) return;
            float previous = EquippedFields.PREVIOUS.getFloat(renderer);
            float progress = previous + (EquippedFields.CURRENT.getFloat(renderer) - previous) * partialTicks;
            try (RenderStateGuard112 ignored = RenderStateGuard112.capture()) {
                GL11.glPushMatrix();
                try {
                    net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
                    boolean blocking = isMainHandBlocking(player);
                    float swing = blocking ? 0.0F : player.getSwingProgress(partialTicks);
                    float rootSwing = (float) Math.sin(Math.sqrt(swing) * Math.PI);
                    GL11.glTranslatef(-0.4F * rootSwing,
                            0.2F * (float) Math.sin(Math.sqrt(swing) * Math.PI * 2),
                            -0.2F * (float) Math.sin(swing * Math.PI));
                    GL11.glTranslatef(0.56F, -0.52F - (1.0F - progress) * 0.6F, -0.72F);
                    GL11.glRotatef(45.0F - 20.0F * (float) Math.sin(swing * swing * Math.PI), 0, 1, 0);
                    GL11.glRotatef(-20.0F * rootSwing, 0, 0, 1);
                    GL11.glRotatef(-80.0F * rootSwing, 1, 0, 0);
                    GL11.glRotatef(-45.0F, 0, 1, 0);
                    com.mod.htgt6.client.inventory112.OffhandRenderer112.applyShieldTransform(
                            blocking ? ShieldRenderTransforms120.FIRST_PERSON_RIGHT_BLOCKING
                                    : ShieldRenderTransforms120.FIRST_PERSON_RIGHT, false);
                    renderRawModel(stack);
                } finally { GL11.glPopMatrix(); }
            }
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to read the equipped shield animation", exception);
        }
    }

    public static boolean isMainHandBlocking(net.minecraft.entity.player.EntityPlayer player) {
        ItemStack main = player.getHeldItem();
        return main != null && main.getItem() == com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.shield
                && (player.isUsingItem() || player.isEating())
                && !com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandSystem.isShieldDisabled(player);
    }

    /** Raw 1.12 TileEntityItemStackRenderer shield path, with no Forge block helper offsets. */
    public static void renderRawModel(ItemStack stack) {
        try (RenderStateGuard112 ignored = RenderStateGuard112.capture()) {
            GL11.glPushMatrix();
            try {
                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glScalef(1.0F, -1.0F, -1.0F);
                renderModel(stack);
            } finally {
                GL11.glPopMatrix();
            }
        }
    }

    private static void renderModel(ItemStack stack) {
        if (!ShieldBannerData120.hasDecoration(stack)) {
            bind(PLAIN_TEXTURE);
            SHIELD.renderAll();
            return;
        }
        bind(DECORATED_TEXTURE);
        GL11.glColor4f(1, 1, 1, 1);
        SHIELD.renderAll();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(-1.0F, -1.0F);
        bind(DECORATION_BASE);
        color(BountifulBannerColors.RGB[ShieldBannerData120.baseColor(stack)]);
        SHIELD.renderPlate();
        NBTTagList patterns = ShieldBannerData120.patterns(stack);
        for (int i = 0; i < patterns.tagCount() && i < ShieldBannerData120.MAX_PATTERNS; ++i) {
            NBTTagCompound pattern = patterns.getCompoundTagAt(i);
            String texture = TileEntityBountifulBannerRenderer.textureName(pattern.getString("Pattern"));
            if (texture == null) continue;
            bind(new ResourceLocation("htgt6", "textures/entity/combat_update/shield/" + texture + ".png"));
            color(BountifulBannerColors.RGB[pattern.getInteger("Color") & 15]);
            SHIELD.renderPlate();
        }
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1, 1, 1, 1);
    }

    private static void bind(ResourceLocation texture) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
    }

    private static void color(int rgb) {
        GL11.glColor4f(((rgb >> 16) & 255) / 255.0F, ((rgb >> 8) & 255) / 255.0F,
                (rgb & 255) / 255.0F, 1.0F);
    }

    private static final class ModelShield extends ModelBase {
        private final ModelRenderer plate;
        private final ModelRenderer handle;

        private ModelShield() {
            textureWidth = 64;
            textureHeight = 64;
            plate = new ModelRenderer(this, 0, 0);
            plate.addBox(-6.0F, -11.0F, -2.0F, 12, 22, 1, 0.0F);
            handle = new ModelRenderer(this, 26, 0);
            handle.addBox(-1.0F, -3.0F, -1.0F, 2, 6, 6, 0.0F);
        }

        private void renderAll() {
            plate.render(0.0625F);
            handle.render(0.0625F);
        }
        private void renderPlate() { plate.render(0.0625F); }
    }
}
