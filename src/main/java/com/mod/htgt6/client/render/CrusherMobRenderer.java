package com.mod.htgt6.client.render;

import com.mod.htgt6.common.crusher.CrusherConveyor;
import com.mod.htgt6.common.crusher.MultiTileIndustrialCrusher;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.client.event.RenderLivingEvent;
import org.lwjgl.opengl.GL11;

/** Scoped body pose, never global model/AI mutations. Foreign mob renderers are reused. */
public final class CrusherMobRenderer {
    private boolean rendering;
    private final WavingZombieRenderer zombie=new WavingZombieRenderer();
    public CrusherMobRenderer(){zombie.setRenderManager(RenderManager.instance);}

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void before(RenderLivingEvent.Pre event){
        if(rendering)return;
        MultiTileIndustrialCrusher machine=CrusherConveyor.owner(event.entity);
        if(machine==null)return;
        event.setCanceled(true);
        rendering=true;
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(event.x,event.y+.36,event.z);
            int facing=machine.getBlockMetadata();
            double yaw=Math.toRadians(facing==3?180:facing==4?-90:facing==5?90:0);
            // Face up, head upstream, feet towards the rollers. Pivot is above the belt.
            GL11.glRotatef(-90,(float)Math.cos(yaw),0,(float)Math.sin(yaw));
            // Preserve custom zombie subclasses/renderers (e.g. husks) and villager geometry.
            if(event.entity.getClass()==EntityZombie.class&&!((EntityZombie)event.entity).isVillager())
                zombie.doRender((EntityZombie)event.entity,0,0,0,event.entity.rotationYaw,0);
            else event.renderer.doRender(event.entity,0,0,0,event.entity.rotationYaw,0);
        } finally {GL11.glPopMatrix();rendering=false;}
    }

    private static final class WavingZombieRenderer extends RenderZombie {
        private final ModelZombie waving=new WavingModel();
        @Override protected void renderModel(EntityLivingBase entity,float limb,float amount,float age,float yaw,float pitch,float scale){
            ModelBase original=mainModel;
            waving.isChild=original.isChild;waving.isRiding=original.isRiding;waving.onGround=original.onGround;
            mainModel=waving;
            try{super.renderModel(entity,limb,amount,age,yaw,pitch,scale);}
            finally{mainModel=original;}
        }
    }
    private static final class WavingModel extends ModelZombie {
        @Override public void setRotationAngles(float limb,float amount,float age,float yaw,float pitch,float scale,Entity entity){
            super.setRotationAngles(limb,amount,age,yaw,pitch,scale,entity);
            MultiTileIndustrialCrusher m=CrusherConveyor.owner(entity);
            double wave=m!=null&&m.isConveyorRunning()?Math.sin(entity.worldObj.getTotalWorldTime()*.65):0;
            bipedRightArm.rotateAngleX=(float)(-1.6+wave*.65);
            bipedLeftArm.rotateAngleX=(float)(-1.6-wave*.65);
            bipedRightArm.rotateAngleZ=(float)(.35+wave*.25);
            bipedLeftArm.rotateAngleZ=(float)(-.35+wave*.25);
        }
    }
}
