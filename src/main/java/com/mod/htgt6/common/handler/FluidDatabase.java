package com.mod.htgt6.common.handler;


import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.item.htgt6.fluids.HelemaLiquid;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class FluidDatabase {
    public static Fluid CHALCOPYRITE_SOLUTION;
    public static Fluid ENRICHED_COPPER_SOLUTION;

    /** Separate from GT6 BlueVitriol: this intermediate still contains PGM traces. */
    public static void registerDeepOreFluids() {
        ENRICHED_COPPER_SOLUTION = FluidRegistry.getFluid("htgt6.enriched_copper_solution");
        if (ENRICHED_COPPER_SOLUTION == null) {
            Fluid fluid = new Fluid("htgt6.enriched_copper_solution") {
                @Override public int getColor() { return 0x267DA6; }
                @Override @SideOnly(Side.CLIENT) public IIcon getStillIcon() { return Blocks.water.getIcon(0, 0); }
                @Override @SideOnly(Side.CLIENT) public IIcon getFlowingIcon() { return Blocks.water.getIcon(1, 0); }
            }.setDensity(1350).setViscosity(1600).setTemperature(300)
                    .setUnlocalizedName("htgt6.enriched_copper_solution");
            FluidRegistry.registerFluid(fluid);
            ENRICHED_COPPER_SOLUTION = FluidRegistry.getFluid(fluid.getName());
        }
    }
    public static Fluid MOLTEN_TRANSCENDENTIC_ALLOY;
    public static Fluid MOLTEN_STABLE_TIME_SPACE;
    public static Fluid MOLTEN_QUANTUM_ALLOY;
    public static Fluid MOLTEN_QUANTINUM;

    /** HiTech6 registers these fluids and binds them to the GT material model. */
    public static void registerEndgameMoltenFluids() {
        MOLTEN_TRANSCENDENTIC_ALLOY = molten(com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator.TRANSCENDENTIC_ALLOY);
        MOLTEN_STABLE_TIME_SPACE = molten(com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator.STABLE_TIME_SPACE);
        MOLTEN_QUANTUM_ALLOY = molten(com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator.QUANTUM_ALLOY);
        MOLTEN_QUANTINUM = molten(com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator.QUANTINUM);
    }

    private static Fluid molten(gregapi.oredict.OreDictMaterial material) {
        if (material == null) throw new IllegalStateException("Endgame material not initialized");
        if (material.mLiquid != null && material.mLiquid.getFluid() != null) return material.mLiquid.getFluid();
        final int color = ((material.mRGBaLiquid[0] & 255) << 16)
                | ((material.mRGBaLiquid[1] & 255) << 8) | (material.mRGBaLiquid[2] & 255);
        String name = "molten." + material.mNameInternal.toLowerCase(java.util.Locale.ENGLISH);
        Fluid fluid = FluidRegistry.getFluid(name);
        if (fluid == null) {
            fluid = new Fluid(name) {
                @Override public int getColor() { return color; }
                @Override @SideOnly(Side.CLIENT) public IIcon getStillIcon() { return Blocks.lava.getIcon(0, 0); }
                @Override @SideOnly(Side.CLIENT) public IIcon getFlowingIcon() { return Blocks.lava.getIcon(1, 0); }
            }.setTemperature((int) material.mMeltingPoint).setViscosity(1000).setLuminosity(10);
            FluidRegistry.registerFluid(fluid);
            fluid = FluidRegistry.getFluid(name);
        }
        material.liquid(new net.minecraftforge.fluids.FluidStack(fluid, 144));
        gregapi.data.CS.FluidsGT.LIQUID.add(name);
        gregapi.data.LH.add(fluid.getUnlocalizedName(), "Molten " + material.mNameLocal);
        return fluid;
    }

    public static final Fluid helemaLiquid = new Fluid(HTGT6.MOD_ID +":HelemaLiquid");
    public static HelemaLiquid HELEMA_LIQUID;

    // Advanced gaseous turbine fuels. They intentionally have no world blocks;
    // pipes, tanks and the universal canister operate directly on FluidStack.
    public static Fluid NETHER_GAS;
    public static Fluid NITRO_BENZENE;
    public static Fluid NAQUADAH_GAS;
    public static Fluid NAQUADRIA_GAS;
    public static Fluid XENOXEN;

    public static void register() {
        registerDeepOreFluids();
        CHALCOPYRITE_SOLUTION = FluidRegistry.getFluid("htgt6.chalcopyrite_solution");
        if (CHALCOPYRITE_SOLUTION == null) {
            Fluid solution = new Fluid("htgt6.chalcopyrite_solution") {
                @Override public int getColor() { return 0x8D9656; }
                @Override @SideOnly(Side.CLIENT) public IIcon getStillIcon() { return Blocks.water.getIcon(0, 0); }
                @Override @SideOnly(Side.CLIENT) public IIcon getFlowingIcon() { return Blocks.water.getIcon(1, 0); }
            }.setDensity(1400).setViscosity(1800).setTemperature(300)
                    .setUnlocalizedName("htgt6.chalcopyrite_solution");
            FluidRegistry.registerFluid(solution);
            CHALCOPYRITE_SOLUTION = FluidRegistry.getFluid("htgt6.chalcopyrite_solution");
        }
        // Жидкость должна быть зарегистрирована раньше, чем блок для которой она будет прикреплена!
        if (FluidRegistry.registerFluid(helemaLiquid)) {
            // Если конфликтов никаких нет, то регистрируем блок для жидкости, иначе ничего не делаем.
            HELEMA_LIQUID = new HelemaLiquid(helemaLiquid);
            GameRegistry.registerBlock(HELEMA_LIQUID, "HelemaLiquid");
        }

        NETHER_GAS = registerGas("NetherGas", 0x9A2BCB, 420);
        NITRO_BENZENE = registerGas("NitroBenzene", 0xD8B51F, 360);
        NAQUADAH_GAS = registerGas("NaquadahGas", 0x39D98A, 520);
        NAQUADRIA_GAS = registerGas("NaquadriaGas", 0x72F7D2, 680);
        XENOXEN = registerGas("Xenoxen", 0x8B72FF, 900);
        registerEndgameMoltenFluids();
    }

    /** Registers a colored gas or reuses a fluid supplied by another mod. */
    private static Fluid registerGas(final String registryName, final int rgbColor, int temperature) {
        Fluid existing = FluidRegistry.getFluid(registryName);
        if (existing != null) return existing;

        Fluid gas = new Fluid(registryName) {
            @Override
            public int getColor() {
                return rgbColor;
            }

            @Override
            @SideOnly(Side.CLIENT)
            public IIcon getStillIcon() {
                return Blocks.water.getIcon(0, 0);
            }

            @Override
            @SideOnly(Side.CLIENT)
            public IIcon getFlowingIcon() {
                return Blocks.water.getIcon(1, 0);
            }
        };
        gas.setDensity(-100)
                .setViscosity(100)
                .setTemperature(temperature)
                .setGaseous(true)
                .setUnlocalizedName(registryName);
        FluidRegistry.registerFluid(gas);
        return FluidRegistry.getFluid(registryName);
    }
}
