package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn;

import com.mod.htgt6.sulimited.client.CloudRendererNone;
import com.mod.htgt6.sulimited.client.SkyProviderSaturnMoon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import galaxyspace.core.world.gen.WorldProviderAdvancedSpace;
import micdoodle8.mods.galacticraft.api.vector.Vector3;

abstract class AbstractSaturnMoonProvider extends WorldProviderAdvancedSpace {
    @Override
    @SideOnly(Side.CLIENT)
    public void registerWorldChunkManager() {
        super.registerWorldChunkManager();
        if (this.worldObj != null && this.worldObj.isRemote) {
            this.setSkyRenderer(new SkyProviderSaturnMoon(
                    getSaturnHorizontalAngle(), getSaturnVerticalAngle(),
                    getSaturnSkySize(), getSaturnRingRotation()));
            // A null renderer falls back to vanilla clouds, so vacuum worlds
            // must install an explicit no-op renderer.
            this.setCloudRenderer(CloudRendererNone.INSTANCE);
            this.setWeatherRenderer(CloudRendererNone.INSTANCE);
        }
    }

    protected abstract float getSaturnHorizontalAngle();
    protected abstract float getSaturnVerticalAngle();
    protected abstract float getSaturnSkySize();
    protected abstract float getSaturnRingRotation();

    public float getSoundVolReductionAmount() { return 19.0F; }
    public double getFuelUsageMultiplier() { return 1.0D; }
    public float getWindLevel() { return 0.0F; }
    public boolean canRainOrSnow() { return false; }
    public boolean hasSunset() { return false; }
    public boolean hasBreathableAtmosphere() { return false; }
    public double getMeteorFrequency() { return 8.0D; }
    public double getSolarEnergyMultiplier() { return 0.015D; }
    public float getSolarSize() { return 0.08F; }
    public Vector3 getFogColor() { return new Vector3(0.012D, 0.015D, 0.021D); }
    public Vector3 getSkyColor() { return new Vector3(0.0D, 0.0D, 0.0D); }
}
