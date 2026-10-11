package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import java.lang.reflect.Field;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerEconomy145;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerTrades146;
import java.util.Collections;
import java.util.Map;
import java.util.Locale;
import java.util.Random;
import java.util.WeakHashMap;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWanderingTrader120;

/** Minecraft 1.12 profession careers and progressive trade levels on the 1.7 villager. */
public final class VillagerCareers112 {
    public static final int VISUAL_DATA_WATCHER=30;
    private static final String[] VISUAL_TYPES={"plains","desert","jungle","savanna","snow","swamp","taiga"};
    private static final String[] VISUAL_PROFESSIONS={"none","armorer","butcher","cartographer","cleric",
            "farmer","fisherman","fletcher","leatherworker","librarian","mason","nitwit","shepherd",
            "toolsmith","weaponsmith"};
    private static final Map<EntityVillager, Data> DATA = Collections.synchronizedMap(
            new WeakHashMap<EntityVillager, Data>());
    private static Field buyingListField;

    public static void onSpawn(EntityVillager villager) {
        if(isWanderingTrader(villager))return;
        // Forge 1.7 selected one of 0..4. Re-roll vanilla professions to include
        // the 1.11/1.12 nitwit while leaving registered mod professions intact.
        if (villager.getProfession() >= 0 && villager.getProfession() <= 4)
            villager.setProfession(villager.getRNG().nextInt(10) == 0 ? 5 : 0);
        Data data = data(villager);
        data.career = randomCareer(villager.getProfession(), villager.getRNG());
        data.level = 0;
        data.modernProfession = villager.getProfession() == 5 ? "nitwit" : "none";
        refreshVisualData(villager);
    }

    /** Reserved vanilla-compatible watcher carrying type, profession and level to remote clients. */
    public static void initializeWatcher(EntityVillager villager){
        villager.getDataWatcher().addObject(VISUAL_DATA_WATCHER,Integer.valueOf(0));
    }

    public static void write(EntityVillager villager, NBTTagCompound tag) {
        if(isWanderingTrader(villager))return;
        Data data = data(villager);
        tag.setInteger("Career", data.career);
        tag.setInteger("CareerLevel", data.level);
        tag.setString("ProfessionName", professionName(villager.getProfession()));
        if (data.modernProfession.length() != 0) tag.setString("Profession120", data.modernProfession);
        if (data.villagerType.length() != 0) tag.setString("VillagerType120", data.villagerType);
        VillagerEconomy145.write(villager,tag);
    }

    /** Removes incompatible modern-template offers before vanilla serializes them. */
    public static void sanitizeOffers(EntityVillager villager) {
        if(isWanderingTrader(villager))return;
        MerchantRecipeList list = recipes(villager);
        if (list == null) return;
        for (int i = list.size() - 1; i >= 0; --i) {
            Object value = list.get(i);
            if (!(value instanceof MerchantRecipe)) { list.remove(i); continue; }
            MerchantRecipe recipe = (MerchantRecipe)value;
            if (recipe.getItemToBuy() == null || recipe.getItemToSell() == null) list.remove(i);
        }
        if (list.isEmpty()) {
            data(villager).level = 0;
            setRecipes(villager, null);
        }
    }

    public static void read(EntityVillager villager, NBTTagCompound tag) {
        if(isWanderingTrader(villager))return;
        if (tag.hasKey("ProfessionName", 8)) {
            villager.setProfession(professionId(tag.getString("ProfessionName"),
                    villager.getProfession()));
        }
        Data data = data(villager);
        data.career = tag.getInteger("Career");
        data.level = tag.getInteger("CareerLevel");
        data.modernProfession = tag.hasKey("Profession120", 8) ? tag.getString("Profession120")
                : inferModernProfession(villager.getProfession(), data.career);
        data.villagerType = tag.hasKey("VillagerType120",8) ? tag.getString("VillagerType120") : "";
        if (villager.getProfession() == 5) data.career = 1;
        // Repair villagers already saved with incompatible igloo Offers before
        // either interaction or the next autosave can dereference null stacks.
        sanitizeOffers(villager);
        VillagerEconomy145.read(villager,tag);
        refreshVisualData(villager);
    }

    /** Returns true for the six vanilla 1.12 professions, replacing legacy random trades. */
    public static boolean populate(EntityVillager villager, int ignoredLegacyCount) {
        if(isWanderingTrader(villager))return true;
        if(VillagerEconomy145.managed(villager))return true;
        int profession = villager.getProfession();
        if (profession < 0 || profession > 5) return false;
        Data data = data(villager);
        if (data.career <= 0) data.career = randomCareer(profession, villager.getRNG());
        MerchantRecipeList recipes = recipes(villager);
        if (recipes == null) {
            recipes = new MerchantRecipeList();
            setRecipes(villager, recipes);
            if(VillagerTrades146.supports(data.modernProfession)){VillagerTrades146.addRank(villager,recipes,1);refreshVisualData(villager);return true;}
        }
        if("none".equals(data.modernProfession)||"nitwit".equals(data.modernProfession))return true;
        int max = maxLevel(profession, data.career);
        if (data.level < max) {
            ++data.level;
            addLevel(recipes, villager, villager.getRNG(), profession, data.career, data.level);
        }
        refreshVisualData(villager);
        return true;
    }

    public static int getCareer(EntityVillager villager) { return data(villager).career; }
    public static int getCareerLevel(EntityVillager villager) { return VillagerEconomy145.managed(villager)?VillagerEconomy145.rank(villager):data(villager).level; }
    public static String getModernProfession(EntityVillager villager) {
        return isWanderingTrader(villager)?"wandering_trader":data(villager).modernProfession; }

    public static boolean hasTraded(EntityVillager villager) {
        if(VillagerEconomy145.hasTraded(villager))return true;
        MerchantRecipeList list = recipes(villager);
        if (list == null) return false;
        for (Object value : list) if (value instanceof MerchantRecipe
                && ((MerchantRecipe)value).writeToTags().getInteger("uses") > 0) return true;
        return false;
    }

    /** Assigns an exact 1.20 profession while retaining the compatible 1.12 renderer/trade family. */
    public static void setModernProfession(EntityVillager villager, String profession) {
        int legacy = legacyProfession(profession);
        int career = legacyCareer(profession);
        Data data = data(villager);
        boolean changed = !profession.equals(data.modernProfession)
                || villager.getProfession() != legacy || data.career != career;
        boolean locked = hasTraded(villager);
        villager.setProfession(legacy);
        data.modernProfession = profession;
        data.career = career;
        if (changed && !locked) { VillagerEconomy145.reset(villager);data.level = 0; setRecipes(villager, null); }
        refreshVisualData(villager);
    }

    public static void refreshVisualData(EntityVillager villager){
        if(villager.worldObj==null||villager.worldObj.isRemote)return;
        Data data=data(villager);
        if(data.villagerType.length()==0)data.villagerType=typeAt(villager);
        String profession=data.modernProfession.length()==0
                ?inferModernProfession(villager.getProfession(),data.career):data.modernProfession;
        int packed=index(VISUAL_TYPES,data.villagerType)
                |(index(VISUAL_PROFESSIONS,profession)<<3)|(Math.min(5,Math.max(0,getCareerLevel(villager)))<<7);
        villager.getDataWatcher().updateObject(VISUAL_DATA_WATCHER,Integer.valueOf(packed | (villager.getDataWatcher().getWatchableObjectInt(VISUAL_DATA_WATCHER)&~1023)));
    }

    public static String getVisualType(EntityVillager villager){
        int packed=villager.getDataWatcher().getWatchableObjectInt(VISUAL_DATA_WATCHER);
        return VISUAL_TYPES[Math.min(packed&7,VISUAL_TYPES.length-1)];
    }
    public static String getVisualProfession(EntityVillager villager){
        int packed=villager.getDataWatcher().getWatchableObjectInt(VISUAL_DATA_WATCHER);
        int value=(packed>>>3)&15;
        return VISUAL_PROFESSIONS[Math.min(value,VISUAL_PROFESSIONS.length-1)];
    }
    public static int getVisualLevel(EntityVillager villager){
        return Math.min(5,(villager.getDataWatcher().getWatchableObjectInt(VISUAL_DATA_WATCHER)>>>7)&7);
    }

    private static String typeAt(EntityVillager villager){
        BiomeGenBase biome=villager.worldObj.getBiomeGenForCoords(
                MathHelper.floor_double(villager.posX),MathHelper.floor_double(villager.posZ));
        String name=biome==null||biome.biomeName==null?"":biome.biomeName.toLowerCase(Locale.ROOT);
        if(name.contains("desert")||name.contains("mesa")||name.contains("badland"))return "desert";
        if(name.contains("jungle")||name.contains("bamboo"))return "jungle";
        if(name.contains("savanna"))return "savanna";
        if(name.contains("snow")||name.contains("ice")||name.contains("frozen")||name.contains("cold"))return "snow";
        if(name.contains("swamp")||name.contains("marsh")||name.contains("mangrove"))return "swamp";
        if(name.contains("taiga")||name.contains("conifer")||name.contains("redwood"))return "taiga";
        return "plains";
    }
    private static int index(String[] values,String value){
        for(int i=0;i<values.length;i++)if(values[i].equals(value))return i;
        return 0;
    }

    private static int legacyProfession(String value) {
        if ("librarian".equals(value) || "cartographer".equals(value)) return 1;
        if ("cleric".equals(value)) return 2;
        if ("armorer".equals(value) || "toolsmith".equals(value)
                || "weaponsmith".equals(value) || "mason".equals(value)) return 3;
        if ("butcher".equals(value) || "leatherworker".equals(value)) return 4;
        if ("nitwit".equals(value)) return 5;
        return 0;
    }

    private static int legacyCareer(String value) {
        if ("fisherman".equals(value)) return 2;
        if ("shepherd".equals(value)) return 3;
        if ("fletcher".equals(value)) return 4;
        if ("cartographer".equals(value)) return 2;
        if ("weaponsmith".equals(value)) return 2;
        if ("toolsmith".equals(value) || "mason".equals(value)) return 3;
        if ("leatherworker".equals(value)) return 2;
        return 1;
    }

    public static int legacyProfessionForFixture(String value) { return legacyProfession(value); }
    public static int legacyCareerForFixture(String value) { return legacyCareer(value); }

    private static String inferModernProfession(int profession, int career) {
        if (profession == 0) return career == 2 ? "fisherman" : career == 3 ? "shepherd"
                : career == 4 ? "fletcher" : "farmer";
        if (profession == 1) return career == 2 ? "cartographer" : "librarian";
        if (profession == 2) return "cleric";
        if (profession == 3) return career == 1 ? "armorer" : career == 2 ? "weaponsmith" : "toolsmith";
        if (profession == 4) return career == 2 ? "leatherworker" : "butcher";
        if (profession == 5) return "nitwit";
        return "none";
    }

    /** Nitwits use the normal entity interaction path, but never enter merchant mode. */
    public static boolean suppressNitwitTrade(EntityVillager villager, EntityPlayer player) {
        if(isWanderingTrader(villager))return false;
        ItemStack held = player.inventory.getCurrentItem();
        boolean spawnEgg = held != null && held.getItem() == Items.spawn_egg;
        String modern = getModernProfession(villager);
        boolean refused = (villager.getProfession() == 5 || "none".equals(modern) || modern.length() == 0)
                && !spawnEgg && villager.isEntityAlive()
                && !villager.isTrading() && !villager.isChild() && !player.isSneaking();
        if(refused&&!villager.worldObj.isRemote){
            setUnhappy(villager,40);
            villager.playSound("mob.villager.no",1F,1F);
        }
        return refused;
    }

    /** Reuse unused bits of the existing synchronized visual field; no extra watcher ID. */
    public static int getUnhappy(EntityVillager v){return (v.getDataWatcher().getWatchableObjectInt(VISUAL_DATA_WATCHER)>>>10)&63;}
    public static void setUnhappy(EntityVillager v,int ticks){
        int visual=v.getDataWatcher().getWatchableObjectInt(VISUAL_DATA_WATCHER)&1023;
        v.getDataWatcher().updateObject(VISUAL_DATA_WATCHER,Integer.valueOf(visual|(Math.max(0,Math.min(40,ticks))<<10)));
    }
    public static void tickUnhappy(EntityVillager v){if(!v.worldObj.isRemote&&getUnhappy(v)>0)setUnhappy(v,getUnhappy(v)-1);}

    public static void resetCareer(EntityVillager villager) {
        Data data = data(villager);
        data.career = randomCareer(villager.getProfession(), villager.getRNG());
        data.level = 0;
        setRecipes(villager, null);
        refreshVisualData(villager);
    }

    /** Restores the 1.12 Career/CareerLevel pair supplied by structure or entity NBT. */
    public static void restoreCareer(EntityVillager villager, int career, int level,
            NBTTagCompound offers) {
        Data data = data(villager);
        int count = careerCount(villager.getProfession());
        data.career = career > 0 && career <= count
                ? career : randomCareer(villager.getProfession(), villager.getRNG());
        data.level = Math.max(0, Math.min(level,
                maxLevel(villager.getProfession(), data.career)));
        // 1.10 template offers store namespaced string IDs, while 1.7 expects
        // numeric Item IDs. Loading them directly creates recipes with null
        // buy/sell stacks and crashes the next world save. Preserve career,
        // then let the compatible 1.12 trade builder unlock level one.
        data.level = 0;
        setRecipes(villager, null);
        refreshVisualData(villager);
    }

    private static void addLevel(MerchantRecipeList list, EntityVillager villager, Random random, int profession,
            int career, int level) {
        if ("mason".equals(data(villager).modernProfession)) { mason(list, random, level); return; }
        switch (profession) {
            case 0: farmer(list, random, career, level); break;
            case 1: librarian(list, villager, random, career, level); break;
            case 2: cleric(list, random, level); break;
            case 3: smith(list, random, career, level); break;
            case 4: butcher(list, random, career, level); break;
            default: break; // Nitwits intentionally have no offers.
        }
    }

    private static void mason(MerchantRecipeList list, Random r, int level) {
        if (level == 1) { buy(list, Item.getItemFromBlock(Blocks.clay), amount(r, 10, 14));
            sell(list, Item.getItemFromBlock(Blocks.brick_block), amount(r, 1, 2), 1); }
        else if (level == 2) { buy(list, Item.getItemFromBlock(Blocks.stone), amount(r, 16, 20));
            sell(list, new ItemStack(Blocks.stonebrick, 1, 0), amount(r, 1, 2), 1); }
        else if (level == 3) { sell(list, new ItemStack(Blocks.stonebrick, 1, 3), amount(r, 1, 2), 1);
            sell(list, Item.getItemFromBlock(Blocks.quartz_block), amount(r, 1, 2), 1); }
    }

    private static void farmer(MerchantRecipeList list, Random r, int career, int level) {
        if (career == 1) {
            if (level == 1) { buy(list, Items.wheat, amount(r, 18, 22)); buy(list, Items.potato, amount(r, 15, 19)); buy(list, Items.carrot, amount(r, 15, 19)); sell(list, Items.bread, 1, amount(r, 2, 4)); }
            else if (level == 2) { buy(list, Item.getItemFromBlock(Blocks.pumpkin), amount(r, 8, 13)); sell(list, Items.pumpkin_pie, 1, amount(r, 2, 3)); }
            else if (level == 3) { buy(list, Items.melon, amount(r, 7, 12)); sell(list, Items.apple, 1, amount(r, 5, 7)); }
            else { sell(list, Items.cookie, 1, amount(r, 6, 10)); sell(list, Items.cake, 1, 1); }
        } else if (career == 2) {
            if (level == 1) {
                buy(list, Items.string, amount(r, 15, 20));
                buy(list, Items.coal, amount(r, 16, 24));
                exchange(list, new ItemStack(Items.fish, 6), new ItemStack(Items.emerald),
                        new ItemStack(Items.cooked_fished, 6));
            } else if (level == 2) {
                sell(list, enchanted(r, Items.fishing_rod, 5), amount(r, 7, 8), 1);
            }
        } else if (career == 3) {
            if (level == 1) {
                buy(list, Item.getItemFromBlock(Blocks.wool), amount(r, 16, 22));
                sell(list, Items.shears, amount(r, 3, 4), 1);
            } else if (level == 2) {
                for (int color = 0; color < 16; ++color) {
                    sell(list, new ItemStack(Blocks.wool, 1, color), amount(r, 1, 2), 1);
                }
            }
        } else {
            if (level == 1) { buy(list, Items.string, amount(r, 15, 20)); sell(list, Items.arrow, 1, amount(r, 8, 12)); }
            else if (level == 2) {
                sell(list, Items.bow, amount(r, 2, 3), 1);
                exchange(list, new ItemStack(Blocks.gravel, 10), new ItemStack(Items.emerald),
                        new ItemStack(Items.flint, amount(r, 6, 10)));
            }
        }
    }

    private static void librarian(MerchantRecipeList list, EntityVillager villager, Random r, int career, int level) {
        if (career == 2) {
            if (level == 1) buy(list, Items.paper, amount(r, 24, 36));
            else if (level == 2) buy(list, Items.compass, 1);
            else if (level == 3) sell(list, Items.map, amount(r, 7, 11), 1);
            else if (level == 4) {
                com.mod.htgt6.minecraft1202.Module.Items.Exploration.ItemExplorerMap112 item =
                        com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112.explorerMap;
                ItemStack monument = item.create(villager.worldObj, "monument", (int)villager.posX, (int)villager.posZ);
                ItemStack mansion = item.create(villager.worldObj, "mansion", (int)villager.posX, (int)villager.posZ);
                if (monument != null) exchange(list, new ItemStack(Items.emerald, 12),
                        new ItemStack(Items.compass), monument);
                if (mansion != null) exchange(list, new ItemStack(Items.emerald, 16),
                        new ItemStack(Items.compass), mansion);
            }
            return;
        }
        if (level == 1) { buy(list, Items.paper, amount(r, 24, 36)); sell(list, enchanted(r, Items.book, 1), amount(r, 5, 64), 1); }
        else if (level == 2) { buy(list, Items.book, amount(r, 8, 10)); sell(list, Items.compass, amount(r, 10, 12), 1); sell(list, Item.getItemFromBlock(Blocks.bookshelf), 3, amount(r, 3, 4)); }
        else if (level == 3) { buy(list, Items.written_book, 2); sell(list, Items.clock, amount(r, 10, 12), 1); sell(list, Item.getItemFromBlock(Blocks.glass), 1, amount(r, 3, 5)); }
        else if (level == 4 || level == 5) sell(list, enchanted(r, Items.book, level), amount(r, 5, 64), 1);
        else if (level == 6) sell(list, Items.name_tag, amount(r, 20, 22), 1);
    }

    private static void cleric(MerchantRecipeList list, Random r, int level) {
        if (level == 1) { buy(list, Items.rotten_flesh, amount(r, 36, 40)); buy(list, Items.gold_ingot, amount(r, 8, 10)); }
        else if (level == 2) { sell(list, Items.redstone, 1, amount(r, 1, 4)); sell(list, new ItemStack(Items.dye, amount(r, 1, 2), 4), 1, 1); }
        else if (level == 3) { sell(list, Item.getItemFromBlock(Blocks.glowstone), 1, amount(r, 1, 3)); sell(list, Items.ender_pearl, amount(r, 4, 7), 1); }
        else sell(list, Items.experience_bottle, amount(r, 3, 11), 1);
    }

    private static void smith(MerchantRecipeList list, Random r, int career, int level) {
        if (career == 1) {
            if (level == 1) { buy(list, Items.coal, amount(r, 16, 24)); sell(list, Items.iron_helmet, amount(r, 4, 6), 1); }
            else if (level == 2) { buy(list, Items.iron_ingot, amount(r, 7, 9)); sell(list, Items.iron_chestplate, amount(r, 10, 14), 1); }
            else if (level == 3) { buy(list, Items.diamond, amount(r, 3, 4)); sell(list, enchanted(r, Items.diamond_chestplate, 10), amount(r, 16, 19), 1); }
            else if (level == 4) { sell(list, Items.chainmail_helmet, amount(r, 5, 7), 1); sell(list, Items.chainmail_chestplate, amount(r, 11, 15), 1); sell(list, Items.chainmail_boots, amount(r, 5, 7), 1); sell(list, Items.chainmail_leggings, amount(r, 9, 11), 1); }
        } else if (career == 2) {
            if (level == 1) { buy(list, Items.coal, amount(r, 16, 24)); sell(list, Items.iron_axe, amount(r, 6, 8), 1); }
            else if (level == 2) { buy(list, Items.iron_ingot, amount(r, 7, 9)); sell(list, enchanted(r, Items.iron_sword, 5), amount(r, 9, 10), 1); }
            else if (level == 3) { buy(list, Items.diamond, amount(r, 3, 4)); sell(list, enchanted(r, Items.diamond_sword, 10), amount(r, 12, 15), 1); sell(list, enchanted(r, Items.diamond_axe, 10), amount(r, 9, 12), 1); }
        } else {
            if (level == 1) { buy(list, Items.coal, amount(r, 16, 24)); sell(list, enchanted(r, Items.iron_shovel, 5), amount(r, 5, 7), 1); }
            else if (level == 2) { buy(list, Items.iron_ingot, amount(r, 7, 9)); sell(list, enchanted(r, Items.iron_pickaxe, 5), amount(r, 9, 11), 1); }
            else if (level == 3) { buy(list, Items.diamond, amount(r, 3, 4)); sell(list, enchanted(r, Items.diamond_pickaxe, 10), amount(r, 12, 15), 1); }
        }
    }

    private static void butcher(MerchantRecipeList list, Random r, int career, int level) {
        if (career == 1) {
            if (level == 1) { buy(list, Items.porkchop, amount(r, 14, 18)); buy(list, Items.chicken, amount(r, 14, 18)); }
            else if (level == 2) { buy(list, Items.coal, amount(r, 16, 24)); sell(list, Items.cooked_porkchop, 1, amount(r, 5, 7)); sell(list, Items.cooked_chicken, 1, amount(r, 6, 8)); }
        } else {
            if (level == 1) { buy(list, Items.leather, amount(r, 9, 12)); sell(list, Items.leather_leggings, amount(r, 2, 4), 1); }
            else if (level == 2) sell(list, enchanted(r, Items.leather_chestplate, 5), amount(r, 7, 12), 1);
            else if (level == 3) sell(list, Items.saddle, amount(r, 8, 10), 1);
        }
    }

    private static void buy(MerchantRecipeList list, Item item, int count) {
        list.addToListWithCheck(new MerchantRecipe(new ItemStack(item, count), new ItemStack(Items.emerald)));
    }
    private static void sell(MerchantRecipeList list, Item item, int emeralds, int count) {
        sell(list, new ItemStack(item, count), emeralds, count);
    }
    private static void sell(MerchantRecipeList list, ItemStack output, int emeralds, int ignored) {
        list.addToListWithCheck(new MerchantRecipe(new ItemStack(Items.emerald, emeralds), output));
    }
    private static void exchange(MerchantRecipeList list, ItemStack first, ItemStack second,
            ItemStack output) {
        list.addToListWithCheck(new MerchantRecipe(first, second, output));
    }
    private static ItemStack enchanted(Random random, Item item, int level) {
        return EnchantmentHelper.addRandomEnchantment(random, new ItemStack(item), level);
    }
    private static int amount(Random random, int min, int max) { return min + random.nextInt(max - min + 1); }

    private static int randomCareer(int profession, Random random) {
        int count = careerCount(profession);
        return 1 + random.nextInt(count);
    }
    private static int careerCount(int profession) {
        return profession == 0 ? 4 : profession == 1 || profession == 4 ? 2
                : profession == 3 ? 3 : 1;
    }
    private static int maxLevel(int profession, int career) {
        if (profession == 0) return career == 1 ? 4 : 2;
        if (profession == 1) return career == 1 ? 6 : 4;
        if (profession == 2) return 4;
        if (profession == 3) return career == 1 ? 4 : 3;
        if (profession == 4) return career == 1 ? 2 : 3;
        return 0;
    }
    public static String professionName(int profession) {
        String[] names = {"minecraft:farmer", "minecraft:librarian", "minecraft:priest",
                "minecraft:smith", "minecraft:butcher", "minecraft:nitwit"};
        return profession >= 0 && profession < names.length ? names[profession] : "htgt6:legacy_" + profession;
    }
    public static int professionId(String name, int fallback) {
        String[] names = {"minecraft:farmer", "minecraft:librarian", "minecraft:priest",
                "minecraft:smith", "minecraft:butcher", "minecraft:nitwit"};
        for (int i = 0; i < names.length; ++i) if (names[i].equals(name)) return i;
        return fallback;
    }
    private static Data data(EntityVillager villager) {
        Data result = DATA.get(villager);
        if (result == null) { result = new Data(); DATA.put(villager, result); }
        return result;
    }
    private static boolean isWanderingTrader(EntityVillager villager){return villager instanceof EntityWanderingTrader120;}
    public static MerchantRecipeList getOffersForEconomy(EntityVillager villager){return recipes(villager);}
    private static MerchantRecipeList recipes(EntityVillager villager) {
        try { return (MerchantRecipeList) buyingListField().get(villager); }
        catch (Exception error) { throw new IllegalStateException("Cannot read villager offers", error); }
    }
    private static void setRecipes(EntityVillager villager, MerchantRecipeList value) {
        try { buyingListField().set(villager, value); }
        catch (Exception error) { throw new IllegalStateException("Cannot write villager offers", error); }
    }
    private static Field buyingListField() {
        if (buyingListField != null) return buyingListField;
        for (Field field : EntityVillager.class.getDeclaredFields()) {
            if (MerchantRecipeList.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true); buyingListField = field; return field;
            }
        }
        throw new IllegalStateException("EntityVillager buyingList field was not found");
    }
    private static final class Data { int career; int level; String modernProfession = "",villagerType=""; }
    private VillagerCareers112() { }
}
