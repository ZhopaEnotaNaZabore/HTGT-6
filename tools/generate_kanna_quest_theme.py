"""Generate BQ Kanna layered UI and a pixel-art quest-frame atlas (requires Pillow)."""
import json
from PIL import Image, ImageDraw
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def color(c): return {'colorType':'betterquesting:color_static','color':c if len(c)==8 else 'FF'+c}
def fill(c,p=0): return {'textureType':'betterquesting:texture_color','color':color(c),'padding':[p]*4}
def frame(edge,inside,p=1): return {'textureType':'betterquesting:texture_layered','layers':[fill(edge),fill(inside,p)]}
tex={}
for name,edge,bg in [('panel_main','62432C','151413'),('panel_dark','34302B','0D0D0D'),('panel_inner','44382D','1D1A17'),('item_frame','67503B','111110'),('aux_frame_0','41392F','60171512'),('aux_frame_1','FF9F43','302316')]:tex[name]=frame(edge,bg)
for group in ['btn_normal','btn_clean','btn_alt','text_box']:
 for n,(edge,bg) in enumerate([('393633','1A1918'),('87603B','29221A'),('FFAB55','49301B')]):tex[f'{group}_{n}']=frame(edge,bg)
for axis in ['h','v']:
 tex[f'scroll_{axis}_bg']=fill('10100F')
 for n,c in enumerate(['45403A','AF783F','FFAD56']):tex[f'scroll_{axis}_{n}']=frame(c,'292018')
 tex[f'meter_{axis}_0']=frame('514131','10100F')
 tex[f'meter_{axis}_1']=fill('EB963F')
tex['hotbar_0']=frame('514131','161411');tex['hotbar_1']=frame('FFAB55','33261B')
cols={'text_header':'FFBC75','text_main':'EEE7DE','text_aux_0':'EEE7DE','text_aux_1':'C7B8A5','text_highlight':'FFC47D','text_watermark':'958675','item_highlight':'50FFAC56','gui_divider':'765130','update_notice':'FFD38A','grid_major':'483629','grid_minor':'29241F','btn_disabled':'8F867D','btn_idle':'EEE7DE','btn_hover':'FFE4BE','quest_line_implicit_mixin':'00000000'}
for state,c in [('locked','6E6257'),('unlocked','EBA050'),('pending','FFE5A4'),('complete','8BC694'),('repeatable','D5ACEE')]:
 for kind in ['line','icon']:cols[f'quest_{kind}_{state}']=c
lines={k:{'lineType':'betterquesting:line_simple','stippleScale':1,'stippleMask':'1111000011110000' if k in ['quest_locked','grid_minor','quest_repeatable'] else '1111111111111111'} for k in ['gui_divider','grid_major','grid_minor','quest_locked','quest_unlocked','quest_pending','quest_complete','quest_repeatable']}
# Grayscale pixel frames are tinted by BQ's quest-state color at draw time.
# Keep the central 16x16 area quiet and all ornaments inside the native 24x24 bounds.
atlas=Image.new('RGBA',(256,256))
for col,kind in enumerate(['norm','main','aux']):
 for state in range(5):
  tile=Image.new('RGBA',(24,24));d=ImageDraw.Draw(tile)
  edge=(210,210,210,255);bright=(255,255,255,255);dark=(24,24,24,255)
  shape=[(4,1),(19,1),(22,4),(22,19),(19,22),(4,22),(1,19),(1,4)]
  d.polygon(shape,fill=dark,outline=edge)
  d.line([(5,3),(18,3)],fill=(90,90,90,255))
  d.line([(3,5),(3,18)],fill=(90,90,90,255))
  d.line([(5,20),(18,20)],fill=(90,90,90,255))
  d.line([(20,5),(20,18)],fill=(90,90,90,255))
  for px,py in [(3,3),(19,3),(3,19),(19,19)]:
   d.rectangle((px,py,px+1,py+1),fill=bright)
  if kind=='main':
   # Twin corner crests echo Kanna's ears; double rails mark branch milestones.
   d.line([(1,7),(1,0),(7,2)],fill=bright)
   d.line([(16,2),(22,0),(22,7)],fill=bright)
   d.line([(7,23),(16,23)],fill=bright)
  if kind=='aux':
   d.line([(0,9),(0,14)],fill=bright);d.line([(23,9),(23,14)],fill=bright)
  if state==0:d.line([(9,1),(14,1)],fill=(72,72,72,255))
  elif state==2:d.line([(8,0),(15,0)],fill=bright)
  elif state==3:d.line([(9,21),(11,23),(15,19)],fill=bright)
  elif state==4:
   d.line([(8,22),(14,22),(12,20)],fill=bright)
  atlas.alpha_composite(tile,(col*24,state*24))
  tex[f'quest_{kind}_{state}']={'textureType':'betterquesting:texture_sliced',
   'atlas':'htgt6:textures/gui/quests/kanna_frames.png',
   'bounds':[col*24,state*24,24,24],'padding':[4,4,4,4],'sliceMode':2}
atlas_path=ROOT/'src/main/resources/assets/htgt6/textures/gui/quests/kanna_frames.png'
atlas_path.parent.mkdir(parents=True,exist_ok=True);atlas.save(atlas_path)
ns=lambda d:{'betterquesting:'+k:v for k,v in d.items()}
theme={'themeID':'htgt6:kanna','themeName':'HiTech6 Kanna','themeParent':'betterquesting:dark','textures':ns(tex),'colors':ns({k:color(v) for k,v in cols.items()}),'lines':ns(lines)}
p=ROOT/'src/main/resources/assets/htgt6/bq_themes.json'
p.write_text(json.dumps([theme],indent=2)+'\n',encoding='utf-8')
print('Generated Kanna:',len(tex),'textures,',len(cols),'colors,',len(lines),'lines')
