package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.ModelTrident;
import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.RenderThrownTrident;
import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.TreasureMapItemRenderer120;
import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.TridentItemRenderer;
import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.TurtleEggRenderHandler120;

import com.mod.htgt6.asm.HTGT6Config;
import com.mod.htgt6.asm.HTGT6Transformer;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.SpyglassItemRenderer120;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.Backport120ClientHandler;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMap120;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.IItemRenderer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/** Headless checks for the exact 1.20.2 trident transforms and RED_X sprite adapter. */
public final class AquaticClientContractFixture120 {
    private static final String RED_X_ARGB_SHA256 =
            "A085B3C290B821F3B349CE35D346F0412967D7045BB92A3B146EADCFDE8E129E";
    private static final String ENTITY_TRIDENT_ARGB_SHA256 =
            "93023D3D713ABD7F47F3FE920C747AC090C2C65275CE185C8964A1FE1FDB99CA";
    private static final String ITEM_TRIDENT_ARGB_SHA256 =
            "8F35FBD09B9E767C1406EE48567E2A53BC29622DD4AE8197F208A6EA35AEB47D";
    private static final String RIPTIDE_ARGB_SHA256 =
            "0E620DF04D831B2DF2D137CB65A86A8175BBE36AFED4FE0F8752B73A4C82D090";
    private static final String DROWNED_OUTER_ARGB_SHA256 =
            "9351AEE97029AA38A958826232DCFC23261D154BAB374F54F42046AF69073486";
    private static final String TURTLE_EGG_ARGB_SHA256 =
            "C915CC38E20C5D19BF101CB25EE0A7B0A9FEA39AD5D6875D21D9B379C3342768";
    private static final String TURTLE_EGG_SLIGHTLY_CRACKED_ARGB_SHA256 =
            "05E81B21FEF1CCDF63A389F2F1332AB1DB6642C2B304A82D5BA0AD8C88E10E76";
    private static final String TURTLE_EGG_VERY_CRACKED_ARGB_SHA256 =
            "197AB3B60368D02B57CB239A503ECEA4F633FA06ED6F8A606E7BD2D540FBA719";
    private static final String SCUTE_ARGB_SHA256 =
            "9109F4044B475E4D6311FFB456A6F7751858799145C8029B08724E21EBB14310";
    private static final String TREASURE_SCROLL_ARGB_SHA256 =
            "0CB4E4D68119EBEA8D20AA467601234027F5D1E8371D207CB4D6A427B33FC83C";
    private static final String FILLED_MAP_TEXTURE_SHA256 =
            "22E0F1856B995AA6A3F71525BBA330EC31E739506D5565F52C05A0C60F93DB42";
    private static final String FILLED_MAP_MARKINGS_SHA256 =
            "7E01CC208DA72F12DC8B1CC30548874D3E984E8CA813E38F893AD092140CA3B0";
    private static final String FILLED_MAP_MODEL_SHA256 =
            "5A79888FF2D5518D6FD3BF62C3231167546FAACFBA411876070BA84A90075B70";
    private static final String SPYGLASS_TEXTURE_SHA256 =
            "B09E9B9046DB1935CD9C35A2E4E3B7763485EC3EFFBC5077EBEAEC1C28513B43";
    private static final String SPYGLASS_MODEL_TEXTURE_SHA256 =
            "EA13F6CA8EB8F96AB8879EE393C6D00581B6B54731B0163054AC1F9E321E6FE9";
    private static final String SPYGLASS_SCOPE_SHA256 =
            "736552955005A663AEAB6142825E7A57BE8C135687BF57A170F6C44911610192";

    public static void assertContract() throws Exception {
        require(ModelTrident.REFERENCE_GEOMETRY.length == 5, "trident part count");
        requireArray(ModelTrident.REFERENCE_GEOMETRY[0],
                new float[]{0, 6, -0.5F, 2, -0.5F, 1, 25, 1}, "trident pole");
        requireArray(ModelTrident.REFERENCE_GEOMETRY[4],
                new float[]{4, 3, 1.5F, -3, -0.5F, 1, 4, 1}, "trident right tine");
        require(ModelTrident.REFERENCE_MIRROR.length == 5
                        && !ModelTrident.REFERENCE_MIRROR[0]
                        && ModelTrident.REFERENCE_MIRROR[4],
                "trident mirrored right tine UV");
        require(RenderThrownTrident.REFERENCE_YAW_OFFSET == -90.0F
                        && RenderThrownTrident.REFERENCE_PITCH_OFFSET == 90.0F,
                "thrown trident orientation");
        requireArray(TridentItemRenderer.FIRST_PERSON,
                new float[]{-3, 17, 1, 0, -90, 25, 1}, "first-person transform");
        requireArray(TridentItemRenderer.THIRD_PERSON_HELD,
                new float[]{11, 17, -2, 0, 60, 0, 1}, "third-person held transform");
        requireArray(TridentItemRenderer.THIRD_PERSON_THROWING,
                new float[]{8, -17, 9, 0, 90, 180, 1}, "third-person throwing transform");
        requireArray(TridentItemRenderer.GROUND,
                new float[]{4, 4, 2, 0, 0, 0, 0.25F}, "ground transform");
        require(TurtleEggRenderHandler120.REFERENCE_CUBES.length == 4,
                "four official turtle egg model elements");
        requireIntArray(TurtleEggRenderHandler120.REFERENCE_CUBES[0],
                new int[]{5, 0, 4, 9, 7, 8}, "first turtle egg cuboid");
        requireIntArray(TurtleEggRenderHandler120.REFERENCE_CUBES[3],
                new int[]{6, 0, 9, 10, 4, 13}, "fourth turtle egg cuboid");

        InputStream stream = AquaticClientContractFixture120.class.getResourceAsStream(
                "/assets/minecraft/textures/map/map_icons.png");
        require(stream != null, "1.7-compatible map icon atlas");
        BufferedImage atlas;
        try {
            atlas = ImageIO.read(stream);
        } finally {
            stream.close();
        }
        require(atlas != null && atlas.getWidth() == 32 && atlas.getHeight() == 32,
                "map icon atlas dimensions");
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (int y = 8; y < 16; y++) for (int x = 24; x < 32; x++) {
            int argb = atlas.getRGB(x, y);
            digest.update((byte)argb);
            digest.update((byte)(argb >>> 8));
            digest.update((byte)(argb >>> 16));
            digest.update((byte)(argb >>> 24));
        }
        require(RED_X_ARGB_SHA256.equals(hex(digest.digest())),
                "official 1.20.2 RED_X pixels in reserved 1.7 atlas slot");
        requireTexture("/assets/htgt6/textures/entity/trident.png", 32, 32,
                ENTITY_TRIDENT_ARGB_SHA256, "official entity trident texture");
        requireTexture("/assets/htgt6/textures/items/trident.png", 16, 16,
                ITEM_TRIDENT_ARGB_SHA256, "official item trident texture");
        requireTexture("/assets/htgt6/textures/entity/trident_riptide.png", 64, 64,
                RIPTIDE_ARGB_SHA256, "official Riptide effect texture");
        requireTexture("/assets/htgt6/textures/entity/zombie/drowned_outer_layer.png", 64, 64,
                DROWNED_OUTER_ARGB_SHA256, "official drowned outer-layer texture");
        requireTexture("/assets/htgt6/textures/blocks/turtle_egg.png", 16, 16,
                TURTLE_EGG_ARGB_SHA256, "official turtle egg texture");
        requireTexture("/assets/htgt6/textures/blocks/turtle_egg_slightly_cracked.png", 16, 16,
                TURTLE_EGG_SLIGHTLY_CRACKED_ARGB_SHA256, "official slightly cracked turtle egg texture");
        requireTexture("/assets/htgt6/textures/blocks/turtle_egg_very_cracked.png", 16, 16,
                TURTLE_EGG_VERY_CRACKED_ARGB_SHA256, "official very cracked turtle egg texture");
        requireTexture("/assets/htgt6/textures/items/scute.png", 16, 16,
                SCUTE_ARGB_SHA256, "official scute texture");
        requireTexture("/assets/htgt6/textures/items/buried_treasure_map_scroll.png", 16, 16,
                TREASURE_SCROLL_ARGB_SHA256, "legacy map icon fallback for sealed scroll");
        requireReferenceTreasureMapAndSpyglass();
        requireReferenceAquaticAssets();
        requireModelBipedPoseTransform();
    }

    /** Exact 1.20.2 item assets plus the 1.7 client registration/dispatch adapter. */
    private static void requireReferenceTreasureMapAndSpyglass() throws Exception {
        requireResourceHash("/assets/htgt6/models/item/treasure_map/filled_map.json",
                FILLED_MAP_MODEL_SHA256);
        requireResourceHash("/assets/htgt6/textures/items/treasure_map/filled_map.png",
                FILLED_MAP_TEXTURE_SHA256);
        requireResourceHash("/assets/htgt6/textures/items/treasure_map/filled_map_markings.png",
                FILLED_MAP_MARKINGS_SHA256);
        requireResourceHash("/assets/htgt6/models/item/buried_treasure_map_scroll.json",
                "AF7B5E89F9734DDD2620FED31D9A9397413BD20BB4EB5FB367056E94784F4709");
        requireResourceHash("/assets/htgt6/models/item/1_20_backport/spyglass.json",
                "28DC42FEA0597F152E57B4AE4E0D89AD3827790DB72A1EACDD9FD0D0EA3097ED");
        requireResourceHash("/assets/htgt6/models/item/1_20_backport/spyglass_in_hand.json",
                "E4B41FF9D7CBB529A243B49785385DAC97D1BF4C06542DFE5B7DCF511F9232CC");
        requireResourceHash("/assets/htgt6/textures/items/1_20_backport/spyglass.png",
                SPYGLASS_TEXTURE_SHA256);
        requireResourceHash("/assets/htgt6/textures/items/1_20_backport/spyglass_model.png",
                SPYGLASS_MODEL_TEXTURE_SHA256);
        requireResourceHash("/assets/htgt6/textures/misc/spyglass_scope.png", SPYGLASS_SCOPE_SHA256);
        requireResourceHash("/assets/htgt6/sounds/item/spyglass/use.ogg",
                "28B93002EE6A2D78107056A0FC84212E1B4A15917A3E45BA5BA4067B2395BD2E");
        requireResourceHash("/assets/htgt6/sounds/item/spyglass/stop.ogg",
                "6C6AED7907357216A7BEF753E2289AB460C2B03E49A3D89778CB5D21D258A49C");
        requireResourceText("/assets/htgt6/sounds.json", "item.spyglass.use",
                "item/spyglass/use", "item.spyglass.stop_using", "item/spyglass/stop");

        java.lang.reflect.Field elementsField = SpyglassItemRenderer120.class.getDeclaredField("ELEMENTS");
        elementsField.setAccessible(true);
        float[][] spyglassElements = (float[][])elementsField.get(null);
        require(spyglassElements.length == 2,
                "official spyglass in-hand element count");
        requireArray(spyglassElements[0],
                new float[]{7, 8.5F, 7, 9, 13.5F, 9, 2, 7}, "spyglass upper tube geometry");
        requireArray(spyglassElements[1],
                new float[]{6.9F, 2.4F, 6.9F, 9.1F, 8.6F, 9.1F, 7, 13},
                "spyglass lower tube geometry");
        ItemStack spyglass = new ItemStack(Backport120Content.spyglass);
        require(spyglass.getMaxStackSize() == 1 && spyglass.getMaxItemUseDuration() == 1200,
                "spyglass stack/use duration");
        SpyglassItemRenderer120 spyglassRenderer = new SpyglassItemRenderer120();
        require(!spyglassRenderer.handleRenderType(spyglass, IItemRenderer.ItemRenderType.INVENTORY)
                        && spyglassRenderer.handleRenderType(spyglass,
                        IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON)
                        && spyglassRenderer.handleRenderType(spyglass,
                        IItemRenderer.ItemRenderType.EQUIPPED),
                "flat inventory and 3D in-hand spyglass model selection");
        require(Backport120ClientHandler.SCOPE_RESET==0.5F
                        && Backport120ClientHandler.SCOPE_TARGET==1.125F
                        && Backport120ClientHandler.SCOPE_LERP==0.5F,
                "official spyglass scope interpolation");
        require(SpyglassItemRenderer120.THIRD_PERSON_TRANSLATE_Y==-2F/16F,
                "official spyglass third-person display translation");

        TreasureMapItemRenderer120 mapRenderer = new TreasureMapItemRenderer120();
        ItemStack ordinaryMap = new ItemStack(Items.filled_map);
        require(!mapRenderer.handleRenderType(ordinaryMap, IItemRenderer.ItemRenderType.INVENTORY),
                "ordinary maps retain the vanilla renderer");
        ItemStack treasureMap = new ItemStack(Items.filled_map);
        treasureMap.setTagCompound(new NBTTagCompound());
        treasureMap.getTagCompound().setBoolean("HTGT6BuriedTreasureMap", true);
        treasureMap.getTagCompound().setString("TargetKind", "buried_treasure");
        require(VanillaTreasureMap120.isTreasureMap(treasureMap)
                        && mapRenderer.handleRenderType(treasureMap,
                        IItemRenderer.ItemRenderType.INVENTORY)
                        && mapRenderer.handleRenderType(treasureMap,
                        IItemRenderer.ItemRenderType.EQUIPPED)
                        && !mapRenderer.handleRenderType(treasureMap,
                        IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON),
                "treasure item model and vanilla first-person MapData renderer dispatch");

        requireRendererRegistration(com.mod.htgt6.minecraft1202.Module.Client.Aquatic.AquaticUpdateClient.class,
                "com/mod/htgt6/minecraft1202/Module/Client/Aquatic/TreasureMapItemRenderer120");
        require(com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent.treasureMapScroll.requiresMultipleRenderPasses(), "sealed map uses the two original icon layers");
        requireRendererRegistration(com.mod.htgt6.minecraft1202.Module.Client.Modern.Minecraft120BackportClient.class,
                "com/mod/htgt6/minecraft1202/Module/Client/Modern/SpyglassItemRenderer120");
        requireNewAndEventRegistration(com.mod.htgt6.minecraft1202.Module.Client.Modern.Minecraft120BackportClient.class,
                "com/mod/htgt6/minecraft1202/Module/Client/Modern/Backport120ClientHandler");
        requireStringConstants(Backport120Content.spyglass.getClass(),
                "htgt6:item.spyglass.use", "htgt6:item.spyglass.stop_using");
    }

    private static void requireResourceText(String path, String... fragments) throws Exception {
        InputStream stream = AquaticClientContractFixture120.class.getResourceAsStream(path);
        require(stream != null, "resource " + path);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[2048];
            for (int count; (count = stream.read(buffer)) >= 0;) output.write(buffer, 0, count);
        } finally { stream.close(); }
        String text = new String(output.toByteArray(), "UTF-8");
        for (String fragment : fragments) require(text.contains(fragment),
                "resource wiring " + path + " -> " + fragment);
    }

    private static ClassNode classNode(Class<?> type) throws Exception {
        InputStream stream = type.getResourceAsStream(type.getSimpleName() + ".class");
        require(stream != null, "bytecode for " + type.getName());
        try {
            ClassNode node = new ClassNode();
            new ClassReader(stream).accept(node, 0);
            return node;
        } finally { stream.close(); }
    }

    private static void requireRendererRegistration(Class<?> owner, String renderer) throws Exception {
        boolean constructed = false, registered = false;
        for (MethodNode method : classNode(owner).methods) for (AbstractInsnNode instruction
                : method.instructions.toArray()) {
            if (instruction instanceof TypeInsnNode && instruction.getOpcode() == org.objectweb.asm.Opcodes.NEW
                    && renderer.equals(((TypeInsnNode)instruction).desc)) constructed = true;
            if (instruction instanceof MethodInsnNode
                    && "net/minecraftforge/client/MinecraftForgeClient".equals(
                    ((MethodInsnNode)instruction).owner)
                    && "registerItemRenderer".equals(((MethodInsnNode)instruction).name)) registered = true;
        }
        require(constructed && registered, "item renderer registration " + renderer);
    }

    private static void requireNewAndEventRegistration(Class<?> owner, String handler) throws Exception {
        boolean constructed = false, registered = false;
        for (MethodNode method : classNode(owner).methods) for (AbstractInsnNode instruction
                : method.instructions.toArray()) {
            if (instruction instanceof TypeInsnNode && instruction.getOpcode() == org.objectweb.asm.Opcodes.NEW
                    && handler.equals(((TypeInsnNode)instruction).desc)) constructed = true;
            if (instruction instanceof MethodInsnNode && "register".equals(
                    ((MethodInsnNode)instruction).name)) registered = true;
        }
        require(constructed && registered, "event registration " + handler);
    }

    private static void requireStringConstants(Class<?> owner, String... expected) throws Exception {
        java.util.Set<String> constants = new java.util.HashSet<String>();
        for (MethodNode method : classNode(owner).methods) for (AbstractInsnNode instruction
                : method.instructions.toArray()) if (instruction instanceof LdcInsnNode
                && ((LdcInsnNode)instruction).cst instanceof String) {
            constants.add((String)((LdcInsnNode)instruction).cst);
        }
        for (String value : expected) require(constants.contains(value),
                "bytecode sound event " + value);
    }

    /** Pixel contracts for every remaining 1.20.2 Aquatic texture used by this module. */
    private static void requireReferenceAquaticAssets() throws Exception {
        String[][] assets = {
                {"blocks/brain_coral_block.png", "16", "16", "36CB1E6EAAB132C049635C185B9615BF90A4AED79BBB840AA71C1E6E2B6F7ACE"},
                {"blocks/brain_coral.png", "16", "16", "07611AB511E4676F03A4D1DEA520A67BAAC9E4B29DCE914C90457784691022B0"},
                {"blocks/brain_coral_fan.png", "16", "16", "080FC1CA0CFC13B6C634AC36DA5DD27B3EB5D96857CE8E0A9EB3B04C2A5258F9"},
                {"blocks/bubble_coral_block.png", "16", "16", "B3CBB8BB742F4648E7BF0877D4C791D7071552BE640944133E1A2034814F3BA5"},
                {"blocks/bubble_coral.png", "16", "16", "3B51F4236B15FEE9C2C17D2B80E647DE5A1C2B1217A592153108B595E668F2EF"},
                {"blocks/bubble_coral_fan.png", "16", "16", "1CA112C8D0716BCBE02EB18C8211D6B3ED03A0A54C35F2F8136DD92D2B131F03"},
                {"blocks/dead_brain_coral_block.png", "16", "16", "32D57BBE333C93D79ADD38982C6C07FB5B35DF648547B13894F578B261E2B748"},
                {"blocks/dead_bubble_coral_block.png", "16", "16", "89303464C44952B3AC7B9EC5104395C48661180F912EA3B67DFFDA2DD702666D"},
                {"blocks/dead_fire_coral_block.png", "16", "16", "6E792B9F897673E97A0FBBCFFDC7F60A51994439EDDD9C7037029A21E16ED109"},
                {"blocks/dead_horn_coral_block.png", "16", "16", "01FF21325AD05F07F8A7B83B5AFDD6BD86E6E313BA52DE88D3F7AD7C94AE20BA"},
                {"blocks/dead_tube_coral_block.png", "16", "16", "1E3AB15E3D45582CC1734DFB5554D865ECD62E7F95AD3DDA3FA8A96BDBC502E6"},
                {"blocks/fire_coral_block.png", "16", "16", "D5EB4585A3EF4ABF96CDC35FB218A6B0339FECD39812095C6AF10FF25E26775B"},
                {"blocks/fire_coral.png", "16", "16", "E68EBABAD66B149FE13260182C8881BCD17D97D26BED1314B9CE2CD02B4D3532"},
                {"blocks/fire_coral_fan.png", "16", "16", "F1D31B261A1D53396AB08D51C22E9D9794022F7C7CE7743039E2E0D52B3ECF66"},
                {"blocks/horn_coral_block.png", "16", "16", "28B4EF4BBBC7DE71E9F569C04041FFC691173E3771F88C9BB96C70CE15D379CA"},
                {"blocks/horn_coral.png", "16", "16", "2C955E1D82C867CEAF418F62558369EF1788577A7D74010B0874C0363CC94E73"},
                {"blocks/horn_coral_fan.png", "16", "16", "DFA131B21FF3E39CD8CBAF93389A378826197F7B37DFBFA9E5E79B0FFE432F4F"},
                {"blocks/kelp_plant.png", "16", "320", "57D6CC4526867170BE987506C7059662E0D1CAF7CB0A075D8D11A5C2CE03903B"},
                {"blocks/kelp.png", "16", "320", "DAF446B8C6B0EE2A62D18047F08DFB12CF1878F4FD708D28CD3563B5A41A505C"},
                {"blocks/sea_pickle.png", "16", "16", "E662132CBEB67070E73BB734CA9E4BB2EB338C2DB0B7620C54093FAFDEC2C336"},
                {"blocks/seagrass.png", "16", "288", "20BAE8920262AB640B43DC07BD40A0DFFC5FEB260215122AD92B31B069C47839"},
                {"blocks/tall_seagrass_bottom.png", "16", "304", "A892D7C2D084ECEB6022E80B0AC0FF42639C86EE706C46119DE5ECC39F7C27FA"},
                {"blocks/tall_seagrass_top.png", "16", "304", "245844F0B2474831F99508EA0949058FACA7ACE571C74C310B40907704DB31AE"},
                {"blocks/tube_coral_block.png", "16", "16", "A1E9ECCAEFAECD4652F7D3620F628B2756EF7475DA65E43182A0D376369DFC08"},
                {"blocks/tube_coral.png", "16", "16", "D35CF3361C21E2F54D77DC890BBFF436C0EA9954BB77922C144BCBCD4D6243A4"},
                {"blocks/tube_coral_fan.png", "16", "16", "C1C8B07461A274BFF3B0942E6D18EAF80CDA32A491DFF8F0BA9C5D9A83CA8796"},
                {"entity/turtle/big_sea_turtle.png", "128", "64", "03442DE40906AF9898AE0197DD4C8DCF0873CD6B88A9E3D18D8B8369EF683B64"},
                {"entity/zombie/drowned.png", "64", "64", "F320AF0DBEB49E88F4BD64839CAE1F38CDA981CC558D171FDF41455D669A0FE1"},
                {"items/1_20_backport/heart_of_the_sea.png", "16", "16", "753F24B00A6B61E46A2733B37ABEC5B14B8816962EE21A3E79148BF9F71E969D"}
        };
        for (String[] asset : assets) requireTexture("/assets/htgt6/textures/" + asset[0],
                Integer.parseInt(asset[1]), Integer.parseInt(asset[2]), asset[3],
                "official Aquatic asset " + asset[0]);
        requireResourceHash("/assets/htgt6/textures/blocks/kelp.png.mcmeta",
                "E9B8025FDE1B09B0A4D2A38427B04F98F28A593045BE6D177EB2B29164C872B0");
        requireResourceHash("/assets/htgt6/textures/blocks/kelp_plant.png.mcmeta",
                "E9B8025FDE1B09B0A4D2A38427B04F98F28A593045BE6D177EB2B29164C872B0");
        String grassAnimation = "3BF9BE6363938CE6E546AADD80F6BA1EB538FE64AA97FF05EE35B883C3A4A657";
        requireResourceHash("/assets/htgt6/textures/blocks/seagrass.png.mcmeta", grassAnimation);
        requireResourceHash("/assets/htgt6/textures/blocks/tall_seagrass_bottom.png.mcmeta", grassAnimation);
        requireResourceHash("/assets/htgt6/textures/blocks/tall_seagrass_top.png.mcmeta", grassAnimation);
    }

    private static void requireResourceHash(String path, String expected) throws Exception {
        InputStream stream = path.contains("/sounds/")?com.mod.htgt6.minecraft1202.Module.Data.Modern.ExternalSoundFixture179.open(path.substring(1)):AquaticClientContractFixture120.class.getResourceAsStream(path);
        require(stream != null, "official asset metadata " + path);
        ByteArrayOutputStream output=new ByteArrayOutputStream();
        try { byte[] buffer=new byte[4096];for(int n;(n=stream.read(buffer))>=0;)output.write(buffer,0,n); }
        finally { stream.close(); }
        byte[] bytes=output.toByteArray();
        if(expected.equals(hex(MessageDigest.getInstance("SHA-256").digest(bytes))))return;
        if(path.endsWith("/buried_treasure_map_scroll.json")) {
            String scroll="{\"parent\":\"item/generated\",\"textures\":{\"layer0\":\"htgt6:items/buried_treasure_map_scroll\"},\"display\":{\"thirdperson_righthand\":{\"rotation\":[0,0,15],\"translation\":[2.4,0.8,0]},\"firstperson_righthand\":{\"rotation\":[0,0,-20],\"translation\":[2.4,3.2,0]},\"ground\":{\"translation\":[0,4,0]}}}";
            com.google.gson.JsonParser parser=new com.google.gson.JsonParser();
            require(parser.parse(new String(bytes,"UTF-8")).equals(parser.parse(scroll)),"custom scroll model content");return;
        }
        String name=path.substring(path.lastIndexOf('/')+1);
        String official=(path.contains("/models/")?"models/item/":path.contains("/textures/blocks/")?"textures/block/":path.contains("/textures/misc/")?"textures/misc/":"textures/item/")+name;
        require(com.mod.htgt6.minecraft1202.Module.Data.Shared.ResourceOracle120.matches(bytes,official),"official asset content "+path);
    }

    private static void requireModelBipedPoseTransform() throws Exception {
        InputStream stream = ModelBiped.class.getResourceAsStream("ModelBiped.class");
        require(stream != null, "ModelBiped bytecode fixture");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[8192];
            for (int count; (count = stream.read(buffer)) >= 0;) output.write(buffer, 0, count);
        } finally {
            stream.close();
        }
        boolean previous = HTGT6Config.RUN_MINECRAFT_1_7_9999;
        HTGT6Config.RUN_MINECRAFT_1_7_9999 = true;
        byte[] transformed;
        try {
            transformed = new HTGT6Transformer().transform(
                    "net.minecraft.client.model.ModelBiped",
                    "net.minecraft.client.model.ModelBiped", output.toByteArray());
        } finally {
            HTGT6Config.RUN_MINECRAFT_1_7_9999 = previous;
        }
        ClassNode node = new ClassNode();
        new ClassReader(transformed).accept(node, 0);
        int hooks = 0;
        for (MethodNode method : node.methods) for (AbstractInsnNode instruction
                : method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode
                    && "apply".equals(((MethodInsnNode)instruction).name)
                    && ((MethodInsnNode)instruction).owner.endsWith("TridentPlayerPoseHooks")) hooks++;
        }
        require(hooks == 1, "ModelBiped THROW_SPEAR ASM hook");
    }

    private static void requireTexture(String path, int width, int height, String hash,
                                       String name) throws Exception {
        InputStream stream = AquaticClientContractFixture120.class.getResourceAsStream(path);
        require(stream != null, name);
        BufferedImage image;
        try {
            image = ImageIO.read(stream);
        } finally {
            stream.close();
        }
        require(image != null && image.getWidth() == width && image.getHeight() == height, name);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
            int argb = image.getRGB(x, y);
            digest.update((byte)argb);
            digest.update((byte)(argb >>> 8));
            digest.update((byte)(argb >>> 16));
            digest.update((byte)(argb >>> 24));
        }
        require(hash.equals(hex(digest.digest())), name);
    }

    private static String hex(byte[] value) {
        StringBuilder result = new StringBuilder(value.length * 2);
        for (byte b : value) result.append(String.format("%02X", b & 0xFF));
        return result.toString();
    }

    private static void requireArray(float[] actual, float[] expected, String name) {
        require(actual.length == expected.length, name);
        for (int i = 0; i < expected.length; i++) require(actual[i] == expected[i], name);
    }

    private static void requireIntArray(int[] actual, int[] expected, String name) {
        require(actual.length == expected.length, name);
        for (int i = 0; i < expected.length; i++) require(actual[i] == expected[i], name);
    }

    private static void require(boolean condition, String name) {
        if (!condition) throw new IllegalStateException(name + " differs from the 1.20.2 contract");
    }

    public static void main(String[] ignored) throws Exception {
        assertContract();
        System.out.println("Aquatic client rendering 1.20.2 contract OK");
    }

    private AquaticClientContractFixture120() { }
}
