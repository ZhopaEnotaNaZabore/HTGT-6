package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.proteus;

import com.mod.htgt6.sulimited.client.CloudRendererNone;
import com.mod.htgt6.sulimited.client.SkyProviderProteus;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysExpanded;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import galaxyspace.core.world.gen.WorldProviderAdvancedSpace;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public final class WorldProviderProteus extends WorldProviderAdvancedSpace {
    @Override @SideOnly(Side.CLIENT) public void registerWorldChunkManager() {
        super.registerWorldChunkManager();
        if (worldObj != null && worldObj.isRemote) {
            setSkyRenderer(new SkyProviderProteus());
            setCloudRenderer(CloudRendererNone.INSTANCE);
            setWeatherRenderer(CloudRendererNone.INSTANCE);
        }
    }
    public CelestialBody getCelestialBody(){return SolSysExpanded.proteus;}
    public Class<? extends IChunkProvider> getChunkProviderClass(){return ChunkProviderProteus.class;}
    public Class<? extends WorldChunkManager> getWorldChunkManagerClass(){return WorldChunkManagerProteus.class;}
    public String getDimensionName(){return "Proteus";}
    public float getGravity(){return 0.079F;}
    public double getMeteorFrequency(){return 15.0D;}
    public float getSoundVolReductionAmount(){return 20.0F;}
    public double getFuelUsageMultiplier(){return 1.15D;}
    public float getFallDamageModifier(){return 0.05F;}
    public float getThermalLevelModifier(){return -5.55F;}
    public float getWindLevel(){return 0.0F;}
    public boolean canRainOrSnow(){return false;}
    public boolean hasSunset(){return false;}
    public long getDayLength(){return 26928L;}
    public boolean hasBreathableAtmosphere(){return false;}
    public boolean canSpaceshipTierPass(int tier){return tier>=7;}
    public double getSolarEnergyMultiplier(){return 0.03D;}
    public float getSolarSize(){return 0.10F;}
    public Vector3 getFogColor(){return new Vector3(0.008D,0.010D,0.016D);}
    public Vector3 getSkyColor(){return new Vector3(0,0,0);}
}
