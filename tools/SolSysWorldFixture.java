import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.*;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysGeology.Profile;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.neptune.*;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import java.lang.reflect.Field;
import java.util.Arrays;

/** Behavioural checks for signed coordinates, region continuity and landing persistence. */
public final class SolSysWorldFixture {
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        net.minecraft.launchwrapper.Launch.classLoader = new net.minecraft.launchwrapper.LaunchClassLoader(
                ((java.net.URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs());
        World world = BountifulMechanicsFixture120.initialize();
        SolSysGeology.saltWater = Blocks.water;
        for (Profile p : Profile.values()) {
            p.strata = Blocks.stone; p.cryoFlow = Blocks.packed_ice; p.cryoVent = Blocks.obsidian;
            p.sediment = Blocks.clay; p.salt = Blocks.sandstone;
            int oceans = 0, land = 0, volcanoes = 0;
            for (int x = -4096; x <= 4096; x += 32) for (int z = -4096; z <= 4096; z += 32) {
                SolSysTerrainFeatures.Sample a = SolSysTerrainFeatures.sample(p, 12345, x, z, 72);
                SolSysTerrainFeatures.Sample b = SolSysTerrainFeatures.sample(p, 12345, x + 1, z, 72);
                require(a.height >= 12 && a.height <= 220, "height bounds " + p);
                require(Math.abs(a.height - b.height) <= 4, "terrain seam " + p + " " + x + "," + z);
                if (a.ocean) oceans++; else land++;
                if (a.vent) volcanoes++;
            }
            require(land > 0, "land " + p);
            require(p.hasOceans() ? oceans > 0 && volcanoes > 0 : oceans == 0 && volcanoes == 0, "regions " + p);
            for (int cx : new int[] {-41, -1, 0, 1, 41}) {
                Block[] a = flat(), b = flat(); byte[] am = new byte[65536], bm = new byte[65536];
                SolSysTerrainFeatures.apply(p, 9, cx, -cx, a, am);
                SolSysTerrainFeatures.apply(p, 9, cx, -cx, b, bm);
                require(Arrays.equals(a, b) && Arrays.equals(am, bm), "repeatability " + p);
                for (int column = 0; column < 256; column++) {
                    require(a[column * 256] == Blocks.bedrock, "bedrock " + p);
                    require(a[column * 256 + 255] == null, "clear sky " + p);
                    for (int y = 65; y < 256; y++) require(a[column * 256 + y] != Blocks.water, "ocean level " + p);
                }
            }
            System.out.println(p + ": ocean samples=" + oceans + ", land=" + land + ", vents=" + volcanoes);
        }
        NeptuneLandingData data = new NeptuneLandingData("test");
        data.record(-17, 31); data.record(17, -31);
        NBTTagCompound tag = new NBTTagCompound(); data.writeToNBT(tag);
        NeptuneLandingData restored = new NeptuneLandingData("test"); restored.readFromNBT(tag);
        require(restored.contains(-17, 31) && restored.contains(17, -31) && !restored.contains(17,31), "landing NBT");
        Field storage = World.class.getDeclaredField("mapStorage"); storage.setAccessible(true); storage.set(world, new MapStorage(null));
        micdoodle8.mods.galacticraft.core.blocks.GCBlocks.basicBlock = Blocks.iron_block;
        NeptuneLandingBase.ensureGenerated(world, 0, 0);
        world.setBlock(2, 201, 2, Blocks.diamond_block, 0, 2);
        NeptuneLandingBase.ensureGenerated(world, 0, 0);
        require(world.getBlock(2,201,2) == Blocks.diamond_block, "preserve base edits");
        world.setBlock(0,200,0,Blocks.air,0,2);
        NeptuneLandingBase.ensureGenerated(world, 0, 0);
        require(world.getBlock(0,200,0) == Blocks.air && world.getBlock(2,201,2) == Blocks.diamond_block,
                "never rebuild destroyed landing site");
        Field uf = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); uf.setAccessible(true);
        ChunkProviderNeptune neptune = (ChunkProviderNeptune) ((sun.misc.Unsafe) uf.get(null)).allocateInstance(ChunkProviderNeptune.class);
        NeptuneContent.core=Blocks.bedrock; NeptuneContent.mantle=Blocks.stone;
        NeptuneContent.pressureIce=Blocks.packed_ice; NeptuneContent.liquefiedAtmosphericGasBlock=Blocks.water;
        Block[] blocks = new Block[65536]; Arrays.fill(blocks, Blocks.dirt);
        byte[] meta = new byte[65536]; Arrays.fill(meta,(byte)15);
        neptune.onChunkProvide(-1, 1, blocks, meta);
        for (int i=0;i<blocks.length;i++) {
            int y=i%256;
            Block expected=y<5?Blocks.bedrock:y<10?Blocks.stone:y<15?Blocks.packed_ice:y<160?Blocks.water:null;
            require(blocks[i]==expected && meta[i]==0,"Neptune pressure column " + i);
        }
        require(new WorldProviderNeptune().canRainOrSnow(),"weather strengths enabled");
        require(!new WorldProviderNeptune().canDoRainSnowIce(null),"no vanilla rain/ice");
        // Constructors must not allocate GL resources before a render context exists.
        new com.mod.htgt6.sulimited.client.SkyProviderCharon();
        new com.mod.htgt6.sulimited.client.SkyProviderProteus();
        new com.mod.htgt6.sulimited.client.SkyProviderMarsMoon(0,0,1);
        new com.mod.htgt6.sulimited.client.SkyProviderSaturnMoon(0,0,1,0);
        new com.mod.htgt6.sulimited.client.SkyProviderUranusMoon(0,0,1,0);
        new com.mod.htgt6.sulimited.client.SkyProviderOuterSol(1);
        new com.mod.htgt6.sulimited.client.SkyProviderNeptune();
        System.out.println("SolSys checks passed: 17 geological profiles, Neptune layers, landing persistence and preservation");
    }
    private static Block[] flat() {
        Block[] blocks = new Block[65536];
        for (int c=0;c<256;c++) {
            blocks[c*256]=Blocks.bedrock;
            Arrays.fill(blocks,c*256+1,c*256+72,Blocks.cobblestone);
            blocks[c*256+72]=Blocks.gravel;
        }
        return blocks;
    }
}
