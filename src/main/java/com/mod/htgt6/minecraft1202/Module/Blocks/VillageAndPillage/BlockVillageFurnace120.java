package com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.VillagePoiData120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.IVillagePoiBlock120;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Furnace inventory/cooking bridge used by blast furnace and smoker identities. */
public final class BlockVillageFurnace120 extends BlockContainer implements IVillagePoiBlock120 {
    private final String type;
    private final boolean smoker;
    public BlockVillageFurnace120(String name,String type,boolean smoker){super(Material.rock);this.type=type;this.smoker=smoker;
        setBlockName("village_pillage_"+name);setHardness(3.5F);setResistance(Backport120Content.legacyResistance(3.5F));
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);
        setBlockTextureName("htgt6:village_pillage/"+name+"_side");}
    @Override public TileEntity createNewTileEntity(World world,int metadata){return new TileEntityVillageFurnace120(smoker);}
    @Override public String getPoiType(){return type;}
    @Override public int getRenderType(){return VillagePillageContent.getJsonRenderId();}
    @Override public int getLightValue(net.minecraft.world.IBlockAccess world,int x,int y,int z){return (world.getBlockMetadata(x,y,z)&4)!=0?13:0;}
    @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public void randomDisplayTick(World world,int x,int y,int z,java.util.Random random){
        if((world.getBlockMetadata(x,y,z)&4)==0)return;
        if(smoker)world.spawnParticle("smoke",x+.5,y+1.05,z+.5,0,.02,0);
        else{int facing=world.getBlockMetadata(x,y,z)&3;double px=x+.5,pz=z+.5;
            if(facing==0)pz=z-.02;else if(facing==1)px=x+1.02;else if(facing==2)pz=z+1.02;else px=x-.02;
            world.spawnParticle("smoke",px,y+.25,pz,0,0,0);world.spawnParticle("flame",px,y+.25,pz,0,0,0);}
    }
    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase placer,ItemStack stack){
        world.setBlockMetadataWithNotify(x,y,z,MathHelper.floor_double(placer.rotationYaw*4F/360F+.5D)&3,2);
        VillagePoiData120.register(world,x,y,z,type);}
    @Override public void onBlockAdded(World world,int x,int y,int z){super.onBlockAdded(world,x,y,z);VillagePoiData120.register(world,x,y,z,type);}
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hitX,float hitY,float hitZ){
        if(!world.isRemote)player.openGui(com.mod.htgt6.minecraft1202.Minecraft1202.instance,36,world,x,y,z);return true;}
    @Override public void breakBlock(World world,int x,int y,int z,Block block,int metadata){VillagePoiData120.remove(world,x,y,z);
        net.minecraft.init.Blocks.furnace.breakBlock(world,x,y,z,block,metadata);}
    @Override public boolean hasComparatorInputOverride(){return true;}
    @Override public int getComparatorInputOverride(World world,int x,int y,int z,int side){
        return Container.calcRedstoneFromInventory((TileEntityVillageFurnace120)world.getTileEntity(x,y,z));}
}
