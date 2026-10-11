package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.BlockSign;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import java.util.Random;

/** Cherry standing/wall sign pair backed by the native 1.7.10 sign protocol. */
public final class BlockBackport120Sign extends BlockSign {
    private boolean netherMaterial;
    @Override public net.minecraft.block.material.Material getMaterial(){return netherMaterial?com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherWoodMaterial116.MATERIAL:super.getMaterial();}

    private Item signItem;

    public BlockBackport120Sign(String name, boolean standing) {
        super(TileEntityBackport120Sign.class, standing);
        netherMaterial=com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherWoodMaterial116.matches(name);
        setBlockName("backport_1_20." + name);
        setHardness(1.0F);
        setResistance(Backport120Content.legacyResistance(1.0F));
        setStepSound(soundTypeWood);
    }

    public void setSignItem(Item item) { signItem = item; }

    @Override public Item getItemDropped(int metadata, Random random, int fortune) { return signItem; }
    @Override public Item getItem(World world, int x, int y, int z) { return signItem; }

    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                              int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && world.getTileEntity(x, y, z) instanceof TileEntityBackport120Sign)
            player.func_146100_a((TileEntityBackport120Sign) world.getTileEntity(x, y, z));
        return true;
    }
}
