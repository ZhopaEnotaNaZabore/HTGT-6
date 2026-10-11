package com.mod.htgt6.minecraft1202.Module.Registry.Modern;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
public final class NetherTreasure116 {
    public static final Item snoutPattern=new Item(){
        @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
        public void addInformation(ItemStack stack,net.minecraft.entity.player.EntityPlayer p,java.util.List lines,boolean advanced){lines.add(net.minecraft.util.EnumChatFormatting.GRAY+net.minecraft.util.StatCollector.translateToLocal("item.backport_1_20.piglin_banner_pattern.desc"));}
    }.setUnlocalizedName("backport_1_20.piglin_banner_pattern").setTextureName("htgt6:1_20_backport/piglin_banner_pattern").setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabMisc);
    private static boolean registered;
    public static void register(){if(registered)return;GameRegistry.registerItem(snoutPattern,"backport_1_20_piglin_banner_pattern");registered=true;}
    /** Exact ownership for all non-legacy entries in the four bastion tables. */
    public static ItemStack resolve(String name){
        if(name.equals("piglin_banner_pattern"))return new ItemStack(snoutPattern);
        if(name.equals("music_disc_pigstep"))return new ItemStack(EtFVanillaGapContent120.musicDiscPigstep);
        if(name.equals("lodestone"))return new ItemStack(LodestoneContent116.lodestone);
        if(name.equals("ancient_debris"))return new ItemStack(EtFVanillaGapContent120.ancientDebris);
        if(name.equals("netherite_ingot"))return new ItemStack(EtFVanillaGapContent120.netheriteIngot);
        if(name.equals("netherite_scrap"))return new ItemStack(EtFVanillaGapContent120.netheriteScrap);
        if(name.equals("gilded_blackstone"))return new ItemStack(EtFVanillaGapContent120.gildedBlackstone);
        if(name.equals("crying_obsidian"))return new ItemStack(EtFVanillaGapContent120.cryingObsidian);
        if(name.equals("chain"))return new ItemStack(Backport120Content.chain);
        if(name.equals("bone_block"))return new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent.boneBlock);
        if(name.equals("crimson_nylium"))return new ItemStack(NetherVegetationContent116.crimsonNylium);
        if(name.equals("crimson_fungus"))return new ItemStack(NetherVegetationContent116.crimsonFungus);
        if(name.equals("crimson_roots"))return new ItemStack(NetherVegetationContent116.crimsonRoots);
        if(name.equals("crossbow"))return new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent.crossbow);
        if(name.equals("spectral_arrow"))return new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.spectralArrow);
        if(name.equals("iron_nugget"))return new ItemStack(Backport120Content.ironNugget);
        if(name.equals("enchanted_golden_apple"))return new ItemStack(net.minecraft.init.Items.golden_apple,1,1);
        if(name.equals("netherite_upgrade_smithing_template"))return new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120.upgradeTemplate);
        if(name.equals("snout_armor_trim_smithing_template")){
            com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120.prepare();
            for(java.util.Map.Entry<Item,String> e:com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120.patterns.entrySet())if(e.getValue().equals("snout"))return new ItemStack(e.getKey());
        }
        return null;
    }
    private NetherTreasure116(){}
}
