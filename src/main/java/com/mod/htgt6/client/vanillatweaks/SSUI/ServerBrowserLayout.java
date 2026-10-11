package com.mod.htgt6.client.vanillatweaks.SSUI;

/** GUI-scaled coordinates shared by rendering, hit testing and layout checks. */
public final class ServerBrowserLayout {
    public final int left = 12, top = 84, rowHeight = 48;
    public final int listWidth, listBottom, detailX, detailWidth, detailBottom, visibleRows;
    public final boolean wide;
    public ServerBrowserLayout(int width, int height) {
        wide = width >= 420;
        detailWidth = wide ? Math.max(140, Math.min(260, width / 3)) : 0;
        listWidth = width - 24 - (wide ? detailWidth + 10 : 0);
        detailX = left + listWidth + 10;
        detailBottom = height - 58;
        listBottom = wide ? detailBottom : height - 92;
        visibleRows = Math.max(1, (listBottom - top) / rowHeight);
    }
    public int rowAt(int x, int y, int offset, int count) {
        if (x < left || x >= left + listWidth || y < top || y >= listBottom) return -1;
        int row = (y - top) / rowHeight;
        if (row >= visibleRows || (y - top) % rowHeight >= rowHeight - 4) return -1;
        int index = offset + row;
        return index < count ? index : -1;
    }
}
