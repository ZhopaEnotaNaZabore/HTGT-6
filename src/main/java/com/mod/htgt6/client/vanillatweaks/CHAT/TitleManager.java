package com.mod.htgt6.client.vanillatweaks.CHAT;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;




public class TitleManager {

    public static class Title {
        public final String id;
        public final String prefix;
        public final String achievementId;
        public final String announceMessage; // Custom announcement message string
        public final boolean isOpOnly;
        public final int coinPrice;

        public Title(String id, String prefix, String achievementId, String announceMessage, boolean isOpOnly) {
            this.id = id;
            this.prefix = prefix;
            this.achievementId = achievementId;
            this.announceMessage = announceMessage;
            this.isOpOnly = isOpOnly;
            this.coinPrice = 0;
        }
        public Title(String id, String prefix, int coinPrice) { this.id=id; this.prefix=prefix; this.achievementId=null; this.announceMessage=""; this.isOpOnly=false; this.coinPrice=coinPrice; }
        public Title(String id, String prefix, String achievementId, String announceMessage) {
            this(id, prefix, achievementId, announceMessage, false);
        }
    }

    public static final Map<String, Title> TITLE_DATABASE = new LinkedHashMap<>();
    public static final Map<String, String> activePlayerTitles = new ConcurrentHashMap<>();
    public static final List<String> clientUnlockedTitles = new ArrayList<>();
    public static final Map<String, Set<String>> purchasedTitles = new ConcurrentHashMap<>(); // UUID -> title ids

    static {
        // Novice  (Авто при первом входе)
        register(new Title("novice", "§a[Novice] §r", null, "только прибыл на сервер!, дайте ему картошки и пару полезных советов."));

        // Тировые
        register(new Title("ulv", "§8[ULV] §r", "advancement.HiTech6.ULVtier", "Поздравляем с достижением ULV тира! 8 вольт тебе в тапки!"));
        register(new Title("lv", "§b[LV] §r", "advancement.HiTech6.LVtier", "достиг низкого вольтажа (LV)! Теперь механизмы работают быстрее."));
        register(new Title("mv", "§6[MV] §r", "advancement.HiTech6.MVtier", "перешёл на средний вольтаж (MV)! Производство набирает обороты."));
        register(new Title("hv", "§e[HV] §r", "advancement.HiTech6.HVtier", "освоил высокое напряжение (HV)! Будьте аккуратнее с проводами."));
        register(new Title("ev", "§d[EV] §r", "advancement.HiTech6.EVTIER", "ворвался в эру Extreme Voltage (EV)! Мощность зашкаливает!"));
        register(new Title("iv", "§1[IV] §r", "advancement.HiTech6.IVtier", "достиг индустриального вольтажа (IV)! Полная автоматизация близко."));
        //adv
        register(new Title("luv", "§3§l[LuV] §r", "advancement.HiTech6.LUVtier", "Достиг (LuV) тира! Роскошь инженерии."));
        register(new Title("zpm", "§b§l§n[ZPM] §r", "advancement.HiTech6.ZPMtier", "обуздал энергию нулевой точки (ZPM)! Бесконечные ресурсы ждут."));
        register(new Title("uv", "§2§l[UV] §r", "advancement.HiTech6.UVtier", "достиг ультрафиолетового вольтажа (UV)! Космический масштаб."));
        register(new Title("puv", "§4§l§k|§r§4§lPUV§4§l§k|§r ", "advancement.HiTech6.PUVtier", "вышел за грани разумного на тире PUV! Реальность трещит по швам!"));
        register(new Title("max", "§d§l§k|§r§c§lM§e§lA§a§lX§d§l§k|§r ", "advancement.HiTech6.MAXtier", "ДОСТИГ АБСОЛЮТНОГО МАКСИМУМА (MAX)! Легенда этого сервера!"));

        // Existing examples
        register(new Title("explorer", "§e[Explorer] §r", "achievement.exploreAllBiomes", "unlocked the secrets of nature!"));
        register(new Title("slayer", "§4[Wither Slayer] §r", "achievement.killWither", "slayed the mighty Wither!"));

        // NEW: Special OP Only Titles (achievementId is null, isOpOnly is true)
        register(new Title("admin", "§c[Admin] §r", null, "has assumed administrative duties!", true));
        register(new Title("owner", "§4[Owner] §r", null, "logged in to rule!", true));
    }

    static {
        register(new Title("veteran", "§d[Veteran] §r", 48));
        register(new Title("legend", "§b§l[Legend] §r", 128));
    }

    static {
        register(new Title("supporter", "§6[Sponsor] §r", 5));
        register(new Title("maiden", "§c§l[Maiden] §r", 12));
        register(new Title("slayer_shop", "§4[Slayer] §r", 5));
        register(new Title("eternity", "§1§l[Eternity] §r", 15));
        register(new Title("radox", "§5[RadoX] §r", 5));
        register(new Title("infinity_horizons", "§3§l[InfinityHorizons] §r", "advancement.HiTech6.InfinityHorizons", "reached the infinite horizon!"));
        register(new Title("space_traveler", "§9[SpaceTraveler] §r", "advancement.HiTech6.SpaceTraveler", "became a space traveler!"));
        // Decorative wings and a frame identify the closed-beta tester title at a glance.
        register(new Title("cbt_dev", "§b≪§d✦ §f§l[CBT-DEV] §d✦§b≫ §r", null, "", true));
        register(new Title("gmc", "§4§l[GMC] §r", null, "", true));
    }

    private static void register(Title title) {
        TITLE_DATABASE.put(title.id, title);
    }

    public static boolean isPurchased(String playerId, String titleId) { Set<String> titles=purchasedTitles.get(playerId); return titles != null && titles.contains(titleId); }
    public static void markPurchased(String playerId, String titleId) { Set<String> titles=purchasedTitles.get(playerId); if(titles==null){titles=Collections.newSetFromMap(new ConcurrentHashMap<String,Boolean>()); purchasedTitles.put(playerId,titles);} titles.add(titleId); }
}
