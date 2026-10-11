package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDrowned;

import com.mod.htgt6.minecraft1202.Module.Entity.Frostburn.EntityFrostburnHusk;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingEvent;

/** Converts after the reference 600-tick drowning delay plus 300 conversion ticks. */
public final class ZombieDrowningHandler {
    private static final String TIMER = "HTGT6DrownedConversion";
    public static final int CONVERSION_TICKS = 900;

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.entityLiving.worldObj.isRemote || !event.entityLiving.isEntityAlive() || !(event.entityLiving instanceof EntityZombie)
                || event.entityLiving instanceof EntityDrowned) return;
        EntityZombie zombie = (EntityZombie) event.entityLiving;
        NBTTagCompound data = zombie.getEntityData();
        int ticks;
        if (data.getInteger(TIMER)>=600 || zombie.isInsideOfMaterial(net.minecraft.block.material.Material.water)) {
            ticks = data.getInteger(TIMER) + 1;
            data.setInteger(TIMER, ticks);
        } else {
            if (data.hasKey(TIMER)) data.removeTag(TIMER);
            return;
        }
        if (ticks < CONVERSION_TICKS) return;

        // The modern chain is deliberately two-stage: Husk -> Zombie -> Drowned.
        // A Husk must never skip the intermediate ordinary zombie conversion.
        EntityZombie converted = zombie instanceof EntityFrostburnHusk
                ? new EntityZombie(zombie.worldObj) : new EntityDrowned(zombie.worldObj);
        converted.copyLocationAndAnglesFrom(zombie);
        converted.setChild(zombie.isChild());
        converted.func_146070_a(!(converted instanceof EntityDrowned) && zombie.func_146072_bX());
        converted.setCanPickUpLoot(zombie.canPickUpLoot());
        if (zombie.isNoDespawnRequired()) converted.func_110163_bv();
        NBTTagCompound source = new NBTTagCompound();
        zombie.writeEntityToNBT(source);
        net.minecraft.nbt.NBTTagList dropChances = source.getTagList("DropChances", 5);
        // Conversion preserves the zombie. It is not a natural drowned spawn
        // and therefore must not roll a new trident over empty equipment slots.
        for (int slot = 0; slot < 5; slot++) {
            ItemStack equipment = zombie.getEquipmentInSlot(slot);
            if (equipment != null) {
                converted.setCurrentItemOrArmor(slot, equipment.copy());
                if (slot < dropChances.tagCount())
                    converted.setEquipmentDropChance(slot, dropChances.func_150308_e(slot));
            }
        }
        if (zombie.hasCustomNameTag()) {
            converted.setCustomNameTag(zombie.getCustomNameTag());
            converted.setAlwaysRenderNameTag(zombie.getAlwaysRenderNameTag());
        }
        // Mob.convertTo creates a fresh target: it does not transfer health or potion effects.
        if(zombie.worldObj.spawnEntityInWorld(converted)) {
            if (zombie.ridingEntity != null) {
                net.minecraft.entity.Entity mount = zombie.ridingEntity;
                zombie.mountEntity(null);
                converted.mountEntity(mount);
            }
            zombie.worldObj.playSoundAtEntity(converted, zombie instanceof EntityFrostburnHusk
                    ? "htgt6:entity.husk.converted_to_zombie" : "htgt6:entity.zombie.converted_to_drowned", 1F, 1F);
            zombie.setDead();
        }
    }
}
