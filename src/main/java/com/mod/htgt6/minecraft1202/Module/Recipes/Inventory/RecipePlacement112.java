package com.mod.htgt6.minecraft1202.Module.Recipes.Inventory;

import java.util.Arrays;
import net.minecraft.item.ItemStack;

/** Side-independent, non-mutating plan; only a fully validated plan may be committed. */
public final class RecipePlacement112 {
    public static final class Plan {
        public final ItemStack[] inventory, grid;
        private Plan(ItemStack[] inventory, ItemStack[] grid) { this.inventory=inventory; this.grid=grid; }
    }

    public static Plan plan(ItemStack[] inventory, ItemStack[] grid, RecipeBookRegistry112.Entry recipe,
            int size, boolean all) {
        return plan(inventory,grid,recipe,size,all,false);
    }

    public static Plan plan(ItemStack[] inventory, ItemStack[] grid, RecipeBookRegistry112.Entry recipe,
            int size, boolean all, boolean matchesCurrentGrid) {
        if (!recipe.fits(size) || grid.length != size*size) return null;
        ItemStack[] returned=copy(inventory);
        for (ItemStack stack:grid) if (stack!=null && !insert(returned,stack.copy())) return null;
        int target=1;
        if(matchesCurrentGrid) {
            int smallest=64,largest=0;
            for(ItemStack stack:grid)if(stack!=null&&stack.stackSize>0) {
                if(stack.stackSize>=Math.min(64,stack.getMaxStackSize()))return null;
                smallest=Math.min(smallest,stack.stackSize);largest=Math.max(largest,stack.stackSize);
            }
            // The reference first checks that every occupied slot can grow, even for Shift.
            if(largest==0||chooseSources(returned,recipe,largest+1)==null)return null;
            target=smallest+1;
        }
        for (int amount=all?64:target;amount>0;amount--) {
            int[] selected=chooseSources(returned,recipe,amount);
            if(selected==null) { if(!all)return null; continue; }
            ItemStack[] remaining=copy(returned), placed=new ItemStack[grid.length];
            for(int i=0;i<selected.length;i++) if(selected[i]>=0) {
                ItemStack source=returned[selected[i]];
                ItemStack part=source.copy();part.stackSize=amount;
                placed[recipe.targetSlot(i,size)]=part;
                int needed=amount;
                for(int slot=0;slot<remaining.length && needed>0;slot++) if(same(remaining[slot],source)) {
                    int taken=Math.min(needed,remaining[slot].stackSize);
                    remaining[slot].stackSize-=taken;needed-=taken;
                    if(remaining[slot].stackSize==0)remaining[slot]=null;
                }
            }
            return new Plan(remaining,placed);
        }
        return null;
    }

    public static int[] chooseSources(ItemStack[] inventory, RecipeBookRegistry112.Entry recipe, int amount) {
        if(amount<1 || amount>64 || recipe.ingredients.size()>9 || recipe.hasUnresolvedIngredients) return null;
        // A target stack may be assembled from several equal stacks in the inventory.
        ItemStack[] pooled=copy(inventory);
        for(int i=0;i<pooled.length;i++) if(pooled[i]!=null)
            for(int j=i+1;j<pooled.length;j++) if(same(pooled[i],pooled[j])) {
                pooled[i].stackSize+=pooled[j].stackSize;pooled[j]=null;
            }
        int[] selected=new int[recipe.ingredients.size()];Arrays.fill(selected,-1);
        int[] used=new int[inventory.length];
        for(int i=0;i<selected.length;i++) if(!recipe.ingredients.get(i).isEmpty()
                && !assign(i,pooled,recipe,amount,selected,used,new boolean[inventory.length])) return null;
        return selected;
    }

    // Capacitated bipartite matching: move an earlier wildcard to another source if necessary.
    private static boolean assign(int ingredient,ItemStack[] inventory,RecipeBookRegistry112.Entry recipe,
            int amount,int[] selected,int[] used,boolean[] visited) {
        for(int slot=0;slot<inventory.length;slot++) {
            ItemStack stack=inventory[slot];
            if(visited[slot] || stack==null || stack.stackSize<amount || stack.getMaxStackSize()<amount)continue;
            boolean matches=false;
            for(ItemStack choice:recipe.ingredients.get(ingredient))
                if(RecipeBookRegistry112.matches(stack,choice)){matches=true;break;}
            if(!matches)continue;
            visited[slot]=true;
            if(used[slot]<stack.stackSize/amount) {selected[ingredient]=slot;used[slot]++;return true;}
            for(int previous=0;previous<selected.length;previous++) if(selected[previous]==slot
                    && assign(previous,inventory,recipe,amount,selected,used,visited)) {
                selected[ingredient]=slot;return true;
            }
        }
        return false;
    }

    private static boolean insert(ItemStack[] inventory,ItemStack stack) {
        for(ItemStack target:inventory) if(target!=null && target.isItemEqual(stack)
                && ItemStack.areItemStackTagsEqual(target,stack)) {
            int moved=Math.min(stack.stackSize,Math.max(0,Math.min(64,target.getMaxStackSize())-target.stackSize));
            target.stackSize+=moved;stack.stackSize-=moved;if(stack.stackSize==0)return true;
        }
        for(int i=0;i<inventory.length;i++) if(inventory[i]==null) {
            int moved=Math.min(stack.stackSize,Math.min(64,stack.getMaxStackSize()));
            inventory[i]=stack.copy();inventory[i].stackSize=moved;stack.stackSize-=moved;
            if(stack.stackSize==0)return true;
        }
        return false;
    }
    private static boolean same(ItemStack a,ItemStack b) {
        return a!=null && b!=null && a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a,b);
    }
    private static ItemStack[] copy(ItemStack[] values) {
        ItemStack[] result=new ItemStack[values.length];
        for(int i=0;i<values.length;i++)result[i]=ItemStack.copyItemStack(values[i]);return result;
    }
    private RecipePlacement112() { }
}
