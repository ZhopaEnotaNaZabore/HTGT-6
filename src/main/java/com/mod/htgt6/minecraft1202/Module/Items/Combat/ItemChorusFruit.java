package com.mod.htgt6.minecraft1202.Module.Items.Combat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import java.util.Map;
import java.util.WeakHashMap;

public final class ItemChorusFruit extends ItemFood {
    private final Map<EntityPlayer,Integer> cooldowns = java.util.Collections.synchronizedMap(new WeakHashMap<EntityPlayer,Integer>());
    public ItemChorusFruit() { super(4, 0.3F, false); setAlwaysEdible(); setUnlocalizedName("chorus_fruit"); setTextureName("htgt6:combat_update/chorus_fruit"); }

    @Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        Integer until=cooldowns.get(player);
        if(until!=null && player.ticksExisted<until)return stack;
        return super.onItemRightClick(stack,world,player);
    }

    @Override protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
        // Weak player keys avoid retaining disconnected players; ticksExisted follows this player instance.
        cooldowns.put(player,player.ticksExisted+20);
        if (world.isRemote) return;
        double ox = player.posX, oy = player.posY, oz = player.posZ;
        for (int attempt = 0; attempt < 16; ++attempt) {
            double x = ox + (player.getRNG().nextDouble() - 0.5D) * 16.0D;
            double y = Math.max(0.0D, Math.min(world.getHeight() - 1, oy + player.getRNG().nextInt(16) - 8));
            double z = oz + (player.getRNG().nextDouble() - 0.5D) * 16.0D;
            if(player.ridingEntity!=null)player.mountEntity(null);
            if (tryTeleport(player,x,y,z)) {
                world.playSoundEffect(ox, oy, oz, "htgt6:item.chorus_fruit.teleport", 1.0F, 1.0F);
                world.playSoundEffect(player.posX,player.posY,player.posZ,"htgt6:item.chorus_fruit.teleport",1.0F,1.0F);
                return;
            }
        }
    }

    /** Legacy equivalent of LivingEntity.randomTeleport: loaded ground, clearance, no liquid, server sync. */
    public static boolean tryTeleport(EntityLivingBase entity,double x,double y,double z) {
        World world=entity.worldObj;
        if(world.isRemote)return false;
        int bx=MathHelper.floor_double(x),bz=MathHelper.floor_double(z);
        if(!world.blockExists(bx,MathHelper.floor_double(y),bz))return false;
        double ox=entity.posX,oy=entity.posY,oz=entity.posZ;
        boolean success=false;
        try {
            entity.setPosition(x,y,z);
            int feet=MathHelper.floor_double(entity.boundingBox.minY);
            while(feet>0&&!world.getBlock(bx,feet-1,bz).getMaterial().blocksMovement()){
                --feet;--y;
            }
            if(feet<=0)return false;
            entity.setPosition(x,y,z);
            if(!world.checkChunksExist(MathHelper.floor_double(entity.boundingBox.minX),feet,
                    MathHelper.floor_double(entity.boundingBox.minZ),MathHelper.floor_double(entity.boundingBox.maxX),
                    MathHelper.floor_double(entity.boundingBox.maxY),MathHelper.floor_double(entity.boundingBox.maxZ)))return false;
            if(!world.getCollidingBoundingBoxes(entity,entity.boundingBox).isEmpty()||world.isAnyLiquid(entity.boundingBox))return false;
            entity.setPositionAndUpdate(x,y,z);
            entity.fallDistance=0;
            success=true;
            return true;
        } finally {
            if(!success)entity.setPosition(ox,oy,oz);
        }
    }
}
