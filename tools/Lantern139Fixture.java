package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockVillagePillageLantern;
import net.minecraft.init.Blocks;import net.minecraft.util.AxisAlignedBB;import java.util.*;
public class Lantern139Fixture {
 static int checks;static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 public static void main(String[] args)throws Exception{
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();w.provider.worldObj=w;w.loaded=true;
  BlockVillagePillageLantern b=new BlockVillagePillageLantern();
  check(!b.canPlaceBlockAt(w,0,10,0),"cannot float");
  w.setBlock(0,9,0,Blocks.stone,0,2);check(b.canPlaceBlockAt(w,0,10,0),"floor support");check(b.onBlockPlaced(w,0,10,0,1,0,0,0,15)==0,"floor resets item metadata");
  w.setBlock(0,11,0,Blocks.stone,0,2);check(b.onBlockPlaced(w,0,10,0,0,0,0,0,0)==1,"ceiling preferred on bottom click");
  w.setBlock(0,9,0,Blocks.air,0,2);check(b.canPlaceBlockAt(w,0,10,0),"ceiling alone");check(b.onBlockPlaced(w,0,10,0,2,0,0,0,0)==1,"horizontal click finds ceiling");
  for(int meta=0;meta<2;meta++){
   w.setBlock(0,10,0,b,meta,2);List<AxisAlignedBB> boxes=new ArrayList<AxisAlignedBB>();b.addCollisionBoxesToList(w,0,10,0,AxisAlignedBB.getBoundingBox(-1,9,-1,2,12,2),boxes,null);
   check(boxes.size()==2,"two modern collision parts");check(boxes.get(0).minY==10+meta/16D,"attachment offset");check(boxes.get(1).maxY==10+(9+meta)/16D,"cap height");
   check(b.damageDropped(meta)==0,"one inventory variant");
  }
  w.setBlock(0,11,0,Blocks.air,0,2);w.isRemote=true;b.onNeighborBlockChange(w,0,10,0,Blocks.air);check(w.getBlock(0,10,0)==b,"client does not remove");
  w.isRemote=false;w.rejectWrites=true;b.onNeighborBlockChange(w,0,10,0,Blocks.air);check(w.getBlock(0,10,0)==b,"failed removal retains block");
  w.rejectWrites=false;b.onNeighborBlockChange(w,0,10,0,Blocks.air);check(w.getBlock(0,10,0)==Blocks.air,"lost support removes");
  w.setBlock(0,9,0,Blocks.fence,0,2);check(b.canPlaceBlockAt(w,0,10,0),"fence center support");
  w.setBlock(0,9,0,Blocks.air,0,2);w.setBlock(0,11,0,Blocks.fence,0,2);check(b.canPlaceBlockAt(w,0,10,0)&&b.onBlockPlaced(w,0,10,0,0,0,0,0,0)==1,"hanging fence center support");
  w.loaded=false;check(!b.canPlaceBlockAt(w,0,10,0),"unloaded support rejected");
  System.out.println("PASS lantern139: "+checks+" checks");
 }
}
