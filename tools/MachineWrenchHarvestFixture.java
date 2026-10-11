import com.mod.htgt6.common.harvest.MachineWrenchHarvest;
import com.mod.htgt6.common.crusher.CrusherOreImportFixture;
import com.mod.htgt6.common.crusher.BlockIndustrialCrusherCasing;
import com.mod.htgt6.common.block.greg.technological.hulls.blockHullStatbase;
import gregapi.item.multiitem.MultiItemTool;
import gregapi.data.MT;
import gregtech.items.tools.machine.GT_Tool_Wrench;
import gregtech.items.tools.electric.GT_Tool_Wrench_LV;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import cpw.mods.fml.common.registry.GameRegistry;
public class MachineWrenchHarvestFixture {
 static void check(boolean b,String message){if(!b)throw new AssertionError(message);}
 static class Player extends EntityPlayer {
  Player(World w){super(w,new com.mojang.authlib.GameProfile(new java.util.UUID(97,98),"WrenchFixture"));}
  public void addChatMessage(net.minecraft.util.IChatComponent c){}
  public boolean canCommandSenderUseCommand(int i,String s){return false;}
  public net.minecraft.util.ChunkCoordinates getPlayerCoordinates(){return new net.minecraft.util.ChunkCoordinates(0,0,0);}
 }
 public static void main(String[] args)throws Exception {
  World world=CrusherOreImportFixture.initializeGT6();
  java.lang.reflect.Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);
  info.set(world,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
  java.lang.reflect.Field provider=net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj");provider.setAccessible(true);provider.set(world.provider,world);
  Block[] blocks={new blockHullStatbase(),new BlockIndustrialCrusherCasing(false),new BlockIndustrialCrusherCasing(true)};
  for(int i=0;i<blocks.length;i++)GameRegistry.registerBlock(blocks[i],"wrench_fixture_"+i);
  MachineWrenchHarvest.install();
  MachineWrenchHarvest handler=new MachineWrenchHarvest();
  MultiItemTool tool=new MultiItemTool("gregtech","wrench_fixture_tool");
  tool.mToolStats.put((short)0,new GT_Tool_Wrench());
  tool.mToolStats.put((short)2,new GT_Tool_Wrench_LV(0));
  ItemStack wrench=tool.getToolWithStats(0,MT.Steel,MT.Steel);
  ItemStack electric=tool.getToolWithStats(2,MT.Steel,MT.Steel);
  Player player=new Player(world);
  for(Block block:blocks){
   check("wrench".equals(block.getHarvestTool(0)),"wrench metadata set");
   check(MachineWrenchHarvest.isUsableWrench(wrench,block,0),"GT wrench accepted");
   check(MachineWrenchHarvest.isUsableWrench(electric,block,0),"GT electric wrench stats accepted");
   for(ItemStack invalid:new ItemStack[]{null,new ItemStack(Items.diamond_pickaxe),new ItemStack(Items.stick),new ItemStack(tool,1,1)}){
    player.inventory.mainInventory[0]=invalid;
    PlayerEvent.BreakSpeed speed=new PlayerEvent.BreakSpeed(player,block,0,10,0,10,0);handler.onBreakSpeed(speed);check(speed.newSpeed==0,"wrong tool zero speed");
    PlayerEvent.HarvestCheck harvest=new PlayerEvent.HarvestCheck(player,block,true);handler.onHarvestCheck(harvest);check(!harvest.success,"wrong tool cannot harvest");
    BlockEvent.BreakEvent event=new BlockEvent.BreakEvent(0,10,0,world,block,0,player){@Override public boolean isCancelable(){return true;}};handler.onBreak(event);check(event.isCanceled(),"server rejects wrong-tool break");
   }
   player.inventory.mainInventory[0]=wrench;
   PlayerEvent.HarvestCheck harvest=new PlayerEvent.HarvestCheck(player,block,false);handler.onHarvestCheck(harvest);check(harvest.success,"wrench harvest allowed");
   BlockEvent.BreakEvent event=new BlockEvent.BreakEvent(0,10,0,world,block,0,player){@Override public boolean isCancelable(){return true;}};handler.onBreak(event);check(!event.isCanceled(),"server accepts wrench");
   player.capabilities.isCreativeMode=true;player.inventory.mainInventory[0]=null;
   event=new BlockEvent.BreakEvent(0,10,0,world,block,0,player){@Override public boolean isCancelable(){return true;}};handler.onBreak(event);check(!event.isCanceled(),"creative retained");player.capabilities.isCreativeMode=false;
  }
  check(!MachineWrenchHarvest.requiresWrench(Blocks.stone),"ordinary blocks unaffected");
  System.out.println("PASS: GT wrench types, invalid/broken tools, client speed, server cancellation, harvest check, creative, ordinary blocks");
 }
}
