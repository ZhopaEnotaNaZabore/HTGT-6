package com.mod.htgt6.common.agent;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.NBTConverter;
import betterquesting.network.handlers.NetQuestEdit;
import betterquesting.questing.QuestDatabase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;

/** BetterQuesting operations used by the agent. All mutations pass through BQ sync. */
public final class BetterQuestingAgentAdapter {
    private static final int MAX_UNDO = 20;
    private static final Deque<QuestSnapshot> UNDO = new ArrayDeque<QuestSnapshot>();

    private BetterQuestingAgentAdapter() {}

    public static int count() {
        return QuestDatabase.INSTANCE.size();
    }

    public static String list(String filter, int limit) {
        String needle = filter == null ? "" : filter.toLowerCase(java.util.Locale.ROOT);
        StringBuilder out = new StringBuilder();
        int found = 0;
        for (Map.Entry<UUID, IQuest> entry : QuestDatabase.INSTANCE.entrySet()) {
            String name = entry.getValue().getProperty(NativeProps.NAME, "");
            if (!needle.isEmpty() && !name.toLowerCase(java.util.Locale.ROOT).contains(needle)
                    && !entry.getKey().toString().startsWith(needle)) continue;
            if (found++ > 0) out.append("\n");
            out.append(shortId(entry.getKey())).append(" — ").append(name.isEmpty() ? "<без названия>" : name);
            if (found >= limit) break;
        }
        return found == 0 ? "Квесты не найдены." : out.toString();
    }

    public static String describe(String token, EntityPlayerMP viewer) {
        UUID id = resolve(token);
        IQuest quest = QuestDatabase.INSTANCE.get(id);
        String name = quest.getProperty(NativeProps.NAME, "");
        String desc = quest.getProperty(NativeProps.DESC, "").replace('\n', ' ');
        if (desc.length() > 180) desc = desc.substring(0, 177) + "...";
        return "Квест " + id + "\nНазвание: " + name + "\nОписание: " + desc
                + "\nЗадачи: " + quest.getTasks().size() + ", награды: " + quest.getRewards().size()
                + ", зависимости: " + quest.getRequirements().size() + ", состояние: " + quest.getState(viewer);
    }

    public static String setText(String token, String field, String value, EntityPlayerMP operator) throws IOException {
        UUID id = resolve(token);
        IQuest quest = QuestDatabase.INSTANCE.get(id);
        boolean nameField = "name".equalsIgnoreCase(field);
        boolean descriptionField = "description".equalsIgnoreCase(field) || "desc".equalsIgnoreCase(field);
        if (!nameField && !descriptionField) {
            throw new IllegalArgumentException("Разрешены поля name и description.");
        }
        NBTTagCompound before = quest.writeToNBT(new NBTTagCompound());
        remember(id, before, operator.getCommandSenderName());

        if (nameField) {
            quest.setProperty(NativeProps.NAME, value);
        } else if (descriptionField) {
            quest.setProperty(NativeProps.DESC, value);
        }
        sync(id, quest.writeToNBT(new NBTTagCompound()));
        return "Квест " + shortId(id) + " обновлён. Резервная копия создана; /htgtagent quest undo для отмены.";
    }

    public static String undo() {
        QuestSnapshot snapshot = UNDO.pollFirst();
        if (snapshot == null) return "Нет изменений для отмены в текущем запуске сервера.";
        sync(snapshot.id, snapshot.config);
        return "Изменение квеста " + shortId(snapshot.id) + " отменено.";
    }

    private static void sync(UUID id, NBTTagCompound config) {
        NBTTagCompound entry = new NBTTagCompound();
        NBTConverter.UuidValueType.QUEST.writeId(id, entry);
        entry.setTag("config", config.copy());
        NBTTagList edits = new NBTTagList();
        edits.appendTag(entry);
        NetQuestEdit.editQuests(edits);
    }

    private static void remember(UUID id, NBTTagCompound config, String operator) throws IOException {
        QuestSnapshot snapshot = new QuestSnapshot(id, (NBTTagCompound) config.copy());
        UNDO.addFirst(snapshot);
        while (UNDO.size() > MAX_UNDO) UNDO.removeLast();

        File dir = MinecraftServer.getServer().getFile("htgt6-agent-backups");
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("Не удалось создать " + dir.getAbsolutePath());
        NBTTagCompound backup = new NBTTagCompound();
        backup.setString("quest", id.toString());
        backup.setString("operator", operator);
        backup.setLong("createdAt", System.currentTimeMillis());
        backup.setTag("config", config.copy());
        File file = new File(dir, System.currentTimeMillis() + "-" + id + ".dat");
        FileOutputStream stream = new FileOutputStream(file);
        try {
            CompressedStreamTools.writeCompressed(backup, stream);
        } finally {
            stream.close();
        }
    }

    private static UUID resolve(String token) {
        try {
            UUID exact = UUID.fromString(token);
            if (QuestDatabase.INSTANCE.containsKey(exact)) return exact;
        } catch (IllegalArgumentException ignored) { }
        UUID match = null;
        String prefix = token.toLowerCase(java.util.Locale.ROOT);
        for (UUID id : QuestDatabase.INSTANCE.keySet()) {
            if (!id.toString().startsWith(prefix)) continue;
            if (match != null) throw new IllegalArgumentException("Префикс UUID неоднозначен: " + token);
            match = id;
        }
        if (match == null) throw new IllegalArgumentException("Квест не найден: " + token);
        return match;
    }

    private static String shortId(UUID id) {
        return id.toString().substring(0, 8);
    }

    private static final class QuestSnapshot {
        final UUID id;
        final NBTTagCompound config;
        QuestSnapshot(UUID id, NBTTagCompound config) { this.id = id; this.config = config; }
    }
}
