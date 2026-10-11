package com.mod.htgt6.client.vanillatweaks.CHAT;

import com.mod.htgt6.HTGT6;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.item.ItemStack;
import com.mod.htgt6.common.item.htgt6.coins.HiTech6Coins;
import java.util.ArrayList;
import java.util.List;

/** Compact two-tab title chooser. Server remains the authority for every purchase. */
public class GuiTitleSelection extends GuiScreen {
    private final GuiScreen parentScreen;
    private final List<TitleManager.Title> unlocked = new ArrayList<TitleManager.Title>();
    private final List<TitleManager.Title> shop = new ArrayList<TitleManager.Title>();
    private boolean shopTab;

    public GuiTitleSelection(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
        Minecraft mc = Minecraft.getMinecraft();
        for (TitleManager.Title title : TitleManager.TITLE_DATABASE.values()) {
            if (title.coinPrice > 0) { shop.add(title); if (TitleManager.clientUnlockedTitles.contains(title.id)) unlocked.add(title); continue; }
            if (title.isOpOnly || title.achievementId == null) { unlocked.add(title); continue; }
            StatBase stat = StatList.func_151177_a(title.achievementId);
            if (stat instanceof Achievement && mc.thePlayer != null && mc.thePlayer.getStatFileWriter().hasAchievementUnlocked((Achievement) stat)) unlocked.add(title);
        }
    }
    public void initGui() {
        buttonList.clear(); int x=width/2-110, y=height/2-70;
        buttonList.add(new GuiButton(300,x,y,109,20,"Titles"));
        buttonList.add(new GuiButton(301,x+111,y,109,20,"Shop")); y+=24;
        buttonList.add(new GuiButton(200,x,y,220,20,shopTab?"Back":"Clear active title")); y+=24;
        List<TitleManager.Title> list=shopTab?shop:unlocked;
        for(int i=0;i<list.size() && i<8;i++) { TitleManager.Title t=list.get(i); String label=shopTab ? "Buy: "+t.prefix : "Equip: "+t.prefix; buttonList.add(new GuiButton(i,x,y,220,20,label)); y+=22; }
    }
    protected void actionPerformed(GuiButton button) {
        if(button.id==300){shopTab=false;initGui();return;} if(button.id==301){shopTab=true;initGui();return;}
        if(button.id==200){ if(!shopTab) HTGT6.network.sendToServer(new PacketRequestChangeTitle("none")); mc.displayGuiScreen(parentScreen); return; }
        List<TitleManager.Title> list=shopTab?shop:unlocked;
        if(button.id>=0 && button.id<list.size()) { TitleManager.Title title=list.get(button.id); HTGT6.network.sendToServer(new PacketRequestChangeTitle(shopTab?"buy:"+title.id:title.id)); mc.displayGuiScreen(parentScreen); }
    }
    public void drawScreen(int mouseX,int mouseY,float partialTicks){
        drawDefaultBackground(); drawCenteredString(fontRendererObj,shopTab?"Title shop (Монеты GMC)":"Your titles",width/2,height/2-88,0xFFFFFF); super.drawScreen(mouseX,mouseY,partialTicks);
        if(shopTab) { int x=width/2-110, y=height/2-22; for(int i=0;i<shop.size() && i<8;i++){ TitleManager.Title title=shop.get(i); String price="§e"+title.coinPrice; fontRendererObj.drawStringWithShadow(price,x+178-fontRendererObj.getStringWidth(price),y+6,0xFFFFFF); itemRender.renderItemAndEffectIntoGUI(fontRendererObj,mc.getTextureManager(),new ItemStack(HiTech6Coins.HiTech6Coin),x+182,y+2); y+=22; } }
    }
}
