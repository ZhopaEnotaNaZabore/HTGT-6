package com.mod.htgt6.common.crusher;

import com.mod.htgt6.common.chemical.MultiTileChemicalPlant;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import com.mod.htgt6.common.handler.recipe.RM.IndustrialCrusherRecipeHandler;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.data.RM;
import gregapi.oredict.IOreDictItemDataOverrideItem;
import gregapi.oredict.OreDictItemData;
import gregapi.oredict.OreDictPrefix;
import gregapi.recipes.IRecipeMapHandler;
import gregapi.recipes.Recipe;
import java.lang.reflect.Method;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Unregistered host-rock inputs through the real importer and controller lookup. */
public final class CrusherOreImportFixture {
    private static OreDictPrefix[] HOSTS;
    private static final class Ore extends Item implements IOreDictItemDataOverrideItem {
        Ore(){setHasSubtypes(true);}
        @Override public OreDictItemData getOreDictItemData(ItemStack stack){
            return new OreDictItemData(HOSTS[stack.getItemDamage()],MT.Ir);
        }
    }
    public static BountifulMechanicsFixture120.Grid initializeGT6()throws Exception{
        BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
        java.lang.reflect.Field mods=cpw.mods.fml.common.Loader.class.getDeclaredField("namedMods");mods.setAccessible(true);
        mods.set(cpw.mods.fml.common.Loader.instance(),new java.util.HashMap<String,cpw.mods.fml.common.ModContainer>());
        java.lang.reflect.Field controller=cpw.mods.fml.common.Loader.class.getDeclaredField("modController");controller.setAccessible(true);
        java.lang.reflect.Field active=cpw.mods.fml.common.LoadController.class.getDeclaredField("activeContainer");active.setAccessible(true);
        cpw.mods.fml.common.ModMetadata metadata=new cpw.mods.fml.common.ModMetadata();metadata.modId="crusher_fixture";metadata.name="Crusher fixture";
        active.set(controller.get(cpw.mods.fml.common.Loader.instance()),new cpw.mods.fml.common.DummyModContainer(metadata));
        java.lang.reflect.Field logSide=cpw.mods.fml.relauncher.FMLRelaunchLog.class.getDeclaredField("side");logSide.setAccessible(true);
        logSide.set(null,cpw.mods.fml.relauncher.Side.SERVER);
        java.lang.reflect.Field unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe=(sun.misc.Unsafe)unsafeField.get(null);
        // This fixture supplies item-data overrides directly. Forge's event
        // constructor transformer is absent outside LaunchWrapper, so do not
        // register GT6's unrelated ore/fluid listeners in this isolated JVM.
        Class.forName("net.minecraftforge.common.MinecraftForge");
        java.lang.reflect.Field bus=net.minecraftforge.common.MinecraftForge.class.getDeclaredField("EVENT_BUS");
        unsafe.putObject(unsafe.staticFieldBase(bus),unsafe.staticFieldOffset(bus),
                new cpw.mods.fml.common.eventhandler.EventBus(){@Override public void register(Object listener){}});
        gregapi.data.CS.GAPI=(gregapi.api.Abstract_Mod)unsafe.allocateInstance(gregapi.GT_API.class);
        gregapi.data.CS.GAPI.mBeforePreInit=new java.util.ArrayList<Runnable>();
        return world;
    }
    public static void main(String[] args)throws Exception{
        BountifulMechanicsFixture120.Grid world=initializeGT6();
        HOSTS=new OreDictPrefix[]{OP.oreLimestone,OP.oreBasalt,OP.oreKomatiite,OP.oreKimberlite};
        Ore ore=new Ore();GameRegistry.registerItem(ore,"crusher_host_fixture");
        final int[] generated={0};
        RM.Crusher.mRecipeMapHandlers.clear();RM.Crusher.mRecipeList.clear();
        RM.Shredder.mRecipeMapHandlers.clear();RM.Shredder.mRecipeList.clear();
        RM.Crusher.mRecipeMapHandlers.add(new IRecipeMapHandler.RecipeMapHandler(){
            @Override public boolean addRecipesUsing(Recipe.RecipeMap map,boolean nei,ItemStack input,OreDictItemData data){
                require(data!=null&&data.mPrefix==HOSTS[input.getItemDamage()],"exact host supplied to GT6 handler");
                generated[0]++;
                Recipe recipe=new Recipe(false,false,true,new ItemStack[]{input.copy()},
                        new ItemStack[]{new ItemStack(Items.dye,2,input.getItemDamage())},null,new long[]{10000},
                        null,null,40,8,0);
                map.mRecipeList.add(recipe);return true;
            }
        });
        IndustrialCrusherRecipeHandler handler=IndustrialCrusherRecipeHandler.INSTANCE;
        handler.getRecipeList().clear();
        MultiTileIndustrialCrusher machine=new MultiTileIndustrialCrusher();machine.setWorldObj(world);
        Method lookup=MultiTileIndustrialCrusher.class.getDeclaredMethod("findRecipe");lookup.setAccessible(true);
        for(int host=0;host<HOSTS.length;host++){
            ItemStack input=new ItemStack(ore,3,host);
            machine.inventory[MultiTileChemicalPlant.INPUT_SLOT_START]=input;
            ChemicalPlantRecipe recipe=(ChemicalPlantRecipe)lookup.invoke(machine);
            require(recipe!=null,"missing world ore must import at runtime: "+HOSTS[host]);
            require(recipe.inputs[0].getItem()==ore&&recipe.inputs[0].getItemDamage()==host,"input rock retained");
            require(recipe.outputs[0].getItemDamage()==host&&recipe.outputs[0].stackSize==2,"GT6 output retained");
            require(input.stackSize==3,"lookup must not consume or normalize input");
            int count=handler.getRecipeList().size();lookup.invoke(machine);
            require(handler.getRecipeList().size()==count,"idempotent lazy import");
        }
        require(generated[0]==4,"one materialization per exact input");
        ChemicalPlantRecipe basalt=handler.getRecipeList().get(1);
        java.lang.reflect.Field current=MultiTileChemicalPlant.class.getDeclaredField("currentRecipe");current.setAccessible(true);
        current.set(machine,basalt);
        GameRegistry.registerTileEntity(MultiTileIndustrialCrusher.class,"crusher_fixture_controller");
        net.minecraft.nbt.NBTTagCompound saved=new net.minecraft.nbt.NBTTagCompound();machine.writeToNBT(saved);
        java.util.Collections.reverse(handler.getRecipeList());
        MultiTileIndustrialCrusher restored=new MultiTileIndustrialCrusher();restored.setWorldObj(world);restored.readFromNBT(saved);
        require(current.get(restored)==basalt,"saved recipe follows stable identity after enumeration order changes");
        checkConveyor(world);
        System.out.println("CrusherOreImportFixture OK: four hosts, unregistered variants, exact outputs, idempotence, server lookup");
    }
    private static final class Mob extends net.minecraft.entity.passive.EntityPig {
        int hits;
        Mob(net.minecraft.world.World world){super(world);}
        @Override public boolean attackEntityFrom(net.minecraft.util.DamageSource source,float damage){
            hits++;return true;
        }
    }
    private static void checkConveyor(BountifulMechanicsFixture120.Grid world)throws Exception{
        MultiTileIndustrialCrusher machine=new MultiTileIndustrialCrusher();machine.setWorldObj(world);
        net.minecraft.nbt.NBTTagCompound nbt=new net.minecraft.nbt.NBTTagCompound();
        nbt.setBoolean("StructureFormed",true);machine.readFromNBT(nbt);machine.enabled=true;
        Mob mob=new Mob(world);mob.setPosition(.5,3.11,1.5);
        world.loadedEntityList.add(mob);
        for(int tick=0;tick<180;tick++){
            machine.conveyor.tick(true);
            if(mob.posZ-.5<CrusherConveyorRules.MOB_END-.01)require(mob.hits==0,"no damage before entering roller");
            require(mob.boundingBox.minY>=CrusherConveyorRules.DECK_Y,"feet stay above deck");
        }
        require(Math.abs(mob.posZ-.5-CrusherConveyorRules.MOB_END)<.001&&mob.hits>0,"mob reaches roller centre and is crushed");
        require(CrusherConveyor.owner(mob)==machine,"mob remains owned beyond old intake limit");
        double stopped=mob.posZ;machine.conveyor.tick(false);
        require(mob.posZ==stopped,"power loss pauses transport");
        TileCrusherCollision cell=new TileCrusherCollision();cell.setWorldObj(world);
        cell.xCoord=0;cell.yCoord=3;cell.zCoord=7;cell.bind(machine,0,3,7);
        world.tiles.put("0,3,7",cell);
        java.util.List<net.minecraft.util.AxisAlignedBB> boxes=new java.util.ArrayList<net.minecraft.util.AxisAlignedBB>();
        new BlockCrusherCollision().addCollisionBoxesToList(world,0,3,7,
                net.minecraft.util.AxisAlignedBB.getBoundingBox(0,2.9,7,1,4,8),boxes,mob);
        require(boxes.size()==1&&Math.abs(boxes.get(0).maxY-CrusherConveyorRules.DECK_Y)<.001,"captured mob retains floor collision inside rollers");
        machine.conveyor.release();world.loadedEntityList.clear();
        System.out.println("Crusher conveyor OK: mob enters roller, delayed damage, supported feet, pause and release");
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
