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

public class blockFluidflowGliese792c extends BlockFluidClassic {
    public blockFluidflowGliese792c(Fluid fluid) {
        super(fluid, Material.water);
        this.setLightOpacity(3);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        // Refers icon mapping directly to our registered static instance blocks
        return GlieseCwater.blockGlieseCwaterStill.getIcon(side, meta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        // Handled completely by the still block class pass above
    }
}