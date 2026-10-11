package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.BuildingSounds116;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import java.util.Random;

/** Stone plates respond to living entities, with the modern 20-tick release interval. */
public final class BlockBlackstonePressurePlate116 extends BlockPressurePlate {
    public BlockBlackstonePressurePlate116() {
        super("htgt6:1_20_backport/polished_blackstone", Material.rock, Sensitivity.mobs);
        setBlockName("backport_1_20.polished_blackstone_pressure_plate");
        setHardness(.5F); setResistance(Backport120Content.legacyResistance(.5F));
        setTickRandomly(false);
        setStepSound(BuildingSounds116.STONE); setHarvestLevel("pickaxe", 0);
    }
    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public void onEntityCollidedWithBlock(World w,int x,int y,int z,Entity e) {
        if (!w.isRemote && w.getBlockMetadata(x,y,z)==0) refresh(w,x,y,z);
    }
    @Override public void updateTick(World w,int x,int y,int z,Random r) {
        if (!w.isRemote && w.getBlockMetadata(x,y,z)!=0) refresh(w,x,y,z);
    }
    private void refresh(World w,int x,int y,int z) {
        boolean old=w.getBlockMetadata(x,y,z)!=0, pressed=func_150065_e(w,x,y,z)>0;
        if (old!=pressed && w.setBlockMetadataWithNotify(x,y,z,pressed?1:0,2)) {
            w.notifyBlocksOfNeighborChange(x,y,z,this);
            w.notifyBlocksOfNeighborChange(x,y-1,z,this);
            w.markBlockRangeForRenderUpdate(x,y,z,x,y,z);
            w.playSoundEffect(x+.5,y+.1,z+.5,"htgt6:block.stone_pressure_plate.click_"+(pressed?"on":"off"),1,1);
        }
        if (pressed) w.scheduleBlockUpdate(x,y,z,this,20);
    }
}
