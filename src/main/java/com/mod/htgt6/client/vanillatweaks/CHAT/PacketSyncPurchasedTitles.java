package com.mod.htgt6.client.vanillatweaks.CHAT;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;

/** Private purchased/OP availability snapshot; active prefixes never grant ownership. */
public final class PacketSyncPurchasedTitles implements IMessage {
    private final List<String> ids = new ArrayList<String>();
    public PacketSyncPurchasedTitles() {}
    public static void send(EntityPlayerMP player) {
        PacketSyncPurchasedTitles packet = new PacketSyncPurchasedTitles();
        String uuid = player.getUniqueID().toString();
        for (TitleManager.Title title : TitleManager.TITLE_DATABASE.values()) {
            if ((title.coinPrice > 0 && TitleManager.isPurchased(uuid, title.id))
                    || (title.isOpOnly && player.canCommandSenderUseCommand(2, ""))) packet.ids.add(title.id);
        }
        HTGT6.network.sendTo(packet, player);
    }
    @Override public void toBytes(ByteBuf buf) {
        buf.writeInt(ids.size());
        for (String id : ids) ByteBufUtils.writeUTF8String(buf, id);
    }
    @Override public void fromBytes(ByteBuf buf) {
        ids.clear();
        int count = buf.readInt();
        if (count < 0 || count > 256) throw new IllegalArgumentException("Invalid title count");
        for (int i = 0; i < count; i++) ids.add(ByteBufUtils.readUTF8String(buf));
    }
    public static final class Handler implements IMessageHandler<PacketSyncPurchasedTitles, IMessage> {
        @Override public IMessage onMessage(PacketSyncPurchasedTitles packet, MessageContext context) {
            final List<String> snapshot = new ArrayList<String>(packet.ids);
            net.minecraft.client.Minecraft.getMinecraft().func_152344_a(() -> {
                TitleManager.clientUnlockedTitles.clear();
                for (String id : snapshot) {
                    TitleManager.Title title = TitleManager.TITLE_DATABASE.get(id);
                    if (title != null && (title.coinPrice > 0 || title.isOpOnly)) TitleManager.clientUnlockedTitles.add(id);
                }
            });
            return null;
        }
    }
}
