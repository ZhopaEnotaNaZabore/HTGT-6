package com.mod.htgt6.client.nei;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.GuiRecipeCatalyst;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.mod.htgt6.client.nei.structure.HTGT6MultiblockRegistry;
import com.mod.htgt6.client.nei.structure.HTGT6MultiblockStructure;
import com.mod.htgt6.client.nei.structure.StructurePreviewMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.glu.GLU;

import java.awt.Point;
import java.awt.Rectangle;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Controller-linked structure viewer. Geometry and per-layer bills of materials are cached once. */
public final class StructureLibNEIHandler extends TemplateRecipeHandler {
    public static final String OVERLAY_ID = "htgt6.structurelib.multiblock";
    private static final int WIDTH=166;
    private final RenderBlocks renderer=new RenderBlocks();
    private final FloatBuffer model=BufferUtils.createFloatBuffer(16), projection=BufferUtils.createFloatBuffer(16);
    private final FloatBuffer projected=BufferUtils.createFloatBuffer(3);
    private final IntBuffer viewport=BufferUtils.createIntBuffer(16), oldScissor=BufferUtils.createIntBuffer(4);
    private final double[] rayStart=new double[3],rayEnd=new double[3];
    private final Rectangle preview=new Rectangle();
    private int originX,originY,viewHeight=220;
    private CachedMultiblock rendered;
    private Cell hovered;
    private boolean dragging;
    private Point lastMouse;

    private static final class Cell {
        final int x,y,z;
        final HTGT6MultiblockStructure.StructureBlock block;
        final ItemStack stack;
        Cell(int x,int y,int z,HTGT6MultiblockStructure.StructureBlock block,ItemStack stack) {
            this.x=x; this.y=y; this.z=z; this.block=block; this.stack=stack;
        }
    }

    public final class CachedMultiblock extends CachedRecipe {
        final HTGT6MultiblockStructure structure;
        final PositionedStack controller;
        final List<Cell> cells=new ArrayList<Cell>();
        final List<List<Cell>> layers=new ArrayList<List<Cell>>();
        final List<List<PositionedStack>> materials=new ArrayList<List<PositionedStack>>();
        int layer=-1;
        float yaw=35,pitch=-18,zoom=1;
        boolean manual;
        CachedMultiblock(HTGT6MultiblockStructure structure) {
            this.structure=structure;
            controller=new PositionedStack(structure.getController(),146,4);
            for(int y=0;y<structure.getHeight();y++) {
                List<Cell> row=new ArrayList<Cell>();
                for(int x=0;x<structure.getWidth();x++) for(int z=0;z<structure.getDepth();z++) {
                    HTGT6MultiblockStructure.StructureBlock block=structure.getBlock(x,y,z);
                    if(block==null || block.block==null) continue;
                    ItemStack stack=x==structure.getControllerX() && y==structure.getControllerY() && z==structure.getControllerZ()
                            ? structure.getController().copy() : new ItemStack(block.block,1,block.metadata);
                    Cell cell=new Cell(x,y,z,block,stack);
                    cells.add(cell); row.add(cell);
                }
                layers.add(Collections.unmodifiableList(row));
            }
            materials.add(count(cells));
            for(List<Cell> row:layers) materials.add(count(row));
        }
        List<Cell> visible() { return layer<0?cells:layers.get(layer); }
        List<PositionedStack> sidebar() { return materials.get(layer+1); }
        @Override public List<PositionedStack> getIngredients() { return Collections.emptyList(); }
        @Override public PositionedStack getResult() { return controller; }
    }

    private static List<PositionedStack> count(List<Cell> cells) {
        List<ItemStack> totals=new ArrayList<ItemStack>();
        for(Cell cell:cells) {
            ItemStack total=null;
            for(ItemStack candidate:totals) if(candidate.getItem()==cell.stack.getItem()
                    && candidate.getItemDamage()==cell.stack.getItemDamage()
                    && ItemStack.areItemStackTagsEqual(candidate,cell.stack)) { total=candidate; break; }
            if(total==null) { total=cell.stack.copy(); total.stackSize=0; totals.add(total); }
            total.stackSize++;
        }
        List<PositionedStack> result=new ArrayList<PositionedStack>();
        for(ItemStack stack:totals) result.add(new PositionedStack(stack,0,0));
        return Collections.unmodifiableList(result);
    }

    @Override public String getRecipeName() { return tr("name"); }
    @Override public String getGuiTexture() { return "minecraft:textures/gui/container/inventory.png"; }
    @Override public String getOverlayIdentifier() { return OVERLAY_ID; }
    @Override public int recipiesPerPage() { return 1; }
    @Override public int getRecipeHeight(int index) { return viewHeight; }

    @Override public void loadCraftingRecipes(String outputId,Object... results) {
        if(!OVERLAY_ID.equals(outputId)) { super.loadCraftingRecipes(outputId,results); return; }
        if(results!=null && results.length>0 && results[0] instanceof ItemStack) {
            loadCraftingRecipes((ItemStack)results[0]); return;
        }
        for(HTGT6MultiblockStructure structure:HTGT6MultiblockRegistry.all()) arecipes.add(new CachedMultiblock(structure));
    }
    @Override public void loadCraftingRecipes(ItemStack result) {
        for(HTGT6MultiblockStructure structure:HTGT6MultiblockRegistry.all())
            if(HTGT6MultiblockRegistry.same(structure.getController(),result)) arecipes.add(new CachedMultiblock(structure));
    }
    @Override public void loadUsageRecipes(ItemStack ingredient) {
        for(HTGT6MultiblockStructure structure:HTGT6MultiblockRegistry.all())
            if(HTGT6MultiblockRegistry.same(structure.getController(),ingredient)) return;
        for(HTGT6MultiblockStructure structure:HTGT6MultiblockRegistry.all())
            if(HTGT6MultiblockRegistry.contains(structure,ingredient)) arecipes.add(new CachedMultiblock(structure));
    }

    /** Called before NEI draws the native catalyst sidebar, after its normal refresh. */
    public void syncSidebar(GuiRecipe<?> gui) {
        List<Integer> indices=gui.getRecipeIndices();
        if(indices.isEmpty()) return;
        int index=indices.get(0);
        if(index<0 || index>=arecipes.size()) return;
        CachedMultiblock recipe=(CachedMultiblock)arecipes.get(index);
        GuiRecipeCatalyst sidebar=gui.getRecipeCatalystWidget();
        // Keep the right edge anchored when the list changes its number of columns.
        int right=sidebar.x+sidebar.w;
        sidebar.setCatalysts(recipe.sidebar());
        sidebar.update();
        sidebar.x=right-sidebar.w;
        viewHeight=Math.max(100,Math.min(512,sidebar.getAvailableHeight()));
    }

    @Override public void drawBackground(int index) {
        CachedMultiblock recipe=(CachedMultiblock)arecipes.get(index);
        if(rendered!=recipe) { rendered=recipe; dragging=false; lastMouse=null; hovered=null; }
        captureOrigin();
        preview.setBounds(originX+3,originY+28,WIDTH-6,viewHeight-53);
        GuiDraw.drawGradientRect(0,0,WIDTH,viewHeight,0xEE12161C,0xEE202A34);
        GuiDraw.drawRect(0,viewHeight-1,WIDTH,1,0xFF49BFD3);
        renderStructure(recipe);
    }
    @Override public void drawExtras(int index) {
        CachedMultiblock recipe=(CachedMultiblock)arecipes.get(index);
        Minecraft mc=Minecraft.getMinecraft();
        String title=recipe.structure.getController().getDisplayName();
        GuiDraw.drawString(mc.fontRenderer.trimStringToWidth(title,137),4,4,0x7FEFFF,true);
        GuiDraw.drawString(recipe.structure.getWidth()+"x"+recipe.structure.getHeight()+"x"+recipe.structure.getDepth()
                +"  "+recipe.visible().size()+" "+tr("blocks_short"),4,16,0xAFC3D0,false);
        int controls=viewHeight-21;
        button(25,controls,16,"<");
        button(125,controls,16,">");
        GuiDraw.drawRect(43,controls,80,16,0xCC26323D);
        String layer=layerName(recipe);
        GuiDraw.drawString(layer,83-GuiDraw.getStringWidth(layer)/2,controls+4,0x7FEFFF,false);
    }
    private static void button(int x,int y,int width,String text) {
        GuiDraw.drawRect(x,y,width,16,0xCC32404C);
        GuiDraw.drawString(text,x+(width-GuiDraw.getStringWidth(text))/2,y+4,0xFFFFFF,false);
    }
    private static String tr(String suffix) { return StatCollector.translateToLocal("nei.htgt6.multiblock_structure."+suffix); }
    private static String layerName(CachedMultiblock recipe) {
        return recipe.layer<0?tr("all_layers"):StatCollector.translateToLocalFormatted(
                "nei.htgt6.multiblock_structure.layer",recipe.layer+1);
    }
    @Override public List<String> handleTooltip(GuiRecipe<?> gui,List<String> tooltip,int index) {
        Point mouse=GuiDraw.getMousePosition();
        if(rendered!=arecipes.get(index) || hovered==null || mouse==null || !preview.contains(mouse)) return tooltip;
        tooltip.add(EnumChatFormatting.AQUA+hovered.stack.getDisplayName());
        tooltip.add(EnumChatFormatting.GRAY+tr("position")+": "
                +(hovered.x-rendered.structure.getControllerX())+", "
                +(hovered.y-rendered.structure.getControllerY())+", "
                +(hovered.z-rendered.structure.getControllerZ()));
        return tooltip;
    }
    public void appendSidebarTooltip(GuiRecipe<?> gui,int mx,int my,List<String> tooltip) {
        PositionedStack stack=gui.getRecipeCatalystWidget().getPositionedStackMouseOver(mx,my);
        if(stack==null || gui.getRecipeIndices().isEmpty()) return;
        CachedMultiblock recipe=(CachedMultiblock)arecipes.get(gui.getRecipeIndices().get(0));
        tooltip.add(EnumChatFormatting.AQUA+tr("required")+": "+stack.item.stackSize+" ("+layerName(recipe)+")");
    }
    @Override public boolean mouseClicked(GuiRecipe<?> gui,int button,int index) {
        CachedMultiblock recipe=(CachedMultiblock)arecipes.get(index);
        Point mouse=GuiDraw.getMousePosition();
        if(rendered!=recipe || mouse==null) return false;
        // drawBackground captured the actual screen origin, including NEI scrolling.
        int lx=mouse.x-originX,ly=mouse.y-originY;
        if(button==0 && ly>=viewHeight-21 && ly<viewHeight-5) {
            int direction=lx>=25 && lx<41?-1:lx>=125 && lx<141?1:0;
            if(direction!=0 || lx>=43 && lx<123) {
                recipe.layer=direction==0?-1:StructurePreviewMath.nextLayer(recipe.layer,recipe.structure.getHeight(),direction);
                hovered=null; dragging=false; lastMouse=null;
                syncSidebar(gui);
                return true;
            }
        }
        if(preview.contains(mouse)) {
            if(button==0) { dragging=true; recipe.manual=true; lastMouse=mouse; return true; }
            if(button==1) { recipe.zoom=1; recipe.pitch=-18; recipe.yaw=35; recipe.manual=false; return true; }
        }
        return super.mouseClicked(gui,button,index);
    }
    @Override public boolean mouseScrolled(GuiRecipe<?> gui,int scroll,int index) {
        Point mouse=GuiDraw.getMousePosition();
        if(rendered!=arecipes.get(index) || mouse==null || !preview.contains(mouse)) return false;
        rendered.zoom=Math.max(.5F,Math.min(3F,rendered.zoom+(scroll>0?.1F:-.1F)));
        return true;
    }
    private void updateDrag(CachedMultiblock recipe) {
        Point mouse=GuiDraw.getMousePosition();
        if(!Mouse.isButtonDown(0)) { dragging=false; lastMouse=null; return; }
        if(dragging && lastMouse!=null && mouse!=null) {
            recipe.yaw+=(mouse.x-lastMouse.x)*1.15F;
            recipe.pitch=Math.max(-75,Math.min(75,recipe.pitch+(mouse.y-lastMouse.y)*.9F));
            lastMouse=mouse;
        }
    }
    private void matrices() {
        model.clear(); projection.clear(); viewport.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,model);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX,projection);
        GL11.glGetInteger(GL11.GL_VIEWPORT,viewport);
    }
    private void captureOrigin() {
        matrices(); projected.clear();
        Minecraft mc=Minecraft.getMinecraft();
        ScaledResolution scaled=new ScaledResolution(mc,mc.displayWidth,mc.displayHeight);
        if(GLU.gluProject(0,0,0,model,projection,viewport,projected)) {
            originX=Math.round(projected.get(0)/scaled.getScaleFactor());
            originY=Math.round((mc.displayHeight-projected.get(1))/scaled.getScaleFactor());
        }
    }
    private boolean ray(Point mouse,int factor,int displayHeight) {
        if(mouse==null || !preview.contains(mouse)) return false;
        projected.clear();
        if(!GLU.gluUnProject(mouse.x*factor,displayHeight-mouse.y*factor,0,model,projection,viewport,projected)) return false;
        for(int i=0;i<3;i++) rayStart[i]=projected.get(i);
        projected.clear();
        if(!GLU.gluUnProject(mouse.x*factor,displayHeight-mouse.y*factor,1,model,projection,viewport,projected)) return false;
        for(int i=0;i<3;i++) rayEnd[i]=projected.get(i);
        return true;
    }
    private void renderStructure(CachedMultiblock recipe) {
        Minecraft mc=Minecraft.getMinecraft();
        ScaledResolution scaled=new ScaledResolution(mc,mc.displayWidth,mc.displayHeight);
        int factor=scaled.getScaleFactor(),unit=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        float lightX=OpenGlHelper.lastBrightnessX,lightY=OpenGlHelper.lastBrightnessY;
        updateDrag(recipe);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            // Intersect the model area with NEI's own scroll clipping.
            int sx=preview.x*factor,sy=mc.displayHeight-(preview.y+preview.height)*factor;
            int ex=sx+preview.width*factor,ey=sy+preview.height*factor;
            if(GL11.glIsEnabled(GL11.GL_SCISSOR_TEST)) {
                oldScissor.clear(); GL11.glGetInteger(GL11.GL_SCISSOR_BOX,oldScissor);
                sx=Math.max(sx,oldScissor.get(0)); sy=Math.max(sy,oldScissor.get(1));
                ex=Math.min(ex,oldScissor.get(0)+oldScissor.get(2)); ey=Math.min(ey,oldScissor.get(1)+oldScissor.get(3));
            }
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(sx,sy,Math.max(0,ex-sx),Math.max(0,ey-sy));
            GL11.glEnable(GL11.GL_DEPTH_TEST); GL11.glDepthMask(true);
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            RenderHelper.enableStandardItemLighting();
            GL11.glColor4f(1,1,1,1);
            float scale=StructurePreviewMath.fit(recipe.structure.getWidth(),recipe.layer<0?recipe.structure.getHeight():1,
                    recipe.structure.getDepth(),preview.width,preview.height,recipe.pitch)*recipe.zoom;
            GL11.glTranslatef(WIDTH/2F,28+preview.height/2F,120);
            GL11.glScalef(scale,-scale,scale);
            GL11.glRotatef(recipe.pitch,1,0,0);
            GL11.glRotatef(recipe.yaw+(recipe.manual?0:cycleticks*.25F),0,1,0);
            GL11.glTranslatef(-(recipe.structure.getWidth()-1)/2F,
                    -(recipe.layer<0?(recipe.structure.getHeight()-1)/2F:recipe.layer),
                    -(recipe.structure.getDepth()-1)/2F);
            matrices();
            hovered=null;
            if(ray(GuiDraw.getMousePosition(),factor,mc.displayHeight)) {
                double closest=Double.POSITIVE_INFINITY;
                for(Cell cell:recipe.visible()) {
                    double hit=StructurePreviewMath.hit(rayStart,rayEnd,cell.x,cell.y,cell.z);
                    if(hit<closest) { closest=hit; hovered=cell; }
                }
            }
            for(int glassPass=0;glassPass<2;glassPass++) {
            GL11.glDepthMask(glassPass==0);
            for(Cell cell:recipe.visible()) {
                boolean glass = cell.block.block instanceof com.mod.htgt6.common.multiblock.glass.BlockObservationGlass;
                if(glass != (glassPass==1)) continue;
                GL11.glPushMatrix();
                try {
                    GL11.glTranslatef(cell.x,cell.y,cell.z);
                    GL11.glColor4f(cell==hovered?.55F:1F,1,1,1);
                    if(glass) com.mod.htgt6.client.nei.structure.ObservationGlassPreview.render(renderer,
                            recipe.structure,recipe.layer,cell.x,cell.y,cell.z,
                            (com.mod.htgt6.common.multiblock.glass.BlockObservationGlass)cell.block.block,cell.block.metadata);
                    else renderer.renderBlockAsItem(cell.block.block,cell.block.metadata,1);
                } finally { GL11.glPopMatrix(); }
            }
            }
        } finally {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,lightX,lightY);
            GL11.glPopMatrix(); GL11.glPopAttrib();
            OpenGlHelper.setActiveTexture(unit);
        }
    }
}
