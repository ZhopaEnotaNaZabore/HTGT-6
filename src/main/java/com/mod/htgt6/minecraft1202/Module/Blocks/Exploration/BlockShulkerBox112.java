package com.mod.htgt6.minecraft1202.Module.Blocks.Exploration;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Portable 1.12 shulker-box inventory with BlockEntityTag-preserving drops. */
public final class BlockShulkerBox112 extends BlockContainer {
    private final String colorName;
    private IIcon icon;

    public BlockShulkerBox112(String colorName) {
        super(Material.rock);
        this.colorName = colorName;
        setBlockName("undyed".equals(colorName) ? "shulker_box" : colorName + "_shulker_box");
        setHardness(2.0F);
        setResistance(Backport120Content.legacyResistance(2.0F));
        setCreativeTab(CreativeTabs.tabDecorations);
    }

    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileEntityShulkerBox112(); }
    public String getColorName() { return colorName; }
    @Override public int getRenderType() { return -1; }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int onBlockPlaced(World world,int x,int y,int z,int side,float hitX,float hitY,float hitZ,int meta) {
        return ShulkerBoxShape112.metadata(side);
    }
    @Override public net.minecraft.util.AxisAlignedBB getCollisionBoundingBoxFromPool(World world,int x,int y,int z) {
        return shape(world,x,y,z).offset(x,y,z);
    }
    private static net.minecraft.util.AxisAlignedBB shape(IBlockAccess world,int x,int y,int z) {
        TileEntity tile=world.getTileEntity(x,y,z);
        float progress=tile instanceof TileEntityShulkerBox112?((TileEntityShulkerBox112)tile).getProgress(1):0;
        return ShulkerBoxShape112.bounds(world.getBlockMetadata(x,y,z),progress*.5D);
    }
    @Override public void setBlockBoundsBasedOnState(IBlockAccess world,int x,int y,int z) {
        net.minecraft.util.AxisAlignedBB box=shape(world,x,y,z);
        setBlockBounds((float)box.minX,(float)box.minY,(float)box.minZ,(float)box.maxX,(float)box.maxY,(float)box.maxZ);
    }
    @Override public boolean onBlockEventReceived(World world, int x, int y, int z, int id, int value) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile != null && tile.receiveClientEvent(id, value);
    }

    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
            int side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityShulkerBox112)) return false;
        if (!world.isRemote && canOpen(world,x,y,z,(TileEntityShulkerBox112)tile)) {
            TileEntityShulkerBox112 box=(TileEntityShulkerBox112)tile;
            if (box.canUnlock(player.getHeldItem())) player.displayGUIChest(box);
            else player.addChatMessage(new net.minecraft.util.ChatComponentTranslation("htgt6.container.isLocked", box.hasCustomInventoryName() ? new net.minecraft.util.ChatComponentText(box.getInventoryName()) : new net.minecraft.util.ChatComponentTranslation(box.getInventoryName())));
        }
        return true;
    }
    public static boolean canOpen(World world,int x,int y,int z,TileEntityShulkerBox112 tile) {
        if(!tile.isFullyClosed())return true;
        double epsilon=1.0E-6D;
        net.minecraft.util.AxisAlignedBB sweep=ShulkerBoxShape112.sweep(world.getBlockMetadata(x,y,z),0,.5D)
                .offset(x,y,z).expand(-epsilon,-epsilon,-epsilon);
        if(!world.func_147461_a(sweep).isEmpty())return false;
        for(Object value:world.getEntitiesWithinAABBExcludingEntity(null,sweep)) {
            net.minecraft.entity.Entity entity=(net.minecraft.entity.Entity)value;
            net.minecraft.util.AxisAlignedBB collision=entity.getBoundingBox();
            if(collision!=null&&collision.intersectsWith(sweep))return false;
        }
        return true;
    }

    @Override public void onBlockPlacedBy(World world, int x, int y, int z,
            EntityLivingBase placer, ItemStack stack) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityShulkerBox112)) return;
        TileEntityShulkerBox112 box = (TileEntityShulkerBox112)tile;
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("BlockEntityTag", 10))
            box.readInventory(stack.getTagCompound().getCompoundTag("BlockEntityTag"));
        if (stack.hasDisplayName()) box.setCustomName(stack.getDisplayName());
    }

    @Override public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!world.isRemote && tile instanceof TileEntityShulkerBox112) {
            dropBlockAsItem(world, x, y, z, portableStack((TileEntityShulkerBox112)tile));
            world.func_147453_f(x, y, z, this);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    public ItemStack portableStack(TileEntityShulkerBox112 tile) {
        ItemStack drop = new ItemStack(this);
        if (tile == null) return drop;
        NBTTagCompound blockTag = new NBTTagCompound();
        tile.writeInventory(blockTag);
        if (blockTag.getTagList("Items", 10).tagCount() > 0
                || blockTag.hasKey("CustomName", 8) || blockTag.hasKey("Lock", 8)) {
            NBTTagCompound root = new NBTTagCompound(); root.setTag("BlockEntityTag", blockTag); drop.setTagCompound(root);
        }
        if (tile.hasCustomInventoryName()) drop.setStackDisplayName(tile.getInventoryName());
        return drop;
    }

    @Override public ItemStack getPickBlock(net.minecraft.util.MovingObjectPosition target, World world, int x, int y, int z) {
        TileEntity tile=world.getTileEntity(x,y,z);
        return portableStack(tile instanceof TileEntityShulkerBox112 ? (TileEntityShulkerBox112)tile : null);
    }

    @Override public boolean isSideSolid(IBlockAccess world,int x,int y,int z,net.minecraftforge.common.util.ForgeDirection side) {
        if (side == net.minecraftforge.common.util.ForgeDirection.UNKNOWN) return false;
        TileEntity tile=world.getTileEntity(x,y,z);
        return !(tile instanceof TileEntityShulkerBox112) || ((TileEntityShulkerBox112)tile).isFullyClosed()
                || side == ShulkerBoxShape112.direction(world.getBlockMetadata(x,y,z)).getOpposite();
    }

    @Override public Item getItemDropped(int meta, java.util.Random random, int fortune) { return null; }
    @Override public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
        return new ArrayList<ItemStack>();
    }
    @Override public boolean hasComparatorInputOverride() { return true; }
    @Override public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileEntityShulkerBox112
                ? net.minecraft.inventory.Container.calcRedstoneFromInventory((TileEntityShulkerBox112)tile) : 0;
    }
    @Override public boolean isNormalCube(IBlockAccess world, int x, int y, int z) { return true; }

    @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister register) {
        icon = register.registerIcon("htgt6:exploration_update/shulker_box/" + colorName);
        blockIcon = icon;
    }
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int side, int meta) { return icon; }
}
