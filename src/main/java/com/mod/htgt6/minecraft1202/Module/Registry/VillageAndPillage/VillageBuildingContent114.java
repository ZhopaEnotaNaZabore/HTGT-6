package com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage;

import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;

/** Missing Village & Pillage building shapes, with distinct saved block identities. */
public final class VillageBuildingContent114 {
    public static final Map<String,Shape> SHAPES=new LinkedHashMap<String,Shape>();
    static {
        family("stone",Blocks.stone,0,"stone",6,true,false);
        family("mossy_stone_brick",Blocks.stonebrick,1,"mossy_stone_bricks",6,true,true);
        family("smooth_red_sandstone",EtFVanillaGapContent120.smoothRedSandstone,0,"red_sandstone_top",6,true,false);
        family("smooth_quartz",EtFVanillaGapContent120.smoothQuartz,0,"quartz_block_bottom",6,true,false);
        family("end_stone_brick",CombatContent.endBricks,0,"end_stone_bricks",9,true,true);
        family("red_nether_brick",FrostburnContent.redNetherBrick,0,"red_nether_bricks",6,true,true);
        wall("brick",Blocks.brick_block,0,"bricks");
        wall("stone_brick",Blocks.stonebrick,0,"stone_bricks");
        wall("nether_brick",Blocks.nether_brick,0,"nether_bricks");
        wall("sandstone",Blocks.sandstone,0,"sandstone");
        wall("red_sandstone",BountifulContent.redSandstone,0,"red_sandstone");
        wall("prismarine",BountifulContent.prismarine,0,"prismarine");
        family("cut_sandstone",Blocks.sandstone,2,"cut_sandstone",.8F,false,false);
        family("cut_red_sandstone",BountifulContent.redSandstone,2,"cut_red_sandstone",.8F,false,false);
    }
    public static final class Shape {
        public final String name,kind;public final Block block,source;public final int sourceMeta;
        public final BlockBackport120Slab doubleSlab;
        Shape(String name,String kind,Block block,Block source,int meta,BlockBackport120Slab full){
            this.name=name;this.kind=kind;this.block=block;this.source=source;sourceMeta=meta;doubleSlab=full;
        }
        public ItemStack ingredient(){return new ItemStack(source,1,sourceMeta);}
    }
    private static void family(String name,Block source,int meta,String texture,float resistance,boolean stairs,boolean wall){
        BlockBackport120Slab half=new BlockBackport120Slab(false,name,texture,source,resistance);
        BlockBackport120Slab full=new BlockBackport120Slab(true,name,texture,source,resistance);
        half.setSingle(half);full.setSingle(half);
        add(new Shape(name+"_slab","slab",half,source,meta,full));
        if(stairs)add(new Shape(name+"_stairs","stairs",new BlockBackport120Stairs(source,name+"_stairs",texture),source,meta,null));
        if(wall)wall(name,source,meta,texture);
    }
    private static void wall(String name,Block source,int meta,String texture){add(new Shape(name+"_wall","wall",new BlockBackport120Wall(source,name+"_wall",texture),source,meta,null));}
    private static void add(Shape shape){SHAPES.put(shape.name,shape);}
    public static void register(){for(Shape s:SHAPES.values()){
        String id="backport_1_14_"+s.name;
        if(s.doubleSlab!=null){
            GameRegistry.registerBlock(s.block,ItemBlockBackport120Slab.class,id,s.block,s.doubleSlab,Boolean.FALSE);
            GameRegistry.registerBlock(s.doubleSlab,ItemBlockBackport120Technical.class,"backport_1_14_double_"+s.name);
        }else GameRegistry.registerBlock(s.block,id);
        if(s.kind.equals("stairs"))GameRegistry.addRecipe(new ItemStack(s.block,4),"S  ","SS ","SSS",'S',s.ingredient());
        else if(s.kind.equals("slab"))GameRegistry.addRecipe(new ItemStack(s.block,6),"SSS",'S',s.ingredient());
        else GameRegistry.addRecipe(new ItemStack(s.block,6),"SSS","SSS",'S',s.ingredient());
        if(s.name.equals("stone_slab")){
            // Override the legacy raw-stone -> smooth-stone-slab recipe by priority.
            List recipes=net.minecraft.item.crafting.CraftingManager.getInstance().getRecipeList();
            recipes.add(0,recipes.remove(recipes.size()-1));
        }
    }
        GameRegistry.addRecipe(new ItemStack(Blocks.stone_slab,6,0),"SSS",'S',new ItemStack(EtFVanillaGapContent120.smoothStone));
    }
    private VillageBuildingContent114(){}
}
