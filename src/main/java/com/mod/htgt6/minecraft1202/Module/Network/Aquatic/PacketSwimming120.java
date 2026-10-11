package com.mod.htgt6.minecraft1202.Module.Network.Aquatic;

import java.util.UUID;
import io.netty.buffer.ByteBuf;
import cpw.mods.fml.common.network.simpleimpl.*;
import net.minecraft.entity.player.EntityPlayer;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.PlayerSwimming120;

/** Only server-to-client; identity checks prevent applying a reused entity ID to another player. */
public final class PacketSwimming120 implements IMessage {
    private int dimension,entityId,pose;private UUID uuid;private boolean swimming;
    public PacketSwimming120() {}
    public PacketSwimming120(EntityPlayer player,boolean value) {
        dimension=player.dimension;entityId=player.getEntityId();uuid=player.getUniqueID();swimming=value;
        pose=com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.SwimmingPose120.kind(player);
    }
    @Override public void fromBytes(ByteBuf buffer) {
        if(buffer.readableBytes()!=25)throw new IllegalArgumentException("Invalid swimming packet length");
        dimension=buffer.readInt();entityId=buffer.readInt();uuid=new UUID(buffer.readLong(),buffer.readLong());
        int flag=buffer.readUnsignedByte();if(flag>5)throw new IllegalArgumentException("Invalid swimming state");swimming=(flag&1)!=0;pose=flag>>1;
    }
    @Override public void toBytes(ByteBuf buffer) {
        buffer.writeInt(dimension);buffer.writeInt(entityId);buffer.writeLong(uuid.getMostSignificantBits());buffer.writeLong(uuid.getLeastSignificantBits());buffer.writeByte((swimming?1:0)|(pose<<1));
    }
    public boolean matches(EntityPlayer player) {
        return player.dimension==dimension&&player.getEntityId()==entityId&&player.getUniqueID().equals(uuid);
    }
    public boolean applyTo(EntityPlayer player,boolean localPlayer) {
        if(!player.worldObj.isRemote||!matches(player))return false;
        PlayerSwimming120.setClientState(player,swimming,localPlayer);
        com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.SwimmingPose120.apply(player,pose);return true;
    }
    public static final class Handler implements IMessageHandler<PacketSwimming120,IMessage> {
        @Override public IMessage onMessage(final PacketSwimming120 packet,MessageContext context) {
            final net.minecraft.client.network.NetHandlerPlayClient connection=context.getClientHandler();
            net.minecraft.client.Minecraft.getMinecraft().func_152344_a(new Runnable(){@Override public void run(){
                net.minecraft.client.Minecraft mc=net.minecraft.client.Minecraft.getMinecraft();
                if(mc.getNetHandler()!=connection||mc.theWorld==null||mc.theWorld.provider.dimensionId!=packet.dimension)return;
                net.minecraft.entity.Entity entity=mc.theWorld.getEntityByID(packet.entityId);
                if(entity instanceof EntityPlayer)packet.applyTo((EntityPlayer)entity,entity==mc.thePlayer);
            }});
            return null;
        }
    }
}
