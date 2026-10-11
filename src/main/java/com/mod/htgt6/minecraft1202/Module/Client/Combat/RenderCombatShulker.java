package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderCombatShulker extends RenderLiving {
    private static final ResourceLocation TEXTURE = new ResourceLocation("htgt6", "textures/entity/combat_update/shulker/shulker.png");
    private static final String[] COLORS={"white","orange","magenta","light_blue","yellow","lime","pink","gray","light_gray","cyan","purple","blue","brown","green","red","black"};
    private static final ResourceLocation[] TEXTURES=new ResourceLocation[16];
    static{for(int i=0;i<16;i++)TEXTURES[i]=new ResourceLocation("htgt6","textures/entity/combat_update/shulker/shulker_"+COLORS[i]+".png");}
    public RenderCombatShulker() { super(new ModelCombatShulker(), 0.0F); }
    @Override protected ResourceLocation getEntityTexture(Entity entity) { int color=((com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatShulker)entity).getColor();return color<16?TEXTURES[color]:TEXTURE; }
    @Override protected void rotateCorpse(net.minecraft.entity.EntityLivingBase entity,float age,float yaw,float partial){
        int face=((com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatShulker)entity).getAttachFace();
        org.lwjgl.opengl.GL11.glTranslatef(0,.5F,0);
        switch(face){
            case 1:org.lwjgl.opengl.GL11.glRotatef(180,1,0,0);break;
            case 2:org.lwjgl.opengl.GL11.glRotatef(90,1,0,0);break;
            case 3:org.lwjgl.opengl.GL11.glRotatef(-90,1,0,0);break;
            case 4:org.lwjgl.opengl.GL11.glRotatef(-90,0,0,1);break;
            case 5:org.lwjgl.opengl.GL11.glRotatef(90,0,0,1);break;
        }
        org.lwjgl.opengl.GL11.glTranslatef(0,-.5F,0);
        super.rotateCorpse(entity,age,0,partial);
    }
}
