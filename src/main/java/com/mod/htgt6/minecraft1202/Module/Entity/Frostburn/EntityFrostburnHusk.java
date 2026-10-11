package com.mod.htgt6.minecraft1202.Module.Entity.Frostburn;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Minecraft 1.10 Husk adapted to the 1.7.10 zombie implementation. */
public final class EntityFrostburnHusk extends EntityZombie {

    public EntityFrostburnHusk(World world) {
        super(world);
        setSize(0.6F, 1.95F);
    }

    @Override public boolean getCanSpawnHere() {
        return super.getCanSpawnHere() && (FrostburnSpawnRules120.fromSpawner(this)||worldObj.canBlockSeeTheSky(
                MathHelper.floor_double(posX),MathHelper.floor_double(boundingBox.minY),MathHelper.floor_double(posZ)));
    }
    /** Wraps only EntityZombie's solar branch, preserving other fire sources. */
    public static boolean solarBurnAllowed(boolean daytime, EntityZombie zombie) {
        return daytime && !(zombie instanceof EntityFrostburnHusk);
    }
    public static float effectiveDifficulty(int difficulty,long dayTime,long inhabited,float moon) {
        if(difficulty==0)return 0;
        float age=MathHelper.clamp_float(((float)dayTime-72000F)/1440000F,0,1)*.25F;
        float local=MathHelper.clamp_float((float)inhabited/3600000F,0,1)*(difficulty==3?1:.75F);
        local+=MathHelper.clamp_float(moon*.25F,0,age);
        if(difficulty==1)local*=.5F;
        return difficulty*(.75F+age+local);
    }
    @Override
    public boolean attackEntityAsMob(Entity target) {
        boolean hit = super.attackEntityAsMob(target);
        if (hit && getHeldItem() == null && target instanceof EntityLivingBase) {
            int difficulty = (int)effectiveDifficulty(worldObj.difficultySetting.getDifficultyId(),
                    worldObj.getWorldTime(),worldObj.getChunkFromBlockCoords(MathHelper.floor_double(posX),MathHelper.floor_double(posZ)).inhabitedTime,
                    worldObj.getCurrentMoonPhaseFactor());
            if (difficulty > 0) {
                ((EntityLivingBase) target).addPotionEffect(
                        new PotionEffect(Potion.hunger.id, 140 * difficulty, 0));
            }
        }
        return hit;
    }

    @Override protected void dropFewItems(boolean recentlyHit,int looting) {
        int flesh=FrostburnLoot120.count(rand,2,looting);
        if(flesh>0)entityDropItem(new net.minecraft.item.ItemStack(net.minecraft.init.Items.rotten_flesh,flesh),0);
        if(recentlyHit&&FrostburnLoot120.rareHuskDrop(rand,looting)) {
            int selected=rand.nextInt(3);
            net.minecraft.item.Item item=selected==0?net.minecraft.init.Items.iron_ingot:
                selected==1?net.minecraft.init.Items.carrot:
                isBurning()?net.minecraft.init.Items.baked_potato:net.minecraft.init.Items.potato;
            entityDropItem(new net.minecraft.item.ItemStack(item),0);
        }
    }
    // EntityLivingBase also calls the legacy 2.5% + 0.5%/level path. The table above
    // already rolled 2.5% + 1%/level; suppress that second, incompatible roll.
    @Override protected void dropRareDrop(int legacyBonus) { }

    @Override protected String getLivingSound() { return "htgt6:entity.husk.ambient"; }
    @Override protected String getHurtSound() { return "htgt6:entity.husk.hurt"; }
    @Override protected String getDeathSound() { return "htgt6:entity.husk.death"; }
    @Override protected void func_145780_a(int x,int y,int z,net.minecraft.block.Block block) {
        playSound("htgt6:entity.husk.step",.15F,1F);
    }
}
