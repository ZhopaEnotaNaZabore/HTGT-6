package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.world.World;

/** Upper-half bits 1/2 store power and its initialization; legacy hinge remains bit 0. */
public final class DoorPower120 {
    private DoorPower120() { }
    public static int upperMetadata(boolean rightHinge,boolean powered) {
        return 8|4|(rightHinge?1:0)|(powered?2:0);
    }
    /** Return false only when vanilla must handle broken halves or missing support. */
    public static boolean neighbor(BlockDoor door,World world,int x,int y,int z,Block changed) {
        if(world.isRemote)return true;
        if((world.getBlockMetadata(x,y,z)&8)!=0)y--;
        if(world.getBlock(x,y,z)!=door||world.getBlock(x,y+1,z)!=door
                ||!World.doesBlockHaveSolidTopSurface(world,x,y-1,z))return false;
        if(changed==door)return true;
        int upper=world.getBlockMetadata(x,y+1,z);
        boolean powered=world.isBlockIndirectlyGettingPowered(x,y,z)||world.isBlockIndirectlyGettingPowered(x,y+1,z);
        int updated=upperMetadata((upper&1)!=0,powered);
        if((upper&4)==0) {
            // Old saves have no previous signal. Adopt the current level without undoing manual OPEN.
            world.setBlockMetadataWithNotify(x,y+1,z,updated,2);
            return true;
        }
        if(((upper&2)!=0)!=powered) {
            world.setBlockMetadataWithNotify(x,y+1,z,updated,2);
            door.func_150014_a(world,x,y,z,powered);
        }
        return true;
    }
}
