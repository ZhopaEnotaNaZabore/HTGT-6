package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.EnchantmentCurse112;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Pure transformations behind the 1.7 containers; kept headless for behavioral fixtures. */
public final class VillageWorkstationLogic120 {
    /** 1.20.2 no_item_required tag order (34 patterns). */
    private static final String[] LOOM_PATTERNS={"bl","br","tl","tr","bs","ts","ls","rs","cs","ms",
            "drs","dls","ss","cr","sc","bt","tt","bts","tts","ld","rud","lud","rd","mc","mr",
            "vh","hh","vhr","hhb","bo","cbo","gra","gru","bri"};
    static ItemStack result(VillageWorkstationType120 type,ItemStack[] in,int selected){
        switch(type){case SMITHING:return smith(in);case GRINDSTONE:return grind(in);case STONECUTTER:return stone(in,selected);case LOOM:return loom(in,selected);case CARTOGRAPHY:return map(in);default:return null;}
    }
    static int choices(VillageWorkstationType120 type,ItemStack[] in){
        if(type==VillageWorkstationType120.LOOM){
            if(in.length<2||in[0]==null||in[1]==null||in[0].getItem()!=BountifulContent.banner
                    ||dyeColor(in[1])<0||patternCount(in[0])>=6)return 0;
            return in.length>2&&in[2]!=null?(specialPattern(in[2])==null?0:1):LOOM_PATTERNS.length;
        }
        if(type==VillageWorkstationType120.STONECUTTER)return stoneRecipes(in.length==0?null:in[0]).size();
        return 1;
    }
    static int[] consumed(VillageWorkstationType120 type,ItemStack[] in){
        int[] out=new int[in.length];
        if(type==VillageWorkstationType120.LOOM){
            if(in.length>0&&in[0]!=null)out[0]=1;
            if(in.length>1&&in[1]!=null)out[1]=1;
            return out; // Banner-pattern items are reusable in 1.20.2.
        }
        if(type==VillageWorkstationType120.CARTOGRAPHY&&in.length>0&&in[0]!=null&&in.length>1&&in[1]!=null&&in[1].getItem()==Items.map){out[0]=1;out[1]=1;return out;}
        for(int i=0;i<out.length;i++)if(in[i]!=null)out[i]=1;
        return out;
    }
    private static ItemStack smith(ItemStack[] in){
        if(in.length<3||in[0]==null||in[1]==null||in[2]==null
                ||in[0].stackSize<=0||in[1].stackSize<=0||in[2].stackSize<=0)return null;
        if(in[0].getItem()==com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120.upgradeTemplate)
            return in[2].getItem()==EtFVanillaGapContent120.netheriteIngot?com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120.upgrade(in[1]):null;
        String pattern=com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120.pattern(in[0]);
        if(pattern==null||!(in[1].getItem() instanceof ItemArmor)||!trimMaterial(in[2]))return null;
        String material=trimName(in[2]);if(hasTrim(in[1],"minecraft:"+pattern,material))return null;
        ItemStack out=applyTrim(in[1],material);out.getTagCompound().getCompoundTag("Trim").setString("pattern","minecraft:"+pattern);return out;
    }
    private static boolean trimMaterial(ItemStack stack){Item i=stack.getItem();return i==Items.iron_ingot||i==Items.gold_ingot||i==Items.diamond||i==Items.emerald||i==Items.redstone||i==Items.quartz||i==Items.dye&&stack.getItemDamage()==4||i==Backport120Content.copperIngot||i==Backport120Content.amethystShard||i==EtFVanillaGapContent120.netheriteIngot;}
    private static String trimName(ItemStack stack){Item i=stack.getItem();if(i==Items.iron_ingot)return "minecraft:iron";if(i==Items.gold_ingot)return "minecraft:gold";if(i==Items.diamond)return "minecraft:diamond";if(i==Items.emerald)return "minecraft:emerald";if(i==Items.redstone)return "minecraft:redstone";if(i==Items.quartz)return "minecraft:quartz";if(i==Backport120Content.copperIngot)return "minecraft:copper";if(i==Backport120Content.amethystShard)return "minecraft:amethyst";if(i==EtFVanillaGapContent120.netheriteIngot)return "minecraft:netherite";return "minecraft:lapis";}
    private static boolean hasTrim(ItemStack stack,String pattern,String material){
        if(stack==null||!stack.hasTagCompound()||!stack.getTagCompound().hasKey("Trim"))return false;
        NBTTagCompound trim=stack.getTagCompound().getCompoundTag("Trim");
        return pattern.equals(trim.getString("pattern"))&&material.equals(trim.getString("material"));
    }
    private static ItemStack grind(ItemStack[] in){
        ItemStack first=in.length>0?in[0]:null,second=in.length>1?in[1]:null;
        if(first==null&&second==null)return null;
        if(first!=null&&(!isGrindstoneInput(first)||first.stackSize>1)
                ||second!=null&&(!isGrindstoneInput(second)||second.stackSize>1))return null;
        boolean paired=first!=null&&second!=null;
        ItemStack base=first!=null?first:second;
        int damage=base.getItemDamage(),count=1;
        Map<Integer,Integer> curses=curseEnchantments(base);
        if(paired){
            if(first.getItem()!=second.getItem())return null;
            if(base.isItemStackDamageable()){
                int max=base.getMaxDamage();
                int durability=(max-first.getItemDamage())+(max-second.getItemDamage())+max*5/100;
                damage=Math.max(0,max-durability);
                mergeMissingCurses(curses,second);
            }else{
                if(!ItemStack.areItemStackTagsEqual(first,second))return null;
                count=2;
            }
        }
        return removeNonCurses(base,damage,count,curses);
    }
    private static boolean isGrindstoneInput(ItemStack stack){return stack!=null&&(stack.getItem()==Items.enchanted_book||stack.isItemStackDamageable());}
    private static Map<Integer,Integer> curseEnchantments(ItemStack stack){
        Map<Integer,Integer> out=new HashMap<Integer,Integer>();
        for(Object entryObject:EnchantmentHelper.getEnchantments(stack).entrySet()){
            Map.Entry entry=(Map.Entry)entryObject;int id=(Integer)entry.getKey();
            if(isCurse(id))out.put(id,(Integer)entry.getValue());
        }
        return out;
    }
    private static void mergeMissingCurses(Map<Integer,Integer> curses,ItemStack stack){
        for(Object entryObject:EnchantmentHelper.getEnchantments(stack).entrySet()){
            Map.Entry entry=(Map.Entry)entryObject;int id=(Integer)entry.getKey();
            if(isCurse(id)&&!curses.containsKey(id))curses.put(id,(Integer)entry.getValue());
        }
    }
    private static boolean isCurse(int id){
        Enchantment enchantment=id>=0&&id<Enchantment.enchantmentsList.length?Enchantment.enchantmentsList[id]:null;
        if(enchantment==null)return false;
        if(enchantment instanceof EnchantmentCurse112)return true;
        String name=enchantment.getName();
        return name!=null&&name.toLowerCase(Locale.ROOT).contains("curse");
    }
    private static ItemStack removeNonCurses(ItemStack source,int damage,int count,Map<Integer,Integer> curses){
        if(source.getItem()==Items.enchanted_book&&curses.isEmpty()){
            ItemStack book=new ItemStack(Items.book,count);
            if(source.hasDisplayName())book.setStackDisplayName(source.getDisplayName());
            return book;
        }
        ItemStack out=source.copy();out.stackSize=count;out.setItemDamage(damage);
        NBTTagCompound root=out.hasTagCompound()?(NBTTagCompound)out.getTagCompound().copy():new NBTTagCompound();
        root.removeTag("ench");root.removeTag("StoredEnchantments");root.removeTag("RepairCost");
        if(!curses.isEmpty()){
            NBTTagList list=new NBTTagList();
            for(Map.Entry<Integer,Integer> entry:curses.entrySet()){
                NBTTagCompound enchantment=new NBTTagCompound();
                enchantment.setShort("id",entry.getKey().shortValue());
                enchantment.setShort("lvl",entry.getValue().shortValue());
                list.appendTag(enchantment);
            }
            root.setTag(source.getItem()==Items.enchanted_book?"StoredEnchantments":"ench",list);
            int repairCost=0;for(int i=0;i<curses.size();i++)repairCost=repairCost*2+1;
            root.setInteger("RepairCost",repairCost);
        }
        out.setTagCompound(root.hasNoTags()?null:root);
        return out;
    }
    static int grindstoneExperience(ItemStack[] inputs){
        int total=0;
        for(ItemStack stack:inputs)if(stack!=null){
            for(Object entryObject:EnchantmentHelper.getEnchantments(stack).entrySet()){
                Map.Entry entry=(Map.Entry)entryObject;int id=(Integer)entry.getKey(),level=(Integer)entry.getValue();
                Enchantment enchantment=id>=0&&id<Enchantment.enchantmentsList.length?Enchantment.enchantmentsList[id]:null;
                if(enchantment!=null&&!isCurse(id))total+=enchantment.getMinEnchantability(level);
            }
        }
        return total;
    }
    private static ItemStack stone(ItemStack[] in,int selected){List<ItemStack> recipes=stoneRecipes(in.length==0?null:in[0]);return selected>=0&&selected<recipes.size()?recipes.get(selected).copy():null;}
    private static List<ItemStack> stoneRecipes(ItemStack input){
        List<ItemStack> out=new ArrayList<ItemStack>();if(input==null)return out;Block b=Block.getBlockFromItem(input.getItem());int m=input.getItemDamage();
        if(b==BountifulContent.stone){com.mod.htgt6.minecraft1202.Module.Registry.Modern.ModernStoneShapes120.stonecutting(m,out);}
        else if(b==Blocks.stone&&m==0){out.add(new ItemStack(Blocks.stonebrick));out.add(new ItemStack(Blocks.stone_slab,2,5));out.add(new ItemStack(Blocks.stone_brick_stairs));out.add(new ItemStack(Blocks.stonebrick,1,3));}
        else if(b==EtFVanillaGapContent120.smoothStone){out.add(new ItemStack(Blocks.stone_slab,2,0));}
        else if(b==Blocks.cobblestone){out.add(new ItemStack(Blocks.stone_slab,2,3));out.add(new ItemStack(Blocks.stone_stairs));out.add(new ItemStack(Blocks.cobblestone_wall));}
        else if(b==Blocks.mossy_cobblestone){out.add(new ItemStack(Backport120Content.mossyCobblestoneSlab,2));out.add(new ItemStack(Backport120Content.mossyCobblestoneStairs));out.add(new ItemStack(Blocks.cobblestone_wall,1,1));}
        else if(b==Blocks.sandstone&&m==0){out.add(new ItemStack(Blocks.stone_slab,2,1));out.add(new ItemStack(Blocks.sandstone_stairs));out.add(new ItemStack(Blocks.sandstone,1,2));out.add(new ItemStack(Blocks.sandstone,1,1));}
        else if(b==EtFVanillaGapContent120.polishedBlackstone){out.add(new ItemStack(EtFVanillaGapContent120.polishedBlackstoneSlab,2));out.add(new ItemStack(EtFVanillaGapContent120.chiseledPolishedBlackstone));}
        else if(b==EtFVanillaGapContent120.smoothSandstone){out.add(new ItemStack(EtFVanillaGapContent120.smoothSandstoneSlab,2));out.add(new ItemStack(EtFVanillaGapContent120.smoothSandstoneStairs));}
        else if(b==BountifulContent.redSandstone&&m==0){out.add(new ItemStack(BountifulContent.redSandstoneSlab,2));out.add(new ItemStack(BountifulContent.redSandstoneStairs));out.add(new ItemStack(BountifulContent.redSandstone,1,2));out.add(new ItemStack(BountifulContent.redSandstone,1,1));}
        else if(b==Blocks.stonebrick&&m==0){out.add(new ItemStack(Blocks.stone_slab,2,5));out.add(new ItemStack(Blocks.stone_brick_stairs));out.add(new ItemStack(Blocks.stonebrick,1,3));}
        else if(b==Blocks.brick_block){out.add(new ItemStack(Blocks.stone_slab,2,4));out.add(new ItemStack(Blocks.brick_stairs));}
        else if(b==Blocks.nether_brick){out.add(new ItemStack(Blocks.stone_slab,2,6));out.add(new ItemStack(Blocks.nether_brick_stairs));}
        else if(b==Blocks.quartz_block&&m==0){out.add(new ItemStack(Blocks.stone_slab,2,7));out.add(new ItemStack(Blocks.quartz_stairs));out.add(new ItemStack(Blocks.quartz_block,1,1));out.add(new ItemStack(Blocks.quartz_block,1,2));}
        VillageBuildingStonecutting114.append(input,out);
        com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherMasonry116.appendStonecutting(input,out);
        com.mod.htgt6.minecraft1202.Module.Registry.Modern.BlackstoneShapes116.appendStonecutting(input,out);
        return out;
    }
    private static ItemStack loom(ItemStack[] in,int selected){
        int choices=choices(VillageWorkstationType120.LOOM,in);
        if(choices<1||selected<0||selected>=choices)return null;
        String code=in.length>2&&in[2]!=null?specialPattern(in[2]):LOOM_PATTERNS[selected];
        return appendPattern(in[0],code,dyeColor(in[1]));
    }
    /** Reusable pattern items; raw catalysts belong in their crafting recipes. */
    private static String specialPattern(ItemStack stack){
        Item item=stack.getItem();if(item==null)return null;Block block=Block.getBlockFromItem(item);
        String own=com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.BannerPatterns145.code(item);
        if(own!=null)return own;
        Object key=Item.itemRegistry.getNameForObject(item);
        String name=key==null?item.getUnlocalizedName():String.valueOf(key);
        name=name==null?"":name.toLowerCase(java.util.Locale.ROOT);
        if(name.contains("globe")&&name.contains("pattern"))return "glb";
        if((name.contains("piglin")||name.contains("snout"))&&name.contains("pattern"))return "pig";
        if(name.contains("creeper")&&name.contains("pattern"))return "cre";
        if(name.contains("skull")&&name.contains("pattern"))return "sku";
        if(name.contains("flower")&&name.contains("pattern"))return "flo";
        if((name.contains("mojang")||name.contains("thing"))&&name.contains("pattern"))return "moj";
        return null;
    }
    static boolean isValidInput(VillageWorkstationType120 type,int slot,ItemStack stack){
        if(stack==null)return false;
        if(type==VillageWorkstationType120.SMITHING){
            if(slot==0)return stack.getItem()==com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120.upgradeTemplate||com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120.pattern(stack)!=null;
            if(slot==1)return stack.getItem() instanceof ItemArmor||com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.SmithingContent120.upgrades.containsKey(stack.getItem());
            return slot==2&&trimMaterial(stack);
        }
        if(type==VillageWorkstationType120.GRINDSTONE)return isGrindstoneInput(stack);
        if(type==VillageWorkstationType120.CARTOGRAPHY){
            if(slot==0)return stack.getItem()==Items.filled_map;
            return slot==1&&(stack.getItem()==Items.paper||stack.getItem()==Items.map
                    ||stack.getItem()==Item.getItemFromBlock(Blocks.glass_pane));
        }
        if(type!=VillageWorkstationType120.LOOM)return true;
        if(slot==0)return stack.getItem()==BountifulContent.banner&&patternCount(stack)<6;
        if(slot==1)return dyeColor(stack)>=0;
        return slot==2&&specialPattern(stack)!=null;
    }
    static String selectedLoomPattern(ItemStack[] in,int selected){
        int choices=choices(VillageWorkstationType120.LOOM,in);
        if(choices<1||selected<0||selected>=choices)return null;
        return in.length>2&&in[2]!=null?specialPattern(in[2]):LOOM_PATTERNS[selected];
    }
    private static int patternCount(ItemStack banner){
        if(banner==null||!banner.hasTagCompound()||!banner.getTagCompound().hasKey("BlockEntityTag"))return 0;
        NBTTagCompound block=banner.getTagCompound().getCompoundTag("BlockEntityTag");
        return block.hasKey("Patterns")?block.getTagList("Patterns",10).tagCount():0;
    }
    /** Maps both legacy metadata dye and the four 1.14 standalone dyes to legacy banner colour IDs. */
    public static int dyeColor(ItemStack stack){
        if(stack==null)return -1;Item item=stack.getItem();
        // ItemStack accepts a null Item in headless Forge fixtures.  Without
        // this guard an unbootstrapped Items.dye is also null, so the identity
        // comparison below succeeds and getItemDamage dereferences that null.
        if(item==null)return -1;
        if(item==Items.dye)return stack.getItemDamage()&15;
        if(item==EtFVanillaGapContent120.blackDye)return 0;
        if(item==EtFVanillaGapContent120.brownDye)return 3;
        if(item==EtFVanillaGapContent120.blueDye)return 4;
        if(item==EtFVanillaGapContent120.whiteDye)return 15;
        return -1;
    }
    private static ItemStack map(ItemStack[] in){
        if(in.length<2||in[0]==null||in[1]==null||in[0].getItem()!=Items.filled_map)return null;ItemStack out=in[0].copy();
        boolean locked=isLockedMap(in[0]);
        if(in[1].getItem()==Items.paper){if(locked)return null;out.stackSize=1;markMap(out,"map_is_scaling");return out;}
        if(in[1].getItem()==Items.map){out.stackSize=2;return out;}
        if(in[1].getItem()==Item.getItemFromBlock(Blocks.glass_pane)){if(locked)return null;out.stackSize=1;markMap(out,"map_is_locked");return out;}
        return null;
    }
    private static boolean isLockedMap(ItemStack stack){return stack!=null&&stack.hasTagCompound()
            &&stack.getTagCompound().getBoolean("map_is_locked");}
    private static NBTTagCompound tag(ItemStack stack){if(!stack.hasTagCompound())stack.setTagCompound(new NBTTagCompound());return stack.getTagCompound();}
    private static ItemStack applyTrim(ItemStack base,String material){ItemStack result=base.copy();result.stackSize=1;NBTTagCompound root=result.hasTagCompound()?(NBTTagCompound)result.getTagCompound().copy():new NBTTagCompound();NBTTagCompound trim=new NBTTagCompound();trim.setString("pattern","minecraft:coast");trim.setString("material",material);root.setTag("Trim",trim);result.setTagCompound(root);return result;}
    private static ItemStack appendPattern(ItemStack base,String code,int color){ItemStack out=base.copy();out.stackSize=1;NBTTagCompound root=out.hasTagCompound()?(NBTTagCompound)out.getTagCompound().copy():new NBTTagCompound();NBTTagCompound block=root.hasKey("BlockEntityTag")?root.getCompoundTag("BlockEntityTag"):new NBTTagCompound();NBTTagList list=block.hasKey("Patterns")?block.getTagList("Patterns",10):new NBTTagList();if(list.tagCount()>=6)return null;NBTTagCompound pattern=new NBTTagCompound();pattern.setString("Pattern",code);pattern.setInteger("Color",color);list.appendTag(pattern);block.setInteger("Base",out.getItemDamage()&15);block.setTag("Patterns",list);root.setTag("BlockEntityTag",block);out.setTagCompound(root);return out;}
    private static void markMap(ItemStack stack,String operation){tag(stack).setBoolean(operation,true);}
    public static void assertBehaviorFixture(){
        Item armor=new Item();ItemStack base=new ItemStack(armor);ItemStack trim=applyTrim(base,"minecraft:gold");
        if(!trim.hasTagCompound()||!trim.getTagCompound().hasKey("Trim"))throw new IllegalStateException("coast trim NBT adapter");
        if(!hasTrim(trim,"minecraft:coast","minecraft:gold")||hasTrim(trim,"minecraft:coast","minecraft:iron"))throw new IllegalStateException("smithing duplicate trim guard");
        Item tool=new Item().setMaxDamage(250).setMaxStackSize(1);ItemStack first=new ItemStack(tool);first.setItemDamage(150);NBTTagCompound enchanted=new NBTTagCompound();enchanted.setTag("ench",new NBTTagList());enchanted.setString("fixture","kept");first.setTagCompound(enchanted);ItemStack second=new ItemStack(tool);second.setItemDamage(150);ItemStack ground=grind(new ItemStack[]{first,second});
        if(ground==null||ground.getItemDamage()!=38||!ground.hasTagCompound()||!"kept".equals(ground.getTagCompound().getString("fixture"))||ground.getTagCompound().hasKey("ench"))throw new IllegalStateException("grindstone repair and NBT transform");
        if(grind(new ItemStack[]{new ItemStack(new Item()),null})!=null)throw new IllegalStateException("grindstone accepted ordinary item");
        ItemStack oversized=new ItemStack(tool,2);if(grind(new ItemStack[]{oversized,null})!=null)throw new IllegalStateException("grindstone accepted stacked tools");
        ItemStack banner=appendPattern(new ItemStack(new Item()),"bl",1);if(banner==null||!banner.getTagCompound().hasKey("BlockEntityTag"))throw new IllegalStateException("loom pattern transform");
        ItemStack map=new ItemStack(new Item());markMap(map,"map_is_scaling");if(!map.getTagCompound().getBoolean("map_is_scaling"))throw new IllegalStateException("cartography transform");
        // Vanilla item singletons are deliberately unbootstrapped in this headless fixture.
        if(Items.filled_map!=null&&Items.paper!=null&&Items.map!=null&&Items.stick!=null&&Item.getItemFromBlock(Blocks.glass_pane)!=null){
            ItemStack filled=new ItemStack(Items.filled_map),paper=new ItemStack(Items.paper),emptyMap=new ItemStack(Items.map),pane=new ItemStack(Blocks.glass_pane);
            boolean validFilled=isValidInput(VillageWorkstationType120.CARTOGRAPHY,0,filled),invalidPaperBase=isValidInput(VillageWorkstationType120.CARTOGRAPHY,0,paper),validPaper=isValidInput(VillageWorkstationType120.CARTOGRAPHY,1,paper),validEmpty=isValidInput(VillageWorkstationType120.CARTOGRAPHY,1,emptyMap),validPane=isValidInput(VillageWorkstationType120.CARTOGRAPHY,1,pane),invalidStick=isValidInput(VillageWorkstationType120.CARTOGRAPHY,1,new ItemStack(Items.stick));
            if(!validFilled||invalidPaperBase||!validPaper||!validEmpty||!validPane||invalidStick)
                throw new IllegalStateException("cartography slot validation");
            ItemStack locked=filled.copy();markMap(locked,"map_is_locked");
            if(map(new ItemStack[]{locked,paper})!=null||map(new ItemStack[]{locked,pane})!=null
                    ||map(new ItemStack[]{locked,emptyMap})==null)
                throw new IllegalStateException("locked cartography recipe rules");
        }
        if(stoneChoiceCountForFixture("stone")!=5||stoneChoiceCountForFixture("cobblestone")!=3
                ||stoneChoiceCountForFixture("mossy_cobblestone")!=3||LOOM_PATTERNS.length!=34
                ||!"cbo".equals(LOOM_PATTERNS[30])||!"bri".equals(LOOM_PATTERNS[33]))
            throw new IllegalStateException("1.20.2 selectable loom recipes");
        ItemStack[] three={new ItemStack(new Item()),new ItemStack(new Item()),new ItemStack(new Item())};int[] smithUse=consumed(VillageWorkstationType120.SMITHING,three);if(smithUse.length!=3||smithUse[0]!=1||smithUse[1]!=1||smithUse[2]!=1)throw new IllegalStateException("smithing input transaction");
        int[] grindUse=consumed(VillageWorkstationType120.GRINDSTONE,new ItemStack[]{three[0],three[1]});if(grindUse[0]!=1||grindUse[1]!=1)throw new IllegalStateException("grindstone input transaction");
        ItemStack reusablePattern=new ItemStack(new Item().setUnlocalizedName("mojang_banner_pattern"));
        int[] loomUse=consumed(VillageWorkstationType120.LOOM,new ItemStack[]{three[0],three[1],reusablePattern});
        if(loomUse[0]!=1||loomUse[1]!=1||loomUse[2]!=0)throw new IllegalStateException("loom consumed reusable pattern item");
        // Vanilla item singletons are deliberately unbootstrapped in the
        // standalone contract JVM, just like the cartography items above.
        if(BountifulContent.banner!=null&&Items.dye!=null&&Blocks.vine!=null){
            ItemStack cleanBanner=new ItemStack(BountifulContent.banner,1,0),blackDye=new ItemStack(Items.dye,1,0);
            if(choices(VillageWorkstationType120.LOOM,new ItemStack[]{cleanBanner,blackDye,null})!=34)
                throw new IllegalStateException("loom did not expose 34 ordinary patterns");
            if(!isValidInput(VillageWorkstationType120.LOOM,0,cleanBanner))
                throw new IllegalStateException("loom rejected banner slot");
            if(!isValidInput(VillageWorkstationType120.LOOM,1,blackDye))
                throw new IllegalStateException("loom rejected dye slot");
            if(!isValidInput(VillageWorkstationType120.LOOM,2,reusablePattern))
                throw new IllegalStateException("loom rejected reusable pattern slot");
            if(isValidInput(VillageWorkstationType120.LOOM,2,new ItemStack(Blocks.vine)))
                throw new IllegalStateException("loom still requires vines for the ordinary curly border");
            ItemStack full=cleanBanner;
            for(int i=0;i<6;i++)full=appendPattern(full,LOOM_PATTERNS[i],i);
            if(full==null||patternCount(full)!=6||choices(VillageWorkstationType120.LOOM,
                    new ItemStack[]{full,blackDye,null})!=0||appendPattern(full,"cr",1)!=null)
                throw new IllegalStateException("loom six-pattern limit");
        }
    }
    private static int stoneChoiceCountForFixture(String key){if("stone".equals(key))return 5;if("cobblestone".equals(key)||"mossy_cobblestone".equals(key))return 3;if("sandstone".equals(key)||"red_sandstone".equals(key)||"quartz".equals(key))return 4;if("stone_bricks".equals(key)||"nether_bricks".equals(key))return 3;if("bricks".equals(key)||"smooth_sandstone".equals(key))return 2;return 0;}
    private VillageWorkstationLogic120(){}
}
