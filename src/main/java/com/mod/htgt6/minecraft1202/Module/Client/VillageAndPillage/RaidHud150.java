package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Network.VillageAndPillage.RaidNetwork150.Snapshot;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

/** Independent raid bar so a raid cannot overwrite the dragon/wither singleton. */
public final class RaidHud150 extends Gui {
    private static final RaidHudState160 state = new RaidHudState160();
    // Keep the render guard for world changes between end ticks.
    private static Object world;
    private static boolean registered;
    private static final net.minecraft.util.ResourceLocation BACKGROUND = new net.minecraft.util.ResourceLocation("htgt6", "textures/gui/raid/red_background.png");
    private static final net.minecraft.util.ResourceLocation PROGRESS = new net.minecraft.util.ResourceLocation("htgt6", "textures/gui/raid/red_progress.png");
    public static int progressWidth(float progress) {
        return Float.isNaN(progress) ? 0 : (int)(182 * Math.max(0F, Math.min(1F, progress)));
    }
    public static void register() {
        if(registered)return;registered=true;RaidHud150 hud=new RaidHud150();
        FMLCommonHandler.instance().bus().register(hud);MinecraftForge.EVENT_BUS.register(hud);
    }
    public static void accept(Snapshot snapshot) {
        Minecraft mc=Minecraft.getMinecraft();
        state.accept(snapshot,mc.theWorld,mc.theWorld==null?0:mc.theWorld.provider.dimensionId);
        world=mc.theWorld;
    }
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        Object currentWorld=Minecraft.getMinecraft().theWorld;
        state.tick(currentWorld);
        if(world!=currentWorld)world=null;
    }
    @SubscribeEvent public void render(RenderGameOverlayEvent.Post event) {
        if(event.type!=RenderGameOverlayEvent.ElementType.ALL)return;
        Minecraft mc=Minecraft.getMinecraft();if(mc.gameSettings.hideGUI||mc.theWorld!=world)return;
        Snapshot current=state.snapshot();if(current==null)return;
        int x=event.resolution.getScaledWidth()/2-91,y=BossStatus.statusBarTime>0?32:12;
        String text=StatCollector.translateToLocal("event.minecraft.raid");
        if(current.status!=0)text+=" - "+StatCollector.translateToLocal(current.status==1?"event.minecraft.raid.victory":"event.minecraft.raid.defeat");
        else if(current.remaining>0&&current.remaining<=2)text+=" - "+StatCollector.translateToLocalFormatted("event.minecraft.raid.raiders_remaining",current.remaining);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try {
            GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glColor4f(1,1,1,1);
            GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
            mc.getTextureManager().bindTexture(BACKGROUND);
            Gui.func_146110_a(x,y,0,0,182,5,182,5);
            int width = progressWidth(current.progress);
            if(width>0){mc.getTextureManager().bindTexture(PROGRESS);Gui.func_146110_a(x,y,0,0,width,5,182,5);}
            mc.fontRenderer.drawStringWithShadow(text,x+91-mc.fontRenderer.getStringWidth(text)/2,y-9,0xFFFFFF);
        } finally {GL11.glPopAttrib();}
    }
}
