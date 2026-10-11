package com.mod.htgt6.common.item.htgt6;

import com.mod.htgt6.client.render.ItemAtlasSprites;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

/** Existing item identities, with icons sourced from the shared electronics sheet. */
public class AtlasItem extends Item {
    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        itemIcon = ItemAtlasSprites.register(register, getIconString());
    }
}
