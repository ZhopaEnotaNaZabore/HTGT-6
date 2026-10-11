//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mod.htgt6.common.block.hitech6;

import com.cleanroommc.modularui.manager.GuiInfos;
import com.mod.htgt6.common.TE.TileEntityAssembler;
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

public class BlockAssembler extends Block implements ITileEntityProvider {
    @SideOnly(Side.CLIENT)
    private IIcon iconFront;
    @SideOnly(Side.CLIENT)
    private IIcon iconTop;

    public BlockAssembler() {
        super(Material.iron);
        this.setBlockName("assembler");
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setStepSound(soundTypeMetal);
        this.isBlockContainer = true;
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) return false;
        if (!world.isRemote && world.getTileEntity(x, y, z) instanceof TileEntityAssembler) {
            GuiInfos.TILE_ENTITY.open(player, world, x, y, z);
        }
        return true;
    }

    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityAssembler();
    }

    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        int l = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5) & 3;
        if (l == 0) {
            world.setBlockMetadataWithNotify(x, y, z, 2, 2);
        }

        if (l == 1) {
            world.setBlockMetadataWithNotify(x, y, z, 5, 2);
        }

        if (l == 2) {
            world.setBlockMetadataWithNotify(x, y, z, 3, 2);
        }

        if (l == 3) {
            world.setBlockMetadataWithNotify(x, y, z, 4, 2);
        }

    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        this.blockIcon = com.mod.htgt6.client.render.MachineAtlasSprites.register(iconRegister, "htgt6:assembler_side");
        this.iconFront = com.mod.htgt6.client.render.MachineAtlasSprites.register(iconRegister, "htgt6:assembler_front");
        this.iconTop = com.mod.htgt6.client.render.MachineAtlasSprites.register(iconRegister, "htgt6:assembler_top");
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
        if (side == 1) {
            return this.iconTop;
        } else if (side == 0) {
            return this.blockIcon;
        } else if (metadata == 0 && side == 3) {
            return this.iconFront;
        } else {
            return side == metadata ? this.iconFront : this.blockIcon;
        }
    }

    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
        world.removeTileEntity(x, y, z);
    }
}
