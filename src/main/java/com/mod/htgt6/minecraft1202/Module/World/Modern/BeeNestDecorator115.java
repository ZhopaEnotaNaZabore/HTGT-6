package com.mod.htgt6.minecraft1202.Module.World.Modern;
import java.util.*;import net.minecraft.world.World;import net.minecraft.block.Block;import net.minecraft.init.Blocks;import com.google.gson.*;import com.mod.htgt6.minecraft1202.Module.Registry.Modern.BuzzyBees115;import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
/** Uses placed trunk/leaf positions; nests always face south and need an open exit. */
public final class BeeNestDecorator115 {
 public static void configured(World w,Random r,JsonObject config,List<int[]> logs,List<int[]> leaves){if(!config.has("decorators"))return;for(JsonElement e:config.getAsJsonArray("decorators")){JsonObject d=e.getAsJsonObject();if("minecraft:beehive".equals(d.get("type").getAsString()))decorate(w,r,d.get("probability").getAsFloat(),logs,leaves);}}
 public static boolean decorate(World w,Random r,float chance,List<int[]> logs,List<int[]> leaves){
  if(w.isRemote||logs.isEmpty()||r.nextFloat()>=chance||net.minecraft.block.Block.getIdFromBlock(BuzzyBees115.beeNest)<0)return false;
  int bottom=Integer.MAX_VALUE,top=0,leaf=Integer.MAX_VALUE;for(int[] p:logs){bottom=Math.min(bottom,p[1]);top=Math.max(top,p[1]);}for(int[] p:leaves)leaf=Math.min(leaf,p[1]);int y=leaves.isEmpty()?Math.min(bottom+1+r.nextInt(3),top):Math.max(leaf-1,bottom+1);
  List<int[]> candidates=new ArrayList<int[]>();for(int[] p:logs)if(p[1]==y)for(int[] d:new int[][]{{1,0},{-1,0},{0,1}})candidates.add(new int[]{p[0]+d[0],y,p[2]+d[1]});Collections.shuffle(candidates,r);
  for(int[] p:candidates)if(w.blockExists(p[0],y,p[2]+1)&&w.isAirBlock(p[0],y,p[2])&&w.isAirBlock(p[0],y,p[2]+1)){
   if(!w.setBlock(p[0],y,p[2],BuzzyBees115.beeNest,2,2))return false;TileEntityBeehive115 t=BlockBeehive115.tile(w,p[0],y,p[2]);if(t==null){w.setBlockToAir(p[0],y,p[2]);return false;}t.seed(2+r.nextInt(2),r);return true;
  }return false;
 }

 public static Set<String> snapshot(World w,int x,int y,int z){Set<String> before=new HashSet<String>();for(int dy=0;dy<24;dy++)for(int dx=-5;dx<=5;dx++)for(int dz=-5;dz<=5;dz++){Block b=w.getBlock(x+dx,y+dy,z+dz);if(b.isWood(w,x+dx,y+dy,z+dz)||b.isLeaves(w,x+dx,y+dy,z+dz)||isHive(b))before.add(key(w,x+dx,y+dy,z+dz));}return before;}
 private static String key(World w,int x,int y,int z){return x+","+y+","+z+":"+Block.getIdFromBlock(w.getBlock(x,y,z))+":"+w.getBlockMetadata(x,y,z);}
 private static boolean isHive(Block b){return b instanceof BlockBeehive115||b.getClass().getName().equals("ganymedes01.etfuturum.blocks.BlockBeeHive");}
 /** Difference against the pre-growth snapshot excludes neighbouring trees and duplicate Et Futurum nests. */
 public static void legacy(World w,Random r,JsonObject config,int x,int y,int z,Set<String> before){List<int[]> logs=new ArrayList<int[]>(),leaves=new ArrayList<int[]>();for(int dy=0;dy<24;dy++)for(int dx=-5;dx<=5;dx++)for(int dz=-5;dz<=5;dz++){if(before.contains(key(w,x+dx,y+dy,z+dz)))continue;Block b=w.getBlock(x+dx,y+dy,z+dz);if(isHive(b))return;if(b.isWood(w,x+dx,y+dy,z+dz))logs.add(new int[]{x+dx,y+dy,z+dz});else if(b.isLeaves(w,x+dx,y+dy,z+dz))leaves.add(new int[]{x+dx,y+dy,z+dz});}configured(w,r,config,logs,leaves);}
}
