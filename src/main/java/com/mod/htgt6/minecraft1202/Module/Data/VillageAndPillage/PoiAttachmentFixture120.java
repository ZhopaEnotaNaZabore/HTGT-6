package com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.PoiAttachment120;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemBlockBell120;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public final class PoiAttachmentFixture120 {
    public static void main(String[] args) throws Exception {
        int[] outward = {0, 0, 0, 2, 3, 1};
        Set<Integer> bells = new HashSet<Integer>(), grinders = new HashSet<Integer>();
        for (int yaw = 0; yaw < 4; yaw++) for (int side = 0; side < 6; side++) {
            int grinder = PoiAttachment120.withPlacerFacing(false, PoiAttachment120.grindstone(side, 0), yaw);
            int expected = side == 0 ? 8 | yaw : side == 1 ? yaw : 4 | outward[side];
            require(grinder == expected, "grindstone click face and placer facing");
            grinders.add(grinder);
            for (int mask = 0; mask < 64; mask++) {
                int placed = PoiAttachment120.bell(side, 0, mask);
                if (placed < 0) continue;
                int bell = PoiAttachment120.withPlacerFacing(true, placed, yaw);
                bells.add(bell);
                require(PoiAttachment120.updateBell(bell, mask) == bell, "placed bell must survive its supports");
            }
        }
        require(grinders.size() == 12 && bells.size() == 16, "every attachment/facing is reachable");
        for (int side = 2; side < 6; side++) {
            int facing = outward[side];
            int single = 8 | facing, dual = 12 | facing;
            int front = 1 << side, back = 1 << (side ^ 1);
            require(PoiAttachment120.updateBell(single, front | back) == dual, "second wall adds attachment");
            require(PoiAttachment120.updateBell(dual, front) == single, "second wall removed");
            require(PoiAttachment120.updateBell(dual, back) == (8 | ((facing + 2) & 3)), "first wall removed reverses facing");
            require(PoiAttachment120.updateBell(single, back) == -1, "single wall cannot move to other support");
            require(PoiAttachment120.updateBell(dual, 0) == -1, "unsupported bell drops");
            require(PoiAttachment120.bell(side, 0, 1) == ((outward[side] + 2) & 3), "wall click falls back to floor");
            require(PoiAttachment120.bell(side, 0, 2) == (4 | ((outward[side] + 2) & 3)), "wall click falls back to ceiling");
        }
        require(PoiAttachment120.bell(0, 0, 1) == -1 && PoiAttachment120.bell(1, 0, 2) == -1, "vertical click needs its chosen support");
        verifyVariants(args[0], "bell", bells, new String[]{"floor", "ceiling", "single_wall", "double_wall"}, "attachment");
        verifyVariants(args[0], "grindstone", grinders, new String[]{"floor", "wall", "ceiling"}, "face");
        ItemBlockBell120 item = new ItemBlockBell120(new Block(Material.iron) { });
        require(item.getSpriteNumber() == 1, "bell uses native item rendering, not block inventory model");
        JsonObject itemModel = json(args[0] + "/assets/htgt6/models/item/village_pillage/bell.json");
        require(itemModel.get("parent").getAsString().equals("minecraft:item/generated") && !itemModel.has("elements"), "generated bell item");
        try (ZipFile reference = new ZipFile(args[1])) {
            byte[] expected = new byte[(int) reference.getEntry("assets/minecraft/textures/item/bell.png").getSize()];
            try (java.io.DataInputStream input = new java.io.DataInputStream(reference.getInputStream(reference.getEntry("assets/minecraft/textures/item/bell.png")))) { input.readFully(expected); }
            require(java.util.Arrays.equals(expected, Files.readAllBytes(Paths.get(args[0], "assets/htgt6/textures/items/village_pillage/bell.png"))), "official bell icon bytes");
        }
        System.out.println("PoiAttachmentFixture120: OK; 12 grindstone and 16 bell states, supports, icon");
    }
    private static void verifyVariants(String root, String block, Set<Integer> values, String[] attachments, String property) throws Exception {
        JsonObject variants = json(root + "/assets/htgt6/blockstates/village_pillage/" + block + ".json").getAsJsonObject("variants");
        for (int meta : values) require(variants.has(property + "=" + attachments[meta >>> 2] + ",facing="
                + new String[]{"north", "east", "south", "west"}[meta & 3]), "metadata resolves to " + block + " JSON variant");
    }
    private static JsonObject json(String path) throws Exception {
        return new JsonParser().parse(new String(Files.readAllBytes(Paths.get(path)), java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
