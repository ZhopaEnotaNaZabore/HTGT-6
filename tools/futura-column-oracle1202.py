"""Independent LegacyRandomSource/biased heights and expanded-layer truncation vectors."""
import json
from pathlib import Path
class Random:
 def __init__(self,s):self.s=(s^0x5deece66d)&((1<<48)-1)
 def bits(self,n):
  self.s=(self.s*0x5deece66d+11)&((1<<48)-1);return self.s>>(48-n)
 def integer(self,n):
  if n&(n-1)==0:return n*self.bits(31)>>31
  while True:
   b=self.bits(31);v=b%n
   if b-v+n-1<1<<31:return v
 def long(self):
  signed=lambda a:a-(1<<32) if a>>31 else a
  return (signed(self.bits(32))<<32)+signed(self.bits(32))
heights=[]
for lo,hi in [(1,3),(2,4)]:
 for seed in [0,1,2,3,42,12345,-1]:
  r=Random(seed);values=[lo+r.integer(r.integer(hi-lo+1)+1) for _ in range(16)]
  heights.append(dict(min=lo,max=hi,seed=seed,values=values,nextLong=r.long()))
trim=[]
for layers in [[1,2,3],[0,4,1],[3],[2,0,2]]:
 expanded=[i for i,n in enumerate(layers) for _ in range(n)]
 for n in range(len(expanded)+1):
  for tip in [False,True]:
   keep=(expanded[-n:] if n else []) if tip else expanded[:n]
   trim.append(dict(layers=layers,allowed=n,tip=tip,expected=[keep.count(i) for i in range(len(layers))]))
Path('src/main/resources/assets/htgt6/worldgen/futura_column_oracle_1202.json').write_text(json.dumps(dict(heights=heights,trim=trim),indent=2))
print(str(sum(len(x['values']) for x in heights))+' height draws; '+str(len(trim))+' truncation vectors')
