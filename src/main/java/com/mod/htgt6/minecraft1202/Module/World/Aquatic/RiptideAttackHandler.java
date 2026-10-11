package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.HashSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/** Adapts LivingEntity's 1.20.2 auto-spin attack used by Riptide. */
public final class RiptideAttackHandler {
    public static final int AUTO_SPIN_TICKS = 20;
    public static final float AUTO_SPIN_DAMAGE = 8.0F;
    static final double HITBOX_EXPANSION = 0.5D;
    // Client and integrated-server ticks share this class in one JVM.
    private static final Map<EntityPlayer, SpinState> ACTIVE = Collections.synchronizedMap(
            new WeakHashMap<EntityPlayer, SpinState>());

    public static void start(EntityPlayer player) {
        if (player == null) return;
        ACTIVE.put(player, new SpinState(AUTO_SPIN_TICKS));
    }

    public static boolean isActive(EntityPlayer player) {
        SpinState state = ACTIVE.get(player);
        return state != null && state.ticks > 0;
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer)event.entityLiving;
        SpinState state = ACTIVE.get(player);
        if (state == null) return;
        if (--state.ticks <= 0 || player.isDead) {
            ACTIVE.remove(player);
            return;
        }
        player.fallDistance = 0.0F;
        if (player.worldObj.isRemote) return;

        List<Entity> contacts = player.worldObj.getEntitiesWithinAABBExcludingEntity(player,
                player.boundingBox.expand(HITBOX_EXPANSION, HITBOX_EXPANSION, HITBOX_EXPANSION));
        for (Entity entity : contacts) {
            if (!(entity instanceof EntityLivingBase) || !state.hit.add(entity.getEntityId())) continue;
            EntityLivingBase target = (EntityLivingBase)entity;
            if (target.attackEntityFrom(DamageSource.causePlayerDamage(player), AUTO_SPIN_DAMAGE)) {
                // LivingEntity#doAutoAttackOnTouch ends the attack after a successful collision.
                ACTIVE.remove(player);
                return;
            }
        }
    }

    private static final class SpinState {
        int ticks;
        final Set<Integer> hit = new HashSet<Integer>();
        SpinState(int ticks) { this.ticks = ticks; }
    }
}
