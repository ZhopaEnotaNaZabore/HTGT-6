package com.mod.htgt6.common.chemical;

import com.cleanroommc.modularui.manager.GuiInfos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Front controller for the 9x13x9 MultiTile Chemical Plant. */
public final class BlockChemicalPlantController extends BlockContainer {
    @SideOnly(Side.CLIENT) private IIcon front;
    @SideOnly(Side.CLIENT) private IIcon side;

    public BlockChemicalPlantController() {
        super(Material.iron);
        setBlockName("multi_tile_chemical_plant");
        setHardness(7.0F);
        setResistance(35.0F);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override public TileEntity createNewTileEntity(World world, int metadata) {
        return new MultiTileChemicalPlant();
    }

    @Override public void onBlockPlacedBy(World world, int x, int y, int z,
                                          EntityLivingBase entity, ItemStack stack) {
        int direction = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        int facing = direction == 0 ? 2 : direction == 1 ? 5 : direction == 2 ? 3 : 4;
        world.setBlockMetadataWithNotify(x, y, z, facing, 2);
    }

    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                               int sideNumber, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) return false;
        if (!world.isRemote && world.getTileEntity(x, y, z) instanceof MultiTileChemicalPlant) {
            GuiInfos.TILE_ENTITY.open(player, world, x, y, z);
        }
        return true;
    }

    @Override @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        side = register.registerIcon("htgt6:multiblock_v2_chemical_casing");
        front = register.registerIcon("htgt6:multiblock_v2_chemical_front");
        blockIcon = side;
    }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(int sideNumber, int metadata) {
        int facing = metadata >= 2 && metadata <= 5 ? metadata : 3;
        return sideNumber == facing ? front : side;
    }
}
