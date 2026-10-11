"""Finish existing PGM quest concepts and move their preserved identities to HV."""
import json,copy,datetime
from pathlib import Path
p=Path('run/saves/DEV 1/betterquesting/QuestDatabase.json');raw=p.read_bytes();d=json.loads(raw.decode('utf-8-sig'));q=d['questDatabase:9']
def ident(x):return {k:x[k] for k in ['questIDHigh:4','questIDLow:4']}
def pair(x):return tuple(x[k] for k in ['questIDHigh:4','questIDLow:4'])
def item(name,meta=0,count=1):return {'id:8':name,'Damage:2':meta,'Count:3':count,'OreDict:8':''}
def dust(meta,count=1):return item('gregtech:gt.meta.dust',meta,count)
def fluid(name):return {'id:8':'gregapi:gt.display.fluid','Damage:2':0,'Count:3':1,'OreDict:8':'','tag:10':{'f:8':name,'h:2':300}}
entries={
353:('Щелочной осадок платиновой группы',item('HTGT6:dustPGMAlkalineResidue',count=4),'В термосмесителе обработайте 4 смеси Os/Ir/Rh/Ru, 3 пыли гидроксида калия и 1000 л воды. Получите 4 щелочных осадка. HV, 480 EU/t, 60 секунд.',0,0),
278:('Раствор дисульфата калия',fluid('potassium_disuplhate'),'Растворите 13 пыли пиросульфата калия в 1000 л воды в термосмесителе. Выход: 1000 л раствора. HV, 480 EU/t, 30 секунд.',1,0),
515:('Разделение платинового остатка',item('HTGT6:dustOsIr',count=2),'Центрифугируйте 4 щелочных осадка с 1000 л раствора дисульфата калия. Выход: 6 сульфата родия, 2 смеси осмия-иридия и 1000 л соли рутения. HV, 480 EU/t, 120 секунд.',2,0),
408:('Сульфат родия',item('HTGT6:dustRhSo4',count=6),'Получите сульфат родия при разделении щелочного осадка в HV-центрифуге. Он нужен для восстановления родия натрием.',3,-1),
441:('Смесь осмия-иридия',item('HTGT6:dustOsIr',count=2),'Получите смесь осмия-иридия при разделении щелочного осадка в HV-центрифуге.',3,0),
445:('Соль рутения',fluid('ruthenium_salt'),'Побочная жидкость HV-центрифугирования: 1000 л соли рутения. Направьте её на восстановление натрием.',3,1),
484:('Отделение осмия от иридия',item('HTGT6:dustIrO',count=3),'В термосмесителе обработайте 2 смеси осмия-иридия, 4000 л соляной кислоты и 2000 л воды. Выход: 3 оксида иридия и 1000 л кислого раствора осмия. HV, 480 EU/t, 90 секунд.',4,0),
266:('Оксид иридия',item('HTGT6:dustIrO',count=3),'Соберите оксид иридия после кислотного разделения. Для восстановления нужен алюминий; рецепт работает на HV.',5,0),
322:('Кислый раствор осмия',fluid('acidic_osmium_sollution'),'Получите 1000 л кислого раствора осмия при отделении от иридия. Восстановление проводится водородом на HV.',5,1),
326:('Чистый иридий',dust(770,3),'Восстановите 9 оксида иридия четырьмя пылями алюминия в термосмесителе. Выход: 3 иридия и 10 глинозёма. HV, 480 EU/t, 120 секунд.',6,0),
232:('Чистый осмий',dust(760),'В термосмесителе восстановите 1000 л кислого раствора осмия 2000 л водорода. Выход: 1 осмий и 2000 л соляной кислоты. HV, 480 EU/t, 120 секунд.',6,1),
57:('Восстановление родия',dust(450),'Обработайте 6 сульфата родия двумя пылями натрия в термосмесителе. Выход: 1 родий и 7 сульфата натрия. HV, 480 EU/t, 90 секунд.',4,-1),
360:('Восстановление рутения',dust(440),'Обработайте 1000 л соли рутения двумя пылями натрия в термосмесителе. Выход: 1 рутений и 4 соли. HV, 480 EU/t, 90 секунд.',4,2)}
line=next(l for l in d['questLines:9'].values() if l['properties:10']['betterquesting:10']['name:8']=='Этаж 3 HV') if any(l['properties:10']['betterquesting:10']['name:8']=='Этаж 3 HV' for l in d['questLines:9'].values()) else d['questLines:9']['9:10']
assert 'HV' in line['properties:10']['betterquesting:10']['name:8']
origin=max(v['x:3']+v['sizeX:3'] for v in line['quests:9'].values())+96
moved={pair(q[str(k)+':10']) for k in entries}
for l in d['questLines:9'].values():l['quests:9']={k:v for k,v in l['quests:9'].items() if pair(v) not in moved}
nextkey=max(int(k.split(':')[0]) for k in line['quests:9'])+1
template=copy.deepcopy(q['233:10']['tasks:9']['0:10'])
for n,(name,out,desc,col,row) in entries.items():
 quest=q[str(n)+':10'];props=quest['properties:10']['betterquesting:10'];props.update({'name:8':name,'desc:8':desc,'icon:10':copy.deepcopy(out)})
 if out['id:8']=='gregapi:gt.display.fluid':
  task={'taskID:8':'bq_standard:fluid','index:3':0,'ignoreNBT:1':1,'consume:1':0,'autoConsume:1':0,'groupDetect:1':0,'requiredFluids:9':{'0:10':{'FluidName:8':out['tag:10']['f:8'],'Amount:3':1000}}}
 else:
  task=copy.deepcopy(template);task['requiredItems:9']={'0:10':copy.deepcopy(out)};task['consume:1']=0;task['autoConsume:1']=0
 quest['tasks:9']={'0:10':task}
 line['quests:9'][str(nextkey)+':10']={**ident(quest),'x:3':origin+col*72,'y:3':row*72,'sizeX:3':24,'sizeY:3':24};nextkey+=1
q['353:10']['preRequisites:9']={'0:10':ident(q['251:10']),'1:10':ident(q['500:10'])}
lookup={pair(v):v for v in q.values()}
def walk(node,active,done):
 assert node not in active,'cycle'
 if node in done:return
 assert node in lookup
 for parent in lookup[node]['preRequisites:9'].values():walk(pair(parent),active|{node},done)
 done.add(node)
for node in moved:walk(node,set(),set())
assert p.read_bytes()==raw
backup=Path('backups')/('pgm-hv-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S'))/'QuestDatabase.json';backup.parent.mkdir(parents=True);backup.write_bytes(raw)
p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Updated 13 existing quests, moved EV -> HV; identities retained; dependency graph PASS; backup',backup)
