package com.mod.htgt6.common.GTHooks.MisscelouseGT6Hooks;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.io.FileInputStream;

/** Read-only diagnostic for comparing inventory stacks saved by an integrated server. */
public final class SavedInventoryNBTDiagnostic {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Expected player .dat path");
        NBTTagCompound player;
        try (FileInputStream input = new FileInputStream(args[0])) {
            player = CompressedStreamTools.readCompressed(input);
        }
        NBTTagList inventory = player.getTagList("Inventory", 10);
        for (int i = 0; i < inventory.tagCount(); i++) {
            NBTTagCompound stack = inventory.getCompoundTagAt(i);
            System.out.println("slot=" + (stack.getByte("Slot") & 255)
                    + " id=" + stack.getString("id")
                    + " count=" + (stack.getByte("Count") & 255)
                    + " damage=" + (stack.getShort("Damage") & 65535)
                    + " tag=" + (stack.hasKey("tag", 10) ? stack.getCompoundTag("tag") : "{}"));
        }
    }

    private SavedInventoryNBTDiagnostic() { }
}
