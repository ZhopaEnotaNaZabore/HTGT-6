package com.mod.htgt6.minecraft1202.Module.Blocks.Shared;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import gregapi.block.behaviors.Drops;
import gregapi.block.prefixblock.PrefixBlock_;
import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictPrefix;
import gregapi.render.BlockTextureCopied;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/** GT6 PrefixBlock whose world geometry is supplied by HTGT6's JSON model bridge. */
public final class BlockGT6DeepslateOre extends PrefixBlock_ {
    public BlockGT6DeepslateOre(String name, OreDictPrefix prefix, Drops drops, Block host,
                        float hardness, float resistance, int harvestOffset) {
        // HTGT6 owns the registry entry, while gregtech owns the generated ore-overlay icons.
        super("HTGT6", "gregtech", name, prefix, drops, BlockTextureCopied.get(host, 0),
                Material.rock, Block.soundTypePiston, "pickaxe", hardness, resistance,
                harvestOffset, 0, false, false, OreDictMaterial.MATERIAL_ARRAY);
    }

    @Override
    public int getRenderType() {
        return Backport120Content.getJsonRenderId();
    }
}
