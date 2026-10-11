package com.mod.htgt6.sulimited.client;

import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.neptune.WorldProviderNeptune;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.lwjgl.opengl.GL11;

/** Dense blue methane haze, strengthened while the server weather state is storming. */
@SideOnly(Side.CLIENT)
public final class NeptuneAtmosphereRenderHandler {
    @SubscribeEvent public void fog(EntityViewRenderEvent.FogDensity event){
        if(!(event.entity.worldObj.provider instanceof WorldProviderNeptune))return;
        boolean storm=event.entity.worldObj.isThundering();
        float depth=depth(event.entity.posY);
        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
        event.density=(storm?0.092F:0.060F)+depth*0.025F;
        event.setCanceled(true);
    }
    @SubscribeEvent public void color(EntityViewRenderEvent.FogColors event){
        if(!(event.entity.worldObj.provider instanceof WorldProviderNeptune))return;
        boolean storm=event.entity.worldObj.isThundering();
        float depth=depth(event.entity.posY);
        float red=storm?0.018F:0.030F,green=storm?0.075F:0.125F,blue=storm?0.175F:0.285F;
        // The high-pressure core is luminous. The quadratic curve keeps the
        // cloud deck dark while making the final descent visibly brighter.
        float glow=depth*depth;
        event.red=red+(0.48F-red)*glow;
        event.green=green+(0.92F-green)*glow;
        event.blue=blue+(1.00F-blue)*glow;
    }
    private float depth(double y){return (float)Math.max(0.0D,Math.min(1.0D,(160.0D-y)/145.0D));}
}
