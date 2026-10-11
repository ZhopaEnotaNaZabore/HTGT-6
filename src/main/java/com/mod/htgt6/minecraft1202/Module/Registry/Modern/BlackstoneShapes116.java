package com.mod.htgt6.minecraft1202.Module.Registry.Modern;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.*;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import java.util.List;

/** Stable registry names; the pre-existing polished slab pair is retained. */
public final class BlackstoneShapes116 {
    public static final class Family {
        public final String name;
        public final Block source;
        public final BlockBackport120Slab slab,doubleSlab;
        public final BlockBlackstoneStairs116 stairs;
        public final BlockBlackstoneWall116 wall;
        final boolean existingSlab;
        Family(String name,Block source,BlockBackport120Slab half,BlockBackport120Slab full) {
            this.name=name;this.source=source;existingSlab=half!=null;
            slab=existingSlab?half:new BlockBlackstoneSlab116(false,name,source);
            doubleSlab=existingSlab?full:new BlockBlackstoneSlab116(true,name,source);
            slab.setSingle(slab);doubleSlab.setSingle(slab);
            stairs=new BlockBlackstoneStairs116(source,name+"_stairs");
            wall=new BlockBlackstoneWall116(source,name+"_wall");
        }
    }
    public static final Family BLACKSTONE=new Family("blackstone",EtFVanillaGapContent120.blackstone,null,null);
    public static final Family POLISHED=new Family("polished_blackstone",EtFVanillaGapContent120.polishedBlackstone,EtFVanillaGapContent120.polishedBlackstoneSlab,EtFVanillaGapContent120.doublePolishedBlackstoneSlab);
    public static final Family BRICKS=new Family("polished_blackstone_brick",EtFVanillaGapContent120.polishedBlackstoneBricks,null,null);
    public static final Family[] FAMILIES={BLACKSTONE,POLISHED,BRICKS};
    private static boolean registered;
    public static void register() {
        if(registered)return;
        for(Family family:FAMILIES) {
            if(!family.existingSlab) {
                GameRegistry.registerBlock(family.slab,ItemBlackstoneSlab116.class,"backport_1_20_"+family.name+"_slab",family.slab,family.doubleSlab,Boolean.FALSE);
                GameRegistry.registerBlock(family.doubleSlab,ItemBlockBackport120Technical.class,"backport_1_20_double_"+family.name+"_slab");
                GameRegistry.addRecipe(new ItemStack(family.slab,6),"BBB",'B',family.source);
            }
            GameRegistry.registerBlock(family.wall,"backport_1_20_"+family.name+"_wall");
            GameRegistry.addRecipe(new ItemStack(family.wall,6),"BBB","BBB",'B',family.source);
            GameRegistry.registerBlock(family.stairs,"backport_1_20_"+family.name+"_stairs");
            GameRegistry.addRecipe(new ItemStack(family.stairs,4),"B  ","BB ","BBB",'B',family.source);
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.ShapeWater116.Buckets());
        registered=true;
    }
    public static void appendStonecutting(ItemStack input,List<ItemStack> out) {
        if(input==null||input.getItemDamage()!=0)return;
        Block source=Block.getBlockFromItem(input.getItem());
        if(source!=BLACKSTONE.source&&source!=POLISHED.source&&source!=BRICKS.source)return;
        for(Family family:FAMILIES) {
            if(source==BRICKS.source&&family!=BRICKS)continue;
            if(source==POLISHED.source&&family==BLACKSTONE)continue;
            if(source!=family.source)add(out,new ItemStack(family.source));
            add(out,new ItemStack(family.slab,2));add(out,new ItemStack(family.stairs));add(out,new ItemStack(family.wall));
        }
        if(source!=BRICKS.source)add(out,new ItemStack(EtFVanillaGapContent120.chiseledPolishedBlackstone));
    }
    private static void add(List<ItemStack> out,ItemStack stack){for(ItemStack old:out)if(old.isItemEqual(stack))return;out.add(stack);}
    private BlackstoneShapes116(){}
}
