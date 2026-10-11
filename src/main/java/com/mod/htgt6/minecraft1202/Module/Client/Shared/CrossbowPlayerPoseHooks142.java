package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandCrossbowUse120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.PlayerItemUse120;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/** Modern two-arm crossbow charge/hold poses, after legacy ModelBiped animation. */
public final class CrossbowPlayerPoseHooks142 {
    private CrossbowPlayerPoseHooks142() {}
    public static void apply(ModelBiped model, Entity entity) {
        if (model == null || !(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer)entity;
        ItemStack main = player.getHeldItem(), secondary = OffhandHooks.getOffhand(player);
        ItemStack using = PlayerItemUse120.item(player);
        if (using != null) {
            if (isCrossbow(using) && using == main)
                pose(model, false, PlayerItemUse120.duration(player), ItemCrossbow.chargeDuration(using));
            return; // Preserve eating, shield, spear and spyglass poses.
        }
        int mainElapsed=OffhandCrossbowUse120.mainUseTicks(player);
        if(mainElapsed>=0){pose(model,false,mainElapsed,ItemCrossbow.chargeDuration(main));return;}
        int elapsed = isCrossbow(secondary) ? OffhandCrossbowUse120.useTicks(player, secondary) : -1;
        if (elapsed >= 0) {
            pose(model, true, elapsed, ItemCrossbow.chargeDuration(secondary));
            return;
        }
        if (player.swingProgress > 0
                || com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandSystem.isShieldBlocking(player)
                || com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandSystem.isClientShieldBlocking(player)) return;
        if (isCrossbow(main) && ItemCrossbow.isCharged(main)) pose(model, false, -1, 1);
        else if (isCrossbow(secondary) && ItemCrossbow.isCharged(secondary)) pose(model, true, -1, 1);
    }
    private static boolean isCrossbow(ItemStack stack) { return stack != null && stack.getItem() instanceof ItemCrossbow; }
    private static void pose(ModelBiped model, boolean left, int elapsed, int duration) {
        ModelRenderer holding = left ? model.bipedLeftArm : model.bipedRightArm;
        ModelRenderer support = left ? model.bipedRightArm : model.bipedLeftArm;
        float sign = left ? -1F : 1F;
        holding.rotateAngleZ = support.rotateAngleZ = 0;
        if (elapsed >= 0) {
            float progress = Math.max(0,Math.min(1,elapsed/(float)Math.max(1,duration)));
            holding.rotateAngleY = -.8F * sign;
            holding.rotateAngleX = -.97079635F;
            support.rotateAngleY = (.4F + .45F * progress) * sign;
            support.rotateAngleX = -.97079635F + progress * (-1.5707964F + .97079635F);
        } else {
            holding.rotateAngleY = -.3F * sign + model.bipedHead.rotateAngleY;
            support.rotateAngleY = .6F * sign + model.bipedHead.rotateAngleY;
            holding.rotateAngleX = -(float)Math.PI/2 + model.bipedHead.rotateAngleX + .1F;
            support.rotateAngleX = -1.5F + model.bipedHead.rotateAngleX;
        }
    }
}
