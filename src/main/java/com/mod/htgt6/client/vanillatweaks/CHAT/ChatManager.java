package com.mod.htgt6.client.vanillatweaks.CHAT;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatManager {

    public static final List<String> messages = new ArrayList<String>();
    public static final List<Long> timestamps = new ArrayList<Long>();
    private static final List<ChatEntry> entries = new ArrayList<ChatEntry>();

    public static abstract class ChatEntry {
        public final long timestamp;

        private ChatEntry(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static final class TextEntry extends ChatEntry {
        public final String text;
        public final boolean system;
        public final List<String> images;

        private TextEntry(String text, long timestamp, boolean system, List<String> images) {
            super(timestamp);
            this.text = text;
            this.system = system;
            this.images = java.util.Collections.unmodifiableList(new ArrayList<String>(images));
        }
    }

    public static final class AchievementEntry extends ChatEntry {
        public final String playerName;
        public final String achievementId;
        public final boolean special;

        private AchievementEntry(String playerName, String achievementId, boolean special, long timestamp) {
            super(timestamp);
            this.playerName = playerName;
            this.achievementId = achievementId;
            this.special = special;
        }
    }

    public static void addMessage(String msg) {
        addMessage(msg, ChatMessageClassifier.isSystem(msg, null));
    }

    public static void addMessage(String msg, boolean system) {
        addMessage(msg, system, ChatImageLinks.find(msg));
    }

    public static void addMessage(String msg, boolean system, List<String> images) {
        if (msg == null || msg.isEmpty()) return;

        // 1. Convert common network color format to Minecraft's standard '§'
        String processedMsg = EmojiRegistry.encodeEmojis(ChatMessageClassifier.colors(msg));

        // 2. Process BBCode tag formats (MOVED UP: Converts [b]/[/b] for ALL incoming player messages)
        processedMsg = formatBBCode(processedMsg);

        synchronized (messages) {
            long timestamp = System.currentTimeMillis();
            String finalMessage = system ? processedMsg : injectTitlePrefix(processedMsg);
            messages.add(finalMessage);
            timestamps.add(timestamp);
            entries.add(new TextEntry(finalMessage, timestamp, system, images));

            // Maintain a manageable history size
            if (messages.size() > 100) {
                messages.remove(0);
                timestamps.remove(0);
            }
            trimEntries();
        }
    }

    public static void addAchievement(String playerName, String achievementId, boolean special) {
        if (playerName == null || playerName.isEmpty() || achievementId == null || achievementId.isEmpty()) return;
        synchronized (messages) {
            long now = System.currentTimeMillis();
            for (int i = entries.size() - 1; i >= 0; i--) {
                ChatEntry entry = entries.get(i);
                if (now - entry.timestamp > 5000L) break;
                if (entry instanceof AchievementEntry) {
                    AchievementEntry card = (AchievementEntry) entry;
                    if (card.playerName.equals(playerName) && card.achievementId.equals(achievementId)) return;
                }
            }
            entries.add(new AchievementEntry(playerName, achievementId, special, System.currentTimeMillis()));
            trimEntries();
        }
        ChatRender.markSystemUnread();
    }

    public static List<ChatEntry> getEntriesSnapshot() {
        synchronized (messages) {
            return new ArrayList<ChatEntry>(entries);
        }
    }

    private static void trimEntries() {
        while (entries.size() > 100) entries.remove(0);
    }

    /** Manual cleanup preserves chat history and only releases our image cache. */
    public static void requestClientCacheCleanup() { ChatImageCache.clear(); }
    /** Owned by MemoryCleaner on the client END tick, independent of chat visibility. */
    public static void tickClientCaches() { ChatImageCache.maintain(); }

    public static void clear() {
        synchronized (messages) {
            messages.clear();
            timestamps.clear();
            entries.clear();
            ChatImageCache.clear();
            ChatRender.resetUnread();
            TitleManager.clientUnlockedTitles.clear();
            ChatModularScreen.clearHistory();
        }
    }

    private static String formatBBCode(String text) {
        text = text.replaceAll("(?i)\\[b\\]", "§l");
        text = text.replaceAll("(?i)\\[/b\\]", "§r");
        return text;
    }

    private static String injectTitlePrefix(String message) {
        Matcher matcher = Pattern.compile("<([^>]+)>").matcher(message);
        if (!matcher.find()) return message;
        String name = matcher.group(1).replaceAll("(?i)§[0-9a-fk-or]", "").trim();
        String prefix = TitleManager.activePlayerTitles.get(name);
        return prefix == null || prefix.isEmpty() ? message : message.replaceFirst("<" + Pattern.quote(matcher.group(1)) + ">", Matcher.quoteReplacement("<" + prefix + matcher.group(1) + ">"));
    }

}
