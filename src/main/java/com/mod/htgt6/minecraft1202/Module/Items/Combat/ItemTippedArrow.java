package com.mod.htgt6.minecraft1202.Module.Items.Combat;

import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.util.IIcon;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatPotionVariants120;

public final class ItemTippedArrow extends Item {
    private IIcon base;
    private IIcon head;
    public ItemTippedArrow() { setUnlocalizedName("tipped_arrow"); setCreativeTab(CreativeTabs.tabCombat); setHasSubtypes(true); }
    @Override public void registerIcons(IIconRegister register) {
        base = register.registerIcon("htgt6:combat_update/tipped_arrow_base");
        head = register.registerIcon("htgt6:combat_update/tipped_arrow_head");
        itemIcon = base;
    }
    @Override public boolean requiresMultipleRenderPasses() { return true; }
    @Override public IIcon getIconFromDamageForRenderPass(int damage, int pass) { return pass == 0 ? base : head; }
    @Override public int getColorFromItemStack(ItemStack stack, int pass) {
        if(pass!=0&&stack.hasTagCompound()&&stack.getTagCompound().hasKey("CustomPotionColor",99))return stack.getTagCompound().getInteger("CustomPotionColor");
        return pass == 0 ? 0xFFFFFF : PotionHelper.calcPotionLiquidColor(PotionHelper.getPotionEffects(stack.getItemDamage(), false));
    }
    @Override public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int damage : CombatPotionVariants120.values()) list.add(new ItemStack(item, 1, damage));
    }
}
