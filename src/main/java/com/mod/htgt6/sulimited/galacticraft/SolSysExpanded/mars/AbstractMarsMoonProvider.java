package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.mars;

import com.mod.htgt6.sulimited.client.CloudRendererNone;
import com.mod.htgt6.sulimited.client.SkyProviderMarsMoon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import galaxyspace.core.world.gen.WorldProviderAdvancedSpace;
import micdoodle8.mods.galacticraft.api.vector.Vector3;

abstract class AbstractMarsMoonProvider extends WorldProviderAdvancedSpace {
    @Override @SideOnly(Side.CLIENT)
    public void registerWorldChunkManager(){super.registerWorldChunkManager();if(worldObj!=null&&worldObj.isRemote){setSkyRenderer(new SkyProviderMarsMoon(marsHorizontalAngle(),marsVerticalAngle(),marsSize()));setCloudRenderer(CloudRendererNone.INSTANCE);setWeatherRenderer(CloudRendererNone.INSTANCE);}}
    protected abstract float marsHorizontalAngle(); protected abstract float marsVerticalAngle(); protected abstract float marsSize();
    public double getMeteorFrequency(){return 9.0D;} public float getSoundVolReductionAmount(){return 20.0F;} public double getFuelUsageMultiplier(){return 0.8D;} public float getWindLevel(){return 0;} public boolean canRainOrSnow(){return false;} public boolean hasSunset(){return false;} public boolean hasBreathableAtmosphere(){return false;} public double getSolarEnergyMultiplier(){return 0.82D;} public float getSolarSize(){return 0.91F;} public Vector3 getFogColor(){return new Vector3(0.012D,0.009D,0.008D);} public Vector3 getSkyColor(){return new Vector3(0,0,0);}
}
