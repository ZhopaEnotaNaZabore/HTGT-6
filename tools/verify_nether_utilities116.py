"""Validate actual anchor model inheritance, official textures and cloud-only audio."""
import argparse, hashlib, json, struct, zipfile
from pathlib import Path
ap=argparse.ArgumentParser();ap.add_argument('--reference');ap.add_argument('--index');args=ap.parse_args()
root=Path(__file__).resolve().parents[1]/'src/main/resources';base=root/'assets/htgt6';checks=0
reference=zipfile.ZipFile(args.reference) if args.reference else None
index=json.loads(Path(args.index).read_text())['objects'] if args.index else None
if args.index:
 assert hashlib.sha1(Path(args.index).read_bytes()).hexdigest()=='117379a04a44c80fc5a5806f2e676ca4ac81f5ce'
def check(value,message):
 global checks
 assert value,message
 checks+=1
def model(name,ancestors=()):
 check(name not in ancestors,'cyclic parent '+name)
 ns,path=name.split(':');d=json.loads((root/'assets'/ns/'models'/(path+'.json')).read_text())
 parent=model(d['parent'],ancestors+(name,)) if 'parent' in d else {}
 merged=dict(parent);merged.update(d);merged['textures']=dict(parent.get('textures',{}));merged['textures'].update(d.get('textures',{}));return merged
def texture(name,textures):
 seen=set()
 while name.startswith('#'):
  check(name not in seen,'cyclic texture');seen.add(name);name=textures[name[1:]]
 ns,path=name.split(':');path=path.removeprefix('block/')
 data=(root/'assets'/ns/'textures/blocks'/(path+'.png')).read_bytes();check(data.startswith(b'\x89PNG'),'PNG texture')
 if reference:check(data==reference.read('assets/minecraft/textures/block/'+path.rsplit('/',1)[-1]+'.png'),'official texture bytes')
state=json.loads((base/'blockstates/1_20_backport/respawn_anchor.json').read_text())
check(set(state['variants'])=={'charges='+str(i) for i in range(5)},'all five charge states')
for i in range(5):
 d=model(state['variants']['charges='+str(i)]['model']);check(len(d['elements'])==1,'full cube')
 for face in d['elements'][0]['faces'].values():texture(face['texture'],d['textures'])
 check(d['textures']['side'].endswith('respawn_anchor_side'+str(i)),'charge side texture')
 check(d['textures']['top'].endswith('respawn_anchor_top' if i else 'respawn_anchor_top_off'),'charged top texture')
d=model('htgt6:item/1_20_backport/respawn_anchor');check(d['textures']['side'].endswith('side0'),'item drops uncharged')
meta=json.loads((base/'textures/blocks/1_20_backport/respawn_anchor_top.png.mcmeta').read_text());check('animation' in meta,'top animation')
for i in range(11):
 data=(base/f'textures/particle/soul_{i}.png').read_bytes();check(data.startswith(b'\x89PNG'),'soul sprite')
 if reference:check(data==reference.read(f'assets/minecraft/textures/particle/soul_{i}.png'),'official soul sprite')
manifest=json.loads((root/'htgt6-external-sounds.json').read_text());imports=manifest['officialIndex116']['entries'];paths={e['resource']:e for e in imports};sounds=json.loads((base/'sounds.json').read_text())
check(len(imports)<=64,'bounded cloud manifest')
for event,count in [('block.respawn_anchor.ambient',3),('block.respawn_anchor.charge',3),('block.respawn_anchor.deplete',2),('block.respawn_anchor.set_spawn',3),('particle.soul_escape',13)]:
 check(len(sounds[event]['sounds'])==count,'sound variants '+event)
 for sound in sounds[event]['sounds']:
  ns,path=sound['name'].split(':');resource='assets/'+ns+'/sounds/'+path+'.ogg'
  check(resource in paths,'cloud object mapping');check(not (root/resource).exists(),'audio must stay outside mod assets')
  if index:check(paths[resource]['officialResource'] in index,'pinned official index object')
for lang in ['en_US','ru_RU']:
 lines=(base/'lang'/(lang+'.lang')).read_text(encoding='utf-8').splitlines()
 for key in ['tile.backport_1_20.respawn_anchor.name','enchantment.soul_speed','block.minecraft.set_spawn']:
  check(sum(line.startswith(key+'=') for line in lines)==1,'unique localization '+key)
print('PASS B116-06/07 assets:',checks,'checks; official reference=',bool(reference),'official index=',bool(index))
