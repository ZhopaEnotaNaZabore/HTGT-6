package com.mod.htgt6.minecraft1202.Module.Data.Combat;

import com.mod.htgt6.minecraft1202.Module.Client.Combat.ShieldRenderTransforms120;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.ShieldItemRenderer;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.ShieldBannerData120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.ShieldMechanics120;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Inventory.OffhandModule112;
import com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketOffhandSync;
import com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketSwapOffhand;
import com.mod.htgt6.client.inventory112.Inventory112ClientHandler;
import com.mod.htgt6.client.inventory112.OffhandRenderer112;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.client.IItemRenderer;

/** Headless parity checks for the shield constants and calculations used at runtime. */
public final class ShieldMechanicsFixture120 {
    public static void main(String[] args) {
        require(args.length == 2, "expected resources and 1.20.2 client jar");
        com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID = 24;
        com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID = 25;
        com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID = 26;
        com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID = 27;
        require(ShieldMechanics120.USE_DURATION == 72000, "use duration");
        require(ShieldMechanics120.EFFECTIVE_BLOCK_DELAY == 5, "effective block delay");
        require(ShieldMechanics120.DISABLE_COOLDOWN == 100, "axe cooldown");
        require(ShieldMechanics120.MAX_DAMAGE == 336, "durability");
        require(ShieldMechanics120.durabilityDamage(2.99F) == 0, "minimum durability damage");
        require(ShieldMechanics120.durabilityDamage(3.0F) == 4, "durability rounding at threshold");
        require(ShieldMechanics120.durabilityDamage(7.9F) == 8, "durability flooring");
        require(ShieldMechanics120.isInFront(0.0D, 1.0D, 0.0F), "front hit");
        require(!ShieldMechanics120.isInFront(0.0D, -1.0D, 0.0F), "rear hit");
        require(!ShieldMechanics120.isInFront(1.0D, 0.0D, 0.0F), "exact side hit is outside shield hemisphere");
        require(!ShieldMechanics120.isInFront(0.0D, 0.0D, 0.0F), "coincident source is outside shield hemisphere");
        require(ShieldMechanics120.isInFront(-1.0D, 0.0D, 90.0F), "rotated front hit");
        require(ShieldMechanics120.disableChance(0, true) >= 1.0F, "axe always disables");
        require(Math.abs(ShieldMechanics120.disableChance(3, false) - 0.4F) < 0.0001F,
                "disable chance formula");
        require(ShieldMechanics120.isPlankRegistryName("minecraft:planks"), "vanilla plank repair");
        require(ShieldMechanics120.isPlankRegistryName("htgt6:cherry_planks"), "modern plank repair");
        require(!ShieldMechanics120.isPlankRegistryName("minecraft:log"), "non-plank repair rejection");
        verifyBannerDecoration();
        verifyRendererAssets(new File(args[0]), new File(args[1]));
        verifyRenderTransforms();
        verifyLegacyApi();
        verifySwapKeyEdge();
        verifySwapProtocol();
        verifyNativeOffhandModule();
        System.out.println("ShieldMechanicsFixture120: OK");
    }

    private static void verifyBannerDecoration() {
        ItemStack banner = new ItemStack(BountifulContent.banner, 1, 14);
        NBTTagCompound bannerRoot = new NBTTagCompound();
        NBTTagCompound bannerBlock = new NBTTagCompound();
        NBTTagList patterns = new NBTTagList();
        for (int i = 0; i < 8; ++i) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setString("Pattern", i == 0 ? "cr" : "bo");
            entry.setInteger("Color", i & 15);
            patterns.appendTag(entry);
        }
        bannerBlock.setTag("Patterns", patterns);
        bannerRoot.setTag("BlockEntityTag", bannerBlock);
        banner.setTagCompound(bannerRoot);
        ItemStack shield = new ItemStack(CombatContent.shield);
        ShieldBannerData120.copyBannerDecoration(shield, banner);
        require(ShieldBannerData120.hasDecoration(shield), "decorated shield marker");
        require(ShieldBannerData120.baseColor(shield) == 14, "banner base colour copy");
        require(ShieldBannerData120.patterns(shield).tagCount() == 6, "six-layer shield limit");
        bannerBlock.setInteger("Base", 3);
        ItemStack explicit = new ItemStack(CombatContent.shield);
        ShieldBannerData120.copyBannerDecoration(explicit, banner);
        require(ShieldBannerData120.baseColor(explicit) == 3, "explicit banner base colour");
    }

    private static void verifyLegacyApi() {
        try {
            // Allocate a real player without starting a World/server. These API methods only read itemInUse.
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field instance = unsafeClass.getDeclaredField("theUnsafe");
            instance.setAccessible(true);
            net.minecraft.entity.player.EntityPlayer player = (net.minecraft.entity.player.EntityPlayer)
                    unsafeClass.getMethod("allocateInstance", Class.class).invoke(instance.get(null),
                            net.minecraft.entity.player.EntityPlayerMP.class);
            Field used = cpw.mods.fml.relauncher.ReflectionHelper.findField(
                    net.minecraft.entity.player.EntityPlayer.class, "itemInUse", "field_71074_e");
            player.inventory = new FixtureInventory(player);
            used.set(player, new ItemStack(CombatContent.shield));
            require(player.isBlocking(), "shield retains legacy render/use action");
            require(!ShieldMechanics120.usesLegacyDamageBlock(player), "shield must not halve unblocked damage again");
            used.set(player, new ItemStack(new net.minecraft.item.ItemSword(net.minecraft.item.Item.ToolMaterial.IRON)));
            require(!ShieldMechanics120.usesLegacyDamageBlock(player), "Combat Update removes sword blocking");
            com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks.setOffhand(player, new ItemStack(CombatContent.shield));
            require(!ShieldMechanics120.usesLegacyDamageBlock(player), "sword cannot bypass offhand shield delay or cooldown");
            com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks.setOffhand(player, null);
            Field raised = Inventory112ClientHandler.class.getDeclaredField("localShieldRaised");
            raised.setAccessible(true);
            try {
                for (boolean mainUse : new boolean[]{false, true}) {
                    used.set(player, mainUse ? new ItemStack(CombatContent.shield) : null);
                    for (boolean offhandUse : new boolean[]{false, true}) {
                        raised.setBoolean(null, offhandUse);
                        require(Inventory112ClientHandler.isUsingItemForMovement(player) == (mainUse || offhandUse),
                                "single vanilla slowdown decision for main=" + mainUse + ", offhand=" + offhandUse);
                    }
                }
            } finally { raised.setBoolean(null, false); }
            used.set(player, null);
            require(!ShieldMechanics120.usesLegacyDamageBlock(player), "idle player has no legacy block");
        } catch (Exception error) { throw new AssertionError("Shield 1.7 API integration", error); }
    }

    private static void verifyRendererAssets(File resources, File clientJar) {
        require(IItemRenderer.class.isAssignableFrom(ShieldItemRenderer.class), "custom shield item renderer");
        File root = new File(resources, "assets/htgt6/textures/entity/combat_update");
        require(new File(root, "shield_base_nopattern.png").isFile(), "plain shield texture");
        require(new File(root, "shield_base.png").isFile(), "decorated shield base texture");
        String[] patterns = {"base", "cross", "border", "bricks", "creeper", "flower", "globe",
                "mojang", "piglin", "skull", "gradient", "stripe_center", "triangle_top"};
        for (String pattern : patterns)
            require(new File(root, "shield/" + pattern + ".png").isFile(), "shield layer " + pattern);
        File[] layers = new File(root, "shield").listFiles();
        require(layers != null && layers.length == 41, "complete official shield layer set");
        try {
            ZipFile jar = new ZipFile(clientJar);
            try {
                verifyReferenceTransforms(jar, "shield", false);
                verifyReferenceTransforms(jar, "shield_blocking", true);
                compare(jar, new File(root, "shield_base.png"),
                        "assets/minecraft/textures/entity/shield_base.png");
                for (File layer : layers) compare(jar, layer,
                        "assets/minecraft/textures/entity/shield/" + layer.getName());
            } finally { jar.close(); }
        } catch (Exception error) {
            throw new AssertionError("Official 1.20.2 shield resource mismatch", error);
        }
    }

    private static final class FixtureInventory extends net.minecraft.entity.player.InventoryPlayer
            implements com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.InventoryPlayer112 {
        private ItemStack[] offhand = new ItemStack[1];
        private java.util.List<ItemStack[]> compartments;
        FixtureInventory(net.minecraft.entity.player.EntityPlayer player) { super(player); }
        public ItemStack[] htgt6$getOffHandInventory() { return offhand; }
        public void htgt6$setOffHandInventory(ItemStack[] value) { offhand = value; }
        public java.util.List<ItemStack[]> htgt6$getAllInventories() { return compartments; }
        public void htgt6$setAllInventories(java.util.List<ItemStack[]> value) { compartments = value; }
    }

    private static void compare(ZipFile jar, File local, String entryName) throws Exception {
        ZipEntry entry = jar.getEntry(entryName);
        require(entry != null, "reference shield asset " + entryName);
        require(com.mod.htgt6.minecraft1202.Module.Data.Shared.ResourceOracle120.matches(local,entryName.substring("assets/minecraft/".length())),
                "pixel-identical shield asset " + entryName);
    }

    private static byte[] read(InputStream input) throws Exception {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096]; int count;
            while ((count = input.read(buffer)) >= 0) output.write(buffer, 0, count);
            return output.toByteArray();
        } finally { input.close(); }
    }

    private static void verifyRenderTransforms() {
        require(ShieldRenderTransforms120.FIRST_PERSON_17_ADAPTER_SCALE == 1.0F,
                "1.7 first-person item-space adapter scale");
        require(ShieldRenderTransforms120.FIRST_PERSON_LEFT.x == 10.0F
                        && ShieldRenderTransforms120.FIRST_PERSON_LEFT.rotationY == 180.0F
                        && ShieldRenderTransforms120.FIRST_PERSON_LEFT.rotationZ == 5.0F,
                "idle first-person left-hand transform");
        require(ShieldRenderTransforms120.FIRST_PERSON_LEFT_BLOCKING.x == 5.0F
                        && ShieldRenderTransforms120.FIRST_PERSON_LEFT_BLOCKING.rotationZ == -5.0F,
                "blocking first-person left-hand transform");
        require(ShieldRenderTransforms120.THIRD_PERSON_LEFT.x == 10.0F
                        && ShieldRenderTransforms120.THIRD_PERSON_LEFT.rotationY == 90.0F,
                "idle third-person left-hand transform");
        require(ShieldRenderTransforms120.THIRD_PERSON_LEFT_BLOCKING.x == 13.51F
                        && ShieldRenderTransforms120.THIRD_PERSON_LEFT_BLOCKING.rotationY == 135.0F,
                "blocking third-person left-hand transform");
        require(ShieldRenderTransforms120.FIRST_PERSON_RIGHT.x == -10.0F
                        &&ShieldRenderTransforms120.FIRST_PERSON_RIGHT_BLOCKING.x == -15.0F,
                "right-hand first-person transforms");
        require(ShieldRenderTransforms120.THIRD_PERSON_RIGHT.z == -4.0F
                        &&ShieldRenderTransforms120.THIRD_PERSON_RIGHT_BLOCKING.x == 3.51F,
                "right-hand third-person transforms");
        require(OffhandRenderer112.cooldownOverlayHeight(0, 100) == 0,
                "finished shield cooldown overlay");
        require(OffhandRenderer112.cooldownOverlayHeight(50, 100) == 8,
                "half shield cooldown overlay");
        require(OffhandRenderer112.cooldownOverlayHeight(100, 100) == 16,
                "full shield cooldown overlay");
    }

    private static void verifyReferenceTransforms(ZipFile jar, String model, boolean blocking) throws Exception {
        com.google.gson.JsonObject display = new com.google.gson.JsonParser().parse(
                new String(read(jar.getInputStream(jar.getEntry("assets/minecraft/models/item/" + model + ".json"))),
                        java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("display");
        String[] names = {"firstperson_lefthand", "firstperson_righthand", "thirdperson_lefthand", "thirdperson_righthand"};
        ShieldRenderTransforms120.Transform[] transforms = blocking
                ? new ShieldRenderTransforms120.Transform[]{ShieldRenderTransforms120.FIRST_PERSON_LEFT_BLOCKING,
                    ShieldRenderTransforms120.FIRST_PERSON_RIGHT_BLOCKING, ShieldRenderTransforms120.THIRD_PERSON_LEFT_BLOCKING,
                    ShieldRenderTransforms120.THIRD_PERSON_RIGHT_BLOCKING}
                : new ShieldRenderTransforms120.Transform[]{ShieldRenderTransforms120.FIRST_PERSON_LEFT,
                    ShieldRenderTransforms120.FIRST_PERSON_RIGHT, ShieldRenderTransforms120.THIRD_PERSON_LEFT,
                    ShieldRenderTransforms120.THIRD_PERSON_RIGHT};
        for (int i = 0; i < names.length; i++) {
            com.google.gson.JsonObject reference = display.getAsJsonObject(names[i]);
            float[] actual = ShieldRenderTransforms120.components(transforms[i], i % 2 == 0);
            float sign = i % 2 == 0 ? -1.0F : 1.0F;
            for (int axis = 0; axis < 3; axis++) {
                float translation = reference.getAsJsonArray("translation").get(axis).getAsFloat() / 16.0F;
                float rotation = reference.getAsJsonArray("rotation").get(axis).getAsFloat();
                require(Math.abs(actual[axis] - translation * (axis == 0 ? sign : 1)) < 0.00001F,
                        model + " " + names[i] + " translation axis " + axis);
                require(Math.abs(actual[axis + 3] - rotation * (axis == 0 ? 1 : sign)) < 0.00001F,
                        model + " " + names[i] + " rotation axis " + axis);
                require(actual[6] == reference.getAsJsonArray("scale").get(axis).getAsFloat(),
                        model + " " + names[i] + " scale axis " + axis);
            }
        }
    }

    private static void verifySwapProtocol() {
        try {
            ByteBuf requestBytes = Unpooled.buffer();
            new PacketSwapOffhand(17, 6).toBytes(requestBytes);
            require(requestBytes.readableBytes() == 8, "swap request must carry sequence and selected slot");
            PacketSwapOffhand request = new PacketSwapOffhand(); request.fromBytes(requestBytes);
            require(integer(request, "sequence") == 17 && integer(request, "selectedSlot") == 6,
                    "swap request round trip");

            ByteBuf syncBytes = Unpooled.buffer();
            new PacketOffhandSync(null, 7, 6, null).toBytes(syncBytes);
            PacketOffhandSync sync = new PacketOffhandSync(); sync.fromBytes(syncBytes);
            require(integer(sync, "selectedSlot") == 6 && integer(sync, "shieldCooldownTicks") == 7,
                    "atomic hand sync metadata round trip");
            require(field(sync, "stack") == null && field(sync, "selectedStack") == null,
                    "empty hand stacks round trip");
            require(Boolean.FALSE.equals(field(sync, "handSwap")), "periodic sync is not a swap acknowledgement");
            syncBytes.clear();
            new PacketOffhandSync(null, 0, 8, null, true).toBytes(syncBytes);
            sync.fromBytes(syncBytes);
            require(Boolean.TRUE.equals(field(sync, "handSwap")) && integer(sync, "selectedSlot") == 8,
                    "swap acknowledgement preserves last hotbar slot");
        } catch (Exception error) {
            throw new AssertionError("Offhand swap protocol mismatch", error);
        }
    }

    private static void verifySwapKeyEdge() {
        require(Inventory112ClientHandler.isSwapEdge(true, true, false),
                "physical F edge must swap even when another KeyBinding owns the 1.7 queue");
        require(!Inventory112ClientHandler.isSwapEdge(true, true, true),
                "holding F must not repeatedly swap hands");
        require(!Inventory112ClientHandler.isSwapEdge(false, true, false),
                "F must not swap while a GUI is open");
        require(PacketOffhandSync.Handler.class != null,
                "F swap must be committed by the authoritative hand-sync packet");
    }

    private static void verifyNativeOffhandModule() {
        require(OffhandModule112.class != null, "standalone native off-hand lifecycle");
        require(com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.InventoryPlayer112.class.isInterface(),
                "native transformed InventoryPlayer contract");
        require(xonin.backhand.api.core.BackhandUtils.getOffhandSlot(null) == 40,
                "legacy compatibility ABI delegates to the native off-hand slot");
        require(xonin.backhand.compat.IOffhandRenderOptOut.class.isInterface(),
                "Battlegear renderer compatibility marker");
    }

    private static int integer(Object owner, String name) throws Exception {
        return ((Integer) field(owner, name)).intValue();
    }

    private static Object field(Object owner, String name) throws Exception {
        Field field = owner.getClass().getDeclaredField(name); field.setAccessible(true); return field.get(owner);
    }

    private static void require(boolean condition, String label) {
        if (!condition) throw new AssertionError("Shield 1.20.2 mismatch: " + label);
    }

    private ShieldMechanicsFixture120() { }
}
