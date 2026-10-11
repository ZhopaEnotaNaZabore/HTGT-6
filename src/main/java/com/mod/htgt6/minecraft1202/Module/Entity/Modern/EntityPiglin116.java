package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.PiglinBartering116;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.PiglinInventory116;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;

/** Server-owned barter transaction; watchers contain only render state. */
public class EntityPiglin116 extends EntityMob {
    private int admiration,anger,conversion,barterCooldown,ateRecently,avoidTicks,pickupCooldown;
    private final PiglinInventory116 inventory=new PiglinInventory116();
    private UUID customer,angryAt;
    private int huntCooldown,huntTicks,retreatTicks;
    private EntityLivingBase retreatTarget;
    public int babyRideCooldown,babyRideTime;
    private int celebration;private boolean dance;private double celebrateX,celebrateY,celebrateZ;
    private ItemStack payment,payout;
    private boolean immuneToZombification,cannotHunt;
    private static final UUID BABY_SPEED=UUID.fromString("766bfa64-11f3-11ea-8d71-362b9e155667");
    public EntityPiglin116(World world){
        super(world);setSize(.6F,1.95F);experienceValue=5;
        tasks.addTask(0,new EntityAISwimming(this));
        tasks.addTask(1,new Avoid());
        tasks.addTask(1,new Admire());
        tasks.addTask(2,new Fight());
        tasks.addTask(3,new CollectGold());
        tasks.addTask(4,new Celebrate());
        tasks.addTask(4,new com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.PiglinBabyRide116(this));
        tasks.addTask(5,new EntityAIWander(this,.6));
        tasks.addTask(6,new EntityAIWatchClosest(this,EntityPlayer.class,8));
        tasks.addTask(7,new EntityAILookIdle(this));
        targetTasks.addTask(1,new SelectTarget());
    }
    @Override protected void entityInit(){super.entityInit();dataWatcher.addObject(20,Byte.valueOf((byte)0));dataWatcher.addObject(21,Integer.valueOf(0));dataWatcher.addObjectByDataType(22,5);}
    @Override protected boolean isAIEnabled(){return true;}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(16);getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.35);getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(5);getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(16);}
    private void flag(int mask,boolean on){byte before=dataWatcher.getWatchableObjectByte(20);dataWatcher.updateObject(20,Byte.valueOf((byte)(on?before|mask:before&~mask)));}
    @Override public boolean isChild(){return (dataWatcher.getWatchableObjectByte(20)&1)!=0;}
    public void setChild(boolean child){flag(1,child);setSize(child?.3F:.6F,child?.975F:1.95F);if(!worldObj.isRemote){net.minecraft.entity.ai.attributes.IAttributeInstance speed=getEntityAttribute(SharedMonsterAttributes.movementSpeed);net.minecraft.entity.ai.attributes.AttributeModifier old=speed.getModifier(BABY_SPEED);if(old!=null)speed.removeModifier(old);if(child)speed.applyModifier(new net.minecraft.entity.ai.attributes.AttributeModifier(BABY_SPEED,"Baby speed boost",.2,1).setSaved(false));}}
    public ItemStack getAdmiredItem(){return dataWatcher.getWatchableObjectItemStack(22);}
    private void syncAdmiration(){flag(2,payment!=null);dataWatcher.updateObject(22,payment==null?null:payment.copy());}
    public boolean isAdmiring(){return (dataWatcher.getWatchableObjectByte(20)&2)!=0;}
    public boolean isAggressive(){return (dataWatcher.getWatchableObjectByte(20)&4)!=0;}
    @Override public void setAttackTarget(EntityLivingBase target){super.setAttackTarget(target);flag(4,target!=null);if(target!=null){celebration=0;flag(8,false);}}
    public int getCrossbowChargeTicks(){return dataWatcher.getWatchableObjectInt(21);}
    public void setCrossbowChargeTicks(int ticks){dataWatcher.updateObject(21,Integer.valueOf(ticks));}
    public static boolean wearsGold(EntityPlayer p){for(int slot=1;slot<=4;slot++){ItemStack s=p.getEquipmentInSlot(slot);if(s!=null&&s.getItem() instanceof ItemArmor&&((ItemArmor)s.getItem()).getArmorMaterial()==ItemArmor.ArmorMaterial.GOLD)return true;}return false;}
    public boolean acceptsGold(){return isEntityAlive()&&payment==null&&payout==null&&anger==0&&barterCooldown==0&&avoidTicks==0;}
    @Override public boolean interact(EntityPlayer player){
        ItemStack held=player.getHeldItem();boolean offhand=false;
        if(held==null||held.getItem()!=Items.gold_ingot){held=OffhandHooks.getOffhand(player);offhand=true;}
        if(isChild()||held==null||held.getItem()!=Items.gold_ingot||held.stackSize<=0||!acceptsGold())return super.interact(player);
        if(!worldObj.isRemote){beginBarter(player.getUniqueID());com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.distracted(player,this);if(!player.capabilities.isCreativeMode){--held.stackSize;if(offhand)OffhandHooks.setOffhand(player,held.stackSize<=0?null:held);else if(held.stackSize<=0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);}}
        return true;
    }
    public void beginBarter(UUID owner){if(worldObj.isRemote||!acceptsGold())return;payment=new ItemStack(Items.gold_ingot);customer=owner;setAttackTarget(null);admiration=120;syncAdmiration();getNavigator().clearPathEntity();playSound("htgt6:entity.piglin.admiring_item",1,1);}
    /** Called once per server tick; no client loot rolls or background world access. */
    public void tickBarter(){
        if(worldObj.isRemote)return;
        if(barterCooldown>0)--barterCooldown;
        if(ateRecently>0)--ateRecently;if(avoidTicks>0)--avoidTicks;if(pickupCooldown>0)--pickupCooldown;
        if(payment!=null&&--admiration<=0){
            ItemStack admired=payment;payment=null;syncAdmiration();
            if(!isChild()&&admired.getItem()==Items.gold_ingot)payout=PiglinBartering116.roll(rand);
            else {keepAdmired(admired);customer=null;}
        }
        if(payout!=null&&barterCooldown==0){EntityItem drop=new EntityItem(worldObj,posX,posY+1,posZ,payout.copy());EntityPlayer receiver=findPlayer(customer);
            double dx=receiver!=null&&getDistanceSqToEntity(receiver)<256?receiver.posX-posX:rand.nextDouble()*4-2;
            double dz=receiver!=null&&getDistanceSqToEntity(receiver)<256?receiver.posZ-posZ:rand.nextDouble()*4-2;
            double length=Math.max(.001,Math.sqrt(dx*dx+dz*dz));drop.motionX=dx/length*.25;drop.motionY=.2;drop.motionZ=dz/length*.25;drop.delayBeforeCanPickup=10;
            if(worldObj.spawnEntityInWorld(drop)){payout=null;customer=null;barterCooldown=20;}else barterCooldown=20;
        }
    }
    private EntityPlayer findPlayer(UUID id){if(id==null)return null;for(Object o:worldObj.playerEntities){EntityPlayer p=(EntityPlayer)o;if(id.equals(p.getUniqueID()))return p;}return null;}
    @Override public void onLivingUpdate(){
        if(!worldObj.isRemote&&getAttackTarget()!=null&&!getAttackTarget().isEntityAlive())celebrateVictory(getAttackTarget());
        super.onLivingUpdate();
        if(worldObj.isRemote){setSize(isChild()?.3F:.6F,isChild()?.975F:1.95F);return;}
        if(babyRideCooldown>0)babyRideCooldown--;if(babyRideTime>0)babyRideTime--;
        if((ridingEntity instanceof EntityPiglin116||ridingEntity instanceof EntityHoglin116)&&(!isChild()||babyRideTime==0||getAITarget()!=null||!com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.PiglinBabyRide116.validMount(ridingEntity)))mountEntity(null);
        if(celebration>0)celebration--;
        flag(8,celebration>0&&dance&&!isAdmiring()&&avoidTicks==0&&getAttackTarget()==null);
        tickBarter();
        tickAnger();
        if(huntCooldown>0)huntCooldown--;
        if(huntTicks>0&&--huntTicks==0&&getAttackTarget() instanceof EntityHoglin116)setAttackTarget(null);
        if(retreatTicks>0&&--retreatTicks==0)retreatTarget=null;
        tickConversion();
    }
    public void tickConversion(){if(worldObj.isRemote||isDead)return;if(!immuneToZombification&&worldObj.provider.dimensionId!=-1){if(++conversion>300&&(conversion==301||conversion%20==0))convert();}else conversion=0;}
    public void tickAnger(){
        if(worldObj.isRemote)return;
        EntityLivingBase target=getAttackTarget();if(target!=null&&!target.isEntityAlive())setAttackTarget(null);
        EntityPlayer enemy=findPlayer(angryAt);
        if(enemy!=null&&!enemy.isEntityAlive()&&(!worldObj.getGameRules().hasRule("forgiveDeadPlayers")||worldObj.getGameRules().getGameRuleBooleanValue("forgiveDeadPlayers"))){anger=0;angryAt=null;setAttackTarget(null);}
        if(anger>0&&--anger==0){angryAt=null;setAttackTarget(null);}
    }
    private void convert(){
        EntityZombifiedPiglin116 zombie=new EntityZombifiedPiglin116(worldObj);zombie.copyLocationAndAnglesFrom(this);zombie.setChild(isChild());
        for(int slot=0;slot<5;slot++){ItemStack s=getEquipmentInSlot(slot);if(s!=null)zombie.setCurrentItemOrArmor(slot,s.copy());}
        if(hasCustomNameTag())zombie.setCustomNameTag(getCustomNameTag());
        if(isNoDespawnRequired())zombie.func_110163_bv();
        zombie.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.potion.Potion.confusion.id,200));
        if(worldObj.spawnEntityInWorld(zombie)){dropTransaction();inventory.drop(this);setDead();playSound(conversionSound(),1,1);}
    }
    protected String conversionSound(){return "htgt6:entity.piglin.converted_to_zombified";}
    @Override protected void func_145780_a(int x,int y,int z,net.minecraft.block.Block block){playSound("htgt6:entity.piglin.step",.15F,1);}
    @Override public boolean attackEntityAsMob(Entity target){
        ItemStack held=getHeldItem();float modern=com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.AttackCooldown112.getVanillaAttackDamage(held);
        if(Float.isNaN(modern)||held.hasTagCompound()&&held.getTagCompound().hasKey("AttributeModifiers"))return super.attackEntityAsMob(target);
        java.util.UUID weapon=java.util.UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
        net.minecraft.entity.ai.attributes.IAttributeInstance attribute=getEntityAttribute(SharedMonsterAttributes.attackDamage);
        net.minecraft.entity.ai.attributes.AttributeModifier before=attribute.getModifier(weapon);
        if(before!=null&&before.getOperation()!=0)return super.attackEntityAsMob(target);
        if(before!=null)attribute.removeModifier(before);
        net.minecraft.entity.ai.attributes.AttributeModifier adjusted=new net.minecraft.entity.ai.attributes.AttributeModifier(weapon,"Modern piglin weapon",modern-1,0);
        attribute.applyModifier(adjusted);
        try{return super.attackEntityAsMob(target);}finally{attribute.removeModifier(adjusted);if(before!=null)attribute.applyModifier(before);}
    }
    public void angerAt(EntityPlayer player,boolean broadcast){
        if(worldObj.isRemote||isChild()||player.capabilities.isCreativeMode)return;
        if(worldObj.getGameRules().getGameRuleBooleanValue("universalAnger")){EntityPlayer nearest=nearestAngerPlayer();if(nearest!=null)player=nearest;}
        anger=600;angryAt=player.getUniqueID();setAttackTarget(player);interruptAdmiration();barterCooldown=400;
        if(broadcast)for(Object o:worldObj.getEntitiesWithinAABB(EntityPiglin116.class,boundingBox.expand(16,8,16))){EntityPiglin116 p=(EntityPiglin116)o;if(p!=this)p.angerAt(player,false);}
    }
    @Override public boolean attackEntityFrom(DamageSource source,float damage){boolean hit=super.attackEntityFrom(source,damage);if(hit&&!worldObj.isRemote){if(isChild()){interruptAdmiration();barterCooldown=400;}if(source.getEntity() instanceof EntityPlayer)angerAt((EntityPlayer)source.getEntity(),true);else if(source.getEntity() instanceof EntityLivingBase&&!isChild()&&!(source.getEntity() instanceof EntityPiglin116)){interruptAdmiration();EntityLivingBase enemy=(EntityLivingBase)source.getEntity();if(enemy instanceof EntityHoglin116&&getClass()==EntityPiglin116.class&&com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherHerd116.hoglinsOutnumber(this)){retreatFrom(enemy);for(EntityPiglin116 p:com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherHerd116.piglins(this))p.retreatFrom(enemy);}else setAttackTarget(enemy);}}return hit;}
    private void dropTransaction(){if(worldObj.isRemote)return;if(payment!=null){entityDropItem(payment,1);payment=null;}if(payout!=null){entityDropItem(payout,1);payout=null;}customer=null;admiration=0;syncAdmiration();}
    @Override public void onDeath(DamageSource source){dropTransaction();inventory.drop(this);super.onDeath(source);}
    @Override protected boolean canDespawn(){return payment==null&&payout==null&&inventory.isEmpty()&&super.canDespawn();}
    @Override public boolean getCanSpawnHere(){int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(boundingBox.minY),z=MathHelper.floor_double(posZ);return worldObj.provider.dimensionId==-1&&worldObj.difficultySetting!=EnumDifficulty.PEACEFUL&&worldObj.getBlockLightValue(x,y,z)<=11&&worldObj.getBlock(x,y-1,z).isNormalCube()&&worldObj.getBlock(x,y-1,z)!=Blocks.nether_wart&&worldObj.checkNoEntityCollision(boundingBox)&&worldObj.getCollidingBoundingBoxes(this,boundingBox).isEmpty()&&!worldObj.isAnyLiquid(boundingBox);}
    @Override public int getMaxSpawnedInChunk(){return 4;}
    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data){setChild(rand.nextFloat()<.2F);setCurrentItemOrArmor(0,new ItemStack(rand.nextBoolean()?Items.golden_sword:VillagePillageContent.crossbow));for(int i=1;i<=4;i++)if(rand.nextFloat()<.1F)setCurrentItemOrArmor(i,new ItemStack(new Item[]{Items.golden_boots,Items.golden_leggings,Items.golden_chestplate,Items.golden_helmet}[i-1]));return data;}
    @Override protected String getLivingSound(){return "htgt6:entity.piglin."+(getAttackTarget()!=null?"angry":"ambient");}
    @Override protected String getHurtSound(){return "htgt6:entity.piglin.hurt";}
    @Override protected String getDeathSound(){return "htgt6:entity.piglin.death";}
    @Override public void writeEntityToNBT(NBTTagCompound n){super.writeEntityToNBT(n);n.setInteger("BabyRideCooldown",babyRideCooldown);n.setInteger("BabyRideTime",babyRideTime);n.setInteger("Celebration",celebration);n.setBoolean("Dancing",dance);n.setDouble("CelebrateX",celebrateX);n.setDouble("CelebrateY",celebrateY);n.setDouble("CelebrateZ",celebrateZ);n.setBoolean("CannotHunt",cannotHunt);n.setInteger("HuntCooldown",huntCooldown);n.setBoolean("IsBaby",isChild());n.setTag("PiglinInventory",inventory.write());n.setInteger("AteRecently",ateRecently);n.setInteger("Admiration",admiration);n.setInteger("AngerTime",anger);n.setInteger("TimeInOverworld",conversion);n.setInteger("BarterCooldown",barterCooldown);n.setBoolean("IsImmuneToZombification",immuneToZombification);n.removeTag("BarterCustomer");n.removeTag("AngryAt");n.removeTag("BarterPayment");n.removeTag("BarterPayout");if(customer!=null)n.setString("BarterCustomer",customer.toString());if(angryAt!=null)n.setString("AngryAt",angryAt.toString());if(payment!=null)n.setTag("BarterPayment",payment.writeToNBT(new NBTTagCompound()));if(payout!=null)n.setTag("BarterPayout",payout.writeToNBT(new NBTTagCompound()));}
    @Override public void readEntityFromNBT(NBTTagCompound n){super.readEntityFromNBT(n);babyRideCooldown=MathHelper.clamp_int(n.getInteger("BabyRideCooldown"),0,800);babyRideTime=MathHelper.clamp_int(n.getInteger("BabyRideTime"),0,600);celebration=MathHelper.clamp_int(n.getInteger("Celebration"),0,300);dance=n.getBoolean("Dancing");celebrateX=n.getDouble("CelebrateX");celebrateY=n.getDouble("CelebrateY");celebrateZ=n.getDouble("CelebrateZ");cannotHunt=n.getBoolean("CannotHunt");huntCooldown=MathHelper.clamp_int(n.getInteger("HuntCooldown"),0,2400);setChild(n.getBoolean("IsBaby"));inventory.read(n.getTagList("PiglinInventory",10));ateRecently=MathHelper.clamp_int(n.getInteger("AteRecently"),0,200);admiration=MathHelper.clamp_int(n.getInteger("Admiration"),0,120);anger=MathHelper.clamp_int(n.getInteger("AngerTime"),0,600);conversion=MathHelper.clamp_int(n.getInteger("TimeInOverworld"),0,300);barterCooldown=Math.max(0,n.getInteger("BarterCooldown"));immuneToZombification=n.getBoolean("IsImmuneToZombification");customer=uuid(n.getString("BarterCustomer"));angryAt=uuid(n.getString("AngryAt"));payment=ItemStack.loadItemStackFromNBT(n.getCompoundTag("BarterPayment"));payout=ItemStack.loadItemStackFromNBT(n.getCompoundTag("BarterPayout"));if(payment!=null){payment.stackSize=1;payout=null;}syncAdmiration();}
    private static UUID uuid(String s){try{return UUID.fromString(s);}catch(IllegalArgumentException e){return null;}}
    private final class Avoid extends EntityAIBase {
        double x,z;int remaining;
        Avoid(){setMutexBits(3);}
        public boolean shouldExecute(){
            if(retreatTicks>0&&retreatTarget!=null&&retreatTarget.isEntityAlive()){x=retreatTarget.posX;z=retreatTarget.posZ;return true;}
            if(ticksExisted%20!=0)return false;
            for(Object o:worldObj.getEntitiesWithinAABB(EntityZoglin116.class,boundingBox.expand(6,4,6))){Entity e=(Entity)o;if(getEntitySenses().canSee(e)){x=e.posX;z=e.posZ;return true;}}
            for(Object o:worldObj.getEntitiesWithinAABB(EntityZombifiedPiglin116.class,boundingBox.expand(6,4,6))){Entity e=(Entity)o;if(getEntitySenses().canSee(e)){x=e.posX;z=e.posZ;return true;}}
            for(Object o:worldObj.getEntitiesWithinAABB(net.minecraft.entity.monster.EntityPigZombie.class,boundingBox.expand(6,4,6))){Entity e=(Entity)o;if(getEntitySenses().canSee(e)){x=e.posX;z=e.posZ;return true;}}
            if(isChild()&&getAITarget()!=null){x=getAITarget().posX;z=getAITarget().posZ;return true;}
            // Bounded local check; no chunk loads and no per-tick volume scan.
            int bx=MathHelper.floor_double(posX),by=MathHelper.floor_double(posY),bz=MathHelper.floor_double(posZ);
            for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)for(int dy=-1;dy<=1;dy++){
                if(!worldObj.blockExists(bx+dx,by+dy,bz+dz))continue;
                net.minecraft.block.Block b=worldObj.getBlock(bx+dx,by+dy,bz+dz);
                if(b==com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.soulFire||b==com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.soulTorch||b==com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.soulLantern||(b==com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherUtilityContent116.soulCampfire&&com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockCampfire149.lit(worldObj.getBlockMetadata(bx+dx,by+dy,bz+dz)))){x=bx+dx+.5;z=bz+dz+.5;return true;}
            }
            return false;
        }
        public void startExecuting(){remaining=40;avoidTicks=40;setAttackTarget(null);Vec3 destination=RandomPositionGenerator.findRandomTargetBlockAwayFrom(EntityPiglin116.this,8,4,Vec3.createVectorHelper(x,posY,z));if(destination!=null)getNavigator().tryMoveToXYZ(destination.xCoord,destination.yCoord,destination.zCoord,1.2);}
        public boolean continueExecuting(){return --remaining>0&&!getNavigator().noPath();}
    }
    public boolean isDancing(){return (dataWatcher.getWatchableObjectByte(20)&8)!=0;}
    public void celebrateVictory(EntityLivingBase victim){
        if(getClass()!=EntityPiglin116.class||victim==null||victim.isEntityAlive()||celebration>0)return;
        celebration=300;celebrateX=victim.posX;celebrateY=victim.posY;celebrateZ=victim.posZ;
        dance=victim instanceof EntityHoglin116&&new Random(worldObj.getTotalWorldTime()).nextFloat()<.1F;
        setAttackTarget(null);flag(8,dance);playSound("htgt6:entity.piglin.celebrate",1,1);
    }
    private final class Celebrate extends EntityAIBase {
        int delay;Celebrate(){setMutexBits(3);}
        public boolean shouldExecute(){return celebration>0&&!isAdmiring()&&avoidTicks==0&&getAttackTarget()==null;}
        public boolean continueExecuting(){return shouldExecute();}
        public void updateTask(){double stop=dance?4:2;if(getDistanceSq(celebrateX,celebrateY,celebrateZ)<=stop*stop){getNavigator().clearPathEntity();return;}if(--delay<=0){delay=20;getNavigator().tryMoveToXYZ(celebrateX,celebrateY,celebrateZ,dance?.6:1);}}
        public void resetTask(){getNavigator().clearPathEntity();flag(8,false);}
    }
    private final class Admire extends EntityAIBase {Admire(){setMutexBits(3);}public boolean shouldExecute(){return isAdmiring();}public void updateTask(){getNavigator().clearPathEntity();EntityPlayer p=findPlayer(customer);if(p!=null)getLookHelper().setLookPositionWithEntity(p,30,30);}}
    private EntityPlayer nearestAngerPlayer(){EntityPlayer next=null;double nearest=256;for(Object o:worldObj.playerEntities){EntityPlayer p=(EntityPlayer)o;double d=getDistanceSqToEntity(p);if(p.isEntityAlive()&&!p.capabilities.isCreativeMode&&d<nearest&&getEntitySenses().canSee(p)){next=p;nearest=d;}}return next;}
    public EntityPlayer selectPlayerTarget(){
        if(isChild()||isAdmiring()||avoidTicks>0||retreatTicks>0)return null;
        if(anger>0&&worldObj.getGameRules().getGameRuleBooleanValue("universalAnger"))return nearestAngerPlayer();
        EntityPlayer enemy=anger>0?findPlayer(angryAt):null;
        if(enemy!=null&&enemy.isEntityAlive()&&!enemy.capabilities.isCreativeMode&&getDistanceSqToEntity(enemy)<1024)return enemy;
        if(anger>0)return null;
        EntityPlayer nearest=null;double distance=256;
        for(Object value:worldObj.playerEntities){EntityPlayer p=(EntityPlayer)value;double d=getDistanceSqToEntity(p);
            if(p.isEntityAlive()&&!p.capabilities.isCreativeMode&&!wearsGold(p)&&d<distance&&getEntitySenses().canSee(p)){nearest=p;distance=d;}
        }
        return nearest;
    }
    public boolean canHunt(){return getClass()==EntityPiglin116.class&&!isChild()&&!cannotHunt;}
    public void postponeHunt(){huntCooldown=600+rand.nextInt(1801);}
    public void retreatFrom(EntityLivingBase enemy){if(getClass()!=EntityPiglin116.class||isChild())return;retreatTarget=enemy;retreatTicks=100+rand.nextInt(301);huntTicks=0;anger=0;angryAt=null;setAttackTarget(null);postponeHunt();}
    public EntityLivingBase selectMobTarget(){
        if(getClass()!=EntityPiglin116.class||isChild()||isAdmiring()||avoidTicks>0||retreatTicks>0||anger>0)return null;
        EntityLivingBase enemy=null;double distance=256;
        for(Object o:worldObj.getEntitiesWithinAABB(EntityLivingBase.class,boundingBox.expand(16,16,16))){EntityLivingBase nemesis=(EntityLivingBase)o;if(!(nemesis instanceof net.minecraft.entity.boss.EntityWither)&&!(nemesis instanceof net.minecraft.entity.monster.EntitySkeleton&&((net.minecraft.entity.monster.EntitySkeleton)nemesis).getSkeletonType()==1))continue;double d=getDistanceSqToEntity(nemesis);if(nemesis.isEntityAlive()&&d<distance&&getEntitySenses().canSee(nemesis)){enemy=nemesis;distance=d;}}
        if(enemy!=null)return enemy;
        if(huntCooldown>0||cannotHunt)return null;
        java.util.List<EntityPiglin116> allies=com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherHerd116.piglins(this);
        for(EntityPiglin116 p:allies)if(p.canHunt()&&p.huntCooldown>0)return null;
        EntityHoglin116 prey=null;
        for(EntityHoglin116 h:com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherHerd116.hoglins(this)){double d=getDistanceSqToEntity(h);if(h.canBeHunted()&&d<distance){prey=h;distance=d;}}
        if(prey==null)return null;
        postponeHunt();huntTicks=600;
        for(EntityPiglin116 p:allies)if(p!=this&&p.canHunt()){p.postponeHunt();if(p.getAttackTarget()==null&&!p.isAdmiring()&&p.retreatTicks==0){p.huntTicks=600;p.setAttackTarget(prey);}}
        return prey;
    }
    private final class SelectTarget extends EntityAIBase {
        EntityLivingBase selected;
        public boolean shouldExecute(){if(ticksExisted%10!=0||getAttackTarget()!=null&&getAttackTarget().isEntityAlive())return false;selected=selectPlayerTarget();if(selected==null)selected=selectMobTarget();return selected!=null;}
        public void startExecuting(){setAttackTarget(selected);}
        public boolean continueExecuting(){return false;}
    }
    public boolean wantsItem(ItemStack stack){
        if(stack==null||stack.stackSize<=0||!acceptsGold())return false;
        if(isChild()&&stack.getItem()==Items.leather)return false;
        if(stack.getItem()==Items.gold_nugget)return inventory.hasRoom(stack);
        if(PiglinInventory116.food(stack))return ateRecently==0&&inventory.hasRoom(stack);
        if(!PiglinInventory116.loved(stack))return com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.PiglinEquipment116.canEquip(this,stack);
        return isChild()||stack.getItem()==Items.gold_ingot||inventory.hasRoom(stack);
    }
    public boolean collectItem(EntityItem item){
        if(worldObj.isRemote||item==null||item.isDead||item.delayBeforeCanPickup>0||!worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))return false;
        ItemStack stack=item.getEntityItem();if(stack==null||stack.stackSize<=0||!wantsItem(stack))return false;
        if(stack.getItem()==Items.gold_nugget){ItemStack rest=inventory.insert(stack);if(rest==null)item.setDead();else item.setEntityItemStack(rest);return true;}
        ItemStack one=stack.copy();one.stackSize=1;
        if(PiglinInventory116.food(one)){ateRecently=200;playSound("random.eat",1,1);}
        else if(PiglinInventory116.loved(one)){payment=one;customer=null;admiration=120;syncAdmiration();getNavigator().clearPathEntity();}
        else if(!com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.PiglinEquipment116.equip(this,one))return false;
        if(!isChild()&&PiglinInventory116.loved(one)&&item.func_145800_j()!=null)for(Object value:worldObj.playerEntities){EntityPlayer thrower=(EntityPlayer)value;if(item.func_145800_j().equals(thrower.getCommandSenderName()))com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.distracted(thrower,this);}
        if(--stack.stackSize==0)item.setDead();else item.setEntityItemStack(stack.copy());
        return true;
    }
    public void equipPickedItem(int slot,ItemStack item){
        ItemStack old=getEquipmentInSlot(slot);if(old!=null&&rand.nextFloat()-.1F<equipmentDropChances[slot])entityDropItem(old,1);
        setCurrentItemOrArmor(slot,item);equipmentDropChances[slot]=2;func_110163_bv();
    }
    private void keepAdmired(ItemStack item){
        if(isChild()){
            ItemStack old=getHeldItem();setCurrentItemOrArmor(0,item);
            if(old!=null){if(PiglinInventory116.loved(old))storeItem(old);else entityDropItem(old,1);}
            equipmentDropChances[0]=2;return;
        }
        if(com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.PiglinEquipment116.equip(this,item))return;
        storeItem(item);
    }
    private void storeItem(ItemStack item){ItemStack rest=inventory.insert(item);if(rest!=null)entityDropItem(rest,1);}
    private void interruptAdmiration(){
        // A hit consumes an adult's unfinished barter currency, without paying or refunding it.
        if(payment!=null){ItemStack interrupted=payment;payment=null;if(isChild()||interrupted.getItem()!=Items.gold_ingot)keepAdmired(interrupted);}
        customer=null;admiration=0;syncAdmiration();
    }
    private final class CollectGold extends EntityAIBase {
        EntityItem item;int attempts;
        CollectGold(){setMutexBits(3);}
        public boolean shouldExecute(){
            if(!acceptsGold()||pickupCooldown>0||ticksExisted%10!=0||!worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))return false;
            double nearest=64;item=null;
            for(Object o:worldObj.getEntitiesWithinAABB(EntityItem.class,boundingBox.expand(8,4,8))){EntityItem e=(EntityItem)o;double d=getDistanceSqToEntity(e);
                if(!e.isDead&&e.delayBeforeCanPickup<=0&&wantsItem(e.getEntityItem())&&d<nearest&&getEntitySenses().canSee(e)){nearest=d;item=e;}}
            return item!=null;
        }
        public void startExecuting(){attempts=0;}
        public boolean continueExecuting(){return attempts<200&&item!=null&&!item.isDead&&wantsItem(item.getEntityItem())&&worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")&&getDistanceSqToEntity(item)<100;}
        public void resetTask(){if(attempts>=200)pickupCooldown=200;item=null;getNavigator().clearPathEntity();}
        public void updateTask(){++attempts;if(ticksExisted%10==0)getNavigator().tryMoveToEntityLiving(item,1);if(getDistanceSqToEntity(item)<2.25&&getEntitySenses().canSee(item)){collectItem(item);getNavigator().clearPathEntity();}}
    }
    private final class Fight extends EntityAIBase {
        int cooldown,visible,path,unseen;ItemStack weapon;Fight(){setMutexBits(3);}public boolean shouldExecute(){return !isChild()&&!isAdmiring()&&getAttackTarget()!=null&&getAttackTarget().isEntityAlive();}
        public boolean continueExecuting(){return shouldExecute();}public void resetTask(){setCrossbowChargeTicks(0);getNavigator().clearPathEntity();weapon=null;visible=unseen=0;}
        public void updateTask(){EntityLivingBase target=getAttackTarget();if(target instanceof EntityPlayer&&(((EntityPlayer)target).capabilities.isCreativeMode||anger==0&&wearsGold((EntityPlayer)target))){setAttackTarget(null);return;}if(getDistanceSqToEntity(target)>1024){setAttackTarget(null);return;}getLookHelper().setLookPositionWithEntity(target,30,30);boolean seen=getEntitySenses().canSee(target);visible=seen?visible+1:0;unseen=seen?0:unseen+1;if(unseen>200){setAttackTarget(null);getNavigator().clearPathEntity();return;}if(cooldown>0)--cooldown;ItemStack held=getHeldItem();boolean crossbow=held!=null&&held.getItem() instanceof ItemCrossbow;double distance=getDistanceSqToEntity(target);
            if(distance>(crossbow?64:2.25)||!seen){if(--path<=0){getNavigator().tryMoveToEntityLiving(target,1);path=20+rand.nextInt(21);}}else getNavigator().clearPathEntity();
            if(!crossbow){if(seen&&distance<2.25&&cooldown==0){swingItem();attackEntityAsMob(target);cooldown=20;}return;}
            if(weapon!=held){weapon=held;setCrossbowChargeTicks(0);}
            if(ItemCrossbow.isCharged(held)){if(seen&&cooldown==0)ItemCrossbow.fire(held,worldObj,EntityPiglin116.this,target);}
            else if(getCrossbowChargeTicks()>0){int t=getCrossbowChargeTicks()+1;setCrossbowChargeTicks(t);if(t>=ItemCrossbow.chargeDuration(held)&&ItemCrossbow.loadMob(EntityPiglin116.this,held)){setCrossbowChargeTicks(0);cooldown=20+rand.nextInt(20);}}
            else if(visible>=5&&distance<=64)setCrossbowChargeTicks(1);
        }
    }
}
