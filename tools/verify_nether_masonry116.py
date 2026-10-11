"""Compare all masonry states/models/textures to the locally supplied Mojang reference."""
import json, zipfile, argparse
from pathlib import Path
ap=argparse.ArgumentParser();ap.add_argument('--reference',default='tools/decompile1202/reference/client.jar');args=ap.parse_args()
root=Path(__file__).resolve().parents[1]/'src/main/resources';base=root/'assets/htgt6';checks=0
z=zipfile.ZipFile(args.reference)
names=['basalt','polished_basalt','chiseled_nether_bricks','cracked_nether_bricks','polished_blackstone_button','polished_blackstone_pressure_plate','blackstone_stairs','blackstone_slab','polished_blackstone_stairs','polished_blackstone_slab','polished_blackstone_brick_stairs','polished_blackstone_brick_slab','blackstone_wall','polished_blackstone_wall','polished_blackstone_brick_wall']
def check(b,message):
 global checks
 assert b,message
 checks+=1
def canonical(v):
 if isinstance(v,dict):return {k:canonical(x) for k,x in v.items()}
 if isinstance(v,list):return [canonical(x) for x in v]
 if isinstance(v,str):return v.replace('htgt6:block/1_20_backport/nether116/','block/').replace('htgt6:block/1_20_backport/','block/').removeprefix('minecraft:')
 return v
for name in names:
 for folder in ['blockstates','models/item']:
  d=json.loads((base/folder/'1_20_backport'/(name+'.json')).read_text(encoding='utf-8'))
  check(canonical(d)==canonical(json.loads(z.read('assets/minecraft/'+folder+'/'+name+'.json'))),'canonical '+folder+'/'+name)
for path in (base/'models/block/1_20_backport/nether116').glob('*.json'):
 d=json.loads(path.read_text(encoding='utf-8'))
 check(canonical(d)==canonical(json.loads(z.read('assets/minecraft/models/block/'+path.name))),'canonical model '+path.name)
 for tex in d.get('textures',{}).values():
  if not tex.startswith('#'):
   name=tex.rsplit('/',1)[1]
   check((base/'textures/blocks/1_20_backport'/(name+'.png')).read_bytes()==z.read('assets/minecraft/textures/block/'+name+'.png'),'canonical texture '+name)
m=json.loads((root/'htgt6-external-sounds.json').read_text(encoding='utf-8'))
paths={e['resource'] for e in m['entries']+m['officialIndex116']['entries']}
check(len(paths)==len(m['entries'])+len(m['officialIndex116']['entries']),'no duplicate cloud paths')
check(len(m['officialIndex116']['entries'])<=64,'bounded imports')
s=json.loads((base/'sounds.json').read_text(encoding='utf-8'))
events=[f'block.{f}.{a}' for f in ['basalt','stone','nether_bricks'] for a in ['break','step','place']]+[f'block.stone_{f}.click_{a}' for f in ['button','pressure_plate'] for a in ['on','off']]
events += ["item.bucket.fill","item.bucket.empty"]
for event in events:
 for sound in s[event]['sounds']:
  ns,name=sound['name'].split(':');resource=f'assets/{ns}/sounds/{name}.ogg'
  check(resource in paths,'cloud '+resource);check(not (root/resource).exists(),'no bundled audio '+resource)
for lang in ['en_US','ru_RU']:
 lines=(base/'lang'/(lang+'.lang')).read_text(encoding='utf-8').splitlines()
 for name in names[2:]:check(sum(line.startswith('tile.backport_1_20.'+name+'.name=') for line in lines)==1,'localized '+lang+'/'+name)
print('Nether masonry resources:',checks,'checks passed')
