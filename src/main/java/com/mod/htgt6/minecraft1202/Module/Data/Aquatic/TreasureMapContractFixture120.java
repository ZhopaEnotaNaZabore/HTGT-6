package com.mod.htgt6.minecraft1202.Module.Data.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemTreasureMapScroll120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.AquaticChestLoot120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.BuriedTreasureHandler;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMap120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.WorldGenOceanRuin;

import java.util.HashSet;
import java.util.Set;

/** Cheap regression contract derived from the official 1.20.2 Aquatic loot tables. */
public final class TreasureMapContractFixture120 {
    public static void assertContract() {
        require(VanillaTreasureMap120.MAP_SCALE == 1, "exploration map zoom must be 1");
        require(BuriedTreasureHandler.EXPLORATION_MAP_SEARCH_RADIUS == 50,
                "exploration_map search radius must be 50 chunks");
        require(VanillaTreasureMap120.RED_X_ICON == 7,
                "the 1.20.2 RED_X sprite must occupy the reserved 1.7 atlas slot");
        require(OceanContent.treasureMapScroll instanceof ItemTreasureMapScroll120
                        && OceanContent.treasureMapScroll.getItemStackLimit() == 1,
                "sealed treasure-map scroll registration");
        try {
            Class.forName("com.mod.htgt6.common.handler.worldgeneration.oceancontent.ItemAquaticTreasureMap");
            throw new IllegalStateException("legacy custom treasure-map item is still packaged");
        } catch (ClassNotFoundException expected) {
            // The opened result must use vanilla Items.filled_map; the sealed loot is not a map implementation.
        }
        require(AquaticChestLoot120.SHIPWRECK_MAP_MISC_WEIGHT == 38,
                "shipwreck map misc pool must have total weight 38");
        require(AquaticChestLoot120.COAST_TRIM_DENOMINATOR == 6,
                "coast trim chance must be 1/6");
        require(AquaticChestLoot120.SHIPWRECK_SUPPLY_WEIGHT == 84,
                "shipwreck supply pool must have total weight 84");
        require(AquaticChestLoot120.SHIPWRECK_TREASURE_MAIN_WEIGHT == 150
                        && AquaticChestLoot120.SHIPWRECK_TREASURE_NUGGET_WEIGHT == 80,
                "shipwreck treasure pool weights must be 150 and 80");
        require(AquaticChestLoot120.BURIED_TREASURE_COMMON_WEIGHT == 35
                        && AquaticChestLoot120.BURIED_TREASURE_RARE_WEIGHT == 15,
                "buried treasure pool weights must be 35 and 15");
        require(WorldGenOceanRuin.BIG_MAP_WEIGHT == 10
                        && WorldGenOceanRuin.BIG_SECOND_POOL_WEIGHT == 23,
                "underwater_ruin_big map weight must be 10/23");
        require(WorldGenOceanRuin.SMALL_MAP_WEIGHT == 5
                        && WorldGenOceanRuin.SMALL_SECOND_POOL_WEIGHT == 12,
                "underwater_ruin_small map weight must be 5/12");

        Set<String> big = new HashSet<String>();
        for (String name : WorldGenOceanRuin.COLD_BIG) big.add(name);
        for (String name : WorldGenOceanRuin.WARM_BIG) big.add(name);
        require(big.size() == 16, "expected all 16 official big ruin templates");
        for (String name : big) require(WorldGenOceanRuin.isBigTemplateName(name),
                "big ruin was classified as small: " + name);
        for (String name : WorldGenOceanRuin.COLD_SMALL) require(!WorldGenOceanRuin.isBigTemplateName(name),
                "small cold ruin was classified as big: " + name);
        for (String name : WorldGenOceanRuin.WARM_SMALL) require(!WorldGenOceanRuin.isBigTemplateName(name),
                "small warm ruin was classified as big: " + name);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    public static void main(String[] ignored) {
        assertContract();
        System.out.println("Treasure map 1.20.2 contract OK");
    }

    private TreasureMapContractFixture120() { }
}
