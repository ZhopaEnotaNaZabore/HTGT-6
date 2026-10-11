package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.IIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.LodestoneBinding116;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.LodestoneCompassClient116;

public final class ItemLodestoneCompass116 extends Item {
    // No client initializer or mutable target in this common singleton.
    @SideOnly(Side.CLIENT) private IIcon[] frames;
    public ItemLodestoneCompass116(){
        setUnlocalizedName("backport_1_20.lodestone_compass");setTextureName("htgt6:1_20_backport/compass_16");setCreativeTab(CreativeTabs.tabTools);
    }
    @Override public void onUpdate(ItemStack stack,World world,Entity holder,int slot,boolean selected){
        if(LodestoneBinding116.validate(stack,world)&&holder instanceof EntityPlayer)((EntityPlayer)holder).inventory.markDirty();
    }
    @Override public boolean onEntityItemUpdate(EntityItem entity){
        ItemStack stack=entity.getEntityItem();
        if(LodestoneBinding116.validate(stack,entity.worldObj))entity.setEntityItemStack(stack.copy());
        return false; // preserve all vanilla motion, merge, fire, pickup and despawn behavior
    }
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,float hx,float hy,float hz){
        return LodestoneBinding116.bind(player,world,x,y,z,side);
    }
    @Override public boolean doesSneakBypassUse(World world,int x,int y,int z,EntityPlayer player){return true;}
    @Override public boolean getShareTag(){return true;}
    @Override @SideOnly(Side.CLIENT) public boolean hasEffect(ItemStack stack,int pass){return true;}
    @Override @SideOnly(Side.CLIENT) public void registerIcons(IIconRegister register){
        frames=new IIcon[32];for(int i=0;i<32;i++)frames[i]=register.registerIcon(String.format(java.util.Locale.ROOT,"htgt6:1_20_backport/compass_%02d",i));itemIcon=frames[16];
    }
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(ItemStack stack,int pass){return icon(stack,null);}
    @Override @SideOnly(Side.CLIENT) public IIcon getIconIndex(ItemStack stack){return icon(stack,null);}
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(ItemStack stack,int pass,EntityPlayer player,ItemStack using,int remaining){return icon(stack,player);}
    @SideOnly(Side.CLIENT) private IIcon icon(ItemStack stack,Object holder){
        if(frames==null)return itemIcon;
        double angle=LodestoneCompassClient116.angle(stack,holder);
        return frames[com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.LodestoneMath116.textureFrame(angle)];
    }
}
