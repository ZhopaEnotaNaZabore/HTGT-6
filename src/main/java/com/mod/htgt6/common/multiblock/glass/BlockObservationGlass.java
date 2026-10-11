package com.mod.htgt6.common.multiblock.glass;

import com.mod.htgt6.client.render.ObservationGlassSprites;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

/** CTM foundation shared by all HTGT6 multiblock observation windows. IDs/drops stay with subclasses. */
public abstract class BlockObservationGlass extends BlockGlass {
    public static final int REINFORCED = 0, FORMED_CLEAR = 1;
    private final boolean alwaysClear;
    @SideOnly(Side.CLIENT) private IIcon[] reinforcedIcons;
    @SideOnly(Side.CLIENT) private IIcon[] clearIcons;

    protected BlockObservationGlass(boolean alwaysClear) {
        super(Material.glass, false);
        this.alwaysClear = alwaysClear;
    }

    @Override @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        reinforcedIcons = ObservationGlassSprites.register(register, false);
        clearIcons = ObservationGlassSprites.register(register, true);
        blockIcon = reinforcedIcons[15];
    }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
        return (alwaysClear || metadata == 1 ? clearIcons : reinforcedIcons)[15];
    }

    @SideOnly(Side.CLIENT)
    public IIcon getConnectedIcon(int metadata, int mask) {
        return (alwaysClear || metadata == 1 ? clearIcons : reinforcedIcons)[mask & 255];
    }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        if (side < 0 || side > 5) return getIcon(0, world.getBlockMetadata(x, y, z));
        int mask = 0;
        if (!connects(world, x, y, z, side, -1, 0)) mask |= 1;
        if (!connects(world, x, y, z, side, 1, 0)) mask |= 2;
        if (!connects(world, x, y, z, side, 0, -1)) mask |= 4;
        if (!connects(world, x, y, z, side, 0, 1)) mask |= 8;
        if (!connects(world, x, y, z, side, -1, -1)) mask |= 16;
        if (!connects(world, x, y, z, side, 1, -1)) mask |= 32;
        if (!connects(world, x, y, z, side, -1, 1)) mask |= 64;
        if (!connects(world, x, y, z, side, 1, 1)) mask |= 128;
        return (alwaysClear || world.getBlockMetadata(x, y, z) == 1 ? clearIcons : reinforcedIcons)[mask];
    }

    private boolean connects(IBlockAccess world, int x, int y, int z, int face, int u, int v) {
        return world.getBlock(x + ObservationGlassPattern.offset(face, u, v, 0),
                y + ObservationGlassPattern.offset(face, u, v, 1),
                z + ObservationGlassPattern.offset(face, u, v, 2)) == this;
    }

    @Override @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        // Coordinates refer to the neighboring block, not this block.
        return world.getBlock(x, y, z) != this && super.shouldSideBeRendered(world, x, y, z, side);
    }

    @Override @SideOnly(Side.CLIENT) public int getRenderBlockPass() { return 1; }
}
