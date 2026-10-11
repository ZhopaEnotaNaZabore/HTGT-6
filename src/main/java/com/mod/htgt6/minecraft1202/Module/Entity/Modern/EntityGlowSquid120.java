package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Glow squid with darkness spawning, glow-ink drops and temporary dimming after damage. */
public final class EntityGlowSquid120 extends EntitySquid {
    private int darkTicks;
    public EntityGlowSquid120(World world){super(world);}

    @Override public void onLivingUpdate(){super.onLivingUpdate();if(darkTicks>0)darkTicks--;}
    @Override public boolean attackEntityFrom(DamageSource source,float amount){boolean hurt=super.attackEntityFrom(source,amount);if(hurt)darkTicks=100;return hurt;}
    public int minimumBlockLight(){return MathHelper.clamp_int(15-darkTicks/10,0,15);}
    @Override public int getBrightnessForRender(float partialTicks){
        int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(posY),z=MathHelper.floor_double(posZ);
        return worldObj.getLightBrightnessForSkyBlocks(x,y,z,minimumBlockLight());
    }
    @Override public boolean getCanSpawnHere(){
        int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(posY),z=MathHelper.floor_double(posZ);
        int maximum=FuturaWorldType120.isFutura(worldObj)?FuturaHeight120.modernToPhysicalY(30):30;
        return y<maximum&&worldObj.getBlockLightValue(x,y,z)==0&&super.getCanSpawnHere();
    }
    @Override protected Item getDropItem(){return Backport120Content.glowInkSac;}
    @Override protected void dropFewItems(boolean hit,int looting){entityDropItem(new ItemStack(Backport120Content.glowInkSac,1+rand.nextInt(3+looting)),0.0F);}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("DarkTicksRemaining",darkTicks);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);darkTicks=tag.getInteger("DarkTicksRemaining");}
}
