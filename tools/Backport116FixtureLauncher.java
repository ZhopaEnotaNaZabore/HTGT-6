import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

/** Loads real Forge event classes with their listener/cancellation instrumentation. */
public final class Backport116FixtureLauncher {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Expected fixture main class");
        String[] paths = System.getProperty("java.class.path").split(File.pathSeparator);
        URL[] urls = new URL[paths.length];
        for (int i = 0; i < paths.length; i++) urls[i] = new File(paths[i]).toURI().toURL();
        Launch.classLoader = new LaunchClassLoader(urls);
        Launch.blackboard = new HashMap<String, Object>();
        Launch.blackboard.put("fml.deobfuscatedEnvironment", true);
        Launch.classLoader.registerTransformer("cpw.mods.fml.common.asm.transformers.EventSubscriptionTransformer");
        Launch.classLoader.registerTransformer("Backport116FixtureLauncher$EnchantmentTransformer");
        Thread.currentThread().setContextClassLoader(Launch.classLoader);
        Launch.classLoader.loadClass(args[0]).getMethod("main", String[].class)
                .invoke(null, (Object) new String[] {"loaded"});
    }

    public static final class EnchantmentTransformer implements IClassTransformer {
        @Override public byte[] transform(String name, String transformedName, byte[] bytes) {
            if(bytes==null)return null;
            if("net.minecraft.block.BlockRedstoneWire".equals(transformedName))return com.mod.htgt6.asm.RedstoneWireASM116.transform(bytes);
            if("net.minecraft.block.BlockWall".equals(transformedName))return com.mod.htgt6.asm.NativeWallASM116.transform(bytes);
            if("net.minecraft.entity.passive.EntityHorse".equals(transformedName))return com.mod.htgt6.asm.LeatherHorseArmorASM151.transform(bytes);
            if("net.minecraft.block.BlockLiquid".equals(transformedName))return com.mod.htgt6.asm.LavaMixingASM116.transform(bytes);
            if("net.minecraft.entity.projectile.EntityFishHook".equals(transformedName))return com.mod.htgt6.asm.OpenWaterFishingASM116.transform(bytes);
            if("net.minecraft.item.ItemHoe".equals(transformedName))return com.mod.htgt6.asm.HoeMiningASM116.transform(bytes);
            if("net.minecraft.tileentity.TileEntityFurnace".equals(transformedName))return com.mod.htgt6.asm.NetherWoodFuelASM116.transform(bytes);
            if("net.minecraft.block.BlockStairs".equals(transformedName))return com.mod.htgt6.asm.BlackstoneStairsASM116.transform(bytes);
            if("net.minecraft.world.Explosion".equals(transformedName))return com.mod.htgt6.asm.AnchorExplosionASM116.transform(bytes);
            if("net.minecraft.entity.player.EntityPlayer".equals(transformedName))return com.mod.htgt6.asm.RespawnAnchorASM116.transform(bytes,true);
            if("net.minecraft.server.management.ServerConfigurationManager".equals(transformedName))return com.mod.htgt6.asm.RespawnAnchorASM116.transform(bytes,false);
            if("net.minecraft.block.BlockSoulSand".equals(transformedName))return com.mod.htgt6.asm.SoulSpeedASM116.transform(bytes);
            if (!"net.minecraft.enchantment.Enchantment".equals(transformedName)) return bytes;
            byte[] once = com.mod.htgt6.asm.NetheriteHoeEnchantmentASM116.transform(bytes);
            byte[] twice = com.mod.htgt6.asm.NetheriteHoeEnchantmentASM116.transform(once);
            if (!Arrays.equals(once, twice)) throw new AssertionError("MCP hoe adapter idempotence");
            return twice;
        }
    }
}
