package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

/** Pure constants and calculations mirrored from Minecraft Java 1.20.2. */
public final class ShieldMechanics120 {
    public static final int USE_DURATION = 72000;
    public static final int EFFECTIVE_BLOCK_DELAY = 5;
    public static final int DISABLE_COOLDOWN = 100;
    public static final int MAX_DAMAGE = 336;
    public static final float MINIMUM_DURABILITY_DAMAGE = 3.0F;

    public static int durabilityDamage(float blockedDamage) {
        return blockedDamage < MINIMUM_DURABILITY_DAMAGE ? 0 : 1 + (int) Math.floor(blockedDamage);
    }

    /** The source must be in the open 180-degree hemisphere in front of the player. */
    public static boolean isInFront(double sourceDeltaX, double sourceDeltaZ, float playerYaw) {
        double yaw = Math.toRadians(playerYaw);
        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);
        return sourceDeltaX * forwardX + sourceDeltaZ * forwardZ > 0.0D;
    }

    /** Shield protection is resolved by CombatEventHandler; legacy sword blocking is disabled. */
    public static boolean usesLegacyDamageBlock(net.minecraft.entity.player.EntityPlayer player) {
        return false; // Sword blocking was removed in Combat Update 1.9.
    }

    /** Player.disableShield: axes pass sprinting=true, making the chance at least 100%. */
    public static float disableChance(int efficiencyLevel, boolean sprinting) {
        return 0.25F + Math.max(0, efficiencyLevel) * 0.05F + (sprinting ? 0.75F : 0.0F);
    }

    /** Modern shield repair ingredient tag, represented safely through 1.7 registry names. */
    public static boolean isPlankRegistryName(String registryName) {
        if (registryName == null) return false;
        int separator = registryName.indexOf(':');
        String path = separator < 0 ? registryName : registryName.substring(separator + 1);
        return "planks".equals(path) || path.endsWith("_planks");
    }

    private ShieldMechanics120() { }
}
