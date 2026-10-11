package com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

/** Metadata container for the five dead coral plants or floor fans. */
public final class BlockDeadCoralPlant120 extends Block {
    private static final String[] TYPES={"tube","brain","bubble","fire","horn"};
    private final IIcon[] icons=new IIcon[5];
    private final boolean fan;
    public BlockDeadCoralPlant120(boolean fan){super(Material.water);this.fan=fan;
        setBlockName(fan?"dead_coral_fan":"dead_coral");setHardness(0F);
        setStepSound(soundTypeGrass);setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(.1F,0,.1F,.9F,fan?.45F:.85F,.9F);}
    @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister register){for(int i=0;i<5;i++)icons[i]=register.registerIcon(HTGT6.MOD_ID.toLowerCase()+":dead_"+TYPES[i]+"_coral"+(fan?"_fan":""));blockIcon=icons[0];}
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int side,int meta){return icons[Math.max(0,Math.min(4,meta))];}
    @Override public int getRenderType(){return OceanContent.getKelpRenderId();}
    @Override public int getRenderBlockPass(){return 1;}
    @Override public boolean canRenderInPass(int pass){return OceanContent.kelp.canRenderInPass(pass);}
    @Override public boolean isOpaqueCube(){return false;}
    @Override public boolean renderAsNormalBlock(){return false;}
    @Override public boolean getBlocksMovement(IBlockAccess world,int x,int y,int z){return true;}
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world,int x,int y,int z){return null;}
    @Override public boolean canPlaceBlockAt(World world,int x,int y,int z){return BlockKelp.isOpenWater(world.getBlock(x,y,z))&&BlockKelp.canRootOn(world.getBlock(x,y-1,z));}
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block neighbor){if(!BlockKelp.canRootOn(world.getBlock(x,y-1,z))){dropBlockAsItem(world,x,y,z,world.getBlockMetadata(x,y,z),0);world.setBlock(x,y,z,BlockKelp.waterFor(world,x,y,z),0,2);}}
    @Override public void breakBlock(World world,int x,int y,int z,Block old,int meta){super.breakBlock(world,x,y,z,old,meta);BlockKelp.restoreWaterAfterRemoval(world,x,y,z);}
    @Override public int damageDropped(int meta){return Math.max(0,Math.min(4,meta));}
    @Override @SuppressWarnings({"rawtypes","unchecked"}) public void getSubBlocks(Item item,CreativeTabs tab,List list){for(int i=0;i<5;i++)list.add(new ItemStack(item,1,i));}
}
