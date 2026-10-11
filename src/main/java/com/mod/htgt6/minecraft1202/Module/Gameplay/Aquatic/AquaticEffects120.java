package com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic;

import com.mod.htgt6.asm.HTGT6Config;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

/** Registered effects use normal expiry, milk removal and extended potion transport. */
public final class AquaticEffects120 {
    private static Potion grace, conduit;
    private AquaticEffects120() {}

    public static synchronized void init() {
        if (grace != null && conduit != null) return;
        int graceId = HTGT6Config.DOLPHINS_GRACE_POTION_ID;
        int conduitId = HTGT6Config.CONDUIT_POWER_POTION_ID;
        if (graceId == conduitId) throw new IllegalStateException("Aquatic potion IDs must be unique");
        validateId(graceId, "DolphinsGrace");
        validateId(conduitId, "ConduitPower");
        grace = new PotionDolphinsGrace120(graceId);
        conduit = new PotionConduitPower120(conduitId);
        MinecraftForge.EVENT_BUS.register(new AquaticEffects120());
    }

    private static void validateId(int id, String name) {
        if (id < 0 || id >= Potion.potionTypes.length)
            throw new IllegalStateException(name + " ID outside potion registry: " + id);
        if (Potion.potionTypes[id] != null)
            throw new IllegalStateException(name + " potion ID " + id + " occupied by "
                    + Potion.potionTypes[id].getName() + "; choose the same free aquatic_potion_ids." + name + " on client and server");
    }

    public static void grant(EntityPlayer player) {
        if (grace != null && !player.worldObj.isRemote)
            player.addPotionEffect(new PotionEffect(grace.id, 100, 0));
    }

    public static void grantConduitPower(EntityPlayer player) {
        if (HTGT6Config.RUN_MINECRAFT_1_7_9999 && conduit != null && !player.worldObj.isRemote)
            player.addPotionEffect(new PotionEffect(conduit.id, 260, 0, true));
    }

    public static boolean hasConduitPower(EntityLivingBase entity) {
        return HTGT6Config.RUN_MINECRAFT_1_7_9999 && conduit != null
                && entity != null && entity.isPotionActive(conduit.id);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!hasConduitPower(event.entityPlayer)) return;
        int power = event.entityPlayer.getActivePotionEffect(conduit).getAmplifier();
        PotionEffect haste = event.entityPlayer.getActivePotionEffect(Potion.digSpeed);
        int hasteLevel = haste == null ? -1 : haste.getAmplifier();
        // Forge already applied Haste: modern logic chooses the stronger effect, never stacks both.
        if (power > hasteLevel)
            event.newSpeed *= (1F + .2F * (power + 1)) / (1F + .2F * (hasteLevel + 1));
    }

    public static double drag(double normal, EntityLivingBase entity) {
        return HTGT6Config.RUN_MINECRAFT_1_7_9999 && grace != null
                && entity.isPotionActive(grace.id) ? (double) .96F : normal;
    }
}
