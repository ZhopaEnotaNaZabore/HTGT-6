import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityParrot120;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import java.lang.reflect.*;
import java.util.*;

public final class ParrotCookie100Fixture {
 public static void main(String[] args)throws Exception {
  World world=BountifulMechanicsFixture120.initialize();
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  Field pw=net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj");pw.setAccessible(true);pw.set(world.provider,world);
  Field accesses=World.class.getDeclaredField("worldAccesses");accesses.setAccessible(true);accesses.set(world,new ArrayList());
  Field scoreboard=World.class.getDeclaredField("worldScoreboard");scoreboard.setAccessible(true);scoreboard.set(world,new net.minecraft.scoreboard.Scoreboard());
  Constructor<?> ctor=Class.forName("LlamaInteraction77Fixture$Player").getDeclaredConstructor(World.class);ctor.setAccessible(true);
  EntityPlayer player=(EntityPlayer)ctor.newInstance(world);
  Field invulnerable=Entity.class.getDeclaredField("invulnerable");invulnerable.setAccessible(true);
  for(boolean creative:new boolean[]{false,true})for(boolean immune:new boolean[]{false,true}) {
   EntityParrot120 parrot=new EntityParrot120(world);invulnerable.setBoolean(parrot,immune);
   player.capabilities.isCreativeMode=creative;player.inventory.setInventorySlotContents(0,new ItemStack(Items.cookie,2));
   check(parrot.interact(player),"cookie accepted");
   check(player.getHeldItem().stackSize==(creative?2:1),"cookie consumption");
   check(parrot.getActivePotionEffect(Potion.poison).getDuration()==900,"poison 900 ticks");
   check((parrot.getHealth()<=0)==(creative||!immune),"lethal damage respects source-aware invulnerability");
   if(creative||!immune)check(parrot.getAITarget()==player,"damage attributed to feeding player");
   check(parrot.isEntityInvulnerable()==immune,"invulnerability restored after damage");
  }
  System.out.println("PASS cookie: survival/creative x normal/invulnerable, consumption, poison duration, lethal result, player attribution, restored invulnerability");
 }
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
