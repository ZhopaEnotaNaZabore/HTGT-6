package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockCampfire149;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/** Modern water splash block footprint; no water source or neighbour flood is created. */
public final class CampfireWater157 {
    private CampfireWater157() {}
    public static boolean afterCancellation(boolean cancelled, EntityPotion potion, MovingObjectPosition hit) {
        if (!cancelled) onImpact(potion, hit);
        return cancelled;
    }
    public static void onImpact(EntityPotion potion, MovingObjectPosition hit) {
        World world = potion.worldObj;
        if (world == null || world.isRemote || hit == null
                || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
                || hit.sideHit < 0 || hit.sideHit > 5 || (potion.getPotionDamage() & ~16384) != 0) return;
        // Metadata alone cannot distinguish water with CustomPotionEffects.
        NBTTagCompound tag = new NBTTagCompound();
        potion.writeEntityToNBT(tag);
        ItemStack bottle = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Potion"));
        if (bottle == null || bottle.getItem() != Items.potionitem) return;
        java.util.List effects = Items.potionitem.getEffects(bottle);
        if (effects != null && !effects.isEmpty()) return;
        ForgeDirection side = ForgeDirection.getOrientation(hit.sideHit);
        int x = hit.blockX + side.offsetX, y = hit.blockY + side.offsetY, z = hit.blockZ + side.offsetZ;
        dowse(world, x, y, z);
        dowse(world, hit.blockX, hit.blockY, hit.blockZ);
        dowse(world, x - 1, y, z); dowse(world, x + 1, y, z);
        dowse(world, x, y, z - 1); dowse(world, x, y, z + 1);
    }
    private static void dowse(World world, int x, int y, int z) {
        if (!world.blockExists(x, y, z)) return;
        net.minecraft.block.Block block = world.getBlock(x, y, z);
        if (block instanceof BlockCampfire149 && BlockCampfire149.lit(world.getBlockMetadata(x, y, z))
                && ((BlockCampfire149) block).setLit(world, x, y, z, false)) {
            world.playSoundEffect(x + .5, y + .5, z + .5, "random.fizz", .5F,
                    2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * .8F);
        }
    }
}
