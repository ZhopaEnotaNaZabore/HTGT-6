package com.mod.htgt6.minecraft1202.Module.Network.Inventory;

import com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.RecipeBookSystem112;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public final class PacketPlaceRecipe112 implements IMessage {
    private String key = "";
    private int window = -1;
    private boolean all;
    public PacketPlaceRecipe112() { }
    public PacketPlaceRecipe112(String key, int window, boolean all) {
        this.key=key==null?"":key;this.window=window;this.all=all;
    }
    @Override public void fromBytes(ByteBuf buffer) {
        key=ByteBufUtils.readUTF8String(buffer);
        // Old packets have no window and are deliberately not applied to an arbitrary container.
        if(buffer.readableBytes()>=5){window=buffer.readInt();all=buffer.readBoolean();}
    }
    @Override public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer,key);buffer.writeInt(window);buffer.writeBoolean(all);
    }

    public static final class Handler implements IMessageHandler<PacketPlaceRecipe112, IMessage> {
        @Override public IMessage onMessage(PacketPlaceRecipe112 message, MessageContext context) {
            RecipeBookSystem112.requestPlace(context.getServerHandler().playerEntity, message.key, message.window, message.all);
            return null;
        }
    }
}
