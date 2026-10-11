package com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.biomeGliese792c;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeDecoratorSpace;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import com.mod.htgt6.sulimited.world.GlieseCSubBiomeGenerator;
import com.mod.htgt6.sulimited.world.GlieseCSubBiomeGenerator.Type;
import com.mod.htgt6.sulimited.world.GlieseTerrain;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.blocks.blocksGliese792C;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysGeology;
/** Per-column habitats, generated in owned chunk arrays before publication. */
public class BiomeDecoratorGlieseC extends BiomeDecoratorSpace {
    private World currentWorld;
    protected void decorate() {}
    protected void setCurrentWorld(World world) { currentWorld=world; }
    protected World getCurrentWorld() { return currentWorld; }
    public void decorateColumns(long seed,int cx,int cz,Block[] blocks,byte[] meta,GlieseCSubBiomeGenerator sampler) {
        for(int x=0;x<16;x++) for(int z=0;z<16;z++) {
            int wx=cx*16+x,wz=cz*16+z,column=(x*16+z)*256;
            GlieseCSubBiomeGenerator.Sample s=sampler.sample(seed,wx,wz,160);
            int y=s.height+1; if(y>=159) continue;
            long hash=GlieseTerrain.hash(seed+317,wx,wz);
            boolean deep=s.type==Type.DEEP_REEF, reef=deep || s.type==Type.REEF;
            if(s.type==Type.UNDERWATER_VOLCANO && (hash&63)==0) {
                // Mineral chimneys remain intact in salt water; exposed lava would quench immediately.
                for(int dy=0;dy<2+((hash>>>8)&3) && y+dy<159;dy++) {
                    if(blocks[column+y+dy]!=SolSysGeology.saltWater) break;
                    blocks[column+y+dy]=blocksGliese792C.ventRock;
                }
            } else if(reef && (hash&7)<3) {
                int mound=(int)((hash>>>8)&1);
                if(mound>0) { blocks[column+y]=deep?blocksGliese792C.deepReefRock:blocksGliese792C.reefRock; y++; }
                int height=1+(int)((hash>>>10)&(deep?3:1));
                for(int dy=0;dy<height && y+dy<160;dy++) {
                    if(blocks[column+y+dy]!=SolSysGeology.saltWater) break;
                    blocks[column+y+dy]=deep?blocksGliese792C.deepCoral:blocksGliese792C.GlieseCCoral;
                    meta[column+y+dy]=(byte)((hash>>>16)%3);
                }
            } else if((s.type==Type.LOW_SEA || s.type==Type.SEA || s.type==Type.REEF) && (hash&15)<3) {
                int height=1+(int)((hash>>>8)&3);
                for(int dy=0;dy<height && y+dy<160;dy++) {
                    if(blocks[column+y+dy]!=SolSysGeology.saltWater) break;
                    blocks[column+y+dy]=blocksGliese792C.GlieseCAlgae;
                }
            }
        }
    }
}
