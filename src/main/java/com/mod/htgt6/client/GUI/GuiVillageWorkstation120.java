package com.mod.htgt6.client.GUI;

import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.ContainerVillageWorkstation120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageWorkstationType120;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.TileEntityBountifulBannerRenderer;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Bountiful.BountifulBannerColors;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/** 1.20 workstation presentation; all transformations remain server-side in the container. */
public final class GuiVillageWorkstation120 extends GuiContainer {
    private static final int GRID_X=57,GRID_Y=14,CELL=14,COLUMNS=4,VISIBLE_ROWS=4;
    private static final int STONE_GRID_X=52,STONE_GRID_Y=14,STONE_CELL_X=16,STONE_CELL_Y=18,STONE_ROWS=3;
    private static final float TARGET_UI_SCALE=1.12F;
    private static final ResourceLocation BANNER_BASE=new ResourceLocation("htgt6","textures/entity/bountiful/banner/base.png");
    private final ContainerVillageWorkstation120 menu;
    private com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulArmorStand armorPreview;
    private int loomScrollRow;
    private int stoneScrollRow;
    private int widgetMouseX,widgetMouseY;
    private boolean draggingScroll;
    private final java.util.List<BookRun> bookRuns=new java.util.ArrayList<BookRun>();
    private static final class BookRun {final net.minecraft.util.IChatComponent component;final String text;final int x,y,w;
        BookRun(net.minecraft.util.IChatComponent c,String t,int x,int y,int w){component=c;text=t;this.x=x;this.y=y;this.w=w;}}

    public GuiVillageWorkstation120(ContainerVillageWorkstation120 menu){super(menu);this.menu=menu;xSize=menu.getType()==VillageWorkstationType120.LECTERN?192:176;ySize=menu.getType()==VillageWorkstationType120.LECTERN?192:166;}
    @Override @SuppressWarnings("unchecked") public void initGui(){super.initGui();if(menu.getType()==VillageWorkstationType120.LECTERN){
        int left=(width-xSize)/2,top=(height-ySize)/2;
        buttonList.add(new GuiButton(0,left+36,top+170,28,18,"<"));
        buttonList.add(new GuiButton(1,left+130,top+170,28,18,">"));
        buttonList.add(new GuiButton(ContainerVillageWorkstation120.LECTERN_TAKE_BOOK,left+65,top+170,70,18,
                StatCollector.translateToLocal("container.lectern.take_book")));
    }}
    private boolean selectable(){VillageWorkstationType120 type=menu.getType();return type==VillageWorkstationType120.STONECUTTER||type==VillageWorkstationType120.LOOM||type==VillageWorkstationType120.LECTERN;}
    private int controlX(){switch(menu.getType()){case STONECUTTER:return 62;case LOOM:return 75;default:return 103;}}
    @Override protected void actionPerformed(GuiButton button){mc.playerController.sendEnchantPacket(menu.windowId,button.id);}
    @Override public void updateScreen(){super.updateScreen();int choices=menu.getType()==VillageWorkstationType120.LECTERN
            ?menu.getLecternPageCount():menu.getChoiceCount();for(Object value:buttonList){GuiButton button=(GuiButton)value;
        button.enabled=menu.getLecternBook()!=null&&(button.id==ContainerVillageWorkstation120.LECTERN_TAKE_BOOK
                ||button.id==0&&menu.getSelected()>0||button.id==1&&menu.getSelected()+1<choices);}}
    @Override protected void mouseClicked(int mouseX,int mouseY,int button){
        mouseX=unscaleX(mouseX);mouseY=unscaleY(mouseY);
        if(button==0&&menu.getType()==VillageWorkstationType120.LECTERN&&clickBook(mouseX-guiLeft,mouseY-guiTop))return;
        if(button==0&&(menu.getType()==VillageWorkstationType120.STONECUTTER||menu.getType()==VillageWorkstationType120.LOOM)
                &&mouseX>=guiLeft+119&&mouseX<guiLeft+131&&mouseY>=guiTop+15&&mouseY<guiTop+69){
            draggingScroll=true;scrollTo(mouseY);return;
        }
        if(button==0&&menu.getType()==VillageWorkstationType120.STONECUTTER){
            int left=(width-xSize)/2,top=(height-ySize)/2,localX=mouseX-left-STONE_GRID_X,
                    localY=mouseY-top-STONE_GRID_Y;
            if(localX>=0&&localX<COLUMNS*STONE_CELL_X&&localY>=0&&localY<STONE_ROWS*STONE_CELL_Y){
                int index=(stoneScrollRow+localY/STONE_CELL_Y)*COLUMNS+localX/STONE_CELL_X;
                if(index<menu.getChoiceCount()){mc.playerController.sendEnchantPacket(menu.windowId,
                        ContainerVillageWorkstation120.STONECUTTER_SELECTION_BASE+index);return;}
            }
        }
        if(button==0&&menu.getType()==VillageWorkstationType120.LOOM){
            int left=(width-xSize)/2,top=(height-ySize)/2,localX=mouseX-left-GRID_X,localY=mouseY-top-GRID_Y;
            if(localX>=0&&localX<COLUMNS*CELL&&localY>=0&&localY<VISIBLE_ROWS*CELL){
                int index=(loomScrollRow+localY/CELL)*COLUMNS+localX/CELL;
                if(index<menu.getChoiceCount()){mc.playerController.sendEnchantPacket(menu.windowId,
                        ContainerVillageWorkstation120.LOOM_SELECTION_BASE+index);return;}
            }
        }
        super.mouseClicked(mouseX,mouseY,button);
    }
    @Override public void handleMouseInput(){
        super.handleMouseInput();
        if(menu.getType()!=VillageWorkstationType120.LOOM&&menu.getType()!=VillageWorkstationType120.STONECUTTER)return;
        int wheel=Mouse.getEventDWheel();if(wheel==0)return;
        int rows=(menu.getChoiceCount()+COLUMNS-1)/COLUMNS;
        if(menu.getType()==VillageWorkstationType120.LOOM){int max=Math.max(0,rows-VISIBLE_ROWS);
            loomScrollRow=Math.max(0,Math.min(max,loomScrollRow+(wheel<0?1:-1)));
        }else{int max=Math.max(0,rows-STONE_ROWS);
            stoneScrollRow=Math.max(0,Math.min(max,stoneScrollRow+(wheel<0?1:-1)));}
    }
    @Override public void drawScreen(int mouseX,int mouseY,float partial){float scale=uiScale();int virtualX=unscaleX(mouseX),virtualY=unscaleY(mouseY);
        GL11.glPushMatrix();GL11.glTranslatef(width*.5F,height*.5F,0);GL11.glScalef(scale,scale,1);GL11.glTranslatef(-width*.5F,-height*.5F,0);
        try{super.drawScreen(virtualX,virtualY,partial);}finally{GL11.glPopMatrix();}}
    @Override protected void mouseMovedOrUp(int x,int y,int button){if(button==0)draggingScroll=false;super.mouseMovedOrUp(unscaleX(x),unscaleY(y),button);}
    @Override protected void mouseClickMove(int x,int y,int button,long elapsed){
        if(draggingScroll&&button==0){scrollTo(unscaleY(y));return;}
        super.mouseClickMove(unscaleX(x),unscaleY(y),button,elapsed);
    }
    private void scrollTo(int y){
        int visible=menu.getType()==VillageWorkstationType120.LOOM?VISIBLE_ROWS:STONE_ROWS;
        int max=Math.max(0,(menu.getChoiceCount()+3)/4-visible);
        int row=Math.round(Math.max(0,Math.min(1,(y-guiTop-15-7.5F)/39F))*max);
        if(menu.getType()==VillageWorkstationType120.LOOM)loomScrollRow=row;else stoneScrollRow=row;
    }
    private float uiScale(){return menu.getType()==VillageWorkstationType120.LECTERN?1:uiScaleForViewport(width,height);}
    public static float uiScaleForViewport(int viewportWidth,int viewportHeight){float fit=Math.min(
            (viewportWidth-8F)/176F,(viewportHeight-8F)/166F);return Math.max(1F,Math.min(TARGET_UI_SCALE,fit));}
    private int unscaleX(int value){float scale=uiScale();return Math.round(width*.5F+(value-width*.5F)/scale);}
    private int unscaleY(int value){float scale=uiScale();return Math.round(height*.5F+(value-height*.5F)/scale);}
    @Override protected void drawGuiContainerBackgroundLayer(float partial,int mouseX,int mouseY){
        widgetMouseX=mouseX;widgetMouseY=mouseY;
        drawDefaultBackground();int left=(width-xSize)/2,top=(height-ySize)/2;
        if(menu.getType()==VillageWorkstationType120.LECTERN){
            GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(new ResourceLocation("minecraft","textures/gui/book.png"));
            drawTexturedModalRect(left,top,0,0,192,192);return;
        }
        GL11.glColor4f(1F,1F,1F,1F);mc.getTextureManager().bindTexture(background());
        drawTexturedModalRect(left,top,0,0,xSize,ySize);
        if(menu.getType()==VillageWorkstationType120.LOOM){drawLoomGrid(left,top);
            String pattern=menu.getSelectedLoomPattern();if(pattern!=null)drawPatternIcon(pattern,left+140,top+13,16,32);}
        if(menu.getType()==VillageWorkstationType120.CARTOGRAPHY)drawMapPreview(left,top);
        if(menu.getType()==VillageWorkstationType120.SMITHING)drawSmithingPreview(left,top);
        if(menu.getType()==VillageWorkstationType120.STONECUTTER)drawStonecutterGrid(left,top);
    }
    @Override protected void drawGuiContainerForegroundLayer(int mouseX,int mouseY){
        int titleX=menu.getType()==VillageWorkstationType120.SMITHING?29:
                menu.getType()==VillageWorkstationType120.LOOM?59:
                menu.getType()==VillageWorkstationType120.LECTERN?42:8;
        if(menu.getType()!=VillageWorkstationType120.LECTERN){fontRendererObj.drawString(title(),titleX,6,0x404040);
            fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"),8,72,0x404040);}
        if(menu.getType()==VillageWorkstationType120.LECTERN){
            String value=(menu.getSelected()+1)+" / "+menu.getLecternPageCount();
            int center=100;fontRendererObj.drawString(value,center-fontRendererObj.getStringWidth(value)/2,16,0x50432F);
        }
        if(menu.getType()==VillageWorkstationType120.LOOM){String pattern=menu.getSelectedLoomPattern();if(pattern!=null){
            String name=StatCollector.translateToLocal("loom.pattern."+pattern);
            fontRendererObj.drawString(name,57,73,0x404040);
        }}
        if(menu.getType()==VillageWorkstationType120.LECTERN){net.minecraft.item.ItemStack book=menu.getLecternBook();
            if(book!=null&&book.hasTagCompound()&&book.getTagCompound().hasKey("pages")){
                net.minecraft.nbt.NBTTagList pages=book.getTagCompound().getTagList("pages",8);
                int page=Math.min(menu.getSelected(),pages.tagCount()-1);
                if(page>=0)drawBookPage(pages.getStringTagAt(page),mouseX-guiLeft,mouseY-guiTop);
            }
        }
    }
    private void drawBookPage(String raw,int mouseX,int mouseY){
        bookRuns.clear();net.minecraft.util.IChatComponent root;
        try{com.google.gson.JsonElement json=new com.google.gson.JsonParser().parse(raw);adaptPageLinks(json);root=net.minecraft.util.IChatComponent.Serializer.func_150699_a(json.toString());}
        catch(Exception failure){root=new net.minecraft.util.ChatComponentText(raw);}
        if(root==null)return;int x=36,y=34;
        for(Object value:root){net.minecraft.util.IChatComponent component=(net.minecraft.util.IChatComponent)value;
            String remaining=component.getUnformattedTextForChat();
            while(!remaining.isEmpty()&&y<151){
                int newline=remaining.indexOf('\n');String line=newline<0?remaining:remaining.substring(0,newline);
                if(line.isEmpty()){x=36;y+=fontRendererObj.FONT_HEIGHT;remaining=remaining.substring(1);continue;}
                String prefix=component.getChatStyle().getFormattingCode();
                String part=fontRendererObj.trimStringToWidth(prefix+line,152-x);
                part=part.startsWith(prefix)?part.substring(prefix.length()):"";
                if(part.isEmpty()){if(x==36)break;x=36;y+=fontRendererObj.FONT_HEIGHT;continue;}
                if(part.length()<line.length()){int space=part.lastIndexOf(' ');if(space>0)part=part.substring(0,space+1);}
                int width=fontRendererObj.getStringWidth(prefix+part);
                bookRuns.add(new BookRun(component,part,x,y,width));fontRendererObj.drawString(prefix+part,x,y,0x302A20);
                remaining=remaining.substring(part.length());x+=width;
                if(!remaining.isEmpty()&&remaining.charAt(0)=='\n'){remaining=remaining.substring(1);x=36;y+=fontRendererObj.FONT_HEIGHT;}
                else if(part.length()<line.length()){x=36;y+=fontRendererObj.FONT_HEIGHT;}
            }
        }
        BookRun hit=bookRun(mouseX,mouseY);
        if(hit!=null&&hit.component.getChatStyle().getChatHoverEvent()!=null){
            net.minecraft.event.HoverEvent hover=hit.component.getChatStyle().getChatHoverEvent();
            if(hover.getAction()==net.minecraft.event.HoverEvent.Action.SHOW_TEXT)
                drawHoveringText(fontRendererObj.listFormattedStringToWidth(hover.getValue().getFormattedText(),160),mouseX,mouseY,fontRendererObj);
        }
    }
    private static void adaptPageLinks(com.google.gson.JsonElement element){
        if(element.isJsonArray()){for(com.google.gson.JsonElement child:element.getAsJsonArray())adaptPageLinks(child);return;}
        if(!element.isJsonObject())return;com.google.gson.JsonObject object=element.getAsJsonObject();
        if(object.has("clickEvent")&&object.get("clickEvent").isJsonObject()){
            com.google.gson.JsonObject click=object.getAsJsonObject("clickEvent");
            if(click.has("action")&&"change_page".equals(click.get("action").getAsString())&&click.has("value")){
                click.addProperty("action","suggest_command");click.addProperty("value","__htgt6_page__"+click.get("value").getAsString());
            }
        }
        for(java.util.Map.Entry<String,com.google.gson.JsonElement> entry:object.entrySet())adaptPageLinks(entry.getValue());
    }
    private BookRun bookRun(int x,int y){for(BookRun run:bookRuns)if(x>=run.x&&x<run.x+run.w&&y>=run.y&&y<run.y+fontRendererObj.FONT_HEIGHT)return run;return null;}
    private boolean clickBook(int x,int y){
        if(menu.getLecternBook()==null)return false;
        BookRun run=bookRun(x,y);if(run==null)return false;
        net.minecraft.event.ClickEvent click=run.component.getChatStyle().getChatClickEvent();if(click==null)return false;
        String value=click.getValue();
        if(click.getAction()==net.minecraft.event.ClickEvent.Action.SUGGEST_COMMAND&&value.startsWith("__htgt6_page__")){try{
            int page=Integer.parseInt(value.substring("__htgt6_page__".length()))-1;if(page>=0&&page<menu.getLecternPageCount()&&page<=124)mc.playerController.sendEnchantPacket(menu.windowId,3+page);
        }catch(NumberFormatException ignored){}return true;}
        if(click.getAction()==net.minecraft.event.ClickEvent.Action.RUN_COMMAND){mc.thePlayer.sendChatMessage(value);return true;}
        if(click.getAction()==net.minecraft.event.ClickEvent.Action.SUGGEST_COMMAND){mc.displayGuiScreen(new net.minecraft.client.gui.GuiChat(value));return true;}
        if(click.getAction()==net.minecraft.event.ClickEvent.Action.OPEN_URL&&mc.gameSettings.chatLinks){
            try{final java.net.URI uri=new java.net.URI(value);if(!"https".equalsIgnoreCase(uri.getScheme())&&!"http".equalsIgnoreCase(uri.getScheme()))return true;
                mc.displayGuiScreen(new net.minecraft.client.gui.GuiConfirmOpenLink(new net.minecraft.client.gui.GuiYesNoCallback(){
                    public void confirmClicked(boolean yes,int id){if(yes)try{java.awt.Desktop.getDesktop().browse(uri);}catch(Exception ignored){}
                        mc.displayGuiScreen(GuiVillageWorkstation120.this);}
                },value,0,false));
            }catch(java.net.URISyntaxException ignored){}return true;
        }
        return false;
    }
    private static String readableBookPage(String raw){if(raw==null)return "";try{return net.minecraft.util.IChatComponent.Serializer.func_150699_a(raw).getFormattedText();}catch(Exception ignored){return raw;}}
    private ResourceLocation background(){return new ResourceLocation("htgt6","textures/gui/container/village_pillage/"+menu.getType().blockName+".png");}
    private String title(){return StatCollector.translateToLocal("container.village_pillage_"+menu.getType().blockName);}
    private void drawSmithingPreview(int left,int top){
        if(armorPreview==null||armorPreview.worldObj!=mc.theWorld){armorPreview=new com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulArmorStand(mc.theWorld);armorPreview.setNoBasePlate(true);armorPreview.setShowArms(true);}
        for(int slot=0;slot<5;slot++)armorPreview.setCurrentItemOrArmor(slot,null);
        net.minecraft.item.ItemStack stack=menu.getSlot(menu.getType().inputSlots).getStack();if(stack==null)stack=menu.getInput(1);
        if(stack!=null)armorPreview.setCurrentItemOrArmor(stack.getItem() instanceof net.minecraft.item.ItemArmor?4-((net.minecraft.item.ItemArmor)stack.getItem()).armorType:0,stack);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);try{
            net.minecraft.client.gui.inventory.GuiInventory.func_147046_a(left+143,top+73,25,15,0,armorPreview);
        }finally{GL11.glPopAttrib();}
    }
    private void drawMapPreview(int left,int top){
        net.minecraft.item.ItemStack stack=menu.getInput(0);
        if(stack==null||!(stack.getItem() instanceof net.minecraft.item.ItemMap))return;
        net.minecraft.world.storage.MapData data=((net.minecraft.item.ItemMap)stack.getItem()).getMapData(stack,mc.theWorld);
        if(data==null)return;
        net.minecraft.item.ItemStack addition=menu.getInput(1);
        boolean scaling=addition!=null&&addition.getItem()==net.minecraft.init.Items.paper;
        boolean copying=addition!=null&&addition.getItem()==net.minecraft.init.Items.map;
        GL11.glPushMatrix();GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try{
            if(copying)drawRect(left+69,top+17,left+126,top+74,0xFFC6B88A);
            drawRect(left+65,top+13,left+123,top+71,0xFFE7DFC8);
            GL11.glTranslatef(left+(scaling?80:67),top+(scaling?28:15),100);
            float factor=scaling?.21F:.42F;GL11.glScalef(factor,factor,1);
            mc.entityRenderer.getMapItemRenderer().func_148246_a(data);
            mc.entityRenderer.getMapItemRenderer().func_148250_a(data,false);
        }finally{GL11.glPopAttrib();GL11.glPopMatrix();}
    }
    private void drawLoomGrid(int left,int top){
        int choices=menu.getChoiceCount(),selected=menu.getSelected();
        int rows=(choices+COLUMNS-1)/COLUMNS,maxScroll=Math.max(0,rows-VISIBLE_ROWS);
        if(loomScrollRow>maxScroll)loomScrollRow=maxScroll;
        for(int visible=0;visible<COLUMNS*VISIBLE_ROWS;visible++){
            int col=visible%COLUMNS,row=visible/COLUMNS,index=(loomScrollRow+row)*COLUMNS+col;
            int x=left+GRID_X+col*CELL,y=top+GRID_Y+row*CELL;
            if(index>=choices)continue;
            drawWidget("loom/pattern"+(index==selected?"_selected":hovered(x,y,14,14)?"_highlighted":""),x,y,14,14);
            if(index<choices)drawPatternIcon(menu.getLoomPattern(index),x+4,y+1,6,11);
        }
        if(maxScroll==0)drawWidget("loom/scroller_disabled",left+119,top+15,12,15);
        if(maxScroll>0){
            int trackX=left+119,trackY=top+15,trackHeight=54,knobHeight=15;
            
            int knobY=trackY+(trackHeight-knobHeight)*loomScrollRow/maxScroll;
            drawWidget((menu.getType()==VillageWorkstationType120.LOOM?"loom":"stonecutter")+"/scroller",trackX,knobY,12,15);
        }
    }
    private void drawStonecutterGrid(int left,int top){
        int choices=menu.getChoiceCount(),rows=(choices+COLUMNS-1)/COLUMNS,maxScroll=Math.max(0,rows-STONE_ROWS);
        if(stoneScrollRow>maxScroll)stoneScrollRow=maxScroll;
        RenderHelper.enableGUIStandardItemLighting();RenderItem renderer=RenderItem.getInstance();
        for(int visible=0;visible<COLUMNS*STONE_ROWS;visible++){
            int col=visible%COLUMNS,row=visible/COLUMNS,index=(stoneScrollRow+row)*COLUMNS+col;
            int x=left+STONE_GRID_X+col*STONE_CELL_X,y=top+STONE_GRID_Y+row*STONE_CELL_Y;
            if(index>=choices)continue;
            drawWidget("stonecutter/recipe"+(index==menu.getSelected()?"_selected":hovered(x,y,16,18)?"_highlighted":""),x,y,16,18);
            net.minecraft.item.ItemStack stack=menu.getChoiceResult(index);
            if(stack!=null)renderer.renderItemAndEffectIntoGUI(fontRendererObj,mc.getTextureManager(),stack,x,y+1);
        }
        RenderHelper.disableStandardItemLighting();GL11.glColor4f(1,1,1,1);
        if(maxScroll==0)drawWidget("stonecutter/scroller_disabled",left+119,top+15,12,15);
        if(maxScroll>0){int trackX=left+119,trackY=top+15,trackHeight=54,knobHeight=15;
            
            int knobY=trackY+(trackHeight-knobHeight)*stoneScrollRow/maxScroll;
            drawWidget((menu.getType()==VillageWorkstationType120.LOOM?"loom":"stonecutter")+"/scroller",trackX,knobY,12,15);}
    }
    private boolean hovered(int x,int y,int width,int height){return widgetMouseX>=x&&widgetMouseX<x+width&&widgetMouseY>=y&&widgetMouseY<y+height;}
    private void drawWidget(String name,int x,int y,int width,int height){
        GL11.glColor4f(1,1,1,1);mc.getTextureManager().bindTexture(new ResourceLocation("htgt6","textures/gui/container/village_pillage/"+name+".png"));
        func_146110_a(x,y,0,0,width,height,width,height);
    }
    private void drawPatternIcon(String code,int x,int y,int width,int height){
        drawBannerLayer(BANNER_BASE,x,y,width,height,BountifulBannerColors.RGB[menu.getLoomBaseColor()]);
        net.minecraft.nbt.NBTTagList existing=menu.getLoomPatterns();
        for(int i=0;i<existing.tagCount()&&i<6;i++){
            net.minecraft.nbt.NBTTagCompound pattern=existing.getCompoundTagAt(i);
            String oldTexture=TileEntityBountifulBannerRenderer.textureName(pattern.getString("Pattern"));
            if(oldTexture!=null)drawBannerLayer(new ResourceLocation("htgt6",
                    "textures/entity/bountiful/banner/"+oldTexture+".png"),x,y,width,height,
                    BountifulBannerColors.RGB[pattern.getInteger("Color")&15]);
        }
        String texture=TileEntityBountifulBannerRenderer.textureName(code);if(texture!=null)
            drawBannerLayer(new ResourceLocation("htgt6","textures/entity/bountiful/banner/"+texture+".png"),
                    x,y,width,height,BountifulBannerColors.RGB[menu.getLoomDyeColor()]);
    }
    private void drawBannerLayer(ResourceLocation texture,int x,int y,int width,int height,int color){
        mc.getTextureManager().bindTexture(texture);GL11.glColor4f(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F,1F);
        Tessellator t=Tessellator.instance;t.startDrawingQuads();
        float u=20F/64F,v=40F/64F;t.addVertexWithUV(x,y+height,zLevel,0,v);t.addVertexWithUV(x+width,y+height,zLevel,u,v);
        t.addVertexWithUV(x+width,y,zLevel,u,0);t.addVertexWithUV(x,y,zLevel,0,0);t.draw();GL11.glColor4f(1,1,1,1);
    }
}
