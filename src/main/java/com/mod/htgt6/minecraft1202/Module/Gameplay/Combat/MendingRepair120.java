package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import java.util.List;
import java.util.Random;
import net.minecraft.item.ItemStack;

/** ExperienceOrb repairPlayerItems from 1.20.2, expressed iteratively for legacy pickup events. */
public final class MendingRepair120 {
    private MendingRepair120() {}

    /** Candidates must be damaged equipped stacks with Mending; the supplied list is consumed. */
    public static int repair(List<ItemStack> candidates,int orbValue,Random random) {
        int remaining=Math.max(0,orbValue);
        // 1.20.2 uses the original orb value for every recursive repair, not remaining XP.
        int capacity=(int)Math.min(Integer.MAX_VALUE,2L*remaining);
        while(remaining>0&&!candidates.isEmpty()) {
            ItemStack stack=candidates.remove(random.nextInt(candidates.size()));
            if(stack==null||!stack.isItemDamaged())continue;
            int repaired=Math.min(stack.getItemDamage(),capacity);
            stack.setItemDamage(stack.getItemDamage()-repaired);
            remaining=Math.max(0,remaining-repaired/2);
        }
        return remaining;
    }
}
