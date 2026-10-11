package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaAncientCityGenerator120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Slab;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import java.util.*;

public final class ModernStoneShapesFixture120 {
    public static void assertContract() throws Exception {
        java.lang.reflect.Field controller=cpw.mods.fml.common.Loader.class.getDeclaredField("modController");controller.setAccessible(true);
        controller.set(cpw.mods.fml.common.Loader.instance(),new cpw.mods.fml.common.LoadController(cpw.mods.fml.common.Loader.instance()));
        if(Item.getItemFromBlock(BountifulContent.stone)==null)GameRegistry.registerBlock(BountifulContent.stone,"fixture_bountiful_stone");
        ModernStoneShapes120.register();
        java.lang.reflect.Method silk=BlockBackport120Slab.class.getDeclaredMethod("createStackedBlock",int.class);silk.setAccessible(true);
        int states=0,forms=0;
        String[] directions={"north","east","south","west"};int[] stairMeta={3,0,2,1};
        for(ModernStoneShapes120.Family f:ModernStoneShapes120.FAMILIES){
            require(f.stairs instanceof net.minecraft.block.BlockStairs,"stair collision API");
            require((f.metadata%2==0)==(f.wall!=null),"only raw stone has walls");
            if(f.wall!=null){List<ItemStack> variants=new ArrayList<ItemStack>();f.wall.getSubBlocks(Item.getItemFromBlock(f.wall),null,variants);
                require(variants.size()==1&&variants.get(0).getItemDamage()==0,"duplicate legacy wall variant");}
            require(f.doubleSlab.getCreativeTabToDisplayOn()==null,"technical slab visible");
            require(Item.getItemFromBlock(f.slab) instanceof com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBlockBackport120Slab,"merge item");
            require(f.doubleSlab.getItemDropped(0,new Random(49),3)==Item.getItemFromBlock(f.slab),"double slab normal drop");
            require(f.doubleSlab.quantityDropped(new Random(49))==2,"double slab quantity");
            ItemStack drop=(ItemStack)silk.invoke(f.doubleSlab,0);
            require(drop.stackSize==2&&drop.getItem()==Item.getItemFromBlock(f.slab),"double slab silk drop");
            require(f.doubleSlab.getItem(null,0,0,0)==Item.getItemFromBlock(f.slab),"double slab pick");
            for(int r=0;r<4;r++)for(int d=0;d<4;d++)for(boolean top:new boolean[]{false,true}){
                Map<String,String> p=new HashMap<String,String>();p.put("facing",directions[d]);p.put("half",top?"top":"bottom");
                require(FuturaAncientCityGenerator120.mappedStateBlockForFixture(f.name+"_stairs",p,r)==f.stairs,"stone stair substitution");
                require(FuturaAncientCityGenerator120.mappedStateMetaForFixture(f.name+"_stairs",p,r)==(stairMeta[(d+r)&3]|(top?4:0)),"stone stair rotation");states++;
            }
            for(String type:new String[]{"bottom","top","double"}){
                Map<String,String> p=Collections.singletonMap("type",type);
                require(FuturaAncientCityGenerator120.mappedStateBlockForFixture(f.name+"_slab",p,0)==(type.equals("double")?f.doubleSlab:f.slab),"slab block");
                require(FuturaAncientCityGenerator120.mappedStateMetaForFixture(f.name+"_slab",p,0)==(type.equals("top")?8:0),"slab half");states++;
            }
            if(f.wall!=null)require(FuturaAncientCityGenerator120.mappedBlockForFixture(f.name+"_wall")==f.wall,"wall substitution");
            List<ItemStack> cuts=new ArrayList<ItemStack>();ModernStoneShapes120.stonecutting(f.metadata,cuts);
            require(cuts.size()==(f.wall!=null?6:2),"stonecutter complete outputs");
            require(cuts.get(0).stackSize==2&&cuts.get(0).getItem()==Item.getItemFromBlock(f.slab),"stonecutter slab yield");
            for(String suffix:f.wall==null?new String[]{"stairs","slab"}:new String[]{"stairs","slab","wall"}){
                require(ModernStoneShapesFixture120.class.getResource("/assets/htgt6/blockstates/1_20_backport/"+f.name+"_"+suffix+".json")!=null,"blockstate resource");
                require(ModernStoneShapesFixture120.class.getResource("/assets/htgt6/models/item/1_20_backport/"+f.name+"_"+suffix+".json")!=null,"item model resource");forms++;
            }
        }
        require(FuturaAncientCityGenerator120.mappedBlockForFixture("smooth_sandstone_stairs")==EtFVanillaGapContent120.smoothSandstoneStairs,"smooth sandstone stair substitution");
        require(FuturaAncientCityGenerator120.mappedStateBlockForFixture("smooth_sandstone_slab",Collections.singletonMap("type","double"),0)==EtFVanillaGapContent120.doubleSmoothSandstoneSlab,"smooth sandstone slab substitution");
        System.out.println("Modern stone 49 OK: "+forms+" forms, "+states+" NBT states, registry, slab drop/pick/silk, stonecutting and assets");
    }
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
    private ModernStoneShapesFixture120(){}
}
