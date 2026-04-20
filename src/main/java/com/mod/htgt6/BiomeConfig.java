package com.mod.htgt6;


import java.io.*;
import java.util.Properties;

public class BiomeConfig {
    public static boolean enabled = true;

    public static void loadConfig(File configDir) {
        File configFile = new File(configDir, "HiTech6_ASM.cfg");
        Properties props = new Properties();

        if (configFile.exists()) {
            try (InputStream in = new FileInputStream(configFile)) {
                props.load(in);
                enabled = Boolean.parseBoolean(props.getProperty("enabled", "true"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Create default config
            try (OutputStream out = new FileOutputStream(configFile)) {
                props.setProperty("enabled", "true");
                props.store(out, "Biome ID Extender Configuration" +
                        "Transforming Packets, AnvilLoader, ChunkLoader" +
                        "Disable this if crashing, this crashing");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}