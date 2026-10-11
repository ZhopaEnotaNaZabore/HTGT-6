package com.mod.htgt6.common.Migaration;

/** Small parser kept independent from Forge so migration rules are testable. */
final class MigrationRegistryName {
    private MigrationRegistryName() {}

    static String pathFrom(String registryName, String expectedModId) {
        if (registryName == null || expectedModId == null) return null;
        int separator = registryName.indexOf(':');
        if (separator <= 0 || separator == registryName.length() - 1) return null;
        if (!expectedModId.equalsIgnoreCase(registryName.substring(0, separator))) return null;
        return registryName.substring(separator + 1);
    }
}
