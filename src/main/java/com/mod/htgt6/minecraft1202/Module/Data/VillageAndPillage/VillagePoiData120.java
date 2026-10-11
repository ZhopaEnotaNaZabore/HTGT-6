package com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagePoiType120;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/** Persistent POI index with independent HOME and JOB_SITE tickets. */
public final class VillagePoiData120 extends WorldSavedData {
    private static final String NAME = "HTGT6VillagePoi120";
    private final List<Entry> entries = new ArrayList<Entry>();
    private final Map<Long, List<Entry>> byChunk = new HashMap<Long, List<Entry>>();
    private final Map<String, Entry> byOwner = new HashMap<String, Entry>();
    private final Set<Long> migratedChunks = new HashSet<Long>();

    public VillagePoiData120() { super(NAME); }
    public VillagePoiData120(String name) { super(name); }

    public static VillagePoiData120 get(World world) {
        VillagePoiData120 data = (VillagePoiData120)world.perWorldStorage.loadData(VillagePoiData120.class, NAME);
        if (data == null) {
            data = new VillagePoiData120();
            world.perWorldStorage.setData(NAME, data);
        }
        return data;
    }

    public static void registerIfPoi(World world, int x, int y, int z) {
        if (world == null || world.isRemote) return;
        String type = VillagePoiType120.forBlock(world.getBlock(x, y, z));
        if ("home".equals(type) && !VillagePoiType120.isHomeHead(world, x, y, z)) return;
        if (type != null) register(world, x, y, z, type);
    }

    public static void register(World world, int x, int y, int z, String type) {
        if (world == null || world.isRemote || !VillagePoiType120.isPoi(type)) return;
        VillagePoiData120 data = get(world);
        Entry old = data.at(x, y, z);
        if (old != null) {
            if (!old.type.equals(type)) {
                if (old.owner.length() != 0 && !"meeting".equals(old.type)) data.byOwner.remove(ownerKey(old.owner, old.type));
                old.type = type; old.owner = ""; data.markDirty();
            }
            return;
        }
        Entry entry = new Entry(x, y, z, type, "");
        data.entries.add(entry);
        data.index(entry);
        data.markDirty();
    }

    public static void remove(World world, int x, int y, int z) {
        if (world == null || world.isRemote) return;
        VillagePoiData120 data = get(world);
        for (Iterator<Entry> it = data.entries.iterator(); it.hasNext();) {
            Entry entry = it.next();
            if (entry.x == x && entry.y == y && entry.z == z) {
                it.remove(); data.unindex(entry); data.markDirty(); return;
            }
        }
    }

    public Entry claimNearest(World world, double x, double y, double z, String owner, String requiredType) {
        return claimNearest(world, x, y, z, owner, requiredType, false);
    }

    public Entry claimNearestHome(World world, double x, double y, double z, String owner) {
        return claimNearest(world, x, y, z, owner, "home", true);
    }

    private Entry claimNearest(World world, double x, double y, double z, String owner,
            String requiredType, boolean allowHome) {
        Entry nearest = null;
        double distance = 48.0D * 48.0D;
        int centerX = ((int)Math.floor(x)) >> 4, centerZ = ((int)Math.floor(z)) >> 4;
        List<Entry> invalid = new ArrayList<Entry>();
        for (int chunkX = centerX - 3; chunkX <= centerX + 3; ++chunkX)
            for (int chunkZ = centerZ - 3; chunkZ <= centerZ + 3; ++chunkZ) {
            List<Entry> section = byChunk.get(chunkKey(chunkX, chunkZ));
            if (section == null) continue;
            for (Entry entry : new ArrayList<Entry>(section)) {
            if (!entry.loaded(world)) continue;
            if (!entry.valid(world)) { invalid.add(entry); continue; }
            if (allowHome ? !"home".equals(entry.type) : !VillagePoiType120.isProfession(entry.type)) continue;
            if (requiredType != null && !requiredType.equals(entry.type)) continue;
            if (entry.owner.length() != 0 && !entry.owner.equals(owner)) continue;
            double dx = entry.x + .5D - x, dy = entry.y + .5D - y, dz = entry.z + .5D - z;
            double candidate = dx * dx + dy * dy + dz * dz;
            if (candidate < distance) { distance = candidate; nearest = entry; }
            }
        }
        for (Entry entry : invalid) removeEntry(entry);
        if (nearest != null && !owner.equals(nearest.owner)) {
            if (nearest.owner.length() != 0) byOwner.remove(ownerKey(nearest.owner, nearest.type));
            nearest.owner = owner; byOwner.put(ownerKey(owner, nearest.type), nearest); markDirty();
        }
        return nearest;
    }

    public Entry ownedBy(World world, String owner) {
        return ownedBy(world, owner, false);
    }

    public Entry homeOwnedBy(World world, String owner) {
        return ownedBy(world, owner, true);
    }

    private Entry ownedBy(World world, String owner, boolean home) {
        Entry entry = byOwner.get(ownerKey(owner, home ? "home" : "job"));
        if (entry == null) return null;
        if (!entry.loaded(world) || entry.valid(world)) return entry;
        removeEntry(entry); return null;
    }

    public int size() { return entries.size(); }
    public void migrateChunkOnLoad(World world,Chunk chunk){
        long key=chunkKey(chunk.xPosition,chunk.zPosition);
        if(!migratedChunks.add(key))return;
        List<Entry> section=byChunk.get(key);
        if(section!=null)for(Entry entry:new ArrayList<Entry>(section))if(!entry.valid(world))removeEntry(entry);
        ExtendedBlockStorage[] storage=chunk.getBlockStorageArray();
        for(ExtendedBlockStorage blocks:storage){
            if(blocks==null||blocks.isEmpty())continue;int baseY=blocks.getYLocation();
            for(int y=0;y<16;++y)for(int z=0;z<16;++z)for(int x=0;x<16;++x){
                Block block=blocks.getBlockByExtId(x,y,z);
                if(VillagePoiType120.forBlock(block)!=null)
                    registerIfPoi(world,(chunk.xPosition<<4)+x,baseY+y,(chunk.zPosition<<4)+z);
            }
        }
        markDirty();
    }
    public static net.minecraft.util.ChunkCoordinates findVillageCenter(World world,int x,int y,int z,int radius){
        List<Entry> occupied=get(world).occupiedNear(world,x,y,z,radius);
        if(occupied.isEmpty())return null;
        double sx=0,sy=0,sz=0;for(Entry e:occupied){sx+=e.x;sy+=e.y;sz+=e.z;}
        return new net.minecraft.util.ChunkCoordinates((int)Math.floor(sx/occupied.size()),(int)Math.floor(sy/occupied.size()),(int)Math.floor(sz/occupied.size()));
    }

    public List<Entry> occupiedNear(World world,double x,double y,double z,int radius) {
        List<Entry> result=new ArrayList<Entry>();
        int cx=((int)Math.floor(x))>>4,cz=((int)Math.floor(z))>>4,r=(radius+15)>>4;
        for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++) {
            List<Entry> section=byChunk.get(chunkKey(cx+dx,cz+dz));if(section==null)continue;
            for(Entry e:section) {
                if(e.owner.isEmpty() || !e.loaded(world) || !e.valid(world))continue;
                double ax=e.x-x,ay=e.y-y,az=e.z-z;
                if(ax*ax+ay*ay+az*az<=(double)radius*radius)result.add(e);
            }
        }
        return result;
    }

    /** Meeting places have shared tickets; keep these separate from HOME/JOB ownership. */
    public void refreshMeetingTickets(World world) {
        for(Entry entry:entries) {
            if(!"meeting".equals(entry.type)||!entry.loaded(world)||!entry.valid(world))continue;
            String owner="";double nearest=48D*48D;
            for(Object value:world.loadedEntityList)if(value instanceof net.minecraft.entity.passive.EntityVillager
                    && !(value instanceof com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWanderingTrader120)) {
                net.minecraft.entity.passive.EntityVillager villager=(net.minecraft.entity.passive.EntityVillager)value;
                double d=villager.getDistanceSq(entry.x,entry.y,entry.z);
                if(villager.isEntityAlive()&&d<nearest){owner=villager.getUniqueID().toString();nearest=d;}
            }
            if(!owner.equals(entry.owner)){entry.owner=owner;markDirty();}
        }
    }

    private Entry findNearest(World world,String type,double x,double y,double z,int radius){
        Entry nearest=null;double distance=(double)radius*radius;int chunkRadius=(radius+15)>>4;
        int centerX=((int)Math.floor(x))>>4,centerZ=((int)Math.floor(z))>>4;
        for(int chunkX=centerX-chunkRadius;chunkX<=centerX+chunkRadius;++chunkX)
            for(int chunkZ=centerZ-chunkRadius;chunkZ<=centerZ+chunkRadius;++chunkZ){
                List<Entry> section=byChunk.get(chunkKey(chunkX,chunkZ));if(section==null)continue;
                for(Entry entry:section){if(!type.equals(entry.type)||!entry.loaded(world)||!entry.valid(world))continue;
                    double dx=entry.x+.5D-x,dy=entry.y+.5D-y,dz=entry.z+.5D-z,candidate=dx*dx+dy*dy+dz*dz;
                    if(candidate<distance){distance=candidate;nearest=entry;}}
            }
        return nearest;
    }
    public void releaseOwner(String owner) {
        boolean changed = releaseOwner(owner, "job");
        changed |= releaseOwner(owner, "home");
        for(Entry entry:entries)if("meeting".equals(entry.type)&&owner.equals(entry.owner)){entry.owner="";changed=true;}
        if (changed) markDirty();
    }

    public void releaseHome(String owner) {
        if (releaseOwner(owner, "home")) markDirty();
    }

    public void releaseJob(String owner) {
        if (releaseOwner(owner, "job")) markDirty();
    }

    private boolean releaseOwner(String owner, String slot) {
        Entry entry = byOwner.remove(ownerKey(owner, slot));
        if (entry == null) return false;
        entry.owner = "";
        return true;
    }

    static void assertRoundTripFixture() {
        VillagePoiData120 source = new VillagePoiData120();
        source.entries.add(new Entry(12, 64, -7, "librarian", "fixture-owner"));
        source.entries.add(new Entry(13, 64, -7, "home", "fixture-owner"));
        source.migratedChunks.add(chunkKey(-4, 9));
        NBTTagCompound tag = new NBTTagCompound();
        source.writeToNBT(tag);
        VillagePoiData120 copy = new VillagePoiData120();
        copy.readFromNBT(tag);
        if (copy.entries.size() != 2 || !"fixture-owner".equals(copy.entries.get(0).owner)
                || !"home".equals(copy.entries.get(1).type)
                || copy.byOwner.get(ownerKey("fixture-owner", "job")) != copy.entries.get(0)
                || copy.byOwner.get(ownerKey("fixture-owner", "home")) != copy.entries.get(1)
                || copy.at(12, 64, -7) != copy.entries.get(0)
                || !copy.migratedChunks.contains(chunkKey(-4, 9)))
            throw new IllegalStateException("POI persistent round-trip lost type or owner");
        copy.releaseHome("fixture-owner");
        if (copy.byOwner.get(ownerKey("fixture-owner", "job")) != copy.entries.get(0)
                || copy.byOwner.containsKey(ownerKey("fixture-owner", "home")))
            throw new IllegalStateException("releasing HOME also released JOB_SITE");
        copy.releaseJob("fixture-owner");
        if (copy.byOwner.containsKey(ownerKey("fixture-owner", "job")))
            throw new IllegalStateException("JOB_SITE ticket was not released independently");
    }
    private Entry at(int x, int y, int z) {
        List<Entry> section = byChunk.get(chunkKey(x >> 4, z >> 4));
        if (section != null) for (Entry entry : section)
            if (entry.x == x && entry.y == y && entry.z == z) return entry;
        return null;
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        entries.clear();
        byChunk.clear(); byOwner.clear(); migratedChunks.clear();
        NBTTagList list = tag.getTagList("Entries", 10);
        for (int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound value = list.getCompoundTagAt(i);
            String type = value.getString("Type");
            if (VillagePoiType120.isPoi(type)) {
                Entry entry = new Entry(value.getInteger("X"), value.getInteger("Y"),
                        value.getInteger("Z"), type, value.getString("Owner"));
                entries.add(entry); index(entry);
            }
        }
        NBTTagList indexed = tag.getTagList("IndexedChunks", 10);
        for (int i = 0; i < indexed.tagCount(); ++i)
            migratedChunks.add(indexed.getCompoundTagAt(i).getLong("Key"));
    }

    @Override public void writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (Entry entry : entries) {
            NBTTagCompound value = new NBTTagCompound();
            value.setInteger("X", entry.x); value.setInteger("Y", entry.y); value.setInteger("Z", entry.z);
            value.setString("Type", entry.type); value.setString("Owner", entry.owner);
            list.appendTag(value);
        }
        tag.setTag("Entries", list);
        NBTTagList indexed = new NBTTagList();
        for (Long key : migratedChunks) {
            NBTTagCompound value = new NBTTagCompound();
            value.setLong("Key", key.longValue());
            indexed.appendTag(value);
        }
        tag.setTag("IndexedChunks", indexed);
    }

    public static final class Entry {
        public final int x, y, z;
        public String type, owner;
        public Entry(int x, int y, int z, String type, String owner) {
            this.x = x; this.y = y; this.z = z; this.type = type; this.owner = owner;
        }
        boolean valid(World world) {
            Block block = world.getBlock(x, y, z);
            return type.equals(VillagePoiType120.forBlock(block))
                    && (!"home".equals(type) || VillagePoiType120.isHomeHead(world, x, y, z));
        }
        public boolean loaded(World world) { return world.getChunkProvider().chunkExists(x >> 4, z >> 4); }
    }

    private void index(Entry entry) {
        long key = chunkKey(entry.x >> 4, entry.z >> 4);
        List<Entry> section = byChunk.get(key);
        if (section == null) { section = new ArrayList<Entry>(); byChunk.put(key, section); }
        section.add(entry);
        if (entry.owner.length() != 0 && !"meeting".equals(entry.type)) byOwner.put(ownerKey(entry.owner, entry.type), entry);
    }

    private void unindex(Entry entry) {
        List<Entry> section = byChunk.get(chunkKey(entry.x >> 4, entry.z >> 4));
        if (section != null) { section.remove(entry); if (section.isEmpty()) byChunk.remove(chunkKey(entry.x >> 4, entry.z >> 4)); }
        if (entry.owner.length() != 0 && !"meeting".equals(entry.type)) byOwner.remove(ownerKey(entry.owner, entry.type));
    }

    private void removeEntry(Entry entry) {
        if (entries.remove(entry)) { unindex(entry); markDirty(); }
    }

    private static long chunkKey(int x, int z) { return ((long)x << 32) ^ (z & 0xffffffffL); }
    private static String ownerKey(String owner, String type) {
        return owner + ("home".equals(type) ? "|home" : "|job");
    }
}
