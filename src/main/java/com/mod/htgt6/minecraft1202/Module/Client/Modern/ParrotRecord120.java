package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityParrot120;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public final class ParrotRecord120 {
    public static void playRecord(String record,int x,int y,int z){
        update(Minecraft.getMinecraft().theWorld,record,x,y,z);
    }
    public static void update(World world,String record,int x,int y,int z){
        if(world==null||!world.isRemote)return;
        AxisAlignedBB box=AxisAlignedBB.getBoundingBox(x,y,z,x+1,y+1,z+1).expand(3.46,3.46,3.46);
        for(Object value:world.getEntitiesWithinAABB(EntityParrot120.class,box)){
            EntityParrot120 parrot=(EntityParrot120)value;
            parrot.setRecordPlayingNearby(x,y,z,record!=null);
            parrot.updatePartyState();
        }
    }
    private ParrotRecord120(){ }
}
