package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

/** Spear-oriented held/charging transforms; avoids the vanilla bow pose. */
public final class TridentItemRenderer implements IItemRenderer {
    // tx, ty, tz, rx, ry, rz, scale from the official 1.20.2 item models.
    public static final float[] FIRST_PERSON = {-3, 17, 1, 0, -90, 25, 1};
    public static final float[] THIRD_PERSON_HELD = {11, 17, -2, 0, 60, 0, 1};
    public static final float[] THIRD_PERSON_THROWING = {8, -17, 9, 0, 90, 180, 1};
    public static final float[] GROUND = {4, 4, 2, 0, 0, 0, 0.25F};
    private final ModelTrident model = new ModelTrident();

    // Inventory deliberately falls back to Item's 16x16 icon renderer.
    @Override public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type != ItemRenderType.INVENTORY;
    }
    @Override public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
                                                    ItemRendererHelper helper) {
        // Avoid Forge's icon-specific rotations/scaling; its block helper only translates by -0.5.
        return helper == ItemRendererHelper.EQUIPPED_BLOCK;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(RenderThrownTrident.TEXTURE);
        try (RenderStateGuard112 ignored = RenderStateGuard112.capture()) {
            GL11.glPushMatrix();
            try {
                GL11.glColor4f(1, 1, 1, 1);
                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);

                if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
                    GL11.glTranslatef(.5F,.5F,.5F);
                    // Undo the legacy item scale; retain its equip/swing translation.
                    GL11.glScalef(2.5F,2.5F,2.5F);
                    GL11.glRotatef(-45,0,1,0);
                    EntityPlayer player=holder(data);
                    if(isThrowing(player,item)) {
                        float ticks=player.getItemInUseDuration();
                        float charge=Math.min(1,ticks/10F);
                        GL11.glTranslatef(-.5F,.7F,.1F);
                        GL11.glRotatef(-55,1,0,0);
                        GL11.glRotatef(35.3F,0,1,0);
                        GL11.glRotatef(-9.785F,0,0,1);
                        if(charge>.1F)GL11.glTranslatef(0,(float)Math.sin((ticks-.1F)*1.3F)*(charge-.1F)*.004F,0);
                        GL11.glTranslatef(0,0,charge*.2F);
                        GL11.glScalef(1,1,1+charge*.2F);
                        GL11.glRotatef(-45,0,1,0);
                    }
                    // trident_in_hand.json / firstperson_righthand.
                    transform(FIRST_PERSON);
                } else if (type == ItemRenderType.EQUIPPED) {
                    GL11.glTranslatef(.5F,.5F,.5F);
                    // Invert RenderPlayer's legacy full-3D item transform back to the arm pivot.
                    GL11.glRotatef(-45,0,1,0);
                    GL11.glRotatef(100,1,0,0);
                    GL11.glScalef(1.6F,-1.6F,1.6F);
                    GL11.glTranslatef(0,-.1875F,0);
                    GL11.glTranslatef(.0625F,-.4375F,-.0625F);
                    // Modern ItemInHandLayer, right hand.
                    GL11.glRotatef(-90,1,0,0);
                    GL11.glRotatef(180,0,1,0);
                    GL11.glTranslatef(.0625F,.125F,-.625F);
                    if (isThrowing(holder(data), item)) {
                        // trident_throwing.json / thirdperson_righthand.
                        transform(THIRD_PERSON_THROWING);
                    } else {
                        // trident_in_hand.json / thirdperson_righthand.
                        transform(THIRD_PERSON_HELD);
                    }
                } else {
                    // trident_in_hand.json / ground. JSON translations are pixels.
                    transform(GROUND);
                }
                // ItemRenderer centers builtin/entity, then BEWLR flips model Y/Z.
                GL11.glTranslatef(-.5F,-.5F,-.5F);
                GL11.glScalef(1,-1,-1);
                model.render();
                if (item != null && item.hasEffect()) TridentGlintRenderer.render(model);
            } finally {
                GL11.glPopMatrix();
            }
        }
    }

    private static EntityPlayer holder(Object[] data) {
        if (data == null) return null;
        for (Object value : data) if (value instanceof EntityPlayer) {
            return (EntityPlayer)value;
        }
        return null;
    }

    public static boolean isThrowing(EntityPlayer holder, ItemStack rendered) {
        return holder != null && rendered != null && holder.isUsingItem()
                && holder.getItemInUse() == rendered;
    }

    private static void transform(float[] value) {
        GL11.glTranslatef(value[0] / 16.0F, value[1] / 16.0F, value[2] / 16.0F);
        if (value[3] != 0.0F) GL11.glRotatef(value[3], 1, 0, 0);
        if (value[4] != 0.0F) GL11.glRotatef(value[4], 0, 1, 0);
        if (value[5] != 0.0F) GL11.glRotatef(value[5], 0, 0, 1);
        GL11.glScalef(value[6], value[6], value[6]);
    }
}
