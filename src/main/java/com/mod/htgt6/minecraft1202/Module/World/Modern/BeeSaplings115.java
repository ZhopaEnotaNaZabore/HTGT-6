package com.mod.htgt6.minecraft1202.Module.World.Modern;
import java.util.*;import net.minecraft.world.World;import net.minecraft.init.Blocks;import cpw.mods.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;import cpw.mods.fml.common.gameevent.TickEvent;import com.google.gson.*;
/** Observe completed growth at tick end without replacing the vanilla/mod tree generator. */
public final class BeeSaplings115 {
 private final Map<World,Map<String,Growth>> pending=new WeakHashMap<World,Map<String,Growth>>();
 @SubscribeEvent public void grow(SaplingGrowTreeEvent e){if(e.world.isRemote||e.world.getBlock(e.x,e.y,e.z)!=Blocks.sapling)return;int meta=e.world.getBlockMetadata(e.x,e.y,e.z)&7;if(meta!=0&&meta!=2)return;
  boolean flower=false;for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)if(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityBee115.flower(e.world.getBlock(e.x+x,e.y,e.z+z),e.world.getBlockMetadata(e.x+x,e.y,e.z+z)))flower=true;
  if(!flower)return;Map<String,Growth> positions=pending.get(e.world);if(positions==null){positions=new LinkedHashMap<String,Growth>();pending.put(e.world,positions);}String key=e.x+","+e.y+","+e.z;if(!positions.containsKey(key))positions.put(key,new Growth(e.world,e.x,e.y,e.z));
 }
 @SubscribeEvent public void tick(TickEvent.WorldTickEvent e){if(e.phase!=TickEvent.Phase.END||e.world.isRemote)return;Map<String,Growth> positions=pending.remove(e.world);if(positions==null)return;JsonObject config=new JsonObject(),d=new JsonObject();JsonArray a=new JsonArray();d.addProperty("type","minecraft:beehive");d.addProperty("probability",.05F);a.add(d);config.add("decorators",a);
  for(Growth growth:positions.values()){int[] p=growth.pos;if(e.world.blockExists(p[0],p[1],p[2])&&e.world.getBlock(p[0],p[1],p[2])==Blocks.log)BeeNestDecorator115.legacy(e.world,e.world.rand,config,p[0],p[1],p[2],growth.before); }
 }
 private static final class Growth {final int[] pos;final Set<String> before;Growth(World w,int x,int y,int z){pos=new int[]{x,y,z};before=BeeNestDecorator115.snapshot(w,x,y,z);}}
}
