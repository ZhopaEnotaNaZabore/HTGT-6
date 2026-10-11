package com.mod.htgt6.common.Migaration;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/** Runtime diagnostic for the old standalone Amun-Ra to combined-JAR move. */
public final class MigrationEnvironmentGuard {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-Migration");

    private MigrationEnvironmentGuard() {}

    public static void verifyEmbeddedAmunRa() {
        ModContainer htgt6 = Loader.instance().getIndexedModList().get(HTGT6.MOD_ID);
        ModContainer amunRa = Loader.instance().getIndexedModList().get(AmunRaRegistryInterceptor.TARGET_MOD_ID);
        if (htgt6 == null || amunRa == null) {
            throw new IllegalStateException("Combined HTGT6 JAR must expose both HTGT6 and GalacticraftAmunRa mod containers");
        }
        if (!sameFile(htgt6.getSource(), amunRa.getSource())) {
            throw new IllegalStateException("GalacticraftAmunRa is loaded from a standalone JAR. Remove AmunRa-GC-0.4.8.jar before migration");
        }
        LOGGER.info("Amun-Ra migration check passed; legacy mod id is provided by {}", htgt6.getSource().getName());
    }

    private static boolean sameFile(File first, File second) {
        try {
            return first.getCanonicalFile().equals(second.getCanonicalFile());
        } catch (IOException ignored) {
            return first.getAbsoluteFile().equals(second.getAbsoluteFile());
        }
    }
}
