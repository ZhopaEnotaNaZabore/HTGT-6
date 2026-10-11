package com.mod.htgt6.orehelper.client;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.recipe.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import java.awt.Point;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/** NEI's compact recipe page is a gateway to the wide standalone vanilla GUI. */
public final class OreHelperNEI extends TemplateRecipeHandler {
    private static boolean registered;
    private final FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
    private int originX, originY, renderedRecipe = -1;
    public static void register() {
        if (registered) return;
        registered = true;
        API.registerRecipeHandler(new OreHelperNEI());
        API.registerUsageHandler(new OreHelperNEI());
    }
    public static int material(ItemStack stack) {
        return com.mod.htgt6.orehelper.OreCatalog.material(stack);
    }
    public String getRecipeName() { return I18n.format("ht6.ore.title"); }
    public String getGuiTexture() { return "minecraft:textures/gui/container/inventory.png"; }
    public String getOverlayIdentifier() { return "hitech6.ore.worldgen"; }
    public int recipiesPerPage() { return 1; }
    @Override public int getRecipeHeight(int recipe) { return 90; }
    public void loadCraftingRecipes(ItemStack result) {
        int id = material(result);
        if (id > 0) arecipes.add(new OrePage(result, id));
    }
    public void loadUsageRecipes(ItemStack ingredient) { loadCraftingRecipes(ingredient); }
    public void drawBackground(int recipe) {
        matrix.clear(); GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrix);
        originX = Math.round(matrix.get(12)); originY = Math.round(matrix.get(13)); renderedRecipe = recipe;
        Gui.drawRect(4, 35, 162, 60, 0xff555555);
        Gui.drawRect(5, 36, 161, 59, 0xffa0a0a0);
    }
    public void drawForeground(int recipe) {
        GuiDraw.drawStringC(I18n.format("ht6.ore.open"), 83, 44, 0xffffff, false);
        GuiDraw.drawStringC(I18n.format("ht6.ore.server"), 83, 72, 0x404040, false);
    }
    public boolean mouseClicked(GuiRecipe<?> gui, int button, int recipe) {
        Point mouse = GuiDraw.getMousePosition();
        if (button == 0 && renderedRecipe == recipe && mouse != null && mouse.x >= originX + 4 && mouse.x < originX + 162 && mouse.y >= originY + 35 && mouse.y < originY + 60) {
            Minecraft.getMinecraft().displayGuiScreen(new OreHelperScreen(gui, ((OrePage) arecipes.get(recipe)).material));
            return true;
        }
        return false;
    }
    private final class OrePage extends CachedRecipe {
        final int material;
        final PositionedStack ore;
        OrePage(ItemStack stack, int material) { this.material = material; ore = new PositionedStack(stack.copy(), 75, 9); }
        public PositionedStack getResult() { return ore; }
    }
}
