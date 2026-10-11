package com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;

/** Crossbow-wielding illager used by patrols, outposts and raids. */
public final class EntityPillager extends EntityMob implements IRangedAttackMob,
        com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.IRaider112 {
    private static final int CAPTAIN_WATCHER = 20;
    private boolean patrolling;
    private net.minecraft.util.ChunkCoordinates patrolTarget;

    public EntityPillager(World world) {
        super(world);
        setSize(0.6F, 1.95F);
        experienceValue = 5;
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(2, new PillagerCrossbowGoal139(this));
        tasks.addTask(4,new com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.PatrolGoal120(this));
        tasks.addTask(5, new EntityAIWander(this, 0.8D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
        targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityVillager.class, 0, false));
        targetTasks.addTask(4, new EntityAINearestAttackableTarget(this, EntityIronGolem.class, 0, false));
    }

    @Override protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(CAPTAIN_WATCHER, Byte.valueOf((byte)0));
        dataWatcher.addObject(21, Integer.valueOf(0));
    }

    // EntityLiving defaults to the obsolete pre-task AI in 1.7.10.
    @Override protected boolean isAIEnabled(){return true;}

    @Override protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(24.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.35D);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(32.0D);
    }

    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
        data = super.onSpawnWithEgg(data);
        if(naturalOutpostCandidate){setCaptain(rand.nextFloat()<.06F);naturalOutpostCandidate=false;}
        setCurrentItemOrArmor(0, new ItemStack(VillagePillageContent.crossbow));
        if(isCaptain())equipCaptainBanner();
        return data;
    }

    @Override public void attackEntityWithRangedAttack(EntityLivingBase target, float power) {
        com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow.fire(getHeldItem(), worldObj, this, target);
    }

    public int getCrossbowChargeTicks() { return dataWatcher.getWatchableObjectInt(21); }
    public void setCrossbowChargeTicks(int ticks) { dataWatcher.updateObject(21, Integer.valueOf(ticks)); }

    public boolean isCaptain() { return dataWatcher.getWatchableObjectByte(CAPTAIN_WATCHER) != 0; }
    public void setCaptain(boolean captain) {
        dataWatcher.updateObject(CAPTAIN_WATCHER, Byte.valueOf((byte)(captain ? 1 : 0)));
        if(captain)equipCaptainBanner();
        else if(isCaptainBanner(getEquipmentInSlot(4))){setCurrentItemOrArmor(4,null);setEquipmentDropChance(4,.085F);}
    }

    public static ItemStack createCaptainBanner() {
        ItemStack banner=new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.banner,1,15);
        NBTTagCompound tag=new NBTTagCompound(),block=new NBTTagCompound();
        net.minecraft.nbt.NBTTagList patterns=new net.minecraft.nbt.NBTTagList();
        String[] codes={"mr","bs","cs","bo","ms","hh","mc","bo"};
        int[] colors={6,7,8,7,0,7,7,0};
        for(int i=0;i<codes.length;i++){NBTTagCompound pattern=new NBTTagCompound();pattern.setString("Pattern",codes[i]);pattern.setInteger("Color",colors[i]);patterns.appendTag(pattern);}
        block.setTag("Patterns",patterns);tag.setTag("BlockEntityTag",block);tag.setBoolean("HTGT6OminousBanner",true);tag.setInteger("HideFlags",32);banner.setTagCompound(tag);
        return banner;
    }
    public static boolean isCaptainBanner(ItemStack item) {
        return item!=null&&item.getItem()==com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.banner
                &&item.hasTagCompound()&&item.getTagCompound().getBoolean("HTGT6OminousBanner");
    }
    private void equipCaptainBanner(){
        if(!isCaptainBanner(getEquipmentInSlot(4)))setCurrentItemOrArmor(4,createCaptainBanner());
        setEquipmentDropChance(4,2F);
    }

    public boolean isPatrolling(){return patrolling;}
    public void setPatrolling(boolean value){patrolling=value;}
    public net.minecraft.util.ChunkCoordinates getPatrolTarget(){return patrolTarget==null?null:new net.minecraft.util.ChunkCoordinates(patrolTarget);}
    public void setPatrolTarget(net.minecraft.util.ChunkCoordinates point){patrolTarget=new net.minecraft.util.ChunkCoordinates(point);patrolling=true;}
    public void findPatrolTarget(){setPatrolTarget(new net.minecraft.util.ChunkCoordinates(MathHelper.floor_double(posX)-500+rand.nextInt(1000),MathHelper.floor_double(posY),MathHelper.floor_double(posZ)-500+rand.nextInt(1000)));}

    @Override public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setBoolean("VillagePillageCaptain", isCaptain());
        tag.setBoolean("Patrolling",patrolling);
        if(patrolTarget!=null){tag.setInteger("PatrolX",patrolTarget.posX);tag.setInteger("PatrolY",patrolTarget.posY);tag.setInteger("PatrolZ",patrolTarget.posZ);}
    }

    @Override public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        boolean captain=tag.getBoolean("VillagePillageCaptain");
        patrolling=tag.getBoolean("Patrolling");
        patrolTarget=tag.hasKey("PatrolX")?new net.minecraft.util.ChunkCoordinates(tag.getInteger("PatrolX"),tag.getInteger("PatrolY"),tag.getInteger("PatrolZ")):null;
        dataWatcher.updateObject(CAPTAIN_WATCHER,Byte.valueOf((byte)(captain?1:0)));
        // Migrate pre-banner captains without replacing custom head equipment in an existing save.
        if(captain&&(getEquipmentInSlot(4)==null||isCaptainBanner(getEquipmentInSlot(4))))equipCaptainBanner();
    }

    private boolean naturalOutpostCandidate;
    @Override public boolean getCanSpawnHere() {
        naturalOutpostCandidate=false;
        int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(boundingBox.minY),
                z = MathHelper.floor_double(posZ);
        if(com.mod.htgt6.minecraft1202.Module.World.VillageAndPillage.OutpostSpawnRuntime120.contains(worldObj,x,y,z))
            return naturalOutpostCandidate=worldObj.difficultySetting!=net.minecraft.world.EnumDifficulty.PEACEFUL
                    &&worldObj.getGameRules().getGameRuleBooleanValue("doMobSpawning")
                    &&worldObj.getSavedLightValue(net.minecraft.world.EnumSkyBlock.Block,x,y,z)<=8
                    &&com.mod.htgt6.minecraft1202.Module.World.VillageAndPillage.PillagerOutpostGenerator.safeGuardPosition(worldObj,this,x,y,z);
        return worldObj.provider.dimensionId == 0 && worldObj.getBlockLightValue(x, y, z) <= 8
                && super.getCanSpawnHere();
    }

    // Crossbows use EntityLiving equipment drops; the modern pillager loot table is empty.
}
