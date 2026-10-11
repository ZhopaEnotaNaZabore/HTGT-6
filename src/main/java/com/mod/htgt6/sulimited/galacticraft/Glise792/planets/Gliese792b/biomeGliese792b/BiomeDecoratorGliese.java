package com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b.biomeGliese792b;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeDecoratorSpace;
import net.minecraft.world.World;

public class BiomeDecoratorGliese extends BiomeDecoratorSpace {
    /** Chunk-local scoria boulders. No world reads, neighbour loads or populate-order dependence. */
    public void decorateColumns(long seed,int cx,int cz,net.minecraft.block.Block[] blocks,byte[] meta) {
        java.util.Random random=new java.util.Random(com.mod.htgt6.sulimited.world.GlieseTerrain.hash(seed+401,cx,cz));
        for(int i=0;i<5;i++) {
            int x=2+random.nextInt(12),z=2+random.nextInt(12);
            com.mod.htgt6.sulimited.world.GlieseTerrain.VolcanicSample t=com.mod.htgt6.sulimited.world.GlieseTerrain.volcanic(seed,cx*16+x,cz*16+z);
            if(t.lavaLevel>=t.height || t.dryChannel || t.wasteland || random.nextBoolean()) continue;
            for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) {
                int column=((x+dx)*16+z+dz)*256;
                int y=t.height+1;
                if(blocks[column+y]!=null || blocks[column+y-1]==null || !blocks[column+y-1].getMaterial().isSolid()) continue;
                blocks[column+y]=com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b.blocks.B_database.scoria;
                if(dx==0 && dz==0) blocks[column+y+1]=blocks[column+y];
            }
        }
    }
    private World currentWorld;
    protected void decorate() {}

    protected void setCurrentWorld(World world) {
        this.currentWorld = world;
    }

    protected World getCurrentWorld() {
        return this.currentWorld;
    }
}
