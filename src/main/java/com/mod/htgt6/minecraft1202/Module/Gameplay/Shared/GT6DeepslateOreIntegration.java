package com.mod.htgt6.minecraft1202.Module.Gameplay.Shared;

import com.mod.htgt6.minecraft1202.Module.Blocks.Shared.BlockGT6DeepslateOre;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.World.Shared.PherithiumDeepslateWorldGenerator;
import com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator;
import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.block.behaviors.Drops;
import gregapi.block.behaviors.Drops_SmallOre;
import gregapi.block.prefixblock.PrefixBlock;
import gregapi.code.ItemStackContainer;
import gregapi.data.CS;
import gregapi.data.OP;
import gregapi.data.TC;
import gregapi.data.TD;
import gregapi.oredict.OreDictPrefix;
import gregapi.util.OM;

/** Registers deepslate as a first-class GT6 ore host without modifying the GT6 jar. */
public final class GT6DeepslateOreIntegration {
    /** Dedicated OreDictionary prefix, producing names such as oreDeepslatePherithium. */
    public static OreDictPrefix oreDeepslate;
    public static BlockGT6DeepslateOre normal;
    public static BlockGT6DeepslateOre broken;
    public static BlockGT6DeepslateOre small;

    private static boolean registered;

    public static synchronized void register() {
        if (registered) return;

        oreDeepslate = createDeepslatePrefix();
        normal = new BlockGT6DeepslateOre("htgt6.meta.ore.normal.deepslate",
                oreDeepslate, null, Backport120Content.deepslate, 3.0F, 6.0F, 0);
        broken = new BlockGT6DeepslateOre("htgt6.meta.ore.broken.deepslate",
                oreDeepslate, null, Backport120Content.cobbledDeepslate, 1.5F, 3.0F, -1);
        small = new BlockGT6DeepslateOre("htgt6.meta.ore.small.deepslate",
                OP.oreSmall, new Drops_SmallOre(GT6MaterialAutoGenerator.DEEPSLATE),
                Backport120Content.deepslate,
                3.0F, 6.0F, -1);
        normal.mDrops = new Drops(broken, normal);

        // Deepslate is axis-aware in HTGT6. Register every possible metadata so GT6
        // worldgen always selects this family, including blocks placed by commands/mods.
        for (int metadata = 0; metadata < 16; ++metadata) {
            ItemStackContainer host = new ItemStackContainer(Backport120Content.deepslate, 1L, metadata);
            CS.BlocksGT.stoneToNormalOres.put(host, normal);
            CS.BlocksGT.stoneToBrokenOres.put(host, broken);
            CS.BlocksGT.stoneToSmallOres.put(host, small);
        }

        CS.BlocksGT.stoneOverridable.add(normal);
        CS.BlocksGT.stoneOverridable.add(broken);
        CS.BlocksGT.stoneOverridable.add(small);
        blacklist(normal);
        blacklist(broken);
        blacklist(small);
        GameRegistry.registerWorldGenerator(new PherithiumDeepslateWorldGenerator(), 10);
        registered = true;
    }

    private static OreDictPrefix createDeepslatePrefix() {
        OreDictPrefix prefix = OreDictPrefix.createPrefix("oreDeepslate")
                .setCategoryName("Deepslate Ores")
                .setLocalItemName("Deepslate", " Ore")
                .setCondition(TD.ItemGenerator.ORES)
                .add(TD.Prefix.UNIFICATABLE, TD.Prefix.ORE, TD.Prefix.UNIFICATABLE_RECIPES,
                        TD.Prefix.MATERIAL_BASED, TD.Prefix.BLOCK_BASED,
                        TD.Prefix.TOOLTIP_ENCHANTS, TD.Prefix.STANDARD_ORE)
                .aspects(TC.TERRA, 1L)
                // Reuse GT6's ore overlay from every selected material TextureSet.
                .setTextureSetName("ore");
        prefix.mByProducts.add(OM.stack(GT6MaterialAutoGenerator.DEEPSLATE, OP.dust.mAmount));
        return prefix;
    }

    private static void blacklist(PrefixBlock block) {
        for (int metadata = 0; metadata < 10; ++metadata) {
            CS.GarbageGT.BLACKLIST.add(block, metadata);
            CS.ItemsGT.ILLEGAL_DROPS.add(block, metadata);
        }
    }

    private GT6DeepslateOreIntegration() { }
}
