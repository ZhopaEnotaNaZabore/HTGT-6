package com.mod.htgt6.minecraft1202.Module.Network.Inventory;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;

public final class PacketRecipeBookSync112 implements IMessage {
    public static final int MAX_ENTRIES = 128;
    public static final int MAX_BYTES = 24576;
    private boolean notifications;
    private boolean replace;
    private int tutorialStep = -1;
    private final List<String> keys = new ArrayList<String>();
    private final List<ItemStack> outputs = new ArrayList<ItemStack>();

    public PacketRecipeBookSync112() { }
    public PacketRecipeBookSync112(boolean replace, int tutorialStep, List<String> keys, List<ItemStack> outputs) {
        this.replace = replace;
        this.notifications = !replace;
        this.tutorialStep = tutorialStep;
        if (keys != null) this.keys.addAll(keys);
        if (outputs != null) for (ItemStack stack : outputs) this.outputs.add(stack == null ? null : stack.copy());
    }

    /** Bounded by encoded bytes as well as count; full login snapshots never generate toasts. */
    public static List<PacketRecipeBookSync112> batches(boolean replace, int tutorial,
            List<String> keys, List<ItemStack> outputs) {
        List<PacketRecipeBookSync112> result = new ArrayList<PacketRecipeBookSync112>();
        PacketRecipeBookSync112 part = new PacketRecipeBookSync112(replace,tutorial,null,null);
        int bytes = 5;
        for(int i=0;i<keys.size();i++) {
            String key=keys.get(i);
            ItemStack output=replace || outputs==null || i>=outputs.size()?null:outputs.get(i);
            int size=rowSize(key,output);
            if(size>MAX_BYTES-5) { output=null;size=rowSize(key,null); }
            if(size>MAX_BYTES-5) throw new IllegalArgumentException("Recipe key exceeds packet budget");
            if(part.keys.size()==MAX_ENTRIES || bytes+size>MAX_BYTES) {
                result.add(part);part=new PacketRecipeBookSync112(false,-1,null,null);
                part.notifications=!replace;bytes=5;
            }
            part.keys.add(key);part.outputs.add(ItemStack.copyItemStack(output));bytes+=size;
        }
        if(!part.keys.isEmpty() || result.isEmpty())result.add(part);
        return result;
    }
    private static int rowSize(String key, ItemStack output) {
        ByteBuf buffer=io.netty.buffer.Unpooled.buffer();
        try { ByteBufUtils.writeUTF8String(buffer,key);ByteBufUtils.writeItemStack(buffer,output);return buffer.readableBytes(); }
        finally { buffer.release(); }
    }

    @Override public void fromBytes(ByteBuf buffer) {
        if(buffer.readableBytes()>MAX_BYTES)throw new IllegalArgumentException("Oversized recipe packet");
        keys.clear();outputs.clear();
        replace = buffer.readBoolean();
        tutorialStep = buffer.readByte();
        notifications = buffer.readBoolean();
        int count = buffer.readUnsignedShort();
        if(count>MAX_ENTRIES)throw new IllegalArgumentException("Too many recipe entries");
        for (int i = 0; i < count; ++i) {
            keys.add(ByteBufUtils.readUTF8String(buffer));
            outputs.add(ByteBufUtils.readItemStack(buffer));
        }
    }

    @Override public void toBytes(ByteBuf buffer) {
        if(keys.size()>MAX_ENTRIES)throw new IllegalArgumentException("Use recipe packet batches");
        int start=buffer.writerIndex();
        buffer.writeBoolean(replace);
        buffer.writeByte(tutorialStep);
        buffer.writeBoolean(notifications);
        buffer.writeShort(keys.size());
        for (int i = 0; i < keys.size(); ++i) {
            ByteBufUtils.writeUTF8String(buffer, keys.get(i));
            ByteBufUtils.writeItemStack(buffer, i < outputs.size() ? outputs.get(i) : null);
        }
        if(buffer.writerIndex()-start>MAX_BYTES)throw new IllegalArgumentException("Oversized recipe packet");
    }

    public static final class Handler implements IMessageHandler<PacketRecipeBookSync112, IMessage> {
        @Override public IMessage onMessage(final PacketRecipeBookSync112 message, MessageContext context) {
            final long session = com.mod.htgt6.client.inventory112.RecipeBookClientState112.session();
            final net.minecraft.client.network.NetHandlerPlayClient handler = context.getClientHandler();
            net.minecraft.client.Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override public void run() {
                    if (net.minecraft.client.Minecraft.getMinecraft().getNetHandler() != handler) return;
                    com.mod.htgt6.client.inventory112.RecipeBookClientState112.applyForSession(session,
                            message.replace, message.tutorialStep, message.keys, message.outputs, message.notifications);
                }
            });
            return null;
        }
    }
}
