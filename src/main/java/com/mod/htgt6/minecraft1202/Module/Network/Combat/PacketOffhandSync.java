package com.mod.htgt6.minecraft1202.Module.Network.Combat;

import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandSystem;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;

public final class PacketOffhandSync implements IMessage {
    private ItemStack stack;
    private int shieldCooldownTicks;
    private int selectedSlot = -1;
    private ItemStack selectedStack;
    private boolean handSwap;
    public PacketOffhandSync() { }
    public PacketOffhandSync(ItemStack stack) { this(stack, 0); }
    public PacketOffhandSync(ItemStack stack, int shieldCooldownTicks) {
        this(stack, shieldCooldownTicks, -1, null);
    }
    public PacketOffhandSync(ItemStack stack, int shieldCooldownTicks, int selectedSlot, ItemStack selectedStack) {
        this(stack, shieldCooldownTicks, selectedSlot, selectedStack, false);
    }
    public PacketOffhandSync(ItemStack stack, int shieldCooldownTicks, int selectedSlot, ItemStack selectedStack, boolean handSwap) {
        this.handSwap = handSwap;
        this.stack = stack == null ? null : stack.copy();
        this.shieldCooldownTicks = Math.max(0, shieldCooldownTicks);
        this.selectedSlot = selectedSlot;
        this.selectedStack = selectedStack == null ? null : selectedStack.copy();
    }
    @Override public void fromBytes(ByteBuf buffer) {
        stack = ByteBufUtils.readItemStack(buffer);
        shieldCooldownTicks = buffer.readInt();
        selectedSlot = buffer.readByte();
        selectedStack = ByteBufUtils.readItemStack(buffer);
        handSwap = buffer.readBoolean();
    }
    @Override public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeItemStack(buffer, stack);
        buffer.writeInt(shieldCooldownTicks);
        buffer.writeByte(selectedSlot);
        ByteBufUtils.writeItemStack(buffer, selectedStack);
        buffer.writeBoolean(handSwap);
    }

    public static final class Handler implements IMessageHandler<PacketOffhandSync, IMessage> {
        @Override public IMessage onMessage(final PacketOffhandSync message, MessageContext context) {
            net.minecraft.client.Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override public void run() {
                    if (net.minecraft.client.Minecraft.getMinecraft().thePlayer != null) {
                        net.minecraft.entity.player.EntityPlayer player =
                                net.minecraft.client.Minecraft.getMinecraft().thePlayer;
                        synchronized (player.inventory) {
                            if (message.selectedSlot >= 0 && message.selectedSlot < 9)
                                player.inventory.mainInventory[message.selectedSlot] = message.selectedStack == null
                                        ? null : message.selectedStack.copy();
                            OffhandHooks.setOffhand(player, message.stack == null ? null : message.stack.copy());
                            player.inventory.markDirty();
                        }
                        OffhandSystem.setClientShieldCooldown(player, message.shieldCooldownTicks);
                        if (message.handSwap)
                            com.mod.htgt6.client.inventory112.OffhandRefillGuard.acknowledge(message.selectedSlot);
                    }
                }
            });
            return null;
        }
    }
}
