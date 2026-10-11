package com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage;
import java.util.*;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.*;
import net.minecraft.init.*;
import net.minecraftforge.common.util.EnumHelper;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
/** Modern smithing identities, with the existing Coast registry ID preserved. */
public final class SmithingContent120 {
    public static final String[] PATTERNS={"sentry","dune","coast","wild","ward","eye","vex","tide","snout","rib","spire","wayfinder","shaper","silence","raiser","host"};
    public static final Map<Item,String> patterns=new LinkedHashMap<Item,String>();
    public static final Map<Item,Item> upgrades=new LinkedHashMap<Item,Item>();
    public static final Item upgradeTemplate=template("netherite_upgrade");
    private static final Item.ToolMaterial TOOLS=EnumHelper.addToolMaterial("NETHERITE",4,2031,9F,4F,15);
    private static final ItemArmor.ArmorMaterial ARMOR=EnumHelper.addArmorMaterial("NETHERITE",37,new int[]{3,8,6,3},15);
    private static Item template(String name){return new Item().setUnlocalizedName(name+"_smithing_template").setTextureName("htgt6:1_20_backport/"+name+"_smithing_template").setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabMaterials);}
    public static String pattern(ItemStack stack){if(stack==null)return null;if(stack.getItem()==OceanContent.coastArmorTrim)return "coast";return patterns.get(stack.getItem());}
    private static boolean prepared;
    public static void register(){
        prepare();GameRegistry.registerItem(upgradeTemplate,"netherite_upgrade_smithing_template");
        for(Item item:patterns.keySet())if(item!=OceanContent.coastArmorTrim)GameRegistry.registerItem(item,item.getUnlocalizedName().substring(5));
        for(Item item:upgrades.values())GameRegistry.registerItem(item,item.getUnlocalizedName().substring(5));
    }
    public static void prepare(){
        if(prepared)return;prepared=true;
        TOOLS.setRepairItem(new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.netheriteIngot));
        ARMOR.customCraftingMaterial=com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.netheriteIngot;
        for(String name:PATTERNS){Item item="coast".equals(name)?OceanContent.coastArmorTrim:template(name+"_armor_trim");
            patterns.put(item,name);}
        Item[] from={Items.diamond_sword,Items.diamond_pickaxe,Items.diamond_axe,Items.diamond_shovel,Items.diamond_hoe};
        Item[] to={new ItemSword(TOOLS){public com.google.common.collect.Multimap getItemAttributeModifiers(){return weapon(super.getItemAttributeModifiers(),7,-2.4);}},
            new ItemPickaxe(TOOLS){public com.google.common.collect.Multimap getItemAttributeModifiers(){return weapon(super.getItemAttributeModifiers(),5,-2.8);}},
            new ItemAxe(TOOLS){public com.google.common.collect.Multimap getItemAttributeModifiers(){return weapon(super.getItemAttributeModifiers(),9,-3);}},
            new ItemSpade(TOOLS){public com.google.common.collect.Multimap getItemAttributeModifiers(){return weapon(super.getItemAttributeModifiers(),5.5,-3);}},
            new com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemNetheriteHoe116(TOOLS){public com.google.common.collect.Multimap getItemAttributeModifiers(){return weapon(super.getItemAttributeModifiers(),0,0);}}};
        String[] names={"sword","pickaxe","axe","shovel","hoe"};
        for(int i=0;i<from.length;i++)registerUpgrade(from[i],to[i],names[i]);
        Item[] armor={Items.diamond_helmet,Items.diamond_chestplate,Items.diamond_leggings,Items.diamond_boots};
        String[] armorNames={"helmet","chestplate","leggings","boots"};
        for(int i=0;i<4;i++)registerUpgrade(armor[i],new ItemArmor(ARMOR,0,i){
            @Override public String getArmorTexture(ItemStack stack,net.minecraft.entity.Entity entity,int slot,String type){return "htgt6:textures/models/armor/netherite_layer_"+(armorType==2?2:1)+".png";}
        },armorNames[i]);
    }
    private static com.google.common.collect.Multimap weapon(com.google.common.collect.Multimap map,double damage,double speed){
        map.removeAll("generic.attackDamage");map.removeAll("generic.attackSpeed");
        map.put("generic.attackDamage",new net.minecraft.entity.ai.attributes.AttributeModifier(java.util.UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF"),"Weapon modifier",damage,0));
        map.put("generic.attackSpeed",new net.minecraft.entity.ai.attributes.AttributeModifier(java.util.UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3"),"Weapon modifier",speed,0));return map;
    }
    public static void duplicationRecipes(){
        for(Map.Entry<Item,String> entry:patterns.entrySet()){
            String name=entry.getValue();net.minecraft.block.Block center=Blocks.cobblestone;
            if("dune".equals(name))center=Blocks.sandstone;
            else if("eye".equals(name))center=Blocks.end_stone;
            else if("wild".equals(name))center=Blocks.mossy_cobblestone;
            else if("rib".equals(name))center=Blocks.netherrack;
            else if("ward".equals(name)||"silence".equals(name))center=com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.cobbledDeepslate;
            else if("snout".equals(name))center=com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.blackstone;
            else if("spire".equals(name))center=com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.purpurBlock;
            else if("tide".equals(name))center=com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.prismarine;
            else if("wayfinder".equals(name)||"shaper".equals(name)||"raiser".equals(name)||"host".equals(name))center=Blocks.hardened_clay;
            duplicate(entry.getKey(),center);
        }
        duplicate(upgradeTemplate,Blocks.netherrack);
    }
    private static void duplicate(Item item,net.minecraft.block.Block center){GameRegistry.addRecipe(new ItemStack(item,2),"DSD","DCD","DDD",'D',Items.diamond,'S',item,'C',center);}
    private static void registerUpgrade(Item base,Item item,String name){item.setUnlocalizedName("netherite_"+name).setTextureName("htgt6:1_20_backport/netherite_"+name);upgrades.put(base,item);}
    /** 1.20.2 copies the base item's tag, not the template/addition or serialized ItemStack root.
     * Container transaction state stays in Java fields, never in output NBT. */
    public static ItemStack upgrade(ItemStack base){if(base==null||base.stackSize<=0)return null;Item item=upgrades.get(base.getItem());if(item==null)return null;
        ItemStack result=new ItemStack(item,1,base.getItemDamage());if(base.hasTagCompound())result.setTagCompound((net.minecraft.nbt.NBTTagCompound)base.getTagCompound().copy());return result;}
    private SmithingContent120(){}
}
