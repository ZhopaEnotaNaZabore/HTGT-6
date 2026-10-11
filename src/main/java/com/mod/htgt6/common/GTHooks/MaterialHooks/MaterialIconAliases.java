package com.mod.htgt6.common.GTHooks.MaterialHooks;

import gregapi.render.TextureSet;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Keeps GT's two passes, tinting and reload lifecycle while sharing identical sprites. */
final class MaterialIconAliases {
    private static final Properties ALIASES = load();
    private static final String PREFIX = "htgt6:materialicons/";

    private static Properties load() {
        Properties result = new Properties();
        try (InputStream input = MaterialIconAliases.class.getResourceAsStream(
                "/assets/htgt6/materialicon-aliases.properties")) {
            if (input == null) throw new IllegalStateException("Missing material icon aliases");
            result.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load material icon aliases", e);
        }
        return result;
    }

    private static IIconRegister redirect(final IIconRegister register, final String atlas) {
        return new IIconRegister() {
            @Override
            public IIcon registerIcon(String name) {
                if (name.startsWith(PREFIX)) {
                    String target = ALIASES.getProperty(atlas + "/" + name.substring(PREFIX.length()));
                    if (target != null) name = PREFIX + target;
                }
                return register.registerIcon(name);
            }
        };
    }

    static final class Item extends TextureSet.TextureSetIconItem {
        Item(String name) { super("htgt6", name); }

        @Override
        public void registerIcons(IIconRegister register) {
            super.registerIcons(redirect(register, "items"));
        }
    }

    static final class Block extends TextureSet.TextureSetIconBlock {
        Block(String name) { super("htgt6", name); }

        @Override
        public void registerIcons(IIconRegister register) {
            super.registerIcons(redirect(register, "blocks"));
        }
    }

    private MaterialIconAliases() { }
}
