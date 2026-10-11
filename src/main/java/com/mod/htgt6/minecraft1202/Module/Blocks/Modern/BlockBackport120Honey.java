package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.Block;import net.minecraft.block.material.Material;import net.minecraft.entity.Entity;import net.minecraft.world.World;import net.minecraft.util.AxisAlignedBB;
/** Separate voxel collision, surface movement and side sliding. Fall/jump hooks live in BuzzyBees115. */
public final class BlockBackport120Honey extends BlockBackport120EtFGapSimple {
    public BlockBackport120Honey(){super(Material.clay,"honey_block","honey_block_side","honey_block_top",0,0,new Block.SoundType("honey",1,1){public String getBreakSound(){return "htgt6:block.honey_block.break";}public String getStepResourcePath(){return "htgt6:block.honey_block.step";}public String func_150496_b(){return "htgt6:block.honey_block.place";}},null,0);setLightOpacity(0);setBlockBounds(.0625F,0,.0625F,.9375F,.9375F,.9375F);}
    @Override public boolean isOpaqueCube(){return false;}
    @Override public boolean renderAsNormalBlock(){return false;}
    @Override public int getRenderBlockPass(){return 1;}
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World w,int x,int y,int z){return AxisAlignedBB.getBoundingBox(x+.0625,y,z+.0625,x+.9375,y+.9375,z+.9375);}
    @Override public void onEntityCollidedWithBlock(World w,int x,int y,int z,Entity e){
        if(e.onGround){e.motionX*=.4;e.motionZ*=.4;return;}
        double edge=.4375+e.width*.5;
        if(e.boundingBox.minY>y+.9375-1E-7||e.motionY>=-.08||Math.abs(x+.5-e.posX)+1E-7<=edge&&Math.abs(z+.5-e.posZ)+1E-7<=edge)return;
        if(e.motionY<-.13){double ratio=-.05/e.motionY;e.motionX*=ratio;e.motionZ*=ratio;}
        e.motionY=-.05;e.fallDistance=0;
        if(!w.isRemote&&e.ticksExisted%20==0){w.playSoundAtEntity(e,"htgt6:block.honey_block.slide",1,1);if(e instanceof net.minecraft.entity.player.EntityPlayer)com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.complete((net.minecraft.entity.player.EntityPlayer)e,"sticky_situation");}
    }
}
