package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

/** Persistent hidden archaeology loot and ten-step brushing progress. */
public final class TileEntityBackport120Brushable extends TileEntity {
    private ItemStack loot;
    private final BrushingProgress120 brushing = new BrushingProgress120();

    public void setLoot(ItemStack stack){loot=stack==null?null:stack.copy();markDirty();}
    public ItemStack getLoot(){return loot==null?null:loot.copy();}

    /** @return true only when this call completed the excavation. */
    public boolean brush(ForgeDirection direction){if(worldObj==null||worldObj.isRemote)return false;
        long now=worldObj.getTotalWorldTime();
        boolean advanced = brushing.brush(now);
        markDirty();
        if (!advanced) return false;
        worldObj.setBlockMetadataWithNotify(xCoord,yCoord,zCoord,brushing.dusted(),3);
        if(brushing.progress()<10)return false;ItemStack result=loot;loot=null;
        if(result!=null){EntityItem entity=new EntityItem(worldObj,xCoord+.5D+direction.offsetX*.55D,yCoord+.5D+direction.offsetY*.55D,
                zCoord+.5D+direction.offsetZ*.55D,result);entity.motionX=direction.offsetX*.08D;entity.motionY=.08D+direction.offsetY*.08D;
            entity.motionZ=direction.offsetZ*.08D;worldObj.spawnEntityInWorld(entity);}
        net.minecraft.block.Block block=worldObj.getBlock(xCoord,yCoord,zCoord);
        if(block instanceof BlockBackport120Suspicious)worldObj.setBlock(xCoord,yCoord,zCoord,
                ((BlockBackport120Suspicious)block).getSettledBlock(),0,3);
        return true;
    }

    @Override public void updateEntity(){if(worldObj==null||worldObj.isRemote)return;
        if(brushing.tick(worldObj.getTotalWorldTime())){
            worldObj.setBlockMetadataWithNotify(xCoord,yCoord,zCoord,brushing.dusted(),3);markDirty();}}
    @Override public void writeToNBT(NBTTagCompound nbt){super.writeToNBT(nbt);nbt.setInteger("Progress",brushing.progress());
        nbt.setLong("BrushCooldownEnds",brushing.cooldownEnds());nbt.setLong("BrushResetAt",brushing.resetAt());
        if(loot!=null){NBTTagCompound item=new NBTTagCompound();loot.writeToNBT(item);nbt.setTag("Loot",item);}}
    @Override public void readFromNBT(NBTTagCompound nbt){super.readFromNBT(nbt);
        long previous=nbt.getLong("LastBrush");
        brushing.restore(nbt.getInteger("Progress"),
                nbt.hasKey("BrushCooldownEnds")?nbt.getLong("BrushCooldownEnds"):previous+10,
                nbt.hasKey("BrushResetAt")?nbt.getLong("BrushResetAt"):previous+40);
        loot=nbt.hasKey("Loot",10)?ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("Loot")):null;}
}
