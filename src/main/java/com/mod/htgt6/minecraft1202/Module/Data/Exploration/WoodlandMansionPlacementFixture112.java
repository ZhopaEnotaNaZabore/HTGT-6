package com.mod.htgt6.minecraft1202.Module.Data.Exploration;

import com.mod.htgt6.minecraft1202.Module.World.Exploration.WoodlandMansionGenerator112;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.nbt.NBTTagCompound;

/** Shuffled population/reload regression for the persistent mansion ledger. */
public final class WoodlandMansionPlacementFixture112 {
    private WoodlandMansionPlacementFixture112() { }

    public static void main(String[] ignored) {
        check(WoodlandMansionGenerator112.isMansionBiome(net.minecraft.world.biome.BiomeGenBase.roofedForest),
                "dark forest eligibility");
        net.minecraft.world.biome.BiomeGenBase darkHills = net.minecraft.world.biome.BiomeGenBase.getBiome(157);
        if (darkHills != null) check(WoodlandMansionGenerator112.isMansionBiome(darkHills),
                "roofed forest hills eligibility");
        check(!WoodlandMansionGenerator112.isMansionBiome(net.minecraft.world.biome.BiomeGenBase.forest),
                "ordinary forest must not host mansions");
        long seed = 4602861541L;
        List<String> first = WoodlandMansionGenerator112.replayFingerprint(seed, -2, 3);
        List<String> second = WoodlandMansionGenerator112.replayFingerprint(seed, -2, 3);
        check(first.equals(second) && first.size() > 100, "deterministic candidate replay");

        int[] candidate = com.mod.htgt6.common.util.StructurePlacement120.candidate(seed, -2, 3,
                com.mod.htgt6.common.util.StructurePlacement120.WOODLAND_MANSION);
        int startX = candidate[0], startZ = candidate[1];
        List<Long> targets = WoodlandMansionGenerator112.replayChunks(seed, -2, 3);
        check(targets.size() > 20, "actual affected chunks");
        for (Long key : targets) check(Math.abs(WoodlandMansionData112.keyX(key) - startX) < 20
                && Math.abs(WoodlandMansionData112.keyZ(key) - startZ) < 20, "region replay radius");
        List<Long> shuffled = new ArrayList<Long>(targets);
        Collections.shuffle(shuffled, new Random(0x112L));

        WoodlandMansionData112 data = new WoodlandMansionData112();
        for (Long key : shuffled) data.addPending(startX, startZ,
                WoodlandMansionData112.keyX(key), WoodlandMansionData112.keyZ(key));
        WoodlandMansionData112.Start start = data.resolve(startX, startZ, 71, true);
        check(start.pendingCount() == targets.size(), "pending population order");
        for (int i = 0; i < shuffled.size() / 2; ++i) {
            long key = shuffled.get(i);
            data.complete(start, WoodlandMansionData112.keyX(key), WoodlandMansionData112.keyZ(key));
            data.complete(start, WoodlandMansionData112.keyX(key), WoodlandMansionData112.keyZ(key));
        }

        NBTTagCompound disk = new NBTTagCompound(); data.writeToNBT(disk);
        WoodlandMansionData112 loaded = new WoodlandMansionData112(); loaded.readFromNBT((NBTTagCompound) disk.copy());
        WoodlandMansionData112.Start restored = loaded.getStart(startX, startZ);
        check(restored != null && restored.resolved && restored.valid && restored.baseY == 71, "start reload");
        check(restored.completedCount() == shuffled.size() / 2, "completion reload");
        for (int i = shuffled.size() / 2; i < shuffled.size(); ++i) {
            long key = shuffled.get(i);
            loaded.complete(restored, WoodlandMansionData112.keyX(key), WoodlandMansionData112.keyZ(key));
        }
        check(restored.pendingCount() == 0 && restored.completedCount() == targets.size(), "shuffled completion");
        for (Long key : targets) check(loaded.isComplete(restored,
                WoodlandMansionData112.keyX(key), WoodlandMansionData112.keyZ(key)), "completion claim");

        NBTTagCompound finalDisk = new NBTTagCompound(); loaded.writeToNBT(finalDisk);
        WoodlandMansionData112 finalLoad = new WoodlandMansionData112(); finalLoad.readFromNBT(finalDisk);
        WoodlandMansionData112.Start finalStart = finalLoad.getStart(startX, startZ);
        check(finalStart.completedCount() == targets.size() && finalStart.pendingCount() == 0, "second reload");
        System.out.println("mansion placement replayPieces=" + first.size()
                + " shuffledChunks=" + targets.size() + " reloads=2 idempotent=true");
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
