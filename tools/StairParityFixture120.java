import com.mod.htgt6.client.model.json.JsonBlockModelLoader;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.Minecraft120BackportClient;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Stairs;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.client.resources.*;
import com.google.gson.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;

/** Compare the actual JSON geometry to the vanilla 1.7.10 collision implementation. */
public final class StairParityFixture120 {
    static int checks;
    static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
    static Object field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
    static int mask(List<AxisAlignedBB> boxes) {
        int mask=0;
        for(int i=0;i<8;i++) {
            double x=(i&1)*.5+.25,y=80+((i>>1)&1)*.5+.25,z=((i>>2)&1)*.5+.25;
            for(AxisAlignedBB b:boxes)if(x>b.minX&&x<b.maxX&&y>b.minY&&y<b.maxY&&z>b.minZ&&z<b.maxZ){mask|=1<<i;break;}
        }
        return mask;
    }
    public static void main(String[] args)throws Exception {
        Grid world=BountifulMechanicsFixture120.initialize();
        IResourceManager resources=(IResourceManager)Proxy.newProxyInstance(StairParityFixture120.class.getClassLoader(),new Class[]{IResourceManager.class},(p,m,a)->{
            ResourceLocation id=(ResourceLocation)a[0];byte[] bytes=Files.readAllBytes(Paths.get("src/main/resources/assets",id.getResourceDomain(),id.getResourcePath()));
            return Proxy.newProxyInstance(StairParityFixture120.class.getClassLoader(),new Class[]{IResource.class},(q,n,b)->n.getName().equals("getInputStream")?new ByteArrayInputStream(bytes):null);
        });
        Constructor<?> ctor=JsonBlockModelLoader.class.getDeclaredConstructor(int.class);ctor.setAccessible(true);Object loader=ctor.newInstance(1);
        Method load=JsonBlockModelLoader.class.getDeclaredMethod("loadModel",IResourceManager.class,ResourceLocation.class,List.class);load.setAccessible(true);
        Class<?> element=Class.forName(JsonBlockModelLoader.class.getName()+"$Element");
        Method rotate=JsonBlockModelLoader.class.getDeclaredMethod("rotateBounds",element,int.class,int.class);rotate.setAccessible(true);
        Map<String,Integer> reference=new HashMap<String,Integer>();int families=0;
        try(java.util.stream.Stream<Path> files=Files.list(Paths.get("src/main/resources/assets/htgt6/blockstates/1_20_backport"))) {
            for(Path file:(Iterable<Path>)files.filter(p->p.toString().endsWith("_stairs.json"))::iterator) {
                families++;
                JsonObject variants=new JsonParser().parse(new String(Files.readAllBytes(file),"UTF-8")).getAsJsonObject().getAsJsonObject("variants");
                check(variants.entrySet().size()==40,"Incomplete stair variants "+file);
                for(Map.Entry<String,JsonElement> entry:variants.entrySet()) {
                    JsonObject v=entry.getValue().getAsJsonObject();String name=v.get("model").getAsString();
                    Object model=load.invoke(loader,resources,new ResourceLocation(name.contains(":")?name:"htgt6:"+name),new ArrayList());
                    List<AxisAlignedBB> boxes=new ArrayList<AxisAlignedBB>();
                    for(Object cube:(List<?>)field(model,"elements")) {
                        Object b=rotate.invoke(null,cube,v.has("x")?v.get("x").getAsInt():0,v.has("y")?v.get("y").getAsInt():0);
                        boxes.add(AxisAlignedBB.getBoundingBox((Double)field(b,"minX")/16,80+(Double)field(b,"minY")/16,(Double)field(b,"minZ")/16,
                                (Double)field(b,"maxX")/16,80+(Double)field(b,"maxY")/16,(Double)field(b,"maxZ")/16));
                    }
                    int shape=mask(boxes);Integer previous=reference.put(entry.getKey(),shape);
                    check(previous==null||previous==shape,"Different family geometry "+file+" "+entry.getKey());
                }
            }
        }
        check(families>20,"Missing family coverage");
        Method shape=Minecraft120BackportClient.class.getDeclaredMethod("stairShape",IBlockAccess.class,int.class,int.class,int.class,int.class,int.class);shape.setAccessible(true);
        BlockStairs port=new BlockBackport120Stairs(Blocks.stone,"fixture","stone");
        int[] dx={1,-1,0,0},dz={0,0,1,-1};String[] facing={"east","west","south","north"};
        for(int meta=0;meta<8;meta++)for(int a=0;a<4;a++)for(int ma=0;ma<9;ma++)for(int b=0;b<4;b++)for(int mb=0;mb<9;mb++) {
            for(int side=0;side<4;side++)world.setBlock(dx[side],80,dz[side],Blocks.air,0,2);
            world.setBlock(0,80,0,port,meta,2);
            world.setBlock(dx[a],80,dz[a],ma==8?Blocks.air:Blocks.oak_stairs,ma&7,2);
            world.setBlock(dx[b],80,dz[b],mb==8?Blocks.air:port,mb&7,2);
            String key="facing="+facing[meta&3]+",half="+((meta&4)==0?"bottom":"top")+",shape="+shape.invoke(null,world,0,80,0,meta&3,meta&4);
            List<AxisAlignedBB> boxes=new ArrayList<AxisAlignedBB>();
            Blocks.oak_stairs.addCollisionBoxesToList(world,0,80,0,AxisAlignedBB.getBoundingBox(0,80,0,1,81,1),boxes,null);
            check(reference.get(key)==mask(boxes),"JSON/vanilla collision mismatch "+key+" neighbors "+a+":"+ma+" "+b+":"+mb);
            List<AxisAlignedBB> actual=new ArrayList<AxisAlignedBB>();
            port.addCollisionBoxesToList(world,0,80,0,AxisAlignedBB.getBoundingBox(0,80,0,1,81,1),actual,null);
            check(mask(actual)==mask(boxes),"Port collision differs from vanilla");
        }
        System.out.println("Stair parity PASS: "+checks+" checks, "+families+" JSON families, mixed vanilla/port neighbors, both halves");
    }
}
