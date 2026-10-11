package com.mod.htgt6.minecraft1202.Module.Items.Combat;

import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatShulker;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/** Dedicated Shulker egg; it cannot be redirected by Forge's global numeric entity IDs. */
public final class ItemCombatSpawnEgg extends Item {
    @SideOnly(Side.CLIENT) private IIcon overlay;
    public ItemCombatSpawnEgg() {
        setUnlocalizedName("combat_update.spawn_egg.shulker");
        setCreativeTab(CreativeTabs.tabMisc);
    }
    @SideOnly(Side.CLIENT) @Override public void registerIcons(IIconRegister register) {
        itemIcon = register.registerIcon("spawn_egg");
        overlay = register.registerIcon("spawn_egg_overlay");
    }
    @SideOnly(Side.CLIENT) @Override public boolean requiresMultipleRenderPasses() { return true; }
    @SideOnly(Side.CLIENT) @Override public int getRenderPasses(int metadata) { return 2; }
    @SideOnly(Side.CLIENT) @Override public IIcon getIconFromDamageForRenderPass(int metadata, int pass) {
        return pass == 0 ? itemIcon : overlay;
    }
    @SideOnly(Side.CLIENT) @Override public int getColorFromItemStack(ItemStack stack, int pass) {
        return pass == 0 ? 0x946794 : 0x4D3852;
    }
    @Override public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
            int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;
        x += Facing.offsetsXForSide[side]; y += Facing.offsetsYForSide[side]; z += Facing.offsetsZForSide[side];
        EntityCombatShulker shulker = new EntityCombatShulker(world);
        shulker.setLocationAndAngles(x + 0.5D, y, z + 0.5D, 0.0F, 0.0F);
        if (stack.hasDisplayName()) shulker.setCustomNameTag(stack.getDisplayName());
        if (!world.spawnEntityInWorld(shulker)) return false;
        if (!player.capabilities.isCreativeMode) --stack.stackSize;
        return true;
    }
}
