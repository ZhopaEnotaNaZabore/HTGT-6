package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockMarinePlant;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemBlockMarinePlant;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import java.util.*;
public final class Marine127Fixture {
 static int checks;
 static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);checks++;}
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  BlockMarinePlant p=(BlockMarinePlant)OceanContent.marinePlant;
  for(int cut:new int[]{1,2}) {
   w.cells.clear();w.setBlock(0,9,0,Blocks.stone,0,2);w.setBlock(0,10,0,p,1,2);w.setBlock(0,11,0,p,2,2);
   check(p.onSheared(null,w,0,9+cut,0,0).get(0).stackSize==2,"either half yields two seagrass");
   w.setBlock(0,9+cut,0,Blocks.air,0,2);p.breakBlock(w,0,9+cut,0,p,cut);
   check(w.getBlock(0,10,0)==Blocks.water && w.getBlock(0,11,0)==Blocks.water,"both halves removed with water restored");
  }
  w.cells.clear();w.setBlock(0,9,0,Blocks.stone,0,2);w.setBlock(0,10,0,p,1,2);w.setBlock(0,11,0,Blocks.stone,0,2);
  p.onNeighborBlockChange(w,0,10,0,Blocks.stone);check(w.getBlock(0,10,0)==Blocks.water,"orphan bottom removed");
  check(w.getBlock(0,11,0)==Blocks.stone,"replacement preserved");
  w.setBlock(0,10,0,p,0,2);w.setBlock(0,11,0,Blocks.water,0,2);w.rejectWrites=true;
  p.func_149853_b(w,new Random(),0,10,0);w.rejectWrites=false;
  check(w.getBlockMetadata(0,10,0)==0 && w.getBlock(0,11,0)==Blocks.water,"denied growth preserves short grass");
  p.func_149853_b(w,new Random(),0,10,0);
  check(w.getBlockMetadata(0,10,0)==1 && w.getBlockMetadata(0,11,0)==2,"grass grows paired");
  w.setBlock(0,10,0,p,8,2);p.func_149853_b(w,new Random(),0,10,0);
  check(w.getBlockMetadata(0,10,0)==8,"stone cannot multiply pickle");
  w.setBlock(0,9,0,OceanContent.coralBlock,8,2);p.func_149853_b(w,new Random(),0,10,0);
  check(w.getBlockMetadata(0,10,0)==8,"dead coral cannot multiply pickle");
  w.cells.clear();
  for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++){w.setBlock(x,9,z,OceanContent.coralBlock,0,2);w.setBlock(x,10,z,Blocks.water,0,2);}
  w.setBlock(0,10,0,p,8,2);
  p.func_149853_b(w,new Random(){public int nextInt(int bound){return 0;}},0,10,0);
  for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)check((w.getBlock(x,10,z)==p)==(Math.abs(x)+Math.abs(z)<=2),"diamond spread boundary");
  check(w.getBlockMetadata(0,10,0)==11,"source cluster full");
  ItemBlockMarinePlant item=new ItemBlockMarinePlant(p);
  for(int m=0;m<=11;m++)check(item.getMetadata(m)==(m<=2?0:Math.min(m,8)),"legacy inventory states normalized");
  List<ItemStack> stacks=new ArrayList<ItemStack>();p.getSubBlocks(item,null,stacks);check(stacks.size()==7,"one creative pickle variant");
  for(int m=8;m<=11;m++){check(p.quantityDropped(m,0,new Random())==m-7,"cluster drop count");check(p.damageDropped(m)==8,"drop normalized");}
  w.setBlock(0,10,0,p,8,2);w.isRemote=true;p.func_149853_b(w,new Random(),0,10,0);check(w.getBlockMetadata(0,10,0)==8,"client no mutation");
  System.out.println("PASS marine127: "+checks);
 }
}
