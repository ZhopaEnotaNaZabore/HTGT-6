package com.mod.htgt6.client.vanillatweaks.CHAT;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Prefer protocol translation keys; use sender-shaped prefixes only for plugin/plain text. */
final class ChatMessageClassifier {
    private static final Pattern PLAYER = Pattern.compile("^(?:\\[[^\\]]+\\]\\s*)*(?:<([^>]+)>|([A-Za-z0-9_]{1,16})\\s*:)\\s*.*", Pattern.DOTALL);
    private ChatMessageClassifier() {}

    static boolean isSystem(String text, String translationKey) {
        if (translationKey != null) {
            if (translationKey.equals("chat.type.text") || translationKey.equals("chat.type.announcement")
                    || translationKey.equals("chat.type.emote") || translationKey.startsWith("commands.message.display.")) return false;
            // Hybrid servers also wrap ordinary player chat in literal/custom translation
            // formats (e.g. "%s" or "<%s> %s"). Translation alone does not mean system.
            if (translationKey.startsWith("commands.") || translationKey.startsWith("death.")
                    || translationKey.startsWith("multiplayer.") || translationKey.startsWith("disconnect.")
                    || translationKey.equals("chat.type.admin") || translationKey.equals("chat.type.achievement")) return true;
        }
        String clean = clean(text);
        String lower = clean.toLowerCase(Locale.ROOT);
        // Explicit service labels take precedence, even if the body mentions <a player>.
        if (lower.matches("^(?:\\[(?:hitech6|server|system|info|memory|broadcast)\\]|(?:server|system|info|broadcast|debug|coord|coords|x|y|z|id|block)\\s*:).*")) return true;
        if (PLAYER.matcher(clean).matches()) return false;
        // Common plain-text private-message bridges.
        return !clean.matches("(?s)^\\[?[A-Za-z0-9_]{1,16}\\s*(?:->|→)\\s*[A-Za-z0-9_]{1,16}\\]?.*");
    }

    static String sender(String text) {
        Matcher matcher = PLAYER.matcher(clean(text));
        if (!matcher.matches()) return "";
        String value = matcher.group(1) == null ? matcher.group(2) : matcher.group(1);
        return value.replaceAll("\\[[^\\]]+\\]", "").trim();
    }

    static String clean(String text) { return text == null ? "" : colors(text).replaceAll("(?i)§[0-9a-fk-or]", "").trim(); }

    static String colors(String text) {
        return text.replaceAll("(?i)&([0-9a-fk-or])", "§$1");
    }
}
