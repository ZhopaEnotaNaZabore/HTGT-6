package com.mod.htgt6.client.vanillatweaks.SSUI;

import java.io.*;
import java.nio.file.*;
import java.util.Locale;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;

/** Address-keyed local metadata; servers.dat remains owned by vanilla ServerList. */
public final class ServerBrowserPreferences {
    private final File file;
    private final Properties values = new Properties();
    public ServerBrowserPreferences(File file) {
        this.file = file;
        if (file.isFile()) {
            try (InputStream in = new FileInputStream(file)) { values.load(in); }
            catch (IOException e) { LogManager.getLogger("HTGT6-ServerBrowser").warn("Cannot read browser preferences", e); }
        }
    }
    private static String key(String address) { return address.trim().toLowerCase(Locale.ROOT); }
    public boolean favorite(String address) { return Boolean.parseBoolean(values.getProperty("favorite." + key(address))); }
    public long recent(String address) {
        try { return Long.parseLong(values.getProperty("recent." + key(address), "0")); }
        catch (NumberFormatException ignored) { return 0; }
    }
    public void toggleFavorite(String address) {
        String key = "favorite." + key(address);
        if (favorite(address)) values.remove(key); else values.setProperty(key, "true");
        save();
    }
    public void recordConnection(String address) {
        values.setProperty("recent." + key(address), Long.toString(System.currentTimeMillis()));
        save();
    }
    public void move(String from, String to) {
        if (key(from).equals(key(to))) return;
        for (String prefix : new String[] {"favorite.", "recent."}) {
            String value = (String) values.remove(prefix + key(from));
            if (value != null) values.setProperty(prefix + key(to), value);
        }
        save();
    }
    public void remove(String address) {
        values.remove("favorite." + key(address));
        values.remove("recent." + key(address));
        save();
    }
    private void save() {
        try {
            Files.createDirectories(file.toPath().toAbsolutePath().getParent());
            Path temp = file.toPath().resolveSibling(file.getName() + ".tmp");
            try (OutputStream out = Files.newOutputStream(temp)) { values.store(out, "HTGT6 server browser"); }
            try { Files.move(temp, file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temp, file.toPath(), StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { LogManager.getLogger("HTGT6-ServerBrowser").warn("Cannot save browser preferences", e); }
    }
}
