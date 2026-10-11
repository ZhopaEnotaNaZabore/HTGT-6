package com.mod.htgt6.minecraft1202.Module.Client.Modern;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150;
import cpw.mods.fml.relauncher.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

/** Opaque geometry in pass 0, native biome-tinted water in pass 1. */
@SideOnly(Side.CLIENT)
public final class ShapeWaterRenderer116 {
    private static final ThreadLocal<Integer> PASS=new ThreadLocal<Integer>(){protected Integer initialValue(){return 0;}};
    public static boolean pass(int pass,boolean full){PASS.set(pass);return pass==0||(!full&&pass==1);}
    public static boolean waterPass(){return PASS.get()==1;}
    public static boolean supports(Block block){return block==Blocks.cobblestone_wall||block instanceof BlockBlackstoneSlab116||block instanceof BlockBlackstoneStairs116||block instanceof BlockBlackstoneWall116||block instanceof BlockBackport120Chain;}
    public static boolean render(IBlockAccess world,int x,int y,int z,Block block,RenderBlocks renderer) {
        if(!supports(block)||!((IWaterloggedBlock150)block).isWaterlogged(world,x,y,z))return false;
        IBlockAccess previous=renderer.blockAccess;renderer.blockAccess=new Access(world);
        try{return renderer.renderBlockLiquid(Blocks.water,x,y,z);}finally{renderer.blockAccess=previous;}
    }
    public static final class Access implements IBlockAccess {
        private final IBlockAccess world;
        public Access(IBlockAccess world){this.world=world;}
        private boolean wet(int x,int y,int z){Block b=world.getBlock(x,y,z);return b instanceof IWaterloggedBlock150&&((IWaterloggedBlock150)b).isWaterlogged(world,x,y,z);}
        public Block getBlock(int x,int y,int z){return wet(x,y,z)?Blocks.water:world.getBlock(x,y,z);}
        public int getBlockMetadata(int x,int y,int z){return wet(x,y,z)?0:world.getBlockMetadata(x,y,z);}
        public TileEntity getTileEntity(int x,int y,int z){return world.getTileEntity(x,y,z);}
        public int getLightBrightnessForSkyBlocks(int x,int y,int z,int min){return world.getLightBrightnessForSkyBlocks(x,y,z,min);}
        public int isBlockProvidingPowerTo(int x,int y,int z,int side){return world.isBlockProvidingPowerTo(x,y,z,side);}
        public boolean isAirBlock(int x,int y,int z){return world.isAirBlock(x,y,z);}
        public BiomeGenBase getBiomeGenForCoords(int x,int z){return world.getBiomeGenForCoords(x,z);}
        public int getHeight(){return world.getHeight();}
        public boolean extendedLevelsInChunkCache(){return world.extendedLevelsInChunkCache();}
        public boolean isSideSolid(int x,int y,int z,ForgeDirection side,boolean fallback){return world.isSideSolid(x,y,z,side,fallback);}
    }
    private ShapeWaterRenderer116(){}
}
