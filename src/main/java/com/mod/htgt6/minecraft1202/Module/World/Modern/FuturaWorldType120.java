package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.asm.HTGT6Config;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

/** Isolated world-type routing boundary for every later Futura generation stage. */
public final class FuturaWorldType120 extends WorldType {
    public static final String KEY = "futura";
    private static FuturaWorldType120 instance;

    private FuturaWorldType120() { super(KEY); }

    public static synchronized FuturaWorldType120 registerIdentity() {
        if (instance == null) {
            WorldType occupied=WorldType.parseWorldType(KEY);
            if(occupied!=null)throw new IllegalStateException("WorldType key '"+KEY
                    +"' is already registered by "+occupied.getClass().getName());
            instance = new FuturaWorldType120();
        }
        return instance;
    }

    public static synchronized FuturaWorldType120 register() {
        registerIdentity();
        if (HTGT6Config.ENABLE_EXTENDED_BIOME_ASM) {
            FuturaBiomeRegistry120.register();
            FuturaWorldChunkManager120.validateContract();
        }
        return instance;
    }

    public static FuturaWorldType120 instance() {
        if (instance == null) throw new IllegalStateException("Futura world type is not registered");
        return instance;
    }

    public static boolean isFutura(World world) {
        return instance != null && world != null && world.getWorldInfo() != null
                && world.getWorldInfo().getTerrainType() == instance;
    }

    /** Safe identity check for terrain-generation events that do not expose a World. */
    public static boolean isFutura(WorldType type) {
        return instance != null && type == instance;
    }

    @Override public WorldChunkManager getChunkManager(World world) {
        requireExactType(world);
        requireEnabled();
        return new FuturaWorldChunkManager120(world.getSeed());
    }

    @Override public IChunkProvider getChunkGenerator(World world, String generatorOptions) {
        requireExactType(world);
        requireEnabled();
        return new ChunkProviderFutura120(world);
    }

    @Override public int getMinimumSpawnHeight(World world) { return 128; }
    @Override public double getHorizon(World world) { return 127.0D; }
    @Override @SideOnly(Side.CLIENT) public float getCloudHeight() {
        return FuturaHeight120.cloudHeight();
    }
    @Override public boolean isCustomizable() { return false; }

    @Override @SideOnly(Side.CLIENT)
    public boolean getCanBeCreated() {
        return HTGT6Config.RUN_MINECRAFT_1_7_9999 && HTGT6Config.ENABLE_EXTENDED_BIOME_ASM;
    }

    private void requireExactType(World world) {
        if (!isFutura(world))
            throw new IllegalStateException("Futura generator requested for a non-Futura world");
    }

    private static void requireEnabled() {
        if (!HTGT6Config.RUN_MINECRAFT_1_7_9999 || !HTGT6Config.ENABLE_EXTENDED_BIOME_ASM)
            throw new IllegalStateException("Futura cannot load unless backports and extended-biome ASM are enabled");
    }
}
