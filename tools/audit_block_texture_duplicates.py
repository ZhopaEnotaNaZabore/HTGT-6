"""Find pixel-identical block textures with identical animation metadata.

Different resource paths can be deliberate API/model contracts. This tool
reports candidates only; it never deletes or rewrites resources.
"""
from pathlib import Path
from collections import defaultdict
from PIL import Image
import hashlib
import json

root = Path(__file__).resolve().parents[1]
groups = defaultdict(list)
count = 0
for path in sorted((root / 'src/main/resources/assets').rglob('*.png')):
    if 'textures' not in path.parts or 'blocks' not in path.parts:
        continue
    with Image.open(path) as image:
        digest = hashlib.sha256(image.convert('RGBA').tobytes()).hexdigest()
        dimensions = image.size
    metadata = path.with_suffix('.png.mcmeta')
    animation = metadata.read_bytes() if metadata.exists() else b''
    groups[(dimensions, digest, animation)].append(path)
    count += 1

duplicates = []
for (dimensions, digest, animation), paths in groups.items():
    if len(paths) < 2:
        continue
    duplicates.append(dict(dimensions=dimensions, pixel_sha256=digest,
                           animation_sha256=hashlib.sha256(animation).hexdigest(),
                           paths=[str(p.relative_to(root)).replace('\\', '/') for p in paths],
                           bytes=sum(p.stat().st_size for p in paths)))
report = dict(files=count, duplicate_groups=len(duplicates),
              extra_files=sum(len(g['paths'])-1 for g in duplicates), groups=duplicates)
target = root / 'docs/assets/cleanup/block-texture-duplicates.json'
target.parent.mkdir(parents=True, exist_ok=True)
target.write_text(json.dumps(report, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
print(json.dumps({k: v for k, v in report.items() if k != 'groups'}))
print(target)
