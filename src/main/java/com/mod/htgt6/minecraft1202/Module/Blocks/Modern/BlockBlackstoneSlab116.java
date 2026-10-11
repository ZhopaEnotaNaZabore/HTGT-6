package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.*;
import net.minecraft.block.Block;
import net.minecraft.world.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.*;

/** Bit 8 is the upper half; bit 4 stores a source of water. Double slabs are dry. */
public final class BlockBlackstoneSlab116 extends BlockBackport120Slab implements IWaterloggedBlock150 {
    private final Block source;
    public BlockBlackstoneSlab116(boolean full,String family,Block source) {
        super(full,family,family.equals("polished_blackstone_brick")?"polished_blackstone_bricks":family,source,6);this.source=source;
        setStepSound(BuildingSounds116.STONE);setLightOpacity(full?255:0);
        // Both raw and brick slabs explicitly use 2; polished inherits the same value.
        setHardness(2);
    }
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public int getRenderBlockPass(){return field_150004_a?0:1;}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public boolean canRenderInPass(int pass){return com.mod.htgt6.minecraft1202.Module.Client.Modern.ShapeWaterRenderer116.pass(pass,field_150004_a);}
    @Override public int getLightOpacity(IBlockAccess w,int x,int y,int z){return field_150004_a?255:isWaterlogged(w,x,y,z)?1:0;}
    public boolean isWaterlogged(IBlockAccess w,int x,int y,int z){return !field_150004_a&&(w.getBlockMetadata(x,y,z)&4)!=0;}
    public boolean setWaterlogged(World w,int x,int y,int z,boolean wet){return !field_150004_a&&ShapeWater116.set(w,x,y,z,this,4,wet);}
    @Override public int onBlockPlaced(World w,int x,int y,int z,int side,float hx,float hy,float hz,int meta) {
        return field_150004_a?0:super.onBlockPlaced(w,x,y,z,side,hx,hy,hz,0)|(ShapeWater116.present(w,x,y,z)?4:0);
    }
    @Override public float getExplosionResistance(net.minecraft.entity.Entity entity,World w,int x,int y,int z,double ex,double ey,double ez) {
        float solid=super.getExplosionResistance(entity,w,x,y,z,ex,ey,ez);
        return isWaterlogged(w,x,y,z)?Math.max(100F,solid):solid;
    }
    @Override public int damageDropped(int meta){return 0;}
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int side,int meta){return source.getIcon(side,0);}
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float hx,float hy,float hz){return !field_150004_a&&ShapeWater116.bucket(this,w,x,y,z,p,side);}
    @Override public boolean removedByPlayer(World w,EntityPlayer p,int x,int y,int z,boolean harvest){return ShapeWater116.remove(w,x,y,z,isWaterlogged(w,x,y,z));}
    @Override public void onBlockExploded(World w,int x,int y,int z,net.minecraft.world.Explosion explosion){boolean wet=isWaterlogged(w,x,y,z);super.onBlockExploded(w,x,y,z,explosion);if(wet&&w.isAirBlock(x,y,z))w.setBlock(x,y,z,net.minecraft.init.Blocks.water,0,3);}
}
