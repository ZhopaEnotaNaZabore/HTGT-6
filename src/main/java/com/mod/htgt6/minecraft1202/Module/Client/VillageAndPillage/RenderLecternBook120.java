package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageLectern120;
/** Native ModelBook shares the modern book mesh and texture layout. */
public final class RenderLecternBook120 extends TileEntitySpecialRenderer {
    private final ModelBook book=new ModelBook();
    public void renderTileEntityAt(TileEntity raw,double x,double y,double z,float partial){
        TileEntityVillageLectern120 tile=(TileEntityVillageLectern120)raw;
        if(tile.getStackInSlot(0)==null)return;
        GL11.glPushMatrix();try{
            GL11.glTranslated(x+.5,y+1.0625,z+.5);
            GL11.glRotatef(-90*(tile.getBlockMetadata()&3),0,1,0);
            GL11.glRotatef(67.5F,0,0,1);
            GL11.glTranslatef(0,-.125F,0);
            bindTexture(new ResourceLocation("minecraft","textures/entity/enchanting_table_book.png"));
            book.render(null,0,.1F,.9F,1.2F,0,.0625F);
        }finally{GL11.glPopMatrix();}
    }
}
