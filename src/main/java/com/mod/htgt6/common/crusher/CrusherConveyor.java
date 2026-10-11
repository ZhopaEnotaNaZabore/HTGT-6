package com.mod.htgt6.common.crusher;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;
import java.util.*;

/** Server-owned transport. No fake players, entity deletion, world-wide scans or client inventory writes. */
public final class CrusherConveyor {
    public static final int POWER=CrusherConveyorRules.POWER, MAX_ENTITIES=CrusherConveyorRules.MAX_ENTITIES;
    private static final Map<Entity, MultiTileIndustrialCrusher> OWNERS=Collections.synchronizedMap(new WeakHashMap<Entity, MultiTileIndustrialCrusher>());
    private final MultiTileIndustrialCrusher machine;
    private final List<Entity> cargo=new ArrayList<Entity>();
    private final List<ItemStack> pending=new ArrayList<ItemStack>();
    private int[] clientIds=new int[0];
    private boolean running;
    private boolean consumedPower;
    private long sequence;
    /** Only the head stack travels; all remaining drops wait behind the rollers. */
    private int outputTicks;
    private long clientOutputSyncTick;
    public CrusherConveyor(MultiTileIndustrialCrusher machine){this.machine=machine;}
    public boolean isRunning(){return running;}
    public boolean consumedPower(){return consumedPower;}
    public ItemStack outputForRender(){return pending.isEmpty()?null:pending.get(0).copy();}
    public float outputTicksForRender(float partial){
        boolean client=machine.getWorldObj()!=null&&machine.getWorldObj().isRemote;
        boolean moving=client&&running&&machine.enabled&&machine.isStructureFormed()&&!pending.isEmpty();
        long elapsed=client?machine.getWorldObj().getTotalWorldTime()-clientOutputSyncTick:0L;
        return com.mod.htgt6.api.hitech6.multiblock.MachineAnimationProgress.ticks(
                outputTicks,CrusherConveyorRules.OUTPUT_TICKS,elapsed,partial,moving);
    }
    public static MultiTileIndustrialCrusher owner(Entity e){
        if(e==null)return null;
        MultiTileIndustrialCrusher m=OWNERS.get(e);
        return m!=null&&!m.isInvalid()&&m.getWorldObj()==e.worldObj&&m.isStructureFormed()&&m.enabled?m:null;
    }
    public void release(){
        synchronized(OWNERS){Iterator<Map.Entry<Entity,MultiTileIndustrialCrusher>> it=OWNERS.entrySet().iterator();
            while(it.hasNext())if(it.next().getValue()==machine)it.remove();}
        cargo.clear();clientIds=new int[0];running=false;
    }
    public void clientTick(){
        for(int id:clientIds){Entity e=machine.getWorldObj().getEntityByID(id);if(e!=null)OWNERS.put(e,machine);}
    }
    private double[] local(Entity e){
        double x=e.posX-machine.xCoord-.5,z=e.posZ-machine.zCoord-.5;
        return CrusherConveyorRules.localOffset(machine.getBlockMetadata(),x,z);
    }
    private void position(Entity e,double u,double v){
        double[] offset=CrusherConveyorRules.worldOffset(machine.getBlockMetadata(),u,v);
        double x=offset[0],z=offset[1];
        double feetOffset=e.posY-e.boundingBox.minY;
        e.setPosition(machine.xCoord+.5+x,machine.yCoord+CrusherConveyorRules.DECK_Y+.01+feetOffset,machine.zCoord+.5+z);
        e.motionX=e.motionY=e.motionZ=0;e.fallDistance=0;e.onGround=true;e.velocityChanged=true;
        if(e instanceof EntityLiving){
            float yaw=machine.getBlockMetadata()==3?180:machine.getBlockMetadata()==4?-90:machine.getBlockMetadata()==5?90:0;
            EntityLiving mob=(EntityLiving)e;mob.rotationYaw=mob.rotationYawHead=mob.renderYawOffset=yaw;
        }
    }
    private boolean eligible(Entity e){
        if(e==null||e.isDead||e.worldObj!=machine.getWorldObj()||e.ridingEntity!=null||e.riddenByEntity!=null)return false;
        if(!(e instanceof EntityItem)&&(!(e instanceof EntityLiving)||e instanceof IBossDisplayData
                ||e instanceof net.minecraft.entity.boss.EntityWither||e instanceof net.minecraft.entity.boss.EntityDragon
                ||!((EntityLiving)e).isEntityAlive()||e.width>2.4F||e.height>3F))return false;
        MultiTileIndustrialCrusher other=owner(e);
        if(other!=null&&other!=machine)return false;
        if(e.getEntityData().getLong("HTCrusherCooldown")>e.worldObj.getTotalWorldTime())return false;
        double[] p=local(e);
        double end=other==machine&&e instanceof EntityLiving?CrusherConveyorRules.MOB_END+.01:6.5;
        return Math.abs(p[0])<1.42&&p[1]>=.55&&p[1]<=end
                &&e.boundingBox.minY>=machine.yCoord+3.0&&e.boundingBox.minY<=machine.yCoord+3.7;
    }
    public boolean tick(boolean powered){
        consumedPower=false;
        boolean wasRunning=running;
        if(!machine.isStructureFormed()||!machine.enabled){release();return wasRunning;}
        boolean dirty=false;
        boolean outputMoving=powered&&!pending.isEmpty()&&outputTicks<CrusherConveyorRules.OUTPUT_TICKS;
        if(outputMoving){outputTicks=CrusherConveyorRules.advanceOutput(outputTicks,true);consumedPower=true;machine.markDirty();}
        if(CrusherConveyorRules.canUnload(outputTicks,powered))dirty=flush();
        if(++sequence%10==0&&cargo.size()<MAX_ENTITIES&&pending.isEmpty()&&powered){
            int[] a=machine.localToWorld(-2,3,0),b=machine.localToWorld(2,4,7);
            AxisAlignedBB bounds=AxisAlignedBB.getBoundingBox(Math.min(a[0],b[0]),machine.yCoord+3,
                    Math.min(a[2],b[2]),Math.max(a[0],b[0])+1,machine.yCoord+4.8,Math.max(a[2],b[2])+1);
            List entities=machine.getWorldObj().getEntitiesWithinAABB(Entity.class,bounds);
            for(Object object:entities){Entity e=(Entity)object;
                if(cargo.size()>=MAX_ENTITIES)break;
                int mobs=0;for(Entity held:cargo)if(held instanceof EntityLiving)mobs++;
                if(eligible(e)&&!cargo.contains(e)&&CrusherConveyorRules.hasCapacity(cargo.size(),mobs,e instanceof EntityLiving)){
                    cargo.add(e);OWNERS.put(e,machine);e.getEntityData().setInteger("HTCrusherHeld",0);dirty=true;
                }
            }
        }
        boolean feedRunning=powered&&!cargo.isEmpty()&&pending.isEmpty();
        running=feedRunning||outputMoving;
        consumedPower|=feedRunning;
        Collections.sort(cargo,new Comparator<Entity>(){public int compare(Entity a,Entity b){return Double.compare(local(b)[1],local(a)[1]);}});
        double mobStop=CrusherConveyorRules.MOB_END;
        for(Iterator<Entity> it=cargo.iterator();it.hasNext();){
            Entity e=it.next();
            if(!eligible(e)){OWNERS.remove(e);it.remove();dirty=true;continue;}
            double[] p=local(e);
            // Pause holds position but never sets NoAI/noClip or alters entity dimensions.
            double limit=e instanceof EntityLiving?mobStop:CrusherConveyorRules.END;
            double v=CrusherConveyorRules.advance(p[1],limit,feedRunning);
            if(e instanceof EntityLiving)mobStop=Math.max(.55,v-CrusherConveyorRules.MOB_GAP);
            position(e,p[0],v);
            if(!feedRunning)continue;
            if(e instanceof EntityItem){
                EntityItem item=(EntityItem)e;
                item.age=Math.min(item.age,4800);
                if(v>=6.29){
                    ItemStack stack=item.getEntityItem();int accepted=insert(stack,MultiTileIndustrialCrusher.INPUT_SLOT_START);
                    if(accepted>0){
                        ItemStack remaining=stack.copy();remaining.stackSize-=accepted;
                        if(remaining.stackSize==0){item.setDead();OWNERS.remove(e);it.remove();}
                        else item.setEntityItemStack(remaining);
                        dirty=true;
                    }
                }
            }else{
                float health=((EntityLiving)e).getHealth();
                int held=health<e.getEntityData().getFloat("HTCrusherHealth")?0:e.getEntityData().getInteger("HTCrusherHeld")+1;
                e.getEntityData().setFloat("HTCrusherHealth",health);e.getEntityData().setInteger("HTCrusherHeld",held);
                if(v>=CrusherConveyorRules.MOB_END-.01&&sequence%CrusherConveyorRules.DAMAGE_INTERVAL==0&&pending.isEmpty())
                    ((EntityLiving)e).attackEntityFrom(new CrushingDamage(machine,e),CrusherConveyorRules.DAMAGE);
                // Release only stalled/invulnerable entities, never a healthy high-HP victim still taking damage.
                if(held>=1200&&((EntityLiving)e).isEntityAlive()){
                    e.getEntityData().setLong("HTCrusherCooldown",e.worldObj.getTotalWorldTime()+200);
                    position(e,p[0],.56);OWNERS.remove(e);it.remove();dirty=true;
                }
            }
            if(!pending.isEmpty()){
                // Stop the input queue, but keep the drive alive for the newly created output cargo.
                feedRunning=false;running=powered;dirty=true;
            }
        }
        if(dirty)machine.markDirty();
        return dirty||wasRunning!=running||sequence%5==0&&(!cargo.isEmpty()||!pending.isEmpty());
    }
    private int insert(ItemStack stack,int start){
        if(stack==null||stack.stackSize<=0)return 0;
        int remaining=stack.stackSize;
        for(int pass=0;pass<2;pass++)for(int i=start;i<start+MultiTileIndustrialCrusher.SLOT_COUNT_PER_BUFFER&&remaining>0;i++){
            ItemStack stored=machine.inventory[i];
            if(pass==0&&stored!=null&&stored.isItemEqual(stack)&&ItemStack.areItemStackTagsEqual(stored,stack)){
                int moved=Math.min(remaining,Math.max(0,Math.min(stored.getMaxStackSize(),machine.getInventoryStackLimit())-stored.stackSize));
                stored.stackSize+=moved;remaining-=moved;
            }else if(pass==1&&stored==null){
                int moved=Math.min(remaining,Math.min(stack.getMaxStackSize(),machine.getInventoryStackLimit()));
                ItemStack copy=stack.copy();copy.stackSize=moved;machine.inventory[i]=copy;remaining-=moved;
            }
        }
        return stack.stackSize-remaining;
    }
    private boolean flush(){
        // Recipe products already reserve output space. Do not steal it mid-operation.
        if(!machine.canDeliverMobDrops()||pending.isEmpty()||outputTicks<CrusherConveyorRules.OUTPUT_TICKS)return false;
        ItemStack stack=pending.get(0);
        int moved=insert(stack,MultiTileIndustrialCrusher.OUTPUT_SLOT_START);
        stack.stackSize-=moved;
        if(stack.stackSize<=0){pending.remove(0);outputTicks=0;}
        // A partial transfer remains at the end of the belt, with its exact remaining amount.
        return moved>0;
    }
    public void write(NBTTagCompound tag){
        NBTTagList drops=new NBTTagList();for(ItemStack stack:pending){NBTTagCompound n=new NBTTagCompound();stack.writeToNBT(n);n.setInteger("CrusherCount",stack.stackSize);drops.appendTag(n);}
        tag.setTag("CrusherMobDrops",drops);tag.setBoolean("CrusherBeltRunning",running);
        tag.setInteger("CrusherOutputTicks",outputTicks);
        int[] ids=new int[cargo.size()];for(int i=0;i<ids.length;i++)ids[i]=cargo.get(i).getEntityId();
        tag.setIntArray("CrusherCargoIds",ids);
    }
    public void read(NBTTagCompound tag){
        pending.clear();NBTTagList list=tag.getTagList("CrusherMobDrops",10);
        for(int i=0;i<list.tagCount();i++){
            NBTTagCompound entry=list.getCompoundTagAt(i);ItemStack stack=ItemStack.loadItemStackFromNBT(entry);
            if(stack!=null&&entry.hasKey("CrusherCount"))stack.stackSize=entry.getInteger("CrusherCount");
            if(stack!=null&&stack.stackSize>0)pending.add(stack);
        }
        outputTicks=pending.isEmpty()?0:Math.max(0,Math.min(CrusherConveyorRules.OUTPUT_TICKS,tag.getInteger("CrusherOutputTicks")));
        if(machine.getWorldObj()!=null&&machine.getWorldObj().isRemote){
            release();clientIds=tag.getIntArray("CrusherCargoIds");running=tag.getBoolean("CrusherBeltRunning");
            clientOutputSyncTick=machine.getWorldObj().getTotalWorldTime();clientTick();return;
        }
        running=false;
    }
    public void dropContents(){
        if(machine.getWorldObj()==null||machine.getWorldObj().isRemote)return;
        for(ItemStack stack:pending)machine.getWorldObj().spawnEntityInWorld(new EntityItem(machine.getWorldObj(),machine.xCoord+.5,machine.yCoord+.5,machine.zCoord+.5,stack.copy()));
        pending.clear();outputTicks=0;release();
    }
    private static final class CrushingDamage extends DamageSource {
        final MultiTileIndustrialCrusher machine;final Entity victim;
        CrushingDamage(MultiTileIndustrialCrusher m,Entity e){super("htgt6.crusher");machine=m;victim=e;setDamageBypassesArmor();}
    }
    public static final class Events {
        @SubscribeEvent(priority=EventPriority.LOWEST)
        public void drops(LivingDropsEvent event){
            if(!(event.source instanceof CrushingDamage)||event.entityLiving.worldObj.isRemote)return;
            CrushingDamage source=(CrushingDamage)event.source;
            if(source.victim!=event.entityLiving||source.machine.isInvalid())return;
            for(EntityItem drop:event.drops)if(drop!=null&&drop.getEntityItem()!=null&&drop.getEntityItem().stackSize>0)
                source.machine.conveyor.pending.add(drop.getEntityItem().copy());
            event.drops.clear();source.machine.markDirty();
        }
        @SubscribeEvent public void living(LivingUpdateEvent event){
            if(!event.entityLiving.worldObj.isRemote&&owner(event.entityLiving)!=null&&event.entityLiving.isEntityAlive()){
                // Forge fires this before Entity.onUpdate: retain base timers, potions,
                // hurt resistance and environmental damage, but skip AI and locomotion.
                event.entityLiving.onEntityUpdate();
                event.setCanceled(true);
            }
        }
        @SubscribeEvent public void unload(WorldEvent.Unload event){
            synchronized(OWNERS){Iterator<Entity> it=OWNERS.keySet().iterator();while(it.hasNext())if(it.next().worldObj==event.world)it.remove();}
        }
    }
}
