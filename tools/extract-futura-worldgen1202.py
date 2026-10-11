"""Extract the pinned local vanilla data used by Futura's placement runtime."""
import json
import pathlib
import re
import zipfile

root = pathlib.Path(__file__).resolve().parents[1]
source = root / 'src/main/java/com/mod/htgt6/minecraft1202/Module/World/Modern/OverworldBiomeParameterMap120.java'
names = re.search(r'public enum Biome \{(.*?)\n    \}', source.read_text(), re.S).group(1)
names = [name.strip().lower() for name in names.split(',')]
with zipfile.ZipFile(root / 'tools/decompile1202/reference/client.jar') as jar:
    assert json.loads(jar.read('version.json'))['id'] == '1.20.2', 'Wrong reference version'
    def read(kind, name):
        return json.loads(jar.read('data/minecraft/worldgen/' + kind + '/' + name + '.json'))
    biomes = {name: read('biome', name)['features'] for name in names}
    placed = {}
    configured = {}
    def visit_placed(value):
        if isinstance(value, str):
            if value in placed:
                return
            data = read('placed_feature', value.removeprefix('minecraft:'))
            placed[value] = data
        else:
            data = value
        visit_configured(data['feature'])

    def visit_configured(value):
        if isinstance(value, str):
            if value in configured:
                return
            data = read('configured_feature', value.removeprefix('minecraft:'))
            configured[value] = data
        else:
            data = value
        kind, config = data['type'], data['config']
        if kind == 'minecraft:random_selector':
            for item in config['features']:
                visit_placed(item['feature'])
            visit_placed(config['default'])
        elif kind == 'minecraft:simple_random_selector':
            for item in config['features']:
                visit_placed(item)
        elif kind == 'minecraft:random_boolean_selector':
            visit_placed(config['feature_true'])
            visit_placed(config['feature_false'])
        elif kind == 'minecraft:random_patch':
            visit_placed(config['feature'])
    for steps in biomes.values():
        for step in steps:
            for key in step:
                visit_placed(key)
    data = {'version': '1.20.2', 'biomes': biomes, 'placed': placed, 'configured': configured}
    target = root / 'src/main/resources/assets/htgt6/worldgen/futura_placements_1202.json'
    target.write_text(json.dumps(data, indent=2) + '\n', encoding='utf-8')
    print('Extracted', len(biomes), 'biomes and', len(placed), 'placed features to', target)
    fossil_dir = root / 'src/main/resources/assets/htgt6/structures/fossil'
    fossil_dir.mkdir(parents=True, exist_ok=True)
    for name in jar.namelist():
        if name.startswith('data/minecraft/structures/fossil/') and name.endswith('.nbt'):
            (fossil_dir / pathlib.PurePosixPath(name).name).write_bytes(jar.read(name))
    structures = {name: read('structure_set', name) for name in
                  ('mineshafts', 'strongholds', 'desert_pyramids', 'jungle_temples', 'swamp_huts', 'igloos')}
    structures['stronghold_biased_to'] = json.loads(jar.read('data/minecraft/tags/worldgen/biome/stronghold_biased_to.json'))['values']
    (target.parent / 'futura_structure_placements_1202.json').write_text(json.dumps(structures, indent=2) + '\n', encoding='utf-8')
