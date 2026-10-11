package com.mod.htgt6.common.GTHooks.MaterialHooks;

import gregapi.data.MT;
import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictPrefix;

import java.util.LinkedHashSet;
import java.util.Set;

/** Collapses GT6's generic Plastic and auto-created polyethylene materials into one target. */
public final class GT6OreDictionaryOverlapHook {
    private static final String[] LEGACY_NAMES = {
            "Plastic",
            "Polyethylene",
            "PolyEthylene",
            "PolyEthilene"
    };

    public static void install(OreDictMaterial canonical) {
        if (canonical == null) throw new IllegalArgumentException("canonical PolyEthyLene is null");

        Set<OreDictMaterial> legacyMaterials = new LinkedHashSet<OreDictMaterial>();
        legacyMaterials.add(MT.Plastic);
        for (String name : LEGACY_NAMES) {
            OreDictMaterial material = OreDictMaterial.MATERIAL_MAP.get(name);
            if (material == null) {
                // Reserve the spelling before GT6 can create a separate AUTO_MATERIAL
                // when another mod registers dust/plate/fluid polyethylene later.
                material = OreDictMaterial.createMaterial(-1, name, name);
            }
            if (material != canonical) legacyMaterials.add(material);
        }

        for (OreDictMaterial legacy : legacyMaterials) {
            legacy.hide();
            legacy.handle(canonical);
            legacy.setRegistration(canonical);
            legacy.setAllToTheOutputOf(canonical);

            // A canonical stack is also registered under the legacy spelling. This
            // retains old recipe compatibility without generating a second item.
            canonical.addReRegistrations(legacy);
            for (OreDictPrefix prefix : OreDictPrefix.VALUES)
                if (prefix != null) prefix.disableItemGeneration(legacy);
        }
    }

    private GT6OreDictionaryOverlapHook() { }
}
