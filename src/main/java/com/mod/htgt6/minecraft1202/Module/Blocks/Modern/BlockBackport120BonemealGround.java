package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import java.util.Random;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.util.ForgeDirection;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.World.Modern.MossBonemeal120;

/** Moss and rooted dirt bonemeal entry points adapted to the native 1.7 IGrowable contract. */
public final class BlockBackport120BonemealGround extends BlockBackport120Simple implements IGrowable {
    private final boolean moss;
    public BlockBackport120BonemealGround(boolean moss) {
        super(moss ? Material.grass : Material.ground, moss ? "moss_block" : "rooted_dirt",
                moss ? 0.1F : 0.5F, moss ? 0.1F : 0.5F,
                moss ? soundTypeGrass : soundTypeGravel, "shovel", 0);
        this.moss = moss;
    }
    @Override public boolean func_149851_a(World world, int x, int y, int z, boolean client) {
        int targetY = y + (moss ? 1 : -1);
        return targetY >= 0 && targetY < world.getHeight() && world.isAirBlock(x, targetY, z);
    }
    @Override public boolean func_149852_a(World world, Random random, int x, int y, int z) { return true; }
    @Override public void func_149853_b(World world, Random random, int x, int y, int z) {
        if (world.isRemote || !func_149851_a(world, x, y, z, false)) return;
        if (moss) MossBonemeal120.generate(world, random, x, y + 1, z);
        else world.setBlock(x, y - 1, z, Backport120Content.hangingRoots, 0, 3);
    }
    @Override public boolean canSustainPlant(IBlockAccess world, int x, int y, int z,
            ForgeDirection direction, IPlantable plant) {
        // Both blocks belong to the modern dirt support tag; native 1.7 grasses ask this Forge hook.
        return direction == ForgeDirection.UP && plant.getPlantType(world, x, y + 1, z) == EnumPlantType.Plains
                || super.canSustainPlant(world, x, y, z, direction, plant);
    }
}
