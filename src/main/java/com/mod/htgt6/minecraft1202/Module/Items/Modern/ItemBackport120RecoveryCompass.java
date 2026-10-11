package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

/** Dimension-aware recovery compass backed by the player's persistent last-death position. */
public final class ItemBackport120RecoveryCompass extends Item {
    private static boolean clientTargetValid;
    private static int clientTargetX;
    private static int clientTargetZ;
    private static int clientTargetDimension;

    // Do not annotate fields with @SideOnly: FML strips them from the dedicated
    // server class, while Java keeps the lastUpdateTick initializer in the
    // shared constructor, producing NoSuchFieldError during content bootstrap.
    private IIcon[] frames;
    private double visualAngle;
    private double visualVelocity;
    private long lastUpdateTick = Long.MIN_VALUE;

    public ItemBackport120RecoveryCompass() {
        setUnlocalizedName("backport_1_20.recovery_compass");
        setTextureName("htgt6:1_20_backport/recovery_compass_16");
        setCreativeTab(CreativeTabs.tabTools);
    }

    public static void setClientTarget(boolean valid, int x, int z, int dimension) {
        clientTargetValid = valid;
        clientTargetX = x;
        clientTargetZ = z;
        clientTargetDimension = dimension;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        frames = new IIcon[32];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = register.registerIcon(String.format("htgt6:1_20_backport/recovery_compass_%02d", i));
        }
        itemIcon = frames[16];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
        if (player == null || frames == null) return itemIcon;
        long tick = player.worldObj.getTotalWorldTime();
        if (tick != lastUpdateTick) {
            lastUpdateTick = tick;
            double target;
            if (clientTargetValid && player.dimension == clientTargetDimension) {
                double bearing = Math.atan2(clientTargetZ + 0.5D - player.posZ,
                        clientTargetX + 0.5D - player.posX) / (Math.PI * 2.0D);
                target = 0.5D - (player.rotationYaw / 360.0D - bearing);
            } else {
                target = Math.random();
            }
            double delta = target - visualAngle;
            delta -= MathHelper.floor_double(delta + 0.5D);
            visualVelocity += delta * 0.1D;
            visualVelocity *= 0.8D;
            visualAngle = normalize(visualAngle + visualVelocity);
        }
        int predicateFrame = MathHelper.floor_double(visualAngle * 32.0D) & 31;
        return frames[(predicateFrame + 16) & 31];
    }

    private static double normalize(double angle) {
        return angle - Math.floor(angle);
    }
}
