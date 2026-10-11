"""Package opaque artwork compactly; preserve masters outside runtime resources."""
from pathlib import Path
from PIL import Image
import io
import json
import shutil

ROOT = Path(__file__).resolve().parents[1]
RUNTIME = ROOT / 'src/main/resources/assets/htgt6/textures/gui/mainmenu'
MASTERS = ROOT / 'design/mainmenu/masters'
MASTERS.mkdir(parents=True, exist_ok=True)
report = []
for filename in ['background.png', 'background_industrial.png']:
    source = RUNTIME / filename
    master = MASTERS / filename
    if not master.exists():
        shutil.copy2(source, master)
    with Image.open(master) as image:
        image.load()
        output = io.BytesIO()
        if filename == 'background_industrial.png':
            image.convert('RGB').save(output, format='JPEG', quality=92, subsampling=0, optimize=True)
            target = RUNTIME / 'background_industrial.jpg'
        else:
            image.save(output, format='PNG', optimize=True, compress_level=9)
            target = source
        data = output.getvalue()
        if len(data) >= master.stat().st_size:
            raise RuntimeError('Compression did not reduce size: ' + filename)
        with Image.open(io.BytesIO(data)) as check:
            check.load()
            assert check.size == image.size
            if target.suffix == '.png':
                assert check.convert('RGBA').tobytes() == image.convert('RGBA').tobytes()
        target.write_bytes(data)
        report.append(dict(file=target.name, width=image.width, height=image.height,
                           before_bytes=master.stat().st_size, after_bytes=len(data),
                           lossless=target.suffix == '.png'))
    if filename == 'background_industrial.png' and source.exists():
        # Only the JPEG is packaged. Its original is retained in design/mainmenu/masters.
        source.unlink()
(ROOT / 'design/mainmenu/compression-report.json').write_text(json.dumps(report, indent=2)+'\n', encoding='utf-8')
print(json.dumps(report, indent=2))
