package com.mod.htgt6.client.GUI;

import codechicken.nei.api.API;
import codechicken.nei.recipe.DefaultOverlayHandler;
import com.mod.htgt6.common.TE.TileEntityAssembler;
import com.mod.htgt6.common.handler.recipe.assembler.ASrecipeHandler;
import com.mod.htgt6.common.inventory.ContainerAssembler;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiAssembler extends GuiContainer {

    // ==========================================
    // TEXTURE
    // ==========================================

    private static final ResourceLocation texture =
            new ResourceLocation(
                    "htgt6",
                    "textures/gui/Assembler.png"
            );

    // ==========================================
    // TILE
    // ==========================================

    private final TileEntityAssembler te;

    // ==========================================
    // BUTTON
    // ==========================================

    private GuiButton powerButton;

    // ==========================================
    // GUI
    // ==========================================

    public GuiAssembler(
            ContainerAssembler container,
            TileEntityAssembler te
    ) {

        super(container);

        this.te = te;

        // STANDARD SIZE
        this.xSize = 176;
        this.ySize = 166;
    }

    // ==========================================
    // INIT
    // ==========================================

    //@Override
   // public void initGui() {

  //      super.initGui();

   //     buttonList.clear();

  //      int guiLeft =
    //            (width - xSize) / 2;

  //      int guiTop =
  //              (height - ySize) / 2;

        // ==========================================
        // POWER BUTTON
        // ==========================================

     //   powerButton = new GuiButton(
      //          0,
     //           guiLeft + 136,
     //           guiTop + 54,
     //           30,
     //           16,
    //            te.enabled
     //                   ? "ON"
     //                   : "OFF"
    //    );

    //    buttonList.add(powerButton);
 //   }

    // ==========================================
    // BUTTON
    // ==========================================

    @Override
    protected void actionPerformed(
            GuiButton button
    ) {

        if (button.id == 0) {

            te.enabled = !te.enabled;

            button.displayString =
                    te.enabled
                            ? "ON"
                            : "OFF";
        }
    }

    // ==========================================
    // BACKGROUND
    // ==========================================

    @Override
    protected void drawGuiContainerBackgroundLayer(
            float partialTicks,
            int mouseX,
            int mouseY
    ) {

        GL11.glColor4f(
                1F,
                1F,
                1F,
                1F
        );

        mc.getTextureManager()
                .bindTexture(texture);

        // ==========================================
        // SMOOTH TEXTURE
        // ==========================================

        GL11.glTexParameteri(
                GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MIN_FILTER,
                GL11.GL_LINEAR
        );

        GL11.glTexParameteri(
                GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MAG_FILTER,
                GL11.GL_LINEAR
        );

        int guiLeft =
                (width - xSize) / 2;

        int guiTop =
                (height - ySize) / 2;

        // ==========================================
        // GUI
        // ==========================================

        drawTexturedModalRect(
                guiLeft,
                guiTop,
                0,
                0,
                xSize,
                ySize
        );

        // ==========================================
        // PROGRESS BAR
        // ==========================================

        if (te.maxProgress > 0
                && te.progress > 0) {

            int progressWidth =
                    (te.progress * 24)
                            / te.maxProgress;

            drawTexturedModalRect(
                    guiLeft + 78,
                    guiTop + 23,
                    176,
                    0,
                    progressWidth,
                    16
            );
        }

        // ==========================================
        // RESET OPENGL
        // ==========================================

        GL11.glTexParameteri(
                GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MIN_FILTER,
                GL11.GL_NEAREST
        );

        GL11.glTexParameteri(
                GL11.GL_TEXTURE_2D,
                GL11.GL_TEXTURE_MAG_FILTER,
                GL11.GL_NEAREST
        );

        GL11.glDisable(GL11.GL_BLEND);

        GL11.glEnable(GL11.GL_ALPHA_TEST);

        GL11.glColor4f(
                1F,
                1F,
                1F,
                1F
        );
    }
    // ==========================================
// READ NBT
// ==========================================


    // ==========================================
    // FOREGROUND
    // ==========================================

    @Override
    protected void drawGuiContainerForegroundLayer(
            int mouseX,
            int mouseY
    ) {

        // ==========================================
        // TITLE
        // ==========================================

        fontRendererObj.drawString(
                "Assembler",
                8,
                6,
                0x404040
        );

        // ==========================================
        // ENERGY
        // ==========================================

        fontRendererObj.drawString(
                "EU: " + te.energy,
                145,
                14,
                0x00FF00
        );

        // ==========================================
        // VOLTAGE
        // ==========================================

        fontRendererObj.drawString(
                te.getMaxInputVoltage()
                        + " EU/t",
                145,
                20,
                0xFFFF00
        );
        // ==========================================
// READ NBT
// ==========================================

        // ==========================================
        // MACHINE TIER
        // ==========================================

        fontRendererObj.drawString(
                te.getTierName(),
                145,
                8,
                te.getTierColor()
        );

        // ==========================================
        // PROGRESS %
        // ==========================================

        if (te.maxProgress > 0) {

            int percent =
                    (te.progress * 100)
                            / te.maxProgress;

            fontRendererObj.drawString(
                    percent + "%",
                    82,
                    22,
                    0xFFFFFF
            );
        }

        // ==========================================
        // MACHINE OFF
        // ==========================================

        if (!te.enabled) {

            fontRendererObj.drawString(
                    "OFF",
                    138,
                    22,
                    0xFF0000
            );
        }

    API.registerRecipeHandler(new ASrecipeHandler());
    API.registerUsageHandler(new ASrecipeHandler());

    API.registerGuiOverlay(
    GuiAssembler.class,
            "assembling_machine"
            );

   API.registerGuiOverlayHandler(
    GuiAssembler.class,
            new DefaultOverlayHandler(),
        "assembling_machine"
                );
}}
