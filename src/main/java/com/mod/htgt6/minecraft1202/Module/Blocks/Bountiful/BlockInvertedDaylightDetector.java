package com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import java.util.Random;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
public final class BlockInvertedDaylightDetector extends net.minecraft.block.BlockDaylightDetector {
    private IIcon invertedTop;
    public BlockInvertedDaylightDetector(){setBlockName("inverted_daylight_detector");setBlockTextureName("minecraft:daylight_detector");setHardness(.2F);setResistance(Backport120Content.legacyResistance(.2F));setCreativeTab(null);}
    public void registerBlockIcons(IIconRegister r){super.registerBlockIcons(r);invertedTop=r.registerIcon("htgt6:bountiful/daylight_detector_inverted_top");}
    public IIcon getIcon(int side,int meta){return side==1?invertedTop:super.getIcon(side,meta);}
    /** Inverted sky brightness has no sun-angle multiplier. */
    public static int invertedPower(int light,int darkening){return Math.max(0,Math.min(15,15-(light-darkening)));}
    @Override public void func_149957_e(World w,int x,int y,int z){
        if(w.isRemote||w.provider.hasNoSky)return;
        int power=invertedPower(w.getSavedLightValue(EnumSkyBlock.Sky,x,y,z),w.skylightSubtracted);
        if(w.getBlockMetadata(x,y,z)!=power)w.setBlockMetadataWithNotify(x,y,z,power,3);
    }
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float a,float b,float c){
        if(!p.capabilities.allowEdit)return false;
        if(!w.isRemote){w.setBlock(x,y,z,Blocks.daylight_detector,0,3);((net.minecraft.block.BlockDaylightDetector)Blocks.daylight_detector).func_149957_e(w,x,y,z);}return true;
    }
    public Item getItemDropped(int meta,Random r,int fortune){return Item.getItemFromBlock(Blocks.daylight_detector);}
    public Item getItem(World w,int x,int y,int z){return Item.getItemFromBlock(Blocks.daylight_detector);}
}
