package com.mod.htgt6.client.vanillatweaks.CHAT;

import com.mod.htgt6.HTGT6;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandTitleAdmin extends CommandBase {
    public String getCommandName() { return "htgt6title"; }
    public String getCommandUsage(ICommandSender sender) { return "/htgt6title <player> <titleId|none>"; }
    public int getRequiredPermissionLevel() { return 2; }
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 2) { sender.addChatMessage(new net.minecraft.util.ChatComponentText(getCommandUsage(sender))); return; }
        EntityPlayerMP target = MinecraftServer.getServer().getConfigurationManager().func_152612_a(args[0]);
        if (target == null) { sender.addChatMessage(new net.minecraft.util.ChatComponentText("§cPlayer is offline.")); return; }
        String prefix = "";
        if (!"none".equals(args[1])) { TitleManager.Title title = TitleManager.TITLE_DATABASE.get(args[1]); if (title == null) { sender.addChatMessage(new net.minecraft.util.ChatComponentText("§cUnknown title id.")); return; } prefix = title.prefix; }
        if (prefix.isEmpty()) TitleManager.activePlayerTitles.remove(target.getCommandSenderName()); else TitleManager.activePlayerTitles.put(target.getCommandSenderName(), prefix);
        TitleSaveData.get(target.worldObj).markDirty();
        HTGT6.network.sendToAll(new PacketSyncActiveTitle(target.getCommandSenderName(), prefix));
        sender.addChatMessage(new net.minecraft.util.ChatComponentText("§aTitle updated for " + target.getCommandSenderName()));
    }
}
