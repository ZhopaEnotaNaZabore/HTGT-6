package com.mod.htgt6.minecraft1202.Module.Events.Combat;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.InventoryPlayer112;

import java.util.Arrays;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Minecraft 1.12 InventoryPlayer algorithms adapted to the 1.7.10 ItemStack API. */
public final class OffhandHooks {
    public static final int OFFHAND_SLOT = 40;
    public static final int OFFHAND_NBT_SLOT = 150;
    public static ItemStack[] offhand(InventoryPlayer inventory) {
        if (!(inventory instanceof InventoryPlayer112))
            throw new IllegalStateException("InventoryPlayer was not upgraded to the Minecraft 1.12 layout");
        ItemStack[] result = ((InventoryPlayer112) inventory).htgt6$getOffHandInventory();
        if (result == null || result.length != 1) {
            result = new ItemStack[1];
            ((InventoryPlayer112) inventory).htgt6$setOffHandInventory(result);
        }
        return result;
    }

    public static ItemStack getOffhand(EntityPlayer player) {
        return player == null ? null : getStackInSlot(player.inventory, OFFHAND_SLOT);
    }

    public static void setOffhand(EntityPlayer player, ItemStack stack) {
        if (player != null) setInventorySlotContents(player.inventory, OFFHAND_SLOT, stack);
    }

    public static void initializeCompartments(InventoryPlayer inventory) {
        setOffhand(inventory, new ItemStack[1]);
    }

    public static int getSizeInventory(InventoryPlayer inventory) { return 41; }

    /** Vanilla current-item lookup extended for the module's native off-hand slot. */
    public static ItemStack getCurrentItem(InventoryPlayer inventory) {
        int slot = inventory.currentItem;
        return (slot >= 0 && slot < inventory.mainInventory.length) || slot == OFFHAND_SLOT
                ? getStackInSlot(inventory, slot) : null;
    }

    /** Armor still ends at index 39; the new off-hand compartment follows it at index 40. */
    public static int getArmorInventoryEnd(InventoryPlayer inventory) {
        return inventory.mainInventory.length + inventory.armorInventory.length;
    }

    public static ItemStack getStackInSlot(InventoryPlayer inventory, int slot) {
        if (slot >= 0 && slot < inventory.mainInventory.length) return inventory.mainInventory[slot];
        int armor = slot - inventory.mainInventory.length;
        if (armor >= 0 && armor < inventory.armorInventory.length) return inventory.armorInventory[armor];
        return slot == OFFHAND_SLOT ? offhand(inventory)[0] : null;
    }

    public static ItemStack decrStackSize(InventoryPlayer inventory, int slot, int amount) {
        ItemStack current = getStackInSlot(inventory, slot);
        if (current == null || amount <= 0) return null;
        if (current.stackSize <= amount) {
            setInventorySlotContents(inventory, slot, null);
            return current;
        }
        ItemStack result = current.splitStack(amount);
        if (current.stackSize <= 0) setInventorySlotContents(inventory, slot, null);
        inventory.markDirty();
        return result;
    }

    public static ItemStack getStackInSlotOnClosing(InventoryPlayer inventory, int slot) {
        ItemStack result = getStackInSlot(inventory, slot);
        if (result != null) setInventorySlotContents(inventory, slot, null);
        return result;
    }

    public static void setInventorySlotContents(InventoryPlayer inventory, int slot, ItemStack stack) {
        if (slot >= 0 && slot < inventory.mainInventory.length) inventory.mainInventory[slot] = stack;
        else {
            int armor = slot - inventory.mainInventory.length;
            if (armor >= 0 && armor < inventory.armorInventory.length) inventory.armorInventory[armor] = stack;
            else if (slot == OFFHAND_SLOT) offhand(inventory)[0] = stack;
            else return;
        }
        inventory.markDirty();
    }

    public static NBTTagList writeToNBT(InventoryPlayer inventory, NBTTagList list) {
        for (int i = 0; i < inventory.mainInventory.length; ++i) append(list, inventory.mainInventory[i], i);
        for (int i = 0; i < inventory.armorInventory.length; ++i) append(list, inventory.armorInventory[i], i + 100);
        append(list, offhand(inventory)[0], OFFHAND_NBT_SLOT);
        return list;
    }

    private static void append(NBTTagList list, ItemStack stack, int slot) {
        if (stack == null) return;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Slot", (byte) slot);
        stack.writeToNBT(tag);
        list.appendTag(tag);
    }

    public static void readFromNBT(InventoryPlayer inventory, NBTTagList list) {
        inventory.mainInventory = new ItemStack[36];
        inventory.armorInventory = new ItemStack[4];
        ItemStack[] hand = new ItemStack[1];
        setOffhand(inventory, hand);
        for (int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
            if (stack == null) continue;
            if (slot < inventory.mainInventory.length) inventory.mainInventory[slot] = stack;
            else if (slot >= 100 && slot < 100 + inventory.armorInventory.length) inventory.armorInventory[slot - 100] = stack;
            else if (slot == OFFHAND_NBT_SLOT) hand[0] = stack;
        }
    }

    private static void setOffhand(InventoryPlayer inventory, ItemStack[] value) {
        if (!(inventory instanceof InventoryPlayer112))
            throw new IllegalStateException("InventoryPlayer was not upgraded to the Minecraft 1.12 layout");
        InventoryPlayer112 bridge = (InventoryPlayer112) inventory;
        bridge.htgt6$setOffHandInventory(value);
        List<ItemStack[]> compartments = Arrays.asList(
                inventory.mainInventory, inventory.armorInventory, value);
        bridge.htgt6$setAllInventories(compartments);
    }

    public static void dropAllItems(InventoryPlayer inventory) {
        for (int i = 0; i < inventory.mainInventory.length; ++i) {
            if (inventory.mainInventory[i] != null) inventory.player.func_146097_a(inventory.mainInventory[i], true, false);
            inventory.mainInventory[i] = null;
        }
        for (int i = 0; i < inventory.armorInventory.length; ++i) {
            if (inventory.armorInventory[i] != null) inventory.player.func_146097_a(inventory.armorInventory[i], true, false);
            inventory.armorInventory[i] = null;
        }
        ItemStack[] hand = offhand(inventory);
        if (hand[0] != null) inventory.player.func_146097_a(hand[0], true, false);
        hand[0] = null;
        inventory.markDirty();
    }

    public static void copyInventory(InventoryPlayer target, InventoryPlayer source) {
        for (int i = 0; i < target.mainInventory.length; ++i) target.mainInventory[i] = ItemStack.copyItemStack(source.mainInventory[i]);
        for (int i = 0; i < target.armorInventory.length; ++i) target.armorInventory[i] = ItemStack.copyItemStack(source.armorInventory[i]);
        offhand(target)[0] = ItemStack.copyItemStack(offhand(source)[0]);
        target.currentItem = source.currentItem;
        target.markDirty();
    }

    public static int clearInventory(InventoryPlayer inventory, Item item, int damage) {
        int removed = 0;
        for (int i = 0; i < inventory.mainInventory.length; ++i)
            removed += clearMatching(inventory.mainInventory, i, item, damage);
        for (int i = 0; i < inventory.armorInventory.length; ++i)
            removed += clearMatching(inventory.armorInventory, i, item, damage);
        removed += clearMatching(offhand(inventory), 0, item, damage);
        ItemStack carried = inventory.getItemStack();
        if (matches(carried, item, damage)) {
            removed += carried.stackSize;
            inventory.setItemStack(null);
        }
        if (removed > 0) inventory.markDirty();
        return removed;
    }

    private static int clearMatching(ItemStack[] contents, int slot, Item item, int damage) {
        ItemStack stack = contents[slot];
        if (!matches(stack, item, damage)) return 0;
        contents[slot] = null;
        return stack.stackSize;
    }

    private static boolean matches(ItemStack stack, Item item, int damage) {
        return stack != null && (item == null || stack.getItem() == item)
                && (damage < 0 || stack.getItemDamage() == damage);
    }

    public static boolean consumeInventoryItem(InventoryPlayer inventory, Item item) {
        for (int i = 0; i < inventory.mainInventory.length; ++i)
            if (inventory.mainInventory[i] != null && inventory.mainInventory[i].getItem() == item) {
                if (--inventory.mainInventory[i].stackSize <= 0) inventory.mainInventory[i] = null;
                inventory.markDirty();
                return true;
            }
        ItemStack[] hand = offhand(inventory);
        if (hand[0] != null && hand[0].getItem() == item) {
            if (--hand[0].stackSize <= 0) hand[0] = null;
            inventory.markDirty();
            return true;
        }
        return false;
    }

    public static boolean hasItem(InventoryPlayer inventory, Item item) {
        for (ItemStack stack : inventory.mainInventory) if (stack != null && stack.getItem() == item) return true;
        for (ItemStack stack : inventory.armorInventory) if (stack != null && stack.getItem() == item) return true;
        ItemStack hand = offhand(inventory)[0];
        return hand != null && hand.getItem() == item;
    }

    public static boolean hasItemStack(InventoryPlayer inventory, ItemStack wanted) {
        if (wanted == null) return false;
        for (ItemStack stack : inventory.mainInventory) if (stack != null && stack.isItemEqual(wanted)) return true;
        for (ItemStack stack : inventory.armorInventory) if (stack != null && stack.isItemEqual(wanted)) return true;
        ItemStack hand = offhand(inventory)[0];
        return hand != null && hand.isItemEqual(wanted);
    }

    public static void decrementOffhandAnimation(InventoryPlayer inventory) {
        ItemStack stack = offhand(inventory)[0];
        if (stack != null) stack.updateAnimation(inventory.player.worldObj, inventory.player, OFFHAND_SLOT, false);
    }

    /** Minecraft 1.12 ContainerPlayer shift-click routing, adapted to nullable 1.7.10 stacks. */
    public static ItemStack transferStackInSlot112(ContainerPlayer container, EntityPlayer player, int index) {
        if (container == null || index < 0 || index >= container.inventorySlots.size()) return null;
        Slot source = (Slot) container.inventorySlots.get(index);
        if (source == null || !source.getHasStack()) return null;
        ItemStack stack = source.getStack();
        if (index >= 5 && index < 9
                && !com.mod.htgt6.minecraft1202.Module.Events.Exploration.ExplorationHooks112.canTakeArmorStack(source, player))
            return null;
        ItemStack original = stack.copy();
        boolean moved;
        if (index == 0) {
            moved = merge(container, stack, 9, 45, true);
            if (moved) source.onSlotChange(stack, original);
        } else if (index >= 1 && index < 9) {
            moved = merge(container, stack, 9, 45, false);
        } else if (stack.getItem() instanceof ItemArmor
                || stack.getItem() instanceof com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemBlockDragonHead) {
            int armorSlot = stack.getItem() instanceof ItemArmor
                    ? 5 + ((ItemArmor) stack.getItem()).armorType : 5;
            moved = !((Slot) container.inventorySlots.get(armorSlot)).getHasStack()
                    && merge(container, stack, armorSlot, armorSlot + 1, false);
            if (!moved) moved = moveBetweenInventoryRows(container, stack, index);
        } else if (isOffhandEquipment(stack) && !((Slot) container.inventorySlots.get(45)).getHasStack()) {
            moved = merge(container, stack, 45, 46, false);
            if (!moved) moved = moveBetweenInventoryRows(container, stack, index);
        } else {
            moved = moveBetweenInventoryRows(container, stack, index);
        }
        if (!moved) return null;
        if (stack.stackSize <= 0) source.putStack(null); else source.onSlotChanged();
        if (stack.stackSize == original.stackSize) return null;
        source.onPickupFromSlot(player, stack);
        return original;
    }

    private static boolean moveBetweenInventoryRows(ContainerPlayer container, ItemStack stack, int index) {
        if (index >= 9 && index < 36) return merge(container, stack, 36, 45, false);
        if (index >= 36 && index < 45) return merge(container, stack, 9, 36, false);
        return merge(container, stack, 9, 45, false);
    }

    private static boolean isOffhandEquipment(ItemStack stack) {
        return stack != null && com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.shield != null
                && stack.getItem() == com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.shield;
    }

    private static boolean merge(ContainerPlayer container, ItemStack stack, int start, int end, boolean reverse) {
        boolean changed = false;
        int index = reverse ? end - 1 : start;
        while (stack.stackSize > 0 && (reverse ? index >= start : index < end)) {
            Slot target = (Slot) container.inventorySlots.get(index);
            ItemStack present = target.getStack();
            if (present != null && target.isItemValid(stack) && present.isItemEqual(stack)
                    && ItemStack.areItemStackTagsEqual(present, stack)) {
                int limit = Math.min(target.getSlotStackLimit(), present.getMaxStackSize());
                int amount = Math.min(stack.stackSize, limit - present.stackSize);
                if (amount > 0) {
                    present.stackSize += amount;
                    stack.stackSize -= amount;
                    target.onSlotChanged();
                    changed = true;
                }
            }
            index += reverse ? -1 : 1;
        }
        index = reverse ? end - 1 : start;
        while (stack.stackSize > 0 && (reverse ? index >= start : index < end)) {
            Slot target = (Slot) container.inventorySlots.get(index);
            if (!target.getHasStack() && target.isItemValid(stack)) {
                int amount = Math.min(stack.stackSize, target.getSlotStackLimit());
                ItemStack inserted = stack.copy();
                inserted.stackSize = amount;
                target.putStack(inserted);
                stack.stackSize -= amount;
                changed = true;
                break;
            }
            index += reverse ? -1 : 1;
        }
        return changed;
    }

    private OffhandHooks() { }
}
