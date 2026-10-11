package com.mod.htgt6.client.vanillatweaks.CHAT;

import java.util.Arrays;
import java.util.List;

/** Standalone checks; no Minecraft bootstrap or graphics context required. */
public final class ChatLayoutCheck {
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    public static void main(String[] args) {
        List<String> markers = Arrays.asList("§e§e§a§r    ", "§e§e§h§r    ");
        ChatTextLayout.Measure font = text -> text.replaceAll("§.", "").length() * 6;
        List<String> expandedMarkers = new java.util.ArrayList<String>();
        StringBuilder expandedMessage = new StringBuilder("<Alex> ");
        for (int i = 0; i < 22; i++) {
            String marker = "§e§e§" + (char) ('a' + i) + "§r    ";
            expandedMarkers.add(marker);
            expandedMessage.append(marker);
        }
        ChatTextLayout expanded = ChatTextLayout.create(expandedMessage.toString(), 240, expandedMarkers, font);
        int seen = 0;
        for (ChatTextLayout.Run run : expanded.runs) {
            if (run.sticker >= 0) require(run.sticker == seen++, "Expanded marker became font formatting");
            require(run.x + run.width <= 240, "Expanded sticker set overflow");
        }
        require(seen == 22, "Expanded sticker set lost an asset");
        ChatTextLayout layout = ChatTextLayout.create("Alex: " + markers.get(0) + markers.get(1) + " привет!", 100, markers, font);
        int stickers = 0;
        for (ChatTextLayout.Run run : layout.runs) {
            require(run.x >= 0 && run.x + run.width <= 100, "Inline content crossed the right edge");
            if (run.sticker >= 0) stickers++;
            require(run.y >= 0 && run.y < layout.height, "Run outside measured height");
        }
        require(stickers == 2, "Lost a repeated/adjacent sticker");
        require(layout.height > ChatTextLayout.TEXT_HEIGHT, "Sticker row did not grow");
        ChatTextLayout spaced = ChatTextLayout.create("<Prizma> hello  world", 240, markers, font);
        require(spaced.runs.size() == 1, "Text fragmented into separate draw calls");
        require(spaced.runs.get(0).text.equals("<Prizma> hello  world"), "Inter-word spaces lost");
        ChatTextLayout caption = ChatTextLayout.create("<Prizma> " + markers.get(0) + "after", 240, markers, font);
        require(caption.runs.get(1).sticker == 0 && caption.runs.get(1).x == 0,
                "Sticker did not start below sender");
        require(caption.runs.get(1).y >= ChatTextLayout.TEXT_HEIGHT,
                "Sticker overlaps sender row");
        require(caption.runs.get(2).y >= caption.runs.get(1).y + ChatTextLayout.STICKER_SIZE,
                "Following text overlaps sticker");
        ChatTextLayout word = ChatTextLayout.create("§b" + "abcdefghijklmnopqrstuvwxy", 48, markers, font);
        require(word.height >= 4 * ChatTextLayout.TEXT_HEIGHT, "Long unbroken text did not wrap");
        for (ChatTextLayout.Run run : word.runs) {
            require(run.x + run.width <= 48, "Long word crossed right edge");
            require(run.text.startsWith("§b"), "Formatting lost across wraps");
        }
        ChatTextLayout mixed = ChatTextLayout.create("§lBold " + markers.get(1) + "normal\nnext", 80, markers, font);
        require(mixed.runs.get(mixed.runs.size()-1).text.equals("next"), "Sticker reset/newline lost");
        require(!ChatMessageClassifier.isSystem("<Alex> my fps died near this block", null), "Player body misclassified");
        require(!ChatMessageClassifier.isSystem("<Prizma> тест тест", "%s"), "Hybrid chat wrapper misclassified");
        require(!ChatMessageClassifier.isSystem("<Prizma> :Pivo:", "<%s> %s"), "Hybrid sticker misclassified");
        require(!ChatMessageClassifier.isSystem("&b<Prizma>&r hello", "plugin.chat.format"), "Colored custom chat misclassified");
        require(ChatMessageClassifier.isSystem("Successfully teleported to logout location!", "%s"), "Teleport notice misclassified");
        require(ChatMessageClassifier.isSystem("Установлен режим игры для игрока Prizma", "commands.gamemode.success.other"), "Gamemode notice misclassified");
        require(!ChatMessageClassifier.isSystem("[VIP] Alex: item debug", null), "Ranked player misclassified");
        require(ChatMessageClassifier.isSystem("[HiTech6] Игрок <Alex> получил титул", null), "Announcement misclassified");
        require(ChatMessageClassifier.isSystem("Server: restart in 5 minutes", null), "Server prefix misclassified");
        require(ChatMessageClassifier.isSystem("Alex утонул", "death.attack.drown"), "Translated death misclassified");
        require(!ChatMessageClassifier.isSystem("Alex шепчет: привет", "commands.message.display.incoming"), "Private message misclassified");
        require(!ChatMessageClassifier.isSystem("[Alex -> Mira] привет", null), "Plain private message misclassified");
        require(ChatMessageClassifier.isSystem("Used JVM Memory: 400 MB", null), "Local diagnostic misclassified");
        require(ChatMessageClassifier.colors("A & B &aGreen").equals("A & B §aGreen"), "Literal ampersand corrupted");
        require(ChatMessageClassifier.sender("<[VIP] Alex> hi").equals("Alex"), "Sender/title extraction");
        System.out.println("PASS: wrapping, inline sticker widths, formatting, channel routing, translations and literal text");
    }
}
