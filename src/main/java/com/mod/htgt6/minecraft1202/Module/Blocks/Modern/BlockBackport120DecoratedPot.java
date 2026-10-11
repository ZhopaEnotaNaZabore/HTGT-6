package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

/** TESR-rendered pot whose four recipe ingredients survive placement, save, sync, and pickup. */
public final class BlockBackport120DecoratedPot extends BlockContainer {
    public BlockBackport120DecoratedPot(){super(Material.rock);setBlockName("backport_1_20.decorated_pot");setHardness(0);
        setBlockTextureName("htgt6:1_20_backport/decorated_pot_side");
        setResistance(Backport120Content.legacyResistance(0));setStepSound(soundTypeStone);setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(1F/16,0,1F/16,15F/16,1,15F/16);}
    @Override public TileEntity createNewTileEntity(World world,int metadata){return new TileEntityBackport120DecoratedPot();}
    @Override public int getRenderType(){return -1;}@Override public boolean isOpaqueCube(){return false;}@Override public boolean renderAsNormalBlock(){return false;}
    @Override public Item getItemDropped(int metadata,Random random,int fortune){return null;}
    @Override public int onBlockPlaced(World world,int x,int y,int z,int side,float hx,float hy,float hz,int metadata){return metadata&3;}
    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase placer,ItemStack stack){
        int facing=MathHelper.floor_double(placer.rotationYaw*4F/360F+.5D)&3;world.setBlockMetadataWithNotify(x,y,z,facing,2);
        TileEntity tile=world.getTileEntity(x,y,z);if(tile instanceof TileEntityBackport120DecoratedPot)((TileEntityBackport120DecoratedPot)tile).readFromItem(stack);}
    @Override public void onBlockHarvested(World world,int x,int y,int z,int metadata,EntityPlayer player){TileEntity tile=world.getTileEntity(x,y,z);
        ItemStack held=player.getCurrentEquippedItem();boolean tool=held!=null&&(held.getItem() instanceof ItemTool||held.getItem() instanceof ItemHoe);
        if(tile instanceof TileEntityBackport120DecoratedPot){TileEntityBackport120DecoratedPot pot=(TileEntityBackport120DecoratedPot)tile;
            pot.setSuppressDrop(player.capabilities.isCreativeMode);pot.setShatter(tool&&!EnchantmentHelper.getSilkTouchModifier(player));}}
    @Override public void breakBlock(World world,int x,int y,int z,net.minecraft.block.Block block,int metadata){TileEntity tile=world.getTileEntity(x,y,z);
        if(!world.isRemote&&tile instanceof TileEntityBackport120DecoratedPot){TileEntityBackport120DecoratedPot pot=(TileEntityBackport120DecoratedPot)tile;if(pot.shouldSuppressDrop()){super.breakBlock(world,x,y,z,block,metadata);return;}
            if(pot.shouldShatter())for(String value:pot.getDecorations())dropBlockAsItem(world,x,y,z,ingredient(value));
            else dropBlockAsItem(world,x,y,z,pot.asItem());}super.breakBlock(world,x,y,z,block,metadata);}
    private static ItemStack ingredient(String name){if("brick".equals(name))return new ItemStack(net.minecraft.init.Items.brick);
        for(int i=0;i<Backport120Content.POTTERY_SHERD_NAMES.length;i++)if(name.equals(Backport120Content.POTTERY_SHERD_NAMES[i]))return new ItemStack(Backport120Content.potterySherds[i]);
        return new ItemStack(net.minecraft.init.Items.brick);}
}
