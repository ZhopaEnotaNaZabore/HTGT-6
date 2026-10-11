package com.mod.htgt6.minecraft1202.Module.World.Exploration;

import com.google.gson.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import java.io.*;
import java.util.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** The bundled official 1.20.2 mansion table, with explicit legacy item adapters. */
public final class WoodlandMansionLoot120 {
    private static final JsonArray POOLS=load();
    private WoodlandMansionLoot120(){}
    private static JsonArray load(){
        InputStream stream=WoodlandMansionLoot120.class.getResourceAsStream("/assets/htgt6/loot_tables/chests/woodland_mansion.json");
        if(stream==null)throw new IllegalStateException("Missing mansion loot table");
        try(Reader reader=new InputStreamReader(stream,"UTF-8")){
            return new JsonParser().parse(reader).getAsJsonObject().getAsJsonArray("pools");
        }catch(IOException error){throw new IllegalStateException("Cannot read mansion loot",error);}
    }
    public static List<ItemStack> generate(Random random){
        List<ItemStack> result=new ArrayList<ItemStack>();
        for(JsonElement value:POOLS){
            JsonObject pool=value.getAsJsonObject();int rolls=number(pool.get("rolls"),random);
            for(int i=0;i<rolls;i++){ItemStack stack=roll(pool,random);if(stack!=null)result.add(stack);}
        }
        return result;
    }
    private static ItemStack roll(JsonObject pool,Random random){
        JsonArray entries=pool.getAsJsonArray("entries");int total=0;
        for(JsonElement entry:entries)total+=weight(entry.getAsJsonObject());
        int choice=random.nextInt(total);
        for(JsonElement value:entries){
            JsonObject entry=value.getAsJsonObject();choice-=weight(entry);if(choice>=0)continue;
            if("minecraft:empty".equals(entry.get("type").getAsString()))return null;
            ItemStack stack=item(entry.get("name").getAsString());if(stack==null)return null;
            if(entry.has("functions"))for(JsonElement function:entry.getAsJsonArray("functions")){
                JsonObject fn=function.getAsJsonObject();String name=fn.get("function").getAsString();
                if("minecraft:set_count".equals(name))stack.stackSize=number(fn.get("count"),random);
                else if("minecraft:enchant_randomly".equals(name))stack=enchantBook(random);
                else throw new IllegalStateException("Unsupported mansion loot function "+name);
            }
            return stack;
        }
        throw new AssertionError("Invalid mansion pool weight");
    }
    private static int weight(JsonObject entry){return entry.has("weight")?entry.get("weight").getAsInt():1;}
    private static int number(JsonElement value,Random random){
        if(value.isJsonPrimitive())return value.getAsInt();
        JsonObject range=value.getAsJsonObject();int min=range.get("min").getAsInt(),max=range.get("max").getAsInt();
        return min+random.nextInt(max-min+1);
    }
    private static ItemStack item(String name){
        if(name.equals("minecraft:enchanted_golden_apple"))return new ItemStack(Items.golden_apple,1,1);
        if(name.equals("minecraft:music_disc_13"))return new ItemStack(Items.record_13);
        if(name.equals("minecraft:music_disc_cat"))return new ItemStack(Items.record_cat);
        if(name.equals("minecraft:beetroot_seeds"))return new ItemStack(CombatContent.beetrootSeeds);
        // The trim module is not ported yet. Keep its independent 50% pool in the source table,
        // but do not invent a nonfunctional item or substitute an unrelated reward.
        if(name.equals("minecraft:vex_armor_trim_smithing_template"))return null;
        Item item=(Item)Item.itemRegistry.getObject(name);
        if(item==null)throw new IllegalStateException("Unmapped mansion loot item "+name);
        return new ItemStack(item);
    }
    private static ItemStack enchantBook(Random random){
        List<Enchantment> options=new ArrayList<Enchantment>();
        for(Enchantment enchantment:Enchantment.enchantmentsList)
            if(enchantment!=null&&enchantment.isAllowedOnBooks())options.add(enchantment);
        if(options.isEmpty())throw new IllegalStateException("No book enchantments registered");
        Enchantment enchantment=options.get(random.nextInt(options.size()));
        int level=enchantment.getMinLevel()+random.nextInt(enchantment.getMaxLevel()-enchantment.getMinLevel()+1);
        return Items.enchanted_book.getEnchantedItemStack(new EnchantmentData(enchantment,level));
    }
    public static void fill(IInventory inventory,Random random){
        List<ItemStack> loot=generate(random),split=new ArrayList<ItemStack>();
        List<Integer> free=new ArrayList<Integer>();
        for(int slot=0;slot<inventory.getSizeInventory();slot++)if(inventory.getStackInSlot(slot)==null)free.add(slot);
        Collections.shuffle(free,random);
        for(Iterator<ItemStack> it=loot.iterator();it.hasNext();){ItemStack stack=it.next();if(stack.stackSize>1){split.add(stack);it.remove();}}
        while(free.size()-loot.size()-split.size()>0&&!split.isEmpty()){
            ItemStack stack=split.remove(random.nextInt(split.size()));ItemStack part=stack.splitStack(1+random.nextInt(stack.stackSize/2));
            if(stack.stackSize>1&&random.nextBoolean())split.add(stack);else loot.add(stack);
            if(part.stackSize>1&&random.nextBoolean())split.add(part);else loot.add(part);
        }
        loot.addAll(split);Collections.shuffle(loot,random);
        for(ItemStack stack:loot){if(free.isEmpty())break;inventory.setInventorySlotContents(free.remove(free.size()-1),stack);}
        inventory.markDirty();
    }
}
