package com.mod.htgt6.server.utils.Netty;
import com.mod.htgt6.asm.HTGT6Config;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Observes write failures without changing packets or swallowing network errors. */
public final class PacketFailureDiagnostics extends ChannelDuplexHandler {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-PacketFailure");

    public static void installEarly(ChannelHandlerContext context) {
        if (!HTGT6Config.NIO_DEBUG) return;
        if (context.pipeline().get("htgt6_packet_failures") == null) {
            if (context.pipeline().get("packet_handler") != null)
                context.pipeline().addBefore("packet_handler", "htgt6_packet_failures", new PacketFailureDiagnostics());
            else context.pipeline().addLast("htgt6_packet_failures", new PacketFailureDiagnostics());
            LOGGER.info("Packet diagnostics active from channelActive: " + context.channel().getClass().getName());
        }
    }

    public static void installEarly(ChannelHandlerContext context, boolean client) {
        if (!HTGT6Config.NIO_DEBUG) return;
        NetworkDebugLog.side(context.channel(), client ? "client" : "server");
        installEarly(context);
    }

    public static void recordInbound(ChannelHandlerContext context, Object packet) {
        if (!HTGT6Config.NIO_DEBUG) return;
        NetworkDebugLog.record(context.channel(), "IN", packet);
    }

    public static void reportEarly(ChannelHandlerContext context, Throwable failure) {
        if (!HTGT6Config.NIO_DEBUG) return;
        NetworkDebugLog.report(context.channel(), "NetworkManager.exceptionCaught", failure, "<pipeline>");
    }

    public static void recordProcessing(Object packet, Object handler, Object manager) {
        if (!HTGT6Config.NIO_DEBUG) return;
        NetworkDebugLog.record(((net.minecraft.network.NetworkManager) manager).channel(), "PROCESS", packet);
    }

    public static void reportProcessing(Object manager, Throwable failure) {
        if (!HTGT6Config.NIO_DEBUG) return;
        try {
            net.minecraft.network.NetworkManager network = (net.minecraft.network.NetworkManager) manager;
            Object handler = network.getNetHandler();
            NetworkDebugLog.report(network.channel(), "network-tick/packet-processing", failure,
                    handler == null ? "<null>" : handler.getClass().getName());
        } catch (Throwable ignored) { /* Never replace the original failure with a logging failure. */ }
    }

    @Override
    public void write(final ChannelHandlerContext context, Object message,
                      ChannelPromise promise) throws Exception {
        if (!HTGT6Config.NIO_DEBUG) {
            context.write(message, promise);
            return;
        }
        // Never retain the packet/player/NBT in an asynchronous listener.
        final String packetClass = message == null ? "<null>" : message.getClass().getName();
        NetworkDebugLog.record(context.channel(), "OUT", message);
        // Void promises cannot accept listeners. Preserve their usual exception propagation.
        if (promise != context.voidPromise()) {
            promise.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (!future.isSuccess() && !future.isCancelled()) {
                        report(context, packetClass, future.cause());
                    }
                }
            });
        }
        try {
            context.write(message, promise);
        } catch (Exception failure) {
            report(context, packetClass, failure);
            throw failure;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable failure) throws Exception {
        report(context, "<pipeline event>", failure);
        context.fireExceptionCaught(failure);
    }

    private void report(ChannelHandlerContext context, String packetClass, Throwable failure) {
        if (!HTGT6Config.NIO_DEBUG) return;
        NetworkDebugLog.report(context.channel(), "pipeline/write packet=" + packetClass, failure, "<pipeline>");
    }
}
