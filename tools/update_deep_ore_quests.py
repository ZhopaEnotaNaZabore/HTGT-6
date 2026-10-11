"""Update the authored DEV guide quests without replacing UUIDs or player progress."""
import copy
import datetime
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DB = ROOT / 'run/saves/DEV 1/betterquesting/QuestDatabase.json'

def fluid(name):
    # The fluid-display compatibility hook resolves the registry name at runtime.
    return {'id:8':'gregapi:gt.display.fluid','Count:3':1,'Damage:2':0,
            'OreDict:8':'','tag:10':{'f:8':name,'h:2':300}}

def identity(quest):
    return quest['questIDHigh:4'], quest['questIDLow:4']

def main():
    raw = DB.read_bytes()
    data = json.loads(raw.decode('utf-8'))
    before = copy.deepcopy(data)
    line = next(v for v in data['questLines:9'].values()
                if v['properties:10']['betterquesting:10']['name:8'] == 'Deep Ore Processing')
    ids = {identity(v) for v in line['quests:9'].values()}
    quests = {q['properties:10']['betterquesting:10']['name:8']:q
              for q in data['questDatabase:9'].values() if identity(q) in ids}
    entries = [
        ('Халькопиритовый раствор','Халькопиритовый раствор',fluid('htgt6.chalcopyrite_solution'),
         'Начните с обычной дроблёной халькопиритовой руды. Во флотационной камере обработайте 32 единицы руды 10 000 мБ серной кислоты. Выход: 10 000 мБ халькопиритового раствора. Базовый режим: 20 секунд, 32 EU/t (LV). Для изготовления самой камеры нужен HV.\n\nОчищенная и центрифугированная руда не заменяют обычную дроблёную. Подайте кислоту через жидкостный вход и отведите раствор через выход. Следующий этап — электромагнитный процессор.\n\nЭто квест-путеводитель: показанные предметы служат подсказкой. После выполнения этапа отметьте флажок; жидкости и предметы не расходуются квестом.'),
        ('Деферритизация','Деферритизация',fluid('htgt6.enriched_copper_solution'),
         'Отделите железную фракцию в электромагнитном процессоре HiTech6. На одну операцию подайте 10 000 мБ халькопиритового раствора. Выход: 8 пыли железа и 10 000 мБ обогащённого медного раствора. Базовый режим: 20 секунд, 128 EU/t, минимум MV.\n\nУстановите модуль MV и подходящий трансформатор. Предметный реагент не нужен. Входной и выходной баки раздельные, по 16 000 мБ: трубы подают сырьё во входной бак и забирают продукт из выходного. Остаток сырья не смешивается с продуктом. При заполненном выходе обработка ждёт освобождения места. Состояние обоих баков показано в диагностике машины.\n\nЖелезо заберите из предметного выхода, а раствор направьте в промышленную центрифугу. После выполнения этапа отметьте флажок.'),
        ('отцентрифужено!','Отцентрифужено!',fluid('bluevitriol'),
         'Завершите переработку в промышленной центрифуге HiTech6. Вход: 10 000 мБ обогащённого медного раствора. Выход: 8 000 мБ медного купороса GT6 (Blue Vitriol) и 1 крошечная пыль шлама платиновой группы. Базовый режим: 30 секунд, 480 EU/t, минимум HV. Предметный реагент и Selector Tag не требуются.\n\nКупорос GT6 — жидкость: обеспечьте свободное место в выходном баке и подключите отвод. Это существующий материал GT6, его дальнейшие рецепты доступны в NEI. Шлам выходит отдельно как предмет, гарантированно по одной крошечной пыли за полный цикл. Девять крошечных пылей составляют одну обычную; накопленный шлам поступает в существующую цепочку химической переработки платиновой группы. Чистые платиновые металлы на этом этапе не выдаются.\n\nПосле отвода обоих продуктов отметьте флажок. Квест не забирает результаты переработки.')
    ]
    for old, name, icon, description in entries:
        quest = quests.get(old) or quests[name]
        props = quest['properties:10']['betterquesting:10']
        props.update({'name:8':name,'desc:8':description,'icon:10':icon})
        previews = {'0:10':copy.deepcopy(icon)}
        if name == 'Отцентрифужено!':
            previews['1:10'] = {'id:8':'gregtech:gt.meta.dustTiny','Count:3':1,'Damage:2':8103,'OreDict:8':''}
        quest['tasks:9'] = {
            '0:10':{'taskID:8':'bq_standard:optional_retrieval','index:3':0,
                    'partialMatch:1':1,'ignoreNBT:1':0,'consume:1':0,'autoConsume:1':0,
                    'groupDetect:1':0,'requiredItems:9':previews},
            '1:10':{'taskID:8':'bq_standard:checkbox','index:3':1}}
    camera = quests['Флотационная камера']['properties:10']['betterquesting:10']
    camera['desc:8'] = camera['desc:8'].replace(' И другие руды для дальнейшей переработки.','')
    line['properties:10']['betterquesting:10']['desc:8'] = (
        'Глубокая переработка халькопирита: флотация → отделение железа → обогащённый медный раствор → купорос GT6 и следовая фракция шлама платиновой группы.')
    lookup = {identity(q):q for q in data['questDatabase:9'].values()}
    assert len(lookup) == len(data['questDatabase:9'])
    for key, q in data['questDatabase:9'].items():
        old = before['questDatabase:9'][key]
        assert identity(q) == identity(old)
        assert q['preRequisites:9'] == old['preRequisites:9']
        assert q['rewards:9'] == old['rewards:9']
        if identity(q) not in ids: assert q == old
        for parent in q['preRequisites:9'].values(): assert identity(parent) in lookup
    for key, other in data['questLines:9'].items():
        assert other['quests:9'] == before['questLines:9'][key]['quests:9']
        if other is not line: assert other == before['questLines:9'][key]
    assert DB.read_bytes() == raw, 'Quest database changed during update'
    backup = ROOT/'backups'/('deep-ore-processing-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S'))
    backup.mkdir(parents=True)
    (backup/'QuestDatabase.json').write_bytes(raw)
    result = (json.dumps(data,ensure_ascii=False,indent=2)+'\n').encode('utf-8')
    DB.write_bytes(result)
    print('Updated four guides; UUIDs, dependencies, rewards, layout and unrelated quests preserved.')
    print('Backup:',backup)
    print('SHA256:',hashlib.sha256(result).hexdigest())

if __name__ == '__main__': main()
