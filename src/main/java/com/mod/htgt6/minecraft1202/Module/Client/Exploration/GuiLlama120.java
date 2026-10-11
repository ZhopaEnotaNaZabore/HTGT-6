package com.mod.htgt6.minecraft1202.Module.Client.Exploration;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.ContainerLlama120;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
public final class GuiLlama120 extends GuiContainer {
    private final ContainerLlama120 container;
    public GuiLlama120(ContainerLlama120 container){super(container);this.container=container;}
    @Override protected void drawGuiContainerForegroundLayer(int x,int y){
        fontRendererObj.drawString(container.llama.getCommandSenderName(),8,6,4210752);
        fontRendererObj.drawString(net.minecraft.util.StatCollector.translateToLocal("container.inventory"),8,72,4210752);
    }
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int x,int y){
        GL11.glColor4f(1,1,1,1);
        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/horse.png"));
        drawTexturedModalRect(guiLeft,guiTop,0,0,xSize,ySize);
        // Draw only usable slots; llamas have no saddle slot.
        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/generic_54.png"));
        drawTexturedModalRect(guiLeft+7,guiTop+35,7,17,18,18);
        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/horse.png"));
        if(container.columns>0)drawTexturedModalRect(guiLeft+79,guiTop+17,0,166,container.columns*18,54);
        net.minecraft.client.gui.inventory.GuiInventory.func_147046_a(guiLeft+51,guiTop+60,17,
                guiLeft+51-x,guiTop+25-y,container.llama);
    }
}
