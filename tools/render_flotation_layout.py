"""Render the actual NEI layout exported by verifyFlotationChamber, not a concept image."""
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

root = Path(__file__).resolve().parents[1]
blocks = {tuple(row[:3]): row[3] for row in json.loads((root / 'build/flotation-layout.json').read_text())}
image = Image.new('RGB', (1400, 850), '#18232b')
draw = ImageDraw.Draw(image)
font = ImageFont.truetype('C:/Windows/Fonts/arial.ttf', 21)
title = ImageFont.truetype('C:/Windows/Fonts/arialbd.ttf', 32)
draw.text((48, 32), 'Флотационная камера — строительная схема', font=title, fill='#e9f2f5')
draw.text((48, 80), '13 × 11 × 7 • три открытые ванны • контроллер справа спереди', font=font, fill='#9fb7c3')
colors = {1: (62, 169, 184), 2: (187, 146, 65), 3: (115, 127, 136), 4: (86, 178, 188), 5: (72, 84, 96)}

def project(x, y, z):
    return 310 + (x + z) * 30, 475 + (x - z) * 14 - y * 35

faces = []
for (x, y, z), kind in blocks.items():
    # Visible faces from the front-right viewpoint. Omit internal faces.
    for neighbor, corners, shade in [
        ((x,y+1,z), [(x,y+1,z),(x+1,y+1,z),(x+1,y+1,z+1),(x,y+1,z+1)], 1.12),
        ((x+1,y,z), [(x+1,y,z),(x+1,y,z+1),(x+1,y+1,z+1),(x+1,y+1,z)], .70),
        ((x,y,z-1), [(x,y,z),(x+1,y,z),(x+1,y+1,z),(x,y+1,z)], .92),
    ]:
        if neighbor in blocks and blocks[neighbor] != 4:
            continue
        depth = sum(a-c+b*.6 for a,b,c in corners)/4
        faces.append((depth,corners,kind,shade))
for _,corners,kind,shade in sorted(faces, key=lambda entry: entry[0]):
    polygon = [project(*point) for point in corners]
    color = tuple(min(255,int(v*shade)) for v in colors[kind])
    draw.polygon(polygon, fill=color, outline='#25323c')
    if kind==4:
        a,b,c,d=polygon
        draw.line((a[0]+5,a[1],c[0]-5,c[1]),fill='#bce5e9',width=2)

for i,(kind,label) in enumerate([(3,'Корпус'),(5,'Усиленная рама'),(4,'Смотровое стекло'),(2,'Шлюз'),(1,'Контроллер')]):
    xx=48+i*267
    draw.rectangle((xx,710,xx+20,730), fill=colors[kind])
    draw.text((xx+28,706),label,font=font,fill='#dbe7ed')
draw.text((48,765),'Реальные строительные блоки из NEI. Модель труб, ограждений и мешалок здесь не показана.',font=font,fill='#9fb7c3')
out=root/'ToDo/Multitile/FlotationChamber/flotation-structure-v2.png'
image.save(out)
print(out)

