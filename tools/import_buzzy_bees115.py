"""Import official bee assets and pinned cloud sound references; never copy OGG into the mod."""
import json,zipfile
from pathlib import Path
root=Path('src/main/resources/assets/htgt6'); jar=zipfile.ZipFile('tools/decompile1202/reference/client.jar'); seen=set()
def write(p,b):
 p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b)
def model(name):
 name=name.split(':')[-1]
 if name.startswith('builtin/'):return name
 target='buzzy115/'+name
 if name in seen:return 'htgt6:'+target
 seen.add(name);data=json.loads(jar.read('assets/minecraft/models/'+name+'.json'))
 if 'parent' in data:data['parent']=model(data['parent'])
 for texture in data.get('textures',{}).values():
  if texture.startswith('#'):continue
  texture=texture.split(':')[-1];write(root/'textures/blocks/buzzy115'/Path(texture+'.png'),jar.read('assets/minecraft/textures/'+texture+'.png'))
 write(root/'models'/Path(target+'.json'),(json.dumps(data,indent=2)+'\n').encode());return 'htgt6:'+target
def walk(node):
 if isinstance(node,dict):
  for key,value in node.items():
   if key=='model':node[key]=model(value)
   else:walk(value)
 elif isinstance(node,list):
  for v in node:walk(v)
for name in ['beehive','bee_nest','honey_block','honeycomb_block']:
 data=json.loads(jar.read('assets/minecraft/blockstates/'+name+'.json'));walk(data)
 if list(data.get('variants',{}))==['']:data['variants']['normal']=data['variants'].pop('')
 write(root/'blockstates/buzzy115'/Path(name+'.json'),(json.dumps(data,indent=2)+'\n').encode());model('item/'+name)
for name in ['honey_bottle','honeycomb']:model('item/'+name)
for name in jar.namelist():
 if name.startswith('assets/minecraft/textures/entity/bee/') and name.endswith('.png'):write(root/'textures/entity/bee'/Path(name).name,jar.read(name))
for level in ['low','medium','high']:
 name='iron_golem_crackiness_'+level+'.png';write(root/'textures/entity/iron_golem'/name,jar.read('assets/minecraft/textures/entity/iron_golem/'+name))
assets=Path.home()/'AppData/Roaming/.minecraft/assets';index=json.loads((assets/'indexes/5.json').read_text())['objects'];h=index['minecraft/sounds.json']['hash'];official=json.loads((assets/'objects'/h[:2]/h).read_text())
p=root/'sounds.json';sounds=json.loads(p.read_text(encoding='utf-8'));mp=Path('src/main/resources/htgt6-external-sounds.json');manifest=json.loads(mp.read_text());entries={e['resource']:e for e in manifest['entries']};events=[]
for key,value in official.items():
 if not key.startswith(('entity.bee.','block.beehive.','block.honey_block.','item.honey_bottle.','entity.iron_golem.')):continue
 sounds[key]={'category':'neutral' if key.startswith('entity.') else 'block','sounds':value['sounds']};events.append(key)
 for sound in value['sounds']:
  name=sound if isinstance(sound,str) else sound['name'];resource='minecraft/sounds/'+name+'.ogg';entry=index[resource];local='assets/htgt6/sounds/'+name+'.ogg';h=entry['hash'];entries[local]={'resource':local,'sha1':h,'size':entry['size'],'officialResource':resource,'assetIndex':'5','url':'https://resources.download.minecraft.net/'+h[:2]+'/'+h}
manifest['entries']=sorted(entries.values(),key=lambda e:e['resource']);mp.write_text(json.dumps(manifest,indent=2)+'\n');p.write_text(json.dumps(sounds,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
for locale,legacy in [('en_us','en_US'),('ru_ru','ru_RU')]:
 if locale=='en_us':lang=json.loads(jar.read('assets/minecraft/lang/en_us.json'))
 else:
  h=index['minecraft/lang/'+locale+'.json']['hash'];lang=json.loads((assets/'objects'/h[:2]/h).read_text(encoding='utf-8'))
 additions={f'tile.backport_1_20.{n}.name':lang['block.minecraft.'+n] for n in ['beehive','bee_nest']};additions['item.backport_1_20.spawn_egg.bee.name']=lang['item.minecraft.bee_spawn_egg'];additions['entity.Minecraft_1.20.2.Bee.name']=lang['entity.minecraft.bee']
 for key,official_key in [('bee_our_guest','husbandry.safely_harvest_honey'),('total_beelocation','husbandry.silk_touch_nest'),('sticky_situation','adventure.honey_block_slide')]:
  for suffix,target in [('', 'title'),('.desc','description')]:additions['achievement.htgt6.backport.'+key+suffix]=lang['advancements.'+official_key+'.'+target]
 p=root/'lang'/Path(legacy+'.lang');lines=[l for l in p.read_text(encoding='utf-8').splitlines() if l.split('=',1)[0] not in additions];p.write_text('\n'.join(lines)+'\n'+'\n'.join(k+'='+v for k,v in additions.items())+'\n',encoding='utf-8')
print('Models:',len(seen),'cloud events:',len(events),'manifest entries:',len(entries))
