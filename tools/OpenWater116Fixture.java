import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.OpenWaterFishing116;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import net.minecraft.init.Blocks;
import net.minecraft.entity.projectile.EntityFishHook;
public final class OpenWater116Fixture {
 static class WetPlant extends net.minecraft.block.Block implements com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150 {
  WetPlant(){super(net.minecraft.block.material.Material.plants);}
  public boolean isWaterlogged(net.minecraft.world.IBlockAccess w,int x,int y,int z){return true;}
  public boolean setWaterlogged(net.minecraft.world.World w,int x,int y,int z,boolean wet){return false;}
  public net.minecraft.util.AxisAlignedBB getCollisionBoundingBoxFromPool(net.minecraft.world.World w,int x,int y,int z){return null;}
 }
 static int checks;static void check(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
 public static void main(String[] args)throws Exception{
  Grid w=B11605Fixture.initialize();w.loaded=true;
  for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)for(int y=79;y<=80;y++)w.setBlock(x,y,z,Blocks.water,0,3);
  check(OpenWaterFishing116.openWater(w,0,80,0),"5x5 two water layers and two air layers qualify");
  w.setBlock(2,80,2,Blocks.water,1,3);check(!OpenWaterFishing116.openWater(w,0,80,0),"flowing edge rejects treasure");w.setBlock(2,80,2,Blocks.water,0,3);
  w.setBlock(0,81,0,Blocks.waterlily,0,3);check(OpenWaterFishing116.openWater(w,0,80,0),"lily counts as above water");
  w.setBlock(2,82,2,Blocks.stone,0,3);check(!OpenWaterFishing116.openWater(w,0,80,0),"low ceiling rejected");w.setBlockToAir(2,82,2);
  w.setBlock(1,80,1,Blocks.air,0,3);check(!OpenWaterFishing116.openWater(w,0,80,0),"mixed air water layer rejected");w.setBlock(1,80,1,Blocks.water,0,3);
  w.loaded=false;check(!OpenWaterFishing116.openWater(w,0,80,0),"no chunk loading");w.loaded=true;
  EntityFishHook hook=new EntityFishHook(w);hook.setPosition(.5,80,.5);OpenWaterFishing116.tick(hook,0,0);check(hook.getEntityData().getBoolean("HTGT6OpenFishingWater"),"idle resets eligibility");
  w.setBlock(2,80,2,Blocks.stone,0,3);OpenWaterFishing116.tick(hook,0,10);w.setBlock(2,80,2,Blocks.water,0,3);OpenWaterFishing116.tick(hook,10,0);check(!hook.getEntityData().getBoolean("HTGT6OpenFishingWater"),"bad water during approach invalidates whole bite");
  OpenWaterFishing116.tick(hook,0,0);OpenWaterFishing116.tick(hook,0,10);check(hook.getEntityData().getBoolean("HTGT6OpenFishingWater"),"next fishing cycle can recover");
  for(int luck=0;luck<=3;luck++)for(int i=0;i<1000;i++)check(OpenWaterFishing116.category(i/1000F,luck,false)!=1,"closed pool never returns treasure");
  int[] counts=new int[3];for(int i=0;i<100;i++)counts[OpenWaterFishing116.category((i+.5F)/100,0,true)]++;check(counts[0]==10&&counts[1]==5&&counts[2]==85,"original unenchanted loot weights");
  w.setBlock(0,80,0,new WetPlant(),0,3);OpenWaterFishing116.tick(hook,0,0);for(int i=0;i<25;i++)OpenWaterFishing116.tick(hook,10,0);check(hook.getEntityData().getBoolean("HTGT6OpenFishingWater"),"waterlogged noncolliding plant never accumulates dry ticks");
  System.out.println("Open water fishing 116: "+checks+" checks passed");
 }
}
