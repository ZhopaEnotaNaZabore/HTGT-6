package com.mod.htgt6.client.GUI;

import com.mod.htgt6.common.inventory.ContainerScanner;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

public class GuiBulkScanner extends GuiContainer {
    private final ItemStack scannerStack;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 10; // Fits comfortably inside single lines on 256x256 frame

    public GuiBulkScanner(EntityPlayer player) {
        super(new ContainerScanner(player.inventory));
        this.scannerStack = player.getHeldItem();
        this.xSize = 256;
        this.ySize = 256;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        this.buttonList.add(new GuiButton(0, k + 12, l + this.ySize - 22, 40, 16, "<<"));
        this.buttonList.add(new GuiButton(1, k + this.xSize - 52, l + this.ySize - 22, 40, 16, ">>"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            if (currentPage > 0) currentPage--;
        } else if (button.id == 1) {
            if (currentPage < getMaxPages() - 1) currentPage++;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int maxPages = getMaxPages();
        for (Object obj : this.buttonList) {
            GuiButton btn = (GuiButton) obj;
            if (btn.id == 0) btn.enabled = (currentPage > 0);
            if (btn.id == 1) btn.enabled = (currentPage < maxPages - 1);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int getMaxPages() {
        if (scannerStack != null && scannerStack.hasTagCompound()) {
            NBTTagCompound nbt = scannerStack.getTagCompound();
            if (nbt.hasKey("OreResults")) {
                NBTTagList oreList = nbt.getTagList("OreResults", 10);
                int totalItems = oreList.tagCount();
                if (totalItems == 0) return 1;
                return (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
            }
        }
        return 1;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.drawDefaultBackground();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        int frameBorderColor = 0xFF555555;
        int techCyanLine    = 0xFF00AAFF;
        int screenBackground = 0xFF0A0F14;
        int gridLineColor    = 0x2200AAFF;

        drawRect(k, l, k + this.xSize, l + this.ySize, frameBorderColor);
        drawRect(k + 3, l + 3, k + this.xSize - 3, l + this.ySize - 3, screenBackground);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        for (int xOffset = 16; xOffset < this.xSize - 6; xOffset += 16) {
            drawRect(k + xOffset, l + 3, k + xOffset + 1, l + this.ySize - 3, gridLineColor);
        }
        for (int yOffset = 24; yOffset < this.ySize - 25; yOffset += 20) {
            drawRect(k + 3, l + yOffset, k + this.xSize - 3, l + yOffset + 1, gridLineColor);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);

        drawRect(k + 4, l + 4, k + this.xSize - 4, l + 18, 0x2500AAFF);
        drawRect(k + 4, l + 18, k + this.xSize - 4, l + 19, techCyanLine);
        drawRect(k + 4, l + this.ySize - 25, k + this.xSize - 4, l + this.ySize - 4, 0x40000000);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString(EnumChatFormatting.AQUA + "📡 ПРОСПЕКТОР: КРУПНЫЕ ЗАЛЕЖИ", 12, 7, 0x00AAFF);

        if (scannerStack != null && scannerStack.hasTagCompound()) {
            NBTTagCompound nbt = scannerStack.getTagCompound();
            if (nbt.hasKey("OreResults")) {
                NBTTagList oreList = nbt.getTagList("OreResults", 10);

                int startIdx = currentPage * ITEMS_PER_PAGE;
                int endIdx = Math.min(startIdx + ITEMS_PER_PAGE, oreList.tagCount());
                int yOffset = 24;

                if (oreList.tagCount() == 0) {
                    this.fontRendererObj.drawString(EnumChatFormatting.RED + "⚠ В данном радиусе крупных руд не найдено.", 12, yOffset + 4, 0xFF5555);
                } else {
                    for (int i = startIdx; i < endIdx; i++) {
                        NBTTagCompound oreTag = oreList.getCompoundTagAt(i);
                        String name = oreTag.getString("Name");
                        String blockID = oreTag.getString("BlockID");
                        int meta = oreTag.getInteger("Meta");

                        // FIX 1: Pulls the actual total count integer
                        int count = oreTag.getInteger("Count");

                        Block oreBlock = Block.getBlockFromName(blockID);
                        if (oreBlock != null) {
                            ItemStack renderStack = new ItemStack(oreBlock, 1, meta);

                            GL11.glPushMatrix();
                            RenderHelper.enableGUIStandardItemLighting();
                            this.itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), renderStack, 12, yOffset + 2);
                            RenderHelper.disableStandardItemLighting();
                            GL11.glPopMatrix();
                        }

                        // Displays name in native, crisp, readable light green text style
                        this.fontRendererObj.drawString(name, 34, yOffset + 6, 0x55FF55);

                        // FIX 2: Formats with requested "шт" suffix string layout
                        String countText = EnumChatFormatting.WHITE + String.valueOf(count) + " шт";

                        // Dynamic right alignment boundaries
                        int stringWidth = this.fontRendererObj.getStringWidth(countText);
                        this.fontRendererObj.drawString(countText, this.xSize - 14 - stringWidth, yOffset + 6, 0xFFFFFF);

                        yOffset += 20;
                    }
                }

                String pageText = (currentPage + 1) + " / " + getMaxPages();
                int pageTextWidth = this.fontRendererObj.getStringWidth(pageText);
                this.fontRendererObj.drawString(pageText, this.xSize / 2 - pageTextWidth / 2, this.ySize - 18, 0x00AAFF);
            }
        }
    }
}