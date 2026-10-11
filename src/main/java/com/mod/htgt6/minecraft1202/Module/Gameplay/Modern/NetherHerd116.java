package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.*;
import net.minecraft.entity.*;
import java.util.*;
/** Local visible herd accounting, called on decisions/hits rather than every entity tick. */
public final class NetherHerd116 {
    public static List<EntityPiglin116> piglins(EntityLiving observer){List<EntityPiglin116> result=new ArrayList<EntityPiglin116>();for(Object o:observer.worldObj.getEntitiesWithinAABB(EntityPiglin116.class,observer.boundingBox.expand(16,16,16))){EntityPiglin116 p=(EntityPiglin116)o;if(p.isEntityAlive()&&!p.isChild()&&(p==observer||observer.getEntitySenses().canSee(p)))result.add(p);}return result;}
    public static List<EntityHoglin116> hoglins(EntityLiving observer){List<EntityHoglin116> result=new ArrayList<EntityHoglin116>();for(Object o:observer.worldObj.getEntitiesWithinAABB(EntityHoglin116.class,observer.boundingBox.expand(16,16,16))){EntityHoglin116 h=(EntityHoglin116)o;if(h.isEntityAlive()&&!h.isChild()&&(h==observer||observer.getEntitySenses().canSee(h)))result.add(h);}return result;}
    public static boolean hoglinsOutnumber(EntityPiglin116 observer){List<EntityPiglin116> ps=piglins(observer);return hoglins(observer).size()>ps.size()+(ps.contains(observer)?0:1);}
    public static boolean piglinsOutnumber(EntityHoglin116 observer){List<EntityHoglin116> hs=hoglins(observer);return piglins(observer).size()>hs.size()+(hs.contains(observer)?0:1);}
    private NetherHerd116(){}
}
