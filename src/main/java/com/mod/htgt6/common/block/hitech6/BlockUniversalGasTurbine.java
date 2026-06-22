package com.mod.htgt6.common.block.hitech6;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.TE.UniversalGasTurbineTE;
import com.mod.htgt6.common.handler.GuiHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class BlockUniversalGasTurbine extends Block implements ITileEntityProvider {

    @SideOnly(Side.CLIENT)
    private IIcon iconFront;
    @SideOnly(Side.CLIENT)
    private IIcon iconTop;

    public BlockUniversalGasTurbine() {
        super(Material.iron);
        setBlockName("universal_gas_turbine");
        setHardness(3.0F);
        setResistance(5.0F);
        setStepSound(soundTypeMetal);
        this.isBlockContainer = true;
    }

    /**
     * Handles opening the GUI when the block is right-clicked.
     */
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof UniversalGasTurbineTE) {
                // Uses UNIVERSAL_GAS_TURBINE_ID (4) mapped in your GuiHandler
                player.openGui(HTGT6.instance, GuiHandler.UNIVERSAL_GAS_TURBINE_ID, world, x, y, z);
            }
        }
        return true;
    }

    /**
     * Link the TileEntity to this Block.
     */
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new UniversalGasTurbineTE();
    }

    /**
     * Set the direction of the block based on how the player is standing.
     */
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int l = MathHelper.floor_double((double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

        if (l == 0) world.setBlockMetadataWithNotify(x, y, z, 2, 2); // North
        if (l == 1) world.setBlockMetadataWithNotify(x, y, z, 5, 2); // East
        if (l == 2) world.setBlockMetadataWithNotify(x, y, z, 3, 2); // South
        if (l == 3) world.setBlockMetadataWithNotify(x, y, z, 4, 2); // West
    }

    // --- Textures / Icons ---

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        // assets/htgt6/textures/blocks/...
        this.blockIcon = iconRegister.registerIcon("htgt6:assembler_side");
        this.iconFront = iconRegister.registerIcon("htgt6:UGS_Front");
        this.iconTop   = iconRegister.registerIcon("htgt6:assembler_top");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
        // side 1 is Top, side 0 is Bottom
        if (side == 1) return iconTop;
        if (side == 0) return blockIcon;

        // If metadata is 0 (default/freshly placed via commands), default front to South (3)
        if (metadata == 0 && side == 3) return iconFront;

        // If the side matches our stored rotation metadata, show the front face
        return side == metadata ? iconFront : blockIcon;
    }

    /**
     * Ensures the TileEntity is removed correctly when the block is broken.
     */
    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
        world.removeTileEntity(x, y, z);
    }
}