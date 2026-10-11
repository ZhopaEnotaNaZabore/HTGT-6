package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

/** Server-authoritative adaptation of the Minecraft 1.9 fall-flying equations. */
public final class ElytraFlight {
    private static final Map<EntityPlayer, Boolean> FLYING =
            Collections.synchronizedMap(new WeakHashMap<EntityPlayer, Boolean>());
    private static final Map<EntityPlayer, Integer> FIREWORK_BOOST =
            Collections.synchronizedMap(new WeakHashMap<EntityPlayer, Integer>());
    private static final Map<EntityPlayer, Integer> FLYING_TICKS =
            Collections.synchronizedMap(new WeakHashMap<EntityPlayer, Integer>());

    private static final Map<EntityPlayer, Boolean> PENDING_START =
            Collections.synchronizedMap(new WeakHashMap<EntityPlayer, Boolean>());

    /** Network thread only records intent; equipment and motion are read on the player tick. */
    public static void requestStart(EntityPlayer player) {
        if (player != null) PENDING_START.put(player, Boolean.TRUE);
    }

    public static boolean start(EntityPlayer player) {
        if (player == null || !player.isEntityAlive()) return false;
        ItemStack chest = player.inventory.armorInventory[2];
        if (chest == null || chest.getItem() != CombatContent.elytra
                || chest.getItemDamage() >= chest.getMaxDamage() - 1
                || player.onGround || player.isInWater() || player.capabilities.isFlying
                || player.isRiding() || player.isPotionActive(CombatContent.levitation)) return false;
        if (isFlying(player)) return true;
        FLYING.put(player, Boolean.TRUE);
        FLYING_TICKS.put(player, Integer.valueOf(0));
        return true;
    }

    private static final Map<EntityPlayer,long[]> REMOTE_FLIGHT=Collections.synchronizedMap(new WeakHashMap<EntityPlayer,long[]>());
    public static boolean isFlying(EntityPlayer player) { return Boolean.TRUE.equals(FLYING.get(player))||REMOTE_FLIGHT.containsKey(player); }
    public static void acceptState(EntityPlayer player,boolean flying,int ticks,int boost,boolean local){
        if(!local){if(flying)REMOTE_FLIGHT.put(player,new long[]{ticks,player.worldObj.getTotalWorldTime()});else REMOTE_FLIGHT.remove(player);return;}
        if(!flying){clear(player);return;}
        FLYING.put(player,Boolean.TRUE);FLYING_TICKS.put(player,ticks);
        if(boost>0)FIREWORK_BOOST.put(player,boost);else FIREWORK_BOOST.remove(player);
    }
    public static void clear(EntityPlayer player){FLYING.remove(player);FLYING_TICKS.remove(player);FIREWORK_BOOST.remove(player);PENDING_START.remove(player);REMOTE_FLIGHT.remove(player);}
    public static void sync(EntityPlayer player){
        if(player.worldObj.isRemote)return;
        Integer boost=FIREWORK_BOOST.get(player);
        com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatUpdate.NETWORK.sendToAllAround(
            new com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketElytraState(player.getEntityId(),isFlying(player),getFlyingTicks(player),boost==null?0:boost),
            new cpw.mods.fml.common.network.NetworkRegistry.TargetPoint(player.dimension,player.posX,player.posY,player.posZ,128));
    }
    public static int getFlyingTicks(EntityPlayer player) {
        long[] remote=REMOTE_FLIGHT.get(player);
        if(remote!=null)return (int)Math.min(Integer.MAX_VALUE,remote[0]+Math.max(0,player.worldObj.getTotalWorldTime()-remote[1]));
        Integer ticks = FLYING_TICKS.get(player);
        return ticks == null ? 0 : ticks.intValue();
    }

    /** Starts the 1.12 attached-firework acceleration while the player is fall-flying. */
    public static boolean boost(EntityPlayer player, ItemStack rocket) {
        if (player == null || rocket == null || rocket.getItem() != Items.fireworks || !isFlying(player)) return false;
        int flight = 0;
        NBTTagCompound root = rocket.getTagCompound();
        if (root != null && root.hasKey("Fireworks", 10)) {
            flight = Math.max(0, root.getCompoundTag("Fireworks").getByte("Flight"));
        }
        FIREWORK_BOOST.put(player, Integer.valueOf(10 * (1 + flight)+(player.worldObj.isRemote?0:player.getRNG().nextInt(6)+player.getRNG().nextInt(7))));
        if(!player.worldObj.isRemote)sync(player);
        return true;
    }

    public static void tick(EntityPlayer player) {
        if (!player.worldObj.isRemote && PENDING_START.remove(player) != null) {start(player);sync(player);}
        if (!Boolean.TRUE.equals(FLYING.get(player))) return;
        ItemStack chest = player.inventory.armorInventory[2];
        if (chest == null || chest.getItem() != CombatContent.elytra || player.onGround
                || player.isInWater() || player.capabilities.isFlying || player.isRiding()
                || !player.isEntityAlive() || player.isPotionActive(CombatContent.levitation)
                || chest.getItemDamage() >= chest.getMaxDamage() - 1) {
            clear(player);sync(player);
            return;
        }
        FLYING_TICKS.put(player, Integer.valueOf(getFlyingTicks(player) + 1));
        if(!player.worldObj.isRemote&&getFlyingTicks(player)%20==0)sync(player);

        Vec3 look = player.getLookVec();
        // Player movement in 1.7 has already applied normal airborne drag and gravity.
        // Undo those values before applying the replacement 1.12 fall-flying equations.
        player.motionX /= 0.91D;
        player.motionZ /= 0.91D;
        player.motionY = player.motionY / 0.98D + 0.08D;
        float pitch = player.rotationPitch * (float) Math.PI / 180.0F;
        double horizontalLook = Math.sqrt(look.xCoord * look.xCoord + look.zCoord * look.zCoord);
        double horizontalSpeed = Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
        double lookLength = Math.sqrt(look.xCoord * look.xCoord + look.yCoord * look.yCoord + look.zCoord * look.zCoord);
        float lift = (float) (Math.cos(pitch) * Math.cos(pitch));
        lift *= Math.min(1.0F, (float) (lookLength / 0.4D));
        player.motionY += -0.08D + lift * 0.06D;

        if (player.motionY < 0.0D && horizontalLook > 0.0D) {
            double recovery = player.motionY * -0.1D * lift;
            player.motionY += recovery;
            player.motionX += look.xCoord * recovery / horizontalLook;
            player.motionZ += look.zCoord * recovery / horizontalLook;
        }
        if (pitch < 0.0F && horizontalLook > 0.0D) {
            double climb = horizontalSpeed * -Math.sin(pitch) * 0.04D;
            player.motionY += climb * 3.2D;
            player.motionX -= look.xCoord * climb / horizontalLook;
            player.motionZ -= look.zCoord * climb / horizontalLook;
        }
        if (horizontalLook > 0.0D) {
            player.motionX += (look.xCoord / horizontalLook * horizontalSpeed - player.motionX) * 0.1D;
            player.motionZ += (look.zCoord / horizontalLook * horizontalSpeed - player.motionZ) * 0.1D;
        }
        player.motionX *= 0.99D;
        player.motionY *= 0.98D;
        player.motionZ *= 0.99D;
        applyFireworkBoost(player, look);
        player.fallDistance = 1.0F;

        if (!player.worldObj.isRemote && getFlyingTicks(player) % 20 == 0) {
            chest.damageItem(1, player);
            if (chest.stackSize <= 0 || chest.getItemDamage() >= chest.getMaxDamage() - 1) {
                clear(player);sync(player);
            }
        }
    }

    private static void applyFireworkBoost(EntityPlayer player, Vec3 look) {
        Integer remaining = FIREWORK_BOOST.get(player);
        if (remaining == null || remaining.intValue() <= 0) return;
        player.motionX += look.xCoord * 0.1D + (look.xCoord * 1.5D - player.motionX) * 0.5D;
        player.motionY += look.yCoord * 0.1D + (look.yCoord * 1.5D - player.motionY) * 0.5D;
        player.motionZ += look.zCoord * 0.1D + (look.zCoord * 1.5D - player.motionZ) * 0.5D;
        int next = remaining.intValue() - 1;
        if (next <= 0) FIREWORK_BOOST.remove(player);
        else FIREWORK_BOOST.put(player, Integer.valueOf(next));
        if (player.worldObj.isRemote) {
            player.worldObj.spawnParticle("fireworksSpark", player.posX - look.xCoord * 0.5D,
                    player.posY + player.height * 0.45D - look.yCoord * 0.5D,
                    player.posZ - look.zCoord * 0.5D, -look.xCoord * 0.15D,
                    -look.yCoord * 0.15D, -look.zCoord * 0.15D);
        }
    }

    private ElytraFlight() { }
}
