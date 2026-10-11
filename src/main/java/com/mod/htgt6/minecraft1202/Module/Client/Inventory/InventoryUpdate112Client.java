package com.mod.htgt6.minecraft1202.Module.Client.Inventory;

import com.mod.htgt6.client.inventory112.SavedHotbars112;
import com.mod.htgt6.client.inventory112.RecipeToast112;
import com.mod.htgt6.client.inventory112.TutorialSystem112;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.MinecraftForge;

@SideOnly(Side.CLIENT)
public final class InventoryUpdate112Client {
    public static void init() {
        SavedHotbars112.init();
        EntityBackportValidator.texture("textures/gui/container/inventory_1_12.png", 256, 256);
        EntityBackportValidator.texture("textures/gui/container/crafting_table_1_12.png", 256, 256);
        EntityBackportValidator.texture("textures/gui/container/creative_inventory/tabs.png", 256, 256);
        EntityBackportValidator.texture("textures/gui/container/creative_inventory/tab_inventory.png", 256, 256);
        EntityBackportValidator.texture("textures/gui/container/creative_inventory/tab_item_search.png", 256, 256);
        EntityBackportValidator.texture("textures/gui/container/creative_inventory/tab_items.png", 256, 256);
        EntityBackportValidator.texture("textures/gui/recipe_book.png", 256, 256);
        EntityBackportValidator.texture("textures/gui/toasts.png", 256, 256);
        EntityBackportValidator.texture("textures/gui/widgets_1_12.png", 256, 256);
        RecipeToast112 toasts = new RecipeToast112();
        MinecraftForge.EVENT_BUS.register(toasts);
        FMLCommonHandler.instance().bus().register(toasts);
        FMLCommonHandler.instance().bus().register(new TutorialSystem112());
    }

    private InventoryUpdate112Client() { }
}
