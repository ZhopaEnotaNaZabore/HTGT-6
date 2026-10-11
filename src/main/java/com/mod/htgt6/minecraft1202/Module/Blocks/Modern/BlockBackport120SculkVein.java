package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

/** Sculk vein with six persistent face states stored outside legacy four-bit metadata. */
public final class BlockBackport120SculkVein extends Block {
    public BlockBackport120SculkVein() {
        super(Material.vine);
        setBlockName("backport_1_20.sculk_vein");
        setBlockTextureName("htgt6:1_20_backport/sculk_vein");
        setHardness(0.2F);
        setResistance(Backport120Content.legacyResistance(0.2F));
        setStepSound(soundTypeGrass);
        setCreativeTab(CreativeTabs.tabDecorations);
    }
    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public boolean hasTileEntity(int metadata){return true;}
    @Override public TileEntity createTileEntity(World world,int metadata){return new TileEntityBackport120SculkVein();}
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }
    @Override protected boolean canSilkHarvest() { return true; }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) { return null; }
    @Override public int getExpDrop(IBlockAccess world,int metadata,int fortune){return 1;}
    @Override public int onBlockPlaced(World world,int x,int y,int z,int side,float hitX,float hitY,float hitZ,int metadata){return side;}
    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase placer,ItemStack stack){
        TileEntityBackport120SculkVein tile=tile(world,x,y,z);
        int clicked=Math.max(0,Math.min(5,world.getBlockMetadata(x,y,z)));
        if(tile!=null)tile.setFaces(1<<ForgeDirection.getOrientation(clicked).getOpposite().ordinal());
        world.setBlockMetadataWithNotify(x,y,z,0,2);
    }
    @Override public boolean canPlaceBlockAt(World world, int x, int y, int z) { return hasAttachment(world, x, y, z); }
    @Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        TileEntityBackport120SculkVein tile=tile(world,x,y,z);int faces=tile==null?0:tile.getFaces(),kept=0;
        for(ForgeDirection face:ForgeDirection.VALID_DIRECTIONS)
            if((faces==0||(faces&(1<<face.ordinal()))!=0)&&supported(world,x,y,z,face))kept|=1<<face.ordinal();
        if(kept==0)world.setBlockToAir(x,y,z);else if(tile!=null&&kept!=faces)tile.setFaces(kept);
    }

    public static boolean attached(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        TileEntityBackport120SculkVein tile=tile(world,x,y,z);
        return (tile==null||tile.getFaces()==0||(tile.getFaces()&(1<<face.ordinal()))!=0)
                &&supported(world,x,y,z,face);
    }
    private static boolean supported(IBlockAccess world,int x,int y,int z,ForgeDirection face){
        int nx=x+face.offsetX,ny=y+face.offsetY,nz=z+face.offsetZ;
        return world.getBlock(nx,ny,nz).isSideSolid(world,nx,ny,nz,face.getOpposite());
    }
    public static void setFace(World world,int x,int y,int z,ForgeDirection face){
        TileEntityBackport120SculkVein tile=tile(world,x,y,z);if(tile!=null)tile.setFaces(tile.getFaces()|1<<face.ordinal());
    }
    private static TileEntityBackport120SculkVein tile(IBlockAccess world,int x,int y,int z){
        TileEntity tile=world.getTileEntity(x,y,z);return tile instanceof TileEntityBackport120SculkVein?(TileEntityBackport120SculkVein)tile:null;
    }
    private static boolean hasAttachment(IBlockAccess world, int x, int y, int z) {
        for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS)
            if (attached(world, x, y, z, face)) return true;
        return false;
    }
}
