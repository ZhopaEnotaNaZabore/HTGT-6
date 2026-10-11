package com.mod.htgt6.minecraft1202.Module.Network.Combat;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Authoritative flight state, including the pose of players other than the local pilot. */
public final class PacketElytraState implements IMessage {
    private int entityId,ticks,boost;
    private boolean flying;
    public PacketElytraState() { }
    public PacketElytraState(int entityId,boolean flying,int ticks,int boost){this.entityId=entityId;this.flying=flying;this.ticks=ticks;this.boost=boost;}
    @Override public void toBytes(ByteBuf b){b.writeInt(entityId);b.writeBoolean(flying);b.writeInt(ticks);b.writeInt(boost);}
    @Override public void fromBytes(ByteBuf b){entityId=b.readInt();flying=b.readBoolean();ticks=Math.max(0,b.readInt());boost=Math.max(0,b.readInt());}
    public static final class Handler implements IMessageHandler<PacketElytraState,IMessage> {
        @Override public IMessage onMessage(final PacketElytraState message,MessageContext context){
            final net.minecraft.client.Minecraft client=net.minecraft.client.Minecraft.getMinecraft();
            client.func_152344_a(new Runnable(){public void run(){
                if(client.theWorld==null)return;
                net.minecraft.entity.Entity entity=client.theWorld.getEntityByID(message.entityId);
                if(entity instanceof net.minecraft.entity.player.EntityPlayer)
                    com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.ElytraFlight.acceptState(
                        (net.minecraft.entity.player.EntityPlayer)entity,message.flying,message.ticks,message.boost,entity==client.thePlayer);
            }});return null;
        }
    }
}
