package com.mod.htgt6.minecraft1202.Module.World.Frostburn;

import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Underground fossils assembled from the sixteen original Minecraft 1.10 NBT templates. */
public final class FrostburnFossilGenerator implements IWorldGenerator {
    private static final String[] NAMES = {
            "fossil_spine_01", "fossil_spine_02", "fossil_spine_03", "fossil_spine_04",
            "fossil_skull_01", "fossil_skull_02", "fossil_skull_03", "fossil_skull_04"
    };
    private static final Map<String, Template> CACHE = new HashMap<String, Template>();
    private static Field tagListField;

    public static void validateTemplates() {
        for (String name : NAMES) {
            load(name);
            load(name + "_coal");
        }
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                         IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if(com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120.isFutura(world))return;
        if (world.provider.dimensionId != 0 || !world.getWorldInfo().isMapFeaturesEnabled()
                || random.nextInt(64) != 0) return;
        int centerX = (chunkX << 4) + 8;
        int centerZ = (chunkZ << 4) + 8;
        BiomeGenBase biome = world.getBiomeGenForCoords(centerX, centerZ);
        if (biome != BiomeGenBase.desert && biome != BiomeGenBase.desertHills
                && biome != BiomeGenBase.swampland) return;

        String name = NAMES[random.nextInt(NAMES.length)];
        Template bones = load(name);
        Template coal = load(name + "_coal");
        int rotation = random.nextInt(4);
        int width = (rotation & 1) == 0 ? bones.sizeX : bones.sizeZ;
        int depth = (rotation & 1) == 0 ? bones.sizeZ : bones.sizeX;
        if (width > 16 || depth > 16) return;
        int baseX = (chunkX << 4) + random.nextInt(17 - width);
        int baseZ = (chunkZ << 4) + random.nextInt(17 - depth);
        int surface = minimumSurface(world, baseX, baseZ, width, depth);
        // FossilFeature clamps to minBuildHeight+10. Futura's translated
        // physical min build height is already zero; adding +64 here would
        // incorrectly delete fossils from the modern -64..-1 layer.
        int baseY = fossilBaseY(world.getActualHeight(),bones.sizeY,surface,random.nextInt(10));
        place(world, random, bones, baseX, baseY, baseZ, rotation, 0.9F, false);
        place(world, random, coal, baseX, baseY, baseZ, rotation, 0.1F, true);
    }

    public static int fossilBaseY(int actualHeight,int templateHeight,int surface,int randomOffset){
        return Math.max(10,Math.min(actualHeight-1-templateHeight,surface-15-randomOffset));
    }

    /** Modern FossilFeature placement, called by its own configured/placed pass. */
    public static void generateConfigured(World world,Random random,com.google.gson.JsonObject config,int x,int y,int z){
        int rotation=random.nextInt(4),selected=random.nextInt(config.getAsJsonArray("fossil_structures").size());
        Template bones=load(config.getAsJsonArray("fossil_structures").get(selected).getAsString());
        Template overlay=load(config.getAsJsonArray("overlay_structures").get(selected).getAsString());
        int width=(rotation&1)==0?bones.sizeX:bones.sizeZ,depth=(rotation&1)==0?bones.sizeZ:bones.sizeX;
        int bx=x-width/2,bz=z-depth/2,minimum=y;
        for(int dx=0;dx<width;dx++)for(int dz=0;dz<depth;dz++)minimum=Math.min(minimum,
                com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaPlacementExecutor120.height(world,"OCEAN_FLOOR_WG",bx+dx,bz+dz));
        int by=Math.max(minimum-15-random.nextInt(10),10),empty=0;
        for(int dx:new int[]{0,width-1})for(int dy:new int[]{0,bones.sizeY-1})for(int dz:new int[]{0,depth-1}){
            Block b=world.getBlock(bx+dx,by+dy,bz+dz);if(b==Blocks.air||b.getMaterial().isLiquid())empty++;
        }
        if(empty>config.get("max_empty_corners_allowed").getAsInt())return;
        placeConfigured(world,random,bones,bx,by,bz,rotation,.9F,false);
        placeConfigured(world,random,overlay,bx,by,bz,rotation,.1F,config.get("overlay_processors").getAsString().equals("minecraft:fossil_diamonds"));
    }

    private static void placeConfigured(World world,Random random,Template template,int bx,int by,int bz,int rotation,float integrity,boolean diamonds){
        List<BlockEntry> ordered=new ArrayList<BlockEntry>(template.blocks);
        java.util.Collections.sort(ordered,(a,b)->a.y!=b.y?Integer.compare(a.y,b.y):a.x!=b.x?Integer.compare(a.x,b.x):Integer.compare(a.z,b.z));
        for(BlockEntry entry:ordered){
            State state=template.palette[entry.state];if(state.name.equals("minecraft:structure_void"))continue;
            if(random.nextFloat()>integrity)continue;
            int[] point=rotate(entry.x,entry.z,template.sizeX,template.sizeZ,rotation);
            int x=bx+point[0],y=by+entry.y,z=bz+point[1];Block old=world.getBlock(x,y,z);
            if(old==Blocks.bedrock||old==Blocks.chest||old==Blocks.mob_spawner||old==Blocks.end_portal_frame
                    ||old==com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.reinforcedDeepslate)continue;
            Block block;int meta=0;
            if(state.name.equals("minecraft:bone_block")){block=FrostburnContent.boneBlock;meta=boneMetadata(state.axis,rotation);}
            else if(state.name.equals("minecraft:coal_ore"))block=diamonds?com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.deepslateDiamondOre:Blocks.coal_ore;
            else if(state.name.equals("minecraft:air"))block=Blocks.air;
            else throw new IllegalStateException("Unsupported fossil palette: "+state.name);
            world.setBlock(x,y,z,block,meta,2);
        }
    }


    private static int minimumSurface(World world, int x, int z, int width, int depth) {
        int minimum = world.getActualHeight()-1;
        for (int dx = 0; dx < width; ++dx) for (int dz = 0; dz < depth; ++dz) {
            minimum = Math.min(minimum, world.getHeightValue(x + dx, z + dz));
        }
        return minimum;
    }

    private static void place(World world, Random random, Template template, int baseX, int baseY,
                              int baseZ, int rotation, float integrity, boolean coalPass) {
        for (BlockEntry entry : template.blocks) {
            if (entry.state < 0 || entry.state >= template.palette.length || random.nextFloat() > integrity) continue;
            State state = template.palette[entry.state];
            boolean coal = "minecraft:coal_ore".equals(state.name);
            boolean bone = "minecraft:bone_block".equals(state.name);
            if (coalPass ? !coal : !bone) continue;
            int[] point = rotate(entry.x, entry.z, template.sizeX, template.sizeZ, rotation);
            int x = baseX + point[0], y = baseY + entry.y, z = baseZ + point[1];
            if (y <= 4 || y >= world.getActualHeight()-1 || !canReplace(world.getBlock(x, y, z))) continue;
            if (coal) world.setBlock(x, y, z, Blocks.coal_ore, 0, 2);
            else world.setBlock(x, y, z, FrostburnContent.boneBlock,
                    boneMetadata(state.axis, rotation), 2);
        }
    }

    private static boolean canReplace(Block block) {
        return block == Blocks.stone || block == Blocks.dirt || block == Blocks.gravel
                || block == com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.deepslate
                || block == Blocks.coal_ore || block == Blocks.air;
    }

    private static int boneMetadata(String axis, int rotation) {
        if ("y".equals(axis)) return 0;
        boolean swap = (rotation & 1) != 0;
        if ("x".equals(axis)) return swap ? 8 : 4;
        return swap ? 4 : 8;
    }

    private static int[] rotate(int x, int z, int sizeX, int sizeZ, int rotation) {
        switch (rotation & 3) {
            case 1: return new int[]{sizeZ - 1 - z, x};
            case 2: return new int[]{sizeX - 1 - x, sizeZ - 1 - z};
            case 3: return new int[]{z, sizeX - 1 - x};
            default: return new int[]{x, z};
        }
    }

    private static synchronized Template load(String name) {
        Template cached = CACHE.get(name);
        if (cached != null) return cached;
        String path = name.startsWith("minecraft:")?"/assets/htgt6/structures/"+name.substring(10)+".nbt"
                :"/assets/htgt6/structures/fossils/" + name + ".nbt";
        InputStream input = FrostburnFossilGenerator.class.getResourceAsStream(path);
        if (input == null) throw new IllegalStateException("Missing Frostburn fossil template: " + path);
        try {
            NBTTagCompound root = CompressedStreamTools.readCompressed(input);
            int[] size = intList(root.getTagList("size", 3));
            NBTTagList paletteTag = root.getTagList("palette", 10);
            State[] palette = new State[paletteTag.tagCount()];
            for (int i = 0; i < palette.length; ++i) {
                NBTTagCompound state = paletteTag.getCompoundTagAt(i);
                String axis = state.hasKey("Properties", 10)
                        ? state.getCompoundTag("Properties").getString("axis") : "y";
                palette[i] = new State(state.getString("Name"), axis);
            }
            List<BlockEntry> blocks = new ArrayList<BlockEntry>();
            NBTTagList blockTag = root.getTagList("blocks", 10);
            for (int i = 0; i < blockTag.tagCount(); ++i) {
                NBTTagCompound block = blockTag.getCompoundTagAt(i);
                int[] pos = intList(block.getTagList("pos", 3));
                blocks.add(new BlockEntry(pos[0], pos[1], pos[2], block.getInteger("state")));
            }
            Template template = new Template(size[0], size[1], size[2], palette, blocks);
            CACHE.put(name, template);
            return template;
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read Frostburn fossil template: " + path, error);
        } finally {
            try { input.close(); } catch (IOException ignored) { }
        }
    }

    private static int[] intList(NBTTagList list) {
        List<NBTBase> values = rawList(list);
        int[] result = new int[values.size()];
        for (int i = 0; i < result.length; ++i) result[i] = ((NBTBase.NBTPrimitive) values.get(i)).func_150287_d();
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<NBTBase> rawList(NBTTagList list) {
        try {
            if (tagListField == null) for (Field field : NBTTagList.class.getDeclaredFields()) {
                if (List.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    tagListField = field;
                    break;
                }
            }
            return (List<NBTBase>) tagListField.get(list);
        } catch (Exception error) {
            throw new IllegalStateException("Cannot access fossil NBT list", error);
        }
    }

    private static final class Template {
        final int sizeX, sizeY, sizeZ;
        final State[] palette;
        final List<BlockEntry> blocks;
        Template(int x, int y, int z, State[] palette, List<BlockEntry> blocks) {
            sizeX = x; sizeY = y; sizeZ = z; this.palette = palette; this.blocks = blocks;
        }
    }

    private static final class State {
        final String name, axis;
        State(String name, String axis) { this.name = name; this.axis = axis; }
    }

    private static final class BlockEntry {
        final int x, y, z, state;
        BlockEntry(int x, int y, int z, int state) { this.x = x; this.y = y; this.z = z; this.state = state; }
    }
}
