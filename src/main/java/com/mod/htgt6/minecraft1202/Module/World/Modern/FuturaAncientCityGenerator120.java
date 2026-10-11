package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulArmorStand;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityAllay120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityCamel120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.TileEntityBountifulBanner;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatZombieVillager;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.VillagePoiData120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112;
import com.mod.htgt6.common.util.StructurePlacement120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.MutableDensityContext120;
import com.mod.htgt6.minecraft1202.Module.Data.Modern.StructureProcessorRuntime120;
import com.mod.htgt6.minecraft1202.Module.Data.Modern.TrailRuinsProcessor120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Brushable;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Candle;
import com.mod.htgt6.minecraft1202.Module.Data.Modern.AncientCityJigsawPlan120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.JigsawPlacementRuntime120;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Chunk-clipped Ancient City assembler for Futura.
 *
 * <p>The source assets are the 58 unmodified 1.20.2 structure templates. Vanilla's
 * full jigsaw engine does not exist in 1.7.10, so a deterministic bounded layout
 * selects pieces from the same pools. Every population call writes only inside its
 * own chunk; a city therefore never recursively provides or populates neighbours.</p>
 */
public final class FuturaAncientCityGenerator120 {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-AncientCity120");
    private static final int START_Y = 37; // modern absolute -27 mapped by Futura (+64)
    private static final int MAX_RADIUS_CHUNKS = 8; // source max_distance_from_center=116
    private static final Map<String, Template> CACHE = new HashMap<String, Template>();
    private static final int LAYOUT_CACHE_SIZE=64;
    private static final Map<LayoutKey,JigsawPlacementRuntime120.Result> LAYOUT_CACHE=
            new LinkedHashMap<LayoutKey,JigsawPlacementRuntime120.Result>(LAYOUT_CACHE_SIZE+1,.75F,true){
                @Override protected boolean removeEldestEntry(Map.Entry<LayoutKey,JigsawPlacementRuntime120.Result> entry){
                    return size()>LAYOUT_CACHE_SIZE;
                }
            };
    private static final Set<String> OPTIONAL_DECOR = new HashSet<String>(Arrays.asList(
            "campfire", "soul_lantern",
            "lectern", "target"));
    private static final String[] ALL_TEMPLATES = {
            "city/entrance/entrance_connector","city/entrance/entrance_path_1","city/entrance/entrance_path_2",
            "city/entrance/entrance_path_3","city/entrance/entrance_path_4","city/entrance/entrance_path_5",
            "city_center/city_center_1","city_center/city_center_2","city_center/city_center_3",
            "city_center/walls/bottom_1","city_center/walls/bottom_2","city_center/walls/bottom_left_corner",
            "city_center/walls/bottom_right_corner","city_center/walls/bottom_right_corner_1","city_center/walls/bottom_right_corner_2","city_center/walls/left",
            "city_center/walls/right","city_center/walls/top","city_center/walls/top_left_corner",
            "city_center/walls/top_right_corner","structures/barracks","structures/camp_1","structures/camp_2",
            "structures/camp_3","structures/chamber_1","structures/chamber_2","structures/chamber_3",
            "structures/ice_box_1","structures/large_pillar_1","structures/large_ruin_1",
            "structures/medium_pillar_1","structures/medium_ruin_1","structures/medium_ruin_2",
            "structures/sauna_1","structures/small_ruin_1","structures/small_ruin_2","structures/small_statue",
            "structures/tall_ruin_1","structures/tall_ruin_2","structures/tall_ruin_3","structures/tall_ruin_4",
            "walls/intact_corner_wall_1","walls/intact_horizontal_wall_1","walls/intact_horizontal_wall_2",
            "walls/intact_horizontal_wall_bridge","walls/intact_horizontal_wall_passage_1",
            "walls/intact_horizontal_wall_stairs_1","walls/intact_horizontal_wall_stairs_2",
            "walls/intact_horizontal_wall_stairs_3","walls/intact_horizontal_wall_stairs_4",
            "walls/intact_intersection_wall_1","walls/intact_lshape_wall_1","walls/ruined_corner_wall_1",
            "walls/ruined_corner_wall_2","walls/ruined_horizontal_wall_stairs_1",
            "walls/ruined_horizontal_wall_stairs_2","walls/ruined_horizontal_wall_stairs_3",
            "walls/ruined_horizontal_wall_stairs_4"
    };
    private static Field tagListField;
    private static boolean validated;
    private static final Set<String> WARNED_ENTITY_GAPS=new HashSet<String>();
    private static final Map<String,Block> VANILLA_BLOCK_CACHE=new HashMap<String,Block>();
    private static final Set<String> MISSING_VANILLA_BLOCKS=new HashSet<String>();
    private static boolean vanillaBlockCacheIndexed;

    public static synchronized void validateTemplates() {
        if (validated) return;
        Set<String> unsupported = new HashSet<String>();
        Set<String> missingOptional = new HashSet<String>();
        for (String name : ALL_TEMPLATES) {
            Template template = load(name);
            for (State state : template.palette) {
                if (map(state, 0) == null) {
                    String path = path(state.name);
                    if (OPTIONAL_DECOR.contains(path)) missingOptional.add(path);
                    else if (!"jigsaw".equals(path) && !"structure_void".equals(path)) unsupported.add(path);
                }
            }
        }
        if (!unsupported.isEmpty()) throw new IllegalStateException(
                "Ancient City contains unmapped structural blocks: " + unsupported);
        validated = true;
        LOGGER.info("Validated all {} official Ancient City templates; optional unavailable decor={}",
                ALL_TEMPLATES.length, missingOptional);
    }

    public static void populateChunk(World world,int chunkX,int chunkZ,FuturaMountainDensityRouter120 terrain) {
        MutableDensityContext120 surfaceSample=new MutableDensityContext120();
        int regionX = StructurePlacement120.floorDiv(chunkX, StructurePlacement120.ANCIENT_CITY.spacing);
        int regionZ = StructurePlacement120.floorDiv(chunkZ, StructurePlacement120.ANCIENT_CITY.spacing);
        for (int rx = regionX - 1; rx <= regionX + 1; rx++) for (int rz = regionZ - 1; rz <= regionZ + 1; rz++) {
            Candidate candidate = candidate(world.getSeed(), rx, rz);
            if (Math.abs(chunkX - candidate.chunkX) > MAX_RADIUS_CHUNKS
                    || Math.abs(chunkZ - candidate.chunkZ) > MAX_RADIUS_CHUNKS) continue;
            int centerX = (candidate.chunkX << 4) + 8, centerZ = (candidate.chunkZ << 4) + 8;
            if (!(world.getWorldChunkManager() instanceof FuturaWorldChunkManager120)) continue;
            FuturaWorldChunkManager120 manager=(FuturaWorldChunkManager120)world.getWorldChunkManager();
            if(manager.getUndergroundBiomeAt(centerX,START_Y,centerZ,terrain,surfaceSample).biomeID
                    !=FuturaBiomeIds120.DEEP_DARK)continue;
            if(chunkX==candidate.chunkX&&chunkZ==candidate.chunkZ)
                LOGGER.info("Generating Minecraft 1.20.2 backport Ancient City start at {},{},{} (chunk {},{})",
                        centerX,START_Y,centerZ,candidate.chunkX,candidate.chunkZ);
            placeOfficialPlan(world,chunkX,chunkZ,centerX,centerZ);
        }
    }

    private static void placeOfficialPlan(World world,int chunkX,int chunkZ,int centerX,int centerZ){
        JigsawPlacementRuntime120.Result layout=layout(world.getSeed(),centerX,centerZ);
        for(JigsawPlacementRuntime120.Piece piece:layout.pieces)
            placeOfficialElement(world,piece.element,piece.x,piece.y,piece.z,piece.rotation,chunkX,chunkZ);
    }
    private static final class OfficialPlanHolder {static final AncientCityJigsawPlan120 INSTANCE=
            new AncientCityJigsawPlan120();}

    public static synchronized JigsawPlacementRuntime120.Result layout(long worldSeed,int centerX,int centerZ){
        LayoutKey key=new LayoutKey(worldSeed,centerX,centerZ);
        JigsawPlacementRuntime120.Result cached=LAYOUT_CACHE.get(key);
        if(cached!=null)return cached;
        AncientCityJigsawPlan120 plan=OfficialPlanHolder.INSTANCE;
        long layoutSeed=mix(worldSeed^(long)centerX*31L^centerZ);
        JigsawPlacementRuntime120.Result generated=JigsawPlacementRuntime120.generateLegacy(plan,
                new JigsawPlacementRuntime120.Terrain(){public int surfaceY(int x,int z){return START_Y;}},
                plan.startPool,layoutSeed,centerX,START_Y,centerZ,plan.size,
                plan.maxDistanceFromCenter,plan.startJigsawName,false);
        LAYOUT_CACHE.put(key,generated);
        return generated;
    }

    public static synchronized int cachedLayoutCount(){return LAYOUT_CACHE.size();}

    private static final class LayoutKey{
        final long seed;final int x,z;
        LayoutKey(long seed,int x,int z){this.seed=seed;this.x=x;this.z=z;}
        @Override public int hashCode(){long h=seed^(seed>>>32);return (int)h*31*31+x*31+z;}
        @Override public boolean equals(Object value){if(!(value instanceof LayoutKey))return false;
            LayoutKey key=(LayoutKey)value;return seed==key.seed&&x==key.x&&z==key.z;}
    }

    private static void placeOfficialElement(World world,JigsawPlacementRuntime120.Element element,
                                             int x,int y,int z,int rotation,int chunkX,int chunkZ){
        switch(element.kind){
            case EMPTY:return;
            case FEATURE:
                if("minecraft:sculk_patch_ancient_city".equals(element.feature)
                        &&(x>>4)==chunkX&&(z>>4)==chunkZ)
                    FuturaDeepDarkRuntime120.generateAncientCity(world,x,y,z);
                return;
            case LIST:
                for(JigsawPlacementRuntime120.Element child:element.children)
                    placeOfficialElement(world,child,x,y,z,rotation,chunkX,chunkZ);
                return;
            default:
                String id=element.template;int split=id.indexOf(':');String path=split<0?id:id.substring(split+1);
                if(!path.startsWith("ancient_city/"))return;
                String resource="/assets/htgt6/structures/ancient_city/"+path.substring("ancient_city/".length())+".nbt";
                placeTemplateAtOrigin(world,resource,x,y,z,rotation,chunkX,chunkZ,
                        element.processors,element.ignoreTemplateAir);
        }
    }

    public static boolean usesOfficialJigsawProduction(){return true;}
    public static boolean supportsOfficialFeature(String feature){
        return "minecraft:sculk_patch_ancient_city".equals(feature);
    }

    private static Candidate candidate(long seed, int regionX, int regionZ) {
        int[] point=StructurePlacement120.candidate(seed,regionX,regionZ,StructurePlacement120.ANCIENT_CITY);
        return new Candidate(point[0],point[1]);
    }

    private static final class Piece {
        final String name; final int centerX,y,centerZ,rotation; final boolean absoluteResource;
        Piece(String name,int x,int y,int z,int rotation){this(name,x,y,z,rotation,false);}
        Piece(String name,int x,int y,int z,int rotation,boolean absoluteResource){this.name=name;centerX=x;this.y=y;centerZ=z;this.rotation=rotation;this.absoluteResource=absoluteResource;}
        void place(World world,int chunkX,int chunkZ) {
            Template template=absoluteResource?loadResource(name):load(name);
            int minChunkX=chunkX<<4,minChunkZ=chunkZ<<4,maxChunkX=minChunkX+15,maxChunkZ=minChunkZ+15;
            int extentX=(rotation&1)==0?template.sizeX:template.sizeZ;
            int extentZ=(rotation&1)==0?template.sizeZ:template.sizeX;
            if(centerX+extentX/2<minChunkX||centerX-extentX/2-1>maxChunkX
                    ||centerZ+extentZ/2<minChunkZ||centerZ-extentZ/2-1>maxChunkZ)return;
            for(BlockEntry entry:template.blocks) {
                if(entry.state<0||entry.state>=template.palette.length)continue;
                State state=template.palette[entry.state];
                int[] point=rotatePoint(entry.x-template.sizeX/2,entry.z-template.sizeZ/2,rotation);
                int wx=centerX+point[0],wy=y+entry.y,wz=centerZ+point[1];
                if(wx<minChunkX||wx>maxChunkX||wz<minChunkZ||wz>maxChunkZ||wy<1||wy>=world.getActualHeight())continue;
                Placed placed;
                if("jigsaw".equals(path(state.name))) placed=mapFinalState(entry.finalState,rotation);
                else placed=map(state,rotation);
                if(placed==null)continue;
                world.setBlock(wx,wy,wz,placed.block,placed.meta,2);
                VillagePoiData120.registerIfPoi(world,wx,wy,wz);
                applyPlacedData(world,placed,wx,wy,wz);
                boolean lootApplied=applyBlockEntityData(world,entry.blockData,wx,wy,wz);
                if(placed.block==Blocks.chest&&!lootApplied)fillChest(world,wx,wy,wz);
            }
        }
    }

    private static void fillChest(World world,int x,int y,int z) {
        TileEntity tile=world.getTileEntity(x,y,z);if(!(tile instanceof IInventory))return;
        IInventory inventory=(IInventory)tile;
        for(int slot=0;slot<inventory.getSizeInventory();slot++)if(inventory.getStackInSlot(slot)!=null)return;
        Random random=new Random(mix(world.getSeed()^(long)x*341873128712L^(long)y*42317861L^(long)z*132897987541L));
        ItemStack[] loot={new ItemStack(Backport120Content.echoShard,1+random.nextInt(3)),
                new ItemStack(Backport120Content.discFragment5,1+random.nextInt(2)),
                new ItemStack(Items.diamond,1+random.nextInt(2)),new ItemStack(Items.gold_ingot,2+random.nextInt(5)),
                new ItemStack(Items.iron_ingot,3+random.nextInt(7)),new ItemStack(Items.book,1+random.nextInt(3))};
        int count=3+random.nextInt(3);for(int i=0;i<count;i++)inventory.setInventorySlotContents(
                 random.nextInt(inventory.getSizeInventory()),loot[random.nextInt(loot.length)].copy());
    }

    /** Applies portable tile data and eagerly resolves modern village loot tables for 1.7 chests. */
    private static boolean applyBlockEntityData(World world,NBTTagCompound source,int x,int y,int z){
        if(source==null)return false;
        String lootTable=source.getString("LootTable");
        TileEntity tile=world.getTileEntity(x,y,z);
        if(tile!=null){
            NBTTagCompound data=(NBTTagCompound)source.copy();
            data.setInteger("x",x);data.setInteger("y",y);data.setInteger("z",z);
            data.removeTag("LootTable");data.removeTag("LootTableSeed");
            tile.readFromNBT(data);tile.markDirty();
        }
        int villageProfile=villageLootProfile(lootTable);
        boolean outpost="minecraft:chests/pillager_outpost".equals(lootTable);
        boolean ancient="minecraft:chests/ancient_city".equals(lootTable)
                ||"minecraft:chests/ancient_city_ice_box".equals(lootTable);
        if(!(tile instanceof IInventory)||(villageProfile==0&&!outpost&&!ancient))return false;
        IInventory inventory=(IInventory)tile;
        for(int slot=0;slot<inventory.getSizeInventory();slot++)if(inventory.getStackInSlot(slot)!=null)return true;
        long tableSeed=source.hasKey("LootTableSeed",99)?source.getLong("LootTableSeed"):0L;
        Random random=new Random(tableSeed!=0L?tableSeed:mix(world.getSeed()^(long)x*341873128712L
                ^(long)y*42317861L^(long)z*132897987541L^lootTable.hashCode()));
        if(villageProfile!=0||outpost){VillageStructureLoot120.fill(inventory,lootTable,random);tile.markDirty();return true;}
        ItemStack[] choices=ancient?ancientCityLoot():outpost?pillagerOutpostLoot():villageLoot(villageProfile);
        int count=3+random.nextInt(4);
        for(int i=0;i<count;i++){
            ItemStack stack=choices[random.nextInt(choices.length)].copy();
            if(stack.getMaxStackSize()>1)stack.stackSize=1+random.nextInt(Math.min(4,stack.getMaxStackSize()));
            int start=random.nextInt(inventory.getSizeInventory());
            for(int offset=0;offset<inventory.getSizeInventory();offset++){
                int slot=(start+offset)%inventory.getSizeInventory();
                if(inventory.getStackInSlot(slot)==null){inventory.setInventorySlotContents(slot,stack);break;}
            }
        }
        tile.markDirty();return true;
    }

    public static int villageLootProfile(String table){
        if(table==null||!table.startsWith("minecraft:chests/village/"))return 0;
        if(table.endsWith("armorer")||table.endsWith("toolsmith")||table.endsWith("weaponsmith"))return 1;
        if(table.endsWith("temple")||table.endsWith("cartographer")||table.endsWith("mason"))return 2;
        if(table.endsWith("butcher")||table.endsWith("tannery")||table.endsWith("shepherd"))return 3;
        if(table.endsWith("fisher")||table.endsWith("fletcher"))return 4;
        return 5;
    }

    public static boolean supportsStructureLootTable(String table){
        return villageLootProfile(table)!=0||"minecraft:chests/pillager_outpost".equals(table)
                ||"minecraft:chests/ancient_city".equals(table)
                ||"minecraft:chests/ancient_city_ice_box".equals(table);
    }

    public static boolean shouldIgnoreTemplateState(boolean legacySingle,String blockName){
        String value=path(blockName);
        return legacySingle&&("air".equals(value)||"cave_air".equals(value));
    }

    private static ItemStack[] villageLoot(int profile){
        switch(profile){
            case 1:return new ItemStack[]{new ItemStack(Items.iron_ingot),new ItemStack(Items.gold_ingot),
                    new ItemStack(Items.coal),new ItemStack(Items.diamond),new ItemStack(Items.bread)};
            case 2:return new ItemStack[]{new ItemStack(Items.book),new ItemStack(Items.paper),
                    new ItemStack(Items.compass),new ItemStack(Items.emerald),new ItemStack(Items.bread)};
            case 3:return new ItemStack[]{new ItemStack(Items.leather),new ItemStack(Items.wheat),
                    new ItemStack(Items.apple),new ItemStack(Items.emerald),new ItemStack(Items.bread)};
            case 4:return new ItemStack[]{new ItemStack(Items.feather),new ItemStack(Items.arrow),
                    new ItemStack(Items.string),new ItemStack(Items.coal),new ItemStack(Items.bread)};
            default:return new ItemStack[]{new ItemStack(Items.wheat),new ItemStack(Items.bread),
                    new ItemStack(Items.apple),new ItemStack(Items.emerald),new ItemStack(Items.potato)};
        }
    }

    private static ItemStack[] pillagerOutpostLoot(){
        return new ItemStack[]{new ItemStack(Items.arrow),new ItemStack(Items.string),
                new ItemStack(Blocks.tripwire_hook),new ItemStack(Items.iron_ingot),
                new ItemStack(Items.wheat),new ItemStack(Items.carrot),new ItemStack(Items.potato),
                new ItemStack(Items.experience_bottle),new ItemStack(Items.emerald)};
    }

    private static ItemStack[] ancientCityLoot(){
        return new ItemStack[]{new ItemStack(Backport120Content.echoShard),
                new ItemStack(Backport120Content.discFragment5),new ItemStack(Items.book),
                new ItemStack(Items.diamond),new ItemStack(Items.diamond_hoe),new ItemStack(Items.diamond_leggings),
                new ItemStack(Items.golden_apple),new ItemStack(Items.gold_ingot),new ItemStack(Items.name_tag),
                new ItemStack(Items.saddle),new ItemStack(Items.lead),new ItemStack(Items.bone),
                new ItemStack(Items.snowball),new ItemStack(Items.potionitem,1,8193)};
    }

    public static long villageBlockEntityFingerprint(){
        List<String> resources=new ArrayList<String>();
        for(String resource:CACHE.keySet())if(resource.contains("/structures/village_1_20/"))resources.add(resource);
        java.util.Collections.sort(resources);long hash=0xcbf29ce484222325L;int dataCount=0,lootCount=0;
        for(String resource:resources){Template template=CACHE.get(resource);for(BlockEntry entry:template.blocks){
            if(entry.blockData==null)continue;dataCount++;String table=entry.blockData.getString("LootTable");
            hash=(hash^resource.hashCode())*0x100000001b3L;hash=(hash^entry.x)*0x100000001b3L;
            hash=(hash^entry.y)*0x100000001b3L;hash=(hash^entry.z)*0x100000001b3L;
            if(table.length()>0){lootCount++;if(villageLootProfile(table)==0)throw new IllegalStateException(
                    "Unsupported village loot table "+table+" in "+resource);hash=(hash^table.hashCode())*0x100000001b3L;}
        }}
        if(dataCount==0||lootCount==0)throw new IllegalStateException("Village block-entity NBT was not retained");
        hash=(hash^dataCount)*0x100000001b3L;return(hash^lootCount)*0x100000001b3L;
    }

    private static Placed mapFinalState(String finalState,int rotation) {
        if(finalState==null||finalState.length()==0||"minecraft:structure_void".equals(finalState))return null;
        int bracket=finalState.indexOf('[');String name=bracket<0?finalState:finalState.substring(0,bracket);
        return map(new State(name,new HashMap<String,String>()),rotation);
    }

    private static Placed map(State state,int rotation) {
        String p=path(state.name);
        com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageBuildingContent114.Shape shape=
                com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageBuildingContent114.SHAPES.get(p);
        if(shape!=null){
            if(shape.kind.equals("stairs"))return stair(shape.block,state,rotation);
            if(shape.kind.equals("slab"))return slab(shape.block,shape.doubleSlab,state);
            return placed(shape.block);
        }
        if("smooth_red_sandstone".equals(p))return placed(EtFVanillaGapContent120.smoothRedSandstone);
        if("smooth_quartz".equals(p))return placed(EtFVanillaGapContent120.smoothQuartz);
        if("air".equals(p)||"cave_air".equals(p))return new Placed(Blocks.air,0);
        if("structure_void".equals(p)||"jigsaw".equals(p))return null;
        if("glass".equals(p))return placed(Blocks.glass);
        if("glass_pane".equals(p))return placed(Blocks.glass_pane);
        if("packed_ice".equals(p))return placed(Blocks.packed_ice);
        if("ice".equals(p))return placed(Blocks.ice);
        if("snow".equals(p))return placed(Blocks.snow_layer);
        if("snow_block".equals(p))return placed(Blocks.snow);
        if("dirt".equals(p))return placed(Blocks.dirt);
        if("soul_sand".equals(p))return placed(Blocks.soul_sand);
        if("soul_soil".equals(p))return placed(EtFVanillaGapContent120.soulSoil);
        if("bone_block".equals(p))return placed(com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent.boneBlock);
        if("water".equals(p))return new Placed(Blocks.water,propertyInt(state,"level",0,15,0));
        if("lava".equals(p))return new Placed(Blocks.lava,propertyInt(state,"level",0,15,0));
        if("iron_bars".equals(p))return placed(Blocks.iron_bars);
        if("redstone_block".equals(p))return placed(Blocks.redstone_block);
        if("redstone_lamp".equals(p))return placed(on(state,"lit")?Blocks.lit_redstone_lamp:Blocks.redstone_lamp);
        if("torch".equals(p))return new Placed(Blocks.torch,5);
        if("wall_torch".equals(p))return new Placed(Blocks.torch,wallAttachment(state,rotation));
        if("lever".equals(p))return new Placed(Blocks.lever,leverStateMeta(state,rotation));
        if("grass".equals(p))return new Placed(Blocks.tallgrass,1);
        if("tall_grass".equals(p))return new Placed(Blocks.double_plant,
                "upper".equals(state.get("half"))?10:2);
        if("fern".equals(p))return new Placed(Blocks.tallgrass,2);
        if("sea_pickle".equals(p)){
            int count=parseInt(state.get("pickles"),1);
            return new Placed(OceanContent.marinePlant,8+Math.max(0,Math.min(3,count-1)));
        }
        if("large_fern".equals(p))return new Placed(Blocks.double_plant,
                "upper".equals(state.get("half"))?11:3);
        if("dead_bush".equals(p))return placed(Blocks.deadbush);
        if("cactus".equals(p))return new Placed(Blocks.cactus,propertyInt(state,"age",0,15,0));
        if("sugar_cane".equals(p))return new Placed(Blocks.reeds,propertyInt(state,"age",0,15,0));
        if("cobweb".equals(p))return placed(Blocks.web);
        if("vine".equals(p))return new Placed(Blocks.vine,vineMeta(state,rotation));
        if("bookshelf".equals(p))return placed(Blocks.bookshelf);
        Placed potted=pottedPlant(p);if(potted!=null)return potted;
        if("dandelion".equals(p))return placed(Blocks.yellow_flower);
        if("poppy".equals(p))return new Placed(Blocks.red_flower,0);
        if("pumpkin".equals(p))return placed(Blocks.pumpkin);
        if("carved_pumpkin".equals(p))return new Placed(Blocks.pumpkin,pumpkinMeta(state,rotation));
        if("melon".equals(p))return placed(Blocks.melon_block);
        if("blue_orchid".equals(p))return new Placed(Blocks.red_flower,1);
        if("cornflower".equals(p))return placed(Backport120Content.cornflower);
        if("allium".equals(p))return new Placed(Blocks.red_flower,2);
        if("azure_bluet".equals(p))return new Placed(Blocks.red_flower,3);
        if("lily_of_the_valley".equals(p))return placed(Backport120Content.lilyOfTheValley);
        if("red_tulip".equals(p))return new Placed(Blocks.red_flower,4);
        if("orange_tulip".equals(p))return new Placed(Blocks.red_flower,5);
        if("white_tulip".equals(p))return new Placed(Blocks.red_flower,6);
        if("pink_tulip".equals(p))return new Placed(Blocks.red_flower,7);
        if("oxeye_daisy".equals(p))return new Placed(Blocks.red_flower,8);
        if("oak_leaves".equals(p))return new Placed(Blocks.leaves,0);
        if(p.endsWith("_sapling")){
            int wood=woodVariant(p.substring(0,p.length()-8)+"_log");
            if(wood>=0)return new Placed(Blocks.sapling,wood);
        }
        if("spruce_leaves".equals(p))return new Placed(Blocks.leaves,1);
        if("birch_leaves".equals(p))return new Placed(Blocks.leaves,2);
        if("jungle_leaves".equals(p))return new Placed(Blocks.leaves,3);
        if("acacia_leaves".equals(p))return new Placed(Blocks.leaves2,0);
        if("dark_oak_leaves".equals(p))return new Placed(Blocks.leaves2,1);
        if("terracotta".equals(p))return placed(Blocks.hardened_clay);
        if(p.endsWith("_glazed_terracotta"))return new Placed(
                Backport120Content.glazedTerracotta[colorMeta(
                        p.substring(0,p.length()-"_glazed_terracotta".length()))],
                repeaterMeta(state,rotation));
        if(p.endsWith("_terracotta"))return new Placed(Blocks.stained_hardened_clay,
                colorMeta(p.substring(0,p.length()-11)));
        if("stone_bricks".equals(p))return new Placed(Blocks.stonebrick,0);
        if("mossy_stone_bricks".equals(p))return new Placed(Blocks.stonebrick,1);
        if("cracked_stone_bricks".equals(p))return new Placed(Blocks.stonebrick,2);
        if("chiseled_stone_bricks".equals(p))return new Placed(Blocks.stonebrick,3);
        int modernStone=modernStoneMeta(p);
        if(modernStone>=0)return new Placed(BountifulContent.stone,modernStone);
        for(com.mod.htgt6.minecraft1202.Module.Registry.Modern.ModernStoneShapes120.Family family:
                com.mod.htgt6.minecraft1202.Module.Registry.Modern.ModernStoneShapes120.FAMILIES){
            if(p.equals(family.name+"_stairs"))return stair(family.stairs,state,rotation);
            if(p.equals(family.name+"_slab"))return slab(family.slab,family.doubleSlab,state);
            if(family.wall!=null&&p.equals(family.name+"_wall"))return placed(family.wall);
        }
        if("stone_button".equals(p))return new Placed(Blocks.stone_button,wallAttachment(state,rotation)|(on(state,"powered")?8:0));
        if("barrel".equals(p))return new Placed(VillagePillageContent.barrel,horizontalMeta(state,rotation));
        if("deepslate".equals(p))return placed(Backport120Content.deepslate);
        if("cobbled_deepslate".equals(p))return placed(Backport120Content.cobbledDeepslate);
        if("polished_deepslate".equals(p))return placed(Backport120Content.polishedDeepslate);
        if("chiseled_deepslate".equals(p))return placed(Backport120Content.chiseledDeepslate);
        if("deepslate_bricks".equals(p))return placed(Backport120Content.deepslateBricks);
        if("cracked_deepslate_bricks".equals(p))return placed(Backport120Content.crackedDeepslateBricks);
        if("deepslate_tiles".equals(p))return placed(Backport120Content.deepslateTiles);
        if("cracked_deepslate_tiles".equals(p))return placed(Backport120Content.crackedDeepslateTiles);
        if("reinforced_deepslate".equals(p))return placed(Backport120Content.reinforcedDeepslate);
        if("smooth_basalt".equals(p))return placed(Backport120Content.smoothBasalt);
        if("polished_basalt".equals(p))return new Placed(Backport120Content.polishedBasalt,
                axisMeta(0,state.get("axis"),rotation));
        if("blue_ice".equals(p))return placed(Backport120Content.blueIce);
        if("sculk_sensor".equals(p))return placed(Backport120Content.sculkSensor);
        if("soul_fire".equals(p))return placed(Backport120Content.soulFire);
        if("candle".equals(p)||"white_candle".equals(p))return new Placed(
                "white_candle".equals(p)?Backport120Content.whiteCandle:Backport120Content.candle,
                BlockBackport120Candle.metadata(parseInt(state.get("candles"),1),"true".equals(state.get("lit"))));
        if("cobbled_deepslate_stairs".equals(p))return stair(Backport120Content.cobbledDeepslateStairs,state,rotation);
        if("polished_deepslate_stairs".equals(p))return stair(Backport120Content.polishedDeepslateStairs,state,rotation);
        if("deepslate_brick_stairs".equals(p))return stair(Backport120Content.deepslateBrickStairs,state,rotation);
        if("deepslate_tile_stairs".equals(p))return stair(Backport120Content.deepslateTileStairs,state,rotation);
        if("cobbled_deepslate_slab".equals(p))return slab(Backport120Content.cobbledDeepslateSlab,Backport120Content.doubleCobbledDeepslateSlab,state);
        if("polished_deepslate_slab".equals(p))return slab(Backport120Content.polishedDeepslateSlab,Backport120Content.doublePolishedDeepslateSlab,state);
        if("deepslate_brick_slab".equals(p))return slab(Backport120Content.deepslateBrickSlab,Backport120Content.doubleDeepslateBrickSlab,state);
        if("deepslate_tile_slab".equals(p))return slab(Backport120Content.deepslateTileSlab,Backport120Content.doubleDeepslateTileSlab,state);
        if("cobbled_deepslate_wall".equals(p))return placed(Backport120Content.cobbledDeepslateWall);
        if("polished_deepslate_wall".equals(p))return placed(Backport120Content.polishedDeepslateWall);
        if("deepslate_brick_wall".equals(p))return placed(Backport120Content.deepslateBrickWall);
        if("deepslate_tile_wall".equals(p))return placed(Backport120Content.deepslateTileWall);
        if("grass_block".equals(p))return placed(Blocks.grass);
        if("stone".equals(p))return placed(Blocks.stone);
        if("cobblestone".equals(p))return placed(Blocks.cobblestone);
        if("cobblestone_stairs".equals(p))return stair(Blocks.stone_stairs,state,rotation);
        if("mossy_cobblestone".equals(p))return placed(Blocks.mossy_cobblestone);
        if("mossy_cobblestone_stairs".equals(p))return stair(Backport120Content.mossyCobblestoneStairs,state,rotation);
        if("mossy_cobblestone_slab".equals(p))return slab(Backport120Content.mossyCobblestoneSlab,
                Backport120Content.doubleMossyCobblestoneSlab,state);
        if("gravel".equals(p))return placed(Blocks.gravel);
        if("mud".equals(p))return placed(Backport120Content.mud);
        if("mud_bricks".equals(p))return placed(Backport120Content.mudBricks);
        if("mud_brick_stairs".equals(p))return stair(Backport120Content.mudBrickStairs,state,rotation);
        if("mud_brick_slab".equals(p))return slab(Backport120Content.mudBrickSlab,Backport120Content.doubleMudBrickSlab,state);
        if("mud_brick_wall".equals(p))return placed(Backport120Content.mudBrickWall);
        if("packed_mud".equals(p))return placed(Backport120Content.packedMud);
        if("suspicious_gravel".equals(p))return placed(Backport120Content.suspiciousGravel);
        if("sand".equals(p))return new Placed(Blocks.sand,"red_sand".equals(state.get("type"))?1:0);
        if("clay".equals(p))return placed(Blocks.clay);
        if("bricks".equals(p))return placed(Blocks.brick_block);
        if("brick_stairs".equals(p))return stair(Blocks.brick_stairs,state,rotation);
        if("brick_wall".equals(p))return placed(Blocks.cobblestone_wall);
        if("coal_block".equals(p))return placed(Blocks.coal_block);
        if("damaged_anvil".equals(p))return new Placed(Blocks.anvil,horizontalMeta(state,rotation)|8);
        if("farmland".equals(p))return new Placed(Blocks.farmland,propertyInt(state,"moisture",0,7,0));
        if("wheat".equals(p))return new Placed(Blocks.wheat,Math.max(0,Math.min(7,parseInt(state.get("age"),0))));
        if("carrots".equals(p))return new Placed(Blocks.carrots,Math.max(0,Math.min(7,parseInt(state.get("age"),0))));
        if("potatoes".equals(p))return new Placed(Blocks.potatoes,Math.max(0,Math.min(7,parseInt(state.get("age"),0))));
        if("beetroots".equals(p))return new Placed(CombatContent.beetrootCrop,
                Math.max(0,Math.min(7,parseInt(state.get("age"),0)*2)));
        if("melon_stem".equals(p)||"pumpkin_stem".equals(p))return new Placed(
                "melon_stem".equals(p)?Blocks.melon_stem:Blocks.pumpkin_stem,
                Math.max(0,Math.min(7,parseInt(state.get("age"),0))));
        if("dirt_path".equals(p))return placed(EtFVanillaGapContent120.dirtPath);
        if("coarse_dirt".equals(p))return placed(BountifulContent.coarseDirt);
        if("smooth_stone".equals(p))return placed(EtFVanillaGapContent120.smoothStone);
        if("smooth_stone_slab".equals(p)||"stone_slab".equals(p))
            return new Placed("double".equals(state.get("type"))?Blocks.double_stone_slab:Blocks.stone_slab,
                    "top".equals(state.get("type"))?8:0);
        int legacySlab=legacyStoneSlabMeta(p);
        if(legacySlab>=0)return new Placed("double".equals(state.get("type"))?Blocks.double_stone_slab:
                Blocks.stone_slab,legacySlab|("top".equals(state.get("type"))?8:0));
        if("sandstone".equals(p))return new Placed(Blocks.sandstone,0);
        if("chiseled_sandstone".equals(p))return new Placed(Blocks.sandstone,1);
        if("cut_sandstone".equals(p))return new Placed(Blocks.sandstone,2);
        if("smooth_sandstone".equals(p))return placed(EtFVanillaGapContent120.smoothSandstone);
        if("smooth_sandstone_stairs".equals(p))return stair(EtFVanillaGapContent120.smoothSandstoneStairs,state,rotation);
        if("smooth_sandstone_slab".equals(p))return slab(EtFVanillaGapContent120.smoothSandstoneSlab,EtFVanillaGapContent120.doubleSmoothSandstoneSlab,state);
        if("sandstone_stairs".equals(p))
            return stair(Blocks.sandstone_stairs,state,rotation);
        if("sandstone_slab".equals(p))return new Placed(
                "double".equals(state.get("type"))?Blocks.double_stone_slab:Blocks.stone_slab,
                1|("top".equals(state.get("type"))?8:0));
        if("red_sandstone".equals(p))return new Placed(BountifulContent.redSandstone,0);
        if("chiseled_red_sandstone".equals(p))return new Placed(BountifulContent.redSandstone,1);
        if("cut_red_sandstone".equals(p)||"smooth_red_sandstone".equals(p))
            return new Placed(BountifulContent.redSandstone,2);
        if("red_sandstone_stairs".equals(p)||"smooth_red_sandstone_stairs".equals(p))
            return stair(BountifulContent.redSandstoneStairs,state,rotation);
        if("red_sandstone_slab".equals(p)||"smooth_red_sandstone_slab".equals(p))
            return new Placed("double".equals(state.get("type"))?BountifulContent.doubleRedSandstoneSlab:
                    BountifulContent.redSandstoneSlab,"top".equals(state.get("type"))?8:0);
        Placed stripped = strippedWood(p,state,rotation);
        if(stripped!=null)return stripped;
        int wood=woodVariant(p);
        if(wood>=0&&p.endsWith("_planks"))return new Placed(Blocks.planks,wood);
        if(wood>=0&&(p.endsWith("_log")||p.endsWith("_wood")))return woodLog(wood,state,rotation);
        if(wood>=0&&p.endsWith("_stairs"))return stair(woodStairs(wood),state,rotation);
        if(wood>=0&&p.endsWith("_slab"))return new Placed(
                "double".equals(state.get("type"))?Blocks.double_wooden_slab:Blocks.wooden_slab,
                wood|("top".equals(state.get("type"))?8:0));
        if(wood>=0&&p.endsWith("_fence"))return placed(wood==0?Blocks.fence:BountifulContent.woodFences[wood-1]);
        if(wood>=0&&p.endsWith("_fence_gate"))return new Placed(
                wood==0?Blocks.fence_gate:BountifulContent.woodFenceGates[wood-1],
                pumpkinMeta(state,rotation)|(on(state,"open")?4:0));
        if(wood>=0&&p.endsWith("_door"))return new Placed(
                wood==0?Blocks.wooden_door:BountifulContent.woodDoors[wood-1],doorMeta(state,rotation));
        if(wood>=0&&p.endsWith("_trapdoor"))return new Placed(Blocks.trapdoor,trapdoorMeta(state,rotation));
        if(wood>=0&&p.endsWith("_wall_sign"))return new Placed(wood==0?Blocks.wall_sign:
                com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageSigns114.WALL[wood-1],
                chestMeta(rotateFacing(state.get("facing"),rotation)));
        if(wood>=0&&p.endsWith("_sign"))return new Placed(wood==0?Blocks.standing_sign:
                com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageSigns114.STANDING[wood-1],
                (parseInt(state.get("rotation"),0)+rotation*4)&15);
        if(p.endsWith("_bed")||"bed".equals(p))return new Placed(VillagePillageContent.coloredBed(
                p.endsWith("_bed")?p.substring(0,p.length()-4):"red"),bedMeta(state,rotation));
        if("lantern".equals(p))return placed(VillagePillageContent.lantern);
        if("soul_lantern".equals(p))return placed(EtFVanillaGapContent120.soulLantern);
        if("bell".equals(p))return new Placed(VillagePillageContent.bell,bellMeta(state,rotation));
        if("grindstone".equals(p))return new Placed(VillagePillageContent.grindstone,grindstoneMeta(state,rotation));
        if("blast_furnace".equals(p))return new Placed(VillagePillageContent.blastFurnace,horizontalMeta(state,rotation));
        if("smoker".equals(p))return new Placed(VillagePillageContent.smoker,horizontalMeta(state,rotation));
        if("composter".equals(p))return new Placed(VillagePillageContent.composter,propertyInt(state,"level",0,8,0));
        if("cauldron".equals(p))return new Placed(Blocks.cauldron,0);
        if("water_cauldron".equals(p))return new Placed(Blocks.cauldron,
                Math.max(1,Math.min(3,parseInt(state.get("level"),3))));
        if("lectern".equals(p))return new Placed(VillagePillageContent.lectern,horizontalMeta(state,rotation));
        if("cartography_table".equals(p))return new Placed(VillagePillageContent.cartographyTable,horizontalMeta(state,rotation));
        if("fletching_table".equals(p))return new Placed(VillagePillageContent.fletchingTable,horizontalMeta(state,rotation));
        if("smithing_table".equals(p))return new Placed(VillagePillageContent.smithingTable,horizontalMeta(state,rotation));
        if("loom".equals(p))return new Placed(VillagePillageContent.loom,horizontalMeta(state,rotation));
        if("stonecutter".equals(p))return new Placed(VillagePillageContent.stonecutter,horizontalMeta(state,rotation));
        if("target".equals(p))return placed(EtFVanillaGapContent120.target);
        if("crafting_table".equals(p))return placed(Blocks.crafting_table);
        if("campfire".equals(p))return new Placed(VillagePillageContent.campfire,
                pumpkinMeta(state,rotation)|(on(state,"waterlogged")?8:on(state,"lit")?4:0));
        if("cobblestone_wall".equals(p))return placed(Blocks.cobblestone_wall);
        if("mossy_cobblestone_wall".equals(p))return new Placed(Blocks.cobblestone_wall,1);
        if("sandstone_wall".equals(p)||"red_sandstone_wall".equals(p))return placed(Blocks.cobblestone_wall);
        if("hay_block".equals(p))return new Placed(Blocks.hay_block,axisMeta(0,state.get("axis"),rotation));
        if("dark_oak_planks".equals(p))return new Placed(Blocks.planks,5);
        if("dark_oak_log".equals(p))return new Placed(Blocks.log2,axisMeta(1,state.get("axis"),rotation));
        if("dark_oak_fence".equals(p))return placed(BountifulContent.woodFences[4]);
        if(p.endsWith("_wool"))return new Placed(Blocks.wool,colorMeta(p.substring(0,p.length()-5)));
        if(p.endsWith("_carpet"))return new Placed(Blocks.carpet,colorMeta(p.substring(0,p.length()-7)));
        if(p.endsWith("_stained_glass_pane"))return new Placed(Blocks.stained_glass_pane,
                colorMeta(p.substring(0,p.length()-19)));
        if(p.endsWith("_stained_glass"))return new Placed(Blocks.stained_glass,
                colorMeta(p.substring(0,p.length()-14)));
        if(p.endsWith("_wall_banner"))return new Placed(BountifulContent.wallBanner,
                chestMeta(rotateFacing(state.get("facing"),rotation)),bannerBase(p.substring(0,p.length()-12)));
        if(p.endsWith("_banner")){
            int bannerRotation=0;try{bannerRotation=Integer.parseInt(state.get("rotation"));}
            catch(NumberFormatException ignored){}
            return new Placed(BountifulContent.standingBanner,(bannerRotation+rotation*4)&15,
                    bannerBase(p.substring(0,p.length()-7)));
        }
        if("chest".equals(p))return new Placed(Blocks.chest,chestMeta(rotateFacing(state.get("facing"),rotation)));
        if("ladder".equals(p))return new Placed(Blocks.ladder,chestMeta(rotateFacing(state.get("facing"),rotation)));
        if("furnace".equals(p))return new Placed(on(state,"lit")?Blocks.lit_furnace:Blocks.furnace,chestMeta(rotateFacing(state.get("facing"),rotation)));
        if("brewing_stand".equals(p))return placed(Blocks.brewing_stand);
        if("iron_trapdoor".equals(p))return new Placed(BountifulContent.ironTrapdoor,trapdoorMeta(state,rotation));
        if("skeleton_skull".equals(p))return new Placed(Blocks.skull,0);
        if("note_block".equals(p))return placed(Blocks.noteblock);
        if("redstone_wire".equals(p))return new Placed(Blocks.redstone_wire,propertyInt(state,"power",0,15,0));
        if("redstone_wall_torch".equals(p)||"redstone_torch".equals(p))return new Placed(
                "false".equals(state.get("lit"))?Blocks.unlit_redstone_torch:Blocks.redstone_torch,
                "redstone_wall_torch".equals(p)?wallAttachment(state,rotation):5);
        if("repeater".equals(p))return new Placed(on(state,"powered")?Blocks.powered_repeater:Blocks.unpowered_repeater,
                pumpkinMeta(state,rotation)|((propertyInt(state,"delay",1,4,1)-1)<<2));
        if("comparator".equals(p))return new Placed(Blocks.unpowered_comparator,pumpkinMeta(state,rotation)
                |("subtract".equals(state.get("mode"))?4:0)|(on(state,"powered")?8:0));
        if("sticky_piston".equals(p)||"piston".equals(p))return new Placed("sticky_piston".equals(p)?Blocks.sticky_piston:Blocks.piston,
                pistonMeta(rotateFacing(state.get("facing"),rotation))|(on(state,"extended")?8:0));
        if("piston_head".equals(p))return new Placed(Blocks.piston_head,pistonMeta(rotateFacing(state.get("facing"),rotation))|("sticky".equals(state.get("type"))?8:0));
        if("stone_pressure_plate".equals(p))return new Placed(Blocks.stone_pressure_plate,on(state,"powered")?1:0);
        if("light_weighted_pressure_plate".equals(p)||"heavy_weighted_pressure_plate".equals(p))return new Placed(
                "light_weighted_pressure_plate".equals(p)?Blocks.light_weighted_pressure_plate:Blocks.heavy_weighted_pressure_plate,propertyInt(state,"power",0,15,0));
        if(p.endsWith("_pressure_plate"))return new Placed(Blocks.wooden_pressure_plate,on(state,"powered")?1:0);
        if(p.endsWith("_button"))return new Placed(Blocks.wooden_button,wallAttachment(state,rotation)|(on(state,"powered")?8:0));
        Block vanilla=legacyVanillaBlock(p);
        return vanilla==null?null:placed(vanilla);
    }

    private static synchronized Block legacyVanillaBlock(String path){
        Block cached=VANILLA_BLOCK_CACHE.get(path);if(cached!=null)return cached;
        if(MISSING_VANILLA_BLOCKS.contains(path))return null;
        Block block=null;
        try{Object value=Blocks.class.getField(path).get(null);if(value instanceof Block)block=(Block)value;}
        catch(ReflectiveOperationException ignored){}
        if(block==null){indexVanillaBlocks();block=VANILLA_BLOCK_CACHE.get(path);}
        if(block==null){Object value=Block.blockRegistry.getObject("minecraft:"+path);
            if(value instanceof Block)block=(Block)value;}
        if(block==null)MISSING_VANILLA_BLOCKS.add(path);else VANILLA_BLOCK_CACHE.put(path,block);
        return block;
    }

    private static void indexVanillaBlocks(){
        if(vanillaBlockCacheIndexed)return;vanillaBlockCacheIndexed=true;
        for(Field field:Blocks.class.getFields())try{
            Object value=field.get(null);if(!(value instanceof Block))continue;
            Block block=(Block)value;String key=block.getUnlocalizedName();
            if(key.startsWith("tile."))key=key.substring(5);
            StringBuilder normalized=new StringBuilder();
            for(int i=0;i<key.length();i++){char c=key.charAt(i);
                if(Character.isUpperCase(c)){normalized.append('_');c=Character.toLowerCase(c);}normalized.append(c);}
            key=normalized.toString();if("crops".equals(key))key="wheat";
            if(!VANILLA_BLOCK_CACHE.containsKey(key))VANILLA_BLOCK_CACHE.put(key,block);
        }catch(IllegalAccessException ignored){}
    }

    private static Placed placed(Block block){return new Placed(block,0);}
    private static Placed stair(Block block,State state,int rotation){int meta=stairMeta(rotateFacing(state.get("facing"),rotation));if("top".equals(state.get("half")))meta|=4;return new Placed(block,meta);}
    private static Placed slab(Block half,Block full,State state){String type=state.get("type");return new Placed("double".equals(type)?full:half,"top".equals(type)?8:0);}
    private static int axisMeta(int base,String axis,int rotation){if("x".equals(axis))return base|((rotation&1)==0?4:8);if("z".equals(axis))return base|((rotation&1)==0?8:4);return base;}
    private static int colorMeta(String color){String[] colors={"white","orange","magenta","light_blue",
            "yellow","lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black"};
        for(int i=0;i<colors.length;i++)if(colors[i].equals(color))return i;return 0;}
    private static int bannerBase(String color){return 15-colorMeta(color);}
    private static int parseInt(String value,int fallback){try{return Integer.parseInt(value);}catch(Exception ignored){return fallback;}}
    private static int modernStoneMeta(String name){String[] names={"granite","polished_granite","diorite",
            "polished_diorite","andesite","polished_andesite"};for(int i=0;i<names.length;i++)
        if(names[i].equals(name))return i;return-1;}
    private static int legacyStoneSlabMeta(String name){String[] names={"smooth_stone_slab","sandstone_slab",
            "petrified_oak_slab","cobblestone_slab","brick_slab","stone_brick_slab","nether_brick_slab","quartz_slab"};
        for(int i=0;i<names.length;i++)if(names[i].equals(name))return i;return-1;}
    private static Placed pottedPlant(String name){
        if(!name.startsWith("potted_"))return null;String plant=name.substring(7);Block block=null;int meta=0;boolean supported=false;
        String[] saplings={"oak_sapling","spruce_sapling","birch_sapling","jungle_sapling","acacia_sapling","dark_oak_sapling"};
        for(int i=0;i<saplings.length;i++)if(saplings[i].equals(plant)){block=Blocks.sapling;meta=i;supported=true;break;}
        if("cactus".equals(plant)){block=Blocks.cactus;supported=true;}else if("dead_bush".equals(plant)){block=Blocks.deadbush;supported=true;}
        else if("dandelion".equals(plant)){block=Blocks.yellow_flower;supported=true;}else if("red_mushroom".equals(plant)){block=Blocks.red_mushroom;supported=true;}
        else if("brown_mushroom".equals(plant)){block=Blocks.brown_mushroom;supported=true;}else if("poppy".equals(plant)){block=Blocks.red_flower;supported=true;}
        else{String[] flowers={"blue_orchid","allium","azure_bluet","red_tulip","orange_tulip","white_tulip","pink_tulip","oxeye_daisy"};
            for(int i=0;i<flowers.length;i++)if(flowers[i].equals(plant)){block=Blocks.red_flower;meta=i+1;supported=true;break;}}
        return !supported?null:new Placed(Blocks.flower_pot,0,block==null?null:Item.getItemFromBlock(block),meta);
    }
    private static void applyPlacedData(World world,Placed placed,int x,int y,int z){
        TileEntity tile=world.getTileEntity(x,y,z);
        if(placed.bannerBase>=0&&tile instanceof TileEntityBountifulBanner)
            ((TileEntityBountifulBanner)tile).setBannerData(placed.bannerBase,new NBTTagList());
        if(placed.potItem!=null&&tile instanceof TileEntityFlowerPot)
            ((TileEntityFlowerPot)tile).func_145964_a(placed.potItem,placed.potMeta);
    }
    private static int trapdoorMeta(State state,int rotation){String f=rotateFacing(state.get("facing"),rotation);
        int m="south".equals(f)?1:"west".equals(f)?2:"east".equals(f)?3:0;
        return m|("top".equals(state.get("half"))?8:0)|(on(state,"open")?4:0);}
    private static boolean on(State state,String key){return "true".equals(state.get(key));}
    private static int propertyInt(State state,String key,int min,int max,int fallback){return Math.max(min,Math.min(max,parseInt(state.get(key),fallback)));}
    private static int wallAttachment(State state,int rotation){String f=rotateFacing(state.get("facing"),rotation);
        return "east".equals(f)?1:"west".equals(f)?2:"south".equals(f)?3:4;}
    private static int leverStateMeta(State state,int rotation){String f=rotateFacing(state.get("facing"),rotation);
        boolean x="east".equals(f)||"west".equals(f);
        int m="floor".equals(state.get("face"))?(x?6:5):"ceiling".equals(state.get("face"))?(x?0:7):wallAttachment(state,rotation);
        return m|(on(state,"powered")?8:0);}

    private static int repeaterMeta(State state,int rotation){String facing=rotateFacing(state.get("facing"),rotation);return "east".equals(facing)?1:"south".equals(facing)?2:"west".equals(facing)?3:0;}
    /** Legacy BlockPumpkin uses south/west/north/east metadata 0/1/2/3. */
    private static int pumpkinMeta(State state,int rotation){String facing=rotateFacing(state.get("facing"),rotation);
        return "west".equals(facing)?1:"north".equals(facing)?2:"east".equals(facing)?3:0;}
    private static int vineMeta(State state,int rotation){int meta=0;
        for(String facing:new String[]{"north","east","south","west"})if("true".equals(state.get(facing))){
            String rotated=rotateFacing(facing,rotation);
            meta|="south".equals(rotated)?1:"west".equals(rotated)?2:"north".equals(rotated)?4:8;
        }
        return meta;
    }
    private static int pistonMeta(String facing){return "down".equals(facing)?0:"up".equals(facing)?1:"north".equals(facing)?2:"south".equals(facing)?3:"west".equals(facing)?4:5;}
    private static int doorMeta(State state,int rotation){if("upper".equals(state.get("half")))return 8|("right".equals(state.get("hinge"))?1:0);int m=repeaterMeta(state,rotation);if("true".equals(state.get("open")))m|=4;return m;}
    private static int bedMeta(State state,int rotation){int m=repeaterMeta(state,rotation);if("head".equals(state.get("part")))m|=8;return m;}
    private static int stairMeta(String facing){return "east".equals(facing)?0:"west".equals(facing)?1:"south".equals(facing)?2:3;}
    private static int chestMeta(String facing){return "south".equals(facing)?3:"west".equals(facing)?4:"east".equals(facing)?5:2;}
    private static String rotateFacing(String facing,int rotation){String[] values={"north","east","south","west"};for(int i=0;i<4;i++)if(values[i].equals(facing))return values[(i+rotation)&3];return facing;}
    private static int[] rotatePoint(int x,int z,int rotation){switch(rotation&3){case 1:return new int[]{-z,x};case 2:return new int[]{-x,-z};case 3:return new int[]{z,-x};default:return new int[]{x,z};}}
    private static String path(String name){return name!=null&&name.startsWith("minecraft:")?name.substring(10):name;}
    private static int woodVariant(String path){String p=path.startsWith("stripped_")?path.substring(9):path;return p.startsWith("oak_")?0:p.startsWith("spruce_")?1:p.startsWith("birch_")?2:p.startsWith("jungle_")?3:p.startsWith("acacia_")?4:p.startsWith("dark_oak_")?5:-1;}
    private static Placed woodLog(int wood,State state,int rotation){Block block=wood<4?Blocks.log:Blocks.log2;int base=wood<4?wood:wood-4;return new Placed(block,axisMeta(base,state.get("axis"),rotation));}
    private static Placed strippedWood(String path,State state,int rotation){
        if(!path.startsWith("stripped_")||(!path.endsWith("_log")&&!path.endsWith("_wood")))return null;
        int wood=woodVariant(path);if(wood<0)return null;
        boolean allBark=path.endsWith("_wood");
        Block block=allBark?(wood<4?Backport120Content.strippedWood:Backport120Content.strippedWood2)
                :(wood<4?Backport120Content.strippedLog:Backport120Content.strippedLog2);
        return new Placed(block,axisMeta(wood<4?wood:wood-4,state.get("axis"),rotation));
    }
    private static Block woodStairs(int wood){switch(wood){case 1:return Blocks.spruce_stairs;case 2:return Blocks.birch_stairs;case 3:return Blocks.jungle_stairs;case 4:return Blocks.acacia_stairs;case 5:return Blocks.dark_oak_stairs;default:return Blocks.oak_stairs;}}

    private static synchronized Template load(String name) {
        String resource="/assets/htgt6/structures/ancient_city/"+name+".nbt";
        return loadResource(resource);
    }

    /** Shared modern-NBT placement path used by the Futura village adapter. */
    static void placeTemplate(World world,String resource,int centerX,int y,int centerZ,
                              int rotation,int chunkX,int chunkZ) {
        new Piece(resource,centerX,y,centerZ,rotation,true).place(world,chunkX,chunkZ);
    }

    /** Origin-based placement used by the official template-pool runtime. */
    public static void placeTemplateAtOrigin(World world,String resource,int originX,int originY,int originZ,
                                       int rotation,int chunkX,int chunkZ,List<String> processors,
                                       boolean ignoreTemplateAir) {
        placeTemplateAtOrigin(world,resource,originX,originY,originZ,rotation,chunkX,chunkZ,
                processors,ignoreTemplateAir,null,false);
    }

    /** Village-aware placement with the implicit TERRAIN_MATCHING gravity processor. */
    public static void placeTemplateAtOrigin(World world,String resource,int originX,int originY,int originZ,
                                       int rotation,int chunkX,int chunkZ,List<String> processors,
                                       boolean ignoreTemplateAir,JigsawPlacementRuntime120.Terrain gravity,
                                       boolean preserveNovaCraft) {
        Template template=loadResource(resource);
        TrailRuinsProcessor120.Context trailProcessor=TrailRuinsProcessor120.context(processors);
        int minChunkX=chunkX<<4,minChunkZ=chunkZ<<4,maxChunkX=minChunkX+15,maxChunkZ=minChunkZ+15;
        for(BlockEntry entry:template.blocks){
            if(entry.state<0||entry.state>=template.palette.length)continue;
            int[] point=rotateOriginPoint(entry.x,entry.z,template.sizeX,template.sizeZ,rotation);
            int wx=originX+point[0],wz=originZ+point[1];
            int wy=gravity==null?originY+entry.y:terrainMatchedBlockY(gravity,wx,wz,entry.y);
            State state=template.palette[entry.state];
            if(shouldIgnoreTemplateState(ignoreTemplateAir,state.name))continue;
            StructureProcessorRuntime120.State inputState=new StructureProcessorRuntime120.State(state.name,state.properties);
            // Capped archaeology must consume the complete template in stable NBT order on
            // every replay. Processing before the ownership clip makes its chosen positions
            // identical for every target chunk and for every chunk-generation order.
            StructureProcessorRuntime120.State processed=trailProcessor==null?null
                    :trailProcessor.apply(inputState,wx,wy,wz);
            if(!com.mod.htgt6.common.util.StructurePlacement120.isOwnedByChunk(wx,wz,chunkX,chunkZ)
                    ||wy<1||wy>=world.getActualHeight())continue;
            if(preserveNovaCraft&&isNovaCraftBlock(world.getBlock(wx,wy,wz)))continue;
            if(trailProcessor==null)processed=StructureProcessorRuntime120.apply(processors,inputState,
                    worldState(world,wx,wy,wz),wx,wy,wz);
            if(processed==null)continue;
            State output=new State(processed.name,processed.properties);
            Placed placed="jigsaw".equals(path(state.name))?mapFinalState(entry.finalState,rotation):map(output,rotation);
            if(placed==null)continue;
            world.setBlock(wx,wy,wz,placed.block,placed.meta,2);
            String archaeologyLoot=processed.properties.get(TrailRuinsProcessor120.LOOT_PROPERTY);
            if(archaeologyLoot!=null&&world.getTileEntity(wx,wy,wz) instanceof TileEntityBackport120Brushable)
                ((TileEntityBackport120Brushable)world.getTileEntity(wx,wy,wz)).setLoot(
                        TrailRuinsProcessor120.loot(archaeologyLoot,StructureProcessorRuntime120.positionalSeed(wx,wy,wz)));
            VillagePoiData120.registerIfPoi(world,wx,wy,wz);
            applyPlacedData(world,placed,wx,wy,wz);
            boolean lootApplied=applyBlockEntityData(world,entry.blockData,wx,wy,wz);
            if(placed.block==Blocks.chest&&!lootApplied)fillChest(world,wx,wy,wz);
        }
        spawnTemplateEntities(world,template,originX,originY,originZ,rotation,chunkX,chunkZ,gravity);
    }

    private static void spawnTemplateEntities(World world,Template template,int originX,int originY,int originZ,
                                              int rotation,int chunkX,int chunkZ){
        spawnTemplateEntities(world,template,originX,originY,originZ,rotation,chunkX,chunkZ,null);
    }

    private static void spawnTemplateEntities(World world,Template template,int originX,int originY,int originZ,
                                              int rotation,int chunkX,int chunkZ,
                                              JigsawPlacementRuntime120.Terrain gravity){
        if(world.isRemote)return;
        for(EntityEntry entry:template.entities){
            double[] point=rotateEntityPoint(entry.x,entry.z,template.sizeX,template.sizeZ,rotation);
            double x=originX+point[0],z=originZ+point[1];
            double y=gravity==null?originY+entry.y:gravity.surfaceY((int)Math.floor(x),(int)Math.floor(z))-1+entry.y;
            if(((int)Math.floor(x)>>4)!=chunkX||((int)Math.floor(z)>>4)!=chunkZ)continue;
            Entity entity=createTemplateEntity(world,entry.data);
            if(entity==null)continue;
            entity.setLocationAndAngles(x,y,z,entity.rotationYaw+rotation*90F,entity.rotationPitch);
            AxisAlignedBB duplicateBox=AxisAlignedBB.getBoundingBox(x-.2D,y-.2D,z-.2D,x+.2D,y+.2D,z+.2D);
            if(!world.getEntitiesWithinAABB(entity.getClass(),duplicateBox).isEmpty())continue;
            if(entity instanceof EntityLiving)((EntityLiving)entity).func_110163_bv();
            world.spawnEntityInWorld(entity);
        }
    }

    private static boolean isNovaCraftBlock(Block block){
        Object key=Block.blockRegistry.getNameForObject(block);
        if(key==null)return false;
        String name=String.valueOf(key).toLowerCase(java.util.Locale.ROOT);
        return name.startsWith("novacraft:")||name.startsWith("nova_craft:");
    }

    /** 1.20.2 TERRAIN_MATCHING's implicit WORLD_SURFACE_WG gravity processor. */
    public static int terrainMatchedBlockY(JigsawPlacementRuntime120.Terrain terrain,int x,int z,int localY){
        if(terrain==null)throw new NullPointerException("terrain");
        return terrain.surfaceY(x,z)-1+localY;
    }

    private static Entity createTemplateEntity(World world,NBTTagCompound data){
        String id=data.getString("id");
        if("minecraft:villager".equals(id)){
            String profession=templateProfessionName(data);
            EntityVillager villager=new EntityVillager(world,templateProfession(data));
            VillagerCareers112.setModernProfession(villager,profession);
            if(data.hasKey("Age",99))villager.setGrowingAge(data.getInteger("Age"));
            return villager;
        }
        if("minecraft:zombie_villager".equals(id)){
            EntityCombatZombieVillager zombie=new EntityCombatZombieVillager(world);
            zombie.setProfession(templateProfession(data));
            if(data.getBoolean("IsBaby"))zombie.setChild(true);
            return zombie;
        }
        if("minecraft:armor_stand".equals(id)){
            EntityBountifulArmorStand stand=new EntityBountifulArmorStand(world);
            stand.setShowArms(data.getBoolean("ShowArms"));
            stand.setNoBasePlate(data.getBoolean("NoBasePlate"));
            return stand;
        }
        if("minecraft:allay".equals(id)){
            EntityAllay120 allay=new EntityAllay120(world);
            if(data.hasKey("Health",99))allay.setHealth(Math.min(allay.getMaxHealth(),data.getFloat("Health")));
            return allay;
        }
        if("minecraft:camel".equals(id)){
            EntityCamel120 camel=new EntityCamel120(world);
            if(data.hasKey("Age",99))camel.setGrowingAge(data.getInteger("Age"));
            return camel;
        }
        if("minecraft:iron_golem".equals(id))return new EntityIronGolem(world);
        if("minecraft:cow".equals(id))return new EntityCow(world);
        if("minecraft:pig".equals(id))return new EntityPig(world);
        if("minecraft:sheep".equals(id))return new EntitySheep(world);
        if("minecraft:horse".equals(id))return new EntityHorse(world);
        if("minecraft:cat".equals(id))return new EntityOcelot(world);
        synchronized(WARNED_ENTITY_GAPS){if(WARNED_ENTITY_GAPS.add(id))LOGGER.warn(
                "Official structure entity '{}' has no 1.7/backport adapter and was skipped",id);}
        return null;
    }

    private static int templateProfession(NBTTagCompound data){
        if(!data.hasKey("VillagerData",10))return 0;
        String value=path(data.getCompoundTag("VillagerData").getString("profession"));
        if("librarian".equals(value)||"cartographer".equals(value))return 1;
        if("cleric".equals(value))return 2;
        if("armorer".equals(value)||"toolsmith".equals(value)||"weaponsmith".equals(value)
                ||"mason".equals(value))return 3;
        if("butcher".equals(value)||"leatherworker".equals(value))return 4;
        return "nitwit".equals(value)?5:0;
    }

    private static String templateProfessionName(NBTTagCompound data){
        if(!data.hasKey("VillagerData",10))return "none";
        String value=path(data.getCompoundTag("VillagerData").getString("profession"));
        return "".equals(value)?"none":value;
    }

    private static int horizontalMeta(State state,int rotation){
        String facing=rotateFacing(state.get("facing"),rotation);
        if("east".equals(facing))return 1;
        if("south".equals(facing))return 2;
        if("west".equals(facing))return 3;
        return 0;
    }

    private static int grindstoneMeta(State state,int rotation){
        String face=state.get("face");
        return horizontalMeta(state,rotation)|("wall".equals(face)?4:"ceiling".equals(face)?8:0);
    }

    private static int bellMeta(State state,int rotation){
        String attachment=state.get("attachment");
        int value="ceiling".equals(attachment)?4:"single_wall".equals(attachment)?8:
                "double_wall".equals(attachment)?12:0;
        return horizontalMeta(state,rotation)|value;
    }

    /** Entity ids from official structure NBT that have a concrete legacy/backport adapter. */
    public static boolean supportsTemplateEntity(String id){
        return "minecraft:villager".equals(id)||"minecraft:zombie_villager".equals(id)
                ||"minecraft:armor_stand".equals(id)
                ||"minecraft:allay".equals(id)
                ||"minecraft:camel".equals(id)
                ||"minecraft:iron_golem".equals(id)||"minecraft:cow".equals(id)
                ||"minecraft:pig".equals(id)||"minecraft:sheep".equals(id)
                ||"minecraft:horse".equals(id)||"minecraft:cat".equals(id);
    }

    public static Block mappedBlockForFixture(String modernName){
        Placed placed=map(new State("minecraft:"+modernName,new HashMap<String,String>()),0);
        return placed==null?null:placed.block;
    }

    public static int mappedMetadataForFixture(String modernName,String facing,int rotation){
        Map<String,String> properties=new HashMap<String,String>();properties.put("facing",facing);
        Placed placed=map(new State("minecraft:"+modernName,properties),rotation);
        return placed==null?-1:placed.meta;
    }

    public static Block mappedStateBlockForFixture(String name,Map<String,String> properties,int rotation){
        Placed p=map(new State("minecraft:"+name,properties),rotation);return p==null?null:p.block;}
    public static int mappedStateMetaForFixture(String name,Map<String,String> properties,int rotation){
        Placed p=map(new State("minecraft:"+name,properties),rotation);return p==null?-1:p.meta;}

    private static double[] rotateEntityPoint(double x,double z,int sizeX,int sizeZ,int rotation){
        switch(rotation&3){case 1:return new double[]{sizeZ-z,x};case 2:return new double[]{sizeX-x,sizeZ-z};
            case 3:return new double[]{z,sizeX-x};default:return new double[]{x,z};}
    }

    private static StructureProcessorRuntime120.State worldState(World world,int x,int y,int z){
        Block block=world.getBlock(x,y,z);Object key=Block.blockRegistry.getNameForObject(block);
        return new StructureProcessorRuntime120.State(key==null?"minecraft:air":String.valueOf(key));
    }

    private static int[] rotateOriginPoint(int x,int z,int sizeX,int sizeZ,int rotation){
        switch(rotation&3){case 1:return new int[]{sizeZ-1-z,x};case 2:return new int[]{sizeX-1-x,sizeZ-1-z};
            case 3:return new int[]{z,sizeX-1-x};default:return new int[]{x,z};}
    }

    public static void validateTemplateResource(String resource) {
        Template template=loadResource(resource);
        Set<String> unsupported=new HashSet<String>();
        for(State state:template.palette){
            String block=path(state.name);
            if(map(state,0)==null&&!"jigsaw".equals(block)&&!"structure_void".equals(block)
                    &&!OPTIONAL_DECOR.contains(block))unsupported.add(block);
        }
        if(!unsupported.isEmpty())throw new IllegalStateException(
                "Modern structure contains unmapped structural blocks in "+resource+": "+unsupported);
    }

    static void retainTemplateResource(String resource){loadResource(resource);}

    private static synchronized Template loadResource(String resource) {
        Template cached=CACHE.get(resource);if(cached!=null)return cached;
        InputStream input=BackportResourceLoader120.open(FuturaAncientCityGenerator120.class,resource);
        if(input==null)throw new IllegalStateException("Missing modern structure template: "+resource);
        try {
            NBTTagCompound root=CompressedStreamTools.readCompressed(input);
            int[] size=intList(root.getTagList("size",3));
            NBTTagList paletteTag=root.getTagList("palette",10);State[] palette=new State[paletteTag.tagCount()];
            for(int i=0;i<palette.length;i++){
                NBTTagCompound value=paletteTag.getCompoundTagAt(i);Map<String,String> properties=new HashMap<String,String>();
                if(value.hasKey("Properties",10)){NBTTagCompound values=value.getCompoundTag("Properties");for(Object key:values.func_150296_c())properties.put(String.valueOf(key),values.getString(String.valueOf(key)));}
                palette[i]=new State(value.getString("Name"),properties);
            }
            NBTTagList blockTag=root.getTagList("blocks",10);List<BlockEntry> blocks=new ArrayList<BlockEntry>(blockTag.tagCount());
            for(int i=0;i<blockTag.tagCount();i++){
                NBTTagCompound value=blockTag.getCompoundTagAt(i);int[] pos=intList(value.getTagList("pos",3));
                NBTTagCompound blockData=value.hasKey("nbt",10)?value.getCompoundTag("nbt"):null;
                String finalState=blockData==null?null:blockData.getString("final_state");
                blocks.add(new BlockEntry(pos[0],pos[1],pos[2],value.getInteger("state"),finalState,blockData));
            }
            NBTTagList entityTag=root.getTagList("entities",10);List<EntityEntry> entities=new ArrayList<EntityEntry>(entityTag.tagCount());
            for(int i=0;i<entityTag.tagCount();i++){
                NBTTagCompound value=entityTag.getCompoundTagAt(i);double[] pos=doubleList(value.getTagList("pos",6));
                if(pos.length>=3&&value.hasKey("nbt",10))entities.add(new EntityEntry(pos[0],pos[1],pos[2],value.getCompoundTag("nbt")));
            }
            Template template=new Template(size[0],size[1],size[2],palette,blocks,entities);CACHE.put(resource,template);return template;
        } catch(IOException exception){throw new IllegalStateException("Cannot read modern structure template: "+resource,exception);}
        finally{try{input.close();}catch(IOException ignored){}}
    }

    private static int[] intList(NBTTagList list){List<NBTBase> values=rawList(list);int[] result=new int[values.size()];for(int i=0;i<result.length;i++)result[i]=((NBTBase.NBTPrimitive)values.get(i)).func_150287_d();return result;}
    private static double[] doubleList(NBTTagList list){List<NBTBase> values=rawList(list);double[] result=new double[values.size()];for(int i=0;i<result.length;i++)result[i]=((NBTBase.NBTPrimitive)values.get(i)).func_150286_g();return result;}
    @SuppressWarnings("unchecked") private static List<NBTBase> rawList(NBTTagList list){try{if(tagListField==null)for(Field field:NBTTagList.class.getDeclaredFields())if(List.class.isAssignableFrom(field.getType())){field.setAccessible(true);tagListField=field;break;}return (List<NBTBase>)tagListField.get(list);}catch(Exception exception){throw new IllegalStateException("Cannot access NBT list",exception);}}
    private static long mix(long value){value=(value^(value>>>33))*0xff51afd7ed558ccdL;value=(value^(value>>>33))*0xc4ceb9fe1a85ec53L;return value^(value>>>33);}
    private static final class Candidate{final int chunkX,chunkZ;Candidate(int x,int z){chunkX=x;chunkZ=z;}}
    private static final class Template{final int sizeX,sizeY,sizeZ;final State[] palette;final List<BlockEntry> blocks;final List<EntityEntry> entities;Template(int x,int y,int z,State[] p,List<BlockEntry>b,List<EntityEntry> e){sizeX=x;sizeY=y;sizeZ=z;palette=p;blocks=b;entities=e;}}
    private static final class BlockEntry{final int x,y,z,state;final String finalState;final NBTTagCompound blockData;
        BlockEntry(int x,int y,int z,int state,String finalState,NBTTagCompound blockData){this.x=x;this.y=y;this.z=z;this.state=state;this.finalState=finalState;this.blockData=blockData;}}
    private static final class EntityEntry{final double x,y,z;final NBTTagCompound data;EntityEntry(double x,double y,double z,NBTTagCompound data){this.x=x;this.y=y;this.z=z;this.data=data;}}
    private static final class State{final String name;final Map<String,String> properties;State(String name,Map<String,String> properties){this.name=name;this.properties=properties;}String get(String name){return properties.get(name);}}
    private static final class Placed{final Block block;final int meta,bannerBase,potMeta;final Item potItem;
        Placed(Block block,int meta){this(block,meta,-1,null,0);}Placed(Block block,int meta,int bannerBase){
            this(block,meta,bannerBase,null,0);}Placed(Block block,int meta,Item potItem,int potMeta){
            this(block,meta,-1,potItem,potMeta);}Placed(Block block,int meta,int bannerBase,Item potItem,int potMeta){
            this.block=block;this.meta=meta;this.bannerBase=bannerBase;this.potItem=potItem;this.potMeta=potMeta;}}
    private FuturaAncientCityGenerator120(){}
}
