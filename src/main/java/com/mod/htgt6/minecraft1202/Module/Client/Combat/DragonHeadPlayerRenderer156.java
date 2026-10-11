package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemBlockDragonHead;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.lwjgl.opengl.GL11;

/** 1.20 CustomHeadLayer transform, attached to the animated 1.7 player head. */
public final class DragonHeadPlayerRenderer156 {
    private final RenderDragonHead120 renderer = new RenderDragonHead120();

    @SubscribeEvent public void render(RenderPlayerEvent.Specials.Pre event) {
        ItemStack helmet = event.entityPlayer.getEquipmentInSlot(4);
        if (!event.renderHelmet || helmet == null || !(helmet.getItem() instanceof ItemBlockDragonHead)) return;
        // Prevent RenderPlayer from drawing it a second time as a held block.
        event.renderHelmet = false;
        EntityLivingBase walker = event.entityPlayer.ridingEntity instanceof EntityLivingBase
                ? (EntityLivingBase) event.entityPlayer.ridingEntity : event.entityPlayer;
        float partial = event.partialRenderTick;
        float walk = walker.limbSwing - walker.limbSwingAmount * (1.0F - partial);
        GL11.glPushMatrix();
        try {
            event.renderer.modelBipedMain.bipedHead.postRender(0.0625F);
            GL11.glScalef(1.1875F, -1.1875F, -1.1875F);
            renderer.renderModel(0, 0, 0, 180, RenderDragonHead120.jawAngle(walk));
        } finally { GL11.glPopMatrix(); }
    }
}
