package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityTraderLlama120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWanderingTrader120;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.GuiMerchant120;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.MerchantScreenHandler120;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBackport120SpawnEgg;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Entities;
import com.mod.htgt6.minecraft1202.Module.World.Modern.WanderingTraderSpawner120;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Random;
import javax.imageio.ImageIO;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.client.gui.GuiMerchant;

/** Entity, merchant, despawn, spawn-cadence, egg and official-resource contract. */
public final class WanderingTraderFixture120 {
    public static void main(String[] ignored)throws Exception{
        com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID=24;com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID=25;com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID=26;com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID=27;
        com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.initialize();
        registerTradeItems();
        require(EntityWanderingTrader120.class.getSuperclass()==EntityVillager.class,"native 1.7 merchant protocol");
        require(EntityTraderLlama120.class.getSuperclass()==EntityLlama112.class,"trader llama reuses llama mechanics");
        require(EntityWanderingTrader120.DEFAULT_DESPAWN_DELAY==48000
                &&EntityTraderLlama120.DEFAULT_DESPAWN_DELAY==48000,"modern despawn delay");
        MerchantRecipeList offers=EntityWanderingTrader120.createOffers(new Random(1202L));
        require(offers.size()==EntityWanderingTrader120.COMMON_OFFERS+EntityWanderingTrader120.SPECIAL_OFFERS,
                "five common and one special offer");
        require(Backport120Entities.WANDERING_TRADER_MOD_ID==48&&Backport120Entities.TRADER_LLAMA_MOD_ID==49,
                "unique trader network ids");
        require(ItemBackport120SpawnEgg.entityType(ItemBackport120SpawnEgg.WANDERING_TRADER)
                ==EntityWanderingTrader120.class,"trader spawn egg");
        require(ItemBackport120SpawnEgg.entityType(ItemBackport120SpawnEgg.TRADER_LLAMA)
                ==EntityTraderLlama120.class,"trader llama spawn egg");
        require(WanderingTraderSpawner120.class.getMethod("onWorldTick",cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent.class)
                .getAnnotation(SubscribeEvent.class)!=null,"FML world tick registration hook");
        WanderingTraderSpawnData120 source=new WanderingTraderSpawnData120();source.tickDelay=17;source.spawnDelay=9000;source.spawnChance=75;
        NBTTagCompound tag=new NBTTagCompound();source.writeToNBT(tag);WanderingTraderSpawnData120 copy=new WanderingTraderSpawnData120();copy.readFromNBT(tag);
        require(copy.tickDelay==17&&copy.spawnDelay==9000&&copy.spawnChance==75,"persistent spawn cadence");
        image("assets/htgt6/textures/entity/wandering_trader.png",64,64);
        image("assets/htgt6/textures/gui/container/wandering_trader.png",512,256);
        image("assets/htgt6/textures/gui/container/villager/experience_bar_background.png",102,5);
        image("assets/htgt6/textures/gui/container/villager/experience_bar_current.png",102,5);
        image("assets/htgt6/textures/gui/container/villager/scroller.png",6,27);
        image("assets/htgt6/textures/gui/container/villager/scroller_disabled.png",6,27);
        image("assets/htgt6/textures/gui/container/villager/trade_arrow.png",10,9);
        image("assets/htgt6/textures/gui/container/villager/trade_arrow_out_of_stock.png",10,9);
        image("assets/htgt6/textures/gui/container/villager/out_of_stock.png",28,21);
        require(GuiMerchant120.class.getSuperclass()==GuiMerchant.class,"modern screen must retain MC|TrList compatibility");
        require(GuiMerchant120.GUI_WIDTH==276&&GuiMerchant120.GUI_HEIGHT==166
                &&GuiMerchant120.VISIBLE_OFFERS==7,"1.20 merchant layout");
        require(MerchantScreenHandler120.class.getMethod("onGuiOpen",
                net.minecraftforge.client.event.GuiOpenEvent.class)!=null,"selective trader GUI replacement");
        image("assets/htgt6/textures/entity/exploration_update/llama/decor/trader_llama.png",128,64);
        System.out.println("Wandering Trader OK; merchant offers, daily spawner, two trader llamas, eggs and resources");
    }
    public static void registerTradeItems(){
        Class<?> content=com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.class;
        String[] names={"cornflower","lilyOfTheValley","cherrySapling","mangrovePropagule","smallDripleaf","rootedDirt","mossBlock","pointedDripstone","blueIce"};
        try{for(String name:names){net.minecraft.block.Block b=(net.minecraft.block.Block)content.getField(name).get(null);
            if(net.minecraft.item.Item.getItemFromBlock(b)==null)cpw.mods.fml.common.registry.GameRegistry.registerBlock(b,"fixture_trader_"+name);}}
        catch(ReflectiveOperationException e){throw new IllegalStateException(e);}
        net.minecraft.block.Block[] aquatic={com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent.kelp,com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent.coralBlock,com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent.marinePlant};
        for(int i=0;i<aquatic.length;i++)if(net.minecraft.item.Item.getItemFromBlock(aquatic[i])==null)cpw.mods.fml.common.registry.GameRegistry.registerBlock(aquatic[i],"fixture_trader_aquatic"+i);
        for(int tier=1;tier<=2;tier++)for(com.google.gson.JsonElement e:com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.WanderingTraderOffers114.definitions(tier)){
            String id=e.getAsJsonObject().get("item").getAsString();net.minecraft.item.Item item=com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.WanderingTraderOffers114.item(id).getItem();
            require(item!=null,"mapped trader item "+id);
            if(net.minecraft.item.Item.getIdFromItem(item)<0)cpw.mods.fml.common.registry.GameRegistry.registerItem(item,"fixture_trader_item_"+id.substring(10));
        }
    }
    private static void image(String path,int width,int height)throws Exception{InputStream in=WanderingTraderFixture120.class
            .getClassLoader().getResourceAsStream(path);require(in!=null,path);BufferedImage image;
        try{image=ImageIO.read(in);}finally{in.close();}require(image!=null&&image.getWidth()==width&&image.getHeight()==height,path+" dimensions");}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private WanderingTraderFixture120(){}
}
