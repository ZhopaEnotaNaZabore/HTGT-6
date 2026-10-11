package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

/** Modern stuck multiplier is consumed by one movement, not multiplied per touched block. */
public final class BerryMovement147 {
    private static final Map<Entity,Contact> PENDING=Collections.synchronizedMap(new WeakHashMap<Entity,Contact>());
    private BerryMovement147(){}
    public static boolean affected(Entity entity){return entity instanceof EntityLivingBase;}
    public static void touch(Entity entity){if(affected(entity)){entity.fallDistance=0;PENDING.put(entity,new Contact(entity));}}
    private static final class Contact {
        final double x,y,z;final int tick,dimension;
        Contact(Entity e){x=e.posX;y=e.posY;z=e.posZ;tick=e.ticksExisted;dimension=e.dimension;}
        boolean valid(Entity e){return e.dimension==dimension&&e.ticksExisted>=tick&&e.ticksExisted-tick<=1
            &&Math.abs(e.posX-x)<1E-6&&Math.abs(e.posY-y)<1E-6&&Math.abs(e.posZ-z)<1E-6;}
    }
    public static boolean consume(Entity entity,boolean inWeb){
        Contact contact=PENDING.remove(entity);
        if(contact==null||entity.noClip||entity.isDead||!contact.valid(entity))return false;
        // Keep vanilla web handling authoritative instead of multiplying two slowdowns.
        if(inWeb)return false;
        entity.motionX=entity.motionY=entity.motionZ=0;
        return true;
    }
}
