package com.mod.htgt6.client.vanillatweaks.CHAT;

import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatManager {

    public static final List<String> messages = new ArrayList<String>();
    public static final List<Long> timestamps = new ArrayList<Long>();

    public static void addMessage(String msg) {
        if (msg == null || msg.isEmpty()) return;

        // 1. Convert common network color format to Minecraft's standard '§'
        String processedMsg = msg.replace('&', '§');

        // 2. Process BBCode tag formats
        processedMsg = formatBBCode(processedMsg);

        // 3. Inject role prefixes and perform final formatting cleanup
        String fullyFormattedMessage = injectRolePrefixes(processedMsg);

        messages.add(fullyFormattedMessage);
        timestamps.add(System.currentTimeMillis());

        // Maintain a manageable history size
        if (messages.size() > 100) {
            messages.remove(0);
            timestamps.remove(0);
        }
    }

    private static String formatBBCode(String text) {
        text = text.replaceAll("(?i)\\[b\\]", "§l");
        text = text.replaceAll("(?i)\\[/b\\]", "§r");
        return text;
    }

    private static String injectRolePrefixes(String msg) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return msg;

        String localPlayer = mc.thePlayer.getCommandSenderName();
        boolean isLocalOp = mc.thePlayer.canCommandSenderUseCommand(2, "");

        String cleanCheck = msg.replaceAll("(?i)§[0-9a-fk-or]", "").trim();
        if (cleanCheck.contains("has just earned the achievement") || cleanCheck.contains("was slain by") ||
                cleanCheck.contains("-> me") || cleanCheck.contains("me ->") || cleanCheck.startsWith("[")) {
            return msg;
        }

        Pattern pattern = Pattern.compile("<([^>]+)>");
        Matcher matcher = pattern.matcher(msg);

        if (matcher.find()) {
            String fullSenderName = matcher.group(1);
            String cleanSenderName = fullSenderName.replaceAll("(?i)§[0-9a-fk-or]", "").trim();

            // FIX: Only check the actual sender's name to see if a prefix is already applied
            if (cleanSenderName.contains("[Developer]") || cleanSenderName.contains("[Player]")) {
                return msg;
            }

            // FIX: Use Pattern.quote to safely inject, and replaceFirst to avoid breaking in-sentence name mentions
            String quotedName = Pattern.quote(fullSenderName);

            if (cleanSenderName.equals(localPlayer)) {
                String prefix = isLocalOp ? "§4[Developer]§r " : "§e[Player]§r ";
                return msg.replaceFirst("<" + quotedName + ">", "<" + prefix + fullSenderName + ">");
            } else {
                return msg.replaceFirst("<" + quotedName + ">", "<§e[Player]§r " + fullSenderName + ">");
            }
        }

        if (msg.startsWith(localPlayer + ":")) {
            String prefix = isLocalOp ? "§4[Developer]§r " : "§e[Player]§r ";
            return prefix + msg;
        }

        return msg;
    }
}