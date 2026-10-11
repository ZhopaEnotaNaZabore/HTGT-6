package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.neptune;

import com.mod.htgt6.sulimited.block.IndexedTerrainBlock;
import com.mod.htgt6.sulimited.block.TerrainTextureStyle;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/** Blocks making up Neptune's artificial playable pressure level. */
public final class NeptuneContent {
    public static Fluid liquefiedAtmosphericGas;
    public static Block liquefiedAtmosphericGasBlock;
    public static Block core;
    public static Block mantle, pressureIce;

    private NeptuneContent() { }

    public static void register() {
        if (core != null) return;
        Fluid candidate = new Fluid("liquefiedAtmosphericGas").setDensity(4300)
                .setViscosity(9000).setTemperature(59).setGaseous(false);
        if (FluidRegistry.registerFluid(candidate)) liquefiedAtmosphericGas = candidate;
        else liquefiedAtmosphericGas = FluidRegistry.getFluid("liquefiedAtmosphericGas");

        liquefiedAtmosphericGasBlock = new LiquefiedGasBlock(liquefiedAtmosphericGas)
                .setBlockName("NeptuneLiquefiedGas").setCreativeTab(CreativeTabs.tabBlock);
        GameRegistry.registerBlock(liquefiedAtmosphericGasBlock, "NeptuneLiquefiedGas");
        core = new CoreBlock().setBlockName("NeptuneCore").setCreativeTab(CreativeTabs.tabBlock);
        GameRegistry.registerBlock(core, "NeptuneCore");
        mantle = new CoreBlock(TerrainTextureStyle.DENSE_CORE, 0x37466A).setBlockName("NeptuneMantle");
        pressureIce = new CoreBlock(TerrainTextureStyle.BROKEN_ICE, 0x7498BC).setBlockName("NeptunePressureIce");
        GameRegistry.registerBlock(mantle, "NeptuneMantle");
        GameRegistry.registerBlock(pressureIce, "NeptunePressureIce");
    }

    private static final class CoreBlock extends IndexedTerrainBlock {
        private CoreBlock() {
            this(TerrainTextureStyle.MAGMATIC, 0x1A2855);
        }
        private CoreBlock(TerrainTextureStyle style, int colour) {
            super(Material.rock, style, colour);
            setBlockUnbreakable();
            setResistance(6000000.0F);
            setLightLevel(1.0F);
        }
    }

    private static final class LiquefiedGasBlock extends BlockFluidClassic {
        private IIcon still;
        private IIcon flow;
        private LiquefiedGasBlock(Fluid fluid) {
            super(fluid, Material.water);
            setLightOpacity(6);
            setHardness(100.0F);
        }
        @Override @SideOnly(Side.CLIENT)
        public void registerBlockIcons(IIconRegister register) {
            still = register.registerIcon("htgt6:neptune_liquefied_gas");
            flow = register.registerIcon("htgt6:neptune_liquefied_gas");
            liquefiedAtmosphericGas.setIcons(still, flow);
        }
        @Override @SideOnly(Side.CLIENT)
        public IIcon getIcon(int side, int meta) { return side == 0 || side == 1 ? still : flow; }
    }
}
