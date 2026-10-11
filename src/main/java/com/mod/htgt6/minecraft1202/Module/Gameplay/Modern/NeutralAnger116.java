package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.eventhandler.*;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.world.WorldEvent;
/** Persistent anger memory for native neutral mobs; ForgeData supplies normal entity saving. */
public final class NeutralAnger116 {
    private static final String TIME="HTGT6NeutralAngerTime",UUID="HTGT6NeutralAngryAt",PLAYER="HTGT6NeutralAngryPlayer",RESET="HTGT6NeutralUniversalReset";
    public static boolean supports(EntityLivingBase e){return e.getClass()==EntityEnderman.class||e.getClass()==EntityIronGolem.class&&!((EntityIronGolem)e).isPlayerCreated()||e.getClass()==EntityWolf.class&&!((EntityWolf)e).isTamed();}
    public static int time(EntityLivingBase e){return Math.max(0,e.getEntityData().getInteger(TIME));}
    public static EntityLivingBase target(EntityCreature e){Entity target=e instanceof EntityEnderman?e.getEntityToAttack():e.getAttackTarget();return target instanceof EntityLivingBase?(EntityLivingBase)target:null;}
    private static void target(EntityCreature e,EntityLivingBase target){if(e instanceof EntityEnderman){e.setTarget(target);((EntityEnderman)e).setScreaming(target!=null);}else e.setAttackTarget(target);if(e instanceof EntityWolf)((EntityWolf)e).setAngry(target!=null||time(e)>0);}
    private static void remember(EntityCreature e,EntityLivingBase target){NBTTagCompound n=e.getEntityData();if(target==null||n.getBoolean(RESET))return;n.setInteger("HTGT6NeutralTargetRuntimeId",target.getEntityId());String id=target.getUniqueID().toString();if(time(e)==0||!id.equals(n.getString(UUID))){n.setInteger(TIME,400+e.getRNG().nextInt(381));n.setString(UUID,id);n.setBoolean(PLAYER,target instanceof EntityPlayer);}}
    public static void clear(EntityCreature e){NBTTagCompound n=e.getEntityData();n.removeTag(TIME);n.removeTag(UUID);n.removeTag(PLAYER);n.removeTag(RESET);e.setRevengeTarget(null);target(e,null);}
    public static void hurt(EntityCreature e,EntityLivingBase attacker){if(!supports(e)||e.worldObj.isRemote||attacker==null||attacker instanceof EntityPlayer&&((EntityPlayer)attacker).capabilities.isCreativeMode)return;
        if(attacker instanceof EntityPlayer&&e.worldObj.getGameRules().getGameRuleBooleanValue("universalAnger")){
            e.getEntityData().setBoolean(RESET,true);
            if(e instanceof EntityWolf)for(Object value:e.worldObj.getEntitiesWithinAABB(EntityWolf.class,e.boundingBox.expand(e.getEntityAttribute(SharedMonsterAttributes.followRange).getAttributeValue(),10,e.getEntityAttribute(SharedMonsterAttributes.followRange).getAttributeValue())))if(supports((EntityLivingBase)value))((EntityLivingBase)value).getEntityData().setBoolean(RESET,true);
        }else remember(e,attacker);
    }
    public static void tick(EntityCreature e){if(!supports(e)||e.worldObj.isRemote||!e.isEntityAlive())return;NBTTagCompound n=e.getEntityData();
        if(n.getBoolean(RESET)){clear(e);n.setInteger(TIME,400+e.getRNG().nextInt(381));}
        EntityLivingBase current=target(e);
        if(current==null&&!n.getBoolean(PLAYER)&&!n.getString(UUID).isEmpty()){Entity remembered=e.worldObj.getEntityByID(n.getInteger("HTGT6NeutralTargetRuntimeId"));if(remembered instanceof EntityLiving&&remembered.getUniqueID().toString().equals(n.getString(UUID))){clear(e);return;}}
if(current!=null&&!current.isEntityAlive()){
            if(!(current instanceof EntityPlayer)||forgives(e)){clear(e);return;}target(e,null);current=null;
        }
        if(current instanceof EntityPlayer&&((EntityPlayer)current).capabilities.isCreativeMode){clear(e);return;}
        remember(e,current);
        if(time(e)<=0)return;
        if(!(current instanceof EntityPlayer)){int remaining=time(e)-1;if(remaining<=0){clear(e);return;}n.setInteger(TIME,remaining);}
        if(current==null&&(e.ticksExisted%10==0||n.getString(UUID).isEmpty())){
            boolean universal=n.getString(UUID).isEmpty()&&e.worldObj.getGameRules().getGameRuleBooleanValue("universalAnger");double radius=e.getEntityAttribute(SharedMonsterAttributes.followRange).getAttributeValue(),best=radius*radius;EntityPlayer selected=null;
            for(Object value:e.worldObj.playerEntities){EntityPlayer p=(EntityPlayer)value;if(!universal&&!p.getUniqueID().toString().equals(n.getString(UUID)))continue;if(!p.isEntityAlive()){if(forgives(e)&&!universal){clear(e);return;}continue;}double distance=e.getDistanceSqToEntity(p);if(!p.capabilities.isCreativeMode&&distance<best&&e.getEntitySenses().canSee(p)){best=distance;selected=p;}}
            if(selected!=null){target(e,selected);remember(e,selected);}
        }
        if(e instanceof EntityWolf)((EntityWolf)e).setAngry(time(e)>0);
    }
    private static boolean forgives(EntityLivingBase e){return !e.worldObj.getGameRules().hasRule("forgiveDeadPlayers")||e.worldObj.getGameRules().getGameRuleBooleanValue("forgiveDeadPlayers");}
    @SubscribeEvent(priority=EventPriority.LOWEST) public void hurt(LivingHurtEvent event){if(event.ammount>0&&supports(event.entityLiving)&&event.source.getEntity() instanceof EntityLivingBase)hurt((EntityCreature)event.entityLiving,(EntityLivingBase)event.source.getEntity());}
    @SubscribeEvent public void target(LivingSetAttackTargetEvent event){if(supports(event.entityLiving)&&!event.entityLiving.worldObj.isRemote)remember((EntityCreature)event.entityLiving,event.target);}
    @SubscribeEvent public void tick(LivingEvent.LivingUpdateEvent event){if(event.entityLiving instanceof EntityCreature)tick((EntityCreature)event.entityLiving);}
    @SubscribeEvent public void died(LivingDeathEvent event){if(!(event.entityLiving instanceof EntityPlayer)||event.entityLiving.worldObj.isRemote||!forgives(event.entityLiving))return;String id=event.entityLiving.getUniqueID().toString();for(Object value:event.entityLiving.worldObj.loadedEntityList)if(value instanceof EntityCreature&&supports((EntityCreature)value)&&id.equals(((EntityCreature)value).getEntityData().getString(UUID)))clear((EntityCreature)value);}
    @SubscribeEvent public void world(WorldEvent.Load event){if(!event.world.getGameRules().hasRule("universalAnger"))event.world.getGameRules().addGameRule("universalAnger","false");if(!event.world.getGameRules().hasRule("forgiveDeadPlayers"))event.world.getGameRules().addGameRule("forgiveDeadPlayers","true");}
}
