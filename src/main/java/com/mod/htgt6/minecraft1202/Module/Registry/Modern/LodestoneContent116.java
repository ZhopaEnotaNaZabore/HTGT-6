package com.mod.htgt6.minecraft1202.Module.Registry.Modern;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockLodestone116;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemLodestoneCompass116;
import com.mod.htgt6.minecraft1202.Module.Events.Modern.LodestoneEvents116;

public final class LodestoneContent116 {
    public static final Block lodestone=new BlockLodestone116();
    public static final Item compass=new ItemLodestoneCompass116();
    private static boolean registered;
    public static void register(){
        if(registered)return;
        GameRegistry.registerBlock(lodestone,"backport_1_20_lodestone");
        GameRegistry.registerItem(compass,"backport_1_20_lodestone_compass");
        GameRegistry.addRecipe(new ItemStack(lodestone),"SSS","SNS","SSS",'S',new ItemStack(Blocks.stonebrick,1,3),'N',EtFVanillaGapContent120.netheriteIngot);
        LodestoneEvents116 events=new LodestoneEvents116();MinecraftForge.EVENT_BUS.register(events);FMLCommonHandler.instance().bus().register(events);
        registered=true;
    }
    private LodestoneContent116(){}
}
