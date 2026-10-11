package com.mod.htgt6.minecraft1202.Module.Entity.Frostburn;

import net.minecraft.entity.EntityLiving;

/** A scoped spawn reason for the legacy spawner, which has no MobSpawnType argument. */
public final class FrostburnSpawnRules120 {
    private static final ThreadLocal<EntityLiving> SPAWNER=new ThreadLocal<EntityLiving>();
    private FrostburnSpawnRules120() { }
    public static boolean fromSpawner(EntityLiving entity) { return SPAWNER.get()==entity; }
    public static boolean canSpawnFromSpawner(EntityLiving entity) {
        EntityLiving previous=SPAWNER.get();
        SPAWNER.set(entity);
        try { return entity.getCanSpawnHere(); }
        finally { if(previous==null)SPAWNER.remove();else SPAWNER.set(previous); }
    }
}
