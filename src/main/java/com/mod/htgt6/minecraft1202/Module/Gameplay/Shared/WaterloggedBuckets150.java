package com.mod.htgt6.minecraft1202.Module.Gameplay.Shared;
import net.minecraft.world.World;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.item.ItemStack;import net.minecraft.init.Items;
/** Bucket transaction for simple waterloggable blocks. */
public final class WaterloggedBuckets150 {
 public static boolean use(IWaterloggedBlock150 block,World w,int x,int y,int z,EntityPlayer p,int side){
  ItemStack held=p.getHeldItem();if(held==null||!p.canPlayerEdit(x,y,z,side,held))return false;
  boolean wet=block.isWaterlogged(w,x,y,z),fill=held.getItem()==Items.water_bucket&&!wet,drain=held.getItem()==Items.bucket&&wet;
  if(!fill&&!drain)return false;if(w.isRemote)return true;
  if(fill&&w.provider.isHellWorld){w.playSoundEffect(x+.5,y+.5,z+.5,"random.fizz",.5F,2.6F);if(!p.capabilities.isCreativeMode)p.inventory.setInventorySlotContents(p.inventory.currentItem,new ItemStack(Items.bucket));return true;}
  if(!block.setWaterlogged(w,x,y,z,fill))return false;
  if(!p.capabilities.isCreativeMode){ItemStack result=new ItemStack(fill?Items.bucket:Items.water_bucket);held.stackSize--;if(held.stackSize<=0)p.inventory.setInventorySlotContents(p.inventory.currentItem,result);else if(!p.inventory.addItemStackToInventory(result))p.dropPlayerItemWithRandomChoice(result,false);}
  return true;
 }
 private WaterloggedBuckets150(){}
}
