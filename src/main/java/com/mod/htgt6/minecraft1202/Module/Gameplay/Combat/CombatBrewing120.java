package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;

/** 1.9 Dragon's Breath brewing recipe adapted to Forge's four-slot legacy stand. */
public final class CombatBrewing120 {
    public static ItemStack lingeringResult(ItemStack input) {
        if (input == null || input.getItem() != Items.potionitem || !ItemPotion.isSplash(input.getItemDamage()))
            return null;
        ItemStack result=new ItemStack(CombatContent.lingeringPotion, input.stackSize, lingeringDamage(input.getItemDamage()));
        if(input.hasTagCompound())result.setTagCompound((net.minecraft.nbt.NBTTagCompound)input.getTagCompound().copy());
        return result;
    }
    public static boolean canBrew(net.minecraft.tileentity.TileEntityBrewingStand stand){
        ItemStack ingredient=stand.getStackInSlot(3);
        if(ingredient==null||ingredient.stackSize<=0||ingredient.getItem()!=CombatContent.dragonBreath)return false;
        for(int i=0;i<3;i++)if(lingeringResult(stand.getStackInSlot(i))!=null)return true;
        return false;
    }
    public static boolean brew(net.minecraft.tileentity.TileEntityBrewingStand stand){
        if(!canBrew(stand))return false;
        ItemStack[] inventory=new ItemStack[4];for(int i=0;i<4;i++)inventory[i]=stand.getStackInSlot(i);
        boolean cancelled=net.minecraftforge.event.ForgeEventFactory.onPotionAttemptBreaw(inventory);
        for(int i=0;i<4;i++)stand.setInventorySlotContents(i,inventory[i]);
        if(cancelled||!canBrew(stand))return true;
        for(int i=0;i<3;i++){ItemStack result=lingeringResult(stand.getStackInSlot(i));if(result!=null)stand.setInventorySlotContents(i,result);}
        ItemStack ingredient=stand.getStackInSlot(3);--ingredient.stackSize;
        if(ingredient.stackSize<=0)stand.setInventorySlotContents(3,new ItemStack(Items.glass_bottle));
        else if(stand.getWorldObj()!=null&&!stand.getWorldObj().isRemote)
            stand.getWorldObj().spawnEntityInWorld(new net.minecraft.entity.item.EntityItem(stand.getWorldObj(),stand.xCoord+.5,stand.yCoord+1,stand.zCoord+.5,new ItemStack(Items.glass_bottle)));
        for(int i=0;i<4;i++)inventory[i]=stand.getStackInSlot(i);
        net.minecraftforge.event.ForgeEventFactory.onPotionBrewed(inventory);
        for(int i=0;i<4;i++)stand.setInventorySlotContents(i,inventory[i]);
        stand.markDirty();return true;
    }
    public static int lingeringDamage(int splashDamage) {
        return ItemPotion.isSplash(splashDamage) ? splashDamage & ~16384 : -1;
    }
}
