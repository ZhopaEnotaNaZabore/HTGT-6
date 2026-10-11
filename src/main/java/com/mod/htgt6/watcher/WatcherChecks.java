package com.mod.htgt6.watcher;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.jar.*;
import java.util.stream.Stream;

/** Session-relative checks. There is deliberately no approved-mod or launcher list. */
public final class WatcherChecks {
    public interface Findings {
        void violation(String code, String detail);
        void unavailable(String check, Exception cause);
    }
    public interface LoadedClass {
        boolean contains(String name, Path source) throws Exception;
    }

    private final Findings findings;
    private final Path mods;
    private final Map<Path, String> baseline = new LinkedHashMap<Path, String>();
    private final Set<String> originalCode = new HashSet<String>();
    private boolean initialized;

    public WatcherChecks(Path mods, Findings findings) {
        this.mods = mods.toAbsolutePath().normalize();
        this.findings = findings;
    }

    /** Only called once. Startup agents and launcher code are part of this session's baseline. */
    public void initialize(Collection<Path> sources, List<String> arguments, URL[] systemSources) {
        if (initialized) throw new IllegalStateException("Session baseline already captured");
        initialized = true;
        Set<Path> initial = new LinkedHashSet<Path>(sources);
        try { initial.addAll(archivesInMods(mods)); }
        catch (Exception e) { findings.unavailable("initial mods inventory", e); }
        for (URL url : systemSources) {
            try { if ("file".equals(url.getProtocol())) initial.add(Paths.get(url.toURI())); }
            catch (Exception e) { findings.unavailable("initial system source", e); }
        }
        for (String argument : arguments) {
            if (argument.startsWith("-javaagent:") || argument.startsWith("-agentpath:")) {
                try { initial.add(Paths.get(argument.substring(argument.indexOf(':') + 1).split("=", 2)[0])); }
                catch (Exception e) { findings.unavailable("initial agent source", e); }
            }
            // -agentlib / -Xrun are JVM startup options, not evidence of a later injection.
        }
        for (Path path : initial) remember(path);
    }

    /** Allow normal loader discovery through LoadComplete; never replace a captured hash. */
    public void files(Collection<Path> loadingSources, boolean bootstrap) {
        requireInitialized();
        if (bootstrap) for (Path path : loadingSources) if (!baseline.containsKey(normalize(path))) remember(path);
        for (Map.Entry<Path, String> entry : baseline.entrySet()) {
            try {
                String current = fingerprint(entry.getKey());
                if (!entry.getValue().equals(current)) {
                    findings.violation("CODE_SOURCE_CHANGED", entry.getKey().getFileName().toString());
                }
            } catch (Exception e) { findings.unavailable("code integrity: " + entry.getKey().getFileName(), e); }
        }
    }

    /** Only a loaded agent from code absent in the startup inventory is an injection signal. */
    public void agents(URL[] systemSources, LoadedClass loaded) {
        requireInitialized();
        for (URL source : systemSources) {
            if (!"file".equals(source.getProtocol())) continue;
            try {
                Path path = Paths.get(source.toURI());
                if (!Files.isRegularFile(path) || !isArchive(path)) continue;
                try (JarFile jar = new JarFile(path.toFile())) {
                    Manifest manifest = jar.getManifest();
                    if (manifest == null) continue;
                    Attributes attrs = manifest.getMainAttributes();
                    for (String key : Arrays.asList("Agent-Class", "Premain-Class")) {
                        String name = attrs.getValue(key);
                        if (name != null && loaded.contains(name.trim(), path)) {
                            if (!knownCode(path)) findings.violation("LATE_AGENT", path.getFileName().toString());
                            break;
                        }
                    }
                }
            } catch (Exception e) { findings.unavailable("loaded agent", e); }
        }
    }

    /** Class names, count, order and generated proxies of ordinary ASM are not detection rules. */
    public void transformers(Collection<Path> origins) {
        requireInitialized();
        for (Path path : origins) {
            try {
                if (!knownCode(path)) findings.violation("LATE_TRANSFORMER_SOURCE", path.getFileName().toString());
            } catch (Exception e) { findings.unavailable("transformer source", e); }
        }
    }

    private boolean knownCode(Path source) throws Exception {
        Path path = normalize(source);
        // Existing sources are content-checked by files(), not rehashed every fast poll.
        if (baseline.containsKey(path)) return true;
        String hash = fingerprint(path);
        if (hash == null) return true; // No code / unavailable evidence is not an accusation.
        if (!originalCode.contains(hash)) return false;
        // A relocated copy of code that already existed is not new injected code.
        baseline.put(path, hash);
        return true;
    }

    private void remember(Path source) {
        Path path = normalize(source);
        if (baseline.containsKey(path)) return;
        try {
            String hash = fingerprint(path);
            if (hash != null) {
                baseline.put(path, hash);
                originalCode.add(hash);
            }
        } catch (Exception e) { findings.unavailable("baseline: " + path.getFileName(), e); }
    }

    private void requireInitialized() {
        if (!initialized) throw new IllegalStateException("Session baseline not captured");
    }

    private static Path normalize(Path path) { return path.toAbsolutePath().normalize(); }

    public static Set<Path> archivesInMods(Path mods) throws IOException {
        Set<Path> paths = new LinkedHashSet<Path>();
        collectArchives(mods, paths);
        collectArchives(mods.resolve("1.7.10"), paths);
        return paths;
    }

    private static void collectArchives(Path directory, Set<Path> paths) throws IOException {
        if (!Files.exists(directory)) return;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) if (Files.isRegularFile(path) && isArchive(path)) paths.add(normalize(path));
        }
    }

    private static boolean isArchive(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".jar") || name.endsWith(".zip");
    }

    /** Directories are dev code roots: monitor .class contents, not textures, config or Java source. */
    private static String fingerprint(Path path) throws Exception {
        if (!Files.exists(path)) throw new FileNotFoundException(path.getFileName().toString());
        if (!Files.isDirectory(path)) return isArchive(path) ? archiveCode(path) : sha256(path);
        List<Path> classes = new ArrayList<Path>();
        try (Stream<Path> walk = Files.walk(path)) {
            walk.filter(file -> Files.isRegularFile(file) && file.toString().endsWith(".class")).forEach(classes::add);
        }
        Collections.sort(classes);
        if (classes.isEmpty()) return null;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (Path file : classes) {
            try (InputStream in = Files.newInputStream(file)) {
                entryDigest(digest, path.relativize(file).toString().replace('\\', '/'), in);
            }
        }
        return hex(digest.digest());
    }

    public static String sha256(Path path) throws Exception {
        try (InputStream in = Files.newInputStream(path)) {
            return hex(digest(in));
        }
    }

    private static String archiveCode(Path path) throws Exception {
        try (JarFile jar = new JarFile(path.toFile())) {
            List<String> names = new ArrayList<String>();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                String lower = name.toLowerCase(Locale.ROOT);
                if (!entry.isDirectory() && (name.endsWith(".class") || lower.endsWith(".dll")
                        || lower.endsWith(".so") || lower.endsWith(".dylib"))) names.add(name);
            }
            if (names.isEmpty()) return null;
            Collections.sort(names);
            MessageDigest fingerprint = MessageDigest.getInstance("SHA-256");
            for (String name : names) {
                try (InputStream in = jar.getInputStream(jar.getJarEntry(name))) { entryDigest(fingerprint, name, in); }
            }
            return hex(fingerprint.digest());
        }
    }

    private static void entryDigest(MessageDigest target, String name, InputStream content) throws Exception {
        target.update(name.getBytes(StandardCharsets.UTF_8));
        target.update((byte) 0);
        target.update(digest(content));
    }

    private static byte[] digest(InputStream in) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[16384];
        int count;
        while ((count = in.read(buffer)) != -1) digest.update(buffer, 0, count);
        return digest.digest();
    }

    private static String hex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(64);
        String digits = "0123456789abcdef";
        for (byte value : bytes) hex.append(digits.charAt((value & 255) >>> 4)).append(digits.charAt(value & 15));
        return hex.toString();
    }
}
