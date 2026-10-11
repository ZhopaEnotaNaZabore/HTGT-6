package com.mod.htgt6.client.vanillatweaks.memory;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.InputStream;

/** Headless contract: network callbacks enqueue cleanup; client tick owns all GL work. */
public final class MemoryCleanerThreadingFixture120 {
    public static void main(String[] ignored) throws Exception {
        require(MemoryCleaner.takeCleanupRequestForTick() == MemoryCleaner.CLEANUP_NONE,
                "initial queue state");
        MemoryCleaner.requestAggressiveCleanup(false);
        require(MemoryCleaner.takeCleanupRequestForTick() == MemoryCleaner.CLEANUP_PARTIAL,
                "manual/world cleanup queue");
        MemoryCleaner.requestAggressiveCleanup(false);
        MemoryCleaner.requestAggressiveCleanup(true);
        MemoryCleaner.requestAggressiveCleanup(false);
        require(MemoryCleaner.takeCleanupRequestForTick() == MemoryCleaner.CLEANUP_FULL_DISCONNECT,
                "disconnect request must dominate coalesced partial requests");
        require(MemoryCleaner.takeCleanupRequestForTick() == MemoryCleaner.CLEANUP_NONE,
                "queue drain");

        ClassNode node = bytecode();
        require(calls(node, "onDisconnect", "requestAggressiveCleanup") == 1,
                "Netty disconnect callback only queues cleanup");
        require(calls(node, "onDisconnect", "executeAggressiveCleanup") == 0,
                "Netty disconnect callback must not execute GL cleanup");
        require(calls(node, "onWorldUnload", "requestAggressiveCleanup") == 1,
                "world unload queues cleanup");
        require(calls(node, "onClientTick", "takeCleanupRequestForTick") == 1
                        && calls(node, "onClientTick", "executeAggressiveCleanup") == 1,
                "client END tick drains and executes cleanup");
        require(calls(node, "executeAggressiveCleanup", "func_152345_ab") == 1,
                "defensive Minecraft-main-thread guard");
        for (MethodNode method : node.methods)
            for (AbstractInsnNode instruction : method.instructions.toArray())
                if (instruction instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    require(!call.name.equals("gc") && !call.name.equals("runFinalization")
                            && !call.name.equals("sleep") && !call.name.equals("loadRenderers")
                            && !call.name.equals("deleteAllDisplayLists")
                            && !call.name.equals("deleteTexture") && !call.name.equals("getDeclaredFields"),
                            "Blocking or foreign-resource cleanup: " + call.owner + "." + call.name);
                }
        require(calls(node,"onClientTick","tickClientCaches")==1,
                "owned caches must drain even while chat is closed");
        System.out.println("MemoryCleaner threading contract OK: Netty queues, client tick owns GL cleanup");
    }

    private static ClassNode bytecode() throws Exception {
        InputStream input = MemoryCleaner.class.getResourceAsStream("MemoryCleaner.class");
        require(input != null, "MemoryCleaner bytecode");
        try {
            ClassNode node = new ClassNode();
            new ClassReader(input).accept(node, 0);
            return node;
        } finally { input.close(); }
    }

    private static int calls(ClassNode node, String methodName, String calledName) {
        int count = 0;
        for (MethodNode method : node.methods) if (methodName.equals(method.name))
            for (AbstractInsnNode instruction : method.instructions.toArray())
                if (instruction instanceof MethodInsnNode
                        && calledName.equals(((MethodInsnNode)instruction).name)) count++;
        return count;
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private MemoryCleanerThreadingFixture120() { }
}
