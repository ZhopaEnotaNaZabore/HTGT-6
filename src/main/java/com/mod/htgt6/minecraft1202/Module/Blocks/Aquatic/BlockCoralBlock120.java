package com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/** Five live and five dead 1.20.2 coral blocks packed into 1.7 metadata. */
public final class BlockCoralBlock120 extends Block {
    private static final String[] TYPES = {"tube", "brain", "bubble", "fire", "horn"};
    private final IIcon[] icons = new IIcon[13];

    public BlockCoralBlock120() {
        super(Material.rock);
        setBlockName("coral_block");
        setHardness(1.5F);
        setResistance(Backport120Content.legacyResistance(6.0F));
        setStepSound(soundTypeStone);
        setCreativeTab(CreativeTabs.tabBlock);
        setTickRandomly(true);
    }

    @Override @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        for (int type = 0; type < 5; type++) {
            icons[type] = register.registerIcon(HTGT6.MOD_ID.toLowerCase() + ":"
                    + TYPES[type] + "_coral_block");
            icons[type + 8] = register.registerIcon(HTGT6.MOD_ID.toLowerCase() + ":dead_"
                    + TYPES[type] + "_coral_block");
        }
        blockIcon = icons[0];
    }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        int value = meta >= 8 ? Math.min(12, meta) : Math.min(4, meta);
        return icons[value];
    }

    @Override public int damageDropped(int meta) { return meta >= 8 ? Math.min(12, meta) : Math.min(4, meta); }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (world.getBlockMetadata(x, y, z) < 8) world.scheduleBlockUpdate(x, y, z, this, 60 + world.rand.nextInt(40));
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta < 8 && !hasAdjacentWater(world, x, y, z)) {
            world.setBlockMetadataWithNotify(x, y, z, meta + 8, 2);
        }
    }

    public static boolean isLive(Block block, int meta) {
        return block == OceanContent.coralBlock && meta >= 0 && meta < 5;
    }

    private static boolean hasAdjacentWater(World world, int x, int y, int z) {
        int[][] directions = {{0,1,0},{0,-1,0},{1,0,0},{-1,0,0},{0,0,1},{0,0,-1}};
        for (int[] direction : directions) if (BlockKelp.isWater(world.getBlock(
                x + direction[0], y + direction[1], z + direction[2]))) return true;
        return false;
    }

    @Override @SuppressWarnings({"rawtypes", "unchecked"})
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int type = 0; type < 5; type++) list.add(new ItemStack(item, 1, type));
        for (int type = 0; type < 5; type++) list.add(new ItemStack(item, 1, type + 8));
    }
}
