package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageLectern120;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.item.ItemMap;
import net.minecraft.world.storage.MapData;

/** Server-authoritative 1.7 container bridge for the 1.20 workstation menus. */
public final class ContainerVillageWorkstation120 extends Container {
    public static final int LOOM_SELECTION_BASE = 2;
    public static final int STONECUTTER_SELECTION_BASE = 2;
    public static final int LECTERN_TAKE_BOOK = 2;
    private final VillageWorkstationType120 type;
    private final World world;
    private final int x,y,z;
    private final IInventory input;
    private final InventoryBasic output;
    private int selected;
    private boolean recomputing;
    private boolean smithingTransaction;
    private boolean closed;

    public ContainerVillageWorkstation120(InventoryPlayer player,VillageWorkstationType120 type,World world,int x,int y,int z){
        this.type=type;this.world=world;this.x=x;this.y=y;this.z=z;
        if(type==VillageWorkstationType120.LECTERN&&world.getTileEntity(x,y,z) instanceof TileEntityVillageLectern120)
            input=(TileEntityVillageLectern120)world.getTileEntity(x,y,z);
        else input=new WorkInventory(type.inputSlots);
        output=type==VillageWorkstationType120.LECTERN?null:new InventoryBasic("workstation.result",false,1);
        addWorkSlots();if(type!=VillageWorkstationType120.LECTERN)addPlayerSlots(player);recompute();
    }
    private void addWorkSlots(){
        int[][] layout=slotLayout(type);
        if(type==VillageWorkstationType120.LECTERN)return;
        for(int i=0;i<type.inputSlots;i++)addSlotToContainer(new ValidatedSlot(input,i,layout[i][0],layout[i][1]));
        if(type!=VillageWorkstationType120.LECTERN)
            addSlotToContainer(new OutputSlot(output,0,layout[type.inputSlots][0],layout[type.inputSlots][1]));
    }
    /** Minecraft 1.20.2 container coordinates, shared with the matching GUI textures. */
    public static int[][] slotLayout(VillageWorkstationType120 type){switch(type){
        case SMITHING:return new int[][]{{8,48},{26,48},{44,48},{98,48}};
        case GRINDSTONE:return new int[][]{{49,19},{49,40},{129,34}};
        case STONECUTTER:return new int[][]{{20,33},{143,33}};
        case LOOM:return new int[][]{{13,26},{33,26},{23,45},{143,58}};
        case CARTOGRAPHY:return new int[][]{{15,15},{15,52},{145,39}};
        default:return new int[][]{{15,15}};
    }}
    private void addPlayerSlots(InventoryPlayer player){for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlotToContainer(new Slot(player,col+row*9+9,8+col*18,84+row*18));for(int col=0;col<9;col++)addSlotToContainer(new Slot(player,col,8+col*18,142));}
    @Override public boolean canInteractWith(EntityPlayer player){return !closed&&world.getBlock(x,y,z)==blockFor(type)&&player.getDistanceSq(x+.5D,y+.5D,z+.5D)<=64D;}
    @Override public void onCraftMatrixChanged(IInventory inventory){super.onCraftMatrixChanged(inventory);recompute();}
    private void recompute(){if(recomputing||closed||output==null)return;recomputing=true;int choices=Math.max(1,VillageWorkstationLogic120.choices(type,inputs()));if(selected>=choices)selected=choices-1;ItemStack result=VillageWorkstationLogic120.result(type,inputs(),selected);if(type==VillageWorkstationType120.CARTOGRAPHY&&result!=null&&result.hasTagCompound()&&result.getTagCompound().getBoolean("map_is_scaling")){ItemStack source=input.getStackInSlot(0);MapData data=source==null?null:((ItemMap)net.minecraft.init.Items.filled_map).getMapData(source,world);if(!canScaleCartographyMap(source,data))result=null;}output.setInventorySlotContents(0,result);recomputing=false;}
    private ItemStack[] inputs(){ItemStack[] stacks=new ItemStack[input.getSizeInventory()];for(int i=0;i<stacks.length;i++)stacks[i]=input.getStackInSlot(i);return stacks;}
    @Override public boolean enchantItem(EntityPlayer player,int id){
        if(type==VillageWorkstationType120.LOOM){
            int choices=VillageWorkstationLogic120.choices(type,inputs());
            if(choices<1)return false;
            // 2+index fits the signed byte in the native C11 enchant packet.
            // Keep 0/1 cycling for compatibility with worlds/clients created by
            // the earlier provisional two-button loom screen.
            if(id>=LOOM_SELECTION_BASE){int requested=id-LOOM_SELECTION_BASE;if(requested<0||requested>=choices)return false;selected=requested;}
            else if(id==0||id==1)selected=(selected+(id==0?-1:1)+choices)%choices;else return false;
            recompute();return true;
        }
        if(type==VillageWorkstationType120.STONECUTTER){int choices=VillageWorkstationLogic120.choices(type,inputs());if(choices<1)return false;
            if(id>=STONECUTTER_SELECTION_BASE){int requested=id-STONECUTTER_SELECTION_BASE;if(requested<0||requested>=choices)return false;selected=requested;}
            else if(id==0||id==1)selected=(selected+(id==0?-1:1)+choices)%choices;else return false;recompute();return true;}
        if(type==VillageWorkstationType120.LECTERN&&input instanceof TileEntityVillageLectern120){TileEntityVillageLectern120 lectern=(TileEntityVillageLectern120)input;if(id==LECTERN_TAKE_BOOK){ItemStack book=lectern.decrStackSize(0,1);if(book==null)return false;if(!player.inventory.addItemStackToInventory(book))player.dropPlayerItemWithRandomChoice(book,false);player.closeScreen();return true;}if(id>=3&&id<=127&&id-3<lectern.pageCount()){lectern.setPage(id-3);return true;}if(id!=0&&id!=1)return false;lectern.setPage(lectern.getPage()+(id==0?-1:1));return true;}return false;
    }
    @Override public void detectAndSendChanges(){super.detectAndSendChanges();int value=type==VillageWorkstationType120.LECTERN&&input instanceof TileEntityVillageLectern120?((TileEntityVillageLectern120)input).getPage():selected;for(Object listener:crafters)((net.minecraft.inventory.ICrafting)listener).sendProgressBarUpdate(this,0,value);}
    @Override public void updateProgressBar(int id,int value){if(id==0)selected=value;}
    public ItemStack getInput(int slot){return slot>=0&&slot<input.getSizeInventory()?input.getStackInSlot(slot):null;}
    public int getSelected(){return selected;}
    public int getChoiceCount(){return VillageWorkstationLogic120.choices(type,inputs());}
    public ItemStack getChoiceResult(int index){return type==VillageWorkstationType120.STONECUTTER
            ?VillageWorkstationLogic120.result(type,inputs(),index):null;}
    public int getLecternPageCount(){return input instanceof TileEntityVillageLectern120?((TileEntityVillageLectern120)input).pageCount():1;}
    public ItemStack getLecternBook(){return type==VillageWorkstationType120.LECTERN?input.getStackInSlot(0):null;}
    public static boolean canScaleCartographyMap(ItemStack stack,MapData data){return stack!=null&&data!=null&&data.scale<4
            &&(!stack.hasTagCompound()||!stack.getTagCompound().getBoolean("map_is_locked"));}
    public String getSelectedLoomPattern(){return type==VillageWorkstationType120.LOOM
            ?VillageWorkstationLogic120.selectedLoomPattern(inputs(),selected):null;}
    public String getLoomPattern(int index){return type==VillageWorkstationType120.LOOM
            ?VillageWorkstationLogic120.selectedLoomPattern(inputs(),index):null;}
    public int getLoomBaseColor(){ItemStack[] values=inputs();return values.length>0&&values[0]!=null
            ?values[0].getItemDamage()&15:15;}
    public int getLoomDyeColor(){ItemStack[] values=inputs();return values.length>1&&values[1]!=null
            ?Math.max(0,VillageWorkstationLogic120.dyeColor(values[1])):0;}
    /** Existing layers on the input banner, copied so client previews cannot mutate its NBT. */
    public NBTTagList getLoomPatterns(){
        ItemStack[] values=inputs();
        if(type!=VillageWorkstationType120.LOOM||values.length==0||values[0]==null
                ||!values[0].hasTagCompound()||!values[0].getTagCompound().hasKey("BlockEntityTag",10))
            return new NBTTagList();
        NBTTagCompound block=values[0].getTagCompound().getCompoundTag("BlockEntityTag");
        return block.hasKey("Patterns",9)?(NBTTagList)block.getTagList("Patterns",10).copy():new NBTTagList();
    }
    public VillageWorkstationType120 getType(){return type;}
    @Override public void onContainerClosed(EntityPlayer player){
        if(closed)return;
        closed=true;super.onContainerClosed(player);
        if(type==VillageWorkstationType120.LECTERN)return;
        // A preview is not owned inventory and must never survive closing the menu.
        if(output!=null)output.setInventorySlotContents(0,null);
        if(!world.isRemote)for(int i=0;i<input.getSizeInventory();i++){
            ItemStack stack=input.getStackInSlotOnClosing(i);
            if(stack==null)continue;
            if(type==VillageWorkstationType120.SMITHING&&!player.isDead&&player.isEntityAlive()){
                returnSmithingInput(player,stack);
            }else player.dropPlayerItemWithRandomChoice(stack,false);
        }
    }

    @Override public ItemStack slotClick(int index,int button,int mode,EntityPlayer player){
        if(closed||smithingTransaction)return null;
        if(type==VillageWorkstationType120.SMITHING&&index==type.inputSlots){
            if(mode==0||mode==1||mode==2||mode==4)return takeSmithing(player,button,mode);
            // Native creative cloning is intentional; drag/double-click may not take previews.
            if(mode!=3)return null;
        }
        return super.slotClick(index,button,mode,player);
    }

    /** Reserve the whole destination BEFORE consuming any of the three inputs. */
    private ItemStack takeSmithing(EntityPlayer player,int button,int mode){
        if(closed||smithingTransaction||!canInteractWith(player))return null;
        if((mode==0||mode==1||mode==4)&&(button<0||button>1))return null;
        if(mode==2&&(button<0||button>=9))return null;
        ItemStack result=VillageWorkstationLogic120.result(type,inputs(),selected);
        if(result==null||result.stackSize!=1||!ItemStack.areItemStacksEqual(result,output.getStackInSlot(0))) {
            recompute();return null;
        }
        int destination=-1,displacedDestination=-1;
        ItemStack carried=player.inventory.getItemStack(),displaced=null;
        if(mode==0){
            if(!fits(carried,result,result.getMaxStackSize()))return result.copy();
        }else if(mode==1){
            // Match the native reverse player-inventory transfer order, without partial merges.
            for(int i=inventorySlots.size()-1;i>type.inputSlots;i--){
                Slot slot=(Slot)inventorySlots.get(i);
                if(slot.isItemValid(result)&&fits(slot.getStack(),result,slot.getSlotStackLimit())){destination=i;break;}
            }
            if(destination<0)return null;
        }else if(mode==2){
            displaced=player.inventory.getStackInSlot(button);
            if(displaced!=null){
                displacedDestination=player.inventory.getFirstEmptyStack();
                if(displacedDestination<0)return null;
            }
        }else if(mode==4){
            if(carried!=null)return null;
        }else return null;
        ItemStack[] snapshot=inputs();
        for(int i=0;i<snapshot.length;i++)snapshot[i]=snapshot[i]==null?null:snapshot[i].copy();
        int[] cost=VillageWorkstationLogic120.consumed(type,snapshot);
        for(int i=0;i<cost.length;i++)if(cost[i]>0&&(snapshot[i]==null||snapshot[i].stackSize<cost[i]))return null;

        smithingTransaction=true;recomputing=true;
        try{
            output.setInventorySlotContents(0,null);
            // Toss cancellation / denied EntityJoinWorld must not destroy the ingredients.
            // Publish the server drop while the re-entrant click guard is held, then commit.
            if(mode==4&&!world.isRemote&&!dropSmithingResult(player,result.copy()))return null;
            // Inventory callbacks cannot expose intermediate results while the guard is held.
            for(int i=0;i<cost.length;i++)if(cost[i]>0){
                ItemStack remaining=snapshot[i];remaining.stackSize-=cost[i];
                input.setInventorySlotContents(i,remaining.stackSize==0?null:remaining);
            }
            ItemStack delivered=result.copy();
            if(mode==0){
                if(carried!=null)delivered.stackSize+=carried.stackSize;
                player.inventory.setItemStack(delivered);
            }else if(mode==1){
                Slot slot=(Slot)inventorySlots.get(destination);
                if(slot.getStack()!=null)delivered.stackSize+=slot.getStack().stackSize;
                slot.putStack(delivered);
            }else if(mode==2){
                if(displaced!=null)player.inventory.setInventorySlotContents(displacedDestination,displaced);
                player.inventory.setInventorySlotContents(button,delivered);
            }
            player.inventory.markDirty();
        }finally{
            recomputing=false;smithingTransaction=false;recompute();
        }
        // Vanilla C0E confirmation compares this return value with client prediction.
        return mode==0||mode==1?result.copy():null;
    }
    private static boolean dropSmithingResult(EntityPlayer player,ItemStack stack){
        net.minecraft.entity.item.EntityItem drop=new net.minecraft.entity.item.EntityItem(player.worldObj,
                player.posX,player.posY-.3D+player.getEyeHeight(),player.posZ,stack);
        drop.delayBeforeCanPickup=40;
        double yaw=Math.toRadians(player.rotationYaw),pitch=Math.toRadians(player.rotationPitch);
        drop.motionX=-Math.sin(yaw)*Math.cos(pitch)*.3D;
        drop.motionY=-Math.sin(pitch)*.3D+.1D;
        drop.motionZ=Math.cos(yaw)*Math.cos(pitch)*.3D;
        if(net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new net.minecraftforge.event.entity.item.ItemTossEvent(drop,player)))return false;
        if(!player.worldObj.spawnEntityInWorld(drop))return false;
        player.addStat(net.minecraft.stats.StatList.dropStat,1);return true;
    }
    private static void returnSmithingInput(EntityPlayer player,ItemStack stack){
        // InventoryPlayer.addItemStackToInventory discards overflow in creative.
        // Closing a menu returns owned ingredients, never creative-generated waste.
        for(int pass=0;pass<2&&stack.stackSize>0;pass++)for(int slot=0;slot<player.inventory.mainInventory.length&&stack.stackSize>0;slot++){
            ItemStack current=player.inventory.mainInventory[slot];
            if(pass==0&&(current==null||!current.isItemEqual(stack)||!ItemStack.areItemStackTagsEqual(current,stack)))continue;
            if(pass==1&&current!=null)continue;
            int limit=Math.min(stack.getMaxStackSize(),player.inventory.getInventoryStackLimit());
            int amount=Math.min(stack.stackSize,limit-(current==null?0:current.stackSize));
            if(amount<=0)continue;
            if(current==null){current=stack.copy();current.stackSize=amount;player.inventory.mainInventory[slot]=current;}
            else current.stackSize+=amount;
            stack.stackSize-=amount;
        }
        player.inventory.markDirty();
        if(stack.stackSize>0)player.dropPlayerItemWithRandomChoice(stack,false);
    }
    private static boolean fits(ItemStack current,ItemStack incoming,int limit){
        if(incoming==null||incoming.stackSize<=0)return false;
        int capacity=Math.min(limit,incoming.getMaxStackSize());
        return current==null?incoming.stackSize<=capacity:
                current.getItem()==incoming.getItem()&&current.getItemDamage()==incoming.getItemDamage()
                &&ItemStack.areItemStackTagsEqual(current,incoming)&&current.stackSize+incoming.stackSize<=capacity;
    }
    @Override public boolean func_94530_a(ItemStack stack,Slot slot){
        return !(type==VillageWorkstationType120.SMITHING&&slot.inventory==output)&&super.func_94530_a(stack,slot);
    }
    @Override public ItemStack transferStackInSlot(EntityPlayer player,int index){
        if(closed||smithingTransaction)return null;
        if(type==VillageWorkstationType120.SMITHING&&index==type.inputSlots)return takeSmithing(player,0,1);
        if(index<0||index>=inventorySlots.size())return null;
        Slot slot=(Slot)inventorySlots.get(index);if(slot==null||!slot.getHasStack())return null;
        ItemStack stack=slot.getStack(),copy=stack.copy();
        int workSlots=type==VillageWorkstationType120.LECTERN?0:type.inputSlots+1;
        int playerStart=workSlots,mainEnd=playerStart+27,playerEnd=mainEnd+9;
        boolean outputSlot=type!=VillageWorkstationType120.LECTERN&&index==type.inputSlots;
        if(outputSlot){
            if(type==VillageWorkstationType120.CARTOGRAPHY&&createsMapData()){
                // Finalize the identity before mergeItemStack copies the result into inventory.
                // A new map ID cannot stack with existing maps, so require one empty slot first.
                boolean room=false;for(int i=playerStart;i<playerEnd;i++)if(!((Slot)inventorySlots.get(i)).getHasStack()){room=true;break;}
                if(!room||world.isRemote)return null;
                ItemStack finished=stack.copy();finishResult(player,finished);
                if(!mergeItemStack(finished,playerStart,playerEnd,true))return null;
                consume();return copy;
            }
            if(!mergeItemStack(stack,playerStart,playerEnd,true))return null;
            slot.onSlotChange(stack,copy);
        }else if(index<workSlots){
            if(!mergeItemStack(stack,playerStart,playerEnd,false))return null;
        }else{
            boolean moved=false;
            for(int target=0;target<type.inputSlots&&!moved;target++)
                if(VillageWorkstationLogic120.isValidInput(type,target,stack))
                    moved=mergeItemStack(stack,target,target+1,false);
            if(!moved){
                if(index<mainEnd)moved=mergeItemStack(stack,mainEnd,playerEnd,false);
                else moved=mergeItemStack(stack,playerStart,mainEnd,false);
            }
            if(!moved)return null;
        }
        if(stack.stackSize==0)slot.putStack(null);else slot.onSlotChanged();
        if(stack.stackSize==copy.stackSize)return null;
        slot.onPickupFromSlot(player,stack);
        return copy;
    }
    private void consume(){int[] amounts=VillageWorkstationLogic120.consumed(type,inputs());for(int i=0;i<amounts.length;i++)if(amounts[i]>0)input.decrStackSize(i,amounts[i]);recompute();}
    private boolean createsMapData(){
        ItemStack material=input.getSizeInventory()>1?input.getStackInSlot(1):null;
        return material!=null&&(material.getItem()==net.minecraft.init.Items.paper
                ||material.getItem()==net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.glass_pane));
    }
    static int scaledMapCenter(int center,int scale){
        int size=128*(1<<Math.max(0,Math.min(4,scale)));
        return (int)(Math.floor((center+64D)/size)*size+size/2D-64D);
    }
    private void finishResult(EntityPlayer player,ItemStack stack){
        if(!world.isRemote&&type==VillageWorkstationType120.CARTOGRAPHY&&createsMapData()&&stack.hasTagCompound()&&(stack.getTagCompound().getBoolean("map_is_scaling")||stack.getTagCompound().getBoolean("map_is_locked"))){
            MapData old=((ItemMap)net.minecraft.init.Items.filled_map).getMapData(stack,world);
            if(old!=null){boolean scaling=stack.getTagCompound().getBoolean("map_is_scaling");int id=world.getUniqueDataId("map");stack.setItemDamage(id);MapData copy=new MapData("map_"+id);copy.scale=(byte)(scaling?Math.min(4,old.scale+1):old.scale);copy.xCenter=scaling?scaledMapCenter(old.xCenter,copy.scale):old.xCenter;copy.zCenter=scaling?scaledMapCenter(old.zCenter,copy.scale):old.zCenter;copy.dimension=old.dimension;if(!scaling&&old.colors!=null)System.arraycopy(old.colors,0,copy.colors,0,Math.min(old.colors.length,copy.colors.length));copy.markDirty();world.setItemData("map_"+id,copy);stack.getTagCompound().removeTag("map_is_scaling");}
        }
        if(type==VillageWorkstationType120.GRINDSTONE&&!world.isRemote){int value=VillageWorkstationLogic120.grindstoneExperience(inputs());int half=(value+1)/2;int xp=value>0?half+world.rand.nextInt(half):0;while(xp>0){int split=EntityXPOrb.getXPSplit(xp);xp-=split;world.spawnEntityInWorld(new EntityXPOrb(world,player.posX,player.posY+.5D,player.posZ,split));}}
    }
    private static net.minecraft.block.Block blockFor(VillageWorkstationType120 t){switch(t){case SMITHING:return VillagePillageContent.smithingTable;case GRINDSTONE:return VillagePillageContent.grindstone;case STONECUTTER:return VillagePillageContent.stonecutter;case LOOM:return VillagePillageContent.loom;case CARTOGRAPHY:return VillagePillageContent.cartographyTable;default:return VillagePillageContent.lectern;}}
    private final class WorkInventory extends InventoryBasic {WorkInventory(int size){super("workstation.input",false,size);}@Override public void markDirty(){super.markDirty();recompute();}}
    private final class ValidatedSlot extends Slot {ValidatedSlot(IInventory inv,int slot,int px,int py){super(inv,slot,px,py);}@Override public boolean isItemValid(ItemStack stack){if(type==VillageWorkstationType120.LECTERN)return input.isItemValidForSlot(slotNumber,stack);return VillageWorkstationLogic120.isValidInput(type,slotNumber,stack);}@Override public int getSlotStackLimit(){return type==VillageWorkstationType120.GRINDSTONE||type==VillageWorkstationType120.LOOM&&slotNumber!=1?1:super.getSlotStackLimit();}@Override public void onSlotChanged(){super.onSlotChanged();recompute();}}
    private final class OutputSlot extends Slot {
        @Override public boolean canTakeStack(EntityPlayer player){return !closed&&type!=VillageWorkstationType120.SMITHING&&super.canTakeStack(player);}
        OutputSlot(IInventory inv,int slot,int px,int py){super(inv,slot,px,py);}@Override public boolean isItemValid(ItemStack stack){return false;}@Override public void onPickupFromSlot(EntityPlayer player,ItemStack stack){finishResult(player,stack);consume();super.onPickupFromSlot(player,stack);}}
}
