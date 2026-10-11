package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.*;
import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.SwimmingPlayerRenderer120;
import com.mod.htgt6.minecraft1202.Module.Network.Aquatic.PacketSwimming120;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import net.minecraft.client.model.ModelBiped;
import java.lang.reflect.*;
import java.util.*;
import io.netty.buffer.*;
public final class Swimming117Fixture {
 static int checks;
 public static void main(String[] args)throws Exception{
  BountifulMechanicsFixture120.Grid grid=BountifulMechanicsFixture120.initialize();
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(grid,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));grid.provider.worldObj=grid;
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(grid);p.setPosition(.5,10,.5);p.wet=p.eyes=true;p.setSprinting(true);
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);CollisionWorld w=(CollisionWorld)((sun.misc.Unsafe)uf.get(null)).allocateInstance(CollisionWorld.class);w.boxes=new ArrayList();p.worldObj=w;
  double foot=p.boundingBox.minY,position=p.posY;float offset=p.yOffset;
  PlayerSwimming120.updateState(p);check(p.height==.6F&&SwimmingPose120.kind(p)==2,"swimming dimensions");check(p.posY==position&&p.yOffset==offset&&p.boundingBox.minY==foot,"legacy stance and feet unchanged");
  check(Math.abs(p.posY+SwimmingPose120.eye(.12F,p)-foot-.4)<1e-6,"eye world height");
  p.prevPosY=p.posY;check(Math.abs(SwimmingPose120.rayOrigin(Vec3.createVectorHelper(0,0,0),p,1).yCoord-foot-.4)<1e-6,"ray origin agrees with eyes");
  check(SwimmingPose120.cameraHeight(1.62F,p,0)==1.62F,"camera starts at previous height");SwimmingPose120.animate(p);float camera=SwimmingPose120.cameraHeight(1.62F,p,.5F);check(camera<1.62F&&camera>.4F,"camera interpolates entry");
  AxisAlignedBB old=p.boundingBox.expand(0,-.4,0).contract(.001,.001,.001);check(old.minY>old.maxY,"legacy small box reproduction");AxisAlignedBB fixed=SwimmingPose120.waterBounds(old,p);check(fixed.maxY>fixed.minY&&Math.abs((fixed.maxY-fixed.minY)-.598)<1e-6,"water query retains physical bounds");
  w.boxes.add(AxisAlignedBB.getBoundingBox(-1,foot+1,-1,2,foot+3,2));p.setSprinting(false);p.wet=p.eyes=false;PlayerSwimming120.updateState(p);check(!PlayerSwimming120.isSwimming(p)&&SwimmingPose120.kind(p)==2&&p.height==.6F,"crawl below one-block ceiling without swim flag");
  w.boxes.clear();w.boxes.add(AxisAlignedBB.getBoundingBox(-1,foot+1.6,-1,2,foot+3,2));PlayerSwimming120.updateState(p);check(SwimmingPose120.kind(p)==1&&p.height==1.5F,"crouch exit when standing blocked");
  w.boxes.clear();PlayerSwimming120.updateState(p);check(SwimmingPose120.kind(p)==0&&p.height==1.8F,"stand after clearance");
  p.wet=p.eyes=true;p.setSprinting(true);PlayerSwimming120.updateState(p);for(int i=0;i<12;i++)SwimmingPose120.animate(p);check(SwimmingPose120.amount(p,1)==1,"animation reaches full pose");
  for(float phase:new float[]{0,7,14,18,22,25}){ModelBiped m=new ModelBiped();SwimmingPlayerRenderer120.model(m,p,phase,p.ticksExisted+1);check(Float.isFinite(m.bipedLeftArm.rotateAngleZ)&&Float.isFinite(m.bipedRightArm.rotateAngleX),"finite stroke "+phase);check(Math.abs(m.bipedLeftLeg.rotateAngleX+m.bipedRightLeg.rotateAngleX)<1e-6,"alternating kicks "+phase);}
  ModelBiped attacking=new ModelBiped();attacking.onGround=.5F;attacking.bipedRightArm.rotateAngleX=.321F;SwimmingPlayerRenderer120.model(attacking,p,7,p.ticksExisted+1);check(attacking.bipedRightArm.rotateAngleX==.321F,"attack arm preserved");
  ByteBuf b=Unpooled.buffer();PacketSwimming120 packet=new PacketSwimming120(p,true);try{packet.toBytes(b);check(b.getUnsignedByte(24)==5,"swim plus low pose encoded");PacketSwimming120 copy=new PacketSwimming120();copy.fromBytes(b);Field remote=World.class.getDeclaredField("isRemote");remote.setAccessible(true);remote.setBoolean(w,true);SwimmingPose120.apply(p,0);check(copy.applyTo(p,false)&&p.height==.6F,"remote packet restores collision pose");}finally{b.release();}
  PlayerSwimming120.clear(p);check(p.height==1.8F&&SwimmingPose120.kind(p)==0,"cleanup restores owned dimensions");
  p.setSprinting(true);check(PlayerSwimming120.waterGravity(.02,p)==0,"sprint does not sink");p.setSprinting(false);check(PlayerSwimming120.waterGravity(.02,p)==.005,"ordinary water gravity");
  net.minecraft.client.entity.EntityPlayerSP input=(net.minecraft.client.entity.EntityPlayerSP)((sun.misc.Unsafe)uf.get(null)).allocateInstance(net.minecraft.client.entity.EntityPlayerSP.class);
  input.capabilities=new net.minecraft.entity.player.PlayerCapabilities();input.movementInput=new net.minecraft.util.MovementInput();Field wet=Entity.class.getDeclaredField("inWater");wet.setAccessible(true);wet.setBoolean(input,true);
  input.movementInput.sneak=true;input.movementInput.moveForward=.3F;input.movementInput.moveStrafe=-.3F;
  com.mod.htgt6.minecraft1202.Module.Client.Aquatic.SwimmingInput120.update(input);check(input.movementInput.moveForward==1&&input.movementInput.moveStrafe==-1,"Shift keeps water movement input");
  input.capabilities.isFlying=true;input.movementInput.moveForward=.3F;com.mod.htgt6.minecraft1202.Module.Client.Aquatic.SwimmingInput120.update(input);check(input.movementInput.moveForward==.3F,"flight input unaffected");
  p.eyes=true;check(com.mod.htgt6.minecraft1202.Module.Client.Aquatic.SwimmingInput120.sprintSurface(false,p),"underwater double tap gate");p.eyes=false;check(!com.mod.htgt6.minecraft1202.Module.Client.Aquatic.SwimmingInput120.sprintSurface(false,p),"air double tap gate unchanged");
  boolean enabled=com.mod.htgt6.asm.HTGT6Config.RUN_MINECRAFT_1_7_9999;try{com.mod.htgt6.asm.HTGT6Config.RUN_MINECRAFT_1_7_9999=false;p.setSprinting(true);check(PlayerSwimming120.waterGravity(.02,p)==.02,"disabled module keeps gravity");check(PlayerSwimming120.waterDrag(p,.8F)==.8F,"disabled module keeps drag");p.setAir(0);check(PlayerBreathing120.surfaceAir(300,p)==300,"disabled module keeps refill");}finally{com.mod.htgt6.asm.HTGT6Config.RUN_MINECRAFT_1_7_9999=enabled;}
  System.out.println("PASS swimming117: "+checks+" collision, stance, eye, ray, animation, pose packet and gravity checks");
 }
 static final class CollisionWorld extends World{
  List<AxisAlignedBB> boxes;private CollisionWorld(){super(null,"fixture",(net.minecraft.world.WorldProvider)null,(net.minecraft.world.WorldSettings)null,null);}
  @Override protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  @Override protected int func_152379_p(){return 0;}
  @Override public Entity getEntityByID(int id){return null;}
  @Override public List getCollidingBoundingBoxes(Entity e,AxisAlignedBB query){List out=new ArrayList();for(AxisAlignedBB b:boxes)if(b.intersectsWith(query))out.add(b);return out;}
 }
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
}
