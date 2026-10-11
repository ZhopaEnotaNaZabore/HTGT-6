package com.mod.htgt6.client.nei;

import codechicken.nei.NEIClientConfig;
import codechicken.nei.event.NEIRegisterHandlerInfosEvent;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.HandlerInfo;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import java.util.List;
import java.util.Map;

/** Per-screen sidebar, not a global catalyst registration shared by all structures. */
public final class StructurePreviewNEIHooks implements IContainerTooltipHandler {
    private static boolean registered;
    public static void register() {
        if(registered) return;
        registered=true;
        StructurePreviewNEIHooks hooks=new StructurePreviewNEIHooks();
        MinecraftForge.EVENT_BUS.register(hooks);
        GuiContainerManager.addTooltipHandler(hooks);
        GuiRecipeTab.handlerAdderFromIMC.put(StructureLibNEIHandler.class.getName(),info());
        GuiRecipeTab.handlerMap.put(StructureLibNEIHandler.class.getName(),info());
    }
    private static HandlerInfo info() {
        HandlerInfo info=new HandlerInfo.Builder(StructureLibNEIHandler.class,"HiTech6","htgt6")
                .setWidth(166).setHeight(512).setMaxRecipesPerPage(1).setUseCustomScroll(true).build();
        info.setShowFavoritesButton(false);
        info.setShowOverlayButton(false);
        return info;
    }
    @SubscribeEvent public void registerInfo(NEIRegisterHandlerInfosEvent event) { event.registerHandlerInfo(info()); }
    @SubscribeEvent public void beforeDraw(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if(event.gui instanceof GuiRecipe) {
            GuiRecipe<?> gui=(GuiRecipe<?>)event.gui;
            if(gui.getHandler() instanceof StructureLibNEIHandler)
                ((StructureLibNEIHandler)gui.getHandler()).syncSidebar(gui);
        }
    }
    @SubscribeEvent public void afterDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        // Materials remain available even when the user disabled JEI-style catalyst tabs.
        if(event.gui instanceof GuiRecipe && (!NEIClientConfig.areJEIStyleTabsVisible()
                || !NEIClientConfig.areJEIStyleRecipeCatalystsVisible())) {
            GuiRecipe<?> gui=(GuiRecipe<?>)event.gui;
            if(gui.getHandler() instanceof StructureLibNEIHandler) gui.getRecipeCatalystWidget().draw(event.mouseX,event.mouseY);
        }
    }
    @Override public List<String> handleItemTooltip(GuiContainer screen,ItemStack stack,int x,int y,List<String> lines) {
        if(screen instanceof GuiRecipe) {
            GuiRecipe<?> gui=(GuiRecipe<?>)screen;
            if(gui.getHandler() instanceof StructureLibNEIHandler)
                ((StructureLibNEIHandler)gui.getHandler()).appendSidebarTooltip(gui,x,y,lines);
        }
        return lines;
    }
    @Override public List<String> handleTooltip(GuiContainer gui,int x,int y,List<String> lines) { return lines; }
    @Override public List<String> handleItemDisplayName(GuiContainer gui,ItemStack stack,List<String> lines) { return lines; }
    @Override public Map<String,String> handleHotkeys(GuiContainer gui,int x,int y,Map<String,String> keys) { return keys; }
}
