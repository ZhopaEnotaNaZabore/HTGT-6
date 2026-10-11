package com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockColoredBed120;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockVillagePoi120;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageBell120;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageFurnace120;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageLectern120;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageComposter120;
import com.mod.htgt6.minecraft1202.Module.Events.VillageAndPillage.VillagePoiHandler120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagePoiType120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillagerSleep120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.ContainerVillageWorkstation120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageWorkstationLogic120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageWorkstationType120;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaAncientCityGenerator120;
import com.mod.htgt6.client.GUI.GuiVillageWorkstation120;
import com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.ModelVillager120;
import com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.RenderVillager120;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.event.world.ChunkEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/** Regression contract for 1.20.2 job sites, model assets and exact profession inheritance. */
public final class VillagePoiContractFixture120 {
    static { com.mod.htgt6.minecraft1202.Module.Data.Modern.VillageBootstrapFixture114.initialize(); }
    private static final String[] TYPES = {"armorer", "butcher", "cartographer", "farmer", "fisherman",
            "fletcher", "librarian", "mason", "shepherd", "toolsmith", "weaponsmith"};
    private static final Block[] BLOCKS = {VillagePillageContent.blastFurnace, VillagePillageContent.smoker,
            VillagePillageContent.cartographyTable, VillagePillageContent.composter, VillagePillageContent.barrel,
            VillagePillageContent.fletchingTable, VillagePillageContent.lectern, VillagePillageContent.stonecutter,
            VillagePillageContent.loom, VillagePillageContent.smithingTable, VillagePillageContent.grindstone};
    private static final String[] MODEL_NAMES = {"blast_furnace", "smoker", "cartography_table", "composter",
            "barrel", "fletching_table", "lectern", "stonecutter", "loom", "smithing_table", "grindstone", "bell"};

    public static void main(String[] args) { assertContract(); System.out.println("VillagePoiContractFixture120: OK"); }

    public static void assertContract() {
        require(TYPES.length == BLOCKS.length, "job-site fixture table mismatch");
        for (int i = 0; i < TYPES.length; ++i)
            require(TYPES[i].equals(VillagePoiType120.forBlock(BLOCKS[i])), "wrong POI block for " + TYPES[i]);
        String[] jobBlocks={"blast_furnace","smoker","cartography_table","composter","barrel","fletching_table",
                "lectern","stonecutter","loom","smithing_table","grindstone"};
        for(int i=0;i<jobBlocks.length;++i)require(BLOCKS[i]==FuturaAncientCityGenerator120
                .mappedBlockForFixture(jobBlocks[i]),"Futura mapping "+jobBlocks[i]);
        if (Blocks.brewing_stand != null)
            require("cleric".equals(VillagePoiType120.forBlock(Blocks.brewing_stand)), "brewing stand POI missing");
        if (Blocks.cauldron != null)
            require("leatherworker".equals(VillagePoiType120.forBlock(Blocks.cauldron)), "cauldron POI missing");
        if (Blocks.bed != null)
            require("home".equals(VillagePoiType120.forBlock(Blocks.bed)), "bed/home POI missing");
        require("meeting".equals(VillagePoiType120.forBlock(VillagePillageContent.bell)), "bell POI missing");
        require(VillagePillageContent.bell.hasTileEntity(0)
                && VillagePillageContent.bell.createTileEntity(null, 0) instanceof TileEntityVillageBell120,
                "bell tile entity/renderer state missing");
        require(VillagePillageContent.composter.hasTileEntity(0)
                && VillagePillageContent.composter.createTileEntity(null,0) instanceof TileEntityVillageComposter120
                && VillagePillageContent.composter.createTileEntity(null,0) instanceof net.minecraft.inventory.ISidedInventory,
                "composter hopper inventory missing");
        require(VillagePillageContent.composter.hasComparatorInputOverride(),"composter comparator output missing");
        require(!VillagePoiHandler120.mayAdoptPoiProfession(true, "librarian", "farmer")
                && VillagePoiHandler120.mayAdoptPoiProfession(false, "none", "farmer"),
                "traded villager profession lock missing");
        assertStableEntityIteration();
        assertChunkMigrationHook();
        assertBellState();
        require(VillagerCareers112.legacyProfessionForFixture("cartographer") == 1
                && VillagerCareers112.legacyCareerForFixture("cartographer") == 2, "cartographer inheritance");
        require(VillagerCareers112.legacyProfessionForFixture("mason") == 3
                && VillagerCareers112.legacyCareerForFixture("mason") == 3, "mason inheritance");
        require(VillagerCareers112.legacyProfessionForFixture("fletcher") == 0
                && VillagerCareers112.legacyCareerForFixture("fletcher") == 4, "fletcher inheritance");
        require(VillagerCareers112.legacyProfessionForFixture("nitwit") == 5, "nitwit inheritance");
        require(VillagerCareers112.VISUAL_DATA_WATCHER==30,"stable visual data watcher");
        require(RenderVillager120.class.getSuperclass()==net.minecraft.client.renderer.entity.RenderVillager.class,
                "modern renderer must stay on the 1.7 rendering API");
        require(ModelVillager120.class.getSuperclass()==net.minecraft.client.model.ModelVillager.class,
                "modern model must stay on the 1.7 model API");
        require(VillagePillageContent.coloredBeds.length == 16, "all sixteen bed colors must be registered");
        for (int i = 0; i < 16; ++i) {
            require(VillagePillageContent.coloredBeds[i] instanceof BlockColoredBed120, "colored bed block " + i);
            require("home".equals(VillagePoiType120.forBlock(VillagePillageContent.coloredBeds[i])), "bed POI " + i);
            String bed = "/assets/htgt6/textures/blocks/village_pillage/bed/"
                    + VillagePillageContent.BED_COLORS[i];
            resource(bed + "_feet_top.png");
            resource(bed + "_feet_side.png");
            resource(bed + "_feet_end.png");
            resource(bed + "_head_top.png");
            resource(bed + "_head_side.png");
            resource(bed + "_head_end.png");
            resource("/assets/htgt6/textures/items/village_pillage/bed/"
                    + VillagePillageContent.BED_COLORS[i] + "_item.png");
            require(VillagePillageContent.coloredBeds[i]==FuturaAncientCityGenerator120
                    .mappedBlockForFixture(VillagePillageContent.BED_COLORS[i]+"_bed"),
                    "Futura colored bed mapping "+i);
        }
        require(VillagePillageContent.barrel instanceof net.minecraft.block.BlockContainer, "barrel inventory missing");
        require(VillagePillageContent.blastFurnace instanceof net.minecraft.block.BlockContainer, "blast furnace inventory missing");
        require(VillagePillageContent.smoker instanceof net.minecraft.block.BlockContainer, "smoker inventory missing");
        require(VillagePillageContent.barrel.createTileEntity(null, 0) instanceof net.minecraft.tileentity.TileEntityChest,
                "barrel tile entity missing");
        require(VillagePillageContent.blastFurnace.createTileEntity(null, 0) instanceof TileEntityVillageFurnace120,
                "blast furnace tile entity missing");
        require(VillagePillageContent.coloredBeds[0].hasTileEntity(0)
                && VillagePillageContent.coloredBeds[0].getRenderType()==-1,
                "colored beds require the modern model on the native tile renderer");
        resource("/assets/htgt6/textures/entity/village_pillage/villager/villager.png");
        resource("/assets/htgt6/textures/entity/village_pillage/villager/profession/nitwit.png");
        String[] villagerTypes={"plains","desert","jungle","savanna","snow","swamp","taiga"};
        for(String type:villagerTypes)resource("/assets/htgt6/textures/entity/village_pillage/villager/type/"+type+".png");
        String[] professions={"armorer","butcher","cartographer","cleric","farmer","fisherman","fletcher",
                "leatherworker","librarian","mason","nitwit","shepherd","toolsmith","weaponsmith"};
        for(String profession:professions)resource(
                "/assets/htgt6/textures/entity/village_pillage/villager/profession/"+profession+".png");
        for(String level:new String[]{"stone","iron","gold","emerald","diamond"})resource(
                "/assets/htgt6/textures/entity/village_pillage/villager/profession_level/"+level+".png");
        resource("/assets/htgt6/textures/entity/village_pillage/bell/bell_body.png");
        resource("/assets/htgt6/models/item/village_pillage/bell.json");
        resource("/assets/htgt6/textures/blocks/village_pillage/stonecutter_saw.png.mcmeta");
        assertFallbackIcons();
        assertWorkstationBehavior();
        VillagePoiData120.assertRoundTripFixture();
        VillagerSleep120.assertFixture();
        for (String name : MODEL_NAMES) resource("/assets/htgt6/blockstates/village_pillage/" + name + ".json");
        String[] models = {"block", "cube", "cube_bottom_top", "orientable", "orientable_with_bottom",
                "blast_furnace", "smoker", "cartography_table", "composter", "barrel", "fletching_table",
                "lectern", "stonecutter", "loom", "smithing_table", "grindstone", "bell_floor", "bell_wall",
                "bell_ceiling", "bell_between_walls", "composter_contents1", "composter_contents2",
                "composter_contents3", "composter_contents4", "composter_contents5", "composter_contents6",
                "composter_contents7", "composter_contents_ready"};
        for (String name : models) inspectModel(name);
    }

    private static void inspectModel(String name) {
        String path = "/assets/minecraft/models/block/" + name + ".json";
        InputStream stream = VillagePoiContractFixture120.class.getResourceAsStream(path);
        require(stream != null, "missing resource " + path);
        try {
            JsonObject json = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!json.has("textures")) return;
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("textures").entrySet()) {
                String texture = entry.getValue().getAsString();
                if (texture.startsWith("#")) continue;
                int colon = texture.indexOf(':');
                if (colon >= 0) texture = texture.substring(colon + 1);
                if (texture.startsWith("block/")) texture = texture.substring(6);
                resource("/assets/htgt6/textures/blocks/village_pillage/" + texture + ".png");
            }
        } finally {
            try { stream.close(); } catch (Exception ignored) { }
        }
    }

    private static void assertChunkMigrationHook() {
        try {
            Method method = VillagePoiHandler120.class.getMethod("chunkLoad", ChunkEvent.Load.class);
            require(method.getAnnotation(SubscribeEvent.class) != null, "chunk-load POI migration is not subscribed");
        } catch (Exception error) {
            throw new IllegalStateException("chunk-load POI migration hook missing", error);
        }
    }

    private static void assertStableEntityIteration() {
        List<Object> live = new ArrayList<Object>();
        live.add("first");
        live.add("second");
        Object[] snapshot = VillagePoiHandler120.stableIterationSnapshot(live);
        int visited = 0;
        for (Object ignored : snapshot) {
            ++visited;
            live.add("spawned-during-tick");
        }
        require(visited == 2 && live.size() == 4,
                "POI tick must tolerate entity-list changes made by callbacks");
    }

    private static void assertBellState() {
        TileEntityVillageBell120.assertRoundTripFixture();
    }

    private static void assertFallbackIcons() {
        Block[] blocks = {VillagePillageContent.blastFurnace, VillagePillageContent.smoker,
                VillagePillageContent.cartographyTable, VillagePillageContent.composter,
                VillagePillageContent.barrel, VillagePillageContent.fletchingTable,
                VillagePillageContent.lectern, VillagePillageContent.stonecutter,
                VillagePillageContent.loom, VillagePillageContent.smithingTable,
                VillagePillageContent.grindstone, VillagePillageContent.bell};
        try {
            Field field = Block.class.getDeclaredField("textureName");
            field.setAccessible(true);
            for (Block block : blocks) {
                String value = (String)field.get(block);
                require(value != null && value.startsWith("htgt6:village_pillage/"),
                        "runtime block icon has the wrong namespace for " + block.getUnlocalizedName());
            }
        } catch (Exception error) {
            throw new IllegalStateException("could not inspect runtime block icon namespace", error);
        }
    }

    private static void assertWorkstationBehavior() {
        require(VillageWorkstationType120.byBlockName("fletching_table") == null,
                "1.20.2 fletching table must not expose a menu");
        for (VillageWorkstationType120 type : VillageWorkstationType120.values())
            require(VillageWorkstationType120.byGuiId(type.guiId) == type, "workstation GUI route " + type);
        requireLayout(VillageWorkstationType120.SMITHING,new int[][]{{8,48},{26,48},{44,48},{98,48}});
        requireLayout(VillageWorkstationType120.GRINDSTONE,new int[][]{{49,19},{49,40},{129,34}});
        requireLayout(VillageWorkstationType120.STONECUTTER,new int[][]{{20,33},{143,33}});
        requireLayout(VillageWorkstationType120.LOOM,new int[][]{{13,26},{33,26},{23,45},{143,58}});
        requireLayout(VillageWorkstationType120.CARTOGRAPHY,new int[][]{{15,15},{15,52},{145,39}});
        for(int base:new int[]{ContainerVillageWorkstation120.LOOM_SELECTION_BASE,ContainerVillageWorkstation120.STONECUTTER_SELECTION_BASE}){
            for(int choice=0;choice<34;choice++)try{
                net.minecraft.network.PacketBuffer buffer=new net.minecraft.network.PacketBuffer(io.netty.buffer.Unpooled.buffer());
                net.minecraft.network.play.client.C11PacketEnchantItem sent=new net.minecraft.network.play.client.C11PacketEnchantItem(7,base+choice);
                sent.writePacketData(buffer);net.minecraft.network.play.client.C11PacketEnchantItem received=new net.minecraft.network.play.client.C11PacketEnchantItem();
                received.readPacketData(buffer);require(received.func_149537_d()==base+choice,"recipe selection lost in native packet");buffer.release();
            }catch(java.io.IOException failure){throw new IllegalStateException(failure);}
        }
        for(String furnace:new String[]{"smoker","blast_furnace"}){
            try(InputStream stream=VillagePoiContractFixture120.class.getResourceAsStream("/assets/htgt6/blockstates/village_pillage/"+furnace+".json")){
                JsonObject variants=new JsonParser().parse(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("variants");
                java.util.Set<String> states=new java.util.HashSet<String>();
                for(Map.Entry<String,JsonElement> entry:variants.entrySet())states.add(com.mod.htgt6.client.model.json.JsonBlockModelLoader.canonicalState(entry.getKey()));
                for(String facing:new String[]{"north","east","south","west"})for(boolean lit:new boolean[]{false,true})
                    require(states.contains(com.mod.htgt6.client.model.json.JsonBlockModelLoader.canonicalState("facing="+facing+",lit="+lit)),"missing furnace state "+furnace+facing+lit);
            }catch(java.io.IOException failure){throw new IllegalStateException(failure);}
        }
        com.mod.htgt6.minecraft1202.Module.Network.VillageAndPillage.RaidNetwork150.MerchantProgress progress=new com.mod.htgt6.minecraft1202.Module.Network.VillageAndPillage.RaidNetwork150.MerchantProgress();
        progress.entityId=42;progress.rank=3;progress.xp=109;progress.offerXp=new int[]{1,5,30};
        io.netty.buffer.ByteBuf buffer=io.netty.buffer.Unpooled.buffer();progress.toBytes(buffer);
        com.mod.htgt6.minecraft1202.Module.Network.VillageAndPillage.RaidNetwork150.MerchantProgress received=new com.mod.htgt6.minecraft1202.Module.Network.VillageAndPillage.RaidNetwork150.MerchantProgress();received.fromBytes(buffer);buffer.release();
        require(received.entityId==42&&received.rank==3&&received.xp==109&&received.offerXp[2]==30,"merchant progress network round trip");
        require(com.mod.htgt6.client.model.json.JsonBlockModelLoader.canonicalState("lit=true,facing=east").equals(
                com.mod.htgt6.client.model.json.JsonBlockModelLoader.canonicalState("facing=east,lit=true")),"property-order independence");
        require(com.mod.htgt6.minecraft1202.Module.Client.Modern.GuiMerchant120.progressWidth(2,40)==51,"merchant XP fraction");
        require(ContainerVillageWorkstation120.LECTERN_TAKE_BOOK==2,
                "lectern take-book protocol");
        net.minecraft.world.storage.MapData maxMap=new net.minecraft.world.storage.MapData("fixture");maxMap.scale=4;
        require(!ContainerVillageWorkstation120.canScaleCartographyMap(new net.minecraft.item.ItemStack(net.minecraft.init.Items.filled_map),maxMap),
                "cartography accepted scale-five map");
        require(Math.abs(GuiVillageWorkstation120.uiScaleForViewport(640,480)-1.12F)<0.0001F,
                "workstation UI enlargement");
        require(GuiVillageWorkstation120.uiScaleForViewport(176,166)==1.0F,
                "small-screen UI scale guard");
        for(String texture:new String[]{"smithing_table","grindstone","stonecutter","loom","cartography_table"})
            resource("/assets/htgt6/textures/gui/container/village_pillage/"+texture+".png");
        float cake=BlockVillagePoi120.compostChanceForKey("cake");
        float bread=BlockVillagePoi120.compostChanceForKey("bread");
        float apple=BlockVillagePoi120.compostChanceForKey("apple");
        float melon=BlockVillagePoi120.compostChanceForKey("melon_slice");
        float seeds=BlockVillagePoi120.compostChanceForKey("wheat_seeds");
        require(cake==1F&&bread==.85F&&apple==.65F&&melon==.5F&&seeds==.3F,
                "official compost chance tiers: "+cake+","+bread+","+apple+","+melon+","+seeds);
        VillageWorkstationLogic120.assertBehaviorFixture();
        TileEntityVillageLectern120.assertStateFixture();
    }

    private static void requireLayout(VillageWorkstationType120 type,int[][] expected){
        int[][] actual=ContainerVillageWorkstation120.slotLayout(type);
        require(actual.length==expected.length,"wrong workstation slot count for "+type);
        for(int i=0;i<expected.length;i++)require(actual[i][0]==expected[i][0]&&actual[i][1]==expected[i][1],
                "wrong workstation slot position for "+type+" at "+i);
    }

    private static void resource(String path) {
        InputStream stream = VillagePoiContractFixture120.class.getResourceAsStream(path);
        require(stream != null, "missing resource " + path);
        try { stream.close(); } catch (Exception error) { throw new IllegalStateException(error); }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
    private VillagePoiContractFixture120() { }
}
