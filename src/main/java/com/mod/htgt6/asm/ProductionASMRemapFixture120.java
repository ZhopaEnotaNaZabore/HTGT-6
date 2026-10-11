package com.mod.htgt6.asm;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Exercises HTGT6 against the actual notch-named Minecraft 1.7.10 classes. */
public final class ProductionASMRemapFixture120 {
    private static final String[] TARGETS = {
            "net/minecraft/world/Explosion",
            "net/minecraft/block/BlockSoulSand",
            "net/minecraft/block/BlockLiquid",
            "net/minecraft/block/BlockWall",
            "net/minecraft/block/BlockRedstoneWire",
            "net/minecraft/block/BlockStairs",
            "net/minecraft/block/BlockPistonBase",
            "net/minecraft/tileentity/TileEntityPiston",
            "net/minecraft/entity/item/EntityItem",
            "net/minecraft/network/NetworkManager",
            "net/minecraft/entity/projectile/EntityArrow",
            "net/minecraft/entity/projectile/EntityThrowable",
            "net/minecraft/entity/projectile/EntityFireball",
            "net/minecraft/entity/projectile/EntityFishHook",
            "net/minecraft/item/ItemBoat",
            "net/minecraft/entity/item/EntityBoat",
            "net/minecraft/entity/monster/EntityEnderman",
            "net/minecraft/tileentity/TileEntityFurnace",
            "net/minecraft/tileentity/TileEntityBrewingStand",
            "net/minecraft/entity/Entity",
            "net/minecraft/entity/EntityLivingBase",
            "net/minecraft/item/ItemSword",
            "net/minecraft/item/ItemHoe",
            "net/minecraft/enchantment/Enchantment",
            "net/minecraft/enchantment/EnchantmentHelper",
            "net/minecraft/enchantment/EnchantmentDamage",
            "net/minecraft/entity/item/EntityEnderPearl",
            "net/minecraft/block/BlockCactus",
            "net/minecraft/block/BlockReed",
            "net/minecraft/item/ItemLilyPad",
            "net/minecraft/block/BlockMushroom",
            "net/minecraft/block/BlockLilyPad",
            "net/minecraft/potion/Potion",
            "net/minecraft/potion/PotionEffect",
            "net/minecraft/network/play/server/S1DPacketEntityEffect",
            "net/minecraft/network/play/server/S1EPacketRemoveEntityEffect",
            "net/minecraft/crash/CrashReport",
            "net/minecraft/crash/CrashReportCategory",
            "net/minecraft/client/Minecraft",
            "net/minecraft/client/settings/KeyBinding",
            "net/minecraft/entity/player/InventoryPlayer",
            "net/minecraft/entity/player/EntityPlayer",
            "net/minecraft/client/entity/EntityPlayerSP",
            "net/minecraft/entity/monster/EntityZombie",
            "net/minecraft/tileentity/MobSpawnerBaseLogic",
            "net/minecraft/entity/passive/EntityVillager",
            "net/minecraft/entity/passive/EntityOcelot",
            "net/minecraft/entity/passive/EntityHorse",
            "net/minecraft/inventory/ContainerPlayer",
            "net/minecraft/inventory/ContainerPlayer$1",
            "net/minecraft/client/renderer/ItemRenderer",
            "net/minecraft/client/renderer/EntityRenderer",
            "net/minecraft/client/renderer/entity/RenderPlayer",
            "net/minecraft/client/model/ModelBiped",
            "net/minecraft/network/NetHandlerPlayServer",
            "net/minecraft/server/management/PlayerManager$PlayerInstance",
            "net/minecraft/world/WorldProviderEnd",
            "net/minecraft/world/gen/ChunkProviderGenerate",
            "net/minecraft/world/chunk/storage/ExtendedBlockStorage",
            "net/minecraft/client/gui/inventory/GuiContainerCreative",
            "net/minecraft/item/ItemMap",
            "net/minecraft/world/World",
            "net/minecraft/world/biome/BiomeGenBase",
            "net/minecraft/client/renderer/RenderGlobal",
            "net/minecraft/client/renderer/WorldRenderer",
            "net/minecraft/client/multiplayer/WorldClient",
            "net/minecraft/client/network/NetHandlerPlayClient",
            "net/minecraft/world/chunk/Chunk",
            "net/minecraft/world/chunk/storage/AnvilChunkLoader",
            "net/minecraft/network/play/server/S21PacketChunkData",
            "net/minecraft/network/play/server/S26PacketMapChunkBulk",
            "net/minecraft/network/play/server/S22PacketMultiBlockChange",
            "net/minecraft/network/play/server/S23PacketBlockChange"
    };

    public static void main(String[] args) throws Exception {
        if (args.length < 3 || args.length > 5)
            throw new IllegalArgumentException("Expected mappings.lzma, client.jar, forge.jar"
                    + ", optional server.jar and optional Forge-patched jar");
        File minecraftFile = new File(args[1]);
        LaunchClassLoader loader = new LaunchClassLoader(new URL[] { minecraftFile.toURI().toURL() });
        FMLDeobfuscatingRemapper.INSTANCE.setup(new File("."), loader,
                "/deobfuscation_data-1.7.10.lzma");
        RuntimeBytecodeRemapper.enableProductionBridge();
        HTGT6Transformer transformer = new HTGT6Transformer();
        ZipFile minecraft = new ZipFile(minecraftFile);
        try {
            for (String target : TARGETS) verifyTarget(minecraft, transformer, target);
        } finally {
            minecraft.close();
        }
        verifyArmorTrimForgeHook(new File(args[2]), transformer);
        verifyForgeBiomeDictionary(new File(args[2]), transformer);
        verifyForgeSaveDirectory(new File(args[2]), transformer);
        int serverTargets = args.length >= 4
                ? verifyDedicatedServer(new File(args[3]), transformer) : 0;
        int forgePatchedTargets = args.length == 5
                ? verifyForgePatchedMinecraft(new File(args[4]), transformer) : 0;
        System.out.println("Production ASM namespace bridge OK: " + TARGETS.length
                + " client classes, " + serverTargets
                + " dedicated-server classes, " + forgePatchedTargets
                + " Forge-patched Minecraft classes and Forge BiomeDictionary");
    }

    /** Reproduce MicdoodleCore 3.0.12's constant replacement on real vanilla bytecode. */
    private static void verifyGalacticraftLevitation(byte[] input, HTGT6Transformer transformer,
                                                    String target) throws Exception {
        ClassNode node=new ClassNode();new ClassReader(input).accept(node,0);
        int replacements=0;
        for(MethodNode method:node.methods)if(method.name.equals("func_70612_e")&&method.desc.equals("(FF)V"))
            for(AbstractInsnNode instruction:method.instructions.toArray())
                if(instruction instanceof org.objectweb.asm.tree.LdcInsnNode
                        && Double.valueOf(.08D).equals(((org.objectweb.asm.tree.LdcInsnNode)instruction).cst)) {
                    method.instructions.insertBefore(instruction,new org.objectweb.asm.tree.VarInsnNode(Opcodes.ALOAD,0));
                    method.instructions.set(instruction,new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "micdoodle8/mods/galacticraft/core/util/WorldUtil","getGravityForEntity",
                            "(Lnet/minecraft/entity/Entity;)D",false));replacements++;
                }
        require(replacements==1,"Galacticraft gravity fixture drift: "+replacements);
        org.objectweb.asm.ClassWriter writer=new org.objectweb.asm.ClassWriter(org.objectweb.asm.ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        byte[] result=transformer.transform(target.replace('/','.'),target.replace('/','.'),writer.toByteArray());
        node=new ClassNode();new ClassReader(result).accept(node,0);int hooks=0;
        for(MethodNode method:node.methods)if(method.name.equals("func_70612_e")) {
            new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicInterpreter()).analyze(node.name,method);
            for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction instanceof MethodInsnNode) {
                MethodInsnNode call=(MethodInsnNode)instruction;
                if(!call.name.equals("getGravityForEntity"))continue;
                AbstractInsnNode load=instruction.getNext(),hook=load==null?null:load.getNext();
                require(load instanceof org.objectweb.asm.tree.VarInsnNode&&load.getOpcode()==Opcodes.ALOAD
                        &&((org.objectweb.asm.tree.VarInsnNode)load).var==0,"Missing levitation receiver after Galacticraft gravity");
                require(hook instanceof MethodInsnNode&&((MethodInsnNode)hook).name.equals("gravityAdjustment")
                        &&((MethodInsnNode)hook).desc.equals("(DLnet/minecraft/entity/EntityLivingBase;)D"),
                        "Galacticraft gravity not wrapped by levitation");hooks++;
            }
        }
        require(hooks==1,"Galacticraft levitation hooks="+hooks);
        System.out.println("Galacticraft gravity + levitation ASM composition OK");
    }

    private static void verifyObserverHooks(byte[] bytes) throws Exception {
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);
        java.util.Set<String> methods=new java.util.HashSet<String>();
        for(MethodNode method:node.methods)for(AbstractInsnNode instruction:method.instructions.toArray()) {
            if(instruction instanceof MethodInsnNode&&((MethodInsnNode)instruction).owner.endsWith("/ObserverUpdates120")) {
                methods.add(method.name);
                new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicInterpreter()).analyze(node.name,method);
            }
        }
        require(methods.size()==2,"Observer must instrument block and metadata changes: "+methods);
    }

    private static int verifyForgePatchedMinecraft(File forgeBin,
            HTGT6Transformer transformer) throws Exception {
        String[] targets = {
                "net/minecraft/server/management/ServerConfigurationManager",
                "net/minecraft/world/World",
                "net/minecraft/world/WorldProvider",
                "net/minecraft/world/storage/SaveHandler",
                "net/minecraft/entity/player/EntityPlayer",
                "net/minecraft/client/entity/EntityPlayerSP",
                "net/minecraft/entity/EntityLivingBase",
                "net/minecraft/tileentity/MobSpawnerBaseLogic"
        };
        ZipFile archive = new ZipFile(forgeBin);
        try {
            for (String target : targets) {
                ZipEntry entry = archive.getEntry(target + ".class");
                require(entry != null, "Missing Forge-patched class " + target);
                byte[] mcp = read(archive.getInputStream(entry));
                byte[] srg = RuntimeBytecodeRemapper.toSrg(mcp);
                byte[] patched = transformer.transform(target, target, srg);
                if(target.endsWith("ServerConfigurationManager")||target.endsWith("EntityPlayer")) {
                    byte[] once=RespawnAnchorASM116.transform(srg,target.endsWith("EntityPlayer"));
                    require(java.util.Arrays.equals(once,RespawnAnchorASM116.transform(once,target.endsWith("EntityPlayer"))),"Anchor SRG idempotence");
                    ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);int hooks=0;
                    for(MethodNode method:node.methods)for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction instanceof MethodInsnNode&&((MethodInsnNode)instruction).owner.endsWith("/RespawnAnchor116")){
                        hooks++;new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(node.name,method);
                    }
                    require(hooks==(target.endsWith("EntityPlayer")?1:6),"Anchor production hook count "+target+": "+hooks);
                }
                if(target.equals("net/minecraft/world/World"))verifyObserverHooks(patched);
                verifyShieldApiHooks(target, patched);
                verifySwimmingPoseHooks(target,patched);
                if(target.endsWith("EntityLivingBase"))verifySurfaceBreathing(patched);
                if(target.endsWith("EntityLivingBase")||target.endsWith("MobSpawnerBaseLogic")) {
                    String hook=target.endsWith("EntityLivingBase")?"airSupply":"canSpawnFromSpawner";
                    ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);int found=0;
                    for(MethodNode method:node.methods)for(AbstractInsnNode instruction:method.instructions.toArray())
                        if(instruction instanceof MethodInsnNode&&((MethodInsnNode)instruction).name.equals(hook)) {
                            found++;
                            new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicInterpreter()).analyze(node.name,method);
                        }
                    require(found==1,"Forge-patched hook missing: "+hook);
                }
                require(target.equals(new ClassReader(patched).getClassName()),
                        "Forge-patched class name drift: " + target);
            }
            return targets.length;
        } finally {
            archive.close();
        }
    }

    private static int verifyDedicatedServer(File serverFile, HTGT6Transformer transformer)
            throws Exception {
        ZipFile server = new ZipFile(serverFile);
        try {
            int checked = 0;
            for (String target : TARGETS) {
                String notch = FMLDeobfuscatingRemapper.INSTANCE.unmap(target);
                if (server.getEntry(notch + ".class") == null) continue;
                verifyTarget(server, transformer, target);
                checked++;
            }
            require(checked > 0, "No dedicated-server ASM targets were found");
            return checked;
        } finally {
            server.close();
        }
    }

    private static void verifyForgeSaveDirectory(File forgeFile, HTGT6Transformer transformer) throws Exception {
        String target = "net/minecraftforge/common/DimensionManager";
        try (ZipFile forge = new ZipFile(forgeFile)) {
            byte[] raw = read(forge.getInputStream(forge.getEntry(target + ".class")));
            byte[] patched = transformer.transform(target, target, raw);
            ClassNode node = new ClassNode();
            new ClassReader(RuntimeBytecodeRemapper.toSrg(patched)).accept(node, 0);
            int hooks = 0;
            for (MethodNode method : node.methods) if (method.name.equals("getCurrentSaveRootDirectory")) {
                new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicInterpreter()).analyze(node.name, method);
                for (AbstractInsnNode instruction : method.instructions.toArray()) if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    require(call.owner.equals("com/mod/htgt6/asm/WorldSaveDirectoryHooks") && call.name.equals("current"), "production directory lookup call");
                    hooks++;
                }
            }
            require(hooks == 1, "production directory hook count=" + hooks);
        }
        System.out.println("Forge production save-directory ASM OK");
    }

    private static void verifyArmorTrimForgeHook(File forgeFile,HTGT6Transformer transformer)throws Exception{
        try(ZipFile forge=new ZipFile(forgeFile)){
            String target="net/minecraftforge/client/ForgeHooksClient";
            byte[] raw=read(forge.getInputStream(forge.getEntry(target+".class")));
            byte[] patched=RuntimeBytecodeRemapper.toSrg(transformer.transform(target.replace('/','.'),target.replace('/','.'),raw));
            ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);int hooks=0;
            for(MethodNode method:node.methods)for(AbstractInsnNode ins:method.instructions.toArray())if(ins instanceof MethodInsnNode){
                MethodInsnNode call=(MethodInsnNode)ins;if(call.owner.endsWith("/ArmorTrimTexture120")){
                    require(call.desc.equals("(Ljava/lang/String;Lnet/minecraft/item/ItemStack;ILjava/lang/String;)Ljava/lang/String;"),"armor trim descriptor after Forge remap");hooks++;
                }
            }
            require(hooks>0,"Forge armor trim hook absent");
        }
    }
    private static void verifyForgeBiomeDictionary(File forgeFile, HTGT6Transformer transformer)
            throws Exception {
        ZipFile forge = new ZipFile(forgeFile);
        try {
            String target = "net/minecraftforge/common/BiomeDictionary";
            ZipEntry entry = forge.getEntry(target + ".class");
            require(entry != null, "Missing Forge BiomeDictionary");
            byte[] raw = read(forge.getInputStream(entry));
            byte[] patched = transformer.transform(target.replace('/', '.'),
                    target.replace('/', '.'), raw);

            // The normal FML deobfuscator runs later and maps Forge's embedded
            // notch descriptors to SRG. Verify that the injected call survives
            // that exact transition and targets the runtime helper.
            byte[] srg = RuntimeBytecodeRemapper.toSrg(patched);
            ClassNode node = new ClassNode();
            new ClassReader(srg).accept(node, 0);
            int hooks = 0;
            for (MethodNode method : node.methods) {
                if (!"registerBiomeType".equals(method.name)) continue;
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (call.getOpcode() == Opcodes.INVOKESTATIC
                            && "com/mod/htgt6/asm/HTGT6Transformer".equals(call.owner)
                            && "registerExtendedBiomeTypes".equals(call.name)) hooks++;
                }
            }
            require(hooks == 1, "Forge BiomeDictionary registration hooks=" + hooks);
        } finally {
            forge.close();
        }
    }

    static void verifyPotionBufferLinkage(ZipFile minecraft, byte[] packetBytes) throws Exception {
        String owner = "net/minecraft/network/PacketBuffer";
        String notch = FMLDeobfuscatingRemapper.INSTANCE.unmap(owner);
        ClassNode buffer = new ClassNode();
        new ClassReader(RuntimeBytecodeRemapper.toSrg(read(minecraft.getInputStream(
                minecraft.getEntry(notch + ".class"))))).accept(buffer, 0);
        ClassNode packet = new ClassNode(); new ClassReader(packetBytes).accept(packet, 0);
        int calls = 0;
        for (MethodNode method : packet.methods) for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (!(insn instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) insn;
            if (!owner.equals(call.owner) || !(call.name.contains("VarInt")
                    || call.name.equals("func_150792_a") || call.name.equals("func_150787_b"))) continue;
            boolean found = false;
            for (MethodNode candidate : buffer.methods)
                if (candidate.name.equals(call.name) && candidate.desc.equals(call.desc)) found = true;
            require(found, "Unresolved production PacketBuffer call: " + call.name + call.desc);
            calls++;
        }
        require(calls == 2, "Expected VarInt reader and writer in " + packet.name);
    }

    private static void verifyTarget(ZipFile minecraft, HTGT6Transformer transformer,
                                     String target) throws Exception {
        String notch = FMLDeobfuscatingRemapper.INSTANCE.unmap(target);
        ZipEntry entry = minecraft.getEntry(notch + ".class");
        require(entry != null, "Missing notch class for " + target + ": " + notch);
        byte[] original = read(minecraft.getInputStream(entry));
        byte[] srg = RuntimeBytecodeRemapper.toSrg(original);
        if(target.equals("net/minecraft/entity/player/EntityPlayer")) {
            ClassNode player=new ClassNode();new ClassReader(srg).accept(player,0);
            boolean use=false,count=false;
            for(org.objectweb.asm.tree.FieldNode field:player.fields) {
                if(field.name.equals("field_71074_e")&&field.desc.equals("Lnet/minecraft/item/ItemStack;"))use=true;
                if(field.name.equals("field_71072_f")&&field.desc.equals("I"))count=true;
            }
            require(use&&count,"Player item-use backing fields absent from "+minecraft.getName());
        }
        require(target.equals(new ClassReader(srg).getClassName()), "Forward class mapping failed: " + target);
        if(target.equals("net/minecraft/block/BlockSoulSand")||target.equals("net/minecraft/world/Explosion")) {
            byte[] once=target.endsWith("Explosion")?AnchorExplosionASM116.transform(srg):SoulSpeedASM116.transform(srg);
            require(java.util.Arrays.equals(once,target.endsWith("Explosion")?AnchorExplosionASM116.transform(once):SoulSpeedASM116.transform(once)),"Nether utility SRG idempotence");
            ClassNode node=new ClassNode();new ClassReader(once).accept(node,0);
            for(MethodNode method:node.methods)new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(node.name,method);
        }
        if(target.equals("net/minecraft/block/BlockStairs")) {
            byte[] once=BlackstoneStairsASM116.transform(srg);
            require(java.util.Arrays.equals(once,BlackstoneStairsASM116.transform(once)),"Stair SRG idempotence");
            ClassNode node=new ClassNode();new ClassReader(once).accept(node,0);
            for(MethodNode method:node.methods)new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(node.name,method);
        }
        if(target.equals("net/minecraft/entity/projectile/EntityFishHook")) {
            byte[] once=OpenWaterFishingASM116.transform(srg);require(java.util.Arrays.equals(once,OpenWaterFishingASM116.transform(once)),"Open-water ASM idempotence");ClassNode node=new ClassNode();new ClassReader(once).accept(node,0);for(MethodNode method:node.methods)new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(node.name,method);
        }
        if(target.equals("net/minecraft/block/BlockRedstoneWire")) {
            byte[] once=RedstoneWireASM116.transform(srg);require(java.util.Arrays.equals(once,RedstoneWireASM116.transform(once)),"Native redstone ASM idempotence");ClassNode node=new ClassNode();new ClassReader(once).accept(node,0);for(MethodNode method:node.methods)new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(node.name,method);
        }
        if(target.equals("net/minecraft/block/BlockWall")) {
            byte[] once=NativeWallASM116.transform(srg);require(java.util.Arrays.equals(once,NativeWallASM116.transform(once)),"Native wall ASM idempotence");ClassNode node=new ClassNode();new ClassReader(once).accept(node,0);for(MethodNode method:node.methods)new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(node.name,method);
        }
        if(target.equals("net/minecraft/block/BlockLiquid")) {
            byte[] once=LavaMixingASM116.transform(srg);require(java.util.Arrays.equals(once,LavaMixingASM116.transform(once)),"Liquid mixing ASM idempotence");ClassNode node=new ClassNode();new ClassReader(once).accept(node,0);for(MethodNode method:node.methods)new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(node.name,method);
        }
        if(target.equals("net/minecraft/item/ItemHoe")) {
            byte[] once=HoeMiningASM116.transform(srg);
            require(java.util.Arrays.equals(once,HoeMiningASM116.transform(once)),"Hoe mining SRG idempotence");
            ClassNode node=new ClassNode();new ClassReader(once).accept(node,0);
            boolean destroy=false;for(MethodNode method:node.methods){if(method.name.equals("func_150894_a"))destroy=true;new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(node.name,method);}
            require(destroy,"Hoe mining uses SRG overrides in production");
        }
        if(target.equals("net/minecraft/enchantment/Enchantment")) {
            byte[] once=NetheriteHoeEnchantmentASM116.transform(srg);
            require(java.util.Arrays.equals(once,NetheriteHoeEnchantmentASM116.transform(once)),"Hoe applicability ASM idempotence");
            ClassNode enchantment=new ClassNode();new ClassReader(once).accept(enchantment,0);
            for(MethodNode method:enchantment.methods)new org.objectweb.asm.tree.analysis.Analyzer(
                    new org.objectweb.asm.tree.analysis.BasicInterpreter()).analyze(enchantment.name,method);
        }
        if(target.equals("net/minecraft/entity/projectile/EntityArrow")||target.equals("net/minecraft/entity/projectile/EntityThrowable")
                ||target.equals("net/minecraft/entity/projectile/EntityFireball")||target.equals("net/minecraft/entity/projectile/EntityFishHook")){
            byte[] once=TargetImpactASM116.transform(srg),twice=TargetImpactASM116.transform(once);
            require(java.util.Arrays.equals(once,twice),"Target ASM idempotence: "+target);
            ClassNode projectile=new ClassNode();new ClassReader(twice).accept(projectile,0);
            for(MethodNode method:projectile.methods)new org.objectweb.asm.tree.analysis.Analyzer(
                    new org.objectweb.asm.tree.analysis.BasicInterpreter()).analyze(projectile.name,method);
        }
        if(target.equals("net/minecraft/entity/item/EntityItem")) {
            byte[] once=NetheriteItemASM120.transform(srg),twice=NetheriteItemASM120.transform(once);
            require(java.util.Arrays.equals(once,twice),"EntityItem ASM must be idempotent: "+minecraft.getName());
            ClassNode item=new ClassNode();new ClassReader(twice).accept(item,0);
            for(MethodNode method:item.methods)new org.objectweb.asm.tree.analysis.Analyzer(
                    new org.objectweb.asm.tree.analysis.BasicInterpreter()).analyze(item.name,method);
        }
        if(target.equals("net/minecraft/entity/EntityLivingBase"))
            verifyGalacticraftLevitation(srg, transformer, target);
        boolean loading = HTGT6Config.ENABLE_NEW_FML_LOADING_SCREEN;
        boolean branding = HTGT6Config.ENABLE_WINDOW_BRANDING;
        if ("net/minecraft/client/Minecraft".equals(target)) {
            // The standalone vanilla jar has not received Forge's
            // beginMinecraftLoading call injection. This fixture checks the
            // creative-slot ASM here; loading-screen hooks have their own fixture.
            HTGT6Config.ENABLE_NEW_FML_LOADING_SCREEN = false;
            HTGT6Config.ENABLE_WINDOW_BRANDING = false;
        }
        byte[] patched;
        try {
            patched = transformer.transform(notch.replace('/', '.'), target.replace('/', '.'), srg);
        } finally {
            HTGT6Config.ENABLE_NEW_FML_LOADING_SCREEN = loading;
            HTGT6Config.ENABLE_WINDOW_BRANDING = branding;
        }
        if(target.endsWith("/BlockPistonBase")||target.endsWith("/TileEntityPiston")) {
            ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);int hooks=0;
            for(MethodNode m:node.methods) {
                for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).owner.equals(StandalonePistonsASM115.HOOK))hooks++;
                if(m.name.equals("func_150078_e")||m.name.equals("func_149696_a")||m.name.equals("func_145845_h"))new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicInterpreter()).analyze(node.name,m);
            }
            require(hooks==(target.endsWith("/BlockPistonBase")?2:1),"Independent piston hooks missing");
            require(java.util.Arrays.equals(patched,StandalonePistonsASM115.transform(patched,target.endsWith("/TileEntityPiston"))),"Piston ASM must be idempotent");
        }
        byte[] restored = RuntimeBytecodeRemapper.toNotch(patched);
        verifySwimmingPoseHooks(target,patched);
        if (target.endsWith("/S1DPacketEntityEffect") || target.endsWith("/S1EPacketRemoveEntityEffect"))
            verifyPotionBufferLinkage(minecraft, RuntimeBytecodeRemapper.toSrg(restored));
        if(target.equals("net/minecraft/entity/EntityLivingBase")) {
            verifySurfaceBreathing(patched);
            ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);int found=0;
            for(MethodNode method:node.methods) {
                for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction instanceof MethodInsnNode
                        &&((MethodInsnNode)instruction).name.equals("airSupply"))found++;
                if(method.name.equals("func_70682_h"))
                    new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicInterpreter()).analyze(node.name,method);
            }
            require(found==1,"Bubble breathing hook missing");
        }
        if(target.equals("net/minecraft/entity/projectile/EntityArrow")||target.equals("net/minecraft/entity/monster/EntityZombie")
                ||target.endsWith("MobSpawnerBaseLogic")) {
            ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);int found=0;
            String expected=target.endsWith("EntityArrow")?"damageTarget":target.endsWith("MobSpawnerBaseLogic")?"canSpawnFromSpawner":"solarBurnAllowed";
            for(MethodNode method:node.methods) {
                for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction instanceof MethodInsnNode
                        &&((MethodInsnNode)instruction).name.equals(expected))found++;
                if(method.name.equals("func_70071_h_")||method.name.equals("func_70636_d")||method.name.equals("func_98278_g"))
                    new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicInterpreter()).analyze(node.name,method);
            }
            require(found==1,"Frostburn hook missing: "+expected);
        }
        verifyShieldApiHooks(target, patched);
        if ("net/minecraft/client/renderer/ItemRenderer".equals(target)) verifyShieldRenderHook(patched);
        require(notch.equals(new ClassReader(restored).getClassName()), "Reverse class mapping failed: " + target);
        byte[] roundTrip = RuntimeBytecodeRemapper.toSrg(restored);
        if (target.equals("net/minecraft/world/World")) {
            verifyObserverHooks(roundTrip);
            ClassNode world = new ClassNode();new ClassReader(roundTrip).accept(world,0);
            int reports=0;
            for(MethodNode method:world.methods)for(AbstractInsnNode instruction:method.instructions.toArray()) {
                if(!(instruction instanceof MethodInsnNode))continue;
                MethodInsnNode call=(MethodInsnNode)instruction;
                if(!call.owner.equals("net/minecraft/crash/CrashReport")
                        || !call.desc.equals("(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/crash/CrashReport;"))continue;
                AbstractInsnNode previous=instruction.getPrevious();
                while(previous!=null&&previous.getOpcode()<0)previous=previous.getPrevious();
                require(previous instanceof MethodInsnNode
                        && ((MethodInsnNode)previous).name.equals("logOriginalWorldFailure120"),
                        "World failure lost before CrashReport in "+method.name);
                require(previous.getPrevious().getOpcode()==Opcodes.DUP2,"report arguments not preserved");
                reports++;
            }
            require(reports>0,"World failure reporting sites not exercised");
        }
        if(target.equals("net/minecraft/block/BlockCactus")||target.equals("net/minecraft/block/BlockReed")) {
            boolean cactus=target.endsWith("BlockCactus");String helper=cactus?"cactusSurvival120":"caneSurvival120";
            ClassNode source=new ClassNode();new ClassReader(srg).accept(source,0);int returns=0;
            for(MethodNode method:source.methods)if(cactus?method.name.equals("func_149718_j"):method.name.equals("func_149742_c"))
                for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction.getOpcode()==Opcodes.IRETURN)returns++;
            require(returns>0&&PlantHeightASMFixture120.callCount(roundTrip,helper)==returns,"Column survival production hook per exit");
        }

        if(target.equals("net/minecraft/block/BlockLilyPad")) {
            require(PlantHeightASMFixture120.callCount(roundTrip,"lilySoil120")>0
                    && PlantHeightASMFixture120.callCount(roundTrip,"lilyCanPlace120")==1,"Lily generic placement hooks missing");
        }
        if(target.equals("net/minecraft/block/BlockLilyPad"))
            require(PlantHeightASMFixture120.survivalReturns(srg)>0
                    && PlantHeightASMFixture120.callCount(roundTrip,"lilySurvival120")==PlantHeightASMFixture120.survivalReturns(srg),
                    "Expected one lily survival hook per original return");
        if(target.equals("net/minecraft/item/ItemLilyPad"))
            require(PlantHeightASMFixture120.lilyItemHooks(roundTrip)==3,"Lily item production hooks missing");
        require(target.equals(new ClassReader(roundTrip).getClassName()), "Round-trip mapping failed: " + target);
        if (target.equals("net/minecraft/block/BlockMushroom") || target.equals("net/minecraft/block/BlockLilyPad"))
            require(PlantHeightASMFixture120.hooks(roundTrip)==1,"Plant height hook missing after production round trip: "+target);
    }

    private static byte[] read(InputStream input) throws Exception {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) output.write(buffer, 0, count);
            return output.toByteArray();
        } finally {
            input.close();
        }
    }

    private static void verifyShieldRenderHook(byte[] bytes) throws Exception {
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        int hooks = 0;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (!"renderFirstPersonMainHand".equals(call.name)) continue;
                require("(F)V".equals(method.desc), "shield hook must be in first-person render");
                require("(Ljava/lang/Object;F)V".equals(call.desc), "shield hook equipped renderer argument");
                AbstractInsnNode next = call.getNext();
                require(next.getOpcode() == Opcodes.FLOAD && next.getNext() instanceof MethodInsnNode
                        && "renderFirstPersonBackHand".equals(((MethodInsnNode) next.getNext()).name),
                        "shield and offhand must share the camera-space tail");
                new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicVerifier())
                        .analyze(node.name, method);
                hooks++;
            }
        }
        require(hooks == 1, "one first-person shield draw hook, got " + hooks);
    }

    private static void verifyShieldApiHooks(String target, byte[] bytes) throws Exception {
        boolean movement = "net/minecraft/client/entity/EntityPlayerSP".equals(target);
        if (!movement && !"net/minecraft/entity/player/EntityPlayer".equals(target)) return;
        ClassNode node = new ClassNode();
        new ClassReader(bytes).accept(node, 0);
        int hooks = 0;
        for (MethodNode method : node.methods) {
            boolean changed = false;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (!(movement ? "isUsingItemForMovement" : "usesLegacyDamageBlock").equals(call.name)) continue;
                require(call.getOpcode() == Opcodes.INVOKESTATIC, "shield API static bridge");
                require("(Lnet/minecraft/entity/player/EntityPlayer;)Z".equals(call.desc), "shield API player descriptor");
                changed = true;
                hooks++;
            }
            if (changed) new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicVerifier())
                    .analyze(node.name, method);
        }
        require(movement ? hooks >= 1 : hooks == 1, "shield API hook count for " + target + ": " + hooks);
    }

    private static void verifySwimmingPoseHooks(String target,byte[] bytes)throws Exception {
        String expected=target.equals("net/minecraft/entity/Entity")?"waterBounds":target.endsWith("/EntityPlayer")?"eye":target.endsWith("/EntityRenderer")?"cameraHeight":target.endsWith("/RenderPlayer")?"rotate":target.endsWith("/ModelBiped")?"model":target.endsWith("/EntityPlayerSP")?"sprintSurface":null;
        if(expected==null)return;ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0;
        for(MethodNode m:node.methods)for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)i;
            if(call.name.equals(expected)&&call.owner.contains("Swimming")){new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(node.name,m);count++;}}
        require(count>0,"Swimming pose hook missing: "+target+"/"+expected);
    }

    private static void verifySurfaceBreathing(byte[] bytes) throws Exception {
        ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);int count=0;
        int swimming=0;
        for(MethodNode method:node.methods)for(AbstractInsnNode instruction:method.instructions.toArray())
            if(instruction instanceof MethodInsnNode&&((MethodInsnNode)instruction).owner.equals(AquaticSwimmingASM.HOOK)&&((MethodInsnNode)instruction).name.equals("beforeTravel")) {
                MethodInsnNode call=(MethodInsnNode)instruction;
                require(call.name.equals("beforeTravel")&&call.desc.equals("(Lnet/minecraft/entity/EntityLivingBase;Z)V"),"swimming travel descriptor");
                org.objectweb.asm.tree.FieldInsnNode jumping=(org.objectweb.asm.tree.FieldInsnNode)instruction.getPrevious();
                boolean exists=false;for(org.objectweb.asm.tree.FieldNode field:node.fields)if(field.name.equals(jumping.name)&&field.desc.equals("Z"))exists=true;
                require(exists,"swimming jumping field linkage");
                new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(node.name,method);swimming++;
            }
        require(swimming==1,"Swimming hook missing or duplicated: "+swimming);
        for(MethodNode method:node.methods)for(AbstractInsnNode instruction:method.instructions.toArray())
            if(instruction instanceof MethodInsnNode) {
                MethodInsnNode call=(MethodInsnNode)instruction;
                if(!call.owner.equals(AquaticBreathingASM.HOOK)||!call.name.equals("surfaceAir"))continue;
                require(call.desc.equals("(ILnet/minecraft/entity/EntityLivingBase;)I"),"surface breathing descriptor");
                new org.objectweb.asm.tree.analysis.Analyzer(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(node.name,method);
                count++;
            }
        require(count==1,"Surface breathing hook missing or duplicated: "+count);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
