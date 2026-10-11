import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityVex112;
import com.mod.htgt6.minecraft1202.Module.Events.Exploration.ExplorationEventHandler112;
import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;
import net.minecraft.world.World;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityVindicator112;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
public final class ExplorationItems82Fixture {
 public static void main(String[]args)throws Exception{
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
  java.lang.reflect.Field accesses=World.class.getDeclaredField("worldAccesses");accesses.setAccessible(true);accesses.set(world,new java.util.ArrayList());
  EntityVindicator112 mob=new EntityVindicator112(world);ExplorationEventHandler112 handler=new ExplorationEventHandler112();
  mob.setCurrentItemOrArmor(0,new ItemStack(ExplorationContent112.totemOfUndying));mob.setHealth(0);
  LivingDeathEvent voidDeath=new Death(mob,DamageSource.outOfWorld);handler.onLivingDeath(voidDeath);
  check(!voidDeath.isCanceled()&&mob.getHeldItem()!=null,"void consumed totem");
  LivingDeathEvent death=new Death(mob,DamageSource.generic);handler.onLivingDeath(death);
  check(death.isCanceled()&&mob.getHealth()==1&&mob.getHeldItem()==null,"nonplayer totem activation");
  check(mob.isPotionActive(net.minecraft.potion.Potion.regeneration)&&mob.isPotionActive(net.minecraft.potion.Potion.fireResistance),"totem effects");
  check(!ExplorationEventHandler112.bypassesTotem(DamageSource.magic),"ordinary armor bypass must not bypass totem");
  EntityVex112 vex=new EntityVex112(world);ItemStack weapon=new ItemStack(Items.diamond_sword);weapon.setStackDisplayName("Saved weapon");vex.setCurrentItemOrArmor(0,weapon);
  NBTTagCompound saved=new NBTTagCompound();vex.writeEntityToNBT(saved);EntityVex112 restored=new EntityVex112(world);restored.readEntityFromNBT(saved);
  check(restored.getHeldItem().getItem()==Items.diamond_sword&&"Saved weapon".equals(restored.getHeldItem().getDisplayName()),"Vex overwrote equipment NBT");
  System.out.println("PASS: non-player totem, void exclusion, effects, ordinary damage and Vex equipment NBT");
 }
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 static final class Death extends LivingDeathEvent {
  Death(net.minecraft.entity.EntityLivingBase entity,DamageSource source){super(entity,source);}
  @Override public boolean isCancelable(){return true;} // supplied by FML's event transformer in-game
 }
}
