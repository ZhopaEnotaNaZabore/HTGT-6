package com.mod.htgt6.common.electronics;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

/** Solid structural skin used by the Electronics Factory and inherited by its gateways. */
public final class BlockElectronicFactoryCasing extends Block {
    @SideOnly(Side.CLIENT) private IIcon centralIcon;
    @SideOnly(Side.CLIENT) private IIcon ribIcon;
    private final boolean reinforced;
    public BlockElectronicFactoryCasing() { this(false); }
    public BlockElectronicFactoryCasing(boolean reinforced) {
        super(Material.iron);
        this.reinforced = reinforced;
        setBlockName(reinforced ? "electronic_factory_rib" : "electronic_factory_casing");
        setHardness(7.0F);
        setResistance(35.0F);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        centralIcon = register.registerIcon("htgt6:electronic_factory_v2_casing");
        ribIcon = register.registerIcon("htgt6:electronic_factory_v2_rib");
        blockIcon = reinforced ? ribIcon : centralIcon;
    }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) { return reinforced ? ribIcon : centralIcon; }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return getIcon(side, 0);
    }

}
