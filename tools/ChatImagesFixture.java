package com.mod.htgt6.client.vanillatweaks.CHAT;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Arrays;
import javax.imageio.ImageIO;
import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBuf;

/** No HTTP requests or OpenGL context: exercises URL transport and actual image decoders. */
public final class ChatImagesFixture {
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private interface Action { void run() throws Exception; }
    private static void rejected(Action action) throws Exception {
        try { action.run(); } catch (Exception expected) { return; }
        throw new AssertionError("Expected rejection");
    }
    public static void main(String[] args) throws Exception {
        String signed = "https://cdn.discordapp.com/attachments/123/456/test.PNG?ex=12345678&is=87654321&hm="
                + "abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789";
        require(ChatImageLinks.valid(signed), "signed PNG");
        require(ChatImageLinks.find("image <" + signed + "> " + signed).equals(Arrays.asList(signed)), "query preserved/deduplicated");
        require(ChatImageLinks.valid("https://example.com/a.JPEG"), "JPEG");
        for (String bad : new String[]{"file:///a.png", "https://user:pass@example.com/a.png", "https://example.com/a.svg",
                "https://example.com:8080/a.png", "https://example.com/a.png#fragment", "https://example.com/a.png\n/command"})
            require(!ChatImageLinks.valid(bad), "reject " + bad);
        for (String address : new String[]{"http://127.0.0.1/a.png", "http://10.0.0.1/a.png", "http://169.254.169.254/a.png",
                "http://[::1]/a.png", "http://[fd00::1]/a.png"}) rejected(() -> ChatImageCache.checkAddress(new URL(address)));
        ByteBuf wire = Unpooled.buffer(); new PacketChatImage(signed).toBytes(wire);
        PacketChatImage received = new PacketChatImage(); received.fromBytes(wire);
        ByteBuf copy = Unpooled.buffer(); received.toBytes(copy); wire.readerIndex(0);
        require(wire.equals(copy), "long signed URL survives packet"); wire.release(); copy.release();
        for (int length : new int[]{101, 512, 2048}) {
            char[] characters = new char[length]; Arrays.fill(characters, 'я');
            String text = new String(characters);
            require(ChatImageLinks.find(text).isEmpty(), "plain text is not an image");
            ByteBuf input = Unpooled.buffer(), output = Unpooled.buffer();
            new PacketChatImage(text).toBytes(input);
            PacketChatImage packet = new PacketChatImage(); packet.fromBytes(input); packet.toBytes(output);
            input.readerIndex(0); require(input.equals(output), "long Unicode chat round trip");
            input.release(); output.release();
        }
        require(!PacketChatImage.validText(" /op name"), "extended packet must not run commands");
        require(!PacketChatImage.validText("hello\nworld"), "multiline packet rejected");
        require(!PacketChatImage.validText(new String(new char[2049]).replace('\0', 'a')), "length bound");
        BufferedImage png = new BufferedImage(32, 16, BufferedImage.TYPE_INT_ARGB);
        png.setRGB(7, 8, 0x804080C0);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); ImageIO.write(png, "png", bytes);
        BufferedImage decoded = ChatImageCache.decode(bytes.toByteArray());
        require(decoded.getWidth() == 32 && decoded.getHeight() == 16 && decoded.getRGB(7, 8) == 0x804080C0, "PNG alpha/pixels");
        bytes.reset(); ImageIO.write(new BufferedImage(2048, 1024, BufferedImage.TYPE_INT_RGB), "jpg", bytes);
        decoded = ChatImageCache.decode(bytes.toByteArray());
        require(decoded.getWidth() == 1024 && decoded.getHeight() == 512, "JPEG bounded thumbnail/aspect");
        rejected(() -> ChatImageCache.decode(new byte[]{1,2,3,4}));
        bytes.reset(); ImageIO.write(png, "gif", bytes); final byte[] gif = bytes.toByteArray();
        rejected(() -> ChatImageCache.decode(gif));
        bytes.reset(); ImageIO.write(new BufferedImage(8193, 1, BufferedImage.TYPE_INT_RGB), "png", bytes);
        final byte[] huge = bytes.toByteArray(); rejected(() -> ChatImageCache.decode(huge));
        System.out.println("ChatImagesFixture OK: signed links, packet roundtrip, address restrictions, PNG alpha, JPEG resize, invalid/oversized images");
    }
}
