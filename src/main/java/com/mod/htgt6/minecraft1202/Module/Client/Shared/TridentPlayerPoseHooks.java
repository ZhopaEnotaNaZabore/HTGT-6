package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/** Applies the 1.20.2 THROW_SPEAR and SPYGLASS arm poses after vanilla animation. */
public final class TridentPlayerPoseHooks {
    public static void apply(ModelBiped model, Entity entity) {
        if (model == null || !(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer)entity;
        ItemStack using = player.getItemInUse();
        if(using!=null&&using.getItem()==Backport120Content.spyglass){
            float crouch=player.isSneaking()?0.2617994F:0.0F;
            model.bipedRightArm.rotateAngleX=clamp(model.bipedHead.rotateAngleX-1.9198622F-crouch,-2.4F,3.3F);
            model.bipedRightArm.rotateAngleY=model.bipedHead.rotateAngleY-0.2617994F;
            model.bipedRightArm.rotateAngleZ=0.0F;
            return;
        }
        if (using == null || using.getItem() != OceanContent.trident) return;
        model.bipedRightArm.rotateAngleX = model.bipedHead.rotateAngleX - (float)Math.PI;
        model.bipedRightArm.rotateAngleY = 0.0F;
        model.bipedRightArm.rotateAngleZ = 0.0F;
    }

    private static float clamp(float value,float min,float max){return Math.max(min,Math.min(max,value));}

    private TridentPlayerPoseHooks() { }
}
