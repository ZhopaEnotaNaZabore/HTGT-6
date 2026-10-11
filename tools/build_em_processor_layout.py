"""Deterministic proposed block layout; does not modify the live machine."""
from pathlib import Path
from collections import Counter
import json
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'ToDo/Multitile/ElectromagneticProcessor'
OUT.mkdir(parents=True, exist_ok=True)
W, D, H = 7, 9, 6
TYPES = {
    'B': ('Основание', '#414d5e'), 'C': ('Корпус / опора', '#8799ab'),
    'G': ('Укреплённое стекло', '#699ba9'), 'M': ('Блок катушки', '#ce8950'),
    'T': ('Канал / транспортёр', '#5c7684'), 'D': ('Узел разделения', '#a8ad87'),
    'U': ('Контроллер', '#47d3d5'), 'I': ('Предметный вход', '#6ca76b'),
    'O': ('Предметный выход', '#d5ad62'), 'E': ('Энерговвод', '#dc6666'),
    'F': ('Жидкостный вход*', '#6296d9'), 'W': ('Верхняя шина', '#9f693e'),
}
blocks = {}
def put(x,y,z,t):
    assert 0 <= x < W and 0 <= y < H and 0 <= z < D
    blocks[x,y,z] = t

for x in range(W):
    for z in range(D): put(x,0,z,'B')
# Side sills and front/rear bulkheads.
for z in range(D):
    for x in (0,6):
        for y in (1,2): put(x,y,z,'C')
for z in (0,8):
    for x in range(1,6): put(x,1,z,'C')
# A continuous transport support, with the working channel above it.
for z in range(1,8):
    for x in range(2,5):
        put(x,1,z,'C')
        put(x,2,z,'T')
# Three transverse closed square magnet frames; aperture x=2..4, y=2..4.
for z in (2,4,6):
    for x in range(1,6):
        put(x,1,z,'M'); put(x,5,z,'M')
    for x in (1,5):
        for y in range(2,5): put(x,y,z,'M')
for z in (3,5): put(5,5,z,'W')
# Sight windows along both sides, closed by a narrow upper casing edge.
for x in (0,6):
    for z in range(1,8):
        put(x,2,z,'G'); put(x,3,z,'G'); put(x,4,z,'C')
# Rear drum volume replaces the last segment of the channel.
for x in range(2,5):
    for y in (2,3): put(x,y,7,'D')
for x in (1,5):
    for y in (2,3): put(x,y,7,'C')
# Front hopper volume and the actual intake hatch.
for x in (2,4): put(x,2,0,'C'); put(x,3,0,'C')
put(3,2,0,'I')
put(0,1,0,'U')
for x in (2,3,4): put(x,1,8,'O')
put(6,1,2,'E')
put(6,1,1,'F')

assert sum(t == 'M' for t in blocks.values()) == 48
assert sum(t == 'U' for t in blocks.values()) == 1
assert len(blocks) == len(set(blocks))
counts = Counter(blocks.values())
layers = [{'y':y, 'rows_z_front_to_back':[''.join(blocks.get((x,y,z),'.') for x in range(W)) for z in range(D)]} for y in range(H)]
data = {'status':'proposed_reference_not_implemented', 'dimensions':{'width_x':W,'depth_z':D,'height_y':H},
        'axes':'x left to right facing front; z front to rear; y bottom to top; all zero-based',
        'air':'.', 'optional':'F is an auxiliary fluid inlet; a casing may substitute it for current dry recipes.',
        'legend':{t:{'name':name,'color':color,'count':counts[t]} for t,(name,color) in TYPES.items()},
        'layers':layers,
        'blocks':[dict(x=x,y=y,z=z,type=t) for (x,y,z),t in sorted(blocks.items())]}
(OUT/'electromagnetic-processor-layout-v1.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

# Raster diagram drawn from the block data, independent of the generated concept image.
im=Image.new('RGB',(2700,1900),'#111923'); draw=ImageDraw.Draw(im)
fontpath='C:/Windows/Fonts/arial.ttf'
def font(size): return ImageFont.truetype(fontpath,size)
def text(x,y,value,size=25,color='#d7e5f0'): draw.text((x,y),value,font=font(size),fill=color)
def rgb(color,factor=1):
    return tuple(min(255,int(int(color[i:i+2],16)*factor)) for i in (1,3,5))
def proj(x,y,z): return (230+70*(x+z),1090+35*(x-z)-70*y)
def poly(vertices,color): draw.polygon([proj(*p) for p in vertices],fill=color,outline='#10151c',width=2)
def render(visible):
    for (x,y,z),t in sorted(visible.items(),key=lambda kv:kv[0][0]-kv[0][2]+kv[0][1]):
        color=TYPES[t][1]
        if (x,y,z-1) not in visible:
            poly([(x,y,z),(x+1,y,z),(x+1,y+1,z),(x,y+1,z)],rgb(color,.84))
        if (x+1,y,z) not in visible:
            poly([(x+1,y,z),(x+1,y,z+1),(x+1,y+1,z+1),(x+1,y+1,z)],rgb(color,.65))
        if (x,y+1,z) not in visible:
            poly([(x,y+1,z),(x+1,y+1,z),(x+1,y+1,z+1),(x,y+1,z+1)],rgb(color,1.13))
        if t in 'UIOEF' and (x+1,y,z) not in visible:
            sx,sy=proj(x+1,y+.65,z+.35)
            text(sx-8,sy-10,t,23,'#ffffff')

text(65,48,'ЭЛЕКТРОМАГНИТНЫЙ ОБРАБОТЧИК',48)
text(68,112,'Кубическая схема v1 · 7 × 9 × 6 · ширина × глубина × высота',30,'#92b6c8')
text(68,170,'Предлагаемая раскладка для будущего мультиблока',24,'#8497a8')
# Cutaway leaves the farther wall visible; all layer plans show the full structure.
visible={p:t for p,t in blocks.items() if not(p[0]==6 and 1<=p[2]<=7 and p[1] in (2,3,4))}
render(visible)
text(85,1290,'ИЗОМЕТРИЯ С РАЗРЕЗОМ',31)
text(85,1340,'Ближние окна и верхний край стенки скрыты для обзора катушек.',24,'#99abba')
text(85,1375,'Полная конструкция — в послойной схеме справа.',24,'#99abba')
sx,sy=proj(0,0,0); text(sx-120,sy+85,'ПЕРЕД / Z=0',25,'#67cdd3')
text(700,1410,'X → 7 блоков',24)
sx,sy=proj(7,0,9); text(sx-5,sy,'Z → 9',24)
for i,layer in enumerate(layers):
    col=i%2; row=i//2; ox=1620+col*505; oy=190+row*490; cell=42
    text(ox,oy,f'СЛОЙ Y={i}'+(' · основание' if i==0 else ''),27)
    for x in range(W): text(ox+41+x*cell,oy+43,str(x),21,'#8497a8')
    for z,line in enumerate(layer['rows_z_front_to_back']):
        text(ox+5,oy+78+z*cell,str(z),21,'#8497a8')
        for x,t in enumerate(line):
            left=ox+35+x*cell; top=oy+70+z*cell
            draw.rectangle((left,top,left+cell-2,top+cell-2),fill=TYPES[t][1] if t!='.' else '#1a2632',outline='#354756')
            text(left+12,top+7,t,23,'#ffffff' if t!='.' else '#516371')
    text(ox+35,oy+455,'Z=0 сверху: передняя сторона',20,'#91a6b7')

text(70,1455,'ОБОЗНАЧЕНИЯ И КОЛИЧЕСТВО ПОЗИЦИЙ',29)
for i,(t,(label,color)) in enumerate(TYPES.items()):
    x=75+(i%4)*385; y=1515+(i//4)*72
    draw.rectangle((x,y,x+40,y+40),fill=color)
    text(x+11,y+6,t,23,'#ffffff'); text(x+53,y+6,label+' — '+str(counts[t]),23)
text(70,1770,f'Всего занятых позиций: {len(blocks)} из {W*D*H}. Точка в слоях — воздух.',25)
text(70,1810,'* Жидкостный вход — резерв: для текущих сухих рецептов допускается корпус.',23,'#9fbbcd')
text(70,1845,'Это концепт структуры. Типы блоков, шлюзы и количество материалов ещё не реализованы в игре.',23,'#9fbbcd')
im.save(OUT/'electromagnetic-processor-cubic-layout-v1.png')
print('Saved cubic diagram and block layout JSON:',len(blocks),'positions',dict(counts))
