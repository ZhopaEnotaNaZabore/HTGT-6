import com.mod.htgt6.asm.HTGT6Config;
import java.io.File;
import net.minecraftforge.common.config.Configuration;
public final class NIODebugConfigFixture {
    public static void main(String[] args) throws Exception {
        File root = new File(args[0]);
        java.lang.reflect.Field home = cpw.mods.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome");
        home.setAccessible(true);
        home.set(null, root.getAbsoluteFile());
        HTGT6Config.load(root);
        if (HTGT6Config.NIO_DEBUG) throw new AssertionError("default must be false");
        File file = new File(root, "config/HTGT6/HiTech6.cfg");
        Configuration cfg = new Configuration(file, null, true);
        cfg.load();
        cfg.get("==DEV DEBUG==", "NIODebug", false).set(true);
        cfg.save();
        HTGT6Config.load(root);
        if (!HTGT6Config.NIO_DEBUG) throw new AssertionError("true not read");
        cfg.get("==DEV DEBUG==", "NIODebug", false).set(false);
        cfg.save();
        HTGT6Config.load(root);
        if (HTGT6Config.NIO_DEBUG) throw new AssertionError("false not read");
        System.out.println("PASS: exact DEV DEBUG category, default false, true and false reload");
    }
}
