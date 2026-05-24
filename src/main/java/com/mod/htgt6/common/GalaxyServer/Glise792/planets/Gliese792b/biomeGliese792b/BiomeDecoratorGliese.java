package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792b.biomeGliese792b;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeDecoratorSpace;
import net.minecraft.world.World;

public class BiomeDecoratorGliese extends BiomeDecoratorSpace {
    private World currentWorld;
    protected void decorate() {}

    protected void setCurrentWorld(World world) {
        this.currentWorld = world;
    }

    protected World getCurrentWorld() {
        return this.currentWorld;
    }
}
