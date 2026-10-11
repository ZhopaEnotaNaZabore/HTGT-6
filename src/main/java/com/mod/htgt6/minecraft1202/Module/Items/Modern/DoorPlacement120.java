package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/** Modern placement choice translated into the existing lower/upper legacy metadata. */
public final class DoorPlacement120 {
    private static final int[] DX={1,0,-1,0}, DZ={0,1,0,-1};
    private DoorPlacement120() { }

    public static boolean use(BlockDoor door,ItemStack stack,EntityPlayer player,World world,
                              int x,int y,int z,int side,float hitX,float hitY,float hitZ) {
        if(side<0||side>5||stack.stackSize<=0)return false;
        double clickX=x+hitX,clickZ=z+hitZ;
        if(!world.getBlock(x,y,z).isReplaceable(world,x,y,z)) {
            ForgeDirection face=ForgeDirection.getOrientation(side);
            x+=face.offsetX;y+=face.offsetY;z+=face.offsetZ;
        }
        if(y<1||y+1>=world.getHeight()||!player.canPlayerEdit(x,y,z,side,stack)
                ||!player.canPlayerEdit(x,y+1,z,side,stack)||!door.canPlaceBlockAt(world,x,y,z))return false;
        if(world.isRemote)return true;
        int facing=MathHelper.floor_double((player.rotationYaw+180F)*4F/360F-.5D)&3;
        boolean hinge=hinge(world,door,x,y,z,facing,clickX-x,clickZ-z);
        boolean powered=world.isBlockIndirectlyGettingPowered(x,y,z)||world.isBlockIndirectlyGettingPowered(x,y+1,z);
        Block previous=world.getBlock(x,y,z);int previousMeta=world.getBlockMetadata(x,y,z);
        if(!world.setBlock(x,y,z,door,facing|(powered?4:0),2))return false;
        if(!world.setBlock(x,y+1,z,door,com.mod.htgt6.minecraft1202.Module.Blocks.Modern.DoorPower120.upperMetadata(hinge,powered),2)) {
            world.setBlock(x,y,z,previous,previousMeta,3);return false;
        }
        world.notifyBlocksOfNeighborChange(x,y,z,door);
        world.notifyBlocksOfNeighborChange(x,y+1,z,door);
        if(!player.capabilities.isCreativeMode)--stack.stackSize;
        return true;
    }

    public static boolean hinge(World world,BlockDoor door,int x,int y,int z,int facing,double hitX,double hitZ) {
        int left=(facing+3)&3,right=(facing+1)&3;
        int lx=x+DX[left],lz=z+DZ[left],rx=x+DX[right],rz=z+DZ[right];
        int balance=(world.getBlock(rx,y,rz).isNormalCube()?1:0)+(world.getBlock(rx,y+1,rz).isNormalCube()?1:0)
                -(world.getBlock(lx,y,lz).isNormalCube()?1:0)-(world.getBlock(lx,y+1,lz).isNormalCube()?1:0);
        boolean leftDoor=world.getBlock(lx,y,lz)==door&&(world.getBlockMetadata(lx,y,lz)&8)==0;
        boolean rightDoor=world.getBlock(rx,y,rz)==door&&(world.getBlockMetadata(rx,y,rz)&8)==0;
        return rightHinge(facing,balance,leftDoor,rightDoor,hitX,hitZ);
    }

    public static boolean rightHinge(int facing,int balance,boolean leftDoor,boolean rightDoor,double hitX,double hitZ) {
        if(leftDoor&&!rightDoor||balance>0)return true;
        if(rightDoor&&!leftDoor||balance<0)return false;
        int dx=DX[facing&3],dz=DZ[facing&3];
        return dx<0&&hitZ<.5D||dx>0&&hitZ>.5D||dz<0&&hitX>.5D||dz>0&&hitX<.5D;
    }
}
