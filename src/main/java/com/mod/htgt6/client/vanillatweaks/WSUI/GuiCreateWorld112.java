package com.mod.htgt6.client.vanillatweaks.WSUI;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateFlatWorld;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;

/** Minecraft 1.12 GuiCreateWorld state machine implemented on the 1.7.10 world API. */
public final class GuiCreateWorld112 extends GuiCreateWorld {
    private final GuiScreen parent112;
    private GuiTextField nameField, seedField;
    private GuiButton createButton, modeButton, moreButton, structuresButton, bonusButton;
    private GuiButton typeButton, commandsButton, customizeButton;
    private String mode = "survival";
    private String folderName = "World";
    private boolean structures = true, bonusChest, commands, commandsExplicit, moreOptions, creating;
    private int worldTypeIndex;

    public GuiCreateWorld112(GuiScreen parent) {
        super(parent); parent112 = parent;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true); buttonList.clear();
        buttonList.add(createButton = new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("selectWorld.create")));
        buttonList.add(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
        buttonList.add(modeButton = new GuiButton(2, width / 2 - 75, 115, 150, 20, ""));
        buttonList.add(moreButton = new GuiButton(3, width / 2 - 75, 187, 150, 20, ""));
        buttonList.add(structuresButton = new GuiButton(4, width / 2 - 155, 100, 150, 20, ""));
        buttonList.add(typeButton = new GuiButton(5, width / 2 + 5, 100, 150, 20, ""));
        buttonList.add(commandsButton = new GuiButton(6, width / 2 - 155, 151, 150, 20, ""));
        buttonList.add(bonusButton = new GuiButton(7, width / 2 + 5, 151, 150, 20, ""));
        buttonList.add(customizeButton = new GuiButton(8, width / 2 + 5, 120, 150, 20, I18n.format("selectWorld.customizeType")));
        nameField = new GuiTextField(fontRendererObj, width / 2 - 100, 60, 200, 20);
        nameField.setMaxStringLength(32); nameField.setText(nameFieldValue()); nameField.setFocused(!moreOptions);
        seedField = new GuiTextField(fontRendererObj, width / 2 - 100, 60, 200, 20);
        seedField.setMaxStringLength(32); seedField.setText(seedValue());
        updateFolderName(); updateLabels(); updateVisibility();
    }

    private String savedName = "", savedSeed = "";
    private String nameFieldValue() { return savedName.isEmpty() ? I18n.format("selectWorld.newWorld") : savedName; }
    private String seedValue() { return savedSeed; }

    @Override public void updateScreen() { nameField.updateCursorCounter(); seedField.updateCursorCounter(); }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;
        switch (button.id) {
            case 0: createWorld(); break;
            case 1: mc.displayGuiScreen(parent112); break;
            case 2: cycleMode(); break;
            case 3: savedName=nameField.getText();savedSeed=seedField.getText();moreOptions=!moreOptions;updateVisibility(); break;
            case 4: structures=!structures;updateLabels(); break;
            case 5: cycleWorldType(); break;
            case 6: commands=!commands;commandsExplicit=true;updateLabels(); break;
            case 7: bonusChest=!bonusChest;updateLabels(); break;
            case 8: customizeWorldType(); break;
            default: break;
        }
    }

    private void cycleMode() {
        if ("survival".equals(mode)) mode="hardcore";
        else if ("hardcore".equals(mode)) mode="creative";
        else mode="survival";
        if (!commandsExplicit) commands="creative".equals(mode);
        updateLabels();
    }

    private void cycleWorldType() {
        int guard = 0;
        do { worldTypeIndex=(worldTypeIndex+1)%WorldType.worldTypes.length; guard++; }
        while (guard <= WorldType.worldTypes.length && (WorldType.worldTypes[worldTypeIndex]==null
                || !WorldType.worldTypes[worldTypeIndex].getCanBeCreated()));
        field_146334_a=""; updateLabels(); updateVisibility();
    }

    private void customizeWorldType() {
        WorldType type = selectedType();
        if (type == WorldType.FLAT) mc.displayGuiScreen(new GuiCreateFlatWorld(this, field_146334_a));
        else type.onCustomizeButton(mc, this);
    }

    private void createWorld() {
        if (creating || nameField.getText().trim().isEmpty()) return;
        creating=true; mc.displayGuiScreen(null);
        long seed = new java.util.Random().nextLong();
        String seedText=seedField.getText().trim();
        if(!seedText.isEmpty())try{long parsed=Long.parseLong(seedText);if(parsed!=0)seed=parsed;}catch(NumberFormatException e){seed=seedText.hashCode();}
        boolean hardcore="hardcore".equals(mode);
        WorldSettings.GameType gameType="creative".equals(mode)?WorldSettings.GameType.CREATIVE:WorldSettings.GameType.SURVIVAL;
        WorldSettings settings=new WorldSettings(seed,gameType,structures,hardcore,selectedType()).func_82750_a(field_146334_a);
        if(bonusChest&&!hardcore)settings.enableBonusChest();
        if(commands&&!hardcore)settings.enableCommands();
        mc.launchIntegratedServer(folderName,nameField.getText().trim(),settings);
    }

    @Override
    public void func_146318_a(WorldInfo info) {
        savedName=I18n.format("selectWorld.newWorld.copyOf",info.getWorldName());
        savedSeed=Long.toString(info.getSeed()); structures=info.isMapFeaturesEnabled(); commands=info.areCommandsAllowed(); commandsExplicit=true;
        field_146334_a=info.getGeneratorOptions();
        WorldType type=info.getTerrainType();worldTypeIndex=type==null?0:type.getWorldTypeID();
        mode=info.isHardcoreModeEnabled()?"hardcore":(info.getGameType()==WorldSettings.GameType.CREATIVE?"creative":"survival");
    }

    private void updateLabels() {
        boolean hardcore="hardcore".equals(mode);
        modeButton.displayString=I18n.format("selectWorld.gameMode")+": "+I18n.format("selectWorld.gameMode."+mode);
        moreButton.displayString=moreOptions?I18n.format("gui.done"):I18n.format("selectWorld.moreWorldOptions");
        structuresButton.displayString=toggle("selectWorld.mapFeatures",structures);
        bonusButton.displayString=toggle("selectWorld.bonusItems",bonusChest&&!hardcore);
        commandsButton.displayString=toggle("selectWorld.allowCommands",commands&&!hardcore);
        typeButton.displayString=I18n.format("selectWorld.mapType")+" "+I18n.format(selectedType().getTranslateName());
        bonusButton.enabled=commandsButton.enabled=!hardcore;
    }

    private void updateVisibility() {
        nameField.setVisible(!moreOptions); seedField.setVisible(moreOptions);
        modeButton.visible=!moreOptions;
        structuresButton.visible=bonusButton.visible=typeButton.visible=commandsButton.visible=moreOptions;
        WorldType type=selectedType(); customizeButton.visible=moreOptions&&type.isCustomizable();
        updateLabels();
    }

    private void updateFolderName() {
        savedName=nameField.getText();
        folderName=GuiCreateWorld.func_146317_a(mc.getSaveLoader(),savedName.trim());
        createButton.enabled=!savedName.trim().isEmpty()&&!creating;
    }

    private WorldType selectedType() {
        if(worldTypeIndex<0||worldTypeIndex>=WorldType.worldTypes.length||WorldType.worldTypes[worldTypeIndex]==null)worldTypeIndex=0;
        return WorldType.worldTypes[worldTypeIndex];
    }

    private static String toggle(String key,boolean value){return I18n.format(key)+" "+I18n.format(value?"options.on":"options.off");}

    @Override
    protected void keyTyped(char character,int key) {
        if(moreOptions){seedField.textboxKeyTyped(character,key);savedSeed=seedField.getText();}
        else{nameField.textboxKeyTyped(character,key);updateFolderName();}
        if(key==Keyboard.KEY_RETURN&&createButton.enabled)createWorld();
        else if(key==Keyboard.KEY_ESCAPE)mc.displayGuiScreen(parent112);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        // GuiCreateWorld.mouseClicked() accesses its private text fields,
        // which this independent 1.12-style state intentionally does not use.
        if (button == 0) {
            for (Object component : buttonList) {
                GuiButton guiButton = (GuiButton) component;
                if (!guiButton.mousePressed(mc, x, y)) continue;
                GuiScreenEvent.ActionPerformedEvent.Pre event =
                        new GuiScreenEvent.ActionPerformedEvent.Pre(this, guiButton, buttonList);
                if (!MinecraftForge.EVENT_BUS.post(event)) {
                    event.button.func_146113_a(mc.getSoundHandler());
                    actionPerformed(event.button);
                    if (this.equals(mc.currentScreen)) {
                        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.ActionPerformedEvent.Post(
                                this, event.button, buttonList));
                    }
                }
                break;
            }
        }
        if (moreOptions) seedField.mouseClicked(x, y, button);
        else nameField.mouseClicked(x, y, button);
    }

    @Override
    public void drawScreen(int mouseX,int mouseY,float partial) {
        drawDefaultBackground();drawCenteredString(fontRendererObj,I18n.format("selectWorld.create"),width/2,20,0xFFFFFF);
        if(moreOptions){drawString(fontRendererObj,I18n.format("selectWorld.enterSeed"),width/2-100,47,0xA0A0A0);drawString(fontRendererObj,I18n.format("selectWorld.seedInfo"),width/2-100,85,0xA0A0A0);seedField.drawTextBox();}
        else{drawString(fontRendererObj,I18n.format("selectWorld.enterName"),width/2-100,47,0xA0A0A0);drawString(fontRendererObj,I18n.format("selectWorld.resultFolder")+" "+folderName,width/2-100,85,0xA0A0A0);nameField.drawTextBox();drawCenteredString(fontRendererObj,I18n.format("selectWorld.gameMode."+mode+".line1"),width/2,137,0xA0A0A0);drawCenteredString(fontRendererObj,I18n.format("selectWorld.gameMode."+mode+".line2"),width/2,149,0xA0A0A0);}
        // GuiCreateWorld.drawScreen() renders its private text fields.  This
        // backport owns a separate 1.12-style state, so those vanilla fields
        // are intentionally not initialized and calling the parent here
        // crashes on field_146333_g.drawTextBox().  Render GuiScreen's two
        // component lists directly instead.
        for (Object component : buttonList) {
            ((GuiButton) component).drawButton(mc, mouseX, mouseY);
        }
        for (Object component : labelList) {
            ((GuiLabel) component).func_146159_a(mc, mouseX, mouseY);
        }
    }

    @Override public void onGuiClosed(){Keyboard.enableRepeatEvents(false);}
}
