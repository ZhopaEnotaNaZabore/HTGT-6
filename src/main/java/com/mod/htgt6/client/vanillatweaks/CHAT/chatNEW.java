package com.mod.htgt6.client.vanillatweaks.CHAT;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.network.NetHandlerPlayClient;

import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class chatNEW extends GuiChat {
    private long lastEmojiSentTime = 0;
    private static final long EMOJI_COOLDOWN_MS = 6000;

    private boolean isEmojiMenuOpen = false;
    private final int EMOJI_BUTTON_ID = 999;

    // Custom non-private buffers to handle arrow-key chat history navigation safely
    private int customHistoryCursor = -1;
    private String customHistoryBuffer = "";

    // Command/Player Suggestion Fields
    private List<String> currentSuggestions = new ArrayList<String>();
    private int selectedSuggestionIndex = 0;

    // Static list of common base commands to suggest
    private static final List<String> BASE_COMMANDS = Arrays.asList(
            "/help", "/gamemode 1", "/gamemode 0", "/tp", "/time", "/weather", "/spawn", "/home", "/msg", "/tell", "/w", "/op", "/deop", "/kick", "/day", "/night"
,"/sun", "/rain", "/ban", "/invsee", "/gm", "/say", "/spawn", "/warp", "/warpset", "//set", "//replace", "//sel", "/wb", "/wb clear", "/wb set",
    "/wb fill", "/wb fill stop", "/wb fill confirm", "/wab fill pause", "/tpa", "/tpyes", "/tpno", "/mute", "/ignore", "/world", "/mem help" );

    public chatNEW() {
        super();
    }

    public chatNEW(String defaultText) {
        super(defaultText);
    }

    @Override
    public void initGui() {
        super.initGui();
        // Clear any vanilla buttons and add an emoji picker toggle button
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(EMOJI_BUTTON_ID, this.width - 60, this.height - 35, 50, 20, "Emoji 😊"));

        // Chat always starts clean and empty now when opened!
        updateCommandSuggestions();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        // Wipe the input text completely so the last typed message is NOT saved
        this.inputField.setText("");

        // Reset the custom history cursor trackers
        this.customHistoryCursor = -1;
        this.customHistoryBuffer = "";
    }




    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        // 1. Handle Emoji menu closing via ESC
        if (keyCode == Keyboard.KEY_ESCAPE && isEmojiMenuOpen) {
            this.isEmojiMenuOpen = false;
            return;
        }

        // 2. Cooldown and Command logic for ENTER key
        if (keyCode == Keyboard.KEY_RETURN) {
            String currentText = this.inputField.getText();

            // Check if it's a message containing one of our emojis
            boolean containsEmoji = false;
            for (String code : EmojiRegistry.getRegisteredEmojis().keySet()) {
                if (currentText.contains(code)) {
                    containsEmoji = true;
                    break;
                }
            }

            if (containsEmoji) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastEmojiSentTime < EMOJI_COOLDOWN_MS) {
                    // STOP: Notify player and return immediately to prevent sending
                    this.mc.thePlayer.addChatMessage(new ChatComponentText("§cНе спамить!"));
                    return;
                }
                lastEmojiSentTime = System.currentTimeMillis();
            }
        }

        // 3. Handle Navigation (Tabs/Arrows) for suggestions
        if (!currentSuggestions.isEmpty()) {
            if (keyCode == Keyboard.KEY_TAB) {
                autocompleteSelected();
                return;
            } else if (keyCode == Keyboard.KEY_UP) {
                selectedSuggestionIndex = (selectedSuggestionIndex <= 0) ? currentSuggestions.size() - 1 : selectedSuggestionIndex - 1;
                return;
            } else if (keyCode == Keyboard.KEY_DOWN) {
                selectedSuggestionIndex = (selectedSuggestionIndex >= currentSuggestions.size() - 1) ? 0 : selectedSuggestionIndex + 1;
                return;
            }
        }

        // 4. Process standard typing inputs only ONCE
        super.keyTyped(typedChar, keyCode);

        // 5. Update UI after input
        updateCommandSuggestions();

        // 6. Handle Chat History navigation if suggestions are inactive
        if (currentSuggestions.isEmpty()) {
            if (keyCode == Keyboard.KEY_UP) {
                this.getSentHistory(-1);
            } else if (keyCode == Keyboard.KEY_DOWN) {
                this.getSentHistory(1);
            }
        }
    }

    /**
     * Scans what the player is typing and generates real-time suggestions matching base commands or online player names
     */
    private void updateCommandSuggestions() {
        currentSuggestions.clear();
        selectedSuggestionIndex = 0;

        String currentText = this.inputField.getText();
        if (currentText.isEmpty()) return;

        String[] words = currentText.split(" ", -1);
        String lastWord = words[words.length - 1];

        if (currentText.startsWith("/") && words.length == 1) {
            // Suggesting base commands
            for (String cmd : BASE_COMMANDS) {
                if (cmd.toLowerCase().startsWith(lastWord.toLowerCase()) && !cmd.equalsIgnoreCase(lastWord)) {
                    currentSuggestions.add(cmd);
                }
            }
        } else if (!lastWord.isEmpty()) {
            // Suggesting online players currently loaded on the server
            NetHandlerPlayClient netHandler = this.mc.thePlayer.sendQueue;
            if (netHandler != null && netHandler.playerInfoList != null) {
                for (Object obj : netHandler.playerInfoList) {
                    if (obj instanceof GuiPlayerInfo) {
                        String name = ((GuiPlayerInfo) obj).name;
                        if (name.toLowerCase().startsWith(lastWord.toLowerCase()) && !name.equalsIgnoreCase(lastWord)) {
                            currentSuggestions.add(name);
                        }
                    }
                }
            }
        }
    }

    /**
     * Completes the highlighted item into your active input bar textbox string
     */
    private void autocompleteSelected() {
        if (currentSuggestions.isEmpty()) return;

        String selected = currentSuggestions.get(selectedSuggestionIndex);
        String currentText = this.inputField.getText();

        int lastSpace = currentText.lastIndexOf(" ");
        if (lastSpace == -1) {
            this.inputField.setText(selected);
        } else {
            this.inputField.setText(currentText.substring(0, lastSpace + 1) + selected);
        }

        updateCommandSuggestions();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // 1. Draw Fancy Custom OpenGL Background instead of vanilla black translucent box
        drawFancyBackground();

        // 2. Allow GuiChat to draw its core text field safely
        super.drawScreen(mouseX, mouseY, partialTicks);

        // 3. Draw Suggestion box if active
        if (!currentSuggestions.isEmpty()) {
            drawSuggestionsPopup();
        }

        // 4. Draw Emoji/Sticker selection panel if toggled open
        if (isEmojiMenuOpen) {
            drawEmojiSelectionMenu(mouseX, mouseY);
        }
    }

    private void drawSuggestionsPopup() {
        int startX = 4;
        int boxBottomY = this.height - 16;
        int rowHeight = 12;
        int maxVisible = Math.min(currentSuggestions.size(), 7);
        int boxTopY = boxBottomY - (maxVisible * rowHeight) - 4;

        // Render sleek popup background container
        this.drawRect(startX, boxTopY, startX + 150, boxBottomY, 0xD0000000);

        for (int i = 0; i < maxVisible; i++) {
            int rowY = boxTopY + 2 + (i * rowHeight);
            String suggestion = currentSuggestions.get(i);

            if (i == selectedSuggestionIndex) {
                this.drawRect(startX + 2, rowY, startX + 148, rowY + rowHeight, 0x60FFFFFF);
                this.fontRendererObj.drawString("> " + suggestion, startX + 6, rowY + 2, 0xFFFF55); // Highlighted active selection
            } else {
                this.fontRendererObj.drawString(suggestion, startX + 6, rowY + 2, 0xAAAAAA); // Unselected list item
            }
        }
    }

    private void drawFancyBackground() {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        // Deep dark, sleek colors for the text input bar at the bottom
        int topColor = 0x800A0A14;    // Semi-transparent deep dark blue
        int bottomColor = 0xE0020205; // Darker near the very bottom edge

        // FIX: Changed 'this.height - 150' to 'this.height - 14' so it doesn't cover up the history log text
        this.drawGradientRect(2, this.height - 14, this.width - 2, this.height - 2, topColor, bottomColor);

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private void drawEmojiSelectionMenu(int mouseX, int mouseY) {
        int menuX = this.width - 200;
        int menuY = this.height - 180;

        // Draw background sheet panel
        this.drawRect(menuX, menuY, this.width - 10, this.height - 40, 0xF005050A);
        this.fontRendererObj.drawString("HiTech6 Stickers", menuX + 6, menuY + 6, 0x43B581); // Discord Green text

        int index = 0;
        for (Map.Entry<String, ResourceLocation> entry : EmojiRegistry.getRegisteredEmojis().entrySet()) {
            int gridX = menuX + 10 + ((index % 6) * 28);
            int gridY = menuY + 22 + ((index / 6) * 28);

            // Draw selection frame block
            this.drawRect(gridX, gridY, gridX + 24, gridY + 24, 0x40FFFFFF);

            // Draw the high resolution Discord icon texture inside the grid box
            ChatEmojiRenderer.drawInlineEmoji(entry.getValue(), gridX + 4, gridY + 4, 16);
            index++;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == EMOJI_BUTTON_ID) {
            this.isEmojiMenuOpen = !this.isEmojiMenuOpen;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Handle clicking directly on options inside the suggestion box popup container
        if (!currentSuggestions.isEmpty() && mouseButton == 0) {
            int startX = 4;
            int boxBottomY = this.height - 16;
            int rowHeight = 12;
            int maxVisible = Math.min(currentSuggestions.size(), 7);
            int boxTopY = boxBottomY - (maxVisible * rowHeight) - 4;

            if (mouseX >= startX && mouseX <= startX + 150 && mouseY >= boxTopY && mouseY <= boxBottomY) {
                int clickedIndex = (mouseY - boxTopY - 2) / rowHeight;
                if (clickedIndex >= 0 && clickedIndex < maxVisible) {
                    this.selectedSuggestionIndex = clickedIndex;
                    autocompleteSelected();
                    return;
                }
            }
        }

        // Handle emoji selections dynamically from the map bounds
        if (isEmojiMenuOpen && mouseButton == 0) {
            int menuX = this.width - 200;
            int menuY = this.height - 180;

            int index = 0;
            for (Map.Entry<String, ResourceLocation> entry : EmojiRegistry.getRegisteredEmojis().entrySet()) {
                int gridX = menuX + 10 + ((index % 6) * 28);
                int gridY = menuY + 22 + ((index / 6) * 28);

                // Check bounding box rules around clicked slots
                if (mouseX >= gridX && mouseX <= gridX + 24 && mouseY >= gridY && mouseY <= gridY + 24) {
                    // Append text code into the textbox
                    this.inputField.writeText(entry.getKey() + " ");
                    this.isEmojiMenuOpen = false; // Auto close panel
                    break;
                }
                index++;
            }
        }
    }

    @Override
    public void getSentHistory(int direction) {
        int historySize = this.mc.ingameGUI.getChatGUI().getSentMessages().size();

        // Initialize custom history index cursor contextually if first time opening
        if (this.customHistoryCursor == -1) {
            this.customHistoryCursor = historySize;
            // Capture what they were typing *right now* as the base buffer before moving away
            this.customHistoryBuffer = this.inputField.getText();
        }

        int newCursor = this.customHistoryCursor + direction;

        if (newCursor < 0) newCursor = 0;
        if (newCursor > historySize) newCursor = historySize;

        if (newCursor != this.customHistoryCursor) {
            if (newCursor == historySize) {
                this.customHistoryCursor = historySize;
                this.inputField.setText(this.customHistoryBuffer);
            } else {
                this.inputField.setText((String) this.mc.ingameGUI.getChatGUI().getSentMessages().get(newCursor));
                this.customHistoryCursor = newCursor;
            }
        }

        // Re-sync suggestions after updating the text field from history cycling
        updateCommandSuggestions();
    }
}