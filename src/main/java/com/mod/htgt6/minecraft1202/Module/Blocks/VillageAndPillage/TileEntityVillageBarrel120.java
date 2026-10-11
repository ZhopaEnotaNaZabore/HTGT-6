package com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage;
/** Keeps the native inventory/container contract without chest adjacency or lid animation. */
public final class TileEntityVillageBarrel120 extends net.minecraft.tileentity.TileEntityChest {
    private int viewers;
    @Override public String getInventoryName(){return hasCustomInventoryName()?super.getInventoryName():"container.barrel";}
    @Override public void updateEntity(){
        if(worldObj==null||worldObj.isRemote)return;
        if(worldObj.getTotalWorldTime()%20==0){
            int count=0;
            for(Object raw:worldObj.playerEntities){net.minecraft.entity.player.EntityPlayer p=(net.minecraft.entity.player.EntityPlayer)raw;
                if(p.openContainer instanceof net.minecraft.inventory.ContainerChest
                    &&((net.minecraft.inventory.ContainerChest)p.openContainer).getLowerChestInventory()==this)count++;}
            viewers=count;updateOpen();
        }
    }
    @Override public void openInventory(){if(worldObj!=null&&!worldObj.isRemote){viewers++;updateOpen();}}
    @Override public void closeInventory(){if(worldObj!=null&&!worldObj.isRemote){viewers=Math.max(0,viewers-1);updateOpen();}}
    private void updateOpen(){int meta=getBlockMetadata(),next=viewers>0?meta|8:meta&~8;
        if(meta!=next){worldObj.setBlockMetadataWithNotify(xCoord,yCoord,zCoord,next,2);
            worldObj.playSoundEffect(xCoord+.5,yCoord+.5,zCoord+.5,viewers>0?"htgt6:block.barrel.open":"htgt6:block.barrel.close",.5F,.9F+worldObj.rand.nextFloat()*.1F);}}
    @Override public void checkForAdjacentChests(){}
}
