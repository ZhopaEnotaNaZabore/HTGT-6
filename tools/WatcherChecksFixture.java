import com.mod.htgt6.watcher.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.jar.*;

public final class WatcherChecksFixture {
    static final class Findings implements WatcherChecks.Findings {
        final List<String> codes = new ArrayList<String>();
        final List<String> errors = new ArrayList<String>();
        public void violation(String code, String detail) { codes.add(code); }
        public void unavailable(String check, Exception e) { errors.add(check + ": " + e); }
        void has(String code) { require(codes.contains(code), "missing " + code + ": " + codes); }
        void clean() { require(codes.isEmpty() && errors.isEmpty(), codes + " " + errors); }
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "--target".equals(args[0])) { attachTarget(); return; }
        if (args.length > 0 && "--startup".equals(args[0])) {
            Findings findings = new Findings();
            WatcherChecks checks = liveBaseline(findings);
            scanAgents(checks);
            checks.files(Collections.<Path>emptyList(), false);
            findings.clean();
            require("true".equals(System.getProperty("watcher.fixture.agentRan")), "startup probe did not run");
            System.out.println("Real startup launcher agent accepted: PASS");
            return;
        }
        Path work = Paths.get("build/watcher/fixture-" + System.nanoTime());
        Files.createDirectories(work);
        files(work);
        agents(work);
        transformers(work);
        devClasses(work);
        arbitraryPacks();
        attach(Paths.get(args[0]).toAbsolutePath());
        System.out.println("Watcher session checks: PASS (arbitrary mods/ASM/launcher accepted, later tampering/attach detected)");
    }

    private static WatcherChecks create(Path mods, Findings findings, Collection<Path> sources) {
        WatcherChecks checks = new WatcherChecks(mods, findings);
        checks.initialize(sources, Collections.<String>emptyList(), new URL[0]);
        return checks;
    }

    private static void files(Path work) throws Exception {
        Path mods = Files.createDirectory(work.resolve("mods"));
        Path mod = mods.resolve("unlisted-mod.jar");
        codeJar(mod, new byte[] {1, 2, 3, 4}, "old resources");
        Findings f = new Findings();
        WatcherChecks checks = create(mods, f, Collections.<Path>emptyList());
        checks.files(Collections.<Path>emptyList(), false);
        f.clean();
        codeJar(mod, new byte[] {1, 2, 3, 4}, "updated texture/config, same code");
        checks.files(Collections.<Path>emptyList(), false);
        f.clean();
        // Merely preparing next launch's mods does not mean code was injected into this JVM.
        Path candidate = mods.resolve("next-launch.zip");
        codeJar(candidate, new byte[] {7}, "");
        checks.files(Arrays.asList(candidate), false);
        Files.delete(candidate);
        checks.files(Collections.<Path>emptyList(), false);
        f.clean();
        FileTime time = Files.getLastModifiedTime(mod);
        codeJar(mod, new byte[] {4, 3, 2, 1}, "updated texture/config, same code");
        Files.setLastModifiedTime(mod, time);
        checks.files(Arrays.asList(mod), true); // A bootstrap scan must not overwrite an existing baseline.
        f.has("CODE_SOURCE_CHANGED");
        Findings nextLaunch = new Findings();
        create(mods, nextLaunch, Collections.<Path>emptyList()).files(Collections.<Path>emptyList(), false);
        nextLaunch.clean(); // Changing the pack between launches is allowed.
        Findings removed = new Findings();
        WatcherChecks removal = create(mods, removed, Collections.<Path>emptyList());
        Files.delete(mod);
        removal.files(Collections.<Path>emptyList(), false);
        require(removed.codes.isEmpty() && !removed.errors.isEmpty(), "missing file is not new injected code");
    }

    private static void codeJar(Path path, byte[] code, String resource) throws Exception {
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(path))) {
            out.putNextEntry(new JarEntry("Example.class"));
            out.write(code);
            out.closeEntry();
            out.putNextEntry(new JarEntry("assets/example.txt"));
            out.write(resource.getBytes("UTF-8"));
            out.closeEntry();
        }
    }

    private static Path agentJar(Path path) throws Exception {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        manifest.getMainAttributes().putValue("Agent-Class", "test.Probe");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(path), manifest)) {
            // Distinct content, so a genuinely new agent cannot match the original launcher by hash.
            out.putNextEntry(new JarEntry("test/Probe.class"));
            out.write(path.getFileName().toString().getBytes("UTF-8"));
            out.closeEntry();
        }
        return path;
    }

    private static void agents(Path work) throws Exception {
        Path launcher = agentJar(work.resolve("arbitrary-launcher-agent.jar"));
        Path nativeAgent = work.resolve("launcher-profiler.dll");
        Files.write(nativeAgent, new byte[] {11, 12});
        URL[] initial = {launcher.toUri().toURL()};
        Findings f = new Findings();
        WatcherChecks checks = new WatcherChecks(work.resolve("no-mods"), f);
        checks.initialize(Collections.<Path>emptyList(), Arrays.asList("-javaagent:" + launcher + "=secret",
                "-agentpath:" + nativeAgent, "-agentlib:jdwp=options", "-Xrunjdwp:transport=dt_socket"), initial);
        checks.agents(initial, (name, source) -> false);
        checks.agents(initial, (name, source) -> true); // Lazy launcher activation is still original code.
        checks.files(Collections.<Path>emptyList(), false);
        f.clean();
        Path injected = agentJar(work.resolve("new-agent.jar"));
        URL[] after = {launcher.toUri().toURL(), injected.toUri().toURL()};
        checks.agents(after, (name, source) -> source.equals(launcher.toAbsolutePath()));
        f.clean(); // A manifest alone, or a class of the same name in another JAR, proves nothing.
        checks.agents(after, (name, source) -> true);
        f.has("LATE_AGENT");
        Files.write(nativeAgent, new byte[] {12, 11});
        checks.files(Collections.<Path>emptyList(), false);
        f.has("CODE_SOURCE_CHANGED");
        require(f.errors.isEmpty(), "agent errors: " + f.errors);
    }

    private static void transformers(Path work) throws Exception {
        Path mod = work.resolve("any-mod-with-ASM.jar");
        codeJar(mod, new byte[] {1, 3, 5}, "");
        Findings f = new Findings();
        WatcherChecks checks = create(work.resolve("no-mods"), f, Arrays.asList(mod));
        checks.transformers(Arrays.asList(mod, mod)); // Arbitrary additional ASM from original mod is fine.
        checks.transformers(Collections.<Path>emptyList()); // Removal/reordering is not an injection signal.
        Path loadingLibrary = work.resolve("ordinary-bootstrap-library.jar");
        codeJar(loadingLibrary, new byte[] {7, 9}, "");
        checks.files(Arrays.asList(loadingLibrary), true);
        checks.transformers(Arrays.asList(loadingLibrary, mod));
        Path relocated = work.resolve("relocated-original-mod.jar");
        Files.copy(mod, relocated);
        checks.transformers(Arrays.asList(relocated));
        f.clean();
        Path unknown = work.resolve("new-runtime-transformer.jar");
        codeJar(unknown, new byte[] {66}, "");
        checks.files(Arrays.asList(unknown), false); // Unknown code is not automatically trusted at runtime.
        checks.transformers(Arrays.asList(unknown));
        f.has("LATE_TRANSFORMER_SOURCE");
        codeJar(relocated, new byte[] {66, 66, 66}, "");
        checks.files(Collections.<Path>emptyList(), false);
        f.has("CODE_SOURCE_CHANGED");
    }

    private static void devClasses(Path work) throws Exception {
        Path root = Files.createDirectory(work.resolve("dev-classes"));
        Path type = root.resolve("MyMod.class");
        Files.write(type, new byte[] {1, 2, 3});
        Findings f = new Findings();
        WatcherChecks checks = create(work.resolve("no-mods"), f, Arrays.asList(root));
        Files.write(root.resolve("config.json"), new byte[] {4});
        Files.write(root.resolve("MyMod.java"), new byte[] {5});
        checks.files(Collections.<Path>emptyList(), false);
        checks.transformers(Arrays.asList(root));
        f.clean();
        Files.write(type, new byte[] {3, 2, 1});
        checks.files(Collections.<Path>emptyList(), false);
        f.has("CODE_SOURCE_CHANGED");
    }

    private static void arbitraryPacks() {
        for (String folder : Arrays.asList("Modpack/HTGT6_104/mods", "run/mods")) {
            Findings f = new Findings();
            WatcherChecks checks = create(Paths.get(folder), f, Collections.<Path>emptyList());
            checks.files(Collections.<Path>emptyList(), false);
            f.clean();
            System.out.println("Arbitrary pack accepted without allowlist: " + folder);
        }
    }

    private static WatcherChecks liveBaseline(Findings findings) {
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        WatcherChecks checks = new WatcherChecks(Paths.get("build/watcher/no-mods"), findings);
        checks.initialize(Collections.<Path>emptyList(), ManagementFactory.getRuntimeMXBean().getInputArguments(),
                ((URLClassLoader) loader).getURLs());
        return checks;
    }

    private static void scanAgents(WatcherChecks checks) throws Exception {
        final ClassLoader loader = ClassLoader.getSystemClassLoader();
        final Method lookup = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
        lookup.setAccessible(true);
        checks.agents(((URLClassLoader) loader).getURLs(), (name, source) -> {
            Class<?> type = (Class<?>) lookup.invoke(loader, name);
            if (type == null || type.getProtectionDomain().getCodeSource() == null) return false;
            URL origin = type.getProtectionDomain().getCodeSource().getLocation();
            return "file".equals(origin.getProtocol()) && Files.isSameFile(source, Paths.get(origin.toURI()));
        });
    }

    private static void attachTarget() throws Exception {
        Findings f = new Findings();
        WatcherChecks checks = liveBaseline(f);
        scanAgents(checks);
        f.clean();
        System.out.println("READY " + ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        System.out.flush();
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(20);
        while (System.nanoTime() < deadline) {
            scanAgents(checks);
            if (f.codes.contains("LATE_AGENT")) {
                require("true".equals(System.getProperty("watcher.fixture.agentRan")), "probe did not run");
                System.out.println("ATTACH_DETECTED");
                return;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Dynamic attach not detected");
    }

    private static void attach(Path agent) throws Exception {
        String javaExecutable = Paths.get(System.getProperty("java.home"), "bin", "java.exe").toString();
        Process target = new ProcessBuilder(javaExecutable, "-cp", System.getProperty("java.class.path"),
                "WatcherChecksFixture", "--target").redirectErrorStream(true).start();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(target.getInputStream(), "UTF-8"));
            String ready = reader.readLine();
            require(ready != null && ready.startsWith("READY "), "target startup failed: " + ready);
            com.sun.tools.attach.VirtualMachine vm = com.sun.tools.attach.VirtualMachine.attach(ready.substring(6));
            try { vm.loadAgent(agent.toString()); } finally { vm.detach(); }
            require(target.waitFor(25, java.util.concurrent.TimeUnit.SECONDS), "target timeout");
            String result = reader.readLine();
            require(target.exitValue() == 0 && "ATTACH_DETECTED".equals(result), "attach check failed: " + result);
            System.out.println("Real Java 8 late agent attach detected: PASS");
        } finally { target.destroyForcibly(); }
    }

    private static void require(boolean result, String message) {
        if (!result) throw new AssertionError(message);
    }
}
