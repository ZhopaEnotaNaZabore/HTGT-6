package com.mod.htgt6.asm;

import com.mod.htgt6.server.utils.Netty.PacketFailureDiagnostics;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

public final class NetworkDiagnosticsFixture {
    private static void check(boolean value, String reason) { if (!value) throw new AssertionError(reason); }
    public static void main(String[] args) throws Exception {
        HTGT6Config.NETWORK_DEBUG_LOG_DIRECTORY = Files.createTempDirectory(
                new File("build").toPath(), "network-debug-fixture-").toFile();
        HTGT6Config.NIO_DEBUG = false;
        EmbeddedChannel disabled = new EmbeddedChannel(new ChannelDuplexHandler());
        PacketFailureDiagnostics.installEarly(disabled.pipeline().firstContext(), true);
        check(disabled.pipeline().get("htgt6_packet_failures") == null, "debug disabled installs nothing");
        check(HTGT6Config.NETWORK_DEBUG_LOG_DIRECTORY.list().length == 0, "debug disabled writes no file");
        HTGT6Config.NIO_DEBUG = true;
        final List<Throwable> forwarded = new ArrayList<Throwable>();
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelDuplexHandler());
        channel.pipeline().addLast("encoder", new ChannelOutboundHandlerAdapter() {
            @Override public void write(ChannelHandlerContext context, Object msg, ChannelPromise promise) {
                promise.setFailure(new EncoderException(new NullPointerException("fixture-encoding-root")));
            }
        });
        channel.pipeline().addLast("packet_handler", new ChannelDuplexHandler() {
            @Override public void exceptionCaught(ChannelHandlerContext context, Throwable failure) { forwarded.add(failure); }
        });
        ChannelHandlerContext manager = channel.pipeline().context("packet_handler");
        PacketFailureDiagnostics.installEarly(manager, true);
        PacketFailureDiagnostics.installEarly(manager, true);
        check(channel.pipeline().names().indexOf("htgt6_packet_failures") < channel.pipeline().names().indexOf("packet_handler"),
                "diagnostics precede vanilla exception consumer");
        PacketFailureDiagnostics.recordInbound(manager, "incoming");
        ChannelFuture write = channel.writeAndFlush("outgoing");
        check(!write.isSuccess() && write.cause() instanceof EncoderException, "failed promise unchanged");
        Throwable decode = new DecoderException(new NullPointerException("fixture-decoding-root"));
        channel.pipeline().context("encoder").fireExceptionCaught(decode);
        check(forwarded.size() == 1 && forwarded.get(0) == decode, "decoder failure propagates unchanged");
        PacketFailureDiagnostics.reportEarly(manager, decode);
        PacketFailureDiagnostics.reportEarly(manager, new NullPointerException("fixture-second-failure"));
        String log = readLog("client");
        check(log.contains("fixture-encoding-root") && log.contains("Caused by:") && log.contains("EncoderException"), "full encoder cause chain");
        check(log.contains("fixture-decoding-root") && log.contains("fixture-second-failure"), "distinct failures retained");
        check(log.contains("side=client") && log.contains("OUT:java.lang.String") && log.contains("IN:java.lang.String")
                && log.contains("connection=") && log.contains("protocol="), "connection and packet context");
        check(log.indexOf("io.netty.handler.codec.DecoderException:") == log.lastIndexOf("io.netty.handler.codec.DecoderException:"),
                "same exception not duplicated");
        PacketFailureDiagnostics.installEarly(manager, false);
        PacketFailureDiagnostics.reportEarly(manager, new NullPointerException("fixture-server-failure"));
        check(readLog("server").contains("side=server"), "server has separate log");
        HTGT6Config.NIO_DEBUG = false;
        PacketFailureDiagnostics.reportEarly(manager, new NullPointerException("fixture-disabled"));
        check(!readLog("server").contains("fixture-disabled"), "runtime config gate");
        channel.finish(); disabled.finish();
        verifyAsm();
        System.out.println("Network diagnostics OK: config gate, encoder/decoder propagation, cause chains, deduplication, both sides, ASM frames and rethrow");
    }

    private static String readLog(String side) throws Exception {
        return new String(Files.readAllBytes(new File(HTGT6Config.NETWORK_DEBUG_LOG_DIRECTORY,
                "htgt6-network-" + side + "-0.log").toPath()), StandardCharsets.UTF_8);
    }

    private static void verifyAsm() throws Exception {
        InputStream input = NetworkDiagnosticsFixture.class.getClassLoader().getResourceAsStream("net/minecraft/network/NetworkManager.class");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192]; int count;
        while ((count = input.read(buffer)) >= 0) output.write(buffer, 0, count);
        input.close();
        byte[] patched = NetworkDiagnosticsASM.transform(output.toByteArray());
        byte[] twice = NetworkDiagnosticsASM.transform(patched);
        check(java.util.Arrays.equals(patched, twice), "ASM is idempotent");
        ClassNode node = new ClassNode(); new ClassReader(patched).accept(node, 0);
        for (MethodNode method : node.methods) new Analyzer(new BasicVerifier()).analyze(node.name, method);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        String name = "com/mod/htgt6/asm/NetworkDebugSynthetic";
        writer.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, name, null, "java/lang/Object", null);
        writer.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "client", "Z", null, null).visitEnd();
        method(writer, "<init>", "()V", false);
        method(writer, "channelActive", "(Lio/netty/channel/ChannelHandlerContext;)V", false);
        method(writer, "exceptionCaught", "(Lio/netty/channel/ChannelHandlerContext;Ljava/lang/Throwable;)V", false);
        method(writer, "channelRead0", "(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", false);
        method(writer, "processReceivedPackets", "()V", true);
        writer.visitEnd();
        final byte[] synthetic = NetworkDiagnosticsASM.transform(writer.toByteArray());
        Class<?> type = new ClassLoader(NetworkDiagnosticsFixture.class.getClassLoader()) {
            Class<?> define() { return defineClass(null, synthetic, 0, synthetic.length); }
        }.define();
        try { type.getMethod("processReceivedPackets").invoke(type.newInstance()); throw new AssertionError("expected NPE"); }
        catch (java.lang.reflect.InvocationTargetException failure) {
            check(failure.getCause() instanceof NullPointerException, "tick NPE is rethrown, not swallowed");
        }
    }

    private static void method(ClassWriter writer, String name, String desc, boolean fail) {
        MethodVisitor method = writer.visitMethod(Opcodes.ACC_PUBLIC, name, desc, null, null);
        method.visitCode();
        if (name.equals("<init>")) {
            method.visitVarInsn(Opcodes.ALOAD, 0);
            method.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        }
        if (fail) { method.visitInsn(Opcodes.ACONST_NULL); method.visitInsn(Opcodes.ATHROW); }
        else method.visitInsn(Opcodes.RETURN);
        method.visitMaxs(0, 0); method.visitEnd();
    }
}
