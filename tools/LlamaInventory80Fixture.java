import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.ContainerLlama120;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import java.lang.reflect.*;
public final class LlamaInventory80Fixture {
 public static void main(String[] args)throws Exception{
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
  Field accesses=World.class.getDeclaredField("worldAccesses");accesses.setAccessible(true);accesses.set(world,new java.util.ArrayList());
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  Field pw=net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj");pw.setAccessible(true);pw.set(world.provider,world);
  Constructor<?> constructor=Class.forName("LlamaInteraction77Fixture$Player").getDeclaredConstructor(World.class);constructor.setAccessible(true);
  EntityPlayer player=(EntityPlayer)constructor.newInstance(world);player.setPosition(0,0,0);
  for(int strength=1;strength<=5;strength++){
   EntityLlama112 llama=new EntityLlama112(world);llama.setPosition(0,0,0);
   NBTTagCompound tag=new NBTTagCompound();llama.writeEntityToNBT(tag);tag.setBoolean("Tame",true);tag.setInteger("Strength",strength);llama.readEntityFromNBT(tag);
   player.ridingEntity=llama;llama.riddenByEntity=player;
   check(com.mod.htgt6.minecraft1202.Module.Network.Exploration.PacketOpenLlamaInventory120.eligibleMount(player)==llama,"mounted inventory eligibility");
   llama.setGrowingAge(-100);
   check(com.mod.htgt6.minecraft1202.Module.Network.Exploration.PacketOpenLlamaInventory120.eligibleMount(player)==null,"reject child mount");
   llama.setGrowingAge(0);llama.riddenByEntity=null;
   check(com.mod.htgt6.minecraft1202.Module.Network.Exploration.PacketOpenLlamaInventory120.eligibleMount(player)==null,"reject stale mount");
   player.ridingEntity=null;
   player.inventory.setInventorySlotContents(0,new ItemStack(Blocks.chest,2));
   llama.interact(player);check(llama.hasChest()&&player.getHeldItem().stackSize==1,"attach chest once");
   ContainerLlama120 menu=new ContainerLlama120(player,llama,llama.getStorageColumns());int slots=1+3*strength;
   check(((Slot)menu.inventorySlots.get(0)).xDisplayPosition==8&&((Slot)menu.inventorySlots.get(0)).yDisplayPosition==36,"carpet slot matches GUI");
   check(menu.inventorySlots.size()==slots+36,"capacity");
   check(!((Slot)menu.inventorySlots.get(0)).isItemValid(new ItemStack(Items.diamond)),"decor validation");
   player.inventory.setInventorySlotContents(0,new ItemStack(Blocks.carpet,16,14));
   menu.transferStackInSlot(player,slots+27);
   check(llama.getLlamaInventory().getStackInSlot(1).stackSize==1&&player.getHeldItem().stackSize==15,"single carpet transfer");
   check(llama.getCarpetColor()==14,"decor metadata");
   EntityLlama112 client=new EntityLlama112(world);
   net.minecraft.network.PacketBuffer packet=new net.minecraft.network.PacketBuffer(io.netty.buffer.Unpooled.buffer());
   try{net.minecraft.entity.DataWatcher.writeWatchedListToPacketBuffer(llama.getDataWatcher().getAllWatched(),packet);
       client.getDataWatcher().updateWatchedObjectsFromList(net.minecraft.entity.DataWatcher.readWatchedListFromPacketBuffer(packet));}
   finally{packet.release();}
   check(client.hasChest()&&client.getCarpetColor()==14&&client.getStorageColumns()==strength&&client.isTamed(),"client equipment metadata");
   player.inventory.setInventorySlotContents(0,new ItemStack(Items.diamond,64));
   menu.transferStackInSlot(player,slots+27);
   check(player.getHeldItem()==null&&llama.getLlamaInventory().getStackInSlot(2).stackSize==64,"cargo shift click");
   NBTTagCompound saved=new NBTTagCompound();llama.writeEntityToNBT(saved);
   EntityLlama112 restored=new EntityLlama112(world);restored.readEntityFromNBT(saved);
   check(restored.getStorageColumns()==strength&&restored.getCarpetColor()==14&&restored.getLlamaInventory().getStackInSlot(2).stackSize==64,"NBT cargo/decor");
   menu.transferStackInSlot(player,0);check(llama.getCarpetColor()==-1,"remove decor");
   llama.getLlamaInventory().setInventorySlotContents(1,new ItemStack(Blocks.carpet,1,3));
   player.inventory.setInventorySlotContents(0,new ItemStack(Blocks.carpet,4,3));
   menu.transferStackInSlot(player,slots+27);
   check(llama.getLlamaInventory().getStackInSlot(1).stackSize==1&&player.getHeldItem()==null,"occupied decor routes spare carpets to cargo");
   int carpets=0;for(int c=1;c<17;c++){ItemStack item=llama.getLlamaInventory().getStackInSlot(c);if(item!=null&&item.getItem()==net.minecraft.item.Item.getItemFromBlock(Blocks.carpet))carpets+=item.stackSize;}
   check(carpets==5,"carpet count conserved");
   for(int c=0;c<36;c++)player.inventory.setInventorySlotContents(c,new ItemStack(Blocks.stone,64));
   check(menu.transferStackInSlot(player,0)==null&&llama.getLlamaInventory().getStackInSlot(1).stackSize==1,"full inventory preserves decor");
   player.setHealth(0);check(!menu.canInteractWith(player),"dead player rejected");player.setHealth(20);
   World previousWorld=player.worldObj;player.worldObj=null;check(!menu.canInteractWith(player),"different world rejected");player.worldObj=previousWorld;
   llama.setGrowingAge(-1);check(!menu.canInteractWith(player),"child GUI rejected");llama.setGrowingAge(0);
   NBTTagCompound current=new NBTTagCompound();llama.writeEntityToNBT(current);
   current.setBoolean("Tame",false);llama.readEntityFromNBT(current);check(!menu.canInteractWith(player),"untamed GUI rejected");
   current.setBoolean("Tame",true);current.setInteger("Strength",strength%5+1);llama.readEntityFromNBT(current);check(!menu.canInteractWith(player),"changed capacity invalidates GUI");
   current.setInteger("Strength",strength);llama.readEntityFromNBT(current);
   player.setPosition(7.99,0,0);check(menu.canInteractWith(player),"inside distance allowed");
   player.setPosition(8,0,0);check(!menu.canInteractWith(player),"exact distance boundary rejected");
   player.setPosition(10,0,0);check(!menu.canInteractWith(player),"distance guard");player.setPosition(0,0,0);
   Method drop=EntityLlama112.class.getDeclaredMethod("dropEquipment",boolean.class,int.class);drop.setAccessible(true);drop.invoke(restored,false,0);
   check(!restored.hasChest()&&restored.getLlamaInventory().getStackInSlot(1)==null&&restored.getLlamaInventory().getStackInSlot(2)==null,"drop clears equipment");
   for(int i=0;i<36;i++)player.inventory.setInventorySlotContents(i,null);
  }
  com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderLlamaSpit112 renderer=new com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderLlamaSpit112();
  Field main=renderer.getClass().getDeclaredField("main");main.setAccessible(true);
  net.minecraft.client.model.ModelRenderer part=(net.minecraft.client.model.ModelRenderer)main.get(renderer);
  check(part.cubeList.size()==7,"seven spit cubes");
  java.awt.image.BufferedImage texture=javax.imageio.ImageIO.read(new java.io.File("src/main/resources/assets/htgt6/textures/entity/exploration_update/llama/spit.png"));
  check(texture.getWidth()==64&&texture.getHeight()==32,"spit texture size");
  System.out.println("PASS: strengths 1..5, chest, NBT, occupied decor routing, full inventory, live player/same world, child/tame/capacity guards, exact 8-block boundary, drops, GUI slot, spit geometry/texture");
 }
 private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
