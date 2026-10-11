package com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b;

import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b.ChunkProviderGlieseB;
import com.mod.htgt6.sulimited.galacticraft.Glise792.sysGliese792;
import com.mod.htgt6.sulimited.client.SkyProviderGliese;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b.WorldChunkManagerGlieseb;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import galaxyspace.core.world.gen.WorldProviderAdvancedSpace;
import galaxyspace.api.dimension.IPlanetFog;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.api.world.ISolarLevel;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import com.mod.htgt6.sulimited.client.CloudRendererNone;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldProviderGliese extends WorldProviderAdvancedSpace implements IGalacticraftWorldProvider, ISolarLevel, IPlanetFog {
    public WorldProviderGliese() {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerWorldChunkManager() {
        super.registerWorldChunkManager();
        if (this.worldObj != null && this.worldObj.isRemote) {
            this.setSkyRenderer(new SkyProviderGliese(false));
            this.setCloudRenderer(CloudRendererNone.INSTANCE);
        }
    }

    public CelestialBody getCelestialBody() {
        return sysGliese792.planetGlieseB;
    }

    public Class<? extends IChunkProvider> getChunkProviderClass() {
        return ChunkProviderGlieseB.class;
    }

    public Class<? extends WorldChunkManager> getWorldChunkManagerClass() {
        return WorldChunkManagerGlieseb.class;
    }

    public float getSoundVolReductionAmount() {
        return 10.0F;
    }

    public String getDimensionName() {
        return "Gliese 792 b";
    }

    public double getFuelUsageMultiplier() {
        return 0.9;
    }

    public float getFallDamageModifier() {
        return 1.0F;
    }

    public float getWindLevel() {
        return 0.0F;
    }

    public float getThermalLevelModifier() {
        // GalaxySpace's celestial-card renderer reads the provider modifier
        // and converts it using 40 °C per level.  Keep it in sync with the
        // 620 °C BodiesData entry registered for Gliese 792 b.
        return 15.5F;
    }

    public boolean canRainOrSnow() {
        return false;
    }

    public boolean hasSunset() {
        return false;
    }

    public long getDayLength() {
        return 24660L;
    }

    public float getGravity() {
        return 0.058F;
    }

    public double getMeteorFrequency() {
        return 10.0;
    }

    public boolean hasBreathableAtmosphere() {
        return false;
    }

    public Vector3 getFogColor() {
        // Ground-level volcanic haze: its glow slowly intensifies and recedes
        // with the day cycle. Ash haze is distinct from water clouds.
        double pulse = this.worldObj == null ? 0.5D : 0.5D + 0.5D * Math.sin(this.worldObj.getWorldTime() * 0.00035D);
        return new Vector3(0.28D + pulse * 0.07D, 0.22D + pulse * 0.05D, 0.19D + pulse * 0.03D);
    }

    public Vector3 getSkyColor() {
        return new Vector3(0.15D, 0.09D, 0.06D);
    }

    @Override
    public float getFogDensity(int x, int y, int z) {
        double pulse = this.worldObj == null ? 0.5D : 0.5D + 0.5D * Math.sin(this.worldObj.getWorldTime() * 0.00035D);
        float groundDensity = 0.009F + 0.018F * (1.0F - Math.max(0.0F, Math.min(1.0F, (y - 60) / 140.0F)));
        return groundDensity + (float) pulse * 0.006F;
    }

    @Override
    public int getFogColor(int x, int y, int z) {
        double pulse = this.worldObj == null ? 0.5D : 0.5D + 0.5D * Math.sin(this.worldObj.getWorldTime() * 0.00035D);
        int red = 72 + (int) (pulse * 18.0D);
        int green = 57 + (int) (pulse * 12.0D);
        int blue = 48 + (int) (pulse * 8.0D);
        return ((red << 16) | (green << 8) | blue) - 0x1000000;
    }

    public boolean canSpaceshipTierPass(int tier) {
        return tier >= 6;
    }

    public double getSolarEnergyMultiplier() {
        return 1.6;
    }

    @Override
    public float getSolarSize() {
        return 1.45F;
    }
}
