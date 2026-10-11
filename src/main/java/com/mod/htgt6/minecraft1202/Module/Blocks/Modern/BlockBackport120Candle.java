package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

/** 1.7-native candle state: bits 0..1 count minus one, bit 2 lit. */
public final class BlockBackport120Candle extends Block {
    public static final int LIT=4;
    private static final int[][] CANDLES={{7,0,7,9,7,9},{4,0,7,6,5,9},{9,0,5,11,6,7},{9,0,10,11,4,12}};
    private static int renderType=2;
    private final String texture;
    public BlockBackport120Candle(String name,boolean white){super(Material.circuits);texture=white?"white_concrete":"smooth_sandstone";
        setBlockName("htgt6.backport120."+name);setHardness(.1F);setStepSound(soundTypeCloth);setTickRandomly(true);}
    public static void setRenderType(int value){renderType=value;}
    public static int count(int metadata){return (metadata&3)+1;}
    public static int metadata(int count,boolean lit){return Math.max(0,Math.min(3,count-1))|(lit?LIT:0);}
    public static int[] candleBounds(int index){return CANDLES[Math.max(0,Math.min(3,index))];}
    public static double[] flame(int index){int[] c=candleBounds(index);return new double[]{(c[0]+c[3])/32D,c[4]/16D+.035D,(c[2]+c[5])/32D};}
    @Override public boolean isOpaqueCube(){return false;}
    @Override public boolean renderAsNormalBlock(){return false;}
    @Override public int getRenderType(){return renderType;}
    @Override public int quantityDropped(int metadata,int fortune,Random random){return count(metadata);}
    @Override public int quantityDropped(Random random){return 1;}
    @Override public int damageDropped(int metadata){return 0;}
    @Override public int getLightValue(IBlockAccess world,int x,int y,int z){int m=world.getBlockMetadata(x,y,z);return (m&LIT)==0?0:3*count(m);}
    @Override public void setBlockBoundsBasedOnState(IBlockAccess world,int x,int y,int z){int count=count(world.getBlockMetadata(x,y,z));float r=(2F+count)/16F;setBlockBounds(.5F-r,0,.5F-r,.5F+r,.5F,.5F+r);}
    @Override public boolean canPlaceBlockAt(World world,int x,int y,int z){return super.canPlaceBlockAt(world,x,y,z)&&World.doesBlockHaveSolidTopSurface(world,x,y-1,z);}
    @Override public void updateTick(World world,int x,int y,int z,Random random){
        int metadata=world.getBlockMetadata(x,y,z);
        if((metadata&LIT)!=0&&world.isRaining()&&world.canLightningStrikeAt(x,y,z)){
            world.setBlockMetadataWithNotify(x,y,z,metadata&3,3);
            world.playSoundEffect(x+.5D,y+.35D,z+.5D,"random.fizz",.25F,1.8F);
        }
    }
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hitX,float hitY,float hitZ){
        int m=world.getBlockMetadata(x,y,z);ItemStack held=player.getCurrentEquippedItem();
        if((m&LIT)==0&&held!=null&&held.getItem()==Items.flint_and_steel){if(!world.isRemote){world.setBlockMetadataWithNotify(x,y,z,m|LIT,3);held.damageItem(1,player);}return true;}
        if((m&LIT)!=0&&(held==null)){if(!world.isRemote){world.setBlockMetadataWithNotify(x,y,z,m&3,3);world.playSoundEffect(x+.5,y+.3,z+.5,"random.fizz",.3F,1.6F);}return true;}
        return false;
    }
    @Override public void randomDisplayTick(World world,int x,int y,int z,Random random){
        int metadata=world.getBlockMetadata(x,y,z);if((metadata&LIT)==0)return;
        int candles=count(metadata);for(int i=0;i<candles;i++){
            double[] flame=flame(i);
            if(random.nextInt(3)==0)world.spawnParticle("smoke",x+flame[0],y+flame[1]+.035,z+flame[2],0,0,0);
            world.spawnParticle("flame",x+flame[0],y+flame[1],z+flame[2],0,0,0);
        }
    }
    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister register){blockIcon=register.registerIcon("htgt6:1_20_backport/"+texture);}
    @SideOnly(Side.CLIENT) @Override public IIcon getIcon(int side,int metadata){return blockIcon;}
}
