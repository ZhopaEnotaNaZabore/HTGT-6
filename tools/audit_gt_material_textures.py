"""Read-only JAR comparison; writes an audit and an HTML preview of original sprites."""
from pathlib import Path
from io import BytesIO
from PIL import Image
import base64, collections, html, json, zipfile

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'build' / 'texture-audit'
OUT.mkdir(parents=True, exist_ok=True)
archives = [zipfile.ZipFile(ROOT / p) for p in (
    'libs/gregtech_1.7.10-6.15.08-HiTech6_Edition.jar',
    'embedded-libs/gregtech-5.09.51.476.jar')]
def names(z):
    return {n for n in z.namelist() if n.startswith('assets/gregtech/textures/')
            and '/materialicons/' in n and n.endswith('.png')}
a, b = map(names, archives)
def pixels(z, path):
    im = Image.open(BytesIO(z.read(path))).convert('RGBA')
    return im.size, im.tobytes()
common = sorted(a & b)
different = [p for p in common if pixels(archives[0], p) != pixels(archives[1], p)]
bases = [p for p in common if not p.endswith('_OVERLAY.png')]
pairs = [p for p in bases if p[:-4] + '_OVERLAY.png' in a & b]
compatible = [p for p in pairs if all(pixels(archives[1], q)[0][0] <= 32
              and pixels(archives[1], q)[0][1] <= 32
              and q + '.mcmeta' not in archives[1].namelist()
              for q in [p, p[:-4] + '_OVERLAY.png'])]
summary = dict(gt6_pngs=len(a), gt5_pngs=len(b), shared_paths=len(common),
    identical_pixels=len(common)-len(different), different_pixels=len(different),
    shared_bases=len(bases), complete_pairs=len(pairs), static_pairs_at_most_32=len(compatible),
    candidate_paths=compatible, missing_gt5_paths=sorted(a-b))
(OUT / 'audit.json').write_text(json.dumps(summary, indent=2), encoding='utf-8')
selected = ['ingot', 'plate', 'plateDouble', 'foil', 'stick', 'stickLong', 'bolt', 'screw',
            'ring', 'gearGt', 'gearGtSmall', 'rotor', 'spring', 'springSmall', 'casingSmall',
            'wireFine', 'dust', 'dustSmall', 'dustTiny', 'crushed', 'gem', 'toolHeadDrill']
def sprite(z, path):
    layers = []
    for p in (path, path[:-4] + '_OVERLAY.png'):
        if p in z.namelist():
            uri = 'data:image/png;base64,' + base64.b64encode(z.read(p)).decode()
            layers.append('<img src="' + uri + '" alt="' + html.escape(p) + '">')
    return '<div class="sprite">' + ''.join(layers) + '</div>'
rows = []
for form in selected:
    path = 'assets/gregtech/textures/items/materialicons/METALLIC/' + form + '.png'
    if path not in a & b: continue
    equal = all(pixels(archives[0], q) == pixels(archives[1], q)
                for q in [path, path[:-4] + '_OVERLAY.png'])
    rows.append('<tr><td>' + form + '</td><td>' + sprite(archives[0], path) + '</td><td>'
                + sprite(archives[1], path) + '</td><td>' + ('Одинаковы' if equal else 'Отличаются') + '</td></tr>')
page = '''<!doctype html><html lang="ru"><meta charset="utf-8"><title>GT6 / GT5 — текстуры материалов</title>
<style>body{background:#171c24;color:#e6edf4;font:16px system-ui;max-width:960px;margin:40px auto;padding:0 24px}
h1{font-size:28px}p{line-height:1.6;color:#b8c5d5}table{width:100%%;border-collapse:collapse}td,th{padding:12px;border-bottom:1px solid #354052;text-align:left}
.sprite{position:relative;width:80px;height:80px;background:#303947}.sprite img{position:absolute;inset:0;width:80px;height:80px;image-rendering:pixelated}</style>
<h1>GT6 → GT5: сравнение текстур материалов</h1>
<p>Оригинальные PNG из локальных JAR. Набор METALLIC, основной слой + overlay, без окраски металлом.
Изображения увеличены средствами браузера; исходные спрайты не изменены. Это предварительное сравнение, не скриншот игрового рендера.</p>
<p>Совпадающих путей: %s; полных пар основной слой + overlay: %s; статических пар до 32×32: %s.</p>
<table><tr><th>Форма</th><th>GT6</th><th>GT5</th><th>Пиксели пары</th></tr>%s</table></html>''' % (len(common), len(pairs), len(compatible), ''.join(rows))
(OUT / 'comparison.html').write_text(page, encoding='utf-8')
print(json.dumps({k:v for k,v in summary.items() if not isinstance(v,list)}))
print(OUT / 'comparison.html')
for form in ['gearGt', 'rotor', 'plate', 'ingot']:
    p = 'assets/gregtech/textures/items/materialicons/METALLIC/' + form + '.png'
    for i,z in enumerate(archives):
        (OUT / ('gt' + str(6-i) + '-' + form + '.png')).write_bytes(z.read(p))
