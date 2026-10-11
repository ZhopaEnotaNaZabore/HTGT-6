package com.mod.htgt6.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;

/** Slices are loaded in memory and then stitched by Forge; no individual PNG files. */
@SideOnly(Side.CLIENT)
public final class ItemAtlasSprites {
    private static final ResourceLocation PNG = new ResourceLocation("htgt6", "textures/items/atlases/electronics.png");
    private static final ResourceLocation INDEX = new ResourceLocation("htgt6", "textures/items/atlases/electronics.properties");
    private static ItemAtlasData data;

    @SubscribeEvent
    public void beforeStitch(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() == 1) data = null;
    }

    private static ItemAtlasData data(IResourceManager manager) throws IOException {
        if (data == null) {
            try (InputStream png = manager.getResource(PNG).getInputStream();
                 InputStream index = manager.getResource(INDEX).getInputStream()) {
                data = new ItemAtlasData(png, index);
            }
        }
        return data;
    }

    public static IIcon register(IIconRegister register, String name) {
        TextureMap map = (TextureMap) register;
        TextureAtlasSprite sprite = map.getTextureExtry(name);
        if (sprite == null) {
            sprite = new Slice(name);
            map.setTextureEntry(name, sprite);
        }
        return sprite;
    }

    private static final class Slice extends TextureAtlasSprite {
        private Slice(String name) { super(name); }
        @Override public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) { return true; }

        @Override @SuppressWarnings("unchecked")
        public boolean load(IResourceManager manager, ResourceLocation location) {
            width = height = ItemAtlasData.SIZE;
            int[] pixels;
            try {
                String name = getIconName();
                String key = name.substring(name.indexOf(':') + 1);
                ItemAtlasData atlas = data(manager);
                width = height = atlas.iconSize(key);
                pixels = atlas.pixels(key);
            } catch (IOException error) {
                LogManager.getLogger("HTGT6-ItemAtlas").error("Cannot load atlas icon " + getIconName(), error);
                pixels = new int[width * height];
                for (int y = 0; y < height; y++) for (int x = 0; x < width; x++)
                    pixels[y * width + x] = ((x / 8 + y / 8) % 2 == 0) ? 0xFFFF00FF : 0xFF000000;
            }
            framesTextureData.clear();
            int[][] mipmaps = new int[Integer.numberOfTrailingZeros(width) + 1][];
            mipmaps[0] = pixels;
            framesTextureData.add(mipmaps);
            return false; // Let Forge stitch the loaded slice and generate mipmaps.
        }
    }
}
