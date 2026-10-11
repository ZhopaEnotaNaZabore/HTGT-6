package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageFurnace120;
import org.lwjgl.opengl.GL11;
public final class GuiVillageFurnace120 extends GuiFurnace {
    private final TileEntityVillageFurnace120 tile;
    public GuiVillageFurnace120(InventoryPlayer player,TileEntityVillageFurnace120 tile){super(player,tile);this.tile=tile;}
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int mouseX,int mouseY){
        GL11.glColor4f(1,1,1,1);
        mc.getTextureManager().bindTexture(new ResourceLocation("htgt6","textures/gui/container/village_pillage/"+tile.furnaceType()+".png"));
        drawTexturedModalRect(guiLeft,guiTop,0,0,xSize,ySize);
        if(tile.isBurning()){
            int height=Math.max(1,Math.min(14,tile.getBurnTimeRemainingScaled(13)+1));
            mc.getTextureManager().bindTexture(new ResourceLocation("htgt6","textures/gui/container/village_pillage/lit_progress.png"));
            func_146110_a(guiLeft+56,guiTop+36+14-height,0,14-height,14,height,14,14);
        }
        int width=Math.min(24,tile.getCookProgressScaled(24));
        if(width>0){mc.getTextureManager().bindTexture(new ResourceLocation("htgt6","textures/gui/container/village_pillage/burn_progress.png"));
            func_146110_a(guiLeft+79,guiTop+34,0,0,width,16,24,16);}
    }
}
