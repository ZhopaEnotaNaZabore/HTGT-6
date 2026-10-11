package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.minor;

import com.mod.htgt6.sulimited.client.CloudRendererNone;
import com.mod.htgt6.sulimited.client.SkyProviderOuterSol;
import com.mod.htgt6.sulimited.client.SkyProviderUranusMoon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import galaxyspace.core.world.gen.WorldProviderAdvancedSpace;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

abstract class AbstractMinorWorldProvider extends WorldProviderAdvancedSpace {
    protected abstract MinorWorlds.Definition definition();
    public abstract Class<? extends WorldChunkManager> getWorldChunkManagerClass();

    @Override @SideOnly(Side.CLIENT)
    public void registerWorldChunkManager() {
        super.registerWorldChunkManager();
        if (worldObj != null && worldObj.isRemote) {
            if (definition().body instanceof Moon) {
                // Ariel is the only moon using this provider family.
                setSkyRenderer(new SkyProviderUranusMoon(72.0F, 31.0F, 32.0F, -22.0F));
            } else {
                float apparentSunSize = (float) Math.max(0.28D, definition().solarMultiplier * 10.0D);
                setSkyRenderer(new SkyProviderOuterSol(apparentSunSize));
            }
            setCloudRenderer(CloudRendererNone.INSTANCE);
            setWeatherRenderer(CloudRendererNone.INSTANCE);
        }
    }

    public CelestialBody getCelestialBody() { return definition().body; }
    public Class<? extends IChunkProvider> getChunkProviderClass() { return ChunkProviderMinorWorld.class; }
    public String getDimensionName() { return definition().name; }
    public float getGravity() { return definition().gravity; }
    public double getMeteorFrequency() { return 7.0D; }
    public float getSoundVolReductionAmount() { return 18.0F; }
    public double getFuelUsageMultiplier() { return 1.0D; }
    public float getFallDamageModifier() { return definition().gravity * 1.25F; }
    public float getThermalLevelModifier() { return definition().thermal; }
    public float getWindLevel() { return 0.0F; }
    public boolean canRainOrSnow() { return false; }
    public boolean hasSunset() { return false; }
    public long getDayLength() { return definition().dayLength; }
    public boolean hasBreathableAtmosphere() { return false; }
    public boolean canSpaceshipTierPass(int tier) { return tier >= definition().tier; }
    public double getSolarEnergyMultiplier() { return definition().solarMultiplier; }
    public float getSolarSize() { return (float) Math.max(0.025D, definition().solarMultiplier * 1.8D); }
    public Vector3 getFogColor() { return new Vector3(0.012D, 0.014D, 0.018D); }
    public Vector3 getSkyColor() { return new Vector3(0.0D, 0.0D, 0.0D); }
}
