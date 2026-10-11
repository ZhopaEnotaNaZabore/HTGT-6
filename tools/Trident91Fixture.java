import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityThrownTrident;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemTrident;
import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.TridentItemRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import java.lang.reflect.*;
public final class Trident91Fixture {
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  Field pw=net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj");pw.setAccessible(true);pw.set(w.provider,w);
  Field accesses=World.class.getDeclaredField("worldAccesses");accesses.setAccessible(true);accesses.set(w,new java.util.ArrayList());
  Constructor<?> pc=Class.forName("LlamaInteraction77Fixture$Player").getDeclaredConstructor(World.class);pc.setAccessible(true);EntityPlayer p=(EntityPlayer)pc.newInstance(w);
  cpw.mods.fml.common.registry.GameRegistry.registerItem(OceanContent.trident,"fixture_trident91");
  ItemTrident item=(ItemTrident)OceanContent.trident;ItemStack stack=new ItemStack(item);p.inventory.mainInventory[0]=stack;
  w.rejectSpawns=true;item.onPlayerStoppedUsing(stack,w,p,71990);check(stack.stackSize==1&&stack.getItemDamage()==0,"rejected spawn preserves item");
  w.rejectSpawns=false;item.onPlayerStoppedUsing(stack,w,p,71991);check(stack.stackSize==1,"undercharged throw");
  w.isRemote=true;item.onPlayerStoppedUsing(stack,w,p,71990);check(stack.stackSize==1,"client does not consume");w.isRemote=false;
  item.onPlayerStoppedUsing(stack,w,p,71990);check(stack.stackSize==0&&w.lastSpawn instanceof EntityThrownTrident,"accepted spawn consumes once");
  ItemStack enchanted=new ItemStack(item,1,42);enchanted.addEnchantment(OceanContent.loyalty,3);enchanted.setStackDisplayName("Return me");
  EntityThrownTrident server=new EntityThrownTrident(w,p,enchanted,true);NBTTagCompound state=new NBTTagCompound();server.writeEntityToNBT(state);state.setBoolean("Embedded",true);state.setInteger("EmbeddedTicks",2);server.readEntityFromNBT(state);
  io.netty.buffer.ByteBuf buffer=io.netty.buffer.Unpooled.buffer();server.writeSpawnData(buffer);EntityThrownTrident client=new EntityThrownTrident(w);client.readSpawnData(buffer);buffer.release();
  NBTTagCompound received=new NBTTagCompound();client.writeEntityToNBT(received);
  check(client.isEnchanted()&&received.getBoolean("Embedded")&&received.getInteger("EmbeddedTicks")==2,"late-spawn state and enchantment");
  ItemStack recovered=ItemStack.loadItemStackFromNBT(received.getCompoundTag("TridentItem"));check(recovered.getItemDamage()==42&&recovered.hasDisplayName(),"spawn item NBT");
  for(int i=0;i<36;i++)p.inventory.mainInventory[i]=new ItemStack(net.minecraft.init.Items.diamond,64);
  Method pickup=EntityThrownTrident.class.getDeclaredMethod("returnToOwner",net.minecraft.entity.EntityLivingBase.class);pickup.setAccessible(true);pickup.invoke(server,p);check(!server.isDead,"full inventory retains returning trident");
  ItemStack using=new ItemStack(item);p.setItemInUse(using,72000);check(TridentItemRenderer.isThrowing(p,using)&&!TridentItemRenderer.isThrowing(p,new ItemStack(item)),"pose belongs to active stack only");
  System.out.println("PASS trident: spawn rejection, threshold, server consumption, enchanted late spawn, NBT, full inventory return and pose selection");
 }
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
