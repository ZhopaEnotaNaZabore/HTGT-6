import com.mod.htgt6.server.utils.Netty.PacketFailureDiagnostics;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.EncoderException;
import java.nio.channels.ClosedChannelException;

public final class PacketFailureDiagnosticsFixture {
    public static void main(String[] args) throws Exception {
        com.mod.htgt6.asm.HTGT6Config.NIO_DEBUG = false;
        EmbeddedChannel disabled = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        PacketFailureDiagnostics.installEarly(disabled.pipeline().firstContext());
        if (disabled.pipeline().get("htgt6_packet_failures") != null) throw new AssertionError("debug installed while disabled");
        PacketFailureDiagnostics.recordInbound(disabled.pipeline().firstContext(), "disabled");
        PacketFailureDiagnostics.reportEarly(disabled.pipeline().firstContext(), new RuntimeException("MUST_NOT_LOG"));
        disabled.finish();
        com.mod.htgt6.asm.HTGT6Config.NIO_DEBUG = true;
        final EncoderException failure = new EncoderException(new IllegalStateException("fixture root cause"));
        EmbeddedChannel broken = new EmbeddedChannel(new ChannelOutboundHandlerAdapter() {
            @Override public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                promise.setFailure(msg.equals("closed") ? new ClosedChannelException() : failure);
            }
        }, new PacketFailureDiagnostics());
        ChannelPromise closed = broken.newPromise();
        broken.pipeline().write("closed", closed);
        if (!(closed.cause() instanceof ClosedChannelException)) throw new AssertionError("closed cause");
        ChannelPromise first = broken.newPromise();
        broken.pipeline().write("first", first);
        if (first.cause() != failure) throw new AssertionError("failure changed");
        ChannelPromise second = broken.newPromise();
        broken.pipeline().write("second", second);
        if (second.cause() != failure) throw new AssertionError("second failure swallowed");
        EmbeddedChannel good = new EmbeddedChannel(new PacketFailureDiagnostics());
        Object packet = new Object();
        good.writeOutbound(packet);
        if (good.readOutbound() != packet) throw new AssertionError("packet changed");
        good.pipeline().writeAndFlush("void", good.voidPromise());
        if (!"void".equals(good.readOutbound())) throw new AssertionError("void promise changed");
        broken.finish();
        good.finish();
        EmbeddedChannel early = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        ChannelHandlerContext ctx = early.pipeline().firstContext();
        PacketFailureDiagnostics.installEarly(ctx);
        PacketFailureDiagnostics.installEarly(ctx);
        if (early.pipeline().get("htgt6_packet_failures") == null) throw new AssertionError("early installation");
        PacketFailureDiagnostics.recordInbound(ctx, "login packet probe");
        PacketFailureDiagnostics.reportEarly(ctx, new ClosedChannelException());
        PacketFailureDiagnostics.reportEarly(ctx, new ClassCastException("LOGIN to PLAY fixture"));
        PacketFailureDiagnostics.reportEarly(ctx, new ClassCastException("duplicate suppressed"));
        early.finish();
        System.out.println("PASS: original failures preserved, successful packets and void promises unchanged");
    }
}
