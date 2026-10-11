package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Axis-aware 1.20 chain block, required by the hanging-sign recipe. */
public final class BlockBackport120Chain extends Block implements com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150 {
    private net.minecraft.item.Item chainItem;
    public BlockBackport120Chain(){
        super(Material.iron);
        setBlockName("backport_1_20.chain");
        setBlockTextureName("htgt6:1_20_backport/chain");
        setHardness(5.0F); setResistance(Backport120Content.legacyResistance(6.0F));
        setStepSound(soundTypeMetal); setHarvestLevel("pickaxe",1);
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);
    }
    public void setChainItem(net.minecraft.item.Item item){chainItem=item;}
    @Override public net.minecraft.item.Item getItemDropped(int metadata,java.util.Random random,int fortune){return chainItem;}
    @Override public net.minecraft.item.Item getItem(World world,int x,int y,int z){return chainItem;}
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
    @Override public boolean isOpaqueCube(){return false;}
    @Override public boolean renderAsNormalBlock(){return false;}
    @Override public int onBlockPlaced(World world,int x,int y,int z,int side,float hitX,float hitY,float hitZ,int metadata){
        return (side<2?0:side<4?8:4)|(world.getBlock(x,y,z).getMaterial()==Material.water?1:0);
    }
    @Override public boolean isWaterlogged(IBlockAccess w,int x,int y,int z){return (w.getBlockMetadata(x,y,z)&1)!=0;}
    @Override public boolean setWaterlogged(World w,int x,int y,int z,boolean wet){return !w.isRemote&&com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.ShapeWater116.set(w,x,y,z,this,1,wet);}
    @Override public boolean onBlockActivated(World w,int x,int y,int z,net.minecraft.entity.player.EntityPlayer p,int side,float hx,float hy,float hz){return com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.ShapeWater116.bucket(this,w,x,y,z,p,side);}
    @Override public void onBlockDestroyedByPlayer(World w,int x,int y,int z,int meta){if(!w.isRemote&&(meta&1)!=0&&w.isAirBlock(x,y,z))w.setBlock(x,y,z,net.minecraft.init.Blocks.water,0,3);}
    @Override public net.minecraft.util.AxisAlignedBB getCollisionBoundingBoxFromPool(World w,int x,int y,int z){setBlockBoundsBasedOnState(w,x,y,z);return super.getCollisionBoundingBoxFromPool(w,x,y,z);}
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) @Override public int getRenderBlockPass(){return 1;}
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT) @Override public boolean canRenderInPass(int pass){return com.mod.htgt6.minecraft1202.Module.Client.Modern.ShapeWaterRenderer116.pass(pass,false);}
    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase placer,ItemStack stack){setBlockBoundsBasedOnState(world,x,y,z);}
    @Override public void setBlockBoundsBasedOnState(IBlockAccess world,int x,int y,int z){
        int axis=world.getBlockMetadata(x,y,z)&12;
        if(axis==4)setBlockBounds(0,.40625F,.40625F,1,.59375F,.59375F);
        else if(axis==8)setBlockBounds(.40625F,.40625F,0,.59375F,.59375F,1);
        else setBlockBounds(.40625F,0,.40625F,.59375F,1,.59375F);
    }
}
