package com.mod.htgt6.common.multiblock.gateway;

import com.cleanroommc.modularui.manager.GuiInfos;
import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.api.hitech6.multiblock.FluidGatewayVariant;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;

import java.util.List;

/** Reusable I/O gateway block for any HTGT6 multiblock controller. */
public class BlockMultiblockGateway extends BlockContainer {
    private static int renderId;
    @SideOnly(Side.CLIENT) private IIcon[] icons;
    @SideOnly(Side.CLIENT) private IIcon overlayIcon;

    protected BlockMultiblockGateway(String blockName) {
        super(Material.iron);
        setBlockName(blockName);
        setHardness(5.0F);
        setResistance(25.0F);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityMultiblockGateway(metadata);
    }

    @Override @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        // Metadata 5-8 belongs to multi-tank fluid variants; logical energy
        // output intentionally lives at 9 to preserve old worlds.
        icons = new IIcon[16];
        for (GatewayType type : GatewayType.VALUES) {
            icons[type.metadata] = register.registerIcon("htgt6:" + type.textureName);
        }
        overlayIcon = register.registerIcon("htgt6:multiblock_gateway_overlay");
        blockIcon = icons[0];
    }

    public static void setRenderId(int id) { renderId = id; }
    @Override public int getRenderType() { return renderId; }

    @SideOnly(Side.CLIENT)
    public IIcon getOverlayIcon() { return overlayIcon; }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) { return icons[FluidGatewayVariant.type(metadata).metadata]; }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityMultiblockGateway) {
            TileEntityMultiblockGateway gateway = (TileEntityMultiblockGateway) tile;
            Block casing = gateway.getInheritedCasingBlock();
            // The controller-facing side and the top retain the functional I/O
            // marker; every other face inherits the linked multiblock casing.
            if (casing != null && casing != this && side != gateway.getFunctionalSide() && side != 1) {
                return casing.getIcon(side, gateway.getInheritedCasingMetadata());
            }
        }
        return getIcon(side, world.getBlockMetadata(x, y, z));
    }

    @Override public int onBlockPlaced(World world, int x, int y, int z, int side,
                                       float hitX, float hitY, float hitZ, int metadata) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityMultiblockGateway) {
            ((TileEntityMultiblockGateway) tile).setVisualSide(side);
        }
        return metadata;
    }
    @Override public int damageDropped(int metadata) { return Math.max(0, Math.min(9, metadata)); }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) return false;
        GatewayType type = FluidGatewayVariant.type(world.getBlockMetadata(x, y, z));
        if (type == GatewayType.ENERGY_INPUT || type == GatewayType.ENERGY_OUTPUT) return true;
        if (!world.isRemote && world.getTileEntity(x, y, z) instanceof TileEntityMultiblockGateway) {
            GuiInfos.TILE_ENTITY.open(player, world, x, y, z);
        }
        return true;
    }

    @Override @SideOnly(Side.CLIENT) @SuppressWarnings("unchecked")
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (GatewayType type : GatewayType.VALUES) {
            if (type != GatewayType.ENERGY_OUTPUT) list.add(new ItemStack(item, 1, type.metadata));
        }
        for (int metadata = FluidGatewayVariant.FLUID_INPUT_4;
             metadata <= FluidGatewayVariant.FLUID_OUTPUT_8; metadata++) {
            list.add(new ItemStack(item, 1, metadata));
        }
        list.add(new ItemStack(item, 1, GatewayType.ENERGY_OUTPUT.metadata));
    }
}
