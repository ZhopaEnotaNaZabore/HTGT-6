import java.util.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.io.*;
import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.inventory.*;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.*;
import net.minecraft.tileentity.TileEntityFurnace;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Events.Modern.Backport120AxeStrippingHandler;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import com.mod.htgt6.client.model.json.JsonBlockModelLoader;

public final class NetherWood116Fixture {
    static int checks;
    static void check(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
    static ItemStack craft(Grid world,ItemStack... inputs){
        InventoryCrafting grid=new InventoryCrafting(new Container(){public boolean canInteractWith(EntityPlayer p){return true;}},3,3);
        for(int i=0;i<inputs.length;i++)grid.setInventorySlotContents(i,inputs[i]);return CraftingManager.getInstance().findMatchingRecipe(grid,world);
    }
    public static void main(String[] args)throws Exception{
        Grid world=B11605Fixture.initialize();NetherWoodContent116.register();NetherWoodContent116.register();
        for(NetherWoodContent116.Family f:NetherWoodContent116.FAMILIES){
            for(Block block:f.blocks()){
                check(Item.getItemFromBlock(block)!=null,"registered item "+block.getUnlocalizedName());
                check(!block.getMaterial().getCanBurn(),"non-burning material "+block.getUnlocalizedName());
                check(block.getFlammability(world,0,80,0,net.minecraftforge.common.util.ForgeDirection.UP)==0,"non-flammable "+block.getUnlocalizedName());
                check(TileEntityFurnace.getItemBurnTime(new ItemStack(block))==0,"native furnace rejects "+block.getUnlocalizedName());
            }
            for(Item item:new Item[]{f.wood.door,f.wood.sign,f.wood.hangingSign})check(TileEntityFurnace.getItemBurnTime(new ItemStack(item))==0,"non-fuel public item");
            for(int axis:new int[]{0,4,8}){
                Backport120AxeStrippingHandler.Result stem=Backport120AxeStrippingHandler.stripped(f.stem,axis);
                check(stem.block==f.strippedStem&&stem.metadata==axis,"stem strip retains axis");
                Backport120AxeStrippingHandler.Result bark=Backport120AxeStrippingHandler.stripped(f.hyphae,axis);
                check(bark.block==f.strippedHyphae&&bark.metadata==axis,"hyphae strip retains axis");
            }
            for(Block source:new Block[]{f.stem,f.hyphae,f.strippedStem,f.strippedHyphae}){ItemStack result=craft(world,new ItemStack(source));check(result!=null&&result.getItem()==Item.getItemFromBlock(f.wood.planks)&&result.stackSize==4,"four planks from each stem variant");}
            ItemStack bark=craft(world,new ItemStack(f.stem),new ItemStack(f.stem),null,new ItemStack(f.stem),new ItemStack(f.stem));check(bark!=null&&bark.getItem()==Item.getItemFromBlock(f.hyphae)&&bark.stackSize==3,"hyphae recipe");
            ItemStack door=craft(world,new ItemStack(f.wood.planks),new ItemStack(f.wood.planks),null,new ItemStack(f.wood.planks),new ItemStack(f.wood.planks),null,new ItemStack(f.wood.planks),new ItemStack(f.wood.planks));check(door!=null&&door.getItem()==f.wood.door&&door.stackSize==3,"door recipe");
        }
        check(TileEntityFurnace.getItemBurnTime(new ItemStack(Blocks.planks))==300,"ordinary wood still burns");check(TileEntityFurnace.getItemBurnTime(new ItemStack(Items.coal))==1600,"coal unaffected");
        byte[] furnace=org.apache.commons.io.IOUtils.toByteArray(NetherWood116Fixture.class.getClassLoader().getResourceAsStream("net/minecraft/tileentity/TileEntityFurnace.class"));
        byte[] transformed=com.mod.htgt6.asm.NetherWoodFuelASM116.transform(furnace);check(Arrays.equals(transformed,com.mod.htgt6.asm.NetherWoodFuelASM116.transform(transformed)),"fuel transformer idempotent");
        models();System.out.println("Nether wood 116: "+checks+" checks passed");
    }
    static void models()throws Exception{
        IResourceManager resources=(IResourceManager)Proxy.newProxyInstance(NetherWood116Fixture.class.getClassLoader(),new Class[]{IResourceManager.class},(p,m,a)->{
            ResourceLocation id=(ResourceLocation)a[0];byte[] bytes=Files.readAllBytes(Paths.get("src/main/resources/assets",id.getResourceDomain(),id.getResourcePath()));
            return Proxy.newProxyInstance(NetherWood116Fixture.class.getClassLoader(),new Class[]{IResource.class},(q,n,b)->n.getName().equals("getInputStream")?new ByteArrayInputStream(bytes):null);
        });
        Constructor<?> constructor=JsonBlockModelLoader.class.getDeclaredConstructor(int.class);constructor.setAccessible(true);JsonBlockModelLoader loader=(JsonBlockModelLoader)constructor.newInstance(1);
        Method load=JsonBlockModelLoader.class.getDeclaredMethod("loadModel",IResourceManager.class,ResourceLocation.class,List.class);load.setAccessible(true);
        Method bind=com.mod.htgt6.minecraft1202.Module.Client.Modern.Minecraft120BackportClient.class.getDeclaredMethod("bindNetherWoodFamilies",JsonBlockModelLoader.class,JsonBlockModelLoader.TextureResolver.class);bind.setAccessible(true);bind.invoke(null,loader,(JsonBlockModelLoader.TextureResolver)(s->new ResourceLocation(s)));
        Field bindings=JsonBlockModelLoader.class.getDeclaredField("bindings");bindings.setAccessible(true);Map map=(Map)bindings.get(loader);
        for(NetherWoodContent116.Family family:NetherWoodContent116.FAMILIES){
            for(Block b:family.blocks())if(!family.sign(b))check(map.containsKey(b),"client binding "+b.getUnlocalizedName());
            for(String suffix:new String[]{"planks","stem","hyphae","stairs","slab","fence","fence_gate","door","trapdoor","button","pressure_plate"}){
                JsonElement state=new JsonParser().parse(new String(Files.readAllBytes(Paths.get("src/main/resources/assets/htgt6/blockstates/1_20_backport/"+family.name+"_"+suffix+".json")),"UTF-8"));
                Set<String> models=new HashSet<String>();collect(state,models);
                for(String model:models)check(load.invoke(loader,resources,new ResourceLocation(model),new ArrayList())!=null,"official model loads "+model);
            }
        }
    }
    static void collect(JsonElement node,Set<String> out){if(node.isJsonArray())for(JsonElement e:node.getAsJsonArray())collect(e,out);else if(node.isJsonObject())for(Map.Entry<String,JsonElement> e:node.getAsJsonObject().entrySet()){if(e.getKey().equals("model"))out.add(e.getValue().getAsString());else collect(e.getValue(),out);}}
}
