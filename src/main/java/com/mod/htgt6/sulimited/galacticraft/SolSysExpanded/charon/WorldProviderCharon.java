package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.charon;

import com.mod.htgt6.sulimited.client.CloudRendererNone;
import com.mod.htgt6.sulimited.client.SkyProviderCharon;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysExpanded;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import galaxyspace.core.world.gen.WorldProviderAdvancedSpace;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public final class WorldProviderCharon extends WorldProviderAdvancedSpace {
    @Override
    @SideOnly(Side.CLIENT)
    public void registerWorldChunkManager() {
        super.registerWorldChunkManager();
        if (this.worldObj != null && this.worldObj.isRemote) {
            this.setSkyRenderer(new SkyProviderCharon());
            this.setCloudRenderer(CloudRendererNone.INSTANCE);
            this.setWeatherRenderer(CloudRendererNone.INSTANCE);
        }
    }

    public CelestialBody getCelestialBody() { return SolSysExpanded.charon; }
    public Class<? extends IChunkProvider> getChunkProviderClass() { return ChunkProviderCharon.class; }
    public Class<? extends WorldChunkManager> getWorldChunkManagerClass() { return WorldChunkManagerCharon.class; }
    public String getDimensionName() { return "Charon"; }
    public float getGravity() { return 0.077F; }
    public double getMeteorFrequency() { return 8.0D; }
    public float getSoundVolReductionAmount() { return 18.0F; }
    public double getFuelUsageMultiplier() { return 1.0D; }
    public float getFallDamageModifier() { return 0.12F; }
    public float getThermalLevelModifier() { return -5.5F; }
    public float getWindLevel() { return 0.0F; }
    public boolean canRainOrSnow() { return false; }
    public boolean hasSunset() { return false; }
    public long getDayLength() { return 153288L; }
    public boolean hasBreathableAtmosphere() { return false; }
    public boolean canSpaceshipTierPass(int tier) { return tier >= 8; }
    public double getSolarEnergyMultiplier() { return 0.04D; }
    public float getSolarSize() { return 0.12F; }
    public Vector3 getFogColor() { return new Vector3(0.018D, 0.022D, 0.030D); }
    public Vector3 getSkyColor() { return new Vector3(0.0D, 0.0D, 0.0D); }
}
