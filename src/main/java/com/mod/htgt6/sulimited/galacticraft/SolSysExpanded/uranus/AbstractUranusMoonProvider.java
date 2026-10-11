package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.uranus;

import com.mod.htgt6.sulimited.client.CloudRendererNone;
import com.mod.htgt6.sulimited.client.SkyProviderUranusMoon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import galaxyspace.core.world.gen.WorldProviderAdvancedSpace;
import micdoodle8.mods.galacticraft.api.vector.Vector3;

abstract class AbstractUranusMoonProvider extends WorldProviderAdvancedSpace {
    @Override @SideOnly(Side.CLIENT) public void registerWorldChunkManager(){super.registerWorldChunkManager();if(worldObj!=null&&worldObj.isRemote){setSkyRenderer(new SkyProviderUranusMoon(uranusHorizontalAngle(),uranusVerticalAngle(),uranusSize(),ringTilt()));setCloudRenderer(CloudRendererNone.INSTANCE);setWeatherRenderer(CloudRendererNone.INSTANCE);}}
    protected abstract float uranusHorizontalAngle(); protected abstract float uranusVerticalAngle(); protected abstract float uranusSize(); protected abstract float ringTilt();
    public double getMeteorFrequency(){return 7;} public float getSoundVolReductionAmount(){return 20;} public double getFuelUsageMultiplier(){return 1.05;} public float getWindLevel(){return 0;} public boolean canRainOrSnow(){return false;} public boolean hasSunset(){return false;} public boolean hasBreathableAtmosphere(){return false;} public double getSolarEnergyMultiplier(){return .0037;} public float getSolarSize(){return .04F;} public Vector3 getFogColor(){return new Vector3(.006,.008,.011);} public Vector3 getSkyColor(){return new Vector3(0,0,0);}
}
