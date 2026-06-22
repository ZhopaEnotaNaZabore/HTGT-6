package com.mod.htgt6.client.vanillatweaks.CHAT;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import java.util.Map;

public class ChatRender {

    private static final float TEXT_SCALE = 1.0F;
    private static final int VANILLA_LINE_HEIGHT = (int) (12 * TEXT_SCALE);

    private static final int EMOJI_SIZE = 36;
    private static final int EMOJI_LINE_HEIGHT = EMOJI_SIZE + 4;

    private static final int MAX_VISIBLE_LINES = 10;


    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        if (event.message != null) {
            String fullyFormattedMessage = formatChatComponent(event.message);
            ChatManager.addMessage(fullyFormattedMessage);
        }
    }

    /**
     * FIX: Completely block vanilla and other mods from rendering over our chat overlay
     */
    @SubscribeEvent
    public void onRenderGameOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.CHAT) {
            event.setCanceled(true); // Cancels default chat rendering completely
            renderCustomChatWindow();
        }
    }

    /**
     * SMART FORMATTER: Re-compiles IChatComponent to fix the 1.7.10 nested color reset bug.
     */
    private String formatChatComponent(IChatComponent mainComponent) {
        if (mainComponent == null) return "";

        StringBuilder sb = new StringBuilder();
        String ambientFormatting = "";

        java.util.Iterator iterator = mainComponent.iterator();
        while (iterator.hasNext()) {
            IChatComponent piece = (IChatComponent) iterator.next();
            String explicitStyle = piece.getChatStyle().getFormattingCode();

            // If this piece has an explicit style, apply it to the ambient tracker
            if (!explicitStyle.isEmpty()) {
                ambientFormatting = updateAmbientFormatting(ambientFormatting, explicitStyle);
            }

            // Append the correct ambient formatting before the text
            sb.append(ambientFormatting);

            String text = piece.getUnformattedTextForChat().replace('&', '§');
            sb.append(text);

            // Update ambient formatting for the next component in case this text contained inline § codes
            ambientFormatting = updateAmbientFormatting(ambientFormatting, text);
        }
        return sb.toString();
    }

    private String updateAmbientFormatting(String currentAmbient, String text) {
        String newAmbient = currentAmbient; // Inherit the current ambient style
        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == '§') {
                char codeChar = text.charAt(i + 1);

                // Handle Reset: Clear ambient entirely
                if (codeChar == 'r' || codeChar == 'R') {
                    newAmbient = "";
                }
                // Handle Colors: Colors override previous colors/styles in 1.7.10
                else if ((Character.isDigit(codeChar) || (codeChar >= 'a' && codeChar <= 'f') || (codeChar >= 'A' && codeChar <= 'F'))) {
                    newAmbient = "§" + codeChar;
                }
                // Handle Styles: Styles (bold, italic) stack on top of current colors
                else {
                    newAmbient += "§" + codeChar;
                }
                i++; // Skip the code character
            }
        }
        return newAmbient;
    }

    private void renderCustomChatWindow() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;

        boolean isChatOpen = mc.currentScreen instanceof chatNEW;
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        int x = 4;
        int absoluteBottomY = sr.getScaledHeight() - 16;

        int totalChatHeight = 0;
        int checkCount = 0;
        int actualLinesToRender = 0;

        for (int i = ChatManager.messages.size() - 1; i >= 0; i--) {
            if (checkCount >= MAX_VISIBLE_LINES) break;

            long timestamp = ChatManager.timestamps.get(i);
            long age = System.currentTimeMillis() - timestamp;
            if (age > 10000 && !isChatOpen) continue;

            String msg = ChatManager.messages.get(i);
            boolean hasEmoji = false;
            int emojiIdx = 0;
            for (Map.Entry<String, ResourceLocation> entry : EmojiRegistry.getRegisteredEmojis().entrySet()) {
                if (msg.contains("§e§e§" + (char)('a' + emojiIdx) + "§r    ")) {
                    hasEmoji = true;
                    break;
                }
                emojiIdx++;
            }

            totalChatHeight += hasEmoji ? EMOJI_LINE_HEIGHT : VANILLA_LINE_HEIGHT;
            checkCount++;
            actualLinesToRender++;
        }

        int calculatedTopBounds = absoluteBottomY - totalChatHeight - 12;

        if (isChatOpen) {
            int leftBounds = x - 2;
            int rightBounds = x + 340;
            int bottomBounds = sr.getScaledHeight() - 12;
            int topBounds = (actualLinesToRender > 0) ? calculatedTopBounds : (bottomBounds - 65);

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

            GL11.glColor4f(0.6F, 0.2F, 1.0F, 1.0F);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(leftBounds, topBounds);
            GL11.glVertex2f(leftBounds, topBounds + 2);
            GL11.glVertex2f(rightBounds, topBounds + 2);
            GL11.glVertex2f(rightBounds, topBounds);
            GL11.glEnd();

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }

        int currentY = absoluteBottomY;
        int renderedLinesCount = 0;
        int playerRowCounter = 0;

        for (int i = ChatManager.messages.size() - 1; i >= 0; i--) {
            String msg = ChatManager.messages.get(i);

            long timestamp = ChatManager.timestamps.get(i);
            long age = System.currentTimeMillis() - timestamp;

            if (age > 10000 && !isChatOpen) continue;
            if (renderedLinesCount >= MAX_VISIBLE_LINES) break;

            float alpha = 1.0F;
            if (!isChatOpen && age > 8000) {
                alpha = 1.0F - ((float)(age - 8000) / 2000.0F);
            }

            boolean hasEmoji = false;
            int emojiIdx = 0;
            for (Map.Entry<String, ResourceLocation> entry : EmojiRegistry.getRegisteredEmojis().entrySet()) {
                String invisibleMarker = "§e§e§" + (char)('a' + emojiIdx) + "§r    ";
                if (msg.contains(invisibleMarker)) {
                    hasEmoji = true;
                    break;
                }
                emojiIdx++;
            }

            int lineHeight = hasEmoji ? EMOJI_LINE_HEIGHT : VANILLA_LINE_HEIGHT;
            boolean isSystemEvent = isSystemMessage(msg);

            if (!isSystemEvent && i > 0) {
                isSystemEvent = isSystemMessage(ChatManager.messages.get(i - 1));
            }
            if (!isSystemEvent && i < ChatManager.messages.size() - 1) {
                isSystemEvent = isSystemMessage(ChatManager.messages.get(i + 1));
            }

            if (isChatOpen && !isSystemEvent) {
                if (playerRowCounter % 2 == 0) {
                    int leftBounds = x - 2;
                    int rightBounds = x + 340;

                    GL11.glPushMatrix();
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.102F);
                    GL11.glBegin(GL11.GL_QUADS);
                    GL11.glVertex2f(leftBounds + 4, currentY - lineHeight);
                    GL11.glVertex2f(leftBounds + 4, currentY);
                    GL11.glVertex2f(rightBounds - 4, currentY);
                    GL11.glVertex2f(rightBounds - 4, currentY - lineHeight);
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
            GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);

            // FIX: Alpha color integration setup to prevent font renderer color code resets
            int alphaInt = ((int)(alpha * 255F) << 24) & 0xFF000000;
            mc.fontRenderer.drawStringWithShadow(msg, (int) scaledX, (int) scaledY, 0xFFFFFF | alphaInt);
            GL11.glPopMatrix();

            emojiIdx = 0;
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
                            drawFadeableEmoji(texture, (int)scaledX + pixelOffset, adjustedY, EMOJI_SIZE, alpha);
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

    private boolean isSystemMessage(String msg) {
        if (msg == null || msg.isEmpty()) return true;

        boolean containsChatBrackets = (msg.contains("<") && msg.contains(">")) || msg.contains(":");
        if (containsChatBrackets) {
            return false;
        }

        String cleanText = msg.replaceAll("(?i)§[0-9a-fk-or]", "").trim();

        return cleanText.contains("has just earned the achievement") || cleanText.contains("has made the advancement") ||
                cleanText.contains("was slain by") || cleanText.contains("fell from") ||
                cleanText.contains("blew up") || cleanText.contains("burned") ||
                cleanText.contains("suffocated") || cleanText.contains("drowned") ||
                cleanText.contains("hit the ground") || cleanText.contains("went up in flames") ||
                cleanText.startsWith("[") || cleanText.isEmpty();
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