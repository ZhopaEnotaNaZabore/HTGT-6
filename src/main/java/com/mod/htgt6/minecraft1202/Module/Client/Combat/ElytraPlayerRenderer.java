package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.ElytraFlight;
import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderPlayerEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public final class ElytraPlayerRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation("htgt6", "textures/entity/combat_update/elytra.png");
    private final Model model = new Model();

    /** Called from the transformed RenderPlayer.rotateCorpse while its entity matrix is active. */
    public static void applyFlightRotation(net.minecraft.entity.player.EntityPlayer player, float partialTicks) {
        if (!ElytraFlight.isFlying(player)) return;
        float flyingTicks = ElytraFlight.getFlyingTicks(player) + partialTicks;
        float blend = MathHelper.clamp_float(flyingTicks * flyingTicks / 100.0F, 0.0F, 1.0F);
        GL11.glRotatef(blend * (-90.0F - player.rotationPitch), 1.0F, 0.0F, 0.0F);

        Vec3 look = player.getLook(partialTicks);
        double motionHorizontal = player.motionX * player.motionX + player.motionZ * player.motionZ;
        double lookHorizontal = look.xCoord * look.xCoord + look.zCoord * look.zCoord;
        if (motionHorizontal > 1.0E-6D && lookHorizontal > 1.0E-6D) {
            double cosine = (player.motionX * look.xCoord + player.motionZ * look.zCoord)
                    / Math.sqrt(motionHorizontal * lookHorizontal);
            cosine = Math.max(-1.0D, Math.min(1.0D, cosine));
            double cross = player.motionX * look.zCoord - player.motionZ * look.xCoord;
            GL11.glRotatef((float) (Math.signum(cross) * Math.acos(cosine) * 180.0D / Math.PI),
                    0.0F, 1.0F, 0.0F);
        }
    }

    /**
     * Render while RenderPlayer's entity matrix is still active. RenderPlayerEvent.Post is
     * fired after RenderLivingBase has popped that matrix, which leaves the animated wings
     * at a fixed world/render origin instead of attaching them to the player.
     */
    @SubscribeEvent public void render(RenderPlayerEvent.Specials.Post event) {
        ItemStack chest = event.entityPlayer.inventory.armorInventory[2];
        if (chest == null || chest.getItem() != CombatContent.elytra) return;
        try(RenderStateGuard112 ignored=RenderStateGuard112.capture()){
            GL11.glPushMatrix();try{GL11.glTranslatef(0.0F,0.0F,0.125F);Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);GL11.glColor4f(1,1,1,1);model.setAngles(event.entityPlayer.motionX,event.entityPlayer.motionY,event.entityPlayer.motionZ,event.entityPlayer.isSneaking(),ElytraFlight.isFlying(event.entityPlayer));model.render(0.0625F);}finally{GL11.glPopMatrix();}
        }
    }

    private static final class Model extends ModelBase {
        private final ModelRenderer left, right;
        Model() {
            textureWidth = 64; textureHeight = 32;
            left = new ModelRenderer(this, 22, 0); left.addBox(-10, 0, 0, 10, 20, 2,1.0F);
            right = new ModelRenderer(this, 22, 0); right.mirror = true; right.addBox(0, 0, 0, 10, 20, 2,1.0F);
        }
        void setAngles(double motionX,double motionY,double motionZ,boolean sneaking,boolean flying) {
            float x=.2617994F,z=-.2617994F,y=0F,pointY=0F;
            if(flying){float blend=1F;double length=Math.sqrt(motionX*motionX+motionY*motionY+motionZ*motionZ);if(motionY<0&&length>1.0E-4D)blend=1F-(float)Math.pow(-motionY/length,1.5D);x=blend*.34906584F+(1F-blend)*x;z=blend*-1.5707964F+(1F-blend)*z;}
            else if(sneaking){x=.6981317F;z=-.7853982F;pointY=3F;y=.08726646F;}
            left.rotationPointX=5F;left.rotationPointY=pointY;left.rotateAngleX=x;left.rotateAngleY=y;left.rotateAngleZ=z;
            right.rotationPointX=-5F;right.rotationPointY=pointY;right.rotateAngleX=x;right.rotateAngleY=-y;right.rotateAngleZ=-z;
        }
        void render(float scale) { left.render(scale); right.render(scale); }
    }
}
