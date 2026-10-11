package com.mod.htgt6.common.Migaration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** File-level migration helpers. Legacy files are retained as rollback copies. */
public final class LegacyConfigMigration {
    private LegacyConfigMigration() {}

    public static boolean copyIfMissing(File legacy, File target, String description) {
        if (target.isFile() || !legacy.isFile()) return false;
        File parent = target.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IllegalStateException("Unable to create migration directory " + parent);
        }
        try {
            Files.copy(legacy.toPath(), target.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
            System.out.println("[HTGT6 Migration] Copied " + description + " to " + target.getAbsolutePath());
            return true;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to migrate " + description + " from " + legacy, e);
        }
    }
}
