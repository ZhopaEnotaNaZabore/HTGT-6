"""Add GT6 energy quest chains to DEV 1, preserving existing quest identities.
Run with --apply to back up and atomically replace the database; default is preview.
"""
import copy, hashlib, json, re, uuid, argparse, datetime
from pathlib import Path
DB=Path("run/saves/DEV 1/betterquesting/QuestDatabase.json")
TIERS=['ULV','LV','MV','HV','EV','IV','LuV','ZPM','UV','PUV']
PLATES=[8610,8651,8630,220,8635,770,8607,780,8629,8683]
# These branches only reach IV. Higher material variants are grouped explicitly,
# never by metadata order or nominal heat output.
GROUPS=[(0,[2,0,1]),(1,[3]),(2,[4,5]),(3,[6,11]),(4,[7,8]),(5,[9,10])]
FAMILIES=[]
EARLY_BURNERS={'solid','solid_dense','liquid','liquid_dense','gas','gas_dense','fluidbed','fluidbed_dense'}
def family(key,name,groups,intro,reuse=None):
    FAMILIES.append(dict(key=key,name=name,groups=groups,intro=intro,reuse=reuse or {}))
def variants(base,allowed):
    return [(t,[base+i for i in ids if i in allowed]) for t,ids in GROUPS if any(i in allowed for i in ids)]
HEAT='Топка превращает топливо в тепловую энергию HU. Направьте тепловой выход к котлу или машине, принимающей HU; сторона выхода и мощность указаны в подсказке блока. Для электричества понадобится отдельная цепочка преобразования энергии.'
for base,key,name in [(1100,'solid','Твёрдотопливные топки'),(1150,'solid_dense','Плотные твёрдотопливные топки'),(1400,'liquid','Жидкостные топки'),(1450,'liquid_dense','Плотные жидкостные топки'),(1600,'gas','Газовые топки'),(1650,'gas_dense','Плотные газовые топки'),(9000,'fluidbed','Топки кипящего слоя'),(9050,'fluidbed_dense','Плотные топки кипящего слоя')]:
    allowed=range(2,12) if base in [1400,1450,1600,1650] else range(12)
    usage=('Загрузите твёрдое топливо и обеспечьте доступ к рабочим сторонам топки.' if base<1200 else 'Подайте подходящее топливо трубой в топливный вход. Допустимые виды топлива проверьте в NEI.')
    if base>=9000: usage='Подайте топливо из карты Fluidized Bed; подходящие виды и их расход можно посмотреть в NEI.'
    family(key,name,variants(base,allowed),HEAT+'\n\n'+usage,{0:435} if base==1600 else ({5:456} if base==1650 else {}))
for base,key,name in [(1200,'boiler','Паровые котлы'),(1250,'boiler_strong','Усиленные паровые котлы')]:
    groups=[(0,[base,base+1,base+2]),(1,[base+3]),(2,[base+4,base+5]),(3,[base+6,base+9]),(4,[base+7,base+8])]
    family(key,name,groups,'Котёл принимает тепловую энергию HU и воду, а выдаёт пар. Установите топку у теплового входа, обеспечьте постоянную подачу воды и свободный отвод пара к двигателю или турбине. Следите за состоянием котла и указаниями в подсказке; не допускайте работы нагретого котла без воды.')
for base,key,name in [(1300,'steam_engine','Паровые двигатели'),(1350,'steam_engine_strong','Усиленные паровые двигатели')]:
    groups=[(0,[base+2,base,base+1,base+9]),(1,[base+3,base+10]),(2,[base+4,base+5,base+11]),(3,[base+6]),(4,[base+7,base+8])]
    family(key,name,groups,'Паровой двигатель превращает пар в кинетическую энергию KU. Подведите пар от котла и направьте рабочую сторону к машине, принимающей KU. Проверьте допустимую мощность и направление выхода в подсказке. Это привод механизмов; для EU требуется другое преобразование энергии.',{0:145 if base==1300 else 146})
family('diesel','Дизельные двигатели',[(0,[9147]),(1,[9149]),(2,[9148]),(3,[9197]),(4,[9198,9199]),(5,[9120])], 'Дизельный двигатель сжигает жидкое топливо и выдаёт вращательную энергию RU. Подайте топливо из дизельной карты GT6 через трубу и соедините выход с потребителем RU. Чтобы получить EU, подключите подходящее электрическое динамо; следите за мощностью и направлением передачи.')
family('gas_turbine','Газовые турбины',[(0,[9160]),(1,[9162]),(2,[9161]),(3,[9163]),(4,[9164,9165]),(5,[9166])], 'Газовая турбина превращает подходящее газовое топливо в вращательную энергию RU. Подайте газ трубой, выбрав топливо по карте GT6 в NEI. Соедините выход с машиной на RU либо с электрическим динамо для получения EU. Согласуйте допустимую мощность обоих устройств.',{0:32,2:327})
family('steam_turbine','Паровые турбины',[(0,[1512]),(2,[1522,1527,1528,1529]),(3,[1530,1531,1535,1538]),(4,[1540,1545,1548])], 'Паровая турбина превращает пар в вращательную энергию RU. Подведите пар от котла и соедините выход RU с механизмом или электрическим динамо. Согласуйте расход пара, мощность и стороны соединения по подсказкам устройств. Латунные и инварные варианты того же начального тира показаны отдельными необязательными заданиями.',{0:135,2:361,3:345})
family('dynamo','Электрические динамо',[(t,[10110+t]) for t in range(1,6)], 'Электрическое динамо преобразует вращательную энергию RU в электричество EU. Соедините вход с турбиной или двигателем на RU, а электрический выход — с кабелем и потребителем нужного напряжения. Само динамо топливо не сжигает; его должен вращать внешний привод.',{1:200,2:214})
family('flux_dynamo','Флаксовые динамо',[(t,[11110+t]) for t in range(1,6)], 'Флаксовое динамо преобразует вращательную энергию RU в RF. Подведите вращение от совместимого двигателя или турбины и подключите выход к потребителю RF. Проверьте стороны подключения и допустимую мощность в подсказке.')
for base,key,name in [(9100,'exchanger','Теплообменники'),(9150,'exchanger_dense','Плотные теплообменники')]:
    family(key,name,[(1,[base+3]),(4,[base+7,base+8]),(5,[base+9])], 'Теплообменник извлекает тепло из подходящей горячей жидкости и передаёт HU. Подайте жидкость из карты Heat Exchanger, обеспечьте отвод охлаждённого продукта и направьте тепловой выход к потребителю HU. Виды жидкостей и их расход проверьте в NEI.')

def stack(meta,count=1,name='gregtech:gt.multitileentity'):
    return {'id:8':name,'Count:3':count,'Damage:2':meta,'OreDict:8':''}
def identity(q):return {k:q[k] for k in ['questIDHigh:4','questIDLow:4']}
def pair(q):return (q['questIDHigh:4'],q['questIDLow:4'])
def quest_uuid(key):
    n=uuid.uuid5(uuid.NAMESPACE_URL,'htgt6:energy-control:'+key).int
    def signed(x):return x-(1<<64) if x>=1<<63 else x
    return {'questIDHigh:4':signed(n>>64),'questIDLow:4':signed(n&((1<<64)-1))}
def main():
    apply=argparse.ArgumentParser();apply.add_argument('--apply',action='store_true');args=apply.parse_args()
    raw=DB.read_bytes();data=json.loads(raw.decode('utf-8-sig'));before=copy.deepcopy(data)
    quests=data['questDatabase:9'];line=data['questLines:9']['23:10'];line['properties:10']['betterquesting:10']['name:8']='EnergyControl'
    root=quests['124:10'];assert pair(root)==(0,71)
    template=copy.deepcopy(quests['0:10']);retrieval=copy.deepcopy(template['tasks:9']['0:10'])
    ids={pair(v):k for k,v in quests.items()};next_id=max(int(k.split(':')[0]) for k in quests)+1
    records=[];touched=set();placements={pair(v):k for k,v in line['quests:9'].items()}
    next_place=max(int(k.split(':')[0]) for k in line['quests:9'])+1
    def put(key,name,tier,items,parent,desc,opening,x,y,reuse=None):
        nonlocal next_id,next_place
        ident=quest_uuid(key)
        qkey=f'{reuse}:10' if reuse is not None else ids.get(pair(ident))
        if qkey is None:qkey=f'{next_id}:10';next_id+=1
        old=quests.get(qkey)
        q=copy.deepcopy(template);q.update(identity(old) if old else ident)
        prop=q['properties:10']['betterquesting:10'];prop.update({'name:8':name,'desc:8':desc,'icon:10':stack(items[0]),'isMain:1':0,'taskLogic:8':'AND','lockedProgress:1':1})
        q['preRequisites:9']={'0:10':identity(parent)}
        tasks={}
        for i,item in enumerate(items):
            task=copy.deepcopy(retrieval);task.update({'index:3':i,'requiredItems:9':{'0:10':stack(item)},'taskID:8':'bq_standard:retrieval' if (opening and i==0) or len(items)==1 else 'bq_standard:optional_retrieval'})
            if not opening and len(items)>1:task['taskID:8']='bq_standard:optional_retrieval'
            tasks[f'{i}:10']=task
        if not opening and len(items)>1:tasks[f'{len(tasks)}:10']={'index:3':len(tasks),'taskID:8':'bq_standard:checkbox'}
        q['tasks:9']=tasks
        q['rewards:9']={'0:10':{'rewardID:8':'bq_standard:item','index:3':0,'ignoreDisabled:1':0,'rewards:9':{'0:10':stack(PLATES[tier],10,'gregtech:gt.meta.plate'),'1:10':stack(0,10,'HTGT6:BlacksmithCoin')}}}
        quests[qkey]=q;touched.add(qkey)
        pkey=placements.get(pair(q))
        if pkey is None:pkey=f'{next_place}:10';next_place+=1
        line['quests:9'][pkey]=dict(identity(q),**{'sizeX:3':24,'sizeY:3':24,'x:3':x,'y:3':y})
        records.append(dict(key=key,database_key=qkey,name=name,tier=TIERS[tier],items=items,opening=opening,parent=identity(parent),x=x,y=y))
        return q
    for row,f in enumerate(FAMILIES):
        parent=quests['97:10'] if f['key'] in EARLY_BURNERS else root
        for col,(tier,items) in enumerate(f['groups']):
            opening=col==0
            desc=f['intro'] if opening else 'Это '+TIERS[tier]+(' версии.' if len(items)>1 else ' версия.')
            parent=put(f['key']+'/'+TIERS[tier],f['name']+' — '+TIERS[tier],tier,items,parent,desc,opening,120+tier*96,144+row*96,f['reuse'].get(tier))
    # Preserve legacy quest UUIDs as small optional variant demonstrations.
    for old,item,tier,parentkey,x,y in [(202,1515,0,135,120,144+14*96+36),(203,1518,0,135,168,144+14*96+36),(37,1525,2,361,312,144+14*96+36)]:
        q=put('steam_turbine/variant/'+str(item),'Паровые турбины — '+TIERS[tier]+' (вариант)',tier,[item],quests[f'{parentkey}:10'],'Это '+TIERS[tier]+' версия.',False,x,y,old)
        q['tasks:9']['0:10']['taskID:8']='bq_standard:optional_retrieval'
        q['tasks:9']['1:10']={'index:3':1,'taskID:8':'bq_standard:checkbox'}
    # Validate schema, existing references, safe IDs, rewards, graph and layout.
    all_ids={pair(q) for q in quests.values()};assert len(all_ids)==len(quests)
    loader=Path('tmp/circuit-machine-audit/gregtech/loaders/b/Loader_MultiTileEntities.java').read_text()
    registered={int(i) for i in re.findall(r'aRegistry.add\(.*?, "[^"]+", (\d+),',loader)}
    for r in records:
        assert set(r['items'])<=registered,r
        q=quests[r['database_key']];assert pair(r['parent']) in all_ids
        if r['opening']:
            expected=quests['97:10'] if r['key'].split('/')[0] in EARLY_BURNERS else root
            assert pair(r['parent'])==pair(expected)
        rewards=q['rewards:9']['0:10']['rewards:9'];assert [v['Count:3'] for v in rewards.values()]==[10,10]
    for k,q in before['questDatabase:9'].items():
        if k not in touched:assert quests[k]==q
    rectangles=[]
    for node in line['quests:9'].values():
        x,y=node['x:3'],node['y:3'];w,h=node['sizeX:3'],node['sizeY:3']
        for a,b,c,d in rectangles:assert x+w<=a or a+c<=x or y+h<=b or b+d<=y,'Quest layout overlap'
        rectangles.append((x,y,w,h))
    graph={pair(q):[pair(v) for v in q.get('preRequisites:9',{}).values()] for q in quests.values()}
    def walk(node,active,done):
        if node in done:return
        assert node not in active,'Cyclic prerequisite'
        assert node in graph,'Dangling prerequisite'
        for parent in graph[node]:walk(parent,active|{node},done)
        done.add(node)
    for r in records:walk(pair(quests[r['database_key']]),set(),set())
    report={'quests_before' :len(before['questDatabase:9']),'quests_after':len(quests),'branches':len(FAMILIES),'updated':sum(k in before['questDatabase:9'] for k in touched),'records':records}
    Path('docs/audits/energy-control-quests.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    if args.apply:
        assert DB.read_bytes()==raw,'Database changed during edit'
        backup=Path('backups')/('energy-control-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S'))/'QuestDatabase.json';backup.parent.mkdir(parents=True);backup.write_bytes(raw)
        temp=DB.with_suffix('.json.energy-control.tmp');temp.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');temp.replace(DB)
        print('Backup:',backup)
    print(json.dumps({k:v for k,v in report.items() if k!='records'}))
if __name__=='__main__':main()
