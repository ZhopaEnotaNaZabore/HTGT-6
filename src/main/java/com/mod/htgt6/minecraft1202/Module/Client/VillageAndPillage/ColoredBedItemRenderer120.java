package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;
import net.minecraftforge.client.IItemRenderer;import net.minecraft.item.ItemStack;import net.minecraft.block.Block;import org.lwjgl.opengl.GL11;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockColoredBed120;
/** Shares the placed bed geometry for inventory, hand and dropped-item rendering. */
public final class ColoredBedItemRenderer120 implements IItemRenderer {
    private final RenderColoredBed120 model=new RenderColoredBed120();
    public boolean handleRenderType(ItemStack stack,ItemRenderType type){return true;}
    public boolean shouldUseRenderHelper(ItemRenderType type,ItemStack stack,ItemRendererHelper helper){return true;}
    public void renderItem(ItemRenderType type,ItemStack stack,Object... data){
        Block block=Block.getBlockFromItem(stack.getItem());if(!(block instanceof BlockColoredBed120))return;
        GL11.glPushMatrix();GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_CURRENT_BIT);
        try{
            GL11.glEnable(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL);GL11.glColor4f(1,1,1,1);
            if(type==ItemRenderType.INVENTORY){GL11.glScalef(.65F,.65F,.65F);GL11.glTranslatef(-.5F,-.25F,0);}
            else if(type==ItemRenderType.ENTITY){GL11.glScalef(.5F,.5F,.5F);GL11.glTranslatef(-.5F,0,0);}
            else{GL11.glScalef(.6F,.6F,.6F);GL11.glTranslatef(0,.1F,.5F);}
            int color=((BlockColoredBed120)block).getColor();model.renderHalf(color,true,0);GL11.glTranslatef(0,0,-1);model.renderHalf(color,false,0);
        }finally{GL11.glPopAttrib();GL11.glPopMatrix();}
    }
}
