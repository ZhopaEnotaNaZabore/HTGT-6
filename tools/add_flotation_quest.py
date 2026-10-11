"""Add one DEV flotation quest, preserving existing UUIDs and unrelated quest content."""
import argparse
import copy
import datetime
import hashlib
import json
import uuid
from fractions import Fraction as F
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DB = ROOT / 'run/saves/DEV 1/betterquesting/QuestDatabase.json'
TARGET = 'HTGT6:flotation_chamber'

def identity(value):
    return {k: value[k] for k in ('questIDHigh:4', 'questIDLow:4')}

def pair(value):
    return tuple(identity(value).values())

def stack(name, count=1, damage=0):
    return {'id:8': name, 'Count:3': count, 'Damage:2': damage, 'OreDict:8': ''}

def requires(quest, item):
    return any(s.get('id:8') == item
               for task in quest.get('tasks:9', {}).values()
               if task.get('taskID:8') == 'bq_standard:retrieval'
               for s in task.get('requiredItems:9', {}).values())

def cost_ledger():
    # Units of metal, using the authored crafting inputs. A FrameBox costs
    # eight half-unit rods, NOT the two-unit recycling value of GT6 OP.frameGt.
    # Four ribs are one batch: one extra casing, four long rods, eight bolts.
    rows = [
        ('9 каркасов из 8 стержней', 8636, F(9 * 8, 2)),
        ('9 корпусов: по 32 болта', 8636, F(9 * 32, 8)),
        ('9 корпусов: по 6 пластин', 8665, F(9 * 6)),
        ('4 ребра: длинные стержни и болты', 8636, F(4) + F(8, 8)),
        ('4 шестерни контроллера', 8636, F(4 * 4)),
        ('Корпус HV: casingMachine и двойная пластина', 8636, F(8 + 2)),
        ('Корпус HV: два кабеля 1x', 8664, F(2, 2)),
        ('6 моторов, включая 2 в помпах: гнутые пластины', 8636, F(6 * 2)),
        ('6 моторов: четыре стальных провода 4x', 8630, F(6 * 4 * 2)),
        ('6 моторов: два кабеля кантала 2x', 8664, F(6 * 2)),
        ('6 моторов: стержень магнитного неодима', 600, F(6, 2)),
        ('2 помпы: роторы и винты', 8636, F(2) * (F(17, 4) + F(1, 9))),
        ('2 помпы: кабель кантала 1x', 8664, F(2, 2)),
        ('8 двойных проводов контроллера', 8664, F(8)),
    ]
    totals = {}
    for _, material, amount in rows:
        totals[material] = totals.get(material, F(0)) + amount
    # Reward only complete plates: fractions never round upwards.
    refunds = {material: int(amount / 4) for material, amount in totals.items() if amount >= 4}
    assert refunds == {8636: 30, 8665: 13, 8664: 5, 8630: 12}
    return rows, totals, refunds

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--apply', action='store_true')
    args = parser.parse_args()
    raw = DB.read_bytes()
    data = json.loads(raw.decode('utf-8-sig'))
    before = copy.deepcopy(data)
    quests = data['questDatabase:9']
    parents = [q for q in quests.values() if requires(q, 'HTGT6:assembler')]
    assert len(parents) == 1, 'Assembler prerequisite is ambiguous'
    parent = parents[0]
    lines = [(k, v) for k, v in data['questLines:9'].items()
             if v['properties:10']['betterquesting:10']['name:8'].replace(' ', '').lower() == 'deeporeprocessing']
    assert len(lines) == 1
    line_key, line = lines[0]
    existing = [(k, q) for k, q in quests.items() if requires(q, TARGET)]
    assert len(existing) <= 1, 'Duplicate flotation quests need manual review'
    if existing:
        key, old = existing[0]
        ident = identity(old)
    else:
        key = str(max(int(k.split(':')[0]) for k in quests) + 1) + ':10'
        value = uuid.uuid5(uuid.NAMESPACE_URL, 'htgt6:deep-ore-processing:flotation-chamber').int
        signed = lambda n: n - (1 << 64) if n >= (1 << 63) else n
        ident = {'questIDHigh:4': signed(value >> 64), 'questIDLow:4': signed(value & ((1 << 64) - 1))}
        assert pair(ident) not in {pair(q) for q in quests.values()}

    rows, totals, refunds = cost_ledger()
    quest = copy.deepcopy(parent)
    quest.update(ident)
    quest['preRequisites:9'] = {'0:10': identity(parent)}
    description = '''Флотационная камера — начало глубокой переработки руды. Она превращает дроблёный халькопирит с серной кислотой в раствор халькопирита для дальнейшей химической обработки.

Создание контроллера
Улучшите HiTech6 Assembler до HV, установив соответствующие тировый модуль и трансформатор. Разложите ингредиенты строго по сетке рецепта в NEI: 8 корпусов флотационной камеры, 4 ребра, корпус HV, 4 схемы HV, 4 мотора HV, 2 помпы HV, 8 двойных проводов кантала и 4 шестерни из нержавеющей стали. Поставьте Selector Tag 5 — он не расходуется. Сборка занимает 60 секунд при 480 EU/t.

Один корпус камеры собирается из каркаса нержавеющей стали, 6 пластин магналия и 32 нержавеющих болтов с Selector Tag 5. Четыре ребра получаются из корпуса, 4 длинных нержавеющих стержней и 8 болтов с тем же селектором. Каркас изготовьте из 8 стержней с ключом в центре верстака либо в ассемблере с Selector Tag 4.

Постройка и работа
Одного контроллера недостаточно: соберите мультиблок шириной 13, глубиной 11 и высотой 7 блоков по схеме структуры в NEI. Используйте корпуса и рёбра флотационной камеры, укреплённое стекло и нужные шлюзы. Подведите энергию к энергетическому входу, дроблёную руду — к предметному, кислоту — к жидкостному. Обеспечьте свободный отвод раствора через жидкостный выход.

Текущий рецепт: 32 дроблёных халькопирита + 10 000 л серной кислоты → 10 000 л раствора халькопирита за 20 секунд при 32 EU/t (LV). Рудные блоки и очищенная дроблёная руда не заменяют обычный дроблёный халькопирит. HV требуется для создания контроллера, а рабочее питание определяется рецептом переработки.

Получите один контроллер флотационной камеры. Задание проверяет наличие предмета и не забирает его.

Награда: 10 технических монет и возврат 25% металлических материалов контроллера, включая металл его механических компонентов, с округлением вниз до целых пластин. Схемы, неметаллические материалы и блоки внешней конструкции в возврат не входят.'''
    prop = quest['properties:10']['betterquesting:10']
    prop.update({'name:8': 'Флотационная камера', 'desc:8': description,
                 'icon:10': stack(TARGET), 'lockedProgress:1': 1, 'isMain:1': 0,
                 'repeatTime:3': -1, 'autoClaim:1': 0, 'taskLogic:8': 'AND', 'questLogic:8': 'AND'})
    task = copy.deepcopy(next(t for t in parent['tasks:9'].values() if t['taskID:8'] == 'bq_standard:retrieval'))
    task.update({'index:3': 0, 'requiredItems:9': {'0:10': stack(TARGET)}, 'consume:1': 0,
                 'autoConsume:1': 0, 'ignoreNBT:1': 1, 'partialMatch:1': 0})
    quest['tasks:9'] = {'0:10': task}
    rewards = [stack('HTGT6:TechCoin', 10)]
    rewards += [stack('gregtech:gt.meta.plate', refunds[m], m) for m in (8636, 8665, 8630, 8664)]
    quest['rewards:9'] = {'0:10': {'rewardID:8': 'bq_standard:item', 'index:3': 0,
        'ignoreDisabled:1': 0, 'rewards:9': {str(i) + ':10': s for i, s in enumerate(rewards)}}}
    quests[key] = quest
    placements = line['quests:9']
    if not any(pair(p) == pair(quest) for p in placements.values()):
        pkey = str(max((int(k.split(':')[0]) for k in placements), default=-1) + 1) + ':10'
        x = max((p['x:3'] + p['sizeX:3'] for p in placements.values()), default=0) + 48
        placements[pkey] = dict(ident, **{'x:3': x, 'y:3': 48, 'sizeX:3': 24, 'sizeY:3': 24})

    # Only one quest and its line membership may change. Resolve real UUIDs, not list indices.
    for k, q in before['questDatabase:9'].items():
        if k != key: assert quests[k] == q
    for k, other in before['questLines:9'].items():
        if k != line_key: assert data['questLines:9'][k] == other
    assert len({pair(q) for q in quests.values()}) == len(quests)
    graph = {pair(q): [pair(p) for p in q.get('preRequisites:9', {}).values()] for q in quests.values()}
    def walk(node, active):
        assert node in graph and node not in active, 'Broken prerequisite or cycle'
        for p in graph[node]: walk(p, active | {node})
    walk(pair(quest), set())
    from inspect_world_nbt import load
    level = next(iter(load(DB.parents[1] / 'level.dat').values()))
    registry = {e['K'][1:] for e in level['FML']['ItemData'] if e['K'].startswith('\x02')}
    assert all(s['id:8'] in registry and 0 < s['Count:3'] <= 64 for s in rewards + [stack(TARGET)])
    report = {'quest_key': key, 'quest_uuid': ident, 'prerequisite': identity(parent),
        'prerequisite_name': parent['properties:10']['betterquesting:10']['name:8'],
        'line_key': line_key, 'coins': {'item': 'HTGT6:TechCoin', 'count': 10,
            'basis': 'Existing assembler and SMC technical-machine retrieval quests'},
        'metal_costs': [{'reason': label, 'material_id': mat, 'ingot_units': str(amount)} for label, mat, amount in rows],
        'totals': {str(m): str(v) for m, v in totals.items()}, 'plate_refund': refunds,
        'basis': 'One controller. Mechanical component metals expanded; circuits and non-metals excluded. floor(metal_units / 4).',
        'sources': ['MultiblockAssemblerRecipes.java', 'CraftingFromGregtechhv.java', 'GT6 OP material amounts',
                    'GT6 Loader_MultiTileEntities and MultiTileEntityWireElectric from installed 6.15.08 JAR'],
        'before_sha256': hashlib.sha256(raw).hexdigest()}
    if args.apply and data != before:
        assert DB.read_bytes() == raw, 'Database changed during edit'
        backup = ROOT / 'backups' / ('flotation-quest-' + datetime.datetime.now().strftime('%Y%m%d-%H%M%S')) / DB.name
        backup.parent.mkdir(parents=True, exist_ok=True)
        backup.write_bytes(raw)
        newline = '\r\n' if b'\r\n' in raw else '\n'
        result = json.dumps(data, ensure_ascii=False, indent='\t') + ('\n' if raw.endswith(b'\n') else '')
        temporary = DB.with_suffix('.json.flotation.tmp')
        temporary.write_bytes(result.replace('\n', newline).encode('utf-8'))
        assert json.loads(temporary.read_text(encoding='utf-8')) == data
        temporary.replace(DB)
        report['backup'] = str(backup.relative_to(ROOT))
        report['after_sha256'] = hashlib.sha256(DB.read_bytes()).hexdigest()
        audit = ROOT / 'docs/audits/flotation-quest.json'
        audit.parent.mkdir(parents=True, exist_ok=True)
        audit.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'changed': data != before, 'applied': args.apply, 'quest_key': key,
                      'plate_refund': refunds, 'backup': report.get('backup')}, ensure_ascii=True))

if __name__ == '__main__':
    main()
