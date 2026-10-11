import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.fluids.*;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.blocks.BlockGlieseCPlant;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysGeology;
import com.mod.htgt6.sulimited.world.GlieseWaterloggedAccess;

/** Fluid displacement and support regressions, no client/OpenGL context required. */
public final class GlieseAquaticFixture {
    static final class Reef extends BlockGlieseCPlant {
        int drops;
        Reef() { super(true); }
        @Override public void dropBlockAsItemWithChance(World w, int x, int y, int z, int meta, float chance, int fortune) { drops++; }
    }
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        net.minecraft.launchwrapper.Launch.classLoader = new net.minecraft.launchwrapper.LaunchClassLoader(
                ((java.net.URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs());
        BountifulMechanicsFixture120.Grid world = BountifulMechanicsFixture120.initialize();
        Fluid fluid = new Fluid("fixture_reef_water").setDensity(1030);
        FluidRegistry.registerFluid(fluid);
        java.lang.reflect.Constructor<?> ctor = Class.forName(SolSysGeology.class.getName() + "$SaltWaterBlock").getDeclaredConstructor(Fluid.class);
        ctor.setAccessible(true);
        BlockFluidClassic water = (BlockFluidClassic) ctor.newInstance(fluid);
        SolSysGeology.saltWater = water;
        Reef reef = new Reef();
        check(reef.canRenderInPass(0) && reef.getActiveRenderPass() == 0,
                "coral geometry must use the opaque terrain pass");
        check(reef.canRenderInPass(1) && reef.getActiveRenderPass() == 1,
                "contained water must use the translucent terrain pass");
        check(!reef.canRenderInPass(2), "unexpected third terrain pass");
        Block.blockRegistry.addObject(3500, "fixture:water", water);
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(reef, "fixture_reef");
        Random random = new Random(13);
        for (int x=0; x<16; x++) for (int z=0; z<16; z++) {
            world.setBlock(x, 39, z, Blocks.stone, 0, 2);
            for (int y=40; y<44; y++) world.setBlock(x, y, z, reef, y%3, 2);
            world.setBlock(x, 44, z, water, 0, 2);
        }
        for (int tick=0; tick<40; tick++) for (int x=0; x<16; x++) for (int z=0; z<16; z++) {
            // Exercise the actual downward fluid tick, as well as both Forge entry points.
            water.updateTick(world, x, 44, z, random);
            for (int y=40; y<44; y++) {
                check(!water.canDisplace(world,x,y,z), "water considers reef displaceable");
                check(!water.displaceIfPossible(world,x,y,z), "water displaced reef");
                reef.onNeighborBlockChange(world,x,y,z,water);
                reef.updateTick(world,x,y,z,random);
                check(world.getBlock(x,y,z)==reef, "pressure destroyed supported reef");
            }
        }
        check(reef.drops==0 && world.lastSpawn==null, "flow produced coral drops");
        check(world.scheduled==0, "stable reef schedules support ticks");
        world.setBlock(40,39,0,Blocks.stone,0,2);
        world.setBlock(40,40,0,reef,2,2);
        check(reef.canBlockStay(world,40,40,0), "waterlogged reef depends on neighbouring fluid level");
        GlieseWaterloggedAccess view = new GlieseWaterloggedAccess(world,water);
        check(view.getBlock(0,40,0)==water && view.getBlockMetadata(0,40,0)==0, "coral colour interpreted as fluid level");
        check(view.getBlock(0,41,0)==water, "internal top surface remains");
        check(RenderBlockFluid.instance.getFluidHeightForRender(view,0,40,0,water)==1F, "water column is not full");
        check(water.getQuantaValue(world,0,40,0)==water.getQuantaValue(view,0,40,0), "simulation does not see contained water");
        world.setBlock(40,39,0,Blocks.air,0,2);
        reef.onNeighborBlockChange(world,40,40,0,Blocks.air);
        check(world.getBlock(40,40,0)==reef, "synchronous support cascade");
        reef.updateTick(world,40,40,0,random);
        check(world.getBlock(40,40,0)==water && reef.drops==0, "lost support creates drops or air");
        reef.updateTick(world,40,40,0,random);
        check(world.getBlock(40,40,0)==water && reef.drops==0, "stale tick generates drops");
        world.setBlock(40,40,0,Blocks.stone,0,2);
        reef.breakBlock(world,40,40,0,Blocks.stone,0);
        check(world.getBlock(40,40,0)==Blocks.stone, "water overwrote replacement block");
        world.setBlock(40,40,0,Blocks.air,0,2);
        reef.breakBlock(world,40,40,0,Blocks.air,0);
        check(world.getBlock(40,40,0)==water, "harvest left an air pocket");
        check(reef.getItemDropped(0,random,0)!=null, "manual harvest loses coral item");
        System.out.println("Aquatic checks passed: 40 pressure updates over 1024 reef blocks, zero drops, zero support ticks for stable reef; waterlogged neighbours, support loss and harvest restoration.");
    }
}
