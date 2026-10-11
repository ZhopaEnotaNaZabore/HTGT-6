package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.client.model.json.JsonBlockModelLoader;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.GT6DeepslateOreIntegration;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherMasonry116;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;

/** Client-only JSON model bindings for the Minecraft 1.20.2 backport. */
public final class Minecraft120BackportClient {
    public static void init() {
        BuzzyModels115.init();
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityStrider116.class,new RenderStrider116());
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityHoglin116.class,new RenderHoglin116(false));
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityZoglin116.class,new RenderHoglin116(true));
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityCat114.class,new RenderCat114());
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityFox114.class,new RenderFox114());
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglin116.class,new RenderPiglin116(false));
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglinBrute116.class,new RenderPiglin116("piglin_brute"));
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityZombifiedPiglin116.class,new RenderPiglin116(true));
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityMooshroom114.class,new RenderMooshroom114(new net.minecraft.client.model.ModelCow(),.7F));
        com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.VillageItemModels114.modern();
        com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator.texture(
                "textures/entity/squid/glow_squid.png",64,32);
        for(String variant:new String[]{"lucy","wild","gold","cyan","blue"})
            com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator.texture(
                    "textures/entity/axolotl/axolotl_"+variant+".png",64,64);
        com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator.texture(
                "textures/entity/goat/goat.png",64,64);
        com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator.texture(
                "textures/entity/allay/allay.png",32,32);
        com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator.texture(
                "textures/entity/camel/camel.png",128,128);
        com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator.texture(
                "textures/entity/warden/warden.png",128,128);
        com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator.texture(
                "textures/entity/warden/warden_bioluminescent_layer.png",128,128);
        for(String layer:new String[]{"warden_heart","warden_pulsating_spots_1","warden_pulsating_spots_2"})
            com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator.texture(
                    "textures/entity/warden/"+layer+".png",128,128);
        for(String variant:new String[]{"red_blue","blue","green","yellow_blue","grey"})
            com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator.texture(
                    "textures/entity/parrot/parrot_"+variant+".png",32,32);
        for(String variant:new String[]{"panda","brown_panda","aggressive_panda","lazy_panda","weak_panda","worried_panda","playful_panda"})
            com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator.texture(
                    "textures/entity/panda/"+variant+".png",64,64);
        com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator.texture(
                "textures/entity/wandering_trader.png",64,64);
        com.mod.htgt6.minecraft1202.Module.Client.Shared.EntityBackportValidator.texture(
                "textures/entity/exploration_update/llama/decor/trader_llama.png",128,64);
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityAxolotl120.class,new RenderAxolotl120());
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityGlowSquid120.class,new RenderGlowSquid120());
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityGoat120.class,new RenderGoat120());
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityAllay120.class,new RenderAllay120());
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityCamel120.class,new RenderCamel120());
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(new CamelInputHandler120());
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityParrot120.class,new RenderParrot120());
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWarden120.class,new RenderWarden120());
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPanda120.class,new RenderPanda120());
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWanderingTrader120.class,
                new RenderWanderingTrader120());
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
                com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityTraderLlama120.class,
                new com.mod.htgt6.minecraft1202.Module.Client.Exploration.RenderLlama112());
        int candleRenderId=cpw.mods.fml.client.registry.RenderingRegistry.getNextAvailableRenderId();
        com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Candle.setRenderType(candleRenderId);
        cpw.mods.fml.client.registry.RenderingRegistry.registerBlockHandler(new CandleRenderHandler120(candleRenderId));
        JsonBlockModelLoader loader = JsonBlockModelLoader.install();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
                new com.mod.htgt6.minecraft1202.Module.Client.Modern.Backport120ClientHandler());
        MerchantScreenHandler120 traderScreen=new MerchantScreenHandler120();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(traderScreen);
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(traderScreen);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new SculkDarknessOverlay120());
        net.minecraftforge.client.MinecraftForgeClient.registerItemRenderer(
                Backport120Content.spyglass,
                new com.mod.htgt6.minecraft1202.Module.Client.Modern.SpyglassItemRenderer120());
        com.mod.htgt6.minecraft1202.Module.Client.Modern.DecoratedPotRenderer120 potRenderer=
                new com.mod.htgt6.minecraft1202.Module.Client.Modern.DecoratedPotRenderer120();
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
                com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120DecoratedPot.class,potRenderer);
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
                com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Sign.class,
                new com.mod.htgt6.minecraft1202.Module.Client.Modern.Backport120SignRenderer());
        net.minecraftforge.client.MinecraftForgeClient.registerItemRenderer(
                net.minecraft.item.Item.getItemFromBlock(Backport120Content.decoratedPot),potRenderer);
        Backport120Content.setJsonRenderId(loader.getRenderId());

        JsonBlockModelLoader.TextureResolver textures = new JsonBlockModelLoader.TextureResolver() {
            @Override
            public ResourceLocation resolve(String modelTexture) {
                String path = modelTexture;
                int colon = path.indexOf(':');
                if (colon >= 0) path = path.substring(colon + 1);
                if (path.startsWith("block/")) path = path.substring("block/".length());
                if (path.startsWith("blocks/")) path = path.substring("blocks/".length());
                if (!path.startsWith("1_20_backport/")) path = "1_20_backport/" + path;
                return new ResourceLocation("htgt6", path);
            }
        };
        JsonBlockModelLoader.StateMapper normal = new JsonBlockModelLoader.StateMapper() {
            @Override
            public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                return "normal";
            }
        };
        JsonBlockModelLoader.OverlayResolver gtOreOverlay = new JsonBlockModelLoader.OverlayResolver() {
            @Override
            public net.minecraft.util.IIcon resolve(Block block, IBlockAccess world, int x, int y, int z,
                                                    int side, int metadata) {
                gregapi.block.prefixblock.PrefixBlock ore = (gregapi.block.prefixblock.PrefixBlock) block;
                return world == null ? ore.getIcon(side, metadata) : ore.getIcon(world, x, y, z, side);
            }
        };

        loader.register(GT6DeepslateOreIntegration.normal, state("deepslate"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                        return "axis=y";
                    }
                }, textures, gtOreOverlay);
        loader.register(GT6DeepslateOreIntegration.small, state("deepslate"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                        return "axis=y";
                    }
                }, textures, gtOreOverlay);
        loader.register(GT6DeepslateOreIntegration.broken, state("cobbled_deepslate"),
                normal, textures, gtOreOverlay);

        loader.register(Backport120Content.deepslate, state("deepslate"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override
                    public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                        int axis = metadata & 12;
                        return axis == 4 ? "axis=x" : axis == 8 ? "axis=z" : "axis=y";
                    }
                }, textures);
        loader.registerItem(com.mod.htgt6.minecraft1202.Module.Registry.Modern.StriderContent116.fungusStick,new ResourceLocation("htgt6","item/1_20_backport/warped_fungus_on_a_stick"),textures);
        loader.registerItem(com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherTreasure116.snoutPattern,new ResourceLocation("htgt6","item/1_20_backport/piglin_banner_pattern"),textures);
        bindNetherWoodFamilies(loader,textures);
        bindSoulCampfire(loader,textures);
        bindNetherVegetation(loader,textures);
        bindAxis(loader, textures, Backport120Content.chainBlock, "chain");
        loader.register(EtFVanillaGapContent120.soulLantern,state("soul_lantern"),new JsonBlockModelLoader.StateMapper(){
            @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata){return "hanging="+((metadata&1)!=0);}
        },textures);

        bind(loader, normal, textures, Backport120Content.cobbledDeepslate, "cobbled_deepslate");
        bind(loader, normal, textures, Backport120Content.polishedDeepslate, "polished_deepslate");
        bind(loader, normal, textures, Backport120Content.chiseledDeepslate, "chiseled_deepslate");
        bind(loader, normal, textures, Backport120Content.deepslateBricks, "deepslate_bricks");
        bind(loader, normal, textures, Backport120Content.crackedDeepslateBricks, "cracked_deepslate_bricks");
        bind(loader, normal, textures, Backport120Content.deepslateTiles, "deepslate_tiles");
        bind(loader, normal, textures, Backport120Content.crackedDeepslateTiles, "cracked_deepslate_tiles");
        bind(loader, normal, textures, Backport120Content.reinforcedDeepslate, "reinforced_deepslate");
        bind(loader, normal, textures, Backport120Content.blueIce, "blue_ice");
        bindAxis(loader, textures, Backport120Content.polishedBasalt, "polished_basalt");
        bindModernStoneFamilies(loader,textures);
        for(com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageBuildingContent114.Shape s:
                com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageBuildingContent114.SHAPES.values()){
            if(s.kind.equals("stairs"))bindStairs(loader,textures,s.block,s.name);
            else if(s.kind.equals("slab"))bindSlab(loader,textures,s.block,s.doubleSlab,s.name);
            else bindWall(loader,textures,s.block,s.name);
            loader.registerItem(net.minecraft.item.Item.getItemFromBlock(s.block),new ResourceLocation("htgt6","village_items114/item/"+s.name),
                source->new ResourceLocation("htgt6","village_items114/"+source.substring(source.indexOf(':')+1)));
        }
        for(int i=0;i<5;i++)loader.registerItem(com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageSigns114.ITEMS[i],
                new ResourceLocation("htgt6","village_items114/item/"+com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageSigns114.WOODS[i]+"_sign"),
                source->new ResourceLocation("htgt6","village_items114/"+source.substring(source.indexOf(':')+1)));
        bindStairs(loader, textures, Backport120Content.mossyCobblestoneStairs, "mossy_cobblestone_stairs");
        bindSlab(loader, textures, Backport120Content.mossyCobblestoneSlab,
                Backport120Content.doubleMossyCobblestoneSlab, "mossy_cobblestone_slab");
        bindStairs(loader, textures, EtFVanillaGapContent120.smoothSandstoneStairs, "smooth_sandstone_stairs");
        bindSlab(loader, textures, EtFVanillaGapContent120.polishedBlackstoneSlab, EtFVanillaGapContent120.doublePolishedBlackstoneSlab, "polished_blackstone_slab");
        bindSlab(loader, textures, EtFVanillaGapContent120.smoothSandstoneSlab,
                EtFVanillaGapContent120.doubleSmoothSandstoneSlab, "smooth_sandstone_slab");
        bindStairs(loader,textures,EtFVanillaGapContent120.bambooStairs,"bamboo_stairs");
        bindSlab(loader,textures,EtFVanillaGapContent120.bambooSlab,
                EtFVanillaGapContent120.doubleBambooSlab,"bamboo_slab");
        bindStairs(loader,textures,EtFVanillaGapContent120.bambooMosaicStairs,"bamboo_mosaic_stairs");
        bindSlab(loader,textures,EtFVanillaGapContent120.bambooMosaicSlab,
                EtFVanillaGapContent120.doubleBambooMosaicSlab,"bamboo_mosaic_slab");
        bindFence(loader,textures,EtFVanillaGapContent120.bambooFence,"bamboo_fence");
        bindFenceGate(loader,textures,EtFVanillaGapContent120.bambooFenceGate,"bamboo_fence_gate");
        bindDoor(loader,textures,EtFVanillaGapContent120.bambooDoorBlock,"bamboo_door");
        bindTrapDoor(loader,textures,EtFVanillaGapContent120.bambooTrapdoor,"bamboo_trapdoor");
        loader.register(EtFVanillaGapContent120.bambooPressurePlate,state("bamboo_pressure_plate"),
                new JsonBlockModelLoader.StateMapper(){@Override public String variant(IBlockAccess world,int x,int y,int z,int metadata){
                    return "powered="+(metadata!=0);}},textures);
        loader.register(EtFVanillaGapContent120.bambooButton,state("bamboo_button"),
                new JsonBlockModelLoader.StateMapper(){@Override public String variant(IBlockAccess world,int x,int y,int z,int metadata){
                    int face=com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button.WALL,direction=2;
                    if(world!=null){net.minecraft.tileentity.TileEntity raw=world.getTileEntity(x,y,z);
                        if(raw instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button){
                            com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button button=
                                    (com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button)raw;
                            face=button.getFace();direction=button.getFacing();}}
                    String faceName=face==com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button.FLOOR?"floor":
                            face==com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button.CEILING?"ceiling":"wall";
                    return "face="+faceName+",facing="+facing(direction)+",powered="+((metadata&1)!=0);}},textures);
        loader.register(NetherMasonry116.pressurePlate,state("polished_blackstone_pressure_plate"),
                new JsonBlockModelLoader.StateMapper(){@Override public String variant(IBlockAccess world,int x,int y,int z,int metadata){
                    return "powered="+(metadata!=0);}},textures);
        loader.register(NetherMasonry116.button,state("polished_blackstone_button"),
                new JsonBlockModelLoader.StateMapper(){@Override public String variant(IBlockAccess world,int x,int y,int z,int metadata){
                    int face=com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button.WALL,direction=2;
                    if(world!=null){net.minecraft.tileentity.TileEntity raw=world.getTileEntity(x,y,z);
                        if(raw instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button){
                            com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button button=
                                    (com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button)raw;
                            face=button.getFace();direction=button.getFacing();}}
                    String faceName=face==com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button.FLOOR?"floor":
                            face==com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button.CEILING?"ceiling":"wall";
                    return "face="+faceName+",facing="+facing(direction)+",powered="+((metadata&1)!=0);}},textures);
        for(com.mod.htgt6.minecraft1202.Module.Registry.Modern.BlackstoneShapes116.Family family:
                com.mod.htgt6.minecraft1202.Module.Registry.Modern.BlackstoneShapes116.FAMILIES) {
            bindStairs(loader,textures,family.stairs,family.name+"_stairs");
            bindSlab(loader,textures,family.slab,family.doubleSlab,family.name+"_slab");
            bindWall(loader,textures,family.wall,family.name+"_wall");
        }
        bindAxis(loader,textures,EtFVanillaGapContent120.basalt,"basalt");
        bind(loader,normal,textures,NetherMasonry116.chiseledNetherBricks,"chiseled_nether_bricks");
        bind(loader,normal,textures,NetherMasonry116.crackedNetherBricks,"cracked_nether_bricks");
        bindStairs(loader, textures, Backport120Content.cobbledDeepslateStairs, "cobbled_deepslate_stairs");
        bindStairs(loader, textures, Backport120Content.polishedDeepslateStairs, "polished_deepslate_stairs");
        bindStairs(loader, textures, Backport120Content.deepslateBrickStairs, "deepslate_brick_stairs");
        bindStairs(loader, textures, Backport120Content.deepslateTileStairs, "deepslate_tile_stairs");
        bindSlab(loader, textures, Backport120Content.cobbledDeepslateSlab,
                Backport120Content.doubleCobbledDeepslateSlab, "cobbled_deepslate_slab");
        bindSlab(loader, textures, Backport120Content.polishedDeepslateSlab,
                Backport120Content.doublePolishedDeepslateSlab, "polished_deepslate_slab");
        bindSlab(loader, textures, Backport120Content.deepslateBrickSlab,
                Backport120Content.doubleDeepslateBrickSlab, "deepslate_brick_slab");
        bindSlab(loader, textures, Backport120Content.deepslateTileSlab,
                Backport120Content.doubleDeepslateTileSlab, "deepslate_tile_slab");
        for(Block wire:new Block[]{net.minecraft.init.Blocks.redstone_wire,com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.RedstoneWire116.dot})loader.register(wire,state("redstone_wire"),new JsonBlockModelLoader.StateMapper(){
            @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata){return world==null?"east=side,north=side,south=side,west=side":com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.RedstoneWire116.variant(world,x,y,z);}
        },textures);
        loader.register(net.minecraft.init.Blocks.cobblestone_wall,state("native_cobblestone_wall"),new JsonBlockModelLoader.StateMapper(){
            @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata){return "inventory="+(world==null)+",mossy="+((metadata&1)!=0)+(world==null?"":","+com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NativeWall116.state(world,x,y,z).variant());}
        },textures);
        bindWall(loader, textures, Backport120Content.cobbledDeepslateWall, "cobbled_deepslate_wall");
        bindWall(loader, textures, Backport120Content.polishedDeepslateWall, "polished_deepslate_wall");
        bindWall(loader, textures, Backport120Content.deepslateBrickWall, "deepslate_brick_wall");
        bindWall(loader, textures, Backport120Content.deepslateTileWall, "deepslate_tile_wall");
        bind(loader, normal, textures, Backport120Content.tuff, "tuff");
        bind(loader, normal, textures, Backport120Content.calcite, "calcite");
        bind(loader, normal, textures, Backport120Content.smoothBasalt, "smooth_basalt");
        bind(loader, normal, textures, Backport120Content.dripstoneBlock, "dripstone_block");
        bind(loader, normal, textures, Backport120Content.mossBlock, "moss_block");
        bind(loader, normal, textures, Backport120Content.rootedDirt, "rooted_dirt");
        bind(loader, normal, textures, Backport120Content.rawCopperBlock, "raw_copper_block");
        bind(loader, normal, textures, Backport120Content.copperOre, "copper_ore");
        bind(loader, normal, textures, Backport120Content.deepslateCopperOre, "deepslate_copper_ore");
        loader.register(Backport120Content.pointedDripstone, state("pointed_dripstone"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override
                    public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                        return "thickness=" + com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120PointedDripstone
                                .thicknessName(com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120PointedDripstone
                                        .thickness(metadata))
                                + ",vertical_direction="
                                + (com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120PointedDripstone
                                        .isDown(metadata) ? "down" : "up");
                    }
                }, textures);

        bind(loader, normal, textures, Backport120Content.mossCarpet, "moss_carpet");
        bind(loader, normal, textures, Backport120Content.azalea, "azalea");
        bind(loader, normal, textures, Backport120Content.floweringAzalea, "flowering_azalea");
        bind(loader, normal, textures, Backport120Content.azaleaLeaves, "azalea_leaves");
        bind(loader, normal, textures, Backport120Content.floweringAzaleaLeaves, "flowering_azalea_leaves");
        bind(loader, normal, textures, Backport120Content.hangingRoots, "hanging_roots");
        bind(loader, normal, textures, Backport120Content.sporeBlossom, "spore_blossom");
        loader.register(Backport120Content.caveVines, state("cave_vines"), berryMapper(), textures);
        loader.register(Backport120Content.caveVinesPlant, state("cave_vines_plant"), berryMapper(), textures);
        loader.register(Backport120Content.smallDripleaf, state("small_dripleaf"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                        return "facing=" + facing(metadata & 3) + ",half=" + ((metadata & 4) != 0 ? "upper" : "lower");
                    }
                }, textures);
        loader.register(Backport120Content.bigDripleaf, state("big_dripleaf"),
                new JsonBlockModelLoader.StateMapper() {
                    private final String[] tilt = {"none", "unstable", "partial", "full"};
                    @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                        return "facing=" + facing(metadata & 3) + ",tilt=" + tilt[(metadata >>> 2) & 3];
                    }
                }, textures);
        loader.register(Backport120Content.bigDripleafStem, state("big_dripleaf_stem"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                        return "facing=" + facing(metadata & 3);
                    }
                }, textures);
        bind(loader, normal, textures, Backport120Content.powderSnow, "powder_snow");
        bind(loader, normal, textures, Backport120Content.cherryPlanks, "cherry_planks");
        bindStairs(loader, textures, Backport120Content.cherryStairs, "cherry_stairs");
        bindSlab(loader, textures, Backport120Content.cherrySlab,
                Backport120Content.doubleCherrySlab, "cherry_slab");
        bindFence(loader, textures, Backport120Content.cherryFence, "cherry_fence");
        bindFenceGate(loader, textures, Backport120Content.cherryFenceGate, "cherry_fence_gate");
        bindDoor(loader, textures, Backport120Content.cherryDoorBlock, "cherry_door");
        bindTrapDoor(loader, textures, Backport120Content.cherryTrapdoor, "cherry_trapdoor");
        loader.register(Backport120Content.cherryPressurePlate, state("cherry_pressure_plate"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                        return "powered=" + (metadata != 0);
                    }
                }, textures);
        loader.register(Backport120Content.cherryButton, state("cherry_button"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                        int face = com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button.WALL;
                        int direction = 2;
                        if (world != null) {
                            net.minecraft.tileentity.TileEntity raw = world.getTileEntity(x, y, z);
                            if (raw instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button) {
                                com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button button =
                                        (com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button) raw;
                                face = button.getFace();
                                direction = button.getFacing();
                            }
                        }
                        String faceName = face == com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button.FLOOR
                                ? "floor" : face == com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button.CEILING
                                ? "ceiling" : "wall";
                        return "face=" + faceName + ",facing=" + facing(direction)
                                + ",powered=" + ((metadata & 1) != 0);
                    }
                }, textures);
        bind(loader, normal, textures, Backport120Content.cherryLeaves, "cherry_leaves");
        bind(loader, normal, textures, Backport120Content.cherrySapling, "cherry_sapling");
        bindAxis(loader, textures, Backport120Content.cherryLog, "cherry_log");
        bindAxis(loader, textures, Backport120Content.cherryWood, "cherry_wood");
        bindAxis(loader, textures, Backport120Content.strippedCherryLog, "stripped_cherry_log");
        bindAxis(loader, textures, Backport120Content.strippedCherryWood, "stripped_cherry_wood");
        bindStripped(loader,textures,Backport120Content.strippedLog,"stripped_log",
                new String[]{"oak","spruce","birch","jungle"});
        bindStripped(loader,textures,Backport120Content.strippedLog2,"stripped_log2",
                new String[]{"acacia","dark_oak"});
        bindStripped(loader,textures,Backport120Content.strippedWood,"stripped_wood",
                new String[]{"oak","spruce","birch","jungle"});
        bindStripped(loader,textures,Backport120Content.strippedWood2,"stripped_wood2",
                new String[]{"acacia","dark_oak"});
        loader.register(Backport120Content.pinkPetals, state("pink_petals"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                        return "facing=" + facing(metadata & 3) + ",flower_amount=" + (((metadata >>> 2) & 3) + 1);
                    }
                }, textures);
        bind(loader, normal, textures, Backport120Content.mud, "mud");
        bind(loader, normal, textures, Backport120Content.packedMud, "packed_mud");
        bind(loader, normal, textures, Backport120Content.mudBricks, "mud_bricks");
        bindStairs(loader, textures, Backport120Content.mudBrickStairs, "mud_brick_stairs");
        bindSlab(loader, textures, Backport120Content.mudBrickSlab,
                Backport120Content.doubleMudBrickSlab, "mud_brick_slab");
        bindWall(loader, textures, Backport120Content.mudBrickWall, "mud_brick_wall");
        bind(loader, normal, textures, Backport120Content.mangrovePlanks, "mangrove_planks");
        bindStairs(loader, textures, Backport120Content.mangroveStairs, "mangrove_stairs");
        bindSlab(loader, textures, Backport120Content.mangroveSlab,
                Backport120Content.doubleMangroveSlab, "mangrove_slab");
        bindFence(loader, textures, Backport120Content.mangroveFence, "mangrove_fence");
        bindFenceGate(loader, textures, Backport120Content.mangroveFenceGate, "mangrove_fence_gate");
        bindDoor(loader, textures, Backport120Content.mangroveDoorBlock, "mangrove_door");
        bindTrapDoor(loader, textures, Backport120Content.mangroveTrapdoor, "mangrove_trapdoor");
        loader.register(Backport120Content.mangrovePressurePlate, state("mangrove_pressure_plate"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                        return "powered=" + (metadata != 0);
                    }
                }, textures);
        loader.register(Backport120Content.mangroveButton, state("mangrove_button"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                        int face = com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button.WALL;
                        int direction = 2;
                        if (world != null) {
                            net.minecraft.tileentity.TileEntity raw = world.getTileEntity(x, y, z);
                            if (raw instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button) {
                                com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button button =
                                        (com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button) raw;
                                face = button.getFace(); direction = button.getFacing();
                            }
                        }
                        String faceName = face == com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button.FLOOR
                                ? "floor" : face == com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button.CEILING
                                ? "ceiling" : "wall";
                        return "face=" + faceName + ",facing=" + facing(direction)
                                + ",powered=" + ((metadata & 1) != 0);
                    }
                }, textures);
        bindAxis(loader, textures, Backport120Content.mangroveLog, "mangrove_log");
        bindAxis(loader, textures, Backport120Content.mangroveWood, "mangrove_wood");
        bindAxis(loader, textures, Backport120Content.strippedMangroveLog, "stripped_mangrove_log");
        bindAxis(loader, textures, Backport120Content.strippedMangroveWood, "stripped_mangrove_wood");
        bind(loader, normal, textures, Backport120Content.mangroveRoots, "mangrove_roots");
        bindAxis(loader, textures, Backport120Content.muddyMangroveRoots, "muddy_mangrove_roots");
        bind(loader, normal, textures, Backport120Content.mangroveLeaves, "mangrove_leaves");
        loader.register(Backport120Content.mangrovePropagule, state("mangrove_propagule"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                        boolean hanging = (metadata & 8) != 0;
                        return "age=" + (hanging ? Math.min(4, metadata & 7) : 0)
                                + ",hanging=" + hanging;
                    }
                }, textures);
        bind(loader, normal, textures, Backport120Content.sculk, "sculk");
        loader.register(Backport120Content.sculkVein, state("sculk_vein"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                        if (world == null) return "down=false,east=false,north=false,south=false,up=false,west=false";
                        return "down=" + attached(world,x,y,z,net.minecraftforge.common.util.ForgeDirection.DOWN)
                                + ",east=" + attached(world,x,y,z,net.minecraftforge.common.util.ForgeDirection.EAST)
                                + ",north=" + attached(world,x,y,z,net.minecraftforge.common.util.ForgeDirection.NORTH)
                                + ",south=" + attached(world,x,y,z,net.minecraftforge.common.util.ForgeDirection.SOUTH)
                                + ",up=" + attached(world,x,y,z,net.minecraftforge.common.util.ForgeDirection.UP)
                                + ",west=" + attached(world,x,y,z,net.minecraftforge.common.util.ForgeDirection.WEST);
                    }
                    private boolean attached(IBlockAccess world,int x,int y,int z,
                                             net.minecraftforge.common.util.ForgeDirection face) {
                        return com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120SculkVein
                                .attached(world,x,y,z,face);
                    }
                }, textures);
        loader.register(Backport120Content.sculkCatalyst, state("sculk_catalyst"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata) {
                        return "bloom=" + ((metadata & 1) != 0);
                    }
                }, textures);
        loader.register(Backport120Content.sculkSensor, state("sculk_sensor"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata) {
                        com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120SculkSensor tile=
                                world==null?null:com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120SculkSensor.tile(world,x,y,z);
                        int phase=tile==null?0:tile.getPhase();return "sculk_sensor_phase="+(phase==1?"active":phase==2?"cooldown":"inactive");
                    }
                }, textures);
        loader.register(Backport120Content.calibratedSculkSensor,state("calibrated_sculk_sensor"),
                new JsonBlockModelLoader.StateMapper(){
                    @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata){
                        com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120SculkSensor tile=
                                world==null?null:com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120SculkSensor.tile(world,x,y,z);
                        int phase=tile==null?0:tile.getPhase(),side=tile==null?2:tile.getFacing();
                        String direction=side==3?"south":side==4?"west":side==5?"east":"north";
                        return "facing="+direction+",sculk_sensor_phase="+(phase==1?"active":phase==2?"cooldown":"inactive");
                    }
                },textures);
        loader.register(Backport120Content.sculkShrieker, state("sculk_shrieker"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata) {
                        return "can_summon=" + ((metadata & 1) != 0);
                    }
                }, textures);
        bind(loader, normal, textures, Backport120Content.rawIronBlock, "raw_iron_block");
        bind(loader, normal, textures, Backport120Content.rawGoldBlock, "raw_gold_block");
        bind(loader, normal, textures, Backport120Content.deepslateCoalOre, "deepslate_coal_ore");
        bind(loader, normal, textures, Backport120Content.deepslateIronOre, "deepslate_iron_ore");
        bind(loader, normal, textures, Backport120Content.deepslateGoldOre, "deepslate_gold_ore");
        bind(loader, normal, textures, Backport120Content.deepslateRedstoneOre, "deepslate_redstone_ore");
        bind(loader, normal, textures, Backport120Content.deepslateEmeraldOre, "deepslate_emerald_ore");
        bind(loader, normal, textures, Backport120Content.deepslateLapisOre, "deepslate_lapis_ore");
        bind(loader, normal, textures, Backport120Content.deepslateDiamondOre, "deepslate_diamond_ore");
        bind(loader, normal, textures, Backport120Content.cornflower, "cornflower");
        bind(loader, normal, textures, Backport120Content.lilyOfTheValley, "lily_of_the_valley");
        loader.register(Backport120Content.sweetBerryBush, state("sweet_berry_bush"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata){return "age="+(metadata&3);}
                }, textures);
        loader.register(Backport120Content.bamboo, state("bamboo"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata){
                        int leaves=(metadata>>>1)&3;return "age="+(metadata&1)+",leaves="+(leaves==1?"small":leaves==2?"large":"none");
                    }
                }, textures);
        bind(loader, normal, textures, Backport120Content.bambooSapling, "bamboo_sapling");
        // Glow lichen uses the native 1.7 vine renderer (render type 20).
        bind(loader, normal, textures, Backport120Content.amethystBlock, "amethyst_block");
        bind(loader, normal, textures, Backport120Content.buddingAmethyst, "budding_amethyst");
        bindFacing(loader, textures, Backport120Content.smallAmethystBud, "small_amethyst_bud");
        bindFacing(loader, textures, Backport120Content.mediumAmethystBud, "medium_amethyst_bud");
        bindFacing(loader, textures, Backport120Content.largeAmethystBud, "large_amethyst_bud");
        bindFacing(loader, textures, Backport120Content.amethystCluster, "amethyst_cluster");
        bind(loader, normal, textures, Backport120Content.tintedGlass, "tinted_glass");
        loader.register(Backport120Content.suspiciousSand,state("suspicious_sand"),dustedMapper(),textures);
        loader.register(Backport120Content.suspiciousGravel,state("suspicious_gravel"),dustedMapper(),textures);
        for(int i=0;i<Backport120Content.glazedTerracotta.length;i++){
            final Block block=Backport120Content.glazedTerracotta[i];
            final String name=Backport120Content.TERRACOTTA_COLORS[i]+"_glazed_terracotta";
            loader.register(block,state(name),new JsonBlockModelLoader.StateMapper(){
                @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata){
                    return "facing="+new String[]{"north","east","south","west"}[metadata&3];
                }
            },textures);
        }
        bind(loader,normal,textures,com.mod.htgt6.minecraft1202.Module.Registry.Modern.LodestoneContent116.lodestone,"lodestone");
        loader.register(com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherUtilityContent116.anchor,state("respawn_anchor"),new JsonBlockModelLoader.StateMapper(){
            @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata){return "charges="+Math.max(0,Math.min(4,metadata));}
        },textures);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new SoulSpeedClient116());
        LodestoneCompassClient116.register();
        loader.registerItem(com.mod.htgt6.minecraft1202.Module.Registry.Modern.LodestoneContent116.compass,
                new net.minecraft.util.ResourceLocation("htgt6","item/1_20_backport/lodestone_compass"),
                new JsonBlockModelLoader.TextureResolver(){
                    @Override public ResourceLocation resolve(String texture){
                        int colon=texture.indexOf(':');String path=colon<0?texture:texture.substring(colon+1);
                        if(path.startsWith("item/"))path=path.substring(5);
                        return new ResourceLocation("htgt6",path.startsWith("1_20_backport/")?path:"1_20_backport/"+path);
                    }
                });
    }

    private static void bindNetherVegetation(JsonBlockModelLoader loader,JsonBlockModelLoader.TextureResolver textures){
        for(int i=0;i<com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherVegetationContent116.POTS.length;i++)loader.register(com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherVegetationContent116.POTS[i],state("potted_"+com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherVegetationContent116.NAMES[i+2]),(w,x,y,z,m)->"normal",textures);
        for(int i=0;i<com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherVegetationContent116.BLOCKS.length;i++){
            Block block=com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherVegetationContent116.BLOCKS[i];
            String name=com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherVegetationContent116.NAMES[i];
            loader.register(block,state(name),(w,x,y,z,m)->"normal",textures);
            loader.registerItem(net.minecraft.item.Item.getItemFromBlock(block),new ResourceLocation("htgt6","item/1_20_backport/"+name),textures);
        }
        for(com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockNetherVine116[] vines:new com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockNetherVine116[][]{com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherVegetationContent116.twisting,com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherVegetationContent116.weeping}){
            String name=vines[0].direction>0?"twisting_vines":"weeping_vines";
            for(int i=0;i<3;i++)loader.register(vines[i],state(name+(i==2?"_plant":"")),(w,x,y,z,m)->"normal",textures);
            loader.registerItem(net.minecraft.item.Item.getItemFromBlock(vines[0]),new ResourceLocation("htgt6","item/1_20_backport/"+name),textures);
        }
    }
    private static void bindSoulCampfire(JsonBlockModelLoader loader,JsonBlockModelLoader.TextureResolver textures){
        loader.register(com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherUtilityContent116.soulCampfire,state("soul_campfire"),
            (world,x,y,z,meta)->"facing="+new String[]{"south","west","north","east"}[meta&3]+",lit="+com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockCampfire149.lit(meta),textures);
        loader.registerItem(net.minecraft.item.Item.getItemFromBlock(com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherUtilityContent116.soulCampfire),new ResourceLocation("htgt6","item/1_20_backport/soul_campfire"),textures);
    }

    private static void bindNetherWoodFamilies(JsonBlockModelLoader loader,JsonBlockModelLoader.TextureResolver textures){
        for(com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherWoodContent116.Family f:com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherWoodContent116.FAMILIES){
            com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120WoodFamily w=f.wood;
            loader.register(w.planks,state(f.name+"_planks"),(world,x,y,z,meta)->"normal",textures);
            bindAxis(loader,textures,f.stem,f.name+"_stem");bindAxis(loader,textures,f.hyphae,f.name+"_hyphae");
            bindAxis(loader,textures,f.strippedStem,"stripped_"+f.name+"_stem");bindAxis(loader,textures,f.strippedHyphae,"stripped_"+f.name+"_hyphae");
            bindStairs(loader,textures,w.stairs,f.name+"_stairs");bindSlab(loader,textures,w.slab,w.doubleSlab,f.name+"_slab");
            bindFence(loader,textures,w.fence,f.name+"_fence");bindFenceGate(loader,textures,w.fenceGate,f.name+"_fence_gate");
            bindDoor(loader,textures,w.doorBlock,f.name+"_door");bindTrapDoor(loader,textures,w.trapdoor,f.name+"_trapdoor");
            loader.register(w.pressurePlate,state(f.name+"_pressure_plate"),(world,x,y,z,meta)->"powered="+(meta!=0),textures);
            loader.register(w.button,state(f.name+"_button"),(world,x,y,z,meta)->{
                int face=0,direction=2;
                if(world!=null&&world.getTileEntity(x,y,z) instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button){
                    com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button b=(com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Button)world.getTileEntity(x,y,z);face=b.getFace();direction=b.getFacing();
                }
                return "face="+(face==1?"floor":face==2?"ceiling":"wall")+",facing="+facing(direction)+",powered="+((meta&1)!=0);
            },textures);
            net.minecraft.item.Item[] items={w.door,w.sign,w.hangingSign};String[] names={"door","sign","hanging_sign"};
            for(int i=0;i<items.length;i++)loader.registerItem(items[i],new ResourceLocation("htgt6","item/1_20_backport/"+f.name+"_"+names[i]),textures);
        }
    }

    private static void bind(JsonBlockModelLoader loader, JsonBlockModelLoader.StateMapper mapper,
                             JsonBlockModelLoader.TextureResolver textures, Block block, String name) {
        loader.register(block, state(name), mapper, textures);
    }

    private static void bindAxis(JsonBlockModelLoader loader, JsonBlockModelLoader.TextureResolver textures,
                                 Block block, String name) {
        loader.register(block, state(name), new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                int axis = metadata & 12;
                return axis == 4 ? "axis=x" : axis == 8 ? "axis=z" : "axis=y";
            }
        }, textures);
    }

    private static void bindStripped(JsonBlockModelLoader loader,JsonBlockModelLoader.TextureResolver textures,
                                     Block block,String name,final String[] species){
        loader.register(block,state(name),new JsonBlockModelLoader.StateMapper(){
            @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata){
                int index=metadata&3;if(index>=species.length)index=0;
                int axis=metadata&12;
                return "species="+species[index]+",axis="+(axis==4?"x":axis==8?"z":"y");
            }
        },textures);
    }

    private static void bindStairs(JsonBlockModelLoader loader, JsonBlockModelLoader.TextureResolver textures,
                                   Block block, String name) {
        loader.register(block, state(name), new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                int direction = metadata & 3;
                return "facing=" + stairFacing(direction) + ",half="
                        + ((metadata & 4) != 0 ? "top" : "bottom") + ",shape="
                        + (world == null ? "straight" : stairShape(world, x, y, z, direction, metadata & 4));
            }
        }, textures);
    }

    private static void bindSlab(JsonBlockModelLoader loader, JsonBlockModelLoader.TextureResolver textures,
                                 Block half, Block full, String name) {
        loader.register(half, state(name), new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                return "type=" + ((metadata & 8) != 0 ? "top" : "bottom");
            }
        }, textures);
        loader.register(full, state(name), new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                return "type=double";
            }
        }, textures);
    }

    private static void bindModernStoneFamilies(JsonBlockModelLoader loader, JsonBlockModelLoader.TextureResolver textures) {
        for(com.mod.htgt6.minecraft1202.Module.Registry.Modern.ModernStoneShapes120.Family family:
                com.mod.htgt6.minecraft1202.Module.Registry.Modern.ModernStoneShapes120.FAMILIES){
            bindStairs(loader,textures,family.stairs,family.name+"_stairs");
            bindSlab(loader,textures,family.slab,family.doubleSlab,family.name+"_slab");
            if(family.wall!=null)bindWall(loader,textures,family.wall,family.name+"_wall");
        }
    }

    private static void bindWall(JsonBlockModelLoader loader, JsonBlockModelLoader.TextureResolver textures,
                                 final Block block, String name) {
        loader.register(block, state(name), new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                if (world == null) return "inventory";
                return ((com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBlackstoneWall116)block).state(world,x,y,z).variant();
            }
        }, textures);
    }

    private static void bindFence(JsonBlockModelLoader loader, JsonBlockModelLoader.TextureResolver textures,
                                  final Block block, String name) {
        loader.register(block, state(name), new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                if (world == null) return "inventory";
                com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Fence fence =
                        (com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Fence) block;
                return "east=" + fence.canConnectFenceTo(world, x + 1, y, z)
                        + ",north=" + fence.canConnectFenceTo(world, x, y, z - 1)
                        + ",south=" + fence.canConnectFenceTo(world, x, y, z + 1)
                        + ",west=" + fence.canConnectFenceTo(world, x - 1, y, z);
            }
        }, textures);
    }

    private static void bindFenceGate(JsonBlockModelLoader loader, JsonBlockModelLoader.TextureResolver textures,
                                      Block block, String name) {
        loader.register(block, state(name), new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                int direction = metadata & 3;
                boolean inWall = false;
                if (world != null) {
                    if (direction == 0 || direction == 2) {
                        inWall = world.getBlock(x - 1, y, z) instanceof net.minecraft.block.BlockWall
                                || world.getBlock(x + 1, y, z) instanceof net.minecraft.block.BlockWall;
                    } else {
                        inWall = world.getBlock(x, y, z - 1) instanceof net.minecraft.block.BlockWall
                                || world.getBlock(x, y, z + 1) instanceof net.minecraft.block.BlockWall;
                    }
                }
                return "facing=" + facing(direction) + ",in_wall=" + inWall
                        + ",open=" + ((metadata & 4) != 0);
            }
        }, textures);
    }

    private static void bindTrapDoor(JsonBlockModelLoader loader, JsonBlockModelLoader.TextureResolver textures,
                                     Block block, String name) {
        loader.register(block, state(name), new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                return "facing=" + new String[]{"south", "north", "east", "west"}[metadata & 3]
                        + ",half=" + ((metadata & 8) != 0 ? "top" : "bottom")
                        + ",open=" + ((metadata & 4) != 0);
            }
        }, textures);
    }

    private static void bindDoor(JsonBlockModelLoader loader, JsonBlockModelLoader.TextureResolver textures,
                                 final Block block, String name) {
        loader.register(block, state(name), new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                if (world == null) return "facing=east,half=lower,hinge=left,open=false";
                net.minecraft.block.BlockDoor door = (net.minecraft.block.BlockDoor) block;
                int combined = door.func_150012_g(world, x, y, z);
                return "facing=" + new String[]{"east", "south", "west", "north"}[combined & 3]
                        + ",half=" + ((metadata & 8) != 0 ? "upper" : "lower")
                        + ",hinge=" + ((combined & 16) != 0 ? "right" : "left")
                        + ",open=" + ((combined & 4) != 0);
            }
        }, textures);
    }

    private static String wallSide(boolean connected, boolean tall) {
        return connected ? tall ? "tall" : "low" : "none";
    }

    private static String stairFacing(int direction) {
        return new String[]{"east", "west", "south", "north"}[direction & 3];
    }

    private static String stairShape(IBlockAccess world, int x, int y, int z,
                                     int direction, int half) {
        int frontX = x + stairDx(direction), frontZ = z + stairDz(direction);
        int neighbor = stairDirection(world, frontX, y, frontZ, half);
        if (neighbor >= 0 && stairAxis(neighbor) != stairAxis(direction)
                && !sameStair(world, x + stairDx(opposite(neighbor)), y,
                        z + stairDz(opposite(neighbor)), direction, half)) {
            return neighbor == stairLeft(direction) ? "outer_left" : "outer_right";
        }
        int back = opposite(direction);
        neighbor = stairDirection(world, x + stairDx(back), y, z + stairDz(back), half);
        if (neighbor >= 0 && stairAxis(neighbor) != stairAxis(direction)
                && !sameStair(world, x + stairDx(neighbor), y, z + stairDz(neighbor), direction, half)) {
            return neighbor == stairLeft(direction) ? "inner_left" : "inner_right";
        }
        return "straight";
    }

    private static int stairDirection(IBlockAccess world, int x, int y, int z, int half) {
        if (!(world.getBlock(x, y, z) instanceof net.minecraft.block.BlockStairs)) return -1;
        int metadata = world.getBlockMetadata(x, y, z);
        return (metadata & 4) == half ? metadata & 3 : -1;
    }

    private static boolean sameStair(IBlockAccess world, int x, int y, int z, int direction, int half) {
        return stairDirection(world, x, y, z, half) == direction;
    }

    private static int stairDx(int direction) { return direction == 0 ? 1 : direction == 1 ? -1 : 0; }
    private static int stairDz(int direction) { return direction == 2 ? 1 : direction == 3 ? -1 : 0; }
    private static int stairAxis(int direction) { return direction < 2 ? 0 : 1; }
    private static int opposite(int direction) { return direction ^ 1; }
    private static int stairLeft(int direction) {
        return new int[]{3, 2, 0, 1}[direction & 3];
    }

    private static void bindFacing(JsonBlockModelLoader loader, JsonBlockModelLoader.TextureResolver textures,
                                   Block block, String name) {
        loader.register(block, state(name), new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata) {
                return "facing=" + new String[]{"down","up","north","south","west","east"}[metadata < 6 ? metadata : 1];
            }
        }, textures);
    }

    private static ResourceLocation state(String name) {
        return new ResourceLocation("htgt6", "1_20_backport/" + name);
    }

    private static JsonBlockModelLoader.StateMapper berryMapper() {
        return new JsonBlockModelLoader.StateMapper() {
            @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                return "berries=" + ((metadata & 8) != 0 ? "true" : "false");
            }
        };
    }

    private static JsonBlockModelLoader.StateMapper dustedMapper(){return new JsonBlockModelLoader.StateMapper(){
        @Override public String variant(IBlockAccess world,int x,int y,int z,int metadata){return "dusted="+Math.min(3,metadata&3);}
    };}

    private static String facing(int metadata) {
        return new String[]{"south", "west", "north", "east"}[metadata & 3];
    }

    private Minecraft120BackportClient() { }
}
