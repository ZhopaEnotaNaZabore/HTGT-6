"""Extract ordinary (non-experimental) 1.20.2 VillagerTrades from named local CFR reference.
Usage: python tools/extract_villager_trades146.py [named-reference] [output.json]
"""
import json, pathlib, re, sys

def split_args(text):
    result=[]; depth=0; start=0
    for i,c in enumerate(text):
        if c in "({[": depth+=1
        elif c in ")}]": depth-=1
        elif c=="," and depth==0:
            result.append(text[start:i].strip()); start=i+1
    result.append(text[start:].strip())
    return result

def extract(text):
    result={}
    for line in text.splitlines():
        profession=re.match(r"\s*\$\$0.put\(VillagerProfession\.(\w+),",line)
        if not profession: continue
        tiers={}
        for match in re.finditer(r"\(Object\)([1-5]), \(Object\)new g\[\]\{",line):
            start=match.end(); depth=1; i=start
            while depth:
                if line[i]=="{": depth+=1
                elif line[i]=="}": depth-=1
                i+=1
            tiers[match[1]]=[]
            for expr in split_args(line[start:i-1]):
                factory=re.fullmatch(r"new VillagerTrades\$(\w+)\((.*)\)",expr)
                assert factory,expr
                tiers[match[1]].append({"type":factory[1],"args":split_args(factory[2])})
        result[profession[1].lower()]=tiers
    assert len(result)==13 and sum(map(len,result.values()))==65
    return result

if __name__=="__main__":
    source=pathlib.Path(sys.argv[1] if len(sys.argv)>1 else "build/economy145/reference/VillagerTrades.named-reference.txt")
    target=pathlib.Path(sys.argv[2] if len(sys.argv)>2 else "src/main/resources/assets/htgt6/data/villager_trades_1202.json")
    target.write_text(json.dumps(extract(source.read_text(encoding="utf-8")),indent=2)+"\n",encoding="utf-8")
