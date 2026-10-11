package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.neptune;

import com.mod.htgt6.sulimited.client.CloudRendererNeptune;
import com.mod.htgt6.sulimited.client.CloudRendererNone;
import com.mod.htgt6.sulimited.client.SkyProviderNeptune;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysExpanded;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import galaxyspace.core.world.gen.WorldProviderAdvancedSpace;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public final class WorldProviderNeptune extends WorldProviderAdvancedSpace {
    @Override @SideOnly(Side.CLIENT) public void registerWorldChunkManager(){
        super.registerWorldChunkManager();
        if(worldObj!=null&&worldObj.isRemote){
            setSkyRenderer(new SkyProviderNeptune());
            setCloudRenderer(CloudRendererNeptune.INSTANCE);
            setWeatherRenderer(CloudRendererNone.INSTANCE);
        }
    }
    public CelestialBody getCelestialBody(){return SolSysExpanded.neptune;}
    public Class<? extends IChunkProvider> getChunkProviderClass(){return ChunkProviderNeptune.class;}
    public Class<? extends WorldChunkManager> getWorldChunkManagerClass(){return WorldChunkManagerNeptune.class;}
    public String getDimensionName(){return "Neptune";}
    public float getGravity(){return -0.012F;}
    public double getMeteorFrequency(){return 0.0D;}
    public float getSoundVolReductionAmount(){return 1.2F;}
    public double getFuelUsageMultiplier(){return 1.8D;}
    public float getFallDamageModifier(){return 0.65F;}
    public float getThermalLevelModifier(){return -5.35F;}
    public float getWindLevel(){return 12.0F;}
    /** Native GalaxySpace pressure status: debuffs and pressure damage without pressurized armor. */
    @Override public int AtmosphericPressure(){return 100;}
    // Allow WorldProviderSpace to update and synchronize weather strengths.
    // Precipitation and vanilla lightning are disabled separately below.
    public boolean canRainOrSnow(){return true;}
    @Override public boolean canDoRainSnowIce(net.minecraft.world.chunk.Chunk chunk){return false;}
    @Override public boolean canDoLightning(net.minecraft.world.chunk.Chunk chunk){return false;}
    public boolean hasSunset(){return false;}
    public long getDayLength(){return 16110L;}
    public boolean hasBreathableAtmosphere(){return false;}
    public boolean canSpaceshipTierPass(int tier){return tier>=8;}
    public double getSolarEnergyMultiplier(){return 0.03D;}
    public float getSolarSize(){return 0.10F;}
    public Vector3 getFogColor(){return new Vector3(0.025D,0.11D,0.29D);}
    public Vector3 getSkyColor(){return new Vector3(0.035D,0.19D,0.48D);}
}
