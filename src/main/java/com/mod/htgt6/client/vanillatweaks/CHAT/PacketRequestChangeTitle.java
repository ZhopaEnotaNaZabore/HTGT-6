package com.mod.htgt6.client.vanillatweaks.CHAT;


import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import com.mod.htgt6.common.item.htgt6.coins.HiTech6Coins;
import net.minecraft.item.ItemStack;

public class PacketRequestChangeTitle implements IMessage {
    private String titleId;

    public PacketRequestChangeTitle() {}
    public PacketRequestChangeTitle(String titleId) { this.titleId = titleId; }

    @Override
    public void fromBytes(ByteBuf buf) { this.titleId = ByteBufUtils.readUTF8String(buf); }
    @Override
    public void toBytes(ByteBuf buf) { ByteBufUtils.writeUTF8String(buf, this.titleId); }

    public static class Handler implements IMessageHandler<PacketRequestChangeTitle, IMessage> {

        private static final java.util.concurrent.ConcurrentHashMap<java.util.UUID, Runnable> PENDING = new java.util.concurrent.ConcurrentHashMap<java.util.UUID, Runnable>();

        @Override
        public IMessage onMessage(PacketRequestChangeTitle message, MessageContext ctx) {
            final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null || message.titleId == null || message.titleId.length() > 64) return null;
            final String requested = message.titleId;
            // Network callbacks must not mutate inventory or saved world data.
            PENDING.put(player.getUniqueID(), () -> {
                if (player.playerNetServerHandler != null && !player.playerNetServerHandler.netManager.isChannelOpen()) return;
                apply(new PacketRequestChangeTitle(requested), player);
            });
            return null;
        }

        static void drain() {
            for (java.util.Map.Entry<java.util.UUID, Runnable> entry : PENDING.entrySet()) {
                if (PENDING.remove(entry.getKey(), entry.getValue())) entry.getValue().run();
            }
        }

        private IMessage apply(PacketRequestChangeTitle message, EntityPlayerMP player) {
            TitleSaveData.get(player.worldObj);
            String username = player.getCommandSenderName();
            String playerId = player.getUniqueID().toString();

            if (message.titleId.equals("__sync")) {
                PacketSyncPurchasedTitles.send(player);
                return null;
            }

            if (message.titleId.startsWith("buy:")) {
                String shopId = message.titleId.substring(4);
                TitleManager.Title shopTitle = TitleManager.TITLE_DATABASE.get(shopId);
                if (shopTitle == null || shopTitle.coinPrice <= 0) return null;
                boolean wasPurchased = TitleManager.isPurchased(playerId, shopId);
                if (!wasPurchased && !consumeCoins(player, shopTitle.coinPrice)) {
                    player.addChatMessage(new net.minecraft.util.ChatComponentText("§cНедостаточно: §fМонета GMC.")); return null;
                }
                TitleManager.markPurchased(playerId, shopId);
                PacketSyncPurchasedTitles.send(player);
                player.inventory.markDirty();
                player.inventoryContainer.detectAndSendChanges();
                TitleManager.activePlayerTitles.put(username, shopTitle.prefix);
                TitleSaveData.get(player.worldObj).markDirty();
                broadcastUpdate(username, shopTitle.prefix);
                if (!wasPurchased) player.addChatMessage(new net.minecraft.util.ChatComponentText("§aУспешно получен §f" + shopTitle.prefix));
                return null;
            }

            if (message.titleId.equals("none")) {
                TitleManager.activePlayerTitles.put(username, "");
                TitleSaveData.get(player.worldObj).markDirty(); // Save deletion
                broadcastUpdate(username, "");
                return null;
            }

            TitleManager.Title title = TitleManager.TITLE_DATABASE.get(message.titleId);
            if (title != null) {
                if (title.coinPrice > 0 && !TitleManager.isPurchased(playerId, title.id)) {
                    player.addChatMessage(new net.minecraft.util.ChatComponentText("§cСначала купите этот титул в магазине."));
                    return null;
                }
                if (title.isOpOnly && !player.canCommandSenderUseCommand(2, "")) {
                    player.addChatMessage(new net.minecraft.util.ChatComponentText("§cYou do not have permission to use this title!"));
                    return null;
                }
                if (title.achievementId != null) {
                    StatBase stat = StatList.func_151177_a(title.achievementId);
                    if (!(stat instanceof Achievement) || !player.func_147099_x().hasAchievementUnlocked((Achievement) stat)) {
                        player.addChatMessage(new net.minecraft.util.ChatComponentText("§cUnlock the required achievement first."));
                        return null;
                    }
                }

                TitleManager.activePlayerTitles.put(username, title.prefix);
                TitleSaveData.get(player.worldObj).markDirty(); // Save assignment
                broadcastUpdate(username, title.prefix);
            }
            return null;
        }

        private void broadcastUpdate(String username, String prefix) {
            com.mod.htgt6.HTGT6.network.sendToAll(new PacketSyncActiveTitle(username, prefix));
        }
        private boolean consumeCoins(EntityPlayerMP player, int amount) {
            int available=0; for(ItemStack stack : player.inventory.mainInventory) if(stack!=null && stack.getItem()==HiTech6Coins.HiTech6Coin) available+=stack.stackSize;
            if(available < amount) return false;
            int left=amount;
            for(int i=0;i<player.inventory.mainInventory.length;i++) { ItemStack stack=player.inventory.mainInventory[i]; if(stack!=null && stack.getItem()==HiTech6Coins.HiTech6Coin) { int take=Math.min(left, stack.stackSize); stack.stackSize-=take; left-=take; if(stack.stackSize==0) player.inventory.mainInventory[i]=null; if(left==0) return true; } }
            return true;
        }
    }
}
