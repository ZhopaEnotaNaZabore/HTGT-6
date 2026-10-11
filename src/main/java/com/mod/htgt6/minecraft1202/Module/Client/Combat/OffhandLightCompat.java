package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.block.material.Material;

/** No Angelica class references: safe when that optional renderer is absent. */
public final class OffhandLightCompat {
    public static ItemStack stack(Entity entity) {
        if(!(entity instanceof EntityPlayer))return null;
        ItemStack stack=OffhandHooks.getOffhand((EntityPlayer)entity);
        return stack!=null&&stack.stackSize>0?stack:null;
    }
    public static boolean underwater(Entity entity) { return entity.isInsideOfMaterial(Material.water); }
    private OffhandLightCompat() { }
}
