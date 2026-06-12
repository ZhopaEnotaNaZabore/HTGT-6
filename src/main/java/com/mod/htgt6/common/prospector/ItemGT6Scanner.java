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

public class ItemGT6Scanner extends Item implements IElectricItem {
    protected final double maxEnergy = 20000.0;
    protected final int tier = 1;
    protected final double transferLimit = 32.0;

    private final int RADIUS_XZ = 15;
    private final int RADIUS_Y = 32;

    private static class BulkData {
        String name;
        Block block;
        int meta;
        int count = 0;

        BulkData(String name, Block block, int meta) {
            this.name = name;
            this.block = block;
            this.meta = meta;
        }
    }

    public ItemGT6Scanner() {
        this.setUnlocalizedName("prospectorScanner");
        this.setTextureName(HTGT6.MOD_ID + ":Prospector");
        this.setMaxStackSize(1);
        this.setMaxDamage(250);
        GameRegistry.registerItem(this, "htgt6_prospector_scanner");
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

        int mode = 0;
        if (stack.hasTagCompound()) {
            mode = stack.getTagCompound().getInteger("ScanMode");
        }
        String modeName = mode == 0 ? "Координаты" : (mode == 1 ? "Жилы" : "Слои камня");
        list.add(EnumChatFormatting.YELLOW + "Режим: " + EnumChatFormatting.WHITE + modeName);

        double charge = ElectricItem.manager.getCharge(stack);
        list.add(EnumChatFormatting.AQUA + "Energy: " + (int)charge + " / " + (int)this.maxEnergy + " EU");
    }

    private void toggleMode(ItemStack stack, EntityPlayer player) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }
        int mode = nbt.getInteger("ScanMode");
        mode = (mode + 1) % 3;
        nbt.setInteger("ScanMode", mode);

        String modeText = "";
        if (mode == 0) modeText = "Координаты (1 руда на строку)";
        else if (mode == 1) modeText = "Жилы (Подсчет всех руд)";
        else if (mode == 2) modeText = "Слои (Текущие породы под игроком)";

        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Режим сканера: " + EnumChatFormatting.AQUA + modeText));
        syncItemToClient(player, stack);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (player.isSneaking()) {
            if (!world.isRemote) {
                toggleMode(stack, player);
            }
            return stack;
        }

        double scanCost = 250.0;
        if (player.capabilities.isCreativeMode || ElectricItem.manager.use(stack, scanCost, player)) {
            if (!world.isRemote) {
                performScan(stack, player, world, (int)player.posX, (int)player.posY, (int)player.posZ);
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
        if (player.isSneaking()) {
            if (!world.isRemote) {
                toggleMode(stack, player);
            }
            return true;
        }

        double scanCost = 250.0;
        if (player.capabilities.isCreativeMode || ElectricItem.manager.use(stack, scanCost, player)) {
            if (!world.isRemote) {
                performScan(stack, player, world, x, y, z);
            }
            return true;
        } else {
            if (!world.isRemote) {
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Недостаточно энергии для сканирования!"));
            }
        }
        return false;
    }

    private void performScan(ItemStack stack, EntityPlayer player, World world, int centerX, int centerY, int centerZ) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }

        int mode = nbt.getInteger("ScanMode");
        NBTTagList resultList = new NBTTagList();

        player.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "Запуск сканирования..."));

        if (mode == 0) {
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
                            if (prefixBlock.mPrefix.mNameInternal.toLowerCase().contains("ore")) {
                                short matID = 0;
                                TileEntity te = world.getTileEntity(scanX, scanY, scanZ);
                                if (te instanceof PrefixBlockTileEntity) matID = ((PrefixBlockTileEntity) te).mMetaData;

                                // FIX: Direct array lookup with bounds verification to avoid type mismatch
                                OreDictMaterial material = (matID >= 0 && matID < OreDictMaterial.MATERIAL_ARRAY.length) ? OreDictMaterial.MATERIAL_ARRAY[matID] : null;
                                if (material != null && material.mNameLocal != null && !material.mNameLocal.equalsIgnoreCase("null")) {
                                    NBTTagCompound tag = new NBTTagCompound();
                                    tag.setString("Name", material.mNameLocal + " Ore");
                                    tag.setString("BlockID", Block.blockRegistry.getNameForObject(block));
                                    tag.setInteger("Meta", matID);
                                    tag.setInteger("X", scanX);
                                    tag.setInteger("Y", scanY);
                                    tag.setInteger("Z", scanZ);
                                    resultList.appendTag(tag);
                                }
                                continue;
                            }
                        }

                        if (block != null && !block.isAir(world, scanX, scanY, scanZ)) {
                            int meta = world.getBlockMetadata(scanX, scanY, scanZ);
                            ItemStack bStack = new ItemStack(block, 1, meta);
                            if (bStack.getItem() != null) {
                                for (int id : OreDictionary.getOreIDs(bStack)) {
                                    if (OreDictionary.getOreName(id).startsWith("ore")) {
                                        NBTTagCompound tag = new NBTTagCompound();
                                        tag.setString("Name", OreDictionary.getOreName(id).substring(3) + " Ore");
                                        tag.setString("BlockID", Block.blockRegistry.getNameForObject(block));
                                        tag.setInteger("Meta", meta);
                                        tag.setInteger("X", scanX);
                                        tag.setInteger("Y", scanY);
                                        tag.setInteger("Z", scanZ);
                                        resultList.appendTag(tag);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            Map<String, BulkData> aggregator = new HashMap<String, BulkData>();

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

                            if (mode == 1 && prefixName.contains("ore")) {
                                short matID = 0;
                                TileEntity te = world.getTileEntity(scanX, scanY, scanZ);
                                if (te instanceof PrefixBlockTileEntity) matID = ((PrefixBlockTileEntity) te).mMetaData;

                                // FIX: Direct array lookup with bounds verification to avoid type mismatch
                                OreDictMaterial material = (matID >= 0 && matID < OreDictMaterial.MATERIAL_ARRAY.length) ? OreDictMaterial.MATERIAL_ARRAY[matID] : null;
                                if (material != null && material.mNameLocal != null && !material.mNameLocal.equalsIgnoreCase("null")) {
                                    String name = material.mNameLocal + " Ore";
                                    String key = Block.blockRegistry.getNameForObject(block) + ":" + matID;
                                    if (!aggregator.containsKey(key)) aggregator.put(key, new BulkData(name, block, matID));
                                    aggregator.get(key).count++;
                                }
                            }
                            else if (mode == 2 && (prefixName.contains("rock") || prefixName.contains("stone") || prefixName.contains("corundum"))) {
                                short matID = 0;
                                TileEntity te = world.getTileEntity(scanX, scanY, scanZ);
                                if (te instanceof PrefixBlockTileEntity) matID = ((PrefixBlockTileEntity) te).mMetaData;

                                // FIX: Direct array lookup with bounds verification to avoid type mismatch
                                OreDictMaterial material = (matID >= 0 && matID < OreDictMaterial.MATERIAL_ARRAY.length) ? OreDictMaterial.MATERIAL_ARRAY[matID] : null;
                                if (material != null && material.mNameLocal != null && !material.mNameLocal.equalsIgnoreCase("null")) {
                                    String key = Block.blockRegistry.getNameForObject(block) + ":" + matID;
                                    if (!aggregator.containsKey(key)) aggregator.put(key, new BulkData(material.mNameLocal, block, matID));
                                    aggregator.get(key).count++;
                                }
                            }
                        }

                        if (mode == 1 && block != null && !(block instanceof PrefixBlock) && !block.isAir(world, scanX, scanY, scanZ)) {
                            int meta = world.getBlockMetadata(scanX, scanY, scanZ);
                            ItemStack bStack = new ItemStack(block, 1, meta);
                            if (bStack.getItem() != null) {
                                for (int id : OreDictionary.getOreIDs(bStack)) {
                                    String dictName = OreDictionary.getOreName(id);
                                    if (dictName.startsWith("ore")) {
                                        String cleanName = dictName.substring(3) + " Ore";
                                        String key = Block.blockRegistry.getNameForObject(block) + ":" + meta;
                                        if (!aggregator.containsKey(key)) aggregator.put(key, new BulkData(cleanName, block, meta));
                                        aggregator.get(key).count++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (BulkData data : aggregator.values()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("Name", data.name);
                tag.setString("BlockID", Block.blockRegistry.getNameForObject(data.block));
                tag.setInteger("Meta", data.meta);
                tag.setInteger("Count", data.count);
                resultList.appendTag(tag);
            }
        }

        nbt.setTag("OreResults", resultList);
        stack.setTagCompound(nbt);

        syncItemToClient(player, stack);

        player.openGui(HTGT6.instance, GuiHandler.SCANNER_GUI_ID, world, centerX, centerY, centerZ);
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