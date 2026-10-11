package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.item.*;
import net.minecraft.enchantment.*;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

/** Owns the attack before damage enters shield, hurt immunity and armour processing. */
public final class CombatAttack120 {
    public static final IAttribute ATTACK_SPEED=new RangedAttribute("generic.attackSpeed",4,0,1024).setShouldWatch(true);
    public static final IAttribute LUCK=new RangedAttribute("generic.luck",0,-1024,1024).setShouldWatch(true);
    public static final IAttribute ARMOR=new RangedAttribute("generic.armor",0,0,1024).setShouldWatch(true);
    public static final IAttribute TOUGHNESS=new RangedAttribute("generic.armorToughness",0,0,1024).setShouldWatch(true);
    private static final Map<EntityPlayer,State> STATES=Collections.synchronizedMap(new WeakHashMap<EntityPlayer,State>());
    private static final UUID WEAPON=UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID STRENGTH=UUID.fromString("648D7064-6A60-4F59-8ABE-C2C23A6DD7A9");
    private static final UUID WEAKNESS=UUID.fromString("22653B89-116E-49DC-9B6B-9971489B5BE5");
    private static final class State {long tick=Long.MIN_VALUE,attacked=Long.MIN_VALUE,swing=Long.MIN_VALUE;Item last;}
    private static State state(EntityPlayer p){State s=STATES.get(p);if(s==null){s=new State();s.last=p.getHeldItem()==null?null:p.getHeldItem().getItem();STATES.put(p,s);}return s;}
    public static void register(EntityLivingBase living){
        if(living.getEntityAttribute(ARMOR)==null)living.getAttributeMap().registerAttribute(ARMOR);
        if(living.getEntityAttribute(TOUGHNESS)==null)living.getAttributeMap().registerAttribute(TOUGHNESS);
        if(living instanceof EntityPlayer&&living.getEntityAttribute(LUCK)==null)living.getAttributeMap().registerAttribute(LUCK);
        if(living instanceof EntityPlayer&&living.getEntityAttribute(ATTACK_SPEED)==null)living.getAttributeMap().registerAttribute(ATTACK_SPEED);
    }
    public static void tick(EntityPlayer p){
        register(p);State s=state(p);long now=p.worldObj.getTotalWorldTime();Item item=p.getHeldItem()==null?null:p.getHeldItem().getItem();
        if(item!=s.last){s.tick=now;s.last=item;}
        // Swing packets precede attack packets. Defer air-swing reset until the following tick.
        if(s.swing!=Long.MIN_VALUE&&s.swing<now){if(s.attacked<s.swing)s.tick=s.swing;s.swing=Long.MIN_VALUE;}
    }
    public static void swing(EntityLivingBase living){if(living instanceof EntityPlayer){EntityPlayer p=(EntityPlayer)living;state(p).swing=p.worldObj.getTotalWorldTime();}}
    public static float strength(EntityPlayer p,float partial){
        State s=state(p);Item item=p.getHeldItem()==null?null:p.getHeldItem().getItem();
        if(item!=s.last){s.tick=p.worldObj.getTotalWorldTime();s.last=item;}
        if(s.tick==Long.MIN_VALUE)return 1;
        return MathHelper.clamp_float((p.worldObj.getTotalWorldTime()-s.tick+partial)*speed(p)/20F,0,1);
    }
    public static float speed(EntityPlayer p){
        IAttributeInstance attr=p.getEntityAttribute(ATTACK_SPEED);
        boolean itemSpeed=p.getHeldItem()!=null&&!p.getHeldItem().getAttributeModifiers().get("generic.attackSpeed").isEmpty();
        double base=(attr==null?4:attr.getBaseValue())+(itemSpeed?0:AttackCooldown112.getAttackSpeed(p.getHeldItem())-4);
        if(attr==null)return (float)Math.max(.001,base);
        for(AttributeModifier m:modifiers(attr,0))base+=m.getAmount();
        double value=base;
        for(AttributeModifier m:modifiers(attr,1))value+=base*m.getAmount();
        for(AttributeModifier m:modifiers(attr,2))value*=1+m.getAmount();
        return (float)Math.max(.001,value);
    }
    private static java.util.List<AttributeModifier> modifiers(IAttributeInstance attr,int operation){
        java.util.List<AttributeModifier> result=new ArrayList<AttributeModifier>();
        for(Object raw:attr.func_111122_c()){AttributeModifier m=(AttributeModifier)raw;if(m.getOperation()==operation)result.add(m);}return result;
    }
    public static float baseDamage(EntityPlayer p){
        IAttributeInstance attr=p.getEntityAttribute(SharedMonsterAttributes.attackDamage);
        float modern=AttackCooldown112.getVanillaAttackDamage(p.getHeldItem());
        if(p.getHeldItem()!=null&&p.getHeldItem().hasTagCompound()&&p.getHeldItem().getTagCompound().hasKey("AttributeModifiers"))modern=Float.NaN;
        double base=attr.getBaseValue();
        for(Object raw:modifiers(attr,0)){AttributeModifier m=(AttributeModifier)raw;
            if(!m.getID().equals(WEAPON)||Float.isNaN(modern))if(!m.getID().equals(STRENGTH)&&!m.getID().equals(WEAKNESS))base+=m.getAmount();}
        if(!Float.isNaN(modern))base+=modern-1;
        if(p.isPotionActive(Potion.damageBoost))base+=3*(p.getActivePotionEffect(Potion.damageBoost).getAmplifier()+1);
        if(p.isPotionActive(Potion.weakness))base-=4*(p.getActivePotionEffect(Potion.weakness).getAmplifier()+1);
        double value=base;
        for(Object raw:modifiers(attr,1)){AttributeModifier m=(AttributeModifier)raw;if(!m.getID().equals(STRENGTH)&&!m.getID().equals(WEAKNESS))value+=base*m.getAmount();}
        for(Object raw:modifiers(attr,2)){AttributeModifier m=(AttributeModifier)raw;if(!m.getID().equals(STRENGTH)&&!m.getID().equals(WEAKNESS))value*=1+m.getAmount();}
        return (float)Math.max(0,value);
    }
    public static void attack(EntityPlayer p,Entity target){
        if(MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(p,target)))return;
        ItemStack held=p.getHeldItem();
        if(held!=null&&held.getItem().onLeftClickEntity(held,p,target)||!target.canAttackWithItem()||target.hitByEntity(p))return;
        float cooled=strength(p,.5F);State s=state(p);s.tick=s.attacked=p.worldObj.getTotalWorldTime();
        float base=baseDamage(p)*( .2F+.8F*cooled*cooled);
        float extra=EnchantmentHelper.func_152377_a(held,target instanceof EntityLivingBase?((EntityLivingBase)target).getCreatureAttribute():EnumCreatureAttribute.UNDEFINED)*cooled;
        if(base<=0&&extra<=0)return;
        boolean strong=cooled>.9F,sprint=p.isSprinting()&&strong;
        boolean critical=strong&&p.fallDistance>0&&!p.onGround&&!p.isOnLadder()&&!p.isInWater()&&!p.isPotionActive(Potion.blindness)&&p.ridingEntity==null&&!p.isSprinting()&&target instanceof EntityLivingBase;
        float damage=base*(critical?1.5F:1F)+extra;
        boolean sweep=strong&&!critical&&!sprint&&p.onGround&&p.distanceWalkedModified-p.prevDistanceWalkedModified<p.getAIMoveSpeed()&&held!=null&&held.getItem() instanceof ItemSword;
        int kb=(target instanceof EntityLivingBase?EnchantmentHelper.getKnockbackModifier(p,(EntityLivingBase)target):0)+(sprint?1:0);
        int fire=EnchantmentHelper.getFireAspectModifier(p);boolean ignite=target instanceof EntityLivingBase&&fire>0&&!target.isBurning();
        if(ignite)target.setFire(1);
        double vx=target.motionX,vy=target.motionY,vz=target.motionZ;
        if(target.attackEntityFrom(DamageSource.causePlayerDamage(p),damage)){
            if(kb>0){knockback(target,kb*.5,p.rotationYaw);p.motionX*=.6;p.motionZ*=.6;p.setSprinting(false);}
            if(sweep)sweep(p,target,damage);
            if(target instanceof EntityPlayerMP&&target.velocityChanged){((EntityPlayerMP)target).playerNetServerHandler.sendPacket(new net.minecraft.network.play.server.S12PacketEntityVelocity(target));target.velocityChanged=false;target.motionX=vx;target.motionY=vy;target.motionZ=vz;}
            if(critical)p.onCriticalHit(target);if(extra>0)p.onEnchantmentCritical(target);
            p.setLastAttacker(target);
            if(target instanceof EntityLivingBase)EnchantmentHelper.func_151384_a((EntityLivingBase)target,p);
            EnchantmentHelper.func_151385_b(p,target);
            Entity hit=target;
            if(target instanceof net.minecraft.entity.boss.EntityDragonPart){net.minecraft.entity.IEntityMultiPart parent=((net.minecraft.entity.boss.EntityDragonPart)target).entityDragonObj;if(parent instanceof EntityLivingBase)hit=(EntityLivingBase)parent;}
            if(held!=null&&hit instanceof EntityLivingBase){held.hitEntity((EntityLivingBase)hit,p);if(held.stackSize<=0)p.destroyCurrentEquippedItem();}
            if(target instanceof EntityLivingBase){p.addStat(net.minecraft.stats.StatList.damageDealtStat,Math.round(damage*10));if(fire>0)target.setFire(fire*4);}
            p.addExhaustion(.1F);
        }else if(ignite)target.extinguish();
    }
    public static void hurtKnockback(EntityLivingBase target,Entity attacker,float damage,double dx,double dz){
        if(dx*dx+dz*dz<1E-8)return;
        knockback(target,.4,(float)(Math.atan2(dx,-dz)*180/Math.PI));
    }
    public static void knockback(Entity target,double strength,float yaw){
        double resistance=target instanceof EntityLivingBase?((EntityLivingBase)target).getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue():0;
        strength*=Math.max(0,1-resistance);if(strength<=0)return;
        double angle=yaw*Math.PI/180;
        target.motionX=target.motionX/2-Math.sin(angle)*strength;target.motionZ=target.motionZ/2+Math.cos(angle)*strength;
        if(target.onGround)target.motionY=Math.min(.4,target.motionY/2+strength);target.velocityChanged=true;
    }
    private static void sweep(EntityPlayer p,Entity primary,float damage){
        int level=EnchantmentHelper.getEnchantmentLevel(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.sweepingEdge.effectId,p.getHeldItem());
        float amount=1+(level>0?(float)level/(level+1):0)*damage;
        for(Object raw:p.worldObj.getEntitiesWithinAABB(EntityLivingBase.class,primary.boundingBox.expand(1,.25,1))){EntityLivingBase t=(EntityLivingBase)raw;
            if(t!=p&&t!=primary&&!t.isOnSameTeam(p)&&p.getDistanceSqToEntity(t)<9){knockback(t,.4,p.rotationYaw);t.attackEntityFrom(DamageSource.causePlayerDamage(p),amount);}}
        if(!p.worldObj.isRemote){double angle=p.rotationYaw*Math.PI/180,x=p.posX-Math.sin(angle),y=p.posY+p.height*.5,z=p.posZ+Math.cos(angle);
            com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatUpdate.NETWORK.sendToAllAround(new com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketSweepAttackEffect(x,y,z),new cpw.mods.fml.common.network.NetworkRegistry.TargetPoint(p.dimension,x,y,z,64));}
        p.playSound("htgt6:entity.player.attack.sweep",1,1);
    }
    private CombatAttack120(){}
}
