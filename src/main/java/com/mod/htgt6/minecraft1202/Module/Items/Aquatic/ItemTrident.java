package com.mod.htgt6.minecraft1202.Module.Items.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityThrownTrident;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.RiptideAttackHandler;

import com.google.common.collect.Multimap;
import com.mod.htgt6.HTGT6;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.util.MathHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Durable melee weapon that can be charged and thrown. */
public final class ItemTrident extends Item {
    public static final int THROW_THRESHOLD_TICKS = 10;
    public static final double ATTACK_DAMAGE_MODIFIER = 8.0D;
    public ItemTrident() {
        setUnlocalizedName("trident");
        setTextureName(HTGT6.MOD_ID.toLowerCase() + ":trident");
        setCreativeTab(CreativeTabs.tabCombat);
        setMaxStackSize(1);
        setMaxDamage(250);
        setFull3D();
    }

    @Override public int getMaxItemUseDuration(ItemStack stack) { return 72000; }

    @Override
    public Multimap getItemAttributeModifiers() {
        Multimap modifiers = super.getItemAttributeModifiers();
        // Player base damage is 1; the reference trident deals 9 total melee damage.
        modifiers.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(),
                new AttributeModifier(field_111210_e, "Weapon modifier", ATTACK_DAMAGE_MODIFIER, 0));
        modifiers.put("generic.attackSpeed",new AttributeModifier(java.util.UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3"),"Weapon speed",-2.9D,0));
        return modifiers;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        stack.damageItem(1, attacker);
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, Block block, int x, int y, int z,
                                    EntityLivingBase user) {
        if (block.getBlockHardness(world, x, y, z) != 0.0F) stack.damageItem(2, user);
        return true;
    }
    // The bow action bends both player arms in 1.7.10. The dedicated client
    // renderer supplies the spear charging pose instead.
    @Override public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.none; }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (stack.getItemDamage() >= stack.getMaxDamage() - 1) return stack;
        if (EnchantmentHelper.getEnchantmentLevel(OceanContent.riptide.effectId, stack)>0 && !player.isWet()) return stack;
        player.setItemInUse(stack, getMaxItemUseDuration(stack));
        return stack;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int remaining) {
        int charge = getMaxItemUseDuration(stack) - remaining;
        if (charge < THROW_THRESHOLD_TICKS) return;
        int riptide = EnchantmentHelper.getEnchantmentLevel(OceanContent.riptide.effectId, stack);
        if (riptide > 0) {
            // LivingEntity#isInWaterOrRain in 1.20.2 maps to Entity#isWet here;
            // global rain alone must not activate Riptide under a roof.
            if (!player.isWet()) return;
            float yaw = player.rotationYaw * (float)Math.PI / 180.0F;
            float pitch = player.rotationPitch * (float)Math.PI / 180.0F;
            float x = -MathHelper.sin(yaw) * MathHelper.cos(pitch);
            float y = -MathHelper.sin(pitch);
            float z = MathHelper.cos(yaw) * MathHelper.cos(pitch);
            float length = MathHelper.sqrt_float(x * x + y * y + z * z);
            float speed = 3.0F * (1.0F + riptide) / 4.0F;
            player.motionX += x / length * speed;
            player.motionY += y / length * speed;
            player.motionZ += z / length * speed;
            player.fallDistance = 0.0F;
            RiptideAttackHandler.start(player);
            if (!world.isRemote) stack.damageItem(1, player);
            world.playSoundAtEntity(player, "htgt6:item.trident.riptide_1", 1.0F, 1.0F);
            return;
        }
        if (!world.isRemote) {
            ItemStack thrownStack = stack.copy();
            thrownStack.stackSize = 1;
            thrownStack.damageItem(1, player);
            EntityThrownTrident trident = new EntityThrownTrident(world, player,
                    thrownStack, !player.capabilities.isCreativeMode);
            trident.setThrowableHeading(trident.motionX, trident.motionY, trident.motionZ, 2.5F, 1.0F);
            if (!world.spawnEntityInWorld(trident)) return;
            if (!player.capabilities.isCreativeMode) stack.stackSize--;
            player.inventory.markDirty();
            world.playSoundAtEntity(player, "htgt6:item.trident.throw", 1.0F, 0.8F + itemRand.nextFloat() * 0.2F);
        }
    }
}
