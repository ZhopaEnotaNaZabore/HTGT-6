package com.mod.htgt6.client.vanillatweaks.CHAT;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/** Offline hostile-input tests; no external requests, Minecraft server, or OpenGL needed. */
public final class ChatImageSecurityFixture {
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static String decode(ByteBuf data) throws Exception {
        try {
            PacketChatImage packet = new PacketChatImage(); packet.fromBytes(data);
            Field field = PacketChatImage.class.getDeclaredField("url"); field.setAccessible(true);
            return (String) field.get(packet);
        } finally { data.release(); }
    }
    private static ByteBuf wire(String url) {
        ByteBuf data = Unpooled.buffer(); ByteBufUtils.writeUTF8String(data, url); return data;
    }
    public static void main(String[] args) throws Exception {
        String prefix = "https://example.com/a.png?q=";
        String maximum = prefix + new String(new char[ChatImageLinks.MAX_URL-prefix.length()]).replace('\0','a');
        require(maximum.equals(decode(wire(maximum))), "2048-character signed-style URL roundtrip");
        for (String invalid : new String[]{maximum + "x", "/op player", "https://example.com/a.png\n/op player",
                "https://example.com/a.png?q=\u00a7a", "file:///a.png", "https://user:pass@example.com/a.png"})
            require(decode(wire(invalid)) == null, "reject invalid text");
        require(decode(wire(prefix).writeByte(0)) == null, "reject trailing bytes");
        require(decode(Unpooled.buffer()) == null, "empty packet");
        require(decode(Unpooled.wrappedBuffer(new byte[]{(byte)128})) == null, "truncated varint");
        require(decode(Unpooled.wrappedBuffer(new byte[]{(byte)255,(byte)255,127})) == null, "huge declared length");
        require(decode(Unpooled.wrappedBuffer(new byte[]{2,(byte)195,40})) == null, "malformed UTF-8");
        require(decode(Unpooled.wrappedBuffer(new byte[9000])) == null, "oversized frame");

        ChatImageIngress<String> queue = new ChatImageIngress<String>();
        UUID sender = UUID.randomUUID(); long start = -5000000000L;
        require(queue.offer(sender, "first", start), "accept with negative nanoTime");
        for (int i=0;i<10000;i++) require(!queue.offer(sender,"spam",start), "same-player flood blocked");
        require("first".equals(queue.poll()) && queue.poll()==null, "no duplicate pending messages");
        require(!queue.offer(sender,"early",start+1999999999L), "cooldown survives queue drain");
        require(queue.offer(sender,"later",start+2000000000L), "cooldown expires");
        queue.clear();
        CountDownLatch ready = new CountDownLatch(32), go = new CountDownLatch(1), done = new CountDownLatch(32);
        AtomicInteger accepted = new AtomicInteger();
        for (int i=0;i<32;i++) new Thread(() -> {
            ready.countDown();
            try { go.await(); if(queue.offer(UUID.randomUUID(),"message",0)) accepted.incrementAndGet(); }
            catch(InterruptedException e) { throw new AssertionError(e); } finally { done.countDown(); }
        }).start();
        ready.await(); go.countDown(); done.await();
        require(accepted.get()==8,"atomic global flood budget");
        queue.clear();
        for (int second=0;second<16;second++) for(int i=0;i<8;i++)
            require(queue.offer(UUID.randomUUID(),"queued",second*1000000000L),"fill bounded queue");
        require(!queue.offer(UUID.randomUUID(),"overflow",20000000000L),"hard queue bound");
        int drained=0; while(queue.poll()!=null) drained++;
        require(drained==128,"exact queue capacity");
        queue.clear(); require(queue.offer(sender,"new server",0),"lifecycle reset");

        for(String ip : new String[]{"127.0.0.1","10.0.0.1","172.16.0.1","192.168.0.1","169.254.169.254",
                "100.64.0.1","0.0.0.0","224.0.0.1","198.18.0.1","::1","fc00::1","fe80::1",
                "::ffff:127.0.0.1","2002:7f00:1::","64:ff9b::7f00:1","2001:db8::1"}) {
            boolean rejected=false;
            try { ChatImageHttp.requirePublic(InetAddress.getByName(ip)); }
            catch(java.net.UnknownHostException expected) { rejected=true; }
            require(rejected,"reject non-public/transition destination " + ip);
        }
        ChatImageHttp.requirePublic(InetAddress.getByName("8.8.8.8"));
        ChatImageHttp.requirePublic(InetAddress.getByName("2606:4700:4700::1111"));
        // Exercise the real HttpClient connection path: these must fail in its resolver,
        // before opening any socket to the local machine.
        for (String url : new String[]{"http://127.0.0.1/a.png", "http://[::1]/a.jpg"}) {
            boolean rejected = false;
            try { ChatImageHttp.download(url); }
            catch (java.net.UnknownHostException expected) { rejected = true; }
            require(rejected, "HTTP connector enforces public-address resolver");
        }
        System.out.println("ChatImageSecurityFixture OK: malformed wire data, exact bounds, concurrent flood, lifecycle, IPv4/IPv6 restrictions");
    }
}
