package com.mod.htgt6.minecraft1202.Module.Entity.Exploration;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWanderingTrader120;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/** 1.12-data-compatible llama with neutral retaliation and ranged spit. */
public class EntityLlama112 extends EntityAnimal implements IRangedAttackMob {
    public static final int TRADER_LLAMA_WATCHER=20;
    private static final int VARIANT_WATCHER = 21;
    private static final int STRENGTH_WATCHER = 22;
    private static final int TAME_WATCHER = 23;
    private int temper;
    private String ownerUUID = "";
    private static final int CHEST_WATCHER=24, DECOR_WATCHER=25;
    private final net.minecraft.inventory.InventoryBasic llamaInventory = new net.minecraft.inventory.InventoryBasic("container.horse",false,17) {
        @Override public void markDirty() {
            super.markDirty();
            if (!worldObj.isRemote) {
                ItemStack carpet=getStackInSlot(1);
                dataWatcher.updateObject(DECOR_WATCHER,Integer.valueOf(isCarpet(carpet)?carpet.getItemDamage():-1));
            }
        }
    };
    public net.minecraft.inventory.IInventory getLlamaInventory(){return llamaInventory;}
    public boolean hasChest(){return dataWatcher.getWatchableObjectByte(CHEST_WATCHER)!=0;}
    public int getStorageColumns(){return hasChest()?getStrength():0;}
    public int getCarpetColor(){return dataWatcher.getWatchableObjectInt(DECOR_WATCHER);}
    public static boolean isCarpet(ItemStack stack){return stack!=null&&stack.getItem()==net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.carpet)&&stack.getItemDamage()>=0&&stack.getItemDamage()<16;}
    private EntityLlama112 caravanHead;
    private EntityLlama112 caravanTail;

    public EntityLlama112(World world) {
        super(world);
        setSize(0.9F, 1.87F);
        getNavigator().setAvoidsWater(true);
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new TamingAI());
        tasks.addTask(2, new CaravanAI());
        tasks.addTask(3, new EntityAIArrowAttack(this, 1.25D, 40, 20.0F));
        tasks.addTask(3, new EntityAIPanic(this, 1.2D));
        tasks.addTask(4, new EntityAIMate(this, 1.0D));
        tasks.addTask(5, new EntityAIFollowParent(this, 1.0D));
        tasks.addTask(6, new EntityAIWander(this, 0.7D));
        tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new net.minecraft.entity.ai.EntityAINearestAttackableTarget(this,
                EntityWolf.class, 16, false, true, new net.minecraft.command.IEntitySelector() {
                    @Override public boolean isEntityApplicable(net.minecraft.entity.Entity entity) {
                        return entity instanceof EntityWolf && !((EntityWolf)entity).isTamed();
                    }
                }));
    }

    @Override protected void entityInit(){super.entityInit();dataWatcher.addObject(TRADER_LLAMA_WATCHER,Byte.valueOf((byte)0));dataWatcher.addObject(VARIANT_WATCHER,Integer.valueOf(0));dataWatcher.addObject(STRENGTH_WATCHER,Integer.valueOf(1));dataWatcher.addObject(TAME_WATCHER,Byte.valueOf((byte)0));dataWatcher.addObject(CHEST_WATCHER,Byte.valueOf((byte)0));dataWatcher.addObject(DECOR_WATCHER,Integer.valueOf(-1));}
    public boolean isTraderLlama(){return dataWatcher.getWatchableObjectByte(TRADER_LLAMA_WATCHER)!=0;}
    protected void setTraderLlama(boolean value){dataWatcher.updateObject(TRADER_LLAMA_WATCHER,Byte.valueOf((byte)(value?1:0)));}

    // Forge 1.7.10 requires this opt-in to execute tasks and targetTasks.
    @Override protected boolean isAIEnabled() { return true; }

    @Override protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(15.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.175D);
    }

    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
        data = super.onSpawnWithEgg(data);
        setVariant(rand.nextInt(4));
        setStrength(1 + rand.nextInt(rand.nextFloat() < 0.04F ? 5 : 3));
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(15.0D + rand.nextInt(16));
        setHealth(getMaxHealth());
        return data;
    }

    @Override public EntityAgeable createChild(EntityAgeable mate) {
        EntityLlama112 child = new EntityLlama112(worldObj);
        int otherStrength = mate instanceof EntityLlama112 ? ((EntityLlama112)mate).getStrength() : getStrength();
        // 1.20: either parent's strength bounds the roll; improvement is a separate 3% chance.
        int inheritedStrength = 1 + rand.nextInt(Math.max(getStrength(), otherStrength));
        if (rand.nextFloat() < 0.03F) ++inheritedStrength;
        child.setStrength(inheritedStrength);
        child.setVariant(rand.nextBoolean() ? getVariant()
                : mate instanceof EntityLlama112 ? ((EntityLlama112)mate).getVariant() : getVariant());
        return child;
    }

    @Override public boolean isBreedingItem(ItemStack stack) {
        return stack != null && (stack.getItem() == Items.wheat
                || stack.getItem() == net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.hay_block));
    }

    @Override public boolean interact(EntityPlayer player) {
        if(isTraderLlama()&&getLeashed()&&getLeashedToEntity() instanceof EntityWanderingTrader120)return false;
        ItemStack held = player.getHeldItem();
        if(isTamed()&&!isChild()&&player.isSneaking()) {
            if(!worldObj.isRemote)player.openGui(com.mod.htgt6.minecraft1202.Minecraft1202.instance,
                    com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.ContainerLlama120.GUI_ID,
                    worldObj,getEntityId(),getStorageColumns(),0);
            return true;
        }
        if(isTamed()&&!isChild()&&!hasChest()&&held!=null&&held.getItem()==net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.chest)) {
            if(!worldObj.isRemote) {
                dataWatcher.updateObject(CHEST_WATCHER,Byte.valueOf((byte)1));
                if(!player.capabilities.isCreativeMode&&--held.stackSize<=0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);
                worldObj.playSoundAtEntity(this,"mob.chicken.plop",1,1);
            }
            return true;
        }
        if (held != null && isBreedingItem(held)) {
            // Inventory, health and love state are authoritative on the server.
            if (worldObj.isRemote) return true;
            boolean hay = held.getItem() == net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.hay_block);
            boolean enterLove = hay && isTamed() && getGrowingAge() == 0 && !isInLove();
            boolean effect = enterLove || getHealth() < getMaxHealth() || isChild();
            if ((effect || !isTamed()) && temper < 30) {
                temper = Math.min(30, temper + (hay ? 6 : 3));
                effect = true;
            }
            if (!effect) return false;
            heal(hay ? 10.0F : 2.0F);
            if (isChild()) addGrowth(hay ? 90 : 10);
            if (enterLove) func_146082_f(player);
            if (!player.capabilities.isCreativeMode && --held.stackSize <= 0)
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            return true;
        }
        if (!isChild() && riddenByEntity == null && !player.isSneaking() && (isTamed() || held == null)) {
            if (!worldObj.isRemote) player.mountEntity(this);
            return true;
        }
        return super.interact(player);
    }

    @Override public boolean canMateWith(EntityAnimal other) {
        return other != this && other instanceof EntityLlama112
                && canParent() && ((EntityLlama112)other).canParent();
    }

    private boolean canParent() {
        return riddenByEntity == null && ridingEntity == null && isTamed()
                && !isChild() && getHealth() >= getMaxHealth() && isInLove();
    }

    @Override public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean result = super.attackEntityFrom(source, amount);
        if (result && source.getEntity() instanceof EntityLivingBase && source.getEntity() != riddenByEntity)
            setAttackTarget((EntityLivingBase)source.getEntity());
        return result;
    }

    @Override public void onLivingUpdate() {
        super.onLivingUpdate();
    }

    @Override public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        EntityLlamaSpit112 spit = new EntityLlamaSpit112(worldObj, this);
        double dx = target.posX - posX;
        double dy = target.boundingBox.minY + target.height / 3.0F - spit.posY;
        double dz = target.posZ - posZ;
        spit.setThrowableHeading(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.2D, dz, 1.5F, 10.0F);
        worldObj.spawnEntityInWorld(spit);
        worldObj.playSoundAtEntity(this, "random.bow", 1.0F, 0.8F);
    }

    public int getVariant() { return dataWatcher.getWatchableObjectInt(VARIANT_WATCHER) & 3; }
    public boolean isTamed() { return dataWatcher.getWatchableObjectByte(TAME_WATCHER) != 0; }
    private void setTamed(boolean value) { dataWatcher.updateObject(TAME_WATCHER, Byte.valueOf((byte)(value ? 1 : 0))); }
    public int getTemper() { return temper; }

    @Override public void handleHealthUpdate(byte state) {
        if (state != 6 && state != 7) { super.handleHealthUpdate(state); return; }
        for (int i = 0; i < 7; ++i) worldObj.spawnParticle(state == 7 ? "heart" : "smoke",
                posX + (rand.nextDouble() * 2 - 1) * width, posY + .5 + rand.nextDouble() * height,
                posZ + (rand.nextDouble() * 2 - 1) * width,
                rand.nextGaussian() * .02, rand.nextGaussian() * .02, rand.nextGaussian() * .02);
    }

    /** Modern RunAroundLikeCrazyGoal adapted to the every-tick 1.7 scheduler. */
    private final class TamingAI extends net.minecraft.entity.ai.EntityAIBase {
        private net.minecraft.util.Vec3 target;
        TamingAI() { setMutexBits(1); }
        @Override public boolean shouldExecute() {
            if (isTamed() || riddenByEntity == null) return false;
            target = net.minecraft.entity.ai.RandomPositionGenerator.findRandomTarget(EntityLlama112.this, 5, 4);
            return target != null;
        }
        @Override public void startExecuting() {
            getNavigator().tryMoveToXYZ(target.xCoord, target.yCoord, target.zCoord, 1.2D);
        }
        @Override public boolean continueExecuting() {
            return !isTamed() && riddenByEntity != null && !getNavigator().noPath();
        }
        @Override public void updateTask() {
            if (worldObj.isRemote || isTamed() || riddenByEntity == null || rand.nextInt(50) != 0) return;
            if (riddenByEntity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer)riddenByEntity;
                if (rand.nextInt(30) < temper) {
                    setTamed(true);
                    ownerUUID = player.getUniqueID().toString();
                    worldObj.setEntityState(EntityLlama112.this, (byte)7);
                    return;
                }
                temper = Math.min(30, temper + 5);
            }
            riddenByEntity.mountEntity(null);
            worldObj.setEntityState(EntityLlama112.this, (byte)6);
        }
    }
    private void setVariant(int value) { dataWatcher.updateObject(VARIANT_WATCHER, Integer.valueOf(value & 3)); }
    public int getStrength() { return dataWatcher.getWatchableObjectInt(STRENGTH_WATCHER); }
    private void setStrength(int value) { dataWatcher.updateObject(STRENGTH_WATCHER, Integer.valueOf(Math.max(1, Math.min(5, value)))); }

    @Override public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("Variant", getVariant());
        tag.setInteger("Strength", getStrength());
        tag.setBoolean("Tame", isTamed());
        tag.setInteger("Temper", temper);
        tag.setString("OwnerUUID", ownerUUID);
        tag.setBoolean("ChestedHorse",hasChest());
        net.minecraft.nbt.NBTTagList cargo=new net.minecraft.nbt.NBTTagList();
        for(int slot=2;slot<17;slot++)if(llamaInventory.getStackInSlot(slot)!=null){
            NBTTagCompound entry=new NBTTagCompound();entry.setByte("Slot",(byte)slot);
            llamaInventory.getStackInSlot(slot).writeToNBT(entry);cargo.appendTag(entry);
        }
        tag.setTag("Items",cargo);
        if(llamaInventory.getStackInSlot(1)!=null){NBTTagCompound decor=new NBTTagCompound();llamaInventory.getStackInSlot(1).writeToNBT(decor);tag.setTag("DecorItem",decor);}
        else tag.removeTag("DecorItem");
        tag.setBoolean("TraderLlama",isTraderLlama());
    }

    @Override public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        setVariant(tag.getInteger("Variant"));
        setStrength(tag.getInteger("Strength"));
        setTamed(tag.getBoolean("Tame"));
        temper = Math.max(0, Math.min(30, tag.getInteger("Temper")));
        ownerUUID = tag.getString("OwnerUUID");
        dataWatcher.updateObject(CHEST_WATCHER,Byte.valueOf((byte)(tag.getBoolean("ChestedHorse")?1:0)));
        for(int slot=0;slot<17;slot++)llamaInventory.setInventorySlotContents(slot,null);
        net.minecraft.nbt.NBTTagList cargo=tag.getTagList("Items",10);
        for(int i=0;i<cargo.tagCount();i++){NBTTagCompound entry=cargo.getCompoundTagAt(i);int slot=entry.getByte("Slot")&255;
            if(slot>=2&&slot<2+3*getStorageColumns())llamaInventory.setInventorySlotContents(slot,ItemStack.loadItemStackFromNBT(entry));}
        ItemStack decor=ItemStack.loadItemStackFromNBT(tag.getCompoundTag("DecorItem"));
        if(isCarpet(decor)){decor.stackSize=1;llamaInventory.setInventorySlotContents(1,decor);}
        setTraderLlama(tag.getBoolean("TraderLlama"));
    }

    @Override protected void dropFewItems(boolean hit, int looting) {
        int count=com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.ExplorationLoot120.count(rand,2,looting);
        if(count>0)dropItem(Items.leather,count);
    }

    @Override protected void dropEquipment(boolean hit,int looting) {
        super.dropEquipment(hit,looting);
        for(int slot=1;slot<17;slot++){ItemStack stack=llamaInventory.getStackInSlot(slot);if(stack!=null){entityDropItem(stack.copy(),0);llamaInventory.setInventorySlotContents(slot,null);}}
        if(hasChest()){entityDropItem(new ItemStack(net.minecraft.init.Blocks.chest),0);dataWatcher.updateObject(CHEST_WATCHER,Byte.valueOf((byte)0));}
    }

    private boolean inCaravan() { return caravanHead != null; }
    private boolean hasCaravanTail() { return caravanTail != null; }
    private final class CaravanAI extends net.minecraft.entity.ai.EntityAIBase {
        private int counter;
        CaravanAI() { setMutexBits(1); }
        @Override @SuppressWarnings("unchecked") public boolean shouldExecute() {
            if (inCaravan() || getLeashed()) return false;
            java.util.List<EntityLlama112> nearby = worldObj.getEntitiesWithinAABB(EntityLlama112.class,
                    boundingBox.expand(9, 4, 9));
            EntityLlama112 best = null; double distance = Double.MAX_VALUE;
            for (EntityLlama112 llama : nearby) {
                if (llama == EntityLlama112.this || llama.hasCaravanTail()) continue;
                if (!llama.getLeashed() && !llama.inCaravan()) continue;
                double d = getDistanceSqToEntity(llama);
                if (d < distance) { distance = d; best = llama; }
            }
            if (best == null || distance < 4.0D) return false;
            caravanHead = best; best.caravanTail = EntityLlama112.this; return true;
        }
        @Override public boolean continueExecuting() {
            if (caravanHead == null || !caravanHead.isEntityAlive()) return false;
            double d = getDistanceSqToEntity(caravanHead);
            if (d > 676.0D) return false;
            if (d > 4.0D) counter = 40;
            return counter-- > 0 || d > 4.0D;
        }
        @Override public void resetTask() {
            if (caravanHead != null) caravanHead.caravanTail = null;
            caravanHead = null; counter = 0;
        }
        @Override public void updateTask() {
            if (caravanHead == null) return;
            double dx = caravanHead.posX-posX, dy=caravanHead.posY-posY, dz=caravanHead.posZ-posZ;
            double d = Math.sqrt(dx*dx+dy*dy+dz*dz);
            if (d > 2.0D) getNavigator().tryMoveToXYZ(posX+dx/d*(d-2),posY+dy/d*(d-2),posZ+dz/d*(d-2),2.1D);
        }
    }
}
