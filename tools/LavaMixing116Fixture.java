import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.LavaMixing116;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import net.minecraft.init.Blocks;
import net.minecraft.block.Block;
public final class LavaMixing116Fixture {
 static int checks;static void check(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
 static final int[][] SIDES={{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
 static void clear(Grid w){for(int x=-1;x<=1;x++)for(int y=79;y<=81;y++)for(int z=-1;z<=1;z++)w.setBlockToAir(x,y,z);}
 public static void main(String[] args)throws Exception{
  Grid w=B11605Fixture.initialize();w.loaded=true;
  for(Block lava:new Block[]{Blocks.lava,Blocks.flowing_lava})for(int meta=0;meta<16;meta++)for(int[] side:SIDES){
   clear(w);w.setBlock(0,80,0,lava,meta,3);w.setBlock(side[0],80+side[1],side[2],Blocks.water,0,3);
   lava.onNeighborBlockChange(w,0,80,0,Blocks.water);check(w.getBlock(0,80,0)==(meta==0?Blocks.obsidian:Blocks.cobblestone),"native lava callback converts every non-source level");
  }
  for(int[] side:SIDES){clear(w);w.setBlock(0,80,0,Blocks.lava,0,3);w.setBlock(0,79,0,EtFVanillaGapContent120.soulSoil,0,3);w.setBlock(side[0],80+side[1],side[2],Backport120Content.blueIce,0,3);
   Blocks.lava.onBlockAdded(w,0,80,0);check(w.getBlock(0,80,0)==EtFVanillaGapContent120.basalt,"basalt generated from native lava placement");}
  clear(w);w.setBlock(0,80,0,Blocks.lava,0,3);w.setBlock(0,79,0,Blocks.soul_sand,0,3);w.setBlock(1,80,0,Backport120Content.blueIce,0,3);LavaMixing116.mix(Blocks.lava,w,0,80,0);check(w.getBlock(0,80,0)==Blocks.lava,"soul sand is not soul soil");
  clear(w);w.setBlock(0,80,0,Blocks.lava,0,3);w.setBlock(0,79,0,Blocks.water,0,3);LavaMixing116.mix(Blocks.lava,w,0,80,0);check(w.getBlock(0,80,0)==Blocks.lava,"water below belongs to downward flow path");
  w.setBlock(1,80,0,Blocks.water,0,3);w.loaded=false;LavaMixing116.mix(Blocks.lava,w,0,80,0);check(w.getBlock(0,80,0)==Blocks.lava,"unloaded neighbor never read");w.loaded=true;
  clear(w);w.setBlock(0,80,0,Blocks.water,0,3);w.setBlock(0,79,0,EtFVanillaGapContent120.soulSoil,0,3);w.setBlock(1,80,0,Backport120Content.blueIce,0,3);LavaMixing116.mix(Blocks.water,w,0,80,0);check(w.getBlock(0,80,0)==Blocks.water,"water not converted to basalt");
  System.out.println("Lava mixing 116: "+checks+" checks passed");
 }
}
