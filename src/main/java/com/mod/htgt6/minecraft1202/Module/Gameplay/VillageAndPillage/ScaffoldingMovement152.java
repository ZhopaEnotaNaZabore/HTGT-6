package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockScaffolding152;
import net.minecraft.entity.EntityLivingBase;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.util.MathHelper;import net.minecraft.world.World;
/** Legacy travel adapter for the modern climbable block. */
public final class ScaffoldingMovement152 {
 private ScaffoldingMovement152(){}
 public static void beforeTravel(EntityLivingBase e,boolean jumping){
  if(e==null||e.isDead||e.noClip||e.ridingEntity!=null||e instanceof EntityPlayer&&((EntityPlayer)e).capabilities.isFlying)return;
  World w=e.worldObj;int x=MathHelper.floor_double(e.posX),y=MathHelper.floor_double(e.boundingBox.minY+1E-5),z=MathHelper.floor_double(e.posZ);
  if(w==null||!w.blockExists(x,y,z)||!(w.getBlock(x,y,z) instanceof BlockScaffolding152))return;
  e.fallDistance=0;e.motionX=Math.max(-.15,Math.min(.15,e.motionX));e.motionZ=Math.max(-.15,Math.min(.15,e.motionZ));
  e.motionY=Math.max(-.15,e.motionY);
  if(jumping||e.isCollidedHorizontally)e.motionY=.2;
 }
}
