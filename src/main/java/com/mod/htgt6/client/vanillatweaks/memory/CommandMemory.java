package com.mod.htgt6.client.vanillatweaks.memory;

import com.mod.htgt6.client.vanillatweaks.CHAT.ChatManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.client.Minecraft;

import java.util.List;

public class CommandMemory extends CommandBase {

    @Override
    public String getCommandName() {
        return "mem";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/mem <clear | check | help | shutdown>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // Local client clearance
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "clear", "check", "help", "shutdown");
        }
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpIndex();
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "clear":
                long beforeMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

                // Triggers our updated safe live asset purge
                MemoryCleaner.executeManualCleanup();

                long afterMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
                long bytesFreed = beforeMemory - afterMemory;

                ChatManager.addMessage("§a[Memory] §7Aggressive runtime memory purging sequence complete.");
                if (bytesFreed > 0) {
                    ChatManager.addMessage("§a[Memory] §7Successfully purged: §b" + bytesFreed + " MB §7from JVM heap.");
                } else {
                    ChatManager.addMessage("§a[Memory] §7Heap usage is already at minimum limits. No garbage collected.");
                }
                break;

            case "check":
                Runtime runtime = Runtime.getRuntime();
                long maxMem = runtime.maxMemory() / (1024 * 1024);
                long totalMem = runtime.totalMemory() / (1024 * 1024);
                long freeMem = runtime.freeMemory() / (1024 * 1024);
                long usedMem = totalMem - freeMem;

                double ratioUsed = ((double) usedMem / maxMem) * 100;
                String textHighlightColor = ratioUsed > 85 ? "§c" : (ratioUsed > 60 ? "§e" : "§a");

                ChatManager.addMessage("§d§l=== Live Heap Performance Metrics ===");
                ChatManager.addMessage("§7Used JVM Memory: " + textHighlightColor + usedMem + " MB §7/ §f" + maxMem + " MB (" + String.format("%.1f", ratioUsed) + "%)");
                ChatManager.addMessage("§7Total Allocated Size: §f" + totalMem + " MB");
                ChatManager.addMessage("§7Unallocated Free Buffer: §f" + freeMem + " MB");
                ChatManager.addMessage("§d§l=====================================");
                break;

            case "shutdown":
            case "shuttdown":
                ChatManager.addMessage("§c[Memory] Shutting down client application instantly...");
                Minecraft.getMinecraft().shutdown();
                break;

            default:
                ChatManager.addMessage("§cUnknown operation target. Use §e/mem help §cfor commands context index.");
                break;
        }
    }

    private void sendHelpIndex() {
        ChatManager.addMessage("§b§l=== Client Memory Utility ===");
        ChatManager.addMessage("§e/mem clear §7-Вручную очистить кишированный мусор");
        ChatManager.addMessage("§e/mem check §7-Посмотреть текущий статус памяти.");
        ChatManager.addMessage("§e/mem shutdown §7-Когда надоело играть вырубить нахрен игру и идти трогать траву");
        ChatManager.addMessage("§e/mem help §7-Пособие по CLU");
        ChatManager.addMessage("§b§l========================================");
    }
}