package com.mod.htgt6.common.item.htgt6.fluids;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;


public class blockFluidHTGT6 extends BlockFluidClassic {



    @SideOnly(Side.CLIENT)
    protected IIcon stillIcon;
    @SideOnly(Side.CLIENT)
    protected IIcon flowingIcon;

    private boolean isDamaged = false;
public blockFluidHTGT6 (Fluid fluid, Material material, boolean isDamage) {
    super(fluid, material);
    this.isDamaged = isDamage;
}
  //  @SideOnly(Side.CLIENT)
   // @Override
  //  public void registerBlockIcons(IIconRegister register) {
    //    stillIcon = register
    //            .registerIcon(HTGT6.ASSET_PREFIX + ":" + "fluid/hitech6/" + this.fluidName.toLowerCase() + "still");
    //    flowingIcon = register
    //            .registerIcon(HTGT6.ASSET_PREFIX + ":" + "fluid/hitech6/" + this.fluidName.toLowerCase() + "flowing");
    }
 //   @SideOnly(Side.CLIENT)
 //   @Override
  //  public CreativeTabs getCreativeTabToDisplayOn() {
  //      return null;
 //   }

 //   @Override
   // public boolean canDisplace(IBlockAccess world, int x, int y, int z) {
  //      if (world.getBlock(x, y, z).getMaterial().isLiquid())
        //    return false;
   //     return super.canDisplace(world, x, y, z);
  //  }
  //  @Override
  //  public boolean displaceIfPossible(World world, int x, int y, int z) {
   //     if (world.getBlock(x, y, z).getMaterial().isLiquid())
   //         return false;
     //   return super.displaceIfPossible(world, x, y, z);
//    }
 //   public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
  //  {
    //    if(this.isDamaged)
   //         entity.attackEntityFrom(DamageSource.generic, 0.5F);
  //  }

//}

