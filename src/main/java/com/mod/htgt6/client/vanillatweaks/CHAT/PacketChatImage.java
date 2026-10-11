package com.mod.htgt6.client.vanillatweaks.CHAT;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.server.MinecraftServer;

/** Legacy packet name; carries extended chat text while retaining normal server chat hooks. */
public final class PacketChatImage implements IMessage {
    private String url;
    public PacketChatImage() {}
    public PacketChatImage(String url) { this.url = url; }
    public void fromBytes(ByteBuf buffer) {
        url = null;
        // Drop malformed payloads without forwarding them or producing an exception log per packet.
        if (buffer.readableBytes() > ChatImageLinks.MAX_URL * 4 + 5) return;
        try {
            int length = ByteBufUtils.readVarInt(buffer, 3);
            if (length <= 0 || length > ChatImageLinks.MAX_URL * 4 || length != buffer.readableBytes()) return;
            byte[] bytes = new byte[length];
            buffer.readBytes(bytes);
            String candidate = java.nio.charset.StandardCharsets.UTF_8.newDecoder()
                    .decode(java.nio.ByteBuffer.wrap(bytes)).toString();
            if (validText(candidate)) url = candidate;
        } catch (RuntimeException | java.nio.charset.CharacterCodingException invalid) {
            /* Untrusted/truncated wire data. */
        }
    }
    public void toBytes(ByteBuf buffer) { ByteBufUtils.writeUTF8String(buffer, url); }
    public static boolean validText(String text) {
        if (text == null || text.trim().isEmpty() || text.length() > ChatImageLinks.MAX_URL
                || text.trim().startsWith("/")) return false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isISOControl(c) || !net.minecraft.util.ChatAllowedCharacters.isAllowedCharacter(c)) return false;
        }
        return true;
    }
    public static final class Handler implements IMessageHandler<PacketChatImage, IMessage> {
        private static final ChatImageIngress<Pending> PENDING = new ChatImageIngress<Pending>();
        public static void clear() { PENDING.clear(); }
        public IMessage onMessage(PacketChatImage message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().playerEntity;
            if (message.url != null)
                PENDING.offer(player.getUniqueID(), new Pending(player, message.url), System.nanoTime());
            return null;
        }
        @SubscribeEvent public void tick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            for (int i = 0; i < 2; i++) {
                Pending pending = PENDING.poll();
                if (pending == null) break;
                if (!MinecraftServer.getServer().getConfigurationManager().playerEntityList.contains(pending.player)) continue;
                final String url = pending.url;
                pending.player.playerNetServerHandler.processChatMessage(new C01PacketChatMessage() {
                    @Override public String func_149439_c() { return url; }
                });
            }
        }
    }
    private static final class Pending {
        final EntityPlayerMP player; final String url;
        Pending(EntityPlayerMP player, String url) { this.player = player; this.url = url; }
    }
}
