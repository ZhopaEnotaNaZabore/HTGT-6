package com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/** Dry/wet sponge with bounded vanilla-style water absorption. */
public final class BlockBountifulSponge extends Block {
    private IIcon dry, wet;
    public BlockBountifulSponge() {
        super(Material.sponge); setBlockName("bountiful_sponge"); setHardness(0.6F);
        setResistance(Backport120Content.legacyResistance(0.6F));
        setStepSound(soundTypeGrass); setCreativeTab(CreativeTabs.tabBlock);
    }
    @Override public int damageDropped(int metadata) { return metadata; }
    @Override public IIcon getIcon(int side, int metadata) { return metadata == 1 ? wet : dry; }
    @Override public void onBlockAdded(World world, int x, int y, int z) {
        if(!world.isRemote&&world.getBlockMetadata(x,y,z)==1&&world.provider.isHellWorld){
            world.setBlockMetadataWithNotify(x,y,z,0,2);world.playSoundEffect(x+.5D,y+.5D,z+.5D,
                    "random.fizz",1.0F,2.6F+(world.rand.nextFloat()-world.rand.nextFloat())*.8F);return;}
        absorb(world, x, y, z);
    }
    @Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) { absorb(world, x, y, z); }
    private void absorb(World world, int x, int y, int z) {
        if (world.isRemote || world.getBlockMetadata(x, y, z) == 1) return;
        Queue<int[]> queue = new ArrayDeque<int[]>(); Set<Long> seen = new HashSet<Long>();
        java.util.List<int[]> changed = new java.util.ArrayList<int[]>();
        queue.add(new int[]{x, y, z, 0}); int removed = 0;
        while (!queue.isEmpty() && removed < 64) {
            int[] p = queue.remove(); if (p[3] >= 6) continue;
            for (int[] d : DIRS) { int nx=p[0]+d[0], ny=p[1]+d[1], nz=p[2]+d[2];
                if(removed>=64)break;
                long key=((long)(nx&0x3ffffff)<<38)^((long)(nz&0x3ffffff)<<12)^(ny&0xfff);
                if(!seen.add(key))continue; Block block=world.getBlock(nx,ny,nz);
                if(block.getMaterial()==Material.water){
                    int meta=world.getBlockMetadata(nx,ny,nz);
                    if(world.setBlock(nx,ny,nz,Blocks.air,0,2)){
                        if(!(block instanceof net.minecraft.block.BlockLiquid)&&block!=Backport120Content.aquiferWater)
                            block.dropBlockAsItem(world,nx,ny,nz,meta,0);
                        removed++;queue.add(new int[]{nx,ny,nz,p[3]+1});changed.add(new int[]{nx,ny,nz});
                    }
                }
            }
        }
        if (removed > 0) {
            // Become wet before notifying neighbours: a water update must not re-enter a dry sponge.
            world.setBlockMetadataWithNotify(x,y,z,1,2);
            for(int[] p:changed)world.notifyBlocksOfNeighborChange(p[0],p[1],p[2],Blocks.air);
        }
    }
    private static final int[][] DIRS={{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
    @Override public void getSubBlocks(Item item, CreativeTabs tab, List list) { list.add(new ItemStack(item,1,0));list.add(new ItemStack(item,1,1)); }
    @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister r){dry=r.registerIcon("htgt6:bountiful/sponge");wet=r.registerIcon("htgt6:bountiful/sponge_wet");}
}
