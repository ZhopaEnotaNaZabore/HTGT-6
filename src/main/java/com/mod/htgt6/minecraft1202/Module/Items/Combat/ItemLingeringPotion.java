package com.mod.htgt6.minecraft1202.Module.Items.Combat;

import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityLingeringPotion;

import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatPotionVariants120;

public final class ItemLingeringPotion extends Item {
    private IIcon overlay;
    private IIcon bottle;
    public ItemLingeringPotion() {
        setUnlocalizedName("lingering_potion");
        setTextureName("htgt6:combat_update/potion_bottle_lingering");
        setCreativeTab(CreativeTabs.tabBrewing);
        setHasSubtypes(true);
        setMaxStackSize(1);
    }
    @Override public void registerIcons(IIconRegister register) {
        overlay = register.registerIcon("minecraft:potion_overlay");
        bottle = register.registerIcon("htgt6:combat_update/potion_bottle_lingering");
        itemIcon = bottle;
    }
    @Override public boolean requiresMultipleRenderPasses() { return true; }
    @Override public IIcon getIconFromDamageForRenderPass(int damage, int pass) { return pass == 0 ? overlay : bottle; }
    @Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
        if (!world.isRemote) {
            world.spawnEntityInWorld(new EntityLingeringPotion(world, player, stack));
        }
        if (!player.capabilities.isCreativeMode) --stack.stackSize;
        return stack;
    }
    @Override public int getColorFromItemStack(ItemStack stack, int pass) {
        if (pass == 0 && stack.hasTagCompound() && stack.getTagCompound().hasKey("CustomPotionColor",99)) return stack.getTagCompound().getInteger("CustomPotionColor");
        return pass == 0 ? PotionHelper.calcPotionLiquidColor(PotionHelper.getPotionEffects(stack.getItemDamage(), false)) : 0xFFFFFF;
    }
    @Override public String getItemStackDisplayName(ItemStack stack) {
        List effects = PotionHelper.getPotionEffects(stack.getItemDamage(), false);
        if (effects == null || effects.isEmpty()) return super.getItemStackDisplayName(stack);
        String effect = StatCollector.translateToLocal(((PotionEffect) effects.get(0)).getEffectName());
        return StatCollector.translateToLocalFormatted("item.lingering_potion.effect.name", effect);
    }
    @Override public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int damage : CombatPotionVariants120.values()) list.add(new ItemStack(item, 1, damage));
    }
}
