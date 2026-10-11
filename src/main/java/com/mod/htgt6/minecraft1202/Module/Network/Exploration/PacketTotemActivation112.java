package com.mod.htgt6.minecraft1202.Module.Network.Exploration;
import cpw.mods.fml.common.network.simpleimpl.IMessage;import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;import cpw.mods.fml.common.network.simpleimpl.MessageContext;import io.netty.buffer.ByteBuf;
/** Client equivalent of Minecraft 1.12 entity status 35. */
public final class PacketTotemActivation112 implements IMessage{
 @Override public void fromBytes(ByteBuf b){}@Override public void toBytes(ByteBuf b){}
 public static final class Handler implements IMessageHandler<PacketTotemActivation112,IMessage>{@Override public IMessage onMessage(PacketTotemActivation112 m,MessageContext c){net.minecraft.client.Minecraft.getMinecraft().func_152344_a(new Runnable(){@Override public void run(){com.mod.htgt6.minecraft1202.Module.Client.Exploration.TotemActivationRenderer112.activate();}});return null;}}
}
