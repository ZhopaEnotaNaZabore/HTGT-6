package com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

/** Player portion of modern LivingEntity.increaseAirSupply; custom mob air limits remain their own. */
public final class PlayerBreathing120 {
    private PlayerBreathing120() {}

    public static boolean showAir(EntityPlayer player,net.minecraft.block.material.Material material){
        return player.isInsideOfMaterial(material)
                ||com.mod.htgt6.asm.HTGT6Config.RUN_MINECRAFT_1_7_9999
                &&material==net.minecraft.block.material.Material.water&&player.getAir()<300;
    }

    public static int surfaceAir(int legacy,EntityLivingBase entity) {
        if(!com.mod.htgt6.asm.HTGT6Config.RUN_MINECRAFT_1_7_9999)return legacy;
        // Crucible plugins can supply their own maxAirTicks; keep that API's refill contract.
        if(!(entity instanceof EntityPlayer)||legacy!=300)return legacy;
        int air=entity.getAir();
        if(!entity.isEntityAlive()||air>=300)return air;
        return Math.min(air+4,300);
    }
}
