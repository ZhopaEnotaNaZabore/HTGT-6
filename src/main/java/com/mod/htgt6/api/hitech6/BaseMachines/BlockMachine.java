package com.mod.htgt6.api.hitech6.BaseMachines;


import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.TE.LineAssemblerTE;
import com.mod.htgt6.common.TE.MetalFormerTE;
import com.mod.htgt6.common.TE.TEElectromagneticProcessor;
import com.mod.htgt6.common.handler.GuiHandler;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.List;

public class BlockMachine extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon iconAssemblerSide;
    @SideOnly(Side.CLIENT)
    private IIcon iconAssemblerFront;
    @SideOnly(Side.CLIENT)
    private IIcon iconAssemblerTop;

    @SideOnly(Side.CLIENT)
    private IIcon iconEmProcessorSide;
    @SideOnly(Side.CLIENT)
    private IIcon iconEmProcessorFront;
    @SideOnly(Side.CLIENT)
    private IIcon iconEmProcessorTop;

    // Metal Former Textures
    @SideOnly(Side.CLIENT)
    private IIcon iconMetalFormerSide;
    @SideOnly(Side.CLIENT)
    private IIcon iconMetalFormerFront;
    @SideOnly(Side.CLIENT)
    private IIcon iconMetalFormerTop;

    public BlockMachine() {
        super(Material.iron);
        setHardness(3.0F);
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        // Meta 0
        this.iconAssemblerSide  = com.mod.htgt6.client.render.MachineAtlasSprites.register(iconRegister, "htgt6:assembler_side");
        this.iconAssemblerFront = com.mod.htgt6.client.render.MachineAtlasSprites.register(iconRegister, "htgt6:machine_front");
        this.iconAssemblerTop   = com.mod.htgt6.client.render.MachineAtlasSprites.register(iconRegister, "htgt6:assembler_top");

        // Meta 1
        this.iconEmProcessorSide  = com.mod.htgt6.client.render.MachineAtlasSprites.register(iconRegister, "htgt6:assembler_side");
        this.iconEmProcessorFront = com.mod.htgt6.client.render.MachineAtlasSprites.register(iconRegister, "htgt6:em_processor_front");
        this.iconEmProcessorTop   = com.mod.htgt6.client.render.MachineAtlasSprites.register(iconRegister, "htgt6:assembler_side");

        // Meta 2 - Metal Former
        this.iconMetalFormerSide  = com.mod.htgt6.client.render.MachineAtlasSprites.register(iconRegister, "htgt6:assembler_side");
        this.iconMetalFormerFront = com.mod.htgt6.client.render.MachineAtlasSprites.register(iconRegister, "htgt6:metal_former_front");
        this.iconMetalFormerTop   = com.mod.htgt6.client.render.MachineAtlasSprites.register(iconRegister, "htgt6:assembler_top");

        this.blockIcon = this.iconAssemblerSide;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        int meta = world.getBlockMetadata(x, y, z);
        TileEntity te = world.getTileEntity(x, y, z);

        if (meta == 0) {
            int facing = (te instanceof LineAssemblerTE) ? ((LineAssemblerTE) te).getFacing() : 3;
            if (side == 1) return this.iconAssemblerTop;
            if (side == facing) return this.iconAssemblerFront;
            return this.iconAssemblerSide;
        }

        if (meta == 1) {
            // Updated to handle facing properly for Electromagnetic Processor if implemented
            int facing = 3;
            if (side == 1) return this.iconEmProcessorTop;
            if (side == facing) return this.iconEmProcessorFront;
            return this.iconEmProcessorSide;
        }

        if (meta == 2) {
            int facing = 3;
            if (side == 1) return this.iconMetalFormerTop;
            if (side == facing) return this.iconMetalFormerFront;
            return this.iconMetalFormerSide;
        }

        return this.blockIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (meta == 0) {
            if (side == 1) return this.iconAssemblerTop;
            if (side == 3) return this.iconAssemblerFront;
            return this.iconAssemblerSide;
        }

        if (meta == 1) {
            if (side == 1) return this.iconEmProcessorTop;
            if (side == 3) return this.iconEmProcessorFront;
            return this.iconEmProcessorSide;
        }
        if (meta == 2) {
            if (side == 1) return this.iconMetalFormerTop;
            if (side == 3) return this.iconMetalFormerFront;
            return this.iconMetalFormerSide;
        }


        return this.blockIcon;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        // Set metadata on placement according to held Item damage
        int meta = stack.getItemDamage();
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);

        int l = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        byte facing = 2;

        if (l == 0) facing = 2;
        if (l == 1) facing = 5;
        if (l == 2) facing = 3;
        if (l == 3) facing = 4;

        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof LineAssemblerTE) {
            ((LineAssemblerTE) te).setFacing(facing);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        switch (meta) {
            case 0: return new LineAssemblerTE();
            case 1: return new TEElectromagneticProcessor();
            case 2: return new MetalFormerTE();
            default: return null;
        }
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
        list.add(new ItemStack(item, 1, 2));
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        TileEntity te = world.getTileEntity(x, y, z);

        if (te instanceof LineAssemblerTE) {
            player.openGui(HTGT6.instance, GuiHandler.LINE_ASSEMBLER_ID, world, x, y, z);
            return true;
        }

        if (te instanceof TEElectromagneticProcessor) {
            // Pass explicit GUI ID (11) instead of meta to avoid conflicts
            player.openGui(HTGT6.instance, GuiHandler.ELECTROMAGNETIC_PROCESSOR_ID, world, x, y, z);
            return true;
        }

        if (te instanceof MetalFormerTE) {
            player.openGui(HTGT6.instance, GuiHandler.MF_ID, world, x, y, z);
            return true;
        }

        return false;
    }
}
