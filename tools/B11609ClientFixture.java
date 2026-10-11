import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;
import com.google.gson.*;
import com.mod.htgt6.client.model.json.JsonBlockModelLoader;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;

/** Uses the production model parser and parent/texture resolution, without OpenGL. */
public final class B11609ClientFixture {
    static int checks;
    static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
    static Object field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
    public static void main(String[] args)throws Exception {
        IResourceManager resources=(IResourceManager)Proxy.newProxyInstance(B11609ClientFixture.class.getClassLoader(),new Class[]{IResourceManager.class},(p,m,a)->{
            ResourceLocation id=(ResourceLocation)a[0];byte[] bytes=Files.readAllBytes(Paths.get("src/main/resources/assets",id.getResourceDomain(),id.getResourcePath()));
            return Proxy.newProxyInstance(B11609ClientFixture.class.getClassLoader(),new Class[]{IResource.class},(q,n,b)->n.getName().equals("getInputStream")?new ByteArrayInputStream(bytes):null);
        });
        Constructor<?> ctor=JsonBlockModelLoader.class.getDeclaredConstructor(int.class);ctor.setAccessible(true);Object loader=ctor.newInstance(1);
        Method load=JsonBlockModelLoader.class.getDeclaredMethod("loadModel",IResourceManager.class,ResourceLocation.class,List.class);load.setAccessible(true);
        Method resolve=JsonBlockModelLoader.class.getDeclaredMethod("resolveTexture",Map.class,String.class);resolve.setAccessible(true);
        Set<String> models=new LinkedHashSet<String>();
        for(String name:new String[]{"basalt","polished_basalt","chiseled_nether_bricks","cracked_nether_bricks","polished_blackstone_button","polished_blackstone_pressure_plate","blackstone_stairs","blackstone_slab","polished_blackstone_stairs","polished_blackstone_slab","polished_blackstone_brick_stairs","polished_blackstone_brick_slab","blackstone_wall","polished_blackstone_wall","polished_blackstone_brick_wall"}) {
            models.add("htgt6:item/1_20_backport/"+name);
            JsonObject state=new JsonParser().parse(new String(Files.readAllBytes(Paths.get("src/main/resources/assets/htgt6/blockstates/1_20_backport/"+name+".json")),"UTF-8")).getAsJsonObject();
            if(state.has("variants"))for(Map.Entry<String,JsonElement> e:state.getAsJsonObject("variants").entrySet())models.add(e.getValue().getAsJsonObject().get("model").getAsString());
            if(state.has("multipart"))for(JsonElement part:state.getAsJsonArray("multipart"))models.add(part.getAsJsonObject().getAsJsonObject("apply").get("model").getAsString());
        }
        for(String name:models) {
            Object model=load.invoke(loader,resources,new ResourceLocation(name),new ArrayList());
            check(!((List)field(model,"elements")).isEmpty(),"drawable "+name);
            Map<String,String> textures=(Map)field(model,"textures");
            for(String texture:textures.values()) {
                String resolved=(String)resolve.invoke(null,textures,texture);String path=resolved.substring(resolved.indexOf(':')+1).replace("block/","blocks/");
                check(Files.isRegularFile(Paths.get("src/main/resources/assets/htgt6/textures",path+".png")),"resolved "+resolved);
            }
        }
        waterRenderer();
        stoneFamilyBindings((JsonBlockModelLoader)loader);
        System.out.println("Nether masonry actual JSON loader: "+checks+" checks passed");
    }
    static void stoneFamilyBindings(JsonBlockModelLoader loader)throws Exception {
        Method bind=com.mod.htgt6.minecraft1202.Module.Client.Modern.Minecraft120BackportClient.class.getDeclaredMethod("bindModernStoneFamilies",JsonBlockModelLoader.class,JsonBlockModelLoader.TextureResolver.class);
        bind.setAccessible(true);bind.invoke(null,loader,(JsonBlockModelLoader.TextureResolver)(source->new ResourceLocation(source)));
        Map bindings=(Map)field(loader,"bindings");int expected=0,withoutWalls=0;
        for(com.mod.htgt6.minecraft1202.Module.Registry.Modern.ModernStoneShapes120.Family family:com.mod.htgt6.minecraft1202.Module.Registry.Modern.ModernStoneShapes120.FAMILIES) {
            check(bindings.containsKey(family.stairs)&&bindings.containsKey(family.slab)&&bindings.containsKey(family.doubleSlab),"stone family shapes registered "+family.name);expected+=3;
            if(family.wall!=null){check(bindings.containsKey(family.wall),"optional wall registered "+family.name);expected++;}else withoutWalls++;
        }
        check(withoutWalls>0,"fixture covers families without walls");
        check(!bindings.containsKey(null)&&bindings.size()==expected,"no null or duplicate family bindings");
    }
    static void waterRenderer()throws Exception {
        com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.initialize();
        final net.minecraft.block.Block slab=new com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBlackstoneSlab116(false,"blackstone",net.minecraft.init.Blocks.stone);
        final int[] meta={4};
        final net.minecraft.block.Block[] rendered={slab};
        net.minecraft.world.IBlockAccess world=(net.minecraft.world.IBlockAccess)Proxy.newProxyInstance(B11609ClientFixture.class.getClassLoader(),new Class[]{net.minecraft.world.IBlockAccess.class},(p,m,a)->{
            if(m.getName().equals("getBlock"))return rendered[0];
            if(m.getName().equals("getBlockMetadata"))return meta[0];
            if(m.getName().equals("getLightBrightnessForSkyBlocks"))return 12345;
            if(m.getReturnType()==boolean.class)return false;
            if(m.getReturnType()==int.class)return 0;
            return null;
        });
        final int[] calls={0};
        net.minecraft.client.renderer.RenderBlocks renderer=new net.minecraft.client.renderer.RenderBlocks(world){
            @Override public boolean renderBlockLiquid(net.minecraft.block.Block b,int x,int y,int z){
                check(b==net.minecraft.init.Blocks.water&&blockAccess.getBlock(x,y,z)==b,"water renderer sees fluid");
                check(blockAccess.getBlockMetadata(x,y,z)==0,"water source metadata");
                check(blockAccess.getLightBrightnessForSkyBlocks(x,y,z,0)==12345,"world lighting retained");calls[0]++;return true;
            }
        };
        check(slab.canRenderInPass(0)&&!com.mod.htgt6.minecraft1202.Module.Client.Modern.ShapeWaterRenderer116.waterPass(),"opaque pass");
        check(slab.canRenderInPass(1)&&com.mod.htgt6.minecraft1202.Module.Client.Modern.ShapeWaterRenderer116.waterPass(),"transparent pass");
        check(com.mod.htgt6.minecraft1202.Module.Client.Modern.ShapeWaterRenderer116.render(world,0,80,0,slab,renderer),"wet fluid rendered");
        check(renderer.blockAccess==world,"render access restored");meta[0]=0;
        check(!com.mod.htgt6.minecraft1202.Module.Client.Modern.ShapeWaterRenderer116.render(world,0,80,0,slab,renderer)&&calls[0]==1,"dry shape has no water mesh");
        meta[0]=4;net.minecraft.client.renderer.RenderBlocks failing=new net.minecraft.client.renderer.RenderBlocks(world){
            @Override public boolean renderBlockLiquid(net.minecraft.block.Block b,int x,int y,int z){throw new IllegalStateException("fixture");}
        };
        try{com.mod.htgt6.minecraft1202.Module.Client.Modern.ShapeWaterRenderer116.render(world,0,80,0,slab,failing);throw new AssertionError("exception swallowed");}catch(IllegalStateException expected){check(failing.blockAccess==world,"render access restored on failure");}
        slab.canRenderInPass(0);Thread worker=new Thread(()->slab.canRenderInPass(1));worker.start();worker.join();
        check(!com.mod.htgt6.minecraft1202.Module.Client.Modern.ShapeWaterRenderer116.waterPass(),"render pass isolated per render thread");
        rendered[0]=new com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBlackstoneWall116(net.minecraft.init.Blocks.stone,"wall_fixture");meta[0]=1;
        check(rendered[0].canRenderInPass(1),"wall transparent pass");
        check(com.mod.htgt6.minecraft1202.Module.Client.Modern.ShapeWaterRenderer116.render(world,0,80,0,rendered[0],renderer),"wall water bit uses fluid renderer");
        check(renderer.blockAccess==world,"wall restores renderer access");
        meta[0]=0;check(!com.mod.htgt6.minecraft1202.Module.Client.Modern.ShapeWaterRenderer116.render(world,0,80,0,rendered[0],renderer),"dry wall has no fluid mesh");
    }

}
