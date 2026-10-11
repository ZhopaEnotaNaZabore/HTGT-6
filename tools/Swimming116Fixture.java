package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.PlayerSwimming120;
import com.mod.htgt6.minecraft1202.Module.Network.Aquatic.PacketSwimming120;
import net.minecraft.world.World;
import io.netty.buffer.*;
import java.lang.reflect.Field;

public final class Swimming116Fixture {
    static int checks;
    public static void main(String[] args)throws Exception {
        BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
        Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));world.provider.worldObj=world;
        Swimming115Fixture.Player player=new Swimming115Fixture.Player(world);player.wet=true;player.eyes=true;player.setSprinting(true);player.motionY=.123;
        PlayerSwimming120.updateState(player);check(PlayerSwimming120.isSwimming(player),"server tick can enter without travel");check(player.motionY==.123,"server state update does not add motion");
        player.setSprinting(false);PlayerSwimming120.updateState(player);check(!PlayerSwimming120.isSwimming(player),"server tick exits without travel");
        PacketSwimming120 yes=roundtrip(new PacketSwimming120(player,true));PacketSwimming120 no=roundtrip(new PacketSwimming120(player,false));
        check(!yes.applyTo(player,false),"network state never changes server entity");
        Field remote=World.class.getDeclaredField("isRemote");remote.setAccessible(true);remote.setBoolean(world,true);
        check(yes.applyTo(player,false)&&PlayerSwimming120.isSwimming(player),"authoritative remote state applies");
        player.wet=false;player.eyes=false;player.motionY=.123;PlayerSwimming120.beforeTravel(player,true);
        check(PlayerSwimming120.isSwimming(player)&&player.motionY==.123,"remote travel cannot erase state or steer");
        check(no.applyTo(player,false)&&!PlayerSwimming120.isSwimming(player),"remote stop applies");
        check(yes.applyTo(player,true),"local correction applies");PlayerSwimming120.beforeTravel(player,false);
        check(!PlayerSwimming120.isSwimming(player),"local player retains prediction");
        int id=player.getEntityId();player.setEntityId(id+1);check(!yes.applyTo(player,false),"wrong entity ID rejected");player.setEntityId(id);
        player.dimension++;check(!yes.applyTo(player,false),"wrong dimension rejected");player.dimension--;
        Swimming115Fixture.Player another=new Swimming115Fixture.Player(world);another.setEntityId(id);check(!yes.applyTo(another,false),"reused entity ID with another UUID rejected");
        yes.applyTo(player,false);PlayerSwimming120.clear(player);check(!PlayerSwimming120.isSwimming(player),"logout clears swimming");
        player.wet=true;player.eyes=true;player.setSprinting(true);PlayerSwimming120.beforeTravel(player,false);check(PlayerSwimming120.isSwimming(player),"clear also releases remote authority marker");
        for(int size:new int[]{0,24,26,100}){ByteBuf b=Unpooled.buffer();try{b.writeZero(size);boolean reject=false;try{new PacketSwimming120().fromBytes(b);}catch(IllegalArgumentException e){reject=true;}check(reject,"reject packet size "+size);}finally{b.release();}}
        ByteBuf b=Unpooled.buffer();try{new PacketSwimming120(player,true).toBytes(b);b.setByte(24,6);boolean reject=false;try{new PacketSwimming120().fromBytes(b);}catch(IllegalArgumentException e){reject=true;}check(reject,"reject invalid pose flags");}finally{b.release();}
        System.out.println("PASS swimming116: "+checks+" server-state, wire, identity and remote/local application checks");
    }
    static PacketSwimming120 roundtrip(PacketSwimming120 packet){ByteBuf b=Unpooled.buffer();try{packet.toBytes(b);check(b.readableBytes()==25,"fixed packet size");PacketSwimming120 copy=new PacketSwimming120();copy.fromBytes(b);check(b.readableBytes()==0,"full decode");return copy;}finally{b.release();}}
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
}
