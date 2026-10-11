package com.mod.htgt6.common.item.htgt6.govnoebanoe;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

import java.util.List;

public class ItemUniversalCanister extends Item implements IFluidContainerItem {

    protected int capacity = 1000; //[cite: 5]

    @SideOnly(Side.CLIENT)
    private IIcon iconFluidLayer; //[cite: 5]

    public ItemUniversalCanister() {
        super();
        this.setMaxStackSize(64); //[cite: 5]
        this.setUnlocalizedName("universal_canister"); //[cite: 5]
        this.setHasSubtypes(true); //[cite: 5]
    }

    // ==========================================
    // FLOAT FLUID HELPER METHODS
    // ==========================================

    public float getFluidAmountFloat(ItemStack container) {
        if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("Fluid")) {
            return 0.0f;
        }
        NBTTagCompound fluidTag = container.stackTagCompound.getCompoundTag("Fluid");
        if (fluidTag.hasKey("AmountFloat")) {
            return fluidTag.getFloat("AmountFloat");
        }
        return fluidTag.getInteger("Amount");
    }

    private void setFluidAmountFloat(ItemStack container, float amount) {
        if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("Fluid")) return;

        // Clamping between 0 and 1000 capacity
        if (amount < 0.0f) amount = 0.0f;
        if (amount > capacity) amount = (float) capacity;

        NBTTagCompound fluidTag = container.stackTagCompound.getCompoundTag("Fluid");

        if (amount <= 0.0f) {
            container.stackTagCompound.removeTag("Fluid");
            if (container.stackTagCompound.hasNoTags()) {
                container.stackTagCompound = null;
            }
        } else {
            fluidTag.setFloat("AmountFloat", amount);
            fluidTag.setInteger("Amount", (int) Math.ceil(amount)); // Ceil ensures Forge sees it as at least 1mB if float > 0
        }
    }

    // ==========================================
    // SHIFT-CLICK DIRECT BLOCK INTERACTION
    // ==========================================

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking()) return false;

        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof IFluidHandler)) return false;

        if (world.isRemote) return true;

        IFluidHandler handler = (IFluidHandler) te;
        ForgeDirection dir = ForgeDirection.getOrientation(side);
        FluidStack contained = getFluid(stack);
        float currentFloat = getFluidAmountFloat(stack);

        if (contained != null && currentFloat > 0.0f) {
            // INSERT FLUID: Canister has fluid, player shifts and clicks to inject it into the machine
            FluidStack toInject = contained.copy();
            toInject.amount = (int) Math.floor(currentFloat); // Safely use floor for transfer amounts

            if (toInject.amount > 0 && handler.canFill(dir, toInject.getFluid())) {
                int filled = handler.fill(dir, toInject, true);
                if (filled > 0) {
                    // Adjust stack logic for single items vs stacks
                    if (stack.stackSize == 1) {
                        setFluidAmountFloat(stack, currentFloat - filled);
                    } else {
                        ItemStack emptiedCopy = stack.copy();
                        emptiedCopy.stackSize = 1;
                        setFluidAmountFloat(emptiedCopy, currentFloat - filled);

                        stack.stackSize--;
                        if (!player.inventory.addItemStackToInventory(emptiedCopy)) {
                            player.dropPlayerItemWithRandomChoice(emptiedCopy, false);
                        }
                    }
                    world.markBlockForUpdate(x, y, z);
                    te.markDirty();
                    return true;
                }
            }
        } else {
            // DRAIN FLUID: Canister is empty, player shifts and clicks to withdraw fluid
            FluidTankInfo[] infos = handler.getTankInfo(dir);
            if (infos != null) {
                for (FluidTankInfo info : infos) {
                    if (info.fluid != null && info.fluid.amount > 0) {
                        FluidStack drainTarget = info.fluid.copy();
                        drainTarget.amount = capacity; // Attempt a max fill

                        if (handler.canDrain(dir, drainTarget.getFluid())) {
                            FluidStack drained = handler.drain(dir, drainTarget, false);
                            if (drained != null && drained.amount > 0) {
                                ItemStack filledCopy = stack.copy();
                                filledCopy.stackSize = 1;

                                int accepted = this.fill(filledCopy, drained, true);
                                if (accepted > 0) {
                                    handler.drain(dir, new FluidStack(drained.getFluid(), accepted), true);

                                    if (stack.stackSize == 1) {
                                        // Directly update hand item when unstacked
                                        player.setCurrentItemOrArmor(0, filledCopy);
                                    } else {
                                        // Reduce stack and add filled item to inventory/ground
                                        stack.stackSize--;
                                        if (!player.inventory.addItemStackToInventory(filledCopy)) {
                                            player.dropPlayerItemWithRandomChoice(filledCopy, false);
                                        }
                                    }
                                    world.markBlockForUpdate(x, y, z);
                                    te.markDirty();
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    // ==========================================
    // OVERRIDDEN NBT FUNCTIONS FOR FLOAT HANDLING
    // ==========================================

    @Override
    public FluidStack getFluid(ItemStack container) {
        if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("Fluid")) {
            return null; //[cite: 5]
        }
        FluidStack stack = FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("Fluid")); //[cite: 5]
        if (stack != null) {
            // Dynamically correct the runtime amount based on precision float NBT data
            stack.amount = (int) Math.ceil(getFluidAmountFloat(container));
        }
        return stack;
    }

    @Override
    public int getCapacity(ItemStack container) {
        return this.capacity; //[cite: 5]
    }

    @Override
    public int fill(ItemStack container, FluidStack resource, boolean doFill) {
        if (resource == null) return 0; //[cite: 5]

        float currentFloat = getFluidAmountFloat(container);
        FluidStack contained = getFluid(container);

        if (contained != null && !contained.isFluidEqual(resource)) return 0; //[cite: 5]

        float space = capacity - currentFloat;
        float toFill = Math.min(space, (float) resource.amount);

        if (toFill <= 0.0f) return 0;

        if (doFill) {
            if (container.stackTagCompound == null) {
                container.stackTagCompound = new NBTTagCompound(); //[cite: 5]
            }
            if (!container.stackTagCompound.hasKey("Fluid")) {
                NBTTagCompound fluidTag = new NBTTagCompound();
                resource.copy().writeToNBT(fluidTag);
                container.stackTagCompound.setTag("Fluid", fluidTag); //[cite: 5]
            }
            setFluidAmountFloat(container, currentFloat + toFill);
        }

        return (int) Math.ceil(toFill);
    }

    @Override
    public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain) {
        if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("Fluid")) {
            return null; //[cite: 5]
        }

        FluidStack contained = getFluid(container);
        if (contained == null) return null; //[cite: 5]

        float currentFloat = getFluidAmountFloat(container);
        float toDrain = Math.min(currentFloat, (float) maxDrain);

        if (toDrain <= 0.0f) return null;

        if (doDrain) {
            setFluidAmountFloat(container, currentFloat - toDrain);
        }

        FluidStack returned = contained.copy();
        returned.amount = (int) Math.ceil(toDrain);
        return returned;
    }

    // ==========================================
    // VISUALS & DYNAMIC TOOLTIPS
    // ==========================================

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        FluidStack fluid = getFluid(stack); //[cite: 5]
        if (fluid != null && fluid.getFluid() != null) { //[cite: 5]
            String fluidName = fluid.getLocalizedName(); //[cite: 5]
            return fluidName + " " + StatCollector.translateToLocal("item.universal_canister.name"); //[cite: 5]
        }
        return StatCollector.translateToLocal("item.universal_canister.empty.name"); //[cite: 5]
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        FluidStack fluid = getFluid(stack); //[cite: 5]
        if (fluid != null && fluid.getFluid() != null) { //[cite: 5]
            list.add("\u00a77Fluid: \u00a7b" + fluid.getLocalizedName()); //[cite: 5]
            // Modified to show the accurate float value layout to the player
            list.add("\u00a77Amount: \u00a7e" + String.format("%.2f", getFluidAmountFloat(stack)) + " / " + capacity + " mB");
        } else {
            list.add("\u00a78Empty"); //[cite: 5]
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true; //[cite: 5]
    }

    @Override
    public int getRenderPasses(int metadata) {
        return 2; //[cite: 5]
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        if (pass == 1 && getFluid(stack) == null) { //[cite: 5]
            return this.iconFluidLayer; //[cite: 5]
        }
        return pass == 1 ? this.iconFluidLayer : this.itemIcon; //[cite: 5]
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int pass) {
        if (pass == 1) {
            FluidStack fluid = getFluid(stack); //[cite: 5]
            if (fluid != null && fluid.getFluid() != null) { //[cite: 5]
                return fluid.getFluid().getColor(fluid); //[cite: 5]
            }
            return 0xFFFFFF; //[cite: 5]
        }
        return 0xFFFFFF; //[cite: 5]
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        this.itemIcon = register.registerIcon("htgt6:UniversalCanister"); //[cite: 5]
        this.iconFluidLayer = register.registerIcon("htgt6:UniversalCanister"); //[cite: 5]
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item)); //[cite: 5]
        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) { //[cite: 5]
            ItemStack filled = new ItemStack(item); //[cite: 5]
            FluidStack fs = new FluidStack(fluid, this.capacity); //[cite: 5]

            NBTTagCompound nbt = new NBTTagCompound(); //[cite: 5]
            NBTTagCompound fluidTag = new NBTTagCompound(); //[cite: 5]
            fs.writeToNBT(fluidTag); //[cite: 5]
            fluidTag.setFloat("AmountFloat", (float) this.capacity); // Setup base float for sub-items
            nbt.setTag("Fluid", fluidTag); //[cite: 5]
            filled.stackTagCompound = nbt; //[cite: 5]

            list.add(filled); //[cite: 5]
        }
    }

    public ItemStack EmptyChain() {
        return new ItemStack(this); //[cite: 5]
    }
    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return getFluid(stack) != null; //[cite: 5]
    }
    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return new ItemStack(this); //[cite: 5]
    }
}