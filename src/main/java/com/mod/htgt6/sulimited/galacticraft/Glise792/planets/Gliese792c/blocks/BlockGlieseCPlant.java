package com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.Random;
import net.minecraft.init.Blocks;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysGeology;

/** Simple water-tolerant decorative plant. Metadata selects an algae/coral variant. */
public class BlockGlieseCPlant extends BlockBush {
    private final boolean coral;
    private int activeRenderPass;

    public BlockGlieseCPlant(boolean coral) {
        super(net.minecraft.block.material.Material.water);
        this.coral = coral;
        this.setHardness(0.1F);
        this.setStepSound(Block.soundTypeGrass);
        this.setTickRandomly(false);
    }

    @Override
    protected boolean canPlaceBlockOn(Block block) {
        return block instanceof BlockGlieseCPlant || block.getMaterial().isSolid();
    }

    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
        // This cell already contains water. Changes in neighbouring water levels
        // cannot dry it out or invalidate a rooted reef.
        return this.canPlaceBlockOn(world.getBlock(x, y - 1, z));
    }

    @Override
    public boolean isReplaceable(IBlockAccess world, int x, int y, int z) {
        // Flowing water must not replace mature underwater plants.
        return false;
    }

    /** Neighbour notifications must not invoke BlockBush's drop-then-air path. */
    @Override
    protected void checkAndDropBlock(World world, int x, int y, int z) {
        if (!world.isRemote && world.getBlock(x, y, z) == this && !canBlockStay(world, x, y, z)) {
            world.scheduleBlockUpdate(x, y, z, this, 1);
        }
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (!world.isRemote && world.getBlock(x, y, z) == this && !canBlockStay(world, x, y, z)) {
            // Natural loss of support restores the contained water without item entities.
            // Player harvesting still uses the normal block drop path.
            world.setBlock(x, y, z, water(), 0, 3);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block replacement, int metadata) {
        super.breakBlock(world, x, y, z, replacement, metadata);
        if (!world.isRemote && world.isAirBlock(x, y, z)) world.setBlock(x, y, z, water(), 0, 2);
    }

    private static Block water() { return SolSysGeology.saltWater == null ? Blocks.water : SolSysGeology.saltWater; }

    @Override public int getRenderType() { return SolSysGeology.aquaticRenderId; }
    @Override public int getRenderBlockPass() { return 1; }
    @Override public boolean canRenderInPass(int pass) {
        // Chunk compilation supplies the terrain pass here, not via Forge's entity render pass.
        activeRenderPass = pass;
        return pass == 0 || pass == 1;
    }
    public int getActiveRenderPass() { return activeRenderPass; }
    public boolean isCoral() { return coral; }

    @Override
    public int getRenderColor(int metadata) {
        if (!this.coral) return 0xC4EAB6;
        if (this == blocksGliese792C.deepCoral) return metadata % 2 == 0 ? 0xA8E9EB : 0xCCADFF;
        switch (metadata % 3) {
            case 0: return 0xD77A72;
            case 1: return 0xB981C9;
            default: return 0xE6B76B;
        }
    }

    @Override
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        return getRenderColor(world.getBlockMetadata(x, y, z));
    }

    @Override
    public IIcon getIcon(int side, int metadata) {
        return this.blockIcon;
    }
}
