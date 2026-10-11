package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Sign;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeRegistry120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.WorldGenFuturaVanillaTree120;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/** Startup contract for tree decorator ownership and hanging-sign save/registry wiring. */
public final class Backport120TreeSignWiringFixture {
    public static void assertContract(){
        block("backport_1_20_cherry_hanging_sign_block",Backport120Content.cherryHangingSignBlock);
        block("backport_1_20_cherry_wall_hanging_sign",Backport120Content.cherryWallHangingSignBlock);
        item("backport_1_20_cherry_hanging_sign",Backport120Content.cherryHangingSign);
        require(!Backport120Content.cherryHangingSignBlock.isWallSign(),"Ceiling hanging sign flagged as wall");
        require(Backport120Content.cherryWallHangingSignBlock.isWallSign(),"Wall hanging sign lost wall flag");
        require(Backport120Content.cherryHangingSignBlock.getItemDropped(0,null,0)
                        ==Backport120Content.cherryHangingSign,"Ceiling sign drop is not its public item");
        require(Backport120Content.cherryWallHangingSignBlock.getItemDropped(0,null,0)
                        ==Backport120Content.cherryHangingSign,"Wall sign drop is not its public item");
        require(Backport120Content.cherryHangingSignBlock.createNewTileEntity(null,15)
                        instanceof TileEntityBackport120Sign,"Hanging sign TE factory mismatch");
        verifyNbtRoundTrip();

        // Cherry/mangrove are owned by ChunkProviderFutura's biome decorator;
        // the terrain-gen TREE hook must skip them to avoid duplicate trees.
        require(WorldGenFuturaVanillaTree120.shapeFor(
                FuturaBiomeRegistry120.get(FuturaBiomeIds120.CHERRY_GROVE))
                ==WorldGenFuturaVanillaTree120.Shape.NONE,"Cherry tree decorators overlap");
        require(WorldGenFuturaVanillaTree120.shapeFor(
                FuturaBiomeRegistry120.get(FuturaBiomeIds120.MANGROVE_SWAMP))
                ==WorldGenFuturaVanillaTree120.Shape.NONE,"Mangrove tree decorators overlap");
    }

    private static void verifyNbtRoundTrip(){
        TileEntityBackport120Sign sign=new TileEntityBackport120Sign();sign.signText[0]="fixture";
        NBTTagCompound tag=new NBTTagCompound();sign.writeToNBT(tag);
        require("htgt6:backport_1_20_sign".equals(tag.getString("id")),"Hanging sign TE save id changed");
        TileEntity loaded=TileEntity.createAndLoadEntity(tag);
        require(loaded instanceof TileEntityBackport120Sign,"Hanging sign TE NBT mapping is not loadable");
        require("fixture".equals(((TileEntityBackport120Sign)loaded).signText[0]),"Sign text NBT changed");
    }

    private static void block(String path,Block expected){Object key=Block.blockRegistry.getNameForObject(expected);
        require(key!=null&&key.toString().endsWith(":"+path),"Missing block registration "+path+": "+key);}
    private static void item(String path,Item expected){Object key=Item.itemRegistry.getNameForObject(expected);
        require(key!=null&&key.toString().endsWith(":"+path),"Missing item registration "+path+": "+key);}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private Backport120TreeSignWiringFixture(){ }
}
