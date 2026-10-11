package com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.CampfireRecipes149;
import net.minecraft.tileentity.TileEntity;import net.minecraft.item.ItemStack;import net.minecraft.nbt.*;import net.minecraft.entity.item.EntityItem;
import net.minecraft.network.*;import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
/** Four independent cooking slots. Deliberately no IInventory: vanilla campfires have no hopper or GUI. */
public final class TileEntityCampfire149 extends TileEntity {
 private final ItemStack[] items=new ItemStack[4];private final int[] progress=new int[4];
 public ItemStack item(int slot){return items[slot];}public int progress(int slot){return progress[slot];}
public boolean insert(ItemStack input, boolean creative) {
    if (input == null || input.getItem() == null || input.stackSize <= 0
            || worldObj == null || worldObj.isRemote
            || CampfireRecipes149.result(input) == null) {
        return false;
    }

    for (int i = 0; i < items.length; i++) {
        if (items[i] == null) {
            ItemStack stored = input.copy();
            stored.stackSize = 1;
            items[i] = stored;
            progress[i] = 0;
            if (!creative) {
                input.stackSize--;
            }
            changed();
            return true;
        }
    }
    return false;
}

 @Override public void updateEntity(){
  if(worldObj==null)return;if(worldObj.isRemote){if(BlockCampfire149.lit(worldObj.getBlockMetadata(xCoord,yCoord,zCoord)))clientParticles();return;}boolean dirty=false;
  boolean lit=BlockCampfire149.lit(worldObj.getBlockMetadata(xCoord,yCoord,zCoord));
  for(int i=0;i<4;i++)if(items[i]!=null){
   if(!lit){if(progress[i]>0){progress[i]=Math.max(0,progress[i]-2);dirty=true;}continue;}
   if(progress[i]<600){progress[i]++;dirty=true;}
   if(progress[i]>=600){ItemStack out=CampfireRecipes149.result(items[i]);if(out==null)out=items[i].copy();
    EntityItem entity=new EntityItem(worldObj,xCoord+.5,yCoord+.5,zCoord+.5,out);entity.delayBeforeCanPickup=10;
    if(worldObj.spawnEntityInWorld(entity)){items[i]=null;progress[i]=0;changed();}
   }
  }
  if(dirty)markDirty();
 }
 @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
 private void clientParticles(){com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.CampfireSmoke152.tick(this);}
 public void dropContents(){if(worldObj==null||worldObj.isRemote)return;for(int i=0;i<4;i++)if(items[i]!=null){worldObj.spawnEntityInWorld(new EntityItem(worldObj,xCoord+.5,yCoord+.5,zCoord+.5,items[i].copy()));items[i]=null;progress[i]=0;}changed();}
 private void changed(){markDirty();if(worldObj!=null)worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}
 @Override public void writeToNBT(NBTTagCompound n){super.writeToNBT(n);NBTTagList list=new NBTTagList();for(int i=0;i<4;i++)if(items[i]!=null){NBTTagCompound tag=new NBTTagCompound();tag.setByte("Slot",(byte)i);items[i].writeToNBT(tag);tag.setInteger("CookingTime",progress[i]);list.appendTag(tag);}n.setTag("Items",list);}
@Override
public void readFromNBT(NBTTagCompound n) {
    super.readFromNBT(n);

    for (int i = 0; i < items.length; i++) {
        items[i] = null;
        progress[i] = 0;
    }

    if (!n.hasKey("Items", 9)) {
        return;
    }

    NBTTagList list = n.getTagList("Items", 10);
    for (int j = 0; j < list.tagCount(); j++) {
        NBTTagCompound tag = list.getCompoundTagAt(j);
        if (!tag.hasKey("Slot", 99)) {
            continue;
        }

        int slot = tag.getInteger("Slot");
        if (slot < 0 || slot >= items.length || items[slot] != null) {
            continue;
        }

        ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
        if (stack == null || stack.getItem() == null || stack.stackSize <= 0) {
            continue;
        }

        stack.stackSize = 1;
        items[slot] = stack;
        progress[slot] = Math.max(0, Math.min(600, tag.getInteger("CookingTime")));
    }
}

 @Override public Packet getDescriptionPacket(){NBTTagCompound n=new NBTTagCompound();writeToNBT(n);return new S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,1,n);}
 @Override public void onDataPacket(NetworkManager manager,S35PacketUpdateTileEntity packet){readFromNBT(packet.func_148857_g());}
}
