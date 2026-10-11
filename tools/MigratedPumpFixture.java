import com.mod.htgt6.common.crusher.CrusherOreImportFixture;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.*;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.crafting3x3.*;
import com.mod.htgt6.common.handler.recipe.RM.*;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.inventory.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;

/** Exercises the authored pump grid and its real assembler importer in an isolated registry. */
public class MigratedPumpFixture {
    static Item item(String mod, String name) {
        Loader.instance().activeModContainer().getMetadata().modId = mod;
        Item item = new Item().setHasSubtypes(true);
        GameRegistry.registerItem(item, name);
        return item;
    }
    static ItemStack stack(Item item, int meta) { return new ItemStack(item, 1, meta); }
    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        World world = CrusherOreImportFixture.initializeGT6();
        check(gregapi.data.OP.ring.isGeneratingItem(gregapi.data.MT.WOODS.Oak),
                "GT6 does not generate the oak rings required by the migrated pump");
        Item parts = item("gregtech", "gt.multiitem.technological");
        Item machine = item("gregtech", "gt.multitileentity");
        Item screw = item("gregtech", "gt.meta.screw");
        Item rotor = item("gregtech", "gt.meta.rotor");
        Item ring = item("gregtech", "gt.meta.ring");
        Item tools = item("fixture", "tools");
        Item selector = item("gregapi", "gt.integrated_circuit");
        OreDictionary.registerOre("craftingToolWrench", stack(tools, 0));
        OreDictionary.registerOre("craftingToolScrewdriver", stack(tools, 1));
        OreDictionary.registerOre("ingotGoldInductive", stack(tools, 2));
        check(GeneratedValueResolver.matchingItems("<ore:*GoldInductive>", "fixture",
                GeneratedRecipeSettings.DEFAULT, LogManager.getLogger("PumpFixture")).size()==1,
                "inductive alloy wildcard resolution");
        CraftingManager.getInstance().getRecipeList().clear();
        CraftingFromGregtechparts.register(GeneratedRecipeSettings.DEFAULT, LogManager.getLogger("PumpFixture"));
        InventoryCrafting grid = new InventoryCrafting(new Container() {
            public boolean canInteractWith(EntityPlayer player) { return true; }
        }, 3, 3);
        ItemStack[] input = {stack(screw,8667),stack(rotor,8667),stack(ring,9300),
                stack(tools,0),stack(machine,26102),stack(tools,1),
                stack(ring,9300),stack(parts,12000),stack(machine,28116)};
        for (int i=0;i<9;i++) grid.setInventorySlotContents(i,input[i]);
        ItemStack result = CraftingManager.getInstance().findMatchingRecipe(grid,world);
        check(result != null && result.isItemEqual(stack(parts,12020)), "authored ULV pump grid missing");
        grid.setInventorySlotContents(4,stack(machine,26142));
        check(CraftingManager.getInstance().findMatchingRecipe(grid,world)==null,"wrong pipe accepted");
        AssemblerRecipeHandler.INSTANCE.getRecipeList().clear();
        ComponentAssemblerRecipes.register();
        ItemStack[] inventory = {stack(screw,8667),stack(rotor,8667),new ItemStack(ring,2,9300),
                stack(machine,26102),stack(parts,12000),stack(machine,28116),null,null,stack(selector,23),null};
        com.mod.htgt6.api.hitech6.RM.MachineRecipe recipe = AssemblerRecipeHandler.INSTANCE.findRecipe(
                inventory,new net.minecraftforge.fluids.FluidTank[0]);
        check(recipe != null && recipe.outputs[0].isItemEqual(stack(parts,12020)),"ULV pump assembler import missing");
        check(recipe.duration==20 && recipe.euPerTick==8,"ULV pump assembler settings");
        System.out.println("Migrated ULV pump PASS: original grid, wrong pipe rejection, two oak rings, tool-free assembler, selector 23, 20 ticks / 8 EU/t");
    }
}
