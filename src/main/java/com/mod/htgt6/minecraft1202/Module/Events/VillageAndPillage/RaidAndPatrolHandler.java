package com.mod.htgt6.minecraft1202.Module.Events.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.VillagePoiData120;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityRavager;

import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageEffects120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidWavePlan145;
import net.minecraft.entity.EntityLiving;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.HashSet;
import java.util.HashMap;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidMechanics120;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;
import cpw.mods.fml.common.eventhandler.EventPriority;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

/** Bad Omen, patrol and persistent raid loop for 1.7.10 villages. */
public final class RaidAndPatrolHandler {
    private final Map<World, Integer> patrolDelays = new WeakHashMap<World, Integer>();


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void death(LivingDeathEvent event) {
        World world = event.entityLiving.worldObj;
        if (event.isCanceled() || world.isRemote || (!(event.entityLiving instanceof
                com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.IRaider112)
                && !(event.entityLiving instanceof net.minecraft.entity.monster.EntityWitch))) return;
        RaidData data = RaidData.get(world);
        for (Raid raid : data.active) {
            if (!raid.members.contains(event.entityLiving.getUniqueID())) continue;
            raid.retire(event.entityLiving.getUniqueID(),false);
            if(event.entityLiving.getUniqueID().equals(raid.leader))raid.leader=null;
            if (event.source.getEntity() instanceof EntityPlayer)
                raid.heroes.add(event.source.getEntity().getUniqueID());
            data.markDirty();
            return; // A raid captain must not give fresh Bad Omen during his own raid.
        }
        if (!(event.entityLiving instanceof EntityLiving)
                || !com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidCaptain153.isCaptain((EntityLiving)event.entityLiving)) return;
        if (!raidsEnabled(world) || !com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidCaptain153.isBanner(
                ((EntityLiving)event.entityLiving).getEquipmentInSlot(4))
                || nearestRaid(world, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ) != null) return;
        net.minecraft.entity.Entity killer = event.source.getEntity();
        if (killer instanceof net.minecraft.entity.passive.EntityWolf && ((net.minecraft.entity.passive.EntityWolf)killer).isTamed())
            killer = ((net.minecraft.entity.passive.EntityWolf)killer).getOwner();
        if (!(killer instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer)killer;
        VillageEffects120.captainKilled(player);
        int level = VillageEffects120.badOmenLevel(player);
        if (!player.worldObj.isRemote) player.addChatMessage(new ChatComponentText(
                "Bad Omen " + roman(level) + " - entering a village will start a raid"));
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if(event.phase==TickEvent.Phase.END) VillageEffects120.migrateLegacy(event.player);
    }

    @SubscribeEvent
    public void worldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (event.phase != TickEvent.Phase.END || world.isRemote || !supportsRaids(world)) return;
        tickPatrol(world);
        if (world.getTotalWorldTime() % 20L != 0L) return;

        VillagePoiData120.get(world).refreshMeetingTickets(world);
        startPendingRaids(world);
        List<Raid> active = RaidData.get(world).active;
        if (!active.isEmpty()) {
            Iterator<Raid> iterator = active.iterator();
            while (iterator.hasNext()) if (iterator.next().tick(world)) iterator.remove();
            RaidData.get(world).markDirty();
        }
        syncHud(world, active);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void entityJoined(net.minecraftforge.event.entity.EntityJoinWorldEvent event) {
        if(event.isCanceled()||event.world.isRemote||!(event.entity instanceof net.minecraft.entity.EntityCreature))return;
        net.minecraft.entity.EntityCreature creature=(net.minecraft.entity.EntityCreature)event.entity;
        if(isActiveRaider(creature)||lostRaid(creature)){
            com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidApproachGoal152.attach(creature);
            com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidBannerGoal153.attach(creature);
            RaidMechanics120.attachVillageGoals(creature);
        }
    }

    /** 1.7 has no DimensionType.hasRaids; surface dimensions opt in, Nether/End do not. */
    public static boolean supportsRaids(World world) { return world.provider.isSurfaceWorld(); }

    @SubscribeEvent public void raiderTick(net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent event) {
        if (!(event.entityLiving instanceof net.minecraft.entity.EntityCreature)
                || !(event.entityLiving instanceof com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.IRaider112)) return;
        net.minecraft.entity.EntityCreature mob=(net.minecraft.entity.EntityCreature)event.entityLiving;
        if (mob.worldObj.isRemote || !mob.isEntityAlive() || mob.ticksExisted % 20 != 0
                || !supportsRaids(mob.worldObj) || !raidsEnabled(mob.worldObj)) return;
        EntityLivingBase target=mob.getAttackTarget();
        int idle=target instanceof EntityPlayer || target instanceof net.minecraft.entity.monster.EntityIronGolem
                ?0:Math.min(100000,mob.getEntityData().getInteger("HTGT6RaidIdle")+20);
        mob.getEntityData().setInteger("HTGT6RaidIdle",idle);
        if (!isActiveRaider(mob) && idle <= 2400) {
            Raid raid=nearestRaid(mob.worldObj,mob.posX,mob.posY,mob.posZ);
            if(raid!=null && raid.status==0) { raid.join(mob); RaidData.get(mob.worldObj).markDirty(); }
        }
    }

    @SubscribeEvent public void preventRaidDespawn(net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn event) {
        if(isActiveRaider(event.entityLiving)||lostRaid(event.entityLiving))
            event.setResult(cpw.mods.fml.common.eventhandler.Event.Result.DENY);
    }

    private static Raid nearestRaid(World world,double x,double y,double z) {
        Raid nearest=null; double distance=96D*96D;
        for(Raid raid:RaidData.get(world).active) {
            double dx=raid.center.posX-x,dy=raid.center.posY-y,dz=raid.center.posZ-z;
            double candidate=dx*dx+dy*dy+dz*dz;
            if(candidate<distance){distance=candidate;nearest=raid;}
        }
        return nearest;
    }

    public static Village legacyVillage(World world,int x,int y,int z,int radius) {
        Village village=world.villageCollectionObj.findNearestVillage(x,y,z,radius);
        return village!=null && village.getNumVillagers()>0 ? village : null;
    }

    public static boolean canPromote(net.minecraft.entity.EntityCreature entity) {
        if(entity==null||entity.worldObj.isRemote||!entity.isEntityAlive()
                ||!com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidCaptain153.eligible(entity)
                ||com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidCaptain153.isBanner(entity.getEquipmentInSlot(4)))return false;
        for(Raid raid:RaidData.get(entity.worldObj).active) {
            if(raid.status!=0||!raid.members.contains(entity.getUniqueID()))continue;
            if(raid.leader!=null&&raid.members.contains(raid.leader))return false;
            // Migration for saved raids predating the explicit leader UUID.
            for(Object value:entity.worldObj.loadedEntityList)if(value instanceof EntityLiving){
                EntityLiving candidate=(EntityLiving)value;
                if(raid.members.contains(candidate.getUniqueID())&&com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidCaptain153.isCaptain(candidate)){
                    raid.leader=candidate.getUniqueID();return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean promote(net.minecraft.entity.EntityCreature entity,net.minecraft.entity.item.EntityItem item) {
        if(!canPromote(entity)||item==null||item.worldObj!=entity.worldObj||item.isDead||item.delayBeforeCanPickup>0
                ||entity.getDistanceSqToEntity(item)>=2D||!com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidCaptain153.isBanner(item.getEntityItem()))return false;
        for(Raid raid:RaidData.get(entity.worldObj).active)if(raid.status==0&&raid.members.contains(entity.getUniqueID())) {
            net.minecraft.item.ItemStack old=entity.getEquipmentInSlot(4);
            if(old!=null){
                NBTTagCompound saved=new NBTTagCompound();entity.writeEntityToNBT(saved);
                NBTTagList chances=saved.getTagList("DropChances",5);
                float chance=chances.tagCount()>4?chances.func_150308_e(4):.085F;
                if(Math.max(entity.getRNG().nextFloat()-.1F,0)<chance)entity.entityDropItem(old.copy(),0);
            }
            com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidCaptain153.equip(entity,item.getEntityItem());
            entity.onItemPickup(item,item.getEntityItem().stackSize);item.setDead();
            raid.leader=entity.getUniqueID();RaidData.get(entity.worldObj).markDirty();return true;
        }
        return false;
    }

    public static ChunkCoordinates activeRaidCenter(EntityLivingBase entity) {
        if(entity==null||entity.worldObj==null||entity.worldObj.isRemote)return null;
        for(Raid raid:RaidData.get(entity.worldObj).active)
            if(raid.status==0&&raid.members.contains(entity.getUniqueID()))return new ChunkCoordinates(raid.center);
        return null;
    }

    public static boolean isActiveRaider(EntityLivingBase entity) {
        if(entity==null||entity.worldObj==null||entity.worldObj.isRemote)return false;
        for(Raid raid:RaidData.get(entity.worldObj).active)
            if(raid.status==0&&raid.members.contains(entity.getUniqueID()))return true;
        return false;
    }

    public static boolean lostRaid(EntityLivingBase entity) {
        if(entity==null||entity.worldObj.isRemote)return false;
        for(Raid raid:RaidData.get(entity.worldObj).active)
            if(raid.status==2&&raid.members.contains(entity.getUniqueID()))return true;
        return false;
    }

    private static void syncHud(World world, List<Raid> raids) {
        for(Object value:world.playerEntities) {
            if(!(value instanceof EntityPlayerMP))continue;
            EntityPlayerMP player=(EntityPlayerMP)value;Raid nearest=null;double distance=96D*96D;
            for(Raid raid:raids) {
                if(!world.checkChunksExist(raid.center.posX,0,raid.center.posZ,raid.center.posX,world.getHeight()-1,raid.center.posZ))continue;
                double candidate=player.getDistanceSq(raid.center.posX,raid.center.posY,raid.center.posZ);
                if(candidate<distance){distance=candidate;nearest=raid;}
            }
            com.mod.htgt6.minecraft1202.Module.Network.VillageAndPillage.RaidNetwork150.send(player,
                new com.mod.htgt6.minecraft1202.Module.Network.VillageAndPillage.RaidNetwork150.Snapshot(
                    world.provider.dimensionId,nearest!=null,nearest==null?0:nearest.status,
                    nearest==null?0:nearest.members.size(),nearest==null?0:nearest.progress(world)));
        }
    }

    private void startPendingRaids(World world) {
        if (world.difficultySetting == EnumDifficulty.PEACEFUL || !raidsEnabled(world)) return;
        for (Object value : world.playerEntities) {
            if (!(value instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer)value;
            VillageEffects120.migrateLegacy(player);
            int omen = VillageEffects120.badOmenLevel(player);
            if (omen <= 0) continue;
            Village village = legacyVillage(world,
                    MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY),
                    MathHelper.floor_double(player.posZ), 48);
            ChunkCoordinates center=village==null?VillagePoiData120.findVillageCenter(world,
                    MathHelper.floor_double(player.posX),MathHelper.floor_double(player.posY),
                    MathHelper.floor_double(player.posZ),48):village.getCenter();
            if (center == null) continue;
            Raid existing=nearestRaid(world,center.posX,center.posY,center.posZ);
            if(existing!=null) {
                if(existing.status==0)existing.absorb(omen);
                VillageEffects120.consumeBadOmen(player);
                RaidData.get(world).markDirty();
                continue;
            }
            List<Raid> active = RaidData.get(world).active;

            active.add(new Raid(center, omen, world.difficultySetting));
            VillageEffects120.consumeBadOmen(player);
            RaidData.get(world).markDirty();
            player.addChatMessage(new ChatComponentText("Raid! Defend the village."));
        }
    }

    private boolean hasRaidAt(World world, ChunkCoordinates center) {
        return nearestRaid(world,center.posX,center.posY,center.posZ)!=null;
    }

    private static boolean raidsEnabled(World world) {
        if (!world.getGameRules().hasRule("disableRaids"))
            world.getGameRules().addGameRule("disableRaids", "false");
        return !world.getGameRules().getGameRuleBooleanValue("disableRaids");
    }

    private static boolean patrolSpawningEnabled(World world) {
        // Register missing rules for existing 1.7.10 worlds without resetting saved values.
        if (!world.getGameRules().hasRule("doPatrolSpawning"))
            world.getGameRules().addGameRule("doPatrolSpawning", "true");
        return world.getGameRules().getGameRuleBooleanValue("doPatrolSpawning");
    }

    private void tickPatrol(World world) {
        if (world.isRemote || world.provider.dimensionId != 0
                || !patrolSpawningEnabled(world)
                || world.difficultySetting == EnumDifficulty.PEACEFUL
                || !world.getGameRules().getGameRuleBooleanValue("doMobSpawning")) return;
        Integer previous = patrolDelays.get(world);
        int remaining = (previous == null ? 0 : previous.intValue()) - 1;
        if (remaining > 0) {
            patrolDelays.put(world, remaining);
            return;
        }
        // Vanilla keeps the countdown even when daylight, age or player checks fail.
        patrolDelays.put(world, remaining + 12000 + world.rand.nextInt(1200));
        trySpawnPatrol(world);
    }

    private static int patrolSize(EnumDifficulty difficulty, long dayTime, long inhabitedTime, float moon) {
        if (difficulty == EnumDifficulty.PEACEFUL) return 0;
        // Port DifficultyInstance using the chunk age already available in 1.7.10.
        // World.func_147462_b returns a differently scaled legacy value.
        float age = MathHelper.clamp_float(((float)dayTime - 72000.0F) / 1440000.0F, 0, 1) * 0.25F;
        float local = MathHelper.clamp_float((float)inhabitedTime / 3600000.0F, 0, 1)
                * (difficulty == EnumDifficulty.HARD ? 1.0F : 0.75F);
        local += MathHelper.clamp_float(moon * 0.25F, 0, age);
        if (difficulty == EnumDifficulty.EASY) local *= 0.5F;
        return (int)Math.ceil(difficulty.getDifficultyId() * (0.75F + age + local)) + 1;
    }

    private void trySpawnPatrol(World world) {
        if (world.isRemote || world.provider.dimensionId != 0
                || !patrolSpawningEnabled(world)
                || world.difficultySetting == EnumDifficulty.PEACEFUL
                || !world.getGameRules().getGameRuleBooleanValue("doMobSpawning")
                || world.getWorldTime() / 24000L < 5L || !world.isDaytime()
                || world.playerEntities.isEmpty() || world.rand.nextInt(5) != 0) return;
        EntityPlayer target = (EntityPlayer)world.playerEntities.get(world.rand.nextInt(world.playerEntities.size()));
        // 1.7.10 has no spectator mode. Creative/invulnerable players remain eligible.
        if (!target.isEntityAlive()) return;
        int px = MathHelper.floor_double(target.posX), py = MathHelper.floor_double(target.posY),
                pz = MathHelper.floor_double(target.posZ);
        if (legacyVillage(world,px, py, pz, 32) != null
                || VillagePoiData120.findVillageCenter(world, px, py, pz, 32) != null) return;
        int x = px + (24 + world.rand.nextInt(24)) * (world.rand.nextBoolean() ? -1 : 1);
        int z = pz + (24 + world.rand.nextInt(24)) * (world.rand.nextBoolean() ? -1 : 1);
        if (!world.checkChunksExist(x - 10, 0, z - 10, x + 10, world.getHeight()-1, z + 10)) return;
        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
        if (biome == BiomeGenBase.mushroomIsland || biome == BiomeGenBase.mushroomIslandShore) return;
        int size = patrolSize(world.difficultySetting, world.getWorldTime(),
                world.getChunkFromBlockCoords(x, z).inhabitedTime, world.getCurrentMoonPhaseFactor());
        for (int i = 0; i < size; i++) {
            if (!spawnPatrolMember(world, x, z, i == 0) && i == 0) break;
            x += world.rand.nextInt(5) - world.rand.nextInt(5);
            z += world.rand.nextInt(5) - world.rand.nextInt(5);
        }
    }

    private static boolean spawnPatrolMember(World world, int x, int z, boolean captain) {
        if (!world.checkChunksExist(x - 1, 0, z - 1, x + 1, world.getHeight()-1, z + 1)) return false;
        int y = world.getTopSolidOrLiquidBlock(x, z);
        if (y <= 0 || y >= world.getHeight()-1 || !World.doesBlockHaveSolidTopSurface(world, x, y - 1, z)
                || world.getBlock(x, y - 1, z) == Blocks.bedrock
                || world.getSavedLightValue(EnumSkyBlock.Block, x, y, z) > 8) return false;
        EntityPillager pillager = new EntityPillager(world);
        pillager.setLocationAndAngles(x + 0.5D, y, z + 0.5D, world.rand.nextFloat() * 360.0F, 0.0F);
        // EntityMob.getCanSpawnHere includes darkness rules, unsuitable for daytime patrols.
        if (!world.checkNoEntityCollision(pillager.boundingBox)
                || !world.getCollidingBoundingBoxes(pillager, pillager.boundingBox).isEmpty()
                || world.isAnyLiquid(pillager.boundingBox)) return false;
        pillager.setCaptain(captain);
        pillager.setPatrolling(true);
        pillager.findPatrolTarget();
        pillager.onSpawnWithEgg(null);
        return world.spawnEntityInWorld(pillager);
    }

    private static final class Raid {
        final ChunkCoordinates center;
        int omen;
        int totalWaves;
        int ticksActive;
        final Set<UUID> members = new HashSet<UUID>();
        final Set<UUID> heroes = new HashSet<UUID>();
        final Map<UUID,Integer> missingTicks = new HashMap<UUID,Integer>();
        final Map<UUID,Integer> outsideTicks = new HashMap<UUID,Integer>();
        final Map<UUID,Float> lastHealth = new HashMap<UUID,Float>();
        int wave;
        int cooldown = 15;
        int status; // 0 ongoing, 1 victory, 2 loss. Terminal states celebrate for 600 ticks.
        int celebrationTicks;
        int postRaidTicks;
        float totalHealth;
        UUID leader;
        int failedWaveAttempts;

        Raid(ChunkCoordinates center, int omen, EnumDifficulty difficulty) {
            this.center = center;
            this.omen = Math.max(0, Math.min(5, omen));
            int groups = difficulty == EnumDifficulty.EASY ? 3
                    : difficulty == EnumDifficulty.NORMAL ? 5
                    : difficulty == EnumDifficulty.HARD ? 7 : 0;
            // Modern Raid captures the base count at creation; Bad Omen II+ adds one group.
            this.totalWaves = groups == 0 ? 0 : groups + (this.omen > 1 ? 1 : 0);
        }

        void absorb(int level) {
            int base=totalWaves-(omen>1?1:0);
            omen=Math.min(5,omen+Math.max(0,level));
            totalWaves=base+(base>0 && omen>1?1:0);
        }

        boolean tick(World world) {
            if (!raidsEnabled(world) || world.difficultySetting == EnumDifficulty.PEACEFUL || totalWaves == 0) return true;
            if (status != 0) {
                celebrationTicks += 20;
                return celebrationTicks >= 600;
            }
            // A missing unloaded village is unknown, not a defeat.
            if (!world.checkChunksExist(center.posX, 0, center.posZ, center.posX, world.getHeight()-1, center.posZ)) return false;
            if (!retainVillage(world)) {
                if (wave == 0) return true;
                status = 2;
                return false;
            }
            ticksActive += 20;
            if (ticksActive >= 48000) return true;
            if (livingRaiders(world) > 0) { postRaidTicks = 0; return false; }
            if (wave >= totalWaves) {
                postRaidTicks += 20;
                if (postRaidTicks >= 40) {
                    rewardHeroes(world);
                    status = 1;
                }
                return false;
            }
            if (cooldown > 0 && --cooldown > 0) return false;
            int spawned = spawnWave(world, wave + 1);
            cooldown = 15;
            if (spawned == 0) return ++failedWaveAttempts >= 3;
            wave++;
            failedWaveAttempts = 0;
            return false;
        }

        private boolean retainVillage(World world) {
            Village village = legacyVillage(world,center.posX, center.posY, center.posZ, 0);
            if (village != null || VillagePoiData120.findVillageCenter(world, center.posX, center.posY, center.posZ, 16) != null) return true;
            // Modern Raid searches two sections around its old center. Check only loaded
            // candidates; neither POI lookup nor legacy village lookup loads terrain.
            ChunkCoordinates nearest = null;
            double distance = Double.MAX_VALUE;
            int sx = center.posX >> 4, sy = center.posY >> 4, sz = center.posZ >> 4;
            for (int dx = -2; dx <= 2; dx++) for (int dy = -2; dy <= 2; dy++) for (int dz = -2; dz <= 2; dz++) {
                int x = ((sx + dx) << 4) + 8, y = ((sy + dy) << 4) + 8, z = ((sz + dz) << 4) + 8;
                if (y < 0 || y >= world.getHeight() || !world.checkChunksExist(x, 0, z, x, world.getHeight()-1, z)) continue;
                village = legacyVillage(world,x, y, z, 0);
                if (village == null && VillagePoiData120.findVillageCenter(world, x, y, z, 16) == null) continue;
                double d = (x-center.posX)*(double)(x-center.posX)+(y-center.posY)*(double)(y-center.posY)+(z-center.posZ)*(double)(z-center.posZ);
                if (d < distance) { distance = d; nearest = new ChunkCoordinates(x,y,z); }
            }
            if (nearest == null) return false;
            center.set(nearest.posX, nearest.posY, nearest.posZ);
            return true;
        }

        private float progress(World world) {
            if(status==1)return 0;
            if(members.isEmpty())return Math.max(0,Math.min(1,(15-cooldown)/15F));
            float health=0;int loaded=0;
            for(Object value:world.loadedEntityList)if(value instanceof EntityLivingBase) {
                EntityLivingBase entity=(EntityLivingBase)value;
                if(members.contains(entity.getUniqueID())){health+=Math.max(0,entity.getHealth());lastHealth.put(entity.getUniqueID(),Math.max(0,entity.getHealth()));loaded++;}
            }
            // A missing member may merely be unloaded; do not advertise a cleared wave.
            if(loaded<members.size())for(UUID id:members) {
                boolean found=false;for(Object value:world.loadedEntityList)if(value instanceof EntityLivingBase
                        && id.equals(((EntityLivingBase)value).getUniqueID())){found=true;break;}
                if(!found)health+=lastHealth.containsKey(id)?lastHealth.get(id):0;
            }
            if(totalHealth<=0)return 1;
            return Math.max(0,Math.min(1,health/totalHealth));
        }

        private int livingRaiders(World world) {
            Map<UUID,EntityLivingBase> loaded=new HashMap<UUID,EntityLivingBase>();
            for(Object value:world.loadedEntityList)if(value instanceof EntityLivingBase) {
                EntityLivingBase mob=(EntityLivingBase)value; loaded.put(mob.getUniqueID(),mob);
            }
            for(UUID id:new HashSet<UUID>(members)) {
                EntityLivingBase mob=loaded.get(id);
                if(mob==null) {
                    // Let saved entities rejoin after chunk loading. Count only active raid ticks.
                    int absent=(missingTicks.containsKey(id)?missingTicks.get(id):0)+20;
                    missingTicks.put(id,absent);
                    if(absent>=600)retire(id,true);
                    continue;
                }
                missingTicks.remove(id); lastHealth.put(id,Math.max(0,mob.getHealth()));
                if(mob.isDead || mob.worldObj!=world || mob.getDistanceSq(center.posX,center.posY,center.posZ)>=112D*112D) {
                    retire(id,true);continue;
                }
                if(mob.ticksExisted>600 && mob.getEntityData().getInteger("HTGT6RaidIdle")>2400
                        && legacyVillage(world,(int)mob.posX,(int)mob.posY,(int)mob.posZ,0)==null
                        && VillagePoiData120.findVillageCenter(world,(int)mob.posX,(int)mob.posY,(int)mob.posZ,16)==null) {
                    int outside=(outsideTicks.containsKey(id)?outsideTicks.get(id):0)+1;
                    outsideTicks.put(id,outside);if(outside>=30)retire(id,true);
                } else outsideTicks.remove(id);
            }
            return members.size();
        }

        private void retire(UUID id,boolean adjustHealth) {
            if(!members.remove(id))return;
            Float health=lastHealth.remove(id);
            if(adjustHealth && health!=null)totalHealth=Math.max(0,totalHealth-health);
            missingTicks.remove(id);outsideTicks.remove(id);
            if(id.equals(leader))leader=null;
        }

        private void join(EntityLivingBase entity) {
            UUID id=entity.getUniqueID();
            if(members.add(id))totalHealth+=entity.getHealth();
            missingTicks.remove(id);lastHealth.put(id,entity.getHealth());
            if(entity instanceof net.minecraft.entity.EntityCreature) {
                net.minecraft.entity.EntityCreature mob=(net.minecraft.entity.EntityCreature)entity;
                com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidApproachGoal152.attach(mob);
                com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidBannerGoal153.attach(mob);
                RaidMechanics120.attachVillageGoals(mob);
            }
            if(entity instanceof EntityPillager)((EntityPillager)entity).setPatrolling(false);
        }

        private boolean spawnMember(World world, EntityLivingBase entity) {
            if (!world.spawnEntityInWorld(entity)) return false;
            join(entity);return true;
        }

        private int spawnWave(World world, int number) {
            int baseWaves=totalWaves-(omen>1?1:0);
            if(world.isRemote||world.difficultySetting==EnumDifficulty.PEACEFUL||number<1||number>totalWaves||baseWaves<=0)return 0;
            ChunkCoordinates anchor=findSpawnAnchor(world);
            if(anchor==null)return 0;
            totalHealth=0;
            int spawned=0;boolean captain=false,bonus=number>baseWaves;
            for(RaidWavePlan145.Type type:RaidWavePlan145.Type.values()){
                int count=RaidWavePlan145.baseCount(type,number,baseWaves)
                        +RaidWavePlan145.bonusCount(type,number,bonus,world.difficultySetting,world.rand);
                for(int index=0;index<count;index++){
                    EntityLiving entity=type.create(world);
                    if(!placeGroundMember(world,entity,anchor))continue;
                    entity.onSpawnWithEgg(null);
                    RaidMechanics120.applyBuffs(entity,number,omen);
                    boolean eligible=com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidCaptain153.eligible(entity);
                    if(!captain&&eligible)com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.RaidCaptain153.equip(entity,EntityPillager.createCaptainBanner());
                    if(!spawnMember(world,entity))continue;
                    spawned++;if(!captain&&eligible){captain=true;leader=entity.getUniqueID();}
                    if(type==RaidWavePlan145.Type.RAVAGER){
                        RaidWavePlan145.Type riderType=RaidWavePlan145.rider(number,index);
                        if(riderType==null)continue;
                        EntityLiving rider=riderType.create(world);
                        double yaw=entity.rotationYaw*Math.PI/180D;
                        rider.setLocationAndAngles(entity.posX+Math.sin(yaw)*.0625D,entity.posY+entity.getMountedYOffset()+rider.getYOffset(),entity.posZ-Math.cos(yaw)*.0625D,entity.rotationYaw,0);
                        if(!hasClearance(world,rider))continue;
                        rider.onSpawnWithEgg(null);
                        RaidMechanics120.applyBuffs(rider,number,omen);
                        if(spawnMember(world,rider)){rider.mountEntity(entity);entity.updateRiderPosition();spawned++;}
                    }
                }
            }
            if(spawned>0) {
                for(Object value:world.playerEntities)if(value instanceof EntityPlayerMP) {
                    EntityPlayerMP player=(EntityPlayerMP)value;
                    double dx=anchor.posX-player.posX,dz=anchor.posZ-player.posZ,d=Math.sqrt(dx*dx+dz*dz);
                    if(d>64 && player.getDistanceSq(center.posX,center.posY,center.posZ)>96D*96D)continue;
                    double scale=d>.001?13/d:0;
                    player.playerNetServerHandler.sendPacket(new net.minecraft.network.play.server.S29PacketSoundEffect(
                            "htgt6:event.raid.horn",player.posX+dx*scale,player.posY,player.posZ+dz*scale,64,1));
                }
            }
            return spawned;
        }

        private ChunkCoordinates findSpawnAnchor(World world){
            int stage=Math.min(2,failedWaveAttempts),ring=stage==0?2:2-stage;
            for(int attempt=0;attempt<20;attempt++){
                float angle=world.rand.nextFloat()*(float)(Math.PI*2D);
                int x=center.posX+MathHelper.floor_float(MathHelper.cos(angle)*32F*ring)+world.rand.nextInt(5);
                int z=center.posZ+MathHelper.floor_float(MathHelper.sin(angle)*32F*ring)+world.rand.nextInt(5);
                if(!world.checkChunksExist(x-10,0,z-10,x+10,world.getHeight()-1,z+10))continue;
                int y=world.getTopSolidOrLiquidBlock(x,z);
                if(!validGround(world,x,y,z))continue;
                if(stage<2&&(legacyVillage(world,x,y,z,0)!=null
                        ||VillagePoiData120.findVillageCenter(world,x,y,z,32)!=null))continue;
                return new ChunkCoordinates(x,y,z);
            }
            return null;
        }

        private boolean placeGroundMember(World world,EntityLiving entity,ChunkCoordinates anchor){
            for(int attempt=0;attempt<20;attempt++){
                int x=anchor.posX+world.rand.nextInt(9)-4,z=anchor.posZ+world.rand.nextInt(9)-4;
                if(!world.checkChunksExist(x-2,0,z-2,x+2,world.getHeight()-1,z+2))continue;
                int y=world.getTopSolidOrLiquidBlock(x,z);
                if(!validGround(world,x,y,z))continue;
                entity.setLocationAndAngles(x+.5D,y,z+.5D,world.rand.nextFloat()*360F,0);
                if(hasClearance(world,entity))return true;
            }
            return false;
        }

        private static boolean validGround(World world,int x,int y,int z){
            if(y<=0||y>=world.getHeight()-1)return false;
            net.minecraft.block.Block floor=world.getBlock(x,y-1,z);
            return floor!=Blocks.bedrock && (World.doesBlockHaveSolidTopSurface(world,x,y-1,z)
                    ||floor==Blocks.snow_layer&&world.isAirBlock(x,y,z));
        }

        private static boolean hasClearance(World world,EntityLiving entity){
            net.minecraft.util.AxisAlignedBB box=entity.boundingBox;
            return box.minY>=0&&box.maxY<world.getHeight()
                    &&world.checkChunksExist(MathHelper.floor_double(box.minX),MathHelper.floor_double(box.minY),MathHelper.floor_double(box.minZ),
                            MathHelper.floor_double(box.maxX),MathHelper.floor_double(box.maxY),MathHelper.floor_double(box.maxZ))
                    &&world.checkNoEntityCollision(box)&&world.getCollidingBoundingBoxes(entity,box).isEmpty()
                    &&!world.isAnyLiquid(box);
        }

        private void rewardHeroes(World world) {
            for (Object value : world.playerEntities) {
                EntityPlayer player = (EntityPlayer)value;
                if (!player.isEntityAlive() || !heroes.contains(player.getUniqueID())) continue;
                VillageEffects120.rewardHero(player, omen);
                player.addChatMessage(new ChatComponentText("Hero of the Village!"));

            }
        }
    }

    /** Per-dimension save. UUIDs intentionally survive entity/chunk unloads. */
    public static final class RaidData extends WorldSavedData {
        private static final String NAME = "HTGT6Raids120";
        final List<Raid> active = new ArrayList<Raid>();
        public RaidData() { super(NAME); }
        public RaidData(String name) { super(name); }
        static RaidData get(World world) {
            RaidData data = (RaidData)world.perWorldStorage.loadData(RaidData.class, NAME);
            if (data == null) {
                data = new RaidData();
                world.perWorldStorage.setData(NAME, data);
            }
            return data;
        }
        public void readFromNBT(NBTTagCompound tag) {
            active.clear();
            NBTTagList list = tag.getTagList("Raids", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound entry = list.getCompoundTagAt(i);
                Raid raid = new Raid(new ChunkCoordinates(entry.getInteger("X"), entry.getInteger("Y"),
                        entry.getInteger("Z")), entry.getInteger("Omen"), EnumDifficulty.NORMAL);
                raid.totalWaves = Math.max(0, Math.min(8, entry.getInteger("TotalWaves")));
                raid.wave = Math.max(0, Math.min(raid.totalWaves, entry.getInteger("Wave")));
                raid.cooldown = Math.max(0, Math.min(15, entry.getInteger("Cooldown")));
                raid.failedWaveAttempts = Math.max(0, Math.min(3, entry.getInteger("Failures")));
                raid.ticksActive = Math.max(0, Math.min(48000, entry.getInteger("TicksActive")));
                raid.status = Math.max(0, Math.min(2, entry.getInteger("Status")));
                raid.celebrationTicks = Math.max(0, Math.min(600, entry.getInteger("CelebrationTicks")));
                raid.postRaidTicks = Math.max(0, Math.min(40, entry.getInteger("PostRaidTicks")));
                if(entry.hasKey("LeaderMost",4)&&entry.hasKey("LeaderLeast",4))raid.leader=new UUID(entry.getLong("LeaderMost"),entry.getLong("LeaderLeast"));
                raid.totalHealth = entry.getFloat("TotalHealth");
                if(Float.isNaN(raid.totalHealth)||Float.isInfinite(raid.totalHealth)||raid.totalHealth<0)raid.totalHealth=0;
                readIds(entry, "Members", raid.members);
                NBTTagList tracking=entry.getTagList("Tracking",10);
                for(int j=0;j<tracking.tagCount();j++) {
                    NBTTagCompound row=tracking.getCompoundTagAt(j);UUID id=new UUID(row.getLong("Most"),row.getLong("Least"));
                    if(!raid.members.contains(id))continue;
                    raid.missingTicks.put(id,Math.max(0,Math.min(600,row.getInteger("Missing"))));
                    raid.outsideTicks.put(id,Math.max(0,Math.min(30,row.getInteger("Outside"))));
                    float hp=row.getFloat("Health");if(!Float.isNaN(hp)&&!Float.isInfinite(hp))raid.lastHealth.put(id,Math.max(0,hp));
                }
                readIds(entry, "Heroes", raid.heroes);
                active.add(raid);
            }
        }
        public void writeToNBT(NBTTagCompound tag) {
            NBTTagList list = new NBTTagList();
            for (Raid raid : active) {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setInteger("X", raid.center.posX); entry.setInteger("Y", raid.center.posY);
                entry.setInteger("Z", raid.center.posZ); entry.setInteger("Omen", raid.omen);
                entry.setInteger("TotalWaves", raid.totalWaves); entry.setInteger("Wave", raid.wave);
                entry.setInteger("Cooldown", raid.cooldown); entry.setInteger("Failures", raid.failedWaveAttempts);
                entry.setInteger("TicksActive", raid.ticksActive);
                if(raid.leader!=null){entry.setLong("LeaderMost",raid.leader.getMostSignificantBits());entry.setLong("LeaderLeast",raid.leader.getLeastSignificantBits());}
                entry.setFloat("TotalHealth", raid.totalHealth);
                entry.setInteger("Status", raid.status);
                entry.setInteger("CelebrationTicks", raid.celebrationTicks);
                entry.setInteger("PostRaidTicks", raid.postRaidTicks);
                writeIds(entry, "Members", raid.members);
                NBTTagList tracking=new NBTTagList();
                for(UUID id:raid.members) {
                    NBTTagCompound row=new NBTTagCompound();row.setLong("Most",id.getMostSignificantBits());row.setLong("Least",id.getLeastSignificantBits());
                    row.setInteger("Missing",raid.missingTicks.containsKey(id)?raid.missingTicks.get(id):0);
                    row.setInteger("Outside",raid.outsideTicks.containsKey(id)?raid.outsideTicks.get(id):0);
                    row.setFloat("Health",raid.lastHealth.containsKey(id)?raid.lastHealth.get(id):0);tracking.appendTag(row);
                }
                entry.setTag("Tracking",tracking); writeIds(entry, "Heroes", raid.heroes);
                list.appendTag(entry);
            }
            tag.setTag("Raids", list);
        }
        private static void readIds(NBTTagCompound tag, String key, Set<UUID> result) {
            NBTTagList ids = tag.getTagList(key, 10);
            for (int i = 0; i < ids.tagCount(); i++) {
                NBTTagCompound id = ids.getCompoundTagAt(i);
                if (id.hasKey("Most", 4) && id.hasKey("Least", 4))
                    result.add(new UUID(id.getLong("Most"), id.getLong("Least")));
            }
        }
        private static void writeIds(NBTTagCompound tag, String key, Set<UUID> values) {
            NBTTagList ids = new NBTTagList();
            for (UUID uuid : values) {
                NBTTagCompound id = new NBTTagCompound();
                id.setLong("Most", uuid.getMostSignificantBits()); id.setLong("Least", uuid.getLeastSignificantBits());
                ids.appendTag(id);
            }
            tag.setTag(key, ids);
        }
    }

    private static String roman(int value) {
        return new String[] {"", "I", "II", "III", "IV", "V"}[Math.max(0, Math.min(5, value))];
    }
}
