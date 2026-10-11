package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatBoat;
import net.minecraft.entity.Entity;

public final class BoatMotion120 {
    private BoatMotion120() { }
    public static boolean breakOnCollision(Entity boat) {
        return !(boat instanceof EntityCombatBoat) && boat.isCollidedHorizontally;
    }
    public static double heading(double legacyRadians, Entity boat) {
        // Legacy atan2(previousZ-currentZ, previousX-currentX) uses the hull's X axis.
        return legacyRadians + (boat instanceof EntityCombatBoat ? Math.PI / 2 : 0);
    }
}
