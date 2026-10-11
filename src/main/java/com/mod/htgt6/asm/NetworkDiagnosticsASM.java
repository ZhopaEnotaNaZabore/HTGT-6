package com.mod.htgt6.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Instruments lifecycle methods shared by Forge and Crucible, including LOGIN. */
final class NetworkDiagnosticsASM {
    private static final String HELPER = "com/mod/htgt6/server/utils/Netty/PacketFailureDiagnostics";
    static byte[] transform(byte[] input) {
        ClassNode node = new ClassNode();
        new ClassReader(input).accept(node, 0);
        int active = 0, errors = 0, reads = 0, ticks = 0;
        String sideField = null;
        for (FieldNode field : node.fields) {
            if ("Z".equals(field.desc) && (field.access & Opcodes.ACC_FINAL) != 0
                    && (field.access & Opcodes.ACC_STATIC) == 0) {
                if (sideField != null) throw new IllegalStateException("Ambiguous NetworkManager side field");
                sideField = field.name;
            }
        }
        if (sideField == null) throw new IllegalStateException("Missing NetworkManager side field");
        for (MethodNode method : node.methods) {
            boolean tick = (method.name.equals("processReceivedPackets") || method.name.equals("func_74428_b")
                    || method.name.equals("a")) && method.desc.equals("()V");
            if (tick) {
                ticks++;
                if (!hasHook(method, "reportProcessing")) {
                    LabelNode start = new LabelNode(), end = new LabelNode(), catcher = new LabelNode();
                    method.instructions.insert(start);
                    method.instructions.add(end);
                    method.instructions.add(catcher);
                    // At entry only 'this' is initialized; discard merged temporary locals in this handler.
                    method.instructions.add(new FrameNode(Opcodes.F_FULL, 1, new Object[]{node.name},
                            1, new Object[]{"java/lang/Throwable"}));
                    method.instructions.add(new InsnNode(Opcodes.DUP));
                    method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    method.instructions.add(new InsnNode(Opcodes.SWAP));
                    method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "reportProcessing",
                            "(Ljava/lang/Object;Ljava/lang/Throwable;)V", false));
                    method.instructions.add(new InsnNode(Opcodes.ATHROW));
                    method.tryCatchBlocks.add(new TryCatchBlockNode(start, end, catcher, "java/lang/Throwable"));
                }
            }
            if (!hasHook(method, "recordProcessing")) {
                for (AbstractInsnNode insn : method.instructions.toArray()) {
                    if (!(insn instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) insn;
                    if (!"net/minecraft/network/Packet".equals(call.owner)
                            || !"(Lnet/minecraft/network/INetHandler;)V".equals(call.desc)) continue;
                    InsnList record = new InsnList();
                    record.add(new InsnNode(Opcodes.DUP2));
                    record.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    record.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, "recordProcessing",
                            "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V", false));
                    method.instructions.insertBefore(call, record);
                }
            }
            String hook = null, desc = null;
            if (method.name.equals("channelActive") && method.desc.equals("(Lio/netty/channel/ChannelHandlerContext;)V")) {
                hook = "installEarly"; desc = "(Lio/netty/channel/ChannelHandlerContext;Z)V"; active++;
            } else if (method.name.equals("exceptionCaught") && method.desc.equals("(Lio/netty/channel/ChannelHandlerContext;Ljava/lang/Throwable;)V")) {
                hook = "reportEarly"; desc = method.desc; errors++;
            } else if ((method.name.equals("channelRead0") || method.name.equals("a"))
                    && method.desc.equals("(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V")) {
                hook = "recordInbound"; desc = "(Lio/netty/channel/ChannelHandlerContext;Ljava/lang/Object;)V"; reads++;
            }
            if (hook == null) continue;
            boolean installed = false;
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (insn instanceof MethodInsnNode && HELPER.equals(((MethodInsnNode) insn).owner)
                        && hook.equals(((MethodInsnNode) insn).name)) installed = true;
            }
            if (installed) continue;
            InsnList code = new InsnList();
            code.add(new VarInsnNode(Opcodes.ALOAD, 1));
            if (hook.equals("installEarly")) {
                code.add(new VarInsnNode(Opcodes.ALOAD, 0));
                code.add(new FieldInsnNode(Opcodes.GETFIELD, node.name, sideField, "Z"));
            } else code.add(new VarInsnNode(Opcodes.ALOAD, 2));
            code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER, hook, desc, false));
            method.instructions.insert(code);
        }
        if (active != 1 || errors != 1 || reads != 1 || ticks != 1)
            throw new IllegalStateException("Network diagnostics anchors: active=" + active + " errors=" + errors + " reads=" + reads + " ticks=" + ticks);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static boolean hasHook(MethodNode method, String name) {
        for (AbstractInsnNode insn : method.instructions.toArray()) {
            if (insn instanceof MethodInsnNode && HELPER.equals(((MethodInsnNode) insn).owner)
                    && name.equals(((MethodInsnNode) insn).name)) return true;
        }
        return false;
    }
}
