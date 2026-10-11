package com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

final class PotionDolphinsGrace120 extends Potion {
    PotionDolphinsGrace120(int id){super(id,false,0x88A3BE);setPotionName("potion.dolphins_grace");}
    @Override public boolean isReady(int duration,int amplifier){return false;}
    @Override @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(int x,int y,PotionEffect effect,net.minecraft.client.Minecraft mc){
        try(com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112 ignored=com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112.capture()){
            org.lwjgl.opengl.GL11.glColor4f(1,1,1,1);
            mc.getTextureManager().bindTexture(new net.minecraft.util.ResourceLocation("htgt6","textures/gui/effects/dolphins_grace.png"));
            net.minecraft.client.gui.Gui.func_146110_a(x+6,y+7,0,0,18,18,18,18);
        }
    }
}
