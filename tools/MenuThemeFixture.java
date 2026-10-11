import com.mod.htgt6.common.ui.MenuTheme;
import java.io.File;
import java.nio.file.Files;
import net.minecraftforge.common.config.Configuration;

/** Real Forge configuration round-trip: defaults, recovery, persistence and independent palettes. */
public final class MenuThemeFixture {
    public static void main(String[] args) throws Exception {
        File directory = Files.createTempDirectory("htgt6-menu-theme-").toFile();
        File file = new File(directory, "client.cfg");
        java.lang.reflect.Field home = cpw.mods.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome");
        home.setAccessible(true);
        home.set(null, directory);
        try {
            Configuration config = new Configuration(file);
            config.load();
            config.get("machine_ui", "theme", "RED").set("RED");
            MenuTheme.load(config);
            require(MenuTheme.getActive() == MenuTheme.MOONLIGHT, "default theme");
            require(MenuTheme.color(0x12345678, 0x87654321) == 0x12345678, "moonlight alpha/colour");
            MenuTheme.setActive(MenuTheme.INDUSTRIAL);
            Configuration reopened = new Configuration(file);
            reopened.load();
            MenuTheme.load(reopened);
            require(MenuTheme.getActive() == MenuTheme.INDUSTRIAL, "persisted theme");
            require(MenuTheme.color(0x12345678, 0x87654321) == 0x87654321, "industrial alpha/colour");
            require("RED".equals(reopened.get("machine_ui", "theme", "").getString()), "machine palette preserved");
            MenuTheme.setActive(null);
            require(MenuTheme.getActive() == MenuTheme.INDUSTRIAL, "null must not reset");
            reopened.get("menu_ui", "theme", "").set("invalid");
            MenuTheme.load(reopened);
            require(MenuTheme.getActive() == MenuTheme.MOONLIGHT, "invalid value fallback");
            Configuration recovered = new Configuration(file);
            recovered.load();
            require("MOONLIGHT".equals(recovered.get("menu_ui", "theme", "").getString()), "fallback persisted");
            verifyRenderer("com/mod/htgt6/client/menu/GuiHTGT6Wiki", "com/mod/htgt6/common/ui/MenuTheme", true);
            verifyRenderer("com/mod/htgt6/client/vanillatweaks/SSUI/GuiModernServerSelect", "com/mod/htgt6/client/menu/MenuAppearance", true);
            verifyRenderer("com/mod/htgt6/client/menu/MenuAppearance", "com/mod/htgt6/common/ui/MenuTheme", true);
            for (String screen : new String[] {"GuiModernWorldSelect", "GuiCreateWorld112", "GuiWorldEdit112", "GuiWorldList"}) {
                verifyRenderer("com/mod/htgt6/client/vanillatweaks/WSUI/" + screen,
                        "com/mod/htgt6/client/menu/MenuAppearance", false);
            }
            try (java.io.InputStream stream = MenuThemeFixture.class.getResourceAsStream(
                    "/assets/htgt6/textures/gui/mainmenu/background_industrial.jpg")) {
                require(stream != null, "packaged JPEG missing");
                java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(stream);
                require(image != null && image.getWidth() == 1536 && image.getHeight() == 1024,
                        "ImageIO must decode the background at original resolution");
            }
            System.out.println("PASS: server and Wiki renderers use active palette; world screens excluded; JPEG decodes with Java ImageIO");
            System.out.println("PASS: menu theme defaults, invalid recovery, config round-trip, independent machine palette, exact colours");
        } finally {
            MenuTheme.setActive(MenuTheme.MOONLIGHT);
            Files.deleteIfExists(file.toPath());
            Files.deleteIfExists(directory.toPath());
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void verifyRenderer(String name, String owner, boolean expected) throws Exception {
        org.objectweb.asm.tree.ClassNode node = new org.objectweb.asm.tree.ClassNode();
        try (java.io.InputStream stream = MenuThemeFixture.class.getResourceAsStream("/" + name + ".class")) {
            require(stream != null, "missing compiled screen: " + name);
            new org.objectweb.asm.ClassReader(stream).accept(node, 0);
        }
        boolean found = false;
        for (Object entry : node.methods) {
            org.objectweb.asm.tree.MethodNode method = (org.objectweb.asm.tree.MethodNode) entry;
            for (org.objectweb.asm.tree.AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof org.objectweb.asm.tree.MethodInsnNode) {
                    found |= ((org.objectweb.asm.tree.MethodInsnNode) instruction).owner.equals(owner);
                }
            }
        }
        require(found == expected, "theme integration: " + name);
    }
}
