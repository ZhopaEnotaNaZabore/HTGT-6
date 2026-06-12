package com.mod.htgt6.common.prospector;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.GuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregapi.block.prefixblock.PrefixBlock;
import gregapi.block.prefixblock.PrefixBlockTileEntity;
import gregapi.oredict.OreDictMaterial;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemGT6BulkScanner extends Item implements IElectricItem {

    private final int RADIUS_XZ = 15;
    private final int RADIUS_Y = 32;

    protected final double maxEnergy = 20000.0;
    protected final int tier = 1;
    protected final double transferLimit = 32.0;

    private static class BulkOreData {
        String name;
        Block block;
        int meta;
        int count = 0;

        BulkOreData(String name, Block block, int meta) {
            this.name = name;
            this.block = block;
            this.meta = meta;
        }
    }

    public ItemGT6BulkScanner() {
        this.setUnlocalizedName("bulkProspectorScanner");
        this.setTextureName(HTGT6.MOD_ID + ":BulkProspector");
        this.setMaxStackSize(1);
        this.setMaxDamage(250);
        GameRegistry.registerItem(this, "htgt6_bulk_prospector_scanner");
    }

    private void syncItemToClient(EntityPlayer player, ItemStack stack) {
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            int slotIndex = player.inventory.currentItem + 36;
            playerMP.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(0, slotIndex, stack));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        list.add(EnumChatFormatting.GRAY + "Используется для поиска скрытых руд.");
        list.add(EnumChatFormatting.DARK_AQUA + "Радиус поиска: " + EnumChatFormatting.WHITE + "15x32x15");

        double charge = ElectricItem.manager.getCharge(stack);
        list.add(EnumChatFormatting.AQUA + "Energy: " + (int)charge + " / " + (int)this.maxEnergy + " EU");
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        double scanCost = 250.0;
        if (player.capabilities.isCreativeMode || ElectricItem.manager.use(stack, scanCost, player)) {
            if (!world.isRemote) {
                performBulkScan(stack, player, world, (int)player.posX, (int)player.posY, (int)player.posZ);
            }
        } else {
            if (!world.isRemote) {
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Недостаточно энергии для сканирования!"));
            }
        }
        return stack;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        double scanCost = 250.0;
        if (player.capabilities.isCreativeMode || ElectricItem.manager.use(stack, scanCost, player)) {
            if (!world.isRemote) {
                performBulkScan(stack, player, world, x, y, z);
            }
            return true;
        } else {
            if (!world.isRemote) {
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Недостаточно энергии для сканирования!"));
            }
        }
        return false;
    }

    private void performBulkScan(ItemStack stack, EntityPlayer player, World world, int centerX, int centerY, int centerZ) {
        player.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Сканирование крупных залежей руды..."));

        Map<String, BulkOreData> foundOres = new HashMap<String, BulkOreData>();

        for (int dx = -RADIUS_XZ; dx <= RADIUS_XZ; dx++) {
            for (int dy = -RADIUS_Y; dy <= RADIUS_Y; dy++) {
                for (int dz = -RADIUS_XZ; dz <= RADIUS_XZ; dz++) {

                    int scanX = centerX + dx;
                    int scanY = centerY + dy;
                    int scanZ = centerZ + dz;

                    if (scanY < 0 || scanY > 255) continue;

                    Block block = world.getBlock(scanX, scanY, scanZ);

                    if (block instanceof PrefixBlock) {
                        PrefixBlock prefixBlock = (PrefixBlock) block;
                        String prefixName = prefixBlock.mPrefix.mNameInternal.toLowerCase();

                        if (prefixName.contains("ore") && !prefixName.contains("small")) {
                            short matID = 0;
                            TileEntity te = world.getTileEntity(scanX, scanY, scanZ);
                            if (te instanceof PrefixBlockTileEntity) {
                                matID = ((PrefixBlockTileEntity) te).mMetaData;
                            }

                            // FIX: Direct array lookup with bounds verification to avoid type mismatch
                            OreDictMaterial material = (matID >= 0 && matID < OreDictMaterial.MATERIAL_ARRAY.length) ? OreDictMaterial.MATERIAL_ARRAY[matID] : null;
                            if (material != null && material.mNameLocal != null && !material.mNameLocal.equalsIgnoreCase("null")) {
                                String oreName = material.mNameLocal + " Ore";
                                String uniqueKey = Block.blockRegistry.getNameForObject(block) + ":" + matID;

                                if (!foundOres.containsKey(uniqueKey)) {
                                    foundOres.put(uniqueKey, new BulkOreData(oreName, block, matID));
                                }
                                foundOres.get(uniqueKey).count++;
                            }
                            continue;
                        }
                    }

                    if (block != null && !block.isAir(world, scanX, scanY, scanZ)) {
                        int meta = world.getBlockMetadata(scanX, scanY, scanZ);
                        ItemStack blockStack = new ItemStack(block, 1, meta);

                        if (blockStack.getItem() != null) {
                            int[] oreIDs = OreDictionary.getOreIDs(blockStack);
                            for (int id : oreIDs) {
                                String dictName = OreDictionary.getOreName(id);
                                if (dictName.startsWith("ore") && !dictName.toLowerCase().contains("small")) {
                                    String cleanName = dictName.substring(3) + " Ore";
                                    String uniqueKey = Block.blockRegistry.getNameForObject(block) + ":" + meta;

                                    if (!foundOres.containsKey(uniqueKey)) {
                                        foundOres.put(uniqueKey, new BulkOreData(cleanName, block, meta));
                                    }
                                    foundOres.get(uniqueKey).count++;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }

        NBTTagList oreList = new NBTTagList();
        for (BulkOreData data : foundOres.values()) {
            NBTTagCompound oreTag = new NBTTagCompound();
            oreTag.setString("Name", data.name);
            oreTag.setInteger("Count", data.count);
            oreTag.setString("BlockID", Block.blockRegistry.getNameForObject(data.block));
            oreTag.setInteger("Meta", data.meta);
            oreList.appendTag(oreTag);
        }
        nbt.setTag("OreResults", oreList);

        syncItemToClient(player, stack);

        player.openGui(HTGT6.instance, GuiHandler.BULK_SCANNER_GUI_ID, world, centerX, centerY, centerZ);

        if (!player.capabilities.isCreativeMode) {
            stack.damageItem(1, player);
        }
    }

    @Override public boolean canProvideEnergy(ItemStack s) { return false; }
    @Override public Item getChargedItem(ItemStack s) { return this; }
    @Override public Item getEmptyItem(ItemStack s) { return this; }
    @Override public double getMaxCharge(ItemStack s) { return this.maxEnergy; }
    @Override public int getTier(ItemStack s) { return this.tier; }
    @Override public double getTransferLimit(ItemStack s) { return this.transferLimit; }
    @Override public boolean showDurabilityBar(ItemStack s) { return true; }
    @Override public double getDurabilityForDisplay(ItemStack s) {
        return 1.0 - (ElectricItem.manager.getCharge(s) / this.maxEnergy);
    }
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item));
        ItemStack charged = new ItemStack(item);
        ElectricItem.manager.charge(charged, this.maxEnergy, Integer.MAX_VALUE, true, false);
        list.add(charged);
    }
}