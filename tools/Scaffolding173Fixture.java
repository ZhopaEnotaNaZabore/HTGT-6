package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.client.model.json.JsonBlockModelLoader;
import com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.VillageAndPillageUpdateClient;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import net.minecraft.client.resources.*;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.init.Blocks;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
public final class Scaffolding173Fixture {
 static int checks;
 static Object field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);checks++;}
 public static void main(String[]args)throws Exception{
  BountifulMechanicsFixture120.initialize();
  Constructor<JsonBlockModelLoader> ctor=JsonBlockModelLoader.class.getDeclaredConstructor(int.class);ctor.setAccessible(true);
  Field singleton=JsonBlockModelLoader.class.getDeclaredField("instance");singleton.setAccessible(true);singleton.set(null,ctor.newInstance(1));
  Method bind=VillageAndPillageUpdateClient.class.getDeclaredMethod("bindPoiModels");bind.setAccessible(true);bind.invoke(null);
  Map bindings=(Map)field(JsonBlockModelLoader.install(),"bindings");
  for(Object entry:bindings.entrySet()){
   Object b=((Map.Entry)entry).getValue();JsonBlockModelLoader.StateMapper mapper=(JsonBlockModelLoader.StateMapper)field(b,"mapper");
   for(int meta=0;meta<16;meta++)check(mapper.variant(null,0,0,0,meta)!=null,"null-world item mapping "+((Map.Entry)entry).getKey());
  }
  Object binding=bindings.get(VillagePillageContent.scaffolding);
  JsonBlockModelLoader.StateMapper mapper=(JsonBlockModelLoader.StateMapper)field(binding,"mapper");
  check("bottom=false".equals(mapper.variant(null,0,0,0,7)),"item uses stable shape independently of metadata");
  for(final int meta:new int[]{0,1,7,8,9,15})for(final boolean below:new boolean[]{false,true}){
   IBlockAccess world=(IBlockAccess)Proxy.newProxyInstance(Scaffolding173Fixture.class.getClassLoader(),new Class[]{IBlockAccess.class},(p,m,a)->m.getName().equals("getBlockMetadata")?meta:m.getName().equals("getBlock")?(below?VillagePillageContent.scaffolding:Blocks.air):null);
   check(mapper.variant(world,2,3,4,meta).equals("bottom="+((meta&7)>0&&!below)),"world bottom unchanged");
  }
  IResourceManager resources=(IResourceManager)Proxy.newProxyInstance(Scaffolding173Fixture.class.getClassLoader(),new Class[]{IResourceManager.class},(p,m,a)->{
   ResourceLocation loc=(ResourceLocation)a[0];Path path=Paths.get("src/main/resources/assets",loc.getResourceDomain(),loc.getResourcePath());
   return Proxy.newProxyInstance(Scaffolding173Fixture.class.getClassLoader(),new Class[]{IResource.class},(q,n,b)->n.getName().equals("getInputStream")?Files.newInputStream(path):null);
  });
  IIconRegister icons=(IIconRegister)Proxy.newProxyInstance(Scaffolding173Fixture.class.getClassLoader(),new Class[]{IIconRegister.class},(p,m,a)->null);
  Method load=binding.getClass().getDeclaredMethod("load",IResourceManager.class,IIconRegister.class);load.setAccessible(true);load.invoke(binding,resources,icons);
  Object dedicated=field(binding,"inventoryVariant");check(dedicated!=null,"dedicated reference inventory model loads");
  Method inventory=binding.getClass().getDeclaredMethod("inventoryVariants",int.class);inventory.setAccessible(true);
  check(((List)inventory.invoke(binding,0)).get(0)==dedicated,"inventory pipeline selects dedicated model");
  Field item=binding.getClass().getDeclaredField("inventoryVariant");item.setAccessible(true);item.set(binding,null);
  check(((List)inventory.invoke(binding,0)).size()==1,"fallback remains safe when resource pack omits item model");
  System.out.println("PASS scaffolding173: "+checks+" checks, no GL context");
 }
}
