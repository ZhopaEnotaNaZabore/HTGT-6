package com.mod.htgt6.client.inventory112;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.RecipeBookRegistry112;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/** Minecraft 1.12 recipe-book surface backed by server-authoritative unlocks. */
final class RecipeBook112 extends Gui {
    private static final ResourceLocation TEXTURE = new ResourceLocation("htgt6", "textures/gui/recipe_book.png");
    private static final int COLUMNS = 5, ROWS = 4;
    private static final int SEARCH=0,EQUIPMENT=1,BUILDING=2,MISC=3,REDSTONE=4,CATEGORY_COUNT=5;
    private Minecraft minecraft;
    private FontRenderer font;
    private final List<IRecipe> recipes = new ArrayList<IRecipe>();
    private final List<IRecipe> filtered = new ArrayList<IRecipe>();
    private boolean open, searchFocused, craftableOnly;
    private String query = "";
    private int page, seenRevision = -1;
    private int category = SEARCH;
    private int seenRegistry = -1, lastTick = Integer.MIN_VALUE;
    private ItemStack[] seenContents = new ItemStack[0];
    private final int gridSize;

    RecipeBook112(int gridSize) {
        this.gridSize = gridSize;
    }

    void initialize(Minecraft minecraft, FontRenderer font) {
        this.minecraft = minecraft;
        this.font = font;
        reloadRecipes();
        refilter();
    }

    private void reloadRecipes() {
        recipes.clear();
        for (RecipeBookRegistry112.Entry entry : RecipeBookRegistry112.entries()) recipes.add(entry.recipe);
        seenRegistry = RecipeBookRegistry112.revision();
        Collections.sort(recipes, new Comparator<IRecipe>() {
            @Override public int compare(IRecipe left, IRecipe right) {
                return left.getRecipeOutput().getDisplayName().compareToIgnoreCase(right.getRecipeOutput().getDisplayName());
            }
        });
    }

    /** Run once per client tick, not once per rendered frame. */
    void refreshContents() {
        if (minecraft != null && minecraft.thePlayer != null) {
            int tick=minecraft.thePlayer.ticksExisted;
            if(lastTick==tick)return;
            lastTick=tick;
        }
        int registry=RecipeBookRegistry112.revision();
        boolean changed=registry!=seenRegistry;
        if(changed)reloadRecipes();
        int unlocks=RecipeBookClientState112.revision();
        changed |= unlocks!=seenRevision;seenRevision=unlocks;
        if(craftableOnly && minecraft!=null && minecraft.thePlayer!=null) {
            java.util.List<ItemStack> contents=new ArrayList<ItemStack>();
            Collections.addAll(contents,minecraft.thePlayer.inventory.mainInventory);
            net.minecraft.inventory.Container container=minecraft.thePlayer.openContainer;
            net.minecraft.inventory.InventoryCrafting grid=container instanceof net.minecraft.inventory.ContainerPlayer
                    ? ((net.minecraft.inventory.ContainerPlayer)container).craftMatrix
                    : container instanceof net.minecraft.inventory.ContainerWorkbench
                    ? ((net.minecraft.inventory.ContainerWorkbench)container).craftMatrix : null;
            if(grid!=null)for(int i=0;i<grid.getSizeInventory();i++)contents.add(grid.getStackInSlot(i));
            boolean inventoryChanged=contents.size()!=seenContents.length;
            for(int i=0;!inventoryChanged && i<contents.size();i++)
                inventoryChanged=!ItemStack.areItemStacksEqual(contents.get(i),seenContents[i]);
            if(inventoryChanged) {
                seenContents=new ItemStack[contents.size()];
                for(int i=0;i<seenContents.length;i++)seenContents[i]=ItemStack.copyItemStack(contents.get(i));
                changed=true;
            }
        }
        if(changed) { int previousPage=page;refilter();page=Math.min(previousPage,Math.max(0,pageCount()-1)); }
    }

    boolean isOpen() { return open; }
    void toggle() { open = !open; searchFocused = false; if (open) refilter(); }

    void draw(int left, int top, int mouseX, int mouseY, float partialTicks) {
        if (!open || minecraft == null) return;
        refreshContents();
        GL11.glColor4f(1, 1, 1, 1);
        minecraft.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(left, top, 1, 1, 147, 166);
        drawCategoryTabs(left, top, mouseX, mouseY);
        drawRect(left + 24, top + 13, left + 106, top + 27, searchFocused ? 0xFF101010 : 0xFF202020);
        String search = query.length() == 0 ? StatCollector.translateToLocal("inventory112.recipe.search") : query;
        String cursor = searchFocused && minecraft.thePlayer.ticksExisted / 6 % 2 == 0 ? "_" : "";
        search = font.trimStringToWidth(search, 76 - font.getStringWidth(cursor), query.length() > 0);
        font.drawString(search + cursor,
                left + 27, top + 16, query.length() == 0 ? 0x777777 : 0xFFFFFF);
        drawToggle(left + 110, top + 12, mouseX, mouseY);
        int first = page * COLUMNS * ROWS;
        RenderHelper.enableGUIStandardItemLighting();
        for (int cell = 0; cell < COLUMNS * ROWS && first + cell < filtered.size(); ++cell) {
            int x = left + 11 + cell % COLUMNS * 25;
            int y = top + 31 + cell / COLUMNS * 25;
            IRecipe recipe = filtered.get(first + cell);
            boolean craftable = RecipeBookClientState112.canCraft(recipe, gridSize);
            drawRecipeButton(x, y, craftable, mouseX, mouseY);
            RenderItem renderer = RenderItem.getInstance();
            renderer.renderItemAndEffectIntoGUI(font, minecraft.getTextureManager(), recipe.getRecipeOutput(), x + 4, y + 4);
            renderer.renderItemOverlayIntoGUI(font, minecraft.getTextureManager(), recipe.getRecipeOutput(), x + 4, y + 4);
        }
        RenderHelper.disableStandardItemLighting();
        if (pageCount() > 1) {
            font.drawString("<", left + 20, top + 143, page > 0 ? 0x404040 : 0x999999);
            font.drawString((page + 1) + "/" + pageCount(), left + 61, top + 143, 0x404040);
            font.drawString(">", left + 123, top + 143, page + 1 < pageCount() ? 0x404040 : 0x999999);
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawCategoryTabs(int left, int top, int mouseX, int mouseY) {
        for (int i = 0; i < CATEGORY_COUNT; ++i) {
            int x = left - 28, y = top + 3 + i * 27;
            boolean selected = category == i;
            boolean hovered = inside(mouseX, mouseY, x, y, 28, 26);
            drawRect(x, y, x + 28, y + 26, selected ? 0xFFB7B7B7 : hovered ? 0xFFA0A0A0 : 0xFF858585);
            drawRect(x, y, x + 28, y + 1, 0xFFFFFFFF);
            drawRect(x, y + 25, x + 28, y + 26, 0xFF373737);
            ItemStack icon = categoryIcon(i);
            RenderHelper.enableGUIStandardItemLighting();
            RenderItem.getInstance().renderItemAndEffectIntoGUI(font, minecraft.getTextureManager(), icon, x + 6, y + 5);
            RenderHelper.disableStandardItemLighting();
        }
    }

    private void drawToggle(int x, int y, int mouseX, int mouseY) {
        boolean hovered = inside(mouseX, mouseY, x, y, 26, 16);
        drawRect(x, y, x + 26, y + 16, hovered ? 0xFFB0B0B0 : 0xFF909090);
        drawRect(x + 1, y + 1, x + 25, y + 15, craftableOnly ? 0xFF5A8E3C : 0xFF5A5A5A);
        font.drawString(craftableOnly ? "+" : "X", x + 9, y + 4, 0xFFFFFF);
    }

    private void drawRecipeButton(int x, int y, boolean craftable, int mouseX, int mouseY) {
        boolean hovered = inside(mouseX, mouseY, x, y, 25, 25);
        int color = craftable ? (hovered ? 0xFFE0E0E0 : 0xFFC6C6C6)
                : (hovered ? 0xFFB87A7A : 0xFF966060);
        drawRect(x, y, x + 25, y + 25, color);
        drawRect(x, y, x + 25, y + 1, 0xFFFFFFFF);
        drawRect(x, y, x + 1, y + 25, 0xFFFFFFFF);
        drawRect(x, y + 24, x + 25, y + 25, 0xFF373737);
        drawRect(x + 24, y, x + 25, y + 25, 0xFF373737);
    }

    boolean mouseClicked(int left, int top, int mouseX, int mouseY, int button) {
        if (mouseX < left - 28 || mouseX >= left + 147 || mouseY < top || mouseY >= top + 166) {
            searchFocused = false;
            return false;
        }
        if (button != 0) return true;
        searchFocused = inside(mouseX, mouseY, left + 24, top + 13, 82, 14);
        for (int i = 0; i < CATEGORY_COUNT; ++i) {
            if (inside(mouseX, mouseY, left - 28, top + 3 + i * 27, 28, 26)) {
                category = i;
                refilter();
                return true;
            }
        }
        if (inside(mouseX, mouseY, left + 110, top + 12, 26, 16)) {
            craftableOnly = !craftableOnly;
            refilter();
            return true;
        }
        if (mouseY >= top + 139 && mouseY < top + 158) {
            if (mouseX < left + 48 && page > 0) page--;
            else if (mouseX > left + 99 && page + 1 < pageCount()) page++;
            return true;
        }
        IRecipe recipe = recipeAt(left, top, mouseX, mouseY);
        if (recipe != null) RecipeBookClientState112.place(recipe);
        return true;
    }

    boolean keyTyped(char character, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) { open = false; return true; }
        if (!searchFocused) return false;
        if (keyCode == Keyboard.KEY_BACK && query.length() > 0) {
            query = query.substring(0, query.length() - 1);
            refilter();
            return true;
        }
        if (query.length() < 50 && ChatAllowedCharacters.isAllowedCharacter(character) && !Character.isISOControl(character)) {
            query += character;
            refilter();
        }
        return true;
    }

    ItemStack hoveredOutput(int left, int top, int mouseX, int mouseY) {
        IRecipe recipe = recipeAt(left, top, mouseX, mouseY);
        return recipe == null ? null : recipe.getRecipeOutput();
    }

    String hoveredControl(int left, int top, int mouseX, int mouseY) {
        if (inside(mouseX, mouseY, left + 110, top + 12, 26, 16))
            return StatCollector.translateToLocal("inventory112.recipe.craftable");
        for (int i = 0; i < CATEGORY_COUNT; ++i)
            if (inside(mouseX, mouseY, left - 28, top + 3 + i * 27, 28, 26))
                return StatCollector.translateToLocal("inventory112.recipe.category." + categoryName(i));
        return null;
    }

    private void refilter() {
        filtered.clear();
        String wanted = query.toLowerCase(java.util.Locale.ROOT);
        for (IRecipe recipe : recipes) {
            if (!RecipeBookClientState112.isUnlocked(recipe)) continue;
            RecipeBookRegistry112.Entry entry = RecipeBookRegistry112.byRecipe(recipe);
            if (entry == null || !entry.fits(gridSize)) continue;
            if (category != SEARCH && categoryOf(recipe) != category) continue;
            if (craftableOnly && !RecipeBookClientState112.canCraft(recipe, gridSize)) continue;
            if (wanted.length() > 0 && !recipe.getRecipeOutput().getDisplayName().toLowerCase(java.util.Locale.ROOT).contains(wanted)) continue;
            filtered.add(recipe);
        }
        page = 0;
    }

    private int categoryOf(IRecipe recipe) {
        Item item = recipe.getRecipeOutput().getItem();
        if (item instanceof ItemArmor || item instanceof ItemSword || item instanceof ItemTool || item instanceof ItemBow || item instanceof com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow)
            return EQUIPMENT;
        if (item.getCreativeTab() == CreativeTabs.tabRedstone) return REDSTONE;
        if (item instanceof ItemBlock) return BUILDING;
        return MISC;
    }

    private static ItemStack categoryIcon(int value) {
        switch (value) {
            case SEARCH:return new ItemStack(Items.compass);
            case EQUIPMENT:return new ItemStack(Items.iron_axe);
            case BUILDING:return new ItemStack(Blocks.brick_block);
            case REDSTONE:return new ItemStack(Items.redstone);
            default: return new ItemStack(Items.lava_bucket);
        }
    }
    private static String categoryName(int value){switch(value){case SEARCH:return "search";case EQUIPMENT:return "equipment";case BUILDING:return "building";case REDSTONE:return "redstone";default:return "misc";}}

    private IRecipe recipeAt(int left, int top, int mouseX, int mouseY) {
        int localX = mouseX - (left + 11), localY = mouseY - (top + 31);
        if (localX < 0 || localY < 0) return null;
        int column = localX / 25, row = localY / 25;
        if (column < 0 || column >= COLUMNS || row < 0 || row >= ROWS) return null;
        int index = page * COLUMNS * ROWS + row * COLUMNS + column;
        return index >= 0 && index < filtered.size() ? filtered.get(index) : null;
    }

    private int pageCount() { return Math.max(1, (filtered.size() + COLUMNS * ROWS - 1) / (COLUMNS * ROWS)); }
    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }
}
