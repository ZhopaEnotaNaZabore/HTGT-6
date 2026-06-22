package com.mod.htgt6.client.vanillatweaks.CHAT;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class ChatEmojiRenderer {

    private static Field drawnChatLinesField = null;
    private static Field lineStringField = null;

    static {
        String[] drawnChatLinesFields = {"field_146243_q", "drawnChatLines", "field_146242_p", "chatLines"};
        for (String fieldName : drawnChatLinesFields) {
            try {
                drawnChatLinesField = GuiNewChat.class.getDeclaredField(fieldName);
                drawnChatLinesField.setAccessible(true);
                break;
            } catch (NoSuchFieldException ignored) {}
        }

        String[] lineStringFields = {"field_151463_b", "lineString", "field_74542_a"};
        for (String fieldName : lineStringFields) {
            try {
                lineStringField = ChatLine.class.getDeclaredField(fieldName);
                lineStringField.setAccessible(true);
                break;
            } catch (NoSuchFieldException ignored) {}
        }
    }

    /**
     * Intercepts messages and tags them with an invisible identifier unique to its placement index
     * followed by the space gap layout width.
     */
    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        if (event.message == null) return;

        // Get text while preserving any server color/formatting codes
        String formattedText = event.message.getFormattedText();
        boolean modified = false;

        int emojiIdx = 0;
        // Automatically loops over whatever you add to EmojiRegistry.java
        for (String tag : EmojiRegistry.getRegisteredEmojis().keySet()) {
            if (formattedText.contains(tag)) {
                // §e§e is our secret marker prefix, followed by a unique letter identifier, ending in a reset (§r)
                // and 4 spaces ("    ") to create a perfect blank spacing layout width in the chat box
                String invisibleMarker = "§e§e§" + (char)('a' + emojiIdx) + "§r    ";
                formattedText = formattedText.replace(tag, invisibleMarker);
                modified = true;
            }
            emojiIdx++;
        }

        if (modified) {
            event.message = new ChatComponentText(formattedText);
        }
    }

    @SubscribeEvent
    public void onRenderChatOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.CHAT || drawnChatLinesField == null || lineStringField == null) return;

        Minecraft mc = Minecraft.getMinecraft();
        GuiNewChat chatGui = mc.ingameGUI.getChatGUI();
        if (chatGui == null) return;

        try {
            @SuppressWarnings("unchecked")
            List<ChatLine> drawnLines = (List<ChatLine>) drawnChatLinesField.get(chatGui);
            if (drawnLines == null || drawnLines.isEmpty()) return;

            FontRenderer fontRenderer = mc.fontRenderer;
            int visibleLinesCount = chatGui.func_146232_i();
            boolean isChatOpen = chatGui.getChatOpen();

            int screenHeight = event.resolution.getScaledHeight();
            int baseBottomY = isChatOpen ? (screenHeight - 45) : (screenHeight - 40);

            float chatScale = chatGui.func_146244_h();

            GL11.glPushMatrix();
            GL11.glScalef(chatScale, chatScale, 1.0F);

            int currentY = (int)(baseBottomY / chatScale) - 8;

            for (int i = 0; i < drawnLines.size() && i < visibleLinesCount; ++i) {
                ChatLine line = drawnLines.get(i);
                if (line == null) continue;

                int updateCounterDiff = mc.ingameGUI.getUpdateCounter() - line.getUpdatedCounter();
                if (updateCounterDiff >= 200 && !isChatOpen) continue;

                int lineX = (int)(2 / chatScale);

                Object componentObj = lineStringField.get(line);
                String formattedLineText;

                if (componentObj instanceof IChatComponent) {
                    formattedLineText = ((IChatComponent) componentObj).getFormattedText();
                } else if (componentObj instanceof String) {
                    formattedLineText = (String) componentObj;
                } else {
                    continue;
                }

                boolean hasEmoji = false;

                int emojiIdx = 0;
                for (Map.Entry<String, ResourceLocation> entry : EmojiRegistry.getRegisteredEmojis().entrySet()) {

                    String invisibleMarker = "§e§e§" + (char)('a' + emojiIdx) + "§r    ";

                    if (formattedLineText.contains(invisibleMarker)) {
                        hasEmoji = true;
                        break;
                    }

                    emojiIdx++;
                }

                int lineHeight = hasEmoji ? 45 : 9;
                int lineY = currentY;

                emojiIdx = 0;

                for (Map.Entry<String, ResourceLocation> entry : EmojiRegistry.getRegisteredEmojis().entrySet()) {

                    ResourceLocation texture = entry.getValue();
                    String invisibleMarker = "§e§e§" + (char)('a' + emojiIdx) + "§r    ";

                    if (formattedLineText.contains(invisibleMarker)) {

                        int index = 0;

                        while ((index = formattedLineText.indexOf(invisibleMarker, index)) != -1) {

                            String textBeforeEmoji = formattedLineText.substring(0, index);

                            int pixelOffset = fontRenderer.getStringWidth(textBeforeEmoji);

                            if (texture != null) {

                                int emojiSize = 45;

                                // grow upward instead of centered
                                int adjustedY = lineY - (emojiSize - 9);

                                drawInlineEmoji(
                                        texture,
                                        lineX + pixelOffset,
                                        adjustedY,
                                        emojiSize
                                );
                            }

                            index += invisibleMarker.length();
                        }
                    }

                    emojiIdx++;
                }

                currentY -= lineHeight;
            }

            GL11.glPopMatrix();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void drawInlineEmoji(ResourceLocation texture, int x, int y, int size) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

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