package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/** Four ordered sides: back, left, right, front. Values are "brick" or pottery-shard names. */
public final class TileEntityBackport120DecoratedPot extends TileEntity {
    private final String[] decorations={"brick","brick","brick","brick"};
    private boolean shatter;
    private boolean suppressDrop;
    public String[] getDecorations(){return decorations.clone();}
    public void setDecorations(String[] values){for(int i=0;i<4;i++)decorations[i]=valid(values!=null&&i<values.length?values[i]:null);
        markDirty();if(worldObj!=null)worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}
    void setShatter(boolean value){shatter=value;}boolean shouldShatter(){return shatter;}
    void setSuppressDrop(boolean value){suppressDrop=value;}boolean shouldSuppressDrop(){return suppressDrop;}
    void readFromItem(ItemStack stack){if(stack!=null&&stack.hasTagCompound()&&stack.getTagCompound().hasKey("BlockEntityTag",10))
        readDecorations(stack.getTagCompound().getCompoundTag("BlockEntityTag"));}
    ItemStack asItem(){ItemStack stack=new ItemStack(Backport120Content.decoratedPot);NBTTagCompound root=new NBTTagCompound(),block=new NBTTagCompound();
        writeDecorations(block);root.setTag("BlockEntityTag",block);stack.setTagCompound(root);return stack;}
    private void writeDecorations(NBTTagCompound nbt){NBTTagList list=new NBTTagList();for(String value:decorations)list.appendTag(new NBTTagString(value));nbt.setTag("Sherds",list);}
    private void readDecorations(NBTTagCompound nbt){if(!nbt.hasKey("Sherds",9))return;NBTTagList list=nbt.getTagList("Sherds",8);
        for(int i=0;i<4;i++)decorations[i]=valid(i<list.tagCount()?list.getStringTagAt(i):null);}
    private static String valid(String value){if("brick".equals(value))return value;for(String name:Backport120Content.POTTERY_SHERD_NAMES)if(name.equals(value))return value;return "brick";}
    @Override public void writeToNBT(NBTTagCompound nbt){super.writeToNBT(nbt);writeDecorations(nbt);}
    @Override public void readFromNBT(NBTTagCompound nbt){super.readFromNBT(nbt);readDecorations(nbt);}
    @Override public Packet getDescriptionPacket(){NBTTagCompound nbt=new NBTTagCompound();writeToNBT(nbt);return new S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,7,nbt);}
    @Override public void onDataPacket(NetworkManager network,S35PacketUpdateTileEntity packet){readFromNBT(packet.func_148857_g());}
}
