import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.common.handler.recipe.RM.AssemblerRecipeHandler;
import com.mod.htgt6.common.handler.recipe.RM.LineAssemblerRecipeHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/** Tests machine matching/consumption with stand-in registry items, without launching a world. */
public final class IronChestMachineFixture {
    private static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            String[] paths = System.getProperty("java.class.path").split(java.io.File.pathSeparator);
            java.net.URL[] urls = new java.net.URL[paths.length];
            for (int i=0;i<paths.length;i++) urls[i]=new java.io.File(paths[i]).toURI().toURL();
            net.minecraft.launchwrapper.Launch.classLoader = new net.minecraft.launchwrapper.LaunchClassLoader(urls);
            net.minecraft.launchwrapper.Launch.blackboard = new java.util.HashMap<String,Object>();
            net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment", true);
            net.minecraft.launchwrapper.Launch.classLoader.registerTransformer(
                    "cpw.mods.fml.common.asm.transformers.EventSubscriptionTransformer");
            Thread.currentThread().setContextClassLoader(net.minecraft.launchwrapper.Launch.classLoader);
            net.minecraft.launchwrapper.Launch.classLoader.loadClass("IronChestMachineFixture")
                    .getMethod("main",String[].class).invoke(null,(Object)new String[]{"loaded"});
            return;
        }
        cpw.mods.fml.common.Loader.injectData("7","10","99","99","1.7.10","9.05",
                new java.io.File("."),new java.util.ArrayList<String>());
        net.minecraft.init.Bootstrap.func_151354_b();
        java.lang.reflect.Field logSide = cpw.mods.fml.relauncher.FMLRelaunchLog.class.getDeclaredField("side");
        logSide.setAccessible(true);
        logSide.set(null, cpw.mods.fml.relauncher.Side.SERVER);
        java.lang.reflect.Field namedMods = cpw.mods.fml.common.Loader.class.getDeclaredField("namedMods");
        namedMods.setAccessible(true);
        namedMods.set(cpw.mods.fml.common.Loader.instance(), new java.util.HashMap<String, cpw.mods.fml.common.ModContainer>());
        cpw.mods.fml.common.LoadController controller = new cpw.mods.fml.common.LoadController(cpw.mods.fml.common.Loader.instance());
        java.lang.reflect.Field controllerField = cpw.mods.fml.common.Loader.class.getDeclaredField("modController");
        controllerField.setAccessible(true);
        controllerField.set(cpw.mods.fml.common.Loader.instance(), controller);
        java.lang.reflect.Field active = cpw.mods.fml.common.LoadController.class.getDeclaredField("activeContainer");
        active.setAccessible(true);
        cpw.mods.fml.common.ModMetadata metadata = new cpw.mods.fml.common.ModMetadata();
        metadata.modId = "IronChestFixture";
        metadata.name = "IronChestFixture";
        active.set(controller, new cpw.mods.fml.common.DummyModContainer(metadata));
        new gregapi.GT_API();
        checkIronChestRegistration();
        Item chest = new Item(), plate = new Item(), screw = new Item(), selector = new Item();
        Fluid glass = new Fluid("fixture_molten_glass"); FluidRegistry.registerFluid(glass);
        ItemStack[] requirements = new ItemStack[9];
        requirements[0] = new ItemStack(chest, 2, 6);
        requirements[1] = new ItemStack(plate, 16);
        requirements[2] = new ItemStack(screw, 32);
        requirements[3] = new ItemStack(selector, 0, 8);
        AssemblerRecipeHandler.addRecipe(requirements, new FluidStack[]{new FluidStack(glass,1296)},
                new ItemStack(chest,1,5),1200,412,3);
        ItemStack[] inventory = new ItemStack[10];
        for(int i=0;i<4;i++) inventory[i]=requirements[i].copy();
        inventory[3].stackSize=1;
        FluidTank[] tanks={new FluidTank(new FluidStack(glass,1295),16000)};
        check(AssemblerRecipeHandler.INSTANCE.findRecipe(inventory,tanks)==null,"reject short fluid amount");
        tanks[0].fill(new FluidStack(glass,1),true);
        inventory[3].setItemDamage(7);
        check(AssemblerRecipeHandler.INSTANCE.findRecipe(inventory,tanks)==null,"reject selector 7");
        inventory[3].setItemDamage(8);
        MachineRecipe crystal=AssemblerRecipeHandler.INSTANCE.findRecipe(inventory,tanks);
        check(crystal!=null,"accept selector 8 and exact inputs");
        AssemblerRecipeHandler.INSTANCE.consumeInputs(crystal,inventory);
        check(inventory[0]==null && inventory[1]==null && inventory[2]==null,"consume item quantities");
        check(inventory[3]!=null && inventory[3].stackSize==1,"selector is not consumed");

        ItemStack[] parts=new ItemStack[16];
        Item component=new Item();
        for(int i=0;i<16;i++) parts[i]=new ItemStack(component,64,i/2);
        MachineRecipe dirt=new MachineRecipe(parts,new ItemStack[]{new ItemStack(chest,1,7)},null,null,72000,134217728L,12);
        ItemStack[] line=new ItemStack[17];
        for(int i=0;i<16;i++) line[i]=parts[i].copy();
        check(LineAssemblerRecipeHandler.INSTANCE.matches(dirt,line,0,16),"all 16 full slots match");
        line[15]=null;
        check(!LineAssemblerRecipeHandler.INSTANCE.matches(dirt,line,0,16),"cannot reuse another slot for duplicate component");
        line[15]=parts[15].copy(); line[15].stackSize=63;
        check(!LineAssemblerRecipeHandler.INSTANCE.matches(dirt,line,0,16),"all 64 items are required");
        line[15].stackSize=64;
        LineAssemblerRecipeHandler.INSTANCE.consumeInputs(dirt,line,0,16);
        for(int i=0;i<16;i++) check(line[i]==null,"all 16 input stacks consumed");
        System.out.println("IronChestMachineFixture OK: selector, fluids, non-consumed catalyst, full 16-slot matching and consumption");
    }

    private static void checkIronChestRegistration() throws Exception {
        Item diamondPlate = net.minecraft.init.Items.diamond;
        net.minecraftforge.oredict.OreDictionary.registerOre("plateGemDiamond", new ItemStack(diamondPlate));
        gregapi.data.IL.Circuit_Selector.set(new ItemStack(net.minecraft.init.Items.clock));
        String[] names = {"goldDiamondUpgrade", "ironGoldUpgrade", "silverGoldUpgrade"};
        java.lang.reflect.Method add = cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry.class
                .getDeclaredMethod("add", int.class, String.class, Object.class, java.util.BitSet.class);
        add.setAccessible(true);
        for (int i = 0; i < names.length; i++) {
            add.invoke(cpw.mods.fml.common.registry.GameData.getItemRegistry(), 30000 + i,
                    "IronChest:" + names[i], new Item(), new java.util.BitSet());
            check(cpw.mods.fml.common.registry.GameRegistry.findItem("IronChest", names[i]) != null,
                    "fixture upgrade item registered: " + names[i]);
        }
        check(com.mod.htgt6.api.hitech6.RM.RecipeMap.gt(gregapi.data.OP.plateGem, gregapi.data.MT.Diamond, 1) != null,
                "fixture crystalline plate registered");
        check(gregapi.data.IL.Circuit_Selector.getWithMeta(0, 4) != null, "fixture selector registered");
        java.lang.reflect.Method replace = com.mod.htgt6.common.handler.recipes.IronChestModifierRecipes.class
                .getDeclaredMethod("replace", String.class, gregapi.oredict.OreDictMaterial.class,
                        boolean.class, String[].class, int[].class,
                        com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings.class,
                        org.apache.logging.log4j.Logger.class);
        replace.setAccessible(true);
        int before = AssemblerRecipeHandler.INSTANCE.getRecipeList().size();
        replace.invoke(null, "goldDiamondUpgrade", gregapi.data.MT.Diamond, false,
                new String[]{"ironGoldUpgrade", "silverGoldUpgrade"}, new int[]{2, 3},
                com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings.DEFAULT,
                org.apache.logging.log4j.LogManager.getLogger("IronChestFixture"));
        check(AssemblerRecipeHandler.INSTANCE.getRecipeList().size() == before + 2,
                "both diamond upgrade recipes register using existing OreDictionary crystalline plate");
        for (int i = before; i < before + 2; i++) {
            MachineRecipe recipe = AssemblerRecipeHandler.INSTANCE.getRecipeList().get(i);
            check(recipe.inputs[1].getItem() == diamondPlate && recipe.inputs[1].stackSize == 12,
                    "diamond upgrade uses 12 existing crystalline plates");
            ItemStack[] inventory = new ItemStack[10];
            for (int slot = 0; slot < 3; slot++) inventory[slot] = recipe.inputs[slot].copy();
            inventory[2].stackSize = 1;
            inventory[1].stackSize = 11;
            check(AssemblerRecipeHandler.INSTANCE.findRecipe(inventory, new FluidTank[0]) == null,
                    "diamond upgrade rejects insufficient plates");
            inventory[1].stackSize = 12;
            check(AssemblerRecipeHandler.INSTANCE.findRecipe(inventory, new FluidTank[0]) == recipe,
                    "registered diamond upgrade is executable");
        }
        java.lang.reflect.Method chestRecipe = com.mod.htgt6.common.handler.recipes.IronChestProgressionRecipes.class
                .getDeclaredMethod("assemblerChest", net.minecraft.block.Block.class, int.class, int.class,
                        gregapi.oredict.OreDictMaterial.class, int.class, org.apache.logging.log4j.Logger.class);
        chestRecipe.setAccessible(true);
        chestRecipe.invoke(null, net.minecraft.init.Blocks.chest, 1, 2, gregapi.data.MT.Diamond, 2,
                org.apache.logging.log4j.LogManager.getLogger("IronChestFixture"));
        MachineRecipe diamond = AssemblerRecipeHandler.INSTANCE.getRecipeList().get(before + 2);
        check(diamond.inputs[1].getItem() == diamondPlate && diamond.inputs[1].stackSize == 6,
                "diamond chest uses six existing plates, without a new double plate");
        check(diamond.inputs[2].getItemDamage() == 4 && diamond.inputs[2].stackSize == 0,
                "chest uses non-consumed selector 4");
        for (int slot = 3; slot < 9; slot++) check(diamond.inputs[slot] == null, "no screws in chest recipe");
        AssemblerRecipeHandler.INSTANCE.getRecipeList().clear();
        System.out.println("IronChest registration OK: diamond upgrade routes and diamond chest using existing GT6 plate");
    }
}
