package com.mod.htgt6.minecraft1202.Module.Registry.Modern;

import java.util.*;
import com.google.common.collect.ListMultimap;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;

/** FML mod-entity IDs are full-width ints, scoped by ModContainer (not global egg IDs). */
public final class BackportEntityIds114 {
    public static void validate(EntityRegistry registry, ModContainer owner) {
        if(owner==null)throw new IllegalStateException("Missing Backport mod container for entity ID validation");
        ListMultimap<ModContainer,EntityRegistry.EntityRegistration> entries=
                ReflectionHelper.getPrivateValue(EntityRegistry.class,registry,"entityRegistrations");
        Map<Integer,Class<?>> ids=new HashMap<Integer,Class<?>>();
        for(EntityRegistry.EntityRegistration entry:entries.get(owner)) {
            Class<?> previous=ids.put(entry.getModEntityId(),entry.getEntityClass());
            if(previous!=null)throw new IllegalStateException("Duplicate Backport entity ID "+entry.getModEntityId()+
                    ": "+previous.getName()+" / "+entry.getEntityClass().getName());
            if(registry.lookupModSpawn(owner,entry.getModEntityId())!=entry)
                throw new IllegalStateException("Backport network entity lookup mismatch: "+entry.getEntityName());
        }
    }
    private BackportEntityIds114() {}
}
