package com.mod.htgt6.minecraft1202.Module.Items.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.TileEntityBountifulBanner;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Bountiful.BountifulBannerColors;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;

import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

/** Sixteen-colour 1.8 banner item with NBT-preserving placement. */
public final class ItemBountifulBanner extends Item {
    private IIcon base,overlay;
    public ItemBountifulBanner(){setUnlocalizedName("bountiful_banner");setHasSubtypes(true);setMaxDamage(0);setMaxStackSize(16);setCreativeTab(CreativeTabs.tabDecorations);}
    @Override public String getItemStackDisplayName(ItemStack stack){
        if(stack.hasTagCompound()&&stack.getTagCompound().getBoolean("HTGT6OminousBanner"))return StatCollector.translateToLocal("block.minecraft.ominous_banner");
        return super.getItemStackDisplayName(stack);
    }
    @Override public void registerIcons(IIconRegister register){base=register.registerIcon("htgt6:bountiful/banner_base");overlay=register.registerIcon("htgt6:bountiful/banner_overlay");itemIcon=base;}
    @Override public boolean requiresMultipleRenderPasses(){return true;} @Override public int getRenderPasses(int damage){return 2;} @Override public IIcon getIconFromDamageForRenderPass(int damage,int pass){return pass==0?base:overlay;}
    @Override public int getColorFromItemStack(ItemStack stack,int pass){return pass==1?BountifulBannerColors.RGB[stack.getItemDamage()&15]:0xFFFFFF;}
    @Override public String getUnlocalizedName(ItemStack stack){return super.getUnlocalizedName()+"."+(stack.getItemDamage()&15);}
    @Override @SuppressWarnings({"rawtypes","unchecked"}) public void getSubItems(Item item,CreativeTabs tab,List list){for(int color=0;color<16;color++)list.add(new ItemStack(item,1,color));}
    @Override @SuppressWarnings({"rawtypes","unchecked"}) public void addInformation(ItemStack stack,EntityPlayer player,List lines,boolean advanced){
        if(!stack.hasTagCompound()||!stack.getTagCompound().hasKey("BlockEntityTag",10)||stack.getTagCompound().getBoolean("HTGT6OminousBanner"))return;
        NBTTagCompound block=stack.getTagCompound().getCompoundTag("BlockEntityTag");
        NBTTagList patterns=block.getTagList("Patterns",10);
        for(int i=0;i<patterns.tagCount()&&i<6;i++){
            String code=patterns.getCompoundTagAt(i).getString("Pattern");
            String key="loom.pattern."+code,translated=StatCollector.translateToLocal(key);
            lines.add(EnumChatFormatting.GRAY+(key.equals(translated)?code:translated));
        }
    }
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,float hitX,float hitY,float hitZ){if(side==0)return false;int px=x,py=y,pz=z;x+=net.minecraft.util.Facing.offsetsXForSide[side];y+=net.minecraft.util.Facing.offsetsYForSide[side];z+=net.minecraft.util.Facing.offsetsZForSide[side];if(!player.canPlayerEdit(x,y,z,side,stack)||!world.isAirBlock(x,y,z))return false;net.minecraft.block.Block block=side==1?BountifulContent.standingBanner:BountifulContent.wallBanner;int meta=side==1?net.minecraft.util.MathHelper.floor_double((player.rotationYaw+180F)*16F/360F+0.5D)&15:side;if(world.isRemote)return true;if(!world.setBlock(x,y,z,block,meta,3))return false;TileEntityBountifulBanner tile=(TileEntityBountifulBanner)world.getTileEntity(x,y,z);if(tile!=null)tile.readFromItem(stack);if(!player.capabilities.isCreativeMode)--stack.stackSize;return true;}
}
