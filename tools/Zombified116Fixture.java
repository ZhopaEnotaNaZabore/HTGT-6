import com.mod.htgt6.minecraft1202.Module.Entity.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import net.minecraft.entity.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
public final class Zombified116Fixture {
 static int checks;static void check(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
 public static void main(String[] args)throws Exception{
  Grid w=B11605Fixture.initialize();w.loaded=true;w.provider.dimensionId=-1;
  EntityZombifiedPiglin116 z=new EntityZombifiedPiglin116(w),ally=new EntityZombifiedPiglin116(w);z.setPosition(0,80,0);ally.setPosition(2,80,0);w.loadedEntityList.add(z);w.loadedEntityList.add(ally);
  B11605Fixture.Player enemy=new B11605Fixture.Player(w),bystander=new B11605Fixture.Player(w);enemy.setPosition(4,80,0);bystander.setPosition(1,80,0);w.playerEntities.add(enemy);w.playerEntities.add(bystander);w.loadedEntityList.add(enemy);w.loadedEntityList.add(bystander);
  check(z.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getBaseValue()==.23&&z.isImmuneToFire(),"modern base speed and fire immunity");
  check(z.selectAngryPlayer()==null,"neutral before provocation");z.angerAt(enemy,true);check(z.isAngryAt(enemy)&&!z.isAngryAt(bystander)&&ally.getAttackTarget()==enemy,"group remembers attacker instead of nearest player");
  check(z.selectAngryPlayer()==enemy,"nearer innocent player ignored");int remaining=z.getAngerTime();for(int i=0;i<100;i++)z.tickAnger();check(z.getAngerTime()==remaining,"anger pauses while fighting player");check(Math.abs(z.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue()-.28)<1E-7,"anger speed boost only .05 and does not stack");
  NBTTagCompound n=new NBTTagCompound();z.writeEntityToNBT(n);EntityZombifiedPiglin116 saved=new EntityZombifiedPiglin116(w);saved.readEntityFromNBT(n);check(saved.getAngryAt().equals(enemy.getUniqueID())&&saved.getAngerTime()==remaining,"anger identity survives save");
  saved.tickAnger();check(saved.getAngerTime()==remaining-1,"unengaged anger counts down");
  enemy.setHealth(0);z.tickAnger();check(z.getAngerTime()==0&&z.getAttackTarget()==null,"default forgiveness on player death");
  enemy.setHealth(20);w.rules.addGameRule("forgiveDeadPlayers","false");z.angerAt(enemy,false);enemy.setHealth(0);z.tickAnger();check(z.getAngerTime()>0&&z.getAttackTarget()==null,"disabled forgiveness retains identity");
  enemy.setHealth(20);w.rules.addGameRule("universalAnger","true");z.angerAt(enemy,true);check(z.isAngryAt(bystander)&&z.selectAngryPlayer()==bystander,"universal anger targets eligible nearest player");
  bystander.capabilities.isCreativeMode=true;check(!z.isAngryAt(bystander)&&z.selectAngryPlayer()==enemy,"creative never targeted");
  z.clearAnger();z.tickAnger();check(z.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue()==.23,"calm removes modifier");
  z.setFire(8);check(!z.isBurning(),"sunlight cannot ignite zombie piglin");
  z.captureDrops=true;java.lang.reflect.Method drop=EntityZombifiedPiglin116.class.getDeclaredMethod("dropFewItems",boolean.class,int.class);drop.setAccessible(true);for(int i=0;i<100;i++){z.capturedDrops.clear();drop.invoke(z,true,3);for(Object value:z.capturedDrops){ItemStack s=((net.minecraft.entity.item.EntityItem)value).getEntityItem();check(s.stackSize>0&&s.stackSize<=4,"no empty loot entities");}}
  System.out.println("Zombified piglin 116: "+checks+" checks passed");
 }
}
