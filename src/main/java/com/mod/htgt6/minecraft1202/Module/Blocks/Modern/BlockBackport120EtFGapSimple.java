package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.IIcon;

/** Full-cube vanilla backports that only require separate side and end textures. */
public class BlockBackport120EtFGapSimple extends Block {
    private final String texture;
    private final String endTexture;
    @SideOnly(Side.CLIENT) private IIcon sideIcon;
    @SideOnly(Side.CLIENT) private IIcon endIcon;

    public BlockBackport120EtFGapSimple(Material material, String name, String texture,
            String endTexture, float hardness, float resistance, SoundType sound,
            String harvestTool, int harvestLevel) {
        super(material);
        this.texture = texture;
        this.endTexture = endTexture;
        setBlockName("backport_1_20." + name);
        setHardness(hardness);
        setResistance(Backport120Content.legacyResistance(resistance));
        setStepSound(sound);
        if (harvestTool != null) setHarvestLevel(harvestTool, harvestLevel);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @SideOnly(Side.CLIENT)
    @Override public void registerBlockIcons(IIconRegister register) {
        sideIcon = register.registerIcon(resolve(texture));
        endIcon = register.registerIcon(resolve(endTexture == null ? texture : endTexture));
        blockIcon = sideIcon;
    }

    private static String resolve(String name) {
        return name.indexOf(':') >= 0 ? name : "htgt6:1_20_backport/" + name;
    }

    @SideOnly(Side.CLIENT)
    @Override public IIcon getIcon(int side, int metadata) {
        return (side == 0 || side == 1) ? endIcon : sideIcon;
    }
}
