import com.mod.htgt6.common.crusher.CrusherOreImportFixture;
import com.mod.htgt6.common.GTHooks.MaterialHooks.MaterialFrameBoxes;
import com.mod.htgt6.common.handler.recipe.RM.*;
import com.mod.htgt6.api.hitech6.RM.*;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.data.*;
import gregapi.oredict.*;
import java.util.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraftforge.oredict.OreDictionary;

public final class MultiblockRecipesFixture {
    static Item item(String mod, String name) {
        Loader.instance().activeModContainer().getMetadata().modId = mod;
        Item item = new Item().setHasSubtypes(true);
        GameRegistry.registerItem(item, name);
        return item;
    }
    static void check(boolean pass, String message) { if (!pass) throw new AssertionError(message); }
    static ItemStack stack(String name) { return GameRegistry.findItemStack("HTGT6", name, 1); }
    public static void main(String[] args) throws Exception {
        CrusherOreImportFixture.initializeGT6();
        CS.GAPI.mBeforeInit = new ArrayList<Runnable>();
        MaterialFrameBoxes.register();
        item("gregapi", "gt.integrated_circuit");
        item("gregtech", "gt.multiitem.technological");
        item("IC2", "itemPartCircuitAdv");
        String[] families = {"mining_unit", "chemical_plant", "electronic_factory", "industrial_mixer",
            "flotation_chamber", "steam_turbine", "industrial_centrifuge", "industrial_crusher",
            "fluid_solidifier", "super_massive_compressor"};
        String[] controllers = {"multi_te_mining_unit", "multi_tile_chemical_plant", "te_electronic_factory",
            "industrial_mixer", "flotation_chamber", "steam_turbine", "industrial_centrifuge", "industrial_crusher", "BlockSolidifier"};
        for (String family : families) { item("HTGT6", family + "_casing"); item("HTGT6", family + "_rib"); }
        for (String name : controllers) item("HTGT6", name);
        for (String name : new String[] {"mining_unit_frame", "mining_unit_port", "hullHV", "unbreakable_glass", "SMCompressor", "assembler"}) item("HTGT6", name);
        OreDictMaterial[] materials = {MT.BlackSteel, MT.StainlessSteel, MT.Al, MT.Magnalium, MT.Steel, MT.Invar, MT.Kanthal, MT.Ag};
        for (OreDictPrefix prefix : new OreDictPrefix[] {OP.plate, OP.bolt, OP.stick, OP.stickLong, OP.gearGt, OP.wireGt02}) {
            Item form = item("fixture", prefix.mNameInternal);
            for (OreDictMaterial material : materials) {
                if (prefix == OP.wireGt02 || prefix.isGeneratingItem(material))
                    OreDictionary.registerOre(prefix.mNameInternal + material.mNameInternal, new ItemStack(form, 1, material.mID));
            }
        }
        CraftingManager.getInstance().getRecipeList().clear();
        AssemblerRecipeHandler.INSTANCE.getRecipeList().clear();
        for (String name : controllers) GameRegistry.addShapelessRecipe(stack(name), net.minecraft.init.Items.stick);
        GameRegistry.addShapelessRecipe(stack("assembler"), net.minecraft.init.Items.stick);
        item("HTGT6", "chemical_plant_glass");
        GameRegistry.addShapelessRecipe(stack("unbreakable_glass"), stack("chemical_plant_glass"));
        GameRegistry.addShapelessRecipe(stack("unbreakable_glass"), net.minecraft.init.Items.stick);
        MachineRecipe smc = new MachineRecipe(new ItemStack[] {stack("hullHV")}, new ItemStack[] {stack("SMCompressor")}, null, null, 12000, 84, 2);
        AssemblerRecipeHandler.INSTANCE.addRecipe(smc);
        // Match production loadComplete: the GT6 early-registration API is closed.
        gregapi.api.Abstract_Mod.sStartedPostInit = 1;
        MultiblockAssemblerRecipes.register();
        List<MachineRecipe> recipes = AssemblerRecipeHandler.INSTANCE.getRecipeList();
        check(recipes.contains(smc) && smc.minTier == 2 && smc.euPerTick == 84, "SMC recipe changed");
        check(CraftingManager.getInstance().getRecipeList().size() >= 2, "bootstrap/recovery removed");
        for (Object value : CraftingManager.getInstance().getRecipeList()) {
            ItemStack out = ((IRecipe)value).getRecipeOutput();
            for (String name : controllers) check(!out.isItemEqual(stack(name)), "Controller crafting bypass: " + name);
        }
        int controllerCount = 0, frameCount = 0;
        for (MachineRecipe recipe : recipes) {
            if (recipe == smc) continue;
            for (String name : controllers) if (recipe.outputs[0].isItemEqual(stack(name))) {
                check(recipe.minTier == 3 && recipe.euPerTick == 480, "HV controller gate"); controllerCount++;
            }
            if (recipe.outputs[0].getItem() == MaterialFrameBoxes.INSTANCE) {
                frameCount++;
                ItemStack frame = recipe.outputs[0];
                OreDictMaterial material = OreDictMaterial.MATERIAL_ARRAY[frame.getItemDamage()];
                check(MaterialFrameBoxes.INSTANCE.getOreDictItemData(frame).mMaterial.mMaterial == material, "Frame material data changed");
                check(!OreDictionary.getOres("frameGt" + material.mNameInternal).isEmpty(), "Frame ore registration missing");
                int oreCount = OreDictionary.getOres("frameGt" + material.mNameInternal).size();
                MaterialFrameBoxes.registerMaterial(material);
                check(OreDictionary.getOres("frameGt" + material.mNameInternal).size() == oreCount,
                        "Duplicate late frame registration");
            }
            ItemStack[] inventory = new ItemStack[10];
            for (int i = 0; i < recipe.inputs.length; i++) {
                inventory[i] = recipe.inputs[i].copy();
                if (inventory[i].stackSize == 0) inventory[i].stackSize = 1;
            }
            MachineRecipe found = AssemblerRecipeHandler.INSTANCE.findRecipe(inventory, new net.minecraftforge.fluids.FluidTank[0]);
            check(found == recipe, "Recipe shadowed: " + recipe.outputs[0]);
            int selector = -1;
            for (int i = 0; i < recipe.inputs.length; i++) if (recipe.inputs[i].stackSize == 0) selector = i;
            check(selector >= 0, "Selector missing");
            inventory[selector].setItemDamage(24);
            check(AssemblerRecipeHandler.INSTANCE.findRecipe(inventory, new net.minecraftforge.fluids.FluidTank[0]) == null, "Wrong selector accepted");
            inventory[selector] = recipe.inputs[selector].copy(); inventory[selector].stackSize = 1;
            AssemblerRecipeHandler.INSTANCE.consumeInputs(recipe, inventory);
            check(inventory[selector] != null && inventory[selector].stackSize == 1, "Selector consumed");
            if (recipe.outputs[0].isItemEqual(stack("steam_turbine_casing"))) {
                check(recipe.inputs[0].getItem() == MaterialFrameBoxes.INSTANCE && recipe.inputs[0].getItemDamage() == MT.BlackSteel.mID, "Turbine frame");
                check(recipe.inputs[1].getItemDamage() == MT.Magnalium.mID && recipe.inputs[1].stackSize == 6, "Turbine plates");
                check(recipe.inputs[2].getItemDamage() == MT.BlackSteel.mID && recipe.inputs[2].stackSize == 32, "Turbine bolts");
                check(recipe.inputs[3].getItemDamage() == 6, "Turbine selector");
            }
        }
        check(controllerCount == 9 && frameCount > 0, "Catalog incomplete");
        check(CraftingManager.getInstance().getRecipeList().size() == frameCount + 2, "Old glass bypass or bootstrap/conversion lost");
        int count = recipes.size(); MultiblockAssemblerRecipes.register();
        check(recipes.size() == count, "Duplicate registration");
        System.out.println("Multiblock recipes PASS: " + count + " recipes; HV controllers, MV SMC preserved, frame materials, exact lookup, selectors and idempotency");
    }
}
