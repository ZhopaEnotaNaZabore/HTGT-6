package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.FishingHooks;
public final class OpenWaterFishing116 {
    private static final String OPEN="HTGT6OpenFishingWater",DRY="HTGT6FishingDryTicks";
    public static boolean openWater(World w,int x,int y,int z){
        int previous=0;
        for(int dy=-1;dy<=2;dy++){
            int layer=0;
            for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){
                int px=x+dx,py=y+dy,pz=z+dz;if(!w.blockExists(px,py,pz))return false;
                Block block=w.getBlock(px,py,pz);int type;
                if(block.isAir(w,px,py,pz)||block==Blocks.waterlily)type=1;
                else {boolean water=block.getMaterial()==Material.water&&w.getBlockMetadata(px,py,pz)==0||block instanceof com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150&&((com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150)block).isWaterlogged(w,px,py,pz);
                    if(!water||block.getCollisionBoundingBoxFromPool(w,px,py,pz)!=null)return false;type=2;}
                if(layer!=0&&layer!=type)return false;layer=type;
            }
            if(layer==1&&previous==0||layer==2&&previous==1)return false;previous=layer;
        }return true;
    }
    public static void tick(EntityFishHook hook,int bite,int approach){
        if(hook.worldObj.isRemote)return;NBTTagCompound n=hook.getEntityData();int x=MathHelper.floor_double(hook.posX),y=MathHelper.floor_double(hook.posY),z=MathHelper.floor_double(hook.posZ);
        int dry=n.getInteger(DRY);boolean wet=hook.worldObj.getBlock(x,y,z).getMaterial()==Material.water||com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.WaterloggedFluid150.wet(hook.worldObj,x,y,z);dry=wet?Math.max(0,dry-1):Math.min(10,dry+1);n.setInteger(DRY,dry);
        boolean was=!n.hasKey(OPEN)||n.getBoolean(OPEN);n.setBoolean(OPEN,bite<=0&&approach<=0||was&&dry<10&&openWater(hook.worldObj,x,y,z));
    }
    /** Weights are the original fishing loot pool; Lure changes time, never loot quality. */
    public static int category(float chance,double luck,boolean open){int junk=Math.max(0,(int)Math.floor(10-2*luck)),treasure=open?Math.max(0,(int)Math.floor(5+2*luck)):0,fish=Math.max(0,(int)Math.floor(85-luck));double roll=chance*(junk+treasure+fish);return roll<junk?0:roll<junk+treasure?1:2;}
    public static double luck(net.minecraft.entity.player.EntityPlayer player){
        int value=net.minecraft.enchantment.EnchantmentHelper.func_151386_g(player);
        net.minecraft.entity.ai.attributes.IAttributeInstance attribute=player.getEntityAttribute(com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatAttack120.LUCK);if(attribute!=null)return value+attribute.getAttributeValue();
        net.minecraft.potion.PotionEffect good=player.getActivePotionEffect(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.luck),bad=player.getActivePotionEffect(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.unluck);
        return value+(good==null?0:good.getAmplifier()+1)-(bad==null?0:bad.getAmplifier()+1);
    }
    public static ItemStack roll(EntityFishHook hook){
        double luck=luck(hook.field_146042_b);
        boolean open=(!hook.getEntityData().hasKey(OPEN)||hook.getEntityData().getBoolean(OPEN))&&openWater(hook.worldObj,MathHelper.floor_double(hook.posX),MathHelper.floor_double(hook.posY),MathHelper.floor_double(hook.posZ));
        int result=category(hook.worldObj.rand.nextFloat(),luck,open);float forced=result==0?0:result==1?.1F:1;
        hook.field_146042_b.addStat(FishingHooks.getFishableCategory(forced,0,0).stat,1);
        return FishingLoot116.roll(hook,result);
    }
    private OpenWaterFishing116(){}
}
