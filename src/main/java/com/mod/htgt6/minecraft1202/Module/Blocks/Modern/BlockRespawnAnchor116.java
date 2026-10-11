package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.RespawnAnchor116;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import cpw.mods.fml.relauncher.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.IIcon;
import net.minecraft.world.*;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;

public final class BlockRespawnAnchor116 extends Block {
    @SideOnly(Side.CLIENT) private IIcon[] sides;
    @SideOnly(Side.CLIENT) private IIcon top,off,bottom;
    public BlockRespawnAnchor116(){super(Material.rock);setBlockName("backport_1_20.respawn_anchor");setHardness(50);setResistance(Backport120Content.legacyResistance(1200));setHarvestLevel("pickaxe",3);setStepSound(soundTypeStone);setCreativeTab(CreativeTabs.tabBlock);}
    @Override public int getRenderType(){int id=Backport120Content.getJsonRenderId();return id>=0?id:0;}
    public static int signal(int charge){return Math.max(0,Math.min(4,charge))*15/4;}
    @Override public int getLightValue(IBlockAccess w,int x,int y,int z){return signal(w.getBlockMetadata(x,y,z));}
    @Override public boolean hasComparatorInputOverride(){return true;}
    @Override public int getComparatorInputOverride(World w,int x,int y,int z,int side){return signal(w.getBlockMetadata(x,y,z));}
    public boolean charge(World w,int x,int y,int z){int c=w.getBlockMetadata(x,y,z);if(w.getBlock(x,y,z)!=this||c>=4)return false;if(!w.isRemote){if(!w.setBlock(x,y,z,this,c+1,3))return false;w.playSoundEffect(x+.5,y+.5,z+.5,"htgt6:block.respawn_anchor.charge",1,1);}return true;}
    private static boolean glowstone(ItemStack s){return s!=null&&s.stackSize>0&&s.getItem()==Item.getItemFromBlock(Blocks.glowstone);}
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float hx,float hy,float hz){
        ItemStack held=p.getHeldItem();boolean offhand=false;
        if(!glowstone(held)){ItemStack other=OffhandHooks.getOffhand(p);if(glowstone(other)){held=other;offhand=true;}}
        if(!w.canMineBlock(p,x,y,z)||!p.canPlayerEdit(x,y,z,side,held))return true;
        if(glowstone(held)&&w.getBlockMetadata(x,y,z)<4){
            if(!charge(w,x,y,z))return true;
            if(!w.isRemote&&w.getBlockMetadata(x,y,z)==4)com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.complete(p,"charge_respawn_anchor");
            if(!w.isRemote&&!p.capabilities.isCreativeMode){if(--held.stackSize==0){if(offhand)OffhandHooks.setOffhand(p,null);else p.inventory.setInventorySlotContents(p.inventory.currentItem,null);}p.inventory.markDirty();p.inventoryContainer.detectAndSendChanges();}return true;
        }
        if(w.getBlockMetadata(x,y,z)==0)return false;
        if(!w.isRemote){if(RespawnAnchor116.works(w)){if(RespawnAnchor116.bind(p,x,y,z))w.playSoundEffect(x+.5,y+.5,z+.5,"htgt6:block.respawn_anchor.set_spawn",1,1);}
            else{com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.AnchorExplosion116.explode(w,x,y,z);}}
        return true;
    }
    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister r){sides=new IIcon[5];for(int i=0;i<5;i++)sides[i]=r.registerIcon("htgt6:1_20_backport/respawn_anchor_side"+i);top=r.registerIcon("htgt6:1_20_backport/respawn_anchor_top");off=r.registerIcon("htgt6:1_20_backport/respawn_anchor_top_off");bottom=r.registerIcon("htgt6:1_20_backport/respawn_anchor_bottom");blockIcon=sides[0];}
    @SideOnly(Side.CLIENT) @Override public IIcon getIcon(int side,int meta){return side==0?bottom:side==1?(meta>0?top:off):sides[Math.max(0,Math.min(4,meta))];}
    @SideOnly(Side.CLIENT) @Override public void randomDisplayTick(World w,int x,int y,int z,java.util.Random r){if(w.getBlockMetadata(x,y,z)>0){if(r.nextInt(100)==0)w.playSound(x+.5,y+.5,z+.5,"htgt6:block.respawn_anchor.ambient",1,1,false);com.mod.htgt6.minecraft1202.Module.Client.Modern.ReversePortal116.spawn(w,x+r.nextDouble(),y+1,z+r.nextDouble(),r.nextFloat()*.04);}}
}
