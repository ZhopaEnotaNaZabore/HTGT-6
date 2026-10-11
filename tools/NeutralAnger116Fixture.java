import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NeutralAnger116;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.nbt.NBTTagCompound;
public final class NeutralAnger116Fixture {
 static int checks;static void check(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
 public static void main(String[] args)throws Exception{
  Grid w=B11605Fixture.initialize();w.loaded=true;NeutralAnger116 events=new NeutralAnger116();net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(events);events.world(new net.minecraftforge.event.world.WorldEvent.Load(w));
  check(!w.rules.getGameRuleBooleanValue("universalAnger")&&w.rules.getGameRuleBooleanValue("forgiveDeadPlayers"),"modern gamerule defaults");
  B11605Fixture.Player attacker=new B11605Fixture.Player(w),near=new B11605Fixture.Player(w);attacker.setPosition(8,80,0);near.setPosition(1,80,0);w.playerEntities.add(attacker);w.playerEntities.add(near);
  for(EntityCreature mob:new EntityCreature[]{new EntityWolf(w),new EntityEnderman(w),new EntityIronGolem(w)}){
   mob.setPosition(0,80,0);NeutralAnger116.hurt(mob,attacker);NeutralAnger116.tick(mob);check(NeutralAnger116.target(mob)==attacker,"remembers only attacking player");int time=NeutralAnger116.time(mob);check(time>=400&&time<=780,"20..39 second anger");for(int i=0;i<100;i++)NeutralAnger116.tick(mob);check(NeutralAnger116.time(mob)==time,"anger pauses while fighting player");
   NBTTagCompound saved=new NBTTagCompound();mob.writeToNBT(saved);check(saved.getCompoundTag("ForgeData").getInteger("HTGT6NeutralAngerTime")==time,"normal entity NBT persists anger memory");
   NeutralAnger116.clear(mob);w.rules.setOrCreateGameRule("universalAnger","true");NeutralAnger116.hurt(mob,attacker);NeutralAnger116.tick(mob);check(NeutralAnger116.target(mob)==near,"universal anger can select a closer uninvolved player");
   w.loadedEntityList.add(mob);events.died(new net.minecraftforge.event.entity.living.LivingDeathEvent(near,net.minecraft.util.DamageSource.generic));check(NeutralAnger116.time(mob)==0&&NeutralAnger116.target(mob)==null,"forgiveDeadPlayers clears the selected player's anger");w.loadedEntityList.remove(mob);w.rules.setOrCreateGameRule("universalAnger","false");
  }
  EntityWolf pet=new EntityWolf(w);pet.setTamed(true);NeutralAnger116.hurt(pet,attacker);check(NeutralAnger116.time(pet)==0,"tamed wolf owner behavior untouched");EntityIronGolem built=new EntityIronGolem(w);built.setPlayerCreated(true);NeutralAnger116.hurt(built,attacker);check(NeutralAnger116.time(built)==0,"player-built golem stays peaceful to players");
  EntityWolf wolf=new EntityWolf(w);wolf.setPosition(0,80,0);w.loadedEntityList.add(wolf);w.rules.setOrCreateGameRule("forgiveDeadPlayers","false");NeutralAnger116.hurt(wolf,attacker);NeutralAnger116.tick(wolf);events.died(new net.minecraftforge.event.entity.living.LivingDeathEvent(attacker,net.minecraft.util.DamageSource.generic));check(NeutralAnger116.time(wolf)>0,"disabled forgiveness preserves anger");NeutralAnger116.clear(wolf);w.playerEntities.clear();NeutralAnger116.hurt(wolf,attacker);for(int i=0;i<800;i++)NeutralAnger116.tick(wolf);check(NeutralAnger116.time(wolf)==0&&!wolf.isAngry(),"unattended anger expires and wolf calms down");
  System.out.println("Neutral anger 116: "+checks+" checks passed");
 }
}
