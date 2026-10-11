package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulGuardian;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.ResourceLocation;

/** Exact 1.12 GuardianSound volume/pitch envelope on the 1.7.10 sound API. */
public final class GuardianBeamSound112 extends MovingSound {
    private final EntityBountifulGuardian guardian;

    public GuardianBeamSound112(EntityBountifulGuardian guardian) {
        super(new ResourceLocation("htgt6", "guardian.attack"));
        this.guardian = guardian;
        field_147666_i = ISound.AttenuationType.NONE;
        repeat = true;
        field_147665_h = 0;
    }

    @Override
    public void update() {
        if (guardian.isDead || !guardian.hasBeamTarget()) {
            donePlaying = true;
            return;
        }
        xPosF = (float)guardian.posX;
        yPosF = (float)guardian.posY;
        zPosF = (float)guardian.posZ;
        float progress = guardian.getBeamProgress(0.0F);
        volume = progress * progress;
        field_147663_c = 0.7F + 0.5F * progress;
    }
}
