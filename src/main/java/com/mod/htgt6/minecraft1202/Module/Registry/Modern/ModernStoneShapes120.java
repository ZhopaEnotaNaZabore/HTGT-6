package com.mod.htgt6.minecraft1202.Module.Registry.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBlockBackport120Slab;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBlockBackport120Technical;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import java.util.List;

/** The six 1.20 stone variants; polished variants deliberately have no wall. */
public final class ModernStoneShapes120 {
    public static final String[] NAMES={"granite","polished_granite","diorite","polished_diorite","andesite","polished_andesite"};
    public static final Family[] FAMILIES=create();
    private static Family[] create(){Family[] a=new Family[NAMES.length];for(int i=0;i<a.length;i++)a[i]=new Family(NAMES[i],i);return a;}
    public static final class Family {
        public final String name;public final int metadata;
        public final Block stairs,wall;
        public final BlockBackport120Slab slab,doubleSlab;
        Family(String name,int metadata){
            this.name=name;this.metadata=metadata;
            Block source=new BlockBackport120EtFGapSimple(Material.rock,name+"_shape_source",name,null,
                    1.5F,6,Block.soundTypePiston,"pickaxe",0).setCreativeTab(null);
            stairs=new BlockBackport120Stairs(source,name+"_stairs",name);
            slab=new BlockBackport120Slab(false,name,name,source,6);
            doubleSlab=new BlockBackport120Slab(true,name,name,source,6);
            slab.setSingle(slab);doubleSlab.setSingle(slab);
            wall=(metadata&1)==0?new BlockBackport120Wall(source,name+"_wall",name):null;
        }
    }
    public static void register(){for(Family f:FAMILIES){
        GameRegistry.registerBlock(f.stairs,"backport_1_20_"+f.name+"_stairs");
        GameRegistry.registerBlock(f.slab,ItemBlockBackport120Slab.class,"backport_1_20_"+f.name+"_slab",f.slab,f.doubleSlab,Boolean.FALSE);
        GameRegistry.registerBlock(f.doubleSlab,ItemBlockBackport120Technical.class,"backport_1_20_double_"+f.name+"_slab");
        if(f.wall!=null)GameRegistry.registerBlock(f.wall,"backport_1_20_"+f.name+"_wall");
        ItemStack source=new ItemStack(BountifulContent.stone,1,f.metadata);
        GameRegistry.addRecipe(new ItemStack(f.stairs,4),"S  ","SS ","SSS",'S',source);
        GameRegistry.addRecipe(new ItemStack(f.slab,6),"SSS",'S',source);
        if(f.wall!=null)GameRegistry.addRecipe(new ItemStack(f.wall,6),"SSS","SSS",'S',source);
    }}
    public static void stonecutting(int metadata,List<ItemStack> out){
        if(metadata<0||metadata>=FAMILIES.length)return;
        Family f=FAMILIES[metadata];addCuts(f,out);
        if((metadata&1)==0){out.add(new ItemStack(BountifulContent.stone,1,metadata+1));addCuts(FAMILIES[metadata+1],out);}
    }
    private static void addCuts(Family f,List<ItemStack> out){out.add(new ItemStack(f.slab,2));out.add(new ItemStack(f.stairs));if(f.wall!=null)out.add(new ItemStack(f.wall));}
    private ModernStoneShapes120(){}
}
