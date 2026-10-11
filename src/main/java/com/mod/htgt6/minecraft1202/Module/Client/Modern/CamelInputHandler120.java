package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityCamel120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.CamelMountSystem120;
import com.mod.htgt6.minecraft1202.Module.Network.Modern.PacketCamelDash120;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;

/** Maps the normal 1.7 jump key to the Camel's charged dash without pretending it is a horse. */
public final class CamelInputHandler120 {
    private final Minecraft minecraft=Minecraft.getMinecraft();
    private int chargeTicks;
    private boolean charging;
    @SubscribeEvent public void onClientTick(TickEvent.ClientTickEvent event){
        if(event.phase!=TickEvent.Phase.END||minecraft.thePlayer==null)return;
        boolean controlling=minecraft.thePlayer.ridingEntity instanceof EntityCamel120
                &&minecraft.thePlayer.ridingEntity.riddenByEntity==minecraft.thePlayer;
        if(!controlling){chargeTicks=0;charging=false;return;}
        EntityCamel120 camel=(EntityCamel120)minecraft.thePlayer.ridingEntity;
        boolean jump=minecraft.gameSettings.keyBindJump.getIsKeyPressed();
        if(jump&&camel.isSaddled()&&camel.getDashCooldown()==0&&camel.onGround){charging=true;if(chargeTicks<10)chargeTicks++;}
        else if(charging){
            CamelMountSystem120.NETWORK.sendToServer(new PacketCamelDash120(Math.min(90,chargeTicks*9)));
            chargeTicks=0;charging=false;
        }
    }
}
