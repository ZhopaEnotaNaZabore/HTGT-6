package com.mod.htgt6.common.item.htgt6.fluids;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

public class HelemaLiquid extends BlockFluidClassic {
    public HelemaLiquid(Fluid fluid) {
        super(fluid, Material.lava);
        // Привязываем жидкость к текущему блоку
        fluid.setBlock(this);
        setBlockName("HelemaLiquid");
        setBlockTextureName(HTGT6.MOD_ID+":fluid1f");
        setCreativeTab(ModTab.INSTANCE);
    }
    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons (IIconRegister register) {
        super.registerBlockIcons(register);
        getFluid().setIcons(blockIcon, register.registerIcon(HTGT6.MOD_ID+":fluid1f"));
    }
}

