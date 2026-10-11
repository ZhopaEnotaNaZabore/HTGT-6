package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.*;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageFoodRecipes114;

public final class EntityMooshroom114 extends EntityMooshroom {
    private UUID lastLightning;
    private ItemStack stew;
    public EntityMooshroom114(World world){super(world);}
    @Override public String getCommandSenderName(){return hasCustomNameTag()?getCustomNameTag():net.minecraft.util.StatCollector.translateToLocal("entity.HTGT6.Mooshroom.name");}
    @Override protected void entityInit(){super.entityInit();dataWatcher.addObject(20,Byte.valueOf((byte)0));}
    public boolean isBrown(){return dataWatcher.getWatchableObjectByte(20)!=0;}
    public void setBrown(boolean brown){dataWatcher.updateObject(20,Byte.valueOf((byte)(brown?1:0)));}
    @Override public void onStruckByLightning(EntityLightningBolt bolt){if(!worldObj.isRemote&&!bolt.getUniqueID().equals(lastLightning)){setBrown(!isBrown());lastLightning=bolt.getUniqueID();playSound("htgt6:entity.mooshroom.convert",2,1);}}
    @Override public boolean interact(EntityPlayer player){
        ItemStack held=player.getHeldItem();
        if(held!=null&&held.stackSize>0&&held.getItem()==Items.bowl&&!isChild()){
            if(!worldObj.isRemote){ItemStack result=stew==null?new ItemStack(Items.mushroom_stew):stew.copy();boolean suspicious=stew!=null;
                if(!player.capabilities.isCreativeMode&&--held.stackSize==0)player.inventory.setInventorySlotContents(player.inventory.currentItem,result);
                else if(!player.inventory.addItemStackToInventory(result))player.dropPlayerItemWithRandomChoice(result,false);
                stew=null;playSound("htgt6:entity.mooshroom."+(suspicious?"suspicious_milk":"milk"),1,1);
            }return true;
        }
        ItemStack effect=isBrown()?VillageFoodRecipes114.stewForFlower(held):null;
        if(effect!=null){if(!worldObj.isRemote&&stew==null){stew=effect;if(!player.capabilities.isCreativeMode)--held.stackSize;playSound("htgt6:entity.mooshroom.eat",2,1);}return true;}
        return super.interact(player);
    }
    @Override public EntityMooshroom114 createChild(EntityAgeable mate){EntityMooshroom114 child=new EntityMooshroom114(worldObj);boolean other=mate instanceof EntityMooshroom114&&((EntityMooshroom114)mate).isBrown();child.setBrown(other==isBrown()?(rand.nextInt(1024)==0?!isBrown():isBrown()):(rand.nextBoolean()?isBrown():other));return child;}
    @Override public ArrayList<ItemStack> onSheared(ItemStack shears,IBlockAccess world,int x,int y,int z,int fortune){
        ArrayList<ItemStack> drops=new ArrayList<ItemStack>();if(worldObj.isRemote||!isEntityAlive()||isChild())return drops;
        EntityCow cow=new EntityCow(worldObj);cow.setLocationAndAngles(posX,posY,posZ,rotationYaw,rotationPitch);cow.setHealth(getHealth());cow.renderYawOffset=renderYawOffset;if(hasCustomNameTag())cow.setCustomNameTag(getCustomNameTag());if(isNoDespawnRequired())cow.func_110163_bv();
        if(!worldObj.spawnEntityInWorld(cow))return drops;
        if(getLeashed()){cow.setLeashedToEntity(getLeashedToEntity(),true);clearLeashed(true,false);}
        setDead();for(int i=0;i<5;i++)drops.add(new ItemStack(isBrown()?Blocks.brown_mushroom:Blocks.red_mushroom));playSound("htgt6:entity.mooshroom.shear",1,1);return drops;
    }
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setString("Type",isBrown()?"brown":"red");if(stew!=null)tag.setTag("HTGT6FlowerStew",stew.writeToNBT(new NBTTagCompound()));}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);setBrown("brown".equals(tag.getString("Type")));stew=tag.hasKey("HTGT6FlowerStew",10)?ItemStack.loadItemStackFromNBT(tag.getCompoundTag("HTGT6FlowerStew")):null;}
}
