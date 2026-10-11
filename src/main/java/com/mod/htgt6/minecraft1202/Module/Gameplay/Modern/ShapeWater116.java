package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/** Metadata water state for the new shapes, consumed by the shared fluid hooks. */
public final class ShapeWater116 {
    public static boolean present(World w,int x,int y,int z) {
        return w.getBlock(x,y,z).getMaterial()==Material.water;
    }
    public static boolean set(World w,int x,int y,int z,Block block,int bit,boolean wet) {
        if(w.getBlock(x,y,z)!=block)return false;
        int before=w.getBlockMetadata(x,y,z),after=wet?before|bit:before&~bit;
        return before!=after && w.setBlock(x,y,z,block,after,3);
    }
    public static boolean remove(World w,int x,int y,int z,boolean wet) {
        return wet?w.setBlock(x,y,z,Blocks.water,0,3):w.setBlockToAir(x,y,z);
    }
    public static boolean bucket(com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150 block,
                                 World w,int x,int y,int z,net.minecraft.entity.player.EntityPlayer player,int side) {
        boolean before=block.isWaterlogged(w,x,y,z);
        boolean used=com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.WaterloggedBuckets150.use(block,w,x,y,z,player,side);
        if(used&&!w.isRemote&&before!=block.isWaterlogged(w,x,y,z))
            w.playSoundEffect(x+.5,y+.5,z+.5,before?"htgt6:item.bucket.fill":"htgt6:item.bucket.empty",1,1);
        return used;
    }
    /** ItemBucket uses this event when sneaking bypasses block activation.
     * Forge owns the inventory transaction; the handler must not consume a bucket itself. */
    public static final class Buckets {
        @cpw.mods.fml.common.eventhandler.SubscribeEvent(priority=cpw.mods.fml.common.eventhandler.EventPriority.LOWEST)
        public void use(net.minecraftforge.event.entity.player.FillBucketEvent event) {
            if(event.getResult()!=cpw.mods.fml.common.eventhandler.Event.Result.DEFAULT||event.current==null||event.current.stackSize<=0
                    ||event.target==null||event.target.typeOfHit!=net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK)return;
            int x=event.target.blockX,y=event.target.blockY,z=event.target.blockZ;World w=event.world;
            Block raw=w.getBlock(x,y,z);
            if(raw!=Blocks.cobblestone_wall&&!(raw instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBlackstoneSlab116)
                    &&!(raw instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBlackstoneStairs116)
                    &&!(raw instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBlackstoneWall116)
                    &&!(raw instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Chain))return;
            com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150 block=
                    (com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150)raw;
            boolean wet=block.isWaterlogged(w,x,y,z);
            boolean fill=event.current.getItem()==net.minecraft.init.Items.water_bucket&&!wet;
            boolean drain=event.current.getItem()==net.minecraft.init.Items.bucket&&wet;
            if(!fill&&!drain||!w.canMineBlock(event.entityPlayer,x,y,z)
                    ||!event.entityPlayer.canPlayerEdit(x,y,z,event.target.sideHit,event.current))return;
            // Full slabs reject the water-container interaction, including in the Nether.
            if(raw instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBlackstoneSlab116&&raw.isOpaqueCube())return;
            if(!w.isRemote) {
                if(fill&&w.provider.isHellWorld)w.playSoundEffect(x+.5,y+.5,z+.5,"random.fizz",.5F,2.6F);
                else {
                    if(!block.setWaterlogged(w,x,y,z,fill))return;
                    w.playSoundEffect(x+.5,y+.5,z+.5,drain?"htgt6:item.bucket.fill":"htgt6:item.bucket.empty",1,1);
                }
            }
            event.result=new net.minecraft.item.ItemStack(fill?net.minecraft.init.Items.bucket:net.minecraft.init.Items.water_bucket);
            event.setResult(cpw.mods.fml.common.eventhandler.Event.Result.ALLOW);
        }
    }
    private ShapeWater116() {}
}
