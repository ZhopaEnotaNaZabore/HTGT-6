package com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.DoorPower120;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import java.util.Random;

/** Uses native door face mirroring with this wood's texture pair. */
public final class BlockBountifulWoodDoor extends BlockDoor {
    private Item doorItem;

    public BlockBountifulWoodDoor(String wood) {
        super(Material.wood);
        setBlockName(wood+"_door_block");
        setBlockTextureName("htgt6:bountiful/door_"+("big_oak".equals(wood)?"dark_oak":wood));
        setHardness(3);
        setResistance(Backport120Content.legacyResistance(3.0F));
        setStepSound(soundTypeWood);
        disableStats();
    }
    public void setDoorItem(Item item) { doorItem=item; }
    @Override public boolean onBlockActivated(World world,int x,int y,int z,net.minecraft.entity.player.EntityPlayer player,
                                              int side,float hitX,float hitY,float hitZ) {
        return com.mod.htgt6.minecraft1202.Module.Blocks.Modern.DoorSound120.toggle(this,"wooden",world,x,y,z);
    }
    @Override public void func_150014_a(World world,int x,int y,int z,boolean open) {
        com.mod.htgt6.minecraft1202.Module.Blocks.Modern.DoorSound120.setOpen(this,"wooden",world,x,y,z,open);
    }
    @Override public Item getItemDropped(int meta,Random random,int fortune) {
        return (meta&8)!=0?null:doorItem;
    }
    @Override public Item getItem(World world,int x,int y,int z) { return doorItem; }
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block changed) {
        if(!DoorPower120.neighbor(this,world,x,y,z,changed))super.onNeighborBlockChange(world,x,y,z,changed);
    }
}
