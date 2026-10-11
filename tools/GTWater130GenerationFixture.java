import com.mod.htgt6.asm.HTGT6Transformer;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;
import java.util.*;
public class GTWater130GenerationFixture {
 static int checks;
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 public static void main(String[] args)throws Exception {
  com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.initialize();
  Class<?> gt=Class.forName("gregapi.data.CS$BlocksGT");
  java.lang.reflect.Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)uf.get(null);
  Block ocean=(Block)unsafe.allocateInstance(net.minecraftforge.fluids.BlockFluidClassic.class);
  Block river=(Block)unsafe.allocateInstance(net.minecraftforge.fluids.BlockFluidClassic.class);
  Block swamp=(Block)unsafe.allocateInstance(net.minecraftforge.fluids.BlockFluidClassic.class);
  gt.getField("Ocean").set(null,ocean);gt.getField("River").set(null,river);gt.getField("Swamp").set(null,swamp);
  Block[] b=new Block[65536];Arrays.fill(b,Blocks.water);BiomeGenBase[] biomes=new BiomeGenBase[256];Arrays.fill(biomes,BiomeGenBase.ocean);
  biomes[1]=BiomeGenBase.river;biomes[2]=BiomeGenBase.swampland;biomes[3]=BiomeGenBase.plains;
  b[20]=Blocks.stone;b[21]=Blocks.air;
  HTGT6Transformer.generateGTWaterInBiomeArray(b,biomes);
  for(int col=0;col<256;col++)for(int y=0;y<256;y++){
   Block expected=y>0&&y<=62?(col==1?river:col==2?swamp:col==3?Blocks.water:ocean):Blocks.water;
   if(col==0&&y==20)expected=Blocks.stone;if(col==0&&y==21)expected=Blocks.air;
   check(b[col*256+y]==expected,"biome/height/content boundary");
  }
  Block[] before=b.clone();HTGT6Transformer.generateGTWaterInBiomeArray(b,biomes);check(Arrays.equals(before,b),"second pass stable");
  System.out.println("PASS GTWater130 generation: "+checks+" cells/checks; marker blocks isolate GT registry from SRG runtime");
 }
}
