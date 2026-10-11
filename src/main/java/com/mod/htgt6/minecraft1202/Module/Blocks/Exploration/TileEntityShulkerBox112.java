package com.mod.htgt6.minecraft1202.Module.Blocks.Exploration;

import com.mod.htgt6.minecraft1202.Module.Items.Exploration.ItemBlockShulkerBox112;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

/** 1.12 shulker-box inventory contract: 27 slots, Items, CustomName and Lock. */
public final class TileEntityShulkerBox112 extends TileEntity implements IInventory {
    private ItemStack[] items = new ItemStack[27];
    private String customName;
    private String lockCode = "";
    private int openCount;
    private float progress;
    private float previousProgress;

    @Override public int getSizeInventory() { return items.length; }
    @Override public ItemStack getStackInSlot(int slot) { return slot >= 0 && slot < items.length ? items[slot] : null; }
    @Override public ItemStack decrStackSize(int slot, int count) {
        ItemStack stack = getStackInSlot(slot);
        if (stack == null) return null;
        if (stack.stackSize <= count) { items[slot] = null; markDirty(); return stack; }
        ItemStack result = stack.splitStack(count);
        if (stack.stackSize <= 0) items[slot] = null;
        markDirty(); return result;
    }
    @Override public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = getStackInSlot(slot); if (stack != null) items[slot] = null; return stack;
    }
    @Override public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= items.length) return;
        items[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) stack.stackSize = getInventoryStackLimit();
        markDirty();
    }
    @Override public String getInventoryName() { return hasCustomInventoryName() ? customName : "container.shulkerBox"; }
    @Override public boolean hasCustomInventoryName() { return customName != null && !customName.isEmpty(); }
    public void setCustomName(String name) { customName = name; }
    public boolean canUnlock(ItemStack key) {
        return lockCode.isEmpty() || key != null && key.stackSize > 0 && key.hasDisplayName()
                && lockCode.equals(key.getDisplayName());
    }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj != null && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
    }
    @Override public void openInventory() {
        if (worldObj != null && worldObj.isRemote) return;
        openCount++;
        sendOpenCount();
    }
    @Override public void closeInventory() {
        if (worldObj != null && worldObj.isRemote) return;
        openCount = Math.max(0, openCount - 1);
        sendOpenCount();
    }
    private void sendOpenCount() {
        if (worldObj != null) worldObj.addBlockEvent(xCoord, yCoord, zCoord, getBlockType(), 1, openCount);
    }
    @Override public boolean receiveClientEvent(int id, int value) {
        if (id != 1) return super.receiveClientEvent(id, value);
        openCount = Math.max(0, value);
        return true;
    }
    @Override public void updateEntity() {
        previousProgress = progress;
        progress = openCount > 0 ? Math.min(1.0F, progress + 0.1F) : Math.max(0.0F, progress - 0.1F);
        if(progress>previousProgress&&worldObj!=null&&!worldObj.isRemote)pushEntities();
        if(worldObj!=null&&!worldObj.isRemote&&progress!=previousProgress&&(previousProgress==0F||progress==0F))
            worldObj.notifyBlocksOfNeighborChange(xCoord,yCoord,zCoord,getBlockType());
    }
    private void pushEntities() {
        int meta=worldObj.getBlockMetadata(xCoord,yCoord,zCoord);
        net.minecraftforge.common.util.ForgeDirection direction=ShulkerBoxShape112.direction(meta);
        // Vanilla push sweep uses unscaled progress; collision bounds use half progress.
        net.minecraft.util.AxisAlignedBB sweep=ShulkerBoxShape112.sweep(meta,previousProgress,progress).offset(xCoord,yCoord,zCoord);
        double distance=progress-previousProgress+.01D;
        for(Object value:worldObj.getEntitiesWithinAABBExcludingEntity(null,sweep)) {
            net.minecraft.entity.Entity entity=(net.minecraft.entity.Entity)value;
            if(!entity.isDead&&!entity.noClip)entity.moveEntity(direction.offsetX*distance,direction.offsetY*distance,direction.offsetZ*distance);
        }
    }
    public float getProgress(float partialTicks) { return previousProgress + (progress - previousProgress) * partialTicks; }
    public boolean isFullyClosed() { return openCount==0&&progress==0F; }
    @Override public net.minecraft.network.Packet getDescriptionPacket() {
        NBTTagCompound state = new NBTTagCompound(); state.setInteger("OpenCount", openCount);
        return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, state);
    }
    @Override public void onDataPacket(net.minecraft.network.NetworkManager manager,
            net.minecraft.network.play.server.S35PacketUpdateTileEntity packet) {
        receiveClientEvent(1, packet.func_148857_g().getInteger("OpenCount"));
    }
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    @Override public net.minecraft.util.AxisAlignedBB getRenderBoundingBox() {
        int meta=worldObj==null?0:worldObj.getBlockMetadata(xCoord,yCoord,zCoord);
        return ShulkerBoxShape112.bounds(meta,.5D).offset(xCoord,yCoord,zCoord);
    }
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return stack == null || !(stack.getItem() instanceof ItemBlockShulkerBox112);
    }

    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        writeInventory(tag);
    }

    public void writeInventory(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (int slot = 0; slot < items.length; ++slot) if (items[slot] != null) {
            NBTTagCompound item = new NBTTagCompound();
            item.setByte("Slot", (byte)slot);
            items[slot].writeToNBT(item);
            list.appendTag(item);
        }
        tag.setTag("Items", list);
        if (hasCustomInventoryName()) tag.setString("CustomName", customName);
        if (!lockCode.isEmpty()) tag.setString("Lock", lockCode);
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readInventory(tag);
    }

    public void readInventory(NBTTagCompound tag) {
        items = new ItemStack[27];
        NBTTagList list = tag.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound item = list.getCompoundTagAt(i);
            int slot = item.getByte("Slot") & 255;
            if (slot < items.length) items[slot] = ItemStack.loadItemStackFromNBT(item);
        }
        customName = tag.hasKey("CustomName", 8) ? tag.getString("CustomName") : null;
        lockCode = tag.hasKey("Lock", 8) ? tag.getString("Lock") : "";
    }
}
