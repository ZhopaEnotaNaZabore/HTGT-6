package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntitySeaTurtle;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import net.minecraft.world.World;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import java.lang.reflect.*;
import java.util.*;
public class Aquatic132Fixture {
 static int checks;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static void field(Class<?> c,String n,Object obj,Object value)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);f.set(obj,value);}
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  field(World.class,"worldInfo",w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  field(World.class,"theProfiler",w,new net.minecraft.profiler.Profiler());
  field(World.class,"worldAccesses",w,new ArrayList());w.provider.worldObj=w;
  EntitySeaTurtle t=new EntitySeaTurtle(w);t.setPosition(.5,10,.5);t.setHome(0,10,0);
  Method egg=EntitySeaTurtle.class.getDeclaredMethod("setHasEgg",boolean.class);egg.setAccessible(true);egg.invoke(t,true);
  Method lay=EntitySeaTurtle.class.getDeclaredMethod("updateEggLaying");lay.setAccessible(true);
  w.setBlock(0,9,0,Blocks.sand,0,2);w.rejectWrites=true;
  for(int i=0;i<200;i++)lay.invoke(t);
  check(t.hasEgg()&&w.getBlock(0,10,0)==Blocks.air,"denied placement retains pregnancy");
  NBTTagCompound tag=new NBTTagCompound();t.writeEntityToNBT(tag);
  check(tag.getBoolean("HasEgg")&&tag.getInteger("LayEggCounter")==0,"denial resets retry timer and persists egg");
  EntitySeaTurtle restored=new EntitySeaTurtle(w);restored.readEntityFromNBT(tag);restored.setPosition(.5,10,.5);w.rejectWrites=false;
  for(int i=0;i<199;i++)lay.invoke(restored);
  check(restored.hasEgg()&&w.getBlock(0,10,0)==Blocks.air,"no early retry");lay.invoke(restored);
  check(!restored.hasEgg()&&w.getBlock(0,10,0)==OceanContent.turtleEgg,"retry after reload succeeds");
  int meta=w.getBlockMetadata(0,10,0);for(int i=0;i<220;i++)lay.invoke(restored);check(w.getBlockMetadata(0,10,0)==meta,"no duplicate clutch");
  egg.invoke(t,true);w.setBlock(0,10,0,Blocks.air,0,2);w.isRemote=true;for(int i=0;i<210;i++)lay.invoke(t);check(w.getBlock(0,10,0)==Blocks.air,"client cannot lay");w.isRemote=false;
  cpw.mods.fml.common.registry.GameRegistry.registerTileEntity(TileEntityConduit120.class,"fixture_conduit132");
  w.cells.clear();TileEntityConduit120 c=new TileEntityConduit120();c.setWorldObj(w);c.xCoord=0;c.yCoord=10;c.zCoord=0;
  for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++)w.setBlock(x,10+y,z,Blocks.water,0,2);
  int count=0;
  for(int x=-2;x<=2;x++)for(int y=-2;y<=2;y++)for(int z=-2;z<=2;z++)if(TileEntityConduit120.isFrameOffset(x,y,z)){
   w.setBlock(x,10+y,z,BountifulContent.seaLantern,0,2);count++;
  }
  c.updateEntity();check(count==42&&c.isActive()&&c.getFrameCount()==42,"all lantern frame activates at 42");
  w.setBlock(0,12,0,Blocks.stone,0,2);c.updateEntity();check(c.isActive()&&c.getFrameCount()==41,"frame changes while active");
  NBTTagCompound packet=((net.minecraft.network.play.server.S35PacketUpdateTileEntity)c.getDescriptionPacket()).func_148857_g();
  check(packet.getByte("Frame")==41&&packet.getBoolean("Active"),"packet carries new frame");
  check(TileEntityConduit120.isFrameBlock(BountifulContent.prismarine),"prismarine accepted");check(!TileEntityConduit120.isFrameBlock(Blocks.stone),"stone rejected");
  System.out.println("PASS aquatic132: "+checks);
 }
}
