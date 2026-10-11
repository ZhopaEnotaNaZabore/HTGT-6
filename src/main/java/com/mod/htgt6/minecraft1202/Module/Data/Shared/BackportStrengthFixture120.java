package com.mod.htgt6.minecraft1202.Module.Data.Shared;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockRedSandstone;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;
import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

/** Values copied from the official Java 1.20.2 Blocks and Items registrations. */
public final class BackportStrengthFixture120 {
    private static final float EPSILON = 0.0002F;

    public static void main(String[] args) {
        com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID = 24;
        com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID = 25;
        com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID = 26;
        com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID = 27;
        assertContract();
        System.out.println("Backport 1.20.2 strength fixture OK: block hardness/resistance and item durability");
    }

    public static void assertContract() {
        block("coarse dirt", BountifulContent.coarseDirt, 0.5F, 0.5F);
        block("sponge", BountifulContent.sponge, 0.6F, 0.6F);
        block("sea lantern", BountifulContent.seaLantern, 0.3F, 0.3F);
        block("iron trapdoor", BountifulContent.ironTrapdoor, 5.0F, 5.0F);
        block("red sandstone slab", BountifulContent.redSandstoneSlab, 2.0F, 6.0F);
        value("red sandstone hardness", BlockRedSandstone.hardnessForMetadata(0), 0.8F);
        value("red sandstone resistance", BlockRedSandstone.resistanceForMetadata(0), 0.8F);
        value("cut red sandstone hardness", BlockRedSandstone.hardnessForMetadata(2), 0.8F);
        value("cut red sandstone resistance", BlockRedSandstone.resistanceForMetadata(2), 0.8F);

        block("purpur", CombatContent.purpurBlock, 1.5F, 6.0F);
        block("end stone bricks", CombatContent.endBricks, 3.0F, 9.0F);
        block("chorus plant", CombatContent.chorusPlant, 0.4F, 0.4F);
        block("chorus flower", CombatContent.chorusFlower, 0.4F, 0.4F);
        block("frosted ice", CombatContent.frostedIce, 0.5F, 0.5F);

        block("magma", FrostburnContent.magmaBlock, 0.5F, 0.5F);
        block("nether wart block", FrostburnContent.netherWartBlock, 1.0F, 1.0F);
        block("red nether bricks", FrostburnContent.redNetherBrick, 2.0F, 6.0F);
        block("bone block", FrostburnContent.boneBlock, 2.0F, 2.0F);
        block("observer", ExplorationContent112.observer, 3.0F, 3.0F);
        block("shulker box", ExplorationContent112.shulkerBoxes[0], 2.0F, 2.0F);
        block("lantern", VillagePillageContent.lantern, 3.5F, 3.5F);
        block("turtle egg", OceanContent.turtleEgg, 0.5F, 0.5F);
        block("coral block", OceanContent.coralBlock, 1.5F, 6.0F);

        block("blue ice", Backport120Content.blueIce, 2.8F, 2.8F);
        block("polished basalt", Backport120Content.polishedBasalt, 1.25F, 4.2F);
        block("reinforced deepslate", Backport120Content.reinforcedDeepslate, 55.0F, 1200.0F);
        block("sculk", Backport120Content.sculk, 0.2F, 0.2F);
        block("sculk vein", Backport120Content.sculkVein, 0.2F, 0.2F);
        block("sculk sensor", Backport120Content.sculkSensor, 1.5F, 1.5F);
        block("sculk catalyst", Backport120Content.sculkCatalyst, 3.0F, 3.0F);
        block("sculk shrieker", Backport120Content.sculkShrieker, 3.0F, 3.0F);
        block("powder snow", Backport120Content.powderSnow, 0.25F, 0.25F);
        block("bamboo", Backport120Content.bamboo, 1.0F, 1.0F);
        block("cherry leaves", Backport120Content.cherryLeaves, 0.2F, 0.2F);
        block("mangrove leaves", Backport120Content.mangroveLeaves, 0.2F, 0.2F);
        block("big dripleaf", Backport120Content.bigDripleaf, 0.1F, 0.1F);

        durability("crossbow", VillagePillageContent.crossbow, 465);
        durability("shield", CombatContent.shield, 336);
        durability("elytra", CombatContent.elytra, 432);
        durability("trident", OceanContent.trident, 250);
        durability("turtle helmet", OceanContent.turtleHelmet, 275);
        durability("brush", Backport120Content.brush, 64);
    }

    private static void block(String name, Block block, float hardness, float resistance) {
        value(name + " hardness", block.getBlockHardness(null, 0, 0, 0), hardness);
        value(name + " resistance", block.getExplosionResistance(null), resistance);
    }

    private static void durability(String name, Item item, int durability) {
        if (item.getMaxDamage() != durability)
            throw new IllegalStateException(name + " durability: " + item.getMaxDamage() + " != " + durability);
    }

    private static void value(String name, float actual, float expected) {
        if (Math.abs(actual - expected) > EPSILON)
            throw new IllegalStateException(name + ": " + actual + " != " + expected);
    }

    private BackportStrengthFixture120() { }
}
