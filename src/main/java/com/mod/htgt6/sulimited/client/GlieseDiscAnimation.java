package com.mod.htgt6.sulimited.client;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Complete billboard discs, with no longitude wrapping or artificial alpha. */
public final class GlieseDiscAnimation {
    private GlieseDiscAnimation() { }
    public static BufferedImage scaleDisc(BufferedImage source, int size) {
        BufferedImage result = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(source, 0, 0, size, size, null);
        } finally { g.dispose(); }
        return result;
    }
    public static float angle(long ticks, float partialTicks, int periodTicks) {
        return (float)((Math.floorMod(ticks, (long)periodTicks) + partialTicks) * 360.0 / periodTicks);
    }
}
