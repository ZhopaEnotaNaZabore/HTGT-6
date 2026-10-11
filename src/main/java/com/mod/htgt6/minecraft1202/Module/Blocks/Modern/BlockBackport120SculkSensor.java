package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.block.Block;
import com.mod.htgt6.minecraft1202.Module.Events.Modern.Backport120SculkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

/** Legacy event bridge for the modern inactive/active/cooldown sensor phases. */
public class BlockBackport120SculkSensor extends BlockBackport120Sculk {
    private final boolean calibrated;
    public BlockBackport120SculkSensor(){this("sculk_sensor",false);}
    protected BlockBackport120SculkSensor(String name,boolean calibrated) {
        super(name, 1.5F, 5);this.calibrated=calibrated;
        setBlockBounds(0, 0, 0, 1, 0.5F, 1);
    }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public boolean hasTileEntity(int metadata){return true;}
    @Override public TileEntity createTileEntity(World world,int metadata){return new TileEntityBackport120SculkSensor();}
    public boolean isCalibrated(){return calibrated;}
    @Override public boolean canProvidePower() { return true; }
    @Override public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        TileEntityBackport120SculkSensor tile=tile(world,x,y,z);return tile==null?0:tile.getPower();
    }
    @Override public int isProvidingStrongPower(IBlockAccess world,int x,int y,int z,int side){return side==1?isProvidingWeakPower(world,x,y,z,side):0;}
    @Override public boolean hasComparatorInputOverride(){return true;}
    @Override public int getComparatorInputOverride(World world,int x,int y,int z,int side){TileEntityBackport120SculkSensor tile=tile(world,x,y,z);return tile==null?0:tile.getLastFrequency();}
    @Override public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if(entity instanceof EntityLivingBase&&!entity.isSneaking())Backport120SculkHandler.emit(world,entity.posX,entity.posY,entity.posZ,1,entity,false);
    }
    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase placer,ItemStack stack){
        if(!calibrated)return;int horizontal=MathHelper.floor_double(placer.rotationYaw*4F/360F+.5D)&3;
        int side=new int[]{2,5,3,4}[horizontal];TileEntityBackport120SculkSensor tile=tile(world,x,y,z);if(tile!=null)tile.setFacing(side);
    }
    public boolean receiveVibration(World world,int x,int y,int z,int frequency,int distance){TileEntityBackport120SculkSensor tile=tile(world,x,y,z);return tile!=null&&tile.receive(frequency,distance);}
    @Override public void updateTick(World world, int x, int y, int z, Random random) {
        TileEntityBackport120SculkSensor tile=tile(world,x,y,z);if(tile!=null)tile.scheduledTick();
    }
    public static TileEntityBackport120SculkSensor tile(IBlockAccess world,int x,int y,int z){TileEntity t=world.getTileEntity(x,y,z);return t instanceof TileEntityBackport120SculkSensor?(TileEntityBackport120SculkSensor)t:null;}
}
