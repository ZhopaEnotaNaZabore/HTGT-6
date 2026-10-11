"""Ordinary, non-experimental 1.20.2 trader offers from the local named reference."""
import json, re
from pathlib import Path
source=Path('build/economy145/reference/VillagerTrades.named-reference.txt').read_text()
line=next(line for line in source.splitlines() if 'Int2ObjectMap<g[]> b =' in line)
result={}
for tier,body in re.findall(r'\(Object\)([12]), \(Object\)new g\[\]\{(.*?)\}',line):
    result[tier]=[]
    for item,cost,count,uses,xp in re.findall(r'new VillagerTrades\$ItemsForEmeralds\(Items\.(\w+), (\d+), (\d+), (\d+), (\d+)\)',body):
        result[tier].append(dict(item='minecraft:'+item.lower(),cost=int(cost),count=int(count),maxUses=int(uses),xp=int(xp)))
assert len(result)==2 and len(result['1'])==64 and len(result['2'])==6
Path('src/main/resources/assets/htgt6/data/wandering_trades_1202.json').write_text(json.dumps(result,indent=2)+'\n')
print({tier:len(offers) for tier,offers in result.items()})
