package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.CrossbowItemRenderer139;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandCrossbowUse120;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
import com.mod.htgt6.client.inventory112.Inventory112ClientHandler;
import net.minecraft.item.*;import net.minecraft.init.Items;
public final class CrossbowLeft147Fixture {
 static int checks;static void check(boolean ok,String m){if(!ok)throw new AssertionError(m);checks++;}
 public static void main(String[]args)throws Exception{
  Crossbow140Fixture.main(args);
  java.util.zip.ZipFile zip=new java.util.zip.ZipFile("tools/decompile1202/reference/client.jar");
  com.google.gson.JsonObject json=new com.google.gson.JsonParser().parse(new java.io.InputStreamReader(zip.getInputStream(zip.getEntry("assets/minecraft/models/item/crossbow.json")),"UTF-8")).getAsJsonObject().getAsJsonObject("display");
  for(boolean first:new boolean[]{false,true}){float[] actual=CrossbowItemRenderer139.leftDisplay(first);com.google.gson.JsonObject ref=json.getAsJsonObject(first?"firstperson_lefthand":"thirdperson_lefthand");for(int i=0;i<3;i++){float translation=ref.getAsJsonArray("translation").get(i).getAsFloat()/16*(i==0?-1:1);check(Math.abs(actual[i]-translation)<.00001,"official mirrored translation");float rotation=ref.getAsJsonArray("rotation").get(i).getAsFloat()*(i==0?1:-1);check(Math.abs(actual[3+i]-rotation)<.00001,"official mirrored rotation");}check(Math.abs(actual[6]-ref.getAsJsonArray("scale").get(0).getAsFloat())<.00001,"official model scale");}zip.close();
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();w.provider.worldObj=w;java.lang.reflect.Field info=net.minecraft.world.World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(w,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));Swimming115Fixture.Player p=new Swimming115Fixture.Player(w);p.inventory=new Crossbow140Fixture.Inventory(p);ItemStack bow=new ItemStack(new ItemCrossbow());OffhandHooks.setOffhand(p,bow);p.inventory.mainInventory[1]=new ItemStack(Items.arrow);
  check(!Inventory112ClientHandler.isUsingItemForMovement(p),"idle offhand does not slow movement");check(OffhandCrossbowUse120.start(p),"charge starts");check(Inventory112ClientHandler.isUsingItemForMovement(p)&&!p.isUsingItem(),"offhand uses existing vanilla slowdown branch once");OffhandCrossbowUse120.release(p);check(!Inventory112ClientHandler.isUsingItemForMovement(p),"release removes slowdown");
  System.out.println("PASS crossbow left147: "+checks);
 }
}
