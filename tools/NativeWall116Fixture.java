import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NativeWall116;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBlackstoneWall116;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.BlackstoneShapes116;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import java.util.*;
public final class NativeWall116Fixture {
 static class ForeignWall extends net.minecraft.block.BlockWall {ForeignWall(){super(Blocks.cobblestone);}}
 static int checks;static void check(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
 public static void main(String[] args)throws Exception{
  Grid w=B11605Fixture.initialize();w.loaded=true;int[] dx={0,1,0,-1},dz={-1,0,1,0};
  for(int variant=0;variant<2;variant++)for(int mask=0;mask<16;mask++)for(int cover=0;cover<2;cover++){
   for(int x=-1;x<=1;x++)for(int y=79;y<=82;y++)for(int z=-1;z<=1;z++)w.setBlockToAir(x,y,z);
   w.setBlock(0,80,0,Blocks.cobblestone_wall,variant,3);for(int i=0;i<4;i++)if((mask&(1<<i))!=0)w.setBlock(dx[i],80,dz[i],Blocks.stone,0,3);if(cover>0)w.setBlock(0,81,0,Blocks.stone,0,3);
   BlockBlackstoneWall116.State state=NativeWall116.state(w,0,80,0);boolean n=(mask&1)!=0,e=(mask&2)!=0,s=(mask&4)!=0,west=(mask&8)!=0;
   check(state.up==(mask==0||n!=s||e!=west||cover>0&&!(n&&s||e&&west)),"native post state");
   for(int i=0;i<4;i++)check(state.side[i]==((mask&(1<<i))==0?0:cover>0?2:1),"native modern side height");
   List<AxisAlignedBB> boxes=new ArrayList<AxisAlignedBB>();Blocks.cobblestone_wall.addCollisionBoxesToList(w,0,80,0,AxisAlignedBB.getBoundingBox(-1,79,-1,2,82,2),boxes,null);check(!boxes.isEmpty(),"actual native callback supplies collision");for(AxisAlignedBB box:boxes)check(box.maxY==81.5,"native collision stays 1.5 high");
   check(Blocks.cobblestone_wall.damageDropped(variant)==variant,"mossy metadata preserved");
  }
  for(int x=-1;x<=1;x++)for(int y=79;y<=82;y++)for(int z=-1;z<=1;z++)w.setBlockToAir(x,y,z);
  w.setBlock(0,80,0,Blocks.cobblestone_wall,0,3);w.setBlock(0,80,-1,Blocks.stone,0,3);w.setBlock(0,80,1,Blocks.stone,0,3);
  check(Blocks.cobblestone_wall.collisionRayTrace(w,0,80,0,Vec3.createVectorHelper(.2,80.5,-1),Vec3.createVectorHelper(.2,80.5,2))==null,"ray misses empty corner rather than legacy envelope");
  for(int y=81;y<240;y++){w.setBlock(0,y,0,Blocks.cobblestone_wall,0,3);w.setBlock(0,y,-1,Blocks.stone,0,3);w.setBlock(0,y,1,Blocks.stone,0,3);}
  w.setBlock(0,240,0,Blocks.stone,0,3);check(!NativeWall116.state(w,0,80,0).up&&NativeWall116.state(w,0,80,0).side[0]==2,"tall stacked wall propagates roof without recursive call stack");
  com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150 fluid=(com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150)Blocks.cobblestone_wall;
  for(int variant=0;variant<2;variant++){
   w.setBlock(0,80,0,Blocks.cobblestone_wall,variant,3);check(fluid.setWaterlogged(w,0,80,0,true)&&w.getBlockMetadata(0,80,0)==(variant|2),"separate water bit preserves mossy type");
   check(Blocks.cobblestone_wall.damageDropped(variant|2)==variant&&Blocks.cobblestone_wall.getDamageValue(w,0,80,0)==variant,"wet native wall pick and drops preserve variant");
   check(Blocks.cobblestone_wall.getLightOpacity(w,0,80,0)==1&&Blocks.cobblestone_wall.getExplosionResistance(null,w,0,80,0,0,0,0)>=100,"water provides light and blast behavior");
   check(fluid.setWaterlogged(w,0,80,0,false)&&w.getBlockMetadata(0,80,0)==variant,"draining restores original variant");
   fluid.setWaterlogged(w,0,80,0,true);check(Blocks.cobblestone_wall.removedByPlayer(w,null,0,80,0,true)&&w.getBlock(0,80,0)==Blocks.water,"breaking native wall preserves source water");
   check(Blocks.cobblestone_wall.onBlockPlaced(w,0,80,0,1,.5F,.5F,.5F,variant)==(variant|2),"wall placement in source water captures fluid");
  }
  w.setBlock(0,80,0,Blocks.cobblestone_wall,3,3);w.setBlock(1,80,0,Blocks.flowing_lava,7,3);Blocks.flowing_lava.onNeighborBlockChange(w,1,80,0,Blocks.cobblestone_wall);check(w.getBlock(1,80,0)==Blocks.cobblestone,"native waterlogged wall solidifies adjacent lava");
  ForeignWall foreign=new ForeignWall();w.setBlock(0,80,0,foreign,3,3);check(foreign.getRenderType()==32&&foreign.damageDropped(3)==3,"foreign wall retains legacy render and metadata");check(!((com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150)foreign).isWaterlogged(w,0,80,0),"foreign metadata never interpreted as water");check(foreign.getRenderBlockPass()==0,"foreign render pass inherited unchanged");
  System.out.println("Native wall 116: "+checks+" checks passed");
 }
}
