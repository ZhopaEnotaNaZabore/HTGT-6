package com.mod.htgt6.server.utils.Netty;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;

/**
 * Applies the same bounded, low-latency TCP profile to both sides of a remote
 * multiplayer connection. It intentionally does not touch local/integrated-server
 * channels and does not attempt to bypass a server's chunk visibility rules.
 */
public final class NetworkOptimizer {
    private static final int SOCKET_BUFFER_BYTES = 256 * 1024;
    private static final int RECEIVE_INITIAL_BYTES = 32 * 1024;
    private static final int RECEIVE_MAX_BYTES = 256 * 1024;
    private static final int WRITE_HIGH_WATER_MARK = 256 * 1024;
    private static final int WRITE_LOW_WATER_MARK = 64 * 1024;

    public NetworkOptimizer() {
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onClientConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        installDiagnostics(event.manager == null ? null : event.manager.channel(), true);
        if (!event.isLocal) {
            configure(event.manager == null ? null : event.manager.channel(), "client");
        }
    }

    @SubscribeEvent
    public void onServerClientConnected(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        final Channel channel = event.manager == null ? null : event.manager.channel();
        installDiagnostics(channel, false);
        configure(channel, "server");
    }

    private static void installDiagnostics(final Channel channel, final boolean client) {
        if (com.mod.htgt6.asm.HTGT6Config.NIO_DEBUG && channel != null && channel.isOpen()) {
            // Registration runs on the channel event loop, after existing packet handlers.
            channel.eventLoop().execute(new Runnable() {
                @Override
                public void run() {
                    if (com.mod.htgt6.asm.HTGT6Config.NIO_DEBUG && channel.isOpen()) {
                        NetworkDebugLog.side(channel, client ? "client" : "server");
                        io.netty.channel.ChannelHandlerContext context = channel.pipeline().context("packet_handler");
                        if (context != null) PacketFailureDiagnostics.installEarly(context, client);
                        else if (channel.pipeline().get("htgt6_packet_failures") == null)
                            channel.pipeline().addLast("htgt6_packet_failures", new PacketFailureDiagnostics());
                    }
                }
            });
        }
    }

    private void configure(Channel channel, String side) {
        if (!(channel instanceof SocketChannel) || !channel.isOpen()) {
            return;
        }

        try {
            ChannelConfig config = channel.config();
            config.setOption(ChannelOption.TCP_NODELAY, true);
            config.setOption(ChannelOption.SO_KEEPALIVE, true);

            // Kernel buffers are a capacity, not a bandwidth cap. Keeping them bounded
            // prevents a slow chunk decode from turning into seconds of queued traffic.
            config.setOption(ChannelOption.SO_RCVBUF, SOCKET_BUFFER_BYTES);
            config.setOption(ChannelOption.SO_SNDBUF, SOCKET_BUFFER_BYTES);
            config.setAllocator(PooledByteBufAllocator.DEFAULT);
            config.setRecvByteBufAllocator(new AdaptiveRecvByteBufAllocator(
                    4 * 1024, RECEIVE_INITIAL_BYTES, RECEIVE_MAX_BYTES));

            // Retain back-pressure: a saturated peer must not cause unbounded packet
            // accumulation and delayed keep-alive/control packets.
            config.setWriteBufferHighWaterMark(WRITE_HIGH_WATER_MARK);
            config.setWriteBufferLowWaterMark(WRITE_LOW_WATER_MARK);
            System.out.println("[NetworkOptimizer] " + side + " socket profile enabled.");
        } catch (Throwable t) {
            System.err.println("[NetworkOptimizer] " + side + " socket profile was not applied: " + t.getMessage());
        }
    }
}
