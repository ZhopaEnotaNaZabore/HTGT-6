package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import java.util.*;
import java.io.*;
import java.lang.reflect.Field;
import com.google.gson.*;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraftforge.common.*;
import com.mod.htgt6.minecraft1202.Module.World.Modern.BackportResourceLoader120;
/** Original fishing tables replace only vanilla entries; Forge additions and removals survive. */
public final class FishingLoot116 {
    private static final String[] CATEGORIES={"junk","treasure","fish"};
    private static final String[][] OLD_NAMES={{"leather_boots","leather","bone","potion","string","fishing_rod","bowl","stick","ink_sac","tripwire_hook","rotten_flesh"},{"lily_pad","name_tag","saddle","bow","fishing_rod","book"},{"cod","salmon","tropical_fish","pufferfish"}};
    private static final List<WeightedRandomFishable>[] LISTS=new List[3];
    private static boolean registered;
    public static JsonObject table(String category){try(InputStream in=BackportResourceLoader120.open(FishingLoot116.class,"/assets/htgt6/loot_tables/gameplay/fishing/"+category+".json")){if(in==null)throw new IllegalStateException("Missing fishing table "+category);return new JsonParser().parse(new InputStreamReader(in,"UTF-8")).getAsJsonObject();}catch(IOException e){throw new IllegalStateException(e);}}
    public static synchronized void register(){if(registered)return;
        List[] vanilla={EntityFishHook.field_146039_d,EntityFishHook.field_146041_e,EntityFishHook.field_146036_f};
        try{for(int i=0;i<3;i++){
            Field field=FishingHooks.class.getDeclaredField(CATEGORIES[i]);field.setAccessible(true);List<WeightedRandomFishable> list=(List<WeightedRandomFishable>)field.get(null);LISTS[i]=list;
            Map<String,JsonObject> entries=new LinkedHashMap<String,JsonObject>();for(JsonElement e:table(CATEGORIES[i]).getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries")){JsonObject entry=e.getAsJsonObject();entries.put(entry.get("name").getAsString().substring(10),entry);}
            for(int index=0;index<OLD_NAMES[i].length;index++){WeightedRandomFishable old=(WeightedRandomFishable)vanilla[i].get(index);int present=list.indexOf(old);JsonObject replacement=entries.remove(OLD_NAMES[i][index]);if(present<0)continue;if(replacement==null)list.remove(present);else{Entry modern=new Entry(replacement);modern.itemWeight=old.itemWeight;list.set(present,modern);}}
            for(JsonObject entry:entries.values())list.add(new Entry(entry));
        }}catch(ReflectiveOperationException e){throw new IllegalStateException("Forge fishing table adapter unavailable",e);}registered=true;
    }
    public static ItemStack roll(EntityFishHook hook,int category){register();List<WeightedRandomFishable> entries=LISTS[category];int sum=0;for(WeightedRandomFishable e:entries)if(available(e,hook))sum+=Math.max(0,e.itemWeight);if(sum<=0)return null;
        int choice=hook.worldObj.rand.nextInt(sum);for(WeightedRandomFishable e:entries)if(available(e,hook)&&(choice-=Math.max(0,e.itemWeight))<0)return e.func_150708_a(hook.worldObj.rand);return null;
    }
    private static boolean available(WeightedRandomFishable e,EntityFishHook hook){return !(e instanceof Entry)||!((Entry)e).jungle||BiomeDictionary.isBiomeOfType(hook.worldObj.getBiomeGenForCoords(net.minecraft.util.MathHelper.floor_double(hook.posX),net.minecraft.util.MathHelper.floor_double(hook.posZ)),BiomeDictionary.Type.JUNGLE);}
    public static final class Entry extends WeightedRandomFishable {
        final JsonObject entry;final boolean jungle;
        public Entry(JsonObject value){super(new ItemStack(Items.stick),value.has("weight")?value.get("weight").getAsInt():1);entry=value;jungle=value.has("conditions");}
        @Override public ItemStack func_150708_a(Random r){ItemStack stack=item(entry.get("name").getAsString());
            if(entry.has("functions"))for(JsonElement e:entry.getAsJsonArray("functions")){JsonObject f=e.getAsJsonObject();String kind=f.get("function").getAsString();
                if(kind.equals("minecraft:set_damage")){JsonObject range=f.getAsJsonObject("damage");float remaining=range.get("min").getAsFloat()+r.nextFloat()*(range.get("max").getAsFloat()-range.get("min").getAsFloat());stack.setItemDamage((int)Math.floor((1-remaining)*stack.getMaxDamage()));}
                else if(kind.equals("minecraft:set_count"))stack.stackSize=f.get("count").getAsInt();
                else if(kind.equals("minecraft:set_potion")){if(!f.get("id").getAsString().equals("minecraft:water"))throw new IllegalArgumentException("Unexpected fishing potion");stack.setItemDamage(0);}
                else if(kind.equals("minecraft:enchant_with_levels"))stack=FishingEnchant116.enchant(stack,r,f.get("levels").getAsInt());
                else throw new IllegalArgumentException("Unsupported fishing function "+kind);
            }return stack;
        }
    }
    public static ItemStack item(String id){String name=id.substring(id.indexOf(':')+1);
        if(name.equals("lily_pad"))return new ItemStack(Blocks.waterlily);
        if(name.equals("ink_sac"))return new ItemStack(Items.dye,1,0);
        if(name.equals("potion"))return new ItemStack(Items.potionitem);
        if(name.equals("nautilus_shell"))return new ItemStack(com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent.nautilusShell);
        if(name.equals("bamboo"))return new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.bamboo);
        String[] fish={"cod","salmon","tropical_fish","pufferfish"};for(int i=0;i<fish.length;i++)if(name.equals(fish[i]))return new ItemStack(Items.fish,1,i);
        Item item=(Item)Item.itemRegistry.getObject("minecraft:"+name);if(item==null)throw new IllegalArgumentException("Unknown fishing item "+id);return new ItemStack(item);
    }
    private FishingLoot116(){}
}
