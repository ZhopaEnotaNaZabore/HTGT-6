package com.mod.htgt6.client.inventory112;

import java.io.File;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Minecraft 1.12-style persistent creative hotbar presets. */
public final class SavedHotbars112 {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-SavedHotbars112");
    private static final ItemStack[][] HOTBARS = new ItemStack[9][9];
    private static final NBTTagCompound[][] UNRESOLVED = new NBTTagCompound[9][9];
    private static File storage;
    private static CreativeTabs tab;

    public static void init() {
        if (tab != null) return;
        storage = new File(new File(Minecraft.getMinecraft().mcDataDir, "config"), "htgt6/saved_hotbars_1_12.dat");
        load();
        tab = new CreativeTabs("hotbar") {
            @Override public Item getTabIconItem() { return Items.book; }
            @Override @SuppressWarnings({"rawtypes", "unchecked"})
            public void displayAllReleventItems(List items) {
                for (int preset = 0; preset < 9; ++preset)
                    for (int slot = 0; slot < 9; ++slot)
                        items.add(copy(HOTBARS[preset][slot]));
            }
        }.setBackgroundImageName("items.png");
    }

    public static void save(int preset) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || !mc.thePlayer.capabilities.isCreativeMode
                || preset < 0 || preset >= HOTBARS.length) return;
        boolean saved = savePreset(preset, mc.thePlayer.inventory.mainInventory);
        mc.thePlayer.addChatMessage(new ChatComponentTranslation(saved
                ? "inventory112.hotbar.saved" : "inventory112.hotbar.save_failed", preset + 1));
    }

    static boolean savePreset(int preset, ItemStack[] inventory) {
        ItemStack[] previous = HOTBARS[preset].clone();
        NBTTagCompound[] missing = UNRESOLVED[preset].clone();
        for (int slot = 0; slot < 9; ++slot) {
            HOTBARS[preset][slot] = copy(inventory[slot]);
            UNRESOLVED[preset][slot] = null;
        }
        if (persist()) return true;
        System.arraycopy(previous, 0, HOTBARS[preset], 0, 9);
        System.arraycopy(missing, 0, UNRESOLVED[preset], 0, 9);
        return false;
    }

    public static void load(int preset) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || !mc.thePlayer.capabilities.isCreativeMode
                || preset < 0 || preset >= HOTBARS.length) return;
        if (mc.playerController == null) return;
        for (int slot = 0; slot < 9; ++slot) {
            ItemStack stack = copy(HOTBARS[preset][slot]);
            mc.thePlayer.inventory.mainInventory[slot] = stack;
            mc.playerController.sendSlotPacket(stack, 36 + slot);
        }
        mc.thePlayer.inventory.markDirty();
        mc.thePlayer.addChatMessage(new ChatComponentTranslation("inventory112.hotbar.loaded", preset + 1));
    }

    private static void load() {
        if (storage == null || !storage.isFile()) return;
        try {
            NBTTagCompound root = CompressedStreamTools.read(storage);
            NBTTagList entries = root == null ? new NBTTagList() : root.getTagList("Hotbars", 10);
            ItemStack[][] loaded = new ItemStack[9][9];
            NBTTagCompound[][] unresolved = new NBTTagCompound[9][9];
            for (int index = 0; index < entries.tagCount(); ++index) {
                NBTTagCompound entry = entries.getCompoundTagAt(index);
                if (!entry.hasKey("Preset", 1) || !entry.hasKey("Slot", 1)) continue;
                int preset = entry.getByte("Preset") & 255;
                int slot = entry.getByte("Slot") & 255;
                if (preset >= 9 || slot >= 9) continue;
                loaded[preset][slot] = null;unresolved[preset][slot] = null;
                if (entry.hasKey("RegistryName",8)) {
                    Item item=(Item)Item.itemRegistry.getObject(entry.getString("RegistryName"));
                    if(item==null) {
                        unresolved[preset][slot]=(NBTTagCompound)entry.copy();
                        continue;
                    }
                    ItemStack stack=new ItemStack(item,entry.getByte("Count"),Math.max(0,entry.getShort("Damage")));
                    if(entry.hasKey("tag",10))stack.setTagCompound((NBTTagCompound)entry.getCompoundTag("tag").copy());
                    if(stack.stackSize>0)loaded[preset][slot]=stack;
                } else {
                    // One-time compatibility with the old numeric-ID-only format.
                    ItemStack stack=ItemStack.loadItemStackFromNBT(entry);
                    if(stack!=null&&stack.stackSize>0)loaded[preset][slot]=stack;
                }
            }
            for(int preset=0;preset<9;preset++)for(int slot=0;slot<9;slot++) {
                HOTBARS[preset][slot]=loaded[preset][slot];UNRESOLVED[preset][slot]=unresolved[preset][slot];
            }
        } catch (IOException exception) {
            LOGGER.warn("Could not read Minecraft 1.12 saved hotbars from {}", storage, exception);
        } catch (RuntimeException exception) {
            LOGGER.warn("Invalid Minecraft 1.12 saved hotbars in {}", storage, exception);
        }
    }

    private static boolean persist() {
        if (storage == null) return false;
        try {
            NBTTagCompound root = new NBTTagCompound();
            NBTTagList entries = new NBTTagList();
            for (int preset = 0; preset < 9; ++preset) {
                for (int slot = 0; slot < 9; ++slot) {
                    ItemStack stack = HOTBARS[preset][slot];
                    if (stack == null) {
                        if(UNRESOLVED[preset][slot]!=null)entries.appendTag(UNRESOLVED[preset][slot].copy());
                        continue;
                    }
                    NBTTagCompound entry = new NBTTagCompound();
                    entry.setByte("Preset", (byte) preset);
                    entry.setByte("Slot", (byte) slot);
                    stack.writeToNBT(entry);
                    String name=(String)Item.itemRegistry.getNameForObject(stack.getItem());
                    if(name!=null)entry.setString("RegistryName",name);
                    entries.appendTag(entry);
                }
            }
            root.setTag("Hotbars", entries);
            File parent = storage.getAbsoluteFile().getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) {
                LOGGER.warn("Could not create saved-hotbar directory {}", parent);
                return false;
            }
            java.nio.file.Path temporary = java.nio.file.Files.createTempFile(parent.toPath(), "hotbars-", ".tmp");
            try {
                CompressedStreamTools.write(root, temporary.toFile());
                java.nio.file.Files.move(temporary, storage.toPath(),
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } finally {
                java.nio.file.Files.deleteIfExists(temporary);
            }
            return true;
        } catch (IOException exception) {
            LOGGER.warn("Could not write Minecraft 1.12 saved hotbars to {}", storage, exception);
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not serialize Minecraft 1.12 saved hotbars to {}", storage, exception);
        }
        return false;
    }

    private static ItemStack copy(ItemStack stack) { return stack == null ? null : stack.copy(); }
    private SavedHotbars112() { }
}
