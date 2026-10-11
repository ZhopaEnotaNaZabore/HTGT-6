import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityParrot120;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import java.lang.reflect.*;
import java.util.*;

public final class Parrot99Fixture {
 public static void main(String[] args)throws Exception {
  World world=BountifulMechanicsFixture120.initialize();
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  Field pw=net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj");pw.setAccessible(true);pw.set(world.provider,world);
  Field accesses=World.class.getDeclaredField("worldAccesses");accesses.setAccessible(true);accesses.set(world,new ArrayList());
  Constructor<?> ctor=Class.forName("LlamaInteraction77Fixture$Player").getDeclaredConstructor(World.class);ctor.setAccessible(true);
  EntityPlayer player=(EntityPlayer)ctor.newInstance(world);
  Field players=World.class.getDeclaredField("playerEntities");players.setAccessible(true);players.set(world,new ArrayList(Arrays.asList(player)));
  Field random=Entity.class.getDeclaredField("rand");random.setAccessible(true);
  Item beetroot=new Item();Method raw=Item.itemRegistry.getClass().getDeclaredMethod("addObjectRaw",int.class,String.class,Object.class);raw.setAccessible(true);raw.invoke(Item.itemRegistry,30000,"Minecraft_1.20.2:beetroot_seeds",beetroot);
  for(Item seed:new Item[]{Items.wheat_seeds,Items.melon_seeds,Items.pumpkin_seeds,beetroot}) {
   EntityParrot120 parrot=new EntityParrot120(world);
   random.set(parrot,new Random(){public int nextInt(int bound){return 0;}});
   player.inventory.setInventorySlotContents(0,new ItemStack(seed,2));
   world.isRemote=true;check(parrot.interact(player)&&!parrot.isTamed()&&player.getHeldItem().stackSize==2,"client seed no mutation: "+seed+" registered="+Item.itemRegistry.getNameForObject(seed)+" found="+Item.itemRegistry.getObject("Minecraft_1.20.2:beetroot_seeds")+" count="+player.getHeldItem().stackSize);
   world.isRemote=false;check(parrot.interact(player)&&parrot.isTamed()&&player.getHeldItem().stackSize==1,"server seed tames/consumes");
   check(parrot.func_152113_b().equals(player.getUniqueID().toString()),"owner saved");
  }
  EntityParrot120 parrot=new EntityParrot120(world);
  random.set(parrot,new Random(){public int nextInt(int bound){return bound-1;}});
  player.inventory.setInventorySlotContents(0,new ItemStack(Items.wheat_seeds,2));
  player.capabilities.isCreativeMode=true;parrot.interact(player);
  check(!parrot.isTamed()&&player.getHeldItem().stackSize==2,"creative failed attempt retains seed");player.capabilities.isCreativeMode=false;
  player.inventory.setInventorySlotContents(0,new ItemStack(Items.cookie,2));world.isRemote=true;
  parrot.interact(player);check(player.getHeldItem().stackSize==2&&parrot.getHealth()==6,"client cookie no mutation");world.isRemote=false;
  parrot.setTamed(true);parrot.func_152115_b(player.getUniqueID().toString());player.inventory.setInventorySlotContents(0,null);
  Field sitting=EntityAISit.class.getDeclaredField("isSitting");sitting.setAccessible(true);
  parrot.onGround=false;parrot.interact(player);check(!sitting.getBoolean(parrot.func_70907_r()),"flying cannot be ordered to sit");
  parrot.onGround=true;check(parrot.interact(player)&&sitting.getBoolean(parrot.func_70907_r()),"ground owner can order sit");
  parrot.attackEntityFrom(net.minecraft.util.DamageSource.generic,0);
  check(!sitting.getBoolean(parrot.func_70907_r()),"hurt clears sit order");
  parrot.setGrowingAge(-24000);check(!parrot.isChild(),"no baby form");
  parrot.func_146082_f(player);EntityParrot120 mate=new EntityParrot120(world);mate.func_146082_f(player);
  check(!parrot.canMateWith(mate)&&parrot.createChild(mate)==null,"cannot breed even forced love");
  for(int variant=0;variant<5;variant++) {parrot.setVariant(variant);NBTTagCompound tag=new NBTTagCompound();parrot.writeEntityToNBT(tag);EntityParrot120 restored=new EntityParrot120(world);restored.readEntityFromNBT(tag);check(restored.getVariant()==variant&&restored.isTamed(),"variant/taming NBT");}
  System.out.println("PASS parrot: four seed types, client authority, creative failure, owner, ground-only sitting, hurt stand, adult-only/no breeding, five variants NBT");
 }
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}



