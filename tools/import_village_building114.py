"""Import the missing 1.14 shapes/signs from the local official 1.20.2 reference."""
import json, zipfile
from pathlib import Path

root = Path('src/main/resources/assets/htgt6')
reference = zipfile.ZipFile('tools/decompile1202/reference/client.jar')
families = {'stone': ['slab', 'stairs'], 'mossy_stone_brick': ['slab', 'stairs', 'wall'],
            'smooth_red_sandstone': ['slab', 'stairs'], 'smooth_quartz': ['slab', 'stairs'],
            'end_stone_brick': ['slab', 'stairs', 'wall'], 'red_nether_brick': ['slab', 'stairs', 'wall'],
            'cut_sandstone': ['slab'], 'cut_red_sandstone': ['slab']}
shapes = [f'{name}_{kind}' for name, kinds in families.items() for kind in kinds]
shapes += [f'{name}_wall' for name in ['brick', 'stone_brick', 'nether_brick', 'sandstone', 'red_sandstone', 'prismarine']]
woods = ['spruce', 'birch', 'jungle', 'acacia', 'dark_oak']
seen = set()

def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)

def model(name, item_namespace):
    name = name.split(':')[-1]
    key = (name, item_namespace)
    if name.startswith('builtin/'):
        return name
    target = ('village_items114/' + name) if item_namespace else ('block/1_20_backport/' + name.split('/', 1)[-1])
    if key in seen:
        return target
    seen.add(key)
    data = json.loads(reference.read('assets/minecraft/models/' + name + '.json'))
    if 'parent' in data:
        parent = model(data['parent'], item_namespace)
        data['parent'] = parent if parent.startswith('builtin/') else 'htgt6:' + parent
    for texture in data.get('textures', {}).values():
        if texture.startswith('#'):
            continue
        texture = texture.split(':')[-1]
        source = 'assets/minecraft/textures/' + texture + '.png'
        dest = root / 'textures/blocks' / ('village_items114/' + texture if item_namespace else '1_20_backport/' + texture.split('/', 1)[-1])
        write(dest.with_suffix('.png'), reference.read(source))
        if source + '.mcmeta' in reference.namelist():
            write(Path(str(dest) + '.png.mcmeta'), reference.read(source + '.mcmeta'))
    write(root / 'models' / (target + '.json'), (json.dumps(data, indent=2) + '\n').encode())
    return target

def state_walk(value):
    if isinstance(value, dict):
        for key, child in value.items():
            if key == 'model':
                value[key] = 'htgt6:' + model(child, False)
            else:
                state_walk(child)
    elif isinstance(value, list):
        for child in value:
            state_walk(child)

for name in shapes:
    state = json.loads(reference.read('assets/minecraft/blockstates/' + name + '.json'))
    state_walk(state)
    write(root / 'blockstates/1_20_backport' / (name + '.json'), (json.dumps(state, indent=2) + '\n').encode())
    model('item/' + name, True)
for wood in woods:
    model('item/' + wood + '_sign', True)
    write(root / 'textures/items/1_20_backport' / (wood + '_sign.png'), reference.read('assets/minecraft/textures/item/' + wood + '_sign.png'))
    write(root / 'textures/entity/1_20_backport/signs' / (wood + '.png'), reference.read('assets/minecraft/textures/entity/signs/' + wood + '.png'))

recipes = []
for path in sorted(reference.namelist()):
    if not path.startswith('data/minecraft/recipes/') or not path.endswith('.json'):
        continue
    recipe = json.loads(reference.read(path))
    if recipe.get('type') == 'minecraft:stonecutting' and recipe['result'].split(':')[-1] in shapes:
        recipes.append(dict(id=Path(path).stem, **recipe))
write(root / 'recipes/village_building114_stonecutting.json', (json.dumps(recipes, indent=2) + '\n').encode())
manifest_path = Path('docs/backport-audit-2026-10-01/items114-models.json')
manifest = json.loads(manifest_path.read_text(encoding='utf-8'))
manifest['items'] = sorted(set(manifest['items']) | set(shapes) | {w + '_sign' for w in woods})
manifest['models'] = sorted(p.relative_to(root / 'models/village_items114').with_suffix('').as_posix() for p in (root / 'models/village_items114').rglob('*.json'))
manifest['textures'] = sorted(p.relative_to(root / 'textures/blocks/village_items114').with_suffix('').as_posix() for p in (root / 'textures/blocks/village_items114').rglob('*.png'))
manifest_path.write_text(json.dumps(manifest, indent=2) + '\n', encoding='utf-8')
assets = Path.home() / 'AppData/Roaming/.minecraft/assets'
index = json.loads((assets / 'indexes/5.json').read_text())['objects']
for locale, legacy in [('en_us', 'en_US'), ('ru_ru', 'ru_RU')]:
    if locale == 'en_us':
        language = json.loads(reference.read('assets/minecraft/lang/en_us.json'))
    else:
        digest = index['minecraft/lang/' + locale + '.json']['hash']
        language = json.loads((assets / 'objects' / digest[:2] / digest).read_text(encoding='utf-8'))
    path = root / 'lang' / (legacy + '.lang')
    text = path.read_text(encoding='utf-8')
    entries = {f'tile.backport_1_20.{s}.name': language['block.minecraft.' + s] for s in shapes}
    entries.update({f'item.backport_1_20.{w}_sign.name': language['block.minecraft.' + w + '_sign'] for w in woods})
    lines = [line for line in text.splitlines() if line.split('=', 1)[0] not in entries]
    path.write_text('\n'.join(lines) + '\n' + '\n'.join(k + '=' + v for k, v in entries.items()) + '\n', encoding='utf-8')
print(f'Imported {len(shapes)} block shapes, {len(woods)} signs, {len(recipes)} stonecutting recipes')
