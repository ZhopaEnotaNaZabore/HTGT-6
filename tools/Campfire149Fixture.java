package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.*;import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.CampfireRecipes149;
import net.minecraft.world.*;import net.minecraft.block.Block;import net.minecraft.entity.*;import net.minecraft.item.*;import net.minecraft.init.*;import net.minecraft.nbt.*;import net.minecraft.tileentity.TileEntity;import java.lang.reflect.*;import java.util.*;
public final class Campfire149Fixture {
 static int n;static void check(boolean b,String s){if(!b)throw new AssertionError(s);n++;}
 public static void main(String[]args)throws Exception{
  BountifulMechanicsFixture120.Grid base=BountifulMechanicsFixture120.initialize();
  sun.misc.Unsafe u=(sun.misc.Unsafe)Dolphin112Fixture.field(sun.misc.Unsafe.class,"theUnsafe").get(null);
  TestWorld w=(TestWorld)u.allocateInstance(TestWorld.class);for(Field f:World.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,f.get(base));}
  Dolphin112Fixture.field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));Dolphin112Fixture.field(World.class,"worldAccesses").set(w,new ArrayList());w.provider.worldObj=w;
  BlockCampfire149 b=(BlockCampfire149)VillagePillageContent.campfire;w.block=b;w.meta=4;w.spawns=new ArrayList();
  cpw.mods.fml.common.registry.GameRegistry.registerTileEntity(TileEntityCampfire149.class,"fixture_campfire149");
  TileEntityCampfire149 tile=new TileEntityCampfire149();tile.setWorldObj(w);tile.yCoord=10;w.tile=tile;
  ItemStack beef=new ItemStack(Items.beef,5);for(int i=0;i<4;i++)check(tile.insert(beef,false),"fills one slot");check(beef.stackSize==1&&!tile.insert(beef,false),"full blocks fifth item without consuming");
  for(int i=0;i<100;i++)tile.updateEntity();for(int i=0;i<4;i++)check(tile.progress(i)==100,"independent concurrent progress");
  NBTTagCompound tag=new NBTTagCompound();tile.writeToNBT(tag);TileEntityCampfire149 saved=new TileEntityCampfire149();saved.readFromNBT(tag);check(saved.progress(3)==100&&saved.item(3).stackSize==1,"NBT slots/progress preserved");
  w.meta=0;tile.updateEntity();check(tile.progress(0)==98,"unlit cools by2");w.meta=4;
  w.rejectSpawns=true;for(int i=98;i<600;i++)tile.updateEntity();check(tile.item(0)!=null&&tile.progress(0)==600,"rejected output keeps input");w.rejectSpawns=false;tile.updateEntity();check(tile.item(0)==null&&w.spawns.size()==4,"four outputs accepted once");
  for(Object obj:w.spawns)check(((net.minecraft.entity.item.EntityItem)obj).getEntityItem().getItem()==Items.cooked_beef,"correct cooked output");
  ItemStack creative=new ItemStack(Items.potato,5);check(tile.insert(creative,true)&&creative.stackSize==5,"creative no consume");
  check(CampfireRecipes149.result(new ItemStack(Blocks.iron_ore))==null,"no furnace ore proxy");check(CampfireRecipes149.result(new ItemStack(Items.fish,1,2))==null&&CampfireRecipes149.result(new ItemStack(Items.fish,1,3))==null,"tropical and puffer not cookable");
  check(CampfireRecipes149.result(new ItemStack(Items.fish,1,1)).getItemDamage()==1,"salmon result metadata");
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(w);p.inventory.mainInventory[0]=new ItemStack(Items.water_bucket);
  check(b.onBlockActivated(w,0,10,0,p,1,0,0,0)&&w.meta==8&&p.getHeldItem().getItem()==Items.bucket,"bucket extinguishes and retains water");
  check(!b.setLit(w,0,10,0,true),"cannot light underwater");
  check(b.onBlockActivated(w,0,10,0,p,1,0,0,0)&&w.meta==0&&p.getHeldItem().getItem()==Items.water_bucket,"bucket extracts retained water");
  p.inventory.mainInventory[0]=new ItemStack(Items.flint_and_steel);check(b.onBlockActivated(w,0,10,0,p,1,0,0,0)&&w.meta==4&&p.getHeldItem().getItemDamage()==1,"flintsteel lights and wears");
  p.inventory.mainInventory[0]=new ItemStack(Items.iron_shovel);check(b.onBlockActivated(w,0,10,0,p,1,0,0,0)&&w.meta==0&&p.getHeldItem().getItemDamage()==1,"shovel extinguishes and wears");
  w.rejectWrites=true;p.inventory.mainInventory[0]=new ItemStack(Items.fire_charge,2);check(!b.onBlockActivated(w,0,10,0,p,1,0,0,0)&&p.getHeldItem().stackSize==2,"denied ignition no consume");w.rejectWrites=false;
  check(b.getItemDropped(4,new Random(),3)==Items.coal&&b.damageDropped(4)==1&&b.quantityDropped(new Random())==2,"two charcoal drop");check(b.getLightValue(w,0,10,0)==0,"unlit no light");w.meta=4;check(b.getLightValue(w,0,10,0)==15,"lit light15");
  check(w.lights>=3,"lit transitions update world lighting");
  w.isRemote=true;int before=tile.progress(0);tile.updateEntity();check(tile.progress(0)==before&&!tile.insert(new ItemStack(Items.beef),false),"client no inventory mutation");
  System.out.println("PASS campfire149: "+n);
 }
 public static class TestWorld extends World {
  Block block;int meta;TileEntity tile;boolean rejectSpawns,rejectWrites,unloaded,aboveWater;int reads,lights;List spawns;
  protected TestWorld(){super(null,"fixture",(WorldProvider)null,(WorldSettings)null,null);}
  public Block getBlock(int x,int y,int z){reads++;return y==10?block:y==11&&aboveWater?Blocks.water:Blocks.air;}public int getBlockMetadata(int x,int y,int z){return meta;}
  public boolean setBlockMetadataWithNotify(int x,int y,int z,int m,int flags){if(rejectWrites)return false;meta=m;return true;}
  public TileEntity getTileEntity(int x,int y,int z){return tile;}
  public boolean spawnEntityInWorld(Entity e){if(rejectSpawns)return false;spawns.add(e);return true;}
  public boolean updateLightByType(EnumSkyBlock type,int x,int y,int z){lights++;return true;}
  public void markBlockForUpdate(int x,int y,int z){}public void markTileEntityChunkModified(int x,int y,int z,TileEntity t){}
  public void func_147453_f(int x,int y,int z,Block b){}public boolean blockExists(int x,int y,int z){return !unloaded;}
  public boolean checkChunksExist(int a,int b,int c,int d,int e,int f){return !unloaded;}
  public Entity getEntityByID(int id){return null;}protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}protected int func_152379_p(){return 0;}
 }
}
