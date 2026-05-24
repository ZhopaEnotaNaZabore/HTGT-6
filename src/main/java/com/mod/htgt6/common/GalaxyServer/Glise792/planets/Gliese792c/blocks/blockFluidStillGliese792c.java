package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class blockFluidStillGliese792c extends BlockFluidClassic {

    @SideOnly(Side.CLIENT)
    protected IIcon stillIcon;
    @SideOnly(Side.CLIENT)
    protected IIcon flowingIcon;

    public blockFluidStillGliese792c(Fluid fluid) {
        super(fluid, Material.water); // Inherits vanilla liquid physics
        this.setLightOpacity(3);       // Light dims as you swim deeper
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        // Uses still texture for top/bottom, flowing texture for sides
        return (side == 0 || side == 1) ? stillIcon : flowingIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        // Point these to your asset folder paths (e.g., assets/modid/textures/blocks/...)
        this.stillIcon = register.registerIcon("htgt6:GlieseC_water_still");
        this.flowingIcon = register.registerIcon("htgt6:GlieseC_water_flow");

        // Connect texture properties directly back to the registered fluid object
        this.stack.getFluid().setIcons(stillIcon, flowingIcon);
    }

    @Override
    public boolean canDisplace(IBlockAccess world, int x, int y, int z) {
        // Prevents liquid from replacing solid obstacles incorrectly
        if (world.getBlock(x, y, z).getMaterial().isLiquid()) return false;
        return super.canDisplace(world, x, y, z);
    }

    @Override
    public boolean displaceIfPossible(World world, int x, int y, int z) {
        if (world.getBlock(x, y, z).getMaterial().isLiquid()) return false;
        return super.displaceIfPossible(world, x, y, z);
    }
}


