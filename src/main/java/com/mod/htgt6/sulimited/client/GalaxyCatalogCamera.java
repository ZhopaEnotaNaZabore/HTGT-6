package com.mod.htgt6.sulimited.client;

/** Frame-rate independent camera on a fixed map plane, independent of GUI resolution. */
public final class GalaxyCatalogCamera {
    public static final float MAP_WIDTH = 2400, MAP_HEIGHT = 1600;
    public float x, y, zoom;
    private float targetX, targetY, targetZoom;
    private float fit;
    public void resize(int width, int height) {
        float next = Math.max(.03F, Math.min(width / MAP_WIDTH, height / MAP_HEIGHT) * 1.12F);
        if (fit == 0) { fit = next; overview(); zoom = targetZoom; }
        else { zoom *= next / fit; targetZoom *= next / fit; fit = next; }
    }
    public void overview() { targetX = targetY = 0; targetZoom = fit; }
    public void focus(float mapX, float mapY) {
        targetX = (mapX - .5F) * MAP_WIDTH;
        targetY = (mapY - .5F) * MAP_HEIGHT;
        targetZoom = Math.max(fit * 3, targetZoom);
    }
    public void wheel(int delta) { targetZoom = Math.max(fit * .4F, Math.min(fit * 8, targetZoom * (float)Math.pow(1.2, delta / 120.0))); }
    public void drag(float dx, float dy) { x -= dx / zoom; y -= dy / zoom; targetX = x; targetY = y; }
    public void step(float seconds) {
        float blend = 1 - (float)Math.exp(-8 * Math.max(0, Math.min(.1F, seconds)));
        x += (targetX - x) * blend; y += (targetY - y) * blend; zoom += (targetZoom - zoom) * blend;
    }
    public int screenX(float mapX, int width) { return Math.round(width * .5F + ((mapX - .5F) * MAP_WIDTH - x) * zoom); }
    public int screenY(float mapY, int height) { return Math.round(height * .5F + ((mapY - .5F) * MAP_HEIGHT - y) * zoom); }
    public float relativeZoom() { return fit == 0 ? 1 : zoom / fit; }
}
