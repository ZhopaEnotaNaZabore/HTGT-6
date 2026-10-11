package com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/** Stores the 1.8 banner base colour and ordered pattern list. */
public final class TileEntityBountifulBanner extends TileEntity {
    private int baseColor;
    private String customName;
    private NBTTagList patterns = new NBTTagList();

    public int getBaseColor() { return baseColor & 15; }
    public NBTTagList getPatterns() { return (NBTTagList)patterns.copy(); }
    public void setBannerData(int base, NBTTagList value) { baseColor = base & 15; patterns = value == null ? new NBTTagList() : (NBTTagList)value.copy(); markDirty(); if(worldObj!=null)worldObj.markBlockForUpdate(xCoord,yCoord,zCoord); }

    public void readFromItem(net.minecraft.item.ItemStack stack) {
        int base = stack.getItemDamage() & 15; NBTTagList value = new NBTTagList();
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("BlockEntityTag")) {
            NBTTagCompound blockTag = stack.getTagCompound().getCompoundTag("BlockEntityTag");
            if (blockTag.hasKey("Base")) base = blockTag.getInteger("Base");
            if (blockTag.hasKey("Patterns")) value = blockTag.getTagList("Patterns", 10);
        }
        customName=stack.hasDisplayName()?stack.getDisplayName():null;
        setBannerData(base, value);
    }

    public net.minecraft.item.ItemStack asItem() {
        net.minecraft.item.ItemStack stack = new net.minecraft.item.ItemStack(BountifulContent.banner, 1, getBaseColor());
        if (patterns.tagCount() > 0) { NBTTagCompound root = new NBTTagCompound(), blockTag = new NBTTagCompound(); blockTag.setInteger("Base", getBaseColor()); blockTag.setTag("Patterns", patterns.copy()); root.setTag("BlockEntityTag", blockTag); stack.setTagCompound(root); }
        if(customName!=null&&!customName.isEmpty())stack.setStackDisplayName(customName);
        return stack;
    }

    @Override public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); tag.setInteger("Base", getBaseColor()); tag.setTag("Patterns", patterns.copy()); if(customName!=null)tag.setString("CustomName",customName); }
    @Override public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag); customName=tag.hasKey("CustomName",8)?tag.getString("CustomName"):null; baseColor = tag.getInteger("Base") & 15; patterns = tag.hasKey("Patterns") ? tag.getTagList("Patterns", 10) : new NBTTagList(); }
    @Override public Packet getDescriptionPacket() { NBTTagCompound tag = new NBTTagCompound(); writeToNBT(tag); return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 6, tag); }
    @Override public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet) { readFromNBT(packet.func_148857_g()); }
}
