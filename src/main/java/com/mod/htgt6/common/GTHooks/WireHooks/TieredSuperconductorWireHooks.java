package com.mod.htgt6.common.GTHooks.WireHooks;

import com.mod.htgt6.api.hitech6.Misc.MachineTierSystem;
import com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator;
import gregapi.block.multitileentity.MultiTileEntityClassContainer;
import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregapi.data.CS;
import gregapi.oredict.OreDictMaterial;
import gregapi.tileentity.connectors.MultiTileEntityWireElectric;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Registers the physical GT6 MultiTileEntity wires for every HTGT6 power tier. */
public final class TieredSuperconductorWireHooks {
    /** 32 materials x 16 thicknesses. The GT6 jar leaves 31100..31611 unused. */
    public static final int FIRST_MTE_ID = 31100;
    public static final int WIRES_PER_MATERIAL = 16;
    private static final int ELECTRIC_WIRE_CREATIVE_TAB_ID = 28366;

    private static boolean registered;
    private static boolean scheduled;

    /**
     * HTGT6 init runs before GT6's deliberately delayed init. Queueing on the
     * GT6 lifecycle gives us both prerequisites at once: its wire block exists
     * and GT6 still permits OreDict target registration.
     */
    public static synchronized void scheduleAfterGTInit() {
        if (registered || scheduled) return;
        if (CS.GT == null) {
            throw new IllegalStateException("GT6 lifecycle object is not available");
        }
        if (CS.GT.mFinishedInit) {
            registerAll();
            return;
        }
        CS.GT.mAfterInit.add(new Runnable() {
            @Override
            public void run() {
                registerAll();
            }
        });
        scheduled = true;
    }

    public static synchronized void registerAll() {
        if (registered) return;

        MultiTileEntityRegistry registry = MultiTileEntityRegistry.getRegistry("gt.multitileentity");
        if (registry == null) {
            throw new IllegalStateException("GT6 MultiTileEntity registry is not available");
        }
        Object wireBlock = getExistingWireBlock(registry);

        validateIdRange(registry);
        int materialIndex = 0;
        for (int tier = MachineTierSystem.LV; tier <= MachineTierSystem.MAX_TIER; tier++) {
            OreDictMaterial base = GT6MaterialAutoGenerator.superconductorBase(tier);
            int baseId = FIRST_MTE_ID + materialIndex++ * WIRES_PER_MATERIAL;
            long voltage = MachineTierSystem.voltage(tier);
            // Round upward: an integer EU/m loss must never become lower than 30%.
            long loss = (voltage * 3L + 9L) / 10L;
            addElectricWires(
                    baseId, ELECTRIC_WIRE_CREATIVE_TAB_ID,
                    voltage, 5L, loss, loss,
                    true, false, false,
                    registry, wireBlock, MultiTileEntityTieredSuperconductorWire.class, base);
            // A base wire is deliberately capped at 5 A for every thickness.
            applyElectricalProfile(registry, baseId, loss, false);
        }
        for (int tier = MachineTierSystem.LV; tier <= MachineTierSystem.MAX_TIER; tier++) {
            OreDictMaterial superconductor = GT6MaterialAutoGenerator.superconductor(tier);
            int baseId = FIRST_MTE_ID + materialIndex++ * WIRES_PER_MATERIAL;
            long voltage = MachineTierSystem.voltage(tier);
            addElectricWires(
                    baseId, ELECTRIC_WIRE_CREATIVE_TAB_ID,
                    voltage, 8L, 0L, 0L,
                    true, false, false,
                    registry, wireBlock, MultiTileEntityTieredSuperconductorWire.class,
                    superconductor);
            applyElectricalProfile(registry, baseId, 0L, true);
        }
        registered = true;
    }

    /**
     * GT6's public method mentions its block class, whose optional Botania API
     * interface is intentionally absent from some server packs. Reflection
     * keeps that optional API out of HTGT6's hard linkage while still calling
     * GT6's own wire registrar.
     */
    private static void addElectricWires(int id, int creativeTabId, long voltage,
            long amperage, long loss, long insulatedLoss,
            boolean contactDamage, boolean insulatedContactDamage, boolean addCables,
            MultiTileEntityRegistry registry, Object wireBlock,
            Class<?> wireClass, OreDictMaterial material) {
        try {
            Method target = null;
            for (Method method : MultiTileEntityWireElectric.class.getDeclaredMethods()) {
                if ("addElectricWires".equals(method.getName())
                        && method.getParameterTypes().length == 13) {
                    target = method;
                    break;
                }
            }
            if (target == null) throw new NoSuchMethodException("addElectricWires/13");
            target.invoke(null, id, creativeTabId, voltage, amperage, loss, insulatedLoss,
                    contactDamage, insulatedContactDamage, addCables,
                    registry, wireBlock, wireClass, material);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to invoke GT6 electric wire registrar", e);
        }
    }

    private static Object getExistingWireBlock(MultiTileEntityRegistry registry) {
        MultiTileEntityClassContainer existingWire = registry.getClassContainer(29950);
        if (existingWire == null) {
            throw new IllegalStateException("GT6 reference wire MTE 29950 is not registered yet");
        }
        try {
            Field blockField = MultiTileEntityClassContainer.class.getField("mBlock");
            return blockField.get(existingWire);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to obtain GT6 wire block", e);
        }
    }

    private static void applyElectricalProfile(MultiTileEntityRegistry registry, int firstId,
            long loss, boolean completedSuperconductor) {
        for (int thickness = 1; thickness <= WIRES_PER_MATERIAL; thickness++) {
            MultiTileEntityClassContainer container = registry.getClassContainer(firstId + thickness - 1);
            if (container == null) {
                throw new IllegalStateException("GT6 did not register wire MTE "
                        + (firstId + thickness - 1));
            }
            long amperage = completedSuperconductor ? 8L + (thickness - 1L) * 4L : 5L;
            container.mParameters.setLong("gt.pipebandwidth", amperage);
            container.mParameters.setLong("gt.pipeloss", loss);
            if (container.mCanonicalTileEntity instanceof MultiTileEntityWireElectric) {
                MultiTileEntityWireElectric wire =
                        (MultiTileEntityWireElectric) container.mCanonicalTileEntity;
                wire.mAmperage = amperage;
                wire.mLoss = loss;
            }
        }
    }

    private static void validateIdRange(MultiTileEntityRegistry registry) {
        int lastId = FIRST_MTE_ID
                + GT6MaterialAutoGenerator.SUPERCONDUCTOR_TIER_COUNT * 2 * WIRES_PER_MATERIAL - 1;
        for (int id = FIRST_MTE_ID; id <= lastId; id++) {
            if (registry.getClassContainer(id) != null) {
                throw new IllegalStateException("GT6 MultiTileEntity ID " + id
                        + " is already occupied; HTGT6 superconductors require "
                        + FIRST_MTE_ID + ".." + lastId);
            }
        }
    }

    private TieredSuperconductorWireHooks() { }
}
