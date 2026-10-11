package com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic;

import java.util.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

/** Keeps legacy position/stance coordinates intact while changing collision and eye heights. */
public final class SwimmingPose120 {
    private static final Map<EntityPlayer,Pose> POSES=Collections.synchronizedMap(new WeakHashMap<EntityPlayer,Pose>());
    private static final class Pose {float originalHeight,previous,amount;int kind;Pose(float h){originalHeight=h;}}
    private SwimmingPose120() {}
    public static int kind(EntityPlayer p){Pose s=POSES.get(p);return s==null?0:s.kind;}
    public static float amount(EntityPlayer p,float partial){Pose s=POSES.get(p);return s==null?0:s.previous+(s.amount-s.previous)*partial;}
    public static void animate(EntityPlayer p){Pose s=POSES.get(p);if(s==null)return;s.previous=s.amount;s.amount=Math.max(0,Math.min(1,s.amount+(s.kind==2?.09F:-.09F)));if(s.kind==0&&s.amount==0&&s.previous==0)POSES.remove(p);}
    public static void clear(EntityPlayer p){Pose s=POSES.remove(p);if(s!=null&&(p.height==.6F||p.height==1.5F)){p.height=s.originalHeight;p.boundingBox.maxY=p.boundingBox.minY+p.height;}}
    private static boolean fits(EntityPlayer p,float height){AxisAlignedBB b=p.boundingBox;return p.worldObj.getCollidingBoundingBoxes(p,AxisAlignedBB.getBoundingBox(b.minX,b.minY,b.minZ,b.maxX,b.minY+height,b.maxZ).contract(1e-7,1e-7,1e-7)).isEmpty();}
    public static void update(EntityPlayer p){
        Pose old=POSES.get(p);
        if(!p.isEntityAlive()||p.isPlayerSleeping()){clear(p);return;}
        boolean swimming=PlayerSwimming120.isSwimming(p);
        if(!swimming&&old==null)return;
        float standing=old==null?p.height:old.originalHeight;
        int desired=swimming?2:p.isSneaking()?1:0;
        if(p.ridingEntity!=null||p.capabilities.isFlying)desired=0;
        float h=desired==2?.6F:desired==1?1.5F:standing;
        if(!p.noClip&&!fits(p,h)){desired=fits(p,1.5F)?1:2;}
        apply(p,desired);
    }
    public static void apply(EntityPlayer p,int kind){
        Pose s=POSES.get(p);if(s==null){if(kind==0)return;s=new Pose(p.height);POSES.put(p,s);}
        s.kind=kind;p.height=kind==2?.6F:kind==1?1.5F:s.originalHeight;
        // Do not change yOffset or posY: C03/C08 still use the 1.7 stance contract.
        p.boundingBox.maxY=p.boundingBox.minY+p.height;
    }
    public static float eye(float legacy,EntityPlayer p){int k=kind(p);return k==0?legacy:(float)((k==2?.4D:1.27D)-(p.posY-p.boundingBox.minY));}
    public static float cameraHeight(float legacy,net.minecraft.entity.EntityLivingBase entity,float partial){if(!(entity instanceof EntityPlayer))return legacy;EntityPlayer p=(EntityPlayer)entity;float standing=kind(p)==1?1.27F:legacy;return standing+(.4F-standing)*amount(p,partial);}
    public static Vec3 rayOrigin(Vec3 legacy,EntityPlayer p,float partial){int k=kind(p);if(k!=0)legacy.yCoord=p.prevPosY+(p.posY-p.prevPosY)*partial-(p.posY-p.boundingBox.minY)+(k==2?.4D:1.27D);return legacy;}
    public static AxisAlignedBB waterBounds(AxisAlignedBB legacy,net.minecraft.entity.Entity entity){return entity instanceof EntityPlayer&&kind((EntityPlayer)entity)!=0?entity.boundingBox.contract(.001D,.001D,.001D):legacy;}
}
