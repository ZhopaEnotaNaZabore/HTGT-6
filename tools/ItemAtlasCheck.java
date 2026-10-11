import com.mod.htgt6.client.render.ItemAtlasData;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import javax.imageio.ImageIO;

/** Real image/index check, runnable without Minecraft or an OpenGL context. */
public final class ItemAtlasCheck {
    private static ItemAtlasData load(byte[] png, byte[] index) throws IOException {
        return new ItemAtlasData(new ByteArrayInputStream(png), new ByteArrayInputStream(index));
    }
    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args.length == 0 ? "." : args[0]);
        Path assets = root.resolve("src/main/resources/assets/htgt6/textures/items/atlases");
        byte[] png = Files.readAllBytes(assets.resolve("electronics.png"));
        byte[] index = Files.readAllBytes(assets.resolve("electronics.properties"));
        ItemAtlasData data = load(png, index);
        Pattern entry = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"row\"\\s*:\\s*(\\d+)\\s*,\\s*\"column\"\\s*:\\s*(\\d+)");
        int checked = 0;
        for (String group : new String[]{"semiconductors", "crystal-substrates", "coils", "smd", "coins", "armor-blanks", "tier-modules", "rocket-composites", "misc", "engraved-chips", "circuit-tiers"}) {
            Path dir = root.resolve("docs/assets/" + group);
            int size = group.equals("coins") || group.equals("tier-modules") || group.equals("rocket-composites") || group.equals("misc") ? 64 : 32;
            int margin = size / 16;
            BufferedImage reference = ImageIO.read(dir.resolve("atlas-" + size + ".png").toFile());
            Matcher matcher = entry.matcher(new String(Files.readAllBytes(dir.resolve("atlas.json")), StandardCharsets.UTF_8));
            while (matcher.find()) {
                String name = matcher.group(1);
                if (group.equals("misc") && (name.equals("ZPMsocket") || name.equals("ZPMCirc") || name.equals("UVCorc"))) continue;
                int row = Integer.parseInt(matcher.group(2)), column = Integer.parseInt(matcher.group(3));
                if (data.iconSize(name) != size) throw new AssertionError("Wrong slice size: " + name);
                int[] expected = reference.getRGB(column*size, row*size, size, size, null, 0, size);
                int[] actual = data.pixels(name);
                if (!Arrays.equals(expected, actual)) throw new AssertionError("Pixels differ: " + name);
                int visible = 0;
                for (int y=0; y<size; y++) for (int x=0; x<size; x++) {
                    int alpha = actual[y*size+x] >>> 24;
                    if (alpha != 0) visible++;
                    if ((x<margin || y<margin || x>=size-margin || y>=size-margin) && alpha != 0) throw new AssertionError("Border: " + name);
                }
                if (visible < 150) throw new AssertionError("Empty icon: " + name);
                checked++;
            }
        }
        if (checked != 167 || data.size() != checked) throw new AssertionError("Wrong count: " + checked);
        for (String name : new String[]{"ZPMsocket","ZPMCirc","UVCorc","PUV1Circ","UXCirc",
                "OLVCirc","OMVCirc","OHVCirc","OEVCirc","OIVCirc","MAXCirc","INFICirc"}) {
            if (data.iconSize(name) != 32) throw new AssertionError("Circuit exceeds 32px: " + name);
        }
        try { data.pixels("absent"); throw new AssertionError("Missing item accepted"); } catch (IOException expected) { }
        String text = new String(index, StandardCharsets.ISO_8859_1);
        try { load(png, text.replace("item.itemWafer2=1", "item.itemWafer2=0").getBytes(StandardCharsets.ISO_8859_1));
            throw new AssertionError("Duplicate cell accepted"); } catch (IOException expected) { }
        try { load(png, text.replace("columns=8", "columns=9").getBytes(StandardCharsets.ISO_8859_1));
            throw new AssertionError("Wrong dimensions accepted"); } catch (IOException expected) { }
        try { load(png, text.replace("size.BeeCoin=64", "size.BeeCoin=48").getBytes(StandardCharsets.ISO_8859_1));
            throw new AssertionError("Invalid size accepted"); } catch (IOException expected) { }
        try { load(png, (text + "\nsize.itemWafer1=64\n").getBytes(StandardCharsets.ISO_8859_1));
            throw new AssertionError("Partial overlap accepted"); } catch (IOException expected) { }
        try { load(png, text.replaceAll("(?m)^item.BeeCoin=\\d+", "item.BeeCoin=7").getBytes(StandardCharsets.ISO_8859_1));
            throw new AssertionError("64px slice crossing row accepted"); } catch (IOException expected) { }
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(index));
        int lastRowCell = (Integer.parseInt(properties.getProperty("rows")) - 1) * 8;
        try { load(png, text.replaceAll("(?m)^item.BeeCoin=\\d+", "item.BeeCoin=" + lastRowCell).getBytes(StandardCharsets.ISO_8859_1));
            throw new AssertionError("64px slice crossing bottom accepted"); } catch (IOException expected) { }
        // A new resource load must actually decode new image bytes, not retain stale colours.
        BufferedImage changed = ImageIO.read(new ByteArrayInputStream(png));
        changed.setRGB(16,16,0xFF123456);
        ByteArrayOutputStream changedPng = new ByteArrayOutputStream();
        ImageIO.write(changed,"png",changedPng);
        if (load(changedPng.toByteArray(),index).pixels("itemWafer1")[16*32+16] != 0xFF123456)
            throw new AssertionError("Reload retained old pixels");
        System.out.println("PASS: 167 exact slices, all updated circuits at 32px, alpha margins, invalid sizes/overlaps/bounds, and fresh reload data.");
    }
}
