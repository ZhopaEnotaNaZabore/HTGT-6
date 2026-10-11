package com.mod.htgt6.minecraft1202.Module.Registry.Combat;

import com.mod.htgt6.minecraft1202.Module.Client.Combat.CombatUpdateClient;

import com.mod.htgt6.minecraft1202.Minecraft1202;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.CombatEventHandler;
import com.mod.htgt6.minecraft1202.Module.World.Combat.CombatWorldGenerator;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatShulker;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatShulkerBullet;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatArrow;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityLingeringCloud;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityLingeringPotion;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatZombieVillager;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatBoat;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityDragonBreathFireball;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatBoatSeat;
import com.mod.htgt6.minecraft1202.Module.World.Combat.IglooNbtGenerator;
import com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketElytraStart;
import com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketSweepAttackEffect;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.EndMechanics;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatBrewing120;
import com.mod.htgt6.minecraft1202.Module.World.Combat.EndCityGenerator;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.TileEntityEndGateway;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.TileEntityCombatCommandBlock;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.TileEntityDragonHead;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Lifecycle entry point for the self-contained Minecraft Java 1.9 Combat Update module. */
public final class CombatUpdate {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-CombatUpdate");
    private static boolean initialized;
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("MC1202_Combat");
    public static final int COMBAT_BOAT_MOD_ID=43;
    public static final int DRAGON_FIREBALL_MOD_ID=44;
    public static final int COMBAT_BOAT_SEAT_MOD_ID=45;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        CombatContent.register();
        NETWORK.registerMessage(PacketElytraStart.Handler.class, PacketElytraStart.class, 0, Side.SERVER);
        NETWORK.registerMessage(PacketSweepAttackEffect.Handler.class, PacketSweepAttackEffect.class, 1, Side.CLIENT);
        NETWORK.registerMessage(com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketElytraState.Handler.class,com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketElytraState.class,2,Side.CLIENT);
        GameRegistry.registerTileEntity(TileEntityEndGateway.class, "htgt6:EndGateway19");
        GameRegistry.registerTileEntity(TileEntityCombatCommandBlock.class, "htgt6:CombatCommandBlock19");
        GameRegistry.registerTileEntity(TileEntityDragonHead.class, "htgt6:DragonHead19");
        EntityRegistry.registerModEntity(EntityCombatShulker.class, "CombatShulker", 14, Minecraft1202.instance, 64, 3, true);
        EntityRegistry.registerModEntity(EntityCombatShulkerBullet.class, "CombatShulkerBullet", 15, Minecraft1202.instance, 96, 1, true);
        EntityRegistry.registerModEntity(EntityCombatArrow.class, "CombatArrow", 16, Minecraft1202.instance, 96, 1, true);
        EntityRegistry.registerModEntity(EntityLingeringCloud.class, "LingeringCloud", 17, Minecraft1202.instance, 96, 5, false);
        EntityRegistry.registerModEntity(EntityLingeringPotion.class, "LingeringPotion", 18, Minecraft1202.instance, 64, 10, true);
        int zombieVillagerId = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(EntityCombatZombieVillager.class, "HTGT6.ZombieVillager",
                zombieVillagerId, 5651507, 7969893);
        EntityRegistry.registerModEntity(EntityCombatZombieVillager.class, "ZombieVillager", 19,
                Minecraft1202.instance, 80, 3, true);
        // 43 follows the complete shared 0..42 backport entity range.
        EntityRegistry.registerModEntity(EntityCombatBoat.class, "CombatBoat", COMBAT_BOAT_MOD_ID,
                Minecraft1202.instance, 80, 3, true);
        EntityRegistry.registerModEntity(EntityDragonBreathFireball.class, "DragonBreathFireball", DRAGON_FIREBALL_MOD_ID,
                Minecraft1202.instance, 96, 1, true);
        EntityRegistry.registerModEntity(EntityCombatBoatSeat.class, "CombatBoatSecondSeat", COMBAT_BOAT_SEAT_MOD_ID,
                Minecraft1202.instance, 80, 1, false);
        CombatEventHandler handler = new CombatEventHandler();
        MinecraftForge.EVENT_BUS.register(handler);
        EndMechanics endMechanics = new EndMechanics();
        MinecraftForge.EVENT_BUS.register(endMechanics);
        FMLCommonHandler.instance().bus().register(endMechanics);
        FMLCommonHandler.instance().bus().register(handler);
        GameRegistry.registerWorldGenerator(new CombatWorldGenerator(), 18);
        registerClient();
        LOGGER.info("Minecraft Java 1.9 - Combat Update module initialized");
    }

    private static void registerClient() {
        if (!FMLCommonHandler.instance().getSide().isClient()) return;
        try { Class.forName("com.mod.htgt6.minecraft1202.Module.Client.Combat.CombatUpdateClient").getMethod("init").invoke(null); }
        catch (Exception exception) { throw new IllegalStateException("Unable to initialize Minecraft Java 1.9 client content", exception); }
    }
    private CombatUpdate() { }
}
