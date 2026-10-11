package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityCampfire149;
import com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderStateGuard112;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.item.EntityItem;import net.minecraft.item.ItemStack;import net.minecraft.tileentity.TileEntity;import org.lwjgl.opengl.GL11;
/** Four visible cooking ingredients; block geometry is the official JSON model. */
public final class RenderCampfireItems149 extends TileEntitySpecialRenderer {
 private final RenderItem renderer=new RenderItem(){@Override public boolean shouldBob(){return false;}@Override public boolean shouldSpreadItems(){return false;}};
 private EntityItem display;
 public RenderCampfireItems149(){renderer.setRenderManager(RenderManager.instance);}
 @Override public void renderTileEntityAt(TileEntity raw,double x,double y,double z,float partial){
  if(!(raw instanceof TileEntityCampfire149)||raw.getWorldObj()==null)return;TileEntityCampfire149 tile=(TileEntityCampfire149)raw;
  if(display==null||display.worldObj!=tile.getWorldObj()){display=new EntityItem(tile.getWorldObj());display.hoverStart=0;}
  int facing=tile.getBlockMetadata()&3;
  try(RenderStateGuard112 guard=RenderStateGuard112.capture()){
   for(int i=0;i<4;i++){ItemStack stack=tile.item(i);if(stack==null)continue;
    GL11.glPushMatrix();try{GL11.glTranslated(x+.5,y+.449,z+.5);GL11.glRotatef(-90F*((i+facing)&3),0,1,0);GL11.glTranslatef(-.3125F,0,-.3125F);GL11.glRotatef(90,1,0,0);GL11.glScalef(.75F,.75F,.75F);
     display.setEntityItemStack(stack);display.age=0;renderer.doRender(display,0,0,0,0,0);
    }finally{GL11.glPopMatrix();}
   }
  }
 }
}
