package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.CampfireSmoke152;
import net.minecraft.client.particle.EntityFX;import net.minecraft.world.World;import java.lang.reflect.*;
public final class Smoke152Fixture {
 static int n;static void check(boolean b,String s){if(!b)throw new AssertionError(s);n++;}
 public static void main(String[]args)throws Exception{
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();Dolphin112Fixture.field(World.class,"theProfiler").set(w,new net.minecraft.profiler.Profiler());
  Field lifetime=Dolphin112Fixture.field(EntityFX.class,"particleMaxAge"),alpha=Dolphin112Fixture.field(EntityFX.class,"particleAlpha"),scale=Dolphin112Fixture.field(EntityFX.class,"particleScale");
  for(boolean signal:new boolean[]{false,true})for(int i=0;i<16;i++){
   CampfireSmoke152 p=new CampfireSmoke152(w,.5,10,.5,signal);int life=lifetime.getInt(p);
   check(life>=(signal?280:80)&&life<(signal?330:130),"modern lifetime range");check(Math.abs(alpha.getFloat(p)-(signal?.95F:.9F))<.00001,"modern alpha");check(scale.getFloat(p)>=3&&scale.getFloat(p)<=6,"modern quad size");
   double start=p.posY;float before=alpha.getFloat(p);for(int t=0;t<life-61;t++)p.onUpdate();check(alpha.getFloat(p)==before&&!p.isDead,"no premature fade");check(p.posY>start+(signal?10:1),"smoke rises with correct lifetime");
   for(int t=0;t<65;t++)p.onUpdate();check(p.isDead,"particle expires");check(alpha.getFloat(p)<before,"final60 ticks fade");
  }
  System.out.println("PASS smoke152: "+n);
 }
}
