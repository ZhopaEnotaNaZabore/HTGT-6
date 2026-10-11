package com.mod.htgt6.api.hitech6.multiblock;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable, reusable multiblock definition. Machine implementations describe
 * only their local geometry; traversal, rotation, counting and gateway rules live here.
 */
public final class MultiblockStructureDefinition {
    public interface PositionMatcher {
        Match match(Context context);
    }

    public static final class Match {
        private static final Match VALID = new Match(true, null);
        private static final Match INVALID = new Match(false, null);
        public final boolean valid;
        public final String elementKey;

        private Match(boolean valid, String elementKey) {
            this.valid = valid;
            this.elementKey = elementKey;
        }

        public static Match valid() { return VALID; }
        public static Match valid(String elementKey) {
            return elementKey == null || elementKey.isEmpty() ? VALID : new Match(true, elementKey);
        }
        public static Match invalid() { return INVALID; }
    }

    public static final class Context {
        public final MultiTileEntityBase controller;
        public final World world;
        public final int u, y, v;
        public final int x, worldY, z;
        public final Block block;
        public final int metadata;
        public final TileEntity tileEntity;

        private Context(MultiTileEntityBase controller, int u, int y, int v, int[] worldPosition) {
            this.controller = controller;
            this.world = controller.getWorldObj();
            this.u = u;
            this.y = y;
            this.v = v;
            this.x = worldPosition[0];
            this.worldY = worldPosition[1];
            this.z = worldPosition[2];
            this.block = world.getBlock(x, worldY, z);
            this.metadata = world.getBlockMetadata(x, worldY, z);
            this.tileEntity = world.getTileEntity(x, worldY, z);
        }

        public boolean isAir() { return block == null || block.isAir(world, x, worldY, z); }
        public IMultiblockGateway getGateway() {
            return tileEntity instanceof IMultiblockGateway ? (IMultiblockGateway) tileEntity : null;
        }
        public GatewayType getGatewayType() {
            IMultiblockGateway gateway = getGateway();
            return gateway == null ? null : gateway.getGatewayType();
        }
    }

    public static final class GatewayRequirement {
        public final int minimum;
        public final int maximum;

        private GatewayRequirement(int minimum, int maximum) {
            this.minimum = Math.max(0, minimum);
            this.maximum = maximum < 0 ? Integer.MAX_VALUE : Math.max(this.minimum, maximum);
        }
    }

    public static final class Result {
        private final boolean formed;
        private final String diagnostic;
        private final int failedU, failedY, failedV;
        private final GatewayType failedGateway;
        private final Map<String, Integer> elementCounts;
        private final EnumMap<GatewayType, Integer> gatewayCounts;
        private final List<IMultiblockGateway> gateways;

        private Result(boolean formed, String diagnostic, int failedU, int failedY, int failedV,
                       GatewayType failedGateway, Map<String, Integer> elementCounts,
                       EnumMap<GatewayType, Integer> gatewayCounts, List<IMultiblockGateway> gateways) {
            this.formed = formed;
            this.diagnostic = diagnostic;
            this.failedU = failedU;
            this.failedY = failedY;
            this.failedV = failedV;
            this.failedGateway = failedGateway;
            this.elementCounts = Collections.unmodifiableMap(new HashMap<String, Integer>(elementCounts));
            this.gatewayCounts = new EnumMap<GatewayType, Integer>(gatewayCounts);
            this.gateways = Collections.unmodifiableList(new ArrayList<IMultiblockGateway>(gateways));
        }

        public boolean isFormed() { return formed; }
        public String getDiagnostic() { return diagnostic; }
        public int getFailedU() { return failedU; }
        public int getFailedY() { return failedY; }
        public int getFailedV() { return failedV; }
        public GatewayType getFailedGateway() { return failedGateway; }
        public int getElementCount(String key) {
            Integer count = elementCounts.get(key);
            return count == null ? 0 : count;
        }
        public int getGatewayCount(GatewayType type) {
            Integer count = gatewayCounts.get(type);
            return count == null ? 0 : count;
        }
        public Map<String, Integer> getElementCounts() { return elementCounts; }
        public List<IMultiblockGateway> getGateways() { return gateways; }
    }

    public static final class Builder {
        private final int minU, maxU, minY, maxY, minV, maxV;
        private final PositionMatcher matcher;
        private final EnumMap<GatewayType, GatewayRequirement> gatewayRequirements =
                new EnumMap<GatewayType, GatewayRequirement>(GatewayType.class);
        private boolean requireHorizontalFacing = true;

        private Builder(int minU, int maxU, int minY, int maxY, int minV, int maxV,
                        PositionMatcher matcher) {
            if (matcher == null) throw new IllegalArgumentException("Position matcher cannot be null");
            if (minU > maxU || minY > maxY || minV > maxV) throw new IllegalArgumentException("Invalid bounds");
            this.minU = minU; this.maxU = maxU;
            this.minY = minY; this.maxY = maxY;
            this.minV = minV; this.maxV = maxV;
            this.matcher = matcher;
        }

        public Builder requireHorizontalFacing(boolean required) {
            this.requireHorizontalFacing = required;
            return this;
        }
        public Builder requireGateway(GatewayType type, int minimum) {
            return gateway(type, minimum, -1);
        }
        public Builder optionalGateway(GatewayType type) {
            return gateway(type, 0, -1);
        }
        public Builder gateway(GatewayType type, int minimum, int maximum) {
            if (type == null) throw new IllegalArgumentException("Gateway type cannot be null");
            gatewayRequirements.put(type, new GatewayRequirement(minimum, maximum));
            return this;
        }
        public MultiblockStructureDefinition build() { return new MultiblockStructureDefinition(this); }
    }

    private final int minU, maxU, minY, maxY, minV, maxV;
    private final PositionMatcher matcher;
    private final EnumMap<GatewayType, GatewayRequirement> gatewayRequirements;
    private final boolean requireHorizontalFacing;

    private MultiblockStructureDefinition(Builder builder) {
        minU = builder.minU; maxU = builder.maxU;
        minY = builder.minY; maxY = builder.maxY;
        minV = builder.minV; maxV = builder.maxV;
        matcher = builder.matcher;
        gatewayRequirements = new EnumMap<GatewayType, GatewayRequirement>(builder.gatewayRequirements);
        requireHorizontalFacing = builder.requireHorizontalFacing;
    }

    public static Builder builder(int minU, int maxU, int minY, int maxY, int minV, int maxV,
                                  PositionMatcher matcher) {
        return new Builder(minU, maxU, minY, maxY, minV, maxV, matcher);
    }

    public Result validate(MultiTileEntityBase controller) {
        Map<String, Integer> elements = new HashMap<String, Integer>();
        EnumMap<GatewayType, Integer> gatewayCounts = emptyGatewayCounts();
        List<IMultiblockGateway> gateways = new ArrayList<IMultiblockGateway>();
        int facing = controller.getWorldObj().getBlockMetadata(controller.xCoord, controller.yCoord, controller.zCoord);
        if (requireHorizontalFacing && (facing < 2 || facing > 5)) {
            return result(false, "INVALID CONTROLLER FACING", 0, 0, 0, null, elements, gatewayCounts, gateways);
        }
        for (int y = minY; y <= maxY; y++) {
            for (int u = minU; u <= maxU; u++) {
                for (int v = minV; v <= maxV; v++) {
                    int[] position = controller.localToWorld(u, y, v);
                    if (!controller.getWorldObj().blockExists(position[0], position[1], position[2])) {
                        return result(false, "STRUCTURE ERROR @ " + u + "," + y + "," + v,
                                u, y, v, null, elements, gatewayCounts, gateways);
                    }
                    Context context = new Context(controller, u, y, v, position);
                    Match match = matcher.match(context);
                    if (match == null || !match.valid) {
                        return result(false, "STRUCTURE ERROR @ " + u + "," + y + "," + v,
                                u, y, v, null, elements, gatewayCounts, gateways);
                    }
                    if (match.elementKey != null) increment(elements, match.elementKey);
                    IMultiblockGateway gateway = context.getGateway();
                    if (gateway != null) {
                        increment(gatewayCounts, gateway.getGatewayType());
                        gateways.add(gateway);
                    }
                }
            }
        }
        // The controller's energy role is authoritative, even if a new generator
        // forgets to declare its mandatory output in the structure builder.
        if (gatewayCounts.get(GatewayType.ENERGY_OUTPUT) < controller.getEnergyRole().requiredOutputs()) {
            return result(false, "MISSING GATEWAY: " + GatewayType.ENERGY_OUTPUT.serializedName,
                    0, 0, 0, GatewayType.ENERGY_OUTPUT, elements, gatewayCounts, gateways);
        }
        for (Map.Entry<GatewayType, GatewayRequirement> entry : gatewayRequirements.entrySet()) {
            int count = gatewayCounts.get(entry.getKey());
            GatewayRequirement requirement = entry.getValue();
            if (count < requirement.minimum) {
                return result(false, "MISSING GATEWAY: " + entry.getKey().serializedName,
                        0, 0, 0, entry.getKey(), elements, gatewayCounts, gateways);
            }
            if (count > requirement.maximum) {
                return result(false, "TOO MANY GATEWAYS: " + entry.getKey().serializedName,
                        0, 0, 0, entry.getKey(), elements, gatewayCounts, gateways);
            }
        }
        return result(true, "STRUCTURE: FORMED / ALL GATEWAYS LINKED",
                0, 0, 0, null, elements, gatewayCounts, gateways);
    }

    /** Missing chunks are not evidence of a broken structure. Do not load them here. */
    public boolean areChunksLoaded(MultiTileEntityBase controller) {
        for (int u = minU; u <= maxU; u++) for (int v = minV; v <= maxV; v++) {
            int[] p = controller.localToWorld(u, 0, v);
            if (!controller.getWorldObj().getChunkProvider().chunkExists(p[0] >> 4, p[2] >> 4)) return false;
        }
        return true;
    }

    public List<IMultiblockGateway> findGateways(MultiTileEntityBase controller) {
        List<IMultiblockGateway> result = new ArrayList<IMultiblockGateway>();
        for (int y = minY; y <= maxY; y++) for (int u = minU; u <= maxU; u++) for (int v = minV; v <= maxV; v++) {
            int[] p = controller.localToWorld(u, y, v);
            if (!controller.getWorldObj().blockExists(p[0], p[1], p[2])) continue;
            TileEntity tile = controller.getWorldObj().getTileEntity(p[0], p[1], p[2]);
            if (tile instanceof IMultiblockGateway) result.add((IMultiblockGateway) tile);
        }
        return result;
    }

    private static Result result(boolean formed, String diagnostic, int u, int y, int v, GatewayType gateway,
                                 Map<String, Integer> elements, EnumMap<GatewayType, Integer> gatewayCounts,
                                 List<IMultiblockGateway> gateways) {
        return new Result(formed, diagnostic, u, y, v, gateway, elements, gatewayCounts, gateways);
    }
    private static EnumMap<GatewayType, Integer> emptyGatewayCounts() {
        EnumMap<GatewayType, Integer> counts = new EnumMap<GatewayType, Integer>(GatewayType.class);
        for (GatewayType type : GatewayType.VALUES) counts.put(type, 0);
        return counts;
    }
    private static <K> void increment(Map<K, Integer> map, K key) {
        Integer current = map.get(key);
        map.put(key, current == null ? 1 : current + 1);
    }
}
