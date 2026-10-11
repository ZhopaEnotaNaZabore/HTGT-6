package com.mod.htgt6.common.agent;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.UUID;

/** Owns the deterministic server-side identity used by the diagnostic agent. */
public final class HTGT6AgentController {
    public static final String NAME = "[HTGT6-Agent]";
    private static final GameProfile PROFILE = new GameProfile(
            UUID.fromString("08aa4d40-4474-4b67-9ce8-8be9b41c5626"), NAME);

    private HTGT6AgentController() {}

    public static FakePlayer at(EntityPlayerMP operator) {
        WorldServer world = (WorldServer) operator.worldObj;
        FakePlayer agent = FakePlayerFactory.get(world, PROFILE);
        agent.setPositionAndRotation(operator.posX, operator.posY, operator.posZ,
                operator.rotationYaw, operator.rotationPitch);
        return agent;
    }
}
