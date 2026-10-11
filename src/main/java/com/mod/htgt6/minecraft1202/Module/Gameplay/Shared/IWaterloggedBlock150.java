package com.mod.htgt6.minecraft1202.Module.Gameplay.Shared;
import net.minecraft.world.IBlockAccess;
/** A source fluid coexisting with a block; never substitutes the block's solid material. */
public interface IWaterloggedBlock150 {
 boolean isWaterlogged(IBlockAccess world,int x,int y,int z);
 boolean setWaterlogged(net.minecraft.world.World world,int x,int y,int z,boolean wet);
}
