package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c.biomeGliese792c;





import java.util.Random;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeDecoratorSpace;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenShrub;
import net.minecraft.world.gen.feature.WorldGenerator;

public class BiomeDecoratorGlieseC extends BiomeDecoratorSpace {

    private World currentWorld;
    protected void decorate() {}

    protected void setCurrentWorld(World world) {
        this.currentWorld = world;
    }

    protected World getCurrentWorld() {
        return this.currentWorld;
    }
}