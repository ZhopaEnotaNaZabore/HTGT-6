"""Inventory every bundled structure and compare decoded NBT with pinned 1.20.2."""
import ast
import collections
import gzip
import io
import json
import pathlib
import struct
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
# Reuse the stdlib NBT reader without executing its command-line report.
tree = ast.parse((ROOT / 'tools/inspect_shipwreck_nbt.py').read_text(encoding='utf-8-sig'))
exec(compile(ast.Module(body=[n for n in tree.body if isinstance(n, ast.FunctionDef)], type_ignores=[]), '<nbt-reader>', 'exec'))

def decode(data):
    stream = io.BytesIO(gzip.decompress(data))
    kind = stream.read(1)[0]
    read_string(stream)
    return read_payload(stream, kind)

def audit():
    groups = {}
    issues = []
    aliases = {'village_1_20': 'village', 'fossils': 'fossil', 'nether_fossils': 'nether_fossils', 'endcity': 'end_city', 'mansion': 'woodland_mansion', 'ocean_ruin': 'underwater_ruin'}
    with zipfile.ZipFile(ROOT / 'tools/decompile1202/reference/client.jar') as ref:
        for folder in sorted((ROOT / 'src/main/resources/assets/htgt6/structures').iterdir()):
            if not folder.is_dir():
                continue
            group = dict(templates=0, blocks=0, block_nbt=0, entities=0, exact_nbt=0,
                         different_nbt=[], missing_in_reference=[], reference_only=[], changed_fields={}, payload_equal=0)
            local_names = set()
            prefix = 'data/minecraft/structures/' + aliases.get(folder.name, folder.name) + '/'
            for path in sorted(folder.rglob('*.nbt')):
                name = path.relative_to(folder).as_posix()
                reference_name = name
                if folder.name == 'fossils': reference_name = name.replace('fossil_', '', 1).replace('_0', '_')
                if folder.name == 'igloo': reference_name = name.replace('igloo_', '', 1)
                if folder.name == 'mansion' and name in ['carpet_south.nbt','carpet_west.nbt','indoors_door.nbt','indoors_wall.nbt']: reference_name = name.replace('.nbt','_1.nbt')
                local_names.add(reference_name)
                data = decode(path.read_bytes())
                group['templates'] += 1
                group['blocks'] += len(data['blocks'])
                group['entities'] += len(data.get('entities', []))
                palettes = data.get('palettes', [data.get('palette', [])])
                for block in data['blocks']:
                    group['block_nbt'] += 'nbt' in block
                    if not all(0 <= block['state'] < len(p) for p in palettes):
                        issues.append(str(path.relative_to(ROOT)) + ': invalid palette index')
                    if not all(0 <= v < limit for v, limit in zip(block['pos'], data['size'])):
                        issues.append(str(path.relative_to(ROOT)) + ': position outside size')
                upstream = prefix + reference_name
                if upstream not in ref.namelist():
                    group['missing_in_reference'].append(name)
                elif data == decode(ref.read(upstream)):
                    group['exact_nbt'] += 1
                else:
                    group['different_nbt'].append(name)
                    other = decode(ref.read(upstream))
                    changed = [k for k in set(data) | set(other) if data.get(k) != other.get(k)]
                    for k in changed: group['changed_fields'][k] = group['changed_fields'].get(k, 0) + 1
                    if all(k in ['DataVersion','author'] for k in changed): group['payload_equal'] += 1
            group['reference_only'] = sorted(n[len(prefix):] for n in ref.namelist()
                                            if n.startswith(prefix) and n.endswith('.nbt')
                                            and n[len(prefix):] not in local_names)
            groups[folder.name] = group
    result = dict(reference='local official Minecraft 1.20.2 client.jar', groups=groups, invalid=issues,
                  note='Decoded asset audit; does not prove runtime generation or modern block-state parity.')
    output = ROOT / 'build/batch47-structure-nbt-audit.json'
    output.write_text(json.dumps(result, indent=2, ensure_ascii=False), encoding='utf-8')
    for name, g in groups.items():
        print(name, 'templates=', g['templates'], 'exact=', g['exact_nbt'],
              'different=', len(g['different_nbt']), 'missing=', len(g['missing_in_reference']),
              'reference_only=', len(g['reference_only']))
    print('Invalid records:', len(issues))
    if issues:
        raise SystemExit(1)

if __name__ == '__main__':
    audit()
