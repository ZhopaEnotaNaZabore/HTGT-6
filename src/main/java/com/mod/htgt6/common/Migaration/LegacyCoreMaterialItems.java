package com.mod.htgt6.common.Migaration;

import com.mod.htgt6.common.handler.ModTab;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Registry bridge for HTGT6 material parts present in Build 13 worlds.
 *
 * These are HTGT6 progression items, not Minecraft backport content.  Removing
 * their registry names while moving the backport modules makes Forge discard
 * existing stacks before a later NBT migration can inspect them.  Keep the old
 * names alive and expose OreDictionary aliases so both legacy and generated
 * GT6 material forms remain usable.
 */
public final class LegacyCoreMaterialItems {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-Migration");

    public static final Item CELESTIAL_TITANIUM_GEAR = item(
            "CelestialTitGear", "gearGt", 0xD9ECFF);
    public static final Item AS_TITANIUM_INGOT = item(
            "IngotASTitanium", "ingot", 0xD9ECFF);
    public static final Item CELESTIAL_TITANIUM_ROD = item(
            "rodCelTit", "stick", 0xD9ECFF);
    public static final Item CELESTIAL_TITANIUM_ROTOR = item(
            "rotorCelTit", "rotor", 0xD9ECFF);
    public static final Item QUANTINUM_INGOT = item(
            "QuantinumIngot", "ingot", 0x0C4A30);
    public static final Item QUANTINUM_GEAR = item(
            "QuantinumGear", "gearGt", 0x0C4A30);
    public static final Item QUANTINUM_ROTOR = item(
            "rotorQuantinum", "rotor", 0x0C4A30);

    private static boolean registered;

    private LegacyCoreMaterialItems() {}

    public static synchronized void register() {
        if (registered) return;

        GameRegistry.registerItem(CELESTIAL_TITANIUM_GEAR, "CelestialTitaniumGear");
        GameRegistry.registerItem(AS_TITANIUM_INGOT, "IngotASTitanium");
        GameRegistry.registerItem(CELESTIAL_TITANIUM_ROD, "rodCelTit");
        GameRegistry.registerItem(CELESTIAL_TITANIUM_ROTOR, "rotorCelTit");
        GameRegistry.registerItem(QUANTINUM_INGOT, "QuantinumIngot");
        GameRegistry.registerItem(QUANTINUM_GEAR, "QuantinumGear");
        GameRegistry.registerItem(QUANTINUM_ROTOR, "rotorQuantinum");

        OreDictionary.registerOre("gearGtCelestialTitanium", CELESTIAL_TITANIUM_GEAR);
        OreDictionary.registerOre("ingotCelestialTitanium", AS_TITANIUM_INGOT);
        OreDictionary.registerOre("stickCelestialTitanium", CELESTIAL_TITANIUM_ROD);
        OreDictionary.registerOre("rotorCelestialTitanium", CELESTIAL_TITANIUM_ROTOR);
        OreDictionary.registerOre("ingotQuantinum", QUANTINUM_INGOT);
        OreDictionary.registerOre("gearGtQuantinum", QUANTINUM_GEAR);
        OreDictionary.registerOre("rotorQuantinum", QUANTINUM_ROTOR);

        registered = true;
        LOGGER.info("Registered 7 Build 13 HTGT6 material aliases for lossless world migration");
    }

    private static Item item(String unlocalizedName, String shape, int color) {
        return new TintedMigrationItem(color)
                .setUnlocalizedName(unlocalizedName)
                .setTextureName("gregtech:materialicons/SHINY/" + shape)
                .setCreativeTab(ModTab.INSTANCE)
                .setMaxStackSize(64);
    }

    private static final class TintedMigrationItem extends Item {
        private final int color;

        private TintedMigrationItem(int color) {
            this.color = color;
        }

        @Override
        public int getColorFromItemStack(net.minecraft.item.ItemStack stack, int renderPass) {
            return color;
        }
    }
}
