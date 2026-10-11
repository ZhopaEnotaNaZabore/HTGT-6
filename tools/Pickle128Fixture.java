package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemBlockMarinePlant;
import net.minecraft.entity.player.*;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import java.lang.reflect.*;
import java.util.*;
public final class Pickle128Fixture {
 static int checks;
 static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);checks++;}
 public static class Player extends EntityPlayer {
  boolean deny;
  private Player(World w){super(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"fixture"));}
  public boolean canPlayerEdit(int x,int y,int z,int side,ItemStack s){return !deny;}
  public boolean isSneaking(){return false;}
  public void addChatMessage(net.minecraft.util.IChatComponent c){}
  public boolean canCommandSenderUseCommand(int p,String s){return false;}
  public net.minecraft.util.ChunkCoordinates getPlayerCoordinates(){return new net.minecraft.util.ChunkCoordinates(0,10,0);}
 }
 public static void main(String[] a)throws Exception {
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  w.provider.worldObj=w;
  Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);sun.misc.Unsafe u=(sun.misc.Unsafe)f.get(null);
  Player p=(Player)u.allocateInstance(Player.class);p.capabilities=new PlayerCapabilities();
  Field access=World.class.getDeclaredField("worldAccesses");access.setAccessible(true);access.set(w,new ArrayList());
  ItemBlockMarinePlant item=new ItemBlockMarinePlant(OceanContent.marinePlant);
  for(boolean creative:new boolean[]{false,true})for(int meta=8;meta<11;meta++) {
   w.setBlock(0,10,0,OceanContent.marinePlant,meta,2);ItemStack s=new ItemStack(item,3,8);p.capabilities.isCreativeMode=creative;
   check(item.onItemUse(s,p,w,0,10,0,1,0,0,0),"add succeeds");check(w.getBlockMetadata(0,10,0)==meta+1,"exactly one added");check(s.stackSize==(creative?3:2),"consumption");
  }
  for(int mode=0;mode<3;mode++){
   w.setBlock(0,10,0,OceanContent.marinePlant,8,2);ItemStack s=new ItemStack(item,3,8);
   p.deny=mode==0;w.rejectWrites=mode==1;w.isRemote=mode==2;
   item.onItemUse(s,p,w,0,10,0,1,0,0,0);
   check(w.getBlockMetadata(0,10,0)==8&&s.stackSize==3,"denied/failed/client preserves state");
   p.deny=false;w.rejectWrites=false;w.isRemote=false;
  }
  java.util.zip.ZipFile zip=new java.util.zip.ZipFile("tools/decompile1202/reference/client.jar");
  java.awt.image.BufferedImage ref=javax.imageio.ImageIO.read(zip.getInputStream(zip.getEntry("assets/minecraft/textures/block/sea_pickle.png")));
  java.awt.image.BufferedImage tex=javax.imageio.ImageIO.read(new java.io.File("src/main/resources/assets/htgt6/textures/blocks/sea_pickle.png"));
  check(ref.getWidth()==tex.getWidth()&&ref.getHeight()==tex.getHeight(),"atlas size");
  for(int x=0;x<ref.getWidth();x++)for(int y=0;y<ref.getHeight();y++)check(ref.getRGB(x,y)==tex.getRGB(x,y),"atlas pixel");zip.close();
  System.out.println("PASS pickle128: "+checks);
 }
}
