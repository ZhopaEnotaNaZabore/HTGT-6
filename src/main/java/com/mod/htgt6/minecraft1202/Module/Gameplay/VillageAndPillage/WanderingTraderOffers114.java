package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import java.util.*;
import java.io.*;
import com.google.gson.*;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.World.Modern.VillageStructureLoot120;

public final class WanderingTraderOffers114 {
    private static final JsonObject TABLES=load();
    private static JsonObject load(){try(InputStream in=WanderingTraderOffers114.class.getResourceAsStream("/assets/htgt6/data/wandering_trades_1202.json")){
        if(in==null)throw new IllegalStateException("Missing wandering trader tables");
        return new JsonParser().parse(new InputStreamReader(in,"UTF-8")).getAsJsonObject();
    }catch(IOException e){throw new IllegalStateException(e);}}
    public static JsonArray definitions(int tier){return TABLES.getAsJsonArray(Integer.toString(tier));}
    public static ItemStack item(String id){
        String name=id.substring(id.indexOf(':')+1);
        switch(name){
            case "nautilus_shell":return new ItemStack(OceanContent.nautilusShell);
            case "kelp":return new ItemStack(OceanContent.kelp);
            case "sugar_cane":return new ItemStack(Items.reeds);
            case "lily_pad":return new ItemStack(Blocks.waterlily);
            case "podzol":return new ItemStack(Blocks.dirt,1,2);
            case "red_sand":return new ItemStack(Blocks.sand,1,1);
            case "tropical_fish_bucket":return new ItemStack(OceanContent.fishBucket,1,3);
            case "pufferfish_bucket":return new ItemStack(OceanContent.fishBucket,1,2);
            case "cherry_sapling":return new ItemStack(Backport120Content.cherrySapling);
            case "mangrove_propagule":return new ItemStack(Backport120Content.mangrovePropagule);
            case "small_dripleaf":return new ItemStack(Backport120Content.smallDripleaf);
            case "rooted_dirt":return new ItemStack(Backport120Content.rootedDirt);
            case "moss_block":return new ItemStack(Backport120Content.mossBlock);
            case "pointed_dripstone":return new ItemStack(Backport120Content.pointedDripstone);
        }
        String[] coral={"tube","brain","bubble","fire","horn"};
        for(int i=0;i<coral.length;i++)if(name.equals(coral[i]+"_coral_block"))return new ItemStack(OceanContent.coralBlock,1,i);
        return VillageStructureLoot120.item(id);
    }
    public static MerchantRecipe create(JsonObject definition){
        ItemStack output=item(definition.get("item").getAsString());
        if(output.getItem()==null)throw new IllegalStateException("Unregistered trader item "+definition.get("item"));
        output.stackSize=definition.get("count").getAsInt();
        NBTTagCompound tag=new MerchantRecipe(new ItemStack(Items.emerald,definition.get("cost").getAsInt()),output).writeToTags();
        tag.setInteger("maxUses",definition.get("maxUses").getAsInt());
        return new MerchantRecipe(tag);
    }
    public static MerchantRecipeList createOffers(Random random){
        MerchantRecipeList result=new MerchantRecipeList();
        for(int tier=1;tier<=2;tier++){
            List<JsonObject> pool=new ArrayList<JsonObject>();for(JsonElement e:definitions(tier))pool.add(e.getAsJsonObject());
            Collections.shuffle(pool,random);
            for(int i=0;i<(tier==1?5:1);i++)result.add(create(pool.get(i)));
        }
        return result;
    }
    private WanderingTraderOffers114(){}
}
