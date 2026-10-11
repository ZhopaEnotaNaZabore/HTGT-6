package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;
import net.minecraft.block.Block;
import net.minecraft.util.IIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import cpw.mods.fml.relauncher.*;
/** Every owned wall shares modern geometry and source-water behavior. Registry IDs remain stable. */
public final class BlockBackport120Wall extends BlockBlackstoneWall116 {
    public BlockBackport120Wall(Block source,String name,String texture){super(source,name);setBlockTextureName("htgt6:1_20_backport/"+texture);}
    @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister icons){blockIcon=icons.registerIcon(getTextureName());}
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int side,int metadata){return blockIcon;}
}
