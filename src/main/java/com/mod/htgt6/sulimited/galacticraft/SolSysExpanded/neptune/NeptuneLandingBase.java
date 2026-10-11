package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.neptune;

import micdoodle8.mods.galacticraft.core.blocks.GCBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/** A safe AmunRa-style landing hull without mothership controller side effects. */
public final class NeptuneLandingBase {
    public static final int DECK_Y=200;
    private NeptuneLandingBase(){ }
    public static micdoodle8.mods.galacticraft.api.vector.Vector3 ensureGenerated(World world,int centerX,int centerZ){
        if (world.isRemote) throw new IllegalStateException("Landing sites are generated on the server");
        NeptuneLandingData data = NeptuneLandingData.get(world);
        int originX = centerX, originZ = centerZ;
        boolean found = false;
        for (int attempt = 0; attempt < 82; attempt++) {
            int offsetX = attempt == 0 ? 0 : ((attempt - 1) % 9 - 4) * 32;
            int offsetZ = attempt == 0 ? 0 : ((attempt - 1) / 9 - 4) * 32;
            centerX = originX + offsetX; centerZ = originZ + offsetZ;
            int border = micdoodle8.mods.galacticraft.core.util.ConfigManagerCore.otherPlanetWorldBorders;
            if (border > 22 && (Math.abs((long) centerX) + 4 >= border
                    || centerZ - 11L <= -border || centerZ + 4L >= border)) continue;
            if (data.contains(centerX, centerZ)) {
                if (world.getBlock(centerX, DECK_Y, centerZ) == GCBlocks.basicBlock
                        && clearApproach(world, centerX, centerZ)) return point(centerX, centerZ);
                continue; // Never rebuild a player-modified recorded site.
            }
            if (world.getBlock(centerX, DECK_Y, centerZ) == GCBlocks.basicBlock
                    && world.getBlockMetadata(centerX, DECK_Y, centerZ) == 4
                    && clearApproach(world, centerX, centerZ)) {
                data.record(centerX, centerZ);
                return point(centerX, centerZ);
            }
            if (emptySite(world, centerX, centerZ)) { found = true; break; }
        }
        if (!found) throw new IllegalStateException("No unobstructed Neptune landing site near arrival coordinates");
        // Four pressure pylons hold the deck above the Y=160 liquid boundary.
        for(int dx:new int[]{-4,4})for(int dz:new int[]{-4,4})for(int y=160;y<DECK_Y;y++)
            world.setBlock(centerX+dx,y,centerZ+dz,GCBlocks.basicBlock,4,2);
        // Nine-wide landing raft, using the same GC decorative metas as AmunRa's base generator.
        for(int dx=-4;dx<=4;dx++)for(int dz=-4;dz<=4;dz++){
            int meta=Math.abs(dx)==4||Math.abs(dz)==4?3:4;
            world.setBlock(centerX+dx,DECK_Y,centerZ+dz,GCBlocks.basicBlock,meta,2);
            for(int y=DECK_Y+1;y<=DECK_Y+10;y++)world.setBlockToAir(centerX+dx,y,centerZ+dz);
        }
        // Rear airlock/cabin, offset so the entry pod lands in the clear centre.
        int cabinZ=centerZ-8;
        for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)world.setBlock(centerX+dx,DECK_Y,cabinZ+dz,GCBlocks.basicBlock,4,2);
        for(int y=DECK_Y+1;y<=DECK_Y+3;y++)for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){
            boolean wall=Math.abs(dx)==3||Math.abs(dz)==3;
            if(!wall)world.setBlockToAir(centerX+dx,y,cabinZ+dz);
            else if(dz==3&&Math.abs(dx)<=1&&y<=DECK_Y+2)world.setBlockToAir(centerX+dx,y,cabinZ+dz);
            else world.setBlock(centerX+dx,y,cabinZ+dz,y==DECK_Y+2?Blocks.glass:GCBlocks.basicBlock,y==DECK_Y+2?0:3,2);
        }
        for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)world.setBlock(centerX+dx,DECK_Y+4,cabinZ+dz,GCBlocks.basicBlock,3,2);
        data.record(centerX, centerZ);
        return point(centerX, centerZ);
    }
    private static micdoodle8.mods.galacticraft.api.vector.Vector3 point(int x, int z) {
        return new micdoodle8.mods.galacticraft.api.vector.Vector3(x + 0.5, DECK_Y + 1, z + 0.5);
    }
    private static boolean clearApproach(World world, int x, int z) {
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++)
            for (int y = DECK_Y + 1; y < 256; y++)
                if (!world.isAirBlock(x + dx, y, z + dz)) return false;
        return true;
    }
    private static boolean emptySite(World world, int x, int z) {
        if (!clearApproach(world, x, z)) return false;
        for (int dx = -4; dx <= 4; dx++) for (int dz = -11; dz <= 4; dz++)
            for (int y = DECK_Y; y <= DECK_Y + 10; y++)
                if (!world.isAirBlock(x + dx, y, z + dz)) return false;
        for (int dx : new int[] {-4, 4}) for (int dz : new int[] {-4, 4})
            for (int y = 160; y < DECK_Y; y++)
                if (!world.isAirBlock(x + dx, y, z + dz)) return false;
        return true;
    }
}
