package com.mod.htgt6.minecraft1202.Module.Events.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.VillagePoiData120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagePoiType120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerSleep120;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.BlockBed;
import net.minecraft.entity.passive.EntityVillager;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWanderingTrader120;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;

import java.util.List;

/** Claims nearby job sites and preserves the exact modern profession across saves. */
public final class VillagePoiHandler120 {
    @SubscribeEvent public void migrateBarrels(ChunkEvent.Load event){
        net.minecraft.world.chunk.Chunk chunk=event.getChunk();
        for(net.minecraft.world.chunk.storage.ExtendedBlockStorage section:chunk.getBlockStorageArray()){
            if(section==null||section.isEmpty())continue;
            for(int x=0;x<16;x++)for(int y=0;y<16;y++)for(int z=0;z<16;z++){
                if(!(section.getBlockByExtId(x,y,z) instanceof com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockColoredBed120))continue;
                int wx=(chunk.xPosition<<4)+x,wy=section.getYLocation()+y,wz=(chunk.zPosition<<4)+z;
                if(event.world.getTileEntity(wx,wy,wz)==null)event.world.setTileEntity(wx,wy,wz,new com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityColoredBed120());
            }
        }
        for(Object raw:new java.util.ArrayList(event.getChunk().chunkTileEntityMap.values())){
            if(raw==null||raw.getClass()!=net.minecraft.tileentity.TileEntityChest.class)continue;
            net.minecraft.tileentity.TileEntity old=(net.minecraft.tileentity.TileEntity)raw;
            if(event.world.getBlock(old.xCoord,old.yCoord,old.zCoord)!=com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent.barrel)continue;
            net.minecraft.nbt.NBTTagCompound tag=new net.minecraft.nbt.NBTTagCompound();old.writeToNBT(tag);
            com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageBarrel120 next=new com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageBarrel120();
            next.readFromNBT(tag);event.world.setTileEntity(old.xCoord,old.yCoord,old.zCoord,next);
        }
    }

    @SubscribeEvent
    public void worldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (event.phase != TickEvent.Phase.END || world.isRemote || world.getTotalWorldTime() % 20L != 0L) return;
        VillagePoiData120 data = VillagePoiData120.get(world);
        // POI updates may wake, navigate or otherwise update villagers. Forge/mod
        // callbacks triggered by those operations are allowed to add or remove an
        // entity, so iterating the live ArrayList can crash the server tick with a
        // ConcurrentModificationException. Process the tick-start snapshot instead.
        for (Object raw : stableIterationSnapshot(world.loadedEntityList)) {
            if (!(raw instanceof EntityVillager)||raw instanceof EntityWanderingTrader120) continue;
            EntityVillager villager = (EntityVillager)raw;
            if (!villager.isEntityAlive() || villager.getProfession()<0 || villager.getProfession()>5) continue;
            VillagerSleep120.initializeAfterConstruction(villager);
            com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112.refreshVisualData(villager);
            String owner = villager.getUniqueID().toString();
            updateHomeAndSleep(data, villager, owner);
            if (VillagerSleep120.isRestTime(world.getWorldTime())) continue;
            if (villager.isChild()) continue;
            String modern = com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112.getModernProfession(villager);
            if ("nitwit".equals(modern)) continue;
            boolean traded = com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112.hasTraded(villager);
            VillagePoiData120.Entry poi = data.ownedBy(world, owner);
            boolean unemployed = modern.length() == 0 || "none".equals(modern);
            if (poi == null && !unemployed)
                poi = data.claimNearest(world, villager.posX, villager.posY, villager.posZ, owner, modern);
            if (poi == null && !unemployed && !traded) {
                com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112.setModernProfession(villager, "none");
                modern = "none"; unemployed = true;
            }
            if (poi == null && unemployed && !traded)
                poi = data.claimNearest(world, villager.posX, villager.posY, villager.posZ, owner, null);
            if (poi != null && !poi.type.equals(modern)) {
                if (mayAdoptPoiProfession(traded, modern, poi.type))
                    VillagePoiType120.applyProfession(villager, poi.type);
                else data.releaseJob(owner);
            }
            com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerEconomy145.workTick(villager,poi);
        }
    }

    private static void updateHomeAndSleep(VillagePoiData120 data, EntityVillager villager, String owner) {
        VillagePoiData120.Entry home = data.homeOwnedBy(villager.worldObj, owner);
        if (home != null) {
            double farX = home.x + .5D - villager.posX;
            double farZ = home.z + .5D - villager.posZ;
            if (farX * farX + farZ * farZ > 48.0D * 48.0D) {
                data.releaseHome(owner);
                home = null;
            }
        }
        if (home == null)
            home = data.claimNearestHome(villager.worldObj, villager.posX, villager.posY, villager.posZ, owner);
        if (home != null && !home.loaded(villager.worldObj)) {
            VillagerSleep120.wake(villager);
            return;
        }
        if (!VillagerSleep120.maySleep(villager)
                || !VillagerSleep120.bedAvailable(villager.worldObj, home, villager)) {
            VillagerSleep120.wake(villager);
            return;
        }
        double dx = home.x + .5D - villager.posX;
        double dy = home.y + .5625D - villager.posY;
        double dz = home.z + .5D - villager.posZ;
        if (dx * dx + dy * dy + dz * dz <= 2.25D) VillagerSleep120.sleep(villager, home);
        else if (!VillagerSleep120.isSleeping(villager))
            villager.getNavigator().tryMoveToXYZ(home.x + .5D, home.y, home.z + .5D, .55D);
    }

    @SubscribeEvent public void animateRefusal(net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent event){
        if(event.entityLiving instanceof EntityVillager&&!(event.entityLiving instanceof EntityWanderingTrader120))
            com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112.tickUnhappy((EntityVillager)event.entityLiving);
    }

    @SubscribeEvent public void entityConstructing(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityVillager&&!(event.entity instanceof EntityWanderingTrader120))
            VillagerSleep120.initializeWatcher((EntityVillager)event.entity);
    }

    @SubscribeEvent public void entityJoinedWorld(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityVillager&&!(event.entity instanceof EntityWanderingTrader120)) {
            VillagerSleep120.initializeAfterConstruction((EntityVillager)event.entity);
            com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112.refreshVisualData(
                    (EntityVillager)event.entity);
        }
    }

    @SubscribeEvent public void blockPlaced(BlockEvent.PlaceEvent event) {
        VillagePoiData120.registerIfPoi(event.world, event.x, event.y, event.z);
        if (event.world.getBlock(event.x, event.y, event.z) instanceof BlockBed) {
            int metadata = event.world.getBlockMetadata(event.x, event.y, event.z);
            if (!BlockBed.isBlockHeadOfBed(metadata)) {
                int direction = metadata & 3;
                int[] dx = {0, -1, 0, 1};
                int[] dz = {1, 0, -1, 0};
                VillagePoiData120.registerIfPoi(event.world, event.x + dx[direction], event.y,
                        event.z + dz[direction]);
            }
        }
    }

    @SubscribeEvent public void blockBroken(BlockEvent.BreakEvent event) {
        VillagePoiData120.remove(event.world, event.x, event.y, event.z);
    }

    @SubscribeEvent(priority=cpw.mods.fml.common.eventhandler.EventPriority.LOWEST) public void livingDeath(LivingDeathEvent event) {
        if (!event.isCanceled() && event.entityLiving instanceof EntityVillager&&!(event.entityLiving instanceof EntityWanderingTrader120)
                && !event.entityLiving.worldObj.isRemote)
            VillagePoiData120.get(event.entityLiving.worldObj).releaseOwner(
                    event.entityLiving.getUniqueID().toString());
    }

    @SubscribeEvent public void livingHurt(LivingHurtEvent event){
        if(event.entityLiving instanceof EntityVillager&&!(event.entityLiving instanceof EntityWanderingTrader120)
                &&!event.entityLiving.worldObj.isRemote)
            VillagerSleep120.wake((EntityVillager)event.entityLiving);
    }

    /** One-time upgrade scan for chunks created before the persistent POI index existed. */
    @SubscribeEvent public void chunkLoad(ChunkEvent.Load event){
        if(!event.world.isRemote)
            VillagePoiData120.get(event.world).migrateChunkOnLoad(event.world,event.getChunk());
    }

    public static boolean mayAdoptPoiProfession(boolean traded, String current, String poiType) {
        return !traded && poiType != null && !poiType.equals(current);
    }

    /** Snapshot boundary used by the world tick and exposed for its headless regression fixture. */
    public static Object[] stableIterationSnapshot(List<?> entities) {
        return entities == null ? new Object[0] : entities.toArray();
    }
}
