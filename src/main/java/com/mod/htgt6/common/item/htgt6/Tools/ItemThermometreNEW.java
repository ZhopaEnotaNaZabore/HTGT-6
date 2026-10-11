package com.mod.htgt6.common.item.htgt6.Tools;

import static gregapi.data.CS.SIDE_ANY;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.network.PacketShowHUD;
import gregapi.tileentity.data.ITileEntityTemperature;
import gregapi.util.WD;
import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorChamber;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemThermometreNEW extends Item {

    public ItemThermometreNEW() {
        setMaxStackSize(1);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                             int x, int y, int z,
                             int side, float hitX, float hitY, float hitZ) {

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound tag = stack.getTagCompound();

        // Perform processing strictly on the Server side to ensure multiplayer sync & stability
        if (!world.isRemote) {

            // 1. Shift + Right Click = Toggle Mode
            if (player.isSneaking()) {
                int mode = tag.getInteger("Mode");
                mode = (mode + 1) % 2;
                tag.setInteger("Mode", mode);

                String modeText = (mode == 0 ? "Mode: Celsius" : "Mode: Kelvin");

                // Send HUD packet to player
                HTGT6.network.sendTo(
                        new PacketShowHUD("Advanced Thermometer", modeText),
                        (EntityPlayerMP) player
                );
                return true;
            }

            // 2. Scan TileEntity & Read Data
            TileEntity te = world.getTileEntity(x, y, z);
            long current = -1;
            long max = -1;

            if (te != null) {
                // GT6 temperature API
                if (te instanceof ITileEntityTemperature) {
                    ITileEntityTemperature t = (ITileEntityTemperature) te;
                    try { current = t.getTemperatureValue((byte)side); } catch (Throwable ignored) {}
                    try { max = t.getTemperatureMax((byte)side); } catch (Throwable ignored) {}
                }

                // IC2 Reactor
                if (current < 0) {
                    try {
                        if (te instanceof IReactor) {
                            IReactor r = (IReactor) te;
                            current = r.getHeat() / 5;
                            max = r.getMaxHeat() / 5;
                        } else if (te instanceof IReactorChamber) {
                            IReactor r = ((IReactorChamber) te).getReactor();
                            if (r != null) {
                                current = r.getHeat() / 5;
                                max = r.getMaxHeat() / 5;
                            }
                        }
                    } catch (Throwable ignored) {}
                }

                // Environment temperature fallback
                if (current < 0) {
                    try {
                        current = WD.temperature(world, x, y, z);
                    } catch (Throwable ignored) {
                        current = 293;
                    }
                }
            }

            if (max < current) {
                max = current;
            }

            int mode = tag.getInteger("Mode");

            // Format Temperature String
            String value = "";
            if (mode == 0) {
                value = (current - 273) + " °C";
                if (max > current) value += " / " + (max - 273) + " °C";
            } else {
                value = current + " K";
                if (max > current) value += " / " + max + " K";
            }

            // Universal Fluid and Item Extraction
            java.util.List<String> contentsList = new java.util.ArrayList<String>();

            if (te != null) {
                net.minecraftforge.common.util.ForgeDirection dir = net.minecraftforge.common.util.ForgeDirection.getOrientation(side);

                // Fluid Extraction (Forge Standard)
                if (te instanceof net.minecraftforge.fluids.IFluidHandler) {
                    try {
                        net.minecraftforge.fluids.IFluidHandler fluidHandler = (net.minecraftforge.fluids.IFluidHandler) te;
                        net.minecraftforge.fluids.FluidTankInfo[] tankInfos = fluidHandler.getTankInfo(dir);
                        if (tankInfos != null) {
                            for (net.minecraftforge.fluids.FluidTankInfo info : tankInfos) {
                                if (info != null && info.fluid != null && info.fluid.amount > 0) {
                                    contentsList.add(info.fluid.amount + "mB " + info.fluid.getLocalizedName());
                                }
                            }
                        }
                    } catch (Throwable ignored) {}
                }

                // Fallback GT6 Crucible / Reflection
                if (contentsList.isEmpty()) {
                    try {
                        java.lang.reflect.Method getTankCountMethod = te.getClass().getMethod("getTankCount");
                        java.lang.reflect.Method getFluidInTankMethod = te.getClass().getMethod("getFluidInTank", int.class);

                        int tankCount = (Integer) getTankCountMethod.invoke(te);
                        for (int i = 0; i < tankCount; i++) {
                            net.minecraftforge.fluids.FluidStack fluid = (net.minecraftforge.fluids.FluidStack) getFluidInTankMethod.invoke(te, i);
                            if (fluid != null && fluid.amount > 0) {
                                contentsList.add(fluid.amount + "mB " + fluid.getLocalizedName());
                            }
                        }
                    } catch (Throwable ignored) {}
                }

                // Item Extraction (Standard & Sided Inventories)
                if (te instanceof IInventory) {
                    try {
                        IInventory inv = (IInventory) te;
                        if (te instanceof ISidedInventory) {
                            ISidedInventory sidedInv = (ISidedInventory) te;
                            int[] slots = sidedInv.getAccessibleSlotsFromSide(side);
                            if (slots == null || slots.length <= 0) slots = sidedInv.getAccessibleSlotsFromSide(SIDE_ANY);
                            if (slots != null) {
                                for (int slotId : slots) {
                                    ItemStack itemStack = sidedInv.getStackInSlot(slotId);
                                    if (itemStack != null) contentsList.add(itemStack.stackSize + "x " + itemStack.getDisplayName());
                                }
                            }
                        } else {
                            for (int i = 0; i < inv.getSizeInventory(); i++) {
                                ItemStack itemStack = inv.getStackInSlot(i);
                                if (itemStack != null) contentsList.add(itemStack.stackSize + "x " + itemStack.getDisplayName());
                            }
                        }
                    } catch (Throwable ignored) {}
                }

                // Fallback GT6 Internal Storage Reflection
                if (contentsList.isEmpty()) {
                    try {
                        java.lang.reflect.Field mInventoryField = null;
                        Class<?> currentClass = te.getClass();
                        while (currentClass != null && mInventoryField == null) {
                            try {
                                mInventoryField = currentClass.getDeclaredField("mInventory");
                            } catch (NoSuchFieldException e) {
                                currentClass = currentClass.getSuperclass();
                            }
                        }

                        if (mInventoryField != null) {
                            mInventoryField.setAccessible(true);
                            ItemStack[] invArray = (ItemStack[]) mInventoryField.get(te);
                            if (invArray != null) {
                                for (ItemStack itemStack : invArray) {
                                    if (itemStack != null) contentsList.add(itemStack.stackSize + "x " + itemStack.getDisplayName());
                                }
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            }

            // Build output string
            if (!contentsList.isEmpty()) {
                StringBuilder contentsString = new StringBuilder(" | Contains: ");
                for (int i = 0; i < contentsList.size(); i++) {
                    contentsString.append(contentsList.get(i));
                    if (i < contentsList.size() - 1) contentsString.append(", ");
                }
                value += contentsString.toString();
            } else {
                value += " | Contains: Empty";
            }

            // Save text to item NBT (for Item Tooltip)
            tag.setString("LastDisplayValue", value);

            // Directly send HUD display packet to the player on the Client!
            HTGT6.network.sendTo(
                    new PacketShowHUD("Advanced Thermometer", value),
                    (EntityPlayerMP) player
            );
        }

        return true;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addInformation(ItemStack stack, EntityPlayer player, java.util.List list, boolean advanced) {
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();

            int mode = tag.getInteger("Mode");
            list.add(net.minecraft.util.EnumChatFormatting.GREEN + "Режим: " +
                    (mode == 0 ? net.minecraft.util.EnumChatFormatting.GOLD + "Цельсий"
                            : net.minecraft.util.EnumChatFormatting.GOLD + "Кельвины"));

            String lastValue = tag.getString("LastDisplayValue");
            if (!lastValue.isEmpty()) {
                if (lastValue.contains(" | ")) {
                    String[] parts = lastValue.split(" \\| ");
                    list.add(net.minecraft.util.EnumChatFormatting.GREEN + "Лог: " + net.minecraft.util.EnumChatFormatting.RESET + parts[0]);
                    for (int i = 1; i < parts.length; i++) {
                        list.add(parts[i]);
                    }
                } else {
                    list.add(lastValue);
                }
            }
        } else {
            list.add(net.minecraft.util.EnumChatFormatting.GREEN + "Режим: " + net.minecraft.util.EnumChatFormatting.GOLD + "Цельсия ");
            list.add(net.minecraft.util.EnumChatFormatting.GOLD + "ПКМ по тигелю, проверить статус");
        }
    }
}