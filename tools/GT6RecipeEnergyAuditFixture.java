package com.mod.htgt6.common.electronics;

import com.mod.htgt6.common.crusher.CrusherOreImportFixture;
import com.mod.htgt6.common.GTHooks.TurbineRotorHooks.TurbineRotorSize;
import gregapi.code.TagData;
import gregapi.data.CS;
import gregapi.data.RM;
import gregapi.data.TD;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.*;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Energy regression only: real GT6 checkRecipe/doWork, stand-in ingredients and short cycles.
 * Machine power profiles are taken from the pinned bundled GT6 registrations.
 * This does not test ingredient resolution, multiblock structures or parallel throughput.
 */
public final class GT6RecipeEnergyAuditFixture {
    private static final Path ROOT = Paths.get("src/main/java/com/mod/htgt6/common/handler/recipes/modpackgenerated/integratedgt");
    private static final Pattern ENTRY = Pattern.compile(
            "GeneratedGTRecipeMaps.add\\(\"([^\"]+)\", \"gt.recipe.(\\w+)\", (?:true|false), (\\d+)L, (\\d+)L");
    private static final Set<String> FIXED_SOURCES = new HashSet<String>(Arrays.asList(
            "GregTechTierLV.zs:206", "GregTechTierLV.zs:208", "GregTechTierLV.zs:210",
            "GregTechTierLV.zs:212", "GregTechTierLV.zs:214", "GregTechTierLV.zs:240",
            "GregTechTierLV.zs:245", "GregTechTierLV.zs:250", "GregTechTierLV.zs:254",
            "mixer.zs:26", "EIO.zs:63", "IC2.zs:18"));
    private static final class Profile {
        final String map;
        final long nominal, minimum, maximum;
        final TagData energy;
        final boolean cheap;
        Profile(String line) throws Exception {
            String[] fields = line.split("\\t");
            map = fields[0]; nominal = Long.parseLong(fields[1]);
            minimum = Long.parseLong(fields[2]); maximum = Long.parseLong(fields[3]);
            energy = (TagData) TD.Energy.class.getField(fields[4]).get(null);
            cheap = Boolean.parseBoolean(fields[5]);
        }
    }
    public static void main(String[] args) throws Exception {
        World world = CrusherOreImportFixture.initializeGT6();
        CS.GAPI.mFinishedServerStarted = 1;
        CS.GAPI_POST = CS.GAPI;
        List<Profile> profiles = new ArrayList<Profile>();
        List<String> lines = Files.readAllLines(Paths.get("tools/fixtures/gt6-machine-energy-profiles.tsv"), StandardCharsets.UTF_8);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(
                Paths.get("libs/gregtech_1.7.10-6.15.08-HiTech6_Edition.jar")));
        StringBuilder hash = new StringBuilder();
        for (byte b : digest) hash.append(String.format("%02x", b & 255));
        require(lines.get(1).endsWith(hash.toString()), "GT6 changed: refresh machine power profiles");
        for (String line : lines) if (!line.startsWith("#") && !line.isEmpty()) profiles.add(new Profile(line));

        // Each former bad power must actually consume input and then stall at nominal supply.
        long[][] regressions = {{10,32},{12,32},{36,128},{44,128},{181,512}};
        for (long[] pair : regressions) {
            GT6CircuitAssemblerEnergyFixture.Machine machine = GT6CircuitAssemblerEnergyFixture.run(
                    world, pair[0], 32, TD.Energy.LU, pair[1], pair[1]/2, pair[1]*2, false, 100);
            require(machine.slot(0) == null && machine.mMinEnergy > pair[1]
                    && machine.slot(RM.Assembler.mInputItemsCount) == null, "old power must reproduce stall: " + pair[0]);
        }

        int recipes = 0, runs = 0;
        Set<String> maps = new HashSet<String>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(ROOT, "IntegratedGTFrom*.java")) {
            for (Path file : files) {
                String source = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                Matcher matcher = ENTRY.matcher(source);
                int parsed = 0;
                while (matcher.find()) {
                    parsed++; recipes++;
                    String origin = matcher.group(1), map = matcher.group(2);
                    long power = Long.parseLong(matcher.group(3));
                    maps.add(map);
                    // Older archived scripts may intentionally differ from the native recipes.
                    if (FIXED_SOURCES.contains(origin)) {
                        String[] location = origin.split(":");
                        String archived = Files.readAllLines(Paths.get("run/scripts.disabled", location[0]), StandardCharsets.UTF_8)
                                .get(Integer.parseInt(location[1])-1);
                        require(archived.contains("\"gt.recipe." + map + "\", false, " + power + ","),
                                "archived recipe differs: " + origin);
                    }
                    runs += check(world, profiles, map, power, origin);
                }
                require(parsed == source.split("GeneratedGTRecipeMaps.add\\(", -1).length-1,
                        "unparsed recipe registration: " + file);
            }
        }
        require(recipes > 0, "no recipes audited");
        for (TurbineRotorSize size : TurbineRotorSize.values())
            runs += check(world, profiles, "assembler", size.assemblyEUt, "rotor " + size);
        System.out.println("GT6 recipe energy PASS: " + recipes + " recipes, " + maps.size()
                + " maps, " + TurbineRotorSize.values().length + " rotor powers, " + runs
                + " nominal-supply cycles; all 5 old power values reproduce stalls");
    }
    private static int check(World world, List<Profile> profiles, String map, long power, String origin) throws Exception {
        int runs = 0;
        for (Profile profile : profiles) {
            // A lower-tier machine cannot be expected to run a recipe above its nominal supply.
            if (!profile.map.equals(map) || power > profile.nominal) continue;
            GT6CircuitAssemblerEnergyFixture.Machine machine = GT6CircuitAssemblerEnergyFixture.run(
                    world, power, 32, profile.energy, profile.nominal, profile.minimum, profile.maximum, profile.cheap, 200);
            ItemStack output = machine.slot(RM.Assembler.mInputItemsCount);
            require(output != null && output.getItem() == Items.diamond && output.stackSize == 4,
                    origin + " power=" + power + " stalls at " + profile.nominal + " " + profile.energy
                    + " cheap=" + profile.cheap + " demand=" + machine.mMinEnergy);
            runs++;
        }
        require(runs > 0, "no suitable machine profile: " + origin + " " + map);
        return runs;
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
