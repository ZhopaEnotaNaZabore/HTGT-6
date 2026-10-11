package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

/** Official generated sprite geometry and hand display transforms, adapted to legacy callers. */
public final class CrossbowItemRenderer139 implements IItemRenderer {
    private IItemRenderer jsonRenderer;
    public void setJsonRenderer(IItemRenderer renderer){jsonRenderer=renderer;}
    private static float framePartial;
    @cpw.mods.fml.common.eventhandler.SubscribeEvent
    public void renderTick(cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent event){
        if(event.phase==cpw.mods.fml.common.gameevent.TickEvent.Phase.START)framePartial=event.renderTickTime;
    }
    public static float chargeAge(int ticks,float partial){return Math.max(0,ticks+partial-1);}
    public static float chargeProgress(float age,int duration){return Math.max(0,Math.min(1,age/Math.max(1,duration)));}
    public static float chargeWobble(float age,float progress){
        return progress<=.1F?0:net.minecraft.util.MathHelper.sin((age-.1F)*1.3F)*(progress-.1F)*.004F;
    }
    /** Official left-hand JSON transform after ItemTransform.apply(true). */
    public static float[] leftDisplay(boolean firstPerson){
        return firstPerson?new float[]{-1.13F/16,3.2F/16,1.13F/16,-90,0,-35,.68F}
                :new float[]{-2F/16,.1F/16,-3F/16,-90,0,-30,.9F};
    }
    /** Called at the modern left-hand pivot, without legacy main-hand inverse transforms. */
    public static void renderOffhand(ItemStack stack,EntityPlayer player,boolean firstPerson){
        IIcon jsonIcon=com.mod.htgt6.client.model.json.JsonBlockModelLoader.install().itemIcon(stack,player);
        IIcon icon=jsonIcon!=null?jsonIcon:stack.getItem().getIcon(stack,0,player,null,0);if(icon==null)return;
        try(RenderStateGuard112 ignored=RenderStateGuard112.capture()){
            GL11.glPushMatrix();try{
                Minecraft.getMinecraft().getTextureManager().bindTexture(jsonIcon!=null?TextureMap.locationBlocksTexture:TextureMap.locationItemsTexture);
                GL11.glColor4f(1,1,1,1);
                if(firstPerson){
                    int ticks=com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandCrossbowUse120.useTicks(player,stack);
                    if(ticks>=0&&!ItemCrossbow.isCharged(stack)){
                        float age=chargeAge(ticks,framePartial);
                        float progress=chargeProgress(age,ItemCrossbow.chargeDuration(stack));
                        GL11.glTranslatef(.4785682F,-.094387F,.05731531F);
                        GL11.glRotatef(-11.935F,1,0,0);GL11.glRotatef(-65.3F,0,1,0);GL11.glRotatef(9.785F,0,0,1);
                        GL11.glTranslatef(0,chargeWobble(age,progress),.04F*progress);GL11.glScalef(1,1,1+.2F*progress);GL11.glRotatef(45,0,1,0);
                    }
                }else{
                    GL11.glRotatef(-90,1,0,0);GL11.glRotatef(180,0,1,0);GL11.glTranslatef(-.0625F,.125F,-.625F);
                }
                com.mod.htgt6.client.model.json.JsonBlockModelLoader.install().applyItemDisplay(stack.getItem(),firstPerson?"firstperson_lefthand":"thirdperson_lefthand",true);
                renderHeldSprite(icon);
                if(stack.hasEffect())renderGlint();
            }finally{GL11.glPopMatrix();}
        }
    }
    @Override public boolean handleRenderType(ItemStack stack, ItemRenderType type) {
        return type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON
                || jsonRenderer!=null&&jsonRenderer.handleRenderType(stack,type);
    }
    @Override public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack stack, ItemRendererHelper helper) {
        return type==ItemRenderType.EQUIPPED||type==ItemRenderType.EQUIPPED_FIRST_PERSON
                ?helper == ItemRendererHelper.EQUIPPED_BLOCK:jsonRenderer!=null&&jsonRenderer.shouldUseRenderHelper(type,stack,helper);
    }
    @Override public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        if(type!=ItemRenderType.EQUIPPED&&type!=ItemRenderType.EQUIPPED_FIRST_PERSON){if(jsonRenderer!=null)jsonRenderer.renderItem(type,stack,data);return;}
        EntityPlayer player = null;
        net.minecraft.entity.EntityLivingBase holder = null;
        for (Object value : data) if (value instanceof net.minecraft.entity.EntityLivingBase) holder = (net.minecraft.entity.EntityLivingBase)value;
        for (Object value : data) if (value instanceof EntityPlayer) player = (EntityPlayer)value;
        IIcon jsonIcon=com.mod.htgt6.client.model.json.JsonBlockModelLoader.install().itemIcon(stack,holder);
        IIcon icon = jsonIcon!=null?jsonIcon:((ItemCrossbow) stack.getItem()).getLivingIcon(stack, holder);
        if (icon == null) return;
        try (RenderStateGuard112 ignored = RenderStateGuard112.capture()) {
            GL11.glPushMatrix();
            try {
                Minecraft.getMinecraft().getTextureManager().bindTexture(jsonIcon!=null?TextureMap.locationBlocksTexture:TextureMap.locationItemsTexture);
                GL11.glColor4f(1,1,1,1);
                GL11.glTranslatef(.5F,.5F,.5F);
                if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
                    GL11.glScalef(2.5F,2.5F,2.5F);
                    GL11.glRotatef(-45,0,1,0);
                    int useTicks = player != null && player.getItemInUse() == stack ? player.getItemInUseDuration()
                            : com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandCrossbowUse120.renderUseTicks(player, stack);
                    if (useTicks >= 0 && !ItemCrossbow.isCharged(stack)) {
                        float age=chargeAge(useTicks,framePartial);
                        float charge=chargeProgress(age,ItemCrossbow.chargeDuration(stack));
                        GL11.glTranslatef(-.4785682F,-.094387F,.05731531F);
                        GL11.glRotatef(-11.935F,1,0,0);
                        GL11.glRotatef(65.3F,0,1,0);
                        GL11.glRotatef(-9.785F,0,0,1);
                        GL11.glTranslatef(0,chargeWobble(age,charge),.04F * charge);
                        GL11.glScalef(1,1,1+.2F * charge);
                        GL11.glRotatef(-45,0,1,0);
                    } else if (player != null && ItemCrossbow.isCharged(stack)
                            && player.swingProgress < .001F) {
                        // ItemInHandRenderer's loaded main-hand resting pose (1.20.2).
                        GL11.glTranslatef(-.641864F,0,0);
                        GL11.glRotatef(10,0,1,0);
                    }
                    com.mod.htgt6.client.model.json.JsonBlockModelLoader.install().applyItemDisplay(stack.getItem(),"firstperson_righthand",false);
                } else {
                    // Invert legacy RenderBiped/RenderPlayer full-3D transforms to the hand pivot.
                    GL11.glRotatef(-45,0,1,0);
                    GL11.glRotatef(100,1,0,0);
                    GL11.glScalef(1.6F,-1.6F,1.6F);
                    GL11.glTranslatef(0,-.1875F,0);
                    GL11.glTranslatef(.0625F,-.4375F,-.0625F);
                    GL11.glRotatef(-90,1,0,0);
                    GL11.glRotatef(180,0,1,0);
                    GL11.glTranslatef(.0625F,.125F,-.625F);
                    com.mod.htgt6.client.model.json.JsonBlockModelLoader.install().applyItemDisplay(stack.getItem(),"thirdperson_righthand",false);
                }
                renderHeldSprite(icon);
                if (stack.hasEffect()) renderGlint();
            } finally { GL11.glPopMatrix(); }
        }
    }
    /** Convert the legacy quad to JSON generated geometry, around the model centre.
     * Legacy +Z uses maxU at x=0; modern SOUTH uses minU. Rotating the whole
     * thin solid also exchanges front/back normals, unlike swapping UVs alone.
     * Keep this after the hand display transform and before sprite centring.
     */
    private static void renderHeldSprite(IIcon icon) {
        GL11.glRotatef(180,0,1,0);
        GL11.glTranslatef(-.5F,-.5F,.03125F);
        ItemRenderer.renderItemIn2D(Tessellator.instance,icon.getMaxU(),icon.getMinV(),
                icon.getMinU(),icon.getMaxV(),icon.getIconWidth(),icon.getIconHeight(),.0625F);
    }

    public static void renderGlint() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(new net.minecraft.util.ResourceLocation("textures/misc/enchanted_item_glint.png"));
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        net.minecraft.client.renderer.OpenGlHelper.glBlendFunc(GL11.GL_SRC_COLOR,GL11.GL_ONE,1,0);
        GL11.glColor4f(.38F,.19F,.608F,1);
        for (int pass=0;pass<2;pass++) {
            GL11.glMatrixMode(GL11.GL_TEXTURE);GL11.glPushMatrix();
            try {
                GL11.glLoadIdentity();GL11.glScalef(.125F,.125F,.125F);
                long period=pass==0?3000L:4873L;
                float offset=(Minecraft.getSystemTime()%period)/(float)period*8;
                GL11.glTranslatef(pass==0?offset:-offset,0,0);
                GL11.glRotatef(pass==0?-50:10,0,0,1);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                ItemRenderer.renderItemIn2D(Tessellator.instance,1,0,0,1,256,256,.0625F);
            } finally { GL11.glMatrixMode(GL11.GL_TEXTURE);GL11.glPopMatrix();GL11.glMatrixMode(GL11.GL_MODELVIEW); }
        }
    }

    private static void display(float x,float y,float z,float rz,float scale) {
        GL11.glTranslatef(x/16,y/16,z/16);
        GL11.glRotatef(-90,1,0,0);
        GL11.glRotatef(rz,0,0,1);
        GL11.glScalef(scale,scale,scale);
    }
}
