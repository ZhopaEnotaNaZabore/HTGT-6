package com.mod.htgt6.minecraft1202.Module.Events.Combat;

import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatZombieVillager;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWanderingTrader120;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.EnumDifficulty;

/** Infection bridge called from EntityZombie#onKillEntity by the core transformer. */
public final class ZombieVillagerHooks {
    public static boolean infectVillager(EntityZombie attacker, EntityLivingBase victim) {
        if (attacker.worldObj.isRemote || !(victim instanceof EntityVillager)
                || victim instanceof EntityWanderingTrader120) return false;
        EnumDifficulty difficulty = attacker.worldObj.difficultySetting;
        if (difficulty != EnumDifficulty.NORMAL && difficulty != EnumDifficulty.HARD) return false;

        // We own both outcomes on Normal/Hard so the legacy method cannot create
        // a second, generic EntityZombie. Normal is 50%; Hard is 100%.
        if (difficulty == EnumDifficulty.NORMAL && attacker.getRNG().nextBoolean()) return true;
        EntityVillager villager = (EntityVillager) victim;
        EntityCombatZombieVillager zombie = new EntityCombatZombieVillager(attacker.worldObj);
        zombie.copyLocationAndAnglesFrom(villager);
        zombie.onSpawnWithEgg((IEntityLivingData) null);
        zombie.setProfession(villager.getProfession());
        zombie.setChild(villager.isChild());
        zombie.captureVillagerState(villager);
        zombie.getEntityData().setTag("HTGT6Gossip114",villager.getEntityData().getTagList("HTGT6Gossip114",10).copy());
        if (villager.hasCustomNameTag()) {
            zombie.setCustomNameTag(villager.getCustomNameTag());
            zombie.setAlwaysRenderNameTag(villager.getAlwaysRenderNameTag());
        }
        zombie.func_110163_bv();
        // Keep the source villager alive until the replacement is accepted by the world.
        // This prevents a failed spawn from turning an infection into a missing/null entity.
        if (!attacker.worldObj.spawnEntityInWorld(zombie)) return true;
        attacker.worldObj.removeEntity(villager);
        attacker.worldObj.playAuxSFXAtEntity((EntityPlayer) null, 1016,
                (int) attacker.posX, (int) attacker.posY, (int) attacker.posZ, 0);
        return true;
    }

    private ZombieVillagerHooks() { }
}
