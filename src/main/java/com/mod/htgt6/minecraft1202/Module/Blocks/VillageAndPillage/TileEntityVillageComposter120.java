package com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/** Virtual one-slot inventory exposing the 1.20 composter contract to 1.7 hoppers. */
public final class TileEntityVillageComposter120 extends TileEntity implements ISidedInventory {
    private static final int[] SLOT = {0};
    private boolean checkedMaturation;
    // Hopper extraction is tentative until markDirty; a failed transfer restores the slot.
    private boolean outputRemoved;

    @Override public void updateEntity() {
        if(worldObj==null||worldObj.isRemote||checkedMaturation)return;
        checkedMaturation=true;
        // Legacy saves may lack the scheduled tick. Existing scheduled entries deduplicate this.
        if(level()==7)worldObj.scheduleBlockUpdate(xCoord,yCoord,zCoord,getBlockType(),20);
    }

    private int level(){return worldObj==null?0:worldObj.getBlockMetadata(xCoord,yCoord,zCoord)&15;}
    private boolean boneMeal(ItemStack stack){return stack!=null&&stack.getItem()==Items.dye&&stack.getItemDamage()==15;}

    @Override public int getSizeInventory(){return 1;}
    @Override public ItemStack getStackInSlot(int slot){return slot==0&&!outputRemoved&&level()==8?new ItemStack(Items.dye,1,15):null;}
    @Override public ItemStack decrStackSize(int slot,int amount){
        if(worldObj==null||worldObj.isRemote||slot!=0||amount<1||outputRemoved||level()!=8)return null;
        ItemStack result=new ItemStack(Items.dye,1,15);
        outputRemoved=true;
        return result;
    }
    @Override public ItemStack getStackInSlotOnClosing(int slot){return null;}
    @Override public void setInventorySlotContents(int slot,ItemStack stack){
        if(worldObj==null||worldObj.isRemote||slot!=0)return;
        if(outputRemoved&&level()==8&&boneMeal(stack)&&stack.stackSize>0){
            outputRemoved=false;
            return;
        }
        if(isItemValidForSlot(slot,stack))
            BlockVillagePoi120.insertCompost(worldObj,xCoord,yCoord,zCoord,stack);
    }
    @Override public void markDirty(){
        if(worldObj!=null&&!worldObj.isRemote&&outputRemoved){
            outputRemoved=false;
            if(level()==8)BlockVillagePoi120.emptyComposter(worldObj,xCoord,yCoord,zCoord);
        }
        super.markDirty();
    }
    @Override public String getInventoryName(){return "container.village_pillage_composter";}
    @Override public boolean hasCustomInventoryName(){return false;}
    @Override public int getInventoryStackLimit(){return 1;}
    @Override public boolean isUseableByPlayer(EntityPlayer player){return false;}
    @Override public void openInventory(){}
    @Override public void closeInventory(){}
    @Override public boolean isItemValidForSlot(int slot,ItemStack stack){return slot==0&&stack!=null&&stack.stackSize>0&&level()<7&&BlockVillagePoi120.compostChance(stack)>=0F;}
    @Override public int[] getAccessibleSlotsFromSide(int side){return side==1&&level()<7||side==0&&level()==8?SLOT:new int[0];}
    @Override public boolean canInsertItem(int slot,ItemStack stack,int side){return side==1&&isItemValidForSlot(slot,stack);}
    @Override public boolean canExtractItem(int slot,ItemStack stack,int side){return side==0&&slot==0&&!outputRemoved&&level()==8&&boneMeal(stack);}
    @Override public void writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);outputRemoved=false;checkedMaturation=false;}
}
