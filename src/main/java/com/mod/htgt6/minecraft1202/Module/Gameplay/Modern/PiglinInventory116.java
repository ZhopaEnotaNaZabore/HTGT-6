package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;

import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;

/** Eight persistent slots; tag membership uses owned identities, never display names. */
public final class PiglinInventory116 {
    private final ItemStack[] slots=new ItemStack[8];

    public static boolean loved(ItemStack stack){
        if(stack==null)return false;
        Item i=stack.getItem();
        if(i instanceof ItemArmor&&((ItemArmor)i).getArmorMaterial()==ItemArmor.ArmorMaterial.GOLD)return true;
        return i==Items.gold_ingot||i==Items.clock||i==Items.golden_carrot||i==Items.speckled_melon
            ||i==Items.golden_apple||i==Items.golden_horse_armor||i==Items.golden_sword
            ||i==Items.golden_pickaxe||i==Items.golden_shovel||i==Items.golden_axe||i==Items.golden_hoe
            ||i==Backport120Content.rawGold||i==Item.getItemFromBlock(Blocks.gold_ore)
            ||i==Item.getItemFromBlock(Blocks.gold_block)||i==Item.getItemFromBlock(Blocks.light_weighted_pressure_plate)
            ||i==Item.getItemFromBlock(EtFVanillaGapContent120.netherGoldOre)
            ||i==Item.getItemFromBlock(EtFVanillaGapContent120.gildedBlackstone)
            ||i==Item.getItemFromBlock(Backport120Content.rawGoldBlock)||i==Item.getItemFromBlock(VillagePillageContent.bell);
    }
    public static boolean food(ItemStack stack){return stack!=null&&(stack.getItem()==Items.porkchop||stack.getItem()==Items.cooked_porkchop);}
    public boolean hasRoom(ItemStack item){
        if(item==null)return false;
        for(ItemStack s:slots)if(s==null||s.isItemEqual(item)&&ItemStack.areItemStackTagsEqual(s,item)&&s.stackSize<s.getMaxStackSize())return true;
        return false;
    }
    /** Returns the uninserted remainder without retaining the caller's mutable stack. */
    public ItemStack insert(ItemStack source){
        if(source==null||source.stackSize<=0)return null;
        ItemStack remainder=source.copy();
        for(int pass=0;pass<2;pass++)for(int i=0;i<slots.length;i++){
            ItemStack stored=slots[i];
            if(pass==0&&stored!=null&&stored.isItemEqual(remainder)&&ItemStack.areItemStackTagsEqual(stored,remainder)){
                int count=Math.min(remainder.stackSize,Math.max(0,stored.getMaxStackSize()-stored.stackSize));stored.stackSize+=count;remainder.stackSize-=count;
            }else if(pass==1&&stored==null){int count=Math.min(remainder.stackSize,remainder.getMaxStackSize());slots[i]=remainder.copy();slots[i].stackSize=count;remainder.stackSize-=count;}
            if(remainder.stackSize==0)return null;
        }
        return remainder;
    }
    public boolean isEmpty(){for(ItemStack s:slots)if(s!=null)return false;return true;}
    public NBTTagList write(){NBTTagList list=new NBTTagList();for(int i=0;i<slots.length;i++)if(slots[i]!=null){NBTTagCompound n=slots[i].writeToNBT(new NBTTagCompound());n.setByte("Slot",(byte)i);list.appendTag(n);}return list;}
    public void read(NBTTagList list){java.util.Arrays.fill(slots,null);for(int i=0;i<list.tagCount();i++){NBTTagCompound n=list.getCompoundTagAt(i);int slot=n.getByte("Slot")&255;ItemStack s=ItemStack.loadItemStackFromNBT(n);if(slot<slots.length&&s!=null&&s.stackSize>0){s.stackSize=Math.min(s.stackSize,s.getMaxStackSize());slots[slot]=s;}}}
    public void drop(net.minecraft.entity.Entity entity){for(int i=0;i<slots.length;i++)if(slots[i]!=null){entity.entityDropItem(slots[i],1);slots[i]=null;}}
}
