package com.mod.htgt6.minecraft1202.Module.World.Modern;
import com.google.gson.*;
import java.io.*;
import java.util.*;
import net.minecraft.item.*;
import net.minecraft.init.*;
import net.minecraft.inventory.IInventory;
import net.minecraft.enchantment.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerTradeItems146;
/** Evaluator for the functions present in the 17 official village/outpost chest tables. */
public final class VillageStructureLoot120 {
    private static final Map<String,JsonObject> TABLES=new HashMap<String,JsonObject>();
    public static synchronized JsonObject table(String id){
        JsonObject value=TABLES.get(id);if(value!=null)return value;
        if(!id.startsWith("minecraft:chests/village/")&&!id.equals("minecraft:chests/pillager_outpost")&&!Arrays.asList("minecraft:chests/bastion_bridge","minecraft:chests/bastion_hoglin_stable","minecraft:chests/bastion_other","minecraft:chests/bastion_treasure").contains(id))throw new IllegalArgumentException(id);
        try(InputStream in=BackportResourceLoader120.open(VillageStructureLoot120.class,"/assets/htgt6/loot_tables/"+id.substring(10)+".json")){
            if(in==null)throw new IllegalStateException("Missing loot table "+id);
            value=new JsonParser().parse(new InputStreamReader(in,"UTF-8")).getAsJsonObject();TABLES.put(id,value);return value;
        }catch(IOException e){throw new IllegalStateException(id,e);}
    }
    public static List<ItemStack> roll(String id,Random random){
        List<ItemStack> result=new ArrayList<ItemStack>();
        for(JsonElement value:table(id).getAsJsonArray("pools")){
            JsonObject pool=value.getAsJsonObject();JsonArray entries=pool.getAsJsonArray("entries");int total=0;
            for(JsonElement e:entries)total+=weight(e.getAsJsonObject());
            int rolls=number(pool.get("rolls"),random);
            for(int i=0;i<rolls;i++){
                int choice=random.nextInt(total);JsonObject entry=null;
                for(JsonElement e:entries){choice-=weight(e.getAsJsonObject());if(choice<0){entry=e.getAsJsonObject();break;}}
                if("minecraft:empty".equals(entry.get("type").getAsString()))continue;
                ItemStack stack=item(entry.get("name").getAsString());
                stack=functions(stack,entry.getAsJsonArray("functions"),random);stack=functions(stack,pool.getAsJsonArray("functions"),random);
                while(stack.stackSize>0){ItemStack part=stack.copy();part.stackSize=Math.min(stack.stackSize,stack.getMaxStackSize());result.add(part);stack.stackSize-=part.stackSize;}
            }
        }
        return result;
    }
    private static int weight(JsonObject e){return e.has("weight")?e.get("weight").getAsInt():1;}
    private static int number(JsonElement e,Random r){if(e.isJsonPrimitive())return e.getAsInt();JsonObject n=e.getAsJsonObject();int lo=n.get("min").getAsInt(),hi=n.get("max").getAsInt();return lo+r.nextInt(hi-lo+1);}
    private static ItemStack functions(ItemStack stack,JsonArray functions,Random r){
        if(functions==null)return stack;
        for(JsonElement e:functions){JsonObject f=e.getAsJsonObject();String name=f.get("function").getAsString();
            if(name.equals("minecraft:set_count"))stack.stackSize=number(f.get("count"),r)+(f.has("add")&&f.get("add").getAsBoolean()?stack.stackSize:0);
            else if(name.equals("minecraft:set_damage")){
                JsonElement value=f.get("damage");float remaining;if(value.isJsonPrimitive())remaining=value.getAsFloat();else{JsonObject range=value.getAsJsonObject();float min=range.get("min").getAsFloat();remaining=min+r.nextFloat()*(range.get("max").getAsFloat()-min);}
                if(f.has("add")&&f.get("add").getAsBoolean())remaining+=1-(float)stack.getItemDamage()/stack.getMaxDamage();
                remaining=Math.max(0,Math.min(1,remaining));stack.setItemDamage((int)Math.floor((1-remaining)*stack.getMaxDamage()));
            }
            else if(name.equals("minecraft:set_instrument"))stack.setItemDamage(r.nextInt(4));
            else if(name.equals("minecraft:enchant_randomly")){
                List<Enchantment> available=new ArrayList<Enchantment>();
                if(f.has("enchantments"))for(JsonElement id:f.getAsJsonArray("enchantments")){
                    if(!id.getAsString().equals("minecraft:soul_speed"))throw new IllegalArgumentException("Unmapped explicit loot enchantment "+id);
                    com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.SoulSpeed116.init();available.add(com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.SoulSpeed116.enchantment);
                }else for(Enchantment ench:Enchantment.enchantmentsList)if(ench!=null&&ench.isAllowedOnBooks()
                        &&(stack.getItem()==Items.book||ench.canApply(stack))
                        &&!ench.getName().toLowerCase(Locale.ROOT).contains("soul_speed")&&!ench.getName().toLowerCase(Locale.ROOT).contains("swift_sneak"))available.add(ench);
                if(!available.isEmpty()){Enchantment ench=available.get(r.nextInt(available.size()));int level=ench.getMinLevel()+r.nextInt(ench.getMaxLevel()-ench.getMinLevel()+1);
                    if(stack.getItem()==Items.book)stack=Items.enchanted_book.getEnchantedItemStack(new EnchantmentData(ench,level));else stack.addEnchantment(ench,level);}
            }else throw new IllegalArgumentException("Unsupported village loot function "+name);
        }return stack;
    }
    public static ItemStack item(String id){
        String name=id.substring(id.indexOf(':')+1);
        ItemStack nether=com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherTreasure116.resolve(name);if(nether!=null)return nether;
        if(name.equals("beetroot_seeds"))return new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.beetrootSeeds);
        if(name.equals("beetroot_soup"))return new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.beetrootSoup);
        if(name.equals("iron_nugget"))return new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112.ironNugget);
        if(name.equals("grass"))return new ItemStack(Blocks.tallgrass,1,1);
        if(name.equals("wheat_seeds"))return new ItemStack(Items.wheat_seeds);
        Item sign=com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageSigns114.item(name);
        if(sign!=null)return new ItemStack(sign);
        if(name.equals("goat_horn"))return new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.goatHorn);
        if(name.equals("sentry_armor_trim_smithing_template")){
            com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120.prepare();
            for(Map.Entry<Item,String> entry:com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120.patterns.entrySet())if(entry.getValue().equals("sentry"))return new ItemStack(entry.getKey());
        }
        ItemStack stack=VillagerTradeItems146.stack(name.toUpperCase(Locale.ROOT));if(stack!=null&&stack.getItem()!=null)return stack;
        Object registered=Item.itemRegistry.getObject(id);if(registered instanceof Item)return new ItemStack((Item)registered);
        net.minecraft.block.Block block=FuturaAncientCityGenerator120.mappedBlockForFixture(name);
        if(block!=null&&Item.getItemFromBlock(block)!=null)return new ItemStack(block,1,FuturaAncientCityGenerator120.mappedStateMetaForFixture(name,Collections.<String,String>emptyMap(),0));
        throw new IllegalStateException("Unmapped structure loot item "+id);
    }
    public static void fill(IInventory inventory,String id,Random random){
        List<ItemStack> items=roll(id,random),splittable=new ArrayList<ItemStack>();
        List<Integer> slots=new ArrayList<Integer>();for(int i=0;i<inventory.getSizeInventory();i++)if(inventory.getStackInSlot(i)==null)slots.add(i);
        Collections.shuffle(slots,random);
        for(Iterator<ItemStack> it=items.iterator();it.hasNext();){ItemStack s=it.next();if(s.stackSize>1){splittable.add(s);it.remove();}}
        int room=slots.size()-items.size();
        while(room-splittable.size()>0&&!splittable.isEmpty()){
            ItemStack a=splittable.remove(random.nextInt(splittable.size()));ItemStack b=a.splitStack(1+random.nextInt(a.stackSize/2));
            if(a.stackSize>1&&random.nextBoolean())splittable.add(a);else{items.add(a);room--;}
            if(b.stackSize>1&&random.nextBoolean())splittable.add(b);else{items.add(b);room--;}
        }
        items.addAll(splittable);Collections.shuffle(items,random);for(ItemStack s:items){if(slots.isEmpty())break;inventory.setInventorySlotContents(slots.remove(slots.size()-1),s);}
    }
    private VillageStructureLoot120(){}
}
