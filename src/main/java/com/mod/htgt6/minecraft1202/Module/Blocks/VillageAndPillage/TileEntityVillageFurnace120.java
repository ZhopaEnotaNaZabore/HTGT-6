package com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemFood;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.item.crafting.FurnaceRecipes;

/** Blast-furnace/smoker tick without vanilla BlockFurnace's destructive block swap. */
public final class TileEntityVillageFurnace120 extends TileEntityFurnace {
    private static final int COOK_TIME=100;
    private boolean smoker;
    public TileEntityVillageFurnace120(){this(false);} TileEntityVillageFurnace120(boolean smoker){this.smoker=smoker;}
    public String furnaceType(){return smoker?"smoker":"blast_furnace";}
    @Override public String getInventoryName(){return hasCustomInventoryName()?super.getInventoryName():"container."+furnaceType();}
    @Override public void updateEntity(){
        boolean wasBurning=isBurning(),dirty=false;
        if(furnaceBurnTime>0)--furnaceBurnTime;
        if(worldObj==null||worldObj.isRemote)return;
        ItemStack fuel=getStackInSlot(1),input=getStackInSlot(0);
        if(furnaceBurnTime!=0||fuel!=null&&input!=null){
            if(furnaceBurnTime==0&&canCook()){
                currentItemBurnTime=furnaceBurnTime=getItemBurnTime(fuel)/2;
                if(furnaceBurnTime>0){dirty=true;if(fuel!=null){
                    ItemStack container=fuel.getItem().getContainerItem(fuel);
                    --fuel.stackSize;
                    if(fuel.stackSize<=0)setInventorySlotContents(1,container);
                }}
            }
            if(isBurning()&&canCook()){
                if(++furnaceCookTime>=COOK_TIME){furnaceCookTime=0;smeltItem();dirty=true;}
            }else furnaceCookTime=0;
        }else furnaceCookTime=0;
        if(wasBurning!=isBurning()){
            dirty=true;int metadata=getBlockMetadata();
            worldObj.setBlockMetadataWithNotify(xCoord,yCoord,zCoord,
                    isBurning()?metadata|4:metadata&~4,2);
            worldObj.func_147451_t(xCoord,yCoord,zCoord);
        }
        if(dirty)markDirty();
    }
    private boolean canCook(){
        ItemStack input=getStackInSlot(0);if(input==null)return false;
        ItemStack result=FurnaceRecipes.smelting().getSmeltingResult(input);if(result==null)return false;
        if(smoker){if(!(result.getItem() instanceof ItemFood))return false;}
        else if(!com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageFurnaceRecipes143.isBlastable(input))return false;
        ItemStack output=getStackInSlot(2);if(output==null)return true;
        if(!output.isItemEqual(result)||!ItemStack.areItemStackTagsEqual(output,result))return false;
        int size=output.stackSize+result.stackSize;
        return size<=getInventoryStackLimit()&&size<=output.getMaxStackSize();
    }
    @Override public int getCookProgressScaled(int pixels){return furnaceCookTime*pixels/COOK_TIME;}
    @Override public void writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);tag.setBoolean("Smoker120",smoker);}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);smoker=tag.getBoolean("Smoker120");currentItemBurnTime=getItemBurnTime(getStackInSlot(1))/2;}
}
