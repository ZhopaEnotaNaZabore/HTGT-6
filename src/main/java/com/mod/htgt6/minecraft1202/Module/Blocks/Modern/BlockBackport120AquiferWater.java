package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/**
 * Stable water written by the Futura aquifer sampler.
 *
 * <p>1.7.10 changes every notified still-water block to flowing water
 * synchronously while chunks are populated. A modern aquifer can contain
 * thousands of touching cells, so using vanilla still water for the raw
 * aquifer volume overflows the server stack. This block keeps vanilla water
 * material, collision and liquid rendering, but deliberately does not start a
 * recursive flow conversion when a neighbouring population feature is placed.
 * Surface oceans and rivers continue to use ordinary vanilla/GT6 water.</p>
 */
public final class BlockBackport120AquiferWater extends BlockStaticLiquid {
    public BlockBackport120AquiferWater() {
        super(Material.water);
        setBlockName("backport_1_20.aquifer_water");
        setHardness(100.0F);
        setLightOpacity(3);
        setCreativeTab(null);
        disableStats();
    }

    @Override
    public IIcon getIcon(int side, int metadata) {
        return Blocks.water.getIcon(side, metadata);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbour) {
        // Raw modern aquifers are resolved during density generation. Letting
        // 1.7 convert their complete volume here causes synchronous recursion.
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        // Raw chunk construction must not enqueue a fluid update for every cell.
    }
}
