package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.ElytraFlight;
import com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketElytraStart;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatUpdate;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;

/** Combat-module-only Elytra jump trigger, independent from the 1.12 inventory module. */
public final class ElytraClientHandler {
    private final Minecraft minecraft = Minecraft.getMinecraft();
    private boolean jumpWasDown;

    @SubscribeEvent public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || minecraft.thePlayer == null) return;
        boolean jump = minecraft.gameSettings.keyBindJump.getIsKeyPressed();
        if (jump && !jumpWasDown && !minecraft.thePlayer.onGround && minecraft.thePlayer.motionY < 0.0D) {
            if(ElytraFlight.start(minecraft.thePlayer))CombatUpdate.NETWORK.sendToServer(new PacketElytraStart());
        }
        jumpWasDown = jump;
    }
}
