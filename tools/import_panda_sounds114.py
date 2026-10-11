"""Use the repository's external-sound manifest, with official local asset-index hashes."""
import json
from pathlib import Path
assets = Path.home() / 'AppData/Roaming/.minecraft/assets'
index = json.loads((assets / 'indexes/5.json').read_text())['objects']
digest = index['minecraft/sounds.json']['hash']
official = json.loads((assets / 'objects' / digest[:2] / digest).read_text())
root = Path('src/main/resources')
sound_path = root / 'assets/htgt6/sounds.json'
sounds = json.loads(sound_path.read_text(encoding='utf-8'))
manifest_path = root / 'htgt6-external-sounds.json'
manifest = json.loads(manifest_path.read_text(encoding='utf-8'))
entries = {e['resource']: e for e in manifest['entries']}
for key, value in official.items():
    if not key.startswith('entity.panda.'):
        continue
    sounds[key] = {'category': 'neutral', 'sounds': value['sounds']}
    for sound in value['sounds']:
        name = sound if isinstance(sound, str) else sound['name']
        resource = 'minecraft/sounds/' + name + '.ogg'
        entry = index[resource]
        local = 'assets/htgt6/sounds/' + name + '.ogg'
        h = entry['hash']
        entries[local] = {'resource': local, 'sha1': h, 'size': entry['size'], 'officialResource': resource,
                          'assetIndex': '5', 'url': 'https://resources.download.minecraft.net/' + h[:2] + '/' + h}
manifest['entries'] = sorted(entries.values(), key=lambda e: e['resource'])
manifest_path.write_text(json.dumps(manifest, indent=2) + '\n', encoding='utf-8')
sound_path.write_text(json.dumps(sounds, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print('Imported 11 panda sound events and their official external resources')
