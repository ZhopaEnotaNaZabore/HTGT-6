package com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage;
import com.mod.htgt6.minecraft1202.Module.Blocks.Shared.BlockLantern120;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
/** Ordinary lantern: shared modern placement/shape, original registry identity. */
public final class BlockVillagePillageLantern extends BlockLantern120 {
    public BlockVillagePillageLantern(){super("village_pillage_lantern","htgt6:village_pillage/lantern",1F);}
    @Override public int getRenderType(){return VillagePillageContent.getJsonRenderId();}
}
