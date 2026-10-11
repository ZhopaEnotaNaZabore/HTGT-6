package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.block.BlockDoor;
import net.minecraft.world.World;

/** One server-side state change and one family-specific sound for manual and powered doors. */
public final class DoorSound120 {
    private DoorSound120() { }
    public static boolean setOpen(BlockDoor door,String family,World world,int x,int y,int z,boolean open) {
        if(world.isRemote||world.getBlock(x,y,z)!=door)return false;
        int soundY=y;
        if((world.getBlockMetadata(x,y,z)&8)!=0)y--;
        if(world.getBlock(x,y,z)!=door)return false;
        int lower=world.getBlockMetadata(x,y,z);
        if(((lower&4)!=0)==open)return false;
        if(!world.setBlockMetadataWithNotify(x,y,z,(lower&3)|(open?4:0),2))return false;
        world.markBlockRangeForRenderUpdate(x,y,z,x,y+1,z);
        world.playSoundEffect(x+.5D,soundY+.5D,z+.5D,"htgt6:block."+family+"_door."+(open?"open":"close"),
                1F,.9F+world.rand.nextFloat()*.1F);
        return true;
    }
    public static boolean toggle(BlockDoor door,String family,World world,int x,int y,int z) {
        setOpen(door,family,world,x,y,z,(door.func_150012_g(world,x,y,z)&4)==0);
        return true;
    }
}
