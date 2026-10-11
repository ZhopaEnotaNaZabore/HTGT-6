package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.*;
import net.minecraftforge.common.util.ForgeDirection;
import java.util.*;

/** Two head identities retain all 26 ages without per-block tile entities. */
public final class BlockNetherVine116 extends Block implements IGrowable,net.minecraftforge.common.IShearable {
    public final int direction,phase;
    public BlockNetherVine116(String name,int direction,int phase){
        super(Material.vine);this.direction=direction;this.phase=phase;setBlockName("backport_1_20."+name);setBlockTextureName("htgt6:1_20_backport/"+(direction>0?"twisting_vines":"weeping_vines")+(phase==2?"_plant":""));
        setHardness(0);setStepSound(new com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.FrostburnSound120(direction>0?"twisting_vines":"weeping_vines"));
        setTickRandomly(phase!=2);if(phase==0)setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);
        setBlockBounds(phase==2?.0625F:.25F,phase==2||direction>0?0:9F/16,phase==2?.0625F:.25F,phase==2?.9375F:.75F,phase==2||direction<0?1:15F/16,phase==2?.9375F:.75F);
    }
    public BlockNetherVine116[] family(){return direction>0?NetherVegetationContent116.twisting:NetherVegetationContent116.weeping;}
    public boolean same(Block b){return b instanceof BlockNetherVine116&&((BlockNetherVine116)b).direction==direction;}
    public int age(int meta){return Math.min(25,meta+(phase==1?16:0));}
    public boolean placeAge(World w,int x,int y,int z,int age){age=Math.max(0,Math.min(25,age));return w.setBlock(x,y,z,family()[age>=16?1:0],age&15,3);}
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
    @Override public boolean isOpaqueCube(){return false;}
    @Override public boolean renderAsNormalBlock(){return false;}
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World w,int x,int y,int z){return null;}
    @Override public boolean isLadder(IBlockAccess w,int x,int y,int z,EntityLivingBase e){return true;}
    private boolean supported(World w,int x,int y,int z){
        if(!w.blockExists(x,y-direction,z))return true;
        Block support=w.getBlock(x,y-direction,z);
        return same(support)||support.isSideSolid(w,x,y-direction,z,direction>0?ForgeDirection.UP:ForgeDirection.DOWN);
    }
    @Override public boolean canPlaceBlockAt(World w,int x,int y,int z){return super.canPlaceBlockAt(w,x,y,z)&&supported(w,x,y,z);}
    @Override public void onBlockPlacedBy(World w,int x,int y,int z,EntityLivingBase e,ItemStack stack){if(!w.isRemote){if(same(w.getBlock(x,y+direction,z)))w.setBlock(x,y,z,family()[2],0,3);else placeAge(w,x,y,z,w.rand.nextInt(25));}}
    @Override public void onNeighborBlockChange(World w,int x,int y,int z,Block neighbor){
        if(w.isRemote)return;
        if(!supported(w,x,y,z)){w.scheduleBlockUpdate(x,y,z,this,1);return;}
        boolean next=same(w.getBlock(x,y+direction,z));
        if(phase==2&&!next)placeAge(w,x,y,z,w.rand.nextInt(25));
        else if(phase!=2&&next)w.setBlock(x,y,z,family()[2],0,3);
    }
    @Override public void updateTick(World w,int x,int y,int z,Random r){
        if(w.isRemote)return;if(!supported(w,x,y,z)){dropBlockAsItem(w,x,y,z,w.getBlockMetadata(x,y,z),0);w.setBlockToAir(x,y,z);return;}
        int age=age(w.getBlockMetadata(x,y,z));if(phase!=2&&age<25&&r.nextDouble()<.1&&canExtend(w,x,y,z))placeAge(w,x,y+direction,z,age+1);
    }
    private boolean canExtend(World w,int x,int y,int z){int next=y+direction;return next>=0&&next<com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.height(w)&&w.blockExists(x,next,z)&&w.isAirBlock(x,next,z);}
    private int tip(World w,int x,int y,int z){for(int i=0;i<384;i++,y+=direction){if(!w.blockExists(x,y,z))return -1;Block b=w.getBlock(x,y,z);if(!same(b))return -1;if(((BlockNetherVine116)b).phase!=2)return y;}return -1;}
    @Override public boolean func_149851_a(World w,int x,int y,int z,boolean client){int end=tip(w,x,y,z);return end>=0&&canExtend(w,x,end,z);}
    @Override public boolean func_149852_a(World w,Random r,int x,int y,int z){return true;}
    @Override public void func_149853_b(World w,Random r,int x,int y,int z){
        if(w.isRemote)return;y=tip(w,x,y,z);if(y<0)return;int age=((BlockNetherVine116)w.getBlock(x,y,z)).age(w.getBlockMetadata(x,y,z));
        int count=0;for(double chance=1;r.nextDouble()<chance&&count<384;chance*=.826)count++;
        for(int i=0;i<count&&canExtend(w,x,y,z);i++){age=Math.min(25,age+1);if(!placeAge(w,x,y+direction,z,age))break;y+=direction;}
    }
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float hx,float hy,float hz){
        ItemStack held=p.getHeldItem();if(phase==2||age(w.getBlockMetadata(x,y,z))>=25||held==null||held.getItem()!=Items.shears||!p.canPlayerEdit(x,y,z,side,held))return false;
        if(!w.isRemote&&placeAge(w,x,y,z,25)){if(!p.capabilities.isCreativeMode)held.damageItem(1,p);w.playSoundEffect(x+.5,y+.5,z+.5,"htgt6:block.growing_plant.crop",1,1);}return true;
    }
    @Override public Item getItemDropped(int m,Random r,int fortune){return r.nextFloat()<(fortune>=3?1F:fortune==2?.77F:fortune==1?.55F:.33F)?Item.getItemFromBlock(family()[0]):null;}
    @Override public int damageDropped(int m){return 0;}
    @Override public Item getItem(World w,int x,int y,int z){return Item.getItemFromBlock(family()[0]);}
    @Override protected boolean canSilkHarvest(){return true;}
    @Override protected ItemStack createStackedBlock(int meta){return new ItemStack(family()[0]);}
    @Override public boolean isShearable(ItemStack tool,IBlockAccess w,int x,int y,int z){return true;}
    @Override public ArrayList<ItemStack> onSheared(ItemStack tool,IBlockAccess w,int x,int y,int z,int fortune){return new ArrayList<ItemStack>(Collections.singletonList(new ItemStack(family()[0])));}
}
