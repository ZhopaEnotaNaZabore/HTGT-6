package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.CampfireSmoke152;
/** Compare steam positions with the existing ingredient renderer's rotated corners. */
public final class Smoke164Fixture {
 public static void main(String[] args)throws Exception {
  for(int facing=0;facing<4;facing++)for(int slot=0;slot<4;slot++){
   double angle=-Math.PI/2*((slot+facing)&3);
   double expectedX=-.3125*Math.cos(angle)-.3125*Math.sin(angle);
   double expectedZ=.3125*Math.sin(angle)-.3125*Math.cos(angle);
   if(Math.abs(CampfireSmoke152.cookingOffsetX(slot,facing)-expectedX)>1E-9
    ||Math.abs(CampfireSmoke152.cookingOffsetZ(slot,facing)-expectedZ)>1E-9)
    throw new AssertionError("Steam not above ingredient: "+slot+"/"+facing);
  }
  System.out.println("PASS smoke164: 16 ingredient/steam positions");
  Smoke152Fixture.main(args);
 }
}
