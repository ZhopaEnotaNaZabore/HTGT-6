package com.mod.htgt6.client.vanillatweaks.CHAT;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AchievementEvent;
import net.minecraft.stats.Achievement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class TitleAchievementWatcher {
    private final Map<String, PendingAchievement> pendingAchievements = new LinkedHashMap<String, PendingAchievement>();

    public TitleAchievementWatcher() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onAchievementGranted(AchievementEvent event) {
        if (event.entityPlayer instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.entityPlayer;
            // EntityPlayerMP posts AchievementEvent on every triggerAchievement
            // call, even when the statistic is already unlocked. Inventory
            // checks in HiTecch6AchivementsNEW run repeatedly, so only the
            // transition from locked to unlocked may create a card/title sync.
            if (player.func_147099_x().hasAchievementUnlocked(event.achievement)) return;
            String statId = event.achievement.statId;
            String pendingKey = player.getUniqueID().toString() + ':' + statId;
            pendingAchievements.put(pendingKey, new PendingAchievement(player, event.achievement));


        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        PacketRequestChangeTitle.Handler.drain();
        if (pendingAchievements.isEmpty()) return;
        Iterator<PendingAchievement> iterator = pendingAchievements.values().iterator();
        while (iterator.hasNext()) {
            PendingAchievement pending = iterator.next();
            EntityPlayerMP player = pending.player;
            if (player != null && player.playerNetServerHandler != null
                    && player.func_147099_x().hasAchievementUnlocked(pending.achievement)) {
                String statId = pending.achievement.statId;
            for (TitleManager.Title title : TitleManager.TITLE_DATABASE.values()) {
                if (title.achievementId != null && title.achievementId.equals(statId)) {
                    String username = player.getCommandSenderName();

                    TitleManager.activePlayerTitles.put(username, title.prefix);

                    // Mark data dirty on global Overworld storage
                    TitleSaveData.get(player.worldObj).markDirty();

                    // Sync to EVERY player in ALL dimensions
                    com.mod.htgt6.HTGT6.network.sendToAll(new PacketSyncActiveTitle(username, title.prefix));
                    break;
                }
            }
                com.mod.htgt6.HTGT6.network.sendToAll(new PacketAchievementChatCard(
                        player.getCommandSenderName(), pending.achievement.statId, pending.achievement.getSpecial()));
                iterator.remove();
            } else if (++pending.attempts >= 20) {
                iterator.remove();
            }
        }
    }

    private static final class PendingAchievement {
        private final EntityPlayerMP player;
        private final Achievement achievement;
        private int attempts;

        private PendingAchievement(EntityPlayerMP player, Achievement achievement) {
            this.player = player;
            this.achievement = achievement;
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            String username = player.getCommandSenderName();

            // Load saved NBT data on server startup/player join
            TitleSaveData.get(player.worldObj);
            PacketSyncPurchasedTitles.send(player);

            // 1. Assign novice if player has no recorded title in NBT
            if (!TitleManager.activePlayerTitles.containsKey(username)) {
                TitleManager.Title novice = TitleManager.TITLE_DATABASE.get("novice");
                if (novice != null) {
                    TitleManager.activePlayerTitles.put(username, novice.prefix);
                    TitleSaveData.get(player.worldObj).markDirty();

                    String welcomeAlert = "§d[HiTech6] §fИгрок §b<" + username + ">§f " + novice.announceMessage;
                    MinecraftServer server = player.mcServer;
                    if (server != null && server.getConfigurationManager() != null) {
                        server.getConfigurationManager().sendChatMsg(new ChatComponentText(welcomeAlert));
                    }
                }
            }

            // 2. Broadcast joining player's title to everyone online
            String currentPrefix = TitleManager.activePlayerTitles.getOrDefault(username, "");
            com.mod.htgt6.HTGT6.network.sendToAll(new PacketSyncActiveTitle(username, currentPrefix));

            // 3. Directly sync ALL currently stored player titles to the newly connected player
            for (Map.Entry<String, String> entry : TitleManager.activePlayerTitles.entrySet()) {
                com.mod.htgt6.HTGT6.network.sendTo(
                        new PacketSyncActiveTitle(entry.getKey(), entry.getValue()),
                        player
                );
            }
        }
    }

}
