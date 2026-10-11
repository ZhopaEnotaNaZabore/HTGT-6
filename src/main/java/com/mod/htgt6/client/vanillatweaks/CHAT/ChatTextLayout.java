package com.mod.htgt6.client.vanillatweaks.CHAT;

import java.util.ArrayList;
import java.util.List;

/** Pixel layout shared by drawing and scrolling, with separate sticker rows. */
final class ChatTextLayout {
    interface Measure { int width(String text); }
    static final int TEXT_HEIGHT = 14;
    static final int STICKER_SIZE = 48;
    static final int STICKER_WIDTH = 56;

    static final class Run {
        String text;
        final int sticker, x;
        int width;
        int y;
        Run(String text, int sticker, int x, int width) {
            this.text = text; this.sticker = sticker; this.x = x; this.width = width;
        }
    }

    final List<Run> runs = new ArrayList<Run>();
    int height;
    private final int maxWidth;
    private final Measure measure;
    private int x, rowStart, rowHeight = TEXT_HEIGHT;
    private String style = "";

    private ChatTextLayout(int width, Measure measure) {
        this.maxWidth = Math.max(1, width);
        this.measure = measure;
    }

    static ChatTextLayout create(String text, int width, List<String> markers, Measure measure) {
        ChatTextLayout result = new ChatTextLayout(width, measure);
        StringBuilder word = new StringBuilder();
        for (int i = 0; i < text.length();) {
            int sticker = -1;
            for (int m = 0; m < markers.size(); m++) {
                if (text.startsWith(markers.get(m), i)) { sticker = m; break; }
            }
            if (sticker >= 0) {
                result.word(word.toString()); word.setLength(0);
                if (result.x > 0 && result.rowHeight == TEXT_HEIGHT) result.finishRow();
                result.add(null, sticker, STICKER_WIDTH);
                result.style = ""; // The legacy marker ends with Minecraft's reset code.
                i += markers.get(sticker).length();
            } else {
                char c = text.charAt(i++);
                if (c == '\n' || c == ' ') {
                    result.word(word.toString()); word.setLength(0);
                    if (c == '\n') result.finishRow();
                    else if (result.x > 0 && result.rowHeight == TEXT_HEIGHT)
                        result.add(result.style + " ", -1, Math.max(4, measure.width(result.style + " ")));
                } else {
                    word.append(c);
                    if (c == '\u00a7' && i < text.length()) word.append(text.charAt(i++));
                }
            }
        }
        result.word(word.toString());
        result.finishRow();
        return result;
    }

    private void word(String word) {
        if (word.isEmpty()) return;
        if (rowHeight != TEXT_HEIGHT) finishRow();
        String formatted = style + word;
        int width = measure.width(formatted);
        if (width <= maxWidth) {
            add(formatted, -1, width);
            updateStyle(word);
            return;
        }
        // Unbroken URLs and long names must also stay inside the viewport.
        for (int i = 0; i < word.length();) {
            char c = word.charAt(i++);
            if (c == '\u00a7' && i < word.length()) {
                updateStyle("\u00a7" + word.charAt(i++));
            } else {
                String glyph = style + c;
                add(glyph, -1, measure.width(glyph));
            }
        }
    }

    private void updateStyle(String text) {
        for (int i = 0; i + 1 < text.length(); i++) {
            if (text.charAt(i) != '\u00a7') continue;
            char code = Character.toLowerCase(text.charAt(++i));
            if (code == 'r') style = "";
            else if ("0123456789abcdef".indexOf(code) >= 0) style = "\u00a7" + code;
            else if ("klmno".indexOf(code) >= 0 && !style.contains("\u00a7" + code)) style += "\u00a7" + code;
        }
    }

    private void add(String text, int sticker, int width) {
        if (x > 0 && x + width > maxWidth) finishRow();
        if (x == 0 && text != null && text.endsWith(" ")) return;
        runs.add(new Run(text, sticker, x, width));
        x += width;
        if (sticker >= 0) rowHeight = STICKER_SIZE + 12;
    }

    private void finishRow() {
        // Draw each text row in one font call: preserve inter-word spacing and formatting.
        // Positions are still measured per word for wrapping.
        if (rowHeight == TEXT_HEIGHT && rowStart < runs.size()) {
            Run first = runs.get(rowStart);
            StringBuilder line = new StringBuilder(first.text);
            for (int i = rowStart + 1; i < runs.size(); i++) line.append(runs.get(i).text);
            first.text = line.toString();
            first.width = x - first.x;
            while (runs.size() > rowStart + 1) runs.remove(runs.size() - 1);
        }
        for (int i = rowStart; i < runs.size(); i++) {
            Run run = runs.get(i);
            run.y = height + (run.sticker >= 0 ? 6 : 3);
        }
        height += rowHeight;
        rowStart = runs.size(); x = 0; rowHeight = TEXT_HEIGHT;
    }
}
