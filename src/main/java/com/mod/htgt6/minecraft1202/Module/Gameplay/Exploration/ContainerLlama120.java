package com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration;

import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;

public final class ContainerLlama120 extends Container {
    public static final int GUI_ID = 111;
    public final EntityLlama112 llama;
    public final int columns;
    private final int animalSlots;
    public ContainerLlama120(EntityPlayer player, EntityLlama112 llama, int columns) {
        this.llama=llama; this.columns=Math.max(0,Math.min(5,columns));
        addSlotToContainer(new Slot(llama.getLlamaInventory(),1,8,36){
            @Override public boolean isItemValid(ItemStack stack){return EntityLlama112.isCarpet(stack);}
            @Override public int getSlotStackLimit(){return 1;}
        });
        for(int row=0;row<3;row++)for(int col=0;col<this.columns;col++)
            addSlotToContainer(new Slot(llama.getLlamaInventory(),2+row*this.columns+col,80+18*col,18+18*row));
        animalSlots=inventorySlots.size();
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)
            addSlotToContainer(new Slot(player.inventory,9+row*9+col,8+col*18,84+row*18));
        for(int col=0;col<9;col++)addSlotToContainer(new Slot(player.inventory,col,8+col*18,142));
    }
    @Override public boolean canInteractWith(EntityPlayer player){
        return player!=null&&player.isEntityAlive()&&player.worldObj==llama.worldObj
                &&llama.isEntityAlive()&&llama.isTamed()&&!llama.isChild()
                &&player.getDistanceSqToEntity(llama)<64&&llama.getStorageColumns()==columns;
    }
    @Override public ItemStack transferStackInSlot(EntityPlayer player,int index){
        if(index<0||index>=inventorySlots.size()||!canInteractWith(player))return null;
        Slot slot=(Slot)inventorySlots.get(index);if(!slot.getHasStack())return null;
        ItemStack stack=slot.getStack(),copy=stack.copy();
        if(index<animalSlots){if(!mergeItemStack(stack,animalSlots,inventorySlots.size(),true))return null;}
        else if(EntityLlama112.isCarpet(stack)&&!((Slot)inventorySlots.get(0)).getHasStack()){
            // Legacy mergeItemStack ignores the destination limit on an empty slot.
            ((Slot)inventorySlots.get(0)).putStack(stack.splitStack(1));
        }else if(animalSlots>1&&mergeItemStack(stack,1,animalSlots,false)){
            // Cargo accepted.
        }else if(index<animalSlots+27){if(!mergeItemStack(stack,animalSlots+27,inventorySlots.size(),false))return null;}
        else if(!mergeItemStack(stack,animalSlots,animalSlots+27,false))return null;
        if(stack.stackSize==0)slot.putStack(null);else slot.onSlotChanged();
        if(stack.stackSize==copy.stackSize)return null;
        slot.onPickupFromSlot(player,stack);return copy;
    }
}
