package com.mod.htgt6.client.vanillatweaks.CHAT;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.manager.GuiManager;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.mod.htgt6.HTGT6;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import net.minecraftforge.client.ClientCommandHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Client-only ModularUI implementation of the HiTech6 chat terminal. */
@SideOnly(Side.CLIENT)
public final class ChatModularScreen extends ModularScreen {
    public static final String OWNER = "htgt6";
    public static final String NAME = "chat";

    private final Controller controller;

    private ChatModularScreen(Bundle bundle) {
        super(OWNER, bundle.panel);
        this.controller = bundle.controller;
        this.controller.screen = this;
    }

    public static void open(String initialText) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;
        GuiManager.openClientUI(mc.thePlayer, new ChatModularScreen(Bundle.create(initialText)));
    }

    public static boolean isOpen() {
        return ModularScreen.isActive(OWNER, NAME);
    }

    static void clearHistory() { Controller.COMMAND_HISTORY.clear(); }

    public static void acceptServerSuggestions(String[] suggestions) {
        ModularScreen current = ModularScreen.getCurrent();
        if (current instanceof ChatModularScreen) {
            ((ChatModularScreen) current).controller.acceptServerSuggestions(suggestions);
        }
    }

    @Override
    public void onResize(int width, int height) {
        if (controller != null) controller.resize(width, height);
        super.onResize(width, height);
    }

    @Override
    public void onOpen() {
        super.onOpen();
        Keyboard.enableRepeatEvents(true);
        this.context.focus(this.controller.input);
        this.controller.updateSuggestions(true);
    }

    @Override
    public void onClose() {
        Keyboard.enableRepeatEvents(false);
        super.onClose();
    }

    @Override
    public void onFrameUpdate() {
        super.onFrameUpdate();
        this.controller.onFrameUpdate();
    }

    @Override
    public boolean onKeyPressed(char typedChar, int keyCode) {
        if (controller.enlargedImage != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) controller.enlargedImage = null;
            return true;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (controller.popupMode != PopupMode.NONE) { controller.popupMode = PopupMode.NONE; controller.requestRefocus(); return true; }
            close();
            return true;
        }
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            this.controller.sendMessage();
            return true;
        }
        if (keyCode == Keyboard.KEY_TAB) {
            this.controller.completeSuggestion();
            return true;
        }
        if (keyCode == Keyboard.KEY_UP) {
            if (!controller.suggestions.isEmpty()) controller.selectedSuggestion = Math.max(0, controller.selectedSuggestion - 1);
            else this.controller.moveHistory(-1);
            return true;
        }
        if (keyCode == Keyboard.KEY_DOWN) {
            if (!controller.suggestions.isEmpty()) controller.selectedSuggestion = Math.min(controller.suggestionCount() - 1, controller.selectedSuggestion + 1);
            else this.controller.moveHistory(1);
            return true;
        }
        if (keyCode == Keyboard.KEY_PRIOR || keyCode == Keyboard.KEY_NEXT) {
            controller.scrollMessages(keyCode == Keyboard.KEY_PRIOR ? 5 : -5);
            return true;
        }
        return super.onKeyPressed(typedChar, keyCode);
    }

    @Override
    public boolean onMousePressed(int mouseButton) {
        if (this.controller.handleOverlayClick(this.context.getAbsMouseX(), this.context.getAbsMouseY(), mouseButton)) {
            return true;
        }
        return super.onMousePressed(mouseButton);
    }

    @Override
    public boolean onMouseScroll(UpOrDown direction, int amount) {
        if (controller.enlargedImage != null) return true;
        int mouseX = this.context.getAbsMouseX();
        int mouseY = this.context.getAbsMouseY();
        if (this.controller.insidePopup(mouseX, mouseY)) {
            if (controller.popupMode == PopupMode.TITLES) controller.scrollTitles(direction.isUp() ? -1 : 1);
            else if (controller.popupMode == PopupMode.EMOJI) controller.scrollEmojis(direction.isUp() ? -1 : 1);
            return true;
        }
        if (this.controller.isInsideMessages(mouseX, mouseY)) {
            this.controller.scrollMessages(direction.isUp() ? 1 : -1);
            return true;
        }
        return super.onMouseScroll(direction, amount);
    }

    private static final class Bundle {
        private final ModularPanel panel;
        private final Controller controller;

        private Bundle(ModularPanel panel, Controller controller) {
            this.panel = panel;
            this.controller = controller;
        }

        private static Bundle create(String initialText) {
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            int width = resolution.getScaledWidth();
            int height = resolution.getScaledHeight();
            Controller controller = new Controller(width, height, initialText == null ? "" : initialText);
            ModularPanel panel = ModularPanel.defaultPanel(NAME, width, height)
                    .background(new ChatRectangle().setColor(0x00000000));
            controller.build(panel);
            return new Bundle(panel, controller);
        }
    }

    /** Rounded horizontal strips avoid the triangle-fan/quad mismatch in MUI 2.0.8. */
    private static final class ChatRectangle extends Rectangle {
        private int radius;
        @Override public ChatRectangle setCornerRadius(int radius) {
            this.radius = Math.max(0, radius);
            return this;
        }
        @Override public void draw(GuiContext context, int x, int y, int w, int h) {
            if (w <= 0 || h <= 0) return;
            int r = Math.min(radius, Math.min(w, h) / 2);
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
            try {
                if (r == 0) {
                    Gui.drawRect(x, y, x + w, y + h, getColor());
                    return;
                }
                Gui.drawRect(x, y + r, x + w, y + h - r, getColor());
                for (int row = 0; row < r; row++) {
                    double dy = r - row - 0.5;
                    int inset = (int) Math.ceil(r - Math.sqrt(r * r - dy * dy));
                    Gui.drawRect(x + inset, y + row, x + w - inset, y + row + 1, getColor());
                    Gui.drawRect(x + inset, y + h - row - 1, x + w - inset, y + h - row, getColor());
                }
            } finally { GL11.glPopAttrib(); }
        }
    }

    private static void softBox(GuiContext context, int x, int y, int width, int height, int fill, int border, int radius) {
        new ChatRectangle().setColor(border).setCornerRadius(radius).draw(context, x, y, width, height);
        new ChatRectangle().setColor(fill).setCornerRadius(Math.max(1, radius - 1)).draw(context, x + 1, y + 1, width - 2, height - 2);
    }

    private static final class ChatTheme {
        static final int PANEL = 0xD51A2329, POPUP = 0xF0212B31, BORDER = 0x506D858C;
        static final int TEXT = 0xFFE4EAE9, MUTED = 0xFF9CAFB3, ACCENT = 0xFF8BD3D5, HOVER = 0x80416F75;
        private static final ChatTheme INSTANCE = new ChatTheme();
        static ChatTheme getActive() { return INSTANCE; }
        int getPanelColor() { return PANEL; }
        int getTerminalColor() { return 0x40121C22; }
        int getFrameColor() { return HOVER; }
        int getTitleColor() { return TEXT; }
        int getProductionColor() { return ACCENT; }
        int getPrimaryColor() { return TEXT; }
        int getLabelColor() { return MUTED; }
    }

    private enum PopupMode { NONE, EMOJI, TITLES }

    private static final class Controller {
        private static final List<String> COMMAND_HISTORY = new ArrayList<String>();
        private static final int MAX_COMMAND_HISTORY = 100;

        private final Minecraft mc = Minecraft.getMinecraft();
        private int width;
        private int height;
        private int messageX;
        private int messageY;
        private int messageWidth;
        private int messageHeight;
        private int actionButtonX;
        private int inputY, panelTop, panelBottom;
        private ChatMessagesWidget messageWidget;
        private final List<ButtonWidget<?>> tabButtons = new ArrayList<ButtonWidget<?>>();
        private ButtonWidget<?> emojiButton, titleButton, imageButton, wakeButton;
        private String enlargedImage;
        private Widget<?> backdrop, overlay;
        private final SafeChatTextField input = new SafeChatTextField();
        private final List<String> suggestions = new ArrayList<String>();
        private final List<TitleManager.Title> unlockedTitles = new ArrayList<TitleManager.Title>();
        private final List<TitleManager.Title> shopTitles = new ArrayList<TitleManager.Title>();
        private ChatModularScreen screen;
        private PopupMode popupMode = PopupMode.NONE;
        private boolean shopTab;
        private int messageScroll;
        private int emojiScroll;
        private int titleScroll;
        private int historyCursor = -1;
        private String historyBuffer = "";
        private String lastObservedText = "";
        private String lastSuggestionRequest = "";
        private int selectedSuggestion;
        private boolean refocus;

        private Controller(int width, int height, String initialText) {
            layout(width, height);
            input.setMaxLength(ChatImageLinks.MAX_URL);
            input.setText(initialText);
            input.setTextColor(ChatTheme.TEXT);
            lastObservedText = initialText;
            collectTitles();
        }

        private void layout(int width, int height) {
            this.width = width; this.height = height;
            messageX = 8;
            messageWidth = Math.max(1, Math.min(560, width - 16));
            panelBottom = Math.max(100, height - 44);
            int panelHeight = Math.min(224, panelBottom - 8);
            panelTop = panelBottom - panelHeight;
            inputY = panelBottom - 28;
            messageY = panelTop + 30;
            messageHeight = Math.max(28, inputY - messageY - 6);
            actionButtonX = messageX + messageWidth - 90;
        }

        private void resize(int width, int height) {
            layout(width, height);
            if (messageWidget == null) return;
            screen.getMainPanel().pos(0, 0).size(width, height);
            backdrop.size(width, height);
            overlay.size(width, height);
            messageWidget.pos(messageX + 6, messageY).size(messageWidth - 12, messageHeight);
            input.pos(messageX + 13, inputY + 7).size(actionButtonX - messageX - 25, 12);
            for (int i = 0; i < tabButtons.size(); i++) tabButtons.get(i).pos(messageX + 8 + i * 65, panelTop + 7);
            emojiButton.pos(actionButtonX, inputY);
            titleButton.pos(actionButtonX + 28, inputY);
            imageButton.pos(actionButtonX + 56, inputY);
            if (wakeButton != null) wakeButton.pos(Math.max(4, width / 2 - 55), 4);
            messageWidget.invalidateLayout();
        }

        private void build(ModularPanel panel) {
            backdrop = new Widget() {
                @Override public boolean canHover() { return false; }
                @Override public void draw(GuiContext context) {
                    softBox(context, messageX, panelTop, messageWidth, panelBottom - panelTop,
                            ChatTheme.PANEL, ChatTheme.BORDER, 6);
                    Gui.drawRect(messageX + 9, messageY - 2, messageX + messageWidth - 9, messageY - 1, 0x185C929A);
                    softBox(context, messageX + 8, inputY, actionButtonX - messageX - 12, 22,
                            0x6610171B, input.isFocused() ? 0x806BABB1 : ChatTheme.BORDER, 4);
                    if (input.getText().isEmpty()) context.font.drawString("Написать сообщение...",
                            messageX + 13, inputY + 7, ChatTheme.MUTED);
                }
            };
            panel.child(backdrop.pos(0, 0).size(width, height));
            messageWidget = new ChatMessagesWidget(this);
            panel.child(messageWidget);
            panel.child(createTabButton("Все", ChatRender.ChatFilter.GLOBAL, 0, 0, 61));
            panel.child(createTabButton("Игроки", ChatRender.ChatFilter.PLAYERS, 0, 0, 61));
            panel.child(createTabButton("Система", ChatRender.ChatFilter.SYSTEM, 0, 0, 61));
            input.background(new ChatRectangle().setColor(0x00000000));
            panel.child(input);
            emojiButton = createActionButton(":)", PopupMode.EMOJI, "Стикеры", 0, 0, 24);
            titleButton = createActionButton("*", PopupMode.TITLES, "Титулы и достижения", 0, 0, 24);
            panel.child(emojiButton); panel.child(titleButton);
            imageButton = new ButtonWidget();
            imageButton.size(24, 22).background(new ChatRectangle().setColor(0x403C4C52).setCornerRadius(4));
            imageButton.overlay(IKey.str("IMG"));
            imageButton.tooltip(t -> t.addLine("Вставить ссылку PNG/JPG из буфера; Enter — отправить"));
            imageButton.onMouseTapped(mouse -> {
                String url = net.minecraft.client.gui.GuiScreen.getClipboardString().trim();
                if (ChatImageLinks.valid(url)) input.setText(url);
                else ChatManager.addMessage("Скопируйте прямую http(s)-ссылку на PNG/JPG и нажмите IMG.", true);
                requestRefocus(); return true;
            });
            panel.child(imageButton);
            if (mc.thePlayer != null && mc.thePlayer.isPlayerSleeping()) {
                wakeButton = new ButtonWidget();
                wakeButton.size(110, 20).background(new ChatRectangle().setColor(ChatTheme.PANEL).setCornerRadius(4));
                wakeButton.overlay(IKey.str("Встать с кровати"));
                wakeButton.onMouseTapped(mouse -> { leaveBed(); return true; });
                panel.child(wakeButton);
            }
            overlay = new ChatOverlayWidget(this);
            panel.child(overlay.pos(0, 0).size(width, height));
            // Initial placement does not require a screen context.
            messageWidget.pos(messageX + 6, messageY).size(messageWidth - 12, messageHeight);
            input.pos(messageX + 13, inputY + 7).size(actionButtonX - messageX - 25, 12);
            for (int i = 0; i < tabButtons.size(); i++) tabButtons.get(i).pos(messageX + 8 + i * 65, panelTop + 7);
            emojiButton.pos(actionButtonX, inputY); titleButton.pos(actionButtonX + 28, inputY);
            imageButton.pos(actionButtonX + 56, inputY);
            if (wakeButton != null) wakeButton.pos(Math.max(4, width / 2 - 55), 4);
        }

        private ButtonWidget<?> createTabButton(String label, final ChatRender.ChatFilter filter,
                                                 int x, int y, int buttonWidth) {
            ButtonWidget<?> button = new ButtonWidget();
            button.pos(x, y).size(buttonWidth, 18);
            button.overlay(IKey.str(label));
            button.hoverBackground(new ChatRectangle().setColor(ChatTheme.HOVER).setCornerRadius(4));
            button.onMouseTapped(mouse -> {
                ChatRender.currentFilter = filter; messageScroll = 0;
                refreshTabButtons(); requestRefocus(); return true;
            });
            tabButtons.add(button);
            refreshTabButtons();
            return button;
        }

        private ButtonWidget<?> createActionButton(String label, final PopupMode mode, String tooltip,
                                                    int x, int y, int buttonWidth) {
            ButtonWidget<?> button = new ButtonWidget();
            button.pos(x, y).size(buttonWidth, 22);
            button.background(new ChatRectangle().setColor(0x403C4C52).setCornerRadius(4));
            button.hoverBackground(new ChatRectangle().setColor(ChatTheme.HOVER).setCornerRadius(4));
            button.overlay(IKey.str(label));
            button.tooltip(t -> t.addLine(tooltip));
            button.onMouseTapped(mouse -> {
                popupMode = popupMode == mode ? PopupMode.NONE : mode;
                if (mode == PopupMode.TITLES && popupMode == PopupMode.TITLES) {
                    HTGT6.network.sendToServer(new PacketRequestChangeTitle("__sync"));
                    collectTitles();
                }
                requestRefocus(); return true;
            });
            return button;
        }

        private void refreshTabButtons() {
            for (int i = 0; i < tabButtons.size(); i++) {
                tabButtons.get(i).background(new ChatRectangle().setColor(
                        ChatRender.currentFilter.ordinal() == i ? 0x99507E83 : 0x203C4C52).setCornerRadius(4));
            }
        }

        private void onFrameUpdate() {
            if (popupMode == PopupMode.TITLES) {
                collectTitles();
                scrollTitles(0);
            }
            String text = input.getText();
            if (text == null) text = "";
            if (!text.equals(lastObservedText)) {
                lastObservedText = text;
                historyCursor = -1;
                updateSuggestions(false);
            }
            if (refocus && screen != null) {
                screen.context.focus(input);
                refocus = false;
            }
        }

        private void requestRefocus() {
            this.refocus = true;
        }

        private void sendMessage() {
            String text = input.getText() == null ? "" : input.getText().trim();
            if (!text.isEmpty() && mc.thePlayer != null) {
                rememberCommand(text);
                if (text.length() > 100 && !text.startsWith("/")) {
                    if (!PacketChatImage.validText(text)) {
                        ChatManager.addMessage("Сообщение содержит недопустимые символы или превышает 2048 символов.", true);
                        return;
                    }
                    HTGT6.network.sendToServer(new PacketChatImage(text));
                }
                else {
                    if (ClientCommandHandler.instance.executeCommand(mc.thePlayer, text) != 0) {
                        // The client command handled the complete input locally.
                    } else if (text.length() > 100) {
                        ChatManager.addMessage("Серверная команда не должна превышать 100 символов.", true);
                        return;
                    } else mc.thePlayer.sendChatMessage(text);
                }
                input.setText("");
                lastObservedText = "";
                suggestions.clear();
                selectedSuggestion = 0;
                historyCursor = -1;
            }
            requestRefocus();
        }

        private void moveHistory(int direction) {
            int size = COMMAND_HISTORY.size();
            if (size == 0) return;
            if (historyCursor == -1) {
                historyCursor = size;
                historyBuffer = input.getText();
            }
            int next = Math.max(0, Math.min(size, historyCursor + direction));
            if (next == historyCursor) return;
            historyCursor = next;
            input.setText(next == size ? historyBuffer : COMMAND_HISTORY.get(next));
            lastObservedText = input.getText();
            updateSuggestions(false);
            requestRefocus();
        }

        private static void rememberCommand(String text) {
            if (!COMMAND_HISTORY.isEmpty() && text.equals(COMMAND_HISTORY.get(COMMAND_HISTORY.size() - 1))) return;
            COMMAND_HISTORY.add(text);
            if (COMMAND_HISTORY.size() > MAX_COMMAND_HISTORY) COMMAND_HISTORY.remove(0);
        }

        private void updateSuggestions(boolean force) {
            suggestions.clear();
            selectedSuggestion = 0;
            String text = input.getText();
            if (text == null || text.isEmpty()) return;
            String[] words = text.split(" ", -1);
            String lastWord = words[words.length - 1];
            if (!lastWord.isEmpty() && !text.startsWith("/")) addOnlinePlayerSuggestions(lastWord);
            // Vanilla server completion is intended for commands. Sending
            // sticker codes such as :Hi: makes some servers answer with an
            // arbitrary player name, which then appears as a command popup.
            if (text.startsWith("/")) requestServerSuggestions(force);
        }

        private void addOnlinePlayerSuggestions(String prefix) {
            NetHandlerPlayClient netHandler = mc.thePlayer == null ? null : mc.thePlayer.sendQueue;
            if (netHandler == null || netHandler.playerInfoList == null) return;
            for (Object value : netHandler.playerInfoList) {
                if (!(value instanceof GuiPlayerInfo)) continue;
                String name = ((GuiPlayerInfo) value).name;
                if (name.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))
                        && !name.equalsIgnoreCase(prefix) && !suggestions.contains(name)) {
                    suggestions.add(name);
                }
            }
        }

        private void requestServerSuggestions(boolean force) {
            if (mc.thePlayer == null || mc.thePlayer.sendQueue == null) return;
            String text = input.getText();
            if (text == null || !text.startsWith("/") || (!force && text.equals(lastSuggestionRequest))) return;
            lastSuggestionRequest = text;
            mc.thePlayer.sendQueue.addToSendQueue(new C14PacketTabComplete(text));
        }

        private void acceptServerSuggestions(String[] values) {
            String currentText = input.getText();
            // S3A has no request id. Ignore replies that arrived after the
            // player changed the input or inserted an emoji.
            if (currentText == null || !currentText.startsWith("/")
                    || !currentText.equals(lastSuggestionRequest)) {
                return;
            }
            suggestions.clear();
            selectedSuggestion = 0;
            if (values == null) return;
            for (String value : values) {
                if (value != null && !value.isEmpty() && !suggestions.contains(value)) suggestions.add(value);
            }
        }

        private void completeSuggestion() {
            if (suggestions.isEmpty()) {
                requestServerSuggestions(true);
                return;
            }
            String selected = suggestions.get(Math.max(0, Math.min(selectedSuggestion, suggestions.size() - 1)));
            String text = input.getText();
            int lastSpace = text.lastIndexOf(' ');
            input.setText(lastSpace < 0 ? selected : text.substring(0, lastSpace + 1) + selected);
            lastObservedText = input.getText();
            updateSuggestions(false);
            requestRefocus();
        }

        private void scrollMessages(int direction) {
            messageScroll = Math.max(0, Math.min(messageWidget.maxScroll(), messageScroll + direction * 28));
        }

        private int maxEmojiScroll() { return Math.max(0, (EmojiRegistry.getRegisteredEmojis().size() + 3) / 4 - 2); }

        private void scrollEmojis(int direction) {
            emojiScroll = Math.max(0, Math.min(maxEmojiScroll(), emojiScroll + direction));
        }

        private void scrollTitles(int direction) {
            List<TitleManager.Title> list = shopTab ? shopTitles : unlockedTitles;
            titleScroll = Math.max(0, Math.min(Math.max(0, list.size() - titleVisibleRows()), titleScroll + direction));
        }

        private boolean isInsideMessages(int x, int y) {
            return x >= messageX && x < messageX + messageWidth && y >= messageY && y < messageY + messageHeight;
        }

        private int popupX() {
            int beside = messageX + messageWidth + 6;
            return beside + 228 <= width - 8 ? beside : Math.max(4, messageX + messageWidth - 228);
        }
        private int popupY() { return Math.max(4, Math.min(height - popupHeight() - 4, inputY - popupHeight() - 4)); }
        private int popupHeight() { return popupMode == PopupMode.EMOJI ? 136 : Math.min(188, Math.max(100, inputY - 8)); }
        private int titleVisibleRows() { return Math.max(1, (popupHeight() - 71) / 17); }
        private boolean insidePopup(int x, int y) {
            return popupMode != PopupMode.NONE && x >= popupX() && x < popupX() + 228
                    && y >= popupY() && y < popupY() + popupHeight();
        }
        private int suggestionBottom() { return inputY - 4; }
        private int suggestionCount() { return Math.min(Math.min(7, suggestions.size()), Math.max(0, (suggestionBottom() - 8) / 15)); }


        private boolean handleOverlayClick(int mouseX, int mouseY, int mouseButton) {
            if (enlargedImage != null) { enlargedImage = null; return true; }
            if (mouseButton != 0) return false;
            if (popupMode == PopupMode.NONE && messageWidget.openImage(mouseX, mouseY)) return true;
            if (popupMode == PopupMode.EMOJI && handleEmojiClick(mouseX, mouseY)) return true;
            if (popupMode == PopupMode.TITLES && handleTitleClick(mouseX, mouseY)) return true;
            if (popupMode == PopupMode.NONE && handleSuggestionClick(mouseX, mouseY)) return true;
            if (popupMode != PopupMode.NONE && mouseY < inputY) { popupMode = PopupMode.NONE; requestRefocus(); }
            return false;
        }

        private boolean handleSuggestionClick(int mouseX, int mouseY) {
            int visible = suggestionCount();
            if (visible == 0) return false;
            int left = messageX + 8, bottom = suggestionBottom(), top = bottom - visible * 15 - 6;
            int boxWidth = Math.min(220, messageWidth - 16);
            if (mouseX < left || mouseX >= left + boxWidth || mouseY < top || mouseY >= bottom) return false;
            int row = (mouseY - top - 3) / 15;
            if (mouseY >= top + 3 && row >= 0 && row < visible) { selectedSuggestion = row; completeSuggestion(); }
            return true;
        }

        private boolean handleEmojiClick(int mouseX, int mouseY) {
            if (!insidePopup(mouseX, mouseY)) return false;
            scrollEmojis(0);
            if (mouseX >= popupX() + 218 && mouseX < popupX() + 227
                    && mouseY >= popupY() + 25 && mouseY < popupY() + 117) {
                int maximum = maxEmojiScroll();
                emojiScroll = Math.max(0, Math.min(maximum,
                        Math.round((mouseY - popupY() - 25) * maximum / 91f)));
                return true;
            }
            int index = 0;
            for (Map.Entry<String, ResourceLocation> entry : EmojiRegistry.getRegisteredEmojis().entrySet()) {
                int slot = index++ - emojiScroll * 4;
                if (slot < 0 || slot >= 8) continue;
                int cellX = popupX() + 10 + slot % 4 * 52;
                int cellY = popupY() + 25 + slot / 4 * 46;
                if (mouseX >= cellX && mouseX < cellX + 48 && mouseY >= cellY && mouseY < cellY + 44) {
                    input.insert(entry.getKey() + " ");
                    lastObservedText = input.getText(); popupMode = PopupMode.NONE;
                    updateSuggestions(false); requestRefocus(); return true;
                }
            }
            return true;
        }

        private boolean handleTitleClick(int mouseX, int mouseY) {
            int x = popupX();
            int y = popupY();
            if (!insidePopup(mouseX, mouseY)) return false;
            if (mouseY >= y + 22 && mouseY < y + 42) {
                shopTab = mouseX >= x + 114;
                titleScroll = 0;
                return true;
            }
            if (mouseY >= y + 44 && mouseY < y + 62 && !shopTab) {
                HTGT6.network.sendToServer(new PacketRequestChangeTitle("none"));
                popupMode = PopupMode.NONE;
                requestRefocus();
                return true;
            }
            int rowsTop = y + 65;
            int row = (mouseY - rowsTop) / 17;
            if (mouseY >= rowsTop && row >= 0 && row < titleVisibleRows()) {
                List<TitleManager.Title> list = shopTab ? shopTitles : unlockedTitles;
                int index = titleScroll + row;
                if (index < list.size()) {
                    TitleManager.Title title = list.get(index);
                    HTGT6.network.sendToServer(new PacketRequestChangeTitle(shopTab ? "buy:" + title.id : title.id));
                    popupMode = PopupMode.NONE;
                    requestRefocus();
                }
            }
            return true;
        }

        private void collectTitles() {
            unlockedTitles.clear(); shopTitles.clear();
            for (TitleManager.Title title : TitleManager.TITLE_DATABASE.values()) {
                if (title.coinPrice > 0) {
                    shopTitles.add(title);
                    if (TitleManager.clientUnlockedTitles.contains(title.id)) unlockedTitles.add(title);
                    continue;
                }
                if (title.isOpOnly && !TitleManager.clientUnlockedTitles.contains(title.id)) continue;
                if (title.achievementId == null) {
                    unlockedTitles.add(title);
                    continue;
                }
                StatBase stat = StatList.func_151177_a(title.achievementId);
                if (stat instanceof Achievement && mc.thePlayer != null
                        && mc.thePlayer.getStatFileWriter().hasAchievementUnlocked((Achievement) stat)) {
                    unlockedTitles.add(title);
                }
            }
        }

        private void leaveBed() {
            if (mc.thePlayer != null && mc.thePlayer.sendQueue != null && mc.thePlayer.isPlayerSleeping()) {
                mc.thePlayer.sendQueue.addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, 3));
            }
        }

    }

    /**
     * ModularUI 2.0.8 does not clamp its cursor when TextFieldWidget#setText
     * replaces a longer value with a shorter one.  Chat changes its text from
     * history, completion, emoji insertion and send/clear operations, so every
     * programmatic replacement must also collapse the cursor to the new end.
     */
    private static final class SafeChatTextField extends TextFieldWidget {
        private SafeChatTextField() {
            this.renderer = new ChatInputRenderer(this.handler);
            this.handler.setRenderer(this.renderer);
        }
        void insert(String value) {
            String text = getText();
            int a = Math.max(0, Math.min(text.length(), handler.getMainCursor().x));
            int b = Math.max(0, Math.min(text.length(), handler.getOffsetCursor().x));
            int start = Math.min(a, b), end = Math.max(a, b);
            int available = Math.max(0, 256 - (text.length() - end + start));
            value = value.substring(0, Math.min(available, value.length()));
            setText(text.substring(0, start) + value + text.substring(end));
            handler.setCursor(0, start + value.length(), false);
        }
        @Override
        public void setText(String text) {
            String safeText = text == null ? "" : text;
            super.setText(safeText);
            // TextFieldHandler uses (line, column), while java.awt.Point stores
            // those coordinates as (column, line).  Set both selection ends.
            this.handler.setCursor(0, safeText.length(), false);
        }

        @Override
        public Interactable.Result onKeyPressed(char typedChar, int keyCode) {
            String text = getText();
            java.awt.Point main = this.handler.getMainCursor();
            java.awt.Point offset = this.handler.getOffsetCursor();
            int length = text == null ? 0 : text.length();
            if (main.y != 0 || offset.y != 0 || main.x < 0 || main.x > length
                    || offset.x < 0 || offset.x > length) {
                this.handler.setCursor(0, Math.max(0, Math.min(length, main.x)), false);
            }
            return super.onKeyPressed(typedChar, keyCode);
        }
    }

    private static final class ChatMessagesWidget extends Widget<ChatMessagesWidget> {
        private static final RenderItem ITEM_RENDERER = new RenderItem();
        private static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm");
        private final Controller controller;
        private final List<String> markers = new ArrayList<String>();
        private final List<ResourceLocation> textures = new ArrayList<ResourceLocation>();
        private final Map<String, ChatTextLayout> cache = new LinkedHashMap<String, ChatTextLayout>();
        private final List<ImageHit> imageHits = new ArrayList<ImageHit>();
        private static final int IMAGE_HEIGHT = 100;
        private static final class ImageHit {
            final String url; final int x, y, w, h;
            ImageHit(String url, int x, int y, int w, int h) { this.url=url; this.x=x; this.y=y; this.w=w; this.h=h; }
        }
        boolean openImage(int x, int y) {
            for (ImageHit hit : imageHits) if (x >= hit.x && x < hit.x+hit.w && y >= hit.y && y < hit.y+hit.h) {
                controller.enlargedImage = hit.url; return true;
            }
            return false;
        }
        private int cachedWidth;
        private ChatManager.ChatEntry newest;
        private ChatMessagesWidget(Controller controller) {
            this.controller = controller;
            int index = 0;
            for (ResourceLocation texture : EmojiRegistry.getRegisteredEmojis().values()) {
                markers.add(EmojiRegistry.marker(index++)); textures.add(texture);
            }
        }
        void invalidateLayout() { cache.clear(); }
        private int gutter() { return getArea().w() >= 280 ? 37 : 0; }
        private ChatTextLayout layout(ChatManager.TextEntry entry) {
            int width = Math.max(40, getArea().w() - gutter() - 18);
            if (cachedWidth != width) { cache.clear(); cachedWidth = width; }
            String animated = TitleVisuals.animate(entry.text, controller.mc.ingameGUI.getUpdateCounter());
            ChatTextLayout result = cache.get(animated);
            if (result == null) {
                result = ChatTextLayout.create(animated, width, markers, controller.mc.fontRenderer::getStringWidth);
                if (cache.size() >= 200) cache.clear();
                cache.put(animated, result);
            }
            return result;
        }
        private int entryHeight(ChatManager.ChatEntry entry) {
            return entry instanceof ChatManager.AchievementEntry ? 42 : layout((ChatManager.TextEntry) entry).height + 4 + ((ChatManager.TextEntry) entry).images.size() * (IMAGE_HEIGHT + 6);
        }
        private int totalHeight(List<ChatManager.ChatEntry> entries) {
            int result = 0;
            for (ChatManager.ChatEntry entry : entries) result += entryHeight(entry);
            return result;
        }
        int maxScroll() { return Math.max(0, totalHeight(ChatRender.getFilteredEntriesSnapshot()) - getArea().h() + 4); }

        @Override public void draw(GuiContext context) {
            imageHits.clear();
            List<ChatManager.ChatEntry> entries = ChatRender.getFilteredEntriesSnapshot();
            if (controller.messageScroll > 0 && newest != null) {
                int at = entries.indexOf(newest);
                if (at >= 0) for (int i = at + 1; i < entries.size(); i++) controller.messageScroll += entryHeight(entries.get(i));
            }
            newest = entries.isEmpty() ? null : entries.get(entries.size() - 1);
            int total = totalHeight(entries), viewport = getArea().h() - 4;
            int maximum = Math.max(0, total - viewport);
            controller.messageScroll = Math.min(controller.messageScroll, maximum);
            int bottom = getArea().h() - 2 + controller.messageScroll;
            // Clip partially visible wrapped messages without changing their measured height.
            ScaledResolution sr = new ScaledResolution(controller.mc, controller.mc.displayWidth, controller.mc.displayHeight);
            int scale = sr.getScaleFactor();
            GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            int left = context.transformX(0, 0), top = context.transformY(0, 0);
            int right = context.transformX(getArea().w(), getArea().h());
            int lower = context.transformY(getArea().w(), getArea().h());
            GL11.glScissor(left * scale, controller.mc.displayHeight - lower * scale,
                    Math.max(0, right - left) * scale, Math.max(0, lower - top) * scale);
            try {
                for (int i = entries.size() - 1; i >= 0 && bottom > 0; i--) {
                    ChatManager.ChatEntry entry = entries.get(i);
                    int h = entryHeight(entry), y = bottom - h;
                    if (y < getArea().h()) {
                        if (entry instanceof ChatManager.AchievementEntry) drawAchievementCard((ChatManager.AchievementEntry) entry, y, context);
                        else drawMessage((ChatManager.TextEntry) entry, y, context);
                    }
                    bottom = y;
                }
                if (entries.isEmpty()) context.font.drawString("Здесь пока нет сообщений", 8, getArea().h() - 19, ChatTheme.MUTED);
            } finally { GL11.glPopAttrib(); }
            if (maximum > 0) {
                int track = getArea().h() - 6;
                int thumb = Math.max(12, track * viewport / Math.max(1, total));
                int y = 3 + (track - thumb) * (maximum - controller.messageScroll) / maximum;
                new ChatRectangle().setColor(0x184B6A70).setCornerRadius(1).draw(context, getArea().w() - 3, 3, 2, track);
                new ChatRectangle().setColor(0x997C9B9F).setCornerRadius(1).draw(context, getArea().w() - 3, y, 2, thumb);
            }
        }
        private void drawMessage(ChatManager.TextEntry entry, int y, GuiContext context) {
            if (gutter() > 0) context.font.drawString(TIME.format(new Date(entry.timestamp)), 3, y + 3, ChatTheme.MUTED);
            for (ChatTextLayout.Run run : layout(entry).runs) {
                int x = 5 + gutter() + run.x;
                if (run.sticker >= 0) ChatEmojiRenderer.drawInlineEmoji(textures.get(run.sticker), x, y + run.y, ChatTextLayout.STICKER_SIZE);
                else context.font.drawStringWithShadow(run.text, x, y + run.y, entry.system ? 0xB7C5C8 : ChatTheme.TEXT);
            }
            int imageY = y + layout(entry).height + 3;
            for (String url : entry.images) {
                int x = 5 + gutter(), w = Math.max(20, getArea().w() - x - 12);
                if (imageY < getArea().h() && imageY + IMAGE_HEIGHT > 0) {
                    ChatImageCache.draw(url, x, imageY, w, IMAGE_HEIGHT);
                    int top = Math.max(0, imageY), bottom = Math.min(getArea().h(), imageY + IMAGE_HEIGHT);
                    imageHits.add(new ImageHit(url, context.transformX(x, top), context.transformY(x, top), w, bottom-top));
                }
                imageY += IMAGE_HEIGHT + 6;
            }
        }
        private void drawAchievementCard(ChatManager.AchievementEntry entry, int y, GuiContext context) {
            int x = 3, w = getArea().w() - 11;
            softBox(context, x, y + 1, w, 38, 0x66222E33, entry.special ? 0x99C6A86E : 0x305F858A, 4);
            StatBase stat = StatList.func_151177_a(entry.achievementId);
            Achievement achievement = stat instanceof Achievement ? (Achievement) stat : null;
            int textX = x + 8;
            if (achievement != null && achievement.theItemStack != null) {
                GL11.glPushMatrix();
                try {
                    RenderHelper.enableGUIStandardItemLighting();
                    ITEM_RENDERER.renderItemAndEffectIntoGUI(context.font, controller.mc.getTextureManager(), achievement.theItemStack, x + 8, y + 11);
                } finally { RenderHelper.disableStandardItemLighting(); GL11.glPopMatrix(); }
                textX += 23;
            }
            int available = x + w - textX - 7;
            context.font.drawString(context.font.trimStringToWidth(entry.playerName + " получил достижение", available), textX, y + 8, ChatTheme.MUTED);
            String title = achievement == null ? entry.achievementId : achievement.func_150951_e().getUnformattedText();
            context.font.drawStringWithShadow(context.font.trimStringToWidth(title, available), textX, y + 22, entry.special ? 0xE4C48B : ChatTheme.ACCENT);
        }
    }

    /** Draws suggestions and rich popup panels above the real ModularUI controls. */
    private static final class ChatOverlayWidget extends Widget<ChatOverlayWidget> {
        private final Controller controller;

        private ChatOverlayWidget(Controller controller) {
            this.controller = controller;
        }

        @Override
        public boolean canHover() {
            return false;
        }

        @Override
        public void draw(GuiContext context) {
            if (controller.enlargedImage != null) {
                Gui.drawRect(0, 0, controller.width, controller.height, 0xED101820);
                ChatImageCache.draw(controller.enlargedImage, 12, 24, Math.max(1, controller.width-24), Math.max(1, controller.height-48));
                context.font.drawString("Изображение — Esc или щелчок, чтобы вернуться", 12, 8, ChatTheme.TEXT);
                return;
            }
            drawActiveActionAccent();
            drawSuggestions(context);
            if (controller.popupMode == PopupMode.EMOJI) drawEmojiPopup(context);
            if (controller.popupMode == PopupMode.TITLES) drawTitlePopup(context);
        }

        private void drawActiveActionAccent() {
            if (controller.popupMode == PopupMode.NONE) return;
            int x = controller.actionButtonX + (controller.popupMode == PopupMode.EMOJI ? 0 : 28);
            Gui.drawRect(x + 6, controller.inputY + 20, x + 18, controller.inputY + 21, ChatTheme.ACCENT);
        }


        private void drawSuggestions(GuiContext context) {
            if (controller.popupMode != PopupMode.NONE) return;
            int visible = controller.suggestionCount();
            if (visible == 0) return;
            int bottom = controller.suggestionBottom(), top = bottom - visible * 15 - 6;
            int x = controller.messageX + 8, w = Math.min(220, controller.messageWidth - 16);
            softBox(context, x, top, w, bottom - top, ChatTheme.POPUP, ChatTheme.BORDER, 4);
            for (int i = 0; i < visible; i++) {
                int y = top + 3 + i * 15;
                if (i == controller.selectedSuggestion) new ChatRectangle().setColor(ChatTheme.HOVER).setCornerRadius(3).draw(context, x + 3, y, w - 6, 14);
                context.font.drawString(context.font.trimStringToWidth(controller.suggestions.get(i), w - 14), x + 7, y + 3, ChatTheme.TEXT);
            }
        }

        private void drawEmojiPopup(GuiContext context) {
            int x = controller.popupX(), y = controller.popupY();
            drawPopupBase(x, y, 228, controller.popupHeight(), "Стикеры", context);
            controller.scrollEmojis(0);
            int index = 0; String hover = "Выберите стикер";
            for (Map.Entry<String, ResourceLocation> entry : EmojiRegistry.getRegisteredEmojis().entrySet()) {
                int slot = index++ - controller.emojiScroll * 4;
                if (slot < 0 || slot >= 8) continue;
                int cellX = x + 10 + slot % 4 * 52, cellY = y + 25 + slot / 4 * 46;
                if (context.getAbsMouseX() >= cellX && context.getAbsMouseX() < cellX + 48
                        && context.getAbsMouseY() >= cellY && context.getAbsMouseY() < cellY + 44) {
                    softBox(context, cellX, cellY, 48, 44, ChatTheme.HOVER, 0x807CBBBB, 4);
                    hover = entry.getKey();
                }
                ChatEmojiRenderer.drawInlineEmoji(entry.getValue(), cellX + 4, cellY + 2, 40);
            }
            int maximum = controller.maxEmojiScroll();
            if (maximum > 0) {
                int thumb = Math.max(12, 92 * 2 / (maximum + 2));
                int thumbY = y + 25 + (92 - thumb) * controller.emojiScroll / maximum;
                Gui.drawRect(x + 220, y + 25, x + 224, y + 117, 0x405F858A);
                Gui.drawRect(x + 220, thumbY, x + 224, thumbY + thumb, ChatTheme.ACCENT);
            }
            context.font.drawString(hover, x + 12, y + 122, ChatTheme.MUTED);
        }

        private void drawTitlePopup(GuiContext context) {
            int x = controller.popupX();
            int y = controller.popupY();
            ChatTheme theme = ChatTheme.getActive();
            drawPopupBase(x, y, 228, controller.popupHeight(), "Титулы", context);
            Gui.drawRect(x + 4, y + 22, x + 113, y + 42,
                    controller.shopTab ? theme.getTerminalColor() : theme.getFrameColor());
            Gui.drawRect(x + 115, y + 22, x + 224, y + 42,
                    controller.shopTab ? theme.getFrameColor() : theme.getTerminalColor());
            context.font.drawStringWithShadow("Доступные", x + 31, y + 28, theme.getTitleColor());
            context.font.drawStringWithShadow("Магазин", x + 146, y + 28, theme.getTitleColor());
            if (!controller.shopTab) {
                Gui.drawRect(x + 6, y + 44, x + 222, y + 62, theme.getTerminalColor());
                context.font.drawStringWithShadow("Снять титул", x + 62, y + 49,
                        theme.getProductionColor());
            } else {
                context.font.drawString("Покупка за GMC", x + 9, y + 50,
                        theme.getLabelColor() & 0xFFFFFF);
            }
            List<TitleManager.Title> list = controller.shopTab ? controller.shopTitles : controller.unlockedTitles;
            int rowsTop = y + 65;
            for (int row = 0; row < controller.titleVisibleRows(); row++) {
                int index = controller.titleScroll + row;
                if (index >= list.size()) break;
                int rowY = rowsTop + row * 17;
                TitleManager.Title title = list.get(index);
                Gui.drawRect(x + 6, rowY, x + 222, rowY + 15,
                        (row & 1) == 0 ? 0x22FFFFFF : theme.getTerminalColor());
                String action = controller.shopTab ? "+ " : "  ";
                context.font.drawStringWithShadow(context.font.trimStringToWidth(action + title.prefix,
                        controller.shopTab ? 155 : 202), x + 10, rowY + 3, theme.getPrimaryColor());
                if (controller.shopTab) {
                    String price = title.coinPrice + " GMC";
                    context.font.drawStringWithShadow(price, x + 218 - context.font.getStringWidth(price), rowY + 3,
                            theme.getProductionColor());
                }
            }
        }

        private void drawPopupBase(int x, int y, int width, int height, String title, GuiContext context) {
            softBox(context, x, y, width, height, ChatTheme.POPUP, ChatTheme.BORDER, 6);
            context.font.drawString(title, x + 11, y + 8, ChatTheme.TEXT);
            Gui.drawRect(x + 10, y + 21, x + width - 10, y + 22, 0x205F858A);
        }

    }
}
