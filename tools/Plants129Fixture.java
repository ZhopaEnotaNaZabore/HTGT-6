package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.*;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
public class Plants129Fixture {
 static int checks; static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  BlockMarinePlant p=(BlockMarinePlant)OceanContent.marinePlant;
  Block fake=new Block(Material.water){};
  check(!BlockKelp.isOpenWater(fake),"water-material content cannot be overwritten");
  check(!BlockKelp.isOpenWater(p)&&!BlockKelp.isOpenWater(OceanContent.kelp),"plants are not free water");
  for(int m=0;m<16;m++){w.setBlock(0,10,0,Blocks.flowing_water,m,2);check(BlockKelp.isFullWater(w,0,10,0)==(m==0||m>=8),"full liquid level");}
  for(int m=0;m<12;m++) {
   w.setBlock(0,10,0,p,m,2);p.setBlockBoundsBasedOnState(w,0,10,0);
   if(m>=8){check(p.getBlockBoundsMaxY()==(m==11?7D:6D)/16D,"pickle height");check(p.getBlockBoundsMinX()==(m==8?6D:m==9?3D:2D)/16D,"pickle width");}
   if(m<=2){check(p.getBlockBoundsMinX()==.125D,"grass width");check(p.getBlockBoundsMaxY()==(m==0?.75D:1D),"grass height");}
  }
  w.setBlock(0,9,0,Blocks.stone,0,2);check(BlockMarinePlant.supportsSeagrass(w,0,10,0),"stone supports grass");
  w.setBlock(0,9,0,OceanContent.kelp,8,2);check(!BlockMarinePlant.supportsSeagrass(w,0,10,0),"kelp cannot support grass");
  w.setBlock(0,9,0,com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent.magmaBlock,0,2);check(!BlockMarinePlant.supportsSeagrass(w,0,10,0),"magma rejected");
  System.out.println("PASS plants129: "+checks);
 }
}
