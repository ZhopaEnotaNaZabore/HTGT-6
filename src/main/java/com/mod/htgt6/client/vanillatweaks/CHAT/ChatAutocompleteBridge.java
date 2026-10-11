package com.mod.htgt6.client.vanillatweaks.CHAT;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S3APacketTabComplete;

/** Routes vanilla tab-complete replies to the ModularUI screen (which is not a GuiChat). */
@SideOnly(Side.CLIENT)
public final class ChatAutocompleteBridge {
    private static final String HANDLER_NAME = "htgt6_chat_autocomplete";
    private final Minecraft mc = Minecraft.getMinecraft();
    private Channel activeChannel;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null || mc.thePlayer.sendQueue == null) return;
        NetworkManager manager = mc.thePlayer.sendQueue.getNetworkManager();
        Channel channel = manager == null ? null : manager.channel();
        if (channel == null || channel == activeChannel) return;
        if (channel.pipeline().get(HANDLER_NAME) != null) return;
        if (channel.pipeline().context("packet_handler") == null) return;
        channel.pipeline().addBefore("packet_handler", HANDLER_NAME, new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
                if (message instanceof S3APacketTabComplete && ChatModularScreen.isOpen()) {
                    final String[] suggestions = ((S3APacketTabComplete) message).func_149630_c();
                    mc.func_152344_a(new Runnable() {
                        @Override
                        public void run() {
                            ChatModularScreen.acceptServerSuggestions(suggestions);
                        }
                    });
                }
                super.channelRead(context, message);
            }
        });
        activeChannel = channel;
    }
}
