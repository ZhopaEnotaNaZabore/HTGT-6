package com.mod.htgt6.common.GTHooks.MaterialHooks;

import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictPrefix;
import gregapi.render.IIconContainer;

import java.util.ArrayList;

/** Assigns addon-owned textures to one material without mutating a shared GT6 TextureSet. */
public final class MaterialTextureHooks {

    public static void applyAll(OreDictMaterial material, String palette) {
        cloneLists(material);
        for (OreDictPrefix prefix : OreDictPrefix.VALUES) {
            if (prefix == null) continue;
            replaceItem(material, prefix, palette);
            replaceBlock(material, prefix, palette);
        }
    }

    public static void applyItems(OreDictMaterial material, String palette,
            OreDictPrefix... prefixes) {
        cloneLists(material);
        if (prefixes == null) return;
        for (OreDictPrefix prefix : prefixes)
            if (prefix != null) replaceItem(material, prefix, palette);
    }

    private static void cloneLists(OreDictMaterial material) {
        material.mTextureSetsItems = new ArrayList<IIconContainer>(material.mTextureSetsItems);
        material.mTextureSetsBlock = new ArrayList<IIconContainer>(material.mTextureSetsBlock);
    }

    private static void replaceItem(OreDictMaterial material, OreDictPrefix prefix, String palette) {
        int index = prefix.mIconIndexItem;
        if (index >= 0 && index < material.mTextureSetsItems.size()) {
            material.mTextureSetsItems.set(index,
                    new MaterialIconAliases.Item(palette + "/" + prefix.mNameInternal));
        }
    }

    private static void replaceBlock(OreDictMaterial material, OreDictPrefix prefix, String palette) {
        int index = prefix.mIconIndexBlock;
        if (index >= 0 && index < material.mTextureSetsBlock.size()) {
            material.mTextureSetsBlock.set(index,
                    new MaterialIconAliases.Block(palette + "/" + prefix.mNameInternal));
        }
    }

    private MaterialTextureHooks() { }
}
