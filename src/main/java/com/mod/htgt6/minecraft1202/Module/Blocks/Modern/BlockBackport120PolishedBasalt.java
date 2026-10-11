package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

/** Raw and polished basalt pillars with persistent X/Y/Z orientation. */
public final class BlockBackport120PolishedBasalt extends Block {
    private IIcon end;
    private final String texture;

    public BlockBackport120PolishedBasalt() { this("polished_basalt"); }
    public BlockBackport120PolishedBasalt(String name) {
        super(Material.rock);
        texture=name;
        setBlockName("backport_1_20."+name);
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabBlock);
        setHardness(1.25F);
        setResistance(Backport120Content.legacyResistance(4.2F));
        setStepSound(com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.BuildingSounds116.BASALT);
        setHarvestLevel("pickaxe", 0);
    }

    @Override public int onBlockPlaced(net.minecraft.world.World w,int x,int y,int z,int side,float hx,float hy,float hz,int meta){return side<2?0:side<4?8:4;}
    @Override public int damageDropped(int metadata){return 0;}
    @Override public int getRenderType(){int id=Backport120Content.getJsonRenderId();return id>=0?id:0;}
    @Override public boolean rotateBlock(net.minecraft.world.World w, int x, int y, int z,
                                          net.minecraftforge.common.util.ForgeDirection axis) {
        if (axis == null || axis == net.minecraftforge.common.util.ForgeDirection.UNKNOWN) return false;
        int old = w.getBlockMetadata(x, y, z) & 12;
        int next;
        if (axis.offsetY != 0) next = old == 4 ? 8 : old == 8 ? 4 : 0;
        else if (axis.offsetX != 0) next = old == 0 ? 8 : old == 8 ? 0 : 4;
        else next = old == 0 ? 4 : old == 4 ? 0 : 8;
        return next != old && w.setBlockMetadataWithNotify(x, y, z, next, 3);
    }
    @Override @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = register.registerIcon("htgt6:1_20_backport/"+texture+"_side");
        end = register.registerIcon("htgt6:1_20_backport/"+texture+"_top");
    }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
        int axis = metadata & 12;
        return axis == 0 && (side == 0 || side == 1)
                || axis == 4 && (side == 4 || side == 5)
                || axis == 8 && (side == 2 || side == 3) ? end : blockIcon;
    }
}
