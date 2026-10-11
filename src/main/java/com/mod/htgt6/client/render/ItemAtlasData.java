package com.mod.htgt6.client.render;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.imageio.ImageIO;

/** GL-independent atlas decoder. Streams are owned and closed by its caller. */
public final class ItemAtlasData {
    public static final int SIZE = 32;
    private final BufferedImage image;
    private final int columns;
    private final Map<String, Integer> cells = new HashMap<String, Integer>();
    private final Map<String, Integer> sizes = new HashMap<String, Integer>();

    public ItemAtlasData(InputStream png, InputStream index) throws IOException {
        Properties properties = new Properties();
        properties.load(index);
        image = ImageIO.read(png);
        try {
            columns = Integer.parseInt(properties.getProperty("columns"));
            int rows = Integer.parseInt(properties.getProperty("rows"));
            int tileSize = Integer.parseInt(properties.getProperty("tileSize"));
            if (columns < 1 || columns > 64 || rows < 1 || rows > 64 || tileSize != SIZE
                    || image == null || image.getWidth() != columns * SIZE || image.getHeight() != rows * SIZE) {
                throw new IOException("Invalid electronics atlas dimensions");
            }
            Set<Integer> occupied = new HashSet<Integer>();
            for (String key : properties.stringPropertyNames()) {
                if (!key.startsWith("item.")) continue;
                int cell = Integer.parseInt(properties.getProperty(key));
                String name = key.substring(5);
                int size = Integer.parseInt(properties.getProperty("size." + name, "32"));
                if (size != 32 && size != 64) throw new IOException("Unsupported icon size: " + key);
                int span = size / SIZE;
                if (cell < 0 || cell >= columns * rows || cell % columns + span > columns
                        || cell / columns + span > rows) {
                    throw new IOException("Atlas icon outside image: " + key);
                }
                for (int y = 0; y < span; y++) for (int x = 0; x < span; x++) {
                    if (!occupied.add(cell + y * columns + x))
                        throw new IOException("Overlapping atlas icon: " + key);
                }
                cells.put(name, cell);
                sizes.put(name, size);
            }
            if (cells.isEmpty()) throw new IOException("Empty electronics atlas index");
        } catch (NumberFormatException error) {
            throw new IOException("Invalid electronics atlas index", error);
        }
    }

    public int[] pixels(String name) throws IOException {
        Integer cell = cells.get(name);
        if (cell == null) throw new IOException("No electronics atlas cell for " + name);
        int size = iconSize(name);
        return image.getRGB((cell % columns) * SIZE, (cell / columns) * SIZE,
                size, size, null, 0, size);
    }

    public int iconSize(String name) throws IOException {
        Integer size = sizes.get(name);
        if (size == null) throw new IOException("No electronics atlas cell for " + name);
        return size;
    }

    public int size() { return cells.size(); }
}
