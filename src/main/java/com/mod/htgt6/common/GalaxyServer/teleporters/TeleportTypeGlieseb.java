package com.mod.htgt6.common.GalaxyServer.teleporters;

import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.ITeleportType;
import micdoodle8.mods.galacticraft.core.entities.EntityLander;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Random;

public class TeleportTypeGlieseb implements ITeleportType {

    public TeleportTypeGlieseb() {

    }

    @Override
    public boolean useParachute() {
        return false;
    }

    @Override
    public Vector3 getPlayerSpawnLocation(WorldServer world, EntityPlayerMP player) {

        if (player != null) {

            GCPlayerStats stats = GCPlayerStats.get(player);

            double x = stats.coordsTeleportedFromX;
            double z = stats.coordsTeleportedFromZ;

            int limit = ConfigManagerCore.otherPlanetWorldBorders - 2;

            if (limit > 20) {

                if (x > limit) {
                    z *= (double) limit / x;
                    x = limit;
                }
                else if (x < -limit) {
                    z *= (double) (-limit) / x;
                    x = -limit;
                }

                if (z > limit) {
                    x *= (double) limit / z;
                    z = limit;
                }
                else if (z < -limit) {
                    x *= (double) (-limit) / z;
                    z = -limit;
                }
            }

            return new Vector3(
                    x,
                    ConfigManagerCore.disableLander ? 250.0D : 900.0D,
                    z
            );
        }

        return null;
    }

    @Override
    public Vector3 getEntitySpawnLocation(WorldServer world, Entity entity) {

        return new Vector3(
                entity.posX,
                ConfigManagerCore.disableLander ? 250.0D : 900.0D,
                entity.posZ
        );
    }

    @Override
    public Vector3 getParaChestSpawnLocation(
            WorldServer world,
            EntityPlayerMP player,
            Random rand) {

        return null;
    }

    // FIXED
    @Override
    public void onSpaceDimensionChanged(
            World newWorld,
            EntityPlayerMP player,
            boolean ridingAutoRocket) {

        if (!ridingAutoRocket
                && player != null
                && GCPlayerStats.get(player).teleportCooldown <= 0) {

            if (player.capabilities.isFlying) {
                player.capabilities.isFlying = false;
            }

            EntityLander lander = new EntityLander(player);

            if (!newWorld.isRemote) {
                newWorld.spawnEntityInWorld(lander);
            }

            GCPlayerStats.get(player).teleportCooldown = 10;
        }
    }

    @Override
    public void setupAdventureSpawn(EntityPlayerMP player) {

    }
}