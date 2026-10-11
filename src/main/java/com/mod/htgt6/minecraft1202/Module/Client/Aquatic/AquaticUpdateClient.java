package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Registry.Aquatic.AquaticUpdate;

import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.KelpRenderHandler;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.OceanWaterRenderHandler;
import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.RenderDrowned;
import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.RenderSeaTurtle;
import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.RenderThrownTrident;
import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.TridentItemRenderer;
import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.RiptideEffectRenderer;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator;
import com.mod.htgt6.minecraft1202.Module.Client.Aquatic.TurtleEggRenderHandler120;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDrowned;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntitySeaTurtle;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityThrownTrident;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDolphin120;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityPhantom120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

/** Client-only half of {@link AquaticUpdate}; loaded reflectively on physical clients. */
@SideOnly(Side.CLIENT)
public final class AquaticUpdateClient {
    public static void init() {
        com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.VillageItemModels114.aquatic();
        EntityBackportValidator.texture("textures/entity/zombie/drowned.png", 64, 64);
        EntityBackportValidator.texture("textures/entity/zombie/drowned_outer_layer.png", 64, 64);
        EntityBackportValidator.texture("textures/entity/turtle/big_sea_turtle.png", 128, 64);
        EntityBackportValidator.texture("textures/entity/trident.png", 32, 32);
        int kelpRenderId = RenderingRegistry.getNextAvailableRenderId();
        OceanContent.setKelpRenderId(kelpRenderId);
        RenderingRegistry.registerBlockHandler(new KelpRenderHandler(kelpRenderId));
        int turtleEggRenderId = RenderingRegistry.getNextAvailableRenderId();
        OceanContent.setTurtleEggRenderId(turtleEggRenderId);
        RenderingRegistry.registerBlockHandler(new TurtleEggRenderHandler120(turtleEggRenderId));
        RenderingRegistry.registerEntityRenderingHandler(EntityDrowned.class, new RenderDrowned());
        RenderingRegistry.registerEntityRenderingHandler(EntityThrownTrident.class,
                new RenderThrownTrident());
        RenderingRegistry.registerEntityRenderingHandler(EntitySeaTurtle.class, new RenderSeaTurtle());
        RenderingRegistry.registerEntityRenderingHandler(EntityAquaticFish120.Cod.class,new RenderAquaticFish120(0));
        RenderingRegistry.registerEntityRenderingHandler(EntityAquaticFish120.Salmon.class,new RenderAquaticFish120(1));
        RenderingRegistry.registerEntityRenderingHandler(EntityAquaticFish120.Pufferfish.class,new RenderAquaticFish120(2));
        RenderingRegistry.registerEntityRenderingHandler(EntityAquaticFish120.Tropical.class,new RenderAquaticFish120(3));
        RenderingRegistry.registerEntityRenderingHandler(EntityDolphin120.class,new RenderDolphin120());
        RenderingRegistry.registerEntityRenderingHandler(EntityPhantom120.class,new RenderPhantom120());
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
                com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.TileEntityConduit120.class,new RenderConduit120());
        MinecraftForgeClient.registerItemRenderer(OceanContent.trident, new TridentItemRenderer());
        MinecraftForgeClient.registerItemRenderer(net.minecraft.init.Items.filled_map,
                new com.mod.htgt6.minecraft1202.Module.Client.Aquatic.TreasureMapItemRenderer120());
        MinecraftForge.EVENT_BUS.register(new OceanWaterRenderHandler());
        MinecraftForge.EVENT_BUS.register(new RiptideEffectRenderer());
    }

    private AquaticUpdateClient() { }
}
