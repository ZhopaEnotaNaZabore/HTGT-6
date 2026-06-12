package com.mod.htgt6.common.block.hitech6;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.TE.TESuperMasicCompressor;
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

public class BlockSuperMasicCompressor extends Block implements ITileEntityProvider {

    @SideOnly(Side.CLIENT) private IIcon iconFront;
    @SideOnly(Side.CLIENT) private IIcon iconTop;

    public BlockSuperMasicCompressor() {
        super(Material.iron);
        setBlockName("superMasicCompressor");
        setHardness(4.0F);
        setResistance(6.0F);
        setStepSound(soundTypeMetal);
        this.isBlockContainer = true;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TESuperMasicCompressor) {
                // Ensure you register a dedicated GUI ID for the Compressor inside your GuiHandler (e.g., ID: 1)
                player.openGui(HTGT6.instance, 1, world, x, y, z);
            }
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TESuperMasicCompressor();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int l = MathHelper.floor_double((double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        if (l == 0) world.setBlockMetadataWithNotify(x, y, z, 2, 2);
        if (l == 1) world.setBlockMetadataWithNotify(x, y, z, 5, 2);
        if (l == 2) world.setBlockMetadataWithNotify(x, y, z, 3, 2);
        if (l == 3) world.setBlockMetadataWithNotify(x, y, z, 4, 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        this.blockIcon = iconRegister.registerIcon("htgt6:assembler_side");
        this.iconFront = iconRegister.registerIcon("htgt6:compressor_front");
        this.iconTop   = iconRegister.registerIcon("htgt6:assembler_top");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
        if (side == 1) return iconTop;
        if (side == 0) return blockIcon;
        if (metadata == 0 && side == 3) return iconFront;
        return side == metadata ? iconFront : blockIcon;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        // Safe standard container block cleanup logic to prevent memory leaks
        super.breakBlock(world, x, y, z, block, meta);
        world.removeTileEntity(x, y, z);
    }
}