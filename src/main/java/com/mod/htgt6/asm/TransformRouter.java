package com.mod.htgt6.asm;



import java.util.HashMap;
import java.util.Map;


import com.mod.htgt6.asm.transformers.AnvilChunkLoaderTransformer;
import com.mod.htgt6.asm.transformers.BiomeCacheTransformer;
import com.mod.htgt6.asm.transformers.BiomeGenBaseTransformer;
import com.mod.htgt6.asm.transformers.ChunkProviderGenerateTransformer;
import com.mod.htgt6.asm.transformers.ChunkTransformer;
import com.mod.htgt6.asm.transformers.GenLayerTransformer;
import com.mod.htgt6.asm.transformers.PacketBufferTransformer;
import com.mod.htgt6.asm.transformers.S21PacketChunkDataTransformer;
import com.mod.htgt6.asm.transformers.WorldChunkManagerTransformer;



public final class TransformRouter {

    public static final Map<String, Class<?>> ROUTES =
            new HashMap<>();

    static {

        /*
         * Only register if enabled
         */

        if (HTGT6Config.ENABLE_BIOME_PATCHES) {

            System.out.println(
                    "[HTGT6 ASM] Registering biome ASM"
            );

            ROUTES.put(
                    "net.minecraft.world.biome.BiomeGenBase",
                    BiomeGenBaseTransformer.class
            );

           ROUTES.put(
                    "net.minecraft.world.chunk.Chunk",
                    ChunkTransformer.class
            );

            ROUTES.put(
                    "net.minecraft.world.chunk.storage.AnvilChunkLoader",
                    AnvilChunkLoaderTransformer.class
            );

            ROUTES.put(
                    "net.minecraft.network.play.server.S21PacketChunkData",
                    S21PacketChunkDataTransformer.class
            );

            ROUTES.put(
                    "net.minecraft.network.PacketBuffer",
                    PacketBufferTransformer.class
            );

            ROUTES.put(
                    "net.minecraft.world.biome.WorldChunkManager",
                    WorldChunkManagerTransformer.class
            );

            ROUTES.put(
                    "net.minecraft.world.biome.BiomeCache",
                    BiomeCacheTransformer.class
            );

            ROUTES.put(
                    "net.minecraft.world.gen.ChunkProviderGenerate",
                    ChunkProviderGenerateTransformer.class
            );
            ROUTES.put(
                    "net.minecraft.world.gen.layer.GenLayer",
                    GenLayerTransformer.class
           );
            ROUTES.put(
                    "gregapi.dummies.DummyWorld",
                    com.mod.htgt6.asm.gt.DummyWorldTransformer.class
            );
        }
   }

    public static boolean hasTransformer(
            String transformedName
    ) {

        return ROUTES.containsKey(transformedName);
    }

    public static Class<?> getTransformer(
            String transformedName
    ) {

        return ROUTES.get(transformedName);
    }

    private TransformRouter() {}
}