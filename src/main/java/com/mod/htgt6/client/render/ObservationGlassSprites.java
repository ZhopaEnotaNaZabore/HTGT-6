package com.mod.htgt6.client.render;

import com.mod.htgt6.common.multiblock.glass.ObservationGlassPattern;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

/** Shared atlas sprites, rebuilt on resource reload. No EnderIO/OptiFine dependency or per-tick rendering. */
@SideOnly(Side.CLIENT)
public final class ObservationGlassSprites extends TextureAtlasSprite {
    private final int mask;
    private final boolean clear;

    private ObservationGlassSprites(String name, int mask, boolean clear) {
        super(name);
        this.mask = mask;
        this.clear = clear;
    }

    public static IIcon[] register(IIconRegister register, boolean clear) {
        TextureMap atlas = (TextureMap) register;
        IIcon[] icons = new IIcon[256];
        for (int mask = 0; mask < icons.length; mask++) {
            int pattern = ObservationGlassPattern.canonical(mask);
            String name = "htgt6:observation_glass/" + (clear ? "clear_" : "reinforced_") + pattern;
            TextureAtlasSprite sprite = atlas.getTextureExtry(name);
            if (sprite == null) {
                sprite = new ObservationGlassSprites(name, pattern, clear);
                atlas.setTextureEntry(name, sprite);
            }
            icons[mask] = sprite;
        }
        return icons;
    }

    @Override public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) { return true; }

    @Override @SuppressWarnings("unchecked")
    public boolean load(IResourceManager manager, ResourceLocation location) {
        width = height = ObservationGlassPattern.SIZE;
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++)
            pixels[y * width + x] = ObservationGlassPattern.pixel(mask, clear, x, y);
        framesTextureData.clear();
        // 1.7.10 TextureUtil indexes every requested mip level directly.
        int[][] mipmaps = new int[7][]; // 64,32,16,8,4,2,1
        mipmaps[0] = pixels;
        framesTextureData.add(mipmaps);
        return false; // Forge must stitch this custom-loaded sprite into the block atlas.
    }
}
