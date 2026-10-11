package com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/** Persistent single-book inventory/page state used by the 1.20 lectern menu. */
public final class TileEntityVillageLectern120 extends TileEntity implements IInventory {
    private ItemStack book;
    private int page;
    public int getPage(){return page;}
    public void setPage(int value){page=Math.max(0,Math.min(pageCount()-1,value));markDirty();sync();}
    public int pageCount(){
        if(book==null||!book.hasTagCompound()||!book.getTagCompound().hasKey("pages"))return 1;
        return Math.max(1,book.getTagCompound().getTagList("pages",8).tagCount());
    }
    private void sync(){if(worldObj!=null){worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);worldObj.notifyBlocksOfNeighborChange(xCoord,yCoord,zCoord,getBlockType());}}
    @Override public int getSizeInventory(){return 1;}
    @Override public ItemStack getStackInSlot(int slot){return slot==0?book:null;}
    @Override public ItemStack decrStackSize(int slot,int amount){if(slot!=0||amount<=0||book==null)return null;ItemStack out=book;book=null;page=0;markDirty();sync();return out;}
    @Override public ItemStack getStackInSlotOnClosing(int slot){return decrStackSize(slot,64);}
    @Override public void setInventorySlotContents(int slot,ItemStack stack){if(slot!=0)return;book=stack==null||stack.stackSize<=0?null:stack; if(book!=null)book.stackSize=1;page=0;markDirty();sync();}
    @Override public String getInventoryName(){return "container.htgt6.lectern";}
    @Override public boolean hasCustomInventoryName(){return false;}
    @Override public int getInventoryStackLimit(){return 1;}
    @Override public boolean isUseableByPlayer(EntityPlayer player){return worldObj==null||worldObj.getTileEntity(xCoord,yCoord,zCoord)==this&&player.getDistanceSq(xCoord+.5D,yCoord+.5D,zCoord+.5D)<=64D;}
    @Override public void openInventory(){}
    @Override public void closeInventory(){}
    @Override public boolean isItemValidForSlot(int slot,ItemStack stack){return slot==0&&stack!=null&&stack.stackSize>0&&(stack.getItem()==net.minecraft.init.Items.written_book||stack.getItem()==net.minecraft.init.Items.writable_book);}
    @Override public void writeToNBT(NBTTagCompound tag){super.writeToNBT(tag);writeState(tag);}
    private void writeState(NBTTagCompound tag){if(book!=null){NBTTagCompound item=new NBTTagCompound();book.writeToNBT(item);tag.setTag("Book",item);}else tag.removeTag("Book");tag.setInteger("Page",page);}
    @Override public void readFromNBT(NBTTagCompound tag){super.readFromNBT(tag);readState(tag);}
    private void readState(NBTTagCompound tag){book=tag.hasKey("Book",10)?ItemStack.loadItemStackFromNBT(tag.getCompoundTag("Book")):null;if(book!=null){if(book.stackSize<=0)book=null;else book.stackSize=1;}page=Math.max(0,Math.min(pageCount()-1,tag.getInteger("Page")));}
    public int comparatorLevel(){return comparatorLevelFor(page,pageCount(),book!=null);}
    public static int comparatorLevelFor(int page,int pages,boolean hasBook){if(!hasBook)return 0;return pages<=1?15:1+(int)Math.floor(14D*Math.max(0,Math.min(pages-1,page))/(pages-1D));}
    @Override public Packet getDescriptionPacket(){NBTTagCompound tag=new NBTTagCompound();writeToNBT(tag);return new S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,2,tag);}
    @Override public void onDataPacket(NetworkManager manager,S35PacketUpdateTileEntity packet){readFromNBT(packet.func_148857_g());}
    public static void assertStateFixture(){TileEntityVillageLectern120 source=new TileEntityVillageLectern120();source.page=3;NBTTagCompound tag=new NBTTagCompound();source.writeState(tag);TileEntityVillageLectern120 copy=new TileEntityVillageLectern120();copy.readState(tag);if(copy.page!=0)throw new IllegalStateException("Lectern empty state page clamp");if(comparatorLevelFor(0,4,false)!=0||comparatorLevelFor(0,4,true)!=1||comparatorLevelFor(3,4,true)!=15||comparatorLevelFor(99,4,true)!=15)throw new IllegalStateException("Lectern comparator page levels");}
}
