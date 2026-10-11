package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120DecoratedPot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

/** 1.7 renderer for the separate base and four side model layers used by 1.20.2. */
public final class DecoratedPotRenderer120 extends TileEntitySpecialRenderer implements IItemRenderer {
    private static final String ROOT="textures/entity/1_20_backport/decorated_pot/";
    private static final ResourceLocation BASE=new ResourceLocation("htgt6",ROOT+"decorated_pot_base.png");
    private static final ResourceLocation BLANK=new ResourceLocation("htgt6",ROOT+"decorated_pot_side.png");
    @Override public void renderTileEntityAt(TileEntity raw,double x,double y,double z,float partial){
        TileEntityBackport120DecoratedPot pot=(TileEntityBackport120DecoratedPot)raw;GL11.glPushMatrix();
        GL11.glTranslated(x,y,z);GL11.glTranslatef(.5F,0,.5F);GL11.glRotatef(-(pot.getBlockMetadata()&3)*90F,0,1,0);
        GL11.glTranslatef(-.5F,0,-.5F);render(pot.getDecorations(),pot.getBlockType().getMixedBrightnessForBlock(
                pot.getWorldObj(),pot.xCoord,pot.yCoord,pot.zCoord));GL11.glPopMatrix();
    }
    @Override public boolean handleRenderType(ItemStack item,ItemRenderType type){return true;}
    @Override public boolean shouldUseRenderHelper(ItemRenderType type,ItemStack item,ItemRendererHelper helper){return type==ItemRenderType.INVENTORY||type==ItemRenderType.ENTITY;}
    @Override public void renderItem(ItemRenderType type,ItemStack item,Object... data){GL11.glPushMatrix();
        if(type==ItemRenderType.INVENTORY){GL11.glRotatef(25,1,0,0);GL11.glRotatef(45,0,1,0);GL11.glScalef(.8F,.8F,.8F);}
        else if(type==ItemRenderType.EQUIPPED_FIRST_PERSON){GL11.glTranslatef(.25F,.1F,.15F);GL11.glScalef(.7F,.7F,.7F);}
        else if(type==ItemRenderType.EQUIPPED){GL11.glTranslatef(.2F,.15F,.1F);GL11.glScalef(.65F,.65F,.65F);}
        else GL11.glTranslatef(-.5F,0,-.5F);render(read(item),15728880);GL11.glPopMatrix();}
    private static String[] read(ItemStack stack){String[] result={"brick","brick","brick","brick"};if(stack.hasTagCompound()&&stack.getTagCompound().hasKey("BlockEntityTag",10)){
        NBTTagCompound block=stack.getTagCompound().getCompoundTag("BlockEntityTag");NBTTagList list=block.getTagList("Sherds",8);
        for(int i=0;i<4&&i<list.tagCount();i++)result[i]=list.getStringTagAt(i);}return result;}
    private static ResourceLocation texture(String sherd){if(sherd==null||"brick".equals(sherd))return BLANK;
        String pattern=sherd.endsWith("_sherd")?sherd.substring(0,sherd.length()-5)+"pattern":sherd;
        return new ResourceLocation("htgt6",ROOT+pattern+".png");}

    private static void render(String[] sides,int brightness){GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT);
        GL11.glColor4f(1,1,1,1);GL11.glEnable(GL11.GL_NORMALIZE);GL11.glDisable(GL11.GL_CULL_FACE);
        drawZ(texture(sides[0]),1F/16,1F/16,15F/16,-1,brightness);drawZ(texture(sides[3]),15F/16,15F/16,1F/16,1,brightness);
        drawX(texture(sides[1]),1F/16,15F/16,1F/16,-1,brightness);drawX(texture(sides[2]),15F/16,1F/16,15F/16,1,brightness);
        Minecraft.getMinecraft().getTextureManager().bindTexture(BASE);drawBase(brightness);GL11.glPopAttrib();}
    private static void drawZ(ResourceLocation texture,float z,float x0,float x1,float normal,int light){Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setBrightness(light);t.setNormal(0,0,normal);
        quad(t,x1,1,z,x0,1,z,x0,0,z,x1,0,z,0,0,1,1);t.draw();}
    private static void drawX(ResourceLocation texture,float x,float z0,float z1,float normal,int light){Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setBrightness(light);t.setNormal(normal,0,0);
        quad(t,x,1,z0,x,1,z1,x,0,z1,x,0,z0,0,0,1,1);t.draw();}
    private static void drawBase(int light){drawBox(4F/16,13F/16,4F/16,12F/16,15.75F/16,12F/16,0,0,8,3,light);
        drawBox(5F/16,15F/16,5F/16,11F/16,1,11F/16,0,5,6,1,light);Tessellator t=Tessellator.instance;
        t.startDrawingQuads();t.setBrightness(light);float u0=0,u1=14F/32,v0=13F/32,v1=27F/32,a=1F/16,b=15F/16;
        t.setNormal(0,1,0);quad(t,a,1,a,b,1,a,b,1,b,a,1,b,u0,v0,u1,v1);
        t.setNormal(0,-1,0);quad(t,a,0,b,b,0,b,b,0,a,a,0,a,u0,v0,u1,v1);t.draw();}
    private static void drawBox(float x0,float y0,float z0,float x1,float y1,float z1,float pu,float pv,float pw,float ph,int light){
        Tessellator t=Tessellator.instance;t.startDrawingQuads();t.setBrightness(light);float u0=pu/32F,v0=pv/32F,u1=(pu+pw)/32F,v1=(pv+ph)/32F;
        t.setNormal(0,0,-1);quad(t,x1,y1,z0,x0,y1,z0,x0,y0,z0,x1,y0,z0,u0,v0,u1,v1);
        t.setNormal(0,0,1);quad(t,x0,y1,z1,x1,y1,z1,x1,y0,z1,x0,y0,z1,u0,v0,u1,v1);
        t.setNormal(-1,0,0);quad(t,x0,y1,z0,x0,y1,z1,x0,y0,z1,x0,y0,z0,u0,v0,u1,v1);
        t.setNormal(1,0,0);quad(t,x1,y1,z1,x1,y1,z0,x1,y0,z0,x1,y0,z1,u0,v0,u1,v1);
        t.setNormal(0,1,0);quad(t,x0,y1,z1,x1,y1,z1,x1,y1,z0,x0,y1,z0,u0,v0,u1,v1);t.draw();}
    private static void quad(Tessellator t,float x0,float y0,float z0,float x1,float y1,float z1,float x2,float y2,float z2,float x3,float y3,float z3,float u0,float v0,float u1,float v1){
        t.addVertexWithUV(x0,y0,z0,u0,v0);t.addVertexWithUV(x1,y1,z1,u1,v0);t.addVertexWithUV(x2,y2,z2,u1,v1);t.addVertexWithUV(x3,y3,z3,u0,v1);}
}
