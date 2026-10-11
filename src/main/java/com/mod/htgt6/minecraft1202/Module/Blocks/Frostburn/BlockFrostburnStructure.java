package com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn;

import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/**
 * Operator-only structure editor. Metadata represents save, load, corner and data.
 */
public final class BlockFrostburnStructure extends Block {
    private final IIcon[] modeIcons = new IIcon[4];

    public BlockFrostburnStructure() {
        super(Material.iron);
        setBlockName("structure_block");
        setBlockUnbreakable();
        setResistance(6000000.0F);
        disableStats();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = register.registerIcon("htgt6:frostburn/structure_block");
        modeIcons[0] = register.registerIcon("htgt6:frostburn/structure_block_save");
        modeIcons[1] = register.registerIcon("htgt6:frostburn/structure_block_load");
        modeIcons[2] = register.registerIcon("htgt6:frostburn/structure_block_corner");
        modeIcons[3] = register.registerIcon("htgt6:frostburn/structure_block_data");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
        return metadata >= 0 && metadata < modeIcons.length ? modeIcons[metadata] : blockIcon;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        if (!player.capabilities.isCreativeMode
                || !player.canCommandSenderUseCommand(2, "")) return false;
        if (!world.isRemote) {
            net.minecraft.tileentity.TileEntity tile=world.getTileEntity(x,y,z);
            if(tile instanceof TileEntityStructure120) {
                if(player instanceof net.minecraft.entity.player.EntityPlayerMP)
                    ((net.minecraft.entity.player.EntityPlayerMP)player).playerNetServerHandler.sendPacket(tile.getDescriptionPacket());
                player.openGui(com.mod.htgt6.minecraft1202.Minecraft1202.instance,
                        com.mod.htgt6.minecraft1202.Module.Network.Frostburn.StructureNetwork120.GUI_ID,world,x,y,z);
            }
        }
        return true;
    }

    @Override public int quantityDropped(java.util.Random random) { return 0; }
    @Override public boolean canEntityDestroy(net.minecraft.world.IBlockAccess world,int x,int y,int z,net.minecraft.entity.Entity entity) { return false; }
    @Override public boolean hasTileEntity(int metadata) { return true; }
    @Override public net.minecraft.tileentity.TileEntity createTileEntity(World world,int metadata) { return new TileEntityStructure120(); }
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block neighbor) {
        net.minecraft.tileentity.TileEntity tile=world.getTileEntity(x,y,z);
        if(tile instanceof TileEntityStructure120)((TileEntityStructure120)tile).redstone(world.isBlockIndirectlyGettingPowered(x,y,z));
    }
    @Override public int getRenderType() { return FrostburnContent.getJsonRenderId(); }
}
