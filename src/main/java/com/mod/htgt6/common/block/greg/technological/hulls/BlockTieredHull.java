package com.mod.htgt6.common.block.greg.technological.hulls;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/** Legacy hull IDs with an RGB-tinted output marker and HV+ reinforced skin. */
public final class BlockTieredHull extends blockHullStatbase {
    private static int renderId;
    private final int inputColor;
    private final int tier;
    @SideOnly(Side.CLIENT) private IIcon inputOverlay;
    @SideOnly(Side.CLIENT) private IIcon advancedSide, advancedTop;

    public BlockTieredHull(int inputColor, int tier) {
        this.inputColor = inputColor & 0xFFFFFF;
        this.tier = tier;
    }
    public int getTier() { return tier; }
    public long getVoltage() {
        return tier == 0 ? 8L : com.mod.htgt6.api.hitech6.Misc.MachineTierSystem.voltage(tier);
    }
    public long getEnergyCapacity() {
        return tier == 0 ? 256L : getVoltage() * 128L;
    }
    @Override public boolean hasTileEntity(int metadata) { return true; }
    @Override public net.minecraft.tileentity.TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityTieredHull();
    }
    @Override public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        world.removeTileEntity(x,y,z);
        super.breakBlock(world,x,y,z,block,meta);
    }
    public int getInputColor() { return inputColor; }
    public int getBodyColor() { return HullAppearance.color(tier); }
    public static void setRenderId(int id) { renderId = id; }
    @Override public int getRenderType() { return renderId; }
    public static int front(int metadata) { return HullAppearance.front(metadata); }

    @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister register) {
        super.registerBlockIcons(register);
        inputOverlay = register.registerIcon("htgt6:hull_input_overlay");
        if (tier >= 3) {
            advancedSide = register.registerIcon("htgt6:hull_advanced_side");
            advancedTop = register.registerIcon("htgt6:hull_advanced_top");
        }
    }
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int side, int meta) {
        int texture=HullAppearance.texture(tier,meta,side);
        return texture == 2 ? advancedTop : texture == 1 ? advancedSide : blockIcon;
    }
    @Override @SideOnly(Side.CLIENT) public int colorMultiplier(net.minecraft.world.IBlockAccess world, int x, int y, int z) {
        return getBodyColor();
    }
    @Override @SideOnly(Side.CLIENT) public int getRenderColor(int meta) { return getBodyColor(); }
    @SideOnly(Side.CLIENT) public IIcon getInputOverlay() { return inputOverlay; }

    @Override public void onBlockPlacedBy(World world, int x, int y, int z,
            EntityLivingBase entity, ItemStack stack) {
        int yaw = MathHelper.floor_double(entity.rotationYaw * 4F / 360F + .5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, yaw == 0 ? 2 : yaw == 1 ? 5 : yaw == 2 ? 3 : 4, 2);
    }
    @Override public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
        if (axis != ForgeDirection.UP && axis != ForgeDirection.DOWN) return false;
        int side = front(world.getBlockMetadata(x, y, z));
        int rotated = ForgeDirection.getOrientation(side).getRotation(axis).ordinal();
        return world.setBlockMetadataWithNotify(x, y, z, rotated, 2);
    }
    @Override public int damageDropped(int metadata) { return 0; }
    @Override public int getDamageValue(World world, int x, int y, int z) { return 0; }
}
