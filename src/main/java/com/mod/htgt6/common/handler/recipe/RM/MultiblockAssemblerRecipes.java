package com.mod.htgt6.common.handler.recipe.RM;

import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.api.hitech6.RM.RecipeMap;
import com.mod.htgt6.common.GTHooks.MaterialHooks.MaterialFrameBoxes;
import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.oredict.OreDictMaterial;
import java.util.*;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapedOreRecipe;

/** Authored after script migration, so old controller recipes cannot bypass the tier gate. */
public final class MultiblockAssemblerRecipes {
    private static boolean registered;
    private MultiblockAssemblerRecipes() { }

    public static void register() {
        if (registered) return;
        List<MachineRecipe> recipes = new ArrayList<MachineRecipe>();
        family(recipes, "multi_te_mining_unit", "mining_unit", MT.BlackSteel, MT.StainlessSteel, 1, 12063);
        family(recipes, "multi_tile_chemical_plant", "chemical_plant", MT.StainlessSteel, MT.StainlessSteel, 2, 12023);
        family(recipes, "te_electronic_factory", "electronic_factory", MT.StainlessSteel, MT.Al, 3, 12083);
        family(recipes, "industrial_mixer", "industrial_mixer", MT.StainlessSteel, MT.Steel, 4, 12003);
        family(recipes, "flotation_chamber", "flotation_chamber", MT.StainlessSteel, MT.Magnalium, 5, 12023);
        family(recipes, "steam_turbine", "steam_turbine", MT.BlackSteel, MT.Magnalium, 6, 12003);
        family(recipes, "industrial_centrifuge", "industrial_centrifuge", MT.StainlessSteel, MT.StainlessSteel, 7, 12003);
        family(recipes, "industrial_crusher", "industrial_crusher", MT.BlackSteel, MT.BlackSteel, 8, 12063);
        family(recipes, "BlockSolidifier", "fluid_solidifier", MT.StainlessSteel, MT.Invar, 9, 12023);
        // Existing SMCompressor controller recipe is deliberately outside this catalog.
        structure(recipes, "super_massive_compressor", MT.Al, MT.Al, 10, 2);

        add(recipes, stack("chemical_plant_casing", 1, 1), 200, 3,
                stack("chemical_plant_casing", 1, 0), gt(OP.plate, MT.Invar, 4), gt(OP.bolt, MT.StainlessSteel, 8), tag(11));
        add(recipes, stack("mining_unit_casing", 1, 1), 200, 3,
                stack("mining_unit_casing", 1, 0), item("IC2", "itemPartCircuitAdv", 1, 0), part(12103, 1), tag(12));
        add(recipes, stack("mining_unit_frame", 1, 0), 100, 3,
                frame(MT.BlackSteel), gt(OP.bolt, MT.BlackSteel, 8), tag(13));
        add(recipes, stack("mining_unit_frame", 4, 1), 100, 3,
                stack("mining_unit_frame", 1, 0), tag(14));
        // Shared structural glass/ports must be available for the MV SMC.
        add(recipes, stack("unbreakable_glass", 4, 0), 200, 2,
                frame(MT.Al), new ItemStack(Blocks.glass, 4), gt(OP.plate, MT.Steel, 4), gt(OP.bolt, MT.Al, 16), tag(15));
        for (int meta = 0; meta <= 9; meta++) {
            int tanks = meta == 5 || meta == 6 ? 4 : meta == 7 || meta == 8 ? 8 : 1;
            int component = meta == 2 || meta == 3 ? 12042 : meta == 4 || meta == 9 ? 12002 : 12022;
            add(recipes, stack("mining_unit_port", 1, meta), 200, 2,
                    frame(MT.Al), gt(OP.plate, MT.Al, 4), part(component, tanks),
                    gt(OP.wireGt02, MT.Ag, 2), part(30302, 1), tag(meta + 1));
        }

        // Resolve every ingredient before removing any previous recipe.
        for (MachineRecipe recipe : recipes) validate(recipe);
        Set<String> replaceCrafting = new HashSet<String>();
        for (MachineRecipe recipe : recipes) {
            ItemStack output = recipe.outputs[0];
            replaceCrafting.add(key(output));
        }
        Iterator<?> crafting = CraftingManager.getInstance().getRecipeList().iterator();
        while (crafting.hasNext()) {
            IRecipe recipe = (IRecipe)crafting.next();
            ItemStack out = recipe.getRecipeOutput();
            if (out != null && replaceCrafting.contains(key(out)) && !legacyGlassConversion(recipe)) crafting.remove();
        }
        Set<String> replaced = new HashSet<String>();
        for (MachineRecipe recipe : recipes) replaced.add(key(recipe.outputs[0]));
        Iterator<MachineRecipe> old = AssemblerRecipeHandler.INSTANCE.getRecipeList().iterator();
        while (old.hasNext()) {
            MachineRecipe recipe = old.next();
            for (ItemStack output : recipe.outputs) if (output != null && replaced.contains(key(output))) {
                old.remove(); break;
            }
        }
        for (MachineRecipe recipe : recipes) AssemblerRecipeHandler.INSTANCE.addRecipe(recipe);
        registerFrames();
        registered = true;
    }

    private static void family(List<MachineRecipe> recipes, String controller, String name,
            OreDictMaterial frame, OreDictMaterial plate, int tag, int component) {
        structure(recipes, name, frame, plate, tag, 3);
        add(recipes, stack(controller, 1, 0), 1200, 3,
                stack(name + "_casing", 8, 0), stack(name + "_rib", 4, 0), stack("hullHV", 1, 0),
                item("IC2", "itemPartCircuitAdv", 4, 0), part(12003, 4), part(component, 2),
                gt(OP.wireGt02, MT.Kanthal, 8), gt(OP.gearGt, frame, 4), tag(tag));
    }

    private static void structure(List<MachineRecipe> recipes, String name, OreDictMaterial frame,
            OreDictMaterial plate, int tag, int tier) {
        add(recipes, stack(name + "_casing", 1, 0), 200, tier,
                frame(frame), gt(OP.plate, plate, 6), gt(OP.bolt, frame, 32), tag(tag));
        add(recipes, stack(name + "_rib", 4, 0), 100, tier,
                stack(name + "_casing", 1, 0), gt(OP.stickLong, frame, 4), gt(OP.bolt, frame, 8), tag(tag));
    }

    private static void registerFrames() {
        if (MaterialFrameBoxes.INSTANCE == null) throw new IllegalStateException("Frame boxes not registered");
        for (OreDictMaterial material : OreDictMaterial.MATERIAL_ARRAY) {
            if (material == null || !OP.frameGt.isGeneratingItem(material)) continue;
            ItemStack rods = RecipeMap.gt(OP.stick, material, 8);
            if (rods == null) continue;
            MaterialFrameBoxes.registerMaterial(material);
            ItemStack output = frame(material);
            GameRegistry.addRecipe(new ShapedOreRecipe(output, "RRR", "RwR", "RRR",
                    'R', OP.stick.mNameInternal + material.mNameInternal, 'w', "craftingToolWrench"));
            AssemblerRecipeHandler.addRecipe(new ItemStack[] {rods, tag(4)}, output, 20, 8, 1);
        }
    }

    private static boolean legacyGlassConversion(IRecipe recipe) {
        if (!(recipe instanceof net.minecraft.item.crafting.ShapelessRecipes)
                || !recipe.getRecipeOutput().isItemEqual(stack("unbreakable_glass", 1, 0))) return false;
        List<?> inputs = ((net.minecraft.item.crafting.ShapelessRecipes)recipe).recipeItems;
        if (inputs.size() != 1 || !(inputs.get(0) instanceof ItemStack)) return false;
        ItemStack input = (ItemStack)inputs.get(0);
        for (String name : new String[] {"chemical_plant_glass", "electronic_factory_glass", "industrial_mixer_glass",
                "industrial_centrifuge_glass", "super_massive_compressor_glass", "fluid_solidifier_glass", "formed_mining_unit_glass"}) {
            ItemStack legacy = GameRegistry.findItemStack("HTGT6", name, 1);
            if (legacy != null && input.getItem() == legacy.getItem()) return true;
        }
        return false;
    }

    private static ItemStack frame(OreDictMaterial material) {
        if (MaterialFrameBoxes.INSTANCE == null || !OP.frameGt.isGeneratingItem(material))
            throw new IllegalStateException("Missing frame box: " + material.mNameInternal);
        gt(OP.stick, material, 8); // A required frame must itself be obtainable.
        return new ItemStack(MaterialFrameBoxes.INSTANCE, 1, material.mID);
    }
    private static ItemStack gt(gregapi.oredict.OreDictPrefix prefix, OreDictMaterial material, int count) {
        ItemStack stack = RecipeMap.gt(prefix, material, count);
        if (stack == null) throw new IllegalStateException("Missing multiblock ingredient: " + prefix + material.mNameInternal);
        return stack;
    }
    private static ItemStack tag(int value) { return item("gregapi", "gt.integrated_circuit", 0, value); }
    private static ItemStack part(int meta, int count) { return item("gregtech", "gt.multiitem.technological", count, meta); }
    private static ItemStack stack(String name, int count, int meta) { return item("HTGT6", name, count, meta); }
    private static ItemStack item(String mod, String name, int count, int meta) {
        ItemStack stack = GameRegistry.findItemStack(mod, name, 1);
        if (stack == null) throw new IllegalStateException("Missing multiblock item: " + mod + ":" + name);
        stack.stackSize = count;
        stack.setItemDamage(meta);
        return stack;
    }
    private static String key(ItemStack stack) { return String.valueOf(net.minecraft.item.Item.itemRegistry.getNameForObject(stack.getItem())) + ":" + stack.getItemDamage(); }
    private static void add(List<MachineRecipe> recipes, ItemStack output, int ticks, int tier, ItemStack... inputs) {
        recipes.add(new MachineRecipe(inputs, new ItemStack[] {output}, null, null, ticks, tier == 2 ? 120 : 480, tier));
    }
    private static void validate(MachineRecipe recipe) {
        if (recipe.inputs.length > 9) throw new IllegalStateException("Multiblock recipe exceeds assembler grid");
        for (ItemStack input : recipe.inputs) if (input == null || input.getItem() == null || input.stackSize < 0 || input.stackSize > 64)
            throw new IllegalStateException("Invalid multiblock ingredient");
    }
}
