package com.mod.htgt6.orehelper;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.*;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Network threads only enqueue. GT registries/config are read on the server tick. */
public final class OreHelperNetwork {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("HT6OreHelper");
    private static final Map<EntityPlayerMP, Request> PENDING = new ConcurrentHashMap<EntityPlayerMP, Request>();
    private static final Map<EntityPlayerMP, Long> LAST = new WeakHashMap<EntityPlayerMP, Long>();
    public static void init() {
        CHANNEL.registerMessage(RequestHandler.class, Request.class, 0, Side.SERVER);
        CHANNEL.registerMessage(ReplyHandler.class, Reply.class, 1, Side.CLIENT);
        FMLCommonHandler.instance().bus().register(new OreHelperNetwork());
    }
    public static class Request implements IMessage {
        public int material, token;
        public Request() {}
        public Request(int material, int token) { this.material = material; this.token = token; }
        public void fromBytes(ByteBuf b) { material = b.readInt(); token = b.readInt(); }
        public void toBytes(ByteBuf b) { b.writeInt(material); b.writeInt(token); }
    }
    public static class Reply implements IMessage {
        public NBTTagCompound data;
        public Reply() {}
        Reply(NBTTagCompound data) { this.data = data; }
        public void fromBytes(ByteBuf b) { data = ByteBufUtils.readTag(b); }
        public void toBytes(ByteBuf b) { ByteBufUtils.writeTag(b, data); }
    }
    public static class RequestHandler implements IMessageHandler<Request, IMessage> {
        public IMessage onMessage(Request r, MessageContext c) {
            // One outstanding query per connection; malformed material IDs are rejected.
            if (r.material >= 0 && r.material < 32768) PENDING.put(c.getServerHandler().playerEntity, r);
            return null;
        }
    }
    public static class ReplyHandler implements IMessageHandler<Reply, IMessage> {
        public IMessage onMessage(Reply r, MessageContext c) {
            if (r.data != null) OreHelperMod.proxy.receive(r.data);
            return null;
        }
    }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        long now = System.currentTimeMillis();
        for (EntityPlayerMP player : PENDING.keySet()) {
            if (player.playerNetServerHandler.netManager.isChannelOpen()) {
                Long last = LAST.get(player);
                if (last != null && now - last < 750) continue;
                Request request = PENDING.remove(player);
                if (request == null) continue;
                LAST.put(player, now);
                boolean failed = false;
                try {
                    List<NBTTagCompound> entries = OreCatalog.query(request.material);
                    // Send individual records instead of a complete catalog in one payload.
                    for (NBTTagCompound entry : entries) {
                        entry.setInteger("token", request.token);
                        CHANNEL.sendTo(new Reply(entry), player);
                    }
                } catch (RuntimeException e) {
                    failed = true;
                    org.apache.logging.log4j.LogManager.getLogger(OreHelperMod.ID).error("Ore catalog request failed", e);
                }
                NBTTagCompound done = new NBTTagCompound();
                done.setInteger("token", request.token);
                done.setBoolean("done", true);
                done.setBoolean("failed", failed);
                CHANNEL.sendTo(new Reply(done), player);
            } else { PENDING.remove(player); LAST.remove(player); }
        }
    }
}
