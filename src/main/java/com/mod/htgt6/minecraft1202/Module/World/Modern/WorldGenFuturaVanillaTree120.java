package com.mod.htgt6.minecraft1202.Module.World.Modern;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

/**
 * Futura adaptation of the 1.20.2 trunk/foliage configurations for the wood
 * species already present in 1.7.10. It changes shape only; block identities
 * remain the original vanilla logs and leaves.
 */
public final class WorldGenFuturaVanillaTree120 extends WorldGenerator {
    public enum Shape { OAK, BIRCH, SPRUCE, JUNGLE, ACACIA, DARK_OAK, SWAMP, NONE }

    private static final WorldGenFuturaVanillaTree120[] GENERATORS =
            new WorldGenFuturaVanillaTree120[Shape.values().length];

    static {
        for (Shape value : Shape.values())
            if (value != Shape.NONE) GENERATORS[value.ordinal()] = new WorldGenFuturaVanillaTree120(value);
    }

    private final Shape shape;

    public WorldGenFuturaVanillaTree120(Shape shape) {
        super(false);
        this.shape = shape;
    }

    public static WorldGenFuturaVanillaTree120 generatorFor(Shape shape) {
        return shape == null ? null : GENERATORS[shape.ordinal()];
    }

    public static void validateContract() {
        requireShape(BiomeGenBase.forest, Shape.OAK);
        requireShape(BiomeGenBase.birchForest, Shape.BIRCH);
        requireShape(BiomeGenBase.taiga, Shape.SPRUCE);
        requireShape(BiomeGenBase.jungle, Shape.JUNGLE);
        requireShape(BiomeGenBase.savanna, Shape.ACACIA);
        requireShape(BiomeGenBase.roofedForest, Shape.DARK_OAK);
        requireShape(BiomeGenBase.swampland, Shape.SWAMP);
        // Extended biomes are intentionally absent when their ASM/config gate is off.
        // Validate their mapping only after that registry has actually been installed.
        BiomeGenBase grove = FuturaBiomeRegistry120.get(FuturaBiomeIds120.GROVE);
        if (grove != null) {
            requireShape(grove, Shape.SPRUCE);
            requireShape(FuturaBiomeRegistry120.get(FuturaBiomeIds120.BAMBOO_JUNGLE), Shape.JUNGLE);
            requireShape(FuturaBiomeRegistry120.get(FuturaBiomeIds120.CHERRY_GROVE), Shape.NONE);
            requireShape(FuturaBiomeRegistry120.get(FuturaBiomeIds120.MANGROVE_SWAMP), Shape.NONE);
        }
    }

    private static void requireShape(BiomeGenBase biome, Shape expected) {
        Shape actual = shapeFor(biome);
        if (actual != expected) throw new IllegalStateException("Futura tree mapping for biome "
                + (biome == null ? "null" : biome.biomeID) + " is " + actual + ", expected " + expected);
    }

    public static Shape shapeFor(BiomeGenBase biome) {
        if (biome == null) return Shape.NONE;
        int id = biome.biomeID;
        if (id == FuturaBiomeIds120.CHERRY_GROVE || id == FuturaBiomeIds120.MANGROVE_SWAMP
                || id == FuturaBiomeIds120.MEADOW || id == FuturaBiomeIds120.SNOWY_SLOPES
                || id == FuturaBiomeIds120.JAGGED_PEAKS || id == FuturaBiomeIds120.FROZEN_PEAKS
                || id == FuturaBiomeIds120.STONY_PEAKS) return Shape.NONE;
        if (id == FuturaBiomeIds120.GROVE) return Shape.SPRUCE;
        if (id == FuturaBiomeIds120.BAMBOO_JUNGLE) return Shape.JUNGLE;
        if (id == 29 || id == 157) return Shape.DARK_OAK;
        if (id == 27 || id == 28 || id == 155 || id == 156) return Shape.BIRCH;
        if (id == 35 || id == 36 || id == 163 || id == 164) return Shape.ACACIA;
        if (id == 21 || id == 22 || id == 23 || id == 149 || id == 151) return Shape.JUNGLE;
        if (id == 5 || id == 19 || id == 30 || id == 31 || id == 32 || id == 33
                || id == 133 || id == 158 || id == 160 || id == 161) return Shape.SPRUCE;
        if (id == 6 || id == 134) return Shape.SWAMP;
        if (id == 4 || id == 18 || id == 132 || id == 1 || id == 3 || id == 20
                || id == 34 || id == 131 || id == 162) return Shape.OAK;
        return Shape.NONE;
    }

    @Override
    public boolean generate(World world, Random random, int x, int y, int z) {
        y = groundY(world, x, y, z, shape == Shape.SWAMP);
        if (y < 1) return false;
        switch (shape) {
            case BIRCH: return straight(world, random, x, y, z, 2, 2, 5 + random.nextInt(3), true);
            case SPRUCE: return spruce(world, random, x, y, z);
            case JUNGLE: return jungle(world, random, x, y, z);
            case ACACIA: return acacia(world, random, x, y, z);
            case DARK_OAK: return darkOak(world, random, x, y, z);
            case SWAMP: return swamp(world, random, x, y, z);
            case OAK:
                return random.nextInt(10) == 0 ? fancyOak(world, random, x, y, z)
                        : straight(world, random, x, y, z, 0, 0, 4 + random.nextInt(3), false);
            default: return false;
        }
    }

    /** Configured identity is selected before shape generation; no second species lottery. */
    public static boolean generateConfigured(World w, Random r, String key, com.google.gson.JsonObject config, int x, int y, int z) {
        if(FuturaStraightTree120.supports(config))return FuturaStraightTree120.generate(w,r,config,x,y,z);
        if(FuturaGiantTree120.supports(config))return FuturaGiantTree120.generate(w,r,config,x,y,z);
        String name=key.substring(key.indexOf(':')+1);
        if(name.startsWith("cherry"))return FuturaTreeFeatureRuntime120.cherry(w,r,x,y,z);
        if(name.equals("mangrove")||name.equals("tall_mangrove"))return FuturaTreeFeatureRuntime120.mangrove(w,r,x,y,z,name.startsWith("tall_"));
        com.google.gson.JsonObject trunk=config.getAsJsonObject("trunk_placer");
        int height=trunk.get("base_height").getAsInt()+r.nextInt(trunk.get("height_rand_a").getAsInt()+1)+r.nextInt(trunk.get("height_rand_b").getAsInt()+1);
        if(name.startsWith("fancy_oak")){java.util.Set<String> before=BeeNestDecorator115.snapshot(w,x,y,z);boolean done=fancyOak(w,r,x,y,z,height);if(done)BeeNestDecorator115.legacy(w,r,config,x,y,z,before);return done;}
        if(name.contains("birch"))return straight(w,r,x,y,z,2,2,height,true);
        if(name.equals("spruce")||name.equals("pine")||name.startsWith("mega_spruce")||name.equals("mega_pine"))return spruce(w,r,x,y,z,name.startsWith("mega_"),height);
        if(name.equals("mega_jungle_tree"))return jungle(w,r,x,y,z,true,height);
        if(name.equals("jungle_tree"))return straight(w,r,x,y,z,3,3,height,false);
        if(name.equals("jungle_bush"))return straight(w,r,x,y,z,3,0,height,false);
        if(name.equals("acacia"))return acacia(w,r,x,y,z,height);
        if(name.equals("dark_oak"))return darkOak(w,r,x,y,z,height);
        if(name.equals("swamp_oak"))return swamp(w,r,x,y,z,height);
        if(name.equals("oak")||name.startsWith("oak_bees"))return straight(w,r,x,y,z,0,0,height,false);
        throw new IllegalArgumentException("Unimplemented configured tree: "+key);
    }

    public static boolean saplingSurvives(World w,String name,int x,int y,int z){
        if(y<1||y>=w.getHeight())return false;
        Block below=w.getBlock(x,y-1,z);
        return below==Blocks.grass||below==Blocks.dirt||below==Blocks.farmland
                ||below==com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.rootedDirt
                ||below==com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.mossBlock
                ||(name.equals("minecraft:mangrove_propagule")&&below==Blocks.clay);
    }

    /** StraightTrunkPlacer with Blob/Birch foliage placer. */
    private static boolean straight(World world, Random random, int x, int y, int z,
                                    int wood, int leaves, int height, boolean birch) {
        if (!space(world, x, y, z, height, 3) || !soil(world, x, y - 1, z)) return false;
        for (int i = 0; i < height; i++) log(world, x, y + i, z, wood, 0);
        int top = y + height;
        for (int dy = birch ? -3 : -2; dy <= 1; dy++) {
            int radius = dy == 1 ? 1 : dy == 0 ? 2 : (birch && dy == -3 ? 1 : 2);
            leavesDisk(world, random, x, top + dy, z, radius, leaves, dy == 1);
        }
        return true;
    }

    /** FancyTrunkPlacer: tall trunk, offset limbs and separate blob attachments. */
    private static boolean fancyOak(World world, Random random, int x, int y, int z) {
        return fancyOak(world,random,x,y,z,7 + random.nextInt(5));
    }
    private static boolean fancyOak(World world, Random random, int x, int y, int z,int height) {
        if (!space(world, x, y, z, height + 2, 4) || !soil(world, x, y - 1, z)) return false;
        for (int i = 0; i < height; i++) log(world, x, y + i, z, 0, 0);
        blob(world, random, x, y + height, z, 3, 0);
        int branches = 2 + random.nextInt(3);
        for (int branch = 0; branch < branches; branch++) {
            int direction = random.nextInt(4), length = 2 + random.nextInt(2);
            int dx = direction == 0 ? 1 : direction == 1 ? -1 : 0;
            int dz = direction == 2 ? 1 : direction == 3 ? -1 : 0;
            int by = y + height - 3 - random.nextInt(3);
            for (int step = 1; step <= length; step++)
                log(world, x + dx * step, by + step / 2, z + dz * step, 0,
                        dx != 0 ? 4 : 8);
            blob(world, random, x + dx * length, by + length / 2 + 1,
                    z + dz * length, 2, 0);
        }
        return true;
    }

    /** SprucePineFoliagePlacer and MegaPineFoliagePlacer configurations. */
    private static boolean spruce(World world, Random random, int x, int y, int z) {
        boolean mega = random.nextInt(8) == 0;
        int height = mega ? 13 + random.nextInt(7) : 7 + random.nextInt(5);
        return spruce(world,random,x,y,z,mega,height);
    }
    private static boolean spruce(World world,Random random,int x,int y,int z,boolean mega,int height){
        int width = mega ? 1 : 0;
        if (!space(world, x, y, z, height + 1, mega ? 4 : 3)
                || !soilSquare(world, x, y - 1, z, width)) return false;
        for (int i = 0; i < height; i++) for (int dx = 0; dx <= width; dx++)
            for (int dz = 0; dz <= width; dz++) log(world, x + dx, y + i, z + dz, 1, 0);
        int top = y + height;
        int foliageHeight = mega ? 8 + random.nextInt(4) : 4 + random.nextInt(3);
        for (int layer = 0; layer <= foliageHeight; layer++) {
            int radius = mega ? Math.min(4, 1 + layer / 2) : Math.min(3, (layer + 1) / 2);
            if (layer == foliageHeight) radius = Math.max(1, radius - 1);
            leavesDisk(world, random, x, top - layer, z, radius, 1, layer == 0);
        }
        return true;
    }

    /** MegaJungleTrunkPlacer with branch foliage attachments. */
    private static boolean jungle(World world, Random random, int x, int y, int z) {
        boolean mega = random.nextInt(5) == 0;
        if (!mega) return straight(world, random, x, y, z, 3, 3, 5 + random.nextInt(4), false);
        return jungle(world,random,x,y,z,true,14 + random.nextInt(8));
    }
    private static boolean jungle(World world,Random random,int x,int y,int z,boolean mega,int height){
        if (!space(world, x, y, z, height + 2, 5) || !soilSquare(world, x, y - 1, z, 1)) return false;
        for (int i = 0; i < height; i++) for (int dx = 0; dx < 2; dx++) for (int dz = 0; dz < 2; dz++)
            log(world, x + dx, y + i, z + dz, 3, 0);
        blob(world, random, x, y + height, z, 4, 3);
        for (int by = y + height - 4; by > y + height / 2; by -= 3 + random.nextInt(2)) {
            int direction = random.nextInt(4), length = 2 + random.nextInt(3);
            int dx = direction == 0 ? 1 : direction == 1 ? -1 : 0;
            int dz = direction == 2 ? 1 : direction == 3 ? -1 : 0;
            for (int step = 1; step <= length; step++)
                log(world, x + dx * step, by + step / 2, z + dz * step, 3, dx != 0 ? 4 : 8);
            blob(world, random, x + dx * length, by + length / 2 + 1, z + dz * length, 2, 3);
        }
        vines(world, random, x, y, z, height);
        return true;
    }

    /** ForkingTrunkPlacer plus flat AcaciaFoliagePlacer. */
    private static boolean acacia(World world, Random random, int x, int y, int z) {
        return acacia(world,random,x,y,z,5 + random.nextInt(4));
    }
    private static boolean acacia(World world, Random random, int x, int y, int z,int height) {
        if (!space(world, x, y, z, height + 3, 4) || !soil(world, x, y - 1, z)) return false;
        int direction = random.nextInt(4), dx = direction == 0 ? 1 : direction == 1 ? -1 : 0;
        int dz = direction == 2 ? 1 : direction == 3 ? -1 : 0;
        int bendStart = height - 2 - random.nextInt(2), bx = x, bz = z;
        for (int i = 0; i < height; i++) {
            if (i >= bendStart) { bx += dx; bz += dz; }
            log(world, bx, y + i, bz, 0, dx != 0 ? 4 : dz != 0 ? 8 : 0, true);
        }
        flatCanopy(world, random, bx, y + height, bz, 0);
        int second = (direction + 1 + random.nextInt(2)) & 3;
        int sx = second == 0 ? 1 : second == 1 ? -1 : 0;
        int sz = second == 2 ? 1 : second == 3 ? -1 : 0;
        int branchY = y + height - 3;
        for (int step = 1; step <= 2; step++)
            log(world, x + sx * step, branchY + step, z + sz * step, 0, sx != 0 ? 4 : 8, true);
        flatCanopy(world, random, x + sx * 2, branchY + 3, z + sz * 2, 0);
        return true;
    }

    /** DarkOakTrunkPlacer with a compact 2x2 crown and short limbs. */
    private static boolean darkOak(World world, Random random, int x, int y, int z) {
        return darkOak(world,random,x,y,z,6+random.nextInt(4));
    }
    private static boolean darkOak(World world, Random random, int x, int y, int z,int height) {
        if (!space(world, x, y, z, height + 3, 4) || !soilSquare(world, x, y - 1, z, 1)) return false;
        for (int i = 0; i < height; i++) for (int dx = 0; dx < 2; dx++) for (int dz = 0; dz < 2; dz++)
            log(world, x + dx, y + i, z + dz, 1, 0, true);
        int top = y + height;
        for (int dy = -1; dy <= 1; dy++) leavesDisk(world, random, x, top + dy, z,
                dy == 0 ? 3 : 2, 1, false, true);
        for (int direction = 0; direction < 4; direction++) if (random.nextBoolean()) {
            int dx = direction == 0 ? 1 : direction == 1 ? -1 : 0;
            int dz = direction == 2 ? 1 : direction == 3 ? -1 : 0;
            log(world, x + dx * 2, top - 2, z + dz * 2, 1, dx != 0 ? 4 : 8, true);
        }
        return true;
    }

    private static boolean swamp(World world, Random random, int x, int y, int z) {
        return swamp(world,random,x,y,z,5 + random.nextInt(3));
    }
    private static boolean swamp(World world, Random random, int x, int y, int z,int height) {
        if (!space(world, x, y, z, height, 3, true) || !soil(world, x, y - 1, z)) return false;
        for (int i = 0; i < height; i++) logReplacingWater(world, x, y + i, z, 0, 0);
        int top = y + height;
        for (int dy = -2; dy <= 1; dy++)
            leavesDisk(world, random, x, top + dy, z, dy == 1 ? 1 : 2, 0, dy == 1);
        vines(world, random, x, y + 2, z, 6);
        return true;
    }

    private static void flatCanopy(World world, Random random, int x, int y, int z, int leaves) {
        leavesDisk(world, random, x, y, z, 3, leaves, false, true);
        leavesDisk(world, random, x, y + 1, z, 2, leaves, true, true);
    }

    private static void blob(World world, Random random, int x, int y, int z, int radius, int leaves) {
        for (int dy = -2; dy <= 1; dy++) {
            int layerRadius = Math.max(1, radius - Math.max(0, Math.abs(dy) - 1));
            leavesDisk(world, random, x, y + dy, z, layerRadius, leaves, dy == 1);
        }
    }

    private static void leavesDisk(World world, Random random, int x, int y, int z,
                                   int radius, int leaves, boolean sparse) {
        leavesDisk(world, random, x, y, z, radius, leaves, sparse, false);
    }

    private static void leavesDisk(World world, Random random, int x, int y, int z,
                                   int radius, int leaves, boolean sparse, boolean secondSet) {
        Block leafBlock = secondSet ? Blocks.leaves2 : Blocks.leaves;
        for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
            if (Math.abs(dx) == radius && Math.abs(dz) == radius && (sparse || random.nextBoolean())) continue;
            int px = x + dx, pz = z + dz;
            if (canReplace(world, px, y, pz)) world.setBlock(px, y, pz, leafBlock, leaves, 2);
        }
    }

    private static void vines(World world, Random random, int x, int y, int z, int height) {
        for (int yy = y; yy < y + height; yy++) {
            if (random.nextInt(3) == 0 && world.isAirBlock(x - 1, yy, z)) world.setBlock(x - 1, yy, z, Blocks.vine, 8, 2);
            if (random.nextInt(3) == 0 && world.isAirBlock(x + 1, yy, z)) world.setBlock(x + 1, yy, z, Blocks.vine, 2, 2);
            if (random.nextInt(3) == 0 && world.isAirBlock(x, yy, z - 1)) world.setBlock(x, yy, z - 1, Blocks.vine, 1, 2);
            if (random.nextInt(3) == 0 && world.isAirBlock(x, yy, z + 1)) world.setBlock(x, yy, z + 1, Blocks.vine, 4, 2);
        }
    }

    private static boolean space(World world, int x, int y, int z, int height, int radius) {
        return space(world, x, y, z, height, radius, false);
    }

    private static boolean space(World world, int x, int y, int z, int height, int radius,
                                 boolean waterInTrunkColumn) {
        if (y < 1 || y + height + 1 >= world.getHeight()) return false;
        for (int yy = y; yy <= y + height; yy++) {
            int r = yy < y + height - 3 ? 1 : radius;
            for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
                if (canReplace(world, x + dx, yy, z + dz)) continue;
                Block block = world.getBlock(x + dx, yy, z + dz);
                if (!(waterInTrunkColumn && dx == 0 && dz == 0
                        && (block == Blocks.water || block == Blocks.flowing_water))) return false;
            }
        }
        return true;
    }

    private static int groundY(World world, int x, int y, int z, boolean allowWater) {
        y = Math.min(world.getHeight() - 2, y);
        while (y > 1) {
            Block below = world.getBlock(x, y - 1, z);
            if (below == Blocks.snow_layer || below == Blocks.tallgrass || below == Blocks.vine) {
                y--;
            } else if (allowWater && (below == Blocks.water || below == Blocks.flowing_water)) y--;
            else break;
        }
        return y;
    }

    private static boolean soil(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return block == Blocks.grass || block == Blocks.dirt || block == Blocks.farmland;
    }

    private static boolean soilSquare(World world, int x, int y, int z, int width) {
        for (int dx = 0; dx <= width; dx++) for (int dz = 0; dz <= width; dz++)
            if (!soil(world, x + dx, y, z + dz)) return false;
        return true;
    }

    private static boolean canReplace(World world, int x, int y, int z) {
        if (y < 0 || y >= world.getHeight()) return false;
        Block block = world.getBlock(x, y, z);
        return block.isAir(world, x, y, z) || block.isLeaves(world, x, y, z)
                || block == Blocks.vine || block == Blocks.tallgrass || block == Blocks.snow_layer;
    }

    private static void log(World world, int x, int y, int z, int wood, int axis) {
        log(world, x, y, z, wood, axis, false);
    }

    private static void log(World world, int x, int y, int z, int wood, int axis, boolean secondSet) {
        if (canReplace(world, x, y, z))
            world.setBlock(x, y, z, secondSet ? Blocks.log2 : Blocks.log, wood | axis, 2);
    }

    private static void logReplacingWater(World world, int x, int y, int z, int wood, int axis) {
        Block block = world.getBlock(x, y, z);
        if (canReplace(world, x, y, z) || block == Blocks.water || block == Blocks.flowing_water)
            world.setBlock(x, y, z, Blocks.log, wood | axis, 2);
    }
}
