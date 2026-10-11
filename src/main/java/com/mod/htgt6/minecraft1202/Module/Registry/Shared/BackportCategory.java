package com.mod.htgt6.minecraft1202.Module.Registry.Shared;

import com.mod.htgt6.minecraft1202.Module.Registry.Aquatic.AquaticUpdate;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulUpdate;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatUpdate;
import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationUpdate112;
import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnUpdate;
import com.mod.htgt6.minecraft1202.Module.Registry.Inventory.InventoryUpdate112;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Minecraft120Backport;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageAndPillageUpdate;

/** Ordered, user-visible categories of content owned by Minecraft_1.20.2. */
public enum BackportCategory {
    BOUNTIFUL("1.8 - Bountiful Update") {
        @Override public void initialize() { BountifulUpdate.init(); }
    },
    COMBAT("1.9 - Combat Update") {
        @Override public void initialize() { CombatUpdate.init(); }
    },
    FROSTBURN("1.10 - Frostburn Update") {
        @Override public void initialize() { FrostburnUpdate.init(); }
    },
    EXPLORATION("1.11 - Exploration Update") {
        @Override public void initialize() { ExplorationUpdate112.init(); }
    },
    INVENTORY("1.12 - Inventory and recipe book") {
        @Override public void initialize() { InventoryUpdate112.init(); }
    },
    AQUATIC("1.13 - Update Aquatic") {
        @Override public void initialize() { AquaticUpdate.init(); }
    },
    VILLAGE_AND_PILLAGE("1.14 - Village and Pillage") {
        @Override public void initialize() { VillageAndPillageUpdate.init(); }
    },
    MODERN_1_20_2("1.15-1.20.2 - Modern content and Futura") {
        @Override public void initialize() { Minecraft120Backport.init(); }
    };

    private final String displayName;

    private BackportCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract void initialize();
}
