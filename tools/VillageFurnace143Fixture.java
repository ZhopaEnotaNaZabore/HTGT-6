package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageFurnaceRecipes143;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.*;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;
import net.minecraft.world.World;import net.minecraft.init.*;import net.minecraft.item.*;import net.minecraft.item.crafting.*;import net.minecraft.tileentity.*;import net.minecraft.nbt.NBTTagCompound;import java.lang.reflect.*;import java.util.*;
public class VillageFurnace143Fixture{
 static int checks;static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 public static class FurnaceWorld extends World{
  int metadata;
  public net.minecraft.entity.Entity getEntityByID(int id){return null;}
  private FurnaceWorld(){super(null,"fixture",(net.minecraft.world.WorldProvider)null,(net.minecraft.world.WorldSettings)null,null);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}protected int func_152379_p(){return 0;}
  public net.minecraft.block.Block getBlock(int x,int y,int z){return Blocks.furnace;}
  public int getBlockMetadata(int x,int y,int z){return metadata;}
  public boolean setBlockMetadataWithNotify(int x,int y,int z,int meta,int flags){metadata=meta;return true;}
  public void markTileEntityChunkModified(int x,int y,int z,TileEntity tile){}
  public void func_147453_f(int x,int y,int z,net.minecraft.block.Block block){}
 }
 public static void main(String[] args)throws Exception{
  Field side=cpw.mods.fml.relauncher.FMLRelaunchLog.class.getDeclaredField("side");side.setAccessible(true);side.set(null,cpw.mods.fml.relauncher.Side.CLIENT);
  BountifulMechanicsFixture120.Grid grid=BountifulMechanicsFixture120.initialize();
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe u=(sun.misc.Unsafe)uf.get(null);
  cpw.mods.fml.common.registry.GameRegistry.registerItem(ExplorationContent112.ironNugget,"furnace143_nugget");
  // Existing pack-specific recipe must retain ownership.
  FurnaceRecipes.smelting().func_151394_a(new ItemStack(Items.golden_hoe),new ItemStack(Items.diamond),.5F);
  VillagePillageContent.register();int size=FurnaceRecipes.smelting().getSmeltingList().size();VillageFurnaceRecipes143.registerRecycling();check(size==FurnaceRecipes.smelting().getSmeltingList().size(),"idempotent recipe registration");
  String[] iron={"iron_pickaxe","iron_shovel","iron_axe","iron_hoe","iron_sword","iron_helmet","iron_chestplate","iron_leggings","iron_boots","iron_horse_armor","chainmail_helmet","chainmail_chestplate","chainmail_leggings","chainmail_boots"};
  String[] gold={"golden_pickaxe","golden_shovel","golden_axe","golden_sword","golden_helmet","golden_chestplate","golden_leggings","golden_boots","golden_horse_armor"};
  for(String[] names:new String[][]{iron,gold})for(String name:names){Item item=(Item)Items.class.getField(name).get(null);for(int damage:new int[]{0,7,100}){
   ItemStack input=new ItemStack(item,1,damage);ItemStack output=FurnaceRecipes.smelting().getSmeltingResult(input);check(VillageFurnaceRecipes143.isBlastable(input),"metal equipment accepted");check(output!=null&&output.stackSize==1&&output.getItem()==(names==iron?ExplorationContent112.ironNugget:Items.gold_nugget),"damage-independent official recycling");check(Math.abs(FurnaceRecipes.smelting().func_151398_b(output)-.1F)<.0001,"recycling XP");}}
  check(FurnaceRecipes.smelting().getSmeltingResult(new ItemStack(Items.golden_hoe)).getItem()==Items.diamond,"custom recipe preserved");
  check(FurnaceRecipes.smelting().getSmeltingResult(new ItemStack(Items.golden_hoe,1,7))==null,"no wildcard over custom damage recipe");
  check(!VillageFurnaceRecipes143.isBlastable(new ItemStack(Items.diamond_sword)),"diamond excluded");check(!VillageFurnaceRecipes143.isBlastable(new ItemStack(Blocks.sand)),"sand excluded");
  FurnaceWorld w=(FurnaceWorld)u.allocateInstance(FurnaceWorld.class);
  for(boolean smoker:new boolean[]{false,true}){
   TileEntityVillageFurnace120 tile=(TileEntityVillageFurnace120)new BlockVillageFurnace120("fixture","fixture",smoker).createNewTileEntity(w,0);tile.setWorldObj(w);
   tile.setInventorySlotContents(0,new ItemStack(smoker?Items.porkchop:Item.getItemFromBlock(Blocks.iron_ore),16));tile.setInventorySlotContents(1,new ItemStack(Items.coal));
   tile.updateEntity();check(tile.furnaceBurnTime==800&&tile.currentItemBurnTime==800,"coal burn time halved");check(tile.getStackInSlot(1)==null,"one fuel consumed");
   for(int tick=1;tick<800;tick++)tile.updateEntity();check(tile.getStackInSlot(2).stackSize==8,"same eight items per coal at double speed");check(tile.getStackInSlot(0).stackSize==8,"eight remaining inputs");
  }
  TileEntityVillageFurnace120 blast=(TileEntityVillageFurnace120)new BlockVillageFurnace120("fixture","fixture",false).createNewTileEntity(w,0);blast.setWorldObj(w);blast.setInventorySlotContents(0,new ItemStack(Items.iron_helmet,1,100));blast.setInventorySlotContents(1,new ItemStack(Items.coal));for(int i=0;i<100;i++)blast.updateEntity();check(blast.getStackInSlot(2).getItem()==ExplorationContent112.ironNugget,"real tick path recycles damaged armor");
  TileEntityVillageFurnace120 restored=(TileEntityVillageFurnace120)new BlockVillageFurnace120("fixture","fixture",false).createNewTileEntity(w,0);restored.setWorldObj(w);restored.setInventorySlotContents(0,new ItemStack(Blocks.iron_ore,2));restored.setInventorySlotContents(1,new ItemStack(Items.coal,2));restored.updateEntity();NBTTagCompound save=new NBTTagCompound();restored.writeToNBT(save);TileEntityVillageFurnace120 loaded=new TileEntityVillageFurnace120();loaded.readFromNBT(save);check(loaded.currentItemBurnTime==800&&loaded.furnaceBurnTime==800,"reload fuel progress uses fast furnace duration");
  TileEntityVillageFurnace120 blocked=(TileEntityVillageFurnace120)new BlockVillageFurnace120("fixture","fixture",false).createNewTileEntity(w,0);blocked.setWorldObj(w);blocked.setInventorySlotContents(0,new ItemStack(Items.iron_helmet));blocked.setInventorySlotContents(1,new ItemStack(Items.coal));ItemStack tagged=new ItemStack(ExplorationContent112.ironNugget);tagged.setTagCompound(new NBTTagCompound());tagged.getTagCompound().setString("Owner","fixture");blocked.setInventorySlotContents(2,tagged);blocked.updateEntity();check(blocked.getStackInSlot(1).stackSize==1&&blocked.furnaceBurnTime==0,"incompatible output NBT cannot consume fuel");
  ItemStack[] barrel={new ItemStack(Blocks.planks),new ItemStack(Blocks.wooden_slab),new ItemStack(Blocks.planks),new ItemStack(Blocks.planks),null,new ItemStack(Blocks.planks),new ItemStack(Blocks.planks),new ItemStack(Blocks.wooden_slab),new ItemStack(Blocks.planks)};
  IRecipe recipe=VillageRecipes132Fixture.find(Item.getItemFromBlock(VillagePillageContent.barrel));check(recipe.matches(VillageRecipes132Fixture.grid(barrel),grid),"reference barrel grid");
  barrel[1]=null;barrel[7]=null;barrel[4]=new ItemStack(Blocks.wooden_slab);check(!recipe.matches(VillageRecipes132Fixture.grid(barrel),grid),"old one-slab barrel rejected");
  System.out.println("PASS village furnace143: "+checks+" checks");
 }
}
