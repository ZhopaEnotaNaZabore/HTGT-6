package com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration;
import com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.BlockObserver112;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraftforge.common.util.ForgeDirection;
public final class ObserverUpdates120 {
 private ObserverUpdates120(){}
 public static boolean afterChange(boolean changed,World world,int x,int y,int z,int flags){
  if(changed)notifyChange(world,x,y,z,flags);return changed;
 }
 public static void notifyChange(World world,int x,int y,int z,int flags){
  if(world.isRemote||(flags&16)!=0)return;
  for(ForgeDirection direction:ForgeDirection.VALID_DIRECTIONS){
   int ox=x+direction.offsetX,oy=y+direction.offsetY,oz=z+direction.offsetZ;
   if(!world.blockExists(ox,oy,oz))continue;
   Block block=world.getBlock(ox,oy,oz);
   if(block instanceof BlockObserver112)((BlockObserver112)block).observedChange(world,ox,oy,oz,x,y,z);
  }
 }
}
