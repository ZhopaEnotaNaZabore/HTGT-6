"""Read-only coverage audit; does not regenerate maintained recipe sources.

Checks output coverage, not ingredient equivalence (recipes have since been balanced).
Runtime registry availability is separately reported by GeneratedValueResolver.
"""
import collections
import re
from pathlib import Path
import migrate_modpack_recipes as migration

ROOT = Path(__file__).resolve().parents[1]
HANDLER = ROOT / 'src/main/java/com/mod/htgt6/common/handler'
files = list((HANDLER / 'recipes/modpackgenerated').rglob('*.java'))
files += list((HANDLER / 'recipe/RM/modpackgenerated').rglob('*.java'))
sources = {p: p.read_text(encoding='utf-8-sig') for p in files}
java = '\n'.join(sources.values())
counts = collections.Counter()
missing = []
calls = {
    'recipes.addShaped': r'GeneratedCrafting3x3\.(?:addTokens|replaceTokens)',
    'MTUtilsGT.addCustomRecipe': r'GeneratedGTRecipeMaps\.add',
    'furnace.addRecipe': r'GeneratedFurnaceRegistry\.add',
    'mods.galaxyspace.Assembler.addRecipe': r'GeneratedExternalMachineRegistry\.addGalaxySpaceAssembler',
    'mods.htgt6.Assembler.addRecipe': r'\badd',
    'mods.htgt6.SMCompressor.addRecipe': r'\badd',
}
for script in sorted((ROOT / 'run/scripts.disabled').glob('*.zs')):
    for op in migration.operations(script):
        call = op['call']
        if call not in calls:
            continue
        outputs = migration.parse_array(op['args'][-1]) if call == 'MTUtilsGT.addCustomRecipe' else [op['args'][0]]
        if call == 'mods.htgt6.SMCompressor.addRecipe':
            outputs = [op['args'][-1]]
        if call == 'MTUtilsGT.addCustomRecipe' and len(op['args']) == 9:
            outputs += migration.parse_array(op['args'][-2])
        tokens = []
        for output in outputs:
            resolved = migration.resolve(output, op['aliases']).replace(
                '<HTGT6:village_pillage_blast_furnace>', '<Minecraft_1.20.2:village_pillage_blast_furnace>')
            tokens += re.findall(r'<[^>]+>', resolved)
        search_java = java
        if call.startswith('mods.htgt6.'):
            filename = 'AssemblerModpackRecipes.java' if '.Assembler.' in call else 'CompressorModpackRecipes.java'
            search_java = next(text for path, text in sources.items() if path.name == filename)
        candidates = re.findall(calls[call] + r'\([^\n]+', search_java)
        # The first token is the output except for GT maps, whose output array is last.
        if call == 'MTUtilsGT.addCustomRecipe':
            candidates = ['new String[]'.join(line.split('new String[]')[3:]) for line in candidates]
        elif call == 'mods.htgt6.SMCompressor.addRecipe':
            candidates = [re.findall(r'<[^>]+>', line)[-1] for line in candidates if '<' in line]
        else:
            candidates = [re.search(r'<[^>]+>', line).group(0) for line in candidates if '<' in line]
        counts[call] += 1
        if not tokens or not any(all(token in line for token in tokens) for line in candidates):
            missing.append(f'{script.name}:{op["line"]}: {tokens}')

# Detect forgotten generated classes that exist on disk but are never called.
orphaned = []
for path, source in sources.items():
    if not re.match(r'(CraftingFrom|IntegratedGTFrom|ExternalFrom|FurnaceFrom)', path.stem):
        continue
    if path.stem + '.register(' not in java:
        orphaned.append(path.stem)
print('Original additions checked:', dict(counts))
print('Missing output coverage:', missing)
print('Uncalled generated recipe classes:', orphaned)
raise SystemExit(bool(missing or orphaned))
