package com.mod.htgt6.client.vanillatweaks.CHAT;

import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.widgets.textfield.TextFieldHandler;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;

/** Tests draw ordering and caret coordinates without an OpenGL context. */
public final class ChatCursorFixture {
    private static final class Renderer extends ChatInputRenderer {
        final StringBuilder order = new StringBuilder();
        int caretX, caretHeight;
        float extraDrawWidth;
        Renderer(TextFieldHandler handler) { super(handler); }
        @Override public TextRenderer.Line line(String text) { return new TextRenderer.Line(text, width(text) * scale); }
        @Override protected float drawText(String text, float x, float y) {
            order.append('T'); return x + width(text) * scale + extraDrawWidth;
        }
        @Override protected int getStartX(float width) { return 4; }
        @Override public float getFontHeight() { return 9 * scale; }
        @Override public Point2D.Float getPosOf(List<TextRenderer.Line> lines, Point point) {
            return new Point2D.Float(4 + width(lines.get(0).getText().substring(0, point.x)) * scale, 2);
        }
        @Override public void drawMarked(float y, float x0, float x1) { order.append('S'); }
        @Override protected void drawCaret(int x, int y, int height) {
            order.append('C'); caretX = x; caretHeight = height;
        }
        void paint(String text) { drawMeasuredLines(Collections.singletonList(line(text))); }
    }
    private static int width(String text) {
        int width = 0;
        for (char c : text.toCharArray()) width += c == ' ' ? 4 : c == 'i' ? 2 : c == 'W' ? 8 : 6;
        return width;
    }
    private static void require(boolean value, String label) { if (!value) throw new AssertionError(label); }
    public static void main(String[] args) {
        for (String text : new String[]{"", "iW", "Привет", "Привет мир "}) {
            for (float scale : new float[]{1, 1.5f, 2}) {
                for (int cursor = 0; cursor <= text.length(); cursor++) {
                    TextFieldHandler handler = new TextFieldHandler(null);
                    handler.getText().add(text); handler.setCursor(0, cursor, false);
                    Renderer renderer = new Renderer(handler); renderer.setScale(scale); renderer.setCursor(true);
                    renderer.paint(text);
                    require(renderer.order.toString().equals("TC"), "caret after text");
                    require(renderer.caretX == (int) Math.ceil(4 + width(text.substring(0, cursor)) * scale
                            + (cursor == text.length() ? Math.max(1, scale) : 0)), "insertion boundary");
                    require(renderer.caretHeight == Math.round(9 * scale), "scaled height");
                    require(renderer.getLastWidth() >= width(text) * scale + 1, "scroll space for caret");
                    require(handler.getMainCursor().x == cursor && !handler.hasTextMarked(), "cursor unchanged");
                    renderer.order.setLength(0); renderer.setCursor(false); renderer.paint(text);
                    require(renderer.order.toString().equals("T"), "blink off");
                    renderer.order.setLength(0); renderer.setCursor(true); renderer.setSimulate(true); renderer.paint(text);
                    require(renderer.order.toString().equals("T"), "measurement has no caret");
                    renderer.setSimulate(false);
                    if (cursor > 0) {
                        handler.setOffsetCursor(0, 0); renderer.order.setLength(0); renderer.paint(text);
                        require(renderer.order.toString().equals("STC"), "selection/text/caret order");
                        require(handler.hasTextMarked(), "selection preserved");
                    }
                }
            }
        }
        TextFieldHandler handler = new TextFieldHandler(null);
        String text = "ыыыыыыыы";
        handler.getText().add(text); handler.setCursor(0, text.length(), false);
        Renderer renderer = new Renderer(handler);
        renderer.extraDrawWidth = 19.25f; // Reproduce a font whose drawn width differs from measurement.
        renderer.setCursor(true); renderer.paint(text);
        require(renderer.caretX == 73, "caret follows actual rendered endpoint");
        require(renderer.getLastWidth() >= 74, "scroll range includes actual endpoint and caret");
        renderer.extraDrawWidth = 0; renderer.paint(text);
        require(renderer.caretX == 53, "font change must not reuse last frame's endpoint");
        System.out.println("ChatCursorFixture OK: boundaries, Cyrillic/Latin, spaces, middle/end, scale, blink, selection, simulation");
    }
}
