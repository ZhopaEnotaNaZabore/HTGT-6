package com.mod.htgt6.asm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

/**
 * Minecraft 1.7.10 extended biome-ID coremod.
 *
 * IMPORTANT:
 * - Does not modify <clinit>.
 * - Keeps BiomeGenBase.biomeList as the legacy 0..255 array.
 * - Stores IDs >= 256 in a primitive-like int keyed registry (ConcurrentHashMap here).
 * - Keeps Chunk.getBiomeArray()/setBiomeArray(byte[]) for binary compatibility and
 *   adds an int[] extended array used by the transformed biome path.
 *
 * This source is written for the MCP/deobfuscated 1.7.10 names shown by the
 * uploaded class files. For an obfuscated production jar, map the names/descriptors.
 */
public final class HTGT6Transformer implements IClassTransformer {

    private static final Logger LOGGER = LogManager.getLogger("HTGT6-ASM");

    private static final String BIOME = "net/minecraft/world/biome/BiomeGenBase";
    private static final String WORLD = "net/minecraft/world/World";
    private static final String CHUNK = "net/minecraft/world/chunk/Chunk";
    private static final String EXTENDED_BLOCK_STORAGE =
            "net/minecraft/world/chunk/storage/ExtendedBlockStorage";
    private static final String CHUNK_PROVIDER_GENERATE = "net/minecraft/world/gen/ChunkProviderGenerate";
    private static final String ANVIL = "net/minecraft/world/chunk/storage/AnvilChunkLoader";
    private static final String DICT = "net/minecraftforge/common/BiomeDictionary";
    private static final String S21 = "net/minecraft/network/play/server/S21PacketChunkData";
    private static final String S26 = "net/minecraft/network/play/server/S26PacketMapChunkBulk";
    private static final String S22 = "net/minecraft/network/play/server/S22PacketMultiBlockChange";
    private static final String S23 = "net/minecraft/network/play/server/S23PacketBlockChange";
    private static final String CLIENT_HANDLER = "net/minecraft/client/network/NetHandlerPlayClient";
    private static final String RENDER_GLOBAL = "net/minecraft/client/renderer/RenderGlobal";
    private static final String WORLD_RENDERER = "net/minecraft/client/renderer/WorldRenderer";
    private static final String WORLD_CLIENT = "net/minecraft/client/multiplayer/WorldClient";
    private static final String SODIUM_CHUNK_RENDER_COLUMN =
            "me/jellysquid/mods/sodium/client/render/chunk/ChunkRenderColumn";
    private static final String SODIUM_CHUNK_RENDER_MANAGER =
            "me/jellysquid/mods/sodium/client/render/chunk/ChunkRenderManager";
    private static final String SODIUM_CHUNK_GRAPH_CULLER =
            "me/jellysquid/mods/sodium/client/render/chunk/cull/graph/ChunkGraphCuller";
    private static final String SODIUM_CLONED_CHUNK_SECTION =
            "me/jellysquid/mods/sodium/client/world/cloned/ClonedChunkSection";
    private static final String SODIUM_WORLD_SLICE =
            "me/jellysquid/mods/sodium/client/world/WorldSlice";
    private static final String SERVER_HANDLER = "net/minecraft/network/NetHandlerPlayServer";
    private static final String PLAYER_INSTANCE = "net/minecraft/server/management/PlayerManager$PlayerInstance";
    private static final String CREATIVE_GUI = "net/minecraft/client/gui/inventory/GuiContainerCreative";
    private static final String ITEM_MAP = "net/minecraft/item/ItemMap";
    private static final String ITEM_RENDERER = "net/minecraft/client/renderer/ItemRenderer";
    private static final String ENTITY_RENDERER = "net/minecraft/client/renderer/EntityRenderer";
    private static final String RENDER_PLAYER = "net/minecraft/client/renderer/entity/RenderPlayer";
    private static final String MODEL_BIPED = "net/minecraft/client/model/ModelBiped";
    private static final String MINECRAFT = "net/minecraft/client/Minecraft";
    private static final String FML_CLIENT_HANDLER = "cpw/mods/fml/client/FMLClientHandler";
    private static final String SPLASH_PROGRESS = "cpw/mods/fml/client/SplashProgress";
    private static final String KEY_BINDING = "net/minecraft/client/settings/KeyBinding";
    private static final String CONTAINER_PLAYER = "net/minecraft/inventory/ContainerPlayer";
    private static final String CONTAINER_PLAYER_ARMOR_SLOT = "net/minecraft/inventory/ContainerPlayer$1";
    private static final String INVENTORY_PLAYER = "net/minecraft/entity/player/InventoryPlayer";
    private static final String ENTITY_ZOMBIE = "net/minecraft/entity/monster/EntityZombie";
    private static final String ENTITY_VILLAGER = "net/minecraft/entity/passive/EntityVillager";
    private static final String POTION = "net/minecraft/potion/Potion";
    private static final String POTION_EFFECT = "net/minecraft/potion/PotionEffect";
    private static final String ENTITY_EFFECT_PACKET =
            "net/minecraft/network/play/server/S1DPacketEntityEffect";
    private static final String REMOVE_EFFECT_PACKET =
            "net/minecraft/network/play/server/S1EPacketRemoveEntityEffect";
    private static final String SAVE_HANDLER = "net/minecraft/world/storage/SaveHandler";
    private static final String WORLD_PROVIDER_END = "net/minecraft/world/WorldProviderEnd";
    private static final String WORLD_PROVIDER = "net/minecraft/world/WorldProvider";
    private static final String AMUN_RA = "de/katzenpapst/amunra/AmunRa";
    private static final String AMUN_RA_NANOTOOL = "de/katzenpapst/amunra/item/ItemNanotool";
    private static final String AMUN_RA_WORLD_PROVIDER_PREFIX = "de/katzenpapst/amunra/world/";
    private static final String AMUN_RA_WORLD_CHUNK_MANAGER = "de/katzenpapst/amunra/world/AmunraWorldChunkManager";
    private static final String AMUN_RA_COMPAT_CHUNK_MANAGER =
            "com/mod/htgt6/sulimited/galacticraft/AmunRaWorldChunkManager";
    private static final String GT_WATER_BASE = "gregtech/blocks/fluids/BlockWaterlike";
    private static final String GT_WATER_OCEAN = "gregtech/blocks/fluids/BlockOcean";
    private static final String GT_WATER_RIVER = "gregtech/blocks/fluids/BlockRiver";
    private static final String GT_WATER_SWAMP = "gregtech/blocks/fluids/BlockSwamp";
    private static final String GT_WORLDGEN_OCEAN = "gregtech/worldgen/WorldgenOcean";
    private static final String GT_WORLDGEN_RIVER = "gregtech/worldgen/WorldgenRiver";
    private static final String GT_WORLDGEN_SWAMP = "gregtech/worldgen/WorldgenSwamp";
    private static final String GT_WORLDGEN_STONE_LAYERS = "gregtech/worldgen/WorldgenStoneLayers";
    private static final String GT_ORE_WASHING =
            "gregapi/oredict/listeners/OreDictListenerItem_Washing";
    private static final String GT_API = "gregapi/GT_API";
    private static final String GT_MULTI_TILE_ITEM =
            "gregapi/block/multitileentity/MultiTileEntityItemInternal";
    private static final String ENDER_CORE_METHODS =
            "com/enderio/core/common/transform/EnderCoreMethods";
    private static final String OPENMODS_FRAMEBUFFER_HOOKS =
            "openmods/stencil/FramebufferHooks";
    private static final String CRASH_REPORT = "net/minecraft/crash/CrashReport";
    private static final String CRASH_REPORT_CATEGORY = "net/minecraft/crash/CrashReportCategory";
    private static final String FML_DIRECTORY_DISCOVERER =
            "cpw/mods/fml/common/discovery/DirectoryDiscoverer";
    private static final String HEE_CLIENT_PROXY = "chylex/hee/proxy/ModClientProxy";
    private static final String ITLT_CLIENT_PROXY = "dk/zlepper/itlt/proxies/ClientProxy";
    private static final String VILLAGE_NAMES_FUNCTIONS =
            "astrotibs/villagenames/utility/FunctionsVN";
    private static final String VILLAGE_NAMES_ENTITY_MONITOR =
            "astrotibs/villagenames/handler/EntityMonitorHandler";
    private static final String NOVA_REGISTRIES =
            "com/NovaCraft/registry/NovaCraftRegistries";

    private static final String SELF = "com/mod/htgt6/asm/HTGT6Transformer";
    private static final String CONFIG = "com/mod/htgt6/asm/HTGT6Config";

    /* biome IDs are ints; this map therefore supports the full positive int range. */
    private static final Map<Integer, Object> EXTENDED_BIOMES = new ConcurrentHashMap<Integer, Object>();
    private static final Map<Integer, java.util.EnumSet<net.minecraftforge.common.BiomeDictionary.Type>>
            EXTENDED_BIOME_TYPES = new ConcurrentHashMap<Integer, java.util.EnumSet<net.minecraftforge.common.BiomeDictionary.Type>>();
    private static final Map<net.minecraft.world.World, Map<Long, java.util.List<PendingLayerOre>>>
            PENDING_LAYER_ORES = java.util.Collections.synchronizedMap(
            new java.util.WeakHashMap<net.minecraft.world.World, Map<Long, java.util.List<PendingLayerOre>>>());
    private static final Map<net.minecraft.world.chunk.Chunk, Boolean> VALIDATED_OCEAN_BIOME_CHUNKS =
            java.util.Collections.synchronizedMap(
                    new java.util.WeakHashMap<net.minecraft.world.chunk.Chunk, Boolean>());
    private static final Map<short[], int[]> FUTURA_BLOCK_UPDATE_POSITIONS =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<short[], int[]>());
    /** 0 unknown, 1 invalid, 2 air, 3 non-ticking solid, 4 randomly ticking solid. */
    private static final byte[] CHUNK_SECTION_BLOCK_STATES = new byte[65536];
    private static volatile java.lang.reflect.Method NEID_BLOCK_ARRAY_ACCESSOR;
    private static volatile boolean NEID_BLOCK_ARRAY_ACCESSOR_RESOLVED;
    private static volatile Boolean NEID_REMOVE_INVALID_BLOCKS;
    private static volatile java.lang.reflect.Field EXTENDED_BIOME_FIELD;

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if(basicClass!=null&&"net.minecraftforge.event.ForgeEventFactory".equals(transformedName))return OutpostSpawnASM120.transform(basicClass);
        if (basicClass == null) return null;
        // The transformer and its coremod support classes are loaded by
        // LaunchClassLoader as one unit. Never run HTGT6 ASM over that unit:
        // doing so can recursively resolve HTGT6Config while it is being
        // defined and results in ClassCircularityError.
        if ((name != null && (name.startsWith("com.mod.htgt6.asm.")
                || name.startsWith("com/mod/htgt6/asm/")))
                || (transformedName != null && (transformedName.startsWith("com.mod.htgt6.asm.")
                || transformedName.startsWith("com/mod/htgt6/asm/")))) {
            return basicClass;
        }
        String target = transformedName != null ? transformedName : name;
        if (HTGT6Config.FIX_SKIN_PORT_BROKEN_RENDER
                && SkinPortRenderFixASM.TARGET.equals(target)) {
            return SkinPortRenderFixASM.transform(basicClass);
        }
        String normalizedName = normalizeClassName(name);
        String normalizedTransformed = normalizeClassName(transformedName);

        try {
            if (isClass("betterquesting/api/storage/BQ_Settings", normalizedName, normalizedTransformed)
                    || isClass("betterquesting/handlers/ConfigHandler", normalizedName, normalizedTransformed))
                return QuestThemeDefaultsASM.transform(basicClass);
            if(isClass("net/minecraftforge/client/GuiIngameForge",normalizedName,normalizedTransformed))
                return AquaticAirHudASM.transform(basicClass);
            if (isClass("com/gtnewhorizons/angelica/dynamiclights/DynamicLights", normalizedName, normalizedTransformed))
                return OffhandLightASM.transform(basicClass);
            if (isClass("net/minecraft/network/NetworkManager", normalizedName, normalizedTransformed))
                return NetworkDiagnosticsASM.transform(basicClass);
            if(HTGT6Config.RUN_MINECRAFT_1_7_9999 && isClass("net/minecraft/block/BlockPistonBase",normalizedName,normalizedTransformed))return StandalonePistonsASM115.transform(basicClass,false);
            if(HTGT6Config.RUN_MINECRAFT_1_7_9999 && isClass("net/minecraft/tileentity/TileEntityPiston",normalizedName,normalizedTransformed))return StandalonePistonsASM115.transform(basicClass,true);
            if(isClass("net/minecraft/block/BlockStairs",normalizedName,normalizedTransformed))return BlackstoneStairsASM116.transform(basicClass);
            if(isClass("net/minecraft/entity/item/EntityItem",normalizedName,normalizedTransformed))return NetheriteItemASM120.transform(basicClass);
            if(isClass("net/minecraftforge/client/ForgeHooksClient",normalizedName,normalizedTransformed))return ArmorTrimASM120.transform(basicClass);
            if(isClass("net/minecraft/entity/projectile/EntityArrow",normalizedName,normalizedTransformed))
                return TargetImpactASM116.transform(transformFrostburnArrow120(basicClass));
            if(isClass("net/minecraft/entity/projectile/EntityFishHook",normalizedName,normalizedTransformed))return OpenWaterFishingASM116.transform(TargetImpactASM116.transform(basicClass));
            if(isClass("net/minecraft/entity/projectile/EntityThrowable",normalizedName,normalizedTransformed)
                    ||isClass("net/minecraft/entity/projectile/EntityFireball",normalizedName,normalizedTransformed))
                return TargetImpactASM116.transform(basicClass);
            if(isClass("net/minecraft/entity/projectile/EntityPotion",normalizedName,normalizedTransformed))
                return CampfireWaterASM157.transform(basicClass);
            if(isClass("net/minecraft/item/ItemBoat",normalizedName,normalizedTransformed))return transformCombatOakBoat120(basicClass);
            if(isClass("net/minecraft/entity/item/EntityBoat",normalizedName,normalizedTransformed))return CombatBoatMotionASM.transform(basicClass);
            if(isClass("net/minecraft/entity/item/EntityFallingBlock",normalizedName,normalizedTransformed))return ConcreteFallingASM.transform(basicClass);
            if (isClass("net/minecraft/entity/passive/EntityHorse", normalizedName, normalizedTransformed))
                return LeatherHorseArmorASM151.transform(basicClass);
            if (isClass("net/minecraft/entity/monster/EntityEnderman", normalizedName, normalizedTransformed))
                return transformBountifulEnderman120(basicClass);
            if (isClass("net/minecraft/tileentity/TileEntityFurnace", normalizedName, normalizedTransformed))
                return transformBountifulFurnace120(basicClass);
            if (isClass("net/minecraft/tileentity/TileEntityBrewingStand", normalizedName, normalizedTransformed))
                return transformCombatBrewing120(basicClass);
            if (isClass("net/minecraft/entity/Entity", normalizedName, normalizedTransformed))
                return WaterloggedFluidASM.transformEntity(BerryMovementASM.transform(SwimmingPoseASM.transform(transformBountifulLanding120(basicClass),0)));
            if (normalizedTransformed != null && normalizedTransformed.startsWith("net/minecraft/entity/EntityLivingBase$"))
                return transformCrucibleShieldModifier(basicClass);
            if (isClass("net/minecraft/item/ItemSword", normalizedName, normalizedTransformed))return CombatPipelineASM120.transform(basicClass,4);
            if (isClass("net/minecraft/world/Explosion", normalizedName, normalizedTransformed)) return AnchorExplosionASM116.transform(basicClass);
            if (isClass("net/minecraft/server/management/ServerConfigurationManager", normalizedName, normalizedTransformed)) return RespawnAnchorASM116.transform(basicClass,false);
            if (isClass("net/minecraft/block/BlockSoulSand", normalizedName, normalizedTransformed)) return SoulSpeedASM116.transform(basicClass);
            if (isClass("net/minecraft/block/BlockRedstoneWire", normalizedName, normalizedTransformed))return RedstoneWireASM116.transform(basicClass);
            if (isClass("net/minecraft/block/BlockWall", normalizedName, normalizedTransformed))return NativeWallASM116.transform(basicClass);
            if (isClass("net/minecraft/block/BlockLiquid", normalizedName, normalizedTransformed))return LavaMixingASM116.transform(basicClass);
            if (isClass("net/minecraft/item/ItemHoe", normalizedName, normalizedTransformed))return HoeMiningASM116.transform(basicClass);
            if (isClass("net/minecraft/enchantment/Enchantment", normalizedName, normalizedTransformed))return NetheriteHoeEnchantmentASM116.transform(basicClass);
            if (isClass("net/minecraft/enchantment/EnchantmentHelper", normalizedName, normalizedTransformed))return CombatPipelineASM120.transform(basicClass,2);
            if (isClass("net/minecraft/enchantment/EnchantmentDamage", normalizedName, normalizedTransformed))return CombatPipelineASM120.transform(basicClass,3);
            if (isClass("net/minecraft/entity/EntityLivingBase", normalizedName, normalizedTransformed))
                return ScaffoldingMovementASM152.transform(AquaticSwimmingASM.transform(ConduitPowerASM.transform(AquaticBreathingASM.transform(transformCombatLevitation120(transformBubbleBreathing120(transformBountifulWater120(CombatPipelineASM120.transform(basicClass,1))))),false)));
            if (isClass("net/minecraft/entity/item/EntityEnderPearl", normalizedName, normalizedTransformed))
                return transformBountifulPearl120(basicClass);
            if (isClass("invtweaks/containers/VanillaSlotMaps", normalizedName, normalizedTransformed))
                return transformInventoryTweaksPlayerSlots(basicClass);
            if (isClass("invtweaks/InvTweaks", normalizedName, normalizedTransformed))
                return transformInventoryTweaksRefill(basicClass);
            if (isClass("net/minecraft/block/BlockCactus", normalizedName, normalizedTransformed))
                return transformColumnPlant120(basicClass,true);
            if (isClass("net/minecraft/block/BlockReed", normalizedName, normalizedTransformed))
                return transformColumnPlant120(basicClass,false);
            if (isClass("net/minecraft/item/ItemLilyPad", normalizedName, normalizedTransformed))
                return transformLilyPlacement120(basicClass);
            if (isClass("net/minecraft/block/BlockMushroom", normalizedName, normalizedTransformed)
                    || isClass("net/minecraft/block/BlockLilyPad", normalizedName, normalizedTransformed)) {
                return transformPlantSurvivalHeight120(basicClass);
            }
            if (isClass(ENDER_CORE_METHODS, normalizedName, normalizedTransformed)) {
                return transformEnderCoreMethodsForDev(basicClass);
            }
            if (isClass(OPENMODS_FRAMEBUFFER_HOOKS, normalizedName, normalizedTransformed)) {
                return transformOpenModsFramebufferHooksForDev(basicClass);
            }
            if (isClass(CRASH_REPORT, normalizedName, normalizedTransformed)) {
                return transformCrashReportForDev(basicClass);
            }
            if (isClass(CRASH_REPORT_CATEGORY, normalizedName, normalizedTransformed)) {
                return transformCrashReportCategoryForDev(basicClass);
            }
            if (isClass(FML_DIRECTORY_DISCOVERER, normalizedName, normalizedTransformed)) {
                return transformFmlDirectoryDiscoverer(basicClass);
            }
            if (HTGT6Config.ENABLE_NEW_FML_LOADING_SCREEN
                    && isClass(FML_CLIENT_HANDLER, normalizedName, normalizedTransformed)) {
                return transformLoadingFmlClientHandler(basicClass);
            }
            if (HTGT6Config.ENABLE_NEW_FML_LOADING_SCREEN
                    && isClass(SPLASH_PROGRESS, normalizedName, normalizedTransformed)) {
                return transformSplashProgressForHTGT6(basicClass);
            }
            if (isClass(KEY_BINDING, normalizedName, normalizedTransformed)) {
                return transformKeyBindingInventoryTweaksCompat(basicClass);
            }
            if (isClass(VILLAGE_NAMES_FUNCTIONS, normalizedName, normalizedTransformed)) {
                return transformVillageNamesTradeMonitor(basicClass);
            }
            if (isClass(VILLAGE_NAMES_ENTITY_MONITOR, normalizedName, normalizedTransformed)) {
                return transformVillageNamesEntityMonitor(basicClass);
            }
            if (isClass(NOVA_REGISTRIES, normalizedName, normalizedTransformed)) {
                return transformNovaCraftRecipeRegistration(basicClass);
            }
            if (isClass(HEE_CLIENT_PROXY, normalizedName, normalizedTransformed)
                    || isClass(ITLT_CLIENT_PROXY, normalizedName, normalizedTransformed)) {
                return transformWindowTitleWriter(basicClass,
                        className(normalizedName, normalizedTransformed));
            }
            if (isClass(AMUN_RA, normalizedName, normalizedTransformed)) {
                return transformAmunRaConfigPath(basicClass);
            }
            if (isClass(AMUN_RA_NANOTOOL, normalizedName, normalizedTransformed)) {
                return transformAmunRaNanotool(basicClass);
            }
            if (isAmunRaWorldProvider(normalizedName, normalizedTransformed)) {
                return transformAmunRaWorldProvider(basicClass);
            }
            if (HTGT6Config.FIX_GT6_WATER_TICK_UPDATE
                    && isGTWaterBlock(normalizedName, normalizedTransformed)) {
                return transformGTWaterBlock(basicClass,
                        className(normalizedName, normalizedTransformed));
            }
            if (HTGT6Config.FIX_GT6_WATER_TICK_UPDATE
                    && isGTWaterWorldgen(normalizedName, normalizedTransformed)) {
                return transformGTWaterWorldgen(basicClass,
                        className(normalizedName, normalizedTransformed));
            }
            if (HTGT6Config.FIX_GT6_STONE_GEN_LAG
                    && isClass(GT_WORLDGEN_STONE_LAYERS, normalizedName, normalizedTransformed)) {
                return transformGTStoneLayers(basicClass);
            }
            if (isClass(GT_ORE_WASHING, normalizedName, normalizedTransformed)) {
                return PlatinumGroupRecipeASM.transformDirectProcessing(HTGT6Config.FIX_GT6_CAUDRON_ISE
                        ? transformGT6CauldronWashing(basicClass) : basicClass, true);
            }
            if (isClass("gregtech/tileentity/multiblocks/MultiTileEntityCrucible", normalizedName, normalizedTransformed)) {
                return PlatinumGroupRecipeASM.transformDirectProcessing(basicClass, false);
            }
            if (isClass("net/minecraftforge/oredict/OreDictionary", normalizedName, normalizedTransformed)) {
                return IC2CircuitOreDictionaryASM.transform(basicClass);
            }
            if (isClass("betterquesting/api/utils/BigItemStack", normalizedName, normalizedTransformed)) {
                return QuestFluidDisplayASM.transform(basicClass);
            }
            if (isClass("gregapi/recipes/Recipe", normalizedName, normalizedTransformed)) {
                return PlatinumGroupRecipeASM.transform(basicClass);
            }
            if (isClass(GT_API, normalizedName, normalizedTransformed)) {
                return transformGT6WireStackSizeConfig(basicClass);
            }
            if (isClass(GT_MULTI_TILE_ITEM, normalizedName, normalizedTransformed)) {
                return transformGT6WireItemStackLimit(basicClass);
            }
            if (isClass(SAVE_HANDLER, normalizedName, normalizedTransformed)) {
                return transformSaveHandler(basicClass);
            }
            if (isClass("net/minecraftforge/common/DimensionManager", normalizedName, normalizedTransformed)) {
                return transformSaveDirectoryLookup(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(POTION, normalizedName, normalizedTransformed)) {
                return transformPotionRegistry120(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(POTION_EFFECT, normalizedName, normalizedTransformed)) {
                return transformPotionEffectNbt120(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(ENTITY_EFFECT_PACKET, normalizedName, normalizedTransformed)) {
                return transformEntityEffectPacket120(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(REMOVE_EFFECT_PACKET, normalizedName, normalizedTransformed)) {
                return transformRemoveEffectPacket120(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(INVENTORY_PLAYER, normalizedName, normalizedTransformed)) {
                return transformInventoryPlayer(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass("net/minecraft/entity/player/EntityPlayer", normalizedName, normalizedTransformed)) {
                return RespawnAnchorASM116.transform(SwimmingPoseASM.transform(CombatPipelineASM120.transform(transformShieldPlayerApi(basicClass, false),0),1),true);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass("net/minecraft/client/entity/EntityPlayerSP", normalizedName, normalizedTransformed)) {
                return SwimmingInputASM.transform(transformShieldPlayerApi(basicClass, true));
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(ENTITY_ZOMBIE, normalizedName, normalizedTransformed)) {
                return transformFrostburnSun120(transformZombieVillagerInfection(basicClass));
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass("net/minecraft/tileentity/MobSpawnerBaseLogic",normalizedName,normalizedTransformed)) {
                return transformFrostburnSpawner120(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(ENTITY_VILLAGER, normalizedName, normalizedTransformed)) {
                return VillagerEconomyASM145.transform(transformVillagerCareers112(basicClass));
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass("net/minecraft/entity/passive/EntityOcelot", normalizedName, normalizedTransformed)) {
                return CatSpeciesASM114.transform(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(CONTAINER_PLAYER, normalizedName, normalizedTransformed)) {
                return transformContainerPlayer(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(CONTAINER_PLAYER_ARMOR_SLOT, normalizedName, normalizedTransformed)) {
                return transformArmorSlotBindingCurse(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(ITEM_RENDERER, normalizedName, normalizedTransformed)) {
                return transformItemRenderer112(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(ENTITY_RENDERER, normalizedName, normalizedTransformed)) {
                return ConduitPowerASM.transform(SwimmingPoseASM.transform(transformEntityRenderer112(basicClass),2),true);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(RENDER_PLAYER, normalizedName, normalizedTransformed)) {
                return transformRenderPlayerElytra(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(MODEL_BIPED, normalizedName, normalizedTransformed)) {
                return transformModelBipedTridentPose(basicClass);
            }
            if (isClass(MINECRAFT, normalizedName, normalizedTransformed)) {
                byte[] transformed = (HTGT6Config.ENABLE_NEW_FML_LOADING_SCREEN
                        || HTGT6Config.ENABLE_WINDOW_BRANDING)
                        ? transformLoadingScreenMinecraft(basicClass) : basicClass;
                return HTGT6Config.RUN_MINECRAFT_1_7_9999
                        ? transformCreativePickBlock112(transformed) : transformed;
            }
            if (isClass(SERVER_HANDLER, normalizedName, normalizedTransformed)) {
                byte[] transformed = transformMissingPlacementSlotGuard(basicClass);
                return HTGT6Config.RUN_MINECRAFT_1_7_9999
                        ? transformCreativeOffhandValidation112(transformed) : transformed;
            }
            if (isClass(PLAYER_INSTANCE, normalizedName, normalizedTransformed)) {
                return transformPlayerInstanceBlockUpdates120(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(WORLD_PROVIDER_END, normalizedName, normalizedTransformed)) {
                return transformWorldProviderEnd(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999 && HTGT6Config.ENABLE_EXTENDED_BIOME_ASM
                    && isClass(WORLD_PROVIDER, normalizedName, normalizedTransformed)) {
                return transformWorldProviderHeight120(basicClass);
            }
            if ((HTGT6Config.FIX_GT6_WATER_TICK_UPDATE || HTGT6Config.FIX_GT6_STONE_GEN_LAG
                    || HTGT6Config.ENABLE_EXTENDED_BIOME_ASM)
                    && isClass(CHUNK_PROVIDER_GENERATE, normalizedName, normalizedTransformed)) {
                return transformChunkProviderGenerateWater(basicClass);
            }
            if (HTGT6Config.OPTIMIZE_CHUNK_SECTION_LOADING
                    && isClass(EXTENDED_BLOCK_STORAGE, normalizedName, normalizedTransformed)) {
                return transformExtendedBlockStorageLoading(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(CREATIVE_GUI, normalizedName, normalizedTransformed)) {
                return transformCreativeInventory(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(ITEM_MAP, normalizedName, normalizedTransformed)) {
                return transformLockedMapUpdate120(basicClass);
            }
            if (isClass(WORLD, normalizedName, normalizedTransformed)) {
                byte[] worldBytes=HTGT6Config.RUN_MINECRAFT_1_7_9999 && HTGT6Config.ENABLE_EXTENDED_BIOME_ASM
                        ? transformWorldInteractionHeight120(basicClass) : basicClass;
                return WaterloggedFluidASM.transformWorld(ObserverUpdatesASM.transform(worldBytes));
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999 && HTGT6Config.ENABLE_EXTENDED_BIOME_ASM
                    && isClass(SODIUM_CHUNK_RENDER_COLUMN, normalizedName, normalizedTransformed)) {
                return transformSodiumChunkRenderColumnHeight120(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999 && HTGT6Config.ENABLE_EXTENDED_BIOME_ASM
                    && isClass(SODIUM_CHUNK_RENDER_MANAGER, normalizedName, normalizedTransformed)) {
                return transformSodiumChunkRenderManagerHeight120(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999 && HTGT6Config.ENABLE_EXTENDED_BIOME_ASM
                    && isClass(SODIUM_CHUNK_GRAPH_CULLER, normalizedName, normalizedTransformed)) {
                return transformSodiumChunkGraphCullerHeight120(basicClass);
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999 && HTGT6Config.ENABLE_EXTENDED_BIOME_ASM
                    && (isClass(SODIUM_CLONED_CHUNK_SECTION, normalizedName, normalizedTransformed)
                    || isClass(SODIUM_WORLD_SLICE, normalizedName, normalizedTransformed))) {
                return transformSodiumBuildLimit120(basicClass,
                        className(normalizedName, normalizedTransformed));
            }
            if (HTGT6Config.RUN_MINECRAFT_1_7_9999
                    && isClass(CLIENT_HANDLER, normalizedName, normalizedTransformed)) {
                byte[] transformed = transformNetHandlerPotionIds120(basicClass);
                return HTGT6Config.ENABLE_EXTENDED_BIOME_ASM
                        ? transformNetHandlerPlayClient(transformed) : transformed;
            }
            if (!HTGT6Config.ENABLE_EXTENDED_BIOME_ASM) return basicClass;
            if (isClass(BIOME, normalizedName, normalizedTransformed)) {
                return transformBiomeGenBase(basicClass);
            }
            if (isClass(CHUNK, normalizedName, normalizedTransformed)) {
                return transformChunk(basicClass);
            }
            if (isClass(ANVIL, normalizedName, normalizedTransformed)) {
                return transformAnvilChunkLoader(basicClass);
            }
            if (isClass(DICT, normalizedName, normalizedTransformed)) {
                return transformBiomeDictionary(basicClass);
            }
            if (isClass(S21, normalizedName, normalizedTransformed)) {
                return transformS21PacketChunkData(basicClass);
            }
            if (isClass(S26, normalizedName, normalizedTransformed)) {
                return transformS26PacketMapChunkBulk(basicClass);
            }
            if (isClass(S22, normalizedName, normalizedTransformed)) {
                return transformS22PacketMultiBlockChange(basicClass);
            }
            if (isClass(S23, normalizedName, normalizedTransformed)) {
                return transformS23PacketBlockChange(basicClass);
            }
            if (isClass(RENDER_GLOBAL, normalizedName, normalizedTransformed)) {
                return ParrotRecordASM.transform(transformRenderGlobalHeight120(basicClass));
            }
            if (isClass(WORLD_RENDERER, normalizedName, normalizedTransformed)) {
                return validateWorldRendererHeight120(basicClass);
            }
            if (isClass(WORLD_CLIENT, normalizedName, normalizedTransformed)) {
                return transformWorldClientHeight120(basicClass);
            }
        } catch (Throwable t) {
            LOGGER.error("HTGT6 transformer failed for " + target, t);
            throw new RuntimeException("HTGT6 ASM transformation failed: " + target, t);
        }

        return basicClass;
    }

    /**
     * Infinity-style potion registry. 65536 slots keep the complete unsigned-short
     * namespace available while the companion NBT and packet patches retain the
     * actual int ID instead of truncating it to vanilla's signed byte.
     */
    private static byte[] transformPotionRegistry120(byte[] bytes) {
        ClassNode cn = read(bytes);
        int replacements = 0;
        for (MethodNode method : cn.methods) if ("<clinit>".equals(method.name)) {
            for (AbstractInsnNode node : method.instructions.toArray()) {
                if (!(node instanceof org.objectweb.asm.tree.TypeInsnNode)
                        || node.getOpcode() != Opcodes.ANEWARRAY
                        || !(POTION.equals(((org.objectweb.asm.tree.TypeInsnNode) node).desc)
                        || cn.name.equals(((org.objectweb.asm.tree.TypeInsnNode) node).desc))) continue;
                AbstractInsnNode size = previousCode(node);
                if (integerConstant(size) != 32) continue;
                method.instructions.set(size, new LdcInsnNode(Integer.valueOf(65536)));
                replacements++;
            }
        }
        if (replacements != 1) throw new IllegalStateException(
                "Potion.potionTypes allocation patch count was " + replacements + ", expected 1");
        LOGGER.info("HTGT6 Infinity potion registry expanded to 65536 IDs");
        return write(cn);
    }

    /** Stores PotionEffect Id as an NBT int and still reads legacy byte tags. */
    private static byte[] transformPotionEffectNbt120(byte[] bytes) {
        ClassNode cn = read(bytes);
        int writes = 0, reads = 0;
        for (MethodNode method : cn.methods) {
            if ("(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;"
                    .equals(method.desc)) {
                for (AbstractInsnNode node : method.instructions.toArray()) {
                    if (!(node instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) node;
                    if (!"net/minecraft/nbt/NBTTagCompound".equals(call.owner)
                            || !"(Ljava/lang/String;B)V".equals(call.desc)
                            || !hasStringBefore(call, "Id", 8)) continue;
                    AbstractInsnNode conversion = previousCode(call);
                    if (conversion != null && conversion.getOpcode() == Opcodes.I2B)
                        method.instructions.remove(conversion);
                    call.name = call.name.startsWith("func_") ? "func_74768_a" : "setInteger";
                    call.desc = "(Ljava/lang/String;I)V";
                    writes++;
                }
            } else if ((method.access & Opcodes.ACC_STATIC) != 0
                    && "(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/potion/PotionEffect;"
                    .equals(method.desc)) {
                for (AbstractInsnNode node : method.instructions.toArray()) {
                    if (!(node instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) node;
                    if (!"net/minecraft/nbt/NBTTagCompound".equals(call.owner)
                            || !"(Ljava/lang/String;)B".equals(call.desc)
                            || !hasStringBefore(call, "Id", 4)) continue;
                    call.name = call.name.startsWith("func_") ? "func_74762_e" : "getInteger";
                    call.desc = "(Ljava/lang/String;)I";
                    reads++;
                }
            }
        }
        if (writes != 1 || reads != 1) throw new IllegalStateException(
                "PotionEffect int-NBT patch mismatch: writes=" + writes + ", reads=" + reads);
        LOGGER.info("HTGT6 preserved full potion IDs in PotionEffect NBT");
        return write(cn);
    }

    private static byte[] transformEntityEffectPacket120(byte[] bytes) {
        ClassNode cn = read(bytes);
        final String fullId = "htgt6PotionId";
        String entityField = null, legacyIdField = null, amplifierField = null, durationField = null;
        for (FieldNode field : cn.fields) {
            if ((field.access & Opcodes.ACC_STATIC) != 0) continue;
            if ("I".equals(field.desc) && entityField == null) entityField = field.name;
            else if ("B".equals(field.desc) && legacyIdField == null) legacyIdField = field.name;
            else if ("B".equals(field.desc) && amplifierField == null) amplifierField = field.name;
            else if ("S".equals(field.desc) && durationField == null) durationField = field.name;
        }
        if (entityField == null || legacyIdField == null || amplifierField == null || durationField == null)
            throw new IllegalStateException("S1DPacketEntityEffect field layout differs from 1.7.10");
        cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_SYNTHETIC, fullId, "I", null, null));

        int constructors = 0, readers = 0, writers = 0;
        for (MethodNode method : cn.methods) {
            if ("<init>".equals(method.name)
                    && "(ILnet/minecraft/potion/PotionEffect;)V".equals(method.desc)) {
                String potionIdGetter = null;
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (POTION_EFFECT.equals(call.owner) && "()I".equals(call.desc)
                            && ("getPotionID".equals(call.name) || "func_76456_a".equals(call.name)))
                        potionIdGetter = call.name;
                }
                if (potionIdGetter == null)
                    throw new IllegalStateException("S1D constructor PotionEffect ID getter was not found");
                for (AbstractInsnNode node : method.instructions.toArray()) if (node.getOpcode() == Opcodes.RETURN) {
                    InsnList inject = new InsnList();
                    inject.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    inject.add(new VarInsnNode(Opcodes.ALOAD, 2));
                    inject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, POTION_EFFECT,
                            potionIdGetter, "()I", false));
                    inject.add(new FieldInsnNode(Opcodes.PUTFIELD, ENTITY_EFFECT_PACKET, fullId, "I"));
                    method.instructions.insertBefore(node, inject);
                    constructors++;
                }
            } else if ("(Lnet/minecraft/network/PacketBuffer;)V".equals(method.desc)
                    && callsMethod(method, "readInt")) {
                rebuildEntityEffectRead(method, entityField, legacyIdField, amplifierField, durationField, fullId);
                readers++;
            } else if ("(Lnet/minecraft/network/PacketBuffer;)V".equals(method.desc)
                    && callsMethod(method, "writeInt")) {
                rebuildEntityEffectWrite(method, entityField, amplifierField, durationField, fullId);
                writers++;
            }
        }
        MethodNode getter = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
                "htgt6$getPotionId120", "()I", null, null);
        getter.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        getter.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, ENTITY_EFFECT_PACKET, fullId, "I"));
        getter.instructions.add(new InsnNode(Opcodes.IRETURN));
        cn.methods.add(getter);
        if (constructors != 1 || readers != 1 || writers != 1) throw new IllegalStateException(
                "S1D potion packet patch mismatch: constructors=" + constructors
                        + ", readers=" + readers + ", writers=" + writers);
        LOGGER.info("HTGT6 upgraded S1DPacketEntityEffect potion ID to VarInt");
        return write(cn);
    }

    private static void rebuildEntityEffectRead(MethodNode method, String entity, String legacyId,
            String amplifier, String duration, String fullId) {
        method.instructions.clear(); method.tryCatchBlocks.clear();
        InsnList code = method.instructions;
        code.add(new VarInsnNode(Opcodes.ALOAD, 0)); code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer",
                "readInt", "()I", false));
        code.add(new FieldInsnNode(Opcodes.PUTFIELD, ENTITY_EFFECT_PACKET, entity, "I"));
        code.add(new VarInsnNode(Opcodes.ALOAD, 0)); code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer",
                method.name.startsWith("func_") ? "func_150792_a" : "readVarIntFromBuffer", "()I", false));
        code.add(new FieldInsnNode(Opcodes.PUTFIELD, ENTITY_EFFECT_PACKET, fullId, "I"));
        code.add(new VarInsnNode(Opcodes.ALOAD, 0)); code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new FieldInsnNode(Opcodes.GETFIELD, ENTITY_EFFECT_PACKET, fullId, "I"));
        code.add(new InsnNode(Opcodes.I2B));
        code.add(new FieldInsnNode(Opcodes.PUTFIELD, ENTITY_EFFECT_PACKET, legacyId, "B"));
        code.add(new VarInsnNode(Opcodes.ALOAD, 0)); code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer",
                "readByte", "()B", false));
        code.add(new FieldInsnNode(Opcodes.PUTFIELD, ENTITY_EFFECT_PACKET, amplifier, "B"));
        code.add(new VarInsnNode(Opcodes.ALOAD, 0)); code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer",
                "readShort", "()S", false));
        code.add(new FieldInsnNode(Opcodes.PUTFIELD, ENTITY_EFFECT_PACKET, duration, "S"));
        code.add(new InsnNode(Opcodes.RETURN));
    }

    private static void rebuildEntityEffectWrite(MethodNode method, String entity, String amplifier,
            String duration, String fullId) {
        method.instructions.clear(); method.tryCatchBlocks.clear();
        InsnList code = method.instructions;
        code.add(new VarInsnNode(Opcodes.ALOAD, 1)); code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new FieldInsnNode(Opcodes.GETFIELD, ENTITY_EFFECT_PACKET, entity, "I"));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer",
                "writeInt", "(I)Lio/netty/buffer/ByteBuf;", false)); code.add(new InsnNode(Opcodes.POP));
        code.add(new VarInsnNode(Opcodes.ALOAD, 1)); code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new FieldInsnNode(Opcodes.GETFIELD, ENTITY_EFFECT_PACKET, fullId, "I"));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer",
                method.name.startsWith("func_") ? "func_150787_b" : "writeVarIntToBuffer", "(I)V", false));
        code.add(new VarInsnNode(Opcodes.ALOAD, 1)); code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new FieldInsnNode(Opcodes.GETFIELD, ENTITY_EFFECT_PACKET, amplifier, "B"));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer",
                "writeByte", "(I)Lio/netty/buffer/ByteBuf;", false)); code.add(new InsnNode(Opcodes.POP));
        code.add(new VarInsnNode(Opcodes.ALOAD, 1)); code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new FieldInsnNode(Opcodes.GETFIELD, ENTITY_EFFECT_PACKET, duration, "S"));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer",
                "writeShort", "(I)Lio/netty/buffer/ByteBuf;", false)); code.add(new InsnNode(Opcodes.POP));
        code.add(new InsnNode(Opcodes.RETURN));
    }

    private static byte[] transformRemoveEffectPacket120(byte[] bytes) {
        ClassNode cn = read(bytes);
        String entityField = null, potionField = null;
        for (FieldNode field : cn.fields) if ((field.access & Opcodes.ACC_STATIC) == 0 && "I".equals(field.desc)) {
            if (entityField == null) entityField = field.name; else if (potionField == null) potionField = field.name;
        }
        if (entityField == null || potionField == null)
            throw new IllegalStateException("S1EPacketRemoveEntityEffect field layout differs from 1.7.10");
        int readers = 0, writers = 0;
        for (MethodNode method : cn.methods) {
            if (!"(Lnet/minecraft/network/PacketBuffer;)V".equals(method.desc)) continue;
            if (callsMethod(method, "readUnsignedByte")) {
                method.instructions.clear(); method.tryCatchBlocks.clear();
                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                        "net/minecraft/network/PacketBuffer", "readInt", "()I", false));
                method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, REMOVE_EFFECT_PACKET, entityField, "I"));
                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                        "net/minecraft/network/PacketBuffer", method.name.startsWith("func_") ? "func_150792_a" : "readVarIntFromBuffer", "()I", false));
                method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, REMOVE_EFFECT_PACKET, potionField, "I"));
                method.instructions.add(new InsnNode(Opcodes.RETURN)); readers++;
            } else if (callsMethod(method, "writeByte")) {
                method.instructions.clear(); method.tryCatchBlocks.clear();
                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, REMOVE_EFFECT_PACKET, entityField, "I"));
                method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                        "net/minecraft/network/PacketBuffer", "writeInt", "(I)Lio/netty/buffer/ByteBuf;", false));
                method.instructions.add(new InsnNode(Opcodes.POP));
                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, REMOVE_EFFECT_PACKET, potionField, "I"));
                method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                        "net/minecraft/network/PacketBuffer", method.name.startsWith("func_") ? "func_150787_b" : "writeVarIntToBuffer", "(I)V", false));
                method.instructions.add(new InsnNode(Opcodes.RETURN)); writers++;
            }
        }
        if (readers != 1 || writers != 1) throw new IllegalStateException(
                "S1E potion packet patch mismatch: readers=" + readers + ", writers=" + writers);
        LOGGER.info("HTGT6 upgraded S1EPacketRemoveEntityEffect potion ID to VarInt");
        return write(cn);
    }

    private static byte[] transformNetHandlerPotionIds120(byte[] bytes) {
        ClassNode cn = read(bytes); int replacements = 0;
        for (MethodNode method : cn.methods) {
            boolean replacedInMethod = false;
            for (AbstractInsnNode node : method.instructions.toArray()) {
                if (!(node instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) node;
                // The first S1D byte getter supplies PotionEffect's ID; the second is its amplifier.
                if (!replacedInMethod && ENTITY_EFFECT_PACKET.equals(call.owner) && "()B".equals(call.desc)) {
                    call.name = "htgt6$getPotionId120"; call.desc = "()I"; replacements++;
                    replacedInMethod = true;
                }
            }
        }
        if (replacements != 1) throw new IllegalStateException(
                "NetHandlerPlayClient entity-effect getter patch count was " + replacements + ", expected 1");
        LOGGER.info("HTGT6 connected the client potion handler to full-width IDs");
        return write(cn);
    }

    private static boolean callsMethod(MethodNode method, String name) {
        for (AbstractInsnNode node : method.instructions.toArray())
            if (node instanceof MethodInsnNode && name.equals(((MethodInsnNode) node).name)) return true;
        return false;
    }

    private static boolean hasStringBefore(AbstractInsnNode node, String value, int distance) {
        AbstractInsnNode cursor = node;
        for (int i = 0; i < distance && (cursor = previousCode(cursor)) != null; i++)
            if (cursor instanceof LdcInsnNode && value.equals(((LdcInsnNode) cursor).cst)) return true;
        return false;
    }

    private static AbstractInsnNode previousCode(AbstractInsnNode node) {
        AbstractInsnNode previous = node == null ? null : node.getPrevious();
        while (previous != null && previous.getOpcode() < 0) previous = previous.getPrevious();
        return previous;
    }

    private static int integerConstant(AbstractInsnNode node) {
        if (node instanceof IntInsnNode) return ((IntInsnNode) node).operand;
        if (node instanceof LdcInsnNode && ((LdcInsnNode) node).cst instanceof Integer)
            return ((Integer) ((LdcInsnNode) node).cst).intValue();
        int opcode = node == null ? -1 : node.getOpcode();
        return opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.ICONST_5
                ? opcode - Opcodes.ICONST_0 : Integer.MIN_VALUE;
    }

    /**
     * NovaCraft 1.4.3 unconditionally builds a few compatibility recipes even
     * when the referenced compatibility item is disabled. Forge 1.7.10 then
     * dereferences that null ingredient and aborts the whole INIT phase. Route
     * only NovaCraft's private shaped-recipe helper through a null-aware guard.
     */
    private static byte[] transformNovaCraftRecipeRegistration(byte[] bytes) {
        ClassNode cn = read(bytes);
        String descriptor = "(Ljava/lang/String;Lnet/minecraft/item/ItemStack;[Ljava/lang/Object;)V";
        MethodNode register = null;
        for (MethodNode method : cn.methods) {
            if ("register".equals(method.name) && descriptor.equals(method.desc)) {
                register = method;
                break;
            }
        }
        if (register == null) throw new IllegalStateException(
                "NovaCraftRegistries.register(String, ItemStack, Object[]) missing");

        register.instructions.clear();
        register.tryCatchBlocks.clear();
        register.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        register.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        register.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
        register.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                "addNovaCraftRecipeSafely", descriptor, false));
        register.instructions.add(new InsnNode(Opcodes.RETURN));
        LOGGER.info("HTGT6 guarded NovaCraft 1.4.3 shaped recipes against disabled compatibility items");
        return write(cn);
    }

    public static void addNovaCraftRecipeSafely(String name, net.minecraft.item.ItemStack output,
            Object[] recipe) {
        if (output == null || output.getItem() == null) {
            LOGGER.warn("Skipping invalid NovaCraft shaped recipe {}: output is null", name);
            return;
        }
        if (recipe == null) {
            LOGGER.warn("Skipping invalid NovaCraft shaped recipe {}: recipe array is null", name);
            return;
        }
        for (int index = 0; index < recipe.length; index++) {
            Object value = recipe[index];
            boolean invalid = value == null
                    || (value instanceof net.minecraft.item.ItemStack
                    && ((net.minecraft.item.ItemStack) value).getItem() == null);
            if (invalid) {
                LOGGER.warn("Skipping invalid NovaCraft shaped recipe {}: ingredient {} is null",
                        name, index);
                return;
            }
        }
        cpw.mods.fml.common.registry.GameRegistry.addRecipe(output, recipe);
    }

    /** Installs the HTGT6 splash after Display.create and owns vanilla loadScreen calls. */
    private static byte[] transformLoadingScreenMinecraft(byte[] bytes) {
        ClassNode cn = read(bytes);
        int branding = 0;
        int starts = 0;
        int screens = 0;
        for (MethodNode method : cn.methods) {
            boolean startGame = "()V".equals(method.desc)
                    && ("startGame".equals(method.name) || "func_71384_a".equals(method.name)
                    || "ag".equals(method.name));
            if (startGame) {
                for (AbstractInsnNode node : method.instructions.toArray()) {
                    if (!(node instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) node;
                    if (FML_CLIENT_HANDLER.equals(call.owner)
                            && "beginMinecraftLoading".equals(call.name)) {
                        InsnList earlyClient = new InsnList();
                        if (HTGT6Config.ENABLE_WINDOW_BRANDING) {
                            earlyClient.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    "com/mod/htgt6/client/loading/HTGT6WindowBranding",
                                    "apply", "()V", false));
                            branding++;
                        }
                        if (HTGT6Config.ENABLE_NEW_FML_LOADING_SCREEN) {
                            earlyClient.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                    "com/mod/htgt6/client/loading/HTGT6LoadingScreen",
                                    "start", "()V", false));
                            starts++;
                        }
                        method.instructions.insertBefore(call, earlyClient);
                        break;
                    }
                }
            }

            boolean loadScreen = "()V".equals(method.desc)
                    && ("loadScreen".equals(method.name) || "func_71357_I".equals(method.name)
                    || "aj".equals(method.name));
            if (loadScreen && HTGT6Config.ENABLE_NEW_FML_LOADING_SCREEN) {
                method.instructions.clear();
                method.tryCatchBlocks.clear();
                method.localVariables = null;
                method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/mod/htgt6/client/loading/HTGT6LoadingScreen",
                        "renderCurrent", "()V", false));
                method.instructions.add(new InsnNode(Opcodes.RETURN));
                screens++;
            }
        }
        int expectedBranding = HTGT6Config.ENABLE_WINDOW_BRANDING ? 1 : 0;
        int expectedLoading = HTGT6Config.ENABLE_NEW_FML_LOADING_SCREEN ? 1 : 0;
        if (branding != expectedBranding || starts != expectedLoading
                || screens != expectedLoading) {
            throw new IllegalStateException("HTGT6 early client hooks were not unique: branding="
                    + branding + ", start=" + starts + ", loadScreen=" + screens);
        }
        LOGGER.info("HTGT6 installed its early window-branding and loading-screen hooks");
        return write(cn);
    }

    /** Closes the custom splash before FML selects the main menu or an error GUI. */
    private static byte[] transformLoadingFmlClientHandler(byte[] bytes) {
        ClassNode cn = read(bytes);
        int patched = 0;
        for (MethodNode method : cn.methods) {
            if (!"onInitializationComplete".equals(method.name) || !"()V".equals(method.desc)) continue;
            method.instructions.insert(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "com/mod/htgt6/client/loading/HTGT6LoadingScreen",
                    "finish", "()V", false));
            patched++;
        }
        if (patched != 1) throw new IllegalStateException(
                "FMLClientHandler loading-screen close hook count was " + patched);
        LOGGER.info("HTGT6 installed the loading-screen completion hook");
        return write(cn);
    }

    /** Forces Forge's SharedDrawable splash thread off without rewriting user config files. */
    private static byte[] transformSplashProgressForHTGT6(byte[] bytes) {
        ClassNode cn = read(bytes);
        int patched = 0;
        for (MethodNode method : cn.methods) {
            if (!"start".equals(method.name) || !"()V".equals(method.desc)) continue;
            for (AbstractInsnNode node : method.instructions.toArray()) {
                if (!(node instanceof FieldInsnNode)) continue;
                FieldInsnNode field = (FieldInsnNode) node;
                if (field.getOpcode() == Opcodes.PUTSTATIC && SPLASH_PROGRESS.equals(field.owner)
                        && "enabled".equals(field.name) && "Z".equals(field.desc)) {
                    InsnList disable = new InsnList();
                    disable.add(new InsnNode(Opcodes.ICONST_0));
                    disable.add(new FieldInsnNode(Opcodes.PUTSTATIC,
                            SPLASH_PROGRESS, "enabled", "Z"));
                    method.instructions.insert(field, disable);
                    patched++;
                    break;
                }
            }
        }
        if (patched != 1) throw new IllegalStateException(
                "SplashProgress disable hook count was " + patched);
        LOGGER.info("HTGT6 disabled the Forge splash thread in favor of its own renderer");
        return write(cn);
    }

    /**
     * FML 7 sorts File.listFiles() without checking for null. A classpath or
     * nested mod directory can disappear/become unreadable while dependencies
     * are being prepared, which otherwise aborts the entire discovery phase.
     */
    private static byte[] transformFmlDirectoryDiscoverer(byte[] bytes) {
        ClassNode cn = read(bytes);
        int patched = 0;
        for (MethodNode method : cn.methods) {
            for (AbstractInsnNode node = method.instructions.getFirst(); node != null;) {
                AbstractInsnNode next = node.getNext();
                if (node instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) node;
                    if (call.getOpcode() == Opcodes.INVOKEVIRTUAL
                            && "java/io/File".equals(call.owner)
                            && "listFiles".equals(call.name)
                            && "(Ljava/io/FileFilter;)[Ljava/io/File;".equals(call.desc)) {
                        method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                SELF, "safeListFiles",
                                "(Ljava/io/File;Ljava/io/FileFilter;)[Ljava/io/File;", false));
                        patched++;
                    }
                }
                node = next;
            }
        }
        if (patched == 0) return bytes;
        LOGGER.info("HTGT6 guarded FML directory discovery against unreadable classpath entries");
        return write(cn);
    }

    public static java.io.File[] safeListFiles(java.io.File directory,
            java.io.FileFilter filter) {
        java.io.File[] content = directory.listFiles(filter);
        if (content != null) return content;
        LOGGER.warn("FML skipped unreadable or missing mod directory: {}",
                directory.getAbsolutePath());
        return new java.io.File[0];
    }

    /**
     * EnderCore 0.4.8 is distributed with SRG member names. ForgeGradle 1.2
     * does not remap jars copied directly into run/mods, so its injected helper
     * fails only in the MCP development runtime. Production remains untouched.
     */
    private static byte[] transformEnderCoreMethodsForDev(byte[] bytes) {
        if (net.minecraft.launchwrapper.Launch.blackboard == null
                || !Boolean.TRUE.equals(net.minecraft.launchwrapper.Launch.blackboard
                .get("fml.deobfuscatedEnvironment"))) return bytes;
        if (System.getProperty("htgt6.devMcpMappings") != null) return bytes;
        ClassNode cn = read(bytes);
        int patched = 0;
        for (MethodNode method : cn.methods) {
            for (AbstractInsnNode node : method.instructions.toArray()) {
                if (node instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) node;
                    String mapped = enderCoreMcpMethod(call.name);
                    if (mapped != null) {
                        call.name = mapped;
                        patched++;
                    }
                } else if (node instanceof FieldInsnNode) {
                    FieldInsnNode field = (FieldInsnNode) node;
                    String mapped = enderCoreMcpField(field.name);
                    if (mapped != null) {
                        field.name = mapped;
                        patched++;
                    }
                }
            }
        }
        if (patched == 0) {
            // The generic runClient compatibility transformer may already have
            // converted the complete class before this legacy targeted patch.
            return bytes;
        }
        LOGGER.info("HTGT6 remapped {} EnderCore SRG member reference(s) for the MCP dev runtime", patched);
        return write(cn);
    }

    private static String enderCoreMcpMethod(String name) {
        if ("func_77973_b".equals(name)) return "getItem";
        if ("func_77613_e".equals(name)) return "getRarity";
        if ("func_75216_d".equals(name)) return "getHasStack";
        if ("func_75211_c".equals(name)) return "getStack";
        if ("func_77946_l".equals(name)) return "copy";
        if ("func_75220_a".equals(name)) return "onSlotChange";
        if ("func_145954_b".equals(name)) return "isItemFuel";
        if ("func_77602_a".equals(name)) return "smelting";
        if ("func_151395_a".equals(name)) return "getSmeltingResult";
        if ("func_75215_d".equals(name)) return "putStack";
        if ("func_75218_e".equals(name)) return "onSlotChanged";
        if ("func_82870_a".equals(name)) return "onPickupFromSlot";
        if ("func_77985_e".equals(name)) return "isStackable";
        if ("func_77981_g".equals(name)) return "getHasSubtypes";
        if ("func_77960_j".equals(name)) return "getItemDamage";
        if ("func_77970_a".equals(name)) return "areItemStackTagsEqual";
        if ("func_77976_d".equals(name)) return "getMaxStackSize";
        return null;
    }

    private static String enderCoreMcpField(String name) {
        if ("field_77138_c".equals(name)) return "FLAT";
        if ("field_77137_b".equals(name)) return "DEFAULT";
        if ("field_75151_b".equals(name)) return "inventorySlots";
        if ("field_77994_a".equals(name)) return "stackSize";
        return null;
    }

    /** Remaps the one production-SRG framebuffer reference used by OpenMods in MCP runs. */
    private static byte[] transformOpenModsFramebufferHooksForDev(byte[] bytes) {
        if (net.minecraft.launchwrapper.Launch.blackboard == null
                || !Boolean.TRUE.equals(net.minecraft.launchwrapper.Launch.blackboard
                .get("fml.deobfuscatedEnvironment"))) return bytes;
        if (System.getProperty("htgt6.devMcpMappings") != null) return bytes;
        ClassNode cn = read(bytes);
        int remapped = 0;
        for (MethodNode method : cn.methods) {
            for (AbstractInsnNode node : method.instructions.toArray()) {
                if (!(node instanceof FieldInsnNode)) continue;
                FieldInsnNode field = (FieldInsnNode) node;
                if ("net/minecraft/client/shader/Framebuffer".equals(field.owner)
                        && "field_147624_h".equals(field.name)
                        && "I".equals(field.desc)) {
                    field.name = "depthBuffer";
                    remapped++;
                }
            }
        }
        if (remapped == 0) return bytes;
        LOGGER.info("HTGT6 remapped {} OpenMods framebuffer SRG reference(s) for the MCP dev runtime",
                remapped);
        return write(cn);
    }

    /** Keeps production coremods able to report their real failure in MCP dev runs. */
    private static byte[] transformCrashReportForDev(byte[] bytes) {
        if (net.minecraft.launchwrapper.Launch.blackboard == null
                || !Boolean.TRUE.equals(net.minecraft.launchwrapper.Launch.blackboard
                .get("fml.deobfuscatedEnvironment"))) return bytes;
        if (System.getProperty("htgt6.devMcpMappings") != null) return bytes;
        final String desc = "(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/crash/CrashReport;";
        final String categoryDesc = "(Ljava/lang/String;)Lnet/minecraft/crash/CrashReportCategory;";
        ClassNode cn = read(bytes);
        boolean makeBridge = true;
        boolean causeBridge = true;
        boolean categoryBridge = true;
        for (MethodNode method : cn.methods) {
            if ("func_85055_a".equals(method.name) && desc.equals(method.desc)) makeBridge = false;
            if ("func_71505_b".equals(method.name) && "()Ljava/lang/Throwable;".equals(method.desc))
                causeBridge = false;
            if ("func_85058_a".equals(method.name) && categoryDesc.equals(method.desc))
                categoryBridge = false;
        }
        if (makeBridge) {
            MethodNode bridge = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "func_85055_a", desc, null, null);
            bridge.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            bridge.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            bridge.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, cn.name,
                    "makeCrashReport", desc, false));
            bridge.instructions.add(new InsnNode(Opcodes.ARETURN));
            bridge.maxStack = 2;
            bridge.maxLocals = 2;
            cn.methods.add(bridge);
        }
        if (causeBridge) {
            MethodNode bridge = new MethodNode(Opcodes.ACC_PUBLIC, "func_71505_b",
                    "()Ljava/lang/Throwable;", null, null);
            bridge.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            bridge.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, cn.name,
                    "getCrashCause", "()Ljava/lang/Throwable;", false));
            bridge.instructions.add(new InsnNode(Opcodes.ARETURN));
            bridge.maxStack = 1;
            bridge.maxLocals = 1;
            cn.methods.add(bridge);
        }
        if (categoryBridge) {
            MethodNode bridge = new MethodNode(Opcodes.ACC_PUBLIC, "func_85058_a",
                    categoryDesc, null, null);
            bridge.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            bridge.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            bridge.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, cn.name,
                    "makeCategory", categoryDesc, false));
            bridge.instructions.add(new InsnNode(Opcodes.ARETURN));
            bridge.maxStack = 2;
            bridge.maxLocals = 2;
            cn.methods.add(bridge);
        }
        if (!makeBridge && !causeBridge && !categoryBridge) return bytes;
        LOGGER.info("HTGT6 added CrashReport SRG bridge(s) for the MCP dev runtime");
        return write(cn);
    }

    private static byte[] transformCrashReportCategoryForDev(byte[] bytes) {
        if (net.minecraft.launchwrapper.Launch.blackboard == null
                || !Boolean.TRUE.equals(net.minecraft.launchwrapper.Launch.blackboard
                .get("fml.deobfuscatedEnvironment"))) return bytes;
        if (System.getProperty("htgt6.devMcpMappings") != null) return bytes;
        final String callableDesc = "(Ljava/lang/String;Ljava/util/concurrent/Callable;)V";
        final String sectionDesc = "(Ljava/lang/String;Ljava/lang/Object;)V";
        ClassNode cn = read(bytes);
        boolean callableBridge = true;
        boolean sectionBridge = true;
        for (MethodNode method : cn.methods) {
            if ("func_71500_a".equals(method.name) && callableDesc.equals(method.desc))
                callableBridge = false;
            if ("func_71507_a".equals(method.name) && sectionDesc.equals(method.desc))
                sectionBridge = false;
        }
        if (callableBridge) {
            MethodNode bridge = new MethodNode(Opcodes.ACC_PUBLIC, "func_71500_a",
                    callableDesc, null, null);
            bridge.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            bridge.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            bridge.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
            bridge.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, cn.name,
                    "addCrashSectionCallable", callableDesc, false));
            bridge.instructions.add(new InsnNode(Opcodes.RETURN));
            bridge.maxStack = 3;
            bridge.maxLocals = 3;
            cn.methods.add(bridge);
        }
        if (sectionBridge) {
            MethodNode bridge = new MethodNode(Opcodes.ACC_PUBLIC, "func_71507_a",
                    sectionDesc, null, null);
            bridge.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            bridge.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            bridge.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
            bridge.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, cn.name,
                    "addCrashSection", sectionDesc, false));
            bridge.instructions.add(new InsnNode(Opcodes.RETURN));
            bridge.maxStack = 3;
            bridge.maxLocals = 3;
            cn.methods.add(bridge);
        }
        if (!callableBridge && !sectionBridge) return bytes;
        LOGGER.info("HTGT6 added the CrashReportCategory SRG bridge for the MCP dev runtime");
        return write(cn);
    }

    /** Adds the 1.20.2 HumanoidModel THROW_SPEAR arm pose after vanilla animation. */
    private static byte[] transformModelBipedTridentPose(byte[] bytes) {
        ClassNode cn = read(bytes);
        int patchedReturns = 0;
        for (MethodNode mn : cn.methods) {
            if (!("setRotationAngles".equals(mn.name) || "func_78087_a".equals(mn.name))) continue;
            if (!"(FFFFFFLnet/minecraft/entity/Entity;)V".equals(mn.desc)) continue;
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                if (node.getOpcode() != Opcodes.RETURN) continue;
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                hook.add(new VarInsnNode(Opcodes.ALOAD, 7));
                hook.add(new VarInsnNode(Opcodes.FLOAD, 1));
                hook.add(new VarInsnNode(Opcodes.FLOAD, 3));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/mod/htgt6/minecraft1202/Module/Client/Aquatic/SwimmingPlayerRenderer120", "model",
                        "(Lnet/minecraft/client/model/ModelBiped;Lnet/minecraft/entity/Entity;FF)V", false));
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                hook.add(new VarInsnNode(Opcodes.ALOAD, 7));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/mod/htgt6/minecraft1202/Module/Client/Shared/TridentPlayerPoseHooks", "apply",
                        "(Lnet/minecraft/client/model/ModelBiped;Lnet/minecraft/entity/Entity;)V", false));
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                hook.add(new VarInsnNode(Opcodes.ALOAD, 7));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/mod/htgt6/minecraft1202/Module/Client/Shared/CrossbowPlayerPoseHooks142", "apply",
                        "(Lnet/minecraft/client/model/ModelBiped;Lnet/minecraft/entity/Entity;)V", false));
                mn.instructions.insertBefore(node, hook);
                patchedReturns++;
            }
        }
        if (patchedReturns == 0) {
            throw new IllegalStateException("ModelBiped trident THROW_SPEAR hook was not applied");
        }
        LOGGER.info("HTGT6 added Minecraft 1.20.2 trident and crossbow player arm poses");
        return write(cn);
    }

    /** Adds the Minecraft 1.12 fall-flying body rotation inside RenderPlayer's active entity matrix. */
    private static byte[] transformRenderPlayerElytra(byte[] bytes) {
        ClassNode cn = read(bytes);
        int patchedReturns = 0;
        for (MethodNode mn : cn.methods) {
            if (!("rotateCorpse".equals(mn.name) || "func_77043_a".equals(mn.name))) continue;
            if (!("(Lnet/minecraft/client/entity/AbstractClientPlayer;FFF)V".equals(mn.desc))) continue;
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                if (node.getOpcode() != Opcodes.RETURN) continue;
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                hook.add(new VarInsnNode(Opcodes.FLOAD, 4));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/mod/htgt6/minecraft1202/Module/Client/Combat/ElytraPlayerRenderer", "applyFlightRotation",
                        "(Lnet/minecraft/entity/player/EntityPlayer;F)V", false));
                hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                hook.add(new VarInsnNode(Opcodes.FLOAD, 4));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/mod/htgt6/minecraft1202/Module/Client/Aquatic/SwimmingPlayerRenderer120", "rotate",
                        "(Lnet/minecraft/entity/player/EntityPlayer;F)V", false));
                mn.instructions.insertBefore(node, hook);
                patchedReturns++;
            }
        }
        if (patchedReturns == 0)
            throw new IllegalStateException("RenderPlayer Elytra rotation hook was not applied");
        LOGGER.info("HTGT6 added the Minecraft 1.12 fall-flying player rotation");
        return write(cn);
    }

    /** A directory query must not acquire or overwrite a world's session token. */
    private static byte[] transformSaveDirectoryLookup(byte[] bytes) {
        ClassNode cn = read(bytes);
        for (MethodNode method : cn.methods) {
            if (!"getCurrentSaveRootDirectory".equals(method.name) || !"()Ljava/io/File;".equals(method.desc)) continue;
            method.instructions.clear();
            method.tryCatchBlocks.clear();
            if (method.localVariables != null) method.localVariables.clear();
            method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "com/mod/htgt6/asm/WorldSaveDirectoryHooks", "current", "()Ljava/io/File;", false));
            method.instructions.add(new InsnNode(Opcodes.ARETURN));
            LOGGER.info("HTGT6 removed session.lock writes from Forge save-directory lookup");
            return write(cn);
        }
        throw new IllegalStateException("Forge save-directory lookup method not found");
    }

    /** Guard the Forge backup prompt when dynamic dimensions have no level.dat yet. */
    private static byte[] transformSaveHandler(byte[] bytes) {
        ClassNode cn = read(bytes);
        int patched = 0;
        for (MethodNode mn : cn.methods) {
            if (!"()Lnet/minecraft/world/storage/WorldInfo;".equals(mn.desc)) continue;
            if (!("loadWorldInfo".equals(mn.name) || "func_75757_d".equals(mn.name))) continue;
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                if (!(node instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) node;
                if (("confirmBackupLevelDatUse".equals(call.name) || "func_152372_a".equals(call.name))
                        && "(Lnet/minecraft/world/storage/SaveHandler;)V".equals(call.desc)) {
                    call.setOpcode(Opcodes.INVOKESTATIC);
                    call.owner = SELF;
                    call.name = "confirmBackupIfPrimaryExists";
                    call.desc = "(Lcpw/mods/fml/common/FMLCommonHandler;Lnet/minecraft/world/storage/SaveHandler;)V";
                    call.itf = false;
                    patched++;
                }
            }
        }
        if (patched != 1) throw new IllegalStateException("SaveHandler backup guard was not applied: " + patched);
        LOGGER.info("HTGT6 guarded Forge backup confirmation when level.dat is absent");
        return write(cn);
    }

    public static void confirmBackupIfPrimaryExists(cpw.mods.fml.common.FMLCommonHandler common,
            net.minecraft.world.storage.SaveHandler handler) {
        if (common == null || handler == null) return;
        java.io.File directory = handler.getWorldDirectory();
        if (directory != null && new java.io.File(directory, "level.dat").isFile()) {
            common.confirmBackupLevelDatUse(handler);
        } else {
            LOGGER.info("Skipping Forge backup prompt because level.dat is absent in {}", directory);
        }
    }

    /** Ports the compartment model used by Minecraft 1.12 to the actual player inventory. */
    private static byte[] transformInventoryPlayer(byte[] bytes) {
        ClassNode cn = read(bytes);
        final String hooks = "com/mod/htgt6/minecraft1202/Module/Events/Combat/OffhandHooks";
        final String bridge = "com/mod/htgt6/minecraft1202/Module/Gameplay/Combat/InventoryPlayer112";
        final String compatibilityBridge = "xonin/backhand/api/core/IOffhandInventory";
        final String stack = "Lnet/minecraft/item/ItemStack;";
        final String list = "Lnet/minecraft/nbt/NBTTagList;";

        boolean hasOffhandField = false;
        boolean hasAllInventoriesField = false;
        for (FieldNode field : cn.fields) {
            if ("offHandInventory".equals(field.name)) hasOffhandField = true;
            if ("allInventories".equals(field.name)) hasAllInventoriesField = true;
        }
        if (!hasOffhandField)
            cn.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "offHandInventory", "[" + stack, null, null));
        if (!hasAllInventoriesField)
            cn.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "allInventories", "Ljava/util/List;",
                    "Ljava/util/List<[Lnet/minecraft/item/ItemStack;>;", null));
        if (!cn.interfaces.contains(bridge)) cn.interfaces.add(bridge);
        if (!cn.interfaces.contains(compatibilityBridge)) cn.interfaces.add(compatibilityBridge);
        addInventoryBridgeMethods(cn);
        addBackhandCompatibilityMethods(cn, hooks);

        int patched = 0;
        for (MethodNode mn : cn.methods) {
            if ("<init>".equals(mn.name)) {
                for (AbstractInsnNode node : mn.instructions.toArray()) if (node.getOpcode() == Opcodes.RETURN) {
                    InsnList init = new InsnList();
                    init.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    init.add(new MethodInsnNode(Opcodes.INVOKESTATIC, hooks, "initializeCompartments",
                            "(Lnet/minecraft/entity/player/InventoryPlayer;)V", false));
                    mn.instructions.insertBefore(node, init);
                    patched++;
                }
            } else if ("()I".equals(mn.desc) && ("getSizeInventory".equals(mn.name) || "func_70302_i_".equals(mn.name))) {
                replaceInventoryMethod(mn, hooks, "getSizeInventory", "(Lnet/minecraft/entity/player/InventoryPlayer;)I", Opcodes.IRETURN, 0);
                patched++;
            } else if (("()" + stack).equals(mn.desc)
                    && ("getCurrentItem".equals(mn.name) || "func_70448_g".equals(mn.name))) {
                replaceInventoryMethod(mn, hooks, "getCurrentItem",
                        "(Lnet/minecraft/entity/player/InventoryPlayer;)" + stack, Opcodes.ARETURN, 0);
                patched++;
            } else if (("(I)" + stack).equals(mn.desc) && ("getStackInSlot".equals(mn.name) || "func_70301_a".equals(mn.name))) {
                replaceInventoryMethod(mn, hooks, "getStackInSlot", "(Lnet/minecraft/entity/player/InventoryPlayer;I)" + stack, Opcodes.ARETURN, 1);
                patched++;
            } else if (("(II)" + stack).equals(mn.desc) && ("decrStackSize".equals(mn.name) || "func_70298_a".equals(mn.name))) {
                replaceInventoryMethod(mn, hooks, "decrStackSize", "(Lnet/minecraft/entity/player/InventoryPlayer;II)" + stack, Opcodes.ARETURN, 2);
                patched++;
            } else if (("(I)" + stack).equals(mn.desc) && ("getStackInSlotOnClosing".equals(mn.name) || "func_70304_b".equals(mn.name))) {
                replaceInventoryMethod(mn, hooks, "getStackInSlotOnClosing", "(Lnet/minecraft/entity/player/InventoryPlayer;I)" + stack, Opcodes.ARETURN, 1);
                patched++;
            } else if (("(I" + stack + ")V").equals(mn.desc) && ("setInventorySlotContents".equals(mn.name) || "func_70299_a".equals(mn.name))) {
                replaceInventoryMethod(mn, hooks, "setInventorySlotContents", "(Lnet/minecraft/entity/player/InventoryPlayer;I" + stack + ")V", Opcodes.RETURN, 2);
                patched++;
            } else if (("(" + list + ")" + list).equals(mn.desc) && ("writeToNBT".equals(mn.name) || "func_70442_a".equals(mn.name))) {
                replaceInventoryMethod(mn, hooks, "writeToNBT", "(Lnet/minecraft/entity/player/InventoryPlayer;" + list + ")" + list, Opcodes.ARETURN, 1);
                patched++;
            } else if (("(" + list + ")V").equals(mn.desc) && ("readFromNBT".equals(mn.name) || "func_70443_b".equals(mn.name))) {
                replaceInventoryMethod(mn, hooks, "readFromNBT", "(Lnet/minecraft/entity/player/InventoryPlayer;" + list + ")V", Opcodes.RETURN, 1);
                patched++;
            } else if ("()V".equals(mn.desc) && ("dropAllItems".equals(mn.name) || "func_70436_m".equals(mn.name))) {
                replaceInventoryMethod(mn, hooks, "dropAllItems", "(Lnet/minecraft/entity/player/InventoryPlayer;)V", Opcodes.RETURN, 0);
                patched++;
            } else if ("(Lnet/minecraft/entity/player/InventoryPlayer;)V".equals(mn.desc)
                    && ("copyInventory".equals(mn.name) || "func_70455_b".equals(mn.name))) {
                replaceInventoryMethod(mn, hooks, "copyInventory", "(Lnet/minecraft/entity/player/InventoryPlayer;Lnet/minecraft/entity/player/InventoryPlayer;)V", Opcodes.RETURN, 1);
                patched++;
            } else if ("(Lnet/minecraft/item/Item;I)I".equals(mn.desc)
                    && ("clearInventory".equals(mn.name) || "func_146027_a".equals(mn.name))) {
                replaceInventoryMethod(mn, hooks, "clearInventory", "(Lnet/minecraft/entity/player/InventoryPlayer;Lnet/minecraft/item/Item;I)I", Opcodes.IRETURN, 2);
                patched++;
            } else if ("(Lnet/minecraft/item/Item;)Z".equals(mn.desc)
                    && ("consumeInventoryItem".equals(mn.name) || "func_146026_a".equals(mn.name))) {
                replaceInventoryMethod(mn, hooks, "consumeInventoryItem", "(Lnet/minecraft/entity/player/InventoryPlayer;Lnet/minecraft/item/Item;)Z", Opcodes.IRETURN, 1);
                patched++;
            } else if ("(Lnet/minecraft/item/Item;)Z".equals(mn.desc)
                    && ("hasItem".equals(mn.name) || "func_146028_b".equals(mn.name))) {
                replaceInventoryMethod(mn, hooks, "hasItem", "(Lnet/minecraft/entity/player/InventoryPlayer;Lnet/minecraft/item/Item;)Z", Opcodes.IRETURN, 1);
                patched++;
            } else if ("(Lnet/minecraft/item/ItemStack;)Z".equals(mn.desc)
                    && ("hasItemStack".equals(mn.name) || "func_70431_c".equals(mn.name))) {
                replaceInventoryMethod(mn, hooks, "hasItemStack", "(Lnet/minecraft/entity/player/InventoryPlayer;Lnet/minecraft/item/ItemStack;)Z", Opcodes.IRETURN, 1);
                patched++;
            } else if ("()V".equals(mn.desc)
                    && ("decrementAnimations".equals(mn.name) || "func_70429_k".equals(mn.name))) {
                for (AbstractInsnNode node : mn.instructions.toArray()) if (node.getOpcode() == Opcodes.RETURN) {
                    InsnList tick = new InsnList();
                    tick.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    tick.add(new MethodInsnNode(Opcodes.INVOKESTATIC, hooks, "decrementOffhandAnimation",
                            "(Lnet/minecraft/entity/player/InventoryPlayer;)V", false));
                    mn.instructions.insertBefore(node, tick);
                }
                patched++;
            }
        }
        if (patched < 10) throw new IllegalStateException("Native off-hand inventory patch incomplete: " + patched);
        LOGGER.info("HTGT6 upgraded InventoryPlayer to the Minecraft 1.12 compartment model");
        return write(cn);
    }

    private static void addInventoryBridgeMethods(ClassNode cn) {
        addFieldBridge(cn, "htgt6$getOffHandInventory", "htgt6$setOffHandInventory",
                "offHandInventory", "[Lnet/minecraft/item/ItemStack;");
        addFieldBridge(cn, "htgt6$getAllInventories", "htgt6$setAllInventories",
                "allInventories", "Ljava/util/List;");
    }

    /** Thin binary ABI for the installed Battlegear build; storage stays owned by HTGT6. */
    private static void addBackhandCompatibilityMethods(ClassNode cn, String hooks) {
        final String stack = "Lnet/minecraft/item/ItemStack;";
        addOffhandCompatibilityGetter(cn, hooks, stack);
        addOffhandCompatibilitySetter(cn, hooks, stack);
        boolean hasSlot = false;
        for (MethodNode method : cn.methods)
            if ("backhand$getOffhandSlot".equals(method.name) && "()I".equals(method.desc)) hasSlot = true;
        if (!hasSlot) {
            MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "backhand$getOffhandSlot", "()I", null, null);
            method.instructions.add(new IntInsnNode(Opcodes.BIPUSH, 40));
            method.instructions.add(new InsnNode(Opcodes.IRETURN));
            cn.methods.add(method);
        }
    }

    private static void addOffhandCompatibilityGetter(ClassNode cn, String hooks, String stack) {
        for (MethodNode method : cn.methods)
            if ("backhand$getOffhandItem".equals(method.name) && ("()" + stack).equals(method.desc)) return;
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "backhand$getOffhandItem", "()" + stack, null, null);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new IntInsnNode(Opcodes.BIPUSH, 40));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, hooks, "getStackInSlot",
                "(Lnet/minecraft/entity/player/InventoryPlayer;I)" + stack, false));
        method.instructions.add(new InsnNode(Opcodes.ARETURN));
        cn.methods.add(method);
    }

    private static void addOffhandCompatibilitySetter(ClassNode cn, String hooks, String stack) {
        for (MethodNode method : cn.methods)
            if ("backhand$setOffhandItem".equals(method.name) && ("(" + stack + ")V").equals(method.desc)) return;
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "backhand$setOffhandItem", "(" + stack + ")V", null, null);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new IntInsnNode(Opcodes.BIPUSH, 40));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, hooks, "setInventorySlotContents",
                "(Lnet/minecraft/entity/player/InventoryPlayer;I" + stack + ")V", false));
        method.instructions.add(new InsnNode(Opcodes.RETURN));
        cn.methods.add(method);
    }

    private static void addFieldBridge(ClassNode cn, String getter, String setter,
            String field, String descriptor) {
        boolean hasGetter = false;
        boolean hasSetter = false;
        for (MethodNode method : cn.methods) {
            if (getter.equals(method.name) && ("()" + descriptor).equals(method.desc)) hasGetter = true;
            if (setter.equals(method.name) && ("(" + descriptor + ")V").equals(method.desc)) hasSetter = true;
        }
        if (!hasGetter) {
            MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, getter, "()" + descriptor, null, null);
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, cn.name, field, descriptor));
            method.instructions.add(new InsnNode(Opcodes.ARETURN));
            cn.methods.add(method);
        }
        if (!hasSetter) {
            MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, setter, "(" + descriptor + ")V", null, null);
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, cn.name, field, descriptor));
            method.instructions.add(new InsnNode(Opcodes.RETURN));
            cn.methods.add(method);
        }
    }

    private static void replaceInventoryMethod(MethodNode mn, String owner, String name,
            String desc, int returnOpcode, int argumentCount) {
        mn.instructions.clear();
        mn.tryCatchBlocks.clear();
        mn.localVariables = null;
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        Type[] arguments = Type.getArgumentTypes(mn.desc);
        int slot = 1;
        for (int i = 0; i < argumentCount; ++i) {
            Type type = arguments[i];
            mn.instructions.add(new VarInsnNode(type.getOpcode(Opcodes.ILOAD), slot));
            slot += type.getSize();
        }
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, desc, false));
        mn.instructions.add(new InsnNode(returnOpcode));
    }

    private static byte[] transformContainerPlayer(byte[] bytes) {
        ClassNode cn = read(bytes);
        int injections = 0;
        int armorFixes = 0;
        int transferFixes = 0;
        for (MethodNode mn : cn.methods) {
            if (("transferStackInSlot".equals(mn.name) || "func_82846_b".equals(mn.name))
                    && "(Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;".equals(mn.desc)) {
                replaceInventoryMethod(mn, "com/mod/htgt6/minecraft1202/Module/Events/Combat/OffhandHooks",
                        "transferStackInSlot112",
                        "(Lnet/minecraft/inventory/ContainerPlayer;Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;",
                        Opcodes.ARETURN, 2);
                transferFixes++;
                continue;
            }
            if (!"<init>".equals(mn.name)
                    || !mn.desc.startsWith("(Lnet/minecraft/entity/player/InventoryPlayer;")) continue;
            String addSlotName = null;
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                if (!(node instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) node;
                if ("(Lnet/minecraft/inventory/Slot;)Lnet/minecraft/inventory/Slot;".equals(call.desc))
                    addSlotName = call.name;
                if (("getSizeInventory".equals(call.name) || "func_70302_i_".equals(call.name))
                        && "()I".equals(call.desc)
                        && INVENTORY_PLAYER.equals(call.owner)) {
                    call.setOpcode(Opcodes.INVOKESTATIC);
                    call.owner = "com/mod/htgt6/minecraft1202/Module/Events/Combat/OffhandHooks";
                    call.name = "getArmorInventoryEnd";
                    call.desc = "(Lnet/minecraft/entity/player/InventoryPlayer;)I";
                    call.itf = false;
                    armorFixes++;
                }
            }
            if (addSlotName == null)
                throw new IllegalStateException("ContainerPlayer add-slot method was not found");
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                if (node.getOpcode() != Opcodes.RETURN) continue;
                InsnList call = new InsnList();
                call.add(new VarInsnNode(Opcodes.ALOAD, 0));
                call.add(new org.objectweb.asm.tree.TypeInsnNode(Opcodes.NEW, "net/minecraft/inventory/Slot"));
                call.add(new InsnNode(Opcodes.DUP));
                call.add(new VarInsnNode(Opcodes.ALOAD, 1));
                call.add(new IntInsnNode(Opcodes.BIPUSH, 40));
                call.add(new IntInsnNode(Opcodes.BIPUSH, 77));
                call.add(new IntInsnNode(Opcodes.BIPUSH, 62));
                call.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/inventory/Slot", "<init>",
                        "(Lnet/minecraft/inventory/IInventory;III)V", false));
                call.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                        cn.name, addSlotName,
                        "(Lnet/minecraft/inventory/Slot;)Lnet/minecraft/inventory/Slot;", false));
                call.add(new InsnNode(Opcodes.POP));
                call.add(new VarInsnNode(Opcodes.ALOAD, 0));
                call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF, "layoutPlayerContainer112",
                        "(Lnet/minecraft/inventory/ContainerPlayer;)V", false));
                mn.instructions.insertBefore(node, call);
                injections++;
            }
        }
        if (injections == 0 || armorFixes == 0 || transferFixes != 1)
            throw new IllegalStateException("ContainerPlayer 1.12 layout integration incomplete: slot="
                    + injections + ", armor=" + armorFixes + ", transfer=" + transferFixes);
        LOGGER.info("HTGT6 ported the Minecraft 1.12 player container, including OFF_HAND transfers");
        return write(cn);
    }

    private static byte[] transformArmorSlotBindingCurse(byte[] bytes) {
        ClassNode cn = read(bytes);
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "canTakeStack",
                "(Lnet/minecraft/entity/player/EntityPlayer;)Z", null, null);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                "com/mod/htgt6/minecraft1202/Module/Events/Exploration/ExplorationHooks112", "canTakeArmorStack",
                "(Lnet/minecraft/inventory/Slot;Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
        method.instructions.add(new InsnNode(Opcodes.IRETURN));
        cn.methods.add(method);
        LOGGER.info("HTGT6 installed the 1.11 Binding Curse armor-slot rule");
        return write(cn);
    }

    /** Installs 1.12 Career/CareerLevel persistence and progressive trade population. */
    private static byte[] transformVillagerCareers112(byte[] bytes) {
        ClassNode cn = read(bytes);
        int watcherHooks = 0, spawnHooks = 0, writeHooks = 0, readHooks = 0, tradeHooks = 0, interactionHooks = 0;
        final String owner = "com/mod/htgt6/minecraft1202/Module/Gameplay/Combat/VillagerCareers112";
        for (MethodNode mn : cn.methods) {
            if ("()V".equals(mn.desc) && ("entityInit".equals(mn.name) || "func_70088_a".equals(mn.name))) {
                for (AbstractInsnNode node : mn.instructions.toArray()) if (node.getOpcode() == Opcodes.RETURN) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, "initializeWatcher",
                            "(Lnet/minecraft/entity/passive/EntityVillager;)V", false));
                    mn.instructions.insertBefore(node, hook); watcherHooks++;
                }
            } else if ("(Lnet/minecraft/entity/IEntityLivingData;)Lnet/minecraft/entity/IEntityLivingData;".equals(mn.desc)
                    && ("onSpawnWithEgg".equals(mn.name) || "func_110161_a".equals(mn.name))) {
                for (AbstractInsnNode node : mn.instructions.toArray()) if (node.getOpcode() == Opcodes.ARETURN) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, "onSpawn",
                            "(Lnet/minecraft/entity/passive/EntityVillager;)V", false));
                    mn.instructions.insertBefore(node, hook); spawnHooks++;
                }
            } else if ("(Lnet/minecraft/nbt/NBTTagCompound;)V".equals(mn.desc)
                    && ("writeEntityToNBT".equals(mn.name) || "func_70014_b".equals(mn.name))) {
                InsnList sanitize = new InsnList();
                sanitize.add(new VarInsnNode(Opcodes.ALOAD, 0));
                sanitize.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, "sanitizeOffers",
                        "(Lnet/minecraft/entity/passive/EntityVillager;)V", false));
                mn.instructions.insert(sanitize);
                for (AbstractInsnNode node : mn.instructions.toArray()) if (node.getOpcode() == Opcodes.RETURN) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, "write",
                            "(Lnet/minecraft/entity/passive/EntityVillager;Lnet/minecraft/nbt/NBTTagCompound;)V", false));
                    mn.instructions.insertBefore(node, hook); writeHooks++;
                }
            } else if ("(Lnet/minecraft/nbt/NBTTagCompound;)V".equals(mn.desc)
                    && ("readEntityFromNBT".equals(mn.name) || "func_70037_a".equals(mn.name))) {
                for (AbstractInsnNode node : mn.instructions.toArray()) if (node.getOpcode() == Opcodes.RETURN) {
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, "read",
                            "(Lnet/minecraft/entity/passive/EntityVillager;Lnet/minecraft/nbt/NBTTagCompound;)V", false));
                    mn.instructions.insertBefore(node, hook); readHooks++;
                }
            } else if ("(I)V".equals(mn.desc)
                    && ("addDefaultEquipmentAndRecipies".equals(mn.name) || "func_70950_c".equals(mn.name))) {
                LabelNode legacy = new LabelNode();
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                hook.add(new VarInsnNode(Opcodes.ILOAD, 1));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, "populate",
                        "(Lnet/minecraft/entity/passive/EntityVillager;I)Z", false));
                hook.add(new JumpInsnNode(Opcodes.IFEQ, legacy));
                hook.add(new InsnNode(Opcodes.RETURN));
                hook.add(legacy);
                mn.instructions.insert(hook); tradeHooks++;
            } else if ("(Lnet/minecraft/entity/player/EntityPlayer;)Z".equals(mn.desc)
                    && ("interact".equals(mn.name) || "func_70085_c".equals(mn.name))) {
                LabelNode vanilla = new LabelNode();
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, "suppressNitwitTrade",
                        "(Lnet/minecraft/entity/passive/EntityVillager;Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
                hook.add(new JumpInsnNode(Opcodes.IFEQ, vanilla));
                hook.add(new InsnNode(Opcodes.ICONST_0));
                hook.add(new InsnNode(Opcodes.IRETURN));
                hook.add(vanilla);
                mn.instructions.insert(hook); interactionHooks++;
            }
        }
        if (watcherHooks != 1 || spawnHooks != 1 || writeHooks != 1 || readHooks != 1 || tradeHooks != 1
                || interactionHooks != 1) {
            throw new IllegalStateException("EntityVillager 1.12 hooks incomplete: watcher="+watcherHooks+", spawn=" + spawnHooks
                    + ", write=" + writeHooks + ", read=" + readHooks + ", trades=" + tradeHooks
                    + ", interactions=" + interactionHooks);
        }
        LOGGER.info("HTGT6 installed Minecraft 1.12 villager professions, careers and trade levels");
        return write(cn);
    }

    /** Replaces the legacy villager flag conversion with Minecraft 1.12's separate entity. */
    private static byte[] transformFrostburnArrow120(byte[] bytes) {
        ClassNode node=read(bytes);int hooks=0;
        for(MethodNode method:node.methods)if((method.name.equals("onUpdate")||method.name.equals("func_70071_h_"))&&method.desc.equals("()V"))
            for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction instanceof MethodInsnNode) {
                MethodInsnNode call=(MethodInsnNode)instruction;
                if((call.name.equals("attackEntityFrom")||call.name.equals("func_70097_a"))&&call.desc.equals("(Lnet/minecraft/util/DamageSource;F)Z")) {
                    method.instructions.insertBefore(instruction,new VarInsnNode(Opcodes.ALOAD,0));
                    method.instructions.set(instruction,new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/mod/htgt6/minecraft1202/Module/Entity/Combat/EntityCombatArrow","damageTarget",
                        "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/DamageSource;FLnet/minecraft/entity/projectile/EntityArrow;)Z",false));hooks++;
                }
            }
        if(hooks!=1)throw new IllegalStateException("Arrow accepted-damage hook drift: "+hooks);
        PiercingArrowASM.transform(node);
        return write(node);
    }
    private static byte[] transformFrostburnSpawner120(byte[] bytes) {
        ClassNode node=read(bytes);int hooks=0;
        for(MethodNode method:node.methods)for(AbstractInsnNode instruction:method.instructions.toArray())
            if(instruction instanceof MethodInsnNode) {
                MethodInsnNode call=(MethodInsnNode)instruction;
                if(call.desc.equals("()Z")&&(call.name.equals("getCanSpawnHere")||call.name.equals("func_70601_bi"))) {
                    method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,
                            "com/mod/htgt6/minecraft1202/Module/Entity/Frostburn/FrostburnSpawnRules120","canSpawnFromSpawner",
                            "(Lnet/minecraft/entity/EntityLiving;)Z",false));hooks++;
                }
            }
        if(hooks!=1)throw new IllegalStateException("Frostburn spawner branch drift: "+hooks);
        return write(node);
    }
    private static byte[] transformFrostburnSun120(byte[] bytes) {
        ClassNode node=read(bytes);int hooks=0;
        for(MethodNode method:node.methods)if((method.name.equals("onLivingUpdate")||method.name.equals("func_70636_d"))&&method.desc.equals("()V"))
            for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction instanceof MethodInsnNode) {
                MethodInsnNode call=(MethodInsnNode)instruction;
                if((call.name.equals("isDaytime")||call.name.equals("func_72935_r"))&&call.desc.equals("()Z")) {
                    InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));
                    code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/mod/htgt6/minecraft1202/Module/Entity/Frostburn/EntityFrostburnHusk","solarBurnAllowed",
                        "(ZLnet/minecraft/entity/monster/EntityZombie;)Z",false));
                    method.instructions.insert(instruction,code);hooks++;
                }
            }
        if(hooks!=1)throw new IllegalStateException("Husk solar branch drift: "+hooks);
        return write(node);
    }
    private static byte[] transformZombieVillagerInfection(byte[] bytes) {
        ClassNode cn = read(bytes);
        int hooks = 0;
        for (MethodNode mn : cn.methods) {
            if (!("(Lnet/minecraft/entity/EntityLivingBase;)V".equals(mn.desc)
                    && ("onKillEntity".equals(mn.name) || "func_70074_a".equals(mn.name)))) continue;
            LabelNode vanilla = new LabelNode();
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
            hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
            hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "com/mod/htgt6/minecraft1202/Module/Events/Combat/ZombieVillagerHooks", "infectVillager",
                    "(Lnet/minecraft/entity/monster/EntityZombie;Lnet/minecraft/entity/EntityLivingBase;)Z", false));
            hook.add(new JumpInsnNode(Opcodes.IFEQ, vanilla));
            hook.add(new InsnNode(Opcodes.RETURN));
            hook.add(vanilla);
            mn.instructions.insert(hook);
            hooks++;
        }
        if (hooks != 1) throw new IllegalStateException(
                "EntityZombie villager infection hook count was " + hooks);
        LOGGER.info("HTGT6 replaced legacy zombie villager infection with the 1.10 entity");
        return write(cn);
    }

    /** Renders BackHand only after 1.7.10 has installed its first-person hand projection. */
    private static byte[] transformItemRenderer112(byte[] bytes) {
        ClassNode cn = read(bytes);
        int hooks = 0;
        for (MethodNode mn : cn.methods) {
            if (!("(F)V".equals(mn.desc)
                    && ("renderItemInFirstPerson".equals(mn.name) || "func_78440_a".equals(mn.name)))) continue;
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                if (node.getOpcode() != Opcodes.RETURN) continue;
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                hook.add(new VarInsnNode(Opcodes.FLOAD, 1));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/mod/htgt6/minecraft1202/Module/Client/Combat/ShieldItemRenderer",
                        "renderFirstPersonMainHand", "(Ljava/lang/Object;F)V", false));
                hook.add(new VarInsnNode(Opcodes.FLOAD, 1));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/mod/htgt6/client/inventory112/OffhandRenderer112",
                        "renderFirstPersonBackHand", "(F)V", false));
                mn.instructions.insertBefore(node, hook);
                hooks++;
            }
        }
        if (hooks == 0) throw new IllegalStateException("ItemRenderer first-person BackHand hook was not found");
        LOGGER.info("HTGT6 attached BackHand to the configured first-person hand projection");
        return write(cn);
    }

    private static byte[] transformShieldPlayerApi(byte[] bytes, boolean movement) {
        ClassNode cn = read(bytes);
        int hooks = 0;
        boolean delegatesToBukkitDamage = false;
        for (MethodNode method : cn.methods) {
            boolean target = movement
                    ? "onLivingUpdate".equals(method.name) || "func_70636_d".equals(method.name)
                    : "damageEntity".equals(method.name) || "func_70665_d".equals(method.name);
            if (!target) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (!movement && "(Lnet/minecraft/util/DamageSource;F)V".equals(method.desc)
                        && "damageEntity_CB".equals(call.name)
                        && "(Lnet/minecraft/util/DamageSource;F)Z".equals(call.desc)
                        && (cn.name.equals(call.owner) || "net/minecraft/entity/EntityLivingBase".equals(call.owner)))
                    delegatesToBukkitDamage = true;
                if (call.getOpcode() != Opcodes.INVOKEVIRTUAL || !"()Z".equals(call.desc)) continue;
                boolean match = movement
                        ? "isUsingItem".equals(call.name) || "func_71039_bw".equals(call.name)
                        : "isBlocking".equals(call.name) || "func_70632_aY".equals(call.name);
                if (!match) continue;
                method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        movement ? "com/mod/htgt6/client/inventory112/Inventory112ClientHandler"
                                : "com/mod/htgt6/minecraft1202/Module/Gameplay/Combat/ShieldMechanics120",
                        movement ? "isUsingItemForMovement" : "usesLegacyDamageBlock",
                        "(Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
                hooks++;
            }
        }
        if (hooks == 0 && delegatesToBukkitDamage) {
            LOGGER.info("HTGT6 detected Bukkit damage bridge; shield damage modifier is patched in EntityLivingBase inner Function");
            return bytes;
        }
        if (hooks == 0) throw new IllegalStateException("Shield API hook missing: " + cn.name);
        LOGGER.info("HTGT6 adapted shield " + (movement ? "movement" : "damage") + " API, hooks=" + hooks);
        return write(cn);
    }

    /**
     * Adapts the two projection differences that remain observable after the
     * 1.7 coordinate system is taken into account.  Player posY/yOffset changed
     * meaning after 1.7, so the 1.12 getEyeHeight code must not be copied
     * literally; the legacy eye position is already equivalent.
     */
    private static byte[] transformEntityRenderer112(byte[] bytes) {
        ClassNode cn = read(bytes);
        int firstPersonOffsets = 0;
        int farPlaneScales = 0;
        for (MethodNode mn : cn.methods) {
            boolean orientCamera = "(F)V".equals(mn.desc)
                    && ("orientCamera".equals(mn.name) || "func_78467_g".equals(mn.name));
            boolean setupCamera = "(FI)V".equals(mn.desc)
                    && ("setupCameraTransform".equals(mn.name) || "func_78479_a".equals(mn.name));
            if (!orientCamera && !setupCamera) continue;
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                // javac emits the original 2.0F as FCONST_2, not LDC.
                if (setupCamera && node.getOpcode() == Opcodes.FCONST_2) {
                    mn.instructions.set(node, new LdcInsnNode(Float.valueOf((float) Math.sqrt(2.0D))));
                    farPlaneScales++;
                    continue;
                }
                if (!(node instanceof LdcInsnNode) || !(((LdcInsnNode) node).cst instanceof Float)) continue;
                LdcInsnNode constant = (LdcInsnNode) node;
                float value = ((Float) constant.cst).floatValue();
                if (orientCamera && Float.floatToIntBits(value) == Float.floatToIntBits(-0.1F)) {
                    constant.cst = Float.valueOf(0.05F);
                    firstPersonOffsets++;
                } else if (setupCamera && Float.floatToIntBits(value) == Float.floatToIntBits(2.0F)) {
                    constant.cst = Float.valueOf((float) Math.sqrt(2.0D));
                    farPlaneScales++;
                }
            }
        }
        if (firstPersonOffsets != 1 || farPlaneScales != 1) {
            throw new IllegalStateException("EntityRenderer 1.12 POV anchors were not unique: offset="
                    + firstPersonOffsets + ", farPlane=" + farPlaneScales);
        }
        LOGGER.info("HTGT6 migrated EntityRenderer first-person offset and 1.12 far-plane projection");
        return write(cn);
    }

    /**
     * Forge 1.7 derives the creative pick-block packet slot from
     * inventoryContainer.size() - 9.  Adding OFF_HAND grows that container from
     * 45 to 46 slots and consequently writes the picked stack into the next
     * hotbar slot.  Keep the vanilla player-container base (45) for this one
     * calculation, just as 1.12 addresses hotbar slots explicitly.
     */
    private static byte[] transformInventoryTweaksPlayerSlots(byte[] bytes) {
        ClassNode cn = read(bytes);
        int hooks = 0;
        for (MethodNode method : cn.methods) {
            if (!"containerPlayerSlots".equals(method.name)) continue;
            for (AbstractInsnNode node : method.instructions.toArray()) {
                if (node.getOpcode() != Opcodes.ARETURN) continue;
                String[] sections = {"INVENTORY", "INVENTORY_NOT_HOTBAR", "INVENTORY_HOTBAR"};
                int[] starts = {9, 9, 36};
                int[] ends = {45, 36, 45};
                InsnList code = new InsnList();
                for (int i = 0; i < sections.length; i++) {
                    code.add(new InsnNode(Opcodes.DUP));
                    code.add(new FieldInsnNode(Opcodes.GETSTATIC, "invtweaks/api/container/ContainerSection",
                            sections[i], "Linvtweaks/api/container/ContainerSection;"));
                    code.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    code.add(new IntInsnNode(Opcodes.BIPUSH, starts[i]));
                    code.add(new IntInsnNode(Opcodes.BIPUSH, ends[i]));
                    code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF, "playerInventorySection112",
                            "(Ljava/lang/Object;II)Ljava/util/List;", false));
                    code.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "java/util/Map", "put",
                            "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true));
                    code.add(new InsnNode(Opcodes.POP));
                }
                method.instructions.insertBefore(node, code);
                hooks++;
            }
        }
        if (hooks != 1) throw new IllegalStateException("Inventory Tweaks player slot map hook missing");
        return write(cn);
    }

    /** ContainerPlayer retains its vanilla 0..44 layout; OFF_HAND is appended at 45. */
    public static java.util.List playerInventorySection112(Object container, int start, int end) {
        return ((net.minecraft.inventory.Container) container).inventorySlots.subList(start, end);
    }

    private static byte[] transformInventoryTweaksRefill(byte[] bytes) {
        ClassNode cn = read(bytes);
        int hooks = 0;
        for (MethodNode method : cn.methods) {
            if (!"handleAutoRefill".equals(method.name) || !"()V".equals(method.desc)) continue;
            // Jump to the existing baseline refresh, rather than returning with stale storedStackId.
            AbstractInsnNode refresh = null;
            AbstractInsnNode decision = null;
            for (AbstractInsnNode node : method.instructions.toArray()) {
                if (!(node instanceof FieldInsnNode)) continue;
                FieldInsnNode field = (FieldInsnNode) node;
                if ("storedStackId".equals(field.name) && node.getOpcode() == Opcodes.PUTFIELD)
                    refresh = node.getPrevious().getPrevious(); // ALOAD 0, ALOAD 2, PUTFIELD
                if (decision == null && "storedStackId".equals(field.name) && node.getOpcode() == Opcodes.GETFIELD)
                    decision = node.getPrevious().getPrevious(); // ALOAD 2, ALOAD 0, GETFIELD
            }
            if (refresh == null || decision == null) throw new IllegalStateException("Inventory Tweaks refill baseline missing");
            LabelNode baseline = new LabelNode();
            method.instructions.insertBefore(refresh, baseline);
            InsnList code = new InsnList();
            code.add(new VarInsnNode(Opcodes.ILOAD, 4)); // focusedSlot + 27
            code.add(new IntInsnNode(Opcodes.BIPUSH, 27));
            code.add(new InsnNode(Opcodes.ISUB));
            code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "com/mod/htgt6/client/inventory112/OffhandRefillGuard", "suppress", "(I)Z", false));
            code.add(new JumpInsnNode(Opcodes.IFNE, baseline));
            method.instructions.insertBefore(decision, code);
            hooks++;
        }
        if (hooks != 1) throw new IllegalStateException("Inventory Tweaks refill hook missing");
        return write(cn);
    }

    private static byte[] transformCreativePickBlock112(byte[] bytes) {
        ClassNode cn = read(bytes);
        rewriteWindowTitleCalls(cn);
        int fixes = 0;
        for (MethodNode mn : cn.methods) {
            if (!("()V".equals(mn.desc)
                    && ("func_147112_ai".equals(mn.name) || "middleClickMouse".equals(mn.name)
                    || "ao".equals(mn.name)))) continue;
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                if (!(node instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) node;
                if ((call.getOpcode() == Opcodes.INVOKEINTERFACE || call.getOpcode() == Opcodes.INVOKEVIRTUAL)
                        && "java/util/List".equals(call.owner) && "size".equals(call.name)
                        && "()I".equals(call.desc)) {
                    call.setOpcode(Opcodes.INVOKESTATIC);
                    call.owner = SELF;
                    call.name = "getPickBlockContainerSize112";
                    call.desc = "(Ljava/util/List;)I";
                    call.itf = false;
                    fixes++;
                }
            }
        }
        if (fixes != 1) throw new IllegalStateException("Creative pick-block slot fix was not applied: " + fixes);
        LOGGER.info("HTGT6 fixed creative pick-block hotbar addressing for the 46-slot container");
        return write(cn);
    }

    /**
     * Inventory Tweaks 1.59 ships an SRG-only AT for field_74512_d. ForgeGradle's
     * deobfuscated runClient exposes the same field as keyCode, leaving it
     * package-private and causing an IllegalAccessError during mod init.
     */
    private static byte[] transformKeyBindingInventoryTweaksCompat(byte[] bytes) {
        ClassNode cn = read(bytes);
        int fixes = 0;
        for (FieldNode field : cn.fields) {
            if (!"I".equals(field.desc)
                    || !("keyCode".equals(field.name) || "field_74512_d".equals(field.name))) continue;
            field.access = (field.access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PUBLIC;
            fixes++;
        }
        if (fixes != 1) throw new IllegalStateException(
                "Inventory Tweaks KeyBinding access fix count was " + fixes);
        LOGGER.info("HTGT6 exposed KeyBinding.keyCode for Inventory Tweaks runClient compatibility");
        return write(cn);
    }

    /**
     * Minecraft_1.20.2 owns modern villager professions and trades. Village
     * Names 4.4.13 otherwise runs its 6 KB reflective trade rewriter on every
     * villager join and living tick. Large migrated villages can keep both the
     * integrated server and client thread inside ReflectionHelper indefinitely,
     * leaving the world view blank even though login completed successfully.
     */
    private static byte[] transformVillageNamesTradeMonitor(byte[] bytes) {
        ClassNode cn = read(bytes);
        int fixes = 0;
        for (MethodNode method : cn.methods) {
            if (!"monitorVillagerTrades".equals(method.name)
                    || !"(Lnet/minecraft/entity/passive/EntityVillager;)V".equals(method.desc)) continue;
            replaceWithReturn(method);
            fixes++;
        }
        if (fixes != 1) throw new IllegalStateException(
                "Village Names trade-monitor compatibility fix count was " + fixes);
        LOGGER.info("HTGT6 disabled Village Names' duplicate modern villager trade monitor");
        return write(cn);
    }

    /**
     * Village Names uses its LivingUpdateEvent hook to rebuild trade state on
     * every server tick and to reflect/synchronize the same villager data every
     * five ticks. HTGT6 owns that trade state, while VN's tracking event already
     * sends the visual skin data when a player starts watching the entity.
     * Skip only the exact vanilla-villager branch; zombie curing, guards and
     * player reputation updates in the same handler remain active.
     */
    private static byte[] transformVillageNamesEntityMonitor(byte[] bytes) {
        ClassNode cn = read(bytes);
        int fixes = 0;
        for (MethodNode method : cn.methods) {
            if (!"onLivingUpdateEvent".equals(method.name)
                    || !"(Lnet/minecraftforge/event/entity/living/LivingEvent$LivingUpdateEvent;)V"
                    .equals(method.desc)) continue;
            LabelNode continueNormally = new LabelNode();
            InsnList guard = new InsnList();
            guard.add(new VarInsnNode(Opcodes.ALOAD, 1));
            guard.add(new FieldInsnNode(Opcodes.GETFIELD,
                    "net/minecraftforge/event/entity/living/LivingEvent$LivingUpdateEvent",
                    "entity", "Lnet/minecraft/entity/Entity;"));
            guard.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass",
                    "()Ljava/lang/Class;", false));
            guard.add(new LdcInsnNode(Type.getObjectType(ENTITY_VILLAGER)));
            guard.add(new JumpInsnNode(Opcodes.IF_ACMPNE, continueNormally));
            guard.add(new InsnNode(Opcodes.RETURN));
            guard.add(continueNormally);
            method.instructions.insert(guard);
            fixes++;
        }
        if (fixes != 1) throw new IllegalStateException(
                "Village Names living villager monitor compatibility fix count was " + fixes);
        LOGGER.info("HTGT6 disabled Village Names' per-tick vanilla villager monitor; skin tracking remains active");
        return write(cn);
    }

    /** Prevent client mods from appending their own dev/GTNH version to the pack title. */
    private static byte[] transformWindowTitleWriter(byte[] bytes, String className) {
        ClassNode cn = read(bytes);
        int rewrites = rewriteWindowTitleCalls(cn);
        if (rewrites == 0) throw new IllegalStateException(
                "Window-title writer did not contain Display.setTitle: " + className);
        LOGGER.info("HTGT6 redirected {} window-title write(s) in {}", rewrites, className);
        return write(cn);
    }

    private static int rewriteWindowTitleCalls(ClassNode cn) {
        int rewrites = 0;
        for (MethodNode method : cn.methods) for (AbstractInsnNode node : method.instructions.toArray()) {
            if (!(node instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) node;
            if (call.getOpcode() != Opcodes.INVOKESTATIC
                    || !"org/lwjgl/opengl/Display".equals(call.owner)
                    || !"setTitle".equals(call.name)
                    || !"(Ljava/lang/String;)V".equals(call.desc)) continue;
            call.owner = SELF;
            call.name = "setWindowTitle";
            call.itf = false;
            rewrites++;
        }
        return rewrites;
    }

    public static void setWindowTitle(String ignored) {
        org.lwjgl.opengl.Display.setTitle(HTGT6Config.WINDOW_TITLE);
    }

    public static int getPickBlockContainerSize112(java.util.List slots) {
        return slots == null ? 45 : Math.min(45, slots.size());
    }

    /** Permit C10 creative updates for the new container slot 45 (OFF_HAND). */
    private static byte[] transformCreativeOffhandValidation112(byte[] bytes) {
        ClassNode cn = read(bytes);
        int fixes = 0;
        for (MethodNode mn : cn.methods) {
            if (!("(Lnet/minecraft/network/play/client/C10PacketCreativeInventoryAction;)V".equals(mn.desc)
                    && ("processCreativeInventoryAction".equals(mn.name) || "func_147344_a".equals(mn.name)))) continue;
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                if (!(node instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) node;
                if (call.getOpcode() == Opcodes.INVOKESTATIC
                        && INVENTORY_PLAYER.equals(call.owner)
                        && ("getHotbarSize".equals(call.name) || "func_70451_h".equals(call.name))
                        && "()I".equals(call.desc)) {
                    call.owner = SELF;
                    call.name = "getCreativeValidationSize112";
                    call.itf = false;
                    fixes++;
                }
            }
        }
        if (fixes != 1) throw new IllegalStateException("Creative OFF_HAND server validation fix was not applied: " + fixes);
        LOGGER.info("HTGT6 allowed authoritative creative updates for OFF_HAND slot 45");
        return write(cn);
    }

    /** Vanilla adds this value to 36 and uses an exclusive upper bound. */
    public static int getCreativeValidationSize112() { return 10; }

    /**
     * ModularUI player slots are item-handler backed and are not discoverable through
     * Container.getSlotFromInventory.  A world click racing the GUI-close packet therefore
     * leaves vanilla with a null Slot at NetHandlerPlayServer:657.  End that exceptional
     * correction branch safely while retaining normal placement for every vanilla container.
     */
    private static byte[] transformMissingPlacementSlotGuard(byte[] bytes) {
        ClassNode cn = read(bytes);
        int guards = 0;
        for (MethodNode mn : cn.methods) {
            if (!("(Lnet/minecraft/network/play/client/C08PacketPlayerBlockPlacement;)V".equals(mn.desc)
                    && ("processPlayerBlockPlacement".equals(mn.name) || "func_147346_a".equals(mn.name)))) continue;
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                if (!(node instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) node;
                if (!("net/minecraft/inventory/Container".equals(call.owner)
                        && ("getSlotFromInventory".equals(call.name) || "func_75147_a".equals(call.name))
                        && "(Lnet/minecraft/inventory/IInventory;I)Lnet/minecraft/inventory/Slot;".equals(call.desc))) continue;
                AbstractInsnNode storeNode = call.getNext();
                while (storeNode != null && storeNode.getOpcode() < 0) storeNode = storeNode.getNext();
                if (!(storeNode instanceof VarInsnNode) || storeNode.getOpcode() != Opcodes.ASTORE) continue;
                int slotLocal = ((VarInsnNode) storeNode).var;
                LabelNode present = new LabelNode();
                InsnList guard = new InsnList();
                guard.add(new VarInsnNode(Opcodes.ALOAD, slotLocal));
                guard.add(new JumpInsnNode(Opcodes.IFNONNULL, present));
                guard.add(new VarInsnNode(Opcodes.ALOAD, 0));
                guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                        "recoverMissingPlacementSlot", "(Ljava/lang/Object;)V", false));
                guard.add(new InsnNode(Opcodes.RETURN));
                guard.add(present);
                mn.instructions.insert(storeNode, guard);
                guards++;
            }
        }
        if (guards != 1) throw new IllegalStateException(
                "NetHandlerPlayServer missing placement-slot guard count=" + guards + ", expected=1");
        LOGGER.info("HTGT6 guarded ModularUI placement synchronization against a missing hotbar Slot");
        return write(cn);
    }

    public static void recoverMissingPlacementSlot(Object handlerObject) {
        if (!(handlerObject instanceof net.minecraft.network.NetHandlerPlayServer)) return;
        net.minecraft.network.NetHandlerPlayServer handler =
                (net.minecraft.network.NetHandlerPlayServer) handlerObject;
        if (handler.playerEntity == null) return;
        handler.playerEntity.isChangingQuantityOnly = false;
        if (handler.playerEntity.openContainer != null) {
            handler.playerEntity.openContainer.detectAndSendChanges();
        }
    }

    /** Applies the exact 1.12 ContainerPlayer coordinates after the legacy constructor has populated it. */
    public static void layoutPlayerContainer112(net.minecraft.inventory.ContainerPlayer container) {
        if (container == null || container.inventorySlots == null || container.inventorySlots.size() != 46) {
            throw new IllegalStateException("Expected the 46-slot Minecraft 1.12 player container");
        }
        java.util.List slots = container.inventorySlots;
        setSlotPosition(slots, 0, 154, 28);
        for (int index = 1; index <= 4; ++index) {
            int craft = index - 1;
            setSlotPosition(slots, index, 98 + craft % 2 * 18, 18 + craft / 2 * 18);
        }
        for (int index = 5; index <= 8; ++index)
            setSlotPosition(slots, index, 8, 8 + (index - 5) * 18);
        for (int index = 9; index <= 35; ++index) {
            int inventory = index - 9;
            setSlotPosition(slots, index, 8 + inventory % 9 * 18, 84 + inventory / 9 * 18);
        }
        for (int index = 36; index <= 44; ++index)
            setSlotPosition(slots, index, 8 + (index - 36) * 18, 142);
        setSlotPosition(slots, 45, 77, 62);
    }

    private static void setSlotPosition(java.util.List slots, int index, int x, int y) {
        net.minecraft.inventory.Slot slot = (net.minecraft.inventory.Slot) slots.get(index);
        slot.xDisplayPosition = x;
        slot.yDisplayPosition = y;
    }

    /** Installs the Stage-8 conditional height seam; it remains legacy-height until all gates activate. */
    private static byte[] transformWorldProviderHeight120(byte[] bytes) {
        ClassNode cn=read(bytes);
        int patched=0;
        for(MethodNode mn:cn.methods){
            if(!"()I".equals(mn.desc))continue;
            String hook;
            if("getHeight".equals(mn.name))hook="worldProviderHeight120";
            else if("getActualHeight".equals(mn.name))hook="worldProviderActualHeight120";
            else continue;
            mn.instructions.clear();mn.tryCatchBlocks.clear();mn.localVariables=null;
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,SELF,hook,
                    "(Lnet/minecraft/world/WorldProvider;)I",false));
            mn.instructions.add(new InsnNode(Opcodes.IRETURN));
            mn.maxStack=1;mn.maxLocals=1;patched++;
        }
        if(patched!=2)throw new IllegalStateException("WorldProvider height seams patched="+patched+", expected=2");
        LOGGER.info("HTGT6 installed dormant conditional Futura WorldProvider height seams");
        return write(cn);
    }

    public static int worldProviderHeight120(net.minecraft.world.WorldProvider provider){
        return com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.height(
                provider==null?null:provider.worldObj);
    }

    public static int worldProviderActualHeight120(net.minecraft.world.WorldProvider provider){
        return com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.actualHeight(provider);
    }

    private static byte[] transformWorldProviderEnd(byte[] bytes) {
        ClassNode cn = read(bytes);
        int replacements = 0;
        for (MethodNode mn : cn.methods) {
            if (!"()Lnet/minecraft/world/chunk/IChunkProvider;".equals(mn.desc)) continue;
            if (!("createChunkGenerator".equals(mn.name) || "func_76555_c".equals(mn.name))) continue;
            mn.instructions.clear();
            mn.tryCatchBlocks.clear();
            mn.localVariables = null;
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "com/mod/htgt6/minecraft1202/Module/Events/Combat/EndUpdateHooks", "createChunkProvider",
                    "(Lnet/minecraft/world/WorldProvider;)Lnet/minecraft/world/chunk/IChunkProvider;", false));
            mn.instructions.add(new InsnNode(Opcodes.ARETURN));
            mn.maxStack = 1;
            mn.maxLocals = 1;
            replacements++;
        }
        if (replacements == 0) throw new IllegalStateException("WorldProviderEnd chunk-provider method was not found");
        LOGGER.info("HTGT6 replaced the 1.7 End terrain provider with the Minecraft 1.9 provider");
        return write(cn);
    }

    /**
     * GT6's water blocks recursively schedule neighbours, relight a 67x67 area
     * and replace nearby vanilla water. Worldgen already writes the complete
     * fluid volume, so those perpetual simulation ticks are unnecessary.
     */
    private static byte[] transformGTWaterBlock(byte[] bytes, String className) {
        ClassNode cn = read(bytes);
        boolean patched = false;
        String neighbourDesc = "(Lnet/minecraft/world/World;IIILnet/minecraft/block/Block;)V";

        for (MethodNode mn : cn.methods) {
            if (GT_WATER_OCEAN.equals(className)
                    && "getLightOpacity".equals(mn.name)
                    && "(Lnet/minecraft/world/IBlockAccess;III)I".equals(mn.desc)) {
                // BlockOcean queries neighbouring blocks through World while
                // Chunk.generateSkylightMap is still constructing that same
                // chunk. That recursively asks ChunkProviderServer to create
                // it again and ends in StackOverflowError. River already uses
                // the same constant opacity value.
                replaceWithInt(mn, 3);
                patched = true;
            } else if (GT_WATER_OCEAN.equals(className)
                    && ("getRenderColor".equals(mn.name) || "func_149741_i".equals(mn.name)) && "(I)I".equals(mn.desc)) {
                replaceWithInt(mn, 0x3F76E4);
                patched = true;
            } else if (GT_WATER_OCEAN.equals(className)
                    && ("colorMultiplier".equals(mn.name) || "func_149720_d".equals(mn.name))
                    && "(Lnet/minecraft/world/IBlockAccess;III)I".equals(mn.desc)) {
                mn.instructions.clear();
                mn.tryCatchBlocks.clear();
                mn.localVariables = null;
                mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
                mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 4));
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                        "getBiomeWaterColor",
                        "(Lnet/minecraft/world/IBlockAccess;II)I", false));
                mn.instructions.add(new InsnNode(Opcodes.IRETURN));
                mn.maxStack = 3;
                mn.maxLocals = 5;
                patched = true;
            } else if (("onNeighborBlockChange".equals(mn.name) || "func_149695_a".equals(mn.name)) && neighbourDesc.equals(mn.desc)) {
                replaceWithReturn(mn);
                patched = true;
            } else if (("onBlockAdded".equals(mn.name) || "func_149726_b".equals(mn.name))
                    && "(Lnet/minecraft/world/World;III)V".equals(mn.desc)) {
                replaceWithReturn(mn);
                patched = true;
            } else if (("updateTick".equals(mn.name) || "func_149674_a".equals(mn.name)
                    || GT_WATER_BASE.equals(className) && "updateFlow".equals(mn.name))
                    && "(Lnet/minecraft/world/World;IIILjava/util/Random;)V".equals(mn.desc)) {
                replaceWithReturn(mn);
                patched = true;
            }
        }

        // Seal inherited Forge fluid scheduling in both naming environments.
        // Existing overrides above are neutralized; missing base overrides must
        // also exist so BlockSwamp cannot inherit BlockFluidClassic scheduling.
        if (GT_WATER_BASE.equals(className)) {
            boolean srg = false;
            for (MethodNode method : cn.methods) if (method.name.startsWith("func_")) srg = true;
            String[] names = srg
                    ? new String[]{"func_149695_a", "func_149726_b", "func_149674_a"}
                    : new String[]{"onNeighborBlockChange", "onBlockAdded", "updateTick"};
            for (int i = 0; i < names.length; i++) {
                String desc = i == 0 ? neighbourDesc : i == 1
                        ? "(Lnet/minecraft/world/World;III)V"
                        : "(Lnet/minecraft/world/World;IIILjava/util/Random;)V";
                boolean found = false;
                for (MethodNode method : cn.methods) {
                    if (names[i].equals(method.name) && desc.equals(method.desc)) { found = true; break; }
                }
                if (!found) {
                    MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, names[i], desc, null, null);
                    method.instructions.add(new InsnNode(Opcodes.RETURN));
                    method.maxStack = 0;
                    method.maxLocals = i == 1 ? 5 : 6;
                    cn.methods.add(method);
                    patched = true;
                }
            }
        }

        if (patched) LOGGER.info("HTGT6 disabled aggressive GT6 water ticks in {}", className);
        return patched ? write(cn) : bytes;
    }

    /** Surface tint for GT ocean water; the original block hard-coded 0xC0C0C0. */
    public static int getBiomeWaterColor(net.minecraft.world.IBlockAccess world, int x, int z) {
        if (world == null) return 0x3F76E4;
        net.minecraft.world.biome.BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
        return biome == null ? 0x3F76E4 : biome.waterColorMultiplier;
    }

    /**
     * GT6 cauldron washing performs random selections from optional byproduct
     * collections. Addons can legally leave those collections empty, while
     * Random.nextInt(0) throws from the server WorldTickEvent.
     */
    private static byte[] transformGT6CauldronWashing(byte[] bytes) {
        ClassNode cn = read(bytes);
        final String descriptor = "(Lgregapi/oredict/OreDictPrefix;"
                + "Lgregapi/oredict/OreDictMaterial;Lnet/minecraft/item/ItemStack;"
                + "Lnet/minecraft/entity/item/EntityItem;)Lnet/minecraft/item/ItemStack;";
        int guards = 0;
        for (MethodNode mn : cn.methods) {
            if (!"onTickWorld".equals(mn.name) || !descriptor.equals(mn.desc)) continue;
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                if (!(node instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) node;
                if (call.getOpcode() != Opcodes.INVOKEVIRTUAL
                        || !"java/util/Random".equals(call.owner)
                        || !"nextInt".equals(call.name) || !"(I)I".equals(call.desc)) continue;
                mn.instructions.insertBefore(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                        SELF, "positiveRandomBound", "(I)I", false));
                guards++;
            }
        }
        if (guards == 0) {
            throw new IllegalStateException("GT6 OreDictListenerItem_Washing.onTickWorld was not found");
        }
        LOGGER.info("HTGT6 guarded {} GT6 Cauldron washing random bound(s)", guards);
        return write(cn);
    }

    /** Public because transformed GT6 bytecode invokes this coremod helper directly. */
    public static int positiveRandomBound(int bound) {
        return bound > 0 ? bound : 1;
    }

    /**
     * GT6 exposes every ore-prefix stack limit in stacksizes.cfg. Wire and
     * cable limits are a pack invariant in HTGT6, so their lookup is filtered
     * instead of leaving dozens of ineffective per-gauge configuration keys.
     */
    private static byte[] transformGT6WireStackSizeConfig(byte[] bytes) {
        ClassNode cn = read(bytes);
        final String owner = "net/minecraftforge/common/config/Configuration";
        final String descriptor = "(Ljava/lang/String;Ljava/lang/String;I)"
                + "Lnet/minecraftforge/common/config/Property;";
        final String redirectedDescriptor = "(Lnet/minecraftforge/common/config/Configuration;"
                + "Ljava/lang/String;Ljava/lang/String;I)"
                + "Lnet/minecraftforge/common/config/Property;";
        int redirects = 0;
        for (MethodNode mn : cn.methods) {
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                if (!(node instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) node;
                if (call.getOpcode() != Opcodes.INVOKEVIRTUAL
                        || !owner.equals(call.owner) || !"get".equals(call.name)
                        || !descriptor.equals(call.desc)) continue;
                call.setOpcode(Opcodes.INVOKESTATIC);
                call.owner = SELF;
                call.name = "getGT6StackSizeConfigProperty";
                call.desc = redirectedDescriptor;
                call.itf = false;
                redirects++;
            }
        }
        if (redirects == 0) {
            throw new IllegalStateException("GT_API integer Configuration.get call was not found");
        }
        LOGGER.info("HTGT6 redirected {} GT6 integer configuration lookup(s) for wire stack limits",
                redirects);
        return write(cn);
    }

    /** Public because transformed GT6 bytecode invokes this coremod helper directly. */
    public static net.minecraftforge.common.config.Property getGT6StackSizeConfigProperty(
            net.minecraftforge.common.config.Configuration configuration,
            String category, String key, int defaultValue) {
        boolean stackSizes = "stacksizes".equalsIgnoreCase(category);
        boolean wire = stackSizes && isGT6WireStackSizeKey(key);
        net.minecraftforge.common.config.Property property = configuration.get(
                category, key, wire ? 64 : defaultValue);
        if (wire) property.set(64);

        // Remove both the property requested in this launch and stale keys
        // whose suffix contains an older default (for example wireGt16_8).
        if (stackSizes) {
            net.minecraftforge.common.config.ConfigCategory values = configuration.getCategory(category);
            for (String existing : new java.util.ArrayList<String>(values.keySet())) {
                if (isGT6WireStackSizeKey(existing)) values.remove(existing);
            }
        }
        return property;
    }

    private static boolean isGT6WireStackSizeKey(String key) {
        if (key == null) return false;
        String normalized = key.toLowerCase(java.util.Locale.ROOT);
        return normalized.startsWith("wire") || normalized.startsWith("cable");
    }

    /** GT6 wires are MultiTileEntity items; their real limit bypasses OreDictPrefix. */
    private static byte[] transformGT6WireItemStackLimit(byte[] bytes) {
        ClassNode cn = read(bytes);
        int patched = 0;
        for (MethodNode mn : cn.methods) {
            if (!"getItemStackLimit".equals(mn.name)
                    || !"(Lnet/minecraft/item/ItemStack;)I".equals(mn.desc)) continue;
            LabelNode useOriginal = new LabelNode();
            InsnList hook = new InsnList();
            hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
            hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                    "getGT6WireStackLimit", "(Lnet/minecraft/item/ItemStack;)I", false));
            hook.add(new InsnNode(Opcodes.DUP));
            hook.add(new JumpInsnNode(Opcodes.IFEQ, useOriginal));
            hook.add(new InsnNode(Opcodes.IRETURN));
            hook.add(useOriginal);
            hook.add(new InsnNode(Opcodes.POP));
            mn.instructions.insert(hook);
            patched++;
        }
        if (patched != 1) {
            throw new IllegalStateException("GT6 MultiTileEntity wire stack-limit hook count " + patched);
        }
        LOGGER.info("HTGT6 installed the GT6 MultiTileEntity wire stack-limit hook");
        return write(cn);
    }

    /** Returns zero for non-wire MultiTileEntity items, preserving their original limits. */
    public static int getGT6WireStackLimit(net.minecraft.item.ItemStack stack) {
        gregapi.oredict.OreDictItemData data = gregapi.util.OM.anyassociation(stack);
        if (data == null || data.mPrefix == null || data.mPrefix.mNameInternal == null) return 0;
        String name = data.mPrefix.mNameInternal.toLowerCase(java.util.Locale.ROOT);
        return name.startsWith("wire") || name.startsWith("cable") ? 64 : 0;
    }

    /**
     * Disables GT6's populate-stage replacement passes. GT water is composed
     * into ChunkProviderGenerate's raw block array instead, before a Chunk and
     * its scheduled-tick lists exist.
     */
    private static byte[] transformGTWaterWorldgen(byte[] bytes, String className) {
        ClassNode cn = read(bytes);
        int replacements = 0;
        for (MethodNode mn : cn.methods) {
            if (!"(Lnet/minecraft/world/World;Lnet/minecraft/world/chunk/Chunk;IIIIILjava/util/Random;"
                    .concat("[[Lnet/minecraft/world/biome/BiomeGenBase;Ljava/util/Set;)Z")
                    .equals(mn.desc)) continue;
            replaceWithFalse(mn);
            replacements++;
        }
        if (replacements > 0) {
            LOGGER.info("HTGT6 disabled GT6 populate-stage water replacement in {}", className);
            return write(cn);
        }
        return bytes;
    }

    /** Injects one raw-array composition call at each exit of vanilla biome surface generation. */
    private static byte[] transformChunkProviderGenerateWater(byte[] bytes) {
        ClassNode cn = read(bytes);
        String desc = "(II[Lnet/minecraft/block/Block;[B[Lnet/minecraft/world/biome/BiomeGenBase;)V";
        String worldField = null;
        for (FieldNode field : cn.fields) {
            if ("Lnet/minecraft/world/World;".equals(field.desc)) {
                worldField = field.name;
                break;
            }
        }
        if (worldField == null) throw new IllegalStateException("ChunkProviderGenerate world field was not found");
        String biomeField = null;
        for (FieldNode field : cn.fields) {
            if ("[Lnet/minecraft/world/biome/BiomeGenBase;".equals(field.desc)) {
                biomeField = field.name;
                break;
            }
        }
        int injections = 0;
        for (MethodNode mn : cn.methods) {
            if (!desc.equals(mn.desc)) continue;
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                if (node.getOpcode() != Opcodes.RETURN) continue;
                InsnList call = new InsnList();
                call.add(new VarInsnNode(Opcodes.ALOAD, 0));
                call.add(new FieldInsnNode(Opcodes.GETFIELD, CHUNK_PROVIDER_GENERATE, worldField,
                        "Lnet/minecraft/world/World;"));
                call.add(new VarInsnNode(Opcodes.ILOAD, 1));
                call.add(new VarInsnNode(Opcodes.ILOAD, 2));
                call.add(new VarInsnNode(Opcodes.ALOAD, 3));
                call.add(new VarInsnNode(Opcodes.ALOAD, 4));
                call.add(new VarInsnNode(Opcodes.ALOAD, 5));
                call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                        "composeGTChunkArrays",
                        "(Lnet/minecraft/world/World;II[Lnet/minecraft/block/Block;[B"
                                + "[Lnet/minecraft/world/biome/BiomeGenBase;)V", false));
                mn.instructions.insertBefore(node, call);
                injections++;
            }
        }
        if (injections == 0) {
            throw new IllegalStateException("ChunkProviderGenerate biome block method was not found");
        }
        int biomeArrayInjections = 0;
        if (HTGT6Config.ENABLE_EXTENDED_BIOME_ASM) {
            if (biomeField == null) {
                throw new IllegalStateException("ChunkProviderGenerate biome array field was not found");
            }
            for (MethodNode mn : cn.methods) {
                if (!"(II)Lnet/minecraft/world/chunk/Chunk;".equals(mn.desc)) continue;
                for (AbstractInsnNode node : mn.instructions.toArray()) {
                    if (node.getOpcode() != Opcodes.ARETURN) continue;
                    InsnList fill = new InsnList();
                    // Preserve the Chunk already on the operand stack for ARETURN.
                    fill.add(new InsnNode(Opcodes.DUP));
                    fill.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    fill.add(new FieldInsnNode(Opcodes.GETFIELD, CHUNK_PROVIDER_GENERATE, biomeField,
                            "[Lnet/minecraft/world/biome/BiomeGenBase;"));
                    fill.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                            "fillGeneratedChunkBiomeIds",
                            "(Lnet/minecraft/world/chunk/Chunk;"
                                    + "[Lnet/minecraft/world/biome/BiomeGenBase;)V", false));
                    mn.instructions.insertBefore(node, fill);
                    biomeArrayInjections++;
                }
            }
            if (biomeArrayInjections == 0) {
                throw new IllegalStateException("ChunkProviderGenerate.provideChunk was not found");
            }
        }
        LOGGER.info("HTGT6 composes GT6 water/stone during raw chunk generation ({} path(s))", injections);
        if (biomeArrayInjections > 0) {
            LOGGER.info("HTGT6 writes all 256 extended biome IDs during chunk creation");
        }
        return write(cn);
    }

    /**
     * Vanilla writes biome IDs through Chunk.getBiomeArray(). That API is only
     * byte-wide, so IDs 300-600 would be truncated and the extended array used
     * to remain lazy/unset. Fill it from the original 16x16 biome result.
     */
    public static void fillGeneratedChunkBiomeIds(net.minecraft.world.chunk.Chunk chunk,
                                                   net.minecraft.world.biome.BiomeGenBase[] biomes) {
        if (chunk == null || biomes == null) return;
        int[] target = getExtendedBiomeArray(chunk);
        int length = Math.min(target.length, biomes.length);
        for (int i = 0; i < length; i++) {
            target[i] = biomes[i] == null ? net.minecraft.world.biome.BiomeGenBase.plains.biomeID
                    : biomes[i].biomeID;
        }
        VALIDATED_OCEAN_BIOME_CHUNKS.put(chunk, Boolean.TRUE);
    }

    public static void composeGTChunkArrays(net.minecraft.world.World world, int chunkX, int chunkZ,
                                             net.minecraft.block.Block[] blocks, byte[] metadata,
                                             net.minecraft.world.biome.BiomeGenBase[] biomes) {
        if (HTGT6Config.FIX_GT6_STONE_GEN_LAG) {
            generateGTStoneLayersInChunkArray(world, chunkX, chunkZ, blocks, metadata, biomes);
        }
        if (HTGT6Config.FIX_GT6_WATER_TICK_UPDATE) {
            generateGTWaterInBiomeArray(blocks, biomes);
        }
    }

    /**
     * Runs before the Chunk constructor. No World.setBlock, lighting update,
     * neighbour notification or scheduled tick is involved.
     */
    public static void generateGTWaterInBiomeArray(net.minecraft.block.Block[] blocks,
                                                    net.minecraft.world.biome.BiomeGenBase[] biomes) {
        generateGTWaterInBiomeArray(blocks,biomes,false);
    }

    /** Futura raw blocks are X-major, while its WorldChunkManager palette is Z-major. */
    public static void generateGTFuturaWaterInBiomeArray(net.minecraft.block.Block[] blocks,
                                                          net.minecraft.world.biome.BiomeGenBase[] biomes) {
        generateGTWaterInBiomeArray(blocks,biomes,true);
    }

    private static void generateGTWaterInBiomeArray(net.minecraft.block.Block[] blocks,
                                                     net.minecraft.world.biome.BiomeGenBase[] biomes,
                                                     boolean zMajorBiomes) {
        if (blocks == null || blocks.length < 65536 || biomes == null) return;
        int height = blocks.length / 256;
        if (height < 256 || height * 256 != blocks.length) return;
        int columns = Math.min(256, biomes.length);
        for (int column = 0; column < columns; column++) {
            int biomeIndex=zMajorBiomes?futuraBiomeIndexForRawColumn(column):column;
            net.minecraft.block.Block water = getGTWaterForBiome(biomes[biomeIndex]);
            if (water == null) continue;
            int base = column * height;
            // Futura translates modern sea level 63 to physical Y=127. The
            // legacy path keeps the original 62 ceiling. Addition is required
            // for the non-power-of-two 384-block column stride; bitwise OR is
            // only accidentally valid for the old 256 stride.
            int waterTop=zMajorBiomes
                    ?com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120.SEA_LEVEL-1
                    :62;
            for (int y = waterTop; y > 0; y--) {
                int index = base + y;
                net.minecraft.block.Block existing = blocks[index];
                if (isGeneratedWaterForGT(existing,zMajorBiomes)) {
                    blocks[index] = water;
                }
            }
        }
    }

    /** Futura's stable raw aquifer fluid represents generated Overworld water. */
    public static boolean isGeneratedWaterForGT(net.minecraft.block.Block block,boolean futura){
        return block==net.minecraft.init.Blocks.water||block==net.minecraft.init.Blocks.flowing_water
                ||futura&&block==com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.aquiferWater;
    }

    /** Maps raw-array column localX*16+localZ to Chunk biome localX+localZ*16. */
    public static int futuraBiomeIndexForRawColumn(int rawColumn) {
        if(rawColumn<0||rawColumn>=256)throw new IllegalArgumentException("Invalid raw column");
        return (rawColumn>>>4)+(rawColumn&15)*16;
    }

    /** Non-power-of-two Futura raw-array index contract used by GT water. */
    public static int futuraWaterRawIndex(int height,int rawColumn,int y){
        if(height<256||rawColumn<0||rawColumn>=256||y<0||y>=height)
            throw new IllegalArgumentException("Invalid Futura raw water coordinate");
        return rawColumn*height+y;
    }

    private static net.minecraft.block.Block getGTWaterForBiome(
            net.minecraft.world.biome.BiomeGenBase biome) {
        if (biome == null) return null;
        if (biome == net.minecraft.world.biome.BiomeGenBase.swampland
                || net.minecraftforge.common.BiomeDictionary.isBiomeOfType(
                biome, net.minecraftforge.common.BiomeDictionary.Type.SWAMP)) {
            return gregapi.data.CS.BlocksGT.Swamp;
        }
        if (biome == net.minecraft.world.biome.BiomeGenBase.river
                || biome == net.minecraft.world.biome.BiomeGenBase.frozenRiver
                || net.minecraftforge.common.BiomeDictionary.isBiomeOfType(
                biome, net.minecraftforge.common.BiomeDictionary.Type.RIVER)) {
            return gregapi.data.CS.BlocksGT.River;
        }
        if (biome == net.minecraft.world.biome.BiomeGenBase.ocean
                || biome == net.minecraft.world.biome.BiomeGenBase.deepOcean
                || biome == net.minecraft.world.biome.BiomeGenBase.frozenOcean
                || net.minecraftforge.common.BiomeDictionary.isBiomeOfType(
                biome, net.minecraftforge.common.BiomeDictionary.Type.OCEAN)) {
            return gregapi.data.CS.BlocksGT.Ocean;
        }
        return null;
    }

    /** Replaces the expensive WorldgenStoneLayers post-pass with a sparse ore flush. */
    private static byte[] transformGTStoneLayers(byte[] bytes) {
        ClassNode cn = read(bytes);
        String desc = "(Lnet/minecraft/world/World;Lnet/minecraft/world/chunk/Chunk;IIIII"
                + "Ljava/util/Random;[[Lnet/minecraft/world/biome/BiomeGenBase;Ljava/util/Set;)Z";
        int patched = 0;
        for (MethodNode mn : cn.methods) {
            if (!desc.equals(mn.desc)) continue;
            mn.instructions.clear();
            mn.tryCatchBlocks.clear();
            mn.localVariables = null;
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
            mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 4));
            mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 5));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 9));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                    "flushPendingGTLayerOres",
                    "(Lnet/minecraft/world/World;Lnet/minecraft/world/chunk/Chunk;II"
                            + "[[Lnet/minecraft/world/biome/BiomeGenBase;)Z", false));
            mn.instructions.add(new InsnNode(Opcodes.IRETURN));
            mn.maxStack = 5;
            patched++;
        }
        if (patched == 0) throw new IllegalStateException("WorldgenStoneLayers.generate was not found");
        LOGGER.info("HTGT6 disabled GT6 post-generation stone scan; sparse layer ores remain enabled");
        return write(cn);
    }

    /**
     * Creates deterministic GT6 rock strata in the raw vanilla arrays. Caves,
     * ravines and structures run afterwards and therefore retain vanilla order.
     */
    public static void generateGTStoneLayersInChunkArray(net.minecraft.world.World world,
                                                          int chunkX, int chunkZ,
                                                          net.minecraft.block.Block[] blocks,
                                                          byte[] metadata,
                                                          net.minecraft.world.biome.BiomeGenBase[] biomes) {
        generateGTStoneLayersInChunkArray(world, chunkX, chunkZ, blocks, metadata, biomes,
                false, false, false);
    }

    /** Futura variant: understands its Z-major biome palette and deepslate host rock. */
    public static void generateGTFuturaStoneLayersInChunkArray(net.minecraft.world.World world,
                                                                int chunkX, int chunkZ,
                                                                net.minecraft.block.Block[] blocks,
                                                                byte[] metadata,
                                                                net.minecraft.world.biome.BiomeGenBase[] biomes) {
        generateGTStoneLayersInChunkArray(world, chunkX, chunkZ, blocks, metadata, biomes,
                true, false, true);
    }

    private static void generateGTStoneLayersInChunkArray(net.minecraft.world.World world,
                                                           int chunkX, int chunkZ,
                                                           net.minecraft.block.Block[] blocks,
                                                           byte[] metadata,
                                                           net.minecraft.world.biome.BiomeGenBase[] biomes,
                                                           boolean zMajorBiomes,
                                                           boolean replaceDeepslate,
                                                           boolean coherentFuturaLayers) {
        if (world == null || blocks == null || blocks.length < 65536
                || metadata == null || metadata.length < 65536
                || biomes == null || gregapi.worldgen.StoneLayer.LAYERS.isEmpty()) return;

        final int height = blocks.length / 256;
        if (height < 256 || height * 256 != blocks.length || metadata.length < blocks.length) return;

        final int layerCount = gregapi.worldgen.StoneLayer.LAYERS.size();
        final int minX = chunkX << 4;
        final int minZ = chunkZ << 4;
        gregtech.worldgen.NoiseGenerator noise = new gregtech.worldgen.NoiseGenerator(world);
        java.util.Random random = gregapi.util.WD.random(world, (long) chunkX, (long) chunkZ);
        java.util.List<PendingLayerOre> pending = new java.util.ArrayList<PendingLayerOre>();

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int column = localZ + localX * 16;
                int base = column * height;
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                // Futura receives the unmodified WorldChunkManager Z-major palette.
                // Keep the historical vanilla ASM path unchanged for Default worlds.
                int biomeIndex = zMajorBiomes ? localX + localZ * 16 : column;
                net.minecraft.world.biome.BiomeGenBase biome = biomeIndex < biomes.length
                        ? biomes[biomeIndex] : net.minecraft.world.biome.BiomeGenBase.plains;
                // GT6's cellular sampler is expensive (27 neighbouring cells per call).
                // Futura samples one representative per four-block vertical band. This
                // both guarantees actual strata instead of isolated Y cells and reduces
                // the sampler cost by roughly 75%. Default keeps the exact legacy path.
                gregapi.worldgen.StoneLayer[] futuraLayers = coherentFuturaLayers
                        ? new gregapi.worldgen.StoneLayer[height] : null;
                if (futuraLayers != null) for (int bandY = 0; bandY < height; bandY += 4) {
                    gregapi.worldgen.StoneLayer layer = gregapi.worldgen.StoneLayer.LAYERS.get(
                            noise.get((float)worldX, (float)Math.min(height-1, bandY + 2),
                                    (float)worldZ, layerCount));
                    for (int fill = bandY; fill < Math.min(height, bandY + 4); fill++) futuraLayers[fill] = layer;
                }
                gregapi.worldgen.StoneLayer[] scan = new gregapi.worldgen.StoneLayer[7];
                for (int i = 0; i < scan.length; i++) {
                    scan[i] = futuraLayers != null ? futuraLayers[Math.max(0, i - 2)]
                            : gregapi.worldgen.StoneLayer.LAYERS.get(
                            noise.get((float) worldX, (float) (i - 2), (float) worldZ, layerCount));
                }

                for (int y = 1; y < height; y++) {
                    int index = base + y;
                    net.minecraft.block.Block existing = blocks[index];
                    boolean protectedFuturaRock = coherentFuturaLayers && (existing
                            == com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.deepslate
                            || existing == com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.tuff);
                    boolean replaceable = !protectedFuturaRock && (existing == net.minecraft.init.Blocks.stone
                            || (replaceDeepslate && existing
                            == com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.deepslate)
                            || (existing == net.minecraft.init.Blocks.monster_egg && metadata[index] == 0)
                            || gregapi.worldgen.StoneLayer.REPLACEABLE_BLOCKS.contains(existing));
                    gregapi.worldgen.StoneLayer current = scan[3];
                    if (replaceable) {
                        PendingLayerOre ore = selectPendingLayerOre(current, scan, world, worldX, y,
                                worldZ, biome, random);
                        if (ore != null) pending.add(ore);
                        blocks[index] = current.mStone;
                        metadata[index] = current.mMetaStone;
                    }
                    for (int i = 1; i < scan.length; i++) scan[i - 1] = scan[i];
                    scan[6] = futuraLayers != null ? futuraLayers[Math.min(height-1, y + 4)]
                            : gregapi.worldgen.StoneLayer.LAYERS.get(
                            noise.get((float) worldX, (float) (y + 4), (float) worldZ, layerCount));
                }
            }
        }

        long key = chunkKey(chunkX, chunkZ);
        synchronized (PENDING_LAYER_ORES) {
            Map<Long, java.util.List<PendingLayerOre>> worldPending = PENDING_LAYER_ORES.get(world);
            if (worldPending == null) {
                worldPending = new ConcurrentHashMap<Long, java.util.List<PendingLayerOre>>();
                PENDING_LAYER_ORES.put(world, worldPending);
            }
            if (pending.isEmpty()) worldPending.remove(Long.valueOf(key));
            else worldPending.put(Long.valueOf(key), pending);
        }
    }

    private static PendingLayerOre selectPendingLayerOre(gregapi.worldgen.StoneLayer current,
                                                          gregapi.worldgen.StoneLayer[] scan,
                                                          net.minecraft.world.World world,
                                                          int x, int y, int z,
                                                          net.minecraft.world.biome.BiomeGenBase biome,
                                                          java.util.Random random) {
        java.util.List<gregapi.worldgen.StoneLayerOres> candidates;
        boolean uniform = scan[5] == scan[1];
        if (uniform) candidates = current.mOres;
        else candidates = gregapi.worldgen.StoneLayer.get(scan[5], scan[1]);
        for (gregapi.worldgen.StoneLayerOres ore : candidates) {
            if (!ore.check(current, world, x, y, z, biome, random)) continue;
            boolean small = uniform ? scan[6] != scan[0]
                    : (y == ore.mMinY || y == ore.mMaxY || random.nextBoolean());
            return new PendingLayerOre(current, ore, x, y, z, small);
        }
        return null;
    }

    public static boolean flushPendingGTLayerOres(net.minecraft.world.World world,
                                                   net.minecraft.world.chunk.Chunk chunk,
                                                   int minX, int minZ,
                                                   net.minecraft.world.biome.BiomeGenBase[][] biomes) {
        java.util.List<PendingLayerOre> pending = null;
        synchronized (PENDING_LAYER_ORES) {
            Map<Long, java.util.List<PendingLayerOre>> worldPending = PENDING_LAYER_ORES.get(world);
            if (worldPending != null) {
                pending = worldPending.remove(Long.valueOf(chunkKey(minX >> 4, minZ >> 4)));
                if (worldPending.isEmpty()) PENDING_LAYER_ORES.remove(world);
            }
        }
        if (pending == null || pending.isEmpty()) return false;
        boolean placed = false;
        for (PendingLayerOre ore : pending) {
            if (world.getBlock(ore.x, ore.y, ore.z) != ore.layer.mStone) continue;
            net.minecraft.world.biome.BiomeGenBase biome = biomes[ore.x & 15][ore.z & 15];
            placed |= ore.small
                    ? ore.ore.small(ore.layer, world, ore.x, ore.y, ore.z, biome)
                    : ore.ore.normal(ore.layer, world, ore.x, ore.y, ore.z, biome);
        }
        return placed;
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xffffffffL);
    }

    private static final class PendingLayerOre {
        private final gregapi.worldgen.StoneLayer layer;
        private final gregapi.worldgen.StoneLayerOres ore;
        private final int x;
        private final int y;
        private final int z;
        private final boolean small;

        private PendingLayerOre(gregapi.worldgen.StoneLayer layer,
                                gregapi.worldgen.StoneLayerOres ore,
                                int x, int y, int z, boolean small) {
            this.layer = layer;
            this.ore = ore;
            this.x = x;
            this.y = y;
            this.z = z;
            this.small = small;
        }
    }

    private static void replaceWithReturn(MethodNode method) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        method.localVariables = null;
        method.instructions.add(new InsnNode(Opcodes.RETURN));
        method.maxStack = 0;
    }

    private static void replaceWithFalse(MethodNode method) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        method.localVariables = null;
        method.instructions.add(new InsnNode(Opcodes.ICONST_0));
        method.instructions.add(new InsnNode(Opcodes.IRETURN));
        method.maxStack = 1;
    }

    private static void replaceWithInt(MethodNode method, int value) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        method.localVariables = null;
        if (value >= -1 && value <= 5) method.instructions.add(new InsnNode(Opcodes.ICONST_0 + value));
        else method.instructions.add(new IntInsnNode(Opcodes.SIPUSH, value));
        method.instructions.add(new InsnNode(Opcodes.IRETURN));
        method.maxStack = 1;
    }

    private static boolean isGTWaterBlock(String name, String transformedName) {
        return isClass(GT_WATER_BASE, name, transformedName)
                || isClass(GT_WATER_OCEAN, name, transformedName)
                || isClass(GT_WATER_RIVER, name, transformedName)
                || isClass(GT_WATER_SWAMP, name, transformedName);
    }

    private static boolean isGTWaterWorldgen(String name, String transformedName) {
        return isClass(GT_WORLDGEN_OCEAN, name, transformedName)
                || isClass(GT_WORLDGEN_RIVER, name, transformedName)
                || isClass(GT_WORLDGEN_SWAMP, name, transformedName);
    }

    private static String className(String name, String transformedName) {
        return transformedName != null && transformedName.length() > 0 ? transformedName : name;
    }

    /**
     * AmunRa 0.4.8's embedded manager refers to an old SRG biome field.  On
     * current ForgeGradle dev runtimes that makes dimension initialization
     * fail as a misleading ClassNotFoundException.  Keep the original world
     * providers and dimensions, but return the equivalent HTGT6 manager.
     */
    private static byte[] transformAmunRaWorldProvider(byte[] bytes) {
        ClassNode cn = read(bytes);
        boolean patched = false;
        for (MethodNode mn : cn.methods) {
            if (!("getWorldChunkManagerClass".equals(mn.name)
                    || "func_76567_e".equals(mn.name))
                    || !"()Ljava/lang/Class;".equals(mn.desc)) continue;
            for (AbstractInsnNode node = mn.instructions.getFirst(); node != null; node = node.getNext()) {
                if (node instanceof LdcInsnNode && ((LdcInsnNode) node).cst instanceof Type) {
                    Type type = (Type) ((LdcInsnNode) node).cst;
                    if (AMUN_RA_WORLD_CHUNK_MANAGER.equals(type.getInternalName())) {
                        ((LdcInsnNode) node).cst = Type.getObjectType(AMUN_RA_COMPAT_CHUNK_MANAGER);
                        patched = true;
                    }
                }
            }
        }
        return patched ? write(cn) : bytes;
    }

    /**
     * Keeps the embedded AmunRa compatible with HTGT6's configuration layout
     * and puts its complete Ra system in the fan-content Micro Galaxy before
     * Galacticraft registers any of its planets or moons.
     */
    private static byte[] transformAmunRaConfigPath(byte[] bytes) {
        ClassNode cn = read(bytes);
        boolean configPatched = false;
        boolean galaxyPatched = false;
        for (MethodNode mn : cn.methods) {
            for (AbstractInsnNode node = mn.instructions.getFirst(); node != null; node = node.getNext()) {
                if (node instanceof MethodInsnNode) {
                    MethodInsnNode call = (MethodInsnNode) node;
                    if ("preInit".equals(mn.name)
                            && "cpw/mods/fml/common/event/FMLPreInitializationEvent".equals(call.owner)
                            && "getSuggestedConfigurationFile".equals(call.name)
                            && "()Ljava/io/File;".equals(call.desc)) {
                        call.setOpcode(Opcodes.INVOKESTATIC);
                        call.owner = CONFIG;
                        call.name = "getAmunRaConfigFile";
                        call.desc = "(Lcpw/mods/fml/common/event/FMLPreInitializationEvent;)Ljava/io/File;";
                        call.itf = false;
                        configPatched = true;
                    }
                    if ("micdoodle8/mods/galacticraft/api/galaxies/SolarSystem".equals(call.owner)
                            && "<init>".equals(call.name)
                            && "(Ljava/lang/String;Ljava/lang/String;)V".equals(call.desc)) {
                        AbstractInsnNode previous = previousRealInstruction(call);
                        if (previous instanceof LdcInsnNode
                                && "milkyWay".equals(((LdcInsnNode) previous).cst)) {
                            ((LdcInsnNode) previous).cst = "Micro";
                            galaxyPatched = true;
                        }
                    }
                }
            }
        }
        if (!configPatched) throw new IllegalStateException("AmunRa config redirect was not applied");
        if (!galaxyPatched) throw new IllegalStateException("AmunRa Micro Galaxy redirect was not applied");
        LOGGER.info("Redirected AmunRa config to config/HTGT6/AmunRa.cfg and Ra system to Micro Galaxy");
        return write(cn);
    }

    private static AbstractInsnNode previousRealInstruction(AbstractInsnNode node) {
        AbstractInsnNode previous = node.getPrevious();
        while (previous != null && (previous.getType() == AbstractInsnNode.LABEL
                || previous.getType() == AbstractInsnNode.LINE
                || previous.getType() == AbstractInsnNode.FRAME)) {
            previous = previous.getPrevious();
        }
        return previous;
    }

    private static AbstractInsnNode nextRealInstruction(AbstractInsnNode node) {
        AbstractInsnNode next = node.getNext();
        while (next != null && (next.getType() == AbstractInsnNode.LABEL
                || next.getType() == AbstractInsnNode.LINE
                || next.getType() == AbstractInsnNode.FRAME)) {
            next = next.getNext();
        }
        return next;
    }

    /**
     * AmunRa's original nanotool calls Minecraft.getMinecraft() from server
     * methods. A player can reach that path by cycling to wrench/workbench,
     * which crashes a dedicated server. Also guard its untrusted NBT mode.
     */
    private static byte[] transformAmunRaNanotool(byte[] bytes) {
        ClassNode cn = read(bytes);
        boolean modePatched = false;
        boolean rightClickPatched = false;
        boolean bypassPatched = false;

        for (MethodNode mn : cn.methods) {
            if ("getMode".equals(mn.name)
                    && "(Lnet/minecraft/item/ItemStack;)Lde/katzenpapst/amunra/item/ItemNanotool$Mode;".equals(mn.desc)) {
                rewriteNanotoolGetMode(mn);
                modePatched = true;
            } else if (("func_77659_a".equals(mn.name) || "onItemRightClick".equals(mn.name))
                    && "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;".equals(mn.desc)) {
                rewriteNanotoolRightClick(mn);
                rightClickPatched = true;
            } else if ("doesSneakBypassUse".equals(mn.name)
                    && "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/player/EntityPlayer;)Z".equals(mn.desc)) {
                rewriteNanotoolSneakBypass(mn);
                bypassPatched = true;
            }
        }

        if (!modePatched || !rightClickPatched || !bypassPatched) {
            throw new IllegalStateException("AmunRa nanotool hardening was not fully applied");
        }
        LOGGER.info("Hardened AmunRa nanotool against server-side mode and NBT crashes");
        return write(cn);
    }

    private static void rewriteNanotoolGetMode(MethodNode mn) {
        LabelNode valid = new LabelNode();
        LabelNode fallback = new LabelNode();
        InsnList code = new InsnList();
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, AMUN_RA_NANOTOOL, "getModeInt",
                "(Lnet/minecraft/item/ItemStack;)I", false));
        code.add(new VarInsnNode(Opcodes.ISTORE, 2));
        code.add(new VarInsnNode(Opcodes.ILOAD, 2));
        code.add(new JumpInsnNode(Opcodes.IFLT, fallback));
        code.add(new VarInsnNode(Opcodes.ILOAD, 2));
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, AMUN_RA_NANOTOOL + "$Mode", "values",
                "()[Lde/katzenpapst/amunra/item/ItemNanotool$Mode;", false));
        code.add(new InsnNode(Opcodes.ARRAYLENGTH));
        code.add(new JumpInsnNode(Opcodes.IF_ICMPLT, valid));
        code.add(fallback);
        code.add(new FieldInsnNode(Opcodes.GETSTATIC, AMUN_RA_NANOTOOL + "$Mode", "WORKBENCH",
                "Lde/katzenpapst/amunra/item/ItemNanotool$Mode;"));
        code.add(new InsnNode(Opcodes.ARETURN));
        code.add(valid);
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, AMUN_RA_NANOTOOL + "$Mode", "values",
                "()[Lde/katzenpapst/amunra/item/ItemNanotool$Mode;", false));
        code.add(new VarInsnNode(Opcodes.ILOAD, 2));
        code.add(new InsnNode(Opcodes.AALOAD));
        code.add(new InsnNode(Opcodes.ARETURN));
        replaceMethodBody(mn, code, 2, 3);
    }

    private static void rewriteNanotoolRightClick(MethodNode mn) {
        final String modeDesc = "Lde/katzenpapst/amunra/item/ItemNanotool$Mode;";
        final String modeMethod = "(Lnet/minecraft/item/ItemStack;)" + modeDesc;
        final String energyMethod = "(Lnet/minecraft/item/ItemStack;F)Z";
        final String energyModeMethod = "(Lnet/minecraft/item/ItemStack;F" + modeDesc + ")Z";
        LabelNode openWorkbench = new LabelNode();
        LabelNode noEnergyToSwitch = new LabelNode();
        LabelNode done = new LabelNode();
        LabelNode fallback = new LabelNode();
        InsnList code = new InsnList();
        code.add(new VarInsnNode(Opcodes.ALOAD, 3));
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF, "isPlayerSneaking",
                "(Lnet/minecraft/entity/player/EntityPlayer;)Z", false));
        code.add(new JumpInsnNode(Opcodes.IFEQ, openWorkbench));
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new LdcInsnNode(Float.valueOf(60.0F)));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, AMUN_RA_NANOTOOL, "hasEnoughEnergy", energyMethod, false));
        code.add(new JumpInsnNode(Opcodes.IFEQ, noEnergyToSwitch));
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, AMUN_RA_NANOTOOL, "getMode", modeMethod, false));
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new InsnNode(Opcodes.SWAP));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, AMUN_RA_NANOTOOL, "getNextMode", "(" + modeDesc + ")" + modeDesc, false));
        code.add(new VarInsnNode(Opcodes.ASTORE, 4));
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new VarInsnNode(Opcodes.ALOAD, 3));
        code.add(new LdcInsnNode(Float.valueOf(60.0F)));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, AMUN_RA_NANOTOOL, "consumePower",
                "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;F)V", false));
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new VarInsnNode(Opcodes.ALOAD, 4));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, AMUN_RA_NANOTOOL, "setMode",
                "(Lnet/minecraft/item/ItemStack;" + modeDesc + ")V", false));
        code.add(new JumpInsnNode(Opcodes.GOTO, done));
        code.add(noEnergyToSwitch);
        code.add(new JumpInsnNode(Opcodes.GOTO, done));
        code.add(openWorkbench);
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new LdcInsnNode(Float.valueOf(40.0F)));
        code.add(new FieldInsnNode(Opcodes.GETSTATIC, AMUN_RA_NANOTOOL + "$Mode", "WORKBENCH", modeDesc));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, AMUN_RA_NANOTOOL, "hasEnoughEnergyAndMode", energyModeMethod, false));
        code.add(new JumpInsnNode(Opcodes.IFEQ, fallback));
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new VarInsnNode(Opcodes.ALOAD, 3));
        code.add(new LdcInsnNode(Float.valueOf(40.0F)));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, AMUN_RA_NANOTOOL, "consumePower",
                "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;F)V", false));
        code.add(new VarInsnNode(Opcodes.ALOAD, 2));
        code.add(new VarInsnNode(Opcodes.ALOAD, 3));
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF, "openAmunRaWorkbench",
                "(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)V", false));
        code.add(new JumpInsnNode(Opcodes.GOTO, done));
        code.add(fallback);
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new VarInsnNode(Opcodes.ALOAD, 2));
        code.add(new VarInsnNode(Opcodes.ALOAD, 3));
        code.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "de/katzenpapst/amunra/item/ItemAbstractBatteryUser", mn.name, mn.desc, false));
        code.add(new InsnNode(Opcodes.ARETURN));
        code.add(done);
        code.add(new VarInsnNode(Opcodes.ALOAD, 1));
        code.add(new InsnNode(Opcodes.ARETURN));
        replaceMethodBody(mn, code, 8, 5);
    }

    private static void rewriteNanotoolSneakBypass(MethodNode mn) {
        InsnList code = new InsnList();
        code.add(new VarInsnNode(Opcodes.ALOAD, 0));
        code.add(new VarInsnNode(Opcodes.ALOAD, 5));
        code.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF, "getHeldItem",
                "(Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;", false));
        code.add(new LdcInsnNode(Float.valueOf(20.0F)));
        code.add(new FieldInsnNode(Opcodes.GETSTATIC, AMUN_RA_NANOTOOL + "$Mode", "WRENCH", "Lde/katzenpapst/amunra/item/ItemNanotool$Mode;"));
        code.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, AMUN_RA_NANOTOOL, "hasEnoughEnergyAndMode",
                "(Lnet/minecraft/item/ItemStack;FLde/katzenpapst/amunra/item/ItemNanotool$Mode;)Z", false));
        code.add(new InsnNode(Opcodes.IRETURN));
        replaceMethodBody(mn, code, 3, 6);
    }

    /** Resolve MCP/SRG members at runtime, so the embedded 1.7.10 bytecode works in both dev and production. */
    public static boolean isPlayerSneaking(net.minecraft.entity.player.EntityPlayer player) {
        return (Boolean) invokeNamed(player, new String[] { "isSneaking", "func_70093_af" }, new Class[0]);
    }

    public static net.minecraft.item.ItemStack getHeldItem(net.minecraft.entity.player.EntityPlayer player) {
        Object inventory = getField(player, "inventory", "field_71071_by");
        if (inventory == null) return null;
        Object item = invokeNamed(inventory, new String[] { "getCurrentItem", "func_70448_g" }, new Class[0]);
        return item instanceof net.minecraft.item.ItemStack ? (net.minecraft.item.ItemStack) item : null;
    }

    public static void openAmunRaWorkbench(net.minecraft.world.World world,
                                            net.minecraft.entity.player.EntityPlayer player) {
        try {
            Class amunRa = Class.forName("de.katzenpapst.amunra.AmunRa");
            Object instance = amunRa.getField("instance").get(null);
            int x = ((Number) getField(player, "posX", "field_70165_t")).intValue();
            int y = ((Number) getField(player, "posY", "field_70163_u")).intValue();
            int z = ((Number) getField(player, "posZ", "field_70161_v")).intValue();
            invokeNamed(player, new String[] { "openGui", "func_71007_a" },
                    new Class[] { Object.class, Integer.TYPE, net.minecraft.world.World.class,
                            Integer.TYPE, Integer.TYPE, Integer.TYPE },
                    instance, Integer.valueOf(6), world, Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(z));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to open AmunRa nanotool workbench", e);
        }
    }

    private static Object invokeNamed(Object target, String[] names, Class[] parameterTypes, Object... arguments) {
        if (target == null) return Boolean.FALSE;
        for (String name : names) {
            try {
                java.lang.reflect.Method method = target.getClass().getMethod(name, parameterTypes);
                return method.invoke(target, arguments);
            } catch (ReflectiveOperationException ignored) { }
        }
        throw new IllegalStateException("Missing compatible method on " + target.getClass().getName());
    }

    private static void replaceMethodBody(MethodNode method, InsnList code, int maxStack, int maxLocals) {
        method.instructions.clear();
        method.tryCatchBlocks.clear();
        method.localVariables = null;
        method.instructions.add(code);
        method.maxStack = maxStack;
        method.maxLocals = maxLocals;
    }

    /* --------------------------------------------------------------------- */
    /* BiomeGenBase                                                          */
    /* --------------------------------------------------------------------- */

    private static byte[] transformBiomeGenBase(byte[] bytes) {
        ClassNode cn = read(bytes);

        boolean constructorPatched = false;
        boolean getBiomePatched = false;

        for (MethodNode mn : cn.methods) {
            // Match the constructor by its bytecode, not only by descriptor.
            // 1.7.10 has both (I)V and (IZ)V constructors.
            if ("<init>".equals(mn.name) && replaceBiomeArrayStore(mn)) {
                constructorPatched = true;
            }

            // MCP: getBiome(I), SRG: func_150568_d(I)
            if ("(I)Lnet/minecraft/world/biome/BiomeGenBase;".equals(mn.desc)
                    && ("getBiome".equals(mn.name) || "func_150568_d".equals(mn.name))) {
                mn.instructions.clear();
                mn.tryCatchBlocks.clear();
                mn.localVariables = null;
                mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0));
                mn.instructions.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        SELF,
                        "getExtendedBiome",
                        "(I)Lnet/minecraft/world/biome/BiomeGenBase;",
                        false));
                mn.instructions.add(new org.objectweb.asm.tree.InsnNode(Opcodes.ARETURN));
                mn.maxStack = 1;
                mn.maxLocals = 1;
                getBiomePatched = true;
            }
        }

        if (!constructorPatched) {
            throw new IllegalStateException(
                    "BiomeGenBase constructor biomeList store was NOT found. " +
                            "The class reached HTGT6Transformer but its bytecode layout/name mapping differs.");
        }

        if (!getBiomePatched) {
            LOGGER.warn("HTGT6: BiomeGenBase.getBiome(int) was not patched; check MCP/SRG names.");
        }

        LOGGER.info(
                "HTGT6 transformed BiomeGenBase: constructorPatched={}, getBiomePatched={}, <clinit> untouched",
                constructorPatched, getBiomePatched);

        return write(cn);
    }

    /**
     * Vanilla constructor contains:
     *   GETSTATIC biomeList
     *   ILOAD 1
     *   ALOAD 0
     *   AASTORE
     *
     * Replace that store with registerBiome(id, this).
     */
    private static boolean replaceBiomeArrayStore(MethodNode mn) {
        AbstractInsnNode[] insns = mn.instructions.toArray();

        for (int i = 0; i < insns.length; i++) {
            AbstractInsnNode n = insns[i];

            if (!(n instanceof org.objectweb.asm.tree.FieldInsnNode)
                    || n.getOpcode() != Opcodes.GETSTATIC) {
                continue;
            }

            org.objectweb.asm.tree.FieldInsnNode field =
                    (org.objectweb.asm.tree.FieldInsnNode) n;

            // Match the actual BiomeGenBase biome registry field.
            if (!BIOME.equals(field.owner)) {
                continue;
            }

            if (!"biomeList".equals(field.name)
                    && !"field_76773_a".equals(field.name)) {
                continue;
            }

            if (!"[Lnet/minecraft/world/biome/BiomeGenBase;".equals(field.desc)) {
                continue;
            }

            /*
             * We now search forward instead of assuming:
             *
             * GETSTATIC
             * ILOAD
             * ALOAD
             * AASTORE
             *
             * are consecutive.
             *
             * Actual 1.7.10 bytecode:
             *
             * ILOAD 2
             * IFEQ label
             * GETSTATIC biomeList
             * ILOAD 1
             * ALOAD 0
             * AASTORE
             */
            int idVar = -1;
            VarInsnNode idLoad = null;
            VarInsnNode selfLoad = null;
            AbstractInsnNode aastor = null;

            for (int j = i + 1; j < Math.min(i + 12, insns.length); j++) {
                AbstractInsnNode x = insns[j];

                if (x.getOpcode() == Opcodes.ILOAD
                        && x instanceof VarInsnNode) {

                    VarInsnNode v = (VarInsnNode) x;

                    /*
                     * biomeID is local 1 in:
                     *
                     * BiomeGenBase(int, boolean)
                     */
                    if (idVar == -1) {
                        idVar = v.var;
                        idLoad = v;
                        continue;
                    }
                }

                if (x.getOpcode() == Opcodes.ALOAD
                        && x instanceof VarInsnNode
                        && ((VarInsnNode) x).var == 0) {

                    selfLoad = (VarInsnNode) x;
                    continue;
                }

                if (x.getOpcode() == Opcodes.AASTORE) {
                    aastor = x;
                    break;
                }
            }

            if (idLoad == null || selfLoad == null || aastor == null) {
                continue;
            }

            /*
             * Replace only:
             *
             * GETSTATIC biomeList
             * ILOAD biomeID
             * ALOAD this
             * AASTORE
             *
             * with:
             *
             * ILOAD biomeID
             * ALOAD this
             * INVOKESTATIC registerBiome
             */
            InsnList replacement = new InsnList();

            replacement.add(
                    new VarInsnNode(
                            Opcodes.ILOAD,
                            idVar));

            replacement.add(
                    new VarInsnNode(
                            Opcodes.ALOAD,
                            0));

            replacement.add(
                    new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            SELF,
                            "registerBiome",
                            "(ILnet/minecraft/world/biome/BiomeGenBase;)V",
                            false));

            /*
             * Insert before GETSTATIC.
             */
            mn.instructions.insertBefore(n, replacement);

            /*
             * Remove the four original array-store instructions.
             */
            mn.instructions.remove(n);
            mn.instructions.remove(idLoad);
            mn.instructions.remove(selfLoad);
            mn.instructions.remove(aastor);

            LOGGER.info(
                    "HTGT6: PATCHED BiomeGenBase constructor {} " +
                            "biomeList[local {}] -> registerBiome()",
                    mn.desc,
                    idVar);

            return true;
        }

        return false;
    }

    /** Called from transformed BiomeGenBase constructor. */
    public static void registerBiome(int id, net.minecraft.world.biome.BiomeGenBase biome) {
        if (id >= 0 && id < 256) {
            try {
                net.minecraft.world.biome.BiomeGenBase[] list =
                        net.minecraft.world.biome.BiomeGenBase.getBiomeGenArray();
                if (id < list.length) {
                    list[id] = biome;
                    return;
                }
            } catch (Throwable t) {
                LOGGER.error("Could not register legacy biome " + id, t);
            }
        }

        if (id >= 0) {
            EXTENDED_BIOMES.put(Integer.valueOf(id), biome);
        }
    }

    /** Called from transformed BiomeGenBase.getBiome(int). */
    public static net.minecraft.world.biome.BiomeGenBase getExtendedBiome(int id) {
        if (id >= 0) {
            try {
                net.minecraft.world.biome.BiomeGenBase[] list =
                        net.minecraft.world.biome.BiomeGenBase.getBiomeGenArray();
                if (id < list.length) {
                    net.minecraft.world.biome.BiomeGenBase b = list[id];
                    if (b != null) return b;
                }
            } catch (Throwable ignored) {
                // Fall through to extended map / ocean.
            }

            Object b = EXTENDED_BIOMES.get(Integer.valueOf(id));
            if (b instanceof net.minecraft.world.biome.BiomeGenBase) {
                return (net.minecraft.world.biome.BiomeGenBase) b;
            }
        }

        return net.minecraft.world.biome.BiomeGenBase.ocean;
    }

    /** Exact registry lookup used while reserving configurable extended IDs. */
    public static net.minecraft.world.biome.BiomeGenBase getRegisteredBiomeExact(int id) {
        if (id < 0) return null;
        if (id < 256) {
            try {
                net.minecraft.world.biome.BiomeGenBase[] list =
                        net.minecraft.world.biome.BiomeGenBase.getBiomeGenArray();
                return id < list.length ? list[id] : null;
            } catch (Throwable ignored) {
                return null;
            }
        }
        Object biome = EXTENDED_BIOMES.get(Integer.valueOf(id));
        return biome instanceof net.minecraft.world.biome.BiomeGenBase
                ? (net.minecraft.world.biome.BiomeGenBase) biome : null;
    }

    public static java.util.List<net.minecraft.world.biome.BiomeGenBase> getRegisteredBiomesSnapshot120() {
        java.util.LinkedHashMap<Integer,net.minecraft.world.biome.BiomeGenBase> result =
                new java.util.LinkedHashMap<Integer,net.minecraft.world.biome.BiomeGenBase>();
        net.minecraft.world.biome.BiomeGenBase[] vanilla =
                net.minecraft.world.biome.BiomeGenBase.getBiomeGenArray();
        if (vanilla != null) for (net.minecraft.world.biome.BiomeGenBase biome : vanilla)
            if (biome != null) result.put(Integer.valueOf(biome.biomeID), biome);
        for (Map.Entry<Integer,Object> entry : EXTENDED_BIOMES.entrySet())
            if (entry.getValue() instanceof net.minecraft.world.biome.BiomeGenBase)
                result.put(entry.getKey(), (net.minecraft.world.biome.BiomeGenBase) entry.getValue());
        return new java.util.ArrayList<net.minecraft.world.biome.BiomeGenBase>(result.values());
    }

    /* --------------------------------------------------------------------- */
    /* World/Chunk Futura interaction bounds                                 */
    /* --------------------------------------------------------------------- */

    private static byte[] transformWorldInteractionHeight120(byte[] bytes) {
        ClassNode cn = read(bytes);
        int patched = 0;
        int lightingHeight = 0;
        int lightingMaxY = 0;
        for (MethodNode mn : cn.methods) {
            // Preserve the original failure if reporting itself fails to link.
            for (AbstractInsnNode instruction : mn.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (call.getOpcode() == Opcodes.INVOKESTATIC && CRASH_REPORT.equals(call.owner)
                        && ("makeCrashReport".equals(call.name) || "func_85055_a".equals(call.name))
                        && "(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/crash/CrashReport;".equals(call.desc)) {
                    InsnList log = new InsnList();
                    log.add(new InsnNode(Opcodes.DUP2));
                    log.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                            "logOriginalWorldFailure120", "(Ljava/lang/Throwable;Ljava/lang/String;)V", false));
                    mn.instructions.insertBefore(call, log);
                }
            }
            if (isWorldInteractionMethod120(mn)) {
                int methodPatches = replaceHeightConstants120(mn, null);
                if (methodPatches != 1)
                    throw new IllegalStateException("World interaction height matcher drift in "
                            + mn.name + mn.desc + ": " + methodPatches);
                patched += methodPatches;
            }
            if (isWorldLightBoundsMethod120(mn)) {
                lightingHeight += replaceLightingConstant120(mn, 256, "worldLightingHeight120");
                lightingMaxY += replaceLightingConstant120(mn, 255, "worldLightingMaxY120");
            }
        }
        if (patched != 7)
            throw new IllegalStateException("World interaction height seams patched=" + patched + ", expected=7");
        // The vanilla client jar contains one additional World lighting path
        // that is stripped from the dedicated-server jar. Both profiles are
        // valid; every remaining 256/255 bound must still be accounted for.
        if ((lightingHeight != 5 && lightingHeight != 4) || lightingMaxY != 3)
            throw new IllegalStateException("World lighting matcher drift: height=" + lightingHeight
                    + ", maxY=" + lightingMaxY + ", expected=4-5/3");
        LOGGER.info("HTGT6 installed Futura-only World interaction and lighting height seams");
        return write(cn);
    }

    public static void logOriginalWorldFailure120(Throwable failure, String context) {
        LOGGER.error("Original world failure before CrashReport: " + context, failure);
    }

    private static boolean isWorldLightBoundsMethod120(MethodNode mn) {
        String n = mn.name;
        String d = mn.desc;
        return (("getFullBlockLightValue".equals(n) || "func_72883_k".equals(n))
                    && "(III)I".equals(d))
                || (("getBlockLightValue_do".equals(n) || "func_72849_a".equals(n))
                    && "(IIIZ)I".equals(d))
                || (("getSkyBlockTypeBrightness".equals(n)
                        || "func_72810_a".equals(n)
                        || "func_72925_a".equals(n))
                    && "(Lnet/minecraft/world/EnumSkyBlock;III)I".equals(d))
                || (("getSavedLightValue".equals(n) || "func_72972_b".equals(n))
                    && "(Lnet/minecraft/world/EnumSkyBlock;III)I".equals(d))
                || (("setLightValue".equals(n) || "func_72915_b".equals(n))
                    && "(Lnet/minecraft/world/EnumSkyBlock;IIII)V".equals(d));
    }

    private static int replaceLightingConstant120(MethodNode mn, int value, String helper) {
        int patched = 0;
        for (AbstractInsnNode node : mn.instructions.toArray()) {
            if (integerConstant120(node) != value) continue;
            InsnList replacement = new InsnList();
            replacement.add(new VarInsnNode(Opcodes.ALOAD, 0));
            replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF, helper,
                    "(Lnet/minecraft/world/World;)I", false));
            mn.instructions.insertBefore(node, replacement);
            mn.instructions.remove(node);
            patched++;
        }
        return patched;
    }

    public static int worldLightingHeight120(net.minecraft.world.World world) {
        return com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.lightingHeight(world);
    }

    public static int worldLightingMaxY120(net.minecraft.world.World world) {
        return worldLightingHeight120(world)-1;
    }

    private static byte[] transformColumnPlant120(byte[] bytes,boolean cactus) {
        ClassNode cn=read(bytes);String helper=cactus?"cactusSurvival120":"caneSurvival120";int changes=0;
        for(MethodNode m:cn.methods) {
            boolean method=cactus?(m.name.equals("canBlockStay")||m.name.equals("func_149718_j")):
                    (m.name.equals("canPlaceBlockAt")||m.name.equals("func_149742_c"));
            if(!method||!m.desc.equals("(Lnet/minecraft/world/World;III)Z"))continue;
            boolean done=false;for(AbstractInsnNode n:m.instructions.toArray())
                if(n instanceof MethodInsnNode&&((MethodInsnNode)n).name.equals(helper))done=true;
            if(done)continue;
            for(AbstractInsnNode n:m.instructions.toArray())if(n.getOpcode()==Opcodes.IRETURN) {
                InsnList call=new InsnList();call.add(new VarInsnNode(Opcodes.ALOAD,1));
                for(int i=2;i<=4;i++)call.add(new VarInsnNode(Opcodes.ILOAD,i));
                call.add(new MethodInsnNode(Opcodes.INVOKESTATIC,SELF,helper,"(ZLnet/minecraft/world/World;III)Z",false));
                m.instructions.insertBefore(n,call);changes++;
            }
        }
        return changes==0?bytes:write(cn);
    }
    public static boolean cactusSurvival120(boolean legacy,net.minecraft.world.World world,int x,int y,int z) {
        return com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaColumnPlantSurvival120.cactus(legacy,world,x,y,z);
    }
    public static boolean caneSurvival120(boolean legacy,net.minecraft.world.World world,int x,int y,int z) {
        return com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaColumnPlantSurvival120.cane(legacy,world,x,y,z);
    }

    /** Adapt the ceiling and lily substrate predicate; preserve mushroom/Forge survival checks. */
    private static byte[] transformPlantSurvivalHeight120(byte[] bytes) {
        ClassNode cn=read(bytes);
        int changed=0;
        for (MethodNode method:cn.methods) {
            if (!("canBlockStay".equals(method.name) || "func_149718_j".equals(method.name))
                    || !"(Lnet/minecraft/world/World;III)Z".equals(method.desc)) continue;
            for (AbstractInsnNode node:method.instructions.toArray()) {
                if (!(node instanceof IntInsnNode) || node.getOpcode()!=Opcodes.SIPUSH
                        || ((IntInsnNode)node).operand!=256) continue;
                InsnList height=new InsnList();
                height.add(new VarInsnNode(Opcodes.ALOAD,1));
                height.add(new MethodInsnNode(Opcodes.INVOKESTATIC,SELF,"interactionHeight120",
                        "(Lnet/minecraft/world/World;)I",false));
                method.instructions.insertBefore(node,height);
                method.instructions.remove(node);
                changed++;
            }
        }
        if (cn.name.equals("net/minecraft/block/BlockLilyPad")) {
            for (MethodNode method:cn.methods) {
                if (!("canBlockStay".equals(method.name)||"func_149718_j".equals(method.name))
                        || !"(Lnet/minecraft/world/World;III)Z".equals(method.desc)) continue;
                boolean wrapped=false;
                for(AbstractInsnNode n:method.instructions.toArray())
                    if(n instanceof MethodInsnNode && ((MethodInsnNode)n).name.equals("lilySurvival120"))wrapped=true;
                if(wrapped)continue;
                for(AbstractInsnNode n:method.instructions.toArray())if(n.getOpcode()==Opcodes.IRETURN) {
                    InsnList call=new InsnList();call.add(new VarInsnNode(Opcodes.ALOAD,1));
                    for(int local=2;local<=4;local++)call.add(new VarInsnNode(Opcodes.ILOAD,local));
                    call.add(new MethodInsnNode(Opcodes.INVOKESTATIC,SELF,"lilySurvival120",
                            "(ZLnet/minecraft/world/World;III)Z",false));
                    method.instructions.insertBefore(n,call);changed++;
                }
            }
        }
        if(cn.name.equals("net/minecraft/block/BlockLilyPad")) {
            boolean hasPlacement=false,srg=false;
            for(MethodNode m:cn.methods) {
                srg|=m.name.equals("func_149718_j");
                hasPlacement|=m.name.equals("canPlaceBlockAt")||m.name.equals("func_149742_c");
                if(!(m.name.equals("canPlaceBlockOn")||m.name.equals("func_149854_a"))
                        || !m.desc.equals("(Lnet/minecraft/block/Block;)Z"))continue;
                boolean done=false;for(AbstractInsnNode n:m.instructions.toArray())
                    if(n instanceof MethodInsnNode&&((MethodInsnNode)n).name.equals("lilySoil120"))done=true;
                if(!done)for(AbstractInsnNode n:m.instructions.toArray())if(n.getOpcode()==Opcodes.IRETURN) {
                    InsnList call=new InsnList();call.add(new VarInsnNode(Opcodes.ALOAD,1));
                    call.add(new MethodInsnNode(Opcodes.INVOKESTATIC,SELF,"lilySoil120","(ZLnet/minecraft/block/Block;)Z",false));
                    m.instructions.insertBefore(n,call);changed++;
                }
            }
            if(!hasPlacement) {
                String name=srg?"func_149742_c":"canPlaceBlockAt";
                MethodNode m=new MethodNode(Opcodes.ACC_PUBLIC,name,"(Lnet/minecraft/world/World;III)Z",null,null);
                m.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));m.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));
                for(int i=2;i<=4;i++)m.instructions.add(new VarInsnNode(Opcodes.ILOAD,i));
                m.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,cn.superName,name,m.desc,false));
                m.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));for(int i=2;i<=4;i++)m.instructions.add(new VarInsnNode(Opcodes.ILOAD,i));
                m.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,SELF,"lilyCanPlace120","(ZLnet/minecraft/world/World;III)Z",false));
                m.instructions.add(new InsnNode(Opcodes.IRETURN));cn.methods.add(m);changed++;
            }
        }
        if (changed==0) return bytes;
        LOGGER.info("HTGT6 adapted plant survival in {} ({} edits)",cn.name,changed);
        return write(cn);
    }

    public static boolean lilySoil120(boolean legacy,net.minecraft.block.Block block) {
        return legacy||(HTGT6Config.RUN_MINECRAFT_1_7_9999&&block instanceof net.minecraft.block.BlockIce);
    }
    public static boolean lilyCanPlace120(boolean legacy,net.minecraft.world.World world,int x,int y,int z) {
        return legacy&&y>=0&&y<interactionHeight120(world)&&lilySurvival120(legacy,world,x,y,z);
    }

    public static boolean lilySurvival120(boolean legacy,net.minecraft.world.World world,int x,int y,int z) {
        if(!HTGT6Config.RUN_MINECRAFT_1_7_9999)return legacy;
        if(y<0||y>=interactionHeight120(world))return false;
        net.minecraft.block.Block below=world.getBlock(x,y-1,z);
        boolean supported=below instanceof net.minecraft.block.BlockIce
                || below.getMaterial()==net.minecraft.block.material.Material.water
                && world.getBlockMetadata(x,y-1,z)==0;
        return supported&&!world.getBlock(x,y,z).getMaterial().isLiquid();
    }

    /** Preserve ItemLilyPad's permission checks, Forge snapshot/cancellation and consumption. */
    private static byte[] transformLilyPlacement120(byte[] bytes) {
        ClassNode cn=read(bytes);int changed=0;
        for(MethodNode m:cn.methods) {
            if(!("onItemRightClick".equals(m.name)||"func_77659_a".equals(m.name)))continue;
            for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof MethodInsnNode) {
                MethodInsnNode call=(MethodInsnNode)n;String helper=null,desc=null;
                if(call.owner.equals("net/minecraft/block/Block") && (call.name.equals("getMaterial")||call.name.equals("func_149688_o"))) {
                    helper="lilyPlacementMaterial120";desc="(Lnet/minecraft/block/Block;)Lnet/minecraft/block/material/Material;";
                } else if(call.owner.equals(WORLD) && (call.name.equals("getBlockMetadata")||call.name.equals("func_72805_g"))) {
                    helper="lilyPlacementMetadata120";desc="(Lnet/minecraft/world/World;III)I";
                } else if(call.owner.equals(WORLD) && (call.name.equals("isAirBlock")||call.name.equals("func_147437_c"))) {
                    helper="lilyPlacementAir120";desc="(Lnet/minecraft/world/World;III)Z";
                }
                if(helper!=null){m.instructions.set(n,new MethodInsnNode(Opcodes.INVOKESTATIC,SELF,helper,desc,false));changed++;}
            }
        }
        return changed==0?bytes:write(cn);
    }
    public static net.minecraft.block.material.Material lilyPlacementMaterial120(net.minecraft.block.Block block) {
        return HTGT6Config.RUN_MINECRAFT_1_7_9999&&block instanceof net.minecraft.block.BlockIce
                ?net.minecraft.block.material.Material.water:block.getMaterial();
    }
    public static int lilyPlacementMetadata120(net.minecraft.world.World world,int x,int y,int z) {
        return HTGT6Config.RUN_MINECRAFT_1_7_9999&&world.getBlock(x,y,z) instanceof net.minecraft.block.BlockIce
                ?0:world.getBlockMetadata(x,y,z);
    }
    public static boolean lilyPlacementAir120(net.minecraft.world.World world,int x,int y,int z) {
        return y>=0&&y<interactionHeight120(world)&&world.isAirBlock(x,y,z);
    }

    private static boolean isWorldInteractionMethod120(MethodNode mn) {
        String n = mn.name;
        String d = mn.desc;
        return (("getBlock".equals(n) || "func_147439_a".equals(n))
                    && "(III)Lnet/minecraft/block/Block;".equals(d))
                || (("blockExists".equals(n) || "func_72899_e".equals(n)) && "(III)Z".equals(d))
                || (("checkChunksExist".equals(n) || "func_72904_c".equals(n)) && "(IIIIII)Z".equals(d))
                || (("setBlock".equals(n) || "func_147465_d".equals(n))
                    && "(IIILnet/minecraft/block/Block;II)Z".equals(d))
                || (("getBlockMetadata".equals(n) || "func_72805_g".equals(n)) && "(III)I".equals(d))
                || (("setBlockMetadataWithNotify".equals(n) || "func_72921_c".equals(n))
                    && "(IIIII)Z".equals(d))
                || (("getTileEntity".equals(n) || "func_147438_o".equals(n))
                    && "(III)Lnet/minecraft/tileentity/TileEntity;".equals(d));
    }

    private static int replaceHeightConstants120(MethodNode mn, String chunkWorldField) {
        int patched = 0;
        for (AbstractInsnNode node : mn.instructions.toArray()) {
            if (!(node instanceof IntInsnNode) || node.getOpcode() != Opcodes.SIPUSH) continue;
            int value = ((IntInsnNode) node).operand;
            if (value != 256 && !(chunkWorldField != null && value == 255)) continue;
            InsnList replacement = new InsnList();
            replacement.add(new VarInsnNode(Opcodes.ALOAD, 0));
            if (chunkWorldField != null) replacement.add(new FieldInsnNode(Opcodes.GETFIELD,
                    "net/minecraft/world/chunk/Chunk", chunkWorldField, "Lnet/minecraft/world/World;"));
            replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF, "interactionHeight120",
                    "(Lnet/minecraft/world/World;)I", false));
            if (value == 255) {
                replacement.add(new InsnNode(Opcodes.ICONST_1));
                replacement.add(new InsnNode(Opcodes.ISUB));
            }
            mn.instructions.insertBefore(node, replacement);
            mn.instructions.remove(node);
            patched++;
        }
        return patched;
    }

    public static int interactionHeight120(net.minecraft.world.World world) {
        return com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.interactionHeight(world);
    }

    /* --------------------------------------------------------------------- */
    /* Chunk                                                                  */
    /* --------------------------------------------------------------------- */

    private static byte[] transformChunk(byte[] bytes) {
        ClassNode cn = read(bytes);
        String interactionWorldField = null;
        for (FieldNode field : cn.fields) if ("Lnet/minecraft/world/World;".equals(field.desc)) {
            interactionWorldField = field.name; break;
        }
        if (interactionWorldField == null) throw new IllegalStateException("Chunk world field not found");

        int storageAllocations = 0;
        int entityListAllocations = 0;
        int lightingPatches = 0;
        int rawArrayIndexPatches = 0;
        int storageGetterPatches = 0;

        // Add a second array instead of changing the public byte[] API.
        boolean exists = false;
        for (FieldNode fn : cn.fields) {
            if ("htgt6ExtendedBiomeArray".equals(fn.name)) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            cn.fields.add(new FieldNode(
                    Opcodes.ACC_PRIVATE,
                    "htgt6ExtendedBiomeArray",
                    "[I",
                    null,
                    null));
        }

        for (MethodNode mn : cn.methods) {
            if ("<init>".equals(mn.name) && "(Lnet/minecraft/world/World;II)V".equals(mn.desc)) {
                injectExtendedArrayInit(mn);
                int[] patched = replaceChunkSectionAllocations120(mn);
                storageAllocations += patched[0];
                entityListAllocations += patched[1];
            }

            if (("fillChunk".equals(mn.name) || "func_76607_a".equals(mn.name))
                    && "([BIIZ)V".equals(mn.desc)) {
                // S26 reads its 16-bit masks with readShort(). With 24 storage
                // slots, sign extension would select sections 16..23 whenever
                // bit 15 is set. All legacy passes (including full-sync clearing)
                // must stay below section 16; the height sidecar owns the rest.
                int bounds = 0;
                for (AbstractInsnNode node : mn.instructions.toArray()) {
                    if (node.getOpcode() != Opcodes.ARRAYLENGTH) continue;
                    AbstractInsnNode previous = node.getPrevious();
                    if (!(previous instanceof FieldInsnNode)) continue;
                    FieldInsnNode field = (FieldInsnNode) previous;
                    if (!"[Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;".equals(field.desc)) continue;
                    mn.instructions.insert(node, new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                            "legacyChunkSectionCount120", "(I)I", false));
                    bounds++;
                }
                if (bounds != 6) throw new IllegalStateException("Chunk legacy packet bounds matcher=" + bounds);
            }

            if (("getBiomeGenForWorldCoords".equals(mn.name) || "func_76591_a".equals(mn.name))
                    && "(IILnet/minecraft/world/biome/WorldChunkManager;)Lnet/minecraft/world/biome/BiomeGenBase;".equals(mn.desc)) {
                replaceChunkBiomeLookup(mn);
            }

            if (("getBiomeArray".equals(mn.name) || "func_76605_m".equals(mn.name))
                    && "()[B".equals(mn.desc)) {
                replaceGetBiomeArray(mn);
            }

            if (("setBiomeArray".equals(mn.name) || "func_76616_a".equals(mn.name))
                    && "([B)V".equals(mn.desc)) {
                replaceSetBiomeArray(mn);
            }
            if (("getBlockStorageArray".equals(mn.name) || "func_76587_i".equals(mn.name))
                    && "()[Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;".equals(mn.desc)) {
                mn.instructions.clear();
                mn.tryCatchBlocks.clear();
                mn.localVariables = null;
                mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                        "ensureChunkStorageArray120",
                        "(Lnet/minecraft/world/chunk/Chunk;)[Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;",
                        false));
                mn.instructions.add(new InsnNode(Opcodes.ARETURN));
                mn.maxStack = 1;
                mn.maxLocals = 1;
                storageGetterPatches++;
            }
            if ("<init>".equals(mn.name)
                    && "(Lnet/minecraft/world/World;[Lnet/minecraft/block/Block;[BII)V".equals(mn.desc)) {
                for (AbstractInsnNode instruction : mn.instructions.toArray()) {
                    if (instruction.getOpcode() != Opcodes.IOR) continue;
                    mn.instructions.set(instruction,new InsnNode(Opcodes.IADD));
                    rawArrayIndexPatches++;
                }
            }

            if (("getAreLevelsEmpty".equals(mn.name) || "func_76606_c".equals(mn.name))
                    && "(II)Z".equals(mn.desc)) {
                int interactionBounds = replaceHeightConstants120(mn, interactionWorldField);
                if (interactionBounds != 2)
                    throw new IllegalStateException("Chunk interaction height matcher drift: " + interactionBounds);
            }
            lightingPatches += patchChunkLighting120(mn);
        }

        if (storageAllocations != 1 || entityListAllocations != 1) {
            throw new IllegalStateException("Chunk 24-section matcher drift: storage="
                    + storageAllocations + ", entityLists=" + entityListAllocations);
        }
        if (lightingPatches != 5)
            throw new IllegalStateException("Chunk lighting matcher drift: patched="
                    + lightingPatches + ", expected=5");
        if (rawArrayIndexPatches != 2)
            throw new IllegalStateException("Chunk non-power-of-two raw stride matcher drift: "
                    + rawArrayIndexPatches);
        if (storageGetterPatches != 1)
            throw new IllegalStateException("Chunk storage getter matcher drift: " + storageGetterPatches);

        LOGGER.info("HTGT6 transformed Chunk with extended biomes, Futura sections and 384-light seams");
        return write(cn);
    }

    /** Keeps late coremods and legacy chunk constructors from shrinking Futura's section array. */
    public static net.minecraft.world.chunk.storage.ExtendedBlockStorage[] ensureChunkStorageArray120(
            net.minecraft.world.chunk.Chunk chunk) {
        Object value = getField(chunk, "storageArrays", "field_76652_q", "u");
        net.minecraft.world.chunk.storage.ExtendedBlockStorage[] sections =
                value instanceof net.minecraft.world.chunk.storage.ExtendedBlockStorage[]
                        ? (net.minecraft.world.chunk.storage.ExtendedBlockStorage[]) value : null;
        int required = com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.SECTION_COUNT;
        if (sections == null) sections = new net.minecraft.world.chunk.storage.ExtendedBlockStorage[required];
        else if (sections.length < required) sections = java.util.Arrays.copyOf(sections, required);
        if (sections != value) setNamedField(chunk, sections, "storageArrays", "field_76652_q", "u");
        return sections;
    }

    private static int patchChunkLighting120(MethodNode mn) {
        int patched = 0;
        if (("relightBlock".equals(mn.name) || "func_76615_h".equals(mn.name))
                && "(III)V".equals(mn.desc)) {
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                AbstractInsnNode next = nextRealInstruction(node);
                if (integerConstant120(node) == 255 && next != null
                        && next.getOpcode() == Opcodes.IAND) {
                    // Heightmap entries are ints; vanilla's byte-era mask truncates 256..383.
                    mn.instructions.remove(next);
                    mn.instructions.remove(node);
                    patched++;
                    break;
                }
            }
        } else if (("enqueueRelightChecks".equals(mn.name) || "func_76594_o".equals(mn.name))
                && "()V".equals(mn.desc)) {
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                int value = integerConstant120(node);
                AbstractInsnNode arithmetic = nextRealInstruction(node);
                if (value == 4096) {
                    replaceChunkLightingConstant120(mn, node, "chunkRelightCheckCount120");
                    patched++;
                } else if (value == 16 && arithmetic != null && arithmetic.getOpcode() == Opcodes.IREM
                        && patched == 1) {
                    replaceChunkLightingConstant120(mn, node, "chunkLightingSectionCount120");
                    patched++;
                } else if (value == 16 && arithmetic != null && arithmetic.getOpcode() == Opcodes.IDIV) {
                    replaceChunkLightingConstant120(mn, node, "chunkLightingSectionCount120");
                    patched++;
                } else if (value == 256 && arithmetic != null && arithmetic.getOpcode() == Opcodes.IDIV) {
                    replaceChunkLightingConstant120(mn, node, "chunkLightingColumnStride120");
                    patched++;
                }
            }
        }
        return patched;
    }

    private static void replaceChunkLightingConstant120(MethodNode mn, AbstractInsnNode node, String helper) {
        InsnList replacement = new InsnList();
        replacement.add(new VarInsnNode(Opcodes.ALOAD, 0));
        replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF, helper,
                "(Lnet/minecraft/world/chunk/Chunk;)I", false));
        mn.instructions.insertBefore(node, replacement);
        mn.instructions.remove(node);
    }

    private static int integerConstant120(AbstractInsnNode node) {
        if (node instanceof IntInsnNode) return ((IntInsnNode) node).operand;
        if (node instanceof LdcInsnNode && ((LdcInsnNode) node).cst instanceof Integer)
            return ((Integer) ((LdcInsnNode) node).cst).intValue();
        switch (node == null ? -1 : node.getOpcode()) {
            case Opcodes.ICONST_M1:return -1;
            case Opcodes.ICONST_0:return 0;
            case Opcodes.ICONST_1:return 1;
            case Opcodes.ICONST_2:return 2;
            case Opcodes.ICONST_3:return 3;
            case Opcodes.ICONST_4:return 4;
            case Opcodes.ICONST_5:return 5;
            default:return Integer.MIN_VALUE;
        }
    }

    public static int chunkLightingHeight120(net.minecraft.world.chunk.Chunk chunk) {
        return com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.lightingHeight(
                chunk == null ? null : chunk.worldObj);
    }
    public static int chunkLightingSectionCount120(net.minecraft.world.chunk.Chunk chunk) {
        return chunkLightingHeight120(chunk)>>4;
    }
    public static int chunkLightingColumnStride120(net.minecraft.world.chunk.Chunk chunk) {
        return chunkLightingSectionCount120(chunk)<<4;
    }
    public static int chunkRelightCheckCount120(net.minecraft.world.chunk.Chunk chunk) {
        return chunkLightingSectionCount120(chunk)<<8;
    }

    /**
     * Replaces only the two exact array allocations in Chunk(World,int,int).
     * Keeping the surrounding PUTFIELD makes the matcher independent of MCP/SRG
     * field names while the array descriptors keep it narrowly targeted.
     */
    private static int[] replaceChunkSectionAllocations120(MethodNode mn) {
        int storage = 0;
        int entities = 0;
        for (AbstractInsnNode node : mn.instructions.toArray()) {
            if (!(node instanceof org.objectweb.asm.tree.TypeInsnNode)
                    || node.getOpcode() != Opcodes.ANEWARRAY) continue;
            org.objectweb.asm.tree.TypeInsnNode array =
                    (org.objectweb.asm.tree.TypeInsnNode) node;
            String helper;
            String helperDesc;
            if ("net/minecraft/world/chunk/storage/ExtendedBlockStorage".equals(array.desc)) {
                helper = "createChunkStorageArrays120";
                helperDesc = "(Lnet/minecraft/world/World;)[Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;";
                storage++;
            } else if ("java/util/List".equals(array.desc)) {
                helper = "createChunkEntityLists120";
                helperDesc = "(Lnet/minecraft/world/World;)[Ljava/util/List;";
                entities++;
            } else {
                continue;
            }

            AbstractInsnNode size = previousRealInstruction(node);
            AbstractInsnNode store = nextRealInstruction(node);
            if (!(size instanceof IntInsnNode)
                    || size.getOpcode() != Opcodes.BIPUSH
                    || ((IntInsnNode) size).operand != 16
                    || !(store instanceof FieldInsnNode)
                    || store.getOpcode() != Opcodes.PUTFIELD) {
                throw new IllegalStateException("Unexpected Chunk section allocation bytecode for " + array.desc);
            }
            mn.instructions.set(size, new VarInsnNode(Opcodes.ALOAD, 1));
            mn.instructions.set(node, new MethodInsnNode(
                    Opcodes.INVOKESTATIC, SELF, helper, helperDesc, false));
        }
        return new int[]{storage, entities};
    }

    public static net.minecraft.world.chunk.storage.ExtendedBlockStorage[] createChunkStorageArrays120(
            net.minecraft.world.World world) {
        return new net.minecraft.world.chunk.storage.ExtendedBlockStorage[
                com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.storageSectionCount(world)];
    }

    public static java.util.List[] createChunkEntityLists120(net.minecraft.world.World world) {
        return new java.util.List[
                com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.storageSectionCount(world)];
    }

    private static void injectExtendedArrayInit(MethodNode mn) {
        // Constructor has no need for <clinit>. Initialize immediately after super().
        AbstractInsnNode[] a = mn.instructions.toArray();
        for (AbstractInsnNode n : a) {
            if (n.getOpcode() == Opcodes.INVOKESPECIAL
                    && n instanceof MethodInsnNode
                    && "<init>".equals(((MethodInsnNode) n).name)
                    && "()V".equals(((MethodInsnNode) n).desc)
                    && "java/lang/Object".equals(((MethodInsnNode) n).owner)) {
                InsnList add = new InsnList();
                add.add(new VarInsnNode(Opcodes.ALOAD, 0));
                add.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC, SELF, "initExtendedBiomeArray",
                        "(Lnet/minecraft/world/chunk/Chunk;)V", false));
                mn.instructions.insert(n, add);
                return;
            }
        }
        throw new IllegalStateException("Chunk constructor super() not found");
    }

    private static void replaceChunkBiomeLookup(MethodNode mn) {
        mn.instructions.clear();
        mn.tryCatchBlocks.clear();
        mn.localVariables = null;

        // this, x, z, worldChunkManager
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
        mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
        mn.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                SELF,
                "getChunkBiome",
                "(Lnet/minecraft/world/chunk/Chunk;IILnet/minecraft/world/biome/WorldChunkManager;)Lnet/minecraft/world/biome/BiomeGenBase;",
                false));
        mn.instructions.add(new org.objectweb.asm.tree.InsnNode(Opcodes.ARETURN));
        mn.maxStack = 4;
        mn.maxLocals = 4;
    }

    public static net.minecraft.world.biome.BiomeGenBase getChunkBiome(
            net.minecraft.world.chunk.Chunk chunk,
            int x,
            int z,
            net.minecraft.world.biome.WorldChunkManager manager) {

        int index = (z << 4) | x;
        int[] array = getExtendedBiomeArray(chunk);
        validateOceanBiomeChunk(chunk, manager, array);
        int id = array[index];

        // Vanilla uses 255 as the lazy/unset marker.
        if (id == 255) {
            // Upgrade the complete legacy/lazy chunk at once. Resolving only
            // the requested cell made extended oceans appear one block at a
            // time and left neighbouring columns as 255 in packets/saves.
            int baseX = chunk.xPosition << 4;
            int baseZ = chunk.zPosition << 4;
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int localX = 0; localX < 16; localX++) {
                    int fillIndex = (localZ << 4) | localX;
                    if (array[fillIndex] != 255) continue;
                    net.minecraft.world.biome.BiomeGenBase biome = manager.getBiomeGenAt(
                            baseX + localX, baseZ + localZ);
                    array[fillIndex] = biome == null
                            ? net.minecraft.world.biome.BiomeGenBase.plains.biomeID : biome.biomeID;
                }
            }
            id = array[index];
        }

        net.minecraft.world.biome.BiomeGenBase result =
                net.minecraft.world.biome.BiomeGenBase.getBiome(id);
        return result != null ? result : net.minecraft.world.biome.BiomeGenBase.plains;
    }

    /** Rebuilds legacy thin warm-ocean traces once per loaded chunk. */
    private static void validateOceanBiomeChunk(net.minecraft.world.chunk.Chunk chunk,
                                                 net.minecraft.world.biome.WorldChunkManager manager,
                                                 int[] array) {
        if (VALIDATED_OCEAN_BIOME_CHUNKS.containsKey(chunk)) return;
        int baseX = chunk.xPosition << 4;
        int baseZ = chunk.zPosition << 4;
        net.minecraft.world.biome.BiomeGenBase[] generated = manager.loadBlockGeneratorData(
                null, baseX, baseZ, 16, 16);
        for (int i = 0; i < 256; i++) {
            int generatedId = generated[i] == null
                    ? net.minecraft.world.biome.BiomeGenBase.plains.biomeID : generated[i].biomeID;
            int storedId = array[i];
            if (storedId == 255 || (isOceanFamilyId(storedId) && isOceanFamilyId(generatedId))) {
                array[i] = generatedId;
            }
        }
        VALIDATED_OCEAN_BIOME_CHUNKS.put(chunk, Boolean.TRUE);
    }

    private static boolean isOceanFamilyId(int id) {
        return id == net.minecraft.world.biome.BiomeGenBase.ocean.biomeID
                || id == net.minecraft.world.biome.BiomeGenBase.deepOcean.biomeID
                || id == net.minecraft.world.biome.BiomeGenBase.frozenOcean.biomeID
                || id == HTGT6Config.WARM_OCEAN_BIOME_ID
                || id == HTGT6Config.DEEP_WARM_OCEAN_BIOME_ID
                // Vanilla GenLayerVoronoiZoom corrupted two corners with &255.
                // Include those historical values so existing chunks migrate.
                || id == (HTGT6Config.WARM_OCEAN_BIOME_ID & 255)
                || id == (HTGT6Config.DEEP_WARM_OCEAN_BIOME_ID & 255);
    }

    public static void initExtendedBiomeArray(net.minecraft.world.chunk.Chunk chunk) {
        try {
            java.lang.reflect.Field f = extendedBiomeField120();
            int[] array = new int[256];
            java.util.Arrays.fill(array, 255);
            f.set(chunk, array);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot initialize Chunk extended biome array", t);
        }
    }

    public static int[] getExtendedBiomeArray(net.minecraft.world.chunk.Chunk chunk) {
        try {
            java.lang.reflect.Field f = extendedBiomeField120();
            int[] array = (int[]) f.get(chunk);
            if (array == null || array.length != 256) {
                array = new int[256];
                java.util.Arrays.fill(array, 255);
                f.set(chunk, array);
            }
            return array;
        } catch (Throwable t) {
            throw new RuntimeException("Cannot access Chunk extended biome array", t);
        }
    }

    private static java.lang.reflect.Field extendedBiomeField120() throws NoSuchFieldException {
        java.lang.reflect.Field field = EXTENDED_BIOME_FIELD;
        if (field != null) return field;
        synchronized (HTGT6Transformer.class) {
            field = EXTENDED_BIOME_FIELD;
            if (field == null) {
                field = net.minecraft.world.chunk.Chunk.class
                        .getDeclaredField("htgt6ExtendedBiomeArray");
                field.setAccessible(true);
                EXTENDED_BIOME_FIELD = field;
            }
        }
        return field;
    }

    private static void replaceGetBiomeArray(MethodNode mn) {
        mn.instructions.clear();
        mn.tryCatchBlocks.clear();
        mn.localVariables = null;

        // Return a legacy byte[] view. Extended users must use getExtendedBiomeArray().
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                SELF,
                "legacyBiomeBytes",
                "(Lnet/minecraft/world/chunk/Chunk;)[B",
                false));
        mn.instructions.add(new org.objectweb.asm.tree.InsnNode(Opcodes.ARETURN));
        mn.maxStack = 1;
        mn.maxLocals = 1;
    }

    public static byte[] legacyBiomeBytes(net.minecraft.world.chunk.Chunk chunk) {
        int[] in = getExtendedBiomeArray(chunk);
        byte[] out = new byte[256];
        for (int i = 0; i < 256; i++) {
            out[i] = (byte) in[i];
        }
        return out;
    }

    private static void replaceSetBiomeArray(MethodNode mn) {
        mn.instructions.clear();
        mn.tryCatchBlocks.clear();
        mn.localVariables = null;

        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        mn.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                SELF,
                "setLegacyBiomeBytes",
                "(Lnet/minecraft/world/chunk/Chunk;[B)V",
                false));
        mn.instructions.add(new org.objectweb.asm.tree.InsnNode(Opcodes.RETURN));
        mn.maxStack = 2;
        mn.maxLocals = 2;
    }

    public static void setLegacyBiomeBytes(net.minecraft.world.chunk.Chunk chunk, byte[] bytes) {
        if (bytes == null || bytes.length != 256) {
            throw new IllegalArgumentException("Biome array must contain 256 entries");
        }
        int[] out = getExtendedBiomeArray(chunk);
        for (int i = 0; i < 256; i++) {
            out[i] = bytes[i] & 255;
        }
    }

    /* --------------------------------------------------------------------- */
    /* ExtendedBlockStorage chunk-load recount                               */
    /* --------------------------------------------------------------------- */

    private static byte[] transformExtendedBlockStorageLoading(byte[] bytes) {
        ClassNode cn = read(bytes);
        java.util.List<String> countFields = null;
        for (MethodNode mn : cn.methods) {
            if (!("removeInvalidBlocks".equals(mn.name) || "func_76672_e".equals(mn.name))
                    || !"()V".equals(mn.desc)) continue;
            countFields = new java.util.ArrayList<String>(2);
            for (AbstractInsnNode instruction : mn.instructions.toArray()) {
                if (!(instruction instanceof FieldInsnNode)
                        || instruction.getOpcode() != Opcodes.PUTFIELD) continue;
                FieldInsnNode field = (FieldInsnNode) instruction;
                if (!cn.name.equals(field.owner) || !"I".equals(field.desc)
                        || countFields.contains(field.name)) continue;
                countFields.add(field.name);
            }
            if (countFields.size() != 2)
                throw new IllegalStateException("Expected block/tick counters in " + cn.name
                        + "." + mn.name + ", found " + countFields);
        }
        if (countFields == null)
            throw new IllegalStateException("Expected one ExtendedBlockStorage recount method");

        // Keep vanilla/NEID's method intact for other callers. Anvil is redirected to this
        // unique bridge, which remains present regardless of coremod/mixin application order.
        MethodNode fast = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                "htgt6$recountBlocksForLoad", "()V", null, null);
        fast.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        fast.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                    "recountChunkSection120",
                    "(Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;)J", false));
        fast.instructions.add(new VarInsnNode(Opcodes.LSTORE, 1));
        fast.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        fast.instructions.add(new VarInsnNode(Opcodes.LLOAD, 1));
        fast.instructions.add(new InsnNode(Opcodes.L2I));
        fast.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, cn.name,
                countFields.get(0), "I"));
        fast.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        fast.instructions.add(new VarInsnNode(Opcodes.LLOAD, 1));
        fast.instructions.add(new IntInsnNode(Opcodes.BIPUSH, 32));
        fast.instructions.add(new InsnNode(Opcodes.LUSHR));
        fast.instructions.add(new InsnNode(Opcodes.L2I));
        fast.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, cn.name,
                countFields.get(1), "I"));
        fast.instructions.add(new InsnNode(Opcodes.RETURN));
        fast.maxStack = 4;
        fast.maxLocals = 3;
        cn.methods.add(fast);
        LOGGER.info("HTGT6 transformed ExtendedBlockStorage packed-ID chunk-load recount");
        return write(cn);
    }

    /** Low 32 bits are non-air count; high 32 bits are randomly-ticking count. */
    public static long recountChunkSection120(
            net.minecraft.world.chunk.storage.ExtendedBlockStorage storage) {
        short[] neidIds = neidBlockIds120(storage);
        if (neidIds != null)
            return recountShortBlockIds120(neidIds, neidRemoveInvalidBlocks120(storage));

        byte[] ids = storage.getBlockLSBArray();
        net.minecraft.world.chunk.NibbleArray msb = storage.getBlockMSBArray();
        byte[] high = msb == null ? null : msb.data;
        int blocks = 0, ticking = 0;
        for (int index = 0; index < ids.length; index++) {
            int id = ids[index] & 255;
            if (high != null) {
                int packed = high[index >> 1] & 255;
                id |= ((index & 1) == 0 ? packed & 15 : packed >>> 4) << 8;
            }
            byte state = chunkSectionBlockState120(id);
            // Vanilla fails while recounting an unmapped 12-bit ID. Keep that
            // fail-fast behavior instead of silently making a section appear empty.
            if (state == 1)
                throw new IllegalStateException("Unmapped block ID " + id
                        + " in vanilla chunk section");
            if (state >= 3) blocks++;
            if (state == 4) ticking++;
        }
        return packSectionCounts120(blocks, ticking);
    }

    static long recountShortBlockIds120(short[] ids, boolean removeInvalid) {
        int blocks = 0, ticking = 0;
        for (int index = 0; index < ids.length; index++) {
            int id = ids[index] & 65535;
            if (id == 0) continue;
            byte state = chunkSectionBlockState120(id);
            if (state == 1) {
                if (removeInvalid) ids[index] = 0;
                continue;
            }
            if (state >= 3) blocks++;
            if (state == 4) ticking++;
        }
        return packSectionCounts120(blocks, ticking);
    }

    private static byte chunkSectionBlockState120(int id) {
        if (id == 0) return 2;
        byte state = CHUNK_SECTION_BLOCK_STATES[id & 65535];
        if (state != 0) return state;
        Object value = net.minecraft.block.Block.blockRegistry.getObjectById(id);
        if (!(value instanceof net.minecraft.block.Block)) state = 1;
        else if (value == net.minecraft.init.Blocks.air) state = 2;
        else state = ((net.minecraft.block.Block) value).getTickRandomly() ? (byte) 4 : (byte) 3;
        CHUNK_SECTION_BLOCK_STATES[id & 65535] = state;
        return state;
    }

    private static long packSectionCounts120(int blocks, int ticking) {
        return ((long) ticking << 32) | (blocks & 0xffffffffL);
    }

    private static short[] neidBlockIds120(
            net.minecraft.world.chunk.storage.ExtendedBlockStorage storage) {
        try {
            if (!NEID_BLOCK_ARRAY_ACCESSOR_RESOLVED) {
                synchronized (HTGT6Transformer.class) {
                    if (!NEID_BLOCK_ARRAY_ACCESSOR_RESOLVED) {
                        try {
                            NEID_BLOCK_ARRAY_ACCESSOR = storage.getClass()
                                    .getMethod("getBlock16BArray");
                        } catch (NoSuchMethodException ignored) { }
                        NEID_BLOCK_ARRAY_ACCESSOR_RESOLVED = true;
                    }
                }
            }
            return NEID_BLOCK_ARRAY_ACCESSOR == null ? null
                    : (short[]) NEID_BLOCK_ARRAY_ACCESSOR.invoke(storage);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("Cannot access NotEnoughIDs chunk-section IDs", exception);
        }
    }

    private static boolean neidRemoveInvalidBlocks120(
            net.minecraft.world.chunk.storage.ExtendedBlockStorage storage) {
        Boolean cached = NEID_REMOVE_INVALID_BLOCKS;
        if (cached != null) return cached.booleanValue();
        boolean value = false;
        try {
            Class<?> config = Class.forName("com.gtnewhorizons.neid.NEIDConfig", false,
                    storage.getClass().getClassLoader());
            value = config.getField("RemoveInvalidBlocks").getBoolean(null);
        } catch (ClassNotFoundException ignored) {
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException("Cannot read NotEnoughIDs invalid-block policy", exception);
        }
        NEID_REMOVE_INVALID_BLOCKS = Boolean.valueOf(value);
        return value;
    }

    /* --------------------------------------------------------------------- */
    /* AnvilChunkLoader                                                       */
    /* --------------------------------------------------------------------- */

    private static byte[] transformAnvilChunkLoader(byte[] bytes) {
        ClassNode cn = read(bytes);

        int sectionAllocations = 0;
        int sectionStores = 0;
        int sectionRecounts = 0;

        for (MethodNode mn : cn.methods) {
            // readChunkFromNBT / checkedReadChunkFromNBT: replace byte[] Biomes load.
            if (mn.name.contains("readChunkFromNBT") || "func_75823_a".equals(mn.name)) {
                replaceAnvilBiomeRead(mn);
            }

            if (("readChunkFromNBT".equals(mn.name) || "func_75823_a".equals(mn.name))
                    && "(Lnet/minecraft/world/World;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/world/chunk/Chunk;".equals(mn.desc)) {
                int[] patched = replaceAnvilSectionRead120(mn);
                sectionAllocations += patched[0];
                sectionStores += patched[1];
                sectionRecounts += patched[2];
            }

            // writeChunkToNBT: replace byte[] Biomes save with BiomesExt int[].
            if ("writeChunkToNBT".equals(mn.name) || "func_75820_a".equals(mn.name)) {
                replaceAnvilBiomeWrite(mn);
            }
        }

        int expectedRecounts = HTGT6Config.OPTIMIZE_CHUNK_SECTION_LOADING ? 1 : 0;
        if (sectionAllocations != 1 || sectionStores != 1 || sectionRecounts != expectedRecounts) {
            throw new IllegalStateException("Anvil 24-section matcher drift: allocation="
                    + sectionAllocations + ", store=" + sectionStores
                    + ", recount=" + sectionRecounts);
        }

        LOGGER.info("HTGT6 transformed AnvilChunkLoader biome persistence and Futura section NBT");
        return write(cn);
    }

    private static int[] replaceAnvilSectionRead120(MethodNode mn) {
        int allocations = 0;
        int stores = 0;
        int recounts = 0;
        for (AbstractInsnNode node : mn.instructions.toArray()) {
            if (node instanceof org.objectweb.asm.tree.TypeInsnNode
                    && node.getOpcode() == Opcodes.ANEWARRAY
                    && "net/minecraft/world/chunk/storage/ExtendedBlockStorage".equals(
                    ((org.objectweb.asm.tree.TypeInsnNode) node).desc)) {
                AbstractInsnNode size = previousRealInstruction(node);
                if (!(size instanceof VarInsnNode) || size.getOpcode() != Opcodes.ILOAD) {
                    throw new IllegalStateException("Unexpected Anvil section-array allocation bytecode");
                }
                mn.instructions.set(size, new VarInsnNode(Opcodes.ALOAD, 1));
                mn.instructions.set(node, new MethodInsnNode(
                        Opcodes.INVOKESTATIC, SELF, "createChunkStorageArrays120",
                        "(Lnet/minecraft/world/World;)[Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;",
                        false));
                allocations++;
            } else if (node instanceof MethodInsnNode
                    && HTGT6Config.OPTIMIZE_CHUNK_SECTION_LOADING
                    && "net/minecraft/world/chunk/storage/ExtendedBlockStorage".equals(
                    ((MethodInsnNode) node).owner)
                    && ("removeInvalidBlocks".equals(((MethodInsnNode) node).name)
                    || "func_76672_e".equals(((MethodInsnNode) node).name))
                    && "()V".equals(((MethodInsnNode) node).desc)) {
                mn.instructions.set(node, new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                        "net/minecraft/world/chunk/storage/ExtendedBlockStorage",
                        "htgt6$recountBlocksForLoad", "()V", false));
                recounts++;
            } else if (node.getOpcode() == Opcodes.AASTORE) {
                AbstractInsnNode value = previousRealInstruction(node);
                AbstractInsnNode index = previousRealInstruction(value);
                AbstractInsnNode array = previousRealInstruction(index);
                if (value instanceof VarInsnNode && value.getOpcode() == Opcodes.ALOAD
                        && index instanceof VarInsnNode && index.getOpcode() == Opcodes.ILOAD
                        && array instanceof VarInsnNode && array.getOpcode() == Opcodes.ALOAD) {
                    mn.instructions.set(node, new MethodInsnNode(
                            Opcodes.INVOKESTATIC, SELF, "storeChunkSection120",
                            "([Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;ILnet/minecraft/world/chunk/storage/ExtendedBlockStorage;)V",
                            false));
                    stores++;
                }
            }
        }
        return new int[]{allocations, stores, recounts};
    }

    /** Bounds-safe for legacy worlds and treats Section.Y as an unsigned byte. */
    public static void storeChunkSection120(
            net.minecraft.world.chunk.storage.ExtendedBlockStorage[] sections,
            int encodedY,
            net.minecraft.world.chunk.storage.ExtendedBlockStorage storage) {
        int section = encodedY & 255;
        if (section < sections.length) sections[section] = storage;
    }

    private static void replaceAnvilBiomeRead(MethodNode mn) {
        // The real byte[] -> int[] upgrade is applied at the private
        // readChunkFromNBT(World, NBTTagCompound) method. Vanilla first creates
        // the Chunk and loads its legacy byte[] Biomes field; immediately before
        // returning it we overlay BiomesExt when present.
        if (!("readChunkFromNBT".equals(mn.name) || "func_75823_a".equals(mn.name))
                || !"(Lnet/minecraft/world/World;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/world/chunk/Chunk;".equals(mn.desc)) {
            return;
        }

        int nbtLocal = findLocalByType(mn, "Lnet/minecraft/nbt/NBTTagCompound;");
        if (nbtLocal < 0) {
            throw new IllegalStateException("Could not locate NBTTagCompound local in readChunkFromNBT");
        }

        int temp = mn.maxLocals;
        mn.maxLocals = temp + 1;

        for (AbstractInsnNode n : mn.instructions.toArray()) {
            if (n.getOpcode() == Opcodes.ARETURN) {
                InsnList add = new InsnList();
                add.add(new VarInsnNode(Opcodes.ASTORE, temp));
                add.add(new VarInsnNode(Opcodes.ALOAD, nbtLocal));
                add.add(new VarInsnNode(Opcodes.ALOAD, temp));
                add.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        SELF,
                        "readExtendedBiomeNBT",
                        "(Lnet/minecraft/nbt/NBTTagCompound;Lnet/minecraft/world/chunk/Chunk;)V",
                        false));
                add.add(new VarInsnNode(Opcodes.ALOAD, temp));
                mn.instructions.insertBefore(n, add);
            }
        }
    }

    private static void replaceAnvilBiomeWrite(MethodNode mn) {
        // The vanilla write method calls Chunk.getBiomeArray() and NBTTagCompound.setByteArray.
        // Redirect the exact getBiomeArray call to getExtendedBiomeArray is not possible
        // because setByteArray expects byte[]. Instead, insert a BiomesExt int[] write
        // immediately before the method returns. The original Biomes byte[] remains for
        // old readers.
        int chunkLocal = findLocalByType(mn, "Lnet/minecraft/world/chunk/Chunk;");
        int nbtLocal = findLocalByType(mn, "Lnet/minecraft/nbt/NBTTagCompound;");
        if (chunkLocal < 0 || nbtLocal < 0) return;

        for (AbstractInsnNode n : mn.instructions.toArray()) {
            if (n.getOpcode() == Opcodes.RETURN) {
                InsnList add = new InsnList();
                add.add(new VarInsnNode(Opcodes.ALOAD, nbtLocal));
                add.add(new VarInsnNode(Opcodes.ALOAD, chunkLocal));
                add.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        SELF,
                        "writeExtendedBiomeNBT",
                        "(Lnet/minecraft/nbt/NBTTagCompound;Lnet/minecraft/world/chunk/Chunk;)V",
                        false));
                mn.instructions.insertBefore(n, add);
            }
        }
    }

    public static void writeExtendedBiomeNBT(
            net.minecraft.nbt.NBTTagCompound nbt,
            net.minecraft.world.chunk.Chunk chunk) {
        int[] ids = getExtendedBiomeArray(chunk);
        validateOceanBiomeChunk(chunk, chunk.worldObj.getWorldChunkManager(), ids);
        nbt.setIntArray("BiomesExt", ids);
    }

    public static void readExtendedBiomeNBT(
            net.minecraft.nbt.NBTTagCompound nbt,
            net.minecraft.world.chunk.Chunk chunk) {
        if (nbt.hasKey("BiomesExt", 11)) {
            int[] in = nbt.getIntArray("BiomesExt");
            if (in.length == 256) {
                int[] out = getExtendedBiomeArray(chunk);
                System.arraycopy(in, 0, out, 0, 256);
            }
        }
    }

    /* --------------------------------------------------------------------- */
    /* BiomeDictionary                                                        */
    /* --------------------------------------------------------------------- */

    private static byte[] transformBiomeDictionary(byte[] bytes) {
        ClassNode cn = read(bytes);
        int registrationHooks = 0;
        int registeredIdHooks = 0;
        int registeredBiomeHooks = 0;
        int typeLookupHooks = 0;
        int typesForBiomeHooks = 0;

        // Do NOT change biomeList's field descriptor here: <clinit> initializes it as
        // BiomeInfo[], and changing the descriptor without rewriting <clinit> would
        // produce a VerifyError. We keep the Forge registry intact and only replace
        // the integer lookup entry point below. Extended biome type registration can
        // be layered on later once the exact BiomeInfo bytecode is available.
        for (MethodNode mn : cn.methods) {
            if ("registerBiomeType".equals(mn.name)
                    && isBiomeDictionaryDescriptor(mn.desc,
                    "[Lnet/minecraftforge/common/BiomeDictionary$Type;)Z")) {
                injectExtendedBiomeDictionaryRegistration(mn);
                registrationHooks++;
            }
            if ("isBiomeRegistered".equals(mn.name)
                    && "(I)Z".equals(mn.desc)) {
                replaceBiomeDictionaryMethod(mn, "isExtendedBiomeRegistered", "(I)Z", Opcodes.ILOAD);
                registeredIdHooks++;
            }
            if ("isBiomeRegistered".equals(mn.name)
                    && isBiomeDictionaryDescriptor(mn.desc, ")Z")) {
                replaceBiomeDictionaryMethod(mn, "isExtendedBiomeRegistered",
                        mn.desc, Opcodes.ALOAD);
                registeredBiomeHooks++;
            }
            if ("isBiomeOfType".equals(mn.name)
                    && isBiomeDictionaryDescriptor(mn.desc,
                    "Lnet/minecraftforge/common/BiomeDictionary$Type;)Z")) {
                mn.instructions.clear();
                mn.tryCatchBlocks.clear();
                mn.localVariables = null;
                mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                        "isExtendedBiomeOfType",
                        mn.desc, false));
                mn.instructions.add(new org.objectweb.asm.tree.InsnNode(Opcodes.IRETURN));
                mn.maxStack = 2;
                mn.maxLocals = 2;
                typeLookupHooks++;
            }
            if ("getTypesForBiome".equals(mn.name)
                    && isBiomeDictionaryDescriptor(mn.desc,
                    ")[Lnet/minecraftforge/common/BiomeDictionary$Type;")) {
                mn.instructions.clear();
                mn.tryCatchBlocks.clear();
                mn.localVariables = null;
                mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                        "getExtendedTypesForBiome",
                        mn.desc, false));
                mn.instructions.add(new InsnNode(Opcodes.ARETURN));
                mn.maxStack = 1;
                mn.maxLocals = 1;
                typesForBiomeHooks++;
            }
        }

        if (registrationHooks != 1 || registeredIdHooks != 1 || registeredBiomeHooks != 1
                || typeLookupHooks != 1 || typesForBiomeHooks != 1) {
            throw new IllegalStateException("BiomeDictionary matcher drift: register="
                    + registrationHooks + ", registeredId=" + registeredIdHooks
                    + ", registeredBiome=" + registeredBiomeHooks + ", typeLookup="
                    + typeLookupHooks + ", typesForBiome=" + typesForBiomeHooks);
        }
        LOGGER.info("HTGT6 transformed BiomeDictionary registration lookup");
        return write(cn);
    }

    /** Forge itself is still notch-named when this early transformer runs. */
    private static boolean isBiomeDictionaryDescriptor(String desc, String suffix) {
        return desc != null && desc.endsWith(suffix)
                && (desc.startsWith("(Lnet/minecraft/world/biome/BiomeGenBase;")
                || desc.startsWith("(Lahu;"));
    }

    public static boolean isExtendedBiomeRegistered(int id) {
        return getBiomeTypes(id) != null;
    }

    /** Applies the Minecraft 1.12 creative-inventory textures and survival-tab slot layout. */
    private static byte[] transformCreativeInventory(byte[] bytes) {
        ClassNode cn = read(bytes);
        int layoutHooks = 0;
        int texturePaths = 0;
        int modelPositionFixes = 0;
        for (MethodNode mn : cn.methods) {
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                if (!(node instanceof LdcInsnNode) || !(((LdcInsnNode) node).cst instanceof String)) continue;
                String value = (String) ((LdcInsnNode) node).cst;
                if (value.startsWith("textures/gui/container/creative_inventory/")) {
                    ((LdcInsnNode) node).cst = "htgt6:" + value;
                    texturePaths++;
                }
            }
            modelPositionFixes += patchCreativePlayerModelPosition(mn);
            if (("setCurrentCreativeTab".equals(mn.name) || "func_147050_b".equals(mn.name))
                    && "(Lnet/minecraft/creativetab/CreativeTabs;)V".equals(mn.desc)) {
                for (AbstractInsnNode node : mn.instructions.toArray()) {
                    if (node.getOpcode() != Opcodes.RETURN) continue;
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF, "layoutCreativeInventory112",
                            "(Lnet/minecraft/client/gui/inventory/GuiContainerCreative;)V", false));
                    mn.instructions.insertBefore(node, hook);
                    layoutHooks++;
                }
            }
        }
        if (layoutHooks == 0 || texturePaths == 0 || modelPositionFixes != 1)
            throw new IllegalStateException("Creative inventory 1.12 integration incomplete: layout="
                    + layoutHooks + ", textures=" + texturePaths + ", playerModel=" + modelPositionFixes);
        LOGGER.info("HTGT6 ported the Minecraft 1.12 creative inventory layout and textures");
        return write(cn);
    }

    /** Redirects the old x=43 preview call; changing a nearby constant is not reliable after other coremods. */
    private static int patchCreativePlayerModelPosition(MethodNode method) {
        if (!"(FII)V".equals(method.desc)) return 0;
        for (AbstractInsnNode node : method.instructions.toArray()) {
            if (!(node instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) node;
            if (call.getOpcode() != Opcodes.INVOKESTATIC
                    || !"(IIIFFLnet/minecraft/entity/EntityLivingBase;)V".equals(call.desc)) continue;
            call.owner = SELF;
            call.name = "drawCreativePlayer112";
            call.itf = false;
            return 1;
        }
        return 0;
    }

    /** The old call passes guiLeft+43; the 1.12 texture's black viewport is centred at guiLeft+88. */
    public static void drawCreativePlayer112(int oldX, int y, int scale, float mouseX, float mouseY,
            net.minecraft.entity.EntityLivingBase player) {
        net.minecraft.client.gui.inventory.GuiInventory.func_147046_a(
                oldX + 45, y, scale, mouseX + 45.0F, mouseY, player);
    }

    public static void layoutCreativeInventory112(
            net.minecraft.client.gui.inventory.GuiContainerCreative gui) {
        if (gui == null || gui.inventorySlots == null) return;
        java.util.List slots = gui.inventorySlots.inventorySlots;
        // 46 wrapped player slots plus the creative destroy slot.
        if (slots.size() != 47) return;
        for (int index = 0; index < 46; ++index) {
            net.minecraft.inventory.Slot slot = (net.minecraft.inventory.Slot) slots.get(index);
            if (index < 5) {
                slot.xDisplayPosition = -2000;
                slot.yDisplayPosition = -2000;
            } else if (index < 9) {
                int armor = index - 5;
                slot.xDisplayPosition = 54 + armor / 2 * 54;
                slot.yDisplayPosition = 6 + armor % 2 * 27;
            } else if (index == 45) {
                slot.xDisplayPosition = 35;
                slot.yDisplayPosition = 20;
            } else {
                int inventory = index - 9;
                slot.xDisplayPosition = 9 + inventory % 9 * 18;
                slot.yDisplayPosition = index >= 36 ? 112 : 54 + inventory / 9 * 18;
            }
        }
        net.minecraft.inventory.Slot destroy = (net.minecraft.inventory.Slot) slots.get(46);
        destroy.xDisplayPosition = 173;
        destroy.yDisplayPosition = 112;
    }

    /**
     * 1.20 cartography locking adapted to the 1.7 ItemMap lifecycle. Keep the
     * rest of onUpdate (especially updateVisiblePlayers/map markers) intact and
     * guard only its terrain-colour updateMapData call.
     */
    private static byte[] transformLockedMapUpdate120(byte[] bytes) {
        ClassNode cn = read(bytes);
        final String updateDescriptor = "(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;"
                + "Lnet/minecraft/world/storage/MapData;)V";
        int patched = 0;
        for (MethodNode method : cn.methods) {
            if (!("onUpdate".equals(method.name) || "func_77663_a".equals(method.name)
                    || "a".equals(method.name))) continue;
            for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode)instruction;
                if (call.getOpcode() != Opcodes.INVOKEVIRTUAL || !updateDescriptor.equals(call.desc)
                        || !("updateMapData".equals(call.name) || "func_77872_a".equals(call.name)
                        || "a".equals(call.name))) continue;
                method.instructions.insertBefore(call, new VarInsnNode(Opcodes.ALOAD, 1));
                call.setOpcode(Opcodes.INVOKESTATIC);
                call.owner = SELF;
                call.name = "updateMapDataUnlessLocked120";
                call.desc = "(Lnet/minecraft/item/ItemMap;Lnet/minecraft/world/World;"
                        + "Lnet/minecraft/entity/Entity;Lnet/minecraft/world/storage/MapData;"
                        + "Lnet/minecraft/item/ItemStack;)V";
                call.itf = false;
                patched++;
            }
        }
        if (patched != 1)
            throw new IllegalStateException("ItemMap locked update guard was not applied: " + patched);
        LOGGER.info("HTGT6 guarded locked cartography maps from ItemMap updates");
        return write(cn);
    }

    public static boolean isLockedMap120(net.minecraft.item.ItemStack stack) {
        return stack != null && stack.hasTagCompound()
                && stack.getTagCompound().getBoolean("map_is_locked");
    }

    public static void updateMapDataUnlessLocked120(net.minecraft.item.ItemMap map,
            net.minecraft.world.World world, net.minecraft.entity.Entity entity,
            net.minecraft.world.storage.MapData data, net.minecraft.item.ItemStack stack) {
        if (!isLockedMap120(stack)) map.updateMapData(world, entity, data);
    }

    public static boolean isExtendedBiomeRegistered(net.minecraft.world.biome.BiomeGenBase biome) {
        return biome != null && isExtendedBiomeRegistered(biome.biomeID);
    }

    public static boolean isExtendedBiomeOfType(net.minecraft.world.biome.BiomeGenBase biome,
                                                net.minecraftforge.common.BiomeDictionary.Type type) {
        java.util.EnumSet<net.minecraftforge.common.BiomeDictionary.Type> types =
                biome == null ? null : getBiomeTypes(biome.biomeID);
        return types != null && type != null && types.contains(type);
    }

    /**
     * Forge 1.7.10's original getTypesForBiome indexes its fixed 256-entry
     * BiomeInfo array directly.  Route that public lookup through the same
     * extended registry as registration, otherwise tooltip providers such as
     * IC2 crops crash while enumerating creative-search items on biome 256+.
     */
    public static net.minecraftforge.common.BiomeDictionary.Type[] getExtendedTypesForBiome(
            net.minecraft.world.biome.BiomeGenBase biome) {
        java.util.EnumSet<net.minecraftforge.common.BiomeDictionary.Type> types =
                biome == null ? null : getBiomeTypes(biome.biomeID);
        if (types == null || types.isEmpty()) {
            return new net.minecraftforge.common.BiomeDictionary.Type[0];
        }
        return types.toArray(new net.minecraftforge.common.BiomeDictionary.Type[types.size()]);
    }

    @SuppressWarnings("unchecked")
    private static java.util.EnumSet<net.minecraftforge.common.BiomeDictionary.Type> getBiomeTypes(int id) {
        if (id < 0) return null;
        if (id >= 256) return EXTENDED_BIOME_TYPES.get(Integer.valueOf(id));
        try {
            java.lang.reflect.Field listField = net.minecraftforge.common.BiomeDictionary.class
                    .getDeclaredField("biomeList");
            listField.setAccessible(true);
            Object[] biomeList = (Object[]) listField.get(null);
            if (id >= biomeList.length || biomeList[id] == null) return null;
            java.lang.reflect.Field typesField = biomeList[id].getClass().getDeclaredField("typeList");
            typesField.setAccessible(true);
            return (java.util.EnumSet<net.minecraftforge.common.BiomeDictionary.Type>) typesField.get(biomeList[id]);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot read legacy BiomeDictionary registration", e);
        }
    }

    /** Keep Forge's original array-backed registration for IDs below 256. */
    private static void injectExtendedBiomeDictionaryRegistration(MethodNode mn) {
        LabelNode legacy = new LabelNode();
        InsnList guard = new InsnList();
        guard.add(new VarInsnNode(Opcodes.ALOAD, 0));
        guard.add(new VarInsnNode(Opcodes.ALOAD, 1));
        guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                "registerExtendedBiomeTypes",
                mn.desc,
                false));
        guard.add(new JumpInsnNode(Opcodes.IFEQ, legacy));
        guard.add(new InsnNode(Opcodes.ICONST_1));
        guard.add(new InsnNode(Opcodes.IRETURN));
        guard.add(legacy);
        mn.instructions.insert(guard);
    }

    public static boolean registerExtendedBiomeTypes(
            net.minecraft.world.biome.BiomeGenBase biome,
            net.minecraftforge.common.BiomeDictionary.Type[] types) {
        if (biome == null || biome.biomeID < 256 || getExtendedBiome(biome.biomeID) != biome) return false;

        java.util.EnumSet<net.minecraftforge.common.BiomeDictionary.Type> registered =
                EXTENDED_BIOME_TYPES.get(Integer.valueOf(biome.biomeID));
        if (registered == null) {
            registered = java.util.EnumSet.noneOf(net.minecraftforge.common.BiomeDictionary.Type.class);
            EXTENDED_BIOME_TYPES.put(Integer.valueOf(biome.biomeID), registered);
        }
        if (types == null) return true;
        for (net.minecraftforge.common.BiomeDictionary.Type type : types) {
            if (type == null || !registered.add(type)) continue;
            addBiomeToForgeTypeList(biome, type);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static void addBiomeToForgeTypeList(net.minecraft.world.biome.BiomeGenBase biome,
                                                net.minecraftforge.common.BiomeDictionary.Type type) {
        try {
            java.lang.reflect.Field field = net.minecraftforge.common.BiomeDictionary.class
                    .getDeclaredField("typeInfoList");
            field.setAccessible(true);
            java.util.ArrayList<net.minecraft.world.biome.BiomeGenBase>[] lists =
                    (java.util.ArrayList<net.minecraft.world.biome.BiomeGenBase>[]) field.get(null);
            int index = type.ordinal();
            if (lists[index] == null) lists[index] = new java.util.ArrayList<net.minecraft.world.biome.BiomeGenBase>();
            lists[index].add(biome);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot add extended biome to BiomeDictionary type list", e);
        }
    }

    /* --------------------------------------------------------------------- */
    /* S21PacketChunkData                                                     */
    /* --------------------------------------------------------------------- */

    private static byte[] transformS21PacketChunkData(byte[] bytes) {
        ClassNode cn = read(bytes);
        int extractorPatched = 0;
        int constructorHooks = 0;
        int readHooks = 0;
        int writeHooks = 0;

        addFieldIfMissing(cn, "htgt6BiomeIds", "[I");
        addFieldIfMissing(cn, "htgt6HighSectionData", "[B");
        addFieldIfMissing(cn, "htgt6HighSectionMask", "I");
        addFieldIfMissing(cn, "htgt6HighAddMask", "I");
        for (MethodNode mn : cn.methods) {
            if (("func_149269_a".equals(mn.name)||"extractChunkData".equals(mn.name))
                    && "(Lnet/minecraft/world/chunk/Chunk;ZI)Lnet/minecraft/network/play/server/S21PacketChunkData$Extracted;".equals(mn.desc)) {
                // Chunk storage has 24 slots, but the legacy extractor owns only
                // sections 0..15 and writes into its fixed 196864-byte scratch array.
                // High bits are serialized by HTGT6's sidecar after construction.
                InsnList lowMask=new InsnList();
                lowMask.add(new VarInsnNode(Opcodes.ILOAD,2));
                lowMask.add(new MethodInsnNode(Opcodes.INVOKESTATIC,SELF,"s21LowSectionMask120","(I)I",false));
                lowMask.add(new VarInsnNode(Opcodes.ISTORE,2));
                mn.instructions.insert(lowMask);
                extractorPatched++;
            }
            if ("<init>".equals(mn.name)
                    && "(Lnet/minecraft/world/chunk/Chunk;ZI)V".equals(mn.desc)) {
                constructorHooks += injectBeforeReturns(mn, "captureS21BiomeIds",
                        "(Ljava/lang/Object;Lnet/minecraft/world/chunk/Chunk;ZI)V",
                        new VarInsnNode(Opcodes.ALOAD, 0), new VarInsnNode(Opcodes.ALOAD, 1),
                        new VarInsnNode(Opcodes.ILOAD, 2),new VarInsnNode(Opcodes.ILOAD, 3));
            } else if (isPacketReadMethod(mn.name)
                    && "(Lnet/minecraft/network/PacketBuffer;)V".equals(mn.desc)) {
                readHooks += injectBeforeReturns(mn, "readS21BiomeIds",
                        "(Ljava/lang/Object;Lnet/minecraft/network/PacketBuffer;)V",
                        new VarInsnNode(Opcodes.ALOAD, 0), new VarInsnNode(Opcodes.ALOAD, 1));
            } else if (isPacketWriteMethod(mn.name)
                    && "(Lnet/minecraft/network/PacketBuffer;)V".equals(mn.desc)) {
                writeHooks += injectBeforeReturns(mn, "writeS21BiomeIds",
                        "(Ljava/lang/Object;Lnet/minecraft/network/PacketBuffer;)V",
                        new VarInsnNode(Opcodes.ALOAD, 0), new VarInsnNode(Opcodes.ALOAD, 1));
            }
        }
        if (extractorPatched != 1 || constructorHooks != 1 || readHooks != 1 || writeHooks != 1) {
            throw new IllegalStateException("S21 packet matcher drift: extractor=" + extractorPatched
                    + ", constructor=" + constructorHooks + ", read=" + readHooks + ", write=" + writeHooks);
        }
        LOGGER.info("HTGT6 transformed S21PacketChunkData with an int[256] biome extension");
        return write(cn);
    }

    public static int s21LowSectionMask120(int requestedMask){
        return requestedMask&com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.LOW_SECTION_MASK;
    }

    public static int legacyChunkSectionCount120(int storageLength) {
        return Math.min(storageLength, 16);
    }

    private static void replaceBiomeDictionaryMethod(MethodNode mn, String helper, String desc, int loadOpcode) {
        mn.instructions.clear();
        mn.tryCatchBlocks.clear();
        mn.localVariables = null;
        mn.instructions.add(new VarInsnNode(loadOpcode, 0));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF, helper, desc, false));
        mn.instructions.add(new org.objectweb.asm.tree.InsnNode(Opcodes.IRETURN));
        mn.maxStack = 1;
        mn.maxLocals = 1;
    }

    private static byte[] transformS26PacketMapChunkBulk(byte[] bytes) {
        ClassNode cn = read(bytes);
        int constructorHooks = 0;
        int readHooks = 0;
        int writeHooks = 0;
        addFieldIfMissing(cn, "htgt6BiomeIds", "[[I");
        addFieldIfMissing(cn, "htgt6HighSectionData", "[[B");
        addFieldIfMissing(cn, "htgt6HighSectionMasks", "[I");
        addFieldIfMissing(cn, "htgt6HighAddMasks", "[I");
        for (MethodNode mn : cn.methods) {
            if ("<init>".equals(mn.name) && mn.desc.startsWith("(Ljava/util/List;")) {
                constructorHooks += injectBeforeReturns(mn, "captureS26BiomeIds",
                        "(Ljava/lang/Object;Ljava/util/List;)V",
                        new VarInsnNode(Opcodes.ALOAD, 0), new VarInsnNode(Opcodes.ALOAD, 1));
            } else if (isPacketReadMethod(mn.name)
                    && "(Lnet/minecraft/network/PacketBuffer;)V".equals(mn.desc)) {
                readHooks += injectBeforeReturns(mn, "readS26BiomeIds",
                        "(Ljava/lang/Object;Lnet/minecraft/network/PacketBuffer;)V",
                        new VarInsnNode(Opcodes.ALOAD, 0), new VarInsnNode(Opcodes.ALOAD, 1));
            } else if (isPacketWriteMethod(mn.name)
                    && "(Lnet/minecraft/network/PacketBuffer;)V".equals(mn.desc)) {
                writeHooks += injectBeforeReturns(mn, "writeS26BiomeIds",
                        "(Ljava/lang/Object;Lnet/minecraft/network/PacketBuffer;)V",
                        new VarInsnNode(Opcodes.ALOAD, 0), new VarInsnNode(Opcodes.ALOAD, 1));
            }
        }
        if (constructorHooks != 1 || readHooks != 1 || writeHooks != 1) {
            throw new IllegalStateException("S26 packet matcher drift: constructor=" + constructorHooks
                    + ", read=" + readHooks + ", write=" + writeHooks);
        }
        LOGGER.info("HTGT6 transformed S26PacketMapChunkBulk with int[256] biome extensions");
        return write(cn);
    }

    private static byte[] transformNetHandlerPlayClient(byte[] bytes) {
        ClassNode cn = read(bytes);
        int attributeRangePatched=0;
        for (MethodNode mn : cn.methods) {
            if ("(Lnet/minecraft/network/play/server/S2FPacketSetSlot;)V".equals(mn.desc)) {
                LabelNode validPacket = new LabelNode();
                InsnList guard = new InsnList();
                guard.add(new VarInsnNode(Opcodes.ALOAD, 1));
                guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                        "ignoreInvalidSetSlot", "(Ljava/lang/Object;)Z", false));
                guard.add(new JumpInsnNode(Opcodes.IFEQ, validPacket));
                guard.add(new InsnNode(Opcodes.RETURN));
                guard.add(validPacket);
                mn.instructions.insert(guard);
            } else if ("(Lnet/minecraft/network/play/server/S21PacketChunkData;)V".equals(mn.desc)) {
                injectBeforeReturns(mn, "applyS21BiomeIds", "(Ljava/lang/Object;)V",
                        new VarInsnNode(Opcodes.ALOAD, 1));
            } else if ("(Lnet/minecraft/network/play/server/S26PacketMapChunkBulk;)V".equals(mn.desc)) {
                injectBeforeReturns(mn, "applyS26BiomeIds", "(Ljava/lang/Object;)V",
                        new VarInsnNode(Opcodes.ALOAD, 1));
            } else if ("(Lnet/minecraft/network/play/server/S22PacketMultiBlockChange;)V".equals(mn.desc)) {
                for (AbstractInsnNode instruction : mn.instructions.toArray()) {
                    if (!(instruction instanceof IntInsnNode)
                            || instruction.getOpcode() != Opcodes.SIPUSH
                            || ((IntInsnNode) instruction).operand != 255) continue;
                    AbstractInsnNode and = instruction.getNext();
                    if (and == null || and.getOpcode() != Opcodes.IAND) continue;
                    InsnList high = new InsnList();
                    high.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    high.add(new VarInsnNode(Opcodes.ILOAD, 5));
                    high.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF, "s22Y120",
                            "(ILjava/lang/Object;I)I", false));
                    mn.instructions.insert(and, high);
                    break;
                }
            } else if ("(Lnet/minecraft/network/play/server/S20PacketEntityProperties;)V".equals(mn.desc)) {
                // 1.7 creates unknown server attributes with default=0 but min=Double.MIN_VALUE.
                // That constructor is internally inconsistent and crashes as soon as a modded mob
                // synchronises an attribute absent on its client-side instance.  Keep the packet's
                // real base value intact and widen only the synthetic client's legal range.
                for(AbstractInsnNode instruction:mn.instructions.toArray()){
                    if(instruction instanceof LdcInsnNode
                            &&Double.valueOf(Double.MIN_NORMAL).equals(((LdcInsnNode)instruction).cst)){
                        ((LdcInsnNode)instruction).cst=Double.valueOf(-Double.MAX_VALUE);
                        attributeRangePatched++;
                    }
                }
            }
        }
        if(attributeRangePatched!=1)throw new IllegalStateException(
                "NetHandlerPlayClient synthetic attribute range matcher="+attributeRangePatched);
        LOGGER.info("HTGT6 transformed NetHandlerPlayClient biome extension, attribute range and stale-container packet guard");
        return write(cn);
    }

    /**
     * A destroyed lander can close its GUI before the last S2F slot update is
     * processed.  Vanilla then indexes the now-empty replacement container.
     */
    public static boolean ignoreInvalidSetSlot(Object packetObject) {
        if (!(packetObject instanceof net.minecraft.network.play.server.S2FPacketSetSlot)) return false;
        net.minecraft.network.play.server.S2FPacketSetSlot packet =
                (net.minecraft.network.play.server.S2FPacketSetSlot) packetObject;
        int windowId = packet.func_149175_c();
        if (windowId == -1) return false;
        net.minecraft.client.entity.EntityClientPlayerMP player =
                net.minecraft.client.Minecraft.getMinecraft().thePlayer;
        if (player == null) return false;
        int slot = packet.func_149173_d();
        net.minecraft.inventory.Container target = null;
        if (windowId == 0 && slot >= 36 && slot < 45) target = player.inventoryContainer;
        else if (player.openContainer != null && player.openContainer.windowId == windowId)
            target = player.openContainer;
        if (target == null) return false;
        int size = target.inventorySlots == null ? 0 : target.inventorySlots.size();
        if (slot >= 0 && slot < size) return false;
        LOGGER.warn("Ignored stale S2F slot {} for window {} with {} slots", slot, windowId, size);
        return true;
    }

    private static byte[] transformS23PacketBlockChange(byte[] bytes) {
        ClassNode cn = read(bytes);
        int constructorHooks = 0;
        int readHooks = 0;
        int writeHooks = 0;
        addFieldIfMissing(cn, "htgt6FuturaY", "Z");
        for (MethodNode mn : cn.methods) {
            if ("<init>".equals(mn.name) && "(IIILnet/minecraft/world/World;)V".equals(mn.desc)) {
                constructorHooks += injectBeforeReturns(mn, "captureS23Y120", "(Ljava/lang/Object;ILnet/minecraft/world/World;)V",
                        new VarInsnNode(Opcodes.ALOAD, 0), new VarInsnNode(Opcodes.ILOAD, 2),
                        new VarInsnNode(Opcodes.ALOAD, 4));
            } else if (isPacketReadMethod(mn.name)) {
                readHooks += injectBeforeReturns(mn, "readS23Y120",
                        "(Ljava/lang/Object;Lnet/minecraft/network/PacketBuffer;)V",
                        new VarInsnNode(Opcodes.ALOAD, 0), new VarInsnNode(Opcodes.ALOAD, 1));
            } else if (isPacketWriteMethod(mn.name)) {
                writeHooks += injectBeforeReturns(mn, "writeS23Y120",
                        "(Ljava/lang/Object;Lnet/minecraft/network/PacketBuffer;)V",
                        new VarInsnNode(Opcodes.ALOAD, 0), new VarInsnNode(Opcodes.ALOAD, 1));
            }
        }
        if (constructorHooks != 1 || readHooks != 1 || writeHooks != 1) {
            throw new IllegalStateException("S23 packet matcher drift: constructor=" + constructorHooks
                    + ", read=" + readHooks + ", write=" + writeHooks);
        }
        LOGGER.info("HTGT6 installed dormant Futura 9-bit S23 Y sidecar");
        return write(cn);
    }

    private static byte[] transformS22PacketMultiBlockChange(byte[] bytes) {
        ClassNode cn = read(bytes);
        int constructorHooks = 0;
        int readHooks = 0;
        int writeHooks = 0;
        addFieldIfMissing(cn, "htgt6FuturaYBits", "[B");
        for (MethodNode mn : cn.methods) {
            if ("<init>".equals(mn.name)
                    && "(I[SLnet/minecraft/world/chunk/Chunk;)V".equals(mn.desc)) {
                constructorHooks += injectBeforeReturns(mn, "captureS22Y120", "(Ljava/lang/Object;I[S)V",
                        new VarInsnNode(Opcodes.ALOAD, 0), new VarInsnNode(Opcodes.ILOAD, 1),
                        new VarInsnNode(Opcodes.ALOAD, 2));
            } else if (isPacketReadMethod(mn.name)) {
                readHooks += injectBeforeReturns(mn, "readS22Y120",
                        "(Ljava/lang/Object;Lnet/minecraft/network/PacketBuffer;)V",
                        new VarInsnNode(Opcodes.ALOAD, 0), new VarInsnNode(Opcodes.ALOAD, 1));
            } else if (isPacketWriteMethod(mn.name)) {
                writeHooks += injectBeforeReturns(mn, "writeS22Y120",
                        "(Ljava/lang/Object;Lnet/minecraft/network/PacketBuffer;)V",
                        new VarInsnNode(Opcodes.ALOAD, 0), new VarInsnNode(Opcodes.ALOAD, 1));
            }
        }
        if (constructorHooks != 1 || readHooks != 1 || writeHooks != 1) {
            throw new IllegalStateException("S22 packet matcher drift: constructor=" + constructorHooks
                    + ", read=" + readHooks + ", write=" + writeHooks);
        }
        LOGGER.info("HTGT6 installed dormant Futura 9-bit S22 Y sidecar");
        return write(cn);
    }

    private static byte[] transformPlayerInstanceBlockUpdates120(byte[] bytes) {
        ClassNode cn = read(bytes);
        addFieldIfMissing(cn, "htgt6FuturaPositions", "[I");
        int patched = 0;
        for (MethodNode mn : cn.methods) {
            if (!("flagChunkForUpdate".equals(mn.name) || "func_151253_a".equals(mn.name))
                    || !"(III)V".equals(mn.desc)) continue;
            InsnList entry = new InsnList();
            org.objectweb.asm.tree.LabelNode vanilla = new org.objectweb.asm.tree.LabelNode();
            entry.add(new VarInsnNode(Opcodes.ALOAD, 0));
            entry.add(new VarInsnNode(Opcodes.ILOAD, 1));
            entry.add(new VarInsnNode(Opcodes.ILOAD, 2));
            entry.add(new VarInsnNode(Opcodes.ILOAD, 3));
            entry.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF, "recordFuturaBlockUpdate120",
                    "(Ljava/lang/Object;III)Z", false));
            entry.add(new JumpInsnNode(Opcodes.IFEQ, vanilla));
            entry.add(new InsnNode(Opcodes.RETURN));
            entry.add(vanilla);
            mn.instructions.insert(entry);
            patched++;
        }
        if (patched != 1) throw new IllegalStateException("PlayerInstance block-update seam count " + patched);
        LOGGER.info("HTGT6 installed dormant Futura 9-bit PlayerInstance accumulator");
        return write(cn);
    }

    private static byte[] transformRenderGlobalHeight120(byte[] bytes) {
        ClassNode cn = read(bytes);
        String renderWorldField = null;
        for (FieldNode field : cn.fields) {
            if ("Lnet/minecraft/client/multiplayer/WorldClient;".equals(field.desc)) {
                renderWorldField = field.name;
                break;
            }
        }
        if (renderWorldField == null)
            throw new IllegalStateException("RenderGlobal WorldClient field not found");
        int capacity = 0, vertical = 0;
        boolean changed = false;
        for (MethodNode mn : cn.methods) {
            if ("<init>".equals(mn.name)
                    && "(Lnet/minecraft/client/Minecraft;)V".equals(mn.desc)) {
                for (AbstractInsnNode node : mn.instructions.toArray()) {
                    if (!(node instanceof IntInsnNode) || node.getOpcode() != Opcodes.BIPUSH
                            || (((IntInsnNode) node).operand != 16 && ((IntInsnNode) node).operand != 24)) continue;
                    AbstractInsnNode next = nextRealInstruction(node);
                    if (!(next instanceof VarInsnNode) || next.getOpcode() != Opcodes.ISTORE
                            || ((VarInsnNode) next).var != 3) continue;
                    if (((IntInsnNode) node).operand == 16) changed = true;
                    ((IntInsnNode) node).operand = 24;
                    capacity++;
                }
            } else if (("loadRenderers".equals(mn.name) || "func_72712_a".equals(mn.name))
                    && "()V".equals(mn.desc)) {
                for (AbstractInsnNode node : mn.instructions.toArray()) {
                    if (node instanceof MethodInsnNode) {
                        MethodInsnNode call = (MethodInsnNode) node;
                        if (call.getOpcode() == Opcodes.INVOKESTATIC && SELF.equals(call.owner)
                                && "renderSectionCount120".equals(call.name)
                                && "(Lnet/minecraft/world/World;)I".equals(call.desc)) {
                            AbstractInsnNode before = previousCode(node), after = nextRealInstruction(node);
                            if (!(before instanceof FieldInsnNode) || before.getOpcode() != Opcodes.GETFIELD
                                    || !RENDER_GLOBAL.equals(((FieldInsnNode)before).owner)
                                    || !renderWorldField.equals(((FieldInsnNode)before).name)
                                    || !isRenderHeightStore120(after))
                                throw new IllegalStateException("RenderGlobal existing height hook shape changed");
                            vertical++;
                        }
                        continue;
                    }
                    if (!(node instanceof IntInsnNode) || node.getOpcode() != Opcodes.BIPUSH
                            || ((IntInsnNode) node).operand != 16) continue;
                    AbstractInsnNode next = nextRealInstruction(node);
                    if (!isRenderHeightStore120(next)) continue;
                    InsnList replacement = new InsnList();
                    replacement.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    replacement.add(new FieldInsnNode(Opcodes.GETFIELD, RENDER_GLOBAL,
                            renderWorldField, "Lnet/minecraft/client/multiplayer/WorldClient;"));
                    replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                            "renderSectionCount120", "(Lnet/minecraft/world/World;)I", false));
                    mn.instructions.insertBefore(node, replacement);
                    mn.instructions.remove(node);
                    changed = true;
                    vertical++;
                }
            }
        }
        if (capacity != 1 || vertical != 1)
            throw new IllegalStateException("RenderGlobal height matcher capacity=" + capacity + ", vertical=" + vertical);
        LOGGER.info("HTGT6 transformed RenderGlobal for conditional 16/24-section grids");
        return changed ? write(cn) : bytes;
    }

    private static boolean isRenderHeightStore120(AbstractInsnNode node) {
        if (!(node instanceof FieldInsnNode) || node.getOpcode() != Opcodes.PUTFIELD) return false;
        FieldInsnNode field = (FieldInsnNode) node;
        return RENDER_GLOBAL.equals(field.owner) && "I".equals(field.desc)
                && ("renderChunksTall".equals(field.name) || "field_72763_n".equals(field.name));
    }

    private static byte[] validateWorldRendererHeight120(byte[] bytes) {
        ClassNode cn = read(bytes);
        int localSectionLoops = 0;
        for (MethodNode mn : cn.methods) {
            if (!("updateRenderer".equals(mn.name) || "func_147892_a".equals(mn.name))) continue;
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                if (node instanceof IntInsnNode && node.getOpcode() == Opcodes.BIPUSH
                        && ((IntInsnNode) node).operand == 16) localSectionLoops++;
                if (node instanceof IntInsnNode && node.getOpcode() == Opcodes.SIPUSH
                        && ((IntInsnNode) node).operand == 256)
                    throw new IllegalStateException("WorldRenderer retained a total-height 256 cap");
            }
        }
        if (localSectionLoops < 3)
            throw new IllegalStateException("WorldRenderer section-local loop contract changed: " + localSectionLoops);
        return bytes;
    }

    /** Angelica beta62's Sodium renderer assumes every column has exactly 16 sections. */
    private static byte[] transformSodiumChunkRenderColumnHeight120(byte[] bytes) {
        ClassNode cn = read(bytes);
        int patched = 0;
        for (MethodNode method : cn.methods) {
            if (!"<init>".equals(method.name) || !"(II)V".equals(method.desc)) continue;
            for (AbstractInsnNode node : method.instructions.toArray()) {
                if (!(node instanceof IntInsnNode) || node.getOpcode() != Opcodes.BIPUSH
                        || ((IntInsnNode) node).operand != 16) continue;
                AbstractInsnNode next = node.getNext();
                if (!(next instanceof org.objectweb.asm.tree.TypeInsnNode)
                        || next.getOpcode() != Opcodes.ANEWARRAY
                        || !"me/jellysquid/mods/sodium/client/render/chunk/ChunkRenderContainer"
                        .equals(((org.objectweb.asm.tree.TypeInsnNode) next).desc)) continue;
                ((IntInsnNode) node).operand = 24;
                patched++;
            }
        }
        if (patched != 1) throw new IllegalStateException(
                "Angelica ChunkRenderColumn section-array matcher=" + patched);
        LOGGER.info("HTGT6 expanded Angelica ChunkRenderColumn to 24 sections");
        return write(cn);
    }

    private static byte[] transformSodiumChunkRenderManagerHeight120(byte[] bytes) {
        ClassNode cn = read(bytes);
        int load = replaceIntConstantInMethod(cn, "loadSections", 16, 24);
        int unload = replaceIntConstantInMethod(cn, "unloadSections", 16, 24);
        int total = replaceIntConstantInMethod(cn, "getTotalSections", 16, 24);
        if (load != 1 || unload != 1 || total != 1) throw new IllegalStateException(
                "Angelica ChunkRenderManager height matcher load=" + load
                        + ", unload=" + unload + ", total=" + total);
        LOGGER.info("HTGT6 expanded Angelica ChunkRenderManager to 24 sections");
        return write(cn);
    }

    private static byte[] transformSodiumChunkGraphCullerHeight120(byte[] bytes) {
        ClassNode cn = read(bytes);
        int patched = 0;
        for (MethodNode method : cn.methods) {
            if (!"initSearch".equals(method.name)) continue;
            for (AbstractInsnNode node : method.instructions.toArray()) {
                if (!(node instanceof IntInsnNode) || node.getOpcode() != Opcodes.BIPUSH
                        || ((IntInsnNode) node).operand != 15) continue;
                AbstractInsnNode next = node.getNext();
                if (!(next instanceof MethodInsnNode)
                        || !"net/minecraft/util/MathHelper".equals(((MethodInsnNode) next).owner)
                        || !("clamp_int".equals(((MethodInsnNode) next).name)
                        || "func_76125_a".equals(((MethodInsnNode) next).name))) continue;
                ((IntInsnNode) node).operand = 23;
                patched++;
            }
        }
        if (patched != 1) throw new IllegalStateException(
                "Angelica ChunkGraphCuller camera clamp matcher=" + patched);
        LOGGER.info("HTGT6 expanded Angelica chunk visibility graph to section 23");
        return write(cn);
    }

    private static byte[] transformSodiumBuildLimit120(byte[] bytes, String owner) {
        ClassNode cn = read(bytes);
        int exclusiveLimit = 0;
        int inclusiveLightClamp = 0;
        for (MethodNode method : cn.methods) {
            for (AbstractInsnNode node : method.instructions.toArray()) {
                if (("isOutOfBuildLimitVertically".equals(method.name)
                        || "getLightBrightnessForSkyBlocks".equals(method.name)
                        || "func_72802_i".equals(method.name))
                        && node instanceof IntInsnNode && node.getOpcode() == Opcodes.SIPUSH
                        && ((IntInsnNode) node).operand == 256) {
                    ((IntInsnNode) node).operand = 384;
                    exclusiveLimit++;
                }
                // WorldSlice accepts Y < 384 after the outer guard above, but its
                // private light lookup separately clamps Y to 255. Building an
                // upper Futura section then subtracts a baseY above 255 and indexes
                // lightArrays with a negative section (observed as index -11).
                if (SODIUM_WORLD_SLICE.equals(owner) && "getLightLevel".equals(method.name)
                        && node instanceof IntInsnNode && node.getOpcode() == Opcodes.SIPUSH
                        && ((IntInsnNode) node).operand == 255) {
                    ((IntInsnNode) node).operand = 383;
                    inclusiveLightClamp++;
                }
            }
        }
        int expectedClamp=SODIUM_WORLD_SLICE.equals(owner)?1:0;
        if (exclusiveLimit != 1 || inclusiveLightClamp != expectedClamp)
            throw new IllegalStateException("Angelica build-limit matcher " + owner
                    + " exclusive=" + exclusiveLimit + ", lightClamp=" + inclusiveLightClamp);
        LOGGER.info("HTGT6 expanded Angelica build limit in {} to 384", owner);
        return write(cn);
    }

    private static int replaceIntConstantInMethod(ClassNode owner, String methodName,
                                                   int expected, int replacement) {
        int patched = 0;
        for (MethodNode method : owner.methods) {
            if (!methodName.equals(method.name)) continue;
            for (AbstractInsnNode node : method.instructions.toArray()) {
                if (node instanceof IntInsnNode && node.getOpcode() == Opcodes.BIPUSH
                        && ((IntInsnNode) node).operand == expected) {
                    ((IntInsnNode) node).operand = replacement;
                    patched++;
                }
            }
        }
        return patched;
    }

    private static byte[] transformWorldClientHeight120(byte[] bytes) {
        ClassNode cn = read(bytes);
        int patched = 0;
        for (MethodNode mn : cn.methods) {
            if (!("doPreChunk".equals(mn.name) || "func_73025_a".equals(mn.name))
                    || !"(IIZ)V".equals(mn.desc)) continue;
            for (AbstractInsnNode node : mn.instructions.toArray()) {
                if (!(node instanceof IntInsnNode) || node.getOpcode() != Opcodes.SIPUSH
                        || ((IntInsnNode) node).operand != 256) continue;
                InsnList replacement = new InsnList();
                replacement.add(new VarInsnNode(Opcodes.ALOAD, 0));
                replacement.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF,
                        "renderHeight120", "(Lnet/minecraft/world/World;)I", false));
                mn.instructions.insertBefore(node, replacement);
                mn.instructions.remove(node);
                patched++;
            }
        }
        if (patched != 1) throw new IllegalStateException("WorldClient render-height matcher count " + patched);
        LOGGER.info("HTGT6 transformed WorldClient render invalidation height");
        return write(cn);
    }

    public static int renderSectionCount120(net.minecraft.world.World world) {
        return renderSectionCount120(com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120.isFutura(world));
    }

    public static int renderSectionCount120(boolean futura) {
        return futura && com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.ActivationGate
                .CLIENT_RENDER_24_SECTIONS.isReady() ? 24 : 16;
    }

    public static int renderHeight120(net.minecraft.world.World world) {
        return renderSectionCount120(world) << 4;
    }

    public static int renderHeight120(boolean futura) {
        return renderSectionCount120(futura) << 4;
    }

    private static void addFieldIfMissing(ClassNode cn, String name, String desc) {
        for (FieldNode field : cn.fields) {
            if (name.equals(field.name)) return;
        }
        cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, name, desc, null, null));
    }

    private static int injectBeforeReturns(MethodNode mn, String helper, String desc,
                                            AbstractInsnNode... arguments) {
        int injected = 0;
        for (AbstractInsnNode node : mn.instructions.toArray()) {
            if (node.getOpcode() != Opcodes.RETURN) continue;
            InsnList call = new InsnList();
            for (AbstractInsnNode argument : arguments) call.add(argument.clone(null));
            call.add(new MethodInsnNode(Opcodes.INVOKESTATIC, SELF, helper, desc, false));
            mn.instructions.insertBefore(node, call);
            injected++;
        }
        return injected;
    }

    private static boolean isPacketReadMethod(String name) {
        return "readPacketData".equals(name) || "func_148837_a".equals(name);
    }

    private static boolean isPacketWriteMethod(String name) {
        return "writePacketData".equals(name) || "func_148840_b".equals(name);
    }

    public static void captureS23Y120(Object packet, int y, net.minecraft.world.World world) {
        if (!com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.ActivationGate
                .BLOCK_UPDATE_PACKET_9_BIT_Y.isReady()
                || !com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120.isFutura(world)) return;
        setField(packet, "htgt6FuturaY", Boolean.TRUE);
        setField(packet, "field_148885_b", Integer.valueOf(y));
    }

    public static void writeS23Y120(Object packet, net.minecraft.network.PacketBuffer buffer)
            throws java.io.IOException {
        if (!Boolean.TRUE.equals(getField(packet, "htgt6FuturaY"))) return;
        int y = intField(packet, "field_148885_b");
        buffer.writeByte(com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.BLOCK_UPDATE_MAGIC);
        buffer.writeByte(y >>> 8 & 1);
    }

    public static void readS23Y120(Object packet, net.minecraft.network.PacketBuffer buffer)
            throws java.io.IOException {
        if (buffer.readableBytes() == 0) return;
        if (buffer.readableBytes() != 2 || buffer.readUnsignedByte()
                != com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.BLOCK_UPDATE_MAGIC)
            throw new java.io.IOException("Invalid Futura S23 Y sidecar");
        int high = buffer.readUnsignedByte();
        if ((high & ~1) != 0) throw new java.io.IOException("Invalid Futura S23 Y bit: " + high);
        setField(packet, "field_148885_b", Integer.valueOf(intField(packet, "field_148885_b") | high << 8));
        setField(packet, "htgt6FuturaY", Boolean.TRUE);
    }

    public static void writeS22Y120(Object packet, net.minecraft.network.PacketBuffer buffer)
            throws java.io.IOException {
        byte[] bits = (byte[]) getField(packet, "htgt6FuturaYBits");
        if (bits == null) return;
        int count = intField(packet, "field_148924_d");
        if (bits.length != count) throw new java.io.IOException("S22 Y-bit count mismatch");
        buffer.writeByte(com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.BLOCK_UPDATE_MAGIC);
        buffer.writeShort(count);
        buffer.writeBytes(bits);
    }

    public static void readS22Y120(Object packet, net.minecraft.network.PacketBuffer buffer)
            throws java.io.IOException {
        if (buffer.readableBytes() == 0) return;
        if (buffer.readUnsignedByte()
                != com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.BLOCK_UPDATE_MAGIC)
            throw new java.io.IOException("Invalid Futura S22 Y sidecar");
        int count = buffer.readUnsignedShort();
        if (count != intField(packet, "field_148924_d") || buffer.readableBytes() != count)
            throw new java.io.IOException("Invalid Futura S22 Y-bit count: " + count);
        byte[] bits = new byte[count];
        buffer.readBytes(bits);
        for (byte bit : bits) if ((bit & ~1) != 0)
            throw new java.io.IOException("Invalid Futura S22 Y bit: " + bit);
        setField(packet, "htgt6FuturaYBits", bits);
    }

    public static int s22Y120(int lowY, Object packet, int index) {
        byte[] bits = (byte[]) getField(packet, "htgt6FuturaYBits");
        return bits != null && index >= 0 && index < bits.length ? lowY | (bits[index] & 1) << 8 : lowY;
    }

    public static boolean recordFuturaBlockUpdate120(Object instance, int x, int y, int z) {
        if (!com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.ActivationGate
                .BLOCK_UPDATE_PACKET_9_BIT_Y.isReady()) return false;
        Object manager = getField(instance, "this$0");
        if (!(manager instanceof net.minecraft.server.management.PlayerManager)) return false;
        net.minecraft.world.World world =
                ((net.minecraft.server.management.PlayerManager) manager).getWorldServer();
        if (!com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120.isFutura(world)) return false;
        int packed = com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120
                .packLocalPosition(x, y, z);
        short[] low = (short[]) getField(instance, "locationOfBlockChange", "field_151254_d");
        int count = intField(instance, "numberOfTilesToUpdate", "field_73262_e", "e");
        int[] full = (int[]) getField(instance, "htgt6FuturaPositions");
        if (low == null) low = new short[64];
        if (full == null) full = new int[low.length];
        for (int index = 0; index < count; index++) if (full[index] == packed) return true;
        if (count == 0) addPlayerInstanceToUpdateList120(manager, instance);
        setNamedField(instance,
                Integer.valueOf(intField(instance, "flagsYAreasToUpdate", "field_73260_f", "f") | 1 << (y >>> 4)),
                "flagsYAreasToUpdate", "field_73260_f", "f");
        if (count == low.length) low = java.util.Arrays.copyOf(low, Math.max(1, low.length << 1));
        if (count == full.length) full = java.util.Arrays.copyOf(full, Math.max(1, full.length << 1));
        low[count] = (short) ((x & 15) << 12 | (z & 15) << 8 | y & 255);
        full[count] = packed;
        count++;
        setNamedField(instance, low, "locationOfBlockChange", "field_151254_d", "d");
        setField(instance, "htgt6FuturaPositions", full);
        setNamedField(instance, Integer.valueOf(count),
                "numberOfTilesToUpdate", "field_73262_e", "e");
        registerS22Positions120(low, full, count);
        return true;
    }

    private static void addPlayerInstanceToUpdateList120(Object manager, Object instance) {
        Object value = getField(manager, "chunkWatcherWithPlayers", "field_72697_d", "e");
        if (!(value instanceof java.util.List))
            throw new IllegalStateException("Cannot resolve PlayerManager chunk update list");
        java.util.List updates = (java.util.List) value;
        if (!updates.contains(instance)) updates.add(instance);
    }

    public static void registerS22Positions120(short[] low, int[] full, int count) {
        if (low == null || full == null || count < 0 || count > low.length || count > full.length)
            throw new IllegalArgumentException("Invalid Futura S22 accumulator");
        FUTURA_BLOCK_UPDATE_POSITIONS.put(low, java.util.Arrays.copyOf(full, count));
    }

    public static void captureS22Y120(Object packet, int count, short[] low) {
        int[] full = FUTURA_BLOCK_UPDATE_POSITIONS.get(low);
        if (full == null || full.length != count) return;
        byte[] bits = new byte[count];
        for (int index = 0; index < count; index++) {
            int packedLow = low[index] & 65535;
            int packed = full[index];
            if (com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.localX(packed)
                    != (packedLow >>> 12 & 15)
                    || com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.localZ(packed)
                    != (packedLow >>> 8 & 15)
                    || (com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.y(packed) & 255)
                    != (packedLow & 255)) return;
            bits[index] = (byte) (com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.y(packed) >>> 8);
        }
        setField(packet, "htgt6FuturaYBits", bits);
    }

    public static void captureS21BiomeIds(Object packet, net.minecraft.world.chunk.Chunk chunk,
                                          boolean fullChunk,int requestedMask) {
        int[] ids = getExtendedBiomeArray(chunk);
        validateOceanBiomeChunk(chunk, chunk.worldObj.getWorldChunkManager(), ids);
        putPacketBiomeIds(packet, ids);
        HighSectionPayload payload=extractHighSections(chunk,fullChunk,requestedMask);
        setField(packet,"htgt6HighSectionData",payload.data);
        setField(packet,"htgt6HighSectionMask",Integer.valueOf(payload.sectionMask));
        setField(packet,"htgt6HighAddMask",Integer.valueOf(payload.addMask));
    }

    public static void writeS21BiomeIds(Object packet, net.minecraft.network.PacketBuffer buffer)
            throws java.io.IOException {
        writeBiomeIds(buffer, (int[]) getPacketBiomeIds(packet));
        writeHeightHeader(buffer);
        writeHighPayload(buffer,(byte[])getField(packet,"htgt6HighSectionData"),
                intField(packet,"htgt6HighSectionMask"),intField(packet,"htgt6HighAddMask"));
    }

    public static void readS21BiomeIds(Object packet, net.minecraft.network.PacketBuffer buffer)
            throws java.io.IOException {
        putPacketBiomeIds(packet, readBiomeIds(buffer));
        readHeightHeader(buffer);
        HighSectionPayload payload=readHighPayload(buffer);
        setField(packet,"htgt6HighSectionData",payload.data);
        setField(packet,"htgt6HighSectionMask",Integer.valueOf(payload.sectionMask));
        setField(packet,"htgt6HighAddMask",Integer.valueOf(payload.addMask));
    }

    public static void captureS26BiomeIds(Object packet, java.util.List chunks) {
        int[][] ids = new int[chunks.size()][];
        for (int i = 0; i < ids.length; i++) {
            net.minecraft.world.chunk.Chunk chunk = (net.minecraft.world.chunk.Chunk) chunks.get(i);
            ids[i] = getExtendedBiomeArray(chunk);
            validateOceanBiomeChunk(chunk, chunk.worldObj.getWorldChunkManager(), ids[i]);
        }
        putPacketBiomeIds(packet, ids);
        byte[][] data=new byte[chunks.size()][];int[] masks=new int[chunks.size()],addMasks=new int[chunks.size()];
        for(int i=0;i<data.length;i++){
            HighSectionPayload payload=extractHighSections((net.minecraft.world.chunk.Chunk)chunks.get(i),true,
                    com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.HIGH_SECTION_MASK);
            data[i]=payload.data;masks[i]=payload.sectionMask;addMasks[i]=payload.addMask;
        }
        setField(packet,"htgt6HighSectionData",data);setField(packet,"htgt6HighSectionMasks",masks);
        setField(packet,"htgt6HighAddMasks",addMasks);
    }

    public static void writeS26BiomeIds(Object packet, net.minecraft.network.PacketBuffer buffer)
            throws java.io.IOException {
        int[][] ids = (int[][]) getPacketBiomeIds(packet);
        buffer.writeShort(ids == null ? 0 : ids.length);
        if (ids != null) for (int[] id : ids) writeBiomeIds(buffer, id);
        writeHeightHeader(buffer);
        byte[][] data=(byte[][])getField(packet,"htgt6HighSectionData");
        int[] masks=(int[])getField(packet,"htgt6HighSectionMasks"),addMasks=(int[])getField(packet,"htgt6HighAddMasks");
        int count=data==null?0:data.length;buffer.writeShort(count);
        for(int i=0;i<count;i++)writeHighPayload(buffer,data[i],masks[i],addMasks[i]);
    }

    public static void readS26BiomeIds(Object packet, net.minecraft.network.PacketBuffer buffer)
            throws java.io.IOException {
        int count = buffer.readUnsignedShort();
        int[][] ids = new int[count][];
        for (int i = 0; i < count; i++) ids[i] = readBiomeIds(buffer);
        putPacketBiomeIds(packet, ids);
        readHeightHeader(buffer);
        int highCount=buffer.readUnsignedShort();
        if(highCount!=count)throw new java.io.IOException("S26 high-section count "+highCount+" != chunk count "+count);
        byte[][] data=new byte[count][];int[] masks=new int[count],addMasks=new int[count];
        for(int i=0;i<count;i++){HighSectionPayload payload=readHighPayload(buffer);data[i]=payload.data;
            masks[i]=payload.sectionMask;addMasks[i]=payload.addMask;}
        setField(packet,"htgt6HighSectionData",data);setField(packet,"htgt6HighSectionMasks",masks);
        setField(packet,"htgt6HighAddMasks",addMasks);
    }

    private static void writeBiomeIds(net.minecraft.network.PacketBuffer buffer, int[] ids)
            throws java.io.IOException {
        if (ids == null || ids.length != 256) ids = new int[256];
        for (int id : ids) buffer.writeInt(id);
    }

    private static int[] readBiomeIds(net.minecraft.network.PacketBuffer buffer) throws java.io.IOException {
        int[] ids = new int[256];
        for (int i = 0; i < ids.length; i++) ids[i] = buffer.readInt();
        return ids;
    }

    public static void applyS21BiomeIds(Object packet) {
        int[] ids = (int[]) getPacketBiomeIds(packet);
        if (ids != null) applyBiomeIds(packet, ids);
        Integer x=(Integer)getField(packet,"field_149284_a","chunkX","xPosition");
        Integer z=(Integer)getField(packet,"field_149282_b","chunkZ","zPosition");
        if(x!=null&&z!=null)applyHighSections(x.intValue(),z.intValue(),
                (byte[])getField(packet,"htgt6HighSectionData"),intField(packet,"htgt6HighSectionMask"),
                intField(packet,"htgt6HighAddMask"));
    }

    public static void applyS26BiomeIds(Object packet) {
        int[][] ids = (int[][]) getPacketBiomeIds(packet);
        if (ids == null) return;
        int[] xs = (int[]) getField(packet, "field_149266_a", "chunkXPositions");
        int[] zs = (int[]) getField(packet, "field_149264_b", "chunkZPositions");
        if (xs == null || zs == null) return;
        for (int i = 0; i < ids.length && i < xs.length && i < zs.length; i++) {
            applyBiomeIds(xs[i], zs[i], ids[i]);
        }
        byte[][] data=(byte[][])getField(packet,"htgt6HighSectionData");
        int[] masks=(int[])getField(packet,"htgt6HighSectionMasks"),addMasks=(int[])getField(packet,"htgt6HighAddMasks");
        if(data!=null&&masks!=null&&addMasks!=null)for(int i=0;i<data.length&&i<xs.length&&i<zs.length;i++)
            applyHighSections(xs[i],zs[i],data[i],masks[i],addMasks[i]);
    }

    private static HighSectionPayload extractHighSections(net.minecraft.world.chunk.Chunk chunk,
                                                           boolean fullChunk,int requestedMask){
        net.minecraft.world.chunk.storage.ExtendedBlockStorage[] sections=chunk.getBlockStorageArray();
        if(sections.length<=16)return new HighSectionPayload(0,0,new byte[0]);
        int allowed=fullChunk?com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.HIGH_SECTION_MASK
                :requestedMask&com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.HIGH_SECTION_MASK;
        int mask=0,addMask=0,size=0;boolean sky=!chunk.worldObj.provider.hasNoSky;boolean neid=false;
        for(int section=16;section<Math.min(24,sections.length);section++){
            net.minecraft.world.chunk.storage.ExtendedBlockStorage storage=sections[section];
            if(storage==null||(fullChunk&&storage.isEmpty())||(allowed&(1<<section))==0)continue;
            neid|=hasMethod(storage,"getBlockData")&&hasMethod(storage,"getBlockMeta");
            mask|=1<<section;size+=4096+2048+2048+(sky?2048:0);
            if(storage.getBlockMSBArray()!=null){addMask|=1<<section;size+=2048;}
        }
        // NotEnoughIDs replaces the vanilla byte/nibble pair with two complete
        // 16-bit arrays. Chunk.fillChunk is mixin-patched to require those 8192-byte
        // streams, so feeding it our legacy sidecar caused the visible Angelica
        // chunk rebuild/flicker loop. Keep a self-identifying NEID representation.
        if(neid){size=4;addMask=0;for(int section=16;section<Math.min(24,sections.length);section++)
            if((mask&(1<<section))!=0)size+=8192+8192+2048+(sky?2048:0);}
        byte[] data=new byte[size];int offset=0;
        if(neid){data[offset++]='N';data[offset++]='1';data[offset++]='6';data[offset++]='B';
            for(int pass=0;pass<4;pass++)for(int section=16;section<Math.min(24,sections.length);section++){
                if((mask&(1<<section))==0)continue;net.minecraft.world.chunk.storage.ExtendedBlockStorage storage=sections[section];byte[] source;
                if(pass==0)source=invokeBytes(storage,"getBlockData");else if(pass==1)source=invokeBytes(storage,"getBlockMeta");
                else if(pass==2)source=storage.getBlocklightArray().data;else {if(!sky)continue;source=storage.getSkylightArray().data;}
                System.arraycopy(source,0,data,offset,source.length);offset+=source.length;
            }return new HighSectionPayload(mask,0,data);}
        for(int pass=0;pass<5;pass++)for(int section=16;section<Math.min(24,sections.length);section++){
            if((mask&(1<<section))==0||(pass==4&&(addMask&(1<<section))==0))continue;
            net.minecraft.world.chunk.storage.ExtendedBlockStorage storage=sections[section];byte[] source;
            if(pass==0)source=storage.getBlockLSBArray();
            else if(pass==1)source=storage.getMetadataArray().data;
            else if(pass==2)source=storage.getBlocklightArray().data;
            else if(pass==3){if(!sky)continue;source=storage.getSkylightArray().data;}
            else source=storage.getBlockMSBArray().data;
            System.arraycopy(source,0,data,offset,source.length);offset+=source.length;
        }
        return new HighSectionPayload(mask,addMask,data);
    }

    private static void writeHeightHeader(net.minecraft.network.PacketBuffer buffer)throws java.io.IOException{
        buffer.writeInt(com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.MAGIC);
        buffer.writeByte(com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.VERSION);
    }
    private static void readHeightHeader(net.minecraft.network.PacketBuffer buffer)throws java.io.IOException{
        int magic=buffer.readInt(),version=buffer.readUnsignedByte();
        if(magic!=com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.MAGIC
                ||version!=com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.VERSION)
            throw new java.io.IOException("Unsupported Futura height sidecar "+Integer.toHexString(magic)+"/"+version);
    }
    private static void writeHighPayload(net.minecraft.network.PacketBuffer buffer,byte[] data,int mask,int addMask)
            throws java.io.IOException{
        if(data==null)data=new byte[0];buffer.writeByte(com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.highMask(mask));
        buffer.writeByte(com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.highMask(addMask));
        buffer.writeInt(data.length);buffer.writeBytes(data);
    }
    private static HighSectionPayload readHighPayload(net.minecraft.network.PacketBuffer buffer)throws java.io.IOException{
        int mask=com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.joinMasks(0,buffer.readUnsignedByte());
        int addMask=com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeightPacket120.joinMasks(0,buffer.readUnsignedByte());
        int length=buffer.readInt();if(length<0||length>393472)throw new java.io.IOException("Invalid high-section payload "+length);
        byte[] data=new byte[length];buffer.readBytes(data);return new HighSectionPayload(mask,addMask,data);
    }
    private static int intField(Object owner,String... names){Object value=getField(owner,names);return value instanceof Number?((Number)value).intValue():0;}

    private static void applyHighSections(int x,int z,byte[] data,int mask,int addMask){
        if(data==null||mask==0)return;
        try{
            net.minecraft.client.Minecraft client=net.minecraft.client.Minecraft.getMinecraft();
            net.minecraft.client.multiplayer.WorldClient world=client.theWorld;if(world==null)return;
            net.minecraft.world.chunk.Chunk chunk=world.getChunkFromChunkCoords(x,z);
            applyHighSectionArrays(chunk,data,mask,addMask);
            world.markBlockRangeForRenderUpdate(x<<4,256,z<<4,(x<<4)+15,383,(z<<4)+15);
        }catch(Throwable error){LOGGER.error("Could not apply Futura high sections for chunk "+x+","+z,error);}
    }

    /** Direct sidecar decoder. It deliberately avoids Chunk.fillChunk because NEID rewrites that method's wire contract. */
    private static void applyHighSectionArrays(net.minecraft.world.chunk.Chunk chunk,byte[] data,int mask,int addMask)throws Exception{
        net.minecraft.world.chunk.storage.ExtendedBlockStorage[] sections=chunk.getBlockStorageArray();boolean sky=!chunk.worldObj.provider.hasNoSky;
        boolean neid=data.length>=4&&data[0]=='N'&&data[1]=='1'&&data[2]=='6'&&data[3]=='B';int offset=neid?4:0;
        for(int section=16;section<Math.min(24,sections.length);section++)if((mask&(1<<section))!=0&&sections[section]==null)
            sections[section]=new net.minecraft.world.chunk.storage.ExtendedBlockStorage(section<<4,sky);
        if(neid){
            for(int pass=0;pass<4;pass++)for(int section=16;section<Math.min(24,sections.length);section++){
                if((mask&(1<<section))==0||(pass==3&&!sky))continue;net.minecraft.world.chunk.storage.ExtendedBlockStorage storage=sections[section];int length=pass<2?8192:2048;
                requirePayload(data,offset,length);if(pass==0)invokeStorageSetter(storage,"setBlockData",data,offset);else if(pass==1)invokeStorageSetter(storage,"setBlockMeta",data,offset);
                else if(pass==2)System.arraycopy(data,offset,storage.getBlocklightArray().data,0,length);else System.arraycopy(data,offset,storage.getSkylightArray().data,0,length);offset+=length;
            }
        }else{
            for(int pass=0;pass<5;pass++)for(int section=16;section<Math.min(24,sections.length);section++){
                if((mask&(1<<section))==0||(pass==4&&(addMask&(1<<section))==0))continue;net.minecraft.world.chunk.storage.ExtendedBlockStorage storage=sections[section];int length=pass==0?4096:2048;
                if(pass==3&&!sky)continue;requirePayload(data,offset,length);byte[] copy=new byte[length];System.arraycopy(data,offset,copy,0,length);offset+=length;
                if(pass==0)storage.setBlockLSBArray(copy);else if(pass==1)storage.setBlockMetadataArray(new net.minecraft.world.chunk.NibbleArray(copy,4));
                else if(pass==2)storage.setBlocklightArray(new net.minecraft.world.chunk.NibbleArray(copy,4));else if(pass==3)storage.setSkylightArray(new net.minecraft.world.chunk.NibbleArray(copy,4));
                else storage.setBlockMSBArray(new net.minecraft.world.chunk.NibbleArray(copy,4));
            }
        }
        if(offset!=data.length)throw new java.io.IOException("High-section payload trailing bytes "+(data.length-offset));
        for(int section=16;section<Math.min(24,sections.length);section++)if((mask&(1<<section))!=0){if(!neid&&(addMask&(1<<section))==0)sections[section].clearMSBArray();sections[section].removeInvalidBlocks();}
        chunk.setChunkModified();
    }
    private static void requirePayload(byte[] data,int offset,int length)throws java.io.IOException{if(offset<0||length<0||offset+length>data.length)throw new java.io.IOException("Truncated high-section payload at "+offset+"/"+data.length+" need "+length);}
    private static boolean hasMethod(Object owner,String name){try{owner.getClass().getMethod(name);return true;}catch(Throwable ignored){return false;}}
    private static byte[] invokeBytes(Object owner,String name){try{return (byte[])owner.getClass().getMethod(name).invoke(owner);}catch(Throwable error){throw new IllegalStateException("Cannot read NEID high section via "+name,error);}}
    private static void invokeStorageSetter(Object owner,String name,byte[] data,int offset)throws Exception{owner.getClass().getMethod(name,byte[].class,Integer.TYPE).invoke(owner,data,Integer.valueOf(offset));}

    private static final class HighSectionPayload{final int sectionMask,addMask;final byte[] data;
        HighSectionPayload(int sectionMask,int addMask,byte[] data){this.sectionMask=sectionMask;this.addMask=addMask;this.data=data;}}

    private static void applyBiomeIds(Object packet, int[] ids) {
        Integer x = (Integer) getField(packet, "field_149284_a", "chunkX", "xPosition");
        Integer z = (Integer) getField(packet, "field_149282_b", "chunkZ", "zPosition");
        if (x != null && z != null) applyBiomeIds(x.intValue(), z.intValue(), ids);
    }

    private static void applyBiomeIds(int x, int z, int[] ids) {
        try {
            net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getMinecraft();
            net.minecraft.client.multiplayer.WorldClient world = client.theWorld;
            if (world == null) return;
            net.minecraft.world.chunk.Chunk chunk = world.getChunkFromChunkCoords(x, z);
            int[] target = getExtendedBiomeArray(chunk);
            System.arraycopy(ids, 0, target, 0, 256);
            VALIDATED_OCEAN_BIOME_CHUNKS.put(chunk, Boolean.TRUE);
        } catch (Throwable t) {
            LOGGER.error("Could not apply extended biome packet for chunk " + x + "," + z, t);
        }
    }

    private static Object getPacketBiomeIds(Object packet) {
        return getField(packet, "htgt6BiomeIds");
    }

    private static void putPacketBiomeIds(Object packet, Object ids) {
        setField(packet, "htgt6BiomeIds", ids);
    }

    private static Object getField(Object owner, String... names) {
        if (owner == null) return null;
        for (Class<?> type = owner.getClass(); type != null; type = type.getSuperclass()) {
            for (String name : names) {
                try {
                    java.lang.reflect.Field field = type.getDeclaredField(name);
                    field.setAccessible(true);
                    return field.get(owner);
                } catch (ReflectiveOperationException ignored) { }
            }
        }
        return null;
    }

    private static void setField(Object owner, String name, Object value) {
        try {
            java.lang.reflect.Field field = owner.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(owner, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Missing transformed field " + name, e);
        }
    }

    private static void setNamedField(Object owner, Object value, String... names) {
        if (owner == null) throw new IllegalStateException("Cannot set field on null owner");
        for (Class<?> type = owner.getClass(); type != null; type = type.getSuperclass()) {
            for (String name : names) {
                try {
                    java.lang.reflect.Field field = type.getDeclaredField(name);
                    field.setAccessible(true);
                    field.set(owner, value);
                    return;
                } catch (NoSuchFieldException ignored) {
                    // Try the next MCP/SRG/notch alias.
                } catch (IllegalAccessException error) {
                    throw new IllegalStateException("Cannot set field " + name, error);
                }
            }
        }
        throw new IllegalStateException("Missing field aliases " + java.util.Arrays.toString(names));
    }

    /* --------------------------------------------------------------------- */
    /* ASM utilities                                                          */
    /* --------------------------------------------------------------------- */

    private static String normalizeClassName(String name) {
        return name == null ? "" : name.replace('.', '/');
    }

    private static boolean isClass(String expected, String name, String transformedName) {
        return expected.equals(name) || expected.equals(transformedName);
    }

    private static boolean isAmunRaWorldProvider(String name, String transformedName) {
        return isAmunRaWorldProvider(name) || isAmunRaWorldProvider(transformedName);
    }

    private static boolean isAmunRaWorldProvider(String name) {
        return name != null && name.startsWith(AMUN_RA_WORLD_PROVIDER_PREFIX)
                && name.endsWith("WorldProvider") && !name.endsWith("AmunraWorldProvider");
    }

    private static ClassNode read(byte[] bytes) {
        ClassNode cn = new ClassNode();
        new ClassReader(bytes).accept(cn, 0);
        return cn;
    }

    private static byte[] transformBountifulEnderman120(byte[] bytes) {
        ClassNode cn=read(bytes);int count=0;
        for(MethodNode m:cn.methods)if((m.name.equals("findPlayerToAttack")||m.name.equals("func_70782_k"))&&m.desc.equals("()Lnet/minecraft/entity/Entity;"))for(AbstractInsnNode n:m.instructions.toArray())if(n.getOpcode()==Opcodes.ARETURN){
            InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,SELF,"endermiteTarget120","(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/monster/EntityEnderman;)Lnet/minecraft/entity/Entity;",false));m.instructions.insertBefore(n,code);count++;
        }
        if(count==0)throw new IllegalStateException("Enderman target hook missing");return write(cn);
    }
    public static net.minecraft.entity.Entity endermiteTarget120(net.minecraft.entity.Entity existing,net.minecraft.entity.monster.EntityEnderman enderman){
        if(existing!=null)return existing;
        net.minecraft.entity.Entity best=null;double distance=4096;
        for(Object value:enderman.worldObj.getEntitiesWithinAABB(com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulEndermite.class,enderman.boundingBox.expand(64,4,64))){
            com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulEndermite mite=(com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulEndermite)value;
            double d=enderman.getDistanceSqToEntity(mite);
            if(mite.isEntityAlive()&&mite.isPlayerSpawned()&&d<distance&&enderman.canEntityBeSeen(mite)){best=mite;distance=d;}
        }return best;
    }
    private static byte[] transformCombatBrewing120(byte[] bytes){
        ClassNode cn=read(bytes);int count=0;
        for(MethodNode method:cn.methods){
            boolean can=method.name.equals("canBrew")||method.name.equals("func_145934_k");
            boolean brew=method.name.equals("brewPotions")||method.name.equals("func_145940_l");
            if(!can&&!brew)continue;
            InsnList code=new InsnList();LabelNode original=new LabelNode();code.add(new VarInsnNode(Opcodes.ALOAD,0));
            code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/mod/htgt6/minecraft1202/Module/Gameplay/Combat/CombatBrewing120",can?"canBrew":"brew","(Lnet/minecraft/tileentity/TileEntityBrewingStand;)Z",false));
            code.add(new JumpInsnNode(Opcodes.IFEQ,original));if(can)code.add(new InsnNode(Opcodes.ICONST_1));code.add(new InsnNode(can?Opcodes.IRETURN:Opcodes.RETURN));code.add(original);
            method.instructions.insert(code);count++;
        }
        if(count!=2)throw new IllegalStateException("Lingering brewing hook drift: "+count);return write(cn);
    }
    private static byte[] transformBountifulFurnace120(byte[] bytes){
        ClassNode cn=read(bytes);int count=0;
        for(MethodNode m:cn.methods)if(m.name.equals("smeltItem")||m.name.equals("func_145949_j"))for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof FieldInsnNode&&n.getOpcode()==Opcodes.PUTFIELD&&n.getPrevious().getOpcode()==Opcodes.ISUB){
            InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,SELF,"spongeBucket120","(Lnet/minecraft/tileentity/TileEntityFurnace;)V",false));m.instructions.insertBefore(n,code);count++;
        }
        if(count!=1)throw new IllegalStateException("Sponge furnace hook drift: "+count);
        return NetherWoodFuelASM116.transform(write(cn));
    }
    public static void spongeBucket120(net.minecraft.tileentity.TileEntityFurnace furnace){
        net.minecraft.item.ItemStack input=furnace.getStackInSlot(0),fuel=furnace.getStackInSlot(1);
        if(input!=null&&input.getItem()==net.minecraft.item.Item.getItemFromBlock(com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.sponge)&&input.getItemDamage()==1&&fuel!=null&&fuel.getItem()==net.minecraft.init.Items.bucket)
            furnace.setInventorySlotContents(1,new net.minecraft.item.ItemStack(net.minecraft.init.Items.water_bucket));
    }
    private static byte[] transformBountifulLanding120(byte[] bytes) {
        ClassNode cn=read(bytes);int patched=0;
        for(MethodNode m:cn.methods)if((m.name.equals("moveEntity")||m.name.equals("func_70091_d"))&&m.desc.equals("(DDD)V")) {
            AbstractInsnNode zero=null;
            for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof FieldInsnNode&&n.getOpcode()==Opcodes.PUTFIELD) {
                FieldInsnNode field=(FieldInsnNode)n;
                if((field.name.equals("motionY")||field.name.equals("field_70181_x"))&&n.getPrevious().getOpcode()==Opcodes.DCONST_0)zero=n.getPrevious();
            }
            if(zero!=null){InsnList call=new InsnList();call.add(new VarInsnNode(Opcodes.ALOAD,0));
                call.add(new MethodInsnNode(Opcodes.INVOKESTATIC,SELF,"slimeLanding120","(Lnet/minecraft/entity/Entity;)D",false));
                m.instructions.insertBefore(zero,call);m.instructions.remove(zero);patched++;}
        }
        if(patched!=1)throw new IllegalStateException("Slime landing hook drift: "+patched);
        return write(cn);
    }
    public static double slimeLanding120(net.minecraft.entity.Entity e) {
        if(!e.onGround||e.isSneaking()||e.motionY>=0)return 0;
        int x=net.minecraft.util.MathHelper.floor_double(e.posX),y=net.minecraft.util.MathHelper.floor_double(e.boundingBox.minY-.2),z=net.minecraft.util.MathHelper.floor_double(e.posZ);
        return e.worldObj.getBlock(x,y,z)==com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.slimeBlock
                ?-e.motionY*(e instanceof net.minecraft.entity.EntityLivingBase?1:.8):0;
    }
    private static byte[] transformCombatOakBoat120(byte[] bytes){
        ClassNode cn=read(bytes);int count=0;
        for(MethodNode m:cn.methods)if((m.name.equals("onItemRightClick")||m.name.equals("func_77659_a"))&&m.desc.equals("(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;")){
            m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();
            m.instructions.add(new VarInsnNode(Opcodes.ALOAD,1));m.instructions.add(new VarInsnNode(Opcodes.ALOAD,2));m.instructions.add(new VarInsnNode(Opcodes.ALOAD,3));
            m.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"com/mod/htgt6/minecraft1202/Module/Gameplay/Combat/BoatPlacement120","oak",m.desc,false));
            m.instructions.add(new InsnNode(Opcodes.ARETURN));count++;
        }
        if(count!=1)throw new IllegalStateException("Oak boat placement hook drift: "+count);return write(cn);
    }
    private static byte[] transformCombatLevitation120(byte[] bytes) {
        ClassNode cn=read(bytes);int count=0;
        for(MethodNode m:cn.methods)if((m.name.equals("moveEntityWithHeading")||m.name.equals("func_70612_e"))&&m.desc.equals("(FF)V"))
            for(AbstractInsnNode n:m.instructions.toArray())if(isCombatAirGravity120(n)) {
                InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));
                code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "com/mod/htgt6/minecraft1202/Module/Gameplay/Combat/PotionLevitation","gravityAdjustment",
                    "(DLnet/minecraft/entity/EntityLivingBase;)D",false));m.instructions.insert(n,code);count++;
            }
        if(count!=1)throw new IllegalStateException("Levitation travel hook drift: "+count);
        return write(cn);
    }

    /** Crucible moves sword blocking into a Guava damage-modifier Function. */
    private static byte[] transformCrucibleShieldModifier(byte[] bytes) {
        ClassNode cn=read(bytes);
        if (!cn.interfaces.contains("com/google/common/base/Function")) return bytes;
        int hooks=0;
        for (MethodNode method:cn.methods) {
            if (!"apply".equals(method.name) || !"(Ljava/lang/Double;)Ljava/lang/Double;".equals(method.desc)) continue;
            for (AbstractInsnNode instruction:method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call=(MethodInsnNode)instruction;
                if (call.getOpcode()!=Opcodes.INVOKEVIRTUAL || !"net/minecraft/entity/player/EntityPlayer".equals(call.owner)
                        || !"()Z".equals(call.desc) || !("isBlocking".equals(call.name)||"func_70632_aY".equals(call.name))) continue;
                method.instructions.set(call,new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/mod/htgt6/minecraft1202/Module/Gameplay/Combat/ShieldMechanics120",
                        "usesLegacyDamageBlock","(Lnet/minecraft/entity/player/EntityPlayer;)Z",false));
                hooks++;
            }
        }
        if(hooks==0)return bytes;
        LOGGER.info("HTGT6 adapted Crucible shield damage modifier {}, hooks={}",cn.name,hooks);
        return write(cn);
    }
    private static boolean isCombatAirGravity120(AbstractInsnNode instruction) {
        if(instruction instanceof LdcInsnNode)
            return Double.valueOf(.08D).equals(((LdcInsnNode)instruction).cst);
        if(!(instruction instanceof MethodInsnNode))return false;
        MethodInsnNode call=(MethodInsnNode)instruction;
        // MicdoodleCore replaces the vanilla constant before HTGT6 runs. Wrap
        // its result so planetary gravity survives when levitation is absent.
        return call.getOpcode()==Opcodes.INVOKESTATIC
                && call.owner.equals("micdoodle8/mods/galacticraft/core/util/WorldUtil")
                && call.name.equals("getGravityForEntity")
                && call.desc.equals("(Lnet/minecraft/entity/Entity;)D");
    }
    private static byte[] transformBubbleBreathing120(byte[] bytes) {
        ClassNode node=read(bytes);int hooks=0;
        for(MethodNode method:node.methods)if(method.desc.equals("(I)I")
                &&(method.name.equals("decreaseAirSupply")||method.name.equals("func_70682_h"))) {
            LabelNode ordinary=new LabelNode();InsnList code=new InsnList();
            code.add(new VarInsnNode(Opcodes.ALOAD,0));code.add(new VarInsnNode(Opcodes.ILOAD,1));
            code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                    "com/mod/htgt6/minecraft1202/Module/World/Frostburn/FuturaBubbleColumn120","airSupply",
                    "(Lnet/minecraft/entity/EntityLivingBase;I)I",false));
            code.add(new InsnNode(Opcodes.DUP));code.add(new LdcInsnNode(Integer.MIN_VALUE));
            code.add(new JumpInsnNode(Opcodes.IF_ICMPEQ,ordinary));code.add(new InsnNode(Opcodes.IRETURN));
            code.add(ordinary);code.add(new InsnNode(Opcodes.POP));method.instructions.insert(code);hooks++;
        }
        if(hooks!=1)throw new IllegalStateException("Bubble breathing method drift: "+hooks);
        return write(node);
    }
    private static byte[] transformBountifulWater120(byte[] bytes) {
        ClassNode cn=read(bytes);int acceleration=0,drag=0;
        for(MethodNode m:cn.methods)if((m.name.equals("moveEntityWithHeading")||m.name.equals("func_70612_e"))&&m.desc.equals("(FF)V")) {
            int waterDrag=0;
            for(AbstractInsnNode n:m.instructions.toArray()) {
                if(n instanceof MethodInsnNode&&acceleration==0) {
                    MethodInsnNode c=(MethodInsnNode)n;
                    if((c.name.equals("moveFlying")||c.name.equals("func_70060_a"))&&c.desc.equals("(FFF)V")) {
                        InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,SELF,"depthAcceleration120","(FLnet/minecraft/entity/EntityLivingBase;)F",false));m.instructions.insertBefore(n,code);acceleration++;
                    }
                }
                if(n instanceof LdcInsnNode&&Double.valueOf(.800000011920929D).equals(((LdcInsnNode)n).cst)&&waterDrag<3) {
                    if(waterDrag!=1){InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,SELF,"depthDrag120","(DLnet/minecraft/entity/EntityLivingBase;)D",false));m.instructions.insert(n,code);drag++;}waterDrag++;
                }
            }
        }
        if(acceleration!=1||drag!=2)throw new IllegalStateException("Depth Strider hooks drift: "+acceleration+"/"+drag);
        return write(cn);
    }
    private static float depthLevel120(net.minecraft.entity.EntityLivingBase e) {
        net.minecraft.item.ItemStack boots=e.getEquipmentInSlot(1);
        int level=boots==null?0:net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.depthStrider.effectId,boots);
        return Math.min(3,level)*(e.onGround?1:.5F);
    }
    public static float depthAcceleration120(float legacy,net.minecraft.entity.EntityLivingBase e){
        float f=depthLevel120(e);
        boolean bear=e instanceof com.mod.htgt6.minecraft1202.Module.Entity.Frostburn.EntityFrostburnPolarBear;
        return f==0?(bear?.02F:legacy):.02F+(e.getAIMoveSpeed()-.02F)*f/3;
    }
    public static double depthDrag120(double legacy,net.minecraft.entity.EntityLivingBase e){
        float f=depthLevel120(e);
        boolean bear=e instanceof com.mod.htgt6.minecraft1202.Module.Entity.Frostburn.EntityFrostburnPolarBear;
        float base=com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.PlayerSwimming120.waterDrag(e,bear?.98F:.8F);
        double drag=f==0?(base!=.8F?(double)base:legacy):base+(.54600006F-base)*f/3;
        return com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.AquaticEffects120.drag(drag,e);
    }
    private static byte[] transformBountifulPearl120(byte[] bytes) {
        ClassNode cn=read(bytes);int count=0;
        for(MethodNode m:cn.methods)if(m.name.equals("onImpact")||m.name.equals("func_70184_a"))for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof MethodInsnNode) {
            MethodInsnNode c=(MethodInsnNode)n;
            boolean vanilla=(c.name.equals("setPositionAndUpdate")||c.name.equals("func_70634_a"))&&c.desc.equals("(DDD)V");
            // Crucible's onImpact replaces the vanilla move with this call,
            // inside the successful Forge + Bukkit teleport-event branches.
            boolean crucible=c.getOpcode()==Opcodes.INVOKEVIRTUAL
                    && "net/minecraft/network/NetHandlerPlayServer".equals(c.owner)
                    && "teleport".equals(c.name)&&"(Lorg/bukkit/Location;)V".equals(c.desc);
            if(vanilla||crucible) {
                InsnList code=new InsnList();code.add(new VarInsnNode(Opcodes.ALOAD,0));code.add(new MethodInsnNode(Opcodes.INVOKESTATIC,SELF,"pearlEndermite120","(Lnet/minecraft/entity/item/EntityEnderPearl;)V",false));m.instructions.insertBefore(n,code);count++;
            }
        }
        if(count!=1)throw new IllegalStateException("Pearl hook drift: "+count);return write(cn);
    }
    public static void pearlEndermite120(net.minecraft.entity.item.EntityEnderPearl pearl){
        net.minecraft.entity.EntityLivingBase player=pearl.getThrower();
        if(!(player instanceof net.minecraft.entity.player.EntityPlayer)||pearl.worldObj.isRemote||!pearl.worldObj.getGameRules().getGameRuleBooleanValue("doMobSpawning")||pearl.worldObj.rand.nextFloat()>=.05F)return;
        com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulEndermite mite=new com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulEndermite(pearl.worldObj);
        mite.setPlayerSpawned(true);mite.setLocationAndAngles(player.posX,player.posY,player.posZ,player.rotationYaw,0);pearl.worldObj.spawnEntityInWorld(mite);
    }

    private static byte[] write(ClassNode cn) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                // ASM's default implementation uses the ClassWriter class loader.
                // That loader cannot resolve vanilla obfuscated names (for example
                // "bdc") when HTGT6 is started by a production LaunchWrapper.
                // Conservative Object frames are valid for the injected control
                // flow and, unlike reflective class loading, cannot recursively
                // trigger another LaunchClassLoader transformation.
                return "java/lang/Object";
            }
        };
        cn.accept(cw);
        return cw.toByteArray();
    }

    private static int findLocalByType(MethodNode mn, String desc) {
        // Method arguments are live for every return. LocalVariableTable entries
        // may describe a short-lived temporary with the same type.
        Type[] args = Type.getArgumentTypes(mn.desc);
        int slot = ((mn.access & Opcodes.ACC_STATIC) != 0) ? 0 : 1;
        for (Type type : args) {
            if (desc.equals(type.getDescriptor())) return slot;
            slot += type.getSize();
        }

        if (mn.localVariables != null) {
            for (Object o : mn.localVariables) {
                org.objectweb.asm.tree.LocalVariableNode lv =
                        (org.objectweb.asm.tree.LocalVariableNode) o;
                if (desc.equals(lv.desc)) return lv.index;
            }
        }
        return -1;
    }

    /** LaunchClassLoader creates coremod transformers reflectively. */
    public HTGT6Transformer() {}
}
