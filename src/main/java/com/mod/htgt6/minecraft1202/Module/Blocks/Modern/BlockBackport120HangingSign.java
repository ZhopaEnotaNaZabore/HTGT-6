package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

/** Ceiling/wall hanging-sign technical block. Rotation is kept in vanilla four-bit metadata. */
public final class BlockBackport120HangingSign extends BlockContainer {
    private final boolean wall;
    private Item signItem;

    public BlockBackport120HangingSign(String name, boolean wall) {
        super(com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherWoodMaterial116.forName(name));
        this.wall = wall;
        setBlockName("backport_1_20." + name);
        String family=name.startsWith("mangrove_")?"mangrove":name.startsWith("bamboo_")?"bamboo_block":"cherry";
        setBlockTextureName("htgt6:1_20_backport/stripped_" + family + ("bamboo_block".equals(family)?"":"_log"));
        if(com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherWoodMaterial116.matches(name))setBlockTextureName("htgt6:1_20_backport/stripped_"+(name.startsWith("crimson_")?"crimson":"warped")+"_stem");
        setHardness(1.0F);
        setResistance(Backport120Content.legacyResistance(1.0F));
        setStepSound(soundTypeWood);
        if (wall) setBlockBounds(0.125F, 0.1875F, 0.125F, 0.875F, 0.875F, 0.875F);
        else setBlockBounds(0.125F, 0.125F, 0.125F, 0.875F, 1.0F, 0.875F);
    }

    public void setSignItem(Item item) { signItem = item; }
    public boolean isWallSign() { return wall; }
    @Override public TileEntity createNewTileEntity(World world, int metadata) { return new TileEntityBackport120Sign(); }
    @Override public int getRenderType() { return -1; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean getBlocksMovement(IBlockAccess world, int x, int y, int z) { return true; }
    @Override public net.minecraft.util.AxisAlignedBB getCollisionBoundingBoxFromPool(World world,int x,int y,int z){return null;}
    @Override public Item getItemDropped(int metadata, Random random, int fortune) { return signItem; }
    @Override public Item getItem(World world, int x, int y, int z) { return signItem; }

    @Override public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        if (!super.canPlaceBlockAt(world, x, y, z)) return false;
        if (!wall) return world.isSideSolid(x, y + 1, z, net.minecraftforge.common.util.ForgeDirection.DOWN);
        return true;
    }

    @Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        int metadata=world.getBlockMetadata(x,y,z);
        boolean supported = wall ? wallSupport(world,x,y,z,metadata)
                : world.isSideSolid(x,y+1,z,net.minecraftforge.common.util.ForgeDirection.DOWN);
        if (!supported) { dropBlockAsItem(world,x,y,z,metadata,0); world.setBlockToAir(x,y,z); }
    }

    private static boolean wallSupport(World world,int x,int y,int z,int side){
        return side==2&&world.getBlock(x,y,z+1).getMaterial().isSolid()
                ||side==3&&world.getBlock(x,y,z-1).getMaterial().isSolid()
                ||side==4&&world.getBlock(x+1,y,z).getMaterial().isSolid()
                ||side==5&&world.getBlock(x-1,y,z).getMaterial().isSolid();
    }

    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,
                                              int side,float hitX,float hitY,float hitZ){
        if(!world.isRemote&&world.getTileEntity(x,y,z) instanceof TileEntityBackport120Sign)
            player.func_146100_a((TileEntityBackport120Sign)world.getTileEntity(x,y,z));
        return true;
    }
}
