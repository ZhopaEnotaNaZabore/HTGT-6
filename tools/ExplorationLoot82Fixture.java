import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.ExplorationLoot120;
import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Items;
import java.lang.reflect.Method;
import java.util.Random;
public final class ExplorationLoot82Fixture {
 public static void main(String[] args)throws Exception{
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
  for(EntityLiving mob:new EntityLiving[]{new EntityEvoker112(world),new EntityVindicator112(world),new EntityLlama112(world)}){
   mob.captureDrops=true;Method drop=mob.getClass().getDeclaredMethod("dropFewItems",boolean.class,int.class);drop.setAccessible(true);
   for(int i=0;i<100;i++){
    mob.capturedDrops.clear();drop.invoke(mob,false,3);
    for(net.minecraft.entity.item.EntityItem item:mob.capturedDrops)check(item.getEntityItem().getItem()!=Items.emerald,"emerald without player kill");
    if(mob instanceof EntityEvoker112)check(mob.capturedDrops.size()==1&&mob.capturedDrops.get(0).getEntityItem().getItem()==ExplorationContent112.totemOfUndying,"evoker totem");
    if(mob instanceof EntityVindicator112)check(mob.capturedDrops.isEmpty(),"extra axe drop");
   }
  }
  Random rng=new Random(82);
  for(int maximum=1;maximum<=2;maximum++)for(int looting=0;looting<=3;looting++){
   int min=99,max=-1,total=0;
   for(int i=0;i<10000;i++){int n=ExplorationLoot120.count(rng,maximum,looting);min=Math.min(min,n);max=Math.max(max,n);total+=n;}
   check(min==0&&max==maximum+looting,"count range");check(Math.abs(total/10000.0-(maximum+looting)/2.0)<.05,"count distribution");
  }
  EntityEvoker112 evoker=new EntityEvoker112(world);
  Method fang=EntityEvoker112.class.getDeclaredMethod("fang",double.class,double.class,double.class,double.class,float.class,int.class);fang.setAccessible(true);
  world.setBlock(0,0,0,net.minecraft.init.Blocks.water,0,0);world.lastSpawn=null;fang.invoke(evoker,.5,.5,1D,2D,0F,0);check(world.lastSpawn==null,"fang on water");
  world.setBlock(0,0,0,net.minecraft.init.Blocks.stone,0,0);world.setBlock(0,1,0,net.minecraft.init.Blocks.stone_slab,0,0);fang.invoke(evoker,.5,.5,1D,2D,0F,0);
  check(world.lastSpawn instanceof EntityEvokerFangs112&&world.lastSpawn.posY==1.5,"fang slab collision height");
  System.out.println("PASS: player-only emeralds, guaranteed totem, no duplicate axe, loot ranges/distributions and fang surface height");
 }
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
