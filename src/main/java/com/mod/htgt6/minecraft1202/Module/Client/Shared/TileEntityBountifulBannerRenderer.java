package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockBountifulBanner;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Bountiful.BountifulBannerColors;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.TileEntityBountifulBanner;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

/** Layered banner renderer using the official 1.8 alpha masks. */
public final class TileEntityBountifulBannerRenderer extends TileEntitySpecialRenderer implements IItemRenderer {
    private static final Map<String,String> PATTERNS=new HashMap<String,String>();
    private static final ResourceLocation BASE=new ResourceLocation("htgt6","textures/entity/bountiful/banner/base.png");
    static{String[] values={"bl","square_bottom_left","br","square_bottom_right","tl","square_top_left","tr","square_top_right","bs","stripe_bottom","ts","stripe_top","ls","stripe_left","rs","stripe_right","cs","stripe_center","ms","stripe_middle","drs","stripe_downright","dls","stripe_downleft","ss","small_stripes","cr","cross","sc","straight_cross","bt","triangle_bottom","tt","triangle_top","bts","triangles_bottom","tts","triangles_top","ld","diagonal_left","rd","diagonal_right","lud","diagonal_up_left","rud","diagonal_up_right","hh","half_horizontal","hhb","half_horizontal_bottom","vh","half_vertical","vhr","half_vertical_right","bo","border","cbo","curly_border","mc","circle","mr","rhombus","bri","bricks","gra","gradient","gru","gradient_up","cre","creeper","sku","skull","flo","flower","moj","mojang","glb","globe","pig","piglin"};for(int i=0;i<values.length;i+=2)PATTERNS.put(values[i],values[i+1]);PATTERNS.put("b","base");}
    private static final ResourceLocation FRAME=new ResourceLocation("htgt6","textures/entity/bountiful/banner/banner_base.png");
    private final BannerModel model=new BannerModel();
    public static String textureName(String code){return PATTERNS.get(code);}
    @Override public void renderTileEntityAt(TileEntity raw,double x,double y,double z,float partial){
        TileEntityBountifulBanner tile=(TileEntityBountifulBanner)raw;
        try(com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112 ignored=com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112.capture()) {
            GL11.glPushMatrix();
            try {
                GL11.glDisable(GL11.GL_CULL_FACE);
                boolean wall=tile.getBlockType() instanceof BlockBountifulBanner&&((BlockBountifulBanner)tile.getBlockType()).isWall();
                GL11.glTranslated(x+.5D,y+(wall?-1D/6D:.5D),z+.5D);
                int side=tile.getBlockMetadata();
                float rotation=wall?wallRotation(side):-(side*360F/16F);
                GL11.glRotatef(rotation,0,1,0);
                if(wall)GL11.glTranslatef(0,-.3125F,-.4375F);
                GL11.glScalef(2F/3F,-2F/3F,-2F/3F);
                long time=tile.hasWorldObj()?tile.getWorldObj().getTotalWorldTime():0L;
                model.setWave(waveAngle(time,partial,tile.xCoord,tile.yCoord,tile.zCoord));
                renderLayers(tile.getBaseColor(),tile.getPatterns(),wall);
            } finally { GL11.glPopMatrix(); }
        }
    }
    public static float wallRotation(int side) { return side==2?180:side==4?-90:side==5?90:0; }
    public static float waveAngle(long time,float partial,int x,int y,int z) {
        float phase=(Math.floorMod(time+(long)x*7+(long)y*9+(long)z*13,100L)+partial)/100F;
        return (-.0125F+.01F*(float)Math.cos(phase*Math.PI*2))*(float)Math.PI;
    }
    public static NBTTagList visiblePatterns(NBTTagList source) {
        NBTTagList result=new NBTTagList();
        for(int i=0;i<source.tagCount()&&result.tagCount()<16;i++) {
            NBTTagCompound pattern=source.getCompoundTagAt(i);
            if(PATTERNS.containsKey(pattern.getString("Pattern")))result.appendTag(pattern.copy());
        }
        return result;
    }
    private void renderLayers(int baseColor,NBTTagList list,boolean wall){
        GL11.glColor4f(1,1,1,1);bindTexture(FRAME);
        model.renderFrame(.0625F,wall);model.renderCloth(.0625F);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glPolygonOffset(-1.0F,-1.0F);
        bindTexture(BASE);color(BountifulBannerColors.RGB[baseColor&15]);model.renderCloth(.0625F);
        list=visiblePatterns(list);
        for(int i=0;i<list.tagCount();i++){
            NBTTagCompound pattern=list.getCompoundTagAt(i);
            String name=PATTERNS.get(pattern.getString("Pattern"));
            if(name!=null){
                bindTexture(new ResourceLocation("htgt6","textures/entity/bountiful/banner/"+name+".png"));
                color(BountifulBannerColors.RGB[pattern.getInteger("Color")&15]);
                model.renderCloth(0.0625F);
            }
        }
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1,1,1,1);
    }
    @Override public boolean handleRenderType(ItemStack item,ItemRenderType type){
        // Pattern masks live in BlockEntityTag and cannot be represented by the
        // fixed two-pass item icon. Keep the layered renderer active in every
        // context so loom output changes become visible without reloading assets.
        return true;
    }
    @Override public boolean shouldUseRenderHelper(ItemRenderType type,ItemStack item,ItemRendererHelper helper){
        return type==ItemRenderType.INVENTORY&&helper==ItemRendererHelper.INVENTORY_BLOCK
                ||type==ItemRenderType.ENTITY&&(helper==ItemRendererHelper.ENTITY_BOBBING
                ||helper==ItemRendererHelper.ENTITY_ROTATION);
    }
    @Override public void renderItem(ItemRenderType type,ItemStack stack,Object... data){
        try(com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112 ignored=com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112.capture()) {
        GL11.glPushMatrix();
        try{
            // Inventory and mirrored offhand transforms contain a negative axis.
            // Inherited face culling otherwise removes the one-pixel cloth entirely.
            GL11.glDisable(GL11.GL_CULL_FACE);
            if(type==ItemRenderType.INVENTORY){GL11.glTranslatef(.5F,.25F,0);GL11.glScalef(.36F,-.36F,-.36F);}
            else if(type==ItemRenderType.ENTITY){GL11.glTranslatef(0,.21F,0);GL11.glScalef(.28F,-.28F,-.28F);}
            else if(type==ItemRenderType.EQUIPPED_FIRST_PERSON){GL11.glTranslatef(.45F,.195F,.15F);GL11.glRotatef(25,0,1,0);GL11.glScalef(.34F,-.34F,-.34F);}
            else{GL11.glTranslatef(.5F,.21F,.1F);GL11.glRotatef(20,0,1,0);GL11.glScalef(.32F,-.32F,-.32F);}
            model.setWave(0.0F);renderLayers(itemBaseColor(stack),itemPatterns(stack),false);
        }finally{GL11.glPopMatrix();}
        }
    }
    /** CustomHeadLayer + template_banner HEAD transform, before the ordinary banner model. */
    public void renderOnHead(ItemStack stack){
        try(com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112 ignored=com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112.capture()){
            GL11.glPushMatrix();
            try{
                GL11.glDisable(GL11.GL_CULL_FACE);
                GL11.glTranslatef(0,-.25F,0);GL11.glRotatef(180,0,1,0);GL11.glScalef(.625F,-.625F,-.625F);
                GL11.glTranslatef(0,1F,7F/16F);GL11.glRotatef(180,0,1,0);GL11.glScalef(1.5F,1.5F,1.5F);
                // ItemRenderer (-.5,-.5,-.5) and BannerRenderer (+.5,+.5,+.5) cancel.
                GL11.glScalef(2F/3F,-2F/3F,-2F/3F);
                model.setWave(0);renderLayers(itemBaseColor(stack),itemPatterns(stack),false);
            }finally{GL11.glPopMatrix();}
        }
    }
    public static int itemBaseColor(ItemStack stack) {
        if(stack.hasTagCompound()&&stack.getTagCompound().hasKey("BlockEntityTag",10)) {
            NBTTagCompound data=stack.getTagCompound().getCompoundTag("BlockEntityTag");
            if(data.hasKey("Base"))return data.getInteger("Base")&15;
        }
        return stack.getItemDamage()&15;
    }
    private static NBTTagList itemPatterns(ItemStack stack){
        if(stack==null||!stack.hasTagCompound()||!stack.getTagCompound().hasKey("BlockEntityTag",10))return new NBTTagList();
        NBTTagCompound block=stack.getTagCompound().getCompoundTag("BlockEntityTag");
        return block.hasKey("Patterns",9)?block.getTagList("Patterns",10):new NBTTagList();
    }
    private static void color(int color){GL11.glColor4f(((color>>16)&255)/255F,((color>>8)&255)/255F,(color&255)/255F,1);}
    private static final class BannerModel extends ModelBase{
        private final ModelRenderer cloth,stand,bar;
        BannerModel(){textureWidth=64;textureHeight=64;cloth=new ModelRenderer(this,0,0);cloth.addBox(-10,0,-2,20,40,1);cloth.setRotationPoint(0,-32,0);stand=new ModelRenderer(this,44,0);stand.addBox(-1,-30,-1,2,42,2);bar=new ModelRenderer(this,0,42);bar.addBox(-10,-32,-1,20,2,2);}
        void renderCloth(float scale){cloth.render(scale);}
        void setWave(float angle){cloth.rotateAngleX=angle;}
        void renderFrame(float scale,boolean wall){if(!wall)stand.render(scale);bar.render(scale);}
    }
}
