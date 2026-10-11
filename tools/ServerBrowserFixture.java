import com.mod.htgt6.client.vanillatweaks.SSUI.*;
import net.minecraft.client.multiplayer.ServerData;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class ServerBrowserFixture {
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("htgt6-browser-");
        File file = directory.resolve("browser.properties").toFile();
        try {
            ServerBrowserPreferences prefs = new ServerBrowserPreferences(file);
            ServerData alpha = new ServerData("Alpha", "example.invalid:25565", false);
            ServerData beta = new ServerData("Beta", "other.invalid:25570", false);
            List<ServerData> all = Arrays.asList(alpha, beta);
            prefs.toggleFavorite(" EXAMPLE.INVALID:25565 ");
            prefs.recordConnection(beta.serverIP);
            prefs = new ServerBrowserPreferences(file);
            check(prefs.favorite(alpha.serverIP), "favorite persisted, normalized address");
            check(prefs.recent(beta.serverIP) > 0, "recent persisted");
            check(ServerBrowserModel.filter(all, prefs, 1, "").equals(Arrays.asList(alpha)), "favorites projection");
            check(ServerBrowserModel.filter(all, prefs, 2, "").equals(Arrays.asList(beta)), "recent projection");
            check(ServerBrowserModel.filter(all, prefs, 0, "  ALPHA ").equals(Arrays.asList(alpha)), "name search");
            check(ServerBrowserModel.filter(all, prefs, 0, "25570").equals(Arrays.asList(beta)), "address search");
            check(ServerBrowserModel.filter(all, prefs, 1, "Beta").isEmpty(), "tab+query intersection");
            check(all.get(0) == alpha && all.get(1) == beta, "saved list unchanged");
            prefs.move(alpha.serverIP, "new.invalid");
            check(!prefs.favorite(alpha.serverIP) && prefs.favorite("new.invalid"), "edit migrates metadata");
            prefs.remove("new.invalid");
            check(!new ServerBrowserPreferences(file).favorite("new.invalid"), "delete cleans metadata");
            prefs.toggleFavorite(beta.serverIP); prefs.toggleFavorite(beta.serverIP);
            check(!prefs.favorite(beta.serverIP), "toggle removal");
            for (int[] size : new int[][] {{320,240},{427,240},{480,270},{640,360},{960,540},{1920,1080}}) {
                ServerBrowserLayout l = new ServerBrowserLayout(size[0], size[1]);
                check(l.listWidth > 100 && l.top + l.visibleRows * l.rowHeight <= l.listBottom, "rows fit " + Arrays.toString(size));
                check(!l.wide || l.detailX + l.detailWidth <= size[0] - 12, "detail fits");
                check(l.rowAt(13, l.top + 1, 2, 8) == 2, "scrolled hit test");
                check(l.rowAt(13, l.top + l.rowHeight - 2, 2, 8) == -1, "gap not clickable");
                check(l.rowAt(l.left + l.listWidth + 1, l.top + 1, 2, 8) == -1, "details not list");
                check(l.rowAt(13, l.listBottom, 0, 8) == -1, "footer not list");
                check(l.rowAt(13, l.top, 0, 0) == -1, "empty list");
            }
            ClassNode browser = read("com/mod/htgt6/client/vanillatweaks/SSUI/GuiModernServerSelect");
            check(com.mod.htgt6.client.menu.ConnectionScreenThemeHandler.supports(net.minecraft.client.multiplayer.GuiConnecting.class), "connecting screen themed");
            check(com.mod.htgt6.client.menu.ConnectionScreenThemeHandler.supports(net.minecraft.client.gui.GuiDisconnected.class), "disconnect screen themed");
            check(com.mod.htgt6.client.menu.ConnectionScreenThemeHandler.supports(net.minecraft.client.gui.GuiDownloadTerrain.class), "terrain download themed");
            check(!com.mod.htgt6.client.menu.ConnectionScreenThemeHandler.supports(net.minecraft.client.gui.GuiMainMenu.class), "main menu excluded");
            check(!com.mod.htgt6.client.menu.ConnectionScreenThemeHandler.supports(net.minecraft.client.gui.GuiSelectWorld.class), "world selection excluded");
            for (String field : new String[] {"field_146304_f", "field_146306_a"})
                check(net.minecraft.client.gui.GuiDisconnected.class.getDeclaredField(field) != null, "disconnect field mapping");
            check(net.minecraft.client.multiplayer.GuiConnecting.class.getDeclaredField("field_146371_g") != null, "login phase field mapping");
            int forgeConnects = 0, setup = 0;
            Set<String> routes = new HashSet<String>();
            for (Object entry : browser.methods) {
                MethodNode method = (MethodNode) entry;
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction instanceof TypeInsnNode)
                        check(!((TypeInsnNode) instruction).desc.equals("net/minecraft/client/multiplayer/GuiConnecting"), "direct GuiConnecting forbidden");
                    if (!(instruction instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (call.owner.equals("cpw/mods/fml/client/FMLClientHandler") && call.name.equals("connectToServer")) forgeConnects++;
                    if (call.owner.equals("cpw/mods/fml/client/FMLClientHandler") && call.name.equals("setupServerList")) setup++;
                    if (call.owner.equals(browser.name) && call.name.equals("connect")) routes.add(method.name);
                }
            }
            check(forgeConnects == 1 && setup == 1, "single initialized Forge connection path");
            check(routes.containsAll(Arrays.asList("actionPerformed", "confirmClicked", "mouseClicked", "keyTyped")),
                    "button, direct dialog, double click, enter route through Forge");
            ClassNode pinger = read("com/mod/htgt6/client/vanillatweaks/SSUI/ServerPingSession");
            check(calls(pinger, "tick", "func_147223_a"), "status packets processed on tick");
            check(calls(read(pinger.name + "$Ping"), "close", "func_147226_b"), "real pinger cancellation method");
            System.out.println("PASS: favorites/history persistence; name/address/tab filters; 6 GUI scales; every login route uses Forge; ping tick/cancel lifecycle");
        } finally {
            Files.deleteIfExists(file.toPath());
            Files.deleteIfExists(directory.resolve("browser.properties.tmp"));
            Files.deleteIfExists(directory);
        }
    }
    private static ClassNode read(String name) throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream in = ServerBrowserFixture.class.getResourceAsStream("/" + name + ".class")) {
            check(in != null, "compiled class " + name); new ClassReader(in).accept(node, 0);
        }
        return node;
    }
    private static boolean calls(ClassNode node, String methodName, String callName) {
        for (Object entry : node.methods) {
            MethodNode method = (MethodNode) entry;
            if (!method.name.equals(methodName)) continue;
            for (AbstractInsnNode insn : method.instructions.toArray())
                if (insn instanceof MethodInsnNode && ((MethodInsnNode) insn).name.equals(callName)) return true;
        }
        return false;
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
