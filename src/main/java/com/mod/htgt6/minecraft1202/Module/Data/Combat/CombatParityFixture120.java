package com.mod.htgt6.minecraft1202.Module.Data.Combat;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatPotionVariants120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatRules120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatBrewing120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.EnchantmentCombat;
import com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemCombatBoat;
import com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemCombatSpawnEgg;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityDragonBreathFireball;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityLingeringCloud;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.World.Combat.EndCityGenerator;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockCombatCommand;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockDragonHead;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatBoatSeat;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.ModelDragonHead120;
import com.mod.htgt6.asm.HTGT6Config;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Survival-content and mechanics gate for the Java 1.9 layer against 1.20.2. */
public final class CombatParityFixture120 {
    public static void main(String[] args) throws Exception {
        require(args.length == 2, "expected resources and the 1.20.2 client");
        // Standalone Forge has only 32 potion slots; production reads the stable expanded-registry values from cfg.
        HTGT6Config.GLOWING_POTION_ID = 24; HTGT6Config.LEVITATION_POTION_ID = 25;
        HTGT6Config.LUCK_POTION_ID = 26; HTGT6Config.UNLUCK_POTION_ID = 27;
        verifyRegistry();
        verifyEnchantmentsAndEffects();
        verifyCombatRules();
        verifyPotionCarriers();
        verifyAssets(new File(args[0]), new File(args[1]));
        System.out.println("Combat 1.9 -> 1.20.2 covered contract OK: registrations, IDs, recipes, effects and assets");
    }

    private static void verifyRegistry() throws Exception {
        require(CombatContent.woodBoats.length == 5, "five post-oak wood boats");
        for (int i = 0; i < CombatContent.woodBoats.length; ++i) {
            require(CombatContent.woodBoats[i] instanceof ItemCombatBoat, CombatContent.BOAT_TYPES[i + 1] + " boat");
            require(CombatContent.boatItem(i + 1) == CombatContent.woodBoats[i], "boat variant mapping " + (i + 1));
        }
        require(CombatContent.boatItem(0) == Items.boat, "legacy oak boat ownership");
        require(CombatContent.shulkerSpawnEgg instanceof ItemCombatSpawnEgg, "mod-local Shulker spawn egg");
        require(CombatContent.chainCommandBlock instanceof BlockCombatCommand
                && CombatContent.repeatingCommandBlock instanceof BlockCombatCommand,
                "chain and repeating command blocks");
        require(CombatContent.dragonHead instanceof BlockDragonHead
                && EndCityGenerator.shipTemplateHasDragonHead(), "Dragon Head preserved on End Ship");
        require(new ModelDragonHead120() != null, "1.7-compatible Dragon Head model construction");
        require(EntityCombatBoatSeat.class.getSuperclass() == net.minecraft.entity.Entity.class,
                "second boat passenger anchor");
        ItemStack elytra = new ItemStack(CombatContent.elytra, 1, 1);
        require(CombatContent.elytra.getIsRepairable(elytra, new ItemStack(OceanContent.phantomMembrane)),
                "Elytra repairs with phantom membrane");
        require(!CombatContent.elytra.getIsRepairable(elytra, new ItemStack(Items.leather)),
                "Elytra no longer repairs with leather");
        require(EntityDragonBreathFireball.class.getSuperclass() == net.minecraft.entity.projectile.EntityFireball.class,
                "Dragon fireball entity");
        require(EntityLingeringCloud.class.getMethod("dragonBreath", net.minecraft.world.World.class,
                double.class, double.class, double.class) != null, "collectible dragon-breath cloud");
        int splashDamage = CombatPotionVariants120.values()[0] | 16384;
        require(CombatContent.dragonBreath.isPotionIngredient(new ItemStack(CombatContent.dragonBreath))
                && CombatBrewing120.lingeringDamage(splashDamage) == (splashDamage & ~16384)
                && CombatBrewing120.lingeringDamage(splashDamage & ~16384) == -1,
                "Dragon's Breath brewing-stand conversion");
    }

    private static void verifyEnchantmentsAndEffects() {
        Set<Integer> potionIds = new HashSet<Integer>();
        potionIds.add(Integer.valueOf(CombatContent.glowing.id)); potionIds.add(Integer.valueOf(CombatContent.levitation.id));
        potionIds.add(Integer.valueOf(CombatContent.luck.id)); potionIds.add(Integer.valueOf(CombatContent.unluck.id));
        require(potionIds.size() == 4, "four collision-free configured potion IDs");
        require(CombatContent.frostWalker.effectId == 9 && CombatContent.mending.effectId == 70,
                "stable 1.9 enchantment IDs");
        EnchantmentCombat frost = (EnchantmentCombat) CombatContent.frostWalker;
        EnchantmentCombat mending = (EnchantmentCombat) CombatContent.mending;
        require(frost.getMinEnchantability(1) == 10 && frost.getMinEnchantability(2) == 20
                && frost.getMaxEnchantability(2) == 35, "Frost Walker costs");
        require(mending.getMinEnchantability(1) == 25 && mending.getMaxEnchantability(1) == 75,
                "Mending costs");
        require(frost.isTreasure() && mending.isTreasure(), "treasure-only enchantments");
        require(!frost.canApplyTogether(com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.depthStrider),
                "Frost Walker conflicts with Depth Strider");
        require(!mending.canApplyTogether(net.minecraft.enchantment.Enchantment.infinity),
                "Mending conflicts with Infinity");
    }

    private static void verifyCombatRules() {
        close(CombatRules120.damageAfterAbsorb(10.0F, 20.0F, 0.0F), 4.0F, "full iron armour");
        close(CombatRules120.damageAfterAbsorb(20.0F, 20.0F, 8.0F), 8.0F, "full diamond toughness");
        close(CombatRules120.damageAfterAbsorb(20F,20F,12F),7.2F,"netherite toughness");
    }

    private static void verifyPotionCarriers() {
        int[] values = CombatPotionVariants120.values();
        require(values.length > 8, "all strong and extended potion carriers");
        Set<Integer> unique = new HashSet<Integer>();
        for (int value : values) {
            require((value & 8192) != 0 && net.minecraft.potion.PotionHelper.getPotionEffects(value, false) != null,
                    "valid potion metadata " + value);
            unique.add(Integer.valueOf(value));
        }
        require(unique.size() == values.length, "unique potion carrier metadata");
    }

    private static void verifyAssets(File resources, File modernJar) throws Exception {
        ZipFile modern = new ZipFile(modernJar);
        try {
            String[] woods = {"spruce", "birch", "jungle", "acacia", "dark_oak"};
            for (int index = 0; index < woods.length; index++) {
                String wood = woods[index];
                compare(modern, new File(resources, "assets/htgt6/textures/items/combat_update/" + wood + "_boat.png"),
                        "assets/minecraft/textures/item/" + wood + "_boat.png");
                compare(modern, new File(resources,
                        "assets/htgt6/textures/entity/combat_update/boat/" + wood + ".png"),
                        "assets/minecraft/textures/entity/boat/" + wood + ".png");
            }
            for (String arrow : new String[]{"tipped_arrow", "spectral_arrow"})
                compare(modern, new File(resources, "assets/htgt6/textures/entity/combat_update/projectiles/" + arrow + ".png"),
                        "assets/minecraft/textures/entity/projectiles/" + arrow + ".png");
            verifySha256(new File(resources, "assets/htgt6/textures/entity/combat_update/dragon_fireball.png"),
                    "69dcb3c3d8c40888a9ba1dcae607d1d62088fca71647f016b9cdcf7720c4f3d1");
            String[] commandNames = {"chain_front", "chain_back", "chain_side", "chain_conditional",
                    "repeating_front", "repeating_back", "repeating_side", "repeating_conditional"};
            String[] commandHashes = {
                    "3ecdc2f4e7019000942d0ac7e5549ec46b0190dc044e6cb8d1af6a10437961e0",
                    "464b575a1cc2821be6b0045c28153b51179ebd6b13380810865bf56c44aaa735",
                    "fa1901bbb755fa26034aaed82c6ad7345e5ae4b74d77191c59da4b0fde66c790",
                    "106e853eaa7094ba930f1da224cad37828cfa8eed4a2f2313b561e78a4637d40",
                    "c5527682abc0640401057738992d678bf9667e3df89b28b082e94e2025350d36",
                    "17c6fb90973a4340b35cfb0f9b547a2c7e3f2a5d2409d282f8a128bf76c6d320",
                    "bf92bf1882d0bb96fe9528a88c672edb16e35f5c3787d95cb8cff903593dd5e0",
                    "c6ac791f75cf33b09eef0d8b8cdb65b8fd355c1cb4afbad20d12bca9ed29facb"
            };
            for (int index = 0; index < commandNames.length; index++) {
                String[] part = commandNames[index].split("_", 2);
                verifySha256(new File(resources, "assets/htgt6/textures/blocks/combat_update/"
                        + part[0] + "_command_block_" + part[1] + ".png"), commandHashes[index]);
            }
        } finally { modern.close(); }
    }

    private static void verifySha256(File file, String expected) throws Exception {
        require(file.isFile(), "asset " + file);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = read(new FileInputStream(file));
        byte[] hash = digest.digest(bytes);
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte value : hash) hex.append(String.format("%02x", value & 255));
        require(expected.equals(hex.toString()), "legacy asset checksum " + file.getName());
    }

    private static void compare(ZipFile zip, File local, String entryName) throws Exception {
        ZipEntry entry = zip.getEntry(entryName);
        require(local.isFile() && entry != null, "asset " + entryName);
        require(Arrays.equals(read(new FileInputStream(local)), read(zip.getInputStream(entry))),
                "byte-identical asset " + entryName);
    }
    private static byte[] read(InputStream input) throws Exception {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096]; int count;
            while ((count = input.read(buffer)) >= 0) output.write(buffer, 0, count);
            return output.toByteArray();
        } finally { input.close(); }
    }
    private static void close(float actual, float expected, String label) {
        require(Math.abs(actual - expected) < 0.0001F, label + " (" + actual + " != " + expected + ")");
    }
    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError("Combat 1.20.2 mismatch: " + label);
    }
    private CombatParityFixture120() { }
}
