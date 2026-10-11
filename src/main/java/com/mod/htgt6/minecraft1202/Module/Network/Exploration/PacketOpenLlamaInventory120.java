package com.mod.htgt6.minecraft1202.Module.Network.Exploration;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.simpleimpl.*;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.ContainerLlama120;
import net.minecraft.entity.player.EntityPlayer;

/** Payload-free request: only the server's current mount can be opened. */
public final class PacketOpenLlamaInventory120 implements IMessage {
    private static final java.util.Set<java.util.UUID> pending=java.util.Collections.newSetFromMap(
            new java.util.concurrent.ConcurrentHashMap<java.util.UUID,Boolean>());
    public void fromBytes(io.netty.buffer.ByteBuf buffer) { }
    public void toBytes(io.netty.buffer.ByteBuf buffer) { }
    public static EntityLlama112 eligibleMount(EntityPlayer player) {
        if(player==null || !player.isEntityAlive() || !(player.ridingEntity instanceof EntityLlama112))return null;
        EntityLlama112 llama=(EntityLlama112)player.ridingEntity;
        return llama.worldObj==player.worldObj && llama.riddenByEntity==player && llama.isEntityAlive() && llama.isTamed() && !llama.isChild()?llama:null;
    }
    public static final class Handler implements IMessageHandler<PacketOpenLlamaInventory120,IMessage> {
        public IMessage onMessage(PacketOpenLlamaInventory120 message,MessageContext context) {
            pending.add(context.getServerHandler().playerEntity.getUniqueID());return null;
        }
    }
    @SubscribeEvent public void tick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player=event.player;
        if(event.phase!=TickEvent.Phase.END || player.worldObj.isRemote || !pending.remove(player.getUniqueID()))return;
        EntityLlama112 llama=eligibleMount(player);
        if(llama!=null && player.openContainer==player.inventoryContainer)
            player.openGui(com.mod.htgt6.minecraft1202.Minecraft1202.instance,ContainerLlama120.GUI_ID,
                    player.worldObj,llama.getEntityId(),llama.getStorageColumns(),0);
    }
    @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent event){pending.remove(event.player.getUniqueID());}
}
