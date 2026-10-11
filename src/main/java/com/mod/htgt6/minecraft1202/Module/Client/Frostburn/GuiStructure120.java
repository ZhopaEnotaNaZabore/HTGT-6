package com.mod.htgt6.minecraft1202.Module.Client.Frostburn;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.TileEntityStructure120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Frostburn.ContainerStructure120;
import com.mod.htgt6.minecraft1202.Module.Network.Frostburn.StructureNetwork120;

public final class GuiStructure120 extends GuiContainer {
    private final TileEntityStructure120 tile;
    private final GuiTextField[] fields=new GuiTextField[10];
    private boolean advanced,showBoundingBox=true;
    private int mode,revision,rotation,mirror;
    public GuiStructure120(ContainerStructure120 container) { super(container);tile=container.tile;xSize=300;ySize=236; }
    @Override public void initGui() {
        super.initGui();Keyboard.enableRepeatEvents(true);buttonList.clear();
        fields[0]=new GuiTextField(fontRendererObj,guiLeft+12,guiTop+33,276,18);fields[0].setMaxStringLength(128);
        for(int i=1;i<7;i++) { fields[i]=new GuiTextField(fontRendererObj,guiLeft+12+(i-1)%3*96,guiTop+(i<=3?76:119),84,18);fields[i].setMaxStringLength(3); }
        fields[7]=new GuiTextField(fontRendererObj,guiLeft+12,guiTop+76,276,18);fields[7].setMaxStringLength(16);
        fields[8]=new GuiTextField(fontRendererObj,guiLeft+12,guiTop+119,276,18);fields[8].setMaxStringLength(20);
        fields[9]=new GuiTextField(fontRendererObj,guiLeft+12,guiTop+33,276,18);fields[9].setMaxStringLength(128);
        readTile();
        buttonList.add(new GuiButton(4,guiLeft+12,guiTop+146,132,20,""));
        buttonList.add(new GuiButton(0,guiLeft+156,guiTop+146,132,20,I18n.format("htgt6.structure.apply")));
        buttonList.add(new GuiButton(1,guiLeft+12,guiTop+176,84,20,I18n.format("htgt6.structure.save")));
        buttonList.add(new GuiButton(2,guiLeft+108,guiTop+176,84,20,I18n.format("htgt6.structure.load")));
        buttonList.add(new GuiButton(3,guiLeft+204,guiTop+176,84,20,I18n.format("htgt6.structure.detect")));
        buttonList.add(new GuiButton(5,guiLeft+204,guiTop+4,84,16,I18n.format("htgt6.structure.options")));
        buttonList.add(new GuiButton(8,guiLeft+108,guiTop+4,90,16,""));
        buttonList.add(new GuiButton(6,guiLeft+12,guiTop+206,132,20,""));
        buttonList.add(new GuiButton(7,guiLeft+156,guiTop+206,132,20,""));
        updateButtons();
    }
    private void readTile() {
        showBoundingBox=tile.showBoundingBox;rotation=tile.rotation;mirror=tile.mirror;mode=tile.getBlockMetadata()&3;revision=tile.clientRevision;
        String[] values={tile.name,""+tile.offsetX,""+tile.offsetY,""+tile.offsetZ,""+tile.sizeX,""+tile.sizeY,""+tile.sizeZ,""+tile.integrity,""+tile.seed,tile.metadata};
        for(int i=0;i<fields.length;i++)fields[i].setText(values[i]);
    }
    private void updateButtons() {
        for(Object value:buttonList) {
            GuiButton b=(GuiButton)value;
            if(b.id==4)b.displayString=I18n.format("htgt6.structure.mode."+mode);
            if(b.id==1||b.id==3)b.enabled=mode==0;
            if(b.id==2)b.enabled=mode==1;
            if(b.id==8){b.enabled=mode==1;b.displayString=I18n.format("htgt6.structure.box."+showBoundingBox);}
            if(b.id==6){b.enabled=mode==1;b.displayString=I18n.format("htgt6.structure.rotation",rotation*90);}
            if(b.id==7){b.enabled=mode==1;b.displayString=I18n.format("htgt6.structure.mirror."+mirror);}
        }
    }
    @Override protected void actionPerformed(GuiButton button) {
        if(button.id==8){showBoundingBox=!showBoundingBox;updateButtons();return;}
        if(button.id==6){rotation=(rotation+1)&3;updateButtons();return;}
        if(button.id==7){mirror=(mirror+1)%3;updateButtons();return;}
        if(button.id==5){advanced=!advanced;for(GuiTextField field:fields)field.setFocused(false);return;}
        if(button.id==4){mode=(mode+1)&3;for(GuiTextField field:fields)field.setFocused(false);updateButtons();return;}
        StructureNetwork120.Edit edit=new StructureNetwork120.Edit();
        edit.dimension=tile.getWorldObj().provider.dimensionId;edit.x=tile.xCoord;edit.y=tile.yCoord;edit.z=tile.zCoord;
        edit.mode=mode;edit.action=button.id;edit.name=fields[0].getText();
        edit.metadata=fields[9].getText();edit.rotation=rotation;edit.mirror=mirror;edit.showBoundingBox=showBoundingBox;
        try {
            edit.ox=Integer.parseInt(fields[1].getText());edit.oy=Integer.parseInt(fields[2].getText());edit.oz=Integer.parseInt(fields[3].getText());
            edit.sx=Integer.parseInt(fields[4].getText());edit.sy=Integer.parseInt(fields[5].getText());edit.sz=Integer.parseInt(fields[6].getText());
            edit.integrity=Float.parseFloat(fields[7].getText());edit.seed=Long.parseLong(fields[8].getText());
        } catch(NumberFormatException error) { invalidInput();return; }
        if(edit.valid())StructureNetwork120.NETWORK.sendToServer(edit);else invalidInput();
    }
    private void invalidInput() { mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentTranslation("htgt6.structure.invalid_input")); }
    @Override public void updateScreen() {
        super.updateScreen();
        if(revision!=tile.clientRevision){readTile();updateButtons();}
        for(GuiTextField field:fields)field.updateCursorCounter();
    }
    @Override protected void keyTyped(char character,int key) {
        if(key==Keyboard.KEY_ESCAPE){mc.thePlayer.closeScreen();return;}
        if(key==Keyboard.KEY_TAB) {
            for(int i=0;i<fields.length;i++)if(fields[i].isFocused()) {
                fields[i].setFocused(false);int next=(i+1)%fields.length;
                while(!visible(next))next=(next+1)%fields.length;
                fields[next].setFocused(true);return;
            }
            fields[mode==3?9:0].setFocused(true);return;
        }
        for(int i=0;i<fields.length;i++)if(visible(i)&&fields[i].textboxKeyTyped(character,key))return;
    }
    @Override protected void mouseClicked(int x,int y,int button) {
        super.mouseClicked(x,y,button);for(int i=0;i<fields.length;i++)if(visible(i))fields[i].mouseClicked(x,y,button);
    }
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int mouseX,int mouseY) {
        drawRect(guiLeft,guiTop,guiLeft+xSize,guiTop+ySize,0xff242424);
        fontRendererObj.drawString(I18n.format("htgt6.structure.title"),guiLeft+12,guiTop+10,0xffffff);
        fontRendererObj.drawString(I18n.format(mode==3?"htgt6.structure.metadata":"htgt6.structure.name"),guiLeft+12,guiTop+23,0xcccccc);
        fontRendererObj.drawString(I18n.format(advanced?"htgt6.structure.integrity":"htgt6.structure.offset"),guiLeft+12,guiTop+63,0xcccccc);
        fontRendererObj.drawString(I18n.format(advanced?"htgt6.structure.seed":"htgt6.structure.size"),guiLeft+12,guiTop+106,0xcccccc);
        for(int i=0;i<fields.length;i++)if(visible(i))fields[i].drawTextBox();
    }
    private boolean visible(int index) {
        if(index==0)return mode!=3;
        if(index==9)return mode==3;
        return advanced?index>=7:index<7;
    }
    @Override public void onGuiClosed() { super.onGuiClosed();Keyboard.enableRepeatEvents(false); }
}
