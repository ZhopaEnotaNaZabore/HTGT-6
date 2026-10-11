package com.mod.htgt6.client.inventory112;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

/** Minecraft 1.12 tutorial and recipe-unlock toast renderer. */
public final class RecipeToast112 extends Gui {
    private static final ResourceLocation BACKGROUND =
            new ResourceLocation("htgt6", "textures/gui/toasts.png");
    private static final Queue<Toast> QUEUE = new ArrayDeque<Toast>();
    private static final List<Toast> ACTIVE = new ArrayList<Toast>();
    private final Minecraft minecraft = Minecraft.getMinecraft();

    static void showTutorial(String id, int iconX, int iconY, String title,
            String description, boolean progressBar) {
        for (Toast toast : ACTIVE) if (toast.tutorial && toast.id.equals(id)) {
            toast.closing = false;
            toast.closingAge = 0;
            return;
        }
        ACTIVE.add(Toast.tutorial(id, iconX, iconY, title, description, progressBar));
    }

    static void setTutorialProgress(String id, float progress) {
        for (Toast toast : ACTIVE) if (toast.tutorial && toast.id.equals(id)) {
            toast.targetProgress = Math.max(0.0F, Math.min(1.0F, progress));
            return;
        }
    }

    static void clearTutorials() {
        for (Iterator<Toast> iterator = ACTIVE.iterator(); iterator.hasNext(); )
            if (iterator.next().tutorial) iterator.remove();
    }

    static void resetForWorld() {
        ACTIVE.clear();
        QUEUE.clear();
    }

    static void hideTutorial(String id) {
        for (Iterator<Toast> iterator = ACTIVE.iterator(); iterator.hasNext(); ) {
            Toast toast = iterator.next();
            if (toast.tutorial && toast.id.equals(id)) iterator.remove();
        }
    }

    static void enqueueRecipes(List<ItemStack> outputs) {
        List<ItemStack> copy = new ArrayList<ItemStack>();
        for (ItemStack stack : outputs) if (stack != null) copy.add(stack.copy());
        if (!copy.isEmpty()) QUEUE.add(Toast.recipe(copy));
    }

    @SubscribeEvent public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || minecraft.isGamePaused()) return;
        boolean tutorialVisible = false;
        for (Toast toast : ACTIVE) if (toast.tutorial) { tutorialVisible = true; break; }
        while (!tutorialVisible && !TutorialSystem112.isActive()
                && ACTIVE.size() < 4 && !QUEUE.isEmpty()) ACTIVE.add(QUEUE.poll());
        for (Iterator<Toast> iterator = ACTIVE.iterator(); iterator.hasNext(); ) {
            Toast toast = iterator.next();
            toast.age++;
            toast.progress += (toast.targetProgress - toast.progress) * 0.2F;
            if (!toast.tutorial && toast.age >= 90) toast.closing = true;
            if (toast.closing && ++toast.closingAge >= 10) iterator.remove();
        }
    }

    @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || ACTIVE.isEmpty()) return;
        for (int index = 0; index < ACTIVE.size(); ++index)
            drawToast(event.resolution.getScaledWidth(), ACTIVE.get(index), index);
    }

    private void drawToast(int scaledWidth, Toast current, int index) {
        int slide = current.age < 10 ? 10 - current.age : current.closing ? current.closingAge : 0;
        int x = scaledWidth - 160 + slide * 16;
        int y = 35 + index * 34;
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT
                | GL11.GL_CURRENT_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glTranslatef(x, y, 500.0F);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        minecraft.getTextureManager().bindTexture(BACKGROUND);
        GL11.glColor4f(1, 1, 1, 1);
        drawTexturedModalRect(0, 0, 0, current.tutorial ? 96 : 32, 160, 32);
        if (current.tutorial) {
            drawTexturedModalRect(6, 6, 176 + current.iconX * 20, current.iconY * 20, 20, 20);
        } else {
            ItemStack icon = current.outputs.get((current.age / Math.max(1,
                    100 / current.outputs.size())) % current.outputs.size());
            RenderHelper.enableGUIStandardItemLighting();
            RenderItem.getInstance().renderItemAndEffectIntoGUI(minecraft.fontRenderer,
                    minecraft.getTextureManager(), icon, 8, 8);
            RenderHelper.disableStandardItemLighting();
        }
        String title = minecraft.fontRenderer.trimStringToWidth(current.title, 126);
        String description = minecraft.fontRenderer.trimStringToWidth(current.description, 126);
        minecraft.fontRenderer.drawString(title, 30, 7, 0x500500);
        minecraft.fontRenderer.drawString(description, 30, 18, 0x000000);
        if (current.progressBar) {
            drawRect(3, 28, 157, 29, 0xFFFFFFFF);
            int color = current.targetProgress >= current.progress ? -16755456 : -11206656;
            drawRect(3, 28, 3 + (int) (154.0F * current.progress), 29, color);
        }
        GL11.glPopAttrib();
        GL11.glPopMatrix();
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static final class Toast {
        final boolean tutorial;
        final String id;
        final int iconX, iconY;
        final String title, description;
        final boolean progressBar;
        final List<ItemStack> outputs;
        int age, closingAge;
        float progress, targetProgress;
        boolean closing;

        private Toast(boolean tutorial, String id, int iconX, int iconY, String title,
                String description, boolean progressBar, List<ItemStack> outputs) {
            this.tutorial = tutorial;
            this.id = id;
            this.iconX = iconX;
            this.iconY = iconY;
            this.title = title;
            this.description = description;
            this.progressBar = progressBar;
            this.outputs = outputs;
        }

        static Toast tutorial(String id, int iconX, int iconY, String title,
                String description, boolean progressBar) {
            return new Toast(true, id, iconX, iconY, title, description, progressBar, null);
        }

        static Toast recipe(List<ItemStack> outputs) {
            return new Toast(false, "recipe", 0, 0,
                    StatCollector.translateToLocal("recipe.toast.title"),
                    StatCollector.translateToLocal("recipe.toast.description"), false, outputs);
        }
    }
}
