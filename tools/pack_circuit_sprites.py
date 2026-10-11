"""Crop the approved generated sheet and pack its twelve icons at 32px.

Only mechanical cropping/downsampling; the artwork is docs/assets/circuit-tiers/source.png.
Run scripts/pack_item_atlas.ps1 afterwards to build the shared runtime atlas.
"""
from pathlib import Path
import json
from PIL import Image

root = Path(__file__).resolve().parents[1]
folder = root / 'docs/assets/circuit-tiers'
image = Image.open(folder / 'source.png').convert('RGBA')
alpha = image.getchannel('A')

def bands(horizontal):
    length = image.width if horizontal else image.height
    breadth = image.height if horizontal else image.width
    found, start = [], None
    for k in range(length + 1):
        count = 0 if k == length else sum(alpha.getpixel((k, v) if horizontal else (v, k)) > 127
                                         for v in range(breadth))
        if count > 60 and start is None:
            start = k
        if count <= 60 and start is not None:
            if k - start > 20:
                found.append((max(0, start - 4), min(length, k + 4)))
            start = None
    return found

xs, ys = bands(True), bands(False)
assert len(xs) == 4 and len(ys) == 3, (xs, ys)
names = ['ZPMsocket','ZPMCirc','UVCorc','PUV1Circ','UXCirc','OLVCirc',
         'OMVCirc','OHVCirc','OEVCirc','OIVCirc','MAXCirc','INFICirc']
sheet = Image.new('RGBA', (128, 96))
entries = []
for i, name in enumerate(names):
    x, y = i % 4, i // 4
    cell = image.crop((xs[x][0], ys[y][0], xs[x][1], ys[y][1]))
    cell.thumbnail((26, 26), Image.Resampling.NEAREST)
    sheet.paste(cell, (x * 32 + (32-cell.width)//2, y * 32 + (32-cell.height)//2))
    entries.append(dict(name=name, row=y, column=x))
sheet.save(folder / 'atlas-32.png')
sheet.resize((768, 576), Image.Resampling.NEAREST).save(folder / 'preview-6x.png')
(folder / 'atlas.json').write_text(json.dumps(dict(tileSize=32, columns=4, rows=3, entries=entries),
                                            indent=2), encoding='utf-8')
print('Packed 12 circuit sprites at 32x32 with transparent margins.')
