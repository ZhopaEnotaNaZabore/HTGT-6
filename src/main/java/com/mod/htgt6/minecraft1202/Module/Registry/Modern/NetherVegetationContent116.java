package com.mod.htgt6.minecraft1202.Module.Registry.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.FrostburnSound120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherVegetation116;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.MinecraftForge;

public final class NetherVegetationContent116 {
    public static final Block.SoundType NYLIUM_SOUND=new FrostburnSound120("nylium"),ROOT_SOUND=new FrostburnSound120("roots"),FUNGUS_SOUND=new FrostburnSound120("fungus"),SPROUT_SOUND=new FrostburnSound120("nether_sprouts");
    public static final BlockNylium116 crimsonNylium=new BlockNylium116(false),warpedNylium=new BlockNylium116(true);
    public static final BlockNetherPlant116 crimsonFungus=new BlockNetherPlant116("crimson_fungus",false,true,false),warpedFungus=new BlockNetherPlant116("warped_fungus",true,true,false);
    public static final BlockNetherPlant116 crimsonRoots=new BlockNetherPlant116("crimson_roots",false,false,false),warpedRoots=new BlockNetherPlant116("warped_roots",true,false,false),sprouts=new BlockNetherPlant116("nether_sprouts",true,false,true);
    public static final Block shroomlight=new BlockBackport120Simple(Material.gourd,"shroomlight",1,1,new FrostburnSound120("shroomlight"),"hoe",0).setLightLevel(1);
    public static final Block warpedWart=new BlockBackport120Simple(Material.grass,"warped_wart_block",1,1,new FrostburnSound120("wart_block"),"hoe",0);
    public static final BlockNetherVine116[] twisting=vines("twisting_vines",1),weeping=vines("weeping_vines",-1);
    public static final Block[] BLOCKS={crimsonNylium,warpedNylium,crimsonFungus,warpedFungus,crimsonRoots,warpedRoots,sprouts,shroomlight,warpedWart};
    public static final String[] NAMES={"crimson_nylium","warped_nylium","crimson_fungus","warped_fungus","crimson_roots","warped_roots","nether_sprouts","shroomlight","warped_wart_block"};
    public static final BlockPottedNether116[] POTS={new BlockPottedNether116("crimson_fungus",crimsonFungus),new BlockPottedNether116("warped_fungus",warpedFungus),new BlockPottedNether116("crimson_roots",crimsonRoots),new BlockPottedNether116("warped_roots",warpedRoots)};
    private static boolean registered;
    private static BlockNetherVine116[] vines(String name,int direction){return new BlockNetherVine116[]{new BlockNetherVine116(name,direction,0),new BlockNetherVine116(name+"_aged",direction,1),new BlockNetherVine116(name+"_plant",direction,2)};}
    public static void register(){
        if(registered)return;for(int i=0;i<BLOCKS.length;i++)GameRegistry.registerBlock(BLOCKS[i],"backport_1_20_"+NAMES[i]);
        for(BlockNetherVine116[] vines:new BlockNetherVine116[][]{twisting,weeping})for(int i=0;i<3;i++){
            String id="backport_1_20_"+(vines[i].direction>0?"twisting_vines":"weeping_vines")+(i==1?"_aged":i==2?"_plant":"");
            if(i==0)GameRegistry.registerBlock(vines[i],com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBlockNetherVine116.class,id);else GameRegistry.registerBlock(vines[i],com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBlockBackport120Technical.class,id);
        }
        for(int i=0;i<POTS.length;i++)GameRegistry.registerBlock(POTS[i],com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBlockBackport120Technical.class,"backport_1_20_potted_"+NAMES[i+2]);
        MinecraftForge.EVENT_BUS.register(new NetherVegetation116());registered=true;
    }
    private NetherVegetationContent116(){}
}
