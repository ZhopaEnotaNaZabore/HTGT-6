package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.world.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
/** Derived side/up connections; power remains native four-bit redstone metadata. */
public final class RedstoneWire116 {
    public static final com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockRedstoneDot116 dot=new com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockRedstoneDot116();
    private static final int[] DX={0,1,0,-1},DZ={-1,0,1,0},LEGACY={2,3,0,1},SIDE={2,5,3,4};
    private static final ThreadLocal<Integer> SIGNAL_DEPTH=new ThreadLocal<Integer>(){protected Integer initialValue(){return 0;}};
    public static boolean emitting(boolean original,BlockRedstoneWire wire){return original&&(wire!=Blocks.redstone_wire&&wire!=dot||SIGNAL_DEPTH.get()==0);}
    public static int externalPower(World w,int x,int y,int z){int previous=SIGNAL_DEPTH.get();SIGNAL_DEPTH.set(previous+1);try{return w.getStrongestIndirectPower(x,y,z);}finally{SIGNAL_DEPTH.set(previous);}}
    private static boolean registered;
    public static void register(){if(registered)return;cpw.mods.fml.common.registry.GameRegistry.registerBlock(dot,com.mod.htgt6.minecraft1202.Module.Items.Bountiful.ItemBlockBountifulTechnical.class,"backport_1_20_redstone_dot");net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new RedstoneShapeUpdates116());registered=true;}
    public static boolean applies(BlockRedstoneWire wire){return wire==Blocks.redstone_wire;}
    private static Block block(IBlockAccess w,int x,int y,int z){return y<0||y>=w.getHeight()||w instanceof World&&!((World)w).blockExists(x,y,z)?Blocks.air:w.getBlock(x,y,z);}
    public static boolean conductor(IBlockAccess w,int x,int y,int z){return block(w,x,y,z).isNormalCube();}
    private static boolean supports(IBlockAccess w,int x,int y,int z){Block b=block(w,x,y,z);return b.isSideSolid(w,x,y,z,ForgeDirection.UP)||b instanceof BlockGlass||b==Blocks.glowstone||b==Blocks.hopper;}
    private static boolean connected(IBlockAccess w,int x,int y,int z,int direction){Block block=block(w,x,y,z);
        if(block instanceof BlockRedstoneWire)return true;
        if(block==Blocks.air)return false;
        if(direction<0)return false;
        if(block instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.BlockObserver112)return (w.getBlockMetadata(x,y,z)&7)==SIDE[direction];
        return BlockRedstoneWire.isPowerProviderOrWire(w,x,y,z,LEGACY[direction]);
    }
    public static int[] raw(IBlockAccess w,int x,int y,int z){int[] result=new int[4];boolean above=!conductor(w,x,y+1,z);
        for(int i=0;i<4;i++){int nx=x+DX[i],nz=z+DZ[i];Block neighbor=block(w,nx,y,nz);
            if(above&&(neighbor instanceof BlockTrapDoor||supports(w,nx,y,nz))&&connected(w,nx,y+1,nz,-1))result[i]=neighbor.isSideSolid(w,nx,y,nz,ForgeDirection.getOrientation(SIDE[i]^1))?2:1;
            else if(connected(w,nx,y,nz,i)||!conductor(w,nx,y,nz)&&connected(w,nx,y-1,nz,-1))result[i]=1;
        }return result;
    }
    public static int[] sides(IBlockAccess w,int x,int y,int z){int[] out=raw(w,x,y,z);boolean ns=out[0]!=0||out[2]!=0,ew=out[1]!=0||out[3]!=0;
        if(!ns&&!ew&&block(w,x,y,z)==dot)return out;
        if(!ns){if(out[1]==0)out[1]=1;if(out[3]==0)out[3]=1;}
        if(!ew){if(out[0]==0)out[0]=1;if(out[2]==0)out[2]=1;}return out;
    }
    public static String variant(IBlockAccess w,int x,int y,int z){int[] sides=sides(w,x,y,z);String[] values={"none","side","up"};return "east="+values[sides[1]]+",north="+values[sides[0]]+",south="+values[sides[2]]+",west="+values[sides[3]];}
    public static int power(BlockRedstoneWire wire,IBlockAccess w,int x,int y,int z,int side){if(!wire.canProvidePower()||side==0)return 0;int power=w.getBlockMetadata(x,y,z)&15;if(side==1)return power;if(side<2||side>5)return 0;int opposite=side^1;int index=opposite==2?0:opposite==5?1:opposite==3?2:3;return sides(w,x,y,z)[index]!=0?power:0;}
    public static int render(BlockRedstoneWire wire){return Backport120Content.getJsonRenderId();}
    public static boolean toggle(BlockRedstoneWire wire,World w,int x,int y,int z,EntityPlayer player,int side,float hx,float hy,float hz){
        if((wire!=Blocks.redstone_wire&&wire!=dot)||!player.capabilities.allowEdit||!w.canMineBlock(player,x,y,z)||!player.canPlayerEdit(x,y,z,side,player.getHeldItem()))return false;
        for(int connection:raw(w,x,y,z))if(connection!=0)return false;
        if(!w.isRemote){int power=w.getBlockMetadata(x,y,z);if(!w.setBlock(x,y,z,wire==dot?Blocks.redstone_wire:dot,power,3))return false;w.notifyBlocksOfNeighborChange(x,y-1,z,wire);}
        return true;
    }
    private RedstoneWire116(){}
}
