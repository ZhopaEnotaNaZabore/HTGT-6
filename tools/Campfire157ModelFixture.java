import java.lang.reflect.*;import java.nio.file.*;import java.util.*;
import net.minecraft.client.resources.*;import net.minecraft.util.ResourceLocation;
import com.mod.htgt6.client.model.json.JsonBlockModelLoader;
public final class Campfire157ModelFixture {
 static Object field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 public static void main(String[] args)throws Exception{
  IResourceManager resources=(IResourceManager)Proxy.newProxyInstance(Campfire157ModelFixture.class.getClassLoader(),new Class[]{IResourceManager.class},(p,m,a)->{
   ResourceLocation loc=(ResourceLocation)a[0];Path path=Paths.get("src/main/resources/assets",loc.getResourceDomain(),loc.getResourcePath());
   return Proxy.newProxyInstance(Campfire157ModelFixture.class.getClassLoader(),new Class[]{IResource.class},(q,n,b)->n.getName().equals("getInputStream")?Files.newInputStream(path):null);
  });
  Constructor<?> ctor=JsonBlockModelLoader.class.getDeclaredConstructor(int.class);ctor.setAccessible(true);Object loader=ctor.newInstance(1);
  Method load=JsonBlockModelLoader.class.getDeclaredMethod("loadModel",IResourceManager.class,ResourceLocation.class,List.class);load.setAccessible(true);
  int checked=0;
  for(boolean lit:new boolean[]{false,true}){
   Object model=load.invoke(loader,resources,new ResourceLocation("minecraft","block/campfire"+(lit?"":"_off")),new ArrayList());
   List elements=(List)field(model,"elements");if(elements.size()!=(lit?7:5))throw new AssertionError("elements "+elements.size());
   double top=0;
   for(Object e:elements){double[] to=(double[])field(e,"to");top=Math.max(top,to[1]);
    for(Object face:((Map)field(e,"faces")).values()){
     Object direction=field(face,"direction");Method vertices=JsonBlockModelLoader.class.getDeclaredMethod("faceVertices",e.getClass(),direction.getClass());vertices.setAccessible(true);
     double[][] points=(double[][])vertices.invoke(null,e,direction);
     for(double[] point:points){if(point[1]<0||point[1]>17)throw new AssertionError("height");checked++;}
    }
   }
   if(top!=(lit?17:7))throw new AssertionError("model height "+top);
  }
  System.out.println("PASS campfire157 model: actual loader, 5/7 elements, 7/16 logs, 17/16 flames, "+checked+" vertices; no GL draw");
 }
}
