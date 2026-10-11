package com.mod.htgt6.sulimited.worldgen.constructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;

/** Configurable depth layers equivalent to WorldEngine's WE_BiomeLayer. */
public final class SurfaceLayerConstructor implements SUColumnConstructor {
    private final List<Layer> layers = new ArrayList<Layer>();

    public SurfaceLayerConstructor add(Block block, int meta, Block replace, int replaceMeta,
            int minDepth, int randomDepth) {
        layers.add(new Layer(block, (byte) meta, replace, (byte) replaceMeta,
                Math.max(0, minDepth), Math.max(0, randomDepth)));
        return this;
    }

    public void generate(SUChunkContext context, int x, int z) {
        int top = context.topSolidY(x, z);
        Random random = context.featureRandom(0x53555246414345L, context.worldX(x), context.worldZ(z));
        int cursor = 0;
        for (Layer layer : layers) {
            int depth = layer.minDepth + (layer.randomDepth == 0 ? 0 : random.nextInt(layer.randomDepth + 1));
            for (int offset = cursor; offset < cursor + depth && top - offset > 0; offset++) {
                int y = top - offset;
                if (context.getBlock(x, y, z) == layer.replace
                        && context.getMetadata(x, y, z) == layer.replaceMeta) {
                    context.setBlock(x, y, z, layer.block, layer.meta);
                }
            }
            cursor += depth;
        }
    }

    private static final class Layer {
        final Block block, replace;
        final byte meta, replaceMeta;
        final int minDepth, randomDepth;
        Layer(Block block, byte meta, Block replace, byte replaceMeta, int minDepth, int randomDepth) {
            this.block=block;this.meta=meta;this.replace=replace;this.replaceMeta=replaceMeta;
            this.minDepth=minDepth;this.randomDepth=randomDepth;
        }
    }
}
