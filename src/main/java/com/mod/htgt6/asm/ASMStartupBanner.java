package com.mod.htgt6.asm;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Cosmetic startup banner; never invoked from a class transformer. */
public final class ASMStartupBanner {
    private static final Logger LOGGER = LogManager.getLogger("HiTech6");
    private static boolean printed;

    private ASMStartupBanner() {}

    /** Standalone preview: no Minecraft bootstrap or mod lifecycle. */
    public static void main(String[] args) {
        printOnce();
    }

    public static synchronized void printOnce() {
        if (printed) return;
        printed = true;
        try (InputStream stream = ASMStartupBanner.class.getResourceAsStream(
                "/assets/htgt6/asm-banner.json")) {
            if (stream == null) {
                LOGGER.warn("HiTech6 banner resource is missing");
                return;
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                StringBuilder message = new StringBuilder("HiTech6 Loading Finished\n");
                for (JsonElement line : new JsonParser().parse(reader).getAsJsonObject()
                        .getAsJsonArray("lines")) {
                    message.append(line.getAsString()).append('\n');
                }
                LOGGER.info(message.toString());
            }
        } catch (Exception exception) {
            // Optional artwork must not prevent the mod from starting.
            LOGGER.warn("Could not display HiTech6 banner", exception);
        }
    }
}
