package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;

import java.util.Map;
import java.lang.ref.WeakReference;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSword;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.InventoryPlayer112;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandSystem;
import java.util.WeakHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.LodestoneContent116;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120;

/** Server-authoritative native C08 interaction. All saved state belongs to the ItemStack. */
public final class LodestoneBinding116 {
    public static final String POS="LodestonePos", DIMENSION="LodestoneDimension", TRACKED="LodestoneTracked";
    // Transient packet coalescing, never serialized into a reusable item.
    private static final Map<EntityPlayer,Stamp> recent=new WeakHashMap<EntityPlayer,Stamp>();
    private static final class Stamp {
        final WeakReference<World> world;final long tick;final int slot,x,y,z;
        Stamp(EntityPlayer p,int slot,int x,int y,int z){world=new WeakReference<World>(p.worldObj);tick=p.worldObj.getTotalWorldTime();this.slot=slot;this.x=x;this.y=y;this.z=z;}
    }
    public static boolean accepts(ItemStack stack){return stack!=null&&stack.stackSize>0&&(stack.getItem()==Items.compass||stack.getItem()==LodestoneContent116.compass);}
    public static boolean hasTarget(ItemStack stack){
        if(stack==null||!stack.hasTagCompound())return false;
        NBTTagCompound tag=stack.getTagCompound();
        if(!tag.hasKey(POS,10)||!tag.hasKey(DIMENSION,3))return false;
        NBTTagCompound pos=tag.getCompoundTag(POS);
        return pos.hasKey("X",3)&&pos.hasKey("Y",3)&&pos.hasKey("Z",3);
    }
    public static boolean pointsTo(ItemStack stack,int dimension,int x,int y,int z){
        if(!hasTarget(stack))return false;
        NBTTagCompound tag=stack.getTagCompound(),pos=tag.getCompoundTag(POS);
        return tag.getInteger(DIMENSION)==dimension&&pos.getInteger("X")==x&&pos.getInteger("Y")==y&&pos.getInteger("Z")==z;
    }
    public static ItemStack boundCopy(ItemStack source,int dimension,int x,int y,int z){
        ItemStack result=new ItemStack(LodestoneContent116.compass,1);
        NBTTagCompound tag=source.hasTagCompound()?(NBTTagCompound)source.getTagCompound().copy():new NBTTagCompound();
        NBTTagCompound pos=new NBTTagCompound();pos.setInteger("X",x);pos.setInteger("Y",y);pos.setInteger("Z",z);
        tag.setTag(POS,pos);tag.setInteger(DIMENSION,dimension);tag.setBoolean(TRACKED,true);
        result.setTagCompound(tag);return result;
    }
    /** Returns true only when item NBT changed. Never asks a provider to load a chunk. */
    public static boolean validate(ItemStack stack,World world){
        if(world==null||world.isRemote||stack==null||stack.getItem()!=LodestoneContent116.compass||!hasTarget(stack))return false;
        NBTTagCompound tag=stack.getTagCompound();
        if(tag.hasKey(TRACKED,1)&&!tag.getBoolean(TRACKED))return false;
        if(tag.getInteger(DIMENSION)!=world.provider.dimensionId)return false;
        NBTTagCompound pos=tag.getCompoundTag(POS);int x=pos.getInteger("X"),y=pos.getInteger("Y"),z=pos.getInteger("Z");
        boolean invalid=x < -30000000||x>=30000000||z < -30000000||z>=30000000||y<0||y>=world.getHeight();
        if(!invalid){
            if(!world.blockExists(x,y,z))return false; // unknown, not destroyed
            invalid=world.getBlock(x,y,z)!=LodestoneContent116.lodestone;
        }
        if(!invalid)return false;
        // Keep the dimension marker. A broken lodestone compass must keep spinning,
        // not silently change into a spawn/recovery compass, nor revive after replacement.
        tag.removeTag(POS);return true;
    }
    /** True means this interaction is handled, including a safe rejected/full-inventory request. */
    public static boolean bind(EntityPlayer player,World world,int x,int y,int z,int side){
        if(player==null||world==null||player.worldObj!=world||side<0||side>5)return false;
        int slot=player.inventory.currentItem;
        ItemStack main=player.inventory.getCurrentItem();
        // The backport already owns the off-hand slot and synchronization contract.
        // Do not steal a consuming main-hand action (placing, eating, drawing a bow).
        if(!accepts(main)&&player.inventory instanceof InventoryPlayer112
                &&(main==null||(!(main.getItem() instanceof ItemBlock)
                &&(main.getMaxItemUseDuration()==0||main.getItem() instanceof ItemSword)))){
            if(accepts(OffhandHooks.getOffhand(player)))slot=OffhandHooks.OFFHAND_SLOT;
        }
        return bindSlot(player,world,x,y,z,side,slot);
    }
    public static boolean bindSlot(EntityPlayer player,World world,int x,int y,int z,int side,int slot){
        if(player==null||world==null||player.worldObj!=world||side<0||side>5)return false;
        if(slot<0||(slot>=player.inventory.mainInventory.length
                &&(slot!=OffhandHooks.OFFHAND_SLOT||!(player.inventory instanceof InventoryPlayer112))))return false;
        ItemStack source=slot==OffhandHooks.OFFHAND_SLOT?OffhandHooks.getOffhand(player):player.inventory.mainInventory[slot];
        if(!accepts(source)||!world.blockExists(x,y,z)||world.getBlock(x,y,z)!=LodestoneContent116.lodestone)return false;
        if(world.isRemote)return true; // prediction does not mutate the inventory
        if(player.isDead||!player.isEntityAlive()||!world.canMineBlock(player,x,y,z)||!player.canPlayerEdit(x,y,z,side,source))return true;
        double reach=player instanceof EntityPlayerMP?((EntityPlayerMP)player).theItemInWorldManager.getBlockReachDistance()+1D:6D;
        if(player.getDistanceSq(x+.5D,y+.5D,z+.5D)>reach*reach)return true;
        Stamp old=recent.get(player);long now=world.getTotalWorldTime();
        if(old!=null&&old.world.get()==world&&old.slot==slot&&old.x==x&&old.y==y&&old.z==z&&now>=old.tick&&now-old.tick<4){sync(player);return true;}
        if(source.getItem()==LodestoneContent116.compass&&pointsTo(source,world.provider.dimensionId,x,y,z)){sync(player);return true;}
        ItemStack result=boundCopy(source,world.provider.dimensionId,x,y,z);
        boolean creative=player.capabilities.isCreativeMode;
        if(!creative&&source.stackSize==1){
            // Replace exactly the source slot, even in a completely full inventory.
            if(slot==OffhandHooks.OFFHAND_SLOT)OffhandHooks.setOffhand(player,result);
            else player.inventory.setInventorySlotContents(slot,result);
        }else{
            int destination=-1;
            for(int i=0;i<player.inventory.mainInventory.length;i++){
                if(i==slot)continue;
                ItemStack current=player.inventory.mainInventory[i];
                if(current==null||(current.isItemEqual(result)&&ItemStack.areItemStackTagsEqual(current,result)&&current.stackSize<Math.min(current.getMaxStackSize(),player.inventory.getInventoryStackLimit()))){destination=i;break;}
            }
            if(destination>=0){
                ItemStack current=player.inventory.mainInventory[destination];
                if(current==null)player.inventory.mainInventory[destination]=result;else current.stackSize++;
            }else{
                EntityItem drop=new EntityItem(world,player.posX,player.posY+.5D,player.posZ,result);
                drop.delayBeforeCanPickup=10;
                // A spawn denied by a protection/event handler must not consume the source.
                if(!world.spawnEntityInWorld(drop)){sync(player);return true;}
            }
            if(!creative)source.stackSize--;
        }
        recent.put(player,new Stamp(player,slot,x,y,z));
        sync(player);
        world.playSoundEffect(x+.5D,y+.5D,z+.5D,"htgt6:item.lodestone_compass.lock",1F,1F);
        BackportAchievements120.complete(player,"country_lode_take_me_home");
        return true;
    }
    private static void sync(EntityPlayer player){
        player.inventory.markDirty();
        player.inventoryContainer.detectAndSendChanges();
        if(player.openContainer!=null&&player.openContainer!=player.inventoryContainer)player.openContainer.detectAndSendChanges();
        if(player instanceof EntityPlayerMP){
            EntityPlayerMP server=(EntityPlayerMP)player;
            server.sendContainerAndContentsToPlayer(player.inventoryContainer,player.inventoryContainer.getInventory());
            if(player.inventory instanceof InventoryPlayer112)OffhandSystem.syncInventory(server);
        }
    }
    private LodestoneBinding116(){}
}
