package com.mod.htgt6.common.network;


import com.mod.htgt6.client.hud.ThermometerHUD;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketShowHUD implements IMessage {

    private String title;
    private String message;

    public PacketShowHUD() {} // Required empty constructor

    public PacketShowHUD(String title, String message) {
        this.title = title;
        this.message = message;
    }

    @Override
    public void fromBytes(io.netty.buffer.ByteBuf buf) {
        this.title = ByteBufUtils.readUTF8String(buf);
        this.message = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(io.netty.buffer.ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.title);
        ByteBufUtils.writeUTF8String(buf, this.message);
    }

    // Handler to safely update the client HUD
    public static class Handler implements IMessageHandler<PacketShowHUD, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(PacketShowHUD message, MessageContext ctx) {
            // Trigger the local client HUD
            ThermometerHUD.show(message.title, message.message);
            return null;
        }
    }
}