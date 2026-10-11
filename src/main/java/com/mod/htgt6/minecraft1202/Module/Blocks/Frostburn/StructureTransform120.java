package com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn;

import java.io.IOException;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;

/** Origin-pivot transforms. Legacy metadata requires explicit, block-specific adapters. */
public final class StructureTransform120 {
    private StructureTransform120() { }
    // Rotation: 0, clockwise 90, 180, counterclockwise 90. Mirror: none, Z, X.
    public static int[] position(int x,int z,int rotation,int mirror) {
        if(mirror==1)z=-z;else if(mirror==2)x=-x;
        switch(rotation) {
            case 1:return new int[]{-z,x};
            case 2:return new int[]{-x,-z};
            case 3:return new int[]{z,-x};
            default:return new int[]{x,z};
        }
    }
    public static int[] bounds(int sx,int sz,int rotation,int mirror) {
        int[] p=position(sx-1,sz-1,rotation,mirror);
        return new int[]{Math.min(0,p[0]),Math.min(0,p[1]),Math.max(0,p[0])+1,Math.max(0,p[1])+1};
    }
    private static int facing(int value,int[] order,int rotation,int mirror) throws IOException {
        // order is north, east, south, west.
        for(int i=0;i<4;i++)if(order[i]==value) {
            int[] p=position(new int[]{0,1,0,-1}[i],new int[]{-1,0,1,0}[i],rotation,mirror);
            return order[p[1]<0?0:p[0]>0?1:p[1]>0?2:3];
        }
        throw new IOException("Invalid directional metadata: "+value);
    }
    public static int metadata(Block b,int m,int r,int mirror) throws IOException {
        if(r==0&&mirror==0)return m;
        String id=Block.blockRegistry.getNameForObject(b);
        if(b==FrostburnContent.boneBlock)return axis(m,r);
        if(b==FrostburnContent.magmaBlock||b==FrostburnContent.netherWartBlock
                ||b==FrostburnContent.redNetherBrick||b==FrostburnContent.structureVoid)return m;
        // Mod subclasses may assign entirely different metadata meanings.
        if(id==null||!id.startsWith("minecraft:")||!b.getClass().getName().startsWith("net.minecraft.block."))
            throw new IOException("No structure transform adapter for "+id);
        if(b instanceof BlockStairs)return (m&4)|facing(m&3,new int[]{3,0,2,1},r,mirror);
        if(b instanceof BlockDoor) {
            if((m&8)!=0)return mirror==0?m:m^1;
            return (m&4)|facing(m&3,new int[]{3,0,1,2},r,mirror);
        }
        if(b instanceof BlockTrapDoor)return (m&12)|facing(m&3,new int[]{1,2,0,3},r,mirror);
        if(b instanceof BlockLog)return axis(m,r);
        if(b==Blocks.hay_block)return axis(m,r);
        if(b==Blocks.quartz_block)return (r&1)==0?m:m==3?4:m==4?3:m;
        if(b instanceof BlockChest||b instanceof BlockFurnace||b==Blocks.ladder||b==Blocks.wall_sign)
            return facing(m,new int[]{2,5,3,4},r,mirror);
        if(b==Blocks.standing_sign)return angle(m,r,mirror);
        if(b==Blocks.pumpkin||b==Blocks.lit_pumpkin||b instanceof BlockFenceGate||b instanceof BlockBed
                ||b instanceof BlockRedstoneRepeater||b instanceof BlockRedstoneComparator||b==Blocks.tripwire_hook)
            return (m&12)|facing(m&3,new int[]{2,3,0,1},r,mirror);
        if(b instanceof BlockDispenser||b instanceof BlockPistonBase||b instanceof BlockPistonExtension||b==Blocks.hopper) {
            int f=m&7;
            return f<2?m:(m&8)|facing(f,new int[]{2,5,3,4},r,mirror);
        }
        if(b==Blocks.torch||b==Blocks.redstone_torch||b==Blocks.unlit_redstone_torch||b instanceof BlockButton) {
            int f=m&7;
            return f==5||f==0?m:(m&8)|facing(f,new int[]{4,1,3,2},r,mirror);
        }
        // Explicit directionless families; other states fail before any writes.
        if(b==Blocks.air||b==Blocks.stone||b==Blocks.cobblestone||b==Blocks.mossy_cobblestone
                ||b==Blocks.bedrock||b==Blocks.dirt||b==Blocks.grass||b==Blocks.gravel||b==Blocks.sand
                ||b==Blocks.sandstone||b==Blocks.planks||b==Blocks.brick_block||b==Blocks.stonebrick
                ||b==Blocks.gold_block||b==Blocks.iron_block||b==Blocks.diamond_block||b==Blocks.emerald_block
                ||b==Blocks.lapis_block||b==Blocks.redstone_block||b==Blocks.coal_block||b==Blocks.obsidian
                ||b==Blocks.netherrack||b==Blocks.nether_brick||b==Blocks.soul_sand||b==Blocks.end_stone
                ||b==Blocks.glowstone||b==Blocks.glass||b==Blocks.stained_glass||b==Blocks.wool
                ||b==Blocks.hardened_clay||b==Blocks.stained_hardened_clay||b==Blocks.bookshelf
                ||b==Blocks.crafting_table||b==Blocks.enchanting_table||b==Blocks.mob_spawner
                ||b==Blocks.water||b==Blocks.flowing_water||b==Blocks.lava||b==Blocks.flowing_lava
                ||b instanceof BlockSlab||b instanceof BlockLeaves||b instanceof BlockPane
                ||b instanceof BlockFence||b instanceof BlockWall||b instanceof BlockCarpet
                ||b instanceof BlockOre)return m;
        throw new IOException("No structure transform adapter for "+id);
    }
    private static int axis(int m,int r) {
        int axis=m&12;
        return (r&1)==0?m:axis==4?(m&3)|8:axis==8?(m&3)|4:m;
    }
    private static int angle(int m,int r,int mirror) {
        if(mirror==1)m=8-m;else if(mirror==2)m=-m;
        return (m+r*4)&15;
    }
    public static void validateTile(Block block,NBTTagCompound nbt,int r,int mirror) throws IOException {
        if(r==0&&mirror==0)return;
        String id=nbt.getString("id");
        if((block instanceof BlockChest&&"Chest".equals(id))
                ||(block instanceof BlockFurnace&&"Furnace".equals(id))
                ||((block==Blocks.standing_sign||block==Blocks.wall_sign)&&"Sign".equals(id))
                ||(block==Blocks.dispenser&&"Trap".equals(id))||(block==Blocks.dropper&&"Dropper".equals(id))
                ||(block==Blocks.hopper&&"Hopper".equals(id))||(block==Blocks.enchanting_table&&"EnchantTable".equals(id)))return;
        throw new IOException("No structure NBT transform adapter for "+id);
    }
}
