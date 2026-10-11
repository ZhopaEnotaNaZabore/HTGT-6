package com.mod.htgt6.common.agent;

import cpw.mods.fml.common.Loader;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.FakePlayer;

import java.util.Set;

/** Operator-only command surface for the in-game diagnostic fake player. */
public final class CommandHTGT6Agent extends CommandBase {
    public String getCommandName() { return "htgtagent"; }
    public String getCommandUsage(ICommandSender sender) {
        return "/htgtagent <status|summon|inspect|recipes|quest|help>";
    }
    public int getRequiredPermissionLevel() { return 2; }

    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) { help(sender); return; }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        FakePlayer agent = HTGT6AgentController.at(player);
        try {
            if ("summon".equalsIgnoreCase(args[0])) {
                say(sender, "Агент активен в dimension " + agent.dimension + " у "
                        + round(agent.posX) + ", " + round(agent.posY) + ", " + round(agent.posZ)
                        + ". Это серверный FakePlayer без отдельного сетевого клиента.");
            } else if ("status".equalsIgnoreCase(args[0])) {
                String quests = Loader.isModLoaded("betterquesting")
                        ? String.valueOf(BetterQuestingAgentAdapter.count()) : "BetterQuesting не загружен";
                say(sender, "Агент: " + agent.getCommandSenderName() + ", dimension=" + agent.dimension
                        + ", quests=" + quests + "\n" + AgentRecipeInspector.summary());
            } else if ("inspect".equalsIgnoreCase(args[0])) {
                inspect(sender, player);
            } else if ("recipes".equalsIgnoreCase(args[0])) {
                say(sender, args.length > 1 && "summary".equalsIgnoreCase(args[1])
                        ? AgentRecipeInspector.summary() : AgentRecipeInspector.findForOutput(player.getHeldItem()));
            } else if ("quest".equalsIgnoreCase(args[0])) {
                quest(sender, player, args);
            } else {
                throw new WrongUsageException(getCommandUsage(sender));
            }
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        } catch (Exception e) {
            throw new CommandException("Ошибка агента: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static void quest(ICommandSender sender, EntityPlayerMP player, String[] args) throws Exception {
        if (!Loader.isModLoaded("betterquesting")) throw new CommandException("BetterQuesting не загружен.");
        if (args.length < 2) throw new WrongUsageException(
                "/htgtagent quest <list [поиск]|show <uuid>|set <uuid> <name|description> <текст>|undo>");
        if ("list".equalsIgnoreCase(args[1])) {
            say(sender, BetterQuestingAgentAdapter.list(args.length > 2 ? join(args, 2) : "", 12));
        } else if ("show".equalsIgnoreCase(args[1]) && args.length == 3) {
            say(sender, BetterQuestingAgentAdapter.describe(args[2], player));
        } else if ("set".equalsIgnoreCase(args[1]) && args.length >= 5) {
            say(sender, BetterQuestingAgentAdapter.setText(args[2], args[3], join(args, 4), player));
        } else if ("undo".equalsIgnoreCase(args[1]) && args.length == 2) {
            say(sender, BetterQuestingAgentAdapter.undo());
        } else {
            throw new WrongUsageException(
                    "/htgtagent quest <list [поиск]|show <uuid>|set <uuid> <name|description> <текст>|undo>");
        }
    }

    @SuppressWarnings("rawtypes")
    private static void inspect(ICommandSender sender, EntityPlayerMP player) {
        MovingObjectPosition hit = player.rayTrace(8.0D, 1.0F);
        if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            say(sender, "В пределах 8 блоков объект не найден."); return;
        }
        int x = hit.blockX, y = hit.blockY, z = hit.blockZ;
        Block block = player.worldObj.getBlock(x, y, z);
        Object blockId = Block.blockRegistry.getNameForObject(block);
        StringBuilder result = new StringBuilder("Блок ").append(blockId).append(':')
                .append(player.worldObj.getBlockMetadata(x, y, z)).append(" @ ")
                .append(x).append(',').append(y).append(',').append(z)
                .append(" dim=").append(player.dimension);
        TileEntity tile = player.worldObj.getTileEntity(x, y, z);
        if (tile != null) {
            NBTTagCompound tag = new NBTTagCompound();
            tile.writeToNBT(tag);
            Set keys = tag.func_150296_c();
            result.append("\nTileEntity: ").append(tile.getClass().getName())
                    .append("\nNBT keys: ").append(keys);
        }
        say(sender, result.toString());
    }

    private static void help(ICommandSender sender) {
        say(sender, "§6HTGT6 Agent§r\n"
                + "/htgtagent summon — создать/переместить контекст FakePlayer\n"
                + "/htgtagent inspect — прочитать блок и TileEntity перед вами\n"
                + "/htgtagent recipes [summary] — рецепты предмета в руке или сводка\n"
                + "/htgtagent quest list [поиск]\n"
                + "/htgtagent quest show <uuid или префикс>\n"
                + "/htgtagent quest set <uuid> <name|description> <текст>\n"
                + "/htgtagent quest undo");
    }

    private static String join(String[] args, int start) {
        StringBuilder out = new StringBuilder();
        for (int i = start; i < args.length; i++) { if (i > start) out.append(' '); out.append(args[i]); }
        return out.toString();
    }

    private static int round(double value) { return (int) Math.floor(value); }
    private static void say(ICommandSender sender, String text) {
        for (String line : text.split("\\n")) sender.addChatMessage(new ChatComponentText(line));
    }
}
