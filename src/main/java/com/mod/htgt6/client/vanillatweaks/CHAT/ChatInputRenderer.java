package com.mod.htgt6.client.vanillatweaks.CHAT;

import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.widgets.textfield.TextFieldHandler;
import com.cleanroommc.modularui.widgets.textfield.TextFieldRenderer;
import java.awt.geom.Point2D;
import java.util.List;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.Gui;

/** Keep ModularUI selection/scrolling, but paint the caret at the insertion boundary, after text. */
@SideOnly(Side.CLIENT)
class ChatInputRenderer extends TextFieldRenderer {
    private float drawnLineEnd = Float.NaN;
    ChatInputRenderer(TextFieldHandler handler) { super(handler); }

    @Override protected void drawMeasuredLines(List<TextRenderer.Line> lines) {
        this.drawnLineEnd = Float.NaN;
        boolean visible = this.renderCursor;
        this.renderCursor = false;
        try {
            super.drawMeasuredLines(lines);
        } finally {
            this.renderCursor = visible;
        }
        // Reserve the caret pixel when a long line scrolls to its right edge.
        for (TextRenderer.Line line : lines) {
            this.lastWidth = Math.max(this.lastWidth, getStartX(line.getWidth()) + line.getWidth() + 1);
        }
        if (!Float.isNaN(this.drawnLineEnd)) {
            this.lastWidth = Math.max(this.lastWidth, (float) Math.ceil(this.drawnLineEnd) + 1);
        }
        if (visible && !this.simulate) {
            Point2D.Float position = getPosOf(lines, this.handler.getMainCursor());
            // Chat is single-line. At its end use the actual draw result, not a
            // second width calculation: custom font renderers can disagree with it.
            if (lines.size() == 1 && this.handler.getMainCursor().x == lines.get(0).getText().length()
                    && !Float.isNaN(this.drawnLineEnd)) {
                position.x = this.drawnLineEnd;
            }
            // getPosOf already includes alignment, padding and text scale.
            // Do not apply ModularUI's -0.8px offset or divide by scale again.
            drawCaret((int) Math.ceil(position.x), Math.round(position.y) - 1,
                    Math.max(1, Math.round(getFontHeight())));
        }
    }

    @Override protected float draw(String text, float x, float y) {
        float end = drawText(text, x, y);
        // FontRenderer returns an integer endpoint, which may truncate a
        // fractional glyph edge before scaling. Reserve that scaled pixel too.
        if (!this.simulate) this.drawnLineEnd = end + Math.max(1, this.scale);
        return end;
    }

    protected float drawText(String text, float x, float y) {
        return super.draw(text, x, y);
    }

    protected void drawCaret(int x, int y, int height) {
        Gui.drawRect(x, y, x + 1, y + height, this.color | 0xFF000000);
    }
}
