package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

/** Random-patch primitive for Futura flowers and sweet berry bushes. */
public final class WorldGenBackport120PlantPatch extends WorldGenerator {
    private final Block plant;private final int metadata,tries,radius;
    public WorldGenBackport120PlantPatch(Block plant,int metadata,int tries,int radius){this.plant=plant;this.metadata=metadata;this.tries=Math.max(1,tries);this.radius=Math.max(1,radius);}
    @Override public boolean generate(World w,Random r,int x,int y,int z){boolean placed=false;for(int i=0;i<tries;i++){
        int px=x+r.nextInt(radius*2+1)-radius,pz=z+r.nextInt(radius*2+1)-radius,py=w.getTopSolidOrLiquidBlock(px,pz);
        if(w.isAirBlock(px,py,pz)&&plant.canPlaceBlockAt(w,px,py,pz)){w.setBlock(px,py,pz,plant,metadata,2);placed=true;}}
        return placed;}
}
