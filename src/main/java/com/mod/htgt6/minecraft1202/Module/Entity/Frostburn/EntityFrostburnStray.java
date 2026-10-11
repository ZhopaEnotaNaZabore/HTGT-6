package com.mod.htgt6.minecraft1202.Module.Entity.Frostburn;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Minecraft 1.10 Stray with cold-biome spawning and Slowness arrows. */
public final class EntityFrostburnStray extends EntitySkeleton {
    public EntityFrostburnStray(World world) {
        super(world);
    }

    @Override
    public boolean getCanSpawnHere() {
        return super.getCanSpawnHere() && (FrostburnSpawnRules120.fromSpawner(this)||canSeeSkyForSpawn(worldObj,
                MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)));
    }

    /** Stray.checkStraySpawnRules checks above the column of powder snow. */
    public static boolean canSeeSkyForSpawn(World world,int x,int y,int z) {
        int checkY=y+1;
        while(checkY<world.getActualHeight() && world.getBlock(x,checkY,z)
                ==com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.powderSnow)checkY++;
        return world.canBlockSeeTheSky(x,checkY-1,z);
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        EntityFrostburnStrayArrow arrow = new EntityFrostburnStrayArrow(worldObj, this, target, 1.6F,
                (float) (14 - worldObj.difficultySetting.getDifficultyId() * 4));
        ItemStack bow = getHeldItem();
        int power = bow == null ? 0 : EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, bow);
        int punch = bow == null ? 0 : EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, bow);
        arrow.setDamage((double) (distanceFactor * 2.0F) + rand.nextGaussian() * 0.25D
                + (double) ((float) worldObj.difficultySetting.getDifficultyId() * 0.11F));
        if (power > 0) arrow.setDamage(arrow.getDamage() + (double) power * 0.5D + 0.5D);
        if (punch > 0) arrow.setKnockbackStrength(punch);
        if (bow != null && EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0) {
            arrow.setFire(100);
        }
        playSound("random.bow", 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
        worldObj.spawnEntityInWorld(arrow);
    }
    @Override protected String getLivingSound() { return "htgt6:entity.stray.ambient"; }
    @Override protected String getHurtSound() { return "htgt6:entity.stray.hurt"; }
    @Override protected String getDeathSound() { return "htgt6:entity.stray.death"; }
    @Override protected void func_145780_a(int x,int y,int z,net.minecraft.block.Block block) {
        playSound("htgt6:entity.stray.step",.15F,1F);
    }
    @Override protected void dropFewItems(boolean recentlyHit,int looting) {
        int arrows=FrostburnLoot120.count(rand,2,looting),bones=FrostburnLoot120.count(rand,2,looting);
        if(arrows>0)entityDropItem(new ItemStack(net.minecraft.init.Items.arrow,arrows),0);
        if(bones>0)entityDropItem(new ItemStack(net.minecraft.init.Items.bone,bones),0);
        if(recentlyHit && Math.min(1,FrostburnLoot120.count(rand,1,looting))>0)
            entityDropItem(new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.tippedArrow,1,8202),0);
    }
}
