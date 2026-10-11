package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.world.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import java.util.*;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityBee115;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockCampfire149;

public final class BlockBeehive115 extends BlockContainer {
    public final boolean nest;
    public BlockBeehive115(boolean natural){super(Material.wood);nest=natural;setBlockName("backport_1_20."+(nest?"bee_nest":"beehive"));setBlockTextureName("htgt6:1_20_backport/"+(nest?"bee_nest":"beehive")+"_side");setHardness(nest?.3F:.6F);setStepSound(soundTypeWood);setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);}
    @Override public TileEntity createNewTileEntity(World world,int meta){return new TileEntityBeehive115();}
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
    public static int side(int meta){return new int[]{2,5,3,4}[meta&3];}
    public static TileEntityBeehive115 tile(IBlockAccess w,int x,int y,int z){TileEntity t=w.getTileEntity(x,y,z);return t instanceof TileEntityBeehive115?(TileEntityBeehive115)t:null;}
    @Override public void onBlockPlacedBy(World w,int x,int y,int z,EntityLivingBase e,ItemStack stack){
        w.setBlockMetadataWithNotify(x,y,z,MathHelper.floor_double(e.rotationYaw*4F/360F+.5F)&3,3);
        TileEntityBeehive115 t=tile(w,x,y,z);if(!w.isRemote&&t!=null&&stack.hasTagCompound()&&stack.getTagCompound().hasKey("BlockEntityTag",10))t.restore(stack.getTagCompound().getCompoundTag("BlockEntityTag"));
    }
    @Override public boolean hasComparatorInputOverride(){return true;}
    @Override public int getComparatorInputOverride(World w,int x,int y,int z,int side){TileEntityBeehive115 t=tile(w,x,y,z);return t==null?0:t.honey();}
    public static boolean smoked(World w,int x,int y,int z){
        for(int d=1;d<=5&&y-d>=0;d++){
            if(BlockCampfire149.isSmokeSource(w,x,y-d,z))return true;
            Block b=w.getBlock(x,y-d,z);List<AxisAlignedBB> shapes=new ArrayList<AxisAlignedBB>();
            b.addCollisionBoxesToList(w,x,y-d,z,AxisAlignedBB.getBoundingBox(x+.375,y-d,z+.375,x+.625,y-d+1,z+.625),shapes,null);
            if(!shapes.isEmpty())return y-d>0&&BlockCampfire149.isSmokeSource(w,x,y-d-1,z);
        }return false;
    }
    public static void anger(World w,int x,int y,int z,EntityPlayer player){
        if(player==null||player.capabilities.isCreativeMode)return;
        for(Object raw:w.getEntitiesWithinAABB(EntityBee115.class,AxisAlignedBB.getBoundingBox(x-8,y-6,z-8,x+9,y+7,z+9)))((EntityBee115)raw).angerAt(player);
        TileEntityBeehive115 t=tile(w,x,y,z);if(t!=null)t.releaseAll(player);
    }
    /** Caller owns item consumption; this method changes the hive exactly once. */
    public static ItemStack harvest(World w,int x,int y,int z,boolean shears,EntityPlayer player){
        TileEntityBeehive115 t=tile(w,x,y,z);if(w.isRemote||t==null||t.honey()!=5)return null;
        t.setHoney(0);w.playSoundEffect(x+.5,y+.5,z+.5,"htgt6:block.beehive."+(shears?"shear":"drip"),1,1);
        boolean safe=smoked(w,x,y,z);if(!safe)anger(w,x,y,z,player);
        if(safe&&player!=null&&!shears)com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.complete(player,"bee_our_guest");
        return new ItemStack(shears?EtFVanillaGapContent120.honeycomb:EtFVanillaGapContent120.honeyBottle,shears?3:1);
    }
    @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float a,float b,float c){
        ItemStack held=p.getHeldItem();if(held==null||!p.canPlayerEdit(x,y,z,side,held))return false;
        boolean shear=held.getItem()==Items.shears;if(!shear&&held.getItem()!=Items.glass_bottle)return false;
        TileEntityBeehive115 t=tile(w,x,y,z);if(t==null||t.honey()!=5)return false;if(w.isRemote)return true;
        ItemStack result=harvest(w,x,y,z,shear,p);if(result==null)return false;
        if(shear){if(!p.capabilities.isCreativeMode)held.damageItem(1,p);dropBlockAsItem(w,x,y,z,result);}
        else {
            if(!p.capabilities.isCreativeMode&&--held.stackSize==0)p.inventory.setInventorySlotContents(p.inventory.currentItem,result);
            else if(!p.inventory.addItemStackToInventory(result))p.dropPlayerItemWithRandomChoice(result,false);
        }return true;
    }
    @Override public boolean removedByPlayer(World w,EntityPlayer p,int x,int y,int z,boolean harvest){return harvest||super.removedByPlayer(w,p,x,y,z,harvest);}
    @Override public void harvestBlock(World w,EntityPlayer p,int x,int y,int z,int meta){
        if(w.isRemote)return;TileEntityBeehive115 t=tile(w,x,y,z);boolean silk=EnchantmentHelper.getSilkTouchModifier(p);
        NBTTagCompound snapshot=t==null?null:t.payload();net.minecraft.entity.item.EntityItem item=null;
        if((silk||!nest)&&w.getGameRules().getGameRuleBooleanValue("doTileDrops")){
            ItemStack drop=new ItemStack(this);
            if(silk&&t!=null){NBTTagCompound n=new NBTTagCompound();n.setTag("BlockEntityTag",snapshot.copy());drop.setTagCompound(n);}
            item=new net.minecraft.entity.item.EntityItem(w,x+.5,y+.5,z+.5,drop);item.delayBeforeCanPickup=10;
            if(!w.spawnEntityInWorld(item)){w.markBlockForUpdate(x,y,z);return;}
            if(silk&&t!=null)t.clearStored();
        }
        if(!silk&&!smoked(w,x,y,z))anger(w,x,y,z,p);
        if(!w.setBlockToAir(x,y,z)){
            if(item!=null)item.setDead();if(silk&&t!=null&&snapshot!=null)t.restore(snapshot);w.markBlockForUpdate(x,y,z);return;
        }
        if(silk&&item!=null&&nest&&snapshot!=null&&snapshot.getTagList("Bees",10).tagCount()==3)
            com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.complete(p,"total_beelocation");
    }

    @Override public void breakBlock(World w,int x,int y,int z,Block old,int meta){TileEntityBeehive115 t=tile(w,x,y,z);if(t!=null){t.releaseAll(null);if(t.occupants()>0&&!w.isRemote){com.mod.htgt6.minecraft1202.Module.World.Modern.BeeRecovery115.save(w,x,y,z,t.payload());t.clearStored();}}super.breakBlock(w,x,y,z,old,meta);}
    @Override public Item getItemDropped(int meta,Random random,int fortune){return nest?null:Item.getItemFromBlock(this);}
}
