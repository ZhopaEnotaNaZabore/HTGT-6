package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockKelpPlant;
import net.minecraft.init.Blocks;
import java.util.Random;
public final class Kelp126Fixture {
 static int checks;
 static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);checks++;}
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  BlockKelpPlant stem=(BlockKelpPlant)OceanContent.kelpPlant;
  w.setBlock(0,9,0,Blocks.stone,0,2);
  w.setBlock(0,10,0,stem,0,2);w.setBlock(0,11,0,Blocks.water,0,2);
  stem.onNeighborBlockChange(w,0,10,0,Blocks.water);
  check(w.getBlock(0,10,0)==OceanContent.kelp,"cut stem becomes head");
  for(final double roll:new double[]{.139999,.14,.99}) {
   w.setBlock(0,10,0,OceanContent.kelp,8,2);w.setBlock(0,11,0,Blocks.water,0,2);
   OceanContent.kelp.updateTick(w,0,10,0,new Random(){public double nextDouble(){return roll;}});
   check((w.getBlock(0,11,0)==OceanContent.kelp)==(roll<.14),"growth boundary");
  }
  w.setBlock(0,10,0,stem,0,2);w.setBlock(0,11,0,OceanContent.kelp,15,2);w.setBlock(0,12,0,Blocks.water,0,2);
  check(stem.func_149851_a(w,0,10,0,false),"bone meal reaches mature head");
  stem.func_149853_b(w,new Random(1),0,10,0);
  check(w.getBlock(0,12,0)==OceanContent.kelp && w.getBlock(0,11,0)==stem,"stem fertilizes head");
  w.setBlock(0,13,0,Blocks.stone,0,2);
  check(!stem.func_149851_a(w,0,10,0,false),"blocked head rejects bone meal");
  w.setBlock(0,13,0,Blocks.water,0,2);w.rejectWrites=true;
  OceanContent.kelp.func_149853_b(w,new Random(),0,12,0);
  check(w.getBlock(0,12,0)==OceanContent.kelp,"failed write preserves head");w.rejectWrites=false;
  w.isRemote=true;stem.func_149853_b(w,new Random(),0,10,0);
  check(w.getBlock(0,13,0)==Blocks.water,"client does not grow");w.isRemote=false;
  System.out.println("PASS kelp126: "+checks);
 }
}
