package com.mod.htgt6.client.vanillatweaks.CHAT;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatRender {

    public enum ChatFilter {
        GLOBAL, PLAYERS, SYSTEM
    }

    // Globally accessible filter state so chatNEW screen can read/write to it
    public static ChatFilter currentFilter = ChatFilter.GLOBAL;

    private static final float TEXT_SCALE = 1.0F;
    private static final int VANILLA_LINE_HEIGHT = (int) (12 * TEXT_SCALE);

    private static final int EMOJI_SIZE = 36;
    private static final int EMOJI_LINE_HEIGHT = EMOJI_SIZE + 4;

    private static final int MAX_VISIBLE_LINES = 16;

    // Separate trackers for unread message types
    private static int unreadPlayerCount = 0;
    private static int unreadSystemCount = 0;
    private boolean wasChatOpenLastFrame = false;

    // Scrolling History mechanics
    private int scrollOffset = 0;

    // Tab positioning variables for mouse clicking tracking
    public static int tabGlobalLeft, tabGlobalRight, tabPlayersLeft, tabPlayersRight, tabSystemLeft, tabSystemRight;
    public static int tabsTopBounds, tabsBottomBounds;

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        if (event.message == null) return;
        if (event.message instanceof ChatComponentTranslation
                && "chat.type.achievement".equals(((ChatComponentTranslation) event.message).getKey())
                && acceptVanillaAchievement((ChatComponentTranslation) event.message)) {
            event.setCanceled(true);
            return;
        }
        String text = formatChatComponent(event.message);
        String key = event.message instanceof ChatComponentTranslation ? ((ChatComponentTranslation) event.message).getKey() : null;
        boolean system = ChatMessageClassifier.isSystem(text, key);
        List<String> images = ChatImageLinks.find(event.message.getUnformattedText());
        for (Object part : event.message) {
            net.minecraft.event.ClickEvent click = ((IChatComponent) part).getChatStyle().getChatClickEvent();
            if (click != null && click.getAction() == net.minecraft.event.ClickEvent.Action.OPEN_URL
                    && ChatImageLinks.valid(click.getValue()) && !images.contains(click.getValue()) && images.size() < 3)
                images.add(click.getValue());
        }
        ChatManager.addMessage(text, system, images);
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof chatNEW) && !ChatModularScreen.isOpen()) {
            String localPlayer = mc.thePlayer == null ? "" : mc.thePlayer.getCommandSenderName();
            if (system) unreadSystemCount++;
            else if (!localPlayer.equals(ChatMessageClassifier.sender(text))) unreadPlayerCount++;
        }
    }

    private boolean acceptVanillaAchievement(ChatComponentTranslation component) {
        Object[] args = component.getFormatArgs();
        if (args.length < 2 || !(args[1] instanceof IChatComponent)) return false;
        IChatComponent badge = (IChatComponent) args[1];
        net.minecraft.event.HoverEvent hover = badge.getChatStyle().getChatHoverEvent();
        if (hover == null || hover.getAction() != net.minecraft.event.HoverEvent.Action.SHOW_ACHIEVEMENT) return false;
        String id = hover.getValue().getUnformattedText();
        net.minecraft.stats.StatBase stat = net.minecraft.stats.StatList.func_151177_a(id);
        if (!(stat instanceof net.minecraft.stats.Achievement)) return false;
        String player = args[0] instanceof IChatComponent ? ((IChatComponent) args[0]).getUnformattedText() : String.valueOf(args[0]);
        ChatManager.addAchievement(player, id, ((net.minecraft.stats.Achievement) stat).getSpecial());
        return true;
    }

    @SubscribeEvent
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.CHAT) {
            event.setCanceled(true); // Cancels default chat rendering completely
            renderCustomChatWindow();
        }
    }

    private String formatChatComponent(IChatComponent mainComponent) {
        if (mainComponent == null) return "";

        StringBuilder sb = new StringBuilder();
        String ambientFormatting = "";

        java.util.Iterator iterator = mainComponent.iterator();
        while (iterator.hasNext()) {
            IChatComponent piece = (IChatComponent) iterator.next();
            String explicitStyle = piece.getChatStyle().getFormattingCode();

            if (!explicitStyle.isEmpty()) {
                ambientFormatting = updateAmbientFormatting(ambientFormatting, explicitStyle);
            }

            sb.append(ambientFormatting);

            String text = ChatMessageClassifier.colors(piece.getUnformattedTextForChat());
            sb.append(text);

            ambientFormatting = updateAmbientFormatting(ambientFormatting, text);
        }
        return sb.toString();
    }

    private String updateAmbientFormatting(String currentAmbient, String text) {
        String newAmbient = currentAmbient;
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '§') {
                char codeChar = text.charAt(i + 1);

                if (codeChar == 'r' || codeChar == 'R') {
                    newAmbient = "";
                }
                else if ((Character.isDigit(codeChar) || (codeChar >= 'a' && codeChar <= 'f') || (codeChar >= 'A' && codeChar <= 'F'))) {
                    newAmbient = "§" + codeChar;
                }
                else {
                    newAmbient += "§" + codeChar;
                }
                i++;
            }
        }
        return newAmbient;
    }

    // Dynamic Filter handler
    static List<String> getFilteredMessagesSnapshot() {
        List<String> result = new ArrayList<String>();
        for (ChatManager.ChatEntry entry : getFilteredEntriesSnapshot()) {
            if (entry instanceof ChatManager.TextEntry) result.add(((ChatManager.TextEntry) entry).text);
        }
        return result;
    }

    static List<ChatManager.ChatEntry> getFilteredEntriesSnapshot() {
        List<ChatManager.ChatEntry> rawEntries = ChatManager.getEntriesSnapshot();
        if (currentFilter == ChatFilter.GLOBAL) return rawEntries;

        List<ChatManager.ChatEntry> filtered = new ArrayList<ChatManager.ChatEntry>();
        for (ChatManager.ChatEntry entry : rawEntries) {
            if (entry instanceof ChatManager.AchievementEntry) {
                if (currentFilter == ChatFilter.SYSTEM) filtered.add(entry);
                continue;
            }
            boolean isSys = ((ChatManager.TextEntry) entry).system;
            if ((currentFilter == ChatFilter.PLAYERS && !isSys)
                    || (currentFilter == ChatFilter.SYSTEM && isSys)) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    static void resetUnread() { unreadPlayerCount = 0; unreadSystemCount = 0; }

    static void markSystemUnread() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof chatNEW) && !ChatModularScreen.isOpen()) {
            unreadSystemCount++;
        }
    }

    private void renderCustomChatWindow() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        boolean isLegacyChatOpen = mc.currentScreen instanceof chatNEW;
        boolean isModularChatOpen = ChatModularScreen.isOpen();
        boolean isChatOpen = isLegacyChatOpen || isModularChatOpen;
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        // Reset unread notifications state instantly when chat screen opens
        if (isChatOpen && !wasChatOpenLastFrame) {
            unreadPlayerCount = 0;
            unreadSystemCount = 0;
            scrollOffset = 0; // Reset scroll position when opening chat
        }
        wasChatOpenLastFrame = isChatOpen;

        // The open ModularUI screen owns the complete chat panel.  This event
        // remains responsible only for the closed unread indicator.
        if (isModularChatOpen) return;

        // If chat is CLOSED, draw the dual indicator icons
        if (!isChatOpen) {
            if (unreadPlayerCount > 0 || unreadSystemCount > 0) {
                renderChatNotificationIcon(sr, unreadPlayerCount, unreadSystemCount);
            }
            return;
        }

        List<String> activeMessagesList = getFilteredMessagesSnapshot();

        // --- MOUSE WHEEL SCROLLING HANDLER ---
        int dWheel = Mouse.getDWheel();
        if (dWheel != 0) {
            if (dWheel > 0) {
                scrollOffset++; // Scroll up into history
            } else if (dWheel < 0) {
                scrollOffset--; // Scroll down towards newest
            }

            // Constrain scrolling limits
            int maxPossibleScroll = Math.max(0, activeMessagesList.size() - MAX_VISIBLE_LINES);
            if (scrollOffset > maxPossibleScroll) scrollOffset = maxPossibleScroll;
            if (scrollOffset < 0) scrollOffset = 0;
        }

        int x = 4;
        int absoluteBottomY = sr.getScaledHeight() - 16;

        int totalChatHeight = 0;
        int checkCount = 0;
        int actualLinesToRender = 0;

        // Account for scrollOffset when analyzing bounds safely (prevents IndexOutOfBoundsException)
        int startIndex;
        if (activeMessagesList.isEmpty()) {
            startIndex = -1;
        } else {
            startIndex = (activeMessagesList.size() - 1) - scrollOffset;
            if (startIndex < 0) startIndex = 0;
        }

        if (startIndex >= 0) {
            for (int i = startIndex; i >= 0; i--) {
                if (checkCount >= MAX_VISIBLE_LINES) break;

                totalChatHeight += hasMessageEmoji(activeMessagesList.get(i)) ? EMOJI_LINE_HEIGHT : VANILLA_LINE_HEIGHT;
                checkCount++;
                actualLinesToRender++;
            }
        }

        int calculatedTopBounds = absoluteBottomY - totalChatHeight - 12;

        int leftBounds = x - 2;
        int rightBounds = sr.getScaledWidth(); // EXTENDED: now extends all the way to screen width edge
        int bottomBounds = sr.getScaledHeight() - 12;
        int topBounds = (actualLinesToRender > 0) ? calculatedTopBounds : (bottomBounds - 65);

        // Draw main background
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glColor4f(0.066F, 0.066F, 0.066F, 0.803F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(leftBounds, topBounds);
        GL11.glVertex2f(leftBounds, bottomBounds);
        GL11.glVertex2f(rightBounds, bottomBounds);
        GL11.glVertex2f(rightBounds, topBounds);
        GL11.glEnd();

        // Top Border Accent
        GL11.glColor4f(0.6F, 0.2F, 1.0F, 1.0F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(leftBounds, topBounds);
        GL11.glVertex2f(leftBounds, topBounds + 2);
        GL11.glVertex2f(rightBounds, topBounds + 2);
        GL11.glVertex2f(rightBounds, topBounds);
        GL11.glEnd();

        // --- TABS RENDERING SWITCHER DESIGN ---
        tabsTopBounds = topBounds - 16;
        tabsBottomBounds = topBounds;

        tabGlobalLeft = leftBounds + 2;
        tabGlobalRight = tabGlobalLeft + 50;
        tabPlayersLeft = tabGlobalRight + 4;
        tabPlayersRight = tabPlayersLeft + 60;
        tabSystemLeft = tabPlayersRight + 4;
        tabSystemRight = tabSystemLeft + 60;

        drawTabBackground(tabGlobalLeft, tabGlobalRight, tabsTopBounds, tabsBottomBounds, currentFilter == ChatFilter.GLOBAL);
        drawTabBackground(tabPlayersLeft, tabPlayersRight, tabsTopBounds, tabsBottomBounds, currentFilter == ChatFilter.PLAYERS);
        drawTabBackground(tabSystemLeft, tabSystemRight, tabsTopBounds, tabsBottomBounds, currentFilter == ChatFilter.SYSTEM);

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Draw Tabs text
        mc.fontRenderer.drawStringWithShadow("Global", tabGlobalLeft + 6, tabsTopBounds + 4, currentFilter == ChatFilter.GLOBAL ? 0xFFFFFF : 0xAAAAAA);
        mc.fontRenderer.drawStringWithShadow("Players", tabPlayersLeft + 6, tabsTopBounds + 4, currentFilter == ChatFilter.PLAYERS ? 0x55FFFF : 0xAAAAAA);
        mc.fontRenderer.drawStringWithShadow("System", tabSystemLeft + 6, tabsTopBounds + 4, currentFilter == ChatFilter.SYSTEM ? 0xFF5555 : 0xAAAAAA);

        // VISUAL SCROLL INDICATOR
        if (activeMessagesList.size() > MAX_VISIBLE_LINES) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(0.6F, 0.2F, 1.0F, 0.4F);
            int sbWidth = 3;
            int sbRight = rightBounds - 2;
            int sbLeft = sbRight - sbWidth;

            float totalHistorySize = activeMessagesList.size();
            float barHeight = ((float) MAX_VISIBLE_LINES / totalHistorySize) * (bottomBounds - topBounds - 4);
            float scrollProgress = (float) scrollOffset / (totalHistorySize - MAX_VISIBLE_LINES);
            float barTop = topBounds + 2 + scrollProgress * ((bottomBounds - topBounds - 4) - barHeight);

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(sbLeft, barTop);
            GL11.glVertex2f(sbLeft, barTop + barHeight);
            GL11.glVertex2f(sbRight, barTop + barHeight);
            GL11.glVertex2f(sbRight, barTop);
            GL11.glEnd();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();

        int currentY = absoluteBottomY;
        int renderedLinesCount = 0;
        int playerRowCounter = 0;

        if (startIndex >= 0) {
            for (int i = startIndex; i >= 0; i--) {
                String msg = activeMessagesList.get(i);
                if (renderedLinesCount >= MAX_VISIBLE_LINES) break;

                boolean hasEmoji = hasMessageEmoji(msg);
                int lineHeight = hasEmoji ? EMOJI_LINE_HEIGHT : VANILLA_LINE_HEIGHT;

                String cleanMsg = clean(msg);
                boolean isSystemEvent = isSystemMessage(cleanMsg);

                if (!isSystemEvent && i > 0) {
                    isSystemEvent = isSystemMessage(clean(activeMessagesList.get(i - 1)));
                }
                if (!isSystemEvent && i < activeMessagesList.size() - 1) {
                    isSystemEvent = isSystemMessage(clean(activeMessagesList.get(i + 1)));
                }

                if (!isSystemEvent) {
                    if (playerRowCounter % 2 == 0) {
                        GL11.glPushMatrix();
                        GL11.glEnable(GL11.GL_BLEND);
                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.102F);
                        GL11.glBegin(GL11.GL_QUADS);
                        GL11.glVertex2f(leftBounds + 4, currentY - lineHeight);
                        GL11.glVertex2f(leftBounds + 4, currentY);
                        GL11.glVertex2f(rightBounds - 6, currentY);
                        GL11.glVertex2f(rightBounds - 6, currentY - lineHeight);
                        GL11.glEnd();

                        GL11.glEnable(GL11.GL_TEXTURE_2D);
                        GL11.glDisable(GL11.GL_BLEND);
                        GL11.glPopMatrix();
                    }
                    playerRowCounter++;
                }

                GL11.glPushMatrix();
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glScalef(TEXT_SCALE, TEXT_SCALE, 1.0F);

                float scaledX = (x + 4) / TEXT_SCALE;
                float scaledY = (currentY - lineHeight + 3) / TEXT_SCALE;

                if (hasEmoji) {
                    scaledY += (EMOJI_LINE_HEIGHT - VANILLA_LINE_HEIGHT - 2) / TEXT_SCALE;
                }

                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

                mc.fontRenderer.drawStringWithShadow(TitleVisuals.animate(msg, mc.ingameGUI.getUpdateCounter()), (int) scaledX, (int) scaledY, 0xFFFFFF);
                GL11.glPopMatrix();

                int emojiIdx = 0;
                for (Map.Entry<String, ResourceLocation> entry : EmojiRegistry.getRegisteredEmojis().entrySet()) {
                    ResourceLocation texture = entry.getValue();
                    String invisibleMarker = "§e§e§" + (char)('a' + emojiIdx) + "§r    ";

                    if (msg.contains(invisibleMarker)) {
                        int index = 0;
                        while ((index = msg.indexOf(invisibleMarker, index)) != -1) {
                            String textBeforeEmoji = msg.substring(0, index);
                            int pixelOffset = (int) (mc.fontRenderer.getStringWidth(textBeforeEmoji) * TEXT_SCALE);

                            if (texture != null) {
                                int adjustedY = currentY - EMOJI_SIZE + 1;
                                drawFadeableEmoji(texture, (int)scaledX + pixelOffset, adjustedY, EMOJI_SIZE, 1.0F);
                            }
                            index += invisibleMarker.length();
                        }
                    }
                    emojiIdx++;
                }

                currentY -= lineHeight;
                renderedLinesCount++;
            }
        }
    }

    private void drawTabBackground(int left, int right, int top, int bottom, boolean isActive) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        if (isActive) {
            GL11.glColor4f(0.6F, 0.2F, 1.0F, 0.8F);
        } else {
            GL11.glColor4f(0.066F, 0.066F, 0.066F, 0.6F);
        }
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(left, top);
        GL11.glVertex2f(left, bottom);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(right, top);
        GL11.glEnd();
    }

    private boolean hasMessageEmoji(String msg) {
        int emojiIdx = 0;
        for (Map.Entry<String, ResourceLocation> entry : EmojiRegistry.getRegisteredEmojis().entrySet()) {
            if (msg.contains(EmojiRegistry.marker(emojiIdx))) {
                return true;
            }
            emojiIdx++;
        }
        return false;
    }

    /**
     * Renders two distinct notification counts simultaneously over the chat icon structure
     */
    private void renderChatNotificationIcon(ScaledResolution sr, int playerMsgs, int systemMsgs) {
        Minecraft mc = Minecraft.getMinecraft();

        // Icon base size rules
        int iconWidth = 32;
        int iconHeight = 18;
        int iconX = 8;
        int iconY = sr.getScaledHeight() - iconHeight - 12;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // 1. Base Bubble Area
        GL11.glColor4f(0.12F, 0.12F, 0.12F, 0.85F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(iconX, iconY);
        GL11.glVertex2f(iconX, iconY + iconHeight);
        GL11.glVertex2f(iconX + iconWidth, iconY + iconHeight);
        GL11.glVertex2f(iconX + iconWidth, iconY);
        GL11.glEnd();

        // 2. Tail Pointer
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(iconX + 5, iconY + iconHeight);
        GL11.glVertex2f(iconX + 5, iconY + iconHeight + 5);
        GL11.glVertex2f(iconX + 11, iconY + iconHeight);
        GL11.glEnd();

        // 3. Left Design Accent Trim
        GL11.glColor4f(0.6F, 0.2F, 1.0F, 1.0F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(iconX, iconY);
        GL11.glVertex2f(iconX, iconY + iconHeight);
        GL11.glVertex2f(iconX + 2.0F, iconY + iconHeight);
        GL11.glVertex2f(iconX + 2.0F, iconY);
        GL11.glEnd();

        // --- DUAL BADGE RENDERING LOGIC ---
        int currentBadgeX = iconX + 4;
        int badgeY = iconY + 3;
        int badgeHeight = 11;
        float textScale = 0.80F;

        // 1. DRAW PLAYER MESSAGE COUNTER (BLUE PILL)
        if (playerMsgs > 0) {
            String pCountStr = playerMsgs > 99 ? "99+" : String.valueOf(playerMsgs);
            int pWidth = (int) (mc.fontRenderer.getStringWidth(pCountStr) * textScale) + 4;

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(0.15F, 0.45F, 0.85F, 1.0F); // Ice Blue
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(currentBadgeX, badgeY);
            GL11.glVertex2f(currentBadgeX, badgeY + badgeHeight);
            GL11.glVertex2f(currentBadgeX + pWidth, badgeY + badgeHeight);
            GL11.glVertex2f(currentBadgeX + pWidth, badgeY);
            GL11.glEnd();

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glPushMatrix();
            GL11.glScalef(textScale, textScale, 1.0F);
            mc.fontRenderer.drawString(pCountStr, (int)((currentBadgeX + 2) / textScale), (int)((badgeY + 2) / textScale), 0xFFFFFF);
            GL11.glPopMatrix();

            currentBadgeX += pWidth + 3; // Shift next indicator position to the right
        }

        // 2. DRAW SYSTEM MESSAGE COUNTER (RED PILL)
        if (systemMsgs > 0) {
            String sCountStr = systemMsgs > 99 ? "99+" : String.valueOf(systemMsgs);
            int sWidth = (int) (mc.fontRenderer.getStringWidth(sCountStr) * textScale) + 4;

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(0.85F, 0.15F, 0.15F, 1.0F); // Crimson Red
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(currentBadgeX, badgeY);
            GL11.glVertex2f(currentBadgeX, badgeY + badgeHeight);
            GL11.glVertex2f(currentBadgeX + sWidth, badgeY + badgeHeight);
            GL11.glVertex2f(currentBadgeX + sWidth, badgeY);
            GL11.glEnd();

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glPushMatrix();
            GL11.glScalef(textScale, textScale, 1.0F);
            mc.fontRenderer.drawString(sCountStr, (int)((currentBadgeX + 2) / textScale), (int)((badgeY + 2) / textScale), 0xFFFFFF);
            GL11.glPopMatrix();
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private void drawRoundedRect(float x, float y, float width, float height, float radius) {
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(x + width / 2.0F, y + height / 2.0F);
        for (int i = 180; i <= 270; i += 10) {
            double angle = Math.toRadians(i);
            GL11.glVertex2f(x + radius + (float)Math.cos(angle) * radius, y + radius + (float)Math.sin(angle) * radius);
        }
        for (int i = 270; i <= 360; i += 10) {
            double angle = Math.toRadians(i);
            GL11.glVertex2f(x + width - radius + (float)Math.cos(angle) * radius, y + radius + (float)Math.sin(angle) * radius);
        }
        for (int i = 0; i <= 90; i += 10) {
            double angle = Math.toRadians(i);
            GL11.glVertex2f(x + width - radius + (float)Math.cos(angle) * radius, y + height - radius + (float)Math.sin(angle) * radius);
        }
        for (int i = 90; i <= 180; i += 10) {
            double angle = Math.toRadians(i);
            GL11.glVertex2f(x + radius + (float)Math.cos(angle) * radius, y + height - radius + (float)Math.sin(angle) * radius);
        }
        GL11.glEnd();
    }

    static String clean(String text) {
        return text == null ? "" : text.replaceAll("(?i)§[0-9a-fk-or]", "").trim();
    }

    static boolean isSystemMessage(String cleanText) {
        return ChatMessageClassifier.isSystem(cleanText, null);
    }

    private void drawFadeableEmoji(ResourceLocation texture, int x, int y, int size, float alpha) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0); GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(0, 1); GL11.glVertex2f(x, y + size);
        GL11.glTexCoord2f(1, 1); GL11.glVertex2f(x + size, y + size);
        GL11.glTexCoord2f(1, 0); GL11.glVertex2f(x + size, y);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}
