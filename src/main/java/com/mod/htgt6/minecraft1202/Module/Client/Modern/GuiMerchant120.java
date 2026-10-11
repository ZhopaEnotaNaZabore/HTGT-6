package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.Collections;

/** 1.20.2 wandering-trader screen backed by the native 1.7 merchant container and packets. */
@SideOnly(Side.CLIENT)
public final class GuiMerchant120 extends GuiMerchant {
    public static final int GUI_WIDTH=276,GUI_HEIGHT=166,VISIBLE_OFFERS=7;
    private static final ResourceLocation TEXTURE=
            new ResourceLocation("htgt6","textures/gui/container/wandering_trader.png");
    private static final ResourceLocation XP_BACKGROUND=sprite("experience_bar_background");
    private static final ResourceLocation XP_CURRENT=sprite("experience_bar_current");
    private static final ResourceLocation XP_RESULT=sprite("experience_bar_result");
    private static final ResourceLocation SCROLLER=sprite("scroller");
    private static final ResourceLocation SCROLLER_DISABLED=sprite("scroller_disabled");
    private static final ResourceLocation TRADE_ARROW=sprite("trade_arrow");
    private static final ResourceLocation TRADE_ARROW_DISABLED=sprite("trade_arrow_out_of_stock");
    private static final ResourceLocation OUT_OF_STOCK=sprite("out_of_stock");
    private final IMerchant merchant;
    private final String displayTitle;
    private int merchantLevel;
    private net.minecraft.entity.passive.EntityVillager trackedVillager;
    public void track(net.minecraft.entity.passive.EntityVillager entity){trackedVillager=entity;}
    private final boolean showProgress;
    private int selectedRecipe,scrollOffset;
    private boolean scrolling;
    private MerchantRecipeList drawingOffers;

    public GuiMerchant120(InventoryPlayer inventory,IMerchant merchant,World world){
        this(inventory,merchant,world,I18n.format("entity.HTGT6.WanderingTrader.name"),0,false);
    }
    public GuiMerchant120(InventoryPlayer inventory,IMerchant merchant,World world,
            String title,int level,boolean progress){
        super(inventory,merchant,world,title);
        displayTitle=title;merchantLevel=Math.max(0,Math.min(5,level));showProgress=progress;
        this.merchant=merchant;xSize=GUI_WIDTH;ySize=GUI_HEIGHT;
        repositionSlots((ContainerMerchant)inventorySlots);
    }
    private static void repositionSlots(ContainerMerchant container){
        int[][] points={{136,37},{162,37},{220,37}};
        for(int i=0;i<3;i++){Slot slot=(Slot)container.inventorySlots.get(i);
            slot.xDisplayPosition=points[i][0];slot.yDisplayPosition=points[i][1];}
        for(int row=0;row<3;row++)for(int column=0;column<9;column++){
            Slot slot=(Slot)container.inventorySlots.get(3+row*9+column);
            slot.xDisplayPosition=108+column*18;slot.yDisplayPosition=84+row*18;
        }
        for(int column=0;column<9;column++){Slot slot=(Slot)container.inventorySlots.get(30+column);
            slot.xDisplayPosition=108+column*18;slot.yDisplayPosition=142;}
    }
    @Override public void initGui(){super.initGui();buttonList.clear();}
    @Override public void updateScreen(){
        if(trackedVillager!=null&&trackedVillager.getEntityData().hasKey("MerchantRank120"))
            merchantLevel=trackedVillager.getEntityData().getInteger("MerchantRank120");
        MerchantRecipeList offers=offers();if(offers==null||offers.isEmpty()){selectedRecipe=scrollOffset=0;return;}
        selectedRecipe=Math.min(selectedRecipe,offers.size()-1);
        scrollOffset=Math.min(scrollOffset,Math.max(0,offers.size()-VISIBLE_OFFERS));
    }
    @Override protected void drawGuiContainerForegroundLayer(int mouseX,int mouseY){
        String title=showProgress&&merchantLevel>0&&merchantLevel<=5
                ?I18n.format("merchant.title",displayTitle,I18n.format("merchant.level."+merchantLevel)):displayTitle;
        fontRendererObj.drawString(title,49+GUI_WIDTH/2-fontRendererObj.getStringWidth(title)/2,6,4210752);
        fontRendererObj.drawString(I18n.format("container.inventory"),107,74,4210752);
        String trades=I18n.format("merchant.trades");
        fontRendererObj.drawString(trades,53-fontRendererObj.getStringWidth(trades)/2,6,4210752);
    }
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int mouseX,int mouseY){
        GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(TEXTURE);
        drawTexture(guiLeft,guiTop,0,0,GUI_WIDTH,GUI_HEIGHT,512,256);
        if(showProgress&&merchantLevel>0&&merchantLevel<5&&trackedVillager!=null){
            drawSprite(XP_BACKGROUND,guiLeft+136,guiTop+16,102,5);
            int xp=trackedVillager.getEntityData().getInteger("MerchantXp120");
            int current=progressWidth(merchantLevel,xp);
            if(current>0)drawSpritePart(XP_CURRENT,guiLeft+136,guiTop+16,current,5,102,5);
            int[] rewards=trackedVillager.getEntityData().getIntArray("MerchantOfferXp120");
            MerchantRecipe selected=selected(drawingOffers==null?offers():drawingOffers);
            if(selected!=null&&!selected.isRecipeDisabled()&&inventorySlots.getSlot(2).getHasStack()&&selectedRecipe<rewards.length){
                int future=progressWidth(merchantLevel,(int)Math.min(Integer.MAX_VALUE,(long)xp+rewards[selectedRecipe]));
                if(future>current){
                    GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(XP_RESULT);
                    drawTexture(guiLeft+136+current,guiTop+16,current,0,future-current,5,102,5);
                }
            }
        }
        MerchantRecipeList list=drawingOffers==null?offers():drawingOffers;
        for(int row=0;row<VISIBLE_OFFERS;row++){
            int index=scrollOffset+row,y=guiTop+18+row*20;
            boolean populated=list!=null&&index<list.size();
            int color=populated&&index==selectedRecipe?0x80606060:
                    populated&&isInside(mouseX,mouseY,guiLeft+5,y,89,20)?0x40404040:0x18000000;
            drawRect(guiLeft+5,y,guiLeft+94,y+20,color);
        }
        if(list!=null&&list.size()>VISIBLE_OFFERS){
            int trackTop=guiTop+18,trackHeight=139;
            int maxOffset=list.size()-VISIBLE_OFFERS;
            int thumbY=trackTop+(trackHeight-27)*scrollOffset/maxOffset;
            drawSprite(SCROLLER,guiLeft+94,thumbY,6,27);
        }else{
            drawSprite(SCROLLER_DISABLED,guiLeft+94,guiTop+18,6,27);
        }
        MerchantRecipe selected=selected(list);
        if(selected!=null&&selected.isRecipeDisabled()){
            drawSprite(OUT_OF_STOCK,guiLeft+182,guiTop+35,28,21);
        }
    }
    @Override public void drawScreen(int mouseX,int mouseY,float partial){
        if(scrolling)updateScrollFromMouse(mouseY);
        MerchantRecipeList real=offers();
        drawingOffers=real;
        merchant.setRecipes(new MerchantRecipeList());
        try{
            super.drawScreen(mouseX,mouseY,partial);
        }finally{
            merchant.setRecipes(real);
        }
        try{
            if(real!=null){
                renderOffers(real,mouseX,mouseY);
                MerchantRecipe selected=selected(real);
                if(selected!=null&&selected.isRecipeDisabled()
                        &&isInside(mouseX,mouseY,guiLeft+182,guiTop+35,28,21)){
                    drawHoveringText(Collections.singletonList(I18n.format("merchant.deprecated")),mouseX,mouseY,fontRendererObj);
                }
            }
        }finally{
            drawingOffers=null;
        }
    }
    private void renderOffers(MerchantRecipeList list,int mouseX,int mouseY){
        RenderHelper.enableGUIStandardItemLighting();GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        itemRender.zLevel=100;
        for(int row=0;row<VISIBLE_OFFERS&&scrollOffset+row<list.size();row++){
            MerchantRecipe recipe=(MerchantRecipe)list.get(scrollOffset+row);
            int y=guiTop+19+row*20;
            drawStack(recipe.getItemToBuy(),guiLeft+10,y);
            if(recipe.getSecondItemToBuy()!=null)drawStack(recipe.getSecondItemToBuy(),guiLeft+40,y);
            drawTradeArrow(guiLeft+60,y+4,recipe.isRecipeDisabled());
            drawStack(recipe.getItemToSell(),guiLeft+73,y);
            if(recipe.isRecipeDisabled()){GL11.glDisable(GL11.GL_LIGHTING);
                drawRect(guiLeft+6,y-1,guiLeft+93,y+17,0x60AA0000);}
        }
        itemRender.zLevel=0;GL11.glDisable(GL11.GL_LIGHTING);
        for(int row=0;row<VISIBLE_OFFERS&&scrollOffset+row<list.size();row++){
            MerchantRecipe recipe=(MerchantRecipe)list.get(scrollOffset+row);int y=guiTop+19+row*20;
            if(isInside(mouseX,mouseY,guiLeft+10,y,16,16))renderToolTip(recipe.getItemToBuy(),mouseX,mouseY);
            else if(recipe.getSecondItemToBuy()!=null&&isInside(mouseX,mouseY,guiLeft+40,y,16,16))
                renderToolTip(recipe.getSecondItemToBuy(),mouseX,mouseY);
            else if(isInside(mouseX,mouseY,guiLeft+73,y,16,16))renderToolTip(recipe.getItemToSell(),mouseX,mouseY);
        }
        GL11.glEnable(GL11.GL_DEPTH_TEST);RenderHelper.enableStandardItemLighting();
    }
    private void drawStack(ItemStack stack,int x,int y){
        itemRender.renderItemAndEffectIntoGUI(fontRendererObj,mc.getTextureManager(),stack,x,y);
        itemRender.renderItemOverlayIntoGUI(fontRendererObj,mc.getTextureManager(),stack,x,y);
    }
    private void drawTradeArrow(int x,int y,boolean disabled){
        drawSprite(disabled?TRADE_ARROW_DISABLED:TRADE_ARROW,x,y-1,10,9);
    }
    @Override protected void mouseClicked(int mouseX,int mouseY,int button){
        super.mouseClicked(mouseX,mouseY,button);if(button!=0)return;
        MerchantRecipeList list=offers();
        if(list!=null&&list.size()>VISIBLE_OFFERS
                &&isInside(mouseX,mouseY,guiLeft+94,guiTop+18,6,140)){
            scrolling=true;
            updateScrollFromMouse(mouseY);
            return;
        }
        if(isInside(mouseX,mouseY,guiLeft+5,guiTop+18,89,VISIBLE_OFFERS*20)){
            int index=scrollOffset+(mouseY-(guiTop+18))/20;
            if(list!=null&&index>=0&&index<list.size())select(index);
        }
    }
    @Override protected void mouseMovedOrUp(int mouseX,int mouseY,int button){
        super.mouseMovedOrUp(mouseX,mouseY,button);
        if(button==0)scrolling=false;
    }
    @Override public void handleMouseInput(){
        super.handleMouseInput();int wheel=Mouse.getEventDWheel();MerchantRecipeList list=offers();
        if(wheel==0||list==null||list.size()<=VISIBLE_OFFERS)return;
        scrollOffset=Math.max(0,Math.min(list.size()-VISIBLE_OFFERS,scrollOffset+(wheel<0?1:-1)));
    }
    private void updateScrollFromMouse(int mouseY){
        MerchantRecipeList list=offers();
        if(list==null||list.size()<=VISIBLE_OFFERS){scrollOffset=0;return;}
        int maxOffset=list.size()-VISIBLE_OFFERS;
        float position=(mouseY-(guiTop+18)-13.5F)/112.0F;
        position=Math.max(0.0F,Math.min(1.0F,position));
        scrollOffset=Math.round(position*maxOffset);
    }
    private void select(int index){
        selectedRecipe=index;((ContainerMerchant)inventorySlots).setCurrentRecipeIndex(index);
        ByteBuf buffer=Unpooled.buffer();
        buffer.writeInt(index);
        mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("MC|TrSel",buffer));
    }
    private MerchantRecipeList offers(){return merchant.getRecipes(mc==null?null:mc.thePlayer);}
    private MerchantRecipe selected(MerchantRecipeList list){
        return list!=null&&selectedRecipe>=0&&selectedRecipe<list.size()
                ?(MerchantRecipe)list.get(selectedRecipe):null;
    }
    private static boolean isInside(int mx,int my,int x,int y,int width,int height){
        return mx>=x&&my>=y&&mx<x+width&&my<y+height;
    }
    public static int progressWidth(int level,int xp){
        if(level<1||level>=5)return 0;
        int min=level==1?0:com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerEconomy145.threshold(level-1);
        int max=com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerEconomy145.threshold(level);
        return Math.max(0,Math.min(102,(int)(102L*(xp-min)/(max-min))));
    }
    private static ResourceLocation sprite(String name){
        return new ResourceLocation("htgt6","textures/gui/container/villager/"+name+".png");
    }
    private void drawSprite(ResourceLocation texture,int x,int y,int width,int height){
        GL11.glColor4f(1,1,1,1);
        mc.getTextureManager().bindTexture(texture);drawTexture(x,y,0,0,width,height,width,height);
    }
    private void drawSpritePart(ResourceLocation texture,int x,int y,int width,int height,
            int textureWidth,int textureHeight){
        GL11.glColor4f(1,1,1,1);
        mc.getTextureManager().bindTexture(texture);drawTexture(x,y,0,0,width,height,textureWidth,textureHeight);
    }
    private void drawTexture(int x,int y,int u,int v,int width,int height,int textureWidth,int textureHeight){
        float u0=(float)u/textureWidth,u1=(float)(u+width)/textureWidth;
        float v0=(float)v/textureHeight,v1=(float)(v+height)/textureHeight;
        Tessellator t=Tessellator.instance;t.startDrawingQuads();
        t.addVertexWithUV(x,y+height,zLevel,u0,v1);t.addVertexWithUV(x+width,y+height,zLevel,u1,v1);
        t.addVertexWithUV(x+width,y,zLevel,u1,v0);t.addVertexWithUV(x,y,zLevel,u0,v0);t.draw();
    }
}
