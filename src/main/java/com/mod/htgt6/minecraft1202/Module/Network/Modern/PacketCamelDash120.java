package com.mod.htgt6.minecraft1202.Module.Network.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityCamel120;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

/** C2S release of the Camel jump key; the server validates the controlling rider and cooldown. */
public final class PacketCamelDash120 implements IMessage {
    private int power;
    public PacketCamelDash120(){}
    public PacketCamelDash120(int power){this.power=power;}
    public int getPower(){return power;}
    @Override public void fromBytes(ByteBuf buffer){power=buffer.readUnsignedByte();}
    @Override public void toBytes(ByteBuf buffer){buffer.writeByte(power);}
    public static final class Handler implements IMessageHandler<PacketCamelDash120,IMessage>{
        @Override public IMessage onMessage(PacketCamelDash120 message,MessageContext context){
            EntityPlayerMP player=context.getServerHandler().playerEntity;
            if(player.ridingEntity instanceof EntityCamel120){
                EntityCamel120 camel=(EntityCamel120)player.ridingEntity;
                if(camel.riddenByEntity==player)camel.requestDash(message.power);
            }
            return null;
        }
    }
}
