package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;
import java.util.Random;

/** Modern button orientation with material-specific timing and arrow sensitivity on a 1.7.10 TileEntity state. */
public final class BlockBackport120Button extends BlockContainer {

    private final int pressTicks;
    private final boolean wooden;

    public BlockBackport120Button(String name, String texture) { this(name,texture,true); }
    public BlockBackport120Button(String name, String texture,boolean wooden) {
        super(Material.circuits);
        this.wooden=wooden;pressTicks=wooden?30:20;
        setBlockName("backport_1_20." + name);
        setBlockTextureName("htgt6:1_20_backport/" + texture);
        setHardness(0.5F);
        setResistance(Backport120Content.legacyResistance(0.5F));
        setStepSound(wooden?Block.soundTypeWood:com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.BuildingSounds116.STONE);
        if(!wooden)setHarvestLevel("pickaxe",0);
        setCreativeTab(CreativeTabs.tabRedstone);
        setTickRandomly(false);
    }

    @Override public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityBackport120Button();
    }

    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int tickRate(World world) { return pressTicks; }
    @Override public boolean canProvidePower() { return true; }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }

    @Override public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
        ForgeDirection outward = ForgeDirection.getOrientation(side);
        return world.isSideSolid(x - outward.offsetX, y - outward.offsetY, z - outward.offsetZ, outward);
    }

    @Override public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        for (int side = 0; side < 6; side++) if (canPlaceBlockOnSide(world, x, y, z, side)) return true;
        return false;
    }

    @Override public int onBlockPlaced(World world, int x, int y, int z, int side,
                                       float hitX, float hitY, float hitZ, int metadata) {
        return side;
    }

    @Override public void onBlockPlacedBy(World world, int x, int y, int z,
                                          EntityLivingBase placer, net.minecraft.item.ItemStack stack) {
        int side = world.getBlockMetadata(x, y, z);
        int face = side == 0 ? TileEntityBackport120Button.CEILING
                : side == 1 ? TileEntityBackport120Button.FLOOR : TileEntityBackport120Button.WALL;
        int facing;
        if (side >= 2) facing = new int[]{0, 0, 2, 0, 1, 3}[side];
        else facing = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityBackport120Button)
            ((TileEntityBackport120Button) tile).setOrientation(face, facing);
        world.setBlockMetadataWithNotify(x, y, z, 0, 2);
    }

    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                              int side, float hitX, float hitY, float hitZ) {
        if ((world.getBlockMetadata(x, y, z) & 1) != 0) return true;
        if (world.isRemote) return true;
        setPressed(world, x, y, z, true);
        world.scheduleBlockUpdate(x, y, z, this, pressTicks);
        return true;
    }

    @Override public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (wooden && !world.isRemote && (world.getBlockMetadata(x, y, z) & 1) == 0 && entity instanceof EntityArrow)
            updateArrowState(world, x, y, z);
    }

    @Override public void updateTick(World world, int x, int y, int z, Random random) {
        if (!world.isRemote && (world.getBlockMetadata(x, y, z) & 1) != 0) { if(wooden)updateArrowState(world,x,y,z);else setPressed(world,x,y,z,false); }
    }

    @Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (world.isRemote) return;
        TileEntityBackport120Button tile = tile(world, x, y, z);
        if (tile == null || hasSupport(world, x, y, z, tile)) return;
        dropBlockAsItem(world, x, y, z, 0, 0);
        world.setBlockToAir(x, y, z);
    }

    @Override public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return (world.getBlockMetadata(x, y, z) & 1) != 0 ? 15 : 0;
    }

    @Override public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
        if ((world.getBlockMetadata(x, y, z) & 1) == 0) return 0;
        TileEntityBackport120Button tile = tile(world, x, y, z);
        return tile != null && side == supportSide(tile) ? 15 : 0;
    }

    @Override public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        TileEntityBackport120Button tile = tile(world, x, y, z);
        setButtonBounds(tile == null ? TileEntityBackport120Button.WALL : tile.getFace(),
                tile == null ? 2 : tile.getFacing(), (world.getBlockMetadata(x, y, z) & 1) != 0);
    }

    private void updateArrowState(World world, int x, int y, int z) {
        setBlockBoundsBasedOnState(world, x, y, z);
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(x + minX, y + minY, z + minZ,
                x + maxX, y + maxY, z + maxZ);
        List arrows = world.getEntitiesWithinAABB(EntityArrow.class, box);
        boolean pressed = !arrows.isEmpty();
        boolean wasPressed = (world.getBlockMetadata(x, y, z) & 1) != 0;
        if (pressed != wasPressed) setPressed(world, x, y, z, pressed);
        if (pressed) world.scheduleBlockUpdate(x, y, z, this, pressTicks);
    }

    private void setPressed(World world, int x, int y, int z, boolean pressed) {
        if (!world.setBlockMetadataWithNotify(x, y, z, pressed ? 1 : 0, 3)) return;
        world.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
        world.notifyBlocksOfNeighborChange(x, y, z, this);
        TileEntityBackport120Button tile = tile(world, x, y, z);
        if (tile != null) {
            int side = supportSide(tile);
            ForgeDirection direction = ForgeDirection.getOrientation(side);
            world.notifyBlocksOfNeighborChange(x - direction.offsetX, y - direction.offsetY,
                    z - direction.offsetZ, this);
        }
        world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, wooden?"random.click":pressed?"htgt6:block.stone_button.click_on":"htgt6:block.stone_button.click_off", wooden ? 0.3F : 1.0F,
                wooden ? (pressed ? 0.6F : 0.5F) : 1.0F);
    }

    @Override public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        if (!world.isRemote && (metadata & 1) != 0) {
            world.notifyBlocksOfNeighborChange(x,y,z,this);
            TileEntityBackport120Button orientation=tile(world,x,y,z);
            if (orientation!=null) {
                ForgeDirection outward=ForgeDirection.getOrientation(supportSide(orientation));
                world.notifyBlocksOfNeighborChange(x-outward.offsetX,y-outward.offsetY,z-outward.offsetZ,this);
            }
        }
        super.breakBlock(world,x,y,z,block,metadata);
    }

    private static boolean hasSupport(World world, int x, int y, int z, TileEntityBackport120Button tile) {
        int side = supportSide(tile);
        ForgeDirection outward = ForgeDirection.getOrientation(side);
        return world.isSideSolid(x - outward.offsetX, y - outward.offsetY, z - outward.offsetZ, outward);
    }

    private static int supportSide(TileEntityBackport120Button tile) {
        if (tile.getFace() == TileEntityBackport120Button.FLOOR) return 1;
        if (tile.getFace() == TileEntityBackport120Button.CEILING) return 0;
        return new int[]{3, 4, 2, 5}[tile.getFacing() & 3];
    }

    private void setButtonBounds(int face, int facing, boolean pressed) {
        float depth = pressed ? 1.0F / 16.0F : 2.0F / 16.0F;
        float a = 5.0F / 16.0F, b = 11.0F / 16.0F;
        float c = 6.0F / 16.0F, d = 10.0F / 16.0F;
        if (face == TileEntityBackport120Button.FLOOR) {if((facing&1)==0)setBlockBounds(a,0,c,b,depth,d);else setBlockBounds(c,0,a,d,depth,b);}
        else if (face == TileEntityBackport120Button.CEILING) {if((facing&1)==0)setBlockBounds(a,1-depth,c,b,1,d);else setBlockBounds(c,1-depth,a,d,1,b);}
        else if (facing == 0) setBlockBounds(a, c, 0, b, d, depth);
        else if (facing == 2) setBlockBounds(a, c, 1 - depth, b, d, 1);
        else if (facing == 1) setBlockBounds(1 - depth, c, a, 1, d, b);
        else setBlockBounds(0, c, a, depth, d, b);
    }

    private static TileEntityBackport120Button tile(IBlockAccess world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileEntityBackport120Button ? (TileEntityBackport120Button) tile : null;
    }
}
