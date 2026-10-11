package com.mod.htgt6.minecraft1202.Module.Blocks.Exploration;

import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public final class ShulkerBoxShape112 {
    private ShulkerBoxShape112() { }
    // Preserve upright boxes from old worlds: metadata zero has always meant UP.
    public static int metadata(int side){return side==0?1:side==1?0:side>=2&&side<=5?side:0;}
    public static ForgeDirection direction(int meta){return ForgeDirection.getOrientation(meta==0?1:meta==1?0:meta>=2&&meta<=5?meta:1);}
    public static AxisAlignedBB bounds(int meta,double extent) {
        ForgeDirection d=direction(meta);
        return AxisAlignedBB.getBoundingBox(Math.min(0,d.offsetX*extent),Math.min(0,d.offsetY*extent),Math.min(0,d.offsetZ*extent),
                1+Math.max(0,d.offsetX*extent),1+Math.max(0,d.offsetY*extent),1+Math.max(0,d.offsetZ*extent));
    }
    public static AxisAlignedBB sweep(int meta,double from,double to) {
        ForgeDirection d=direction(meta);double low=Math.min(from,to),high=Math.max(from,to);
        return AxisAlignedBB.getBoundingBox(d.offsetX<0?-high:d.offsetX>0?1+low:0,
                d.offsetY<0?-high:d.offsetY>0?1+low:0,d.offsetZ<0?-high:d.offsetZ>0?1+low:0,
                d.offsetX<0?-low:d.offsetX>0?1+high:1,d.offsetY<0?-low:d.offsetY>0?1+high:1,
                d.offsetZ<0?-low:d.offsetZ>0?1+high:1);
    }
}
