package com.mod.htgt6.minecraft1202.Module.Client.Frostburn;

import com.mod.htgt6.client.model.json.JsonBlockModelLoader;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.RenderFrostburnHusk;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.ModelFrostburnPolarBear;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.RenderFrostburnPolarBear;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.RenderFrostburnStray;
import com.mod.htgt6.minecraft1202.Module.Entity.Frostburn.EntityFrostburnHusk;
import com.mod.htgt6.minecraft1202.Module.Entity.Frostburn.EntityFrostburnPolarBear;
import com.mod.htgt6.minecraft1202.Module.Entity.Frostburn.EntityFrostburnStray;
import com.mod.htgt6.minecraft1202.Module.Entity.Frostburn.EntityFrostburnStrayArrow;
import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;

/** Registers the shared 1.8+ JSON block-model bridge and Frostburn model bindings. */
public final class FrostburnUpdateClient {
    public static void init() {
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(new BubbleColumnClient120());
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
                com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.TileEntityStructure120.class,new RenderStructure120());
        EntityBackportValidator.texture("textures/entity/frostburn/husk.png", 64, 64);
        EntityBackportValidator.texture("textures/entity/frostburn/stray.png", 64, 32);
        EntityBackportValidator.texture("textures/entity/frostburn/stray_overlay.png", 64, 32);
        EntityBackportValidator.texture("textures/entity/frostburn/polar_bear.png", 128, 64);
        EntityBackportValidator.modelParts(new ModelFrostburnPolarBear(), "head", "muzzle",
                "right_ear", "left_ear", "body", "back_right_leg", "back_left_leg",
                "front_right_leg", "front_left_leg");
        RenderingRegistry.registerEntityRenderingHandler(EntityFrostburnHusk.class,
                new RenderFrostburnHusk());
        RenderingRegistry.registerEntityRenderingHandler(EntityFrostburnStray.class,
                new RenderFrostburnStray());
        RenderingRegistry.registerEntityRenderingHandler(EntityFrostburnPolarBear.class,
                new RenderFrostburnPolarBear());
        RenderingRegistry.registerEntityRenderingHandler(EntityFrostburnStrayArrow.class,
                new com.mod.htgt6.minecraft1202.Module.Client.Combat.RenderCombatArrow());

        JsonBlockModelLoader loader = JsonBlockModelLoader.install();
        FrostburnContent.setJsonRenderId(loader.getRenderId());

        JsonBlockModelLoader.TextureResolver textures = new JsonBlockModelLoader.TextureResolver() {
            @Override public ResourceLocation resolve(String modelTexture) {
                String path = modelTexture;
                int colon = path.indexOf(':');
                if (colon >= 0) path = path.substring(colon + 1);
                if (path.startsWith("blocks/")) path = path.substring("blocks/".length());
                return new ResourceLocation("htgt6", "frostburn/" + path);
            }
        };
        JsonBlockModelLoader.StateMapper normal = new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) { return "normal"; }
        };

        loader.register(FrostburnContent.magmaBlock, state("magma"), normal, textures);
        loader.register(FrostburnContent.netherWartBlock, state("nether_wart_block"), normal, textures);
        loader.register(FrostburnContent.redNetherBrick, state("red_nether_brick"), normal, textures);
        loader.register(FrostburnContent.boneBlock, state("bone_block"), new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                int axis = metadata & 12;
                return axis == 4 ? "axis=x" : axis == 8 ? "axis=z" : "axis=y";
            }
        }, textures);
        loader.register(FrostburnContent.structureBlock, state("structure_block"), new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                switch (metadata & 3) {
                    case 0: return "mode=save";
                    case 1: return "mode=load";
                    case 2: return "mode=corner";
                    default: return "mode=data";
                }
            }
        }, textures);
    }

    private static ResourceLocation state(String path) { return new ResourceLocation("htgt6", path); }
    private FrostburnUpdateClient() { }
}
