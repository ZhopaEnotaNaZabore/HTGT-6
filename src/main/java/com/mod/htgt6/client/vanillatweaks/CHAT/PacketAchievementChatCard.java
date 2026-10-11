package com.mod.htgt6.client.vanillatweaks.CHAT;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

/** Server-authoritative notification used by the ModularUI chat history. */
public class PacketAchievementChatCard implements IMessage {
    private String playerName;
    private String achievementId;
    private boolean special;

    public PacketAchievementChatCard() {}

    public PacketAchievementChatCard(String playerName, String achievementId, boolean special) {
        this.playerName = playerName;
        this.achievementId = achievementId;
        this.special = special;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerName = ByteBufUtils.readUTF8String(buf);
        this.achievementId = ByteBufUtils.readUTF8String(buf);
        this.special = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.playerName);
        ByteBufUtils.writeUTF8String(buf, this.achievementId);
        buf.writeBoolean(this.special);
    }

    public static class Handler implements IMessageHandler<PacketAchievementChatCard, IMessage> {
        @Override
        public IMessage onMessage(PacketAchievementChatCard message, MessageContext ctx) {
            final String playerName = message.playerName;
            final String achievementId = message.achievementId;
            final boolean special = message.special;
            Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override
                public void run() {
                    ChatManager.addAchievement(playerName, achievementId, special);
                }
            });
            return null;
        }
    }
}
