"""Read-only audit of standalone PNGs identical to packed runtime sprites."""
from pathlib import Path
from PIL import Image
import hashlib
import json

root = Path(__file__).resolve().parents[1]
assets = root / 'src/main/resources/assets'
known = {}
for index in assets.rglob('*.properties'):
    if 'atlases' not in index.parts:
        continue
    props = dict(line.split('=', 1) for line in index.read_text().splitlines()
                 if '=' in line and not line.startswith('#'))
    if 'tileSize' not in props:
        continue
    image = Image.open(index.with_suffix('.png')).convert('RGBA')
    columns, tile = int(props['columns']), int(props['tileSize'])
    for key, value in props.items():
        if not key.startswith('item.'):
            continue
        name, cell = key[5:], int(value)
        size = int(props.get('size.' + name, tile))
        x, y = cell % columns * tile, cell // columns * tile
        crop = image.crop((x, y, x + size, y + size))
        digest = (crop.size, hashlib.sha256(crop.tobytes()).hexdigest())
        known.setdefault(digest, []).append(str(index.relative_to(root)) + ':' + name)

duplicates = []
sizes = {key[0] for key in known}
for path in assets.rglob('*.png'):
    if 'atlases' in path.parts:
        continue
    with Image.open(path) as image:
        if image.size not in sizes:
            continue
        digest = (image.size, hashlib.sha256(image.convert('RGBA').tobytes()).hexdigest())
    if digest in known:
        duplicates.append(dict(path=str(path.relative_to(root)), atlas=known[digest]))
print(json.dumps(dict(sprites=len(known), duplicates=duplicates), indent=2))
# No deletion here: a matching file may still have independent consumers.
raise SystemExit(bool(duplicates))
