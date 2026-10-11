package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Client-only spyglass zoom; this class is loaded only through Minecraft120BackportClient. */
public final class Backport120ClientHandler {
    public static final float SCOPE_RESET=0.5F;
    public static final float SCOPE_TARGET=1.125F;
    public static final float SCOPE_LERP=0.5F;
    private float scopeScale=SCOPE_RESET;
    private static final ResourceLocation SCOPE=new ResourceLocation("htgt6","textures/misc/spyglass_scope.png");
    @SubscribeEvent public void onFov(FOVUpdateEvent event){
        if(event.entity!=Minecraft.getMinecraft().thePlayer)return;ItemStack using=event.entity.getItemInUse();
        if(using!=null&&using.getItem()==Backport120Content.spyglass){
            event.newfov=0.1F;scopeScale+=(SCOPE_TARGET-scopeScale)*SCOPE_LERP;
        }else scopeScale=SCOPE_RESET;
    }
    @SubscribeEvent public void onOverlay(RenderGameOverlayEvent.Post event){
        if(event.type!=RenderGameOverlayEvent.ElementType.ALL)return;
        Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null)return;
        if(mc.gameSettings.thirdPersonView!=0)return;
        ItemStack using=mc.thePlayer.getItemInUse();
        if(using==null||using.getItem()!=Backport120Content.spyglass)return;
        int width=event.resolution.getScaledWidth(),height=event.resolution.getScaledHeight();
        int size=(int)(Math.min(width,height)*scopeScale),left=(width-size)/2,top=(height-size)/2;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
        Gui.drawRect(0,0,left,height,0xFF000000);Gui.drawRect(left+size,0,width,height,0xFF000000);
        Gui.drawRect(left,0,left+size,top,0xFF000000);Gui.drawRect(left,top+size,left+size,height,0xFF000000);
        mc.getTextureManager().bindTexture(SCOPE);GL11.glColor4f(1,1,1,1);
        Tessellator t=Tessellator.instance;t.startDrawingQuads();
        t.addVertexWithUV(left,top+size,-90,0,1);t.addVertexWithUV(left+size,top+size,-90,1,1);
        t.addVertexWithUV(left+size,top,-90,1,0);t.addVertexWithUV(left,top,-90,0,0);t.draw();
        GL11.glPopAttrib();
    }
    @SubscribeEvent public void onHand(RenderHandEvent event){
        Minecraft mc=Minecraft.getMinecraft();if(mc.thePlayer==null||mc.gameSettings.thirdPersonView!=0)return;
        ItemStack using=mc.thePlayer.getItemInUse();
        if(using!=null&&using.getItem()==Backport120Content.spyglass)event.setCanceled(true);
    }
}
