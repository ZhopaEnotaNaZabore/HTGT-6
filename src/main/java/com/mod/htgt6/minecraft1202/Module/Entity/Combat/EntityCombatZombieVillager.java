package com.mod.htgt6.minecraft1202.Module.Entity.Combat;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112;

import java.util.UUID;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/** Minecraft 1.12's separate zombie-villager entity on the 1.7 entity runtime. */
public final class EntityCombatZombieVillager extends EntityZombie {
    private int profession;
    private int conversionTime = -1;
    private UUID conversionPlayer;
    private boolean conversionFinished;
    private boolean conversionInProgress;
    private NBTTagCompound villagerState;

    public void captureVillagerState(EntityVillager villager) {
        villagerState = com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerConversion114.capture(villager);
    }

    public EntityCombatZombieVillager(World world) {
        super(world);
        super.setVillager(true);
    }

    public int getProfession() { return profession; }

    public void setProfession(int value) {
        profession = value < 0 || value > 5 ? 0 : value;
    }

    public void restoreConversion(int ticks, UUID player) {
        conversionPlayer = player;
        if (ticks > -1) startConversion(ticks);
    }

    @Override public void setVillager(boolean ignored) {
        super.setVillager(true);
    }

    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
        data = super.onSpawnWithEgg(data);
        super.setVillager(true);
        setProfession(rand.nextInt(6));
        return data;
    }

    @Override protected void startConversion(int ticks) {
        conversionTime = ticks;
        conversionFinished = false;
        super.startConversion(ticks);
    }

    @Override public boolean interact(EntityPlayer player) {
        boolean convertingBefore = isConverting();
        boolean handled = super.interact(player);
        if (!worldObj.isRemote && handled && !convertingBefore && isConverting()) {
            conversionPlayer = player.getUniqueID();
        }
        return handled;
    }

    @Override public void onUpdate() {
        super.onUpdate();
        if (!worldObj.isRemote && !conversionFinished && isConverting()
                && conversionTime >= 0 && --conversionTime <= 0) {
            convertToVillager();
        }
    }

    @Override public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("Profession", profession);
        if (villagerState != null) tag.setTag("HTGT6VillagerState114", villagerState.copy());
        tag.setString("ProfessionName", VillagerCareers112.professionName(profession));
        tag.setInteger("ConversionTime", isConverting() && conversionTime >= 0 ? conversionTime : -1);
        if (conversionPlayer != null) {
            tag.setLong("ConversionPlayerMost", conversionPlayer.getMostSignificantBits());
            tag.setLong("ConversionPlayerLeast", conversionPlayer.getLeastSignificantBits());
        }
    }

    @Override public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        villagerState = tag.hasKey("HTGT6VillagerState114", 10)
                ? (NBTTagCompound) tag.getCompoundTag("HTGT6VillagerState114").copy() : null;
        super.setVillager(true);
        setProfession(tag.getInteger("Profession"));
        if (tag.hasKey("ProfessionName", 8)) {
            setProfession(VillagerCareers112.professionId(tag.getString("ProfessionName"), profession));
        }
        if (tag.hasKey("ConversionTime", 99)) {
            conversionTime = tag.getInteger("ConversionTime");
        } else if (tag.hasKey("HTGT6ConversionTime", 99)) {
            // One-way migration from early hybrid builds; new saves only use ConversionTime.
            conversionTime = tag.getInteger("HTGT6ConversionTime");
            if (conversionTime > -1) startConversion(conversionTime);
        }
        if (tag.hasKey("ConversionPlayerMost", 99) && tag.hasKey("ConversionPlayerLeast", 99)) {
            conversionPlayer = new UUID(tag.getLong("ConversionPlayerMost"),
                    tag.getLong("ConversionPlayerLeast"));
        }
    }

    @Override protected void convertToVillager() {
        if (conversionFinished || conversionInProgress || worldObj.isRemote) return;
        conversionInProgress = true;
        EntityVillager villager = new EntityVillager(worldObj);
        villager.copyLocationAndAnglesFrom(this);
        villager.onSpawnWithEgg(null);
        villager.setProfession(profession);
        if (villagerState == null) VillagerCareers112.resetCareer(villager);
        else com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerConversion114.restore(villager, villagerState);
        villager.setLookingForHome();
        villager.func_110163_bv();
        if (isChild()) villager.setGrowingAge(-24000);
        if (hasCustomNameTag()) {
            villager.setCustomNameTag(getCustomNameTag());
            villager.setAlwaysRenderNameTag(getAlwaysRenderNameTag());
        }
        villager.getEntityData().setTag("HTGT6Gossip114",getEntityData().getTagList("HTGT6Gossip114",10).copy());
        com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerGossip114.cured(villager,conversionPlayer);
        if (!worldObj.spawnEntityInWorld(villager)) {
            conversionInProgress = false;
            conversionTime = 20;
            super.startConversion(20);
            return;
        }
        conversionFinished = true;
        worldObj.removeEntity(this);
        villager.addPotionEffect(new PotionEffect(Potion.confusion.id, 200, 0));
        worldObj.playAuxSFXAtEntity((EntityPlayer) null, 1017,
                (int) posX, (int) posY, (int) posZ, 0);
    }
}
