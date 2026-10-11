from __future__ import print_function

import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SCRIPTS = ROOT / "run" / "scripts"
JAVA = ROOT / "src" / "main" / "java"
BASE = JAVA / "com" / "mod" / "htgt6" / "common" / "handler" / "recipes" / "modpackgenerated"

CALLS = [
    "recipes.addShaped",
    "recipes.remove",
    "furnace.addRecipe",
    "furnace.remove",
    "MTUtilsGT.addCustomRecipe",
    "MTUtilsGT.removeAllRecipes",
    "mods.htgt6.Assembler.addRecipe",
    "mods.htgt6.SMCompressor.addRecipe",
    "mods.galaxyspace.Assembler.addRecipe",
    "mods.immersiveengineering.ArcFurnace.removeRecipe",
    "mods.immersiveengineering.Crusher.removeRecipe",
    "mods.immersiveengineering.MetalPress.removeRecipe",
]


def strip_comments(text):
    out = []
    quote = None
    escaped = False
    index = 0
    while index < len(text):
        char = text[index]
        if quote:
            out.append(char)
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == quote:
                quote = None
            index += 1
            continue
        if char in "\"'":
            quote = char
            out.append(char)
            index += 1
            continue
        if char == "/" and index + 1 < len(text) and text[index + 1] == "/":
            while index < len(text) and text[index] not in "\r\n":
                out.append(" ")
                index += 1
            continue
        out.append(char)
        index += 1
    return "".join(out)


def find_closing(text, opening, open_char="(", close_char=")"):
    depth = 0
    quote = None
    escaped = False
    for index in range(opening, len(text)):
        char = text[index]
        if quote:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == quote:
                quote = None
            continue
        if char in "\"'":
            quote = char
        elif char == open_char:
            depth += 1
        elif char == close_char:
            depth -= 1
            if depth == 0:
                return index
    raise ValueError("Unclosed expression at offset %d" % opening)


def split_top(text):
    result = []
    start = 0
    round_depth = square_depth = curly_depth = angle_depth = 0
    quote = None
    escaped = False
    for index, char in enumerate(text):
        if quote:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == quote:
                quote = None
            continue
        if char in "\"'":
            quote = char
        elif char == "(": round_depth += 1
        elif char == ")": round_depth -= 1
        elif char == "[": square_depth += 1
        elif char == "]": square_depth -= 1
        elif char == "{": curly_depth += 1
        elif char == "}": curly_depth -= 1
        elif char == "<": angle_depth += 1
        elif char == ">": angle_depth -= 1
        elif char == "," and not (round_depth or square_depth or curly_depth or angle_depth):
            result.append(text[start:index].strip())
            start = index + 1
    result.append(text[start:].strip())
    return result if not (len(result) == 1 and result[0] == "") else []


def parse_array(expr):
    expr = expr.strip()
    if expr == "null":
        return []
    if not (expr.startswith("[") and expr.endswith("]")):
        return [expr]
    return split_top(expr[1:-1])


def java_string(value):
    return json.dumps(value, ensure_ascii=False).replace("</", "<\\/")


def java_array(values):
    return "new String[] {%s}" % ", ".join(java_string(value) for value in values)


def java_long_array(values):
    return "new long[] {%s}" % ", ".join(value.strip() + "L" for value in values)


def class_name(stem):
    return re.sub(r"[^A-Za-z0-9]", "", stem.title()) or "Script"


def variables(clean):
    found = {}
    for match in re.finditer(r"(?m)^\s*(?:var|val)\s+([A-Za-z_]\w*)\s*=\s*([^;]+);", clean):
        found[match.group(1)] = match.group(2).strip()
    return found


def resolve(expr, aliases):
    expr = expr.strip()
    match = re.match(r"^([A-Za-z_]\w*)(\s*\*\s*-?\d+)?$", expr)
    if not match or match.group(1) not in aliases:
        return re.sub(r"\s+", " ", expr)
    base = aliases[match.group(1)]
    suffix = match.group(2)
    return re.sub(r"\s+", "", base + (suffix or ""))


def operations(path):
    raw = path.read_text(encoding="utf-8-sig")
    clean = strip_comments(raw)
    aliases = variables(clean)
    result = []
    occupied = set()
    for call in CALLS:
        start = 0
        needle = call + "("
        while True:
            at = clean.find(needle, start)
            if at < 0:
                break
            opening = at + len(call)
            closing = find_closing(clean, opening)
            if at not in occupied:
                args = split_top(clean[opening + 1:closing])
                line = clean.count("\n", 0, at) + 1
                result.append({"call": call, "args": args, "line": line, "aliases": aliases})
                occupied.add(at)
            start = closing + 1

    for line_no, line in enumerate(clean.splitlines(), 1):
        match = re.match(r'^\s*(.+?)\.addTooltip\(format\.([A-Za-z]+)\("(.*)"\)\);\s*$', line)
        if match:
            result.append({"call": "addTooltip", "args": [match.group(1), match.group(2), match.group(3)],
                           "line": line_no, "aliases": aliases})
    return sorted(result, key=lambda entry: entry["line"])


HEADER = "// Generated from run/scripts by the HTGT6 migration; do not edit individual entries by hand.\n"


def write_java(path, package, imports, class_decl, body):
    path.parent.mkdir(parents=True, exist_ok=True)
    content = HEADER + "package %s;\n\n" % package
    if imports:
        content += "\n".join("import %s;" % item for item in imports) + "\n\n"
    content += "public final class %s {\n\n" % class_decl
    content += "    private %s() {}\n\n" % class_decl
    content += body
    content += "}\n"
    path.write_text(content, encoding="utf-8")


def source(entry, filename):
    return "%s:%d" % (filename, entry["line"])


def generate_simple_category(entries_by_file, folder, package_suffix, class_base, emitter, imports):
    calls = []
    for filename, entries in entries_by_file.items():
        if not entries:
            continue
        name = class_base + class_name(Path(filename).stem)
        lines = []
        for entry in entries:
            lines.extend(emitter(entry, filename))
        body = "    public static void register(GeneratedRecipeSettings settings, Logger logger) {\n"
        body += "\n".join("        " + line for line in lines) + "\n    }\n"
        write_java(BASE / folder / (name + ".java"),
                   "com.mod.htgt6.common.handler.recipes.modpackgenerated." + package_suffix,
                   imports, name, body)
        calls.append(name + ".register(settings, logger);")
    return calls


def main():
    parsed = {path.name: operations(path) for path in sorted(SCRIPTS.glob("*.zs"))}

    disabled = {}
    removed = {}
    shaped = {}
    removed_gt = {}
    integrated_gt = {}
    warnings = {}
    furnace = {}
    external = {}
    assembler = []
    compressor = []

    for filename, entries in parsed.items():
        for entry in entries:
            call = entry["call"]
            if call == "recipes.remove":
                (disabled if filename == "Bans+Info.zs" else removed).setdefault(filename, []).append(entry)
            elif call == "recipes.addShaped": shaped.setdefault(filename, []).append(entry)
            elif call == "MTUtilsGT.removeAllRecipes": removed_gt.setdefault(filename, []).append(entry)
            elif call == "MTUtilsGT.addCustomRecipe": integrated_gt.setdefault(filename, []).append(entry)
            elif call == "addTooltip": warnings.setdefault(filename, []).append(entry)
            elif call.startswith("furnace."): furnace.setdefault(filename, []).append(entry)
            elif call == "mods.htgt6.Assembler.addRecipe": assembler.append((filename, entry))
            elif call == "mods.htgt6.SMCompressor.addRecipe": compressor.append((filename, entry))
            else: external.setdefault(filename, []).append(entry)

    common_imports = [
        "com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings",
        "org.apache.logging.log4j.Logger",
    ]

    def removal(entry, filename):
        token = resolve(entry["args"][0], entry["aliases"])
        return ["GeneratedCraftingRemoval.remove(%s, %s, settings, logger);" %
                (java_string(source(entry, filename)), java_string(token))]

    disabled_calls = generate_simple_category(disabled, "disabled", "disabled", "DisabledFrom",
        removal, common_imports + ["com.mod.htgt6.common.handler.recipes.modpackgenerated.removed.GeneratedCraftingRemoval"])
    removed_calls = generate_simple_category(removed, "removed", "removed", "RemovedFrom",
        removal, common_imports + ["com.mod.htgt6.common.handler.recipes.modpackgenerated.removed.GeneratedCraftingRemoval"])

    def shaped_recipe(entry, filename):
        args = entry["args"]
        output = resolve(args[0], entry["aliases"])
        rows = parse_array(args[1])
        matrix = []
        for row in rows:
            matrix.append([resolve(cell, entry["aliases"]) if cell.strip() != "null" else "null"
                           for cell in parse_array(row)])
        width = max(len(row) for row in matrix)
        for row in matrix:
            row.extend(["null"] * (width - len(row)))
        grid = "new String[][] {%s}" % ", ".join(java_array(row) for row in matrix)
        return ["GeneratedCrafting3x3.addTokens(%s, %s, %s, settings, logger);" %
                (java_string(source(entry, filename)), java_string(output), grid)]

    shaped_calls = generate_simple_category(shaped, "crafting3x3", "crafting3x3", "CraftingFrom",
        shaped_recipe, common_imports)

    def remove_gt(entry, filename):
        args = entry["args"]
        map_name = args[0].strip().strip('"')
        outputs = [resolve(arg, entry["aliases"]) for arg in args[1:]]
        return ["GeneratedGTRecipeMaps.removeByOutputs(%s, %s, %s, settings, logger);" %
                (java_string(source(entry, filename)), java_string(map_name), java_array(outputs))]

    removed_gt_calls = generate_simple_category(removed_gt, "removedgt", "removedgt", "RemovedGTFrom",
        remove_gt, common_imports)

    def add_gt(entry, filename):
        args = entry["args"]
        if len(args) not in (7, 9):
            raise ValueError("Unexpected MTUtilsGT.addCustomRecipe arity at %s: %d" %
                             (source(entry, filename), len(args)))
        map_name = args[0].strip().strip('"')
        hidden, eut, duration = args[1].strip(), args[2].strip(), args[3].strip()
        chances = parse_array(args[4])
        inputs = [resolve(item, entry["aliases"]) for item in parse_array(args[5])]
        fluid_inputs = fluid_outputs = []
        if len(args) == 7:
            outputs = [resolve(item, entry["aliases"]) for item in parse_array(args[6])]
        else:
            raw_fluid_inputs = ["null"] if args[6].strip() == "null" else parse_array(args[6])
            raw_fluid_outputs = ["null"] if args[7].strip() == "null" else parse_array(args[7])
            fluid_inputs = [resolve(item, entry["aliases"]) for item in raw_fluid_inputs]
            fluid_outputs = [resolve(item, entry["aliases"]) for item in raw_fluid_outputs]
            outputs = [resolve(item, entry["aliases"]) for item in parse_array(args[8])]
        # Keep the lowered monocrystal energy requirement when importing legacy scripts.
        if map_name == "gt.recipe.crystallisationcrucible" and outputs == ["<HTGT6:itemMonocrystall1>*1"]:
            eut = "26"
        return ["GeneratedGTRecipeMaps.add(%s, %s, %s, %sL, %sL, %s, %s, %s, %s, %s, settings, logger);" %
                (java_string(source(entry, filename)), java_string(map_name), hidden, eut, duration,
                 java_long_array(chances), java_array(inputs), java_array(fluid_inputs),
                 java_array(fluid_outputs), java_array(outputs))]

    integrated_gt_calls = generate_simple_category(integrated_gt, "integratedgt", "integratedgt",
        "IntegratedGTFrom", add_gt, common_imports)

    def tooltip(entry, filename):
        target = resolve(entry["args"][0], entry["aliases"])
        return ["GeneratedTooltipRegistry.add(%s, %s, %s, %s, settings, logger);" %
                (java_string(source(entry, filename)), java_string(target), java_string(entry["args"][1]),
                 java_string(entry["args"][2]))]

    warning_calls = generate_simple_category(warnings, "warn", "warn", "WarningsFrom",
        tooltip, common_imports)

    def furnace_op(entry, filename):
        args = entry["args"]
        src = java_string(source(entry, filename))
        if entry["call"] == "furnace.remove":
            return ["GeneratedFurnaceRegistry.remove(%s, %s, settings, logger);" %
                    (src, java_string(resolve(args[0], entry["aliases"])))]
        return ["GeneratedFurnaceRegistry.add(%s, %s, %s, settings, logger);" %
                (src, java_string(resolve(args[0], entry["aliases"])),
                 java_string(resolve(args[1], entry["aliases"])))]

    furnace_calls = generate_simple_category(furnace, "compat", "compat", "FurnaceFrom",
        furnace_op, common_imports)

    def external_op(entry, filename):
        args = entry["args"]
        src = java_string(source(entry, filename))
        if entry["call"] == "mods.galaxyspace.Assembler.addRecipe":
            inputs = [resolve(item, entry["aliases"]) for item in parse_array(args[1])]
            return ["GeneratedExternalMachineRegistry.addGalaxySpaceAssembler(%s, %s, %s, settings, logger);" %
                    (src, java_string(resolve(args[0], entry["aliases"])), java_array(inputs))]
        machine = entry["call"].split('.')[2]
        return ["GeneratedExternalMachineRegistry.removeImmersiveEngineering(%s, %s, %s, settings, logger);" %
                (src, java_string(machine), java_string(resolve(args[0], entry["aliases"])))]

    external_calls = generate_simple_category(external, "compat", "compat", "ExternalFrom",
        external_op, common_imports)

    def aggregator(path, package, name, calls, extra_import):
        body = "    public static void register(GeneratedRecipeSettings settings, Logger logger) {\n"
        body += "\n".join("        " + call for call in calls) + "\n    }\n"
        write_java(path, package, common_imports + extra_import, name, body)

    aggregator(BASE / "disabled" / "DisabledRecipes.java",
        "com.mod.htgt6.common.handler.recipes.modpackgenerated.disabled", "DisabledRecipes", disabled_calls, [])
    aggregator(BASE / "removed" / "RemovedCraftingRecipes.java",
        "com.mod.htgt6.common.handler.recipes.modpackgenerated.removed", "RemovedCraftingRecipes", removed_calls, [])
    aggregator(BASE / "crafting3x3" / "GeneratedCraftingRecipes.java",
        "com.mod.htgt6.common.handler.recipes.modpackgenerated.crafting3x3", "GeneratedCraftingRecipes",
        shaped_calls, [])
    aggregator(BASE / "removedgt" / "RemovedGTRecipes.java",
        "com.mod.htgt6.common.handler.recipes.modpackgenerated.removedgt", "RemovedGTRecipes",
        removed_gt_calls, [])
    aggregator(BASE / "integratedgt" / "IntegratedGTRecipes.java",
        "com.mod.htgt6.common.handler.recipes.modpackgenerated.integratedgt", "IntegratedGTRecipes",
        integrated_gt_calls, [])
    aggregator(BASE / "warn" / "GeneratedRecipeWarnings.java",
        "com.mod.htgt6.common.handler.recipes.modpackgenerated.warn", "GeneratedRecipeWarnings",
        warning_calls, [])
    aggregator(BASE / "compat" / "GeneratedCompatibilityRecipes.java",
        "com.mod.htgt6.common.handler.recipes.modpackgenerated.compat", "GeneratedCompatibilityRecipes",
        furnace_calls + external_calls, [])

    machine_dir = JAVA / "com" / "mod" / "htgt6" / "common" / "handler" / "recipe" / "RM" / "modpackgenerated"

    def machine_class(name, entries, handler):
        lines = []
        for filename, entry in entries:
            args = entry["args"]
            output = resolve(args[0], entry["aliases"])
            duration, eut, tier = args[1].strip(), args[2].strip(), args[3].strip()
            inputs = [resolve(item, entry["aliases"]) for item in args[4:]]
            lines.append("add(%s, %s, %s, %s, %s, %s, settings, logger);" %
                         (java_string(source(entry, filename)), java_string(output), duration, eut, tier,
                          java_array(inputs)))
        body = "    public static void register(GeneratedRecipeSettings settings, Logger logger) {\n"
        body += "\n".join("        " + line for line in lines) + "\n    }\n\n"
        body += "    private static void add(String source, String outputToken, int duration, int euPerTick, int tier,\n"
        body += "            String[] inputTokens, GeneratedRecipeSettings settings, Logger logger) {\n"
        body += "        ItemStack output = GeneratedValueResolver.item(outputToken, source + \" output\", settings, logger);\n"
        body += "        if (output == null) return;\n"
        body += "        ItemStack[] inputs = new ItemStack[inputTokens.length];\n"
        body += "        for (int index = 0; index < inputTokens.length; index++) {\n"
        body += "            inputs[index] = GeneratedValueResolver.item(inputTokens[index], source + \" input[\" + index + ']\', settings, logger);\n"
        body += "            if (inputs[index] == null) return;\n"
        body += "        }\n"
        body += "        %s.addRecipe(inputs, output, duration, euPerTick, tier);\n" % handler
        body += "    }\n"
        write_java(machine_dir / (name + ".java"),
                   "com.mod.htgt6.common.handler.recipe.RM.modpackgenerated",
                   ["com.mod.htgt6.common.handler.recipe.RM.%s" % handler,
                    "com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings",
                    "com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedValueResolver",
                    "net.minecraft.item.ItemStack", "org.apache.logging.log4j.Logger"], name, body)

    machine_class("AssemblerModpackRecipes", assembler, "AssemblerRecipeHandler")
    machine_class("CompressorModpackRecipes", compressor, "CompressorRecipeHandler")

    report = {
        "scripts": len(parsed),
        "disabled": sum(map(len, disabled.values())),
        "removed": sum(map(len, removed.values())),
        "crafting3x3": sum(map(len, shaped.values())),
        "removedGT": sum(map(len, removed_gt.values())),
        "integratedGT": sum(map(len, integrated_gt.values())),
        "warn": sum(map(len, warnings.values())),
        "furnace": sum(map(len, furnace.values())),
        "external": sum(map(len, external.values())),
        "htgt6Assembler": len(assembler),
        "htgt6Compressor": len(compressor),
    }
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
