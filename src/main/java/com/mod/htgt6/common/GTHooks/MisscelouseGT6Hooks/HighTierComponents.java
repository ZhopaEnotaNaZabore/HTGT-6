package com.mod.htgt6.common.GTHooks.MisscelouseGT6Hooks;

import com.mod.htgt6.common.handler.ModTab;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregapi.data.IL;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;
import java.util.Locale;

/** HTGT6 continuation of the standard GT6 machine-component series after PUV1. */
public final class HighTierComponents extends Item {
    public static final HighTierComponents INSTANCE = new HighTierComponents();

    public enum Tier {
        UX(8, 0xE15BFF), OLV(9, 0x61E9FF), OMV(10, 0x63FF91),
        OHV(11, 0xFFF06A), OEV(12, 0xFF9B45), OIV(13, 0xFF5374), MAX(14, 0xFFFFFF);

        public final int power;
        public final int color;

        Tier(int power, int color) {
            this.power = power;
            this.color = color;
        }
    }

    public enum Component {
        MOTOR("electric_motor", "electricMotor", 12010),
        PISTON("electric_piston", "electricPiston", 12070),
        PUMP("electric_pump", "electricPump", 12030),
        CONVEYOR("conveyor_module", "conveyorModule", 12050),
        ROBOT_ARM("robot_arm", "robotArm", 12090),
        EMITTER("emitter", "emitter", 12130),
        SENSOR("sensor", "sensor", 12150),
        FIELD_GENERATOR("field_generator", "fieldGenerator", 12110);

        public final String translationKey;
        public final String oreName;
        public final int gtTextureMeta;

        Component(String translationKey, String oreName, int gtTextureMeta) {
            this.translationKey = translationKey;
            this.oreName = oreName;
            this.gtTextureMeta = gtTextureMeta;
        }
    }

    private final IIcon[][] icons = new IIcon[Tier.values().length][Component.values().length];
    private static boolean registered;

    private HighTierComponents() {
        setUnlocalizedName("highTierComponent");
        setHasSubtypes(true);
        setMaxDamage(0);
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }

    public static synchronized void register() {
        if (registered) return;
        GameRegistry.registerItem(INSTANCE, "high_tier_component");
        for (Tier tier : Tier.values()) {
            for (Component component : Component.values()) {
                OreDictionary.registerOre(component.oreName + tier.name(), stack(component, tier, 1));
            }
        }
        bindMissingGT6Aliases();
        registered = true;
    }

    /** Fills GT6's declared-but-unassigned PUV2..OMEGA IL slots without replacing valid GT items. */
    public static void bindMissingGT6Aliases() {
        if (!registered && Item.getIdFromItem(INSTANCE) == 0) return;
        bind(Component.MOTOR, IL.Electric_Motor_PUV2, IL.Electric_Motor_PUV3,
                IL.Electric_Motor_PUV4, IL.Electric_Motor_PUV5, IL.Electric_Motor_OMEGA);
        bind(Component.PISTON, IL.Electric_Piston_PUV2, IL.Electric_Piston_PUV3,
                IL.Electric_Piston_PUV4, IL.Electric_Piston_PUV5, IL.Electric_Piston_OMEGA);
        bind(Component.PUMP, IL.Electric_Pump_PUV2, IL.Electric_Pump_PUV3,
                IL.Electric_Pump_PUV4, IL.Electric_Pump_PUV5, IL.Electric_Pump_OMEGA);
        bind(Component.CONVEYOR, IL.Conveyor_Module_PUV2, IL.Conveyor_Module_PUV3,
                IL.Conveyor_Module_PUV4, IL.Conveyor_Module_PUV5, IL.Conveyor_Module_OMEGA);
        bind(Component.ROBOT_ARM, IL.Robot_Arm_PUV2, IL.Robot_Arm_PUV3,
                IL.Robot_Arm_PUV4, IL.Robot_Arm_PUV5, IL.Robot_Arm_OMEGA);
        bind(Component.EMITTER, IL.Emitter_PUV2, IL.Emitter_PUV3,
                IL.Emitter_PUV4, IL.Emitter_PUV5, IL.Emitter_OMEGA);
        bind(Component.SENSOR, IL.Sensor_PUV2, IL.Sensor_PUV3,
                IL.Sensor_PUV4, IL.Sensor_PUV5, IL.Sensor_OMEGA);
        bind(Component.FIELD_GENERATOR, IL.Field_Generator_PUV2, IL.Field_Generator_PUV3,
                IL.Field_Generator_PUV4, IL.Field_Generator_PUV5, IL.Field_Generator_OMEGA);
    }

    private static void bind(Component component, IL... aliases) {
        Tier[] tiers = Tier.values();
        for (int i = 0; i < aliases.length && i < tiers.length; i++)
            if (!aliases[i].hasBeenSet()) aliases[i].set(stack(component, tiers[i], 1));
    }

    public static ItemStack stack(Component component, Tier tier, int amount) {
        return new ItemStack(INSTANCE, amount, metadata(component, tier));
    }

    private static int metadata(Component component, Tier tier) {
        return tier.ordinal() * Component.values().length + component.ordinal();
    }

    private static Tier tier(int metadata) {
        int index = metadata / Component.values().length;
        return Tier.values()[Math.max(0, Math.min(index, Tier.values().length - 1))];
    }

    private static Component component(int metadata) {
        int index = metadata % Component.values().length;
        return Component.values()[Math.max(0, index)];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        for (Tier tier : Tier.values())
            for (Component component : Component.values())
                icons[tier.ordinal()][component.ordinal()] = register.registerIcon(
                        "htgt6:high_tier_components/"
                                + tier.name().toLowerCase(Locale.ROOT) + "/"
                                + component.translationKey);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int metadata) {
        return icons[tier(metadata).ordinal()][component(metadata).ordinal()];
    }

    @Override
    public int getColorFromItemStack(ItemStack stack, int pass) {
        // Material colours are baked into the component animation while its
        // outlines and optical accents stay intact. Do not apply a second tint.
        return 0xFFFFFF;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        Component component = component(stack.getItemDamage());
        Tier tier = tier(stack.getItemDamage());
        return tier.name() + " " + StatCollector.translateToLocal(
                "item.htgt6.component." + component.translationKey);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (Tier tier : Tier.values())
            for (Component component : Component.values())
                list.add(stack(component, tier, 1));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        Tier tier = tier(stack.getItemDamage());
        long euPerTick = 128L;
        for (int i = 0; i < tier.power; i++) euPerTick *= 4L;
        list.add("Tier: " + tier.name());
        list.add(euPerTick + " EU/t");
    }
}
