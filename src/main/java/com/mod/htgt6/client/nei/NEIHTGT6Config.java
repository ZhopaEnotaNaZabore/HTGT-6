package com.mod.htgt6.client.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import com.mod.htgt6.asm.HTGT6Config;
import com.mod.htgt6.client.inventory112.Inventory112NEIGuiHandler;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class NEIHTGT6Config implements IConfigureNEI {

    @Override
    public void loadConfig() {
        // Create an explicit static link to guarantee initialization
        registerManual();
    }

    // Force-registration fallback method
    public static void registerManual() {
        com.mod.htgt6.orehelper.client.OreHelperNEI.register();
        HTGT6RecipeHandler.registerAll();
        Inventory112NEIGuiHandler.register();
        hideTechnicalBackportItems();
        configureBackportVariants();
        configureMaterialRepresentatives();
        for (Block block : com.mod.htgt6.common.multiblock.glass.ObservationGlassContent.legacy)
            if (block != null) hide(block);

        System.out.println("[HiTech6] NEI integration loaded");
    }

    /** Keep registry companions for old worlds, but never expose them in NEI. */
    private static void hideTechnicalBackportItems() {
        if (!HTGT6Config.RUN_MINECRAFT_1_7_9999) return;

        hide(BountifulContent.standingBanner);
        hide(BountifulContent.wallBanner);
        hide(BountifulContent.doubleRedSandstoneSlab);
        hide(BountifulContent.barrier);
        hide(BountifulContent.leapingPotion);
        for (Block door : BountifulContent.woodDoors) hide(door);

        hide(CombatContent.doublePurpurSlab);
        hide(CombatContent.endGateway);
        hide(CombatContent.frostedIce);
        hide(CombatContent.beetrootCrop);

        hide(Backport120Content.doubleCobbledDeepslateSlab);
        hide(Backport120Content.doublePolishedDeepslateSlab);
        hide(Backport120Content.doubleDeepslateBrickSlab);
        hide(Backport120Content.doubleDeepslateTileSlab);
        hide(Backport120Content.doubleCherrySlab);
        hide(Backport120Content.doubleMudBrickSlab);
        hide(Backport120Content.cherryDoorBlock);
        hide(Backport120Content.cherryStandingSign);
        hide(Backport120Content.cherryWallSign);
        hide(Backport120Content.cherryHangingSignBlock);
        hide(Backport120Content.cherryWallHangingSignBlock);
        hide(Backport120Content.chainBlock);
        hide(Backport120Content.aquiferWater);
    }

    private static void hide(Block technicalBlock) {
        hide(Item.getItemFromBlock(technicalBlock));
    }

    private static void configureBackportVariants() {
        if (!HTGT6Config.RUN_MINECRAFT_1_7_9999) return;
        for (Object value : Item.itemRegistry) {
            Item item = (Item) value;
            if (!BackportItemVisibility.isBackport(item)) continue;
            if (BackportItemVisibility.isTechnical(item)) {
                hide(item);
            } else {
                java.util.List<ItemStack> variants = BackportItemVisibility.publicVariants(item);
                // An empty override triggers NEI's damageSearch, so hide the whole item instead.
                if (variants.isEmpty()) hide(item);
                else API.setItemListEntries(item, variants);
            }
        }
    }

    /** Limit the NEI catalog only; all material variants remain valid in recipes and worlds. */
    private static void configureMaterialRepresentatives() {
        singleVariant(com.mod.htgt6.common.GTHooks.MaterialHooks.MaterialFrameBoxes.INSTANCE);
        singleVariant(com.mod.htgt6.common.GTHooks.TurbineRotorHooks.GT6TurbineRotorHooks.ITEM);
        singleVariant(Item.getItemFromBlock(com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.GT6DeepslateOreIntegration.normal));
        singleVariant(Item.getItemFromBlock(com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.GT6DeepslateOreIntegration.broken));
        singleVariant(Item.getItemFromBlock(com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.GT6DeepslateOreIntegration.small));
    }

    private static void singleVariant(Item item) {
        if (item == null || Item.itemRegistry.getNameForObject(item) == null) return;
        java.util.List<ItemStack> variants = new java.util.ArrayList<ItemStack>();
        item.getSubItems(item, item.getCreativeTab(), variants);
        // Metadata zero is not necessarily a valid material. Keep the first real variant.
        if (!variants.isEmpty()) {
            API.setItemListEntries(item, java.util.Collections.singletonList(variants.get(0).copy()));
        }
    }

    private static void hide(Item technicalItem) {
        if (technicalItem != null) {
            // NEI's ItemStackSet treats wildcard damage as the complete item.
            // Hiding metadata 0 alone leaves its automatically probed 1..15
            // variants visible as untranslated/debug stacks.
            API.hideItem(new ItemStack(technicalItem, 1, OreDictionary.WILDCARD_VALUE));
        }
    }

    @Override
    public String getName() {
        return "HTGT6 Core Plugins";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}
