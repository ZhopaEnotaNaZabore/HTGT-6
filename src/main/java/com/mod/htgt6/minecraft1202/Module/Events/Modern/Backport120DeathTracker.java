package com.mod.htgt6.minecraft1202.Module.Events.Modern;

import com.mod.htgt6.minecraft1202.Module.Network.Modern.PacketBackport120DeathPosition;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

/** Persists and synchronizes the modern last-death position without patching vanilla players. */
public final class Backport120DeathTracker {
    private static final String CHANNEL = "MC1202_120";
    private static final String TAG = "HTGT6LastDeath120";
    private static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
    private static final Backport120DeathTracker INSTANCE = new Backport120DeathTracker();
    private static boolean initialized;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        NETWORK.registerMessage(PacketBackport120DeathPosition.Handler.class,
                PacketBackport120DeathPosition.class, 0, Side.CLIENT);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        FMLCommonHandler.instance().bus().register(INSTANCE);
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (!(event.entityLiving instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.entityLiving;
        NBTTagCompound death = new NBTTagCompound();
        death.setInteger("X", MathHelper.floor_double(player.posX));
        death.setInteger("Y", MathHelper.floor_double(player.posY));
        death.setInteger("Z", MathHelper.floor_double(player.posZ));
        death.setInteger("Dimension", player.dimension);
        persisted(player).setTag(TAG, death);
        sync(player);
    }

    @SubscribeEvent
    public void onClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        NBTTagCompound oldPersisted = persisted(event.original);
        if (oldPersisted.hasKey(TAG, 10)) {
            persisted(event.entityPlayer).setTag(TAG, oldPersisted.getCompoundTag(TAG).copy());
        }
    }

    @SubscribeEvent public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) sync((EntityPlayerMP) event.player);
    }

    @SubscribeEvent public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.player instanceof EntityPlayerMP) sync((EntityPlayerMP) event.player);
    }

    private static NBTTagCompound persisted(EntityPlayer player) {
        NBTTagCompound entity = player.getEntityData();
        if (!entity.hasKey(EntityPlayer.PERSISTED_NBT_TAG, 10)) {
            entity.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
        }
        return entity.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
    }

    private static void sync(EntityPlayerMP player) {
        NBTTagCompound root = persisted(player);
        boolean valid = root.hasKey(TAG, 10);
        NBTTagCompound death = valid ? root.getCompoundTag(TAG) : new NBTTagCompound();
        NETWORK.sendTo(new PacketBackport120DeathPosition(valid, death.getInteger("X"),
                death.getInteger("Z"), death.getInteger("Dimension")), player);
    }

    private Backport120DeathTracker() { }
}
