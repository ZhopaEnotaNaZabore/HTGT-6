package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.*;
import com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemBlockDragonHead;
import com.mod.htgt6.minecraft1202.Module.Client.Combat.RenderDragonHead120;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;

public final class DragonHead156Fixture {
    private static int checks;
    public static void main(String[] args) throws Exception {
        BountifulMechanicsFixture120.Grid world = BountifulMechanicsFixture120.initialize();
        java.lang.reflect.Field info = World.class.getDeclaredField("worldInfo");
        info.setAccessible(true);
        info.set(world, new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
        world.provider.worldObj = world;
        Swimming115Fixture.Player player = new Swimming115Fixture.Player(world);
        ContainerPlayer container = new ContainerPlayer(player.inventory, false, player);
        ItemBlockDragonHead item = new ItemBlockDragonHead(new BlockDragonHead());
        ItemStack stack = new ItemStack(item, 3);
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString("fixture", "preserved");
        for (int i = 0; i < 4; i++) check(container.getSlot(5+i).isItemValid(stack) == (i==0), "armor slot " + i);
        player.inventory.setInventorySlotContents(0, stack);
        check(OffhandHooks.transferStackInSlot112(container, player, 36) != null, "shift equip succeeds");
        check(player.inventory.armorInventory[3].getItem() == item, "equipped head");
        check(player.inventory.armorInventory[3].stackSize == 1 && stack.stackSize == 2, "one equipped, remainder retained");
        check("preserved".equals(player.inventory.armorInventory[3].getTagCompound().getString("fixture")), "NBT preserved");
        OffhandHooks.transferStackInSlot112(container, player, 36);
        check(player.inventory.armorInventory[3].stackSize == 1, "occupied helmet not overwritten");
        check(OffhandHooks.transferStackInSlot112(container, player, 5) != null, "shift unequip succeeds");
        check(player.inventory.armorInventory[3] == null, "helmet cleared");
        int total = 0; for (ItemStack s : player.inventory.mainInventory) if (s != null && s.getItem() == item) total += s.stackSize;
        check(total == 3, "no duplication or loss");
        TileEntityDragonHead tile = new TileEntityDragonHead(); tile.setWorldObj(world);
        world.powered = false; tile.updateEntity(); near(tile.getAnimation(.5F), 0, "idle phase");
        world.powered = true; tile.updateEntity(); near(tile.getAnimation(.5F), 1.5F, "powered interpolation");
        tile.updateEntity(); world.powered = false; tile.updateEntity(); near(tile.getAnimation(.5F), 2, "unpowered freezes phase");
        world.powered = true; tile.updateEntity(); near(tile.getAnimation(0), 3, "resumes phase");
        near(RenderDragonHead120.jawAngle(0), .2F, "rest jaw");
        near(RenderDragonHead120.jawAngle(2.5F), .4F, "open jaw");
        near(RenderDragonHead120.jawAngle(7.5F), 0, "closed jaw");
        System.out.println("PASS dragon156: " + checks + " inventory, NBT and animation checks");
    }
    private static void near(float a, float b, String name) { check(Math.abs(a-b)<.00001F, name); }
    private static void check(boolean value, String name) { if (!value) throw new AssertionError(name); checks++; }
}
