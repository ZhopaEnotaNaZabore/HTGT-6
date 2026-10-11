package com.mod.htgt6.minecraft1202.Module.Events.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemHoe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public final class BountifulEventHandler {


    @SubscribeEvent public void onInteract(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || event.world.isRemote) return;
        ItemStack tool=event.entityPlayer.getHeldItem();
        if(event.face!=0&&event.world.getBlock(event.x,event.y,event.z)==BountifulContent.coarseDirt
                &&tool!=null&&tool.getItem() instanceof ItemHoe
                &&event.world.isAirBlock(event.x,event.y+1,event.z)){
            event.world.playSoundEffect(event.x+.5D,event.y+.5D,event.z+.5D,
                    "step.gravel",1.0F,0.8F);
            event.world.setBlock(event.x,event.y,event.z,Blocks.dirt,0,3);
            tool.damageItem(1,event.entityPlayer);event.setCanceled(true);return;
        }
        if (event.world.getBlock(event.x, event.y, event.z) == Blocks.daylight_detector) {
            if(!event.entityPlayer.capabilities.allowEdit)return;
            event.world.setBlock(event.x, event.y, event.z, BountifulContent.invertedDaylightDetector, 0, 3);
            ((net.minecraft.block.BlockDaylightDetector)BountifulContent.invertedDaylightDetector)
                    .func_149957_e(event.world,event.x,event.y,event.z);
            event.setCanceled(true);
            return;
        }
        ItemStack held = event.entityPlayer.getHeldItem();
        if (event.world.getBlock(event.x, event.y, event.z) == Blocks.cauldron
                && event.world.getBlockMetadata(event.x, event.y, event.z) > 0
                && held != null && held.getItem() == BountifulContent.banner && held.hasTagCompound()) {
            net.minecraft.nbt.NBTTagCompound root = held.getTagCompound();
            net.minecraft.nbt.NBTTagCompound block = root.getCompoundTag("BlockEntityTag");
            net.minecraft.nbt.NBTTagList patterns = block.getTagList("Patterns", 10);
            if (patterns.tagCount() > 0) {
                ItemStack cleaned=held.copy();cleaned.stackSize=1;
                NBTTagCompound cleanRoot=(NBTTagCompound)root.copy();
                NBTTagCompound cleanBlock=cleanRoot.getCompoundTag("BlockEntityTag");
                NBTTagList cleanPatterns=cleanBlock.getTagList("Patterns",10);
                cleanPatterns.removeTag(cleanPatterns.tagCount()-1);
                cleanBlock.setTag("Patterns",cleanPatterns);cleanRoot.setTag("BlockEntityTag",cleanBlock);
                cleaned.setTagCompound(cleanRoot);
                if(held.stackSize==1)event.entityPlayer.inventory.setInventorySlotContents(
                        event.entityPlayer.inventory.currentItem,cleaned);
                else {--held.stackSize;if(!event.entityPlayer.inventory.addItemStackToInventory(cleaned))
                    event.entityPlayer.dropPlayerItemWithRandomChoice(cleaned,false);}
                event.entityPlayer.inventory.markDirty();
                event.world.setBlockMetadataWithNotify(event.x, event.y, event.z,
                        event.world.getBlockMetadata(event.x, event.y, event.z) - 1, 3);
                event.setCanceled(true);
            }
        }
    }


    

    /** Minecraft 1.8 sheep mutton: 1..2 plus 0..Looting, including non-player deaths. */
    @SubscribeEvent public void onDrops(LivingDropsEvent event) {
        if (!(event.entityLiving instanceof EntitySheep) || event.entityLiving.worldObj.isRemote) return;
        int looting = Math.max(0, event.lootingLevel);
        int count = 1 + event.entityLiving.getRNG().nextInt(2)
                + event.entityLiving.getRNG().nextInt(1 + looting);
        ItemStack stack = new ItemStack(event.entityLiving.isBurning()
                ? BountifulContent.cookedMutton : BountifulContent.rawMutton, count);
        event.drops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX,
                event.entityLiving.posY, event.entityLiving.posZ, stack));
    }

    


    

    @SubscribeEvent public void onFall(LivingFallEvent event) {
        int x = net.minecraft.util.MathHelper.floor_double(event.entityLiving.posX);
        int y = net.minecraft.util.MathHelper.floor_double(event.entityLiving.boundingBox.minY) - 1;
        int z = net.minecraft.util.MathHelper.floor_double(event.entityLiving.posZ);
        if (!event.entityLiving.isSneaking()
                && event.entityLiving.worldObj.getBlock(x, y, z) == BountifulContent.slimeBlock) {
            event.setCanceled(true);
        }
    }
}
