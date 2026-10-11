package com.mod.htgt6.minecraft1202.Module.Items.Combat;

import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatBoat;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

/** Java 1.20.2 five-plank boat item adapted to the legacy ray-trace API. */
public final class ItemCombatBoat extends Item {
    private final int variant;

    public ItemCombatBoat(String wood, int variant) {
        this.variant = variant;
        setUnlocalizedName(wood + "_boat");
        setTextureName("htgt6:combat_update/" + wood + "_boat");
        setCreativeTab(CreativeTabs.tabTransport);
        setMaxStackSize(1);
    }

    @Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        return com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.BoatPlacement120.place(stack,world,player,variant);
    }
}
