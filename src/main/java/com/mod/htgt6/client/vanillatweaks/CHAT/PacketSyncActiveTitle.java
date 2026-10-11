package com.mod.htgt6.client.vanillatweaks.CHAT;



import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

public class PacketSyncActiveTitle implements IMessage {
    private String username;
    private String prefix;

    public PacketSyncActiveTitle() {}
    public PacketSyncActiveTitle(String username, String prefix) {
        this.username = username;
        this.prefix = prefix;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.username = ByteBufUtils.readUTF8String(buf);
        this.prefix = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.username);
        ByteBufUtils.writeUTF8String(buf, this.prefix);
    }

    public static class Handler implements IMessageHandler<PacketSyncActiveTitle, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncActiveTitle message, MessageContext ctx) {
            final String username = message.username;
            final String prefix = message.prefix;
            Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override
                public void run() {
                    // Empty is an explicit "no title" choice, including integrated servers.
                    TitleManager.activePlayerTitles.put(username, prefix);
                }
            });
            return null;
        }
    }
}
