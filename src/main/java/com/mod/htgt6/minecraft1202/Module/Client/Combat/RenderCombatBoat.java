package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatBoat;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import net.minecraft.client.renderer.entity.RenderBoat;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import net.minecraft.util.MathHelper;

public final class RenderCombatBoat extends RenderBoat {
    private static final ResourceLocation[] TEXTURES = textures();
    @Override public void doRender(EntityBoat boat, double x, double y, double z, float yaw, float partialTick) {
        ((ModelCombatBoat120) modelBoat).setPartialTick(partialTick);
        GL11.glPushMatrix();
        try {
            // Legacy posY includes yOffset; modern rendering is relative to the bounding-box bottom.
            GL11.glTranslated(x, y - boat.yOffset + 0.375D, z);
            GL11.glRotatef(180F-yaw,0,1,0);
            float hit=boat.getTimeSinceHit()-partialTick;
            float damage=Math.max(0,boat.getDamageTaken()-partialTick);
            if(hit>0)GL11.glRotatef(MathHelper.sin(hit)*hit*damage/10F*boat.getForwardDirection(),1,0,0);
            bindEntityTexture(boat);
            GL11.glScalef(-1,-1,1);
            GL11.glRotatef(90,0,1,0);
            modelBoat.render(boat,0,0,0,0,0,0.0625F);
        } finally { GL11.glPopMatrix(); }
    }
    public RenderCombatBoat() {
        modelBoat = new ModelCombatBoat120();
    }
    @Override protected ResourceLocation getEntityTexture(EntityBoat entity) {
        int variant = entity instanceof EntityCombatBoat ? ((EntityCombatBoat) entity).getVariant() : 1;
        return TEXTURES[Math.max(0, Math.min(CombatContent.BOAT_TYPES.length - 1, variant))];
    }
    private static ResourceLocation[] textures() {
        ResourceLocation[] values = new ResourceLocation[CombatContent.BOAT_TYPES.length];
        for (int i = 0; i < values.length; ++i) values[i] = new ResourceLocation("htgt6",
                "textures/entity/combat_update/boat/" + CombatContent.BOAT_TYPES[i] + ".png");
        return values;
    }
}
