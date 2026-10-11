package com.mod.htgt6.common.handler.recipes.modpackgenerated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Logger;

/** Resolves the compact item/fluid tokens emitted by the one-time ZS migration. */
public final class GeneratedValueResolver {

    private GeneratedValueResolver() {}

    public static ItemStack item(String token, String source, GeneratedRecipeSettings settings, Logger logger) {
        if (token == null || "null".equals(token.trim())) return null;
        String value = token.trim();
        int count = trailingMultiplierProduct(value, 1);
        value = withoutTrailingMultipliers(value);

        String nbt = null;
        int tagStart = value.indexOf(".withTag(");
        if (tagStart >= 0) {
            nbt = value.substring(tagStart + 9, value.length() - 1);
            value = value.substring(0, tagStart);
        }
        if (!value.startsWith("<") || !value.endsWith(">")) {
            return missing("invalid item token " + token, source, settings, logger);
        }

        String id = value.substring(1, value.length() - 1);
        if (id.startsWith("ore:")) {
            return missing("ore dictionary token used where one item is required: " + token,
                    source, settings, logger);
        }
        int namespaceEnd = id.indexOf(':');
        if (namespaceEnd <= 0 || namespaceEnd == id.length() - 1) {
            return missing("invalid registry name " + token, source, settings, logger);
        }
        String modId = id.substring(0, namespaceEnd);
        String nameAndMeta = id.substring(namespaceEnd + 1);
        int metadata = 0;
        int metaSeparator = nameAndMeta.lastIndexOf(':');
        if (metaSeparator >= 0) {
            String candidate = nameAndMeta.substring(metaSeparator + 1);
            if ("*".equals(candidate) || isInteger(candidate)) {
                metadata = "*".equals(candidate) ? OreDictionary.WILDCARD_VALUE : Integer.parseInt(candidate);
                nameAndMeta = nameAndMeta.substring(0, metaSeparator);
            }
        }

        ItemStack registered = GameRegistry.findItemStack(modId, nameAndMeta, 1);
        if (registered == null || registered.getItem() == null) {
            return missing("missing item " + value, source, settings, logger);
        }
        ItemStack result = registered.copy();
        result.stackSize = count;
        result.setItemDamage(metadata);
        if (nbt != null) {
            try {
                NBTBase parsed = JsonToNBT.func_150315_a(nbt);
                if (!(parsed instanceof NBTTagCompound)) {
                    return missing("item NBT is not a compound: " + token, source, settings, logger);
                }
                result.setTagCompound((NBTTagCompound) parsed);
            } catch (Exception error) {
                return missing("invalid item NBT " + token + " (" + error.getMessage() + ')',
                        source, settings, logger);
            }
        }
        return result;
    }

    public static Object ingredient(String token, String source,
            GeneratedRecipeSettings settings, Logger logger) {
        if (token == null || "null".equals(token.trim())) return null;
        String value = token.trim();
        int count = trailingMultiplierProduct(value, 1);
        value = withoutTrailingMultipliers(value);
        if (value.startsWith("<ore:") && value.endsWith(">")) {
            String oreName = value.substring(5, value.length() - 1);
            if (count != 1) {
                logger.warn("Ore ingredient count {} cannot be represented in a crafting recipe at {}; using one",
                        count, source);
            }
            if (OreDictionary.getOres(oreName).isEmpty()) {
                missing("missing ore dictionary entry <ore:" + oreName + '>', source, settings, logger);
                return Missing.INSTANCE;
            }
            return oreName;
        }
        ItemStack stack = item(token, source, settings, logger);
        return stack == null ? Missing.INSTANCE : stack;
    }

    public static List<ItemStack> matchingItems(String token, String source,
            GeneratedRecipeSettings settings, Logger logger) {
        if (token == null) return Collections.emptyList();
        String value = withoutTrailingMultipliers(token.trim());
        if (value.startsWith("<ore:") && value.endsWith(">")) {
            String oreName = value.substring(5, value.length() - 1);
            List<ItemStack> ores = new ArrayList<ItemStack>();
            if (oreName.indexOf('*') >= 0) {
                Pattern pattern = Pattern.compile(globPattern(oreName));
                for (String registeredName : OreDictionary.getOreNames()) {
                    if (pattern.matcher(registeredName).matches()) ores.addAll(OreDictionary.getOres(registeredName));
                }
            } else {
                ores.addAll(OreDictionary.getOres(oreName));
            }
            if (ores.isEmpty()) missing("missing ore dictionary entry " + value, source, settings, logger);
            List<ItemStack> copies = new ArrayList<ItemStack>(ores.size());
            for (ItemStack ore : ores) if (ore != null) copies.add(ore.copy());
            return copies;
        }
        ItemStack stack = item(token, source, settings, logger);
        return stack == null ? Collections.<ItemStack>emptyList() : Collections.singletonList(stack);
    }

    public static FluidStack fluid(String token, String source,
            GeneratedRecipeSettings settings, Logger logger) {
        if (token == null || "null".equals(token.trim())) return null;
        String value = token.trim();
        int amount = trailingMultiplierProduct(value, 1);
        value = withoutTrailingMultipliers(value);
        if (!value.startsWith("<liquid:") || !value.endsWith(">")) {
            missing("invalid fluid token " + token, source, settings, logger);
            return null;
        }
        String fluidName = value.substring(8, value.length() - 1);
        if (FluidRegistry.getFluid(fluidName) == null) {
            missing("missing fluid " + fluidName, source, settings, logger);
            return null;
        }
        return new FluidStack(FluidRegistry.getFluid(fluidName), amount);
    }

    public static boolean isMissing(Object value) {
        return value == Missing.INSTANCE;
    }

    private static ItemStack missing(String problem, String source,
            GeneratedRecipeSettings settings, Logger logger) {
        String message = problem + " at " + source;
        switch (settings.getMissingReferencePolicy()) {
            case WARN:
                logger.warn(message + "; entry skipped");
                return null;
            case FAIL:
                throw new IllegalStateException(message);
            case IGNORE:
            default:
                return null;
        }
    }

    private static int trailingMultiplierProduct(String value, int fallback) {
        int boundary = tokenBoundary(value);
        int cursor = boundary + 1;
        int product = fallback;
        boolean found = false;
        while (cursor < value.length()) {
            while (cursor < value.length() && Character.isWhitespace(value.charAt(cursor))) cursor++;
            if (cursor >= value.length()) break;
            if (value.charAt(cursor) != '*') return fallback;
            int next = value.indexOf('*', cursor + 1);
            String number = value.substring(cursor + 1, next < 0 ? value.length() : next).trim();
            if (!isInteger(number)) return fallback;
            product *= Integer.parseInt(number);
            found = true;
            cursor = next < 0 ? value.length() : next;
        }
        return found ? product : fallback;
    }

    private static String withoutTrailingMultipliers(String value) {
        int boundary = tokenBoundary(value);
        if (boundary < 0 || boundary == value.length() - 1) return value;
        int cursor = boundary + 1;
        boolean found = false;
        while (cursor < value.length()) {
            while (cursor < value.length() && Character.isWhitespace(value.charAt(cursor))) cursor++;
            if (cursor >= value.length()) break;
            if (value.charAt(cursor) != '*') return value;
            int next = value.indexOf('*', cursor + 1);
            String number = value.substring(cursor + 1, next < 0 ? value.length() : next).trim();
            if (!isInteger(number)) return value;
            found = true;
            cursor = next < 0 ? value.length() : next;
        }
        return found ? value.substring(0, boundary + 1).trim() : value;
    }

    private static boolean isInteger(String value) {
        if (value == null || value.length() == 0) return false;
        int start = value.charAt(0) == '-' ? 1 : 0;
        if (start == value.length()) return false;
        for (int i = start; i < value.length(); i++) if (!Character.isDigit(value.charAt(i))) return false;
        return true;
    }

    private static int tokenBoundary(String value) {
        return Math.max(value.lastIndexOf('>'),
                Math.max(value.lastIndexOf('}'), value.lastIndexOf(')')));
    }

    private static String globPattern(String glob) {
        StringBuilder result = new StringBuilder("^");
        int start = 0;
        for (int index = 0; index < glob.length(); index++) {
            if (glob.charAt(index) != '*') continue;
            result.append(Pattern.quote(glob.substring(start, index))).append(".*");
            start = index + 1;
        }
        return result.append(Pattern.quote(glob.substring(start))).append('$').toString();
    }

    private enum Missing { INSTANCE }
}
