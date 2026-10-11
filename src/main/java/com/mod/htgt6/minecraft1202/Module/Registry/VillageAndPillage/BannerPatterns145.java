package com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Reusable 1.14 loom pattern items. Globe is obtained through the cartographer trade. */
public final class BannerPatterns145 {
    public static final Item flower=pattern("flower"),creeper=pattern("creeper"),skull=pattern("skull"),
            mojang=pattern("mojang"),globe=pattern("globe");
    private static boolean registered;
    private BannerPatterns145(){}
    private static Item pattern(String name){return new PatternItem().setUnlocalizedName("village_pillage_"+name+"_banner_pattern")
            .setTextureName("htgt6:village_pillage/"+name+"_banner_pattern").setCreativeTab(CreativeTabs.tabMisc);}
    public static void register(){
        if(registered)return;
        String[] names={"flower","creeper","skull","mojang","globe"};Item[] items={flower,creeper,skull,mojang,globe};
        for(int i=0;i<names.length;i++)GameRegistry.registerItem(items[i],"village_pillage_"+names[i]+"_banner_pattern");
        GameRegistry.addShapelessRecipe(new ItemStack(flower),Items.paper,new ItemStack(Blocks.red_flower,1,8));
        GameRegistry.addShapelessRecipe(new ItemStack(creeper),Items.paper,new ItemStack(Items.skull,1,4));
        GameRegistry.addShapelessRecipe(new ItemStack(skull),Items.paper,new ItemStack(Items.skull,1,1));
        GameRegistry.addShapelessRecipe(new ItemStack(mojang),Items.paper,new ItemStack(Items.golden_apple,1,1));
        registered=true;
    }
    public static String code(Item item){
        if(item==com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherTreasure116.snoutPattern)return "pig";
        if(item==flower)return "flo";if(item==creeper)return "cre";if(item==skull)return "sku";
        if(item==mojang)return "moj";if(item==globe)return "glb";return null;
    }
    private static final class PatternItem extends Item {
        @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
        public void addInformation(ItemStack stack,net.minecraft.entity.player.EntityPlayer player,java.util.List lines,boolean advanced){
            lines.add(net.minecraft.util.EnumChatFormatting.GRAY+net.minecraft.util.StatCollector.translateToLocal(getUnlocalizedName()+".desc"));
        }
    }
}
