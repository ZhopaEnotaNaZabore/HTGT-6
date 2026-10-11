package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Sign;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Wood-family sign renderer; JSON supplies particles while this TESR supplies entity geometry and text. */
public final class Backport120SignRenderer extends TileEntitySpecialRenderer {
    static final float HANGING_TRANSLATE_Y=.9375F;
    static final float HANGING_MODEL_OFFSET_Y=-.3125F;
    static final float MODEL_PIXEL_SCALE=.0625F;
    static final float CHAIN_ANGLE=.7853982F;
    static final int HANGING_TEXTURE_WIDTH=64,HANGING_TEXTURE_HEIGHT=32;
    static final int[] BOARD_BOX={-7,0,-1,14,10,2};
    static final int[] PLANK_BOX={-8,-6,-2,16,2,4};
    static final int[] V_CHAINS_BOX={-6,-6,0,12,6,0};
    private static final ResourceLocation SIGN = new ResourceLocation("htgt6", "textures/entity/1_20_backport/signs/cherry.png");
    private static final ResourceLocation OAK=new ResourceLocation("textures/entity/sign.png");
    private static final ResourceLocation HANGING = new ResourceLocation("htgt6", "textures/entity/1_20_backport/signs/hanging/cherry.png");
    private static final ResourceLocation MANGROVE_SIGN = new ResourceLocation("htgt6", "textures/entity/1_20_backport/signs/mangrove.png");
    private static final ResourceLocation MANGROVE_HANGING = new ResourceLocation("htgt6", "textures/entity/1_20_backport/signs/hanging/mangrove.png");
    private static final ResourceLocation BAMBOO_SIGN = new ResourceLocation("htgt6", "textures/entity/1_20_backport/signs/bamboo.png");
    private static final ResourceLocation BAMBOO_HANGING = new ResourceLocation("htgt6", "textures/entity/1_20_backport/signs/hanging/bamboo.png");
    private final ModelSign signModel = new ModelSign();
    private final HangingModel hangingModel = new HangingModel();

    @Override public void renderTileEntityAt(TileEntity raw,double x,double y,double z,float partialTicks) {
        TileEntityBackport120Sign sign=(TileEntityBackport120Sign)raw;
        Block block=sign.getBlockType();
        com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherWoodContent116.Family nether=com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherWoodContent116.signFamily(block);
        if(nether!=null){
            if(block==nether.wood.hangingSignBlock||block==nether.wood.wallHangingSignBlock)renderHanging(sign,x,y,z,block==nether.wood.wallHangingSignBlock,false,false);
            else renderNormal(sign,x,y,z,block==nether.wood.standingSign,false,false);
            return;
        }
        boolean mangrove=block==Backport120Content.mangroveStandingSign||block==Backport120Content.mangroveWallSign
                ||block==Backport120Content.mangroveHangingSignBlock||block==Backport120Content.mangroveWallHangingSignBlock;
        boolean bamboo=block==EtFVanillaGapContent120.bambooStandingSign||block==EtFVanillaGapContent120.bambooWallSign
                ||block==EtFVanillaGapContent120.bambooHangingSignBlock||block==EtFVanillaGapContent120.bambooWallHangingSignBlock;
        if(block==Backport120Content.cherryHangingSignBlock||block==Backport120Content.cherryWallHangingSignBlock
                ||block==Backport120Content.mangroveHangingSignBlock||block==Backport120Content.mangroveWallHangingSignBlock
                ||block==EtFVanillaGapContent120.bambooHangingSignBlock||block==EtFVanillaGapContent120.bambooWallHangingSignBlock)
            renderHanging(sign,x,y,z,block==Backport120Content.cherryWallHangingSignBlock
                    ||block==Backport120Content.mangroveWallHangingSignBlock
                    ||block==EtFVanillaGapContent120.bambooWallHangingSignBlock,mangrove,bamboo);
        else renderNormal(sign,x,y,z,block==Backport120Content.cherryStandingSign
                ||block==Backport120Content.mangroveStandingSign||block==EtFVanillaGapContent120.bambooStandingSign
                ||com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageSigns114.standing(block)
                ||block==com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.SignDye114.OAK_STANDING,mangrove,bamboo);
    }

    private void renderNormal(TileEntityBackport120Sign tile,double x,double y,double z,boolean standing,boolean mangrove,boolean bamboo) {
        GL11.glPushMatrix(); float scale=0.6666667F;
        if(standing){
            GL11.glTranslatef((float)x+.5F,(float)y+.75F*scale,(float)z+.5F);
            GL11.glRotatef(-tile.getBlockMetadata()*360F/16F,0,1,0); signModel.signStick.showModel=true;
        }else{
            int metadata=tile.getBlockMetadata(); float rotation=wallRotation(metadata);
            GL11.glTranslatef((float)x+.5F,(float)y+.75F*scale,(float)z+.5F);
            GL11.glRotatef(-rotation,0,1,0); GL11.glTranslatef(0,-.3125F,-.4375F); signModel.signStick.showModel=false;
        }
        int wood=com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageSigns114.index(tile.getBlockType());
        com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherWoodContent116.Family nether=com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherWoodContent116.signFamily(tile.getBlockType());
        bindTexture(nether!=null?new ResourceLocation("htgt6","textures/entity/1_20_backport/signs/"+nether.name+".png"):wood>=0?new ResourceLocation("htgt6","textures/entity/1_20_backport/signs/"+
                com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageSigns114.WOODS[wood]+".png"):
                tile.getBlockType()==com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.SignDye114.OAK_STANDING||tile.getBlockType()==com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.SignDye114.OAK_WALL?OAK:
                bamboo?BAMBOO_SIGN:mangrove?MANGROVE_SIGN:SIGN); GL11.glPushMatrix(); GL11.glScalef(scale,-scale,-scale); signModel.renderSign(); GL11.glPopMatrix();
        renderText(tile,0,.5F*scale,.07F*scale,.016666668F*scale);
        GL11.glPopMatrix();
    }

    private void renderHanging(TileEntityBackport120Sign tile,double x,double y,double z,boolean wall,boolean mangrove,boolean bamboo) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float)x+.5F,(float)y+HANGING_TRANSLATE_Y,(float)z+.5F);
        float rotation=wall?wallRotation(tile.getBlockMetadata()):ceilingRotation(tile.getBlockMetadata());
        GL11.glRotatef(-rotation,0,1,0);
        GL11.glTranslatef(0,HANGING_MODEL_OFFSET_Y,0);
        com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherWoodContent116.Family nether=com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherWoodContent116.signFamily(tile.getBlockType());
        bindTexture(nether!=null?new ResourceLocation("htgt6","textures/entity/1_20_backport/signs/hanging/"+nether.name+".png"):bamboo?BAMBOO_HANGING:mangrove?MANGROVE_HANGING:HANGING); GL11.glPushMatrix(); GL11.glScalef(1F,-1F,-1F);
        hangingModel.render(wall,tile.isHangingSignAttached()); GL11.glPopMatrix();
        renderText(tile,0,-.32F,.073F,.0140625F);
        GL11.glPopMatrix();
    }

    private void renderText(TileEntityBackport120Sign tile,float x,float y,float z,float scale){
        FontRenderer font=func_147498_b(); GL11.glTranslatef(x,y,z); GL11.glScalef(scale,-scale,scale);
        GL11.glNormal3f(0,0,-1); GL11.glDepthMask(false);
        for(int i=0;i<tile.signText.length;i++){
            String text=tile.signText[i]; if(i==tile.lineBeingEdited)text="> "+text+" <";
            font.drawString(text,-font.getStringWidth(text)/2,i*10-tile.signText.length*5,com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.SignDye114.textColor(tile.getColor()));
        }
        GL11.glDepthMask(true); GL11.glColor4f(1,1,1,1);
    }

    static float ceilingRotation(int metadata){return metadata*360F/16F;}
    static float wallRotation(int metadata){return metadata==2?180F:metadata==4?90F:metadata==5?-90F:0F;}
    static int hangingPartMask(boolean wall,boolean attached){
        int mask=1;
        if(wall)mask|=2;
        if(!wall&&attached)mask|=32;else mask|=4|8|16;
        return mask;
    }

    private static final class HangingModel extends ModelBase {
        private final ModelRenderer board;
        private final ModelRenderer plank;
        private final ModelRenderer[] normalChains=new ModelRenderer[4];
        private final ModelRenderer vChains;
        private HangingModel(){textureWidth=HANGING_TEXTURE_WIDTH;textureHeight=HANGING_TEXTURE_HEIGHT;
            board=new ModelRenderer(this,0,12); addBox(board,BOARD_BOX);
            plank=new ModelRenderer(this,0,0); addBox(plank,PLANK_BOX);
            normalChains[0]=chain(0,-CHAIN_ANGLE,-5);
            normalChains[1]=chain(6,CHAIN_ANGLE,-5);
            normalChains[2]=chain(0,-CHAIN_ANGLE,5);
            normalChains[3]=chain(6,CHAIN_ANGLE,5);
            vChains=new ModelRenderer(this,14,6); addBox(vChains,V_CHAINS_BOX);
        }
        private static void addBox(ModelRenderer renderer,int[] box){renderer.addBox(box[0],box[1],box[2],box[3],box[4],box[5]);}
        private ModelRenderer chain(int textureX,float angle,float pivotX){
            ModelRenderer chain=new ModelRenderer(this,textureX,6);
            chain.addBox(-1.5F,0,0,3,6,0); chain.setRotationPoint(pivotX,-6,0);
            chain.rotateAngleZ=angle; return chain;
        }
        private void render(boolean wall,boolean attached){
            int mask=hangingPartMask(wall,attached);
            board.render(MODEL_PIXEL_SCALE);
            if((mask&2)!=0)plank.render(MODEL_PIXEL_SCALE);
            if((mask&32)!=0)vChains.render(MODEL_PIXEL_SCALE);
            else for(ModelRenderer chain:normalChains)chain.render(MODEL_PIXEL_SCALE);
        }
    }
}
