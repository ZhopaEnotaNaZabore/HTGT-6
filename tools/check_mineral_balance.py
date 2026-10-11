"""Check empirical formulas against GT6 6.15.08 component/dust normalization.

These are material-balance checks, not an in-game recipe execution test.
GT6 definitions were checked in the installed HiTech6 Edition dependency:
MT.OREMATS.Uraninite uses setMcfg(1), other products use automatic dividers.
"""
from collections import defaultdict
from fractions import Fraction
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/com/mod/htgt6"
materials = (JAVA / "common/GTHooks/MaterialHooks/GT6MaterialAutoGenerator.java").read_text(encoding="utf-8")
recipes = (JAVA / "common/handler/recipe/RM/NativeIndustrialCentrifugeRecipes.java").read_text(encoding="utf-8")


def formula(text):
    result = defaultdict(Fraction)
    for symbol, number in re.findall(r"([A-Z][a-z]?)(\d*)", text):
        result[symbol] += int(number or 1)
    return dict(result)


products = {
    "Pyrite": ("FeS2", 3),
    "SiO2": ("SiO2", 3),
    "OREMATS.Uraninite": ("UO2", 1),
    "Water": ("H2O", 3),
    "VanadiumPentoxide": ("V2O5", 7),
    "OREMATS.Bastnasite": ("CeCO3F", 6),
}

for name, count, seconds, eut, tier in [("PHERITHIUM", 10, 56, 512, 3), ("VANITE", 15, 82, 2048, 4)]:
    block = materials.split(name + " = register(", 1)[1].split(name + ".tooltip(", 1)
    components = [(p, int(n)) for p, n in re.findall(r"c\(MT\.([\w.]+), (\d+)\)", block[0])]
    assert sum(n for _, n in components) == count
    atoms = defaultdict(Fraction)
    for product, amount in components:
        chemical, divider = products[product]
        for element, number in formula(chemical).items():
            atoms[element] += amount * number / divider
    display = formula(block[1].split('"')[1])
    assert atoms.keys() == display.keys(), (name, atoms, display)
    assert len({atoms[e] / display[e] for e in atoms}) == 1, (name, atoms, display)
    recipe = recipes.split("OP.dust.mat(GT6MaterialAutoGenerator." + name + ", ", 1)[1]
    assert recipe.startswith(str(count) + ")")
    recipe = recipe.split(";", 1)[0]
    outputs = [(p, int(n)) for p, n in re.findall(r"OP.dust.mat\(MT\.([\w.]+), (\d+)\)", recipe)]
    water = re.search(r"MT.Water.liquid\((\d+) \* CS.U, false\)", recipe)
    if water:
        outputs.append(("Water", int(water[1])))
    assert dict(outputs) == dict(components), (name, outputs, components)
    assert f"{seconds} * 20, {eut}, {tier})" in recipe
    print(f"{name}: exact element ratios and recipe quantities/timing/tier OK")
