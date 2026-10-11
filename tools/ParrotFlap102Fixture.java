import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityParrot120;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.ModelParrot120;
import net.minecraft.util.MathHelper;

public final class ParrotFlap102Fixture {
 public static void main(String[] args)throws Exception {
  EntityParrot120 parrot=new EntityParrot120(BountifulMechanicsFixture120.initialize());
  parrot.onGround=false;parrot.motionY=-1;parrot.updateFlapping();
  close(parrot.flapSpeed,1,"air amplitude clamp");close(parrot.flap,1.8F,"phase");close((float)parrot.motionY,-.6F,"glide applied once");
  close(parrot.flapAmount(0),0,"previous tick interpolation");
  close(parrot.flapAmount(.5F),(MathHelper.sin(.9F)+1)*.5F,"half tick interpolation");
  close(parrot.flapAmount(1),(MathHelper.sin(1.8F)+1),"current tick interpolation");
  parrot.onGround=true;for(int i=0;i<10;i++)parrot.updateFlapping();
  close(parrot.flapSpeed,0,"settles on ground");close(parrot.flapAmount(1),0,"ground wings stop");
  parrot.onGround=false;parrot.motionY=1;parrot.updateFlapping();close((float)parrot.motionY,1,"ascending speed unchanged");
  parrot.ridingEntity=new EntityParrot120(parrot.worldObj);for(int i=0;i<4;i++)parrot.updateFlapping();close(parrot.flapSpeed,0,"passenger amplitude settles");parrot.ridingEntity=null;
  ModelParrot120 model=new ModelParrot120();
  model.setRotationAngles(0,0,1,0,0,.0625F,parrot);
  close(model.leftWing.rotateAngleZ,-1.0873F,"left wing uses flap amount");close(model.rightWing.rotateAngleZ,1.0873F,"right wing uses flap amount");
  close(model.body.rotationPointY,16.8F,"flight bob");close(model.leftLeg.rotateAngleX,-.0299F+.6981317F,"flight legs");
  parrot.setSitting(true);model.setRotationAngles(1,1,1,0,0,.0625F,parrot);
  close(model.head.rotationPointY,17.59F,"sitting head");close(model.body.rotationPointY,18.4F,"sitting body");
  close(model.tail.rotateAngleX,1.5388988F,"sitting tail");close(model.leftLeg.rotationPointY,23.9F,"sitting legs");
  parrot.setRecordPlayingNearby(0,0,0,true);parrot.ticksExisted=2;model.setRotationAngles(1,1,1,0,0,.0625F,parrot);
  close(model.head.rotationPointY,15.69F+MathHelper.sin(2),"party overrides sitting");
  parrot.setRecordPlayingNearby(0,0,0,false);parrot.setSitting(false);parrot.onGround=true;
  model.setRotationAngles(0,0,0,0,0,.0625F,parrot);
  close(model.head.rotationPointX,0,"sway reset");close(model.leftLeg.rotationPointY,22,"leg height reset");close(model.tail.rotateAngleX,1.015F,"tail reset");close(model.leftWing.rotateAngleZ,-.0873F,"ground wing reset");
  System.out.println("PASS flap: phase/amplitude/interpolation, glide once, ascending/passenger/ground, flight/sitting/party pose and reset");
 }
 static void close(float actual,float expected,String message){if(Math.abs(actual-expected)>.00001F)throw new AssertionError(message+": "+actual);}
}
