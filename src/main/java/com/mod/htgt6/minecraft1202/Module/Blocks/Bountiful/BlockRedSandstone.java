package com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/** Normal, chiseled and smooth red sandstone variants from Minecraft 1.8. */
public final class BlockRedSandstone extends Block {
    private final IIcon[] side=new IIcon[3]; private IIcon top,bottom;
    public BlockRedSandstone(){super(Material.rock);setBlockName("red_sandstone");setHardness(0.8F);setResistance(Backport120Content.legacyResistance(0.8F));setStepSound(soundTypePiston);setCreativeTab(CreativeTabs.tabBlock);}
    @Override public void registerBlockIcons(IIconRegister r){side[0]=r.registerIcon("htgt6:bountiful/red_sandstone_normal");side[1]=r.registerIcon("htgt6:bountiful/red_sandstone_carved");side[2]=r.registerIcon("htgt6:bountiful/red_sandstone_smooth");top=r.registerIcon("htgt6:bountiful/red_sandstone_top");bottom=r.registerIcon("htgt6:bountiful/red_sandstone_bottom");blockIcon=side[0];}
    @Override public IIcon getIcon(int sideIndex,int meta){meta=Math.max(0,Math.min(2,meta));return sideIndex==0?bottom:sideIndex==1?top:side[meta];}
    @Override public int damageDropped(int meta){return Math.max(0,Math.min(2,meta));}
    public static float hardnessForMetadata(int metadata){return 0.8F;}
    public static float resistanceForMetadata(int metadata){return 0.8F;}
    @Override public float getBlockHardness(World world,int x,int y,int z){return hardnessForMetadata(world==null?0:world.getBlockMetadata(x,y,z));}
    @Override public float getExplosionResistance(Entity entity,World world,int x,int y,int z,double explosionX,double explosionY,double explosionZ){return resistanceForMetadata(world.getBlockMetadata(x,y,z));}
    @Override @SuppressWarnings({"rawtypes","unchecked"}) public void getSubBlocks(Item item,CreativeTabs tab,List list){for(int i=0;i<3;i++)list.add(new ItemStack(item,1,i));}
}
