package com.mod.htgt6.minecraft1202.Module.Blocks.Exploration;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Two-tick Minecraft 1.12 observer pulse adapted to the 1.7 metadata/redstone API. */
public final class BlockObserver112 extends Block {
    private static final int POWERED = 8;
    private IIcon front, back, backOn, side, top;

    public BlockObserver112() {
        super(Material.rock);
        setBlockName("observer");
        setHardness(3.0F);
        setResistance(Backport120Content.legacyResistance(3.0F));
        setCreativeTab(CreativeTabs.tabRedstone);
        setStepSound(soundTypeStone);
    }

    @Override public void onBlockAdded(World world, int x, int y, int z) {
        if (!world.isRemote && (world.getBlockMetadata(x, y, z) & POWERED) != 0)
            world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) & 7, 2);
        schedulePulse(world,x,y,z);
    }

    @Override public void breakBlock(World world,int x,int y,int z,Block block,int meta) {
        if(!world.isRemote&&(meta&POWERED)!=0)notifyOutput(world,x,y,z,meta&7);
        super.breakBlock(world,x,y,z,block,meta);
    }

    @Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        // Directional state updates arrive through ObserverUpdates120; this callback has no source coordinates.
    }

    public void observedChange(World world,int x,int y,int z,int changedX,int changedY,int changedZ) {
        net.minecraftforge.common.util.ForgeDirection facing=net.minecraftforge.common.util.ForgeDirection.getOrientation(world.getBlockMetadata(x,y,z)&7);
        if(changedX==x+facing.offsetX&&changedY==y+facing.offsetY&&changedZ==z+facing.offsetZ)
            schedulePulse(world,x,y,z);
    }

    private void schedulePulse(World world, int x, int y, int z) {
        if (!world.isRemote && (world.getBlockMetadata(x, y, z) & POWERED) == 0
                && !world.isBlockTickScheduledThisTick(x, y, z, this)) {
            world.scheduleBlockUpdate(x, y, z, this, 2);
        }
    }

    @Override public void updateTick(World world, int x, int y, int z, Random random) {
        int meta = world.getBlockMetadata(x, y, z);
        if ((meta & POWERED) != 0) {
            world.setBlockMetadataWithNotify(x, y, z, meta & 7, 3);
        } else {
            world.setBlockMetadataWithNotify(x, y, z, meta | POWERED, 3);
            world.scheduleBlockUpdate(x, y, z, this, 2);
        }
        notifyOutput(world, x, y, z, meta & 7);
    }

    private void notifyOutput(World world, int x, int y, int z, int facing) {
        int[] dx = {0, 0, 0, 0, -1, 1};
        int[] dy = {-1, 1, 0, 0, 0, 0};
        int[] dz = {0, 0, -1, 1, 0, 0};
        int opposite = facing ^ 1;
        world.notifyBlockOfNeighborChange(x + dx[opposite], y + dy[opposite], z + dz[opposite], this);
        world.notifyBlocksOfNeighborChange(x + dx[opposite], y + dy[opposite], z + dz[opposite], this);
    }

    @Override public boolean canProvidePower() { return true; }

    @Override public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int sideId) {
        int meta = world.getBlockMetadata(x, y, z);
        // The query side points from the receiving block towards this source.
        return (meta & POWERED) != 0 && sideId == (meta & 7) ? 15 : 0;
    }

    @Override public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int sideId) {
        return isProvidingWeakPower(world, x, y, z, sideId);
    }

    @Override public int onBlockPlaced(World world, int x, int y, int z, int sideId,
            float hitX, float hitY, float hitZ, int meta) {
        return sideId ^ 1;
    }

    @Override public void onBlockPlacedBy(World world, int x, int y, int z,
            EntityLivingBase placer, ItemStack stack) {
        int facing;
        if (MathHelper.abs((float)placer.posX - x) < 2.0F
                && MathHelper.abs((float)placer.posZ - z) < 2.0F) {
            double eye = placer.posY + placer.getEyeHeight();
            if (eye - y > 2.0D) facing = 1;
            else if (y - eye > 0.0D) facing = 0;
            else facing = horizontalFacing(placer.rotationYaw);
        } else facing = horizontalFacing(placer.rotationYaw);
        world.setBlockMetadataWithNotify(x, y, z, facing, 2);
    }

    private int horizontalFacing(float yaw) {
        switch (MathHelper.floor_double(yaw * 4.0F / 360.0F + 0.5D) & 3) {
            case 0: return 2;
            case 1: return 5;
            case 2: return 3;
            default: return 4;
        }
    }

    @Override @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        front = register.registerIcon("htgt6:exploration_update/observer_front");
        back = register.registerIcon("htgt6:exploration_update/observer_back");
        backOn = register.registerIcon("htgt6:exploration_update/observer_back_on");
        side = register.registerIcon("htgt6:exploration_update/observer_side");
        top = register.registerIcon("htgt6:exploration_update/observer_top");
        blockIcon = side;
    }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(int renderedSide, int meta) {
        int facing = meta & 7;
        if (renderedSide == facing) return front;
        if (renderedSide == (facing ^ 1)) return (meta & POWERED) != 0 ? backOn : back;
        if (renderedSide == 0 || renderedSide == 1) return top;
        return side;
    }
}
