package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120;

import com.mod.htgt6.asm.HTGT6Transformer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Headless regression for the Forge 1.7.10 Chunk/Anvil 24-section seams. */
public final class FuturaHeightStorageFixtures120 {
    private static final String CHUNK_NAME = "net/minecraft/world/chunk/Chunk";
    private static final String CHUNK_ENTRY = CHUNK_NAME + ".class";
    private static final String WORLD_NAME = "net/minecraft/world/World";
    private static final String WORLD_ENTRY = WORLD_NAME + ".class";
    private static final String ANVIL_NAME = "net/minecraft/world/chunk/storage/AnvilChunkLoader";
    private static final String ANVIL_ENTRY = ANVIL_NAME + ".class";
    private static final String WORLD_PROVIDER_NAME = "net/minecraft/world/WorldProvider";
    private static final String WORLD_PROVIDER_ENTRY = WORLD_PROVIDER_NAME + ".class";
    private static final String S21_NAME = "net/minecraft/network/play/server/S21PacketChunkData";
    private static final String S21_ENTRY = S21_NAME + ".class";
    private static final String S26_NAME = "net/minecraft/network/play/server/S26PacketMapChunkBulk";
    private static final String S26_ENTRY = S26_NAME + ".class";
    private static final String S22_NAME = "net/minecraft/network/play/server/S22PacketMultiBlockChange";
    private static final String S22_ENTRY = S22_NAME + ".class";
    private static final String S23_NAME = "net/minecraft/network/play/server/S23PacketBlockChange";
    private static final String S23_ENTRY = S23_NAME + ".class";
    private static final String CLIENT_HANDLER_NAME = "net/minecraft/client/network/NetHandlerPlayClient";
    private static final String CLIENT_HANDLER_ENTRY = CLIENT_HANDLER_NAME + ".class";
    private static final String PLAYER_INSTANCE_NAME = "net/minecraft/server/management/PlayerManager$PlayerInstance";
    private static final String PLAYER_INSTANCE_ENTRY = PLAYER_INSTANCE_NAME + ".class";
    private static final String TRANSFORMER = "com/mod/htgt6/asm/HTGT6Transformer";

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("Expected Forge deobfuscated jar and optional built HTGT6 jar");
        }
        FuturaHeight120.validateContract();
        require(FuturaHeight120.PHYSICAL_CLOUD_Y == 384
                        && FuturaHeight120.cloudHeight(384) == 384
                        && FuturaHeight120.cloudHeight(192) == 192
                        && FuturaHeight120.cloudHeight(128) == 192
                        && FuturaHeight120.cloudHeight(999) == 384,
                "Futura cloud plane is not aligned with the configured physical Y=384");
        require(FuturaHeight120.ActivationGate.S21_CHUNK_PACKET_24_SECTIONS.isReady()
                        && FuturaHeight120.ActivationGate.S26_BULK_PACKET_24_SECTIONS.isReady(),
                "Proven S21/S26 packet gates must remain ready");
        require(FuturaHeight120.ActivationGate.BLOCK_UPDATE_PACKET_9_BIT_Y.isReady(),
                "Proven S22/S23 block-update gate must remain ready");
        require(FuturaHeight120.ActivationGate.COLLISION_AND_INTERACTION_384.isReady(),
                "World/Chunk interaction gate must remain ready");
        require(FuturaHeight120.interactionHeight(false) == 256
                        && FuturaHeight120.interactionHeight(true) == 384,
                "Futura-only interaction height contract changed");
        require(interactionY(false,255) && !interactionY(false,256) && !interactionY(false,383),
                "Vanilla interaction bounds changed");
        require(interactionY(true,255) && interactionY(true,256) && interactionY(true,383)
                        && !interactionY(true,384), "Futura interaction bounds changed");
        require(FuturaHeight120.storageSectionCount(false) == 16,
                "Legacy storage must remain 16 sections");
        require(FuturaHeight120.storageSectionCount(true) == 24,
                "Futura storage must contain 24 sections");
        require(HTGT6Transformer.s21LowSectionMask120(0x00ffffff)==0x0000ffff,
                "S21 vanilla scratch buffer must receive only its 16 low sections");
        verifyHighSectionDecoderIsolation();
        verifyLightingSectionArithmetic();
        verifyNbtRoundTrip();
        verifyAsmMatchers(args[0]);
        if (args.length == 2) verifyBuiltManifest(args[1]);
        require(FuturaHeight120.isPipelineComplete(),
                "All proven height gates must complete the pipeline");
        System.out.println("Futura height storage fixtures OK; EBS NBT sections=0..23, "
                + "Chunk lighting=5, World lighting=8, sections=16..23, COMPLETE_PIPELINE=true");
    }

    private static void verifyLightingSectionArithmetic() {
        require(FuturaHeight120.lightingHeight(false) == 256
                        && FuturaHeight120.lightingSectionCount(false) == 16,
                "Vanilla lighting bounds changed");
        require(FuturaHeight120.lightingHeight(true) == 384
                        && FuturaHeight120.lightingSectionCount(true) == 24,
                "Futura lighting bounds must expose 24 sections");
        for (int section = 16; section < 24; section++) {
            for (int x : new int[]{0, 7, 15}) for (int z : new int[]{0, 9, 15}) {
                int encoded = section + x * 24 + z * 384;
                require(encoded % 24 == section, "Relight section decode failed: " + section);
                require(encoded / 24 % 16 == x, "Relight X decode failed: " + x);
                require(encoded / 384 == z, "Relight Z decode failed: " + z);
            }
        }
    }

    /** NEID changes Chunk.fillChunk's expected byte layout; the Futura sidecar must never call it. */
    private static void verifyHighSectionDecoderIsolation() throws Exception {
        java.io.InputStream stream=HTGT6Transformer.class.getResourceAsStream("HTGT6Transformer.class");
        require(stream!=null,"HTGT6 transformer bytecode");ClassNode node=new ClassNode();try{new ClassReader(stream).accept(node,0);}finally{stream.close();}
        boolean directDecoder=false,neidSetter=false;
        for(MethodNode method:node.methods){
            if("applyHighSections".equals(method.name))for(AbstractInsnNode instruction:method.instructions.toArray())
                if(instruction instanceof MethodInsnNode&&"fillChunk".equals(((MethodInsnNode)instruction).name))
                    throw new IllegalStateException("Futura sidecar still enters NEID-patched Chunk.fillChunk");
            if("applyHighSectionArrays".equals(method.name)){directDecoder=true;for(AbstractInsnNode instruction:method.instructions.toArray())
                if(instruction instanceof LdcInsnNode&&"setBlockData".equals(((LdcInsnNode)instruction).cst))neidSetter=true;}
        }
        require(directDecoder&&neidSetter,"direct NEID 16-bit high-section decoder");
    }

    private static void verifyNbtRoundTrip() throws IOException {
        NBTTagCompound root = new NBTTagCompound();
        NBTTagList sections = new NBTTagList();
        for (int section = 0; section < FuturaHeight120.SECTION_COUNT; section++) {
            ExtendedBlockStorage storage = fixtureStorage(section);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("Y", (byte) section);
            tag.setByteArray("Blocks", storage.getBlockLSBArray());
            tag.setByteArray("Add", storage.getBlockMSBArray().data);
            tag.setByteArray("Data", storage.getMetadataArray().data);
            tag.setByteArray("BlockLight", storage.getBlocklightArray().data);
            tag.setByteArray("SkyLight", storage.getSkylightArray().data);
            sections.appendTag(tag);
        }
        root.setTag("Sections", sections);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(root, bytes);
        NBTTagCompound decoded = CompressedStreamTools.readCompressed(
                new ByteArrayInputStream(bytes.toByteArray()));
        NBTTagList decodedSections = decoded.getTagList("Sections", 10);
        require(decodedSections.tagCount() == FuturaHeight120.SECTION_COUNT,
                "NBT section count changed during roundtrip");
        boolean[] seen = new boolean[FuturaHeight120.SECTION_COUNT];
        ExtendedBlockStorage[] decodedStorage = new ExtendedBlockStorage[FuturaHeight120.SECTION_COUNT];
        for (int i = 0; i < decodedSections.tagCount(); i++) {
            NBTTagCompound tag = decodedSections.getCompoundTagAt(i);
            int section = FuturaHeight120.sectionIndexFromNbt(tag.getByte("Y"));
            require(section < seen.length, "NBT returned an out-of-range section: " + section);
            ExtendedBlockStorage storage = new ExtendedBlockStorage(section << 4, true);
            storage.setBlockLSBArray(tag.getByteArray("Blocks"));
            storage.setBlockMSBArray(new NibbleArray(tag.getByteArray("Add"), 4));
            storage.setBlockMetadataArray(new NibbleArray(tag.getByteArray("Data"), 4));
            storage.setBlocklightArray(new NibbleArray(tag.getByteArray("BlockLight"), 4));
            storage.setSkylightArray(new NibbleArray(tag.getByteArray("SkyLight"), 4));
            HTGT6Transformer.storeChunkSection120(decodedStorage, section, storage);
            verifyStoragePayload(section, storage);
            seen[section] = true;
        }
        for (int section = 0; section < seen.length; section++) {
            require(seen[section], "NBT lost section " + section);
        }
    }

    private static ExtendedBlockStorage fixtureStorage(int section) {
        ExtendedBlockStorage storage = new ExtendedBlockStorage(section << 4, true);
        storage.setBlockLSBArray(filled(4096, section + 1));
        storage.setBlockMSBArray(new NibbleArray(filled(2048, section ^ 0x5A), 4));
        storage.setBlockMetadataArray(new NibbleArray(filled(2048, section ^ 0x33), 4));
        storage.setBlocklightArray(new NibbleArray(filled(2048, section ^ 0x0F), 4));
        storage.setSkylightArray(new NibbleArray(filled(2048, section ^ 0xF0), 4));
        return storage;
    }

    private static void verifyStoragePayload(int section, ExtendedBlockStorage storage) {
        require(storage.getYLocation() == section << 4, "EBS Y mismatch for section " + section);
        require(allEqual(storage.getBlockLSBArray(), section + 1), "Blocks mismatch for section " + section);
        require(allEqual(storage.getBlockMSBArray().data, section ^ 0x5A), "Add mismatch for section " + section);
        require(allEqual(storage.getMetadataArray().data, section ^ 0x33), "Data mismatch for section " + section);
        require(allEqual(storage.getBlocklightArray().data, section ^ 0x0F), "BlockLight mismatch for section " + section);
        require(allEqual(storage.getSkylightArray().data, section ^ 0xF0), "SkyLight mismatch for section " + section);
    }

    private static byte[] filled(int length, int value) {
        byte[] result = new byte[length];
        java.util.Arrays.fill(result, (byte) value);
        return result;
    }

    private static boolean allEqual(byte[] values, int expected) {
        byte value = (byte) expected;
        for (byte actual : values) if (actual != value) return false;
        return true;
    }

    private static void verifyAsmMatchers(String jarPath) throws IOException {
        ZipFile jar = new ZipFile(jarPath);
        try {
            HTGT6Transformer transformer = new HTGT6Transformer();
            // LaunchWrapper supplies binary/internal names without the resource
            // suffix. Passing the Zip entry name (".class") silently selects
            // no transform and would make this fixture test the original bytes.
            byte[] chunk = transformer.transform(CHUNK_NAME, CHUNK_NAME, read(jar, CHUNK_ENTRY));
            byte[] world = transformer.transform(WORLD_NAME, WORLD_NAME, read(jar, WORLD_ENTRY));
            byte[] anvil = transformer.transform(ANVIL_NAME, ANVIL_NAME, read(jar, ANVIL_ENTRY));
            byte[] provider = transformer.transform(WORLD_PROVIDER_NAME, WORLD_PROVIDER_NAME,
                    read(jar, WORLD_PROVIDER_ENTRY));
            byte[] s21 = transformer.transform(S21_NAME, S21_NAME, read(jar, S21_ENTRY));
            byte[] s26 = transformer.transform(S26_NAME, S26_NAME, read(jar, S26_ENTRY));
            byte[] s22 = transformer.transform(S22_NAME, S22_NAME, read(jar, S22_ENTRY));
            byte[] s23 = transformer.transform(S23_NAME, S23_NAME, read(jar, S23_ENTRY));
            byte[] handler = transformer.transform(CLIENT_HANDLER_NAME, CLIENT_HANDLER_NAME,
                    read(jar, CLIENT_HANDLER_ENTRY));
            byte[] playerInstance = transformer.transform(PLAYER_INSTANCE_NAME, PLAYER_INSTANCE_NAME,
                    read(jar, PLAYER_INSTANCE_ENTRY));
            require(countCalls(chunk, "createChunkStorageArrays120") == 1,
                    "Chunk storage allocation matcher was not applied exactly once");
            require(countCalls(chunk, "createChunkEntityLists120") == 1,
                    "Chunk entity-list allocation matcher was not applied exactly once");
            require(countCalls(chunk, "legacyChunkSectionCount120") == 6,
                    "All six legacy chunk decoding passes must exclude sidecar sections");
            verifySignedLegacyMasks();
            require(countCalls(world, "interactionHeight120") == 7,
                    "World interaction matchers were not applied exactly seven times");
            require(countCalls(chunk, "interactionHeight120") == 2,
                    "Chunk interaction matcher was not applied exactly twice");
            verifyWorldInteractionMethods(world);
            verifyRayCollisionDelegation(world);
            require(countCalls(anvil, "createChunkStorageArrays120") == 1,
                    "Anvil storage allocation matcher was not applied exactly once");
            require(countCalls(anvil, "storeChunkSection120") == 1,
                    "Anvil section store matcher was not applied exactly once");
            require(countCalls(provider, "worldProviderHeight120") == 1,
                    "WorldProvider getHeight matcher was not applied exactly once");
            require(countCalls(provider, "worldProviderActualHeight120") == 1,
                    "WorldProvider getActualHeight matcher was not applied exactly once");
            require(countCalls(world, "worldLightingHeight120") == 5
                            && countCalls(world, "worldLightingMaxY120") == 3,
                    "World actual lighting bound matcher count changed");
            require(countCalls(chunk, "chunkRelightCheckCount120") == 1
                            && countCalls(chunk, "chunkLightingSectionCount120") == 2
                            && countCalls(chunk, "chunkLightingColumnStride120") == 1,
                    "Chunk actual 24-section relight matcher count changed");
            verifyRelightHeightMapIsNotByteMasked(chunk);
            require(countCalls(s21, "captureS21BiomeIds") == 1
                            && countCalls(s21, "readS21BiomeIds") == 1
                            && countCalls(s21, "writeS21BiomeIds") == 1
                            && countCalls(s21, "s21LowSectionMask120") == 1,
                    "S21 actual transform matcher count changed");
            require(countCalls(s26, "captureS26BiomeIds") == 1
                            && countCalls(s26, "readS26BiomeIds") == 1
                            && countCalls(s26, "writeS26BiomeIds") == 1,
                    "S26 actual transform matcher count changed");
            require(countCalls(s22, "readS22Y120") == 1 && countCalls(s22, "writeS22Y120") == 1,
                    "S22 actual transform matcher count changed");
            require(countCalls(s23, "captureS23Y120") == 1 && countCalls(s23, "readS23Y120") == 1
                            && countCalls(s23, "writeS23Y120") == 1,
                    "S23 actual transform matcher count changed");
            require(countCalls(handler, "s22Y120") == 1,
                    "S22 client-handler high-Y matcher was not applied exactly once");
            require(countCalls(playerInstance, "recordFuturaBlockUpdate120") == 1,
                    "PlayerInstance Futura accumulator matcher was not applied exactly once");
            verifyPacketRoundTrip(s21,s26,s22,s23);
            verifyVanillaWriteUsesActualArrayLength(anvil);
        } finally {
            jar.close();
        }
    }

    private static void verifyRelightHeightMapIsNotByteMasked(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        for (MethodNode method : node.methods) {
            if (!"relightBlock".equals(method.name) || !"(III)V".equals(method.desc)) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction.getOpcode() != org.objectweb.asm.Opcodes.IAND) continue;
                AbstractInsnNode previous = instruction.getPrevious();
                while (previous != null && previous.getOpcode() < 0) previous = previous.getPrevious();
                if (previous instanceof org.objectweb.asm.tree.IntInsnNode
                        && ((org.objectweb.asm.tree.IntInsnNode) previous).operand == 255)
                    throw new IllegalStateException("relightBlock still byte-masks an int heightmap");
            }
            return;
        }
        throw new IllegalStateException("Transformed relightBlock was not found");
    }

    private static void verifySignedLegacyMasks() {
        // Reproduce S26 readShort sign extension and NEID's two 8192-byte
        // arrays per selected section, for every mask and both lighting modes.
        for (int wireMask = 0; wireMask <= 65535; wireMask++) {
            int signedMask = (short) wireMask;
            for (boolean sky : new boolean[] {false, true}) {
                int selected = Integer.bitCount(wireMask);
                int bytes = selected * (18432 + (sky ? 2048 : 0)) + 256;
                int cursor = 0;
                int limit = HTGT6Transformer.legacyChunkSectionCount120(24);
                for (int pass = 0; pass < 2; pass++) {
                    for (int section = 0; section < limit; section++) {
                        if ((signedMask & (1 << section)) == 0) continue;
                        require(cursor + 8192 <= bytes, "NEID metadata buffer overflow: " + wireMask);
                        cursor += 8192;
                    }
                }
                require(cursor == selected * 16384, "Sign-extended mask selected high sections");
                require(limit == 16, "Legacy full chunk sync would clear high sections");
            }
        }
        System.out.println("S26 signed-mask regression OK: all 65536 masks, NEID metadata, sky/no-sky, high-section isolation");
    }

    private static void verifyPacketRoundTrip(byte[] s21,byte[] s26,byte[] s22,byte[] s23){
        try{
            Map<String,byte[]> definitions=new HashMap<String,byte[]>();
            definitions.put(S21_NAME.replace('/','.'),s21);definitions.put(S26_NAME.replace('/','.'),s26);
            definitions.put(S22_NAME.replace('/','.'),s22);definitions.put(S23_NAME.replace('/','.'),s23);
            ClassLoader loader=new FixtureLoader(FuturaHeightStorageFixtures120.class.getClassLoader(),definitions);
            verifyS21RoundTrip(loader.loadClass(S21_NAME.replace('/','.')));
            verifyS26RoundTrip(loader.loadClass(S26_NAME.replace('/','.')));
            verifyS22RoundTrip(loader.loadClass(S22_NAME.replace('/','.')));
            verifyS23RoundTrip(loader.loadClass(S23_NAME.replace('/','.')));
        }catch(Exception error){throw new IllegalStateException("Transformed packet client/server roundtrip failed",error);}
    }

    private static void verifyS21RoundTrip(Class<?> type)throws Exception{
        Object outgoing=type.newInstance();set(outgoing,"field_149281_e",new byte[0]);set(outgoing,"field_149285_h",Integer.valueOf(0));
        set(outgoing,"field_149284_a",Integer.valueOf(7));set(outgoing,"field_149282_b",Integer.valueOf(-9));
        set(outgoing,"field_149279_g",Boolean.FALSE);set(outgoing,"field_149283_c",Integer.valueOf(0));
        set(outgoing,"field_149280_d",Integer.valueOf(0));set(outgoing,"htgt6BiomeIds",biomes(300));
        byte[] high=filled(12288,0x6A);set(outgoing,"htgt6HighSectionData",high);
        set(outgoing,"htgt6HighSectionMask",Integer.valueOf(0x00810000));
        set(outgoing,"htgt6HighAddMask",Integer.valueOf(0x00010000));
        Object incoming=packetRoundTrip(type,outgoing);
        require(java.util.Arrays.equals(high,(byte[])field(incoming,"htgt6HighSectionData")),"S21 high payload changed");
        require(((Integer)field(incoming,"htgt6HighSectionMask")).intValue()==0x00810000,"S21 high mask changed");
        require(((Integer)field(incoming,"htgt6HighAddMask")).intValue()==0x00010000,"S21 add mask changed");
        require(java.util.Arrays.equals((int[])field(outgoing,"htgt6BiomeIds"),(int[])field(incoming,"htgt6BiomeIds")),
                "S21 biome sidecar changed");
    }

    private static void verifyS26RoundTrip(Class<?> type)throws Exception{
        Object outgoing=type.newInstance();set(outgoing,"field_149263_e",new byte[0]);set(outgoing,"field_149261_g",Integer.valueOf(0));
        set(outgoing,"field_149266_a",new int[]{2,-4});set(outgoing,"field_149264_b",new int[]{3,8});
        set(outgoing,"field_149265_c",new int[]{0,0});set(outgoing,"field_149262_d",new int[]{0,0});
        set(outgoing,"field_149267_h",Boolean.TRUE);set(outgoing,"htgt6BiomeIds",new int[][]{biomes(400),biomes(500)});
        byte[][] high={filled(4096,0x21),filled(6144,0x42)};int[] masks={0x00010000,0x00800000};
        int[] addMasks={0,0x00800000};set(outgoing,"htgt6HighSectionData",high);
        set(outgoing,"htgt6HighSectionMasks",masks);set(outgoing,"htgt6HighAddMasks",addMasks);
        Object incoming=packetRoundTrip(type,outgoing);byte[][] decoded=(byte[][])field(incoming,"htgt6HighSectionData");
        require(decoded.length==2&&java.util.Arrays.equals(high[0],decoded[0])&&java.util.Arrays.equals(high[1],decoded[1]),
                "S26 high payload changed");
        require(java.util.Arrays.equals(masks,(int[])field(incoming,"htgt6HighSectionMasks")),"S26 high masks changed");
        require(java.util.Arrays.equals(addMasks,(int[])field(incoming,"htgt6HighAddMasks")),"S26 add masks changed");
    }

    private static void verifyS22RoundTrip(Class<?> type)throws Exception{
        Object outgoing=type.newInstance();
        set(outgoing,"field_148925_b",new net.minecraft.world.ChunkCoordIntPair(4,-7));
        set(outgoing,"field_148924_d",Integer.valueOf(3));
        short[] low={(short)0x1200,(short)0x1200,(short)0x127F};
        int[] full={FuturaHeightPacket120.packLocalPosition(1,0,2),
                FuturaHeightPacket120.packLocalPosition(1,256,2),
                FuturaHeightPacket120.packLocalPosition(1,383,2)};
        set(outgoing,"field_148926_c",new byte[]{0x12,0,0,1,0x12,0,0,2,0x12,0x7F,0,3});
        HTGT6Transformer.registerS22Positions120(low,full,3);
        HTGT6Transformer.captureS22Y120(outgoing,3,low);
        byte[] bits={0,1,1};
        Object incoming=packetRoundTrip(type,outgoing);
        require(java.util.Arrays.equals(bits,(byte[])field(incoming,"htgt6FuturaYBits")),"S22 Y bits changed");
        require(HTGT6Transformer.s22Y120(127,incoming,0)==127,"S22 low Y changed");
        require(HTGT6Transformer.s22Y120(0,incoming,1)==256,"S22 bit8 was not restored");
        require(HTGT6Transformer.s22Y120(127,incoming,2)==383,"S22 maximum Y was not restored");
        short[] overflowLow=new short[130];int[] overflowFull=new int[130];
        for(int i=0;i<130;i++){int y=i<128?i:256+i-128;overflowLow[i]=(short)(y&255);
            overflowFull[i]=FuturaHeightPacket120.packLocalPosition(0,y,0);}
        HTGT6Transformer.registerS22Positions120(overflowLow,overflowFull,130);
        Object overflow=type.newInstance();HTGT6Transformer.captureS22Y120(overflow,130,overflowLow);
        require(((byte[])field(overflow,"htgt6FuturaYBits")).length==130,
                "S22 accumulator overflow fixture lost entries");
    }

    private static void verifyS23RoundTrip(Class<?> type)throws Exception{
        Object outgoing=type.newInstance();set(outgoing,"field_148887_a",Integer.valueOf(12));
        set(outgoing,"field_148885_b",Integer.valueOf(383));set(outgoing,"field_148886_c",Integer.valueOf(-2));
        set(outgoing,"field_148883_d",net.minecraft.init.Blocks.stone);
        set(outgoing,"field_148884_e",Integer.valueOf(5));set(outgoing,"htgt6FuturaY",Boolean.TRUE);
        Object incoming=packetRoundTrip(type,outgoing);
        require(((Integer)field(incoming,"field_148885_b")).intValue()==383,"S23 bit8 was not restored");
        require(Boolean.TRUE.equals(field(incoming,"htgt6FuturaY")),"S23 Futura sidecar marker was lost");
    }

    private static Object packetRoundTrip(Class<?> type,Object outgoing)throws Exception{
        net.minecraft.network.PacketBuffer buffer=new net.minecraft.network.PacketBuffer(io.netty.buffer.Unpooled.buffer());
        Method write=type.getMethod("writePacketData",net.minecraft.network.PacketBuffer.class);
        Method read=type.getMethod("readPacketData",net.minecraft.network.PacketBuffer.class);
        write.invoke(outgoing,buffer);buffer.readerIndex(0);Object incoming=type.newInstance();read.invoke(incoming,buffer);
        require(buffer.readableBytes()==0,type.getName()+" left "+buffer.readableBytes()+" unread bytes");return incoming;
    }

    private static int[] biomes(int base){int[] values=new int[256];for(int i=0;i<values.length;i++)values[i]=base+i;return values;}
    private static Object field(Object owner,String name)throws Exception{Field field=owner.getClass().getDeclaredField(name);
        field.setAccessible(true);return field.get(owner);}
    private static void set(Object owner,String name,Object value)throws Exception{Field field=owner.getClass().getDeclaredField(name);
        field.setAccessible(true);field.set(owner,value);}

    private static final class FixtureLoader extends ClassLoader{
        private final Map<String,byte[]> definitions;
        FixtureLoader(ClassLoader parent,Map<String,byte[]> definitions){super(parent);this.definitions=definitions;}
        @Override protected synchronized Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException{
            Class<?> loaded=findLoadedClass(name);if(loaded==null&&definitions.containsKey(name)){
                byte[] bytes=definitions.get(name);loaded=defineClass(name,bytes,0,bytes.length);
            }else if(loaded==null)loaded=super.loadClass(name,false);
            if(resolve)resolveClass(loaded);return loaded;
        }
    }

    private static void verifyBuiltManifest(String jarPath) throws IOException {
        ZipFile jar = new ZipFile(jarPath);
        try {
            java.util.jar.Manifest manifest = new java.util.jar.Manifest(
                    jar.getInputStream(jar.getEntry("META-INF/MANIFEST.MF")));
            String value = manifest.getMainAttributes().getValue("FMLCorePluginContainsFMLMod");
            require("true".equalsIgnoreCase(value),
                    "Built manifest FMLCorePluginContainsFMLMod must be true, got " + value);
        } finally {
            jar.close();
        }
    }

    private static void verifyVanillaWriteUsesActualArrayLength(byte[] bytes) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        for (MethodNode method : node.methods) {
            if (!"writeChunkToNBT".equals(method.name)) continue;
            boolean arrayLength = false;
            boolean writesY = false;
            boolean readsChunkSections = false;
            boolean writesByte = false;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction.getOpcode() == org.objectweb.asm.Opcodes.ARRAYLENGTH) arrayLength = true;
                if (instruction instanceof LdcInsnNode
                        && "Y".equals(((LdcInsnNode) instruction).cst)) writesY = true;
                if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if ("net/minecraft/world/chunk/Chunk".equals(call.owner)
                            && "getBlockStorageArray".equals(call.name)
                            && "()[Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;".equals(call.desc)) {
                        readsChunkSections = true;
                    }
                    if ("net/minecraft/nbt/NBTTagCompound".equals(call.owner)
                            && "setByte".equals(call.name)
                            && "(Ljava/lang/String;B)V".equals(call.desc)) writesByte = true;
                }
            }
            require(arrayLength && writesY && readsChunkSections && writesByte,
                    "Anvil write matcher fixture no longer sees length-based Section.Y serialization");
            return;
        }
        throw new IllegalStateException("writeChunkToNBT not found in transformed AnvilChunkLoader");
    }

    private static int countCalls(byte[] bytes, String name) {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        int count = 0;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (TRANSFORMER.equals(call.owner) && name.equals(call.name)) count++;
                }
            }
        }
        return count;
    }

    private static boolean interactionY(boolean futura, int y) {
        return y >= 0 && y < FuturaHeight120.interactionHeight(futura);
    }

    private static void verifyWorldInteractionMethods(byte[] bytes) {
        ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0);
        String[] expected={"getBlock(III)Lnet/minecraft/block/Block;","blockExists(III)Z",
                "checkChunksExist(IIIIII)Z","setBlock(IIILnet/minecraft/block/Block;II)Z",
                "getBlockMetadata(III)I","setBlockMetadataWithNotify(IIIII)Z",
                "getTileEntity(III)Lnet/minecraft/tileentity/TileEntity;"};
        for(String signature:expected){int count=0;for(MethodNode method:node.methods)
            if(signature.equals(method.name+method.desc))for(AbstractInsnNode insn:method.instructions.toArray())
                if(insn instanceof MethodInsnNode&&TRANSFORMER.equals(((MethodInsnNode)insn).owner)
                        &&"interactionHeight120".equals(((MethodInsnNode)insn).name))count++;
            require(count==1,"World interaction method matcher changed: "+signature+" calls="+count);}
    }

    private static void verifyRayCollisionDelegation(byte[] bytes) {
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
        boolean rayBlock=false,rayMeta=false,collisionExists=false,collisionBlock=false;
        for(MethodNode method:node.methods){boolean ray=("func_147447_a".equals(method.name)
                    &&"(Lnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;ZZZ)Lnet/minecraft/util/MovingObjectPosition;".equals(method.desc));
            boolean collision="getCollidingBoundingBoxes".equals(method.name)
                    &&"(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;".equals(method.desc);
            if(!ray&&!collision)continue;for(AbstractInsnNode insn:method.instructions.toArray())if(insn instanceof MethodInsnNode){
                MethodInsnNode call=(MethodInsnNode)insn;
                if(ray&&"getBlock".equals(call.name))rayBlock=true;
                if(ray&&"getBlockMetadata".equals(call.name))rayMeta=true;
                if(collision&&"blockExists".equals(call.name))collisionExists=true;
                if(collision&&"getBlock".equals(call.name))collisionBlock=true;}}
        require(rayBlock&&rayMeta,"Ray trace no longer delegates through patched block accessors");
        require(collisionExists&&collisionBlock,"Entity collision no longer delegates through patched World bounds");
    }

    private static byte[] read(ZipFile jar, String path) throws IOException {
        ZipEntry entry = jar.getEntry(path);
        if (entry == null) throw new IOException("Missing " + path + " in " + jar.getName());
        java.io.InputStream input = jar.getInputStream(entry);
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream((int) entry.getSize());
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) output.write(buffer, 0, read);
            return output.toByteArray();
        } finally {
            input.close();
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private FuturaHeightStorageFixtures120() { }
}
