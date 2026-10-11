package com.mod.htgt6.minecraft1202.Module.Items.Modern;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Slab;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBlackstoneSlab116;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.*;

/** Merges wet or dry halves of exactly the same family; water is displaced by the full block. */
public final class ItemBlackstoneSlab116 extends ItemBlock {
    private final BlockBackport120Slab half,full;
    public ItemBlackstoneSlab116(Block block,BlockBackport120Slab half,BlockBackport120Slab full,Boolean ignored){super(block);this.half=half;this.full=full;}
    // GameRegistry resolves constructor parameters by their concrete runtime classes.
    public ItemBlackstoneSlab116(Block block,BlockBlackstoneSlab116 half,BlockBlackstoneSlab116 full,Boolean ignored){this(block,(BlockBackport120Slab)half,(BlockBackport120Slab)full,ignored);}
    @Override public int getMetadata(int damage){return 0;}
    private boolean direct(World w,int x,int y,int z,int side,float hitY) {
        if(w.getBlock(x,y,z)!=half)return false;
        boolean top=(w.getBlockMetadata(x,y,z)&8)!=0;
        return top?(side==0||(side!=1&&hitY<=.5F)):(side==1||(side!=0&&hitY>.5F));
    }
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer p,World w,int x,int y,int z,int side,float hx,float hy,float hz) {
        if(stack.stackSize<=0)return false;
        if(direct(w,x,y,z,side,hy))return merge(stack,p,w,x,y,z,side);
        ForgeDirection d=ForgeDirection.getOrientation(side);
        if(w.getBlock(x+d.offsetX,y+d.offsetY,z+d.offsetZ)==half)return merge(stack,p,w,x+d.offsetX,y+d.offsetY,z+d.offsetZ,side);
        return super.onItemUse(stack,p,w,x,y,z,side,hx,hy,hz);
    }
    private boolean merge(ItemStack stack,EntityPlayer p,World w,int x,int y,int z,int side) {
        if(!w.canMineBlock(p,x,y,z)||!p.canPlayerEdit(x,y,z,side,stack))return false;
        if(!w.checkNoEntityCollision(full.getCollisionBoundingBoxFromPool(w,x,y,z)))return true;
        if(w.isRemote)return true;
        if(w.setBlock(x,y,z,full,0,3)) {
            w.playSoundEffect(x+.5,y+.5,z+.5,full.stepSound.func_150496_b(),1,.8F);
            if(!p.capabilities.isCreativeMode)--stack.stackSize;
        }
        return true;
    }
    @Override @SideOnly(Side.CLIENT) public boolean func_150936_a(World w,int x,int y,int z,int side,EntityPlayer p,ItemStack stack) {
        if(direct(w,x,y,z,side,.5F))return true;
        ForgeDirection d=ForgeDirection.getOrientation(side);
        return w.getBlock(x+d.offsetX,y+d.offsetY,z+d.offsetZ)==half||super.func_150936_a(w,x,y,z,side,p,stack);
    }
}
