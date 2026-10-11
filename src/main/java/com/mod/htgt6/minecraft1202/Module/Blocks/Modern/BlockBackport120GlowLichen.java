package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.BlockVine;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

/** Non-growing luminous vine rendered by Minecraft 1.7's native vine renderer. */
public final class BlockBackport120GlowLichen extends BlockVine {
    public BlockBackport120GlowLichen(){
        super();
        setBlockName("backport_1_20.glow_lichen");setHardness(.2F);setResistance(Backport120Content.legacyResistance(.2F));
        setStepSound(soundTypeGrass);setLightLevel(7F/15F);
        setBlockTextureName("htgt6:1_20_backport/glow_lichen");
        setTickRandomly(false);
    }

    /** Keep vanilla vine attachment behaviour, but never spread. */
    @Override public void updateTick(World world,int x,int y,int z,Random random){ }

    /** Glow lichen has its own colour and must not receive biome foliage tint. */
    @Override public int getBlockColor(){return 0xFFFFFF;}
    @Override public int getRenderColor(int metadata){return 0xFFFFFF;}
    @Override public int colorMultiplier(IBlockAccess world,int x,int y,int z){return 0xFFFFFF;}
}
