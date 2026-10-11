package com.mod.htgt6.common.block.hitech6;


import com.mod.htgt6.common.TE.FluidSolidifierTE;
import com.cleanroommc.modularui.manager.GuiInfos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

public class BlockFluidSolidifier extends Block implements ITileEntityProvider {

    @SideOnly(Side.CLIENT)
    private IIcon iconFront;
    @SideOnly(Side.CLIENT)
    private IIcon iconTop;

    private final Random rand = new Random();

    public BlockFluidSolidifier() {
        super(Material.iron);
        setBlockName("fluidSolidifier");
        setHardness(3.0F);
        setResistance(5.0F);
        setStepSound(soundTypeMetal);
        this.isBlockContainer = true;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof FluidSolidifierTE) {
                GuiInfos.TILE_ENTITY.open(player, world, x, y, z);
            }
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new FluidSolidifierTE();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int l = MathHelper.floor_double((double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

        if (l == 0) world.setBlockMetadataWithNotify(x, y, z, 2, 2); // North[cite: 8]
        if (l == 1) world.setBlockMetadataWithNotify(x, y, z, 5, 2); // East[cite: 8]
        if (l == 2) world.setBlockMetadataWithNotify(x, y, z, 3, 2); // South[cite: 8]
        if (l == 3) world.setBlockMetadataWithNotify(x, y, z, 4, 2); // West[cite: 8]
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        this.blockIcon = iconRegister.registerIcon("htgt6:multiblock_v2_foundry_casing");
        this.iconFront = iconRegister.registerIcon("htgt6:multiblock_v2_foundry_front");
        this.iconTop   = iconRegister.registerIcon("htgt6:multiblock_v2_foundry_casing");
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
        TileEntity te = world.getTileEntity(x, y, z);

        if (te instanceof FluidSolidifierTE) {
            FluidSolidifierTE solidifier = (FluidSolidifierTE) te;
            solidifier.restoreReinforcedGlass();

            for (int i = 0; i < solidifier.getSizeInventory(); ++i) {
                ItemStack itemstack = solidifier.getStackInSlot(i);

                if (itemstack != null) {
                    float f = this.rand.nextFloat() * 0.8F + 0.1F;
                    float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
                    float f2 = this.rand.nextFloat() * 0.8F + 0.1F;

                    while (itemstack.stackSize > 0) {
                        int j = this.rand.nextInt(21) + 1;

                        if (j > itemstack.stackSize) {
                            j = itemstack.stackSize;
                        }

                        itemstack.stackSize -= j;
                        EntityItem entityitem = new EntityItem(world, (double) ((float) x + f), (double) ((float) y + f1), (double) ((float) z + f2), new ItemStack(itemstack.getItem(), j, itemstack.getItemDamage()));

                        if (itemstack.hasTagCompound()) {
                            entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
                        }

                        float f3 = 0.05F;
                        entityitem.motionX = (double) ((float) this.rand.nextGaussian() * f3);
                        entityitem.motionY = (double) ((float) this.rand.nextGaussian() * f3 + 0.2F);
                        entityitem.motionZ = (double) ((float) this.rand.nextGaussian() * f3);
                        world.spawnEntityInWorld(entityitem);
                    }
                }
            }
            world.func_147453_f(x, y, z, block);
        }

        super.breakBlock(world, x, y, z, block, meta);
        world.removeTileEntity(x, y, z);
    }
}
