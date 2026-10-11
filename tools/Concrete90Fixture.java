package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120ConcretePowder;
import net.minecraft.block.BlockFalling;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.util.ForgeDirection;
public final class Concrete90Fixture {
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  BlockBackport120ConcretePowder powder=new BlockBackport120ConcretePowder();powder.setHardened(Blocks.stained_hardened_clay);
  for(int color=0;color<16;color++) {
   for(int side=0;side<6;side++) {
    w.cells.clear();w.setBlock(0,10,0,powder,color,3);ForgeDirection d=ForgeDirection.getOrientation(side);
    w.setBlock(d.offsetX,10+d.offsetY,d.offsetZ,Blocks.water,0,3);powder.onNeighborBlockChange(w,0,10,0,Blocks.water);
    check(w.getBlock(0,10,0)==(side==0?powder:Blocks.stained_hardened_clay)&&w.getBlockMetadata(0,10,0)==color,"contact direction and color");
   }
   w.cells.clear();w.setBlock(0,10,0,powder,color,3);w.setBlock(1,10,0,Blocks.water,0,3);w.isRemote=true;
   powder.onBlockAdded(w,0,10,0);check(w.getBlock(0,10,0)==powder,"client does not mutate");w.isRemote=false;
   boolean previous=BlockFalling.fallInstantly;BlockFalling.fallInstantly=true;
   try {
    w.cells.clear();w.setBlock(0,10,0,powder,color,3);w.setBlock(0,4,0,Blocks.stone,0,3);
    powder.updateTick(w,0,10,0,w.rand);
    check(w.getBlock(0,5,0)==powder&&w.getBlockMetadata(0,5,0)==color&&w.getBlock(0,10,0)==Blocks.air,"dry instant fall retains color");
    w.cells.clear();w.setBlock(0,10,0,powder,color,3);w.setBlock(0,7,0,Blocks.flowing_water,0,3);
    powder.updateTick(w,0,10,0,w.rand);
    check(w.getBlock(0,7,0)==Blocks.stained_hardened_clay&&w.getBlockMetadata(0,7,0)==color&&w.getBlock(0,10,0)==Blocks.air,"instant fall solidifies at first water");
   } finally {BlockFalling.fallInstantly=previous;}
  }
  w.cells.clear();w.setBlock(0,10,0,powder,3,3);w.setBlock(1,10,0,Blocks.lava,0,3);powder.onBlockAdded(w,0,10,0);
  check(w.getBlock(0,10,0)==powder,"lava does not solidify");
  System.out.println("PASS concrete: 16 colors x 6 contact directions, client authority, dry instant fall, first-water solidification, lava exclusion");
 }
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
