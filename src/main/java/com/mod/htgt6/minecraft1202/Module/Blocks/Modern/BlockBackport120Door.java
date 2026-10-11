package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.Random;

/** Two-block wooden door whose item and modern model state are supplied by the backport module. */
public final class BlockBackport120Door extends BlockDoor {
    private Item doorItem;
    private final String textureBase;
    @SideOnly(Side.CLIENT) private IIcon lowerIcon;
    @SideOnly(Side.CLIENT) private IIcon upperIcon;

    public BlockBackport120Door(String name) {
        super(com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherWoodMaterial116.forName(name));
        textureBase = name;
        setBlockName("backport_1_20." + name + "_block");
        setHardness(3.0F);
        setResistance(Backport120Content.legacyResistance(3.0F));
        setStepSound(Block.soundTypeWood);
        disableStats();
    }

    public void setDoorItem(Item item) { doorItem = item; }
    private String soundFamily() {
        return com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherWoodMaterial116.matches(textureBase)?"nether_wood":textureBase.startsWith("cherry_")?"cherry_wood":textureBase.startsWith("bamboo_")?"bamboo_wood":"wooden";
    }
    @Override public boolean onBlockActivated(World world,int x,int y,int z,net.minecraft.entity.player.EntityPlayer player,
                                              int side,float hitX,float hitY,float hitZ) {
        return DoorSound120.toggle(this,soundFamily(),world,x,y,z);
    }
    @Override public void func_150014_a(World world,int x,int y,int z,boolean open) {
        DoorSound120.setOpen(this,soundFamily(),world,x,y,z,open);
    }
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block changed) {
        if(!DoorPower120.neighbor(this,world,x,y,z,changed))super.onNeighborBlockChange(world,x,y,z,changed);
    }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) {
        return (metadata & 8) != 0 ? null : doorItem;
    }
    @Override public Item getItem(World world, int x, int y, int z) { return doorItem; }
    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @SideOnly(Side.CLIENT)
    @Override public void registerBlockIcons(IIconRegister register) {
        lowerIcon = register.registerIcon("htgt6:1_20_backport/" + textureBase + "_bottom");
        upperIcon = register.registerIcon("htgt6:1_20_backport/" + textureBase + "_top");
        blockIcon = lowerIcon;
    }
    @SideOnly(Side.CLIENT)
    @Override public IIcon getIcon(int side, int metadata) {
        return (metadata & 8) != 0 ? upperIcon : lowerIcon;
    }
}
