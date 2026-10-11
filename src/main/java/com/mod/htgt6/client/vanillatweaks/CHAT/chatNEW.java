package com.mod.htgt6.client.vanillatweaks.CHAT;

import com.mod.htgt6.client.vanillatweaks.CHAT.ChatRender.ChatFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class chatNEW extends GuiChat {
    private boolean isEmojiMenuOpen = false;
    private final int EMOJI_BUTTON_ID = 999;
    private int customHistoryCursor = -1;
    private String customHistoryBuffer = "";
    /** Per-session command history, kept separately from ordinary chat messages. */
    private static final List<String> COMMAND_HISTORY = new ArrayList<String>();
    private static final int MAX_COMMAND_HISTORY = 100;
    private List<String> currentSuggestions = new ArrayList<String>();
    private int selectedSuggestionIndex = 0;
    private String lastSuggestionRequest = "";

    public chatNEW() {
    }

    public chatNEW(String defaultText) {
        super(defaultText);
    }

    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.inputField.width = this.width - 50;
        this.buttonList.add(new GuiButton(999, this.width - 60, this.height - 35, 50, 20, "Emoji \ud83d\ude0a"));
        this.buttonList.add(new GuiButton(998, this.width - 120, this.height - 35, 55, 20, "Titles \ud83d\udc51"));

        // Replace the legacy large controls with compact input-edge buttons.
        ((GuiButton) this.buttonList.get(0)).visible = false;
        ((GuiButton) this.buttonList.get(1)).visible = false;
        this.buttonList.add(new GuiButton(997, this.width - 46, this.height - 14, 20, 12, ":)"));
        this.buttonList.add(new GuiButton(996, this.width - 24, this.height - 14, 20, 12, "T"));

        // Add Leave Bed button if sleeping
        if (this.mc.thePlayer != null && this.mc.thePlayer.isPlayerSleeping()) {
            this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height - 40, 200, 20, net.minecraft.client.resources.I18n.format("multiplayer.stopSleeping")));
        }

        this.updateCommandSuggestions();
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        if (this.inputField != null) this.inputField.setText("");
        this.customHistoryCursor = -1;
        this.customHistoryBuffer = "";
    }

    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 15) {
            if (!this.currentSuggestions.isEmpty()) {
                this.autocompleteSelected();
            } else {
                this.requestServerSuggestions(true);
            }
            return;
        }

        if (keyCode == 200) {
            getSentHistory(-1);
            return;
        }
        if (keyCode == 208) {
            getSentHistory(1);
            return;
        }

        if (keyCode == 1) {
            this.mc.displayGuiScreen((GuiScreen)null);
        } else if (keyCode != 28 && keyCode != 156) {
            this.inputField.textboxKeyTyped(typedChar, keyCode);
            this.customHistoryCursor = -1;
            this.updateCommandSuggestions();
        } else {
            String text = this.inputField.getText().trim();
            if (!text.isEmpty() && this.mc.thePlayer != null) {
                rememberCommand(text);
                this.mc.thePlayer.sendChatMessage(text);
                this.inputField.setText("");
                this.currentSuggestions.clear();
            }

        }
    }

    private void updateCommandSuggestions() {
        this.currentSuggestions.clear();
        this.selectedSuggestionIndex = 0;
        String currentText = this.inputField.getText();
        if (currentText.isEmpty()) return;

        String[] words = currentText.split(" ", -1);
        String lastWord = words[words.length - 1];
        if (!lastWord.isEmpty() && !currentText.startsWith("/")) {
            addOnlinePlayerSuggestions(lastWord);
        }
        requestServerSuggestions(false);
    }

    /** Uses the actual server command registry through vanilla C14 tab-complete. */
    private void requestServerSuggestions(boolean force) {
        if (this.mc.thePlayer == null || this.mc.thePlayer.sendQueue == null || this.inputField == null) return;
        String text = this.inputField.getText();
        if (text.isEmpty() || (!force && text.equals(this.lastSuggestionRequest))) return;
        this.lastSuggestionRequest = text;
        this.mc.thePlayer.sendQueue.addToSendQueue(new C14PacketTabComplete(text));
    }

    private void addOnlinePlayerSuggestions(String prefix) {
        NetHandlerPlayClient netHandler = this.mc.thePlayer == null ? null : this.mc.thePlayer.sendQueue;
        if (netHandler == null || netHandler.playerInfoList == null) return;
        for (Object obj : netHandler.playerInfoList) {
            if (obj instanceof GuiPlayerInfo) {
                String name = ((GuiPlayerInfo) obj).name;
                if (name.toLowerCase().startsWith(prefix.toLowerCase()) && !name.equalsIgnoreCase(prefix)) {
                    this.currentSuggestions.add(name);
                }
            }
        }
    }

    @Override
    public void func_146406_a(String[] suggestions) {
        this.currentSuggestions.clear();
        this.selectedSuggestionIndex = 0;
        if (suggestions == null) return;
        for (String suggestion : suggestions) {
            if (suggestion != null && !suggestion.isEmpty() && !this.currentSuggestions.contains(suggestion)) {
                this.currentSuggestions.add(suggestion);
            }
        }
    }

    private void autocompleteSelected() {
        if (!this.currentSuggestions.isEmpty()) {
            String selected = (String)this.currentSuggestions.get(this.selectedSuggestionIndex);
            String currentText = this.inputField.getText();
            int lastSpace = currentText.lastIndexOf(" ");
            if (lastSpace == -1) {
                this.inputField.setText(selected);
            } else {
                this.inputField.setText(currentText.substring(0, lastSpace + 1) + selected);
            }

            this.updateCommandSuggestions();
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawFancyBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!this.currentSuggestions.isEmpty()) {
            this.drawSuggestionsPopup();
        }

        if (this.isEmojiMenuOpen) {
            this.drawEmojiSelectionMenu(mouseX, mouseY);
        }

    }

    private void drawSuggestionsPopup() {
        int startX = 4;
        int boxBottomY = this.height - 16;
        int rowHeight = 12;
        int maxVisible = Math.min(this.currentSuggestions.size(), 7);
        int boxTopY = boxBottomY - maxVisible * rowHeight - 4;
        drawRect(startX, boxTopY, startX + 150, boxBottomY, -805306368);

        for(int i = 0; i < maxVisible; ++i) {
            int rowY = boxTopY + 2 + i * rowHeight;
            String suggestion = (String)this.currentSuggestions.get(i);
            if (i == this.selectedSuggestionIndex) {
                drawRect(startX + 2, rowY, startX + 148, rowY + rowHeight, 1627389951);
                this.fontRendererObj.drawString("> " + suggestion, startX + 6, rowY + 2, 16777045);
            } else {
                this.fontRendererObj.drawString(suggestion, startX + 6, rowY + 2, 11184810);
            }
        }

    }

    private void drawFancyBackground() {
        GL11.glPushMatrix();
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glShadeModel(7425);
        int topColor = -2146825708;
        int bottomColor = -536739323;
        this.drawGradientRect(2, this.height - 14, this.width - 2, this.height - 2, topColor, bottomColor);
        GL11.glShadeModel(7424);
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glPopMatrix();
    }

    private void drawEmojiSelectionMenu(int mouseX, int mouseY) {
        int menuX = this.width - 200;
        int menuY = this.height - 180;
        drawRect(menuX, menuY, this.width - 10, this.height - 40, -268106486);
        this.fontRendererObj.drawString("HiTech6 Stickers", menuX + 6, menuY + 6, 4437377);
        int index = 0;

        for(Iterator var6 = EmojiRegistry.getRegisteredEmojis().entrySet().iterator(); var6.hasNext(); ++index) {
            Map.Entry<String, ResourceLocation> entry = (Map.Entry)var6.next();
            int gridX = menuX + 10 + index % 6 * 28;
            int gridY = menuY + 22 + index / 6 * 28;
            drawRect(gridX, gridY, gridX + 24, gridY + 24, 1090519039);
            ChatEmojiRenderer.drawInlineEmoji((ResourceLocation)entry.getValue(), gridX + 4, gridY + 4, 16);
        }

    }

    protected void actionPerformed(GuiButton button) {
        if (button.id == 999 || button.id == 997) {
            this.isEmojiMenuOpen = !this.isEmojiMenuOpen;
        }

        if (button.id == 998 || button.id == 996) {
            this.mc.displayGuiScreen(new GuiTitleSelection(this));
        }

        if (button.id == 1) {
            if (this.mc.thePlayer != null && this.mc.thePlayer.sendQueue != null) {
                this.mc.thePlayer.sendQueue.addToSendQueue(new net.minecraft.network.play.client.C0BPacketEntityAction(this.mc.thePlayer, 3));
            }
        }

    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } catch (Exception var11) {
            var11.printStackTrace();
        }

        if (mouseButton == 0 && mouseY >= ChatRender.tabsTopBounds && mouseY <= ChatRender.tabsBottomBounds) {
            if (mouseX >= ChatRender.tabGlobalLeft && mouseX <= ChatRender.tabGlobalRight) {
                ChatRender.currentFilter = ChatFilter.GLOBAL;
                return;
            }

            if (mouseX >= ChatRender.tabPlayersLeft && mouseX <= ChatRender.tabPlayersRight) {
                ChatRender.currentFilter = ChatFilter.PLAYERS;
                return;
            }

            if (mouseX >= ChatRender.tabSystemLeft && mouseX <= ChatRender.tabSystemRight) {
                ChatRender.currentFilter = ChatFilter.SYSTEM;
                return;
            }
        }

        int menuX;
        int menuY;
        int index;
        int gridX;
        if (!this.currentSuggestions.isEmpty() && mouseButton == 0) {
            menuX = 4;
            menuY = this.height - 16;
            index = 12;
            int maxVisible = Math.min(this.currentSuggestions.size(), 7);
            int boxTopY = menuY - maxVisible * index - 4;
            if (mouseX >= menuX && mouseX <= menuX + 150 && mouseY >= boxTopY && mouseY <= menuY) {
                gridX = (mouseY - boxTopY - 2) / index;
                if (gridX >= 0 && gridX < maxVisible) {
                    this.selectedSuggestionIndex = gridX;
                    this.autocompleteSelected();
                    return;
                }
            }
        }

        if (this.isEmojiMenuOpen && mouseButton == 0) {
            menuX = this.width - 200;
            menuY = this.height - 180;
            index = 0;

            for(Iterator var12 = EmojiRegistry.getRegisteredEmojis().entrySet().iterator(); var12.hasNext(); ++index) {
                Map.Entry<String, ResourceLocation> entry = (Map.Entry)var12.next();
                gridX = menuX + 10 + index % 6 * 28;
                int gridY = menuY + 22 + index / 6 * 28;
                if (mouseX >= gridX && mouseX <= gridX + 24 && mouseY >= gridY && mouseY <= gridY + 24) {
                    this.inputField.writeText((String)entry.getKey() + " ");
                    this.isEmojiMenuOpen = false;
                    break;
                }
            }
        }

    }

    public void getSentHistory(int direction) {
        int historySize = COMMAND_HISTORY.size();
        if (historySize == 0 || this.inputField == null) return;
        if (this.customHistoryCursor == -1) {
            this.customHistoryCursor = historySize;
            this.customHistoryBuffer = this.inputField.getText();
        }

        int newCursor = this.customHistoryCursor + direction;
        if (newCursor < 0) {
            newCursor = 0;
        }

        if (newCursor > historySize) {
            newCursor = historySize;
        }

        if (newCursor != this.customHistoryCursor) {
            if (newCursor == historySize) {
                this.customHistoryCursor = historySize;
                this.inputField.setText(this.customHistoryBuffer);
            } else {
                this.inputField.setText(COMMAND_HISTORY.get(newCursor));
                this.customHistoryCursor = newCursor;
            }
        }

        this.updateCommandSuggestions();
    }

    private static void rememberCommand(String text) {
        if (!text.startsWith("/")) return;
        if (!COMMAND_HISTORY.isEmpty() && text.equals(COMMAND_HISTORY.get(COMMAND_HISTORY.size() - 1))) return;
        COMMAND_HISTORY.add(text);
        if (COMMAND_HISTORY.size() > MAX_COMMAND_HISTORY) COMMAND_HISTORY.remove(0);
    }
}
