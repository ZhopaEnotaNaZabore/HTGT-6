package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.neptune;

import galaxyspace.core.prefab.entity.EntityEntryPod;
import java.util.Random;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.ITeleportType;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public final class TeleportTypeNeptune implements ITeleportType {
    public boolean useParachute(){return false;}
    public Vector3 getPlayerSpawnLocation(WorldServer world,EntityPlayerMP player){
        if(player==null)return null; GCPlayerStats stats=GCPlayerStats.get(player);
        Vector3 point=clamp(stats.coordsTeleportedFromX,stats.coordsTeleportedFromZ);
        point=NeptuneLandingBase.ensureGenerated(world,MathHelper.floor_double(point.x),MathHelper.floor_double(point.z));
        return new Vector3(point.x,ConfigManagerCore.disableLander?point.y:900.0D,point.z);
    }
    public Vector3 getEntitySpawnLocation(WorldServer world,Entity entity){
        Vector3 point=clamp(entity.posX,entity.posZ);
        point=NeptuneLandingBase.ensureGenerated(world,MathHelper.floor_double(point.x),MathHelper.floor_double(point.z));
        return new Vector3(point.x,ConfigManagerCore.disableLander?point.y:900.0D,point.z);
    }
    public Vector3 getParaChestSpawnLocation(WorldServer world,EntityPlayerMP player,Random rand){return null;}
    public void onSpaceDimensionChanged(World world,EntityPlayerMP player,boolean ridingAutoRocket){
        if(!ConfigManagerCore.disableLander&&!ridingAutoRocket&&player!=null&&GCPlayerStats.get(player).teleportCooldown<=0){
            if(player.capabilities.isFlying)player.capabilities.isFlying=false;
            if(!world.isRemote)world.spawnEntityInWorld(new EntityEntryPod(player));
            GCPlayerStats.get(player).teleportCooldown=10;
        }
    }
    public void setupAdventureSpawn(EntityPlayerMP player){ }
    private Vector3 clamp(double x,double z){
        int limit=ConfigManagerCore.otherPlanetWorldBorders-2;
        if(limit>20){if(x>limit){z*=limit/x;x=limit;}else if(x< -limit){z*=(-limit)/x;x=-limit;}
            if(z>limit){x*=limit/z;z=limit;}else if(z< -limit){x*=(-limit)/z;z=-limit;}}
        return new Vector3(x,0,z);
    }
}
