package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;

import net.minecraft.potion.Potion;

/** Marker potion rendered with the pulsing 1.19+ Darkness vignette. */
public final class PotionDarkness120 extends Potion {
    public PotionDarkness120(int id){super(id,true,0x292721);setPotionName("potion.darkness");}
    @Override public boolean isReady(int duration,int amplifier){return false;}
}
