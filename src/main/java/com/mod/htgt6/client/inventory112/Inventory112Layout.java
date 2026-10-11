package com.mod.htgt6.client.inventory112;

import com.mod.htgt6.asm.HTGT6Config;

/** Responsive screen placement backported from the 1.12 recipe-book screens. */
final class Inventory112Layout {
    static final int INVENTORY_WIDTH = 176;
    static final int INVENTORY_HEIGHT = 166;
    static final int RECIPE_WIDTH = 147;
    static final int COMBINED_WIDTH = INVENTORY_WIDTH + RECIPE_WIDTH + 1;
    static final int SIDE_LAYOUT_MIN_WIDTH_112 = 379;

    private final int inventoryLeft;
    private final int top;
    private final int recipeLeft;
    private final boolean overlay;

    private Inventory112Layout(int inventoryLeft, int top, int recipeLeft, boolean overlay) {
        this.inventoryLeft = inventoryLeft;
        this.top = top;
        this.recipeLeft = recipeLeft;
        this.overlay = overlay;
    }

    static Inventory112Layout calculate(int screenWidth, int screenHeight, boolean recipeOpen) {
        int margin = Math.max(0, HTGT6Config.INVENTORY_112_UI_SAFE_MARGIN);
        // GuiInventory 1.12 uses widthTooNarrow = width < 379; that allowance
        // includes the five category tabs protruding from the 147 px book.
        boolean sideFits = screenWidth >= SIDE_LAYOUT_MIN_WIDTH_112 + margin * 2;
        // Newer screens use an available-area test. AUTO keeps the book beside the inventory
        // whenever the complete 324 px surface fits, and overlays only when it actually cannot.
        // A wide 1.12 inventory never overlays its recipe book. Keep the config
        // only for backwards compatibility; safe layout is determined by space.
        boolean overlay = recipeOpen && !sideFits;

        int top = clamp((screenHeight - INVENTORY_HEIGHT) / 2, margin,
                Math.max(margin, screenHeight - INVENTORY_HEIGHT - margin));
        int inventoryLeft;
        int recipeLeft;
        if (recipeOpen && !overlay) {
            // Exact 1.12 centering: recipe panel centre -86, inventory centre +77.
            inventoryLeft = (screenWidth - INVENTORY_WIDTH) / 2 + 77;
            recipeLeft = (screenWidth - RECIPE_WIDTH) / 2 - 86;
            int min = Math.min(recipeLeft, inventoryLeft);
            int max = Math.max(recipeLeft + RECIPE_WIDTH, inventoryLeft + INVENTORY_WIDTH);
            int correction = min < margin ? margin - min
                    : max > screenWidth - margin ? screenWidth - margin - max : 0;
            inventoryLeft += correction;
            recipeLeft += correction;
        } else {
            inventoryLeft = clamp((screenWidth - INVENTORY_WIDTH) / 2, margin,
                    Math.max(margin, screenWidth - INVENTORY_WIDTH - margin));
            recipeLeft = clamp((screenWidth - RECIPE_WIDTH) / 2, margin,
                    Math.max(margin, screenWidth - RECIPE_WIDTH - margin));
        }
        return new Inventory112Layout(inventoryLeft, top, recipeLeft, overlay);
    }

    int inventoryLeft() { return inventoryLeft; }
    int top() { return top; }
    int recipeLeft() { return recipeLeft; }
    boolean overlay() { return overlay; }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
